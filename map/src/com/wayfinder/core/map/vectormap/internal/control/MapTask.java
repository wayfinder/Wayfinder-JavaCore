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

import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;


/**
 * 
 * This class handles external and internal tasks that needs to be handled by
 * the map component.  
 * 
 *
 */
public class MapTask {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(MapTask.class);
    
    private static final int ID_REMOVE_AND_RELOAD_TMFD          = 0;
    private static final int ID_REMOVE_AND_RELOAD_ONE_TILEMAP   = 1;
    private static final int ID_RELOAD_TILE_ID                  = 2;
    private static final int ID_LOAD_TEXT_MAP                   = 3;
    private static final int ID_RESET_LAYER                     = 4;
    private static final int ID_LOAD_BITMAP                     = 5;
    private static final int ID_REMOVE_AND_RELOAD_TILEMAPS      = 6;
    private static final int ID_SET_NEW_TMFD_IN_APPLICATION     = 7;
    private static final int ID_REFRESH_ALL_LAYERS              = 8;
    private static final int ID_SAVE_CACHE                      = 9;
    private static final int ID_SET_NEW_ROUTE_ID                = 10;
    private static final int ID_SET_NIGHT_OR_DAY_MODE           = 11;
    private static final int ID_SET_POI_CATEGORIES              = 12;
    private static final int ID_SET_DOWNLOAD_STRING_TILE        = 13;
    
    private String iIDString;   
    private String iGeoParam, iStringParam;
    private int iID, iLayerNbr; 
    private boolean iIsNightMode = false;
    private boolean m_DownloadStringTile = false;
    private TileMapFormatDesc iTmfd;
    private TileMapParams iTileMapParams = null;    
    private TileMapControlThread iTileMapControlThread;
    private PoiCategory []m_poiCategories;
    
    public MapTask(TileMapControlThread aTileMapControlThread) {
        iTileMapControlThread = aTileMapControlThread;
    }
    
    public void execute() {
        
        TileMapLoader iTileMapLoader = iTileMapControlThread.getTileMapLoader();    
        TileMapHandler iTileMapHandler = iTileMapControlThread.getTileMapHandler();
        
        switch(iID) {
            case ID_LOAD_TEXT_MAP:
                //System.out.println("ID_LOAD_TEXT_MAP layerNbr= "+iLayerNbr);
                iTileMapHandler.internalLoadTextMaps(iLayerNbr);
                break;
                
            case ID_RESET_LAYER:
                //System.out.println("ID_RESET_LAYER layerNbr= "+iLayerNbr);
                iTileMapHandler.internalResetLayer(iLayerNbr);
                break;
                
            case ID_REFRESH_ALL_LAYERS:
                //System.out.println("ID_REFRESH_ALL_LAYERS ");
                iTileMapHandler.refreshAllTiles();
                break;
                
            case ID_RELOAD_TILE_ID:
                //System.out.println("ID_RELOAD_TILE_ID paramString= "+iIDString);
                iTileMapHandler.internalReloadTileID(iIDString);
                break;
                                
            case ID_LOAD_BITMAP:
                //System.out.println("ID_LOAD_BITMAP paramString= "+iIDString);
                TileMapParams params = new TileMapParams(iIDString,iIDString);
                iTileMapLoader.request(params.getAsString(), params, null);
                break;
                
            case ID_REMOVE_AND_RELOAD_TMFD:
                //System.out.println("ID_REMOVE_AND_RELOAD_TMFD");
                iTileMapControlThread.loadTMFD(true, false);
                break;
                
            case ID_REMOVE_AND_RELOAD_TILEMAPS:
                //System.out.println("ID_REMOVE_AND_RELOAD_TILEMAPS: geoStr= "+iGeoParam+" strStr= "+iStringParam+" tileID= "+iTileID);             
                iTileMapLoader.removeFromCache(iTileMapParams);
                iTileMapLoader.removeFromMemCache(iGeoParam);
                iTileMapLoader.removeFromMemCache(iStringParam);
                iTileMapHandler.internalReloadTileID(iTileMapParams.getTileID());
                break;
                
            case ID_REMOVE_AND_RELOAD_ONE_TILEMAP:
                //System.out.println("ID_REMOVE_AND_RELOAD_ONE_TILEMAP paramString= "+iTileMapParams.getAsString());
                iTileMapLoader.removeFromCache(iTileMapParams);
                iTileMapLoader.getMemCache().clearMemCache();
                TileMapLayerWrapper wrapper = 
                    iTileMapHandler.getRequestedWrapper(iTileMapParams.getTileID());                
                if(wrapper != null)
                    iTileMapLoader.request(iTileMapParams.getAsString(), iTileMapParams, wrapper);
                break;
                
            case ID_SET_NEW_TMFD_IN_APPLICATION:
                //System.out.println("ID_SET_NEW_TMFD_IN_APPLICATION");
                iTileMapControlThread.setNewTileMapFormatDesc(iTmfd, iIsNightMode);
                break;
                
            case ID_SAVE_CACHE:
                iTileMapLoader.getCache().saveCache();
                break;
                
            case ID_SET_NEW_ROUTE_ID:
                iTileMapHandler.internalResetLayer(iLayerNbr);
                iTileMapHandler.setRouteID(iIDString);
                break;
                
            case ID_SET_NIGHT_OR_DAY_MODE:
                if(!iTileMapControlThread.updateTMFD(iIsNightMode)) {
                    iTileMapControlThread.loadTileMapFormatDescCRC();
                } else {
                    iTileMapControlThread.notifyThread();
                }
                break;
                
            case ID_SET_POI_CATEGORIES:
                iTileMapControlThread.internalSetPoiCategories(m_poiCategories);
                int layerNbr = iTmfd.getLayerNbrFromID(2);
                iTileMapHandler.internalResetLayer(layerNbr);
                break;
                
            case ID_SET_DOWNLOAD_STRING_TILE:
                iTileMapControlThread.internalDownloadStringTile(iLayerNbr, m_DownloadStringTile);
                break;
                
            default:
                if(LOG.isError()) {
                    LOG.error("MapTask.execute()", "Unhanded map task! ID= "+iID);
                }
                break;
                
        }
                
    }   
    
