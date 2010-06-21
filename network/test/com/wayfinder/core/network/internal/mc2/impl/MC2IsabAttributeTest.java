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

import com.wayfinder.core.network.internal.mc2.impl.MC2IsabAttribute;

import junit.framework.TestCase;

public class MC2IsabAttributeTest extends TestCase {



    public void testMC2IsabAttribute() {
        new MC2IsabAttribute("x", "2");
    }
    
    
    public void testMC2IsabAttributeNullNotAllowed() {
        // null is not allowed as an attribute
        try {
            new MC2IsabAttribute(null, "2");
            fail("MC2IsabAttribute allows null as attribute");
        } catch(IllegalArgumentException iae) {
           // pass
        }
    }
    

    public void testGetAttribute() {
        MC2IsabAttribute attrib = new MC2IsabAttribute("x", "2");
        assertEquals("x", attrib.getAttribute());
    }

    public void testGetValue() {
        MC2IsabAttribute attrib = new MC2IsabAttribute("x", "2");
        assertEquals("2", attrib.getValue());
    }

    
    public void testEqualsObject() {
        // only attribute should count
        assertTrue(new MC2IsabAttribute("x", "2").equals(new MC2IsabAttribute("x", "5")));
        assertFalse(new MC2IsabAttribute("x", "2").equals(new MC2IsabAttribute("y", "2")));
        assertFalse(new MC2IsabAttribute("x", "2").equals(new Integer(2)));
    }

    
    public void testHashCode() {
        MC2IsabAttribute[] attribArray = new MC2IsabAttribute[5];
        for (int i = 0; i < attribArray.length; i++) {
            attribArray[i] = new MC2IsabAttribute("x", Integer.toString(i));
        }
        final int code = new MC2IsabAttribute("x", "2").hashCode();
        for (int i = 0; i < attribArray.length; i++) {
            assertEquals(code, attribArray[i].hashCode());
        }
    }
}
