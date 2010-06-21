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

package com.wayfinder.core.map.vectormap.internal.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapper;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapperInterface;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.core.shared.util.qtree.QuadTree;
import com.wayfinder.core.shared.util.qtree.QuadTreeEntry;
import com.wayfinder.core.shared.util.qtree.QuadTreeNode;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 * 
 * The class handles the file map cache for the vector maps. 
 * <p>
 * Just like the RMS/RIM cache the map data are saved in page files on
 * disc. To each entry in the page file a cache index are created. The index
 * holds information about where on disc the map data are saved. 
 * <p>
 * The cache indices are saved in a QuadTree. The quad tree has a limit
 * of 500 indices in each node. When we have exceed that number the node will
 * be split up into 2 or more sub-nodes. The nodes that currently contains loaded
 * tiles will be hold in memory. When no tiles from a node exist in memory the node
 * will be unloaded from the memory and saved on disc.  
 * <p>
 * There exist a Hashtable where all non-tile and overview maps are saved. This hashtable
 * will always be held in memory. 
 * <p>
 * When a page has become full we start writing to the next page in the cache. When we have
 * filled up the whole cache we start over with page 0 again. The cached mapdata in this page
 * will be discarded. To avoid having to go throw all the nodes in the quad tree when the cache
 * has become full we increase the page number one step and remove any inactive items from the 
 * node when we need to load it. 
 * <p>
 * 
 * NOTE: All public methods are synchronized except {@link #setVisible(boolean)}
 * (see methods doc) and {@link #printQTDebug(boolean)}   
 * 
 *
 */
//TODO: Investigate the synchronization on this class, in order to not block
//others calls because of time consuming methods are synchronized

public class FileCache implements CacheInterface {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(FileCache.class);
    
    /* 4 bytes for representing the total size of the cache block. 
     * 2 bytes for representing the empty importances for the tile 
     */
    static final int CACHE_HEADER_OFFSET = 6;
    
    /* 1 byte  for representing the size of the parameter string
     * 2 bytes for representing the size of the importance
     */
    static final int CACHE_IMP_OFFSET = 4;
    
    /* The maximal number of errors that can be detected before
     * we close the cache. The reason for that we don't close the 
     * case directly is to have a little more tolerant approach and
     * allow a small number of errors before we close the cache. */
    static final int MAX_NUMBER_OF_ERRORS = 3;
    
    /* The number of pages in the cache. */
    static final int NUMBER_OF_PAGES = 10;
    
    /* The maximum size for each page (in bytes) */
    static final int MAX_PAGE_SIZE = 1024000;
    
    /* The maximal number of items that can be added to a node in the
     * quad tree before we split it up into one or more sub nodes. */
    static final int MAX_NBR_ITEM_PER_NODE = 500;
    
    /* The version of the cache. */
    static final int CACHE_VERSION = 2;
    
    
    /* Hashtable that holds the index table for all non map data */
    private Hashtable m_IndexTableHashtable;
    
    /* The output stream for the current page to write to */
    private DataOutputStream dout;  
    
    /* Reference to the memory cache */
    private MemCache m_MemCache;
    
    private int m_Language;
    private TileMapFormatDesc m_tmfd;
    
    /* The QuadTree */
    private QuadTree m_cacheQuadTree;
    
    private FileHandler m_CacheFileHandler;
    
    private boolean m_IsCacheOpen;
    
    /* The current active page to write into. */
    private byte m_CurrentPageNumber;
    
    /* The file number. */
    private byte m_FilePageNumber;

    /* The number of bytes written to the current active page */
    private int m_CurrentOffset;
    
    /* Count the number of critical error that has occur. */
    private int m_ErrorCount = 0;
    
    private boolean m_HasBeenSplit = true;
    private Vector m_QtNodeVector = new Vector();
    
    private volatile boolean m_IsVisible = false;
    
    /* Buffer the is used when reading data from cache to avoid re-allocating if
     * every time we read from disc. */
    private byte []paramData = new byte[30];
    private BitBuffer bitBuffer = new BitBuffer(new byte[30]);
    
    /* Buffers used to buffer the data that should be writed before writing it to disc. */
    private int m_CacheBufferSize = 32000;
    private WFByteArrayOutputStream wfout = new WFByteArrayOutputStream(m_CacheBufferSize);
    private DataOutputStream daout = new DataOutputStream(wfout);
    
    /* Holds the nodes that are visible on the screen when we exit the map view. 
     * This nodes will be kept in memory until the either become not visible or
     * we exit the application. This to speed up the enter and exit of the map view.*/
    private Vector iLoadedNodes = new Vector();
    
    /* True if the cache should be fully saved continuously. */
    private boolean m_SaveCacheContinuously;
    /* The number of tiles that has to be saved before the cache if fully saved if
     * m_SaveCacheContinuously == true. */
    private static final int SAVE_MAP_COUNT = 50;
    /* Counts how many tiles that has been cached since we last saved the cache. */
    private int m_SaveMapCnt = 0;
    
    public FileCache(PersistenceLayer persistenceLayer, boolean saveCacheContinuously) {
        
        m_SaveCacheContinuously = saveCacheContinuously;
        
        m_CurrentPageNumber = 0; 
        m_FilePageNumber = 0;        
        m_CurrentOffset = 0;        
        
        /* Create the quad tree with the whole world as bounding box 
         * and the limit of max 500 items in each node. */
        m_cacheQuadTree = new QuadTree(Integer.MIN_VALUE/2,
                          Integer.MIN_VALUE,
                          Integer.MAX_VALUE/2,
                          Integer.MAX_VALUE,
                          MAX_NBR_ITEM_PER_NODE,
                          "root_");
        m_CacheFileHandler = new FileHandler(NUMBER_OF_PAGES, persistenceLayer);          
        m_IndexTableHashtable = new Hashtable();
        m_MemCache = null;
        m_tmfd = null;
    }
    
    /**
     * Set the cache to visible or not visible when we enter and exit
     * the map view. 
     * 
     * NOTE: This is not synchronize because is called from event dispatcher 
     * thread and it's use as an optimization which is not critical,
     * anyway the value is stored in a volatile member so the changes will be 
     * visible from other thread 
     */
    public void setVisible(boolean aVisible) {
        m_IsVisible = aVisible;
    }
    
    /**
     * Set the memory cache
     */
    public synchronized void setMemCache(MemCache aMemCache) {
        m_MemCache = aMemCache;
    }
    
    /**
     * Set the language used by the map. 
     * 
     * @param aLanguage 
     */
    public synchronized void setLanguage(int aLanguage) {
        m_Language = aLanguage;
    }
    
    /**
     * 
     * @param aTmfd
     */
    public synchronized void setTileMapFormatDesc(TileMapFormatDesc aTmfd) {
        m_tmfd = aTmfd;               
    }
    
    /**
     * Open the cache and read the loaded data. 
     * 
     */
    public synchronized boolean openCache() {
                 
        m_IsCacheOpen = false;            
            
        long time = System.currentTimeMillis();
        try {                
            
            if(!m_CacheFileHandler.openCache()) {
                if(LOG.isError()) {
                    LOG.error("FileCache.openCache()", "Unable to open the file cache.");
                }
                return false;
            }
            
            /* Load the cache info file, return true if the file contains data. */
            if(m_CacheFileHandler.loadCacheInfoFile()) {
                /* Check the version number*/
                if(m_CacheFileHandler.getVersionNumber() == CACHE_VERSION) {                    
                    m_CurrentPageNumber = m_CacheFileHandler.getPageNumber();
                    m_FilePageNumber = (byte)(m_CurrentPageNumber%NUMBER_OF_PAGES); 
                    m_CurrentOffset = m_CacheFileHandler.getOffset(m_FilePageNumber);
                } else {
                    if(LOG.isError()) {
                        LOG.error("FileCache.openCache()", "REBOOT CACHE START. "+
                                " m_CacheFileHandler.getVersionNumber()= "+m_CacheFileHandler.getVersionNumber()+
                                " CACHE_VERSION= "+CACHE_VERSION);
                    }
                    m_CurrentPageNumber = 0;
                    m_FilePageNumber = 0; 
                    m_CurrentOffset = 0;
                    /* Delete the cache if we have bump the version number. */
                    m_CacheFileHandler.rebootCache();                    
                }                    
            }
            
            /* Read the cache index file that contains the IndexTableEntrys for all 
             * non tilemaps (poi bitmap, tmfd etc.). */
            m_CacheFileHandler.readIndexTableFromFile(m_IndexTableHashtable);  
            m_CacheFileHandler.readQuadTreeFromFile(m_cacheQuadTree,false);
            m_IsCacheOpen = true;
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileCache.openCache()", e);
            }
        }
            
        if(LOG.isInfo()) {
            LOG.info("FileCache.openCache()", "time= "+(System.currentTimeMillis()-time)+" ms, iIsCacheOpen= "+m_IsCacheOpen+
                " iCurrentPageNumber= "+m_CurrentPageNumber+" filePageNumber= "+m_FilePageNumber);
        }
        return m_IsCacheOpen;
    }
    
    /**
     * 
     */
    public synchronized boolean writeDataToCache(byte [][]aCacheData, 
            TileMapParams []aParams, 
            TileMapParams aTileIDParam, 
            int aTotalSize, 
            int aNbrOfImp, 
            short aEmptyImp) {

        /* Return if the cache isn't open or if we try to save a null buffer. */
        if(!m_IsCacheOpen || aCacheData == null) {
            return false;
        }

        long time = System.currentTimeMillis();

        final String tileID = aTileIDParam.getTileID();           
        byte []extraData = null;
        int totalSize = aTotalSize + CACHE_HEADER_OFFSET;

        /* Set the total size of the writed data depending if it's a tilemap or not. */
        if(!TileMapParamTypes.isMap(tileID)) {
            totalSize += tileID.getBytes().length;
            totalSize += 3;
        } else {
            totalSize += (CACHE_IMP_OFFSET * aNbrOfImp);
        }   

        try {
            if(dout == null) {
                dout = m_CacheFileHandler.getPageDataOutputStream(m_FilePageNumber, m_CurrentOffset);  

                if(LOG.isInfo()) {
                    LOG.info("FileCache.writeDataToCache()", "dout==NULL, open a new stream, dout= "+dout);
                }
            }

            IndexTableEntry entry = null;
            int mc2Lat = 0;
            int mc2Lon = 0;

            /*
             * Check if there already exist data for the tile_id, if it exist we need to update
             * the existing entry with more data. This will happen when we zoom in a already cached
             * tile, then more importance will be available. 
             */
            if(TileMapParamTypes.isMap(tileID)) {
                final int layerNbr = m_tmfd.getLayerNbrFromID(aTileIDParam.getLayerID());
                final int mc2unit = (int)m_tmfd.getMc2UnitsPerTile(layerNbr, aTileIDParam.getDetailLevel());                
                mc2Lat = aTileIDParam.getTileIndexLat() * mc2unit;
                mc2Lon = aTileIDParam.getTileIndexLon() * mc2unit;             
                entry = (IndexTableEntry)m_cacheQuadTree.getEntry(mc2Lat, mc2Lon, tileID, m_CacheFileHandler);

                /* The entry already exist in the cache and more importance
                 * need to be added. */
                if(entry != null) {

                    if(LOG.isDebug()) {
                        LOG.debug("FileCache.writeDataToCache()", "tileID= "+entry.getName()+" Exist in cache"+
                                ", offset= "+entry.getOffset()+" page= "+entry.getPage()+" nbrNewImp= "+aNbrOfImp);
                    }

                    extraData = getAlreadyCachedData(entry);
                    if(extraData != null)
                        totalSize += extraData.length;                       
                }
            }

            /* Increase the temporary buffer the data to save is larger
             * then the current buffer.  */
            if(totalSize >= m_CacheBufferSize) {

                if(LOG.isError()) {
                    LOG.error("FileCache.writeDataToCache()", "the total size " +totalSize + " was bigger then the buffer " + wfout.size());
                }

                m_CacheBufferSize = totalSize;
                wfout = new WFByteArrayOutputStream(m_CacheBufferSize);
                daout = new DataOutputStream(wfout);                    
            }


            /* Check to see if there are enough bytes left on the current 
             * page to cache the data. */
            if((m_CurrentOffset + totalSize) > MAX_PAGE_SIZE) {              
                if(!moveToNextPage()) {
                    m_ErrorCount++;
                    if(m_ErrorCount >= MAX_NUMBER_OF_ERRORS) {
                        m_IsCacheOpen = false;    
                    }                       
                }   
            }

            /* Reset the temporary buffer before starting to write. */
            wfout.reset();
            /* Total number of bytes to write */
            daout.writeInt(totalSize);
            /* Write empty importance */
            daout.writeShort(aEmptyImp);

            /* 
             * TileMap tiles, for example the map, poi, traffic info layers. 
             */
            if(TileMapParamTypes.isMap(tileID)) {   

                if(extraData != null) {
                    daout.write(extraData);
                }   

                internalWriteTileMapData(aCacheData, 
                        aParams, 
                        entry, 
                        tileID, 
                        aTileIDParam, 
                        mc2Lat, 
                        mc2Lon, 
                        totalSize);

                /* Update the current offset into the page file. */
                m_CurrentOffset += totalSize;

                if(m_SaveCacheContinuously) {
                    if(m_SaveMapCnt == SAVE_MAP_COUNT) {
                        saveCache();
                        m_SaveMapCnt = 0;
                    }                        
                    m_SaveMapCnt++;
                }

                /*
                 * Other stuff that will be cached, for example bitmap images, tmfd etc.  
                 */
            } else {

                internalWriteNonTileMapData(aCacheData, 
                        aParams, 
                        tileID, 
                        totalSize);

                /* Update the current offset into the page file. */
                m_CurrentOffset += totalSize;

            }

        } catch (Exception e) {

            if(LOG.isError()) {
                LOG.error("FileCache.writeDataToCache()", "failed for tileID= "+tileID);
                LOG.error("FileCache.writeDataToCache()", e);
            }

            /* Reset the default size of the buffer. */
            m_CacheBufferSize = 32000;
            wfout = new WFByteArrayOutputStream(m_CacheBufferSize);
            daout = new DataOutputStream(wfout);  

            m_ErrorCount++;
            if(m_ErrorCount >= MAX_NUMBER_OF_ERRORS) {
                if(LOG.isError()) {
                    LOG.error("FileCache.writeDataToCache()", "DISABLE CACHE DUE TO, TO MANY ERRORS: "+m_ErrorCount);
                }
                m_IsCacheOpen = false;    
            }
        }

        if(LOG.isDebug()) {
            LOG.debug("FileCache.writeDataToCache()", "tileID= "+tileID+
                    " page= "+m_CurrentPageNumber+
                    " offset= "+(m_CurrentOffset-totalSize)+
                    " size= "+totalSize+
                    " nbrImp= "+aNbrOfImp+
                    " layerID= "+aTileIDParam.getLayerID()+
                    " lat= "+aTileIDParam.getTileIndexLat()+
                    " lon= "+aTileIDParam.getTileIndexLon()+
                    " time= "+(System.currentTimeMillis()-time)+" ms");
        }
        return true;
    }
    
    
    /**
     * Write tile maps to the cache file in the format specified 
     * by the table below. 
     * 
     * Add a index table entry to the quad tree after writing to 
     * the cache. 
     * 
     * 
     * |--------------------------------------------------|
     * | Cache data table           | SIZE                |
     * |--------------------------------------------------|
     * | Importance number          | 1 byte              |
     * | TileMap type (map/string)  | 1 byte              |
     * | Cache data size            | 2 bytes             |
     * | Cached data                | #Cached data size   |
     * |--------------------------------------------------|
     * 
     * NOTE: This method should be only called from synchronize context
    */
    private void internalWriteTileMapData(byte [][]aCacheData, 
                                          TileMapParams []aParams,
                                          IndexTableEntry entry, 
                                          String tileID,
                                          TileMapParams aTileIDParam,
                                          int mc2Lat, 
                                          int mc2Lon,
                                          int totalSize) 
    throws IOException {
            
        /* Write map data to cache. */
        for(int i=0; i<aCacheData.length; i++) {                    
            if(aParams[i] != null && aCacheData[i] != null) {
                /* Write importance number*/                    
                daout.writeByte(aParams[i].getImportance());
                /* Write the map type (MAP or STRING)*/
                daout.writeByte(aParams[i].getTileMapType());
                /* Write data */                
                daout.writeShort(aCacheData[i].length);
                daout.write(aCacheData[i]);
                
                aCacheData[i] = null;
            }
        }
        
        daout.flush();
        dout.write(wfout.getByteArray(), 0, totalSize);
        dout.flush();
        
        /* Create a indexEntry for where the data are cached and save it. */
        if(entry == null) {                        
            entry = new IndexTableEntry(m_CurrentPageNumber, m_CurrentOffset, tileID, mc2Lat, mc2Lon);     
            
            if(!aTileIDParam.isOverviewMap()) {
                m_HasBeenSplit = m_cacheQuadTree.addEntry(entry, m_CacheFileHandler);
                
                if(m_HasBeenSplit) {
                    saveCache();
                }                
            } else {
                m_IndexTableHashtable.put(tileID, entry);
            }
        } else {
            /* Update a already saved entry with the new location of the cached data. */
            entry.setPage(m_CurrentPageNumber);
            entry.setOffset(m_CurrentOffset);                        
        }        
    }
    
    
    /**
     * Write data that isn't a tile map to the cache file, 
     * for example TileMapFormatDesc, POI bitmap image etc. 
     * 
     * Add a index table entry to a hashtable after writing. 
     * 
     * |---------------------------------------------------|
     * | Cache data table           | SIZE                 |
     * |---------------------------------------------------|
     * | #Size of paramString       | 1 byte               |
     * | ParamString                | #Size of paramString |
     * | Cache data size            | 2 bytes              |
     * | Cached data                | #Cached data size    |
     * |---------------------------------------------------|
     * 
     *  NOTE: This method should be only called from synchronize context
    */
    private void internalWriteNonTileMapData(byte [][]aCacheData, 
                                             TileMapParams []aParams, 
                                             String tileID,
                                             int totalSize) 
    throws IOException {
        
        if(aParams[0] != null && aCacheData[0] != null) {
            /* Write the parameter string. */
            daout.writeByte(aParams[0].getAsString().getBytes().length);
            daout.write(aParams[0].getAsString().getBytes());
            
            /* Write data */                
            daout.writeShort(aCacheData[0].length);
            daout.write(aCacheData[0]);
            daout.flush();
            
            /* Write to file. */
            dout.write(wfout.getByteArray(), 0, totalSize);
            dout.flush();
            
            /* Add the new block to the index table */
            IndexTableEntry entry = new IndexTableEntry(m_CurrentPageNumber, m_CurrentOffset, tileID);     
            
            if(m_IndexTableHashtable.put(tileID, entry) != null) {
                if(LOG.isError()) {
                    LOG.error("FileCache.internalWriteNonTileMapData()", "tileID= "+tileID+" added 2 times!");
                }
            }
            
        }
    }
    
    /**
     * Called when more importance for a tile needs to be added. This will happen if
     * a user zoom inside a already cached tile since more importance will be available. 
     * 
     * Return the map data for the cached importance for the tile. 
     * 
     * NOTE: This method should be only called from synchronize context
     */
    private byte[]getAlreadyCachedData(IndexTableEntry entry) throws IOException, CorruptCacheException {
        
        byte []extraData = null;
        if(TileMapParamTypes.isMap(entry.getName())) {
            DataInputStream din = null;
            try {
                din = m_CacheFileHandler.getPageDataInputStream(entry.getPage()%NUMBER_OF_PAGES);
    
                if(din.skip(entry.getOffset()) != entry.getOffset()) {                    
                    throw new EOFException("Unable to skip "+entry.getOffset()+" bytes");
                }

                // Read the total size of the buffer
                final int size = din.readInt();
                
                if (size <= CACHE_HEADER_OFFSET || size > 400000) {
                    // The offset into the file has been corrupt when reading already cached data
                    m_cacheQuadTree.removeEntry(entry);
                    if (LOG.isError()) {
                        LOG.error("FileCache.getAlreadyCachedData()", "Invalid size: "+size+
                                ". Throwing CorruptCacheException");             
                    }
                    
                    throw new CorruptCacheException("Invalid size " + size + " for page " + entry.getPage());
                }
                
                // Ignore empty importance
                din.readShort();

                extraData = new byte[size-CACHE_HEADER_OFFSET];
                if(din.read(extraData) != extraData.length) {
                    if(LOG.isError()) {
                        LOG.error("FileCache.getAlreadyCachedData()", "Unable to read "+extraData.length+
                                " from file!");
                    }
                    extraData = null;
                }
            } finally {
                if(din != null)
                    din.close();
            }
        }
        return extraData;
    }
    
    /**
     * This method saves the loaded nodes, qt, index and info file on disc if needed. 
     * No data will be unloaded from the memory, if you want to save and free
     * memory use the {@link FileCache#purgeData()} method.  
     */
    public synchronized void saveCache() {
        
        long tot_time = System.currentTimeMillis();
        long time = tot_time;
        long t1=0, t2=0, t3=0, t4=0, t5=0;
        
        try {                
            m_QtNodeVector.removeAllElements();
            m_cacheQuadTree.getAllNodesThatContainsLoadedEntrys(m_QtNodeVector);
            t1 = System.currentTimeMillis()-time;
            time = System.currentTimeMillis();
            boolean qtHasChanged = false;
            
            for(int i=0; i<m_QtNodeVector.size(); i++) {
                QuadTreeNode node = (QuadTreeNode)m_QtNodeVector.elementAt(i);
                
                // Only write nodes that has changed to file. 
                if(node.hasBeenChanged()) {
                    m_CacheFileHandler.writeNodeToFile(node);
                    qtHasChanged = true;
                }
                
                // Don't clear the node since this method only save the data, not clean up
                // any memory. To free memory use the purgeData() method. 
            }
            t2 = System.currentTimeMillis()-time;
            time = System.currentTimeMillis();
            
            // Only write the quad tree to file if anything has changed. 
            if(qtHasChanged)
                m_CacheFileHandler.writeQuadTreeToFile(m_cacheQuadTree);
            t3 = System.currentTimeMillis()-time;
            time = System.currentTimeMillis();
            m_CacheFileHandler.writeCacheInfoToFile(m_CurrentPageNumber, CACHE_VERSION);
            t4 = System.currentTimeMillis()-time;
            time = System.currentTimeMillis();
            m_CacheFileHandler.writeIndexTableToFile(m_IndexTableHashtable);
            t5 = System.currentTimeMillis()-time;
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileCache.saveCacheToFile()", e);
            }
        }
        
        if(LOG.isInfo()) {
            LOG.info("FileCache.saveCacheToFile()", "SAVE MAP CACHE FILES tot_time= "+(System.currentTimeMillis()-tot_time)+" ms"+
                    " load node: "+t1+" ms write node: "+t2+" ms write qt: "+t3+" ms write info: "+t4+" ms write index: "+t5+" ms");
        }
    }
    
    // -------------------------------------------------------------------------------------------------
    // Read data from cache 
    
    public synchronized byte[] getDataFromCache(TileMapLayerWrapper aTileMapWrapper, 
                                                String aParamString, 
                                                String aTileID) {
        
        byte d[] = null;
        /* Return if the cache isn't open. */
        if(!m_IsCacheOpen)
            return d;

        IndexTableEntry entry = null;
        if(aTileMapWrapper == null || aTileMapWrapper.isOverviewMap()) {
            entry = (IndexTableEntry)m_IndexTableHashtable.get(aTileID);

            if(entry != null && (m_CurrentPageNumber-NUMBER_OF_PAGES) >= entry.getPage()) {
                if(LOG.isInfo()) {
                    LOG.info("FileCache.getDataFromCache()", "Remove a tiles form a not active page!");
                    LOG.info("FileCache.getDataFromCache()","Page= "+entry.getPage()+
                            " iCurrentPageNumber= "+m_CurrentPageNumber+" name: "+aTileID);
                }
                m_IndexTableHashtable.remove(aTileID);
                return d;
            }
        } else {
            int mc2Unit = (int)m_tmfd.getMc2UnitsPerTile(m_tmfd.getLayerNbrFromID(aTileMapWrapper.getLayerID()), 
                    aTileMapWrapper.getDetailLevel());
            int mc2Lat = aTileMapWrapper.getTileIndexLat() * mc2Unit;
            int mc2Lon = aTileMapWrapper.getTileIndexLon() * mc2Unit;                
            entry = (IndexTableEntry)m_cacheQuadTree.getEntry(mc2Lat, mc2Lon, aTileID, m_CacheFileHandler);   

            /* Remove the entry if it's saved in a page that isn't active anymore. See documentation in
             * the moveToNextPage() method. */
            if(entry != null && (m_CurrentPageNumber-NUMBER_OF_PAGES) >= entry.getPage()) {
                if(LOG.isInfo()) {
                    LOG.info("FileCache.getDataFromCache()", "Remove a tiles form a not active page!");
                    LOG.info("FileCache.getDataFromCache()","Page= "+entry.getPage()+
                            " iCurrentPageNumber= "+m_CurrentPageNumber+" name: "+aTileID);
                }
                m_cacheQuadTree.removeEntry(mc2Lat, mc2Lon, aTileID);
                return d;
            }
        }

        if(entry == null) {
            /* The tile isn't in the cache. */
            return d;
        }

        return loadDataFromFile(entry, aTileMapWrapper, aParamString, aTileID);
    }
    
    /**
     * @param entry
     * @param aTileMapWrapper
     * @param aParamString
     * @param aTileID
     * @return
     * 
     * NOTE: This method should be only called from synchronize context
     */
    private byte[] loadDataFromFile(IndexTableEntry entry, 
                                  TileMapLayerWrapper aTileMapWrapper, 
                                  String aParamString, 
                                  String aTileID) {
        
        long time = System.currentTimeMillis();
        
        DataInputStream din = null;
        byte []data = null;
        
        try {
        
            /* Read the cached data. */
            din = m_CacheFileHandler.getPageDataInputStream(entry.getPage()%NUMBER_OF_PAGES);
            
            if(din == null) {
                throw new IOException("Cache.getDataFromCache(): page "+entry.getPage()+" doesn't exist");
            }
            
            /* Skip data to the correct position in the cache file. */
            if(din.skipBytes(entry.getOffset()) != entry.getOffset()) {
                din.close();
                throw new IOException("Unable to skip "+entry.getOffset()+" number of bytes");
            }
            
            TileMapParams tileIDParams = null;
            String paramString;
            byte paramSize;
            short dataSize;
            
            /* Make a copy of the TileMapParams object, this object is used
             * when creating the parameter string from the cached data*/
            if(aTileMapWrapper != null) {
                tileIDParams = aTileMapWrapper.getTileIDParam().cloneTileMapParams();
            }
            
            /* Read the total number of bytes cached for this tile ID. */
            int totalSize = din.readInt();
            /* Read the empty importance bit field for this tile. */
            int emptyImp = din.readShort();
            if(aTileMapWrapper != null)
                aTileMapWrapper.setAllEmptyImportances(emptyImp);
             
            int currentOffset = CACHE_HEADER_OFFSET;
            byte importanceNbr;
            
            /* For all cached importance */
            while(currentOffset < totalSize) {
                
                if(TileMapParamTypes.isMap(aTileID)) {                          
                    importanceNbr = din.readByte();
                    byte mapType = din.readByte();
                    if(mapType == TileMapParams.MAP) {
                        tileIDParams.setLanguageType(LangTypes.SWEDISH);
                    } else { 
                        tileIDParams.setLanguageType(m_Language);
                    }
                    
                    /* Create the parameter string for the loaded importance. */
                    bitBuffer.softReset();
                    paramString = tileIDParams.getAsString(bitBuffer, importanceNbr, mapType);
                    currentOffset += 2;
                    
                } else {
                    paramSize = din.readByte(); 
                    if (paramSize > paramData.length || paramSize <= 0) {
                        //TODO find why this can happen
                        throw new IOException("Corrupted file cache paramSize has wrong value " + paramSize);
                    }
                    din.readFully(paramData, 0, paramSize);
                    paramString = new String(paramData, 0, paramSize);
                    currentOffset += (1 + paramSize);
                }
                
                /* Read the byte buffer for the importance. */
                dataSize = din.readShort();
                byte []b = new byte[dataSize];
                din.readFully(b);
                currentOffset += (2 + dataSize);
                
                /* Write all importance to the memory cache. */
                m_MemCache.writeToCache(paramString, b);
                
                /* Return the byte buffer if the parameter string are
                 * the one that we ask for.*/
                if(paramString.equals(aParamString)) {                      
                    data = b;
                }
            }
        } catch (Exception e) {

            if(LOG.isError()) {
                LOG.error("FileCache.loadDataFromFile()", e);
            }

            /* Remove the index from the storage if a exception occur while reading. */
            try {
                if(TileMapParamTypes.isMap(aTileID)) {
                    TileMapParams params = aTileMapWrapper.getTileIDParam();
                    final int mc2unit = 
                        (int)m_tmfd.getMc2UnitsPerTile(m_tmfd.getLayerNbrFromID(params.getLayerID()), params.getDetailLevel());
                    final int lat = params.getTileIndexLat() * mc2unit;
                    final int lon = params.getTileIndexLon() * mc2unit;
                    m_cacheQuadTree.removeEntry(lat, lon, params.getTileID());
                } else {
                    m_IndexTableHashtable.remove(aTileID);                        
                }
            } catch (Exception ex) {
                if(LOG.isError()) {
                    LOG.error("FileCache.loadDataFromFile()", ex);
                }
            }            
        } finally {            
            try {
                if(din != null)
                    din.close();            
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("FileCache.loadDataFromFile()", e);
                }
                din = null;
            }
        }
                
        if(LOG.isDebug()) {
            LOG.debug("FileCache.loadDataFromFile()", "tileID= "+aTileID+
                " page= "+entry.getPage()+" offset= "+entry.getOffset()+
                " time= "+(System.currentTimeMillis()-time)+" ms");
        }
        return data;
    }
    
    /**
     * Clean up the memory.
     */
    public synchronized void purgeData() {
        try {                
            m_QtNodeVector.removeAllElements();
            m_cacheQuadTree.getAllNodesThatContainsLoadedEntrys(m_QtNodeVector);
            for(int i=0; i<m_QtNodeVector.size(); i++) {
                QuadTreeNode node = (QuadTreeNode)m_QtNodeVector.elementAt(i);
                if(LOG.isDebug()) {
                    LOG.debug("FileCache.purgeData()", "Write node "+node.getName()+
                        " to cache..., node.hasBeenChanged()= "+node.hasBeenChanged());
                }
                
                if(node.hasBeenChanged()) {
                    m_CacheFileHandler.writeNodeToFile(node);
                }
                node.clear();
            }
            
            m_CacheFileHandler.writeQuadTreeToFile(m_cacheQuadTree);
            m_CacheFileHandler.writeCacheInfoToFile(m_CurrentPageNumber, CACHE_VERSION);
            m_CacheFileHandler.writeIndexTableToFile(m_IndexTableHashtable);
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileCache.purgeData()", e);
            }
        }
    }
    
    /**
     * Called when a tile has been removed from the screen. This method check if there
     * exit any other tiles in the node, if not the node is unloaded from the memory.
     */    
    public synchronized boolean updateLoadedNodes(TileMapLayerWrapperInterface wrapper, Hashtable aRequestedWrappers) {
            long time = System.currentTimeMillis();
            int layerNbr = m_tmfd.getLayerNbrFromID(wrapper.getLayerID());
            int mc2UnitPerTile = (int)m_tmfd.getMc2UnitsPerTile(layerNbr, wrapper.getDetailLevel());
            int lat = wrapper.getTileIndexLat() * mc2UnitPerTile;
            int lon = wrapper.getTileIndexLon() * mc2UnitPerTile;
            
            QuadTreeNode node = m_cacheQuadTree.getNode(lat, lon);
            
            /* If a node has become not visible before it has been loaded from disc, 
             * i.e. tiles has only been loaded from the memory cache. */
            if(node == null || node.getAllEntrys() == null) {
                return false;
            }
            
            boolean exist = false;
            Enumeration params = aRequestedWrappers.elements();
            
            /* Check to see if there exist any loaded wrappers inside the node, 
             * if it does we don't unload the node, else we save it on disc. */
            while(params.hasMoreElements()) {
                TileMapLayerWrapperInterface w = (TileMapLayerWrapperInterface)params.nextElement();
                layerNbr = m_tmfd.getLayerNbrFromID(w.getLayerID());           
                mc2UnitPerTile = (int)m_tmfd.getMc2UnitsPerTile(layerNbr, w.getDetailLevel());
                lat = w.getTileIndexLat() * mc2UnitPerTile;
                lon = w.getTileIndexLon() * mc2UnitPerTile;
                
                if(lat > node.getMinLat() && lat < node.getMaxLat() &&
                   lon > node.getMinLon() && lon < node.getMaxLon()) {
                    exist = true;
                    break;
                }
            }
            
            /* Unload the node if no more tiles are loaded in the node */
            if(!exist) {
                if(m_IsVisible) {
                    /* Write the node to disc if the node has been updated. */
                    if(node.hasBeenChanged()) {
                        try {
                            m_CacheFileHandler.writeNodeToFile(node);
                            m_CacheFileHandler.writeQuadTreeToFile(m_cacheQuadTree);
                            m_CacheFileHandler.writeCacheInfoToFile(m_CurrentPageNumber, CACHE_VERSION);
                        } catch (Exception e) {
                            if(LOG.isError()) {
                                LOG.error("FileCache.updateLoadedNodes()", e);
                            }
                        }
                    }
                    if(LOG.isInfo()) {
                        LOG.info("FileCache.updateLoadedNodes()", "Unload node: "+node.getName()+
                                " time= "+(System.currentTimeMillis()-time)+
                                " ms, node.hasBeenChanged()= "+node.hasBeenChanged());
                    }
                    
                    /* Unload the node from memory. */
                    node.clear();
                } else {
                    /* Add */
                    if(!iLoadedNodes.contains(node)) {
                        iLoadedNodes.addElement(node);
                    }
                }
            }
            
            if(m_IsVisible && (iLoadedNodes.size() > 0)) {                
                removeNodesFromLoadedVector(aRequestedWrappers);                
            }

            return !exist;
    }
    
    /**
     * When we leave the map view we save all active nodes in to a vector to avoid having to unload them
     * when we exit the view. The probability that we want to load tiles from the same node are high when
     * we enter the map view again. Therefore we keep the current visible nodes into the memory and unload
     * them if we don't need them the next time we enter the map view. 
     * 
     * This is what this method does. It check the current saved nodes in the iLoadedNodes vector and
     * save the nodes that are not visible on the screen anymore. 
     * 
     * @param aRequestedWrappers the hashtable that contains the current loaded wrappers.
     * 
     * NOTE: This method should be only called from synchronize context
     */
    private void removeNodesFromLoadedVector(Hashtable aRequestedWrappers) {
        
        if(LOG.isInfo()) {
            LOG.info("FileCache.removeNodesFromLoadedVector()", "remove from loadedNodesVector, size= "+iLoadedNodes.size());
        }
        
        if(LOG.isInfo()) {
            LOG.info("FileCache.removeNodesFromLoadedVector()", "iLoadedNodes.size()= "+iLoadedNodes.size());
        }
        
        boolean exist = false;
        
        final int size = iLoadedNodes.size();                
        for(int i=(size-1); i>=0; i--) {
            
            QuadTreeNode node = (QuadTreeNode)iLoadedNodes.elementAt(i);
            iLoadedNodes.removeElementAt(i);
            
            if(LOG.isInfo()) {
                LOG.info("FileCache.removeNodesFromLoadedVector()", "node= "+node);
            }
            
            /* If a loaded nodes has been split into 4 sub nodes before we have
             * handle it it can be null. Just continue with the next node. The 
             * current node has already been saved on disc. */
            if(node == null)
                continue;
            
            if(LOG.isDebug()) {
                LOG.debug("FileCache.removeNodesFromLoadedVector()", "name= "+node.getName()+" hasBeenChanged= "+node.hasBeenChanged());
            }
            
            if(node.hasBeenChanged()) {
                Enumeration params = aRequestedWrappers.elements();
                exist = false;
                
                /* Check to see if there exist any loaded wrappers inside the node, 
                 * if it does we don't unload the node, else we save it on disc. */
                while(params.hasMoreElements()) {
                    TileMapLayerWrapperInterface w = (TileMapLayerWrapperInterface)params.nextElement();
                    int layerNbr = m_tmfd.getLayerNbrFromID(w.getLayerID());           
                    int mc2UnitPerTile = (int)m_tmfd.getMc2UnitsPerTile(layerNbr, w.getDetailLevel());
                    int lat = w.getTileIndexLat() * mc2UnitPerTile;
                    int lon = w.getTileIndexLon() * mc2UnitPerTile;
                    
                    if(lat > node.getMinLat() && lat < node.getMaxLat() &&
                       lon > node.getMinLon() && lon < node.getMaxLon()) {
                        exist = true;
                        break;
                    }
                }
                
                if(!exist) {
                    try {
                        if(LOG.isDebug()) {
                            LOG.debug("FileCache.removeNodesFromLoadedVector()", "write node "+node.getName()+" to file");
                        }
                        
                        m_CacheFileHandler.writeNodeToFile(node);
                        m_CacheFileHandler.writeQuadTreeToFile(m_cacheQuadTree);
                        m_CacheFileHandler.writeCacheInfoToFile(m_CurrentPageNumber, CACHE_VERSION);      
                        node.clear();
                    } catch (Exception e) {
                        if(LOG.isError()) {
                            LOG.error("FileCache.removeNodesFromLoadedVector()", e);
                        }
                        node.clear();
                    }
                }                        
            }
        }                                
    }
    
    /**
     * Check if the tile id exist in the cache.
     * 
     * @param aTileID the id for the tile map.
     * @return true if the tile id exist in the cache, false if not.
     */
    public synchronized boolean existInCache(TileMapParams aParams) {
            final int layerNbr = m_tmfd.getLayerNbrFromID(aParams.getLayerID());
            final int mc2Unit = (int)m_tmfd.getMc2UnitsPerTile(layerNbr, aParams.getDetailLevel());
            final int lat = aParams.getTileIndexLat() * mc2Unit;
            final int lon = aParams.getTileIndexLon() * mc2Unit;
            QuadTreeEntry entry = m_cacheQuadTree.getEntry(lat, lon, aParams.getTileID(), m_CacheFileHandler);
            return (entry != null);
    }
    
    /**
     * Remove the cache entry with the id specified by the parameter
     * from the cache. The index file will be updated on disc. 
     */
    public synchronized void removeFromCache(TileMapParams aParams) {
        
        if(LOG.isInfo()) {
            LOG.info("FileCache.removeFromCache()", "aTileID= "+aParams.getTileID()+" isOpen= "+m_IsCacheOpen);
        }
        
        /* Return if the cache isn't open. */
        if(!m_IsCacheOpen)
            return;

        try {
            if(TileMapParamTypes.isMap(aParams.getTileID())) {
                final int mc2unit = 
                    (int)m_tmfd.getMc2UnitsPerTile(m_tmfd.getLayerNbrFromID(aParams.getLayerID()), aParams.getDetailLevel());
                final int lat = aParams.getTileIndexLat() * mc2unit;
                final int lon = aParams.getTileIndexLon() * mc2unit;
                m_cacheQuadTree.removeEntry(lat, lon, aParams.getTileID());
            } else {
                m_IndexTableHashtable.remove(aParams.getTileID());                    
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileCache.removeFromCache()", e);
            }
        }
    }
    
    /**
     * @return
     * @throws IOException
     * 
     * NOTE: This method should be only called from synchronize context
     */
    private boolean moveToNextPage() throws IOException {
        
        /* Move to the new page. */
        m_CurrentPageNumber++;
        m_FilePageNumber = (byte)(m_CurrentPageNumber%NUMBER_OF_PAGES);
        m_CurrentOffset = 0;
        
        /* To avoid having to go throw all nodes and remove all old items that points
         * to a inactive page we just increase the page number and remove any inactive 
         * items when we load the node. Since the page are represented by a byte the maximal
         * number of pages are 127 before we need to start over with page 0. Since we
         * don't want to delete more pages then necessary we must choose the largest
         * value for a byte that are 0 when you do % with NUMBER_OF_PAGES. 
         * This value is 120... This is not easy to explain in words, please 
         * ask if you want a better explanation :) 
         * 
         * This will take a long time to do but since the page size are 1 MB we need
         * to have downloaded and cached 120 MB before we need to do it for the first
         * time. 
         * */
        if(m_CurrentPageNumber >= 120) {
            removeInactiveItemsFromNodes();
            m_CurrentPageNumber = 0;
            m_FilePageNumber = 0;
        }
        
        if(dout != null)
            dout.close();
        
        m_CacheFileHandler.clearPageFile(m_FilePageNumber);        
        dout = m_CacheFileHandler.getPageDataOutputStream(m_FilePageNumber, m_CurrentOffset);  
        
        m_CacheFileHandler.writeQuadTreeToFile(m_cacheQuadTree);
        m_CacheFileHandler.writeCacheInfoToFile(m_CurrentPageNumber, CACHE_VERSION);
        
        if(LOG.isDebug()) {
            LOG.debug("FileCache.moveToNextPage()", " iCurrentPageNumber= "+m_CurrentPageNumber+
                    " iFilePageNumber= "+m_FilePageNumber+" iCurrentOffset= "+m_CurrentOffset);
        }
        return true;
    }
    
    /*
     * Go throw all nodes and removes items that points to a page that no longer
     * exist. This method are called when the page number exceed the MAX_BYTE. 
     * Then we need to start over from page 0 and must therefore remove any items
     * pointing to currently inactive pages. 
     * 
     * Note that this method are very time consuming and shouldn't be called 
     * unless absolutely necessary!
     *  
     */
    //XXX: Fix this, the size of the node are wrong! see node.setData(...)
    private void removeInactiveItemsFromNodes() {
        int pageLimit = (m_CurrentPageNumber-NUMBER_OF_PAGES);
        Vector v = new Vector();
        m_cacheQuadTree.getAllNodes(v);
        final int size = v.size();
        
        if(LOG.isInfo()) {
            LOG.info("FileCache.removeInactiveItemsFromNodes()", "AllNodes.size= "+size+" pageLimit= "+pageLimit);
        }
        
        /* Go throw all the nodes and check if there are still any items that has a reference to a 
         * page that has been removed. */
        for(int i=0; i<size; i++) {
            QuadTreeNode node = (QuadTreeNode)v.elementAt(i);
            int nodeSize = node.getSize();
            
            if(node.getAllEntrys() == null) {
                m_CacheFileHandler.readNodeFromFile(node);
                nodeSize = node.getSize();
                if(LOG.isInfo()) {
                    LOG.info("FileCache.removeInactiveItemsFromNodes()", 
                            "Read node from file: "+node.getName()+" size= "+node.getSize());
                }
            }
            IndexTableEntry qte = (IndexTableEntry)node.getAllEntrys();
            boolean removeFirst = true;
            while(removeFirst) {
                if(qte != null && qte.getPage() <= pageLimit) {
//                    node.removeEntry(qte.getLatitude(), qte.getLongitude(), qte.getName());
//                    node.setData(qte.getNext());
                    nodeSize--;
                    qte = (IndexTableEntry)qte.getNext();
                } else {
                    removeFirst = false;
                }
            }
            while(qte != null && qte.getNext() != null) {
                IndexTableEntry e = (IndexTableEntry)qte.getNext();
                if(e.getPage() <= pageLimit) {
                    qte.setNext(qte.getNext().getNext());
                    nodeSize--;
                }                        
                qte = (IndexTableEntry)qte.getNext();
            }
            
            if(LOG.isInfo()) {
                LOG.info("FileCache.removeInactiveItemsFromNodes()", "nodeSize= "+nodeSize+" node.getSize= "+node.getSize());
            }
            
            /* Write the node to file if it has been changed. */
            if(nodeSize != node.getSize()) {
//                node.setSize(nodeSize);
                m_CacheFileHandler.writeNodeToFile(node);
            }                
        }
    }
    
    /**
     * Close the cache. 
     */
    public synchronized void closeCache() {
        try {            
            /* Save any loaded nodes that has need modified to file before we 
             * close the cache. */
            if(iLoadedNodes.size() > 0) {
                boolean cacheChanged = false;
                for(int i=0; i<iLoadedNodes.size(); i++) {
                    QuadTreeNode node = (QuadTreeNode)iLoadedNodes.elementAt(i);
                    if(node != null && node.hasBeenChanged()) {
                        m_CacheFileHandler.writeNodeToFile(node);
                        cacheChanged = true;
                        node.clear();
                    }
                }
                iLoadedNodes.removeAllElements();
                if(cacheChanged) {
                    m_CacheFileHandler.writeQuadTreeToFile(m_cacheQuadTree);
                    m_CacheFileHandler.writeCacheInfoToFile(m_CurrentPageNumber, CACHE_VERSION);                                       
                }
            }
            
            /* Write the index hash to file when we exit the application. The nodes and quad tree
             * file has been saved when we unload the maps from memory and doesn't have to be done here. */
            m_CacheFileHandler.writeIndexTableToFile(m_IndexTableHashtable);                        
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileCache.closeCache()", e);
            }
        }
    }

    Hashtable getIndexTableHashtable() {
        return m_IndexTableHashtable;
    }

    QuadTree getCacheQuadTree() {
        return m_cacheQuadTree;
    }

    FileHandler getCacheFileHandler() {
        return m_CacheFileHandler;
    }

    byte getCurrentPageNumber() {
        return m_CurrentPageNumber;
    }

    byte getFilePageNumber() {
        return m_FilePageNumber;
    }

    int getErrorCount() {
        return m_ErrorCount;
    }
    
    int getCurrentOffset() {
        return m_CurrentOffset;
    }
}
