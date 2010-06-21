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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.shared.internal;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class UTF8CStringBufferParserTest extends TestCase {
    private byte[] m_buf;

    private static final String TEST_STRING_0 = "";
    private static final String TEST_STRING_1 = "Wayfinder";
    private static final String TEST_STRING_2 = "Wayfinder\u2122";
    private static final String TEST_STRING_3 = "foo";

    private UTF8CStringBufferParser m_csparser;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        baos.write(0);
        baos.write(TEST_STRING_1.getBytes("UTF-8"));
        baos.write(0);
        baos.write(TEST_STRING_2.getBytes("UTF-8"));
        baos.write(0);
        baos.write(TEST_STRING_3.getBytes("UTF-8"));
        // no ending terminator
        
        m_buf = baos.toByteArray();
        m_csparser = new UTF8CStringBufferParser(m_buf);
    }
    
    public void testEmpty() {
        // according to documentation, the behaviour is undefined in this case.
        byte[] buf = new byte[]{};
        UTF8CStringBufferParser csparser = new UTF8CStringBufferParser(buf);
        assertEquals("", csparser.getNextString());
        // assertEquals(0, csparser.getLastNbrBytes());
    }
    
    public void testEmptyString() {
        byte[] buf = new byte[]{0};
        UTF8CStringBufferParser csparser = new UTF8CStringBufferParser(buf);
        assertEquals("", csparser.getNextString());
        assertEquals(1, csparser.getLastNbrBytes());
        assertEquals(1, csparser.getNextOffset());
    }

    public void test3Strings() throws UnsupportedEncodingException {
        int expected_offset = 0;
        assertEquals(0, m_csparser.getNextOffset());
        assertEquals(TEST_STRING_0, m_csparser.getNextString());
        expected_offset += 1;
        assertEquals(expected_offset, m_csparser.getNextOffset());
        assertEquals(1, m_csparser.getLastNbrBytes());

        assertEquals(TEST_STRING_1, m_csparser.getNextString());
        byte[] buf =  TEST_STRING_1.getBytes("UTF-8");
        expected_offset += buf.length + 1; 
        assertEquals(expected_offset, m_csparser.getNextOffset());
        assertEquals(buf.length + 1, m_csparser.getLastNbrBytes());

        assertEquals(TEST_STRING_2, m_csparser.getNextString());
        buf =  TEST_STRING_2.getBytes("UTF-8");
        expected_offset += buf.length + 1; 
        assertEquals(expected_offset, m_csparser.getNextOffset());
        assertEquals(buf.length + 1, m_csparser.getLastNbrBytes());

        assertEquals(TEST_STRING_3, m_csparser.getNextString());
        buf =  TEST_STRING_3.getBytes("UTF-8");
        expected_offset += buf.length + 1; 
        assertEquals(expected_offset, m_csparser.getNextOffset());

        // no trailing \0, but getLastNbrBytes() errs in this case...
        assertEquals(buf.length + 1, m_csparser.getLastNbrBytes());
    }
}
