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

package com.wayfinder.core.shared.error;


/**
 * Base class for all errors reported via
 * com.wayfinder.core.shared.ResponseListener.
 * 
 * Also serves as a general unspecified error case.
 */
public class CoreError {
    
    /**
     * Error is a general unspecified case.
     */
    public static final int ERROR_GENERAL    = 0;
    
    
    /**
     * Error originates from an unexpected situation in the Core.
     * <p>
     * For more information, this error can be cast to an
     * {@link UnexpectedError}
     */
    public static final int ERROR_UNEXPECTED = 1;
    
    
    /**
     * Error originates from a network failure.
     * <p>
     * For more information, this error can be cast to a
     * {@link com.wayfinder.core.network.NetworkError}
     */
    public static final int ERROR_NETWORK    = 2;
    
    
    /**
     * Error originates from the server.
     * <p>
     * For more information, this error can be cast to a
     * {@link com.wayfinder.core.network.ServerError}
     */
    public static final int ERROR_SERVER        = 3;
    
    
    /**
     * Error originates from the search.
     * <p>
     * For more information, this error can be cast to a
     * {@link com.wayfinder.core.search.SearchError}
     */
    public static final int ERROR_SEARCH     = 4;
    
    /**
     * Error that signal the cancel of the request.
     * <p>
     * This is a convenient way to signal that a request has been 
     * cancel because is obsoleted by a new request or in the future
     * has be canceled by the user  
     * For more information, this error can be cast to a
     * {@link CancelError}.
     */
    public static final int ERROR_CANCEL     = 5;
    
    
    private final String m_internalMsg;
    private final int m_errorType;
    
    /**
     * Subtype constructor.
     * 
     * @param errorType One of the ERROR constants in this class.
     * @param internalMsg if null, will be set to the empty string
     */
    protected CoreError(int errorType, String internalMsg) {
        m_errorType = errorType;
        if (internalMsg == null) {
            m_internalMsg = "";
        } else {
            m_internalMsg = internalMsg;
        }
    }
    
    
    /**
     * Default constructor.
     * 
     * @param internalMsg if null, m_internalMsg will be set to the empty string
     */
    public CoreError(String internalMsg) {
        this(ERROR_GENERAL, internalMsg);
    }
    
    
    
    /**
     * @return an unlocalized message describing the error. Only suitable for
     * debug output. Never returns null.
     */
    public final String getInternalMsg() {
        return m_internalMsg;
    }
    
    
    /**
     * Returns the subtype of this error
     *
     * @return One of the ERROR constants in this class
     */
    public final int getErrorType() {
        return m_errorType;
    }
    
    public String toString() {
        return "CoreError: type="+m_errorType+", message="+ m_internalMsg; 
    }
}
