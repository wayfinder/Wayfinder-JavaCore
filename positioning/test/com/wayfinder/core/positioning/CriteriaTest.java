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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.positioning;

import com.wayfinder.core.positioning.Criteria.Builder;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class CriteriaTest extends TestCase {
    
    private Builder m_builderGood;
    private Builder m_builderExcellent;
    private Builder m_builderBad;
    private Builder m_builderNone;
    private Criteria m_c;
    /**
     * @param name
     */
    public CriteriaTest(String name) {
        super(name);
        m_builderGood = new Builder()
            .accuracy(Criteria.ACCURACY_GOOD)
            .altitudeReguired()
            .costAllowed()
            .courseRequired()
            .speedRequired();
        m_builderExcellent = new Builder().accuracy(Criteria.ACCURACY_EXCELLENT);
        m_builderBad = new Builder().accuracy(Criteria.ACCURACY_BAD);
        m_builderNone = new Builder().accuracy(Criteria.ACCURACY_BAD + 1);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_c = m_builderGood.build();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCriteriaSettings() {
        assertEquals(Criteria.ACCURACY_GOOD, m_c.getAccuracy());
        assertTrue(m_c.isAltitudeReguired());
        assertTrue(m_c.isCostAllowed());
        assertTrue(m_c.isCourseRequired());
        assertTrue(m_c.isSpeedRequired());
        assertEquals(Criteria.ACCURACY_EXCELLENT, m_builderExcellent.build().getAccuracy());
        assertEquals(Criteria.ACCURACY_BAD, m_builderBad.build().getAccuracy());
        assertEquals(Criteria.ACCURACY_NONE, m_builderNone.build().getAccuracy());
    }
    
    public void testHashCode() {
        assertEquals(2 * 3 * 5 * 7 * 13, m_c.hashCode());
        assertEquals(11, m_builderExcellent.build().hashCode());
        assertEquals(17, m_builderBad.build().hashCode());
        assertEquals(19, m_builderNone.build().hashCode());
    }
    
    public void testEquals() {
        assertTrue(m_c.equals(m_builderGood.build()));
        assertFalse(m_c.equals(m_builderBad.build()));
        assertFalse(m_c.equals(new Object()));
    }
}
