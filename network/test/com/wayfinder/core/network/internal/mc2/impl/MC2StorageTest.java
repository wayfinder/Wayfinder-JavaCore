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
package com.wayfinder.core.network.internal.mc2.impl;

import java.util.Enumeration;

import com.wayfinder.core.network.internal.mc2.impl.MC2IsabAttribute;
import com.wayfinder.core.network.internal.mc2.impl.MC2Storage;

import junit.framework.TestCase;

public class MC2StorageTest extends TestCase {
    
    private MC2Storage m_storage;
    
    protected void setUp() throws Exception {
        super.setUp();
        m_storage = new MC2Storage();
    }
    
    
    protected void tearDown() throws Exception {
        super.tearDown();
        m_storage = null;
    }
    

    public void testSetMC2AuthAttribute() {
        m_storage.setMC2AuthAttribute(new MC2IsabAttribute("x", "2"));
    }
    

    public void testGetMC2IsabAttributes() {
        MC2IsabAttribute[] array = new MC2IsabAttribute[] {
                new MC2IsabAttribute("a", "2"),
                new MC2IsabAttribute("b", "2"),
                new MC2IsabAttribute("c", "2"),
                new MC2IsabAttribute("d", "2")
        };
        
        for (int i = 0; i < array.length; i++) {
            m_storage.setMC2AuthAttribute(array[i]);
        }
        
        Enumeration e = m_storage.getMC2IsabAttributes();
        assertTrue(e.hasMoreElements());
        while (e.hasMoreElements()) {
            MC2IsabAttribute a = (MC2IsabAttribute) e.nextElement();
            final int index = assertAttribIsInArray(a, array);
            array[index] = null;
        }
        
        // make sure all are accounted for
        for (int i = 0; i < array.length; i++) {
            if(array[i] != null) {
                fail("Some attributes are not returned");
            }
        }
        // and make sure nothing was removed in the process
        e = m_storage.getMC2IsabAttributes();
        int i = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            i++;
        }
        assertEquals(i, array.length);
    }
    
    
    public void testReplaceAndGetMC2IsabAttributes() {
        MC2IsabAttribute[] array = new MC2IsabAttribute[] {
                new MC2IsabAttribute("a", "5"),
                new MC2IsabAttribute("b", "5"),
                new MC2IsabAttribute("c", "5"),
                new MC2IsabAttribute("d", "5")
        };
        for (int i = 0; i < array.length; i++) {
            m_storage.setMC2AuthAttribute(array[i]);
        }
        // the run the above test again
        testGetMC2IsabAttributes();
        // and then check that the attributes are actually "2" as is inserted
        // by the other test method
        Enumeration en = m_storage.getMC2IsabAttributes();
        while (en.hasMoreElements()) {
            MC2IsabAttribute a = (MC2IsabAttribute) en.nextElement();
            assertEquals("2", a.getValue());
        }
    }
    
    
    public void testRemoveMC2IsabAttributes() {
        MC2IsabAttribute[] array = new MC2IsabAttribute[] {
                new MC2IsabAttribute("a", "5"),
                new MC2IsabAttribute("b", "5"),
                new MC2IsabAttribute("c", "5"),
                new MC2IsabAttribute("d", "5")
        };
        for (int i = 0; i < array.length; i++) {
            m_storage.setMC2AuthAttribute(array[i]);
        }
        
        MC2IsabAttribute[] arrayWithNull = new MC2IsabAttribute[] {
                new MC2IsabAttribute("a", null),
                new MC2IsabAttribute("b", null),
                new MC2IsabAttribute("c", null),
                new MC2IsabAttribute("d", null)
        };
        for (int i = 0; i < array.length; i++) {
            m_storage.setMC2AuthAttribute(arrayWithNull[i]);
        }
        
        // it should now be empty
        assertFalse(m_storage.getMC2IsabAttributes().hasMoreElements());
    }
    
    
    private static int assertAttribIsInArray(MC2IsabAttribute attrib, MC2IsabAttribute[] array) {
        for (int i = 0; i < array.length; i++) {
            if(attrib.equals(array[i])) {
                return i;
            }
        }
        fail("Could not find attribute in array");
        return 0; // will never be called
    }
    
    
    public void testRemoveMC2AuthAttribute() {
        m_storage.setMC2AuthAttribute(new MC2IsabAttribute("x", "2"));
    }
    
    
    
//
//    public void testAddMC2IsabStatusListener() {
//        
//        
//        
//        fail("Not yet implemented");
//    }
//
//    public void testRemoveMC2IsabStatusListener() {
//        fail("Not yet implemented");
//    }
//
//    public void testGetMC2IsabStatusListeners() {
//        fail("Not yet implemented");
//    }

}
