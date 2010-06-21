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

package com.wayfinder.core.network;

import com.wayfinder.core.shared.error.CoreError;

/**
 * There was a network error, like security error, network time out, server too
 * busy etc.
 * 
 * TODO: define reasonable classes. But we don't need a class for each HTTP
 * Error etc. Think about what kind of error messages the UI will reasonably
 * present to user: "check your settings" and "please try later" and not much
 * more.
 */
public class NetworkError extends CoreError {
    
    /**
     * Signifies that the network connection failed due to lack of permissions
     */
    public static final int REASON_BLOCKED_BY_PERMISSIONS = 0;
    
    
    /**
     * Placeholder. Signifies that the connection failed due to actual networking
     * reasons :)
     * 
     * @deprecated Will be split into better constants
     */
    public static final int REASON_NETWORK_FAILURE        = 1;
    
    private final int m_reason;
    
    private Exception m_cause; 
    
    public NetworkError(String internalMsg, int reason) {
        super(ERROR_NETWORK, internalMsg);
        m_reason = reason;
    }

    public NetworkError(Exception ex, int reason) {
        super(ERROR_NETWORK, ex.toString());
        m_reason = reason;
        m_cause = ex;
    }
    
    /**
     * Returns a constant describing the reason for the network error
     * 
     * @return One eof the REASON constants in this class
     */
    public int getReasonForError() {
        return m_reason;
    }
    
    /**
     * @return the {@link Exception} that caused the error is available 
     */
    public Exception getCause() {
        return m_cause;
    }
    
    public String toString() {
        return "NetworkError: " + this.getInternalMsg();   
    }

}
