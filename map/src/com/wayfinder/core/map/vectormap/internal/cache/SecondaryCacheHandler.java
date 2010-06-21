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
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.io.WFByteArrayInputStream;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.pal.error.PermissionsException;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.SecondaryCacheStorage;

/**
 * 
 * 
 * 
 */
public class SecondaryCacheHandler {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(SecondaryCacheHandler.class);
    
    private PersistenceLayer m_PersistenceLayer;
    
    /* Name of the RecordStore that holds the cached data. */
    private static final String CACHE_PAGE     = "cachePage";  
    private SecondaryCacheStorage[] cachePage;
    
    /* Name of the Index table that holds the keys for each entry in the page. */
    private static final String CACHE_INDEX_TABLE = "cacheIndex";
    private SecondaryCacheStorage[] cacheIndex;
    
    /* Name of the Info file, that holds information about the current active page
     * between two sessions. */
    private static final String CACHE_INFO     = "cacheInfo";
    private SecondaryCacheStorage cacheInfo;
   
    private WFByteArrayInputStream m_PageBAIS;
    private DataInputStream m_Page_din;
    
    private WFByteArrayOutputStream m_PageBAOS;
    private DataOutputStream m_Page_dout;
    
    private WFByteArrayOutputStream iIndexBAOS;
    private DataOutputStream iIndexTabledout;
    
    
    private final int m_NumberOfPages;
    private int lastPage = -1;
    
    private byte m_StartPage = 0;
    private int m_StartOffset = 0;
    private int m_Version = 0;
    private int m_MaxPageSize = -1;
    
    public SecondaryCacheHandler(PersistenceLayer perLayer, int nbrOfPages) {
        m_PersistenceLayer = perLayer;
        m_NumberOfPages = nbrOfPages;
        cachePage = null;
        cacheIndex = null;
        cacheInfo = null;
        m_Page_din = null;
        m_PageBAOS = null;
        m_Page_dout = null;
        iIndexBAOS = null;
        iIndexTabledout = null;
        m_PageBAIS = null;
    }
    
    /* Index table methods */
    /**
     * Return the DataInputStream for the index file.  
     */
    DataInputStream getIndexTableDataInputStream(int page) throws IOException {
        if(cacheIndex[page].size() > 0)
            return cacheIndex[page].getDataInputStream();
        else
            return null;
    }
    
    /**
     * 
     * @param currentPage
     * @param nbrOfTiles
     * @param indexTableHash
     * @return
     * @throws IOException
     */
    boolean writeIndexTableToCache(int currentPage, int nbrOfTiles, Hashtable indexTableHash) throws IOException {
        
        if(nbrOfTiles == 0)
            return false;
        
      
        iIndexBAOS.reset();
        
        /* Write the number of tiles that will be cached. */
        iIndexTabledout.writeInt(nbrOfTiles);        
        int cnt = 0;
        Enumeration iEnum = indexTableHash.elements();
        IndexTableEntry entry;
        while(iEnum.hasMoreElements()) {
            entry = (IndexTableEntry)iEnum.nextElement();
            if(entry.getPage() == currentPage) {
                cnt++;
                iIndexTabledout.writeByte(entry.getPage());
                iIndexTabledout.writeInt(entry.getOffset());
                iIndexTabledout.writeByte(entry.getName().getBytes().length);
                iIndexTabledout.write(entry.getName().getBytes());
            }
        }
        
        return cacheIndex[currentPage].writeToStorage(iIndexBAOS.getByteArray(), 0, iIndexBAOS.size());                
    }
     
    /**
     * Return the DataOutputStream for the current active page. 
     */
    DataOutputStream getActivePageDataOutputStream() {       
        m_PageBAOS.reset();
        return m_Page_dout;
    }
    
    /**
     * Return the DataInputStream for the page specified by the parameter. 
     * 
     * @param page the page to load data from
     * @param currentPageNumber the current active page
     * @return 
     * @throws IOException
     */
    DataInputStream getDataInputStream(int page, int currentPageNumber) throws IOException {
        
        // If the page is the current active page
        if(page == currentPageNumber) {
            m_PageBAIS.reset();
            // Check if din has changed since the last time we read data from it. 
            if(page != lastPage || (m_PageBAOS.size() != m_PageBAIS.available() && page == lastPage)) {       
                lastPage = page;
                
                if(LOG.isInfo()) {
                    LOG.info("SecondaryCacheHandler.getDataInputStream()", "SAME PAGE... copy "+m_PageBAOS.size()+" bytes into iBAIS!");
                }
                
                m_PageBAIS.reset();          
                System.arraycopy(m_PageBAOS.getByteArray(), 0, m_PageBAIS.getByteArray(), 0, m_PageBAOS.size());
                m_PageBAIS.setCount(m_PageBAOS.size());
            }
            return m_Page_din;
        } else if(cachePage[page].size() > 0){            
            return cachePage[page].getDataInputStream();
        }
        
        return null;
    }
    
    /**
     * Write the current DataOutputStream to the current active page in the cache. 
     * 
     * @param dout DataOutputStream for the current page. 
     * @return
     */
    boolean writeOutputStreamToCache(DataOutputStream dout, int currentPageNumber) throws IOException {        
        return cachePage[currentPageNumber].writeToStorage(m_PageBAOS.getByteArray(), 0, m_PageBAOS.size());
    }
    
