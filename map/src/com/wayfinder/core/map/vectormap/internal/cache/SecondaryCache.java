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

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapper;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapperInterface;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.SecondaryCacheStorage;

/**
 * 
 * Implementation of the secondary cache system that is used if
 * we can't use the file cache. A typical implementation of the 
 * secondary cache system are a RMS cache. 
 * <p>
 * 
 * The secondary cache is built up like this:<br>
 * The cache contains NUMBER_OF_PAGES pages, the size of each page
 * are defined by {@link SecondaryCacheStorage#getMaxPageSize()}. 
 * To each page in the cache there are one cache index file. The 
 * cache index file contain information in which page file and at 
 * which offset the map data are saved.
 * <p>
 * The cache index objects will be loaded into the memory during runtime
 * and are stored in a {@link Hashtable} as {@link IndexTableEntry} with
 * the parameter string as the key.
 * <p>
 * The cache system also has a cache info file that contain the current active
 * page and the version of the cache. 
 * 
 *
 */
public class SecondaryCache implements CacheInterface {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(SecondaryCache.class);

    /* The number of pages in the cache. */
    private static final int NUMBER_OF_PAGES = 10;
    
    /* The version of the cache. */
    private static final int CACHE_VERSION = 1;
    
    /* 4 bytes for representing the total size of the cache block. 
     * 2 bytes for representing the empty importances for the tile 
     */
    private static final int CACHE_HEADER_OFFSET = 6;
    
    /* 1 byte  for representing the size of the parameter string
     * 1 byte  for representing the TileMap type (map/string)
     * 2 bytes for representing the size of the importance
     */
    private static final int CACHE_IMP_OFFSET = 4;
    
    /* The maximum size for each page (in bytes) */
    private int m_MaxPageSize;
    
    /* The current active page to write into. */
    private byte m_CurrentPageNumber;
    private byte m_StartPage;
    
    /* The number of bytes written to the current active page */
    private int m_CurrentOffset;
    
    /* Holds the number of entry on each page. This is
     * used when writing the index file to the persistent storage.*/
    private int []m_NbrOfEntrysPage;
    
    /* Hashtable that holds the index-table */
    private Hashtable m_IndexTableHashtable;
    
    /* The output stream for the current page to write to */
    private DataOutputStream dout;  
    
    /* Reference to the memory cache */
    private MemCache m_MemCache;
    
    /* The language in the client, default is English. */
    private int m_Language = 0;
    
    private SecondaryCacheHandler m_CacheHandler;
    
    /* Buffer the is used when reading data from cache to avoid re-allocating
     * every time we read from disc. */
    private byte []paramData = new byte[30];
    private BitBuffer bitBuffer = new BitBuffer(new byte[30]);
    
    /* Sets to true if the cache is ready to be used. */
    private boolean m_IsCacheOpen = false;
    
    /* Count the number of critical error that has occur. */
    private int m_ErrorCount = 0;
    
    /* The maximal number of errors that can be detected before
     * we close the cache. The reason for that we don't close the 
     * case directly is to have a little more tolerant approach and
     * allow a small number of errors before we close the cache. */
    private static final int MAX_NUMBER_OF_ERRORS = 3;
    
    /**
     * 
     */
    public SecondaryCache(PersistenceLayer perLayer) {
        
        m_CacheHandler = new SecondaryCacheHandler(perLayer, NUMBER_OF_PAGES);        
        m_NbrOfEntrysPage = new int[NUMBER_OF_PAGES];
        for(int i=0; i<m_NbrOfEntrysPage.length; i++) {
            m_NbrOfEntrysPage[i] = 0;
        }
        
        m_CurrentPageNumber = m_StartPage = 0; 
        m_CurrentOffset = 0;
        
        m_IndexTableHashtable = new Hashtable(100);    
        m_IndexTableHashtable.clear();
        m_MemCache = null;
    }
    
    public synchronized void setMemCache(MemCache memCache) {
        m_MemCache = memCache;
    }
    
    public synchronized void setLanguage(int language) {
        m_Language = language;
    }
    
    // ----------------------------------------------------------------------------------------------
    // Open cache 
    
