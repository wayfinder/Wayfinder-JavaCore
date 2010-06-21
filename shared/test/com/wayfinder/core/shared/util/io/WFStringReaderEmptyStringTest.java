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
package com.wayfinder.core.shared.util.io;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests {@link WFStringReader} with an empty string.
 */
public class WFStringReaderEmptyStringTest extends TestCase {

    private WFStringReader m_reader;
    private static final String TESTSTR = "";
    private static final int N = 0;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_reader = new WFStringReader(TESTSTR);
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.io.WFStringReader#read(char[], int, int)}.
     */
    public final void testReadCharArrayIntInt() throws IOException {
        char[] cbuf = new char[N];
        int n = m_reader.read(cbuf, 0, N);
        assertEquals(-1, n);
    }

    /**
     * <p>Test method for {@link com.wayfinder.core.shared.util.io.WFStringReader#ready()}.</p>
     * 
     * <p>Tests that ready() returns true since we can find out that we're
     * at EOF without blocking.</p>
     */
    public final void testReady() throws IOException {
        assertTrue(m_reader.ready());
    }

    /**
     * Test method for {@link java.io.Reader#read()}.
     */
    public final void testRead() throws IOException {
        assertEquals(-1, m_reader.read());
    }

    /**
     * Test method for {@link java.io.Reader#read(char[])}.
     */
    public final void testReadCharArray() throws IOException {
        char[] cbuf = new char[N];
        assertEquals(-1, m_reader.read(cbuf));
    }

    /**
     * Test method for {@link java.io.Reader#skip(long)}.
     */
    public final void testSkipPastEnd() throws IOException {
        assertEquals(0, m_reader.skip(5));
        assertEquals(-1, m_reader.read());
    }

    public final void testSkipZero() throws IOException {
        assertEquals(0, m_reader.skip(0));
        assertEquals(-1, m_reader.read());
    }

    public final void testSkipNegPastEnd() throws IOException {
        assertEquals(0, m_reader.skip(-5));
        assertEquals(-1, m_reader.read());
    }


    // ------------------------------------------------------------

    /**
     * Checks that the constructor does not allow null instead of an
     * empty string.
     */
    public void testCtorNull() {
        try {
            WFStringReader reader = new WFStringReader(null);
            fail("new WFStringReader(null) must not be allowed.");
        } catch (IllegalArgumentException e) {
        }
    }
}
