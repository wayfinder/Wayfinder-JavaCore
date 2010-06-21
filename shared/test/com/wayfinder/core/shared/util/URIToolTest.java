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

import java.util.Hashtable;

import junit.framework.TestCase;

public class URIToolTest extends TestCase {
    private Hashtable m_hash;

    protected void setUp() throws Exception {
        super.setUp();
        m_hash = new Hashtable();
    }

    public void testEmpty() {
        URITool.parseHTTPQueryURL("", m_hash);
        assertEquals(0, m_hash.size());
    }

    public void testNoParams() {
        URITool.parseHTTPQueryURL("ftp://a:b@foo/bar", m_hash);
        assertEquals(0, m_hash.size());
    }
    
    public void testFragment() {
        do3UriParams("#some_fragment which contains # ?foo=99");
    }
    
    public void testNoFragment() {
        do3UriParams(null);
    }
    
    protected void do3UriParams(String suffix) {
        String uri = "http://x?foo=1&bar=2&quux=3";
        if (suffix != null) {
            uri = uri + suffix;
        }
        URITool.parseHTTPQueryURL(uri, m_hash);
        assertEquals(3, m_hash.size());
        String s = (String) m_hash.get("foo");
        assertEquals("1", s);
        s = (String) m_hash.get("bar");
        assertEquals("2", s);
        s = (String) m_hash.get("quux");
        assertEquals("3", s);
    }
    
    public void testEncodedParams() {
        String uri = "wf://startup?action=setuin&uin=1476659607&successuri=wf%3A%2F%2Fstartup%3Faction%3Dstartup_complete&failureuri=wf%3A%2F%2Fstartup%3Faction%3Dexit";
        URITool.parseHTTPQueryURL(uri, m_hash);
        assertEquals(4, m_hash.size());
        String s = (String) m_hash.get("action");
        assertEquals("setuin", s);
        s = (String) m_hash.get("uin");
        assertEquals("1476659607", s);
        s = (String) m_hash.get("successuri");
        assertEquals("wf://startup?action=startup_complete", s);
        s = (String) m_hash.get("failureuri");
        assertEquals("wf://startup?action=exit", s);        

    }
}
