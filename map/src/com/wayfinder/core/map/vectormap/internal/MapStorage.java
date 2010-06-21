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

import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.internal.drawer.TileMapWrapper;
import com.wayfinder.core.map.vectormap.internal.process.TileImportanceNotice;
import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFGraphicsFactory;

/**
 * 
 * 
 * 
 * 
 *
 */
public class MapStorage {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(MapStorage.class);
    
    /* Holds the current detail level for all the layer number */
    private int []iCurrentDetailLevel;
    
    /* Holds the total number of tiles visible inside the current bounding box for 
     * all layer number*/
    private int []iTotalNumberOfTiles;
    
    /* Int array buffer that are used to calculate which tile indices that are visible. 
     * The int array will be re-used when calculate the tile indices for each layer. */
    private int []tmpTileIndices = new int[4];
    
    /* Holds the tiles to draw for each layer */
    private Vector []tileMapWrappersPerLayerNumberArray;
    
    /* The current active TileMapForamtDesc */
    private TileMapFormatDesc tmfd;
    
    private ScreenInfo iScreenInfo;
    
    /* Variable that indicate if all the string tiles are loaded so that the strings
     * are ready to be processed. */
    private boolean iStringsAreReadyToBeProcessed = false;
    
    // Indicate if all map data has been loaded
    private boolean iIsAllMapDataLoaded = false;
    
    // Hold the curren route id string or null if no route is set.
    private String m_CurrentRouteID = null;
    
    /**
     * Standard ctor
     */
    public MapStorage() {
    }
    
    /**
     * @return the tiles to draw. 
     * 
     * The tiles are sorted in layer number order.
     *  
     */
    Vector []getTileMaps() {
        return tileMapWrappersPerLayerNumberArray;
    }
    
    /** 
     * @return the current detail level for the layer number. 
     */
    int getCurrentDetailLevel(int layerNbr) {
        return iCurrentDetailLevel[layerNbr];
    }
    
    void setRouteID(String routeID) {
        m_CurrentRouteID = routeID;
    }
    
