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
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;

final class MC2DocumentMergeRequest extends MC2Document {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(MC2DocumentMergeRequest.class);

    public static final int MAX_NBR_OF_REQUESTS = 5; 
    
    private final MC2Request[] m_reqArray;
    private final int[] m_transActCorr;
    private int requestNbr; 
    private boolean started;

    MC2DocumentMergeRequest(SharedSystems systems, ModuleData modData, MC2Storage store, 
            InternalUserDataInterface usrDatIfc, MC2Request firstRequest) {
        super(systems, modData, store, usrDatIfc);
        m_reqArray = new MC2Request[MAX_NBR_OF_REQUESTS];
        m_transActCorr = new int[MAX_NBR_OF_REQUESTS];
        
        m_reqArray[0] = firstRequest;
        requestNbr = 1;
    }

    synchronized boolean offerRequest(MC2Request request) {
        if (!started && requestNbr < MAX_NBR_OF_REQUESTS) {
            m_reqArray[requestNbr++] = request;
            return true;
        } else {
            return false;
        }
    }
    synchronized void started() {
        started = true;
    }
    
    //-------------------------------------------------------------------------
    // Outgoing data

    void writeRequests(MC2WriterImpl mc2Writer) throws IOException {
        started();
        for (int i = 0; i < requestNbr; i++) {
            // for each document, grab a transaction id and write the request
            // remember the transaction id for later pairing between the requests
            // and the replys
            MC2Request req = m_reqArray[i];
            final int transID = getNextTransactionID();
            mc2Writer.write_request_start(req.getRequestElementName(), transID);
            m_reqArray[i].write(mc2Writer);
            m_transActCorr[i] = transID;
            
            //check and close request_* element 
            mc2Writer.endElement(req.getRequestElementName());
        }
    }

    //-------------------------------------------------------------------------
    // Incoming response
    void parseRequests(MC2ParserImpl mc2parser) 
                throws IllegalStateException, IOException, MC2ParserException {
        started();
        // parse all that we can find
        final int docDepth = mc2parser.getDepth();
        while (mc2parser.getDepth() > 0) {
            final int id = mc2parser.getNextTransactionID();
            if (id != Integer.MIN_VALUE) {
                // find corresponding request
                final int reqIndex = getRequestIndexForID(id);
                MC2Request req = m_reqArray[reqIndex];
                if (req != null) {
                    // there are reply elements which have attributes
                    // those will be lost if we advance in search for a status
                    try {
                        // each element has to try to parse his status
                        req.parse(mc2parser);
                    } catch (MC2ParserException ex) {
                        if (LOG.isError()) {
                            LOG.error("MC2Document.readResponse()", ex);
                        }
                        req.error(new UnexpectedError(
                                "Caught exception while parsing request", ex));
                    }
                }
                m_reqArray[reqIndex] = null;
            }
            mc2parser.advanceToDepth(docDepth);
        }
        failUtstandingRequests();
    }
    
    private void failUtstandingRequests() {
        for (int i = 0; i < requestNbr; i++) {
            if(m_reqArray[i] != null) {
                m_reqArray[i].error(new ServerError(ServerError.ERRSERV_REQUEST_TIMEOUT, "No reponse received", null));
                m_reqArray[i] = null;//don't report any other errors
            }
        }
    }
    
    private int getRequestIndexForID(int id) {
        for (int i = 0; i < requestNbr; i++) {
            if(id == m_transActCorr[i]) {
                return i;
            }
        }
        return -1;
    }

    public void error(CoreError error) {
        //kind of late but anyway if this happen before the write part 
        //dosen't matter which are the requests were all will fail 
        started();
        for (int i = 0; i < requestNbr; i++) {
            if(m_reqArray[i] != null) {
                m_reqArray[i].error(error);
                m_reqArray[i] = null;//don't report any other errors
            }
        }
    }
}
