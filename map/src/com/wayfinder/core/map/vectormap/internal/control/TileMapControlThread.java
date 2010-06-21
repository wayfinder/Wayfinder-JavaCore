/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.map.vectormap.internal.control;

import java.util.Hashtable;

import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.map.MapDownloadListener;
import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.map.vectormap.internal.VectorMapOptimizationFilter;
import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapLayerInfo;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.util.UtilFactory;

/**
 * This is the class that updates all TileMaps that are inside the 
 * current bounding box. 
 * 
 * The class contains one thread that are used for:
 *  - Updating and requesting new TileMapParams to the server.
 *  - Removing unseen tiles.
 *  - Handle extracted TileMaps and send them to the extraction listener. 
 * 
 *
 */
public class TileMapControlThread implements Runnable {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapControlThread.class);
    
    // TileMapFormatDesc
    volatile private TileMapFormatDesc tmfd      = null;
    private TileMapFormatDesc tmfdDay   = null;
    private TileMapFormatDesc tmfdNight = null;
    private long tmfdDayCrc     = -1;
    private long tmfdNightCrc   = -1;
     
    private static boolean isRunning = true;
    
    private static Object TILEMAP_MONITOR = new Object();
    
    private TileMapLoader iMapLoader;
    private TileMapExtractionThread iTileMapExtraction;
    private TileMapExtractionListener iExtractionListener;      
    private TileMapHandler iTileMapHandler;
    
    private LinkedList iNewExtractedTileMaps;
    private LinkedList iExtractedTileMaps;
    
    // Text language (default English)
    private int iTextLanguage = 1;  
    
    private boolean isVisible = false;
    
    // Sets to true if we want to update the overview maps. 
    private boolean iUpdateOverviewMaps = false;
    // Sets to true if we want to request string as soon as possible for
    // all visible layers
    private boolean alwaysFetchStrings = false;
    
    private boolean m_IsInNightMode = false;
    
    public TileMapControlThread(PersistenceLayer perLayer, UtilFactory utilFactory) {
        iTileMapExtraction  = new TileMapExtractionThread(this, utilFactory);
        iMapLoader          = new TileMapLoader(iTileMapExtraction, this, perLayer, utilFactory);
        iTileMapHandler     = new TileMapHandler(this,iMapLoader);
        
        // Set a invalid init zoom level. 
        iNewZoomLevel[0] = -1;
        
        setStringTable();
        setNbrStrings();
    }
    
    public void init(ConcurrencyLayer currLayer, boolean supportPolygons, int language) {
        
        iExtractedTileMaps         = new LinkedList();
        iNewExtractedTileMaps   = new LinkedList();
        iTextLanguage = language;
        
        // Start the main thread 
        Thread t = currLayer.startNewDaemonThread(this, "TMControl");
        t.setPriority(Thread.NORM_PRIORITY);
        
        iTileMapExtraction.init(currLayer, supportPolygons);
    }
    
    public void clearMemCache() {
        iMapLoader.getMemCache().clearMemCache();
    }
    
    /**
     * The TileMapExtractionListener will be called when a new TileMap has been extracted 
     * and when a tile has been removed from the loaded wrapper array.
     * 
     * The class that implements the TileMapExtractionListener are responsible for 
     * handling the extracted tileMaps. The callback must return quickly. 
     * 
     * @param aListener, can be null
     * @see TileMapExtractionListener
     */
    public void setTileMapExtractionListener(TileMapExtractionListener aListener) {
        iExtractionListener = aListener;
        iTileMapHandler.setTileMapExtractionListener(iExtractionListener);
    }
    
    /**
     * The aTilemapRequester will be used for requesting new TileMaps form the server. 
     * 
     * @param aTileMapRequester, can't be null. 
     */
    public void addTileMapRequester(SharedSystems systems, InternalNetworkInterface aNetworkInterface) {
        iMapLoader.setTileMapRequester(systems, aNetworkInterface);
    }
    
    /**
     * Add TileMapDownloadListner that is used it notify that the map component
     * communicate with the server. 
     * 
     * @param aListener, can be null
     */
    public void setTileMapDownloadListener(MapDownloadListener aListener) {
        iMapLoader.setMapDownloadListener(aListener);
    }
    
    private LinkedList m_CacheConfig;
    
    /** 
     * Start the map cache (memory cache and persistent cache)
     * 
     * @param aUseFileCache, true if we want to use the file cache
     */
    public void startCache(LinkedList cacheConfig) {
            m_CacheConfig = cacheConfig;
            notifyUpdate();
    }
    
    private void notifyUpdate() {
        synchronized(TILEMAP_MONITOR) {
            iNewUpdateHasBeenRequested = true;
            TILEMAP_MONITOR.notifyAll();
        }
    }
    
    /**
     * @param layerID, the layerID
     * 
     * @return true if the tiles in the layer specified by the parameter should
     * be saved in the cache. 
     */
    boolean shouldBeSavedInCache(int layerID) {
        if(tmfd != null)
            return (tmfd.getUpdateTimeForLayer(layerID) == 0);
        
        return true;
    }
    
    /**
     * Close the map component, should be called when we exit the application to
     * save the cache index to the persistent storage. If this method isn't called then
     * the cache will be invalid the next time we start the client. 
     */
    public void closeMapComponent() {
        try {
            iMapLoader.closeMapCache();
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("TileMapControlThread.closeMapComponent()", e);
            }
        }
    }
    
    /** 
     * @param aVisible, true if the map component should be visible. 
     * No TileMaps will be downloaded if it's set to false. 
     */
    public void setVisible(boolean aVisible) {
        isVisible = aVisible;
        if(iMapLoader.getCache() != null)
            iMapLoader.getCache().setVisible(isVisible);
        if(isVisible) {
            // Reset the exponential backoff time when we enter the mapview
            iMapLoader.getTileMapNetworkHandler().resetExponentialBackoff();
        }
    }
    
    /**
     * Reset the exponential backoff time for the TileMapRequester. 
     */
    public void resetExponentialBackoff() {
        if(!iMapLoader.isOffline())
            iMapLoader.getTileMapNetworkHandler().resetExponentialBackoff();
    }
    
    /**
     * @return true if the user are in the mapview
     */
    boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Clean up the memory
     */
    public void purgeData() {
    	
        resetAllLayers();
        iMapLoader.resetRequestedParams();
        
        notifyUpdate();
    }
    
    /**
     * @return the current TileMapFormatDesc
     */
    public TileMapFormatDesc getTileMapFormatDesc() {
        return tmfd;
    }
    
    /**
     * @return the TileMapLoader
     */
    public TileMapLoader getTileMapLoader() {
        return iMapLoader;
    }
    
    /**
     * @return a TileCategory array for the categories loaded in the TMFD
     */
    public PoiCategory[] getPoiCategories(){
        if(tmfd != null)
            return tmfd.getCategories();
        
        return null;
    }
    
    public void setPoiCategories(PoiCategory []poiCat) {
        if(tmfd != null) {
            MapTask task = new MapTask(this);
            task.setPoiCategories(poiCat, tmfd);
        }
    }
    
    private PoiCategory []m_PoiCategories = null;
    
    void internalSetPoiCategories(PoiCategory []poiCat) {
        
        if(poiCat != null) {
            m_PoiCategories = new PoiCategory[poiCat.length];
            for(int i=0; i<m_PoiCategories.length; i++) {
                m_PoiCategories[i] = new PoiCategory(poiCat[i].getName(), poiCat[i].isEnable());
                tmfd.setCategoryEnabled(i, poiCat[i].isEnable());                                
            }
        } else {
            m_PoiCategories = null;
        }
    }
    
    /**
     * Enables/Disables a category in the TMFD
     *
     * @param id layer ID
     * @param enabled enable/disable the category
     * @return True if category was visible
     */
    public boolean setCategoryEnabled(int id,boolean enabled){
        return tmfd.setCategoryEnabled(id,enabled);
    }
    
    /**
     * Returns an array containing TileMapLayerInfo-objects
     * for the currently used layers. Invisible layers are returned as NULL.
     *
     * @return an array containing TileMapLayerInfo-objects
     */
    public TileMapLayerInfo[] getLayerList(){
        TileMapLayerInfo[] visibleLayers = new TileMapLayerInfo[tmfd.getNumberOfLayers()];
        for (int i = 0; i < visibleLayers.length; i++) {
            if(tmfd.visibleLayer(tmfd.getLayerIDFromLayerNbr(i))){
                visibleLayers[i] = tmfd.getLayer(tmfd.getLayerIDFromLayerNbr(i));
            }else{
                visibleLayers[i]= null;
            }
        }
        return visibleLayers;
    }
    
    /** 
     * @param layerID
     * @return the layer or null if it doesn't exist.  
     */
    public TileMapLayerInfo getLayer(int layerID) {     
        try {
            if(tmfd != null) 
                return tmfd.getLayer(layerID);
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("TileMapControlThread.getLayer()", e);
            }
        }
        return null;
    }
    
    /**
     * Updates layer visibility in TMFD for all layers
     *
     * @param visibleLayers boolean array flagging visibility for layers
     */
    public void updateLayerInfo(boolean[]visibleLayers){
        for(int i=0;i<visibleLayers.length;i++){
            tmfd.setVisibleLayer(i,visibleLayers[i]);
        }
    }
    
    
    /**
     * Method that allows us to override the normal behavior that we first request
     * geodata and when we have received it we request string data. That behavior are
     * a good strategy if we want to draw the map. If we want to use the tiles for other
     * purpose we perhaps want both the geometric and string data as soon as possible. 
     * 
     * @param override, sets to true if we want to load the string at the same time as
     * the geometric data. This will also override the tmfd.alwaysFetchStrings.  
     */
    public void overrideAlwaysFetchStrings(boolean override) {
        alwaysFetchStrings = override;
    }
    
    /**
     * Set the map to download string tiles for a specific layer ID. 
     * This method can be used to override the functionality in
     * tmfd.alwaysFetchStrings(layerID). This is useful if we
     * want to download for example POI string tiles directly and
     * not wait until the user explicitly has requested it. 
     * <p>
     * See tmfd.alwaysFetchStrings(layerID) for more info which layers
     * that by default download string tiles together with the geo tiles.
     * 
     * @param layerID the ID of the layer.  
     * @param download true to download string tiles, false if not. 
     */
    public void setDownloadStringTile(int layerID, boolean download) {
        if(tmfd != null) {            
            MapTask mapTask = new MapTask(this);
            mapTask.setDownloadStringTile(tmfd.getLayerNbrFromID(layerID), download);
        }
    }
    
    /*
     * Internal method for setting the download status for a specific layer number.
     */
    void internalDownloadStringTile(int layerNbr, boolean download) {
        m_DownloadStringTiles[layerNbr] = download;        
    }
    
    /**
     * Sets to true if we want to update and load the overview maps 
     * 
     * @param update, true if we want to update the overview maps. 
     */
    public void setUpdateOverviewMaps(boolean update) {
        iUpdateOverviewMaps = update;
    }
        
    /**
     * Switch between day mode and night mode TilemapFormatDesc. 
     * 
     * @param isNightMode: true if night mode
     */
    public void setNightMode(boolean isNightMode) { 
        if(LOG.isInfo()) {
            LOG.info("TileMapControlThread.setNightMode()", "isNightMode= "+isNightMode);
        }
        
        MapTask task = new MapTask(this);
        task.setNightMode(isNightMode);        
    }
    
    /**
     * Return true if the map use night colors. 
     * 
     * @return true if the map use night colors.
     */
    public boolean isInNightMode() {
        return m_IsInNightMode;
    }
    
    /**
     * 
     * Set the route id in the map component. The route
     * layer will be reloaded. 
     * 
     * @param aRouteID the current route_id
     */
    public boolean setRouteID(String routeID) {
        if(LOG.isInfo()) {
            LOG.info("TileMapControlThread.setRouteID()", "routeID= "+routeID);
        }
        
        if(tmfd != null) {
            MapTask task = new MapTask(this);
            int layerNbr = tmfd.getLayerNbrFromID(1);
            task.setNewRouteID(routeID, layerNbr);
            return true;
        }
        return false;
    }
    
    /**
     * Return the current used RouteID or null if no route is available. 
     * 
     * @return the current used RouteID or null if no route is available.
     */
    public String getRouteID() {
        return iTileMapHandler.getCurrentRouteID();
    }
    
    /**
     * Set a new language in the map component. All visible layers
     * will be reloaded. 
     * 
     * @param lang, the new language. 
     * @see LangTypes
     */
    public void setLanguage(int lang) {
        if (iTextLanguage != lang) {
            //TODO for this to work TMFD need to be reloaded from server   
            iTextLanguage = lang;
            resetAllLayers();
        }
    }
    
    /**
     * @return the current text language that are used in the map
     */
    public int getLanguage() {
        return iTextLanguage;
    }
      
    /**
     * Reload the tile with provided tileID. The tile will only be reloaded
     * if it are visible inside the current bounding box. 
     * 
     * The data will NOT be reloaded from the memory cache. The data will be
     * fetched from rw-cache, pre-loaded maps or internet. 
     * 
     * @param tileID, the tile ID to reload. 
     */
    public void reloadTileID(String tileID) {
        if(LOG.isTrace()) {
            LOG.trace("TileMapControlThread.reloadTileID()", "tileID= "+tileID);
        }
        
        MapTask event = new MapTask(this);
        event.reloadTileID(tileID);     
    }
        
    TileMapHandler getTileMapHandler() {
        return iTileMapHandler;
    }
    
    
    String getGeoMapParamString(String tileID, int importance) {
        String paramString = null;
        TileMapLayerWrapper tmw = iTileMapHandler.getRequestedWrapper(tileID);
        if(tmw != null)
            paramString = tmw.getGeoTileParamString(importance);
        return paramString;
    }
    
    /** 
     * Sets the bounding box and zoom level that the map component should use
     * to load new TileMaps inside.
     * 
     * This method will notify the working thread so that all visible layer will
     * be updated. New tiles will be loaded and tiles that aren't visible anymore 
     * will be removed. 
     * 
     * @param camBoundingBox, the bounding box in MC2 [latMin, latMax, lonMin, lonMax]
     * @param zoomLevel the current scale (meter/pixel)
     */
    private int []iNewCamBoundingBox = new int[4];
    private int []iNewZoomLevel = new int[1];
    private int []iCurrentCamBoundingBox = null;
    private int iCurrentZoomLevel = -1;
    private int iLastUsedZoomLevel = 0;
    private volatile boolean iNewUpdateHasBeenRequested = false;
    // True if a request for update has been made, false if no update
    // has been requested since we start the map. 
    private volatile boolean m_RequestForUpdatesHasBeenMade = false;
    public void updateTileMaps(int[] camBoundingBox, int zoomLevel) {
        m_RequestForUpdatesHasBeenMade = true;
        iNewCamBoundingBox[0] = camBoundingBox[0];
        iNewCamBoundingBox[1] = camBoundingBox[1];
        iNewCamBoundingBox[2] = camBoundingBox[2];
        iNewCamBoundingBox[3] = camBoundingBox[3];          
        iNewZoomLevel[0] = zoomLevel;   
        
        notifyUpdate();
    }
    
    /**
     * 
     * The main run method that will update the map. The thread will stopped if 
     * no new tiles need to be loaded and no new tiles has been extracted. 
     * 
     * 
     * 1. Update the bounding box and zoom level
     * 2. Execute Map Event
     * 3. Update and request new tile maps 
     * 4. Handle new extracted tile maps
     * 5. Update and request the overview maps
     * 
     */
    public void run() {     
        while(isRunning) {      
            try {
                // wait for the thread to wake up
                waitForUpdates();

                /* execute the map tasks if any has been added. */
                if (iMapEventArray.size() > 0) {
                    executeMapTasks();
                }

                if(!iMapLoader.isCacheStarted()) {
                    if(LOG.isInfo()) {
                        LOG.info("TileMapControlThread.run()", "Start cache");
                    }

                    iMapLoader.startCache(m_CacheConfig);
                    iMapLoader.getCache().setVisible(isVisible);

                    if(shouldLoadTmfd()) {
                        /* Request TileMapFormatDesc (Daymode) */
                        if(iTmfdIDStringDay != null && tmfdDay == null) {
                            iMapLoader.loadTMFD(false, false, false, iTmfdIDStringDay);
                        }

                        /* Request TileMapFormatDesc (Nightmode) */
                        if(iTmfdIDStringNight != null && tmfdNight == null) {                   
                            iMapLoader.loadTMFD(true, false, false, iTmfdIDStringNight);
                        }                   
                    }
                    setShouldLoadTmfd(false);

                    continue;
                    //waitForUpdates();
                }

                // update the current camera bounding box and zoom level
                updateBBAndZoomLevel();


                while (isVisible && ((iNewExtractedTileMaps.size() > 0) || 
                        (iCurrentCamBoundingBox != null && iCurrentZoomLevel != -1))) {

                    // Request new TileMaps, bitmap, text map and reset layers
                    updateParamStringsToSend();

                    // Handles new extracted tileMaps 
                    handleExtractedTileMaps();

                    // Check to see if the position has been updated
                    updateBBAndZoomLevel(); 

                    Thread.yield();
                }

                // Update the overview maps
                if (!VectorMapOptimizationFilter.skipOverViewMaps(tmfd, iLastUsedZoomLevel) && iUpdateOverviewMaps && 
                        (iCurrentCamBoundingBox != null) && m_RequestForUpdatesHasBeenMade) {
                    iTileMapHandler.updateOverviewMaps(iCurrentCamBoundingBox);
                }

            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("TileMapControlThread.run()", e);
                    LOG.error("TileMapControlThread.run()", "cache= "+iMapLoader.getCache()+
                            " iExtractedTileMaps= "+iExtractedTileMaps);
                    e.printStackTrace();
                }
            }           
        }
    }
    
    private void updateParamStringsToSend() {
        executeMapTasks();
        
        // Update and request new TileMaps
        if((iCurrentCamBoundingBox != null) && (iCurrentZoomLevel != -1)) {                             
            iTileMapHandler.updateAllTileMaps(iCurrentCamBoundingBox, iCurrentZoomLevel, alwaysFetchStrings);
            //TODO checkif this is needed iTileMapExtraction.notifyExtraction();
        }
    }
    
    private void executeMapTasks() {
        // Update the array with new map tasks
        synchronized (iMapEventArray) {
            while(iMapEventArray.size() > 0) {
                iTasksToExecute.addLast((MapTask)iMapEventArray.removeFirst());
            }
        }
        
        // Execute the the map tasks
        MapTask event;
        while(iTasksToExecute.size() > 0) {
            event = (MapTask)iTasksToExecute.removeFirst();
            event.execute();
            event = null;
        }
    }
    
    /**
     * If no new TileMaps needs to be loaded and no new TileMaps has been 
     * extracted the thread will be placed in wait mode. 
     * @throws InterruptedException 
     */
    private void waitForUpdates() throws InterruptedException {
        synchronized(TILEMAP_MONITOR) {
            while (!iNewUpdateHasBeenRequested) {
                TILEMAP_MONITOR.wait();
            }
            iNewUpdateHasBeenRequested = false;
        }
    }
    
    /**
     * Update the camera bounding box and zoom level that will be used
     * in the map component.
     */
    private void updateBBAndZoomLevel() {       
        if(iNewCamBoundingBox != null) {
            synchronized (iNewCamBoundingBox) {
                iCurrentCamBoundingBox = iNewCamBoundingBox;
            }       
        }       
        synchronized (iNewZoomLevel) {
            iCurrentZoomLevel = iNewZoomLevel[0];
            iNewZoomLevel[0] = -1;
            // Check if camera was updated (moved/zoomed) by user, 
            // and if so save the zoom level
            if (iCurrentZoomLevel != -1) {
                iLastUsedZoomLevel = iCurrentZoomLevel;
            }
        }       
    }
    
    
    private LinkedList iMapEventArray = new LinkedList();   
    private LinkedList iTasksToExecute = new LinkedList();
    
    /**
     * Add a new MapTask to the queue. The MapTask will
     * be executed by the TileMapControl thread. 
     * 
     * @param event, the event to be executed 
     * @see com.wayfinder.map.vectormap.control.MapTask
     */
    void addMapTask(MapTask event) {        
        synchronized (iMapEventArray) {
            iMapEventArray.addLast(event);
        }
        notifyThread();
    }
    
    
    // --------------------------------------------------------------------
    // RESET DATA 
    
    /**
     * Reset and request one layer
     * 
     * @param layerID: the layer to reloaded
     */
    public void resetOneLayer(int layerID) {
        if(LOG.isTrace()) {
            LOG.trace("TileMapControlThread.resetOneLayer()", "layerID= "+layerID);
        }
        
        if(tmfd != null) {
            MapTask event = new MapTask(this);
            int layerNbr = tmfd.getLayerNbrFromID(layerID);
            event.resetLayer(layerNbr);
        }
    }
    
    /**
     * Reset and request all visible layers. 
     */
    public void resetAllLayers() {
        if(tmfd != null) {
            MapTask event;
            for(int i=0; i<tmfd.getNumberOfLayers(); i++) {
                event = new MapTask(this);
                event.resetLayer(i);
            }
        }
    }
    
    /**
     * Save the cache
     */
    public boolean saveCache() {
        if(iMapLoader.isCacheStarted() && isVisible && tmfd != null) {
            MapTask mapTask = new MapTask(this);
            mapTask.saveCache();
            return true;
        }
        return false;
    }
    
    /**
     * Refresh all tiles. 
     * This means that all importance that has been requested but not 
     * received will be loaded again. 
     * 
     */
    public void refreshAllLayers() {
        if(LOG.isTrace()) {
            LOG.trace("TileMapControlThread.refreshAllLayers()", "");
        }
        
        if(tmfd != null) {
            MapTask event = new MapTask(this);
            event.refreshAllLayers();           
        }
    }
    
    // --------------- LOAD TEXT MAPS ---------------------------------------------------
    /**
     * Load the text map for the provided layer. This will be used by layer that has 
     * allwaysFetchStrings set to false (e.g. the POI layer)
     * 
     * @param layerID to load the text maps. 
     */
    public void loadTextMaps(int layerID) {
        if(LOG.isInfo()) {
            LOG.info("TileMapControlThread.loadTextMaps()", "layerID= "+layerID);
        }
        
        if(tmfd != null) {
            int layerNbr = tmfd.getLayerNbrFromID(layerID);
            MapTask event = new MapTask(this);
            event.loadTextMap(layerNbr);
        }
    }
    
    /** 
     * @param aParams
     * @return true if the TileMap are visible inside the current bounding box
     */
    boolean isTileMapVisible(TileMapParams aParams) {
        return iTileMapHandler.isVisible(aParams);
    }
    
    // ---------------- HANDLE EXTRACTED DATA -------------------------------------------
    /**
     * Called from the TileMapExtractionThread when a new TileMap has been extracted. 
     */
    void addExtractImportance(TileMapParams tmp, TileMap aTilemap) {
//      System.out.println("TileMapControlThread: new Tiles has been extracted: "+tmp.getAsString()+" tm= "+aTilemap);
        
        synchronized(iNewExtractedTileMaps) {           
            iNewExtractedTileMaps.addLast(aTilemap);
        }
        notifyThread();
    }
    
    /**
     * 
     */
    private void updateNewExtractedTileMaps() {
        synchronized(iNewExtractedTileMaps) {
            while(iNewExtractedTileMaps.size() > 0) {
                iExtractedTileMaps.addLast((TileMap)iNewExtractedTileMaps.removeFirst());               
            }
        }       
    }
    
    /**
     * Handles the extracted TileMaps that has been send from the TileMapExtractionThread. 
     * 
     * Geodata tiles will be send to the extraction listener as soon it has been extracted. 
     * 
     * String tiles will be send to the extraction listener when all sting TileMaps for one
     * layer has been extracted. 
     * 
     * When all Geodata TileMaps has been extracted for one tile, the matching string tiles 
     * will be requested. This can be overrided by the 
     * overrideAlwaysFetchStrings(boolean override) method. 
     * 
     */
    private void handleExtractedTileMaps() {   
        updateNewExtractedTileMaps();
        
        while(iExtractedTileMaps.size() > 0) {
            TileMap tileMap = (TileMap)iExtractedTileMaps.removeFirst();            
            TileMapParams params = tileMap.getTileMapParams();
            TileMapLayerWrapper wrapper = iTileMapHandler.getRequestedWrapper(params.getTileID());
            
            if(wrapper != null) {
                
                // Remove the tilemap if it's not visible any more
                if(!iTileMapHandler.isVisible(params)) {
                    iTileMapHandler.removeRequestedWrapper(wrapper.getTileID());
                    if(iExtractionListener != null)
                        iExtractionListener.removeTileMap(wrapper.getTileIDParam());
                    continue;
                }
                
                // Set the number of importance that the tile has
                tileMap.setMaxNbrOfImportances(wrapper.getMaxNumberOfImportace());
                tileMap.setNumberOfImportance(wrapper.getNbrImportances());
                
                // Set the empty importance             
                wrapper.setAllEmptyImportances(tileMap.getEmptyImportances());
                
                switch (params.getTileMapType()) {                              
                    case TileMapParams.MAP:                             
                        handleExtractedGeoData(wrapper, params, tileMap);
                        break;
                        
                    case TileMapParams.STRINGS:
                        handleExtractedStringTiles(wrapper, params, tileMap);
                        break;
                        
                    default:
                        if(LOG.isError()) {
                            LOG.error("TileMapControlThread.handleExtractedTileMaps()", "Unknown TileMapParam type extracted");
                            LOG.error("TileMapControlThread.handleExtractedTileMaps()", "ParamString= "+params.getAsString());
                        }
                        break;
                }
            }
            iMapLoader.sendRequest();
        }
        //TODO check if this is needed iTileMapExtraction.notifyExtraction();
    }
    
    /**
     * Handles new extracted Geodata tilemaps. The method will request string tiles when all geodata is loaded. 
     * 
     * When all Geodata TileMaps are requested the String TileMaps will be requested. 
     * 
     * @param wrapper, the TileMapLayerWrapper that holds the TileMaps for this tile and layer
     * @param params
     * @param tileMap
     */
    private void handleExtractedGeoData(TileMapLayerWrapper wrapper, TileMapParams params, TileMap tileMap) {
        
        if(!wrapper.isEmptyImportance(params.getImportance())) {
            if(!wrapper.isReceived(params.getImportance())) {
                
                /* Data that are requested from internet and doesn't have a update time should
                 * be cached. */
                if(wrapper.isGeoDataRequestedFromInternet(params.getImportance()) &&
                   tmfd.getUpdateTimeForLayer(params.getLayerID()) == 0) {
                    wrapper.addData(params, tileMap.getByteData());             
                }
                
                wrapper.setReceived(params.getImportance());            
                wrapper.setGeoMapCrc(params.getImportance(), tileMap.getCRC());
                
                // Sends the extracted tilemap to the listener
                if(iExtractionListener != null) {           
                    if(!wrapper.isOverviewMap())
                        iExtractionListener.addExtractedTileMap(tileMap);               
                    else
                        iExtractionListener.addExtractedOverviewMap(tileMap);
                }
            }
        } else {
            wrapper.unSetRequested(params.getImportance());
            
            /* Add the extracted geometric data to the tile wrapper, when the 
             * tile is complete we will save it in the r/w cache. */
            if(params.getImportance() == 0 && tmfd.getUpdateTimeForLayer(params.getLayerID()) == 0 && 
                    wrapper.isGeoDataRequestedFromInternet(0)) {
                wrapper.addData(params, tileMap.getByteData());
            }
            
            if(LOG.isTrace()) {
                LOG.trace("TileMapControlThread.handleExtractedGeoData()", 
                        "Extracted a empty geodata tile: "+params.getAsString()+
                        " importance= "+params.getImportance()+
                        " lat= "+params.getTileIndexLat()+
                        " lon= "+params.getTileIndexLon());
            }
        }
        
        tileMap.clearByteData();
                
        // Request the rest of the geodata (if it exist)
        if(wrapper.shouldRequestGeoData()) {            
            iTileMapHandler.updateOneTileMap(wrapper, false, false);
            
        // When all geodata is requested, request the "string tiles" 
        } else if(wrapper.isGeoDataDone()) {
            
            /* Load the string data for the tile if it exist. */
            if(!wrapper.isOverviewMap()) {
                final int layerNbr = tmfd.getLayerNbrFromID(wrapper.getLayerID());
                if(m_DownloadStringTiles[layerNbr])
                    iTileMapHandler.updateOneTileMap(wrapper, true, false);
                
            /* else write the overview map to cache if there are data to cache. */
            } else if(wrapper.isOverviewMap() && wrapper.getNbrOfImpToCache() > 0) {
                iMapLoader.getCache().writeDataToCache(wrapper.getDataToCache(),
                                                        wrapper.getParamsToCache(),
                                                        params,
                                                        wrapper.getTotalSize(),
                                                        wrapper.getNbrOfImpToCache(),
                                                        (short)tileMap.getEmptyImportances());
                wrapper.purgeTileMaps();
            }
        }
    }
    
    /**
     * 
     * @param aTileID
     * @param importance
     * @return
     */
    long getGeoMapCRC(String aTileID, int importance) {
        TileMapLayerWrapper tmw = iTileMapHandler.getRequestedWrapper(aTileID);
        
        if(tmw != null)
            return tmw.getGeoMapCrc(importance);
        else
            return 0;
    }
    
    /**
     * Handles new extracted String data tilemaps. 
     * 
     * When all String TileMaps are requested it will be sent to the extraction listener.  
     * 
     * @param wrapper
     * @param params
     * @param tileMap
     */
    private void handleExtractedStringTiles(TileMapLayerWrapper wrapper, TileMapParams params, TileMap tileMap) {
        
        if(!wrapper.isEmptyImportance(params.getImportance())) {
            
            /* Add the extracted string data to the tile wrapper, when the tile is
             * complete we will save it in the r/w cache. */
            if(wrapper.isStringDataRequestedFromInternet(params.getImportance()) &&
               tmfd.getUpdateTimeForLayer(params.getLayerID()) == 0) {
                wrapper.addData(params, tileMap.getByteData());             
            }
            
            wrapper.setReceivedString(params.getImportance());    
            wrapper.addTileMap(tileMap, params.getImportance());
            
        } else {
            wrapper.unSetRequestedString(params.getImportance());
            if(LOG.isTrace()) {
                LOG.trace("TileMapControlThread.handleExtractedStringTiles()", 
                        "Extracted a empty string tile: "+params.getAsString()+" imp= "+params.getImportance());
            }
        }
        
        tileMap.clearByteData();
        
        if(wrapper.isStringDataDone()) {
            // Send all string importances to the listener
            if(iExtractionListener != null) {
                iExtractionListener.addExtractedTileMaps(wrapper.getTileMaps());
            }
            
            if(wrapper.getNbrOfImpToCache() > 0) {                  
                iMapLoader.getCache().writeDataToCache(wrapper.getDataToCache(), 
                                                       wrapper.getParamsToCache(),
                                                       params, 
                                                       wrapper.getTotalSize(), 
                                                       wrapper.getNbrOfImpToCache(),
                                                       (short)tileMap.getEmptyImportances());                   
            }
            
            // clean up the memory 
            wrapper.purgeTileMaps();
        }
    }
    
    /**
     * Notify the TileMapControl thread to start process new data. 
     * 
     */
    void notifyThread() {
        if(isVisible && tmfd != null) {
            this.notifyUpdate();
        } else {
            if (tmfd == null) {
                if(LOG.isWarn()) {
                    LOG.warn("TileMapControlThread.notifyThread()", "update ignored because tmdf is null");
                }
            } else {
                if(LOG.isWarn()) {
                    LOG.warn("TileMapControlThread.notifyThread()", "update ignored because is not visible");
                }
            }
        }
    }
    
    // ------------- LOAD STRINGS AND BITMAPS -----------------------------------------------
    
    private static int      nbrStrings;
    private static String[] stringTable;
    
    private static void setNbrStrings() {
        nbrStrings  = 0;
    }
    
    private static void setStringTable() {
        stringTable = new String[64];
    }
    
    /**
     * Returns an index for a poi-String.
     * Adds it to table if it didn't exist.
     *
     * @param s POI file name
     * @return Integer index
     */
    public static int getStringIndex(String s) {
        
        for(int i=0; i<nbrStrings; i++) {
            if(i<stringTable.length && stringTable[i] != null ){
                if(stringTable[i].compareTo(s)==0) return i;
            }
        }
        
        if(nbrStrings>=stringTable.length) {
            String[] newStringTable = new String[stringTable.length<<1];
            System.arraycopy(stringTable, 0, newStringTable, 0, stringTable.length);
            stringTable = newStringTable;
        }
        stringTable[nbrStrings] = s;
        nbrStrings++;        
        return nbrStrings-1;
    }
    
    /**
     * Returns a POI file name for an index
     *
     * @param index index for String
     * @return String for index
     */
    public static String getString(int index) {     
        if(index > -1 && index < stringTable.length)
            return stringTable[index];
        else 
            return null;
    }
    
    //FIXME: use a external ImageLoader class    
    private Hashtable iBitmapImages = new Hashtable();
    public byte[] getBitmapImage(String url) {      
        byte[] img = (byte [])iBitmapImages.remove(url);
        
        if(img == null && TileMapParamTypes.isBitmap(url)) {            
            MapTask event = new MapTask(this);
            event.loadBitMap(url);
        }
        return img;
    }
    
    //FIXME: use a external ImageLoader class
    void addBitmap(String paramString,byte []data) {
        try {           
            iBitmapImages.put(paramString, data);
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("TileMapControlThread.addBitmap()", e);
            }
        }
    }
    
    // ---------------- UPDATE THE TILEMAPFORMATDESC --------------------------------------------
    /**
     * 
     * Called by the TileMapExtractionThread when a new TileMapFormatDesc has been extracted. 
     * 
     * @param aTmfd, the new TileMapFormatDesc
     */
    void addExtractedTileMapFormatDesc(TileMapFormatDesc aTmfd, String paramString) {   
        
        
        /* If there already exist a extracted tmfd we add a MapTask so that the new tmfd will be loaded
        * by the main thread in TileMapControlThread. */
        if(tmfd != null) {
            if(LOG.isInfo()) {
                LOG.info("TileMapControlThread", "addExtractedTileMapFormatDesc() " + "post a task to change tmfd:" + paramString );
            }
            MapTask task = new MapTask(this);
            task.setNewTileMapFormatDescInApplication(aTmfd, TileMapParamTypes.isTmfdNight(paramString));
            
        /* If no tmfd is loaded yet we just test the extracted tmfd a the current tmfd. */
        } else {
            if(LOG.isInfo()) {
                LOG.info("TileMapControlThread", "addExtractedTileMapFormatDesc() "+ "set new tmfd paramString= " + paramString);
            }
            setNewTileMapFormatDesc(aTmfd, TileMapParamTypes.isTmfdNight(paramString));         
        }
    }
    
    /**
     * 
     * This method must be called by the TileMapControlThread to make sure that we switch the tmfd
     * in a safe way. 
     * 
     * @param aTmfd
     * @param aisNightModeTmfd
     */
    void setNewTileMapFormatDesc(TileMapFormatDesc aTmfd, boolean aisNightModeTmfd)  {
        if(!aisNightModeTmfd) {
            if(LOG.isInfo()) {
                LOG.info("TileMapControlThread", "setNewTileMapFormatDesc() " + 
                        "DayMode tmfd.crc= "+aTmfd.getCRC()+" tmfdDayCrc= "+tmfdDayCrc);
            }
            tmfdDay = aTmfd;
            
            if(tmfdDayCrc != -1 && tmfdDay.getCRC() != tmfdDayCrc) {
                tmfdDay = null;
                iMapLoader.loadTMFD(false, true, false, iTmfdIDStringDay);
            }
            

            
        } else {
            if(LOG.isInfo()) {
                LOG.info("TileMapControlThread", "setNewTileMapFormatDesc() " + 
                        "NightMode tmfd.crc= "+aTmfd.getCRC()+" tmfdNightCrc= "+tmfdNightCrc);
            }
            tmfdNight = aTmfd;
            
            if(tmfdNightCrc != -1 && tmfdNight.getCRC() != tmfdNightCrc) {
                tmfdNight = null;
                iMapLoader.loadTMFD(true, true, false, iTmfdIDStringNight);
            }
        }
        
        // Always load tmfd for the daymode when starting the client
        // this set both tmfdNight and tmfdDay
        updateTMFD(false);
    }
    
    /**
     * 
     * Called by the TileMapExtractionThread when a new TileMapFormatDesc CRC has been extracted. 
     * 
     * @param tmfdCRC
     * @param paramString
     */
    void addExtractedTileMapFormatDescCrc(long tmfdCRC, String paramString) {
        
        if(TileMapParamTypes.isTmfdDayCrc(paramString)) {
            if(LOG.isInfo()) {
                LOG.info("TileMapControlThread", "addExtractedTileMapFormatDescCrc() " +  
                        "Daymode: paramString= "+paramString+" tmfdCRC= "+tmfdCRC+" tmfdDayCrc= "+tmfdDayCrc);
            }
            
            tmfdDayCrc = tmfdCRC;
            if(tmfdDay != null) {
                if(tmfdDayCrc != tmfdDay.getCRC()) {
                    tmfdDay = null;
                    iMapLoader.loadTMFD(false, true, false, iTmfdIDStringDay);
                }
            }
            
        } else if(TileMapParamTypes.isTmfdNightCrc(paramString)) {
            if(LOG.isInfo()) {
                LOG.info("TileMapControlThread", "addExtractedTileMapFormatDescCrc() " + 
                        "Nightmode: paramString= "+paramString+" tmfdCRC= "+tmfdCRC+" tmfdNightCrc= "+tmfdNightCrc);
            }
            
            tmfdNightCrc = tmfdCRC;
            if(tmfdNight != null) {
                if(tmfdNightCrc != tmfdNight.getCRC()) {
                    tmfdNight = null;
                    iMapLoader.loadTMFD(true, true, false, iTmfdIDStringNight);
                }
            }
                        
        } else {
            if(LOG.isError()) {
                LOG.error("TileMapControlThread", "addExtractedTileMapFormatDescCrc() " + 
                        "UNKNOWN TMFD-CRC, paramString= "+paramString);
            }
        }
        iMapLoader.sendRequest();
    }
    
    private boolean []m_DownloadStringTiles;
        
    /**
     * When both TileMapFormatDesc for day mode and night mode has been extracted the map 
     * component are ready to load tiles. 
     * 
     * @param isNightMode, true if we want to use the night mode TileMapFormatDesc. 
     * @return true if a new TMFD has been set. 
     */
    boolean updateTMFD(boolean isNightMode) {
        
        if((tmfdDay == null && iTmfdIDStringDay != null) || (tmfdNight == null && iTmfdIDStringNight != null)) {
            return false;       
        }
        
        if(isNightMode)
            tmfd = tmfdNight;
        else
            tmfd = tmfdDay;
            
        m_IsInNightMode = isNightMode;
        iTileMapExtraction.setTileMapFormatDesc(tmfd); 
        iTileMapHandler.setTileMapFormatDesc(tmfd);
        iMapLoader.setTileMapFormatDescritor(tmfd);
        
        // Reset all layers so that we use the new tmfd
        resetAllLayers();
        
        // Update the new loaded tmfd with the category visibility if they have been changed. 
        if(m_PoiCategories != null) {
            for(int i=0; i<m_PoiCategories.length; i++) {
                tmfd.setCategoryEnabled(i, m_PoiCategories[i].isEnable());
            }
        }
        
        // Set if the string tiles should be downloaded or not when we have loaded the geo tiles, for a specific layer. 
        // If there if the first time we load tmfd or if the number of layer differs (which is very unlikely) we use 
        // the default settings provided from tmfd. 
        if(m_DownloadStringTiles == null || (m_DownloadStringTiles.length != tmfd.getNumberOfLayers())) {
            m_DownloadStringTiles = new boolean[tmfd.getNumberOfLayers()];
            for(int i=0; i<m_DownloadStringTiles.length; i++) {
                int layerID = tmfd.getLayerIDFromLayerNbr(i);
                m_DownloadStringTiles[i] = tmfd.alwaysFetchStrings(layerID);
            }
        }
        
        // Send the tmfd to the extraction listener
        if(iExtractionListener != null) {
            iExtractionListener.addExtractedTileMapFormatDesc(tmfd);
        }           
        
        if(LOG.isInfo()) {
            LOG.info("TileMapControlThread", "updateTMFD() " + "TileMapFormatDesc has been updated");
        }

        return true;
    }
    
    
    private boolean iLoadTmdf = false;
    
    /**
     * Sets to true if we need to load the TileMapFormatDesc. This method
     * will be called if we have requested that TileMapFormatDesc should 
     * be loaded before the cache has been started. Then we need to wait
     * until the cache is fully started before we can loaded tmfd. Otherwise
     * we will always load a new tmfd from the server.  
     * 
     * @param aLoadTmfd true if we need to load the TileMapFormatDesc. 
     */
    synchronized void setShouldLoadTmfd(boolean aLoadTmfd) {
        iLoadTmdf = aLoadTmfd;
    }
    
    /**
     * Return true if we should load TileMapFormatDesc when 
     * the cache has been started. 
     * 
     * @return true if we should load TileMapFormatDesc when 
     * the cache has been started.
     */
    synchronized boolean shouldLoadTmfd() {
        return iLoadTmdf;
    }
    
    
    /**
     * 
     * Load the TileMapFormatDescCRC from the server when the 
     * map has been stated. 
     * <p>
     * 
     * The TileMapFormatDesc will be loaded from cache or server
     * if the cache has been stated. If the cache hasn't been stated
     * yet we will load tmfd as soon as the cache has been stated. 
     * <p>
     * 
     * When both the tmfd-crc and tmfd has been loaded and extracted we
     * will compare the crc and if it's differs a new tmfd will be loaded
     * from the server. 
     * <p>
     * 
     */
    public void loadTileMapFormatDescCRC() {        

        if(iTmfdIDStringDay != null) {
            // TMFD-CRC (daymode)
            iMapLoader.loadTMFDCRC(false, iTmfdIDStringDay);
            
            /* Load tmfd if the cache has been stated, otherwise it will be loaded
             * when the cache has been fully initialized.*/
            if(tmfdDay == null) {
                if(iMapLoader.isCacheStarted())
                    iMapLoader.loadTMFD(false, false, false, iTmfdIDStringDay);
                else
                    setShouldLoadTmfd(true);
            }
        }
 
        if(iTmfdIDStringNight != null) {
            // TMFD-CRC (nightmode)
            iMapLoader.loadTMFDCRC(true, iTmfdIDStringNight);
            
            /* Load tmfd if the cache has been stated, otherwise it will be loaded
             * when the cache has been fully initialized.*/
            if(tmfdNight == null) {
                if(iMapLoader.isCacheStarted())
                    iMapLoader.loadTMFD(true, false, false, iTmfdIDStringNight);
                else
                    setShouldLoadTmfd(true);
            }
        }
        
        iMapLoader.sendRequest();
        
        if(LOG.isInfo()) {
            LOG.info("TileMapControlThread", "loadTileMapFormatDescCRC() "+ 
                    "tmfdDayCRC= "+tmfdDayCrc+" tmfdNightCRC= "+tmfdNightCrc);
        }       
    }
    
    
    private String iTmfdIDStringDay = null;
    private String iTmfdIDStringNight = null;
    public void setTileMapFormatDescIDString(String aDayMode, String aNightMode) {
        iTmfdIDStringDay = aDayMode;
        iTmfdIDStringNight = aNightMode;        
    }
    
    /**
     * Load the TileMapFormatDesc
     * 
     * @param reset, true if we want to load the TileMapFormatDesc from the server
     * @aOnlyFromCache, true if we only want to load the tmfd from the cache and not 
     * download it from the server. 
     */
    void loadTMFD(boolean reset, boolean aOnlyFormCache) {
        if(iTmfdIDStringDay != null)
            iMapLoader.loadTMFD(false, reset, aOnlyFormCache, iTmfdIDStringDay);
        if(iTmfdIDStringNight != null)
            iMapLoader.loadTMFD(true,  reset, aOnlyFormCache, iTmfdIDStringNight);      
        iMapLoader.sendRequest();
    }
    
    /**
     * 
     * Set the ACP mode setting.
     * 
     * @param aEnable: true if the ACP is enabled, false if not. 
     */
    public void setDownloadACPEnabled( boolean aEnabled ){
        if(tmfd != null)
            tmfd.setDownloadACPEnabled(aEnabled);
        else{
            if(LOG.isError()) {
                LOG.error("TileMapControlThread.setDownloadACPEnabled()", "TileMapFormatDesc is missing!");
            }
        }
    }
    
    /**
     * 
     * Set the traffic info layer setting. 
     * 
     * @param aIsVisible, true if the traffic info layer should 
     *        be visible, false if not
     */
    public void setTrafficLayerVisible( boolean aIsVisible){
        if(tmfd != null)
            tmfd.setTrafficLayerVisible(aIsVisible);
        else{
            if(LOG.isError()) {
                LOG.error("TileMapControlThread.setTrafficLayerVisible()", "TileMapFormatDesc is missing!");
            }
        }
    }

    /**
     * 
     * Set the update time for the traffic layer. 
     * 
     * @param aMinutes, the update time in minutes. 
     */
    public void setTrafficInfoUpdateTime( int aMinutes){
        if(tmfd != null)
            tmfd.setTrafficInfoUpdateTime(aMinutes);
        else{
            if(LOG.isError()) {
                LOG.error("TileMapControlThread.setTrafficInfoUpdateTime()", "TileMapFormatDesc is missing!");
            }
        }
    }
    
}
