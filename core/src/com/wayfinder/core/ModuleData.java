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
package com.wayfinder.core;

import com.wayfinder.pal.PAL;


/**
 * A collection class of all data required by the modules to work
 */
public final class ModuleData {
    
    private final PAL m_pal;
    private final ServerData m_bootData;
    private final CallbackHandler m_callbackHandler;

    
    /**
     * Constructor for the ModuleData collection class.
     * 
     * @param pal The {@link PAL} object for the running platform
     * @param data A {@link ServerData} object
     * @param handler An implementation of {@link CallbackHandler}
     */
    public ModuleData(PAL pal, ServerData data, CallbackHandler handler) {
        if(pal == null) {
            throw new IllegalArgumentException("PAL object may not be null");
        } else if(data == null) {
            throw new IllegalArgumentException("ServerData object may not be null");
        } else if(handler == null) {
            throw new IllegalArgumentException("CallbackHandler implementation may not be null");
        }
        m_pal = pal;
        m_bootData = data;
        m_callbackHandler = handler;
    }

    
    /**
     * Returns a reference to the current platform abstraction layer
     * 
     * @return A {@link PAL} object
     */
    public PAL getPAL() {
        return m_pal;
    }
    
    
    /**
     * Returns a reference to the {@link ServerData} object containing info
     * on how to contact the server
     * 
     * @return A {@link ServerData} object
     */
    public ServerData getServerData() {
        return m_bootData;
    }


    /**
     * Returns a reference to the {@link CallbackHandler} implemented by the
     * calling application
     * 
     * @return An implementation of {@link CallbackHandler}
     */
    public CallbackHandler getCallbackHandler() {
        return m_callbackHandler;
    }
    
    

}
