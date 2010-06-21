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
package com.wayfinder.core.map.vectormap.internal;

import java.util.Vector;

import com.wayfinder.core.map.CopyrightHandler;
import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.internal.control.TileMapExtractionListener;
import com.wayfinder.core.map.vectormap.internal.drawer.MapRenderTask;
import com.wayfinder.core.map.vectormap.internal.drawer.RenderManager;
import com.wayfinder.core.map.vectormap.internal.drawer.TileMapWrapper;
import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.graphics.WFFont;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFGraphicsFactory;


/**
 * 
 * 
 */
public class WFTileMapHolder implements TileMapExtractionListener {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(WFTileMapHolder.class);

    private Vector iNewTileMaps;
    private LinkedList iTilesToRemove;
    private TileMapFormatDesc tmfd;
    
    private VectorMapModule iVectorInterface;
    
    private Vector overviewMaps;
    private LinkedList iOverviewMapsToRemove;
    
    private CopyrightHandler copyrightHandler;
    private ScreenInfo iScreenInfo;
    
    private MapStorage m_MapStorage;
    private RenderManager m_RenderManager;
        
    public WFTileMapHolder(RenderManager aRenderManager) {
        m_RenderManager = aRenderManager;
    }
    
    public void init(VectorMapModule aVectorInterface, int w, int h) {
        iTilesToRemove     = new LinkedList();
        iNewTileMaps    = new Vector();
        iVectorInterface = aVectorInterface;
        copyrightHandler = new CopyrightHandler();
        iScreenInfo = new ScreenInfo(w, h, true);
        m_MapStorage = new MapStorage();
        tmfd = null;
        overviewMaps = null;
        iOverviewMapsToRemove = null;
    }
    
    /**
     * The update method needs to be called before getTileMaps() to 
     * update the array with new extracted tilemaps.  
     * 
     * @return the array with the extracted tilemaps
     */
    public Vector []getTileMaps() {
        return m_MapStorage.getTileMaps();
    }
    