    /**
     * Update the cache info file with the current active page and offset. <p> 
     * 
     * This is used when starting the cache to know where to start writing new data 
     * 
     * @return true if the update was successfully 
     * @throws IOException
     */
    boolean updateCacheInfoFile(int currentPageNumber, int version) throws IOException {   
        
        iIndexBAOS.reset();
        iIndexTabledout.writeByte(currentPageNumber);
        iIndexTabledout.writeInt(version);
        
        return cacheInfo.writeToStorage(iIndexBAOS.getByteArray(), 0, iIndexBAOS.size());
    }
    
    /**
     * Return the active page saved in the cache info file. 
     * 
     * @return
     */
    byte getStartPage() {
        return m_StartPage;
    }
    
    /**
     * Return the offset where to start writing new data. 
     * 
     * @return
     */
    int getStartOffset() {
        return m_StartOffset;
    }
    
    /**
     * Return the version of the cache saved in the info file. 
     * 
     * @return
     */
    int getVersion() {
        return m_Version;
    }
    
    /**
     * Return the max number of bytes for each page in the cache. 
     * 
     * @return
     */
    int getMaxPageSize() {
        return m_MaxPageSize;
    }
    
    /* Methods for open and close the cache */      
    boolean internalOpenCache() {
        
        try {
            cachePage = new SecondaryCacheStorage[m_NumberOfPages];
            cacheIndex = new SecondaryCacheStorage[m_NumberOfPages];
            for(int i=0; i<m_NumberOfPages; i++) {
                cachePage[i] = m_PersistenceLayer.openSecondaryCacheStorage(CACHE_PAGE+i);
                cacheIndex[i] = m_PersistenceLayer.openSecondaryCacheStorage(CACHE_INDEX_TABLE+i);
            }
            cacheInfo = m_PersistenceLayer.openSecondaryCacheStorage(CACHE_INFO);
            m_MaxPageSize = cacheInfo.getMaxPageSize();
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("SecondaryCacheHandler.internalOpenCache()", e);
            }
             
            return false;
        }
        
        m_PageBAOS = new WFByteArrayOutputStream(m_MaxPageSize);
        m_Page_dout = new DataOutputStream(m_PageBAOS);
        
        iIndexBAOS = new WFByteArrayOutputStream(32000);
        iIndexTabledout = new DataOutputStream(iIndexBAOS);
        
        m_PageBAIS = new WFByteArrayInputStream(new byte[m_MaxPageSize]);
        m_Page_din = new DataInputStream(m_PageBAIS);
        
        return true;
    }   
    
    void loadCacheInfoFile() {
        DataInputStream din = null;
        try {
            if(cacheInfo.size() > 0) {
                din = cacheInfo.getDataInputStream();
                if(din != null) {
                    m_StartPage = din.readByte();
                    m_StartOffset = cachePage[m_StartPage].size();
                    m_Version = din.readInt();
                }
            } 
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("SecondaryCacheHandler.loadCacheInfoFile()", "e1= "+e);
            }
            /* Ignore the error, the info file will be updated when we close 
             * the cache and/or we have filled up a page. */
        } finally {
            try {
                if(din != null)
                    din.close();
            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("SecondaryCacheHandler.loadCacheInfoFile()", e);
                }
            }
        }
    }
    
    /**
     * 
     * Sets the start offset where to start writing new data to the current active page. 
     * 
     * @param aPage
     * @param aOffset
     * @return
     */
    DataOutputStream initStartPage(int aPage, int aOffset) {
                
        DataInputStream din = null;
        try {
            m_PageBAOS.reset();
            final int size = cachePage[aPage].size();
            
            if(size > 0) {
                din = cachePage[aPage].getDataInputStream(); 
                
                int readSize = din.read(m_PageBAOS.getByteArray(), 0, size);                
                if(readSize != size)
                    throw new IOException("Unable to set init offset! size= "+size+" readSize= "+readSize);
                m_PageBAOS.setCount(size);
            }
        } catch (Exception e) {
            
            if(LOG.isError()) {
                LOG.error("SecondaryCacheHandler.initStartPage()", e);
            }
            
            m_PageBAOS.reset();
            return null;
        } finally {
            try {
                if(din != null) {
                    din.close();
                }
            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("SecondaryCacheHandler.initStartPage()", e);
                }
            }
        }
        
        return m_Page_dout;
    }
    
    boolean internalCloseCache() {
        
        for(int i=0; i<m_NumberOfPages; i++) {
            cachePage[i].close();
            cacheIndex[i].close();
        }
        cacheInfo.close();
        return true;        
    }
    
    // Reboot the cache
    void rebootCache() {
        
        try {            
            for(int i=0; i<m_NumberOfPages; i++) {
                // Reboot the page files
                if(cachePage[i].size() > 0) {
                    cachePage[i].delete();
                    cachePage[i] = m_PersistenceLayer.openSecondaryCacheStorage(CACHE_PAGE+i);
                }
                
                // Reboot the cache index files
                if(cacheIndex[i].size() > 0) {
                    cacheIndex[i].delete();
                    cacheIndex[i] = m_PersistenceLayer.openSecondaryCacheStorage(CACHE_INDEX_TABLE+i);
                }               
            }
            
            // Reboot the cache info file
            if(cacheInfo.size() > 0) {
                cacheInfo.delete();
                cacheInfo = m_PersistenceLayer.openSecondaryCacheStorage(CACHE_INFO);
            }
            
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.rebootCache()", "e= "+e);
            }
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.rebootCache()", "e= "+e);
            }
        }
    }
    
}
