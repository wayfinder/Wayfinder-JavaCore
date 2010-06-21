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

import java.io.IOException;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.CharArray;
import com.wayfinder.core.shared.util.WFBase64;
import com.wayfinder.core.wfserver.tunnel.TunnelResponse;
import com.wayfinder.core.wfserver.tunnel.TunnelResponseListener;

final class TunnelMC2Request implements MC2Request {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TunnelMC2Request.class);
    
    private static final boolean USE_BASE64 = true;
    private static final boolean READ_HEADERS = true;

    private final RequestID m_id;
    private final TunnelRequestImpl m_query;
    private final TunnelResponseListener m_listener;
    private final CallbackHandler m_callhandler;
    
    TunnelMC2Request(RequestID id, TunnelRequestImpl query, CallbackHandler handler, 
                     TunnelResponseListener listener) {
        m_id = id;
        m_query = query;
        m_listener = listener;
        m_callhandler = handler;
    }


    public String getRequestElementName() {
        return MC2Strings.ttunnel_request;
    }
    

    public void write(MC2Writer mc2w) throws IOException {
        
        if(LOG.isDebug()) {
            LOG.debug("TunnelMC2Request.write()", 
                    "writing tunnel request to URL: " + m_query.getUrl());
        }
        
        mc2w.attribute(MC2Strings.aurl, m_query.getUrl());
        if(m_query.getType() == TunnelRequestImpl.TYPE_HTTP_POST) {
            byte[] postData = m_query.getBody();
            if(LOG.isTrace()) {
                LOG.trace("TunnelMC2Request.write()", 
                        "found POST data with length: " + postData.length);
            }
            mc2w.startElement(MC2Strings.tpost_data);
            final String transferEncoding;
            final String postText;
            if(USE_BASE64) {
                if(LOG.isTrace()) {
                    LOG.trace("TunnelMC2Request.write()", 
                            "encoding POST data with base64");
                }
                transferEncoding = "base64";
                postText = WFBase64.encode(postData);
            } else {
                if(LOG.isTrace()) {
                    LOG.trace("TunnelMC2Request.write()", 
                            "POST data will not be encoded");
                }
                transferEncoding = "identity";
                postText = new String(postData, "UTF-8");
            }
            mc2w.attribute(MC2Strings.ate, transferEncoding);
            mc2w.text(postText);
            mc2w.endElement(MC2Strings.tpost_data);
        }
    }
    
    
    
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        if(LOG.isDebug()) {
            LOG.debug("TunnelMC2Request.parse()", 
                    "Parsing tunnel response");
        }
        
        final String status_line = mc2p.attribute(MC2Strings.astatus_line);
        
        if(LOG.isTrace()) {
            LOG.trace("TunnelMC2Request.parse()", "found status line: " + status_line);
        }
        
        final TunnelResponse response;
        if (!mc2p.children()) {
            // most likely the tunnel request resulted in the end destination
            // returning a non-OK HTTP code
            if(LOG.isTrace()) {
                LOG.trace("TunnelMC2Request.parse()", 
                        "found no further children, returning");
            }
            response = new TunnelResponseImpl(status_line);
        } else {
            
            //check and report status code 
            ServerError status = mc2p.getErrorIfExists();
            if (status != null) {
                error(status);
                return;
            }

            // read the response headers sent by the webserver at the far
            // end of the tunnel
            final TunnelHeadersImpl headers = new TunnelHeadersImpl();
            while (mc2p.name() == MC2Strings.theader) {
                if(READ_HEADERS) {
                    // The internal browser content manager in the BlackBerry must 
                    // have correct headers to work as intended.
                    String key = mc2p.attribute(MC2Strings.afield);
                    String value = mc2p.attribute(MC2Strings.avalue);
                    
                    if(LOG.isTrace()) {
                        LOG.trace("TunnelMC2Request.parse()", 
                                "read header    " + key + ": " + value);
                    }
                    headers.addHeader(key, value);
                } else {
                    
                    if(LOG.isTrace()) {
                        LOG.trace("TunnelMC2Request.parse()", "skipping header");
                    }
                }
                mc2p.advance();
            }
            
            // read the actual body (if any sent)
            final byte[] responseBody;
            final int transferEncodingInt;
            if (mc2p.name() == MC2Strings.tbody) {
                String transferEncoding  = mc2p.attribute(MC2Strings.ate);
                if(LOG.isTrace()) {
                    LOG.trace("TunnelMC2Request.parse()", 
                            "found body with transfer encoding " + transferEncoding);
                }
                if("base64".equals(transferEncoding)) {
                    // the body has been transfer encoded with base64 so we
                    // need to decode it before passing it on. 
                    if(USE_BASE64) {
                        // decode the data and pass it on
                        if(LOG.isTrace()) {
                            LOG.trace("TunnelMC2Request.parse()", 
                                    "decoding body from base64 ...");
                        }
                        CharArray bodyCharArray = mc2p.valueCharArray();
                        responseBody = WFBase64.decode(bodyCharArray.getInternalBuffer(), 
                                bodyCharArray.getStart(), bodyCharArray.length());
                        transferEncodingInt = TunnelResponse.BODY_ENCODING_NONE;
                        //TODO: decode the content and stream it directly from xml
                        //but in order to be efficient we need the length of the decoded
                        //data
                    } else {
                        // don't decode the data, just pass it on as is but set
                        // the flag in the response to "base64 encoded body"
                        if(LOG.isTrace()) {
                            LOG.trace("TunnelMC2Request.parse()", 
                                    "will NOT decode from base64");
                        }
                        responseBody = mc2p.value().getBytes();
                        transferEncodingInt = TunnelResponse.BODY_ENCODING_BASE64;
                    }
                } else {
                    // body has not been encoded
                    if(LOG.isTrace()) {
                        LOG.trace("TunnelMC2Request.parse()", 
                                "body is not encoded");
                    }
                    responseBody = mc2p.value().getBytes();
                    transferEncodingInt = TunnelResponse.BODY_ENCODING_NONE;
                }
            } else {
                // no body in response
                if(LOG.isTrace()) {
                    LOG.trace("TunnelMC2Request.parse()",
                              "no body included in reply");
                }
                responseBody = null;
                transferEncodingInt = TunnelResponse.BODY_ENCODING_NONE;
            }
            response = new TunnelResponseImpl(status_line, headers, responseBody, transferEncodingInt);
        }
        
        // response received. Ping the client
        m_callhandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.tunnelResponse(m_id, response);
            }
        });
    }
    
    
    
    public void error(final CoreError coreError) {
        m_callhandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(m_id, coreError);
            }
        });
    }
}
