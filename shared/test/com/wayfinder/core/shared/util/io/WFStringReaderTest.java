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
 * Tests {@link WFStringReader}.
 */
public class WFStringReaderTest extends TestCase {

    private WFStringReader m_reader;
    private static final String TESTSTR = "I am a string. Isn't that nice?";
    private static final int N = TESTSTR.length();

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
        assertEquals(N, n);
        String s = new String(cbuf);
        assertEquals(TESTSTR, s);
    }

    /**
     * Test method for {@link java.io.Reader#ready()}.
     */
    public final void testReady() throws IOException {
        assertTrue(m_reader.ready());
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.io.WFStringReader#close()}.
     */
    public final void testClose() throws IOException {
        m_reader.close();
        try {
            m_reader.read();
            fail("read() does not throw exception after close().");
        } catch (IOException e) {
        }
    }

    /**
     * Test method for {@link java.io.Reader#read()}.
     */
    public final void testRead() throws IOException {
        assertEquals(TESTSTR.charAt(0), m_reader.read());
    }

    /**
     * Test method for {@link java.io.Reader#read(char[])}.
     */
    public final void testReadCharArray() throws IOException {
        char[] cbuf = new char[N];
        assertEquals(N, m_reader.read(cbuf));
        String s = new String(cbuf);
        assertEquals(TESTSTR, s);
    }

    /**
     * Test method for {@link java.io.Reader#skip(long)}.
     */
    public final void testSkip() throws IOException {
        // no actual guarantee that skip() will skip the requested amount
        // of chars but we assume that at least 5 always works.
        m_reader.skip(5);
        assertEquals(TESTSTR.charAt(5), m_reader.read());
    }

    public final void testSkipPastEnd() throws IOException {
        assertEquals(N, m_reader.skip(N + 1));
        assertEquals(-1, m_reader.read());
    }

    public final void testSkipZero() throws IOException {
        assertEquals(0, m_reader.skip(0));
        assertEquals(TESTSTR.charAt(0), m_reader.read());
    }

    public final void testSkipNeg() throws IOException {
        m_reader.skip(5);
        assertEquals(2, m_reader.skip(-2));
        assertEquals(TESTSTR.charAt(3), m_reader.read());
    }

    public final void testSkipNegPastEnd() throws IOException {
        assertEquals(0, m_reader.skip(-5));
        assertEquals(TESTSTR.charAt(0), m_reader.read());
    }
}
