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
package com.wayfinder.core.shared.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.wayfinder.core.shared.util.WFBase64;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class WFBase64InputStreamTest extends TestCase {

    /* using test vectors from RFC 4648 http://tools.ietf.org/html/rfc4648#section-10
     * 
     * BASE64("") = "" 
     * BASE64("f") = "Zg==" 
     * BASE64("fo") = "Zm8=" 
     * BASE64("foo") = "Zm9v" 
     * BASE64("foob") = "Zm9vYg==" 
     * BASE64("fooba") = "Zm9vYmE=" 
     * BASE64("foobar") = "Zm9vYmFy"
     */

    private ByteArrayInputStream m_bais;
    private WFBase64InputStream m_b64In;

    /**
     * @param name
     */
    public WFBase64InputStreamTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        byte[] inBuf = "Zm9vYmE".getBytes();
        m_bais = new ByteArrayInputStream(inBuf);
        m_b64In = new WFBase64InputStream(m_bais);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFillBuffer() throws IOException {
        m_b64In.fillBuffer();
        assertEquals(0, m_b64In.m_outBufOffset);
        assertEquals("fooba", new String(m_b64In.m_outBuf,0, m_b64In.m_outBufValid));
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.io.WFBase64InputStream#read(byte[], int, int)}.
     * @throws IOException 
     */
    public void testReadByteArrayIntInt() throws IOException {

        byte[] decodeTo = new byte[10];
        int decodedSize = m_b64In.read(decodeTo, 0, 0);
        assertEquals(0, decodedSize);

        decodedSize = m_b64In.read(decodeTo, 0, decodeTo.length);
        assertEquals(5, decodedSize);
        assertEquals("fooba", new String(decodeTo, 0, decodedSize));

        // attempt another read:
        decodedSize = m_b64In.read(decodeTo, 0, 4);
        assertEquals(-1, decodedSize);
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.io.WFBase64InputStream#read(byte[])}.
     * @throws IOException 
     */    
    public void testReadByteArray() throws IOException {
        byte[] decodeTo = new byte[10];
        int decodedSize = m_b64In.read(decodeTo);
        assertEquals(5, decodedSize);
        assertEquals("fooba", new String(decodeTo, 0, decodedSize));
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.io.WFBase64InputStream#read()}.
     * @throws IOException 
     */
    public void testReadByte() throws IOException {
        int c = m_b64In.read();
        assertEquals('f', c);
        c = m_b64In.read();
        assertEquals('o', c);
        c = m_b64In.read();
        assertEquals('o', c);
        c = m_b64In.read();
        assertEquals('b', c);
        c = m_b64In.read();
        assertEquals('a', c);
        c = m_b64In.read();
        assertEquals(-1, c);
    }

    public void testSkip() throws IOException {
        // a reasonable skip
        long skipped = m_b64In.skip(2);
        assertEquals(2, skipped);
        int c = m_b64In.read();
        assertEquals('o', c);

        //skip past the end
        skipped = m_b64In.skip(5);
        // should have skipped another 2 (we skipped 'f','o' and read the 
        // 2nd 'o')
        assertEquals(2, skipped);

        // attempt another skip:
        skipped = m_b64In.skip(1);
        assertEquals(0, skipped);
    }


    /**
     * Test method combining one byte read with byte array read
     * @throws IOException 
     */
    public void testCombinedRead() throws IOException {
        int c = m_b64In.read();
        assertEquals('f', c);
        byte[] decodeTo = new byte[10];
        int decodedSize = m_b64In.read(decodeTo, 0, decodeTo.length);
        assertEquals(4, decodedSize);
        assertEquals("ooba", new String(decodeTo, 0, decodedSize));
    }

    public void testReadWithDataInput() throws IOException {
        /*
         * should contain
         * the int 123456
         * the string "That was an int and this is some random text to test the 
         * wf base64 decoder input stream"
         */
        byte[] inBuf = ("AAHiQABXVGhhdCB3YXMgYW4gaW50IGFuZCB0aGlzIGlzIHNvbWUgcmFuZG9tIHRleHQgdG8gdGVzdCB0aGUgd2YgYmFzZTY0IGRlY29kZXIgaW5wdXQgc3RyZWFt").getBytes();
        m_bais = new ByteArrayInputStream(inBuf);
        m_b64In = new WFBase64InputStream(m_bais);
        DataInputStream din = new DataInputStream(m_b64In);
        int i = din.readInt();
        assertEquals(123456, i);
        String str = din.readUTF();
        assertEquals("That was an int and this is some random text to test the wf base64 decoder input stream", str);
    }

    public void testReadExceptions() throws IOException {
        try {
            m_b64In.read(null, 0, 0);
            fail("should have thrown an NPE");
        }
        catch (NullPointerException e) {
            // ok
        }

        byte[] tmp = new byte[8];
        try {
            m_b64In.read("Zm9vYmFy".getBytes(), -1, 8);
            fail("Should have failed, offset < 0");
        }
        catch (IndexOutOfBoundsException e) {
            //ok
        }

        try {    
            WFBase64.decode("Zm9vYmFy".getBytes(), 9, 8);
            fail("Should have failed, offset > array length");
        }
        catch (IndexOutOfBoundsException e) {
            //ok
        }

        try {
            WFBase64.decode("Zm9vYmFy".getBytes(),  -1, -1);
            fail("Should have failed, offset + len < 0 and len < 0");
        }
        catch (IndexOutOfBoundsException e) {
            //ok
        }

        try {
            WFBase64.decode("Zm9vYmFy".getBytes(), 5, 8);
            fail("Should have failed, offset + len > array legth");
        }
        catch (IndexOutOfBoundsException e) {
            //ok
        }
    }
}
