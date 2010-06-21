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
package com.wayfinder.core.wfserver;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.ServerData;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.wfserver.info.ClientUpgradeInfo;
import com.wayfinder.core.wfserver.info.UpgradeCheckListener;
import com.wayfinder.core.wfserver.tunnel.TunnelException;
import com.wayfinder.core.wfserver.tunnel.TunnelFactory;
import com.wayfinder.core.wfserver.tunnel.TunnelRequest;
import com.wayfinder.core.wfserver.tunnel.TunnelResponse;
import com.wayfinder.core.wfserver.tunnel.TunnelResponseListener;


/**
 * This interface encapsulates network operations where the goal is
 * to access capabilities and resources of the Wayfinder server
 * <p>
 * Examples of these are:
 * <ul>
 * <li>Tunneling of data through the server</li>
 * <li>Access to POI images</li>
 * </ul>
 */
public interface WFServerInterface {
    
    // tunneling
    
    /**
     * Invokes a request to tunnel data through the Wayfinder server.
     * 
     * @param request A {@link TunnelRequest} created through the {@link TunnelFactory}
     * @param listener The {@link TunnelResponseListener} which will receive the
     * response
     * @return A {@link RequestID} to identify the request
     */
    public RequestID tunnel(TunnelRequest request, TunnelResponseListener listener);
    
    
    
    /**
     * Invokes a request to tunnel data through the Wayfinder server.
     * <p>
     * <b>This method is blocking and should never be done on an event dispatcher! 
     * To tunnel asynchronous, please use 
     * {@link #tunnel(TunnelRequest, TunnelResponseListener)} instead</b>
     * 
     * @param request A {@link TunnelRequest} created through the {@link TunnelFactory}
     * @return The {@link TunnelResponse} from the tunneling
     * @throws TunnelException if the tunneling failed
     */
    public TunnelResponse tunnelSynchronous(TunnelRequest request) 
            throws TunnelException;
    
    /**
     * Returns the {@link TunnelFactory} which can be used to create 
     * {@link TunnelRequest} objects
     * 
     * @return The {@link TunnelFactory}
     */
    public TunnelFactory getTunnelFactory();
    
    // end tunneling
    
    /**
     * <p>Check the server if there is a new version available and if current 
     * version is not obsoleted.</p>
     * 
     * <p>This should be the first call that the client dose after creating the 
     * core in order to make sure that the rest of the request will not fail
     * because the protocols have changed. For supporting this the request 
     * dosen't require any user activation and should be run before that.</p>  
     * 
     * <p>The checking is base on {@link ServerData#getClientType()} and 
     * {@link ServerData#getVersionNumber()} used when the core was created</p> 
     * <p>WARNING: If the application name is changed (package name for Android,
     *  MIDlet-Name for J2ME) a separate client type need to be used other way 
     * the update will lead to an install instead of an upgrade as the 
     * {@link ClientUpgradeInfo#getUpgradeUri()} will point to an application 
     * with a different name</p> 
     * 
     * @param A {@link UpgradeCheckListener} that will be notified through 
     * {@link CallbackHandler} with the result. Cannot be null. 
     *     
     * @return A {@link RequestID} to identify the request
     */
    public RequestID clientUpgradeCheck(UpgradeCheckListener listener);
}