    /**
     * 
     * Add a new extracted TileMap. 
     * 
     * @param tileMap
     */
    void addNewExtractedTileMap(TileMap tileMap) {
        
        TileMapParams params = tileMap.getTileMapParams();
        int layerNbr = tmfd.getLayerNbrFromID(params.getLayerID());
        
        /* Don't add tile maps with wrong detail level */
        if(params.getDetailLevel() != iCurrentDetailLevel[layerNbr])
            return;
        
        // Don't add tile maps with wrong route id
        // This can happen if a new route tile
        if(layerNbr == ROUTE_LAYER_NBR) {
            if(!params.getRouteID().equals(m_CurrentRouteID)) {
                if(LOG.isError()) {
                    LOG.error("MapStorage.addNewExtractedTileMap()", "Skip to add a non acive route tile with ID: "+
                            params.getRouteID());
                }
                return;
            }
        }
        
        boolean shouldAddNewTileMapWrapper = true;      
        final int size = tileMapWrappersPerLayerNumberArray[layerNbr].size();
        
        // For all existing TileMaps
        for(int j=0; j<size; j++) {
            TileMapWrapper tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[layerNbr].elementAt(j);
            
            // If the tileID are the same we add the TileMap to the existing wrapper
            if(tmw.getTileID().equals(params.getTileID())) {                                
                if(params.getTileMapType() == TileMapParams.MAP) {
                    
                    /* If the importance has already been loaded into memory we ignore it. 
                     * This can happen if you zoom in and out very fast and the tiles
                     * hasn't been updated between the zooming. */
                    if(tmw.isGeoMapLoaded(params.getImportance())) {
                        return;
                    }
                    
                    
                    /* Set the sort value for the importance */
                    TileImportanceNotice tin = 
                        tmfd.getTileImportanceNotice(params.getLayerID(), 
                                                     params.getDetailLevel(), 
                                                     params.getImportance());               
                    tmw.setTileNoticeSortValue(params.getImportance(), tin.getSortValue());
                    
                    // Add GEODATA MAP
                    tmw.addTileMap(tileMap, tileMap.getImportance(), false);
                    tmw.setEmptyImportances(tileMap.getEmptyImportances());
                    
                    /*
                     * Remove any tiles with wrong detail level if all the visible tiles has
                     * been fully loaded.
                     * 
                     * Do the importance replace if all the tiles has the current importance
                     * 
                     */
                    int coverageNbr = hasFullCoverage(params.getImportance(), layerNbr);                    
                    if(coverageNbr == 1)
                        importanceReplace(params.getImportance(), layerNbr);                        
                    else if(coverageNbr == 2)
                        removeTilesWithWrongDetailLevel(layerNbr);
                                            
                } else {
                    // ADD STRING MAP
                    tmw.addTileMap(tileMap, tileMap.getImportance(), true);                             
                }                           
                shouldAddNewTileMapWrapper = false;
                break;                          
            }
        }
        
        // If we can't found any existing TileMapWrapper for the tilemap we create a new one
        // and add it to the extracted wrapper array. 
        // 
        // If the tilemap are a string tile we just ignore it because the tile has already been 
        // removed. This can happen when the user zoom / pan fast. 
        if(shouldAddNewTileMapWrapper && (params.getTileMapType() == TileMapParams.MAP)) {
            if(LOG.isTrace()) {
                LOG.trace("MapStorage.addNewExtractedTileMap()", 
                        "Add new TileMapWrapper: lat= "+params.getTileIndexLat()+" lon= "+params.getTileIndexLon());
            }
            
            TileMapWrapper t = 
                new TileMapWrapper(params.getLayerID(),params.getDetailLevel(),
                        params.getTileIndexLat(),params.getTileIndexLon(),
                        params.getTileID(),tileMap.getNumberOfImportance(),
                        tileMap.getMaxNbrOfImportance());

            TileImportanceNotice tin = 
                tmfd.getTileImportanceNotice(params.getLayerID(), 
                                             params.getDetailLevel(), 
                                             params.getImportance());                                       
            t.setTileNoticeSortValue(params.getImportance(), tin.getSortValue());
            
            t.addTileMap(tileMap, tileMap.getImportance(), false);  
            t.setEmptyImportances(tileMap.getEmptyImportances());
            
            tileMapWrappersPerLayerNumberArray[layerNbr].addElement(t);
            
            if(t.isDone()) {
                removeTilesWithWrongDetailLevel(layerNbr);
            }       
        }
    }
        
    /**
     * 
     * The method check the coverage for the current importance for the layer number 
     * specified by the parameter. 
     * 
     * @param imp, the importance number
     * @param layerNbr, the layer number
     * 
     * @return 1. if the all tiles for the current importance has been loaded
     *         2. if all the tiles for the current detail level are fully loaded
     *            (all the geo data has been loaded).
     *         
     *         else return 0. 
     * 
     */
    public int hasFullCoverage(int imp, int layerNbr) {
        
        int nbrOfTiles = 0; 
        int nbrOfTilesDone = 0;
        final int size = tileMapWrappersPerLayerNumberArray[layerNbr].size();
        TileMapWrapper tmw;
        
        for(int i=0; i<size; i++) {
            tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[layerNbr].elementAt(i);
            if(tmw.getDetailLevel() == iCurrentDetailLevel[layerNbr]) {
                if(tmw.isGeoMapLoaded(imp) || tmw.isEmptyImportances(imp)) {
                    nbrOfTiles++;
                    if(tmw.isDone())
                        nbrOfTilesDone++;
                }
            }
        }
        
        /* Note that there temporarily can be more tiles in the memory that should
         * be drawn, thats why we check that we at least have equal or more tiles
         * loaded or tiles done. The tile that are not visible will be removed next
         * time we update the tiles in the TileMapControlThread class.*/
        
        if(iTotalNumberOfTiles[layerNbr]<=nbrOfTilesDone)
            return 2;
        else if(iTotalNumberOfTiles[layerNbr]<=nbrOfTiles)
            return 1;

        
        return 0;
    }
    
