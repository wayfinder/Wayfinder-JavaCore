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
package com.wayfinder.core.map.vectormap.internal.control;

import java.util.Enumeration;
import java.util.Hashtable;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.map.vectormap.internal.process.RouteID;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;

/**
 * 
 *
 */
public class TileMapHandler {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapHandler.class);

    private Hashtable  iRequestedWrapper;
    
    private TileMapLoader iTileMapLoader;
    private TileMapExtractionListener iExtractionListener;
    private TileMapControlThread iTileMapControl;
    
    private static boolean GZIP = true;
    private RouteID iRouteID = null;
    
    // Holds the current TileMap indices, detail level and nbr of importances 
    private int[] prevMinLat = null, prevMaxLat = null, prevMinLon = null, prevMaxLon = null;
    private int[] prevDetailLevel = null, prevNbrImportance = null;
    
    // Overview map tile indices and boundaries
    private int[]overviewMapIndices = null;
    private int[]overviewMapsBoundaries = null;
    
    // The current TileMapFormatDesc
    private TileMapFormatDesc tmfd = null;
    
    public TileMapHandler(TileMapControlThread aTileMapControl, TileMapLoader aMapLoader) {
        iTileMapControl = aTileMapControl;
        iTileMapLoader = aMapLoader;
        iExtractionListener = null;
        iRequestedWrapper = new Hashtable();
    }
    
    void setTileMapExtractionListener(TileMapExtractionListener aListener) {
        iExtractionListener = aListener;
    }
    
    TileMapLayerWrapper getRequestedWrapper(String aTileID) {
        return (TileMapLayerWrapper)iRequestedWrapper.get(aTileID);
    }
    
    void removeRequestedWrapper(String aTileID) {
        iRequestedWrapper.remove(aTileID);
    }
    
    /**
     * 
     * @param aTmfd
     */
    void setTileMapFormatDesc(TileMapFormatDesc aTmfd) {
        tmfd = aTmfd;
        
        final int nbrLayer = tmfd.getNumberOfLayers();      
        prevMinLat = new int[nbrLayer];
        prevMaxLat = new int[nbrLayer];
        prevMinLon = new int[nbrLayer];
        prevMaxLon = new int[nbrLayer];
        prevDetailLevel = new int[nbrLayer];
        prevNbrImportance = new int[nbrLayer];
        
        overviewMapIndices = new int[]{ Utils.MIN_INTEGER, 
                                        Utils.MAX_INTEGER, 
                                        Utils.MIN_INTEGER, 
                                        Utils.MAX_INTEGER};
        
        int[] bound = tmfd.getTileIndex(new int[]{Utils.MIN_INTEGER, 
                                                  Utils.MAX_INTEGER, 
                                                  Utils.MIN_INTEGER>>1, 
                                                  Utils.MAX_INTEGER>>1}, 
                                                  0, 
                                                  tmfd.getOverviewMapDetailLevel());
        overviewMapsBoundaries = new int[4];
        overviewMapsBoundaries[0] = bound[0];
        overviewMapsBoundaries[1] = bound[1];
        overviewMapsBoundaries[2] = bound[2];
        overviewMapsBoundaries[3] = bound[3];
        
    }
    
    /**
     * Set a new RouteID in the map component. 
     * 
     * @param aRouteID, the route id string from the navigate route. 
     */
    void setRouteID(String aRouteID) {
        iRouteID = null;
        if(aRouteID != null)
            iRouteID = new RouteID(aRouteID);
    }
    
    /**
     * Return the current used RouteID string, or null if no route is available. 
     * 
     * @return
     */
    String getCurrentRouteID() {
        if(iRouteID != null)
            return iRouteID.getRouteIDAsString();
        
        return null;
    }
    
    /**
     * 
     * The internal method that reset and request the layer specified by 
     * the parameter. 
     * 
     * @param layerID, the layer to reset
     */
    void internalResetLayer(int layerNbr) {     
        int layerID = tmfd.getLayerIDFromLayerNbr(layerNbr);
        
        if(LOG.isTrace()) {
            LOG.trace("TileMapHandler.internalResetLayer()", "layerID= "+layerID+" layerNbr= "+layerNbr);
        }
        
        Enumeration iEnum = iRequestedWrapper.elements();
        TileMapLayerWrapper wrapper = null;     
        while(iEnum.hasMoreElements()) {
            wrapper = (TileMapLayerWrapper)iEnum.nextElement();
            if(wrapper.getLayerID()==layerID) {             
                internalRemoveLoadedWrapper(wrapper);               
            }
        }
        
        /* For tilemaps*/
        prevMinLat[layerNbr] = Integer.MAX_VALUE;
        prevMaxLat[layerNbr] = Integer.MIN_VALUE;
        prevMinLon[layerNbr] = Integer.MAX_VALUE;
        prevMaxLon[layerNbr] = Integer.MIN_VALUE;
        prevDetailLevel[layerNbr] = -1;
        prevNbrImportance[layerNbr] = -1;       
        
        /* For overview maps*/
        overviewMapIndices[0] = Integer.MAX_VALUE;
        overviewMapIndices[1] = Integer.MAX_VALUE;
        overviewMapIndices[2] = Integer.MAX_VALUE;
        overviewMapIndices[3] = Integer.MAX_VALUE;
        
    }
    
    /**
     * Refresh all layers, i.e all importance that has been requested but not yet received will be reloaded.
     * 
     * Note: Only layers that can be cached will be refreshed. 
     * 
     */
    void refreshAllTiles() {        
        
        if(LOG.isTrace()) {
            LOG.trace("TileMapHandler.refreshAllTiles()", "");
        }
        
        Enumeration iEnum = iRequestedWrapper.elements();
        TileMapLayerWrapper wrapper = null;     
        while(iEnum.hasMoreElements()) {
            wrapper = (TileMapLayerWrapper)iEnum.nextElement();
            if(iTileMapControl.shouldBeSavedInCache(wrapper.getLayerID())) {
                final int nbrImp = wrapper.getNbrImportances();
                for(int i=0; i<nbrImp; i++) {
                    if(!wrapper.isEmptyImportance(i) && wrapper.isRequested(i) && !wrapper.isReceived(i)) {
                        if(LOG.isTrace()) {
                            LOG.trace("TileMapHandler.refreshAllTiles()", "importance= "+i+" layerID= "+wrapper.getLayerID()+
                                           " lat= "+wrapper.getTileIndexLat()+" lon= "+wrapper.getTileIndexLon());
                        }
                        wrapper.unSetRequested(i);
                        
                        /* Reset the lat, lon, detaillevel and nbr of importance value to enable reloading of
                         * the tile. Otherwise will the sameTileMapsAsPrevious(..) method return true next time
                         * we update the tiles. */
                        int layerNbr = tmfd.getLayerNbrFromID(wrapper.getLayerID());
                        prevMinLat[layerNbr] = Integer.MAX_VALUE;
                        prevMaxLat[layerNbr] = Integer.MIN_VALUE;
                        prevMinLon[layerNbr] = Integer.MAX_VALUE;
                        prevMaxLon[layerNbr] = Integer.MIN_VALUE;
                        prevDetailLevel[layerNbr] = -1;
                        prevNbrImportance[layerNbr] = -1;                       
                    }
                }
            }
        }
    }
    
    /**
     * Reload the tile with the tile id specified by the parameter. 
     * 
     * @param aTileID, the tile to reload. 
     */
    void internalReloadTileID(String aTileID) {
        TileMapLayerWrapper wrapper = (TileMapLayerWrapper)iRequestedWrapper.get(aTileID);
        
        if(wrapper != null) {
            wrapper.reset();
            
            if(iExtractionListener != null)
                iExtractionListener.removeTileMap(wrapper.getTileIDParam());
            
            /* Update one tile, any data in the memory cache will be removed. */
            updateOneTileMap(wrapper, false, true);
        }
    }
    
    /**
     * The internal method that loads the text maps.
     * 
     * This method will be called for layer that has tmfd.alwaysFetchStrings 
     * set to false, when we want to load the strings. Example when we move 
     * the cursor over a POI.   
     * 
     * @param layerNbr
     */
    void internalLoadTextMaps(int layerNbr) {       
        int layerID = tmfd.getLayerIDFromLayerNbr(layerNbr);
        
        if(LOG.isTrace()) {
            LOG.trace("TileMapHandler.internalLoadTextMaps()", "layerID= "+layerNbr);
        }
        
        Enumeration iEnum = iRequestedWrapper.elements();
        TileMapLayerWrapper wrapper = null;     
        while(iEnum.hasMoreElements()) {
            wrapper = (TileMapLayerWrapper)iEnum.nextElement();
            if(wrapper.getLayerID()==layerID) {
                updateOneTileMap(wrapper, true, false);               
            }
        }       
    }
    
    // ------------------------------------------------------------------------------------------
    /**
     * 
     * The method check if we need to load any new TileMaps. 
     * 
     * If the method return true we know for sure that we don't need to load any new tiles. 
     * 
     * If the method return false one or more tiles needs to be loaded / removed.  
     *   
     * 
     * @param minLat
     * @param maxLat
     * @param minLon
     * @param maxLon
     * @param aDetailLevel
     * @param aNbrImp (number of importance)
     * @param layerNbr
     * @return
     */
    private boolean sameTileMapsAsPrevious(int minLat, int maxLat, int minLon, int maxLon, int aDetailLevel, int aNbrImp, int layerNbr) {
        if(minLat == prevMinLat[layerNbr] && maxLat == prevMaxLat[layerNbr] && minLon == prevMinLon[layerNbr] && 
           maxLon == prevMaxLon[layerNbr] && aDetailLevel == prevDetailLevel[layerNbr] && aNbrImp == prevNbrImportance[layerNbr]) {                    
            return true;
        } else {
            prevMinLat[layerNbr] = minLat;
            prevMaxLat[layerNbr] = maxLat;
            prevMinLon[layerNbr] = minLon;
            prevMaxLon[layerNbr] = maxLon;
            prevDetailLevel[layerNbr] = aDetailLevel;
            prevNbrImportance[layerNbr] = aNbrImp;
            return false;
        }       
    }
    
    /**
     * The method has the same function as the sameTileMapsAsPrevious(..) but for overview maps. 
     *  
     */
    private boolean sameOverviewMapsAsPrevious(int []tileIndices) {
        
        if(overviewMapIndices!=null &&
        (overviewMapIndices[0]!=tileIndices[0] || overviewMapIndices[1]!=tileIndices[1] ||      
         overviewMapIndices[2]!=tileIndices[2] || overviewMapIndices[3]!=tileIndices[3])) {
            
            overviewMapIndices[0] = tileIndices[0];
            overviewMapIndices[1] = tileIndices[1];
            overviewMapIndices[2] = tileIndices[2];
            overviewMapIndices[3] = tileIndices[3];
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Return true if the TileMap are inside the current bounding box.  
     * 
     */
    boolean isVisible(TileMapParams aParams) {
        
        if(prevMaxLat==null || 
                TileMapParamTypes.isMapFormatDesc(aParams.getAsString()) || 
                aParams.isOverviewMap()) {
            return true;
        }
        
        final int layerNbr = tmfd.getLayerNbrFromID(aParams.getLayerID());      
        if(aParams.getTileIndexLat() >= prevMinLat[layerNbr] && aParams.getTileIndexLat() <= prevMaxLat[layerNbr] && 
           aParams.getTileIndexLon() >= prevMinLon[layerNbr] && aParams.getTileIndexLon() <= prevMaxLon[layerNbr]) {
            return true;
        } else {
            return false;
        }       
    }
    
    // ------------- UPDATE TILEMAPS ---------------------------------------------------------------
    
    private int []iOverviewMapIndex = new int[2];

    private int[] tileIndices = new int[4];

    private int[] overViewTileIndices = new int[4]; 
    
    /**
     * Update and request new Overview maps 
     * 
     */
    void updateOverviewMaps(int []currentCamBoundingBox) {
        
        if(currentCamBoundingBox == null)
            return;
        
        int detailLevel = tmfd.getOverviewMapDetailLevel();        
        int layerID = 0;
        
        int camX = (currentCamBoundingBox[2]+currentCamBoundingBox[3])/2;
        int camY = (currentCamBoundingBox[0]+currentCamBoundingBox[1])/2;
        
        // Calculate the tile indices for the overview maps
        tmfd.getTileIndex(tmfd.getLayerNbrFromID(layerID), detailLevel, camX, camY, iOverviewMapIndex);
        int add = (tmfd.getNbrOverviewMaps()-1)/2;                  
        overViewTileIndices[0] = iOverviewMapIndex[0]-add<overviewMapsBoundaries[0]?overviewMapsBoundaries[0]:iOverviewMapIndex[0]-add;
        overViewTileIndices[1] = iOverviewMapIndex[0]+add>overviewMapsBoundaries[1]?overviewMapsBoundaries[1]:iOverviewMapIndex[0]+add;
        overViewTileIndices[2] = iOverviewMapIndex[1]-add<overviewMapsBoundaries[2]?overviewMapsBoundaries[2]:iOverviewMapIndex[1]-add;
        overViewTileIndices[3] = iOverviewMapIndex[1]+add>overviewMapsBoundaries[3]?overviewMapsBoundaries[3]:iOverviewMapIndex[1]+add;

        if(!sameOverviewMapsAsPrevious(overViewTileIndices)) { 
            String tileID;
            TileMapLayerWrapper wrapper;
            removeUnseenTileMaps(layerID, detailLevel, overViewTileIndices, 1, true);           
            for(int lat=overviewMapIndices[0]; lat<=overviewMapIndices[1]; lat++) {
                for(int lon=overviewMapIndices[2]; lon<=overviewMapIndices[3]; lon++) {
                    
                    TileMapParams iTileIDParam = getTileID(lat, lon, layerID, detailLevel);
                    buffer.softReset();
                    tileID = iTileIDParam.getAsString(buffer);
                    // add the 'om' string at the end to mark that this is a overview tile                  
                    tileID = tileID+"OM";
                    iTileIDParam.setTileID(tileID);
                    wrapper = (TileMapLayerWrapper)iRequestedWrapper.get(tileID);
                    int importance = 0;
                    
                    if(wrapper == null) {                       
                        wrapper = new TileMapLayerWrapper(lat,lon,layerID,detailLevel,iTileIDParam,true);
                        wrapper.setNbrImportances(1);
                        wrapper.setMaxNbrOfImportance(1);
                        iRequestedWrapper.put(tileID, wrapper);
                    }
                    
                    if(wrapper.shouldRequestGeoData(importance)) {
                        wrapper.setRequested(importance);
                        
                        // Create the ParamString                       
                        TileMapParams iTileMapParams = 
                            createTileMapParams(layerID, importance, lat, lon, detailLevel, tileID, true, false);
                        buffer.softReset();
                        String paramString = iTileMapParams.getAsString(buffer);
                        iTileMapLoader.request(paramString, iTileMapParams, wrapper);
                    }                   
                }
            }           
        }        
    }
    
    private byte[] b = new byte[30];
    private BitBuffer buffer = new BitBuffer(b);
   
    /**
     * Update all tiles that are visible inside the current bounding box.  
     * 
     */
    void updateAllTileMaps(int[] camBoundingBox, int zoomLevel, boolean aFetchStrings) {
        
        if(camBoundingBox==null || zoomLevel==-1)
            return;
        
        // For all layers
        for(int layerNbr = 0; layerNbr <tmfd.getNumberOfLayers(); layerNbr++ ){
            
            int layerID = tmfd.getLayerIDFromLayerNbr(layerNbr);
            // If the layer isn't visible we skip to download the layer and
            // we don't download the routelayer if we don't have any route set
            if(!tmfd.visibleLayer(layerID)  || (layerID==1 && iRouteID==null)) {
                continue;
            }
            
            int detailLevel    = tmfd.getCurrentDetailLevel(layerNbr ,zoomLevel);
            tmfd.getTileIndex(camBoundingBox,layerID,detailLevel,tileIndices);                               
            int nbrImportances = tmfd.getNbrImportances(zoomLevel,detailLevel,layerID);

            // Test if the tile indexes is the same as the previous ones
            if(!sameTileMapsAsPrevious(tileIndices[0], tileIndices[1],tileIndices[2],tileIndices[3],
                    detailLevel,nbrImportances,layerNbr)) {
                
                
                //For all importances in the layer
                for(int importance=0; importance<nbrImportances; importance++) {
                    
                    // For all tile indices in the layer
                    for(int lat=tileIndices[0]; lat<=tileIndices[1]; lat++) {
                        for(int lon=tileIndices[2]; lon<=tileIndices[3]; lon++) {
                            
                            TileMapParams iTileIDParam = getTileID(lat, lon, layerID, detailLevel);
                            buffer.softReset();
                            String tileID = iTileIDParam.getAsString(buffer);
                            iTileIDParam.setTileID(tileID);
                            TileMapLayerWrapper wrapper = (TileMapLayerWrapper)iRequestedWrapper.get(tileID);
                            
                            /* The wrapper are not loaded i.e. it's a new tile */
                            if(wrapper == null) {
                                
                                int minScale = tmfd.getMinScaleForDetailLevel(layerNbr, detailLevel);
                                int maxScale = tmfd.getMaxScaleForDetailLevel(layerNbr, detailLevel);
                                int nbrImp1 = tmfd.getNbrImportances(minScale, detailLevel, layerID);
                                int nbrImp2 = tmfd.getNbrImportances(maxScale, detailLevel, layerID);
                                
                                wrapper = new TileMapLayerWrapper(lat,lon,layerID,detailLevel,iTileIDParam);
                                
                                /* Set the max number of importance for the detail level. This to make sure
                                 * that you can zoom in the detail level and be able to load more importance. */
                                wrapper.setMaxNbrOfImportance(Math.max(nbrImp1, nbrImp2));
                                wrapper.setNbrImportances(nbrImportances);
                                
                                iRequestedWrapper.put(tileID, wrapper);
                            } else {
                                
                                /* The wrapper are already loaded and needs to be updated. E.g. we have
                                 * zoomed in/out in a detail level and the number of importance has been
                                 * changed. */
                                wrapper.setNbrImportances(nbrImportances);
                                if(!wrapper.isGeoDataDone()) {
                                    if(LOG.isTrace()) {
                                        LOG.trace("TileMapHandler.updateAllTileMaps()", "A current loaded tile needs to be updated");
                                    }
                                    updateOneTileMap(wrapper, false, false);
                                }                           
                                continue;
                            }
                                    
                            if(wrapper.shouldRequestGeoData(importance)) {
                                wrapper.setRequested(importance);
                                
                                // Create the ParamString
                                TileMapParams iTileMapParams = 
                                    createTileMapParams(layerID, importance, lat, lon, detailLevel, tileID, false, false);
                                buffer.softReset();
                                String paramString = iTileMapParams.getAsString(buffer);     
                                wrapper.setGeoTileMapParamString(paramString, importance);
                                iTileMapLoader.request(paramString, iTileMapParams, wrapper);
                            }
                            
                            if(aFetchStrings && wrapper.shouldRequestString(importance)) {
                                wrapper.setRequestedString(importance);
                                
                                // Create the ParamString
                                TileMapParams iTileMapParams = 
                                    createTileMapParams(layerID, importance, lat, lon, detailLevel, tileID, false, true); 
                                buffer.softReset();                             
                                String paramString = iTileMapParams.getAsString(buffer);     
                                iTileMapLoader.request(paramString, iTileMapParams, wrapper);
                            }                                   
                        }
                    }
                }
                
                // Remove any unseend tile
                removeUnseenTileMaps(layerID, detailLevel, tileIndices, nbrImportances, false);
                
                // Send the request after loaded each layer.
                iTileMapLoader.sendRequest();
            }
        }
    }

    /**
     * Update and request all not empty importance for one TileMap and for one layer
     * 
     * @param wrapper the TileMapLayerWrapper that will be updated
     * @param lat (MC2)
     * @param lon (MC2)
     * @param layerID 
     * @param parent, the id of the tile
     * @param isString, true if we want to load the string maps
     * @param resetFromMemCache, true if we want to remove any saved data from the memory cache. 
     */
    void updateOneTileMap(TileMapLayerWrapper wrapper, boolean isString, boolean resetFromMemCache) { 
                                              
        final int nbrImportances = wrapper.getNbrImportances();
        boolean shouldRequest = false;
        
        // Update all importances for the tile 
        for(int importance=0; importance<nbrImportances; importance++) {            
            if(isString) {
                shouldRequest = wrapper.shouldRequestString(importance);
            } else {
                shouldRequest = wrapper.shouldRequestGeoData(importance);
            }
            
            if(shouldRequest) {             
                if(isString)
                    wrapper.setRequestedString(importance);
                else
                    wrapper.setRequested(importance);
                
                // Create the ParamString
                TileMapParams iTileMapParams = 
                    createTileMapParams(wrapper.getLayerID(), importance, 
                            wrapper.getTileIndexLat(), wrapper.getTileIndexLon(), 
                            wrapper.getDetailLevel(), wrapper.getTileID(), false, isString); 
                buffer.softReset();
                String paramString = iTileMapParams.getAsString(buffer);
                
                if(resetFromMemCache) {
                    if(LOG.isTrace()) {
                        LOG.trace("TileMapHandler.updateOneTileMap()", paramString+" removed from memory cache!");
                    }
                    iTileMapLoader.removeFromMemCache(paramString);
                }
                
                if(!isString)
                    wrapper.setGeoTileMapParamString(paramString, importance);
                
                iTileMapLoader.request(paramString, iTileMapParams, wrapper);
            }
        }        
    }
    
    //XXX: Debug only
//  public void printLoadedWrappers() {
//      
//      System.out.println("TileMapHandler.printLoadedWrappers() iRequestedWrapper.size= "+iRequestedWrapper.size());
//      
//      Enumeration params = iRequestedWrapper.elements();     
//        while(params.hasMoreElements()) {                       
//            TileMapLayerWrapper wrapper = (TileMapLayerWrapper)params.nextElement();
//            
//            int layerNbr = tmfd.getLayerNbrFromID(wrapper.getLayerID());
//            int mc2unit = (int)tmfd.getMc2UnitsPerTile(layerNbr, wrapper.getDetailLevel());
//            int lat = wrapper.getTileIndexLat() * mc2unit;
//            int lon = wrapper.getTileIndexLon() * mc2unit;            
//            QuadTreeNode node = ((FileCache)iTileMapLoader.getCache()).getNode(lat, lon);
//            
//            String name = null;
//            boolean loaded = false;
//            if(node != null) {
//                name = node.getName();
//                loaded = (node.getAllEntrys()!=null);
//            }
//            
//            System.out.println("tileID: "+wrapper.getTileID()+
//                                " lat= "+lat+
//                                " lon= "+lon+
//                                " detailLevel= "+wrapper.getDetailLevel()+
//                                " layerID= "+wrapper.getLayerID()+
//                                " nodeName: "+name+
//                                " loaded: "+loaded);
//        }       
//  }
    
    
    /**
     * Remove tiles that are outside the bounding box. 
     * 
     * @param layerID 
     * @param detailLevel
     * @param aTileIndices
     */
    private void removeUnseenTileMaps(int layerID, int detailLevel, int[] aTileIndices, int nbrImportances, boolean overviewMap) {   
        
        Enumeration params = iRequestedWrapper.elements();      
        while(params.hasMoreElements()) {           
            TileMapLayerWrapper wrapper = (TileMapLayerWrapper)params.nextElement();            
            if(wrapper.isOverviewMap() == overviewMap) {                
                if((wrapper.getLayerID() == layerID) && ((wrapper.getDetailLevel() != detailLevel) ||
                        wrapper.getTileIndexLat() < aTileIndices[0] || wrapper.getTileIndexLat() > aTileIndices[1] ||
                        wrapper.getTileIndexLon() < aTileIndices[2] || wrapper.getTileIndexLon() > aTileIndices[3]
                        )) {
                        // wrapper.getNbrImportances() != nbrImportances
                        
                    internalRemoveLoadedWrapper(wrapper);
                }
            }
        }       
    }
    
    /*
     * Internal method for removing a loaded TileMapLayerWrapper. 
     */
    private void internalRemoveLoadedWrapper(TileMapLayerWrapper wrapper) {
        if(wrapper.getNbrOfImpToCache() > 0) {
            iTileMapLoader.getCache().writeDataToCache(wrapper.getDataToCache(), 
                                                       wrapper.getParamsToCache(),
                                                       wrapper.getTileIDParam(), 
                                                       wrapper.getTotalSize(), 
                                                       wrapper.getNbrOfImpToCache(),
                                                       (short)wrapper.getEmptyImportances());
            wrapper.purgeTileMaps();
        }
        
        if(wrapper.isOverviewMap())
            iRequestedWrapper.remove(wrapper.getTileIDParam().getTileID());
        else
            iRequestedWrapper.remove(wrapper.getTileID());
        
        /* Update the cache with the currently loaded wrappers. */
        iTileMapLoader.getCache().updateLoadedNodes(wrapper, iRequestedWrapper);
        
        if(iExtractionListener != null) {
            if(!wrapper.isOverviewMap())
                iExtractionListener.removeTileMap(wrapper.getTileIDParam());
            else
                iExtractionListener.removeOverviewMap(wrapper.getTileIDParam().getTileID());
        }
    }
    
    /**
     * Create a new TileMapParams. 
     * 
     * @param layerID
     * @param importance
     * @param lat in MC2
     * @param lon in MC2
     * @param detailLevel 
     * @param tileID
     * @param overviewMap
     * @param isString
     * @return
     */
    private TileMapParams createTileMapParams(int layerID, int importance, int lat, int lon, 
            int detailLevel, String tileID, boolean overviewMap, boolean isString) {
        TileMapParams iTileMapParams = new TileMapParams();
        iTileMapParams.setParams(tmfd.getServerPrefix(),
                  GZIP,
                  layerID,
                  (isString==true)?TileMapParams.STRINGS:TileMapParams.MAP,
                  importance,
                  // geodata is SWEDISH other is the selected language
                  (isString==true)?iTileMapControl.getLanguage():LangTypes.SWEDISH,
                  lat,
                  lon,
                  detailLevel,
                  iRouteID,
                  tileID,
                  overviewMap );
        
        return iTileMapParams;
    }
    
    /**
     * Return the param string for importance 0 for the tile. That string
     * is used as a ID for the TileMapLayerWrapper. 
     * 
     * @param lat (MC2)
     * @param lon (MC2)
     * @param layerID
     * @param detailLevel
     * @return the id string for the tile
     */
    private TileMapParams getTileID(int lat, int lon, int layerID, int detailLevel) {
        TileMapParams iTileMapParams = new TileMapParams();
        iTileMapParams.setParams(tmfd.getServerPrefix(),
                GZIP,
                layerID,
                TileMapParams.MAP,
                0, // use importance 0 for the tile id. 
                LangTypes.SWEDISH,
                lat,
                lon,
                detailLevel,
                iRouteID,
                "");
        return iTileMapParams;
    }
}
