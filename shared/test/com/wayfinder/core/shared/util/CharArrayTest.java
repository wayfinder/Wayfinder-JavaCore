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

package com.wayfinder.core.shared.util;

import junit.framework.TestCase;

public class CharArrayTest extends TestCase {
    public void testCreate() {
        CharArray ca = new CharArray(15);
        assertEquals(0, ca.length());
    }
    
    public void testAppend() {
        final String test = "foobar";
        CharArray ca = new CharArray(15);
        ca.append(test);
        assertEquals(6, ca.length());
        assertTrue(ca.equals(test));
    }

    public void testAppendChar() {
        final String test = "foobar";
        CharArray ca = new CharArray(15);
        for(int i=0; i < test.length(); i++) {
            ca.append(test.charAt(i));
        }
        assertTrue(ca.equals(test));
    }

    public void testEmpty() {
        CharArray ca = new CharArray(5);
        ca.append("abcde");
        ca.empty();
        assertEquals("", ca.toString());
    }

    public void testsetBounds() {
        final String s1 = "foo";
        CharArray ca = new CharArray(10);
        ca.append("abc" + s1);
        ca.setBounds(3, 3+s1.length());
        assertEquals(s1, ca.toString());
    }

    public void testsubCharArray() {
        CharArray ca = new CharArray(5);
        ca.append("0123456789abcdef");
        ca.subCharArray(10);
        assertEquals("abcdef", ca.toString());
        ca.subCharArray(6);
        assertEquals("", ca.toString());
    }

    public void testsubCharArrayTooFar() {
        CharArray ca = new CharArray(5);
        final String s1 = "0123456789abcdef"; // 16 chars
        ca.append(s1);
        try {
            // compare with testsubCharArray()
            ca.subCharArray(s1.length() + 1);
            fail("testsubCharArrayTooFar() the expected exception was not throw");
        } catch (Exception e) {
            // works as intended
        }
    }

    
    public void testIndexIn() {
        final String test = "foobar";
        CharArray ca = new CharArray(15);
        ca.append(test);
        assertEquals(1, ca.indexIn(new String[]{"", test, ""}));
        assertEquals(-1, ca.indexIn(new String[]{"", "", ""}));
    }

    public void testIndexIn2() {
        CharArray ca = new CharArray(15);
        assertEquals(-1, ca.indexIn(new String[]{"a", "b"}));
    }

    public void testIntValue() {
        final int test = -574309;
        CharArray ca = new CharArray(15);
        ca.append(String.valueOf(test));
        assertEquals(test, ca.intValue());
    }
    
    public void testIntValueFromHex() {
        final int test = -574309;
        CharArray ca = new CharArray(15);
        ca.append(Integer.toHexString(test));
        assertEquals(test, ca.intValueFromHex());        
    }
    
    public void testInvalidInt() {
        CharArray ca = new CharArray(15);
        ca.append("A1234");
        try {
            int i = ca.intValue();
            fail("testInvalidInt(): the expected exception was not thrown.");
        } catch (NumberFormatException e) {
            // method works as intended
        }
    }

    public void testTrim1() {
        CharArray ca = new CharArray(10);
        ca.trim();
        assertEquals(0, ca.length());
        String s1 = "foo";
        ca.append("  " + s1);
        ca.trim();
        assertEquals(s1, ca.toString());
    }

    public void testTrim2() {
        CharArray ca = new CharArray(10);
        String s1 = "foo";
        ca.append(s1 + "  ");
        ca.trim();
        assertEquals(s1, ca.toString());
    }

    public void testTrim3() {
        CharArray ca = new CharArray(10);
        String s1 = "foo";
        ca.append("                 " + s1 + "  ");
        ca.trim();
        assertEquals(s1, ca.toString());
    }

    public void testStartsWith() {
        CharArray ca = new CharArray(15);
        ca.append("foobar");
        assertTrue(ca.startsWith("foo"));
        assertTrue(ca.startsWith("foobar"));
        assertFalse(ca.startsWith("foobarbaz"));
        assertFalse(ca.startsWith("bar"));
        assertFalse(ca.startsWith("f00bar"));
    }
}
