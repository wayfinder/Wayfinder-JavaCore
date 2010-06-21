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
package com.wayfinder.core.network.internal.mc2;

import com.wayfinder.core.network.NetworkInterface;

/** 
 * Internal interface for making mc2 xml server requests.
 *  
 * 
 */
public interface MC2Interface {
    
    /**
     * Adding a request to an internal queue and return immediately
     * Methods on given {@link MC2Request} will be called later
     * The priority is {@link NetworkInterface#PRIORITY_NORMAL}, 
     * and the request is considered joinable   
     * 
     * @param mc2Request represent the request and the handler of the response 
     */
    void pendingMC2Request(MC2Request mc2Request);
    
    /**
     * Adding a request to an internal queue and return immediately
     * Methods on given {@link MC2Request} will be called later   
     * The priority is {@link NetworkInterface#PRIORITY_NORMAL}.
     * 
     * @param mc2Request represent the request and the handler of the response
     * @param joinable if the request can be joined with other requests to 
     * be sent in a single mc2 document. 
     */
    void pendingMC2Request(MC2Request mc2Request, boolean joinable);
    
    /**
     * Adding a request to an internal queue and return immediately
     * Methods on given {@link MC2Request} will be called later   
     * 
     * @param mc2Request represent the request and the handler of the response
     * @param joinable if the request can be joined with other requests to 
     * be sent in a single mc2 document. 
     * @param priority the priority of this request one of the PRIORITY 
     * constants from {@link NetworkInterface}
     */
    void pendingMC2Request(MC2Request mc2Request, boolean joinable, int priority);

    
    /**
     * Adds an attribute to the MC2 Module.
     * <p>
     * Once this is called, the supplied attribute / value will be included in
     * the auth element for all future MC2 documents sent to the server
     * <p>
     * If called multiple times with the same attribute, the last value will replace
     * the former values. If called with value as <code>null</code>, the attribute
     * will be removed from future documents.
     * 
     * @param attrib The name of the attribute, usually from the {@link MC2Strings} class
     * @param value The value to be sent as attribute value
     */
    void setMC2AuthAttribute(String attrib, String value);

    
    /**
     * Adds a listener for handling global status codes and messages from the
     * server.
     * 
     * @param listener The {@link MC2StatusListener} to add
     */
    void addGlobalMC2StatusListener(MC2StatusListener listener);

    
    /**
     * Removes a listener for handling global status codes and messages from the
     * server. There are no guarantees for when the listener will actually be
     * removed.
     * 
     * @param listener The {@link MC2StatusListener} to remove
     */
    void removeGlobalMC2StatusListener(MC2StatusListener listener);
}
