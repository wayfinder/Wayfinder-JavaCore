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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.search.internal;

import com.wayfinder.core.search.internal.BasicSearchMatchImpl.PositionDistanceComparator;
import com.wayfinder.core.shared.Position;

/**
 * Tests the distance functions of the {@link BasicSearchMatchImpl} class.
 */
public class BasicSearchMatchImplDistanceTest
    extends BasicSearchMatchImplTest {

    private static final Position OTHER_POSITION = new Position(-1000, 1000);
    private static final Position CLOSE_TO_OTHER = new Position(-1100, 1100);

    private BasicSearchMatchImpl m_other;


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_other = new BasicSearchMatchImpl(MATCH_ID,
                                           MATCH_LOCATION,
                                           MATCH_NAME,
                                           OTHER_POSITION);
    }

    public void testDistanceCompareSelf() {
        PositionDistanceComparator comp = new PositionDistanceComparator(OTHER_POSITION);
        assertTrue(comp.canBeCompared(m_match));
        assertEquals(0, comp.compare(m_match, m_match));
    }

    public void testDistanceCloser() {
        PositionDistanceComparator comp = new PositionDistanceComparator(CLOSE_TO_OTHER);
        assertTrue(comp.canBeCompared(m_other));
        assertTrue(comp.compare(m_match, m_other) > 0);
    }

    public void testDistanceFarther() {
        // can't just assertFalse and turn the condition
        // we must also change objects or both code paths will not be taken
        PositionDistanceComparator comp = new PositionDistanceComparator(CLOSE_TO_OTHER);
        assertTrue(comp.compare(m_other, m_match) < 0);
    }
    
    public void testDistanceCanBeCompared() {
        // can be compared
        PositionDistanceComparator comp = new PositionDistanceComparator(CLOSE_TO_OTHER);
        assertFalse(comp.canBeCompared(null));
        assertFalse(comp.canBeCompared("String"));
    }
    
    public void testPosDistCompErrors() {
        PositionDistanceComparator comp = new PositionDistanceComparator(CLOSE_TO_OTHER);
        try {
            comp.compare(null, null);
            fail("Should throw IllegalArgumentExcepion");
        }
        catch (IllegalArgumentException e) {
            // ok
        }
        
        try {
            comp.compare("String", m_other);
            fail("Should throw IllegalArgumentExcepion");
        }
        catch (IllegalArgumentException e) {
            // ok
        }
    }

}
