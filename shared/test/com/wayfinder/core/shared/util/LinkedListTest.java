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

public final class LinkedListTest extends TestCase {
    private LinkedList m_list;
    private Object[] m_elements;
    private static final int NBR_ELEMENTS = 10;

    public void setUp() throws Exception {
        super.setUp();
        
        m_list = new LinkedList();
        m_elements = new Object[NBR_ELEMENTS];

        // this really only needs to be run once, but no @BeforeClass annotation
        // support in JUnit 3
        for(int i=0; i < NBR_ELEMENTS; i++) {
            m_elements[i] = new Integer(i);
        }
    }
    
    public void testCreateList() {
        assertNull(m_list.getFirst());
        assertNull(m_list.getLast());
        assertEquals(0, m_list.size());
        assertTrue(m_list.isEmpty());
    }
    
    /**
     * make sure that calling remove on an empty list doesn't throw strange
     * exceptions.
     */
    public void testRemovingInEmptyList() {
        assertNull(m_list.removeFirst());
        assertNull(m_list.removeLast());
        assertEquals(0, m_list.size());
        assertTrue(m_list.isEmpty());
    }

 
    public void testAddFirstOneElement() {
        Object o0 = m_elements[0];
        m_list.addFirst(o0);
        // we want to test that the same object is returned, not a copy.
        assertTrue(o0 == m_list.getFirst());
        assertTrue(o0  == m_list.getLast());
        assertEquals(1, m_list.size());
        assertFalse(m_list.isEmpty());
    }

    /**
     * Add the first n elements of m_elements to list
     * 
     * @param n - the number of elements to add.
     */
    private void addElementsLast(int n) {
        for(int i=0; i < n; i++) {
            m_list.addLast(m_elements[i]);
        }
    }
    
    public void testAddTwoElements() {
        Object o0 = m_elements[0];
        Object o1 = m_elements[1];
        m_list.addLast(o0);
        m_list.addLast(o1);
        assertEquals(2, m_list.size());
        assertTrue(o0 == m_list.getFirst());
        assertTrue(o1 == m_list.getLast());
    }

    public void testAddManyElements() {
        addElementsLast(NBR_ELEMENTS);
        assertEquals(NBR_ELEMENTS, m_list.size());
        assertFalse(m_list.isEmpty());
        assertTrue(m_list.getFirst() == m_elements[0]);
        assertTrue(m_list.getLast() == m_elements[NBR_ELEMENTS - 1]);
    }
    

    public void testRemoveTheSingleElement() {
        addElementsLast(1);
        m_list.removeFirst();
        assertTrue(m_list.isEmpty());
        assertEquals(0, m_list.size());
        assertNull(m_list.getFirst());
    }

    public void testRemovingSeveralElements() {
        addElementsLast(NBR_ELEMENTS);
        m_list.remove(m_elements[2]);
        m_list.remove(m_elements[3]);
        assertEquals(NBR_ELEMENTS - 2, m_list.size());
    }

    public void testRemovingNonExisting() {
        addElementsLast(NBR_ELEMENTS);
        // remove() uses equals, not object identity
        assertFalse(m_list.remove(new Integer(NBR_ELEMENTS + 1)));
    }
    

    public void testClear() {
        addElementsLast(NBR_ELEMENTS);
        m_list.clear();
        assertEquals(0, m_list.size());
        assertNull(m_list.getLast());
    }


    public void testGet() {
        addElementsLast(NBR_ELEMENTS);
        assertTrue(m_list.get(3) == m_elements[3]);
    }
    
    public void testGetInvalid() {
        addElementsLast(NBR_ELEMENTS);
        try {
            m_list.get(NBR_ELEMENTS + 1);
            fail("testGetInvalid(): expected exception was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
    }


    public void testSet() {
        addElementsLast(NBR_ELEMENTS);
        final int INDEX = 5;
        Object o = m_list.set(INDEX, new Integer(NBR_ELEMENTS + 1));
        assertTrue(o == m_elements[INDEX]);
        assertFalse(o == m_list.get(INDEX));
        assertEquals(NBR_ELEMENTS, m_list.size());
    }


    public void testAddInMiddle() {
        addElementsLast(NBR_ELEMENTS);
        final int INDEX = 5;
        final Object newElement = new Integer(NBR_ELEMENTS + 1); 
        m_list.add(INDEX, newElement);
        assertTrue(m_list.get(INDEX) == newElement);
        
        // two below tests that shift was OK
        assertTrue(m_list.get(INDEX + 1) == m_elements[INDEX]);
        assertTrue(m_list.get(INDEX - 1) == m_elements[INDEX - 1]);        

        assertEquals(NBR_ELEMENTS + 1, m_list.size());        
    }

    public void testAddAtLast() {
        addElementsLast(NBR_ELEMENTS);
        final Object newElement = new Integer(NBR_ELEMENTS + 1);
        m_list.add(NBR_ELEMENTS, newElement);
        assertEquals(NBR_ELEMENTS + 1, m_list.size());
        assertTrue(newElement == m_list.getLast());
        assertTrue(m_elements[0] == m_list.getFirst());
    }
    

    public void testRemoveIndex() {
        addElementsLast(NBR_ELEMENTS);
        m_list.remove(5);
        assertEquals(NBR_ELEMENTS - 1, m_list.size());
        assertTrue(m_elements[0] == m_list.getFirst());
        assertTrue(m_elements[NBR_ELEMENTS - 1] == m_list.getLast());
        assertFalse(m_list.contains(new Integer(5)));       
    }


    public void testContains() {
        addElementsLast(NBR_ELEMENTS);
        // violates the constant...
        assertTrue(m_list.contains(m_elements[5]));
    }


    public void testIndexOf() {
        addElementsLast(NBR_ELEMENTS);
        // violates the constant...
        assertEquals(5, m_list.indexOf(m_elements[5]));
    }


    public void testLastIndexOf() {
        addElementsLast(NBR_ELEMENTS);
        addElementsLast(NBR_ELEMENTS);
        // violates the constant...
        assertEquals(NBR_ELEMENTS + 5, m_list.lastIndexOf(m_elements[5]));
    }


    public void testToArray() {
        addElementsLast(NBR_ELEMENTS);
        Object[] elements = m_list.toArray();
        assertEquals(NBR_ELEMENTS, elements.length);
        for(int i=0; i < NBR_ELEMENTS; i++) {
            assertTrue(m_elements[i] == elements[i]);
        }
    }
    
    
    public void testToArrayWithParamArray() {
        addElementsLast(NBR_ELEMENTS);
        Object[] elements = new Object[m_list.size()];
        m_list.toArray(elements);
        assertEquals(NBR_ELEMENTS, elements.length);
        for(int i=0; i < NBR_ELEMENTS; i++) {
            assertTrue(m_elements[i] == elements[i]);
        }
    }
    
    
    
}
