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
package com.wayfinder.core.network.internal.mc2.impl;

import java.io.IOException;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;

final class MC2DocumentSingleRequest extends MC2Document {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(MC2DocumentSingleRequest.class);
    
    private MC2Request m_req;
    private int m_transactionID;

    MC2DocumentSingleRequest(SharedSystems systems, ModuleData modData, MC2Storage store, 
            InternalUserDataInterface usrDatIfc, MC2Request request) {
        super(systems, modData, store, usrDatIfc);
        m_req = request;
    }

    //-------------------------------------------------------------------------
    // Outgoing data

    public long getContentLength() {
        return -1;
    }

    public String getContentType() {
        return "text/xml; charset=\"UTF-8\"";
    }

    void writeRequests(MC2WriterImpl mc2Writer) throws IOException {
        m_transactionID = getNextTransactionID();
        mc2Writer.write_request_start(m_req.getRequestElementName(), m_transactionID);
        m_req.write(mc2Writer);
        //close and check request_ element 
        mc2Writer.endElement(m_req.getRequestElementName());
    }

    //-------------------------------------------------------------------------
    // Incoming response

    void parseRequests(MC2ParserImpl mc2parser) throws IllegalStateException,
            MC2ParserException, IOException {
        // parse a single request if available
        try {
            int id = mc2parser.getNextTransactionID();
            if(id == m_transactionID) {
                //there are reply elements which have attributes
                //those will be lost if we advance in search for a status
            
                //each element has to try to parse his status
                m_req.parse(mc2parser);
            } else {
                m_req.error(new ServerError(ServerError.ERRSERV_REQUEST_TIMEOUT,
                        "No reponse received", null));
            }
        } catch(MC2ParserException ex) {
            if(LOG.isError()) {
                LOG.error("MC2DocumentSingleRequest.parseRequests()",ex);
            }
            m_req.error(new UnexpectedError(
                          "Caught exception while parsing request", ex));
        }
        m_req = null;
    }
    
    public void error(CoreError error) {
        if (m_req != null) {
            m_req.error(error);
            m_req = null;
        }
    }
}