    /**
     * Remove all tile maps with wrong detail level. 
     * Set all loaded maps to be rendered.   
     * 
     * This method will be called when all the importance for the layer number 
     * has been loaded. 
     * 
     * @param layerNbr, the layer number
     */
    private void removeTilesWithWrongDetailLevel(int layerNbr) {
        
        final int size = tileMapWrappersPerLayerNumberArray[layerNbr].size();
        TileMapWrapper tmw;
        
        for(int i=(size-1); i>=0; i--) {
            tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[layerNbr].elementAt(i);
            if(tmw.getDetailLevel() == iCurrentDetailLevel[layerNbr]) {
                tmw.setAllRendered();
            } else {                            
                tileMapWrappersPerLayerNumberArray[layerNbr].removeElementAt(i);
            }
        }       
    }
    
    /**
     * For the map and route layer we use the importance replace when zooming that's
     * why only tiles that are outside the current bounding box will be removed. 
     * 
     * For all other layer we remove tiles that are outside the current bounding box
     * and has wrong detail level. 
     * 
     * @param aTileIDParam, the TileMapParams for the TileMap. 
     */
    void removeUnseenTiles(TileMapParams aTileIDParam) {
        
        String tileID = aTileIDParam.getAsString();
        int layerNbr = tmfd.getLayerNbrFromID(aTileIDParam.getLayerID());
        int size = tileMapWrappersPerLayerNumberArray[layerNbr].size();
        for(int j=(size-1); j>=0; j--) {
            TileMapWrapper tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[layerNbr].elementAt(j);                         
            if(tmw.getTileID().equals(tileID)) {        
                /* Only do importance replace for layer ID 0 and 1 (route and map)*/
                if(tmw.getLayerID() < 2) {
                    if(tmw.getDetailLevel() == iCurrentDetailLevel[layerNbr]) {
                        tileMapWrappersPerLayerNumberArray[layerNbr].removeElementAt(j);
                    }
                } else {
                    tileMapWrappersPerLayerNumberArray[layerNbr].removeElementAt(j);
                }               
            }
        }       
    }
    
    /**
     * 
     * The importance replace method make sure that one loaded importance will be replaced
     * with the correct new importance. 
     * 
     * We can't just replace importance 3 in detail level 2 with importance 3 in detail level 2. 
     * They might not contain the same features. Please see the sort value in the TileMapNotice class
     * for more information. 
     * 
     * @param imp, the importance 
     * @param layerNbr, the layer number
     */
    void importanceReplace(int imp, int layerNbr) {
        
        final int size = tileMapWrappersPerLayerNumberArray[layerNbr].size();
        TileMapWrapper tmw;
        int impToTurnOff = -1;
        
        for(int i=0; i<size; i++) {
            tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[layerNbr].elementAt(i);
            if(tmw.getDetailLevel() != iCurrentDetailLevel[layerNbr]) {
                
                if(impToTurnOff == -1 ) {
                
                    /* If the current importance are larger then the loaded map then there are
                     * no matching detail level to replace with */
                    if(imp < tmw.getNbrImportances()) {                     
                        /* Search for the matching importance in the old detail level. */
                        for(int j=0; j<tmw.getNbrImportances(); j++) {
                            
                            /* If the sort value are the same we can replace the new replace that importance*/
                            if(tmw.getTileNoticeSortValue(imp) == tmw.getTileNoticeSortValue(j)) {
                                impToTurnOff=j;
                                tmw.setRender(j, false);
                                break;
                            }
                        }
                    }
                } else {
                    /* We only have to find the matching importance to replace for one tile. For
                     * tile 2 and more we can just use the same importance */
                    tmw.setRender(impToTurnOff, false);
                }
                
            } else {
                /* Turn on the importance for the current importance... */
                tmw.setRender(imp, true);
            }
        }
    }
    
    //----------------------------------------------------------------------------------------
    // Init methods 
    
