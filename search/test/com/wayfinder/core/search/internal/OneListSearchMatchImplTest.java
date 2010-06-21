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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.search.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;

import junit.framework.TestCase;

/**
 * Tests {@link OneListSearchMatchImpl}.
 */
public class OneListSearchMatchImplTest extends TestCase {

    protected static final String BRAND_IMAGE_NAME = "brand";
    protected static final String CATEGORY_IMAGE_NAME = "category";
    protected static final String PROVIDER_IMAGE_NAME = "provider";

    private OneListSearchMatchImpl m_match;


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_match = new OneListSearchMatchImpl(
                BasicSearchMatchImplTest.MATCH_ID,
                BasicSearchMatchImplTest.MATCH_LOCATION,
                BasicSearchMatchImplTest.MATCH_NAME,
                BasicSearchMatchImplTest.MATCH_POSITION,
                BRAND_IMAGE_NAME,
                CATEGORY_IMAGE_NAME,
                PROVIDER_IMAGE_NAME,
                null,
                true);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMatchImpl#getMatchBrandImageName()}.
     */
    public final void testGetMatchBrandImageName() {
        assertEquals(BRAND_IMAGE_NAME, m_match.getMatchBrandImageName());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMatchImpl#getMatchCategoryImageName()}.
     */
    public final void testGetMatchCategoryImageName() {
        assertEquals(CATEGORY_IMAGE_NAME, m_match.getMatchCategoryImageName());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMatchImpl#getMatchProviderImageName()}.
     */
    public final void testGetMatchProviderImageName() {
        assertEquals(PROVIDER_IMAGE_NAME, m_match.getMatchProviderImageName());
    }
    
    public final void testDetailsLevel() {
        //this should actually be tested together with 
        //OneListSearchMC2RequestTest and MatchDetailsMC2RequestTest
        assertTrue(m_match.additionalInfoExists());
        m_match.setFullInfo(new PoiDetailImpl());
        assertFalse(m_match.additionalInfoExists());
    }
    

    private static final String MATCH_XML =
        "<search_match search_match_type=\"street\">"
        + "<name>"+BasicSearchMatchImplTest.MATCH_NAME+"</name>" // our writer doesn't use the empty element
                          // short hand <name />
        + "<itemid>" + BasicSearchMatchImplTest.MATCH_ID + "</itemid>"
        + "<location_name>" + BasicSearchMatchImplTest.MATCH_LOCATION + "</location_name>"
        // position is optional and the server only cares about the ID
        // when data comes from us
        // + "<lat>" + MATCH_POSITION.getMc2Latitude() + "</lat>"
        // + "<lon>" + MATCH_POSITION.getMc2Longitude() + "</lon>"
        + "</search_match>";


    /**
     * <p>Test method for {@link OneListSearchMatchImpl#write(com.wayfinder.core.network.internal.mc2.MC2Writer)}.</p>
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
        m_match.write(mc2Writer);
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        String xml = out.bufToString("UTF-8");
        assertEquals(MATCH_XML , xml);
    }
}
