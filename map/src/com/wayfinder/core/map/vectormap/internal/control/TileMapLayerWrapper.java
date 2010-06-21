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

import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;


/**
 * The TileMapLayerWrapper class handles all TileMaps for one layer of one tile.
 *  
 * It contains both geometric data and string data. 
 * 
 */
public class TileMapLayerWrapper implements TileMapLayerWrapperInterface {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapLayerWrapper.class);
    
    private int lat, lon;
    private int layerID, detailLevel;
    private int requestedImportances, receivedImportances;
    private int requestedStrings, receivedStrings;
    private int emptyImportances;
    
    /* The current number of importance */
    private int nbrImportances;
    
    /* The max number of importance for the detail level */
    private int iMaxNbrOfImportance;
    
    private TileMap []iTileMaps;
    
    private String []iGeoTileParamStrings;
    
    private long []iGeoMapCRC;
    private boolean iIsOverviewMap = false;
    
    private String iTileID;
    private TileMapParams iTileIDParam;
    
    public TileMapLayerWrapper(int aLat, int aLon, int aLayerID, int aDetailLevel, TileMapParams aTileIDParam) {        
        lat = aLat;
        lon = aLon;
        layerID = aLayerID;
        detailLevel = aDetailLevel;
        
        emptyImportances = -1;      
        requestedImportances = 0;
        receivedImportances = 0;
        requestedStrings = 0;
        receivedStrings = 0;        
        nbrImportances = 0;   
        iMaxNbrOfImportance = 0;

        iTileIDParam = aTileIDParam;
        iTileID = iTileIDParam.getAsString();
        iIsOverviewMap = false;
    }
    
    public TileMapLayerWrapper(int aLat, int aLon, int aLayerID, int aDetailLevel, 
            TileMapParams aTileIDParam, boolean aIsOverview) {
        this(aLat,aLon,aLayerID,aDetailLevel,aTileIDParam);
        iIsOverviewMap = aIsOverview;
    }
    
    /**
     * @return the id for the tile
     */
    public String getTileID() {
        return iTileID;
    }
    
    public TileMapParams getTileIDParam() {
        return iTileIDParam;
    }
    
    /**
     * @return true if the tile if a Overview map tile
     */
    public boolean isOverviewMap() {
        return iIsOverviewMap;
    }

    public boolean equals(int aLat, int aLon, int aDetailLevel, int aLayerID) {
        return (aLayerID==layerID) && (aDetailLevel== detailLevel) && (aLon==lon) && (aLat==lat);
    }
    
    public int getEmptyImportances() {
        return emptyImportances;
    }
    
    /**
     * Set all the empty importance for the tile. 
     * 
     * @param emptyImportances
     */
    public void setAllEmptyImportances(int aEmptyImportances) {
        emptyImportances = aEmptyImportances; // & 0xFFFFFFFF;
    }
    
    /**
     * Set one importance as empty.  
     * 
     * @param emptyImportances
     */
    public void setEmptyImportances(int aEmptyImportances) {
        if(emptyImportances == -1)
            emptyImportances = 0;
        
        emptyImportances |= (0x1 << aEmptyImportances);
    }
    
    /**
     * Return true if the importance are empty
     */
    public boolean isEmptyImportance(int importance) {
        if(emptyImportances==-1) {
            return false;
        } else {            
            return (emptyImportances & (0x1 << importance))>0;
        }
    }
    
    /**
     * Add a extracted TileMap to the TileMapLayerWrapper. 
     * 
     * This method are used to collect all the string tiles for one TileMapLayerWrapper
     * 
     * @param aTileMap
     * @param imp
     */
    public void addTileMap(TileMap aTileMap, int imp) {
        if(iTileMaps != null)
            iTileMaps[imp] = aTileMap;
    }
    
    /**
     * @return all the added TileMaps
     */
    TileMap[] getTileMaps() {
        return iTileMaps;
    }
    
    /**
     * 
     */
    public void purgeTileMaps() {
        iTileMaps = null;    
        iNbrOfImpToCache = 0;
    }
    
    /**
     * 
     * Set the max number of importance for the detail level. 
     * 
     * @param nbrImp
     */
    public void setMaxNbrOfImportance(int nbrImp) {
        
        iMaxNbrOfImportance = nbrImp;
        iTileMaps = new TileMap[nbrImp];
        iGeoMapCRC = new long[nbrImp];      
        iGeoTileParamStrings = new String[nbrImp];
        iData = new byte[nbrImp*2][];
        iParamsToCache = new TileMapParams[nbrImp*2];
        
        for(int i= 0; i<iGeoMapCRC.length; i++) {
            iGeoMapCRC[i] = 0;
        }
    }
    
    /**
     * @return the max number of importance for the detail level. 
     */
    public int getMaxNumberOfImportace() {
        return iMaxNbrOfImportance;
    }
    
    /**
     * 
     * Set the number of importance for the current used scale. 
     * 
     * @param nbr, the number of importace. 
     */
    public void setNbrImportances(int nbr) { 
        nbrImportances = nbr;
    }
    
    /**
     * The method are used when we compare the extracted string TileMap
     * when the matching Geometric TileMap. If the CRC missmatch we need
     * to reload it. 
     * 
     * @param importance
     * @return
     */
    public long getGeoMapCrc(int importance) {
        return iGeoMapCRC[importance];
    }
    
    /**
     * Save the geo-tile paramString. This is used when we compare the crc between
     * string TileMaps and Geometric TileMaps. 
     * 
     * @param str, the paramString
     * @param imp, the importance
     */
    public void setGeoTileMapParamString(String str, int imp) {
        if(imp < iGeoTileParamStrings.length)
            iGeoTileParamStrings[imp] = str;
    }
    
    /**
     * The method return the ParamString for the Geometric TileMap with the importance
     * number specified of the parameter.  
     * 
     * @param imp
     * @return
     */
    public String getGeoTileParamString(int imp) {
        if(imp < nbrImportances)
            return iGeoTileParamStrings[imp];
        else {      
            if(LOG.isError()) {
                LOG.error("TileMapLayerWrapper.getGeoTileParamString()", 
                        "imp= "+imp+" nbrImp= "+nbrImportances);
            }
            return null;
        }
    }
    
    public void setGeoMapCrc(int importance, long crc) {
        try {
            iGeoMapCRC[importance] = crc;
        } catch (Exception e) {         
            if(LOG.isError()) {
                LOG.error("TileMapLayerWrapper.setGeoMapCrc()", e);
            }           
        }
    }
    
    /**
     * @return the number of importance for the TileMapWrapper. 
     */
    public int getNbrImportances() {
        return nbrImportances;
    }
    
    /**
     * @return the latitude index for the tile
     */
    public int getTileIndexLat() {
        return lat;
    }
    
    /** 
     * @return the longitude index for the tile. 
     */
    public int getTileIndexLon() {
        return lon;
    }
    
    public int getLayerID() {
        return layerID;
    }
    
    public int getDetailLevel() {
        return detailLevel;
    }
    
    public boolean shouldRequestGeoData(int importance) {
        return !isEmptyImportance(importance) && !isRequested(importance);
    }
    
    public boolean shouldRequestString(int importance) {
        return !isEmptyImportance(importance) && !isRequestedString(importance);
    }
    
    public boolean isRequested(int importance) {
        return (requestedImportances & (0x1 << importance))>0;
    }
    
    public boolean isReceived(int importance) {
        return (receivedImportances & (0x1 << importance))>0;
    }
    
    public void setRequested(int importance) {    
        requestedImportances |= (0x1 << importance);
    }
    
    public void unSetRequested(int importance) {
        requestedImportances &= ~(0x1 << importance);
    }
    
    public void setReceived(int importance) {      
        receivedImportances |= (0x1 << importance);
    }
    
    public boolean isRequestedString(int importance) {
        return (requestedStrings & (0x1 << importance))>0;
    }
    
    public boolean isReceivedString(int importance) {
        return (receivedStrings & (0x1 << importance))>0;
    }
    
    public void setRequestedString(int importance) {    
        requestedStrings |= (0x1 << importance);
    }
    
    public void unSetRequestedString(int importance) {
        requestedStrings &= ~(0x1 << importance);
    }
    
    public void setReceivedString(int importance) {      
        receivedStrings |= (0x1 << importance);
    }
    
    public boolean shouldRequestGeoData() {
        return (emptyImportances==-1)||(~((requestedImportances^(emptyImportances))|0x1))<<(32-nbrImportances)!=0;
    }
    
    public boolean isGeoDataDone() {
        return (emptyImportances>=0)&&(~((receivedImportances^(emptyImportances))|0x1))<<(32-nbrImportances)==0;
    }
    
    public boolean isStringDataDone() {
        return (emptyImportances>=0)&&(~((receivedStrings^(emptyImportances))|0x1))<<(32-nbrImportances)==0;
    }
    
    public void resetStrings() {
        receivedStrings = 0x0;
        requestedStrings = 0x0;
    }
    
    public void reset() {
        resetStrings();
        
        emptyImportances = -1;      
        requestedImportances = 0;
        receivedImportances = 0;
        requestedStrings = 0;
        receivedStrings = 0;
        iReqestedGeoDataFromInternet = 0;
        
        if(iData != null && iParamsToCache != null) {
            for(int i=0; i<iData.length; i++) {
                iData[i] = null;
                iParamsToCache[i] = null;
            }
        }       
    }
    
    // ------------------------------------------------------------------------------------------------------
    // New cache methods
    
    // GeoData importance that are requested from internet
    private int iReqestedGeoDataFromInternet = 0x0;
    private int iNbrOfGeoTilesRequestedFromInternet = 0;
    
    public void setGeoDataRequestedFromInternet(int importance) {
        iNbrOfGeoTilesRequestedFromInternet++;
        iReqestedGeoDataFromInternet |= (0x1 << importance);
    }
    
    public boolean isGeoDataRequestedFromInternet(int importance) {
        return (iReqestedGeoDataFromInternet & (0x1 << importance)) > 0;
    }
    
    public int getRequestedGeoDataFromInternet() {
        return iReqestedGeoDataFromInternet;
    }
    
    public int getNbrOfGeoTileRequestedFromInternet() {
        return iNbrOfGeoTilesRequestedFromInternet;
    }
    
    // String importance that are requested from internet
    private int iReqestedStringDataFromInternet = 0x0;
    
    public void setStringDataRequestedFromInternet(int importance) {
        iReqestedStringDataFromInternet |= (0x1 << importance);
    }
    
    public boolean isStringDataRequestedFromInternet(int importance) {
        return (iReqestedStringDataFromInternet & (0x1 << importance)) > 0;
    }
    
    public int getRequestedStringDataFromInternet() {
        return iReqestedStringDataFromInternet;
    }
    
    private int iTotalCacheSize;    
    private byte [][]iData;
    private TileMapParams []iParamsToCache;
    private int iNbrOfImpToCache = 0;
    
    /**
     * 
     * Add data for the tile that should be saved in the persistent cache. 
     * 
     * @param aParam the TileMapParams for the TileMap. 
     * @param aData the byte data for the TileMap. 
     */
    public void addData(TileMapParams aParam, byte []aData) {
        synchronized(iData) {
            
            if(aParam.getTileMapType() == TileMapParams.MAP) {
            
                
                if(LOG.isError()) {
                    if(iData[aParam.getImportance()] != null) {
                        LOG.error("TileMapLayerWrapper.addData()", "Add a imp that isn't empty:" +
                                " paramString= "+aParam.getAsString()+
                                " imp= "+aParam.getImportance());
                    }
                }
                
                iData[aParam.getImportance()] = aData;
                iParamsToCache[aParam.getImportance()] = aParam;
            } else {
                
                if(LOG.isError()) {
                    if(iData[iMaxNbrOfImportance+aParam.getImportance()] != null) {
                        LOG.error("TileMapLayerWrapper.addData()", "Add a imp that isn't empty:" +
                                " paramString= "+aParam.getAsString()+
                                " imp= "+aParam.getImportance());                   
                    }
                }
                
                iData[iMaxNbrOfImportance+aParam.getImportance()] = aData;
                iParamsToCache[iMaxNbrOfImportance+aParam.getImportance()] = aParam;
            }           
            iTotalCacheSize += aData.length;
            iNbrOfImpToCache++;         
        }
    }
    
    /**
     * Return the byte data for all TileMaps that should be saved in
     * the cache. 
     * 
     * @return the byte data for all TileMaps that should be saved in
     * the cache.
     */
    byte [][]getDataToCache() {
        return iData;
    }
    
    /**
     * Return the TileMapParams for all TileMaps that should be saved in
     * the cache. 
     * 
     * @return the TileMapParams for all TileMaps that should be saved in
     * the cache. 
     */
    TileMapParams []getParamsToCache() {
        return iParamsToCache;
    }
    
    /**
     * Return the total number of bytes for all byte buffers that should be 
     * saved in the cache. 
     * 
     * @return the total number of bytes for all byte buffers that should be 
     * saved in the cache.
     */
    public int getTotalSize() {
        return iTotalCacheSize;
    }
    
    /**
     * Return the number of TileMaps to save in the cache. 
     * 
     * @return
     */
    public int getNbrOfImpToCache() {
        return iNbrOfImpToCache;
    }
}
