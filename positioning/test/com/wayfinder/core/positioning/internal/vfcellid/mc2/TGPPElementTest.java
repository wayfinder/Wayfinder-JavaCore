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

import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.shared.xml.XmlWriter;
import com.wayfinder.pal.network.info.TGPPInfo;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class TGPPElementTest extends TestCase {
    
    private static final String MCC = "226";
    private static final String MNC = "10";
    private static final String LAC = "064a";
    private static final String CELLID = "000f576b";
    private static final int SIGNAL_DBM = -80;
    
    private static final String REQ_XML_UMTS = 
        "<TGPP c_mcc=\"226\" c_mnc=\"10\" lac=\"064a\" cell_id=\"000f576b\" " +
        "network_type=\"UMTS\" signal_strength=\"-80\"/>";
    private static final String REQ_XML_GPRS = 
        "<TGPP c_mcc=\"226\" c_mnc=\"10\" lac=\"064a\" cell_id=\"000f576b\" " +
        "network_type=\"GPRS\" signal_strength=\"-80\"/>";
    
    private TGPPElement m_UMTS;
    private TGPPElement m_GPRS;

    /**
     * @param name
     */
    public TGPPElementTest(String name) {
        super(name);
        m_UMTS = new TGPPElement(MCC, MNC, LAC, CELLID, SIGNAL_DBM, TGPPInfo.TYPE_3GPP_UMTS);
        m_GPRS = new TGPPElement(MCC, MNC, LAC, CELLID, SIGNAL_DBM, TGPPInfo.TYPE_3GPP_GPRS);
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
    
    public void testWriteUMTS() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        m_UMTS.write(mc2Writer);
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.toString(); //
        assertEquals(REQ_XML_UMTS , xml);
    }
    
    public void testWriteGPRS() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        m_GPRS.write(mc2Writer);
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.toString(); //
        assertEquals(REQ_XML_GPRS , xml);

    }
    
    public void testToString() {
        assertNotNull(m_UMTS.toString());
    }
}
