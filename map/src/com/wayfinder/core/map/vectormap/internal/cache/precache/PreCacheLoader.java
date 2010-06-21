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
package com.wayfinder.core.map.vectormap.internal.cache.precache;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.persistence.PersistenceLayer;


public class PreCacheLoader implements SFDLoadableHeaderListener, SFDHeaderLoadedListener {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(PreCacheLoader.class);
    
    private boolean headerDone;    
    private SFDLoadableHeader tempHeader;
    private FileHandler tempFileHandler;
    
    public PreCacheLoader() {
        headerDone = false;
    }
    
    public SingleFileDBufRequester addSingleFileCache( String fileName, int lang, PersistenceLayer perLayer) {     
        SingleFileDBufRequester preCache = 
            new SingleFileDBufRequester(fileName,this,lang, perLayer);
        try{
            preCache.loadDone(tempHeader,tempFileHandler);
        }catch(Exception e){
            if(LOG.isError()) {
                LOG.error("PreCacheLoader.addSingleFileCache()", "unable to load file "+fileName);
                LOG.error("PreCacheLoader.addSingleFileCache()", e);
            }
            return null;
        }
        if(preCache.getState() == SingleFileDBufRequester.PERMANENT_ERROR){
            return null;
        }        
        return preCache;
    }
    
    public void loadDone(SFDLoadableHeader header, FileHandler fileHandler) {       
        headerDone = true;
        tempHeader = header;
        tempFileHandler = fileHandler;
    }

    public boolean readDone() {
        return headerDone;
    }

    public void headerLoaded(SingleFileDBufRequester requester) {
        headerDone = true;      
    }
    
    

}
