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
package com.wayfinder.core.wfserver.info;

import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.ResponseListener;
import com.wayfinder.core.wfserver.info.internal.ClientUpgradeInfoImpl;

public interface UpgradeCheckListener extends ResponseListener {
    
    /**
     * <p>Called if there is an upgrade available for the client application.
     * Check upgradeInfo to see if the upgrade is a must.</p>
     * @param a {@link ClientUpgradeInfoImpl} containing the information need to 
     * update
     * @param requestID the {@link RequestID} that uniquely identify the request.  
     */
    void clientUpgrade(RequestID requestID, ClientUpgradeInfo upgradeInfo);
    
    /**
     * <p>Called if the client version is the same with one from server. 
     * This will happen most of the time</p>
     * @param requestID the {@link RequestID} that uniquely identify the request. 
     */
    void clientUpToDate(RequestID requestID);
    
    /**
     * <p>Called if the client was not found on server. 
     * Check the data sent in the request and/or the server as something could 
     * be wrong</p>
     * @param requestID the {@link RequestID} that uniquely identify the request. 
     */
    void clientNotFound(RequestID requestID);
    
}
