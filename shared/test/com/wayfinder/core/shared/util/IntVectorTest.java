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

public class IntVectorTest extends TestCase {

    private static final int INITIAL_CAPACITY = 10;
    
    private IntVector m_iv;
    
    private static final int DATA_SIZE = 7;
    private final int[] m_elements = new int[]{0, 1, 2, 3, 4, 5, 6};
    
    protected void setUp() throws Exception {
        super.setUp();
        m_iv = new IntVector(INITIAL_CAPACITY);
    }

    public void testEmpty() {
        m_iv = new IntVector(INITIAL_CAPACITY);
        assertEquals(0, m_iv.size());
    }
    

    public void testAddInt() {
        m_iv.add(0);
        assertEquals(1, m_iv.size());
        assertTrue(m_iv.contains(0));
    }
    
    public void testAddArray() {
        m_iv.add(m_elements);
        assertEquals(m_elements.length, m_iv.size());
        for(int i=0; i < m_elements.length; i++) {
            assertEquals(m_elements[i], m_iv.get(i));
        }
    }
    
    public void testAddIntVector() {
        IntVector iv2 = new IntVector(INITIAL_CAPACITY);
        iv2.add(m_elements); // testAddArray makes sure this works
        assertTrue(m_iv.add(iv2));
        assertEquals(m_elements.length, m_iv.size());
        for(int i=0; i < m_elements.length; i++) {
            assertEquals(m_elements[i], m_iv.get(i));
        }     
    }

    public void testAddEmptyIntVector() {
        IntVector iv2 = new IntVector(INITIAL_CAPACITY);
        assertFalse(m_iv.add(iv2));
    }


    public void testGetInvalid() {
        try {
            int v = m_iv.get(1);
            fail("testGetInvalid(): expected exception not thrown");
        } catch (IndexOutOfBoundsException e) {
        }        
    }

    public void testGetRange() {
        final int INDEX = 3;
        final int RANGE_SIZE = DATA_SIZE - INDEX;
        
        m_iv.add(m_elements); // testAddArray makes sure this works
        int[] v = m_iv.get(INDEX, RANGE_SIZE);
        assertEquals(RANGE_SIZE, v.length);
        for(int i=0; i < RANGE_SIZE; i++) {
            assertEquals(m_elements[INDEX + i], v[i]);
        }
    }

    public void testGetRangeInvalid() {
        try {
            int[] v = m_iv.get(1, 1);
            fail("testGetRangeInvalid(): expected exception not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
    }


    public void testEnsureCapacity() {
        m_iv.add(m_elements);
        m_iv.ensureCapacity(5 * INITIAL_CAPACITY);
        assertEquals(m_elements.length, m_iv.size());
        assertTrue(m_iv.contains(m_elements[2]));
    }


    public void testClear() {
        m_iv.add(m_elements);
        m_iv.clear();
        assertEquals(0, m_iv.size());
        assertFalse(m_iv.contains(m_elements[2]));
    }


    public void testNotContains() {
        assertFalse(m_iv.contains(555555));
        m_iv.add(m_elements);
        assertFalse(m_iv.contains(555555));
    }

    public void testGetArray() {
        m_iv.add(m_elements);
        int[] v = m_iv.getArray();

        // this test is questionable since it locks implementation detail.
        assertEquals(INITIAL_CAPACITY, v.length);
        for(int i=0; i < DATA_SIZE; i++) {
            assertEquals(m_elements[i], v[i]);
        }        
    }

    public void testToStringEmpty() {
        String s = m_iv.toString();
        assertNotNull(s);
        assertFalse("".equals(s));
    }

    public void testToString() {
        m_iv.add(m_elements);
        String s = m_iv.toString();
        assertNotNull(s);
        assertFalse("".equals(s));
    }
    
    public void testRemove() {
        m_iv.add(m_elements);
        int last = m_elements.length - 1;
        assertEquals(m_elements[last],m_iv.remove(last));
        assertEquals(m_elements[0],m_iv.remove(0));
    }
    
    public void testRemoveLast() {
        m_iv.add(m_elements);
        int last = m_elements.length - 1;
        assertEquals(m_elements[last],m_iv.removeLast());
    }

}