    /**
     * 
     * Calculate the detail level and total number of visible tiles for each layer. 
     * 
     * @param aZoomLevel, the current scale.
     * @param cambb, the current camera bounding box in MC2 coordinates. 
     */
    void initBeforeUpdate(int aZoomLevel, int []cambb) {
        
        int layerID;
        int nbrLat;
        int nbrLon;
        
        // For all layer 
        for(int layerNbr=0; layerNbr<tmfd.getNumberOfLayers(); layerNbr++) {
            layerID = tmfd.getLayerIDFromLayerNbr(layerNbr);
            
            /* Set the detail level for the layer number*/
            iCurrentDetailLevel[layerNbr] = tmfd.getCurrentDetailLevel(layerNbr, aZoomLevel);
            
            /* Set the total number of tiles visible inside the current bounding box. */
            tmfd.getTileIndex(cambb, layerID, iCurrentDetailLevel[layerNbr],tmpTileIndices);                        
            nbrLat = (tmpTileIndices[1] - tmpTileIndices[0]) + 1;
            nbrLon = (tmpTileIndices[3] - tmpTileIndices[2]) + 1;           
            iTotalNumberOfTiles[layerNbr] = nbrLat * nbrLon;
            
        }
    }
    
    private int MAP_LAYER_NBR;
    private int ROUTE_LAYER_NBR;
    
    /**
     * 
     * Init when a new TileMapFormatDesc has been loaded. 
     * 
     * @param aTmfd, The current active TileMapFormatDesc.
     * @param aScreenInfo, the screen info object. 
     */
    void init(TileMapFormatDesc aTmfd, ScreenInfo aScreenInfo) {
        
        tmfd = aTmfd;
        iScreenInfo = aScreenInfo;
        
        int nbrLayer = tmfd.getNumberOfLayers();
        
        iCurrentDetailLevel = new int[nbrLayer];
        iTotalNumberOfTiles = new int[nbrLayer];
        
        tileMapWrappersPerLayerNumberArray = new Vector[tmfd.getNumberOfLayers()];
        for(int i=0;i<tmfd.getNumberOfLayers();i++){
            tileMapWrappersPerLayerNumberArray[i] = new Vector();
        }
        
        MAP_LAYER_NBR = tmfd.getLayerNbrFromID(0);
        ROUTE_LAYER_NBR = tmfd.getLayerNbrFromID(1);
        
    }
    
    // ----------------------------------------------------------------------------------------
    // String methods
    /**
     * 
     * The method set the iStringsAreReadyToBeProcessed variable to true if the strings
     * are ready to be processed and iIsAllMapDataLoaded to true if all map data has
     * been loaded. 
     *
     * All the string maps for one tile will be send at ones thats why we just check
     * that any string importance has been set to rendered (if one has been loaded the other
     * has also been loaded). 
     */
    void updateLoadedStringAndMapData() {   
        int nbrOfTiles = 0;
        int nbrOfTilesDone = 0;
        TileMapWrapper tmw;
        int size = tileMapWrappersPerLayerNumberArray[MAP_LAYER_NBR].size();    
        for (int j=0; j<size; j++) {
            tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[MAP_LAYER_NBR].elementAt(j);
            
            if (tmw.getDetailLevel() == iCurrentDetailLevel[MAP_LAYER_NBR]) {
               if (tmw.getRenderStringImportance() != 0) {                              
                   nbrOfTiles++;                                        
               }
               if (tmw.isDone()) {
                   nbrOfTilesDone++;
               }
            }
        }
        
        iStringsAreReadyToBeProcessed = (nbrOfTiles == iTotalNumberOfTiles[MAP_LAYER_NBR]);
        iIsAllMapDataLoaded = (nbrOfTilesDone == iTotalNumberOfTiles[MAP_LAYER_NBR]);
    }
    
