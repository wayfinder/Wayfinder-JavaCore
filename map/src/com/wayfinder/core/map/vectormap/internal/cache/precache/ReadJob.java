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

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.control.TileMapRequestListener;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;

    
public class ReadJob {
    
    public TileMapParams desc;
    public TileMapRequestListener caller;
    public boolean cacheOrInternet;
    
    /** Creates a new instance of ReadJob */
    public ReadJob(TileMapParams desc,TileMapRequestListener caller, boolean cacheOrInternet) {
        this.desc = desc;
        this.caller = caller;
        this.cacheOrInternet = cacheOrInternet;
    }
    
    public void callListener(BitBuffer buf, SingleFileDBufRequester origin){
        // Make temporary string since this can be deleted while
        // sending. Scary.
        TileMapParams tmp = desc;
        caller.requestReceived( tmp, buf, true);
    }
    
    public boolean equals (Object aObject) {
        if (aObject instanceof ReadJob) {
            ReadJob other = (ReadJob) aObject;
            return caller == other.caller &&
                cacheOrInternet == other.cacheOrInternet &&
                desc.equals(other.desc);
        }
        return false;
    }
    
    /// Equals operator, du.
/*  public boolean equals(ReadJob other )  {
        return caller == other.caller &&
            cacheOrInternet == other.cacheOrInternet &&
            desc.equals(other.desc);
    }*/
    
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + (int) (cacheOrInternet ? 1 : 0) ;
        hash = hash * 31 + (desc == null ? 0 : desc.hashCode());
        return hash;
    }
        
    /// Special version where we send some stuff not yet requested.
    public void callListener(TileMapParams str,BitBuffer buf,SingleFileDBufRequester origin) {
        caller.requestReceived( str, buf, true);
    }
}