    /**
     * 
     * Open the cache. 
     * 
     */
    public synchronized boolean openCache() {
        
        m_IsCacheOpen = false;
        
        // Open the cache
        if(m_CacheHandler.internalOpenCache()) {
            
            m_CacheHandler.loadCacheInfoFile();
            m_CurrentPageNumber = m_CacheHandler.getStartPage();
            m_StartPage = m_CurrentPageNumber;
            m_CurrentOffset = m_CacheHandler.getStartOffset();
            m_MaxPageSize = m_CacheHandler.getMaxPageSize();
            
            // Reboot the cache if the version number is wrong. 
            if(m_CacheHandler.getVersion() != CACHE_VERSION) {
                
                if(LOG.isError()) {
                    LOG.error("SecondaryCache.openCache()", "REBOOT CACHE saved version= "+m_CacheHandler.getVersion()+
                            " CACHE_VERSION= "+CACHE_VERSION);
                }
                
                m_CacheHandler.rebootCache();                
                m_CurrentPageNumber = 0;
                m_StartPage = 0;
                m_CurrentOffset = 0;   
                
            } else {
                try {
                    // Load the cache index into the memory. 
                    internalLoadCacheIndex();
                }catch (Exception e) {
                    m_CacheHandler.rebootCache();
                    m_IsCacheOpen = false;
                    return false;
                }
            }
            
            // Set the start offset to the current active page file. 
            dout = m_CacheHandler.initStartPage(m_CurrentPageNumber, m_CurrentOffset);
            if(dout == null) {                
                m_CurrentPageNumber = 0;
                m_StartPage = 0;
                m_CurrentOffset = 0;
            }
            
            m_IsCacheOpen = true;
        } else {
            
            if(LOG.isError()) {
                LOG.error("SecondaryCache.openCache()", "Can't open the cache!");
            }
            m_IsCacheOpen = false;
        }
        
        return m_IsCacheOpen;
    }
    
