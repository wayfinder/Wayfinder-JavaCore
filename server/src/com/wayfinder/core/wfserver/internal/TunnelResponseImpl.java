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

import com.wayfinder.core.wfserver.tunnel.TunnelHeaders;
import com.wayfinder.core.wfserver.tunnel.TunnelResponse;


final class TunnelResponseImpl implements TunnelResponse {
    
    private final int m_responseCode;
    private final String m_responseMessage;
    private final TunnelHeaders m_httpHeaders;
    private final byte[] m_responseBody;
    private final int m_transferEncoding;
    
    
    /**
     * Constructor for use for responses with HTTP Code != 200
     * 
     * @param aResponseCode The response code
     * @param aResponseMessage The response message
     */
    TunnelResponseImpl(int aResponseCode, String aResponseMessage) {
        this(aResponseCode, aResponseMessage, new TunnelHeadersImpl(), null, BODY_ENCODING_NONE);
    }
    
    
    /**
     * Constructor for use with the status line sent in the XML tunnel requests
     * <p>
     * The status line will be parsed into response code and message
     * 
     * @param aStatusline The status line
     */
    TunnelResponseImpl(String aStatusline) {
        this(aStatusline, new TunnelHeadersImpl(), null, BODY_ENCODING_NONE);
    }
    

    /**
     * 
     * @param aResponseCode
     * @param aResponseMessage
     * @param aHttpHeaders
     * @param aResponseBody
     */
    TunnelResponseImpl(int aResponseCode, 
                       String aResponseMessage, 
                       TunnelHeaders aHttpHeaders, 
                       byte[] aResponseBody,
                       int transferEncoding) {
        m_responseCode = aResponseCode;
        m_responseMessage = aResponseMessage;
        m_httpHeaders = aHttpHeaders;
        m_responseBody = aResponseBody;
        m_transferEncoding = transferEncoding;
    }
    
    
    /**
     * 
     * @param aStatusline
     * @param aHttpHeaders
     * @param aResponseBody
     */
    TunnelResponseImpl(String aStatusline, 
                       TunnelHeaders aHttpHeaders, 
                       byte[] aResponseBody,
                       int transferEncoding) {
        m_httpHeaders = aHttpHeaders;
        m_responseBody = aResponseBody;
        m_transferEncoding = transferEncoding;
        
        if (aStatusline == null || aStatusline.length() == 0) {
            throw new IllegalArgumentException("response empty");
        }

        final int httpEnd = aStatusline.indexOf(' ');
        if (httpEnd < 0) {
            throw new IllegalArgumentException("cannot find status code in response: " + aStatusline);
        }
    
        if (aStatusline.length() <= httpEnd) {
            throw new IllegalArgumentException("status line ends after HTTP version");
        }
    
        int codeEnd = aStatusline.substring(httpEnd + 1).indexOf(' ');
        if (codeEnd < 0) {
            throw new IllegalArgumentException("cannot find reason phrase in response");
        }

        codeEnd += (httpEnd + 1);
        if (aStatusline.length() <= codeEnd) {
            throw new IllegalArgumentException("status line end after status code");
        }
    
        try {
            m_responseCode = Integer.parseInt(aStatusline.substring(httpEnd+1, 
                                                               codeEnd));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("status code in response is not a number");
        }
    
        m_responseMessage = aStatusline.substring(codeEnd + 1);
    }

    
    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.TunnelResponse#responseWasOK()
     */
    public boolean responseWasOK() {
        // HTTP OK!
        return m_responseCode == 200; 
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.TunnelResponse#getResponseCode()
     */
    public int getResponseCode() {
        return m_responseCode;
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.TunnelResponse#getResponseMessage()
     */
    public String getResponseMessage() {
        return m_responseMessage;
    }

    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelResponse#getBodyTransferEncoding()
     */
    public int getBodyTransferEncoding() {
        return m_transferEncoding;
    }
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.TunnelResponse#getResponseBody()
     */
    public byte[] getResponseBody() {
        if(m_responseBody != null) {
            byte[] newArray = new byte[m_responseBody.length];
            System.arraycopy(m_responseBody, 0, newArray, 0, m_responseBody.length);
            return newArray;
        }
        return m_responseBody; // which is null...
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelResponse#getResponseBodyNoCopy()
     */
    public byte[] getResponseBodyNoCopy() {
        return m_responseBody;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.TunnelResponse#getResponseHeaders()
     */
    public TunnelHeaders getResponseHeaders() {
        return m_httpHeaders;
    }
}
