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
package com.wayfinder.core.wfserver.tunnel;


public interface TunnelResponse {

    /**
     * Checks to see if the request returned HTTP/1.0 200 OK
     * 
     * @return true if and only if the response code was 200
     */
    public boolean responseWasOK();

    /**
     * Returns the HTTP response status code.
     *
     * @return the HTTP Status-Code or -1 if no status code could be determined
     */
    public int getResponseCode();

    /**
     * Gets the HTTP response message, if one was included in the response
     * 
     * @return the HTTP response message, or <code>null</code>
     */
    public String getResponseMessage();
    
    
    /**
     * Signifies that the body has no encoding
     */
    public static final int BODY_ENCODING_NONE   = 0;
    
    
    /**
     * Signifies that the body has been encoded with Base64
     */
    public static final int BODY_ENCODING_BASE64 = 1;
    
    
    /**
     * Returns the encoding of the body returned from the {@link #getResponseBody()}
     * and {@link #getResponseBodyNoCopy()} methods.
     * <p>
     * This is a temporary method and will be removed in the future when proper
     * base64 decoding is in place
     * 
     * @return One of the BODY_ENCODING constants in this class
     */
    public int getBodyTransferEncoding();
    

    /**
     * Returns a <b>copy</b> of the response body (if any)
     * <p>
     * Do note that a body may exist even if the response code is other than
     * 200 OK. In these cases the body often consists of a page informing the
     * user of the error code.
     * <p>
     * <b>Beware that this method will return a copy of the array. If memory
     * consumption is a factory, please use the {@link #getResponseBodyNoCopy()}
     * instead.</b>
     * 
     * @return The body as a bytearray or null if no response was found
     */
    public byte[] getResponseBody();
    
    
    /**
     * Returns the body of the response (if any)
     * <p>
     * Do note that a body may exist even if the response code is other than
     * 200 OK. In these cases the body often consists of a page informing the
     * user of the error code.
     * <p>
     * <b>Beware that this method will not return a copy of the array. If the 
     * array needs to be modified, it may be prudent to use the
     * {@link #getResponseBody()} method instead if more than one system needs
     * access to this object.</b>
     * 
     * @return The body as a bytearray or null if no response was found
     */
    public byte[] getResponseBodyNoCopy();
    

    /**
     * Returns the HTTP response headers. This method will never return null,
     * however the object may be empty if no headers were sent
     * 
     * @return The response headers as a {@link TunnelHeaders} object
     */
    public TunnelHeaders getResponseHeaders();

}
