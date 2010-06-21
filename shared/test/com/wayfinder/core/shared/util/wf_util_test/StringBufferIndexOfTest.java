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

package com.wayfinder.core.shared.util.wf_util_test;

import junit.framework.TestCase;

import com.wayfinder.core.shared.util.WFUtil;

public class StringBufferIndexOfTest extends TestCase {

    private StringBuffer m_sBuf;
    private static final String TEST_STRING = "foobar";
    
    protected void setUp() throws Exception {
        super.setUp();
        m_sBuf = new StringBuffer(TEST_STRING);
    }
    
    public void testEmptySB() {
        assertEquals(-1,
            WFUtil.stringBufferIndexOf(new StringBuffer(), 'A', 0));
    }

    public void test1() {
        int index = WFUtil.stringBufferIndexOf(m_sBuf, 'b', 0);
        assertEquals(3, index);
    }
    
    public void testMultiOccurence() {
        int index = WFUtil.stringBufferIndexOf(m_sBuf, 'o', 0);
        assertEquals(1, index);
    }
    
    public void testNegativeIndex() {
        int index = WFUtil.stringBufferIndexOf(m_sBuf, 'b', -5);
        assertEquals(3, index);
    }
    
    public void testNotFound() {
        int index = WFUtil.stringBufferIndexOf(m_sBuf, 'B', 0);
        assertEquals(-1, index);
    }
    
    public void testOnlyExistsBefore() {
        int index = WFUtil.stringBufferIndexOf(m_sBuf, 'b', 4);
        assertEquals(-1, index);        
    }
}
