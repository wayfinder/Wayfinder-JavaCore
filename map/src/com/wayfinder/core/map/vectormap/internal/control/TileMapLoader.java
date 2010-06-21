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

import java.util.Hashtable;
import java.util.Vector;

import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.map.MapDownloadListener;
import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.PreInstalledMapsListener;
import com.wayfinder.core.map.vectormap.internal.cache.CacheConfiguration;
import com.wayfinder.core.map.vectormap.internal.cache.CacheInterface;
import com.wayfinder.core.map.vectormap.internal.cache.DummyCache;
import com.wayfinder.core.map.vectormap.internal.cache.FileCache;
import com.wayfinder.core.map.vectormap.internal.cache.MemCache;
import com.wayfinder.core.map.vectormap.internal.cache.SecondaryCache;
import com.wayfinder.core.map.vectormap.internal.cache.precache.PreCacheLoader;
import com.wayfinder.core.map.vectormap.internal.cache.precache.SingleFileDBufRequester;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDescCRC;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.util.UtilFactory;

/**
 * The class load the ParamString from the:
 *   - Memory cache
 *   - Persistent cache
 *   - Pre-installed map cache
 *   - Server 
 * 
 */
public class TileMapLoader implements TileMapRequestListener {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapLoader.class);
    
    private Hashtable m_RequestedParams;
    private CacheInterface iCache;
    private MemCache iMemCache;
    
    private TileMapExtractionThread iTileMapExtraction;
    private TileMapControlThread iTileMapControlThread;
    private MapDownloadListener m_TileMapDownloadListener;
    private TileMapNetworkHandler m_TileMapNetworkHandler;
    private PreCacheLoader iPreCacheLoader;
    
    private SingleFileDBufRequester []iPreCacheRequester;
    private PersistenceLayer m_PersistenceLayer;
    private UtilFactory m_UtilFactory; 
    
    // Indicate if we should load tiles from internet or only form cache. 
    private boolean iOfflineMode = false;
    
    public TileMapLoader(TileMapExtractionThread aMapExtraction, TileMapControlThread aControlThread,
            PersistenceLayer persistenceLayer, UtilFactory utilFactory) {
        
        m_RequestedParams = new Hashtable();
        
        iTileMapExtraction = aMapExtraction;
        iTileMapControlThread = aControlThread;
        iPreCacheLoader = new PreCacheLoader();
        setCacheIsStarted(false);
        m_PersistenceLayer = persistenceLayer;
        m_UtilFactory = utilFactory;
    }
    
    /**
     * Sets the list of available file caches
     * <p>
     * Each path in the array <b>MUST</b> be complete including scheme<br>
     * (ex: "file:///e:/Other/wayfinder/wf-map-uk_London_en.wfd")
     * 
     * @param aFileNames An array of paths to files containing map data. 
     */
    public void setFileCaches(String folder, String[] aFileNames, PreInstalledMapsListener listener) {
        long crc = 0;
        if(aFileNames != null && aFileNames.length > 0) {
            final int length = aFileNames.length;
            Vector al = new Vector(length);

            final int language = iTileMapControlThread.getLanguage();
            for (int i = 0; i < length; i++) {
                String fileName = aFileNames[i];
                if(fileName != null && fileName.length() > 0) {
                    SingleFileDBufRequester preCache = 
                        iPreCacheLoader.addSingleFileCache(fileName,language, m_PersistenceLayer);

                    if(preCache != null) {
                        al.addElement(preCache);
                        // Yes, the CRC is far from foolproof since it's
                        // theoretically possible to get the same crc for
                        // two different setups, but I gather that it's good
                        // enough
                        crc += preCache.getCacheCRC();
                        
                        if(listener != null) {
                            listener.preInstalledMapUsed(fileName);
                        }
                    } else {
                        if(listener != null) {
                            listener.preInstalledMapFailed(fileName);
                        }
                    }
                }
            }

            if(al.size() > 0) {
                SingleFileDBufRequester[] preCaches = new SingleFileDBufRequester[al.size()];
                al.copyInto(preCaches);                
                iPreCacheRequester = preCaches;
            }
        }
        
        if(listener != null) {
            listener.preInstalledMapsCrc(folder, crc);
        }
    }
    
    /**
     * This method close the map cache. The cache index file will be saved. 
     */
    void closeMapCache() {
        if(iCache != null) {
            iCache.closeCache();
        }
    }
    
    /**
     * Set the map component in offline mode. No param string will be loaded from the server. 
     * 
     * @param aOffline, true if we want to set the client in offline mode. 
     */
    public void setOfflineMode(boolean aOffline) {
        if(LOG.isInfo()) {
            LOG.info("TileMapLoader.setOfflineMode()", "offline= "+aOffline);
        }
        iOfflineMode = aOffline;
    }
    
    
    private boolean iOfflineForCachedLayer = false; 
    
    /**
     * @param aOffline, sets to true if we want to block the download of layers that
     * can be cached in the r/w cache, false if not. 
     * 
     */
    public void setOfflineModeForCachedLayer(boolean aOffline) {
        iOfflineForCachedLayer = aOffline;
    }
    
    /* for debug only */
    public boolean isOffline() {
        return (iOfflineMode==true);
    }
    
    private boolean iCacheIsStarted = false;
    
    synchronized boolean isCacheStarted() {
        return iCacheIsStarted;
    }
    
    synchronized void setCacheIsStarted(boolean isStarted) {
        iCacheIsStarted = isStarted;
    }
    
    /**
     * Starts the memory cache and read/write cache. 
     * 
     * @param aUseFileCache, true if we can use the file cache
     */
    void startCache(LinkedList cacheConfig) {

        iMemCache = new MemCache();
        
        final int size = cacheConfig.size();        
        for(int i=0; i<size; i++) {
            CacheConfiguration cacheSetting = (CacheConfiguration)cacheConfig.get(i);
            
            if(cacheSetting.getType() == CacheConfiguration.TYPE_FILE_CACHE) {
                iCache = new FileCache(m_PersistenceLayer, cacheSetting.saveCacheContinuously());
                if(iCache.openCache()) {
                    break;
                }
                iCache = null;
            } else if(cacheSetting.getType() == CacheConfiguration.TYPE_SECONDARY_CACHE) {
                iCache = new SecondaryCache(m_PersistenceLayer);
                if(iCache.openCache()) {
                    break;
                }
                iCache = null;
            } else if(cacheSetting.getType() == CacheConfiguration.TYPE_NO_CACHE) {
                iCache = new DummyCache();
                break;
            }           
        }
        
        if(LOG.isInfo()) {
            
            if(iCache instanceof FileCache) {
                LOG.info("TileMapLoader.startCache()", "Using FileCache");
            }
            if(iCache instanceof SecondaryCache) {
                LOG.info("TileMapLoader.startCache()", "Using SecondaryCache");
            }
            if(iCache instanceof DummyCache) {
                LOG.info("TileMapLoader.startCache()", "Using DummyCache");
            }
        }
                
        iCache.setMemCache(iMemCache);  
        setCacheIsStarted(true);
    }
    
    /**
     * Sets the TileMapRequesterInterface that will be used to load TileMaps from the server
     * 
     * @param aTileMapRequester
     */
    public void setTileMapRequester(SharedSystems systems, InternalNetworkInterface aNetworkInterface) {
        m_TileMapNetworkHandler = new TileMapNetworkHandler(
                aNetworkInterface, this, m_UtilFactory, systems.getWorkScheduler());
    }
    
    /** 
     * @return the current TileMapRequester
     */
    public TileMapNetworkHandler getTileMapNetworkHandler() {
        return m_TileMapNetworkHandler;
    }
    
    /**
     * @return the read/write cache
     */
    public CacheInterface getCache() {
        return iCache;
    }
    
    public MemCache getMemCache() {
        return iMemCache;
    }
    
    /**
     * Remove the paramstring from the memory cache. 
     * @param aParamString
     */
    synchronized void removeFromMemCache(String aParamString) {
        if(iMemCache != null)
            iMemCache.removeFromCache(aParamString);
    }
    
    /**
     * @return the number of requested TileMapParams
     */
    public int getNbrOfRequestedParams() {
        return m_RequestedParams.size();
    }
    
    /**
     * 
     * @param paramString
     */
    public void removeFromCache(TileMapParams aParams) {
        iCache.removeFromCache(aParams);
    }
    
    /**
     * Set the TileMapDownloadListener that signal if the map component
     * has server contact. It's used to trigger the spinning logo
     * 
     * @param listener
     * @see com.wayfinder.map.TileMapDownloadListener
     */
    public void setMapDownloadListener(MapDownloadListener listener) {
        m_TileMapDownloadListener = listener;
    }
    
    public MapDownloadListener getMapDownloadListener() {
        return m_TileMapDownloadListener;
    }
    
    /**
     * Clean up the memory 
     */
    void resetRequestedParams() {
        m_RequestedParams.clear();
    }
    
    /**
     * 
     * Create and request a tmfd 
     * 
     * @param nightMode true if we want to load tmfd nightmode
     * @param reset, true if we should load the tmfd from the server
     */
    void loadTMFD(boolean nightMode, boolean reset, boolean aOnlyFromCache, String aIDString) {
        if(LOG.isInfo()) {
            LOG.info("TileMapLoader.loadTMFD()", "nightMode:" + nightMode 
                    + ", reset:" + reset + ", aOnlyFromCache:" + aOnlyFromCache 
                    + ", aIDString:" + aIDString);
        }
        
        /* Create the TileMapParams and the param string */
        String paramString;
        String paramStringPreCache;
        if(!nightMode) {
            paramString = 
                TileMapFormatDesc.createParamString(iTileMapControlThread.getLanguage(), aIDString, "", false);
            paramStringPreCache = 
                TileMapFormatDesc.createParamString(iTileMapControlThread.getLanguage(), aIDString, "", true);
                
            if(LOG.isInfo()) {
                LOG.info("TileMapLoader.loadTMFD()", "nightmode reset= "+reset+" paramString= "+paramString);
            }
        } else {
            paramString = 
                TileMapFormatDesc.createParamStringNight(iTileMapControlThread.getLanguage(), aIDString, "", false);
            paramStringPreCache = 
                TileMapFormatDesc.createParamStringNight(iTileMapControlThread.getLanguage(), aIDString, "", true);
            
            if(LOG.isInfo()) {
                LOG.info("TileMapLoader.loadTMFD()", "daymode reset= "+reset+" paramString= "+paramString);
            }
        }
        
        TileMapParams tmp = new TileMapParams(paramString,paramString);
        TileMapParams tmpPreCache = new TileMapParams(paramStringPreCache, paramStringPreCache);
        
        if(reset) {
            // Clear the cache if the tmfd crc missmatch
            iMemCache.removeFromCache(paramString);
            iCache.removeFromCache(tmp);        
        }
        
        
        /* Load the tmfd from cache or internet */
        if(!m_RequestedParams.containsKey(tmp.getAsString())) {
        
            byte[] data = iCache.getDataFromCache(null, paramString, paramString);
            
            boolean existInCache = false;
            
            if(data != null) {                              
                iTileMapExtraction.addTileToExtraction(tmp, data);
            } else {
                // Try to load the tmfd from the pre-cached map files 
                if(iPreCacheRequester != null && !reset) {
                    if(LOG.isInfo()) {
                        LOG.info("TileMapLoader.loadTMFD()", "try load from precache" );
                    }

                    m_RequestedParams.put(paramStringPreCache, tmpPreCache);
                    for(int i=0; i<iPreCacheRequester.length; i++) {                
                        if(iPreCacheLoader.readDone()) {                            
                            existInCache = iPreCacheRequester[i].request(tmpPreCache, this);
                            if(existInCache)
                                break;
                        }
                    }
                }
                
                // Load the tmfd from internet
                if(!existInCache && !aOnlyFromCache && !iOfflineMode) {
                    if(LOG.isInfo()) {
                        LOG.info("TileMapLoader.loadTMFD()", "try load from server " + paramString );
                    }
                    m_RequestedParams.put(paramString, tmp);
                    m_RequestedParams.remove(paramStringPreCache);


                    m_TileMapNetworkHandler.request(paramString);
                }
            }
        }
    }
    
    /**
     * Request TileMapFormatDesc CRC for day mode and night mode
     * 
     */
    void loadTMFDCRC(boolean nightMode, String aID_String) {
        
        String paramString;
        
        if(nightMode) {
            paramString = TileMapFormatDescCRC.createParamStringNight(iTileMapControlThread.getLanguage(), aID_String, "");
        } else {
            paramString = TileMapFormatDescCRC.createParamString(iTileMapControlThread.getLanguage(), aID_String, "");
        }
        
        if(LOG.isInfo()) {
            LOG.info("TileMapLoader.loadTMFDCRC()", paramString);
        }
        TileMapParams tmpCRC = new TileMapParams(paramString, paramString);
        m_RequestedParams.put(paramString, tmpCRC);
        m_TileMapNetworkHandler.request(paramString);
    } 
    
    /**
     * Help method that checks if the read/write cache and/or the pre-installed 
     * cache contains the provided TileMapParams. 
     * 
     * @param param the current TileMapParams
     * @return true if the paramString exist in any cache (not memory cache) 
     */
    public boolean existInCache(TileMapParams param) {
        
        boolean existInCache = false;
        
        if(iPreCacheRequester != null) {            
            for(int i=0; i<iPreCacheRequester.length; i++) {                
                existInCache = iPreCacheRequester[i].existInPrecache(param);
                if(existInCache)
                    break;                              
            }
        }
        
        if(!existInCache) {
            existInCache = iCache.existInCache(param);
        }
                
        return existInCache;
    }
    
    /**
     * Request a new TileMapParams. If the tmp exist in any cache it will be 
     * sent to the extraction thread othervise it will be requested from the server. 
     * 
     * @param paramString the paramsting to send
     * @param tmp The tileMapParams for the importance 
     */
    public void request(String paramString, TileMapParams tmp, TileMapLayerWrapper aCurrentRequestedWrapper) {
        
        if(!m_RequestedParams.containsKey(paramString)) {
            boolean existInCache = false;
            
            // Mem cache
            byte []data = iMemCache.getDataFromCache(paramString);
            iCurrentRequesedWrapper = aCurrentRequestedWrapper;
            
            if(data == null) {
                // Pre installed cache
                if(iPreCacheRequester != null && 
                        //!TileMapParamTypes.isMapFormatDesc(paramString) && 
                        !TileMapParamTypes.isTmfdCRC(paramString) &&
                        (tmp.getLayerID() != 3) ) {
                    
                    if(tmp.getImportance() == 0) {                                              
                        m_RequestedParams.put(paramString, tmp);
                        
                        /* Need to make copy of the TileMapParam when searching in the
                         * pre-installed maps because the tmp may be modified when loading
                         * the pre-installed maps. */
                        TileMapParams tmp_copy = tmp.cloneTileMapParams(); 
                        
                        for(int i=0; i<iPreCacheRequester.length; i++) {                
                            if(iPreCacheLoader.readDone()) {
                                existInCache = iPreCacheRequester[i].request(tmp_copy, this);                               
                                if(existInCache) {                              
                                    if(i != 0) {
                                        /* Move the lasted used pre-installed map first in the queue. 
                                         * It's a big change that we want to use that file again. */
                                        SingleFileDBufRequester file = iPreCacheRequester[0];
                                        iPreCacheRequester[0] = iPreCacheRequester[i];
                                        iPreCacheRequester[i] = file;
                                    }                               
                                    break;          
                                }
                            }
                        }
                        
                    /* If it's not importance 0 we just have to check if the importance are inside
                     * the current loaded pre-installed maps. If it's inside the loaded pre-installed 
                     * maps then we know it's a empty importance. Othervise it should have been in the
                     * memory cache. 
                     * 
                     * If the importance isn't inside the pre-installed maps area we need to keep checking
                     * the reset of the caches and/or internet. 
                     * */
                    } else {
                        
                        for(int i=0; i<iPreCacheRequester.length; i++) {                
                            if(iPreCacheLoader.readDone()) {
                                existInCache = iPreCacheRequester[i].existInPrecache(tmp);      
                                
                                if(existInCache) {  
                                    /* Remove the requested flag if it's a empty importance */
                                    if(tmp.getTileMapType() == TileMapParams.MAP)
                                        iCurrentRequesedWrapper.unSetRequested(tmp.getImportance());
                                    else
                                        iCurrentRequesedWrapper.unSetRequestedString(tmp.getImportance());
                                    break;          
                                }
                            }
                        }
                    }
                    
                    if(!existInCache) {
                        m_RequestedParams.remove(paramString);
                    }                                       
                }
                            
                // Request from the read/write cache
                if(!existInCache) {
                    
                    /* Check if data exist in the cache. */
                    if(tmp.getImportance() == 0) {
                        data = iCache.getDataFromCache(iCurrentRequesedWrapper, paramString, tmp.getTileID());
                        if(iCurrentRequesedWrapper != null && iCurrentRequesedWrapper.isEmptyImportance(tmp.getImportance())) {
                            /* The tile exist in the cache but the current requested importance are empty, return */
                            return;
                        }
                    }
                    
                    // Request from internet
                    if((data == null) && !iOfflineMode) {
                        /* Check to see if we should request layer that can be cached. */
                        if(!(iOfflineForCachedLayer && iTileMapControlThread.shouldBeSavedInCache(tmp.getLayerID()))) {
                            
                            if(LOG.isTrace()) {
                                LOG.trace("TileMapLoader.request()", "send: "+tmp.getAsString()+" layerID= "+tmp.getLayerID());
                            }
                            
                            // Sets the time when we request the tilemap
                            tmp.updateTimeStamp();
                            m_RequestedParams.put(paramString, tmp);
                            m_TileMapNetworkHandler.request(paramString);

                            /* If the requested data is a TileMap we set it as requested from internet. 
                             * This to know if we should save it to cache or not. */
                            if(iCurrentRequesedWrapper != null) {
                                if(tmp.getTileMapType() == TileMapParams.MAP) {
                                    iCurrentRequesedWrapper.setGeoDataRequestedFromInternet(tmp.getImportance());   
                                } else if(tmp.getTileMapType() == TileMapParams.STRINGS) {
                                    iCurrentRequesedWrapper.setStringDataRequestedFromInternet(tmp.getImportance());
                                }
                            }
                            
                            if(m_TileMapDownloadListener != null) {
                                //  Update the tilemap download listener (spinning logo)
                                m_TileMapDownloadListener.handleDownloadEvent(true);
                            }
                        }
                    }   
                }
            }
            
            // Extract data if it's found in the cache
            if(data != null) {
                iTileMapExtraction.addTileToExtraction(tmp, data);
            }   
            
            iCurrentRequesedWrapper = null;
        }       
    }
    
    /**   
     * Send new request to the server. 
     */
    void sendRequest() {        
        if(!iOfflineMode) {         
            m_TileMapNetworkHandler.sendRequest();
        }
    }
    
    /**
     * Save CRC to the cache. 
     */
    void saveCRCToCache(String paramString, int crc) {  
//      iCache.writeToCache(paramString, new byte[1], crc);
    }
    
    byte [][]iDataToCache = new byte[1][];
    TileMapParams []iParamToCache = new TileMapParams[1];
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.map.vectormap.control.TileMapRequestListener#requestReceived(java.lang.String, byte[], boolean)
     */
    public void requestReceived(String paramString, byte[] data, boolean fromCache) {
        
        if(LOG.isDebug()) {
            LOG.debug("TileMapLoader.requestReceived()", "paramString= "+paramString+" fromCache= "+fromCache );
        }
        
        TileMapParams param = (TileMapParams)m_RequestedParams.remove(paramString);
        
        if(param != null) {
            if(!fromCache) {
                if(!TileMapParamTypes.isTmfdCRC(paramString)) {             
                    /* Data will always be saved in the memory cache. It's up the the user
                     * of the data the reload the tile if the valid time for the tile
                     * has expired (use iTileMapControlThread.reloadTileID(String aTileID). */
                    iMemCache.writeToCache(paramString, data);                  
                }
                
                /* Write tmfd, bitmap images etc. directly to the cache when we have downloaded it. Other
                 * map data will be saved when we have loaded a compete tile for each layer. */
                if(!TileMapParamTypes.isMap(paramString) && !TileMapParamTypes.isTmfdCRC(paramString)) {
                    try {
                        iDataToCache[0] = data;
                        iParamToCache[0] = param;                       
                        iCache.writeDataToCache(iDataToCache, iParamToCache, param, data.length, 1, (short)-1);
                    } catch (Exception e) {
                        if(LOG.isError()) {
                            LOG.error("TileMapLoader.requestReceived()", e);
                        }
                    }
                }
            }
            
            // Add TileMap to extraction if it's visible on the screen
            if(iTileMapControlThread.isTileMapVisible(param)) {
                if(LOG.isDebug()) {
                    LOG.debug("TileMapLoader.requestReceived()", "post for extraction paramString="+paramString);
                }
                iTileMapExtraction.addTileToExtraction(param,data);
            }           
        }
                
        // Update the TileMapDownloadListener
        if(m_RequestedParams.size() == 0 && m_TileMapDownloadListener != null) {
            m_TileMapDownloadListener.handleDownloadEvent(false);
        }
    }
    
    private TileMapLayerWrapper iCurrentRequesedWrapper = null;
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.map.vectormap.control.TileMapRequestListener#requestReceived(com.wayfinder.map.vectormap.process.TileMapParams, com.wayfinder.map.vectormap.process.BitBuffer, boolean)
     * 
     * Will be called when tiles are loaded from the pre-installed cache. 
     * 
     */
    public void requestReceived(TileMapParams param, BitBuffer buf, boolean fromCache) {
            
        if(buf != null) {
            iMemCache.writeToCache(param.getAsString(), buf.getByteArray());
            requestReceived(param.getAsString(), buf.getByteArray(), fromCache);
        } else {
            /* Set the empty importance direct to the current requested wrapper. */
            if(iCurrentRequesedWrapper != null && param.getTileMapType() != TileMapParams.STRINGS &&
                    iCurrentRequesedWrapper.getLayerID() == param.getLayerID()) {   
                
                iCurrentRequesedWrapper.setEmptyImportances(param.getImportance());             
            }       
            m_RequestedParams.remove(param.getAsString());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see wmmg.map.vectormap.control.TileMapRequestListener#requestFailed(java.lang.String[])
     */
    public void requestFailed(String []paramStrings) {
        if(paramStrings != null && !iOfflineMode) {
            
            TileMapParams param;            
            for(int i=0; i<paramStrings.length; i++) {              
                param = (TileMapParams)m_RequestedParams.remove(paramStrings[i]);                
                if(param != null && iTileMapControlThread.isTileMapVisible(param)) {
                    param.updateTimeStamp();
                    m_RequestedParams.put(param.getAsString(), param);
                    m_TileMapNetworkHandler.request(param.getAsString());
                    
                    if(LOG.isTrace()) {
                        LOG.trace("TileMapLoader.requestFailed()", "paramString= "+param.getAsString());
                    }
                }   
            }
            
            m_TileMapNetworkHandler.sendRequest();
        } else {
            synchronized (m_RequestedParams) {
                m_RequestedParams.clear();
            }
        }
    }

    public void setTileMapFormatDescritor(TileMapFormatDesc tmfd) {
        iCache.setTileMapFormatDesc(tmfd);
    }   
}
