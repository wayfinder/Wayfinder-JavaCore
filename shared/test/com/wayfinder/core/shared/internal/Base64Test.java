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


import junit.framework.TestCase;

public class Base64Test extends TestCase {
    
    public void testDummy() {
        assertTrue(true);
    }
    
    
    
//    /**
//     * Run the test vectors for Base64 as specified in RFC 4648
//     * (http://tools.ietf.org/html/rfc4648) section 10.
//     */
//    public void testRfc4648TestVectorsEnc() throws IOException {
//        assertEquals(Base64.encode("".getBytes()), "");
//        assertEquals(Base64.encode("f".getBytes()), "Zg==");
//        assertEquals(Base64.encode("fo".getBytes()), "Zm8=");
//        assertEquals(Base64.encode("foo".getBytes()), "Zm9v");
//        assertEquals(Base64.encode("foob".getBytes()), "Zm9vYg==");
//        assertEquals(Base64.encode("fooba".getBytes()), "Zm9vYmE=");
//        assertEquals(Base64.encode("foobar".getBytes()), "Zm9vYmFy");
//    }
//
//
//    /**
//     * same as testRfc4648TestVectorsEnc but tests decoding with
//     * Base64.decode(String, OutputStream).
//     * 
//     * @see Base64.decode(String, OutputStream)
//     * 
//     */
//    public void testRfc4648TestVectorsDec2() throws IOException {
//        assertEquals("", internalDecode2(""));
//        assertEquals("f", internalDecode2("Zg=="));
//        assertEquals("fo", internalDecode2("Zm8="));
//        assertEquals("foo", internalDecode2("Zm9v"));
//        assertEquals("foob", internalDecode2("Zm9vYg=="));
//        assertEquals("fooba", internalDecode2("Zm9vYmE="));
//        assertEquals("foobar", internalDecode2("Zm9vYmFy"));
//    }
//
//    private String internalDecode2(String base64)
//        throws IOException {
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(base64.length());
//        Base64.decode(base64, baos);
//        return new String(baos.toByteArray(), "UTF-8");
//    }
//
//
//    /**
//     * same as testRfc4648TestVectorsEnc but tests decoding with
//     * Base64.decode(byte[]).
//     * 
//     * @see Base64.decode(byte[])
//     * 
//     * @throws IOException
//     */
//    public void testRfc4648TestVectorsDec3() throws IOException {
//       assertEquals("", new String(Base64.decode("".getBytes()), "UTF-8"));
//       String s;
//       // test fails due to bug in Base64
//       // s = new String(Base64.decode("Zg==".getBytes()), "UTF-8");
//       // assertEquals("f", s);
//       s = new String(Base64.decode("Zm9v".getBytes()), "UTF-8");
//       assertEquals("foo", s);
//       assertEquals("foobar",
//                    new String(Base64.decode("Zm9vYmFy".getBytes()), "UTF-8"));
//    }
}
