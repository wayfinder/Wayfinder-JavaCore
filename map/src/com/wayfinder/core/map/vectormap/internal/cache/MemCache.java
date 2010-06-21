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

import java.util.Hashtable;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * The memory cache that holds the data buffer for each TileMap, bitmap etc. 
 * in the memory. The data will be put in the memory cache when:<br>
 * 
 * 1. It has been loaded from the server
 * 2. It has been loaded from the cache / pre-installed map files. 
 * <p>
 * 
 * The memory cache holds each data buffer in a hashtable with the parameter 
 * string as the key and the byte buffer as the value. 
 * <p>
 * 
 * The number of pages in the memory cache is specified by N_PAGES <br>
 * The size of each page (in bytes) is specified by MAX_PAGE_SIZE <br>
 * 
 * 
 *
 */
public class MemCache {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(MemCache.class);
    
    // Number of pages in the memory cache. 
    private static final int N_PAGES = 2;
    // The size (in bytes) of each page
    private static final int MAX_PAGE_SIZE = 32000;
    
    private Hashtable []m_MemCacheBuffers;
    private int m_CurrentPage;
    private int m_BufferSize;
    
    public MemCache() {
        
        m_CurrentPage = 0;
        m_BufferSize = 0;
        
        m_MemCacheBuffers = new Hashtable[N_PAGES];
        for(int i=0; i<m_MemCacheBuffers.length; i++) {
            m_MemCacheBuffers[i] = new Hashtable();
        }       
    }
    
    public void clearMemCache() {
        synchronized (m_MemCacheBuffers) {
            for(int i=0; i<m_MemCacheBuffers.length; i++) {
                m_MemCacheBuffers[i].clear();
            }
        }
        
        if(LOG.isInfo()) {
            LOG.info("MemCache.clearMemCache()", "");
        }
    }
    
    public byte []getDataFromCache(String aParamString) {
        byte []data = null;
        for(int i=0; i<m_MemCacheBuffers.length; i++) {
            data = (byte[])m_MemCacheBuffers[i].get(aParamString);
            if(data != null) {
                if(LOG.isTrace()) {
                    LOG.trace("MemCache.getDataFromCache()", "paramString= "+aParamString+" data= "+data.length);
                }             
                return data;
            }
        }
        return data;
    }
    
    public void removeFromCache(String aParamString) {
        for(int i=0; i<m_MemCacheBuffers.length; i++) {
            if(m_MemCacheBuffers[i].remove(aParamString) != null)
                return;
        }
    }
    
    public void writeToCache(String aParamString, byte []aData) {
        
        if(m_MemCacheBuffers[m_CurrentPage].put(aParamString, aData) == null) {
            m_BufferSize += aData.length;
        }
        
        if(m_BufferSize > MAX_PAGE_SIZE) {
            moveToNextPage();
        }       
    }
    
    private void moveToNextPage() {
        m_CurrentPage = (byte)((m_CurrentPage + 1) % N_PAGES);
        m_BufferSize = 0;
        m_MemCacheBuffers[m_CurrentPage].clear();
    }
}
