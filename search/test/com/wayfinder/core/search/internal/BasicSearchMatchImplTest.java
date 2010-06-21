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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.core.shared.xml.XmlWriter;

import junit.framework.TestCase;

/**
 * Tests the {@link BasicSearchMatchImpl} class except the distance
 * functions.
 */
public class BasicSearchMatchImplTest extends TestCase {

    protected static final String MATCH_ID = "ID1";
    protected static final Position MATCH_POSITION = new Position(123, 123);

    /**
     * A human readable description of the location of the match. Not an exact
     * position but rather the name of the city area or similar.
     */
    protected static final String MATCH_LOCATION = "Klostergården";
    protected static final String MATCH_NAME = "Konsum";

    /**
     * Will be created by setUp().
     */
    protected BasicSearchMatchImpl m_match;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_match = new BasicSearchMatchImpl(MATCH_ID,
                                           MATCH_LOCATION,
                                           MATCH_NAME,
                                           MATCH_POSITION);
    }


    /**
     * Test method for {@link com.wayfinder.core.search.internal.BasicSearchMatchImpl#getMatchID()}.
     */
    public final void testGetMatchID() {
        assertEquals(MATCH_ID, m_match.getMatchID());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.BasicSearchMatchImpl#getMatchLocation()}.
     */
    public final void testGetMatchLocation() {
        assertEquals(MATCH_LOCATION, m_match.getMatchLocation());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.BasicSearchMatchImpl#getMatchName()}.
     */
    public final void testGetMatchName() {
        assertEquals(MATCH_NAME, m_match.getMatchName());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.BasicSearchMatchImpl#getPosition()}.
     */
    public final void testGetPosition() {
        assertEquals(MATCH_POSITION, m_match.getPosition());
    }

    /**
     * Tests that the constructor does not allow null matchID. 
     */
    public final void testNullCtorParam1() {
        try {
            BasicSearchMatchImpl b =
                new BasicSearchMatchImpl(null,
                                         MATCH_LOCATION,
                                         MATCH_NAME,
                                         MATCH_POSITION);
            fail("BasicSearchMatchImpl ctor must not allow null matchID");
                                            
        } catch (IllegalArgumentException e) {
        }
    }
    
    /**
     * Tests that the constructor does not allow null matchLocation. 
     */
    public final void testNullCtorParam2() {
        try {
            BasicSearchMatchImpl b =
                new BasicSearchMatchImpl(MATCH_ID,
                                         null,
                                         MATCH_NAME,
                                         MATCH_POSITION);
            fail("BasicSearchMatchImpl ctor must not allow null matchLocation");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null matchName. 
     */
    public final void testNullCtorParam3() {
        try {
            BasicSearchMatchImpl b =
                new BasicSearchMatchImpl(MATCH_ID,
                                         MATCH_LOCATION,
                                         null,
                                         MATCH_POSITION);
            fail("BasicSearchMatchImpl ctor must not allow null matchName");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null position. 
     */
    public final void testNullCtorParam4() {
        try {
            BasicSearchMatchImpl b =
                new BasicSearchMatchImpl(MATCH_ID,
                                         MATCH_LOCATION,
                                         MATCH_NAME,
                                         null);
            fail("BasicSearchMatchImpl ctor must not allow null position");
                                            
        } catch (IllegalArgumentException e) {
        }
    }
}