    public void loadTextMap(int aLayerNbr) {
        iID = ID_LOAD_TEXT_MAP;
        iLayerNbr = aLayerNbr;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void resetLayer(int aLayerNbr) {
        iID = ID_RESET_LAYER;
        iLayerNbr = aLayerNbr;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void refreshAllLayers() {
        iID = ID_REFRESH_ALL_LAYERS;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void removeAndReloadTileMaps(String aGeoParam, String aStingParam, TileMapParams aTileIDParams) {
        iID = ID_REMOVE_AND_RELOAD_TILEMAPS;
        iGeoParam = aGeoParam;
        iStringParam = aStingParam;
        iTileMapParams = aTileIDParams;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void removeAndReloadOneTileMap(TileMapParams params) {
        iID = ID_REMOVE_AND_RELOAD_ONE_TILEMAP;
        iTileMapParams = params;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void reloadTileID(String aTileID) {
        iID = ID_RELOAD_TILE_ID;
        iIDString = aTileID;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void loadBitMap(String aParamString) {
        iID = ID_LOAD_BITMAP;
        iIDString = aParamString;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void loadTileMapFormatDesc() {
        iID = ID_REMOVE_AND_RELOAD_TMFD;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void setNewTileMapFormatDescInApplication(TileMapFormatDesc aTmfd, boolean aisNightMode) {
        iID = ID_SET_NEW_TMFD_IN_APPLICATION;
        iIsNightMode = aisNightMode;
        iTmfd = aTmfd;
        iTileMapControlThread.addMapTask(this);
    }
    
    public void saveCache() {
        iID = ID_SAVE_CACHE;
        iTileMapControlThread.addMapTask(this);
    }
    
    void setNewRouteID(String routeID, int layerNbr) {
        iID = ID_SET_NEW_ROUTE_ID;
        iIDString = routeID;        
        iLayerNbr = layerNbr;
        iTileMapControlThread.addMapTask(this);
    }
    
    void setNightMode(boolean useNightModeColors) {
        iID = ID_SET_NIGHT_OR_DAY_MODE;
        iIsNightMode = useNightModeColors;
        iTileMapControlThread.addMapTask(this);
    }
    
    void setPoiCategories(PoiCategory []poiCat, TileMapFormatDesc tmfd) {
        iID = ID_SET_POI_CATEGORIES;
        m_poiCategories = poiCat;
        iTmfd = tmfd;
        iTileMapControlThread.addMapTask(this);
    }
    
    void setDownloadStringTile(int layerNbr, boolean download) {
        iID = ID_SET_DOWNLOAD_STRING_TILE;
        iLayerNbr = layerNbr;
        m_DownloadStringTile = download;
        iTileMapControlThread.addMapTask(this);
    }
}