    /**
     * @return the vector with the current visible overview maps
     */
    public Vector getOverviewMaps() {
        return overviewMaps;
    }
    
    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapExtractionListener#addExtractedOverviewMap(wmmg.map.vectormap.wayfinder.TileMap)
     */
    public void addExtractedOverviewMap(TileMap tileMap) {  
        TileMapParams params = tileMap.getTileMapParams();
        TileMapWrapper t = new TileMapWrapper(params.getLayerID(),params.getDetailLevel(),params.getTileIndexLat(),
                                              params.getTileIndexLon(),params.getTileID(),tileMap.getNumberOfImportance(), 
                                              tileMap.getMaxNbrOfImportance());
        t.addTileMap(tileMap, params.getImportance(), false);
        t.setRender(params.getImportance(), true);
        synchronized(overviewMaps) {            
            overviewMaps.addElement(t);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapExtractionListener#removeOverviewMap(java.lang.String)
     */
    public void removeOverviewMap(String tileID) {      
        synchronized(iOverviewMapsToRemove) {
            iOverviewMapsToRemove.addLast(tileID);
        }       
    }
    
    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapExtractionListener#addExtractedTileMap(wmmg.map.vectormap.wayfinder.TileMap)
     */
    public void addExtractedTileMap(TileMap tileMap) {
        if(tileMap != null) {
            iNewTileMaps.addElement(tileMap);
            iVectorInterface.requestMapUpdate();
        }
    }

    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapExtractionListener#addExtractedTileMaps(wmmg.map.vectormap.wayfinder.TileMap[])
     */
    public void addExtractedTileMaps(TileMap[] tileMaps) {      
        if(tileMaps != null) {
            for(int i=0; i<tileMaps.length; i++) {
                addExtractedTileMap(tileMaps[i]);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapExtractionListener#removeTileMap(java.lang.String, int)
     */
    public void removeTileMap(TileMapParams aTileIDParam) {
        synchronized(iTilesToRemove) {
            iTilesToRemove.addLast(aTileIDParam);
        }
    }
    
    /**
     * Updates the overview maps and the TileMaps. 
     * 
     * @param zoomLevel, the current zoom level in the map. 
     * @return true if any new tilemaps has been added. 
     */
    public boolean update(int zoomLevel, int []cambb) {
        m_MapStorage.initBeforeUpdate(zoomLevel, cambb);
        updateOverviewMaps();
        removeUnseenTiles();
        return updateNewTileMaps(zoomLevel);
    }
    
    /*
     * Remove overview maps that isn't visible any more. 
     */
    private void updateOverviewMaps() {
        synchronized (iOverviewMapsToRemove) {
            String tileID = null;
            while(iOverviewMapsToRemove.size() > 0) {
                tileID = (String)iOverviewMapsToRemove.removeLast();
                int size = overviewMaps.size();
                TileMapWrapper tmw = null;
                for(int i=(size-1); i>=0; i--) {
                    tmw = (TileMapWrapper)overviewMaps.elementAt(i);
                    if(tmw.getTileID().equals(tileID)) {
                        overviewMaps.removeElementAt(i);                        
                    }
                }
            }
        }
    }
    
    /*
     * Add new extracted tile maps to the draw array. 
     */
    private boolean updateNewTileMaps(int zoomLevel) {
        
        if(iNewTileMaps.size() == 0)
            return false;
                
        try {                       
            synchronized (iNewTileMaps) {
                final int tilemapsize = iNewTileMaps.size();            
                TileMap tileMap = null;
                // For all new tileMaps to be added
                for(int i=0; i<tilemapsize; i++) {
                    tileMap = (TileMap)iNewTileMaps.elementAt(i);
                    m_MapStorage.addNewExtractedTileMap(tileMap);
                }
                // Check to see if the strings are ready to be proceed 
                m_MapStorage.updateLoadedStringAndMapData();
                iNewTileMaps.removeAllElements();
            }
        } catch (RuntimeException e) {
            if(LOG.isError()) {
                LOG.error("WFTileMapHolder.updateNewTileMaps()", e);
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("WFTileMapHolder.updateNewTileMaps()", e);
            }
        }
        
        return true;
    }   
    
    /**
     * Remove any tiles the are outside the current tile indices or has 
     * wrong detail level. 
     */
    private void removeUnseenTiles() {  
        
        if(iTilesToRemove.size() == 0)
            return;
        
        synchronized (iTilesToRemove) {
            while(iTilesToRemove.size() > 0) {
                TileMapParams tileIDParam = (TileMapParams)iTilesToRemove.removeFirst();
                m_MapStorage.removeUnseenTiles(tileIDParam);             
            }
        }
    }
    
    /**
     * @return the current used detail level for the layer number specified by
     * the parameter. 
     */
    public int getCurrentDetailLevel(int layerNbr) {
        return m_MapStorage.getCurrentDetailLevel(layerNbr);
    }
    
    /**
     * clean up the memory
     */
    public void purgeData() {
        Vector []tileMapPerLayerNumberArray = m_MapStorage.getTileMaps();
        
        // If we exit the map view before the tmfd has been downloaded, the
        // tileMapPerLayerNumberArray hasn't been created. 
        if(tileMapPerLayerNumberArray != null) {
            synchronized (tileMapPerLayerNumberArray) {
                int size = tileMapPerLayerNumberArray.length;
                for(int i=0; i<size; i++) {
                    tileMapPerLayerNumberArray[i].removeAllElements();
                }
            }       
        }
    }
    
    // ----------------------------------------------------------------------------------------
    // String methods
    
    /**
     * Process the strings if they are ready. 
     * 
     * @return if the strings are ready to be drawn on the screen. 
     */
    public boolean updateStrings(WFGraphicsFactory aFactory, WFGraphics g) {
        return m_MapStorage.updateStrings(aFactory, g);
    }
    
    /**
     * Clear any processed strings. This method must be called before any
     * new strings will be processed. 
     */
    public void clearStrings() {
        m_MapStorage.clearStrings();
    }
    
    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapExtractionListener#addExtractedTileMapFormatDesc(wmmg.map.vectormap.wayfinder.TileMapFormatDesc)
     */
    public void addExtractedTileMapFormatDesc(TileMapFormatDesc tileMapFormatDesc) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, null);
        task.requestNewTileMapFormatDesc(tileMapFormatDesc);
        iVectorInterface.updateMapStartedStatus();

        if(LOG.isInfo()) {
            LOG.info("WFTileMapHolder.addExtractedTileMapFormatDesc()", "new TileMapFormatDesc extracted: tmfd= "+tmfd);
        }
    }
    
    public void setNewTileMapFormatDesc(TileMapFormatDesc aTmfd) {
        tmfd = aTmfd;
        overviewMaps = new Vector();
        iOverviewMapsToRemove = new LinkedList();
        m_MapStorage.init(tmfd,iScreenInfo);
        updateCopyrightHandler();
    }
    
    //-------------------------------------------------------------------------------------------------
    // Copyright handler
    
    /**
     * Update the copyrigth handler with the new TileMapFormatDesc
     */
    private void updateCopyrightHandler() {     
       if ( tmfd.getCopyrightHolder()!= null ) {
          // Set the CopyrightHolder to the CopyrightHandler.
          copyrightHandler.setCopyrightHolder(tmfd.getCopyrightHolder());
       } else {
          // Set the old version of copyright data from TMFD.
          copyrightHandler.setStaticCopyrightString(tmfd.getStaticCopyrightString());
          //System.out.println("No copyrightHolder from tmd!");
       }

    }
    
    /**
     * Returns the copyright string from the TMFD
     *
     * @return the copyright string
     */
    public String getCopyrightString(BoundingBox screenBox, WFFont font, int screenWidth){
        return copyrightHandler.getCopyrightString(screenBox, font, screenWidth);
    }
    
    public CopyrightHandler getCopyrightHandler() {
        return copyrightHandler;
    }
    
    /**
     * Clear the route layer. This method is needed to make sure that all route tiles
     * are removed when we call setRouteID(String) with a empty route id. Otherwise we 
     * will wait to remove the route tiles until:
     * 
     * 1. They are outside the screen
     * 2. We have something better to draw. 
     */
    public void clearRouteLayer() {
        m_MapStorage.clearRouteLayer();
    }
    
    public void setRouteID(String routeID) {
        m_MapStorage.setRouteID(routeID);
    }
    
    public boolean allMapDataLoaded() {
        return m_MapStorage.allMapDataLoaded();
    }
    
    // ---------------------------------------------------------------------------------------------------------
    // Debug    
    public void printDebug(int zoomLevel) {     
        m_MapStorage.printDebug(zoomLevel);
    }
    
}
