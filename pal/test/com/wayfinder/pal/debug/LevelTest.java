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
package com.wayfinder.pal.debug;

import junit.framework.TestCase;

public class LevelTest extends TestCase {

    public void testGetIntValue() {
        for (int i = 0; i < 10; i++) {
            Level l = new Level(i, Integer.toString(i));
            assertEquals(i, l.getIntValue());
        }
    }

    public void testToString() {
        for (int i = 0; i < 10; i++) {
            Level l = new Level(i, Integer.toString(i));
            assertEquals(Integer.toString(i), l.toString());
        }
    }

    public void testIsHigherOrEqualTo() {
        Level high = new Level(100, "HIGH");
        Level low  = new Level(50, "LOW");
        assertTrue(high.isHigherOrEqualTo(low));
        assertFalse(low.isHigherOrEqualTo(high));
        Level alsoHigh = new Level(100, "HIGH");
        assertTrue(alsoHigh.isHigherOrEqualTo(high));
    }

    public void testEqualsObject() {
        Level high = new Level(100, "HIGH");
        Level alsoHigh = new Level(100, "HIGH");
        Level low  = new Level(50, "LOW");
        assertEquals(high, alsoHigh);
        assertTrue(high.equals(alsoHigh));
        assertFalse(high.equals(low));
    }
    
    public void testLevelsAreConsistent() {
        Level[] levelArray = {
                Level.TRACE,
                Level.DEBUG,
                Level.INFO,
                Level.WARN,
                Level.ERROR,
                Level.FATAL
        };
        for (int i = 1; i < levelArray.length; i++) {
            assertTrue(levelArray[i].isHigherOrEqualTo(levelArray[i-1]));
        }
        
    }

}
