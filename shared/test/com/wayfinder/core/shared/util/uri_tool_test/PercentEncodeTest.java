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

package com.wayfinder.core.shared.util.uri_tool_test;

import com.wayfinder.core.shared.util.URITool;

import junit.framework.TestCase;

public class PercentEncodeTest extends TestCase {

    public final String UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                     "abcdefghijklmopqrstuvwxyz" +
                                     "0123456789" + "-._~";
    private StringBuffer m_sb;
    
    protected void setUp() throws Exception {
        super.setUp();
        m_sb = new StringBuffer(20);
    }

    public void testIsUriUnreservedYes() {
        for(int i=0; i < UNRESERVED.length(); i++) {
            assertTrue(URITool.isUriUnreserved(UNRESERVED.charAt(i)));
        }
    }
    
    public void testIsUriUnreservedNo() {
        final String SOMECHARS = "!\"@#$%&/{([)]=}+?\\´`*<>|;:éï";

        for(int i=0; i < SOMECHARS.length(); i++) {
            assertFalse(URITool.isUriUnreserved(SOMECHARS.charAt(i)));
        }
    }
    
    public void testPercentEncodeByte() {
        URITool.percentEncode((byte) ' ', m_sb);
        assertEquals("%20",  m_sb.toString());
    }

    public void testPercentEncodeBytes() {
        URITool.percentEncode((byte) 'W', m_sb);
        URITool.percentEncode((byte) 'A', m_sb);
        URITool.percentEncode((byte) 'Y', m_sb);
        URITool.percentEncode((byte) '\u00FF', m_sb);
        
        assertEquals("%57%41%59%FF",  m_sb.toString());
    }

    /**
     * test cases from RFC3986, section 2.5 
     */
    public void testPercentEncodeStringRFC3986_Laguna_Beach() {
        final String RESULT = "Laguna%20Beach"; 
        int enclen = URITool.percentEncodeString("Laguna Beach", m_sb);
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT.length(), enclen);
        
    }

    public void testPercentEncodeStringRFC3986_A() {
        final String RESULT = "A"; 
        int enclen = URITool.percentEncodeString("A", m_sb);
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT.length(), enclen);

    }

    public void testPercentEncodeStringRFC3986_LATIN_CAPITAL_LETTER_A_WITH_GRAVE() {
        // LATIN CAPITAL LETTER A WITH GRAVE
        final String RESULT = "%C3%80"; 
        int enclen = URITool.percentEncodeString("\u00C0", m_sb);
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT.length(), enclen);

    }

    public void testPercentEncodeStringRFC3986_KATAKANA_LETTER_A() {
        // KATAKANA LETTER A
        final String RESULT = "%E3%82%A2"; 
        int enclen = URITool.percentEncodeString("\u30A2", m_sb);
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT, m_sb.toString());
        assertEquals(RESULT.length(), enclen);

    }
    
    public void testPercentEncodeStringNull() {
        int enclen = URITool.percentEncodeString(null, m_sb);
        assertEquals(0, enclen);
        assertEquals(0, m_sb.length());
    }
}
