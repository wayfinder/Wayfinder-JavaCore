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

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;


/**
 * 
 * Interface that are used when a new byte buffer has arrived from the server
 * or the pre-installed map cache. 
 * 
 * 
 */
public interface TileMapRequestListener {
    
    /**
     * Called when a new bytebuffer has been received (from internet or cache)
     * 
     * @param paramString
     * @param tiledata
     * @param fromCache
     */
    public void requestReceived(String paramString, byte []tiledata, boolean fromCache);
    
    /**
     * Called when a new byte buffer has been received from the pre-installed map cache. 
     * 
     * @param desc, the TileMapParams
     * @param buf
     * @param fromCache, true if the TileMap are loaded from cache
     */
    public void requestReceived(TileMapParams desc, BitBuffer buf, boolean fromCache);
    
    /**
     * Called when the request has faild.
     *
     * The implementor MAY immediately re-request the failed tiles
     * using TileMapRequesterInterface.request(). It MAY do so in the
     * context of the calling thread or in a separate thread. The
     * first case can lead to stack exhaustion (infinite
     * recursion). The latter approach would avoid this problem but
     * still the system can be stuck in an infinite loop where thread
     * T1 fails all incoming requests and thread T2 immediately
     * re-requests them. Thus, observing the rule of "don't call
     * TileMapRequestListener in TileMapRequesterInterface's public
     * methods" is not enough to avoid infinite loops.
     * 
     * @param paramStrings, a array with paramStrings that has to be resend to the server
     */
    public void requestFailed(String []paramStrings);

}
