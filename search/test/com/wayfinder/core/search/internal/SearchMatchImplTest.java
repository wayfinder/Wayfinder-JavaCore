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
package com.wayfinder.core.search.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.search.internal.SearchMatchImpl;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.core.shared.xml.XmlWriter;

import junit.framework.TestCase;

/**
 * Tests the {@link SearchMatchImpl} class.
 */
public class SearchMatchImplTest extends TestCase {
    
    private static final String MATCH_ID = "ID1";
    private static final Position MATCH_POSITION = new Position(123, 123);

    /**
     * A human readable description of the location of the match. Not an exact
     * position but rather the name of the city area or similar.
     */
    private static final String MATCH_LOC = "Klostergården";
    private static final String MATCH_NAME = "Konsum";
    private static final String MATCH_IMAGE = "btat_greatlooking";
    private static final Provider PROVIDER =
        new Provider(1, 2, "Test provider", "Test type", 100, "TestImage");


    private SearchMatchImpl m_smi;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_smi = new SearchMatchImpl(MATCH_ID,
                                    MATCH_NAME,
                                    MATCH_IMAGE,
                                    MATCH_LOC,
                                    MATCH_POSITION,
                                    PROVIDER);
        
    }


    // ---------------------------------------------------------------------
    // test the SearchReply.SearchMatch interface parts.
    
    public void testSearchProvider() {
        assertSame(PROVIDER, m_smi.getSearchProvider());
    }

    public void testMatchImageName() {
        assertEquals(MATCH_IMAGE, m_smi.getMatchImageName());
    }
    

    private static final String MATCH_XML =
        "<search_item search_item_type=\"street\">"
        + "<name></name>" // our writer doesn't use the empty element
                          // short hand <name />
        + "<itemid>" + MATCH_ID + "</itemid>"
        // position is optional and the server only cares about the ID
        // when data comes from us
        // + "<lat>" + MATCH_POSITION.getMc2Latitude() + "</lat>"
        // + "<lon>" + MATCH_POSITION.getMc2Longitude() + "</lon>"
        + "</search_item>";


    /**
     * <p>Test method for {@link com.wayfinder.core.search.internal.SearchMatchImpl#write(com.wayfinder.core.network.internal.mc2.MC2Writer)}.</p>
     * 
     * <p>This serves as a regression test, not a XML validation tests
     * because we test that a specific stream of characters are output - we
     * don't validate against the DTD.</p>
     */
    public final void testWrite()
        throws UnsupportedEncodingException, IOException {

        // A mock implementation of the MC2Writer system would make
        // this testing less tedious.
        
        WFByteArrayOutputStream out = new WFByteArrayOutputStream(1000);

        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        m_smi.write(mc2Writer);
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        String xml = out.bufToString("UTF-8");
        assertEquals(MATCH_XML , xml);
    }
}
