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
package com.wayfinder.core.search.internal.topregion;

import com.wayfinder.core.search.internal.topregion.TopRegionImpl;

import junit.framework.TestCase;

public class TopRegionImplTest extends TestCase {
    
    /**
     * useful for other testcases in different packages that might need a 
     * TopRegionImpl object
     */
    public static final TopRegionImpl TOP_REGION_SWEDEN = 
        new TopRegionImpl("Sweden", TopRegionImpl.TYPE_COUNTRY, 1);
    
    private static final String REGION_NAME = "Bahamas";
    private static final int    REGION_TYPE = 56;
    private static final int    REGION_ID   = 89;
    
    
    public void testStandardTopRegion() {
        TopRegionImpl region = new TopRegionImpl(REGION_NAME, REGION_TYPE, REGION_ID);
        assertEquals(REGION_NAME, region.getRegionName());
        assertEquals(REGION_ID, region.getRegionID());
        assertEquals(REGION_TYPE, region.getRegionType());
    }
    
    
    public void testInvalidConstructorParams() {
        String[] names = {
                "",
                null
        };
        
        for (int i = 0; i < names.length; i++) {
            try {
                new TopRegionImpl(names[i], REGION_TYPE, REGION_ID);
                fail("TopRegion should throw an exception for invalid parameter" + names[i]);
            } catch(IllegalArgumentException iae) {
                // passed the test
            }
        }
    }
    
    
    public void testHashCode() {
        TopRegionImpl region = new TopRegionImpl(REGION_NAME, REGION_TYPE, REGION_ID);
        assertEquals(REGION_ID, region.hashCode());
    }
    
    
    public void testEqualTopRegions() {
        TopRegionImpl region1 = new TopRegionImpl(REGION_NAME, REGION_TYPE, REGION_ID);
        TopRegionImpl region2 = new TopRegionImpl(REGION_NAME, REGION_TYPE, REGION_ID);
        assertTrue(region1.equals(region2));
    }
    
    
    public void testNonEqualTopRegions() {
        TopRegionImpl region1 = new TopRegionImpl(REGION_NAME, REGION_TYPE, REGION_ID);
        TopRegionImpl region2 = new TopRegionImpl("ghana", 5, 5);
        assertFalse(region1.equals(region2));
    }
    
    
    public void testEqualsWrongClass() {
        TopRegionImpl region1 = new TopRegionImpl(REGION_NAME, REGION_TYPE, REGION_ID);
        Integer i = new Integer(2);
        assertFalse(region1.equals(i));
    }
    
}
