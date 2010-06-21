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
package com.wayfinder.core.shared.util;

import junit.framework.TestCase;

public class FibonacciHeapTest extends TestCase {

    public void testHeap() {
        try {
            new FibonacciHeap(null);
            fail("FibonacciHeap should not allow creation with null comparator");
        } catch(IllegalArgumentException iae) {
            // pass
        }
        
        FibonacciHeap heap = new FibonacciHeap(new IntegerComparator());
        assertTrue(heap.isEmpty()); // should be empty when created
        assertNull(heap.findHighestPrio());
        assertNull(heap.removeHighestPrio());
        
        // test insert, first with something that cannot be comparated
        try {
            heap.insert(new Float(1.0));
            fail("FibonacciHeap should not allow insertion of non-comparable values");
        } catch(IllegalArgumentException iae) {
            // pass
        }
        
        // now with real values
        // first smallest -> biggest
        final int nbrOfValues = 20;
        int expectedSize = 0;
        for (int i = 1; i <= nbrOfValues/2; i++) {
            heap.insert(new Integer(i));
            assertFalse(heap.isEmpty());
            assertEquals(++expectedSize, heap.size());
        }
        // now biggest -> smallest
        for (int i = nbrOfValues; i > nbrOfValues/2; i--) {
            heap.insert(new Integer(i));
            assertFalse(heap.isEmpty());
            assertEquals(++expectedSize, heap.size());
        }
        
        // test peek
        assertEquals(nbrOfValues, ((Integer)heap.findHighestPrio()).intValue());
        
        // test extraction
        for (int i = nbrOfValues; i > 0; i--) {
            Integer intObj = (Integer) heap.removeHighestPrio();
            assertEquals(i, intObj.intValue());
        }
        assertTrue(heap.isEmpty());
        
        // test clear
        for (int i = 0; i < nbrOfValues; i++) {
            heap.insert(new Integer(i));
            assertFalse(heap.isEmpty());
            assertEquals(1, heap.size());
            heap.clear();
            assertTrue(heap.isEmpty());
            assertEquals(0, heap.size());
        }
    }
    
    
    private static class IntegerComparator implements Comparator {
        
        public boolean canBeCompared(Object obj) {
            return obj instanceof Integer;
        }
        
        public int compare(Object obj1, Object obj2) throws IllegalArgumentException {
            if(canBeCompared(obj1) && canBeCompared(obj2)) {
                Integer one = (Integer) obj1;
                Integer two = (Integer) obj2;
                int nbr1 = one.intValue();
                int nbr2 = two.intValue();
                
                if(nbr1 == nbr2) {
                    return 0;
                } else if(nbr1 > nbr2) {
                    return -1;
                }
                return 1;
            }
            return 0;
        }
    }
}