    /**
     * 
     * Updates the strings that will be shown in the map. The strings are only updated when 
     * the map is not moving and all string tiles are loaded.
     * 
     * @param graphicsFactory  instance of graphics factory
     * @param g  graphics object
     * 
     * @return true if the strings are ready to be drawn. 
     */
    public boolean updateStrings(WFGraphicsFactory graphicsFactory, WFGraphics g) {
        boolean retValue = false;
        
        if (iStringsAreReadyToBeProcessed) {   
            iClearStrings = true;
            TileMapWrapper tmw = null;
            int size = tileMapWrappersPerLayerNumberArray[MAP_LAYER_NBR].size();                
            for (int i=0; i<size; i++) {
                tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[MAP_LAYER_NBR].elementAt(i);                       
                if (tmw.getDetailLevel() == iCurrentDetailLevel[MAP_LAYER_NBR]) {
                    retValue = tmw.placeTexts(iScreenInfo, graphicsFactory, tmfd, g);
                }
            }
            
            if(LOG.isTrace()) {
                LOG.trace("MapStorage.updateStrings()", "returns "+retValue);
            }
        }
        
        return retValue;
    }
    
    
    private boolean iClearStrings = false;
    
    /**
     * Clear the processed strings
     */ 
    void clearStrings() {
        if(iClearStrings && iStringsAreReadyToBeProcessed) {
            if(LOG.isTrace()) {
                LOG.trace("MapStorage.clearStrings()", "");
            }
            
            iClearStrings = false;
            int size = tileMapWrappersPerLayerNumberArray[MAP_LAYER_NBR].size();
            for(int j=0; j<size; j++) {
                TileMapWrapper tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[MAP_LAYER_NBR].elementAt(j);                    
                tmw.clearStrings();
            }
        }
    }

    /**
     * Clear the route layer. This method is needed to make sure that all route tiles
     * are removed when we call setRouteID(String) with a empty route id. Otherwise we 
     * will wait to remove the route tiles until:
     * 
     * 1. They are outside the screen
     * 2. We have something better to draw. 
     * 
     */
    public void clearRouteLayer() {
        if(tileMapWrappersPerLayerNumberArray != null) {
            tileMapWrappersPerLayerNumberArray[ROUTE_LAYER_NBR].removeAllElements();    
        }
    }
    
    /**
     * Check if all map data at the current visible layers have been loaded
     * 
     * @return true if map data has been loaded, false otherwise
     */
    
    public boolean allMapDataLoaded() {
        return iIsAllMapDataLoaded;
    }
    
    // -------------------------------------------------------------------------------------
    // Debug
    public void printDebug(int zoomLevel) {
        
        if(LOG.isError()) {
            LOG.error("MapStorage.printDebug()", "tileMapWrappersPerLayerNumberArray.size= "+
                tileMapWrappersPerLayerNumberArray.length);
        
            for(int i=0; i<tileMapWrappersPerLayerNumberArray.length; i++) {
                Vector v = tileMapWrappersPerLayerNumberArray[i];
                for(int j=0; j<v.size(); j++) {
                    TileMapWrapper tmw = (TileMapWrapper)v.elementAt(j);
                    
                    int nbrImp = tmfd.getNbrImportances(zoomLevel, tmw.getDetailLevel(), tmw.getLayerID());
                    LOG.error("MapStorage.printDebug()", 
                            "START: layerID= "+tmw.getLayerID()+" lat= "+tmw.getLatitude()+
                            " lon= "+tmw.getLongitude()+" nbrImp= "+tmw.getNbrImportances()+
                            " realNbrImp= "+nbrImp+" detailLevel= "+tmw.getDetailLevel());
                                
                    LOG.error("MapStorage.printDebug()",
                            "loadedMaps:   "+Integer.toBinaryString(tmw.getLoadedMaps() | (0x1<<31))+
                            " emptyImportances: "+Integer.toBinaryString(tmw.getEmptyImportances() | (0x1<<31)));
                            
                    LOG.error("MapStorage.printDebug()",
                            "mapsToRender: "+Integer.toBinaryString(tmw.getRender() | (0x1<<31))+
                            " renderStrings:    "+Integer.toBinaryString(tmw.getRenderStringImportance() | (0x1<<31))+" :END");                                                             
                }
            }
        }
    }
}
