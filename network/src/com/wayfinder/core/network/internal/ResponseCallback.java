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
package com.wayfinder.core.network.internal;

import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.core.shared.error.CoreError;

/**
 * Act as request listener and response handler, in case of failure 
 * {@link #error(CoreError)}error method. In case of success the implementing 
 * class is responsible for handling the response
 * 
 * Note: Do not use blocking code inside the methods
 * 
 * 
 */
public interface ResponseCallback {
    
    /**
     * Called when request succeed and we have an answer good or bad, 
     * implement it to handle the response.
     * 
     * @param in input stream from where to read the response
     * @param length the length of the response if available, -1 if not
     * @throws IOException any IOException caused by InputStream operations 
     * should be thrown further
     */
    void readResponse(InputStream in, long length) throws IOException;
    
    /**
     * Called if an error has occur during the request or if the request 
     * could not been sent or if an exception has occur during 
     * {@link #readResponse(InputStream, long)}
     * @param error which can be one of the NetworkError or UnexpectedError 
     */
    void error(CoreError error);
}
