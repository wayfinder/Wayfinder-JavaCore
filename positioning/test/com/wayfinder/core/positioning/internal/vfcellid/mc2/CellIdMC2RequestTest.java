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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.positioning.internal.vfcellid.mc2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.impl.MC2ParserImpl;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.util.io.WFStringReader;
import com.wayfinder.pal.network.info.TGPPInfo;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class CellIdMC2RequestTest extends TestCase {
    
    private static final String MCC = "226";
    private static final String MNC = "10";
    private static final String LAC = "064a";
    private static final String CELLID = "000f576b";
    private static final int SIGNAL_DBM = -80;
    
    private static final String REQ_XML = 
        "<cell_id_request transaction_id=\"ID0\" position_system=\"MC2\">" +
        "<TGPP c_mcc=\"226\" c_mnc=\"10\" lac=\"064a\" cell_id=\"000f576b\" " +
        "network_type=\"UMTS\" signal_strength=\"-80\"/></cell_id_request>";
    
    private static final String REPLY_STATUS_XML = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?>" +
        "<!DOCTYPE isab-mc2><isab-mc2><cell_id_reply transaction_id=\"ID0\">" +
        "<status_code>-1</status_code>" +
        "<status_message>No cell id position found</status_message>" +
        "</cell_id_reply></isab-mc2>";
    
    private static final String REPLY_OK_XML = 
        "?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2><isab-mc2>\n" + 
        "   <cell_id_reply altitude=\"0\" end_angle=\"360\" inner_radius=\"0\" outer_radius=\"531\" start_angle=\"0\" transaction_id=\"ID0\">\n" +  
        "      <position_item position_system=\"MC2\">\n" +  
        "         <lat>558006267</lat>\n" +  
        "         <lon>281682173</lon>\n" + 
        "   </position_item></cell_id_reply>\n" + 
        "</isab-mc2>";
    
    private TGPPElement m_UMTS;
    
    private CellIdMC2Request m_req;
    
    private RequestID m_reqID;
    
    private CoreError m_error;
    
    /**
     * @param name
     */
    public CellIdMC2RequestTest(String name) {
        super(name);
        m_UMTS = new TGPPElement(MCC, MNC, LAC, CELLID, SIGNAL_DBM, TGPPInfo.TYPE_3GPP_UMTS);
        m_reqID = RequestID.getNewRequestID();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2Request#error(com.wayfinder.core.shared.error.CoreError)}.
     */
    public void testError() {
        m_req = new CellIdMC2Request(m_reqID, m_UMTS, new CellIdMC2ReplyListener() {
            
            public void error(RequestID requestID, CoreError error) {
                assertSame(m_reqID, requestID);
                assertSame(m_error, error);
            }
            
            public void cellIdReplyDone(RequestID reqID, Position pos, int radius) {
                fail("CellIdMC2RequestTest.testError().new CellIdMC2ReplyListener() {...}.cellIdReplyDone() should not be called");
            }
        });
        
        m_error = new CoreError("error");
        m_req.error(m_error);
    }

    /**
     * Test method for {@link com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2Request#getRequestElementName()}.
     */
    public void testGetRequestElementName() {
        m_req = new CellIdMC2Request(m_reqID, m_UMTS, null);
        assertEquals("cell_id_request", m_req.getRequestElementName());
    }

    /**
     * Test method for {@link com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)}.
     * @throws IOException 
     * @throws MC2ParserException 
     * @throws IllegalStateException 
     */
    public void testParseOk() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_OK_XML));
        
        m_req = new CellIdMC2Request(m_reqID, m_UMTS, new CellIdMC2ReplyListener() {
            
            public void error(RequestID requestID, CoreError error) {
                fail(error.getInternalMsg());
            }
            
            public void cellIdReplyDone(RequestID reqID, Position pos, int radius) {
                assertSame(m_reqID, reqID);
                assertEquals(558006267, pos.getMc2Latitude());
                assertEquals(281682173, pos.getMc2Longitude());
                assertEquals(531, radius);
            }
        });
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        m_req.parse(mc2parser);
    }
    
    public void testParseStatus() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_STATUS_XML));
        
        m_req = new CellIdMC2Request(m_reqID, m_UMTS, new CellIdMC2ReplyListener() {
            
            public void error(RequestID requestID, CoreError error) {
                assertSame(m_reqID, requestID);
                assertNotNull(error);
                assertEquals(CoreError.ERROR_SERVER, error.getErrorType());
                ServerError errSrv = (ServerError)error;
                assertEquals(-1, errSrv.getStatusCode());
                assertEquals("No cell id position found", errSrv.getInternalMsg());
            }
            
            public void cellIdReplyDone(RequestID reqID, Position pos, int radius) {
                fail("CellIdMC2RequestTest.testParseStatus().new CellIdMC2ReplyListener() {...}.cellIdReplyDone() should not be called");
            }
        });
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        m_req.parse(mc2parser);
    }

    /**
     * Test method for {@link com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)}.
     * @throws IOException 
     */
    public void testWrite() throws IOException {
        m_req = new CellIdMC2Request(m_reqID, m_UMTS, null);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        
        mc2Writer.startElement(m_req.getRequestElementName());
        mc2Writer.attribute(MC2Strings.atransaction_id, "ID0");
        m_req.write(mc2Writer);
        mc2Writer.endElement(m_req.getRequestElementName());
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.toString(); //
        assertEquals(REQ_XML , xml);
    }

}
