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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.PostContent;
import com.wayfinder.core.network.internal.ResponseCallback;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;

abstract class MC2Document implements PostContent, ResponseCallback {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(MC2Document.class);

    private static int CURRENT_TRANSACTION_ID = 0;

    protected static synchronized int getNextTransactionID() {
        return CURRENT_TRANSACTION_ID++;
    }
    
    private final ModuleData m_modData;
    private final SharedSystems m_systems;
    private final MC2Storage m_storage;
    private final InternalUserDataInterface m_usrDatIfc;

    MC2Document(SharedSystems systems, ModuleData modData, MC2Storage store, 
            InternalUserDataInterface usrDatIfc) {
        m_systems = systems;
        m_modData = modData;
        m_storage = store;
        m_usrDatIfc = usrDatIfc;
    }

    //-------------------------------------------------------------------------
    // Outgoing data

    public long getContentLength() {
        return -1;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if(LOG.isTrace()) {
            // writes the outbound request to the log
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeToInternal(out);
            byte[] data = out.toByteArray();
            LOG.trace("MC2Document.writeTo()", new String(data));
            outstream.write(data);
        } else {
            writeToInternal(outstream);
        }
    }
    
    
    private void writeToInternal(OutputStream outstream) throws IOException {
        MC2WriterImpl mc2Writer = new MC2WriterImpl(outstream, "UTF-8");
        // write isab document start
        mc2Writer.writeDocumentStart(m_systems, m_modData, m_storage.getMC2IsabAttributes(), 
                                     m_usrDatIfc, false); 
        // write requests
        writeRequests(mc2Writer);
        // write end
        mc2Writer.writeDocumentEnd();
    }


    abstract void writeRequests(MC2WriterImpl mc2Writer) throws IOException;

    //-------------------------------------------------------------------------
    // Incoming response

    public void readResponse(InputStream in, long length) throws IOException {
        if(LOG.isTrace()) {
            byte[] data = new byte[1024];
            int off = 0;
            int len = 0;
            int n;
            while ( (n = in.read(data,off,data.length - off)) >= 0) {
                len += n;
                off += n;
                if (len == data.length) {
                    byte[] tmp = data;
                    data = new byte[data.length*4/3+ 4];
                    System.arraycopy(tmp, 0, data, 0, len);
                }
            } 
            LOG.trace("MC2Document.readResponse()", new String(data,0,len));
            in = new ByteArrayInputStream(data,0,len);
        }
        
        if(LOG.isDebug()) {
            LOG.debug("MC2Document.readResponse()", "Reading XML response");
        }
        
        MC2ParserImpl mc2parser = new MC2ParserImpl(in, null);
        try {
            ServerError error = mc2parser.parseDocumentStart(m_storage);
            if(error != null) {
                // all requests where completely void
                error(error);
                return;
            }
        } catch (MC2ParserException e1) {
            error(new UnexpectedError("Could not parse document start", e1));
            throw new IOException("Could not parse document start");
        }
        
        try {
            // parse all that we can find
            parseRequests(mc2parser);
        } catch (MC2ParserException ex) {
            if(LOG.isError()) {
                LOG.error("MC2Document.readResponse()", ex);
            }
            error(new UnexpectedError("Caught exception while parsing document", ex));
        }
    }


    abstract void parseRequests(MC2ParserImpl mc2parser) throws IllegalStateException, MC2ParserException, IOException;
}
