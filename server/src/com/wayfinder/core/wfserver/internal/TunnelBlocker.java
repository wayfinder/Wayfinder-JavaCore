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
package com.wayfinder.core.wfserver.internal;

import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.wfserver.tunnel.TunnelException;
import com.wayfinder.core.wfserver.tunnel.TunnelResponse;
import com.wayfinder.core.wfserver.tunnel.TunnelResponseListener;

final class TunnelBlocker implements TunnelResponseListener {
    
    private boolean responseReceived;
    private TunnelResponse m_response;
    private CoreError m_error;
    
    synchronized TunnelResponse getResponse() throws TunnelException {
        while(!responseReceived) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        if(m_response != null) {
            return m_response;
        }
        throw new TunnelException(m_error);
    }
    
    
    private synchronized void done(TunnelResponse response, CoreError error) {
        m_response = response;
        m_error = error;
        responseReceived = true;
        notifyAll();
    }

    //-------------------------------------------------------------------------
    // TunnelResponseListener ifc
    
    
    public void tunnelResponse(RequestID reqID, TunnelResponse response) {
        done(response, null);
    }

    
    public void error(RequestID requestID, CoreError error) {
        done(null, error);
    }

}