    /*
     * Load the cache index files into the memory. 
     */
    private void internalLoadCacheIndex() throws IOException {
        
        /* For all pages in the cache, read the cached tiles to the index table. */
        for(int pageNbr=0; pageNbr<NUMBER_OF_PAGES; pageNbr++) {  
            DataInputStream din = null;
            try {
                /* Get the data input stream for the index file. */
                din = m_CacheHandler.getIndexTableDataInputStream(pageNbr);    
                
                /* If the index file exist. */
                if(din != null) {   
                    
                    /* Get the number of entry saved in the index table file. */
                    int size = din.readInt();
                    
                    /* Read the page, offset and tileID and add it to the index table hash. */
                    for(int i=0; i < size; i++) {
                        byte page =  din.readByte();
                        int offset = din.readInt();
                        byte pSize = din.readByte();
                        byte []b = new byte[pSize];                         
                        if(din.read(b, 0, pSize) != pSize)
                            throw new IOException("Unable to read IndexTable when open the cache!");
                        String tileId = new String(b);
                        
                        IndexTableEntry entry = new IndexTableEntry(page, offset, tileId);
                        if(m_IndexTableHashtable.put(tileId, entry) != null) {                                   
                            if(LOG.isError()) {
                                LOG.error("SecondaryCache.openCache()", "tileID= "+tileId+" already exist in one index file!");
                            }
                            m_NbrOfEntrysPage[pageNbr]--;
                        }
                        m_NbrOfEntrysPage[pageNbr]++;                                
                    }
                } else {                        
                    /* If the index file for the file that are set to the current active page are
                     * empty. Start write data from the beginning of the file. This can for example 
                     * happen if the user delete the index file from the memory card. */
                    if(pageNbr == m_CurrentPageNumber) {
                        m_CurrentOffset = 0;
                    }
                }
            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("SecondaryCache.internalLoadCacheIndex()", e);
                }
                
                /* Try to reset the index file if the content are corrupt. */
                
                // Remove all entrys from the index table
                clearPage(pageNbr);
                // Rewrite the index table file with 0 entry
                m_CacheHandler.writeIndexTableToCache(pageNbr, m_NbrOfEntrysPage[pageNbr], m_IndexTableHashtable);
                // If this is the current active page, update the cache info file. 
                if(m_StartPage == pageNbr)
                    m_CacheHandler.updateCacheInfoFile(m_CurrentPageNumber, CACHE_VERSION);                
            } finally {
                try {
                    if(din != null)
                        din.close();
                        
                } catch (Exception e) {
                    if(LOG.isError()) {
                        LOG.error("SecondaryCache.internalLoadCacheIndex()", e);
                    }
                }
            }
        } // for
    }
    
    /**
     * Return true if the cache has been open without any problems. 
     * 
     * @return true if the cache has been open without any problems, false if not. 
     */
    public synchronized boolean isOpen() {
        return m_IsCacheOpen;
    }
    
    // ------------------------------------------------------------------------------------
    // WRITE DATA TO CACHE
    
    /**
     *   Write data to the cache. The data are divided into two different
     *   save format. The reason for that are mainly for making the cached
     *   data more compact for map tiles. Instead of saving the whole parameter
     *   string for each importance we just save the importance number and if
     *   it's a map or string tile. When we read the data from the cache we
     *   generate the correct parameter string.  
     *   <p>
     *
     *   Map tiles (tiles with both a geometric and string tile) are save
     *   at the format below:<br>    
     *   <pre>
     *   |--------------------------------------------------|
     *   | Cache data table           | SIZE                |
     *   |--------------------------------------------------|
     *   | Importance number          | 1 byte              |
     *   | TileMap type (map/string)  | 1 byte              |
     *   | Cache data size            | 2 bytes             |
     *   | Cached data                | #Cached data size   |
     *   |--------------------------------------------------|
     *   </pre>
     *   <p>
     *   Data that isn't map tiles (eg. bitmaps, tmfd) are saved 
     *   at the format below:<br>
     *
     *  <pre>
     *   |---------------------------------------------------|
     *   | Cache data table           | SIZE                 |
     *   |---------------------------------------------------|
     *   | Size of paramString        | 1 byte               |
     *   | ParamString                | #Size of paramString |
     *   | Cache data size            | 2 bytes              |
     *   | Cached data                | #Cached data size    |
     *   |---------------------------------------------------|        
     *   </pre>
     *   <p>
     *   
     *   If more importance need to be cached to a already existing cache
     *   post. Since a map tile can contain different number of importance 
     *   depending on how zoomed in/out you are in a detail level, we can 
     *   be forced to add more data to a entry. The strategy are that we 
     *   collect all available data for the tile and save it to a new cache
     *   entry. The index files will be updated if the changed cache post 
     *   differs from the current active page. If the change effect the current
     *   active page we will update this page when the page are full or when we
     *   exit the application. 
     *   <p>
     *   
     *   When a page are complete we are writing the index file for that file to
     *   disc. That means that if the application crash we are loosing the data
     *   at the current active page. All other pages that contains data will be 
     *   intact the next time we start the application.         
     */ 
    public synchronized boolean writeDataToCache(byte [][]cacheData, TileMapParams []params, TileMapParams tmp, 
                                                 int totSize, int nbrOfImp, short emptyImp) {
        
        /* Return if the cache isn't open or if we try to save a null buffer. */
        if(!m_IsCacheOpen || cacheData == null) {
            return false;
        }
        
        long time = System.currentTimeMillis();
            
        final String aTileID = tmp.getTileID();
        byte []extraData = null;
        int totalSize = totSize;
        totalSize += CACHE_HEADER_OFFSET;
        
        /* If the tile is NOT a map tile */
        if(!TileMapParamTypes.isMap(aTileID)) {
            totalSize += aTileID.getBytes().length;
            totalSize += 3;
        } else {
            totalSize += (CACHE_IMP_OFFSET * nbrOfImp);
        }   
        
        try {
            
            if(dout == null) {               
                dout = m_CacheHandler.getActivePageDataOutputStream();           
            }
            
            IndexTableEntry entry = (IndexTableEntry)m_IndexTableHashtable.remove(aTileID);
            
            /* The entry already exist in the cache and more importance
             * need to be added. */
            if(entry != null) {
                
                long t = System.currentTimeMillis();            
                
                if(LOG.isInfo()) {
                    LOG.info("SecondaryCache.writeDataToCache()", "tileID= "+aTileID+" Exist in cache"+
                        ", offset= "+entry.getOffset()+" page= "+entry.getPage());
                }
                
                m_NbrOfEntrysPage[entry.getPage()]--;
                
                if(TileMapParamTypes.isMap(aTileID)) {
                    extraData = getAlreadyCachedData(entry);
                    if(extraData != null)
                        totalSize += extraData.length;                                        
                }
                
                /* Update the index table files if it's not the current active page. */
                if(entry.getPage() != m_CurrentPageNumber) {
                    final int pageNbr = entry.getPage();
                    m_CacheHandler.writeIndexTableToCache(pageNbr, m_NbrOfEntrysPage[pageNbr], m_IndexTableHashtable);
                }
                
                if(LOG.isDebug()) {
                    LOG.debug("SecondaryCache.writeDataToCache()", " TileID= "+aTileID+" already exist in cache, time: "+
                            (System.currentTimeMillis()-t)+" ms");
                }
            }
            
            /* Check to see if there are enough bytes left on the current page to cache the data. */
            if((m_CurrentOffset + totalSize) > m_MaxPageSize) {              
                if(!moveToNextPage()) {
                    m_ErrorCount++;
                    if(m_ErrorCount >= MAX_NUMBER_OF_ERRORS) {
                        m_IsCacheOpen = false;    
                    }                       
                }
                
                if(totalSize > m_MaxPageSize) {
                    /* You try to cache a block of data that are larger then the 
                     * size of the page. Just return without saving the data in 
                     * the cache.*/
                    return false;
                }
            }
            
            /* Total nbr of bytes for the cache block */
            dout.writeInt(totalSize);
            /* Write empty importance */
            dout.writeShort(emptyImp);
               
            if(TileMapParamTypes.isMap(aTileID)) {
                
                // TileMap tiles, for example the map, poi, traffic info layers.
                internalWriteTileMapData(extraData, cacheData, params, aTileID);                
            
            } else {
                
                // Other stuff that will be cached, for example bitmap images, tmfd etc.
                internalWriteNonTileMapData(cacheData, params, aTileID);                
            }
            m_CurrentOffset += totalSize;
            
        } catch (Exception e) {
            
            m_ErrorCount++;
            if(m_ErrorCount >= MAX_NUMBER_OF_ERRORS) {
                m_IsCacheOpen = false;    
            }   
            
            if(LOG.isError()) {
                LOG.error("SecondaryCache.writeDataToCache()", e);
            }
        }
        
        if(LOG.isDebug()) {
            LOG.debug("SecondaryCache.writeDataToCache()", " page= "+m_CurrentPageNumber+
                    " tileID= "+aTileID+
                    " nbrImp= "+nbrOfImp+
                    " totalSize= "+totalSize+
                    " totDataSize= "+totSize+
                    " iBufferSize= "+m_CurrentOffset+
                    " totTime= "+(System.currentTimeMillis()-time)+" ms");
        }
        
        return true;
    }
    
    /*
     *  |--------------------------------------------------|
     *  | Cache data table           | SIZE                |
     *  |--------------------------------------------------|
     *  | Importance number          | 1 byte              |
     *  | TileMap type (map/string)  | 1 byte              |
     *  | Cache data size            | 2 bytes             |
     *  | Cached data                | #Cached data size   |
     *  |--------------------------------------------------|
    */   
    private void internalWriteTileMapData(byte []extraData, byte [][]cacheData, TileMapParams []params, String tileID) 
        throws IOException {
        
        if(extraData != null) {
            dout.write(extraData);
        }
                       
        for(int i=0; i<cacheData.length; i++) {
            
            TileMapParams param = params[i];
            byte []data = cacheData[i];
            
            if(param != null && data != null) {
                
                /* Write importance number*/                    
                dout.writeByte(param.getImportance());
                /* Write the map type (MAP or STRING)*/
                dout.writeByte(param.getTileMapType());
                
                /* Write data */                
                dout.writeShort(data.length);
                dout.write(data);
            }
        }
        
        dout.flush();
        m_NbrOfEntrysPage[m_CurrentPageNumber]++;
        /* Add the new block to the index table */
        IndexTableEntry entry = new IndexTableEntry(m_CurrentPageNumber, m_CurrentOffset, tileID);       
        m_IndexTableHashtable.put(tileID, entry);
    }
    
    /*
     *  |---------------------------------------------------|
     *  | Cache data table           | SIZE                 |
     *  |---------------------------------------------------|
     *  | Size of paramString        | 1 byte               |
     *  | ParamString                | #Size of paramString |
     *  | Cache data size            | 2 bytes              |
     *  | Cached data                | #Cached data size    |
     *  |---------------------------------------------------|
    */   
    private void internalWriteNonTileMapData(byte [][]cacheData, TileMapParams []params, String tileID) 
        throws IOException {
                   
        TileMapParams param = params[0];
        byte []data = cacheData[0];
        
        if(param != null && data != null) {     
            dout.writeByte(param.getAsString().getBytes().length);
            dout.write(param.getAsString().getBytes());
            
            /* Write data */                
            dout.writeShort(data.length);
            dout.write(data);
        }
        
        dout.flush();
        m_NbrOfEntrysPage[m_CurrentPageNumber]++;
        /* Add the new block to the index table */
        IndexTableEntry entry = new IndexTableEntry(m_CurrentPageNumber, m_CurrentOffset, tileID);       
        m_IndexTableHashtable.put(tileID, entry);
    }
    
    /**
     * Called when more importance for a tile needs to be added. This will happen if
     * a user zoom inside a already cached tile since more importance will be available. 
     * 
     * Return the map data for the cached importance for the tile. 
     * 
     * NOTE: This method should be only called from synchronize context
     */
    private byte[]getAlreadyCachedData(IndexTableEntry entry) throws IOException {
        
        DataInputStream din = null;
        
        try {
            din = m_CacheHandler.getDataInputStream(entry.getPage(), m_CurrentPageNumber);
            
            if(din == null)
                throw new IOException("get already cached data, page "+entry.getPage()+" doesn't exist!");
            
            if(din.skip(entry.getOffset()) != entry.getOffset()) {
                throw new EOFException("Unable to skip "+entry.getOffset()+" bytes");
            }
            
            // Read the total size of the buffer
            int size = din.readInt();                    
            // Ignore empty importance
            din.readShort();
            
            int dataSize = (size-CACHE_HEADER_OFFSET);
            byte []extraData = new byte[dataSize];
            if(din.read(extraData, 0, dataSize) != dataSize)
                throw new IOException("Unable to read already cached data from storage!");
            
            return extraData;
        
        } finally {
            if(din != null)
                din.close();
        }
    }
    
    /**
     * Check if the tile id exist in the cache. 
     * 
     * @param aTileID the id for the tile map. 
     * @return true if the tile id exist in the cache, false if not. 
     */
    public synchronized boolean existInCache(TileMapParams params) {
        return m_IndexTableHashtable.containsKey(params.getTileID());
    }
    
    // --------------------------------------------------------------------------------------------------------------
    // READ DATA FROM CACHE
    
    /**
     * Return the byte buffer that correspond to the aParamString parameter if it exist in the cache. 
     * <p>
     * Any other importance that exist for the tile will be added to the memory cache. 
     * 
     * @param tileMapWrapper the wrapper for the tile, can be null. 
     * @param paramStr the parameter string for the requested tile map. 
     * @param tileID the id for the whole tile, can be the same as the parameter string if 
     *                the requested data is not a map tile. 
     * 
     * @return the byte buffer that correspond to the aParamString parameter or null if the tile doesn't exist in the cache. 
     * 
     * @see The writeDataToCache method for information about how data are cached. 
     */
    public synchronized byte []getDataFromCache(TileMapLayerWrapper tileMapWrapper, String paramStr, String tileID) {
        
        byte []data = null;
        
        /* Return if the cache isn't open. */
        if(!m_IsCacheOpen)
            return null;
        
        long time = System.currentTimeMillis();        
        IndexTableEntry entry = (IndexTableEntry)m_IndexTableHashtable.get(tileID);
        
        if(entry == null) {
            /* The tile isn't in the cache. */
            return null;
        }
        
        /* Internal variables*/
        int parameterPage = entry.getPage();            
        DataInputStream din = null;
        String paramString;
        byte importanceNbr;
        short dataSize;
        int currentOffset;
        int totalSize=0;            
        byte paramSize;         
        TileMapParams tileIDParams = null;
        
        /* Make a copy of the TileMapParams object, this object is used
         * when creating the parameter string from the cached data*/
        if(tileMapWrapper != null) {
            tileIDParams = tileMapWrapper.getTileIDParam().cloneTileMapParams();
        }
        
        try {
            
            /* Read the cached data. */
            din = m_CacheHandler.getDataInputStream(entry.getPage(), m_CurrentPageNumber);
            
            if(din == null) {
                throw new IOException("Cache.getDataFromCache(): page "+entry.getPage()+" doesn't exist");
            }
            
            /* Skip data to the correct position in the cache file. */
            if(din.skipBytes(entry.getOffset()) != entry.getOffset())
                throw new IOException("Unable to skip "+entry.getOffset()+" number of bytes");
            
            /* Read the total number of bytes cached for this tile ID in this page*/
            totalSize = din.readInt();           
            /* Read the empty importance bit field for this tile. */
            int emptyImp = din.readShort();
            if(tileMapWrapper != null)
                tileMapWrapper.setAllEmptyImportances(emptyImp);
             
            currentOffset = CACHE_HEADER_OFFSET;
            
            /* For all cached importance */
            while(currentOffset < totalSize) {
                
                if(TileMapParamTypes.isMap(tileID)) {                          
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
                    if(din.read(paramData, 0, paramSize) != paramSize)
                        throw new IOException("Unable to read the paramString from cache!");
                    
                    paramString = new String(paramData, 0, paramSize);
                    currentOffset += (1 + paramSize);
                    
                }
                
                /* Read the byte buffer for the importance. */
                dataSize = din.readShort();
                byte []b = new byte[dataSize];
                if(din.read(b, 0, dataSize) != dataSize)
                    throw new IOException("Unable to read data from cache!");
                currentOffset += (2 + dataSize);
                
                /* Write all importance to the memory cache. */
                m_MemCache.writeToCache(paramString, b);
                
                /* Return the byte buffer if the parameter string are
                 * the one that we ask for.*/
                if(paramString.equals(paramStr)) {                      
                    data = b;
                }
            }                           
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("SecondaryCache.getDataFromCache()", e);
                LOG.error("SecondaryCache.getDataFromCache()", "tileID= "+tileID+" iPageNumber= "+m_CurrentPageNumber+" offset= "+entry.getOffset()+
                        " parameterPage= "+parameterPage+" totalSize= "+totalSize);
            }
            
            /* If a exception occur when reading the data from disc. Reset the current used page. */                
            try {
                clearPage(parameterPage);
                m_CacheHandler.writeIndexTableToCache(parameterPage, m_NbrOfEntrysPage[parameterPage], m_IndexTableHashtable);
                if(m_StartPage == parameterPage)
                    m_CacheHandler.updateCacheInfoFile(m_CurrentPageNumber, CACHE_VERSION);
            } catch (Exception e2) {
                if(LOG.isError()) {
                    LOG.error("SecondaryCache.getDataFromCache()", e);
                }
            }               
        } finally {
            try {
                if(din != null)
                    din.close();
            } catch (Exception e){
                if(LOG.isError()) {
                    LOG.error("SecondaryCache.getDataFromCache()", e);
                }
            }
        }
        
        if(LOG.isDebug()) {
            LOG.debug("SecondaryCache.getDataFromCache()", "page= "+parameterPage+
                " tileID= "+tileID+
                " totSize= "+totalSize+
                " paramString= "+paramStr+
                " totTime= "+(System.currentTimeMillis()-time)+" ms");
        }
            
        return data;
    }
    
    // ---------------------------------------------------------------------------------------------------
    // Index table and change cache page methods
    
    /*
     * Move the the next page in the cache.  
     */
    private boolean moveToNextPage() {
        
        long time = System.currentTimeMillis();
        
        try {
            
            /* Flush outgoing data to the current page. */
            if(!m_CacheHandler.writeOutputStreamToCache(dout, m_CurrentPageNumber))
                return false;
            dout.close();
                        
            /* Write the index table for the current page to disc. */             
            if(!m_CacheHandler.writeIndexTableToCache(m_CurrentPageNumber, m_NbrOfEntrysPage[m_CurrentPageNumber], m_IndexTableHashtable))
                return false;
            
            /* Move to the new page. */
            m_CurrentPageNumber = (byte)((m_CurrentPageNumber + 1) % NUMBER_OF_PAGES);
            m_CurrentOffset = 0;
            
            /* Update the info file with information about the current active
             * page and offset*/
            if(!m_CacheHandler.updateCacheInfoFile(m_CurrentPageNumber, CACHE_VERSION))
                return false;
            
            /* Remove any existing tiles from the index table for the current page.*/
            clearPage(m_CurrentPageNumber);  
        
            /* Open the output stream for the new page. */
            if(dout != null)
                dout.close();
            dout = m_CacheHandler.getActivePageDataOutputStream();
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("SecondaryCache.moveToNextPage()", e);
            }
            return false;
        }
        
        if(LOG.isDebug()) {
            LOG.debug("SecondaryCache.moveToNextPage()", "TOTAL TIME= "+(System.currentTimeMillis()-time)+
                            " ms, iCurrentPageNumber= "+m_CurrentPageNumber);
        }
        return true;
    }
    
    /*
     * NOTE: This method should be only called from synchronize context
     */
    private void clearPage(int pageNbr) {
        
        /* Remove all entrys from the index table hash. */
        Enumeration iEnum = m_IndexTableHashtable.elements();
        IndexTableEntry entry;
        while(iEnum.hasMoreElements()) {
            entry = (IndexTableEntry)iEnum.nextElement();
            if(entry.getPage() == pageNbr) {
                m_IndexTableHashtable.remove(entry.getName());
                m_MemCache.removeFromCache(entry.getName());
            }
        }
        m_NbrOfEntrysPage[pageNbr] = 0;
        if(pageNbr == m_CurrentPageNumber) {
            m_CurrentOffset = 0;
        }
    }
    
    // ---------------------------------------------------------------------------------------------------
    // Close cache methods
    
    /**
     * Save any unsaved data and close the cache.
     */
    public synchronized void closeCache() {
        
        /* Return if the cache isn't open. */
        if(!m_IsCacheOpen)
            return;
        
        if(LOG.isError()) {
            LOG.error("SecondaryCache.closeCache()", " ActivePage= "+m_CurrentPageNumber+" offset= "+m_CurrentOffset);
        }
        
        long time = System.currentTimeMillis();
        
        try {
            /* Write the current data to cache */
            if(dout != null) {
                m_CacheHandler.writeOutputStreamToCache(dout, m_CurrentPageNumber);
                dout.close();
            }
            
            /* Write the index table to cache. */           
            m_CacheHandler.writeIndexTableToCache(m_CurrentPageNumber, m_NbrOfEntrysPage[m_CurrentPageNumber], m_IndexTableHashtable);
            
            /* Update the info file with information about the current active
             * page and offset*/
            m_CacheHandler.updateCacheInfoFile(m_CurrentPageNumber, CACHE_VERSION);
            
            /* Close the cache. */
            m_CacheHandler.internalCloseCache();
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("SecondaryCache.closeCache()", e);
            }
        }       

        if(LOG.isInfo()) {
            LOG.info("SecondaryCache.closeCache()", "time= "+(System.currentTimeMillis()-time)+" ms");
        }
    }
    
    // -------------------------------------------------------------------------------------------------
    // Remove from cache
    
    /**
     * Remove the cache entry with the id specified by the parameter
     * from the cache. The index file will be updated on disc. 
     */
    public synchronized void removeFromCache(TileMapParams params) {
        
        if(LOG.isInfo()) {
            LOG.info("SecondaryCache.removeFromCache()", "aTileID= "+params.getTileID()+" isOpen= "+m_IsCacheOpen);
        }
        
        /* Return if the cache isn't open. */
        if(!m_IsCacheOpen)
            return;
        
        try {                
            IndexTableEntry entry = (IndexTableEntry)m_IndexTableHashtable.remove(params.getTileID());
            if(entry != null) {
                m_NbrOfEntrysPage[entry.getPage()]--;
                if(entry.getPage() != m_CurrentPageNumber) {
                    final int pageNbr = entry.getPage();
                    m_CacheHandler.writeIndexTableToCache(pageNbr, m_NbrOfEntrysPage[pageNbr], m_IndexTableHashtable);
                }
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("SecondaryCache.removeFromCache()", e);
            }
        }        
    }
    
    // --------------------------------------------------------------------------------
    // Methods from CacheInterface that isn't used in the SecondaryCache     
    
    public boolean updateLoadedNodes(TileMapLayerWrapperInterface wrapper,
            Hashtable loadedWrapper) {
        // 
        return false;
    }
    
    public void setTileMapFormatDesc(TileMapFormatDesc tmfd) {
        // 
    }
    
    public void setVisible(boolean visible) {
        // 
    }

    public void saveCache() {
        // 
    }
    
    public void purgeData() {
        // 
        
    }
    
}
