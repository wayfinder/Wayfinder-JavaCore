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
/**
 * 
 */
package com.wayfinder.core.shared.util;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class ListenerListTest extends TestCase {


    /**
     * Test method for {@link com.wayfinder.core.shared.util.ListenerList#getListenerInternalArray()}.
     */
    public void testGetListenerInternalArray() {
        ListenerList list = new ListenerList();
        for(int i=0; i <100; i++) {
            list.add("dummy" + i);
        }
        Object[] listenerArray1 = list.getListenerInternalArray();
        
        assertTrue(list.remove("dummy"+20));
        assertTrue(list.remove("dummy"+40));
        
        Object[] listenerArray2 = list.getListenerInternalArray();
        
        for(int i=0; i <100; i++) {
            assertEquals("dummy" + i, listenerArray1[i]);
        }
        
        assertEquals(100-2, listenerArray2.length);
        
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.ListenerList#isEmpty()}.
     */
    public void testIsEmpty() {
        ListenerList list = new ListenerList();
        assertTrue(list.isEmpty());
        
        list.add("listener1");
        list.add("listener2");
        
        list.remove("listener2");
        assertFalse(list.isEmpty());
        list.remove("listener1");
        assertTrue(list.isEmpty());
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.ListenerList#add(java.lang.Object)}.
     */
    public void testAdd() {
        ListenerList list = new ListenerList(); 
        
        String listener1 = new String("dummy1");
        String listener2 = new String("dummy1");
        
        
        assertTrue(list.add("dummy2"));
        assertTrue(list.add(listener1));
        assertFalse("try to add twice same instance", list.add(listener1));
        assertTrue("add twice equals but other instance",list.add(listener2));
        
        assertFalse("try to add null",list.add(null));
        
        Object[] listenerArray = list.getListenerInternalArray();
        assertEquals("dummy2", listenerArray[0]);
        assertSame(listener2, listenerArray[1]);
        
        
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.ListenerList#remove(java.lang.Object)}.
     */
    public void testRemove() {
        ListenerList list = new ListenerList(); 
        String listener1 = new String("dummy");
        String listener2 = new String("dummy");
        
        assertFalse(list.remove("dummy"));
        list.add(listener1);
        
        assertFalse("remove null", list.remove(null));
        assertTrue("remove equal listener", list.remove(listener2));
        assertTrue("check if was removed", list.isEmpty());
        
        
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.util.ListenerList#toString()}.
     */
    public void testToString() {
        ListenerList list = new ListenerList();
        assertNotNull(list.toString());
        list.add("dummy");
        assertNotNull(list.toString());
    }
}
