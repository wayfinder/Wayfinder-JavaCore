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
public class LocationInformationTest extends TestCase {
    
    /**
     * 
     */
    private static final int ALTITUDE = 10;
    /**
     * 
     */
    private static final int LON = 157372032;
    /**
     * 
     */
    private static final int LAT = 664744320;
    /**
     * 
     */
    private static final short COURSE = 135;
    /**
     * 
     */
    private static final float SPEED_MPS = 20;
    private Builder m_builderGood;
    private Builder m_builderExcellent;
    private Builder m_builderBad;
    private Builder m_builderNone;
    
    private LocationInformation m_locationExcellent;
    private LocationInformation m_locationGood;
    private LocationInformation m_locationBad;
    private LocationInformation m_locationNone;
    
    private long m_posTime;

    /**
     * @param name
     */
    public LocationInformationTest(String name) {
        super(name);
        m_builderGood = new Builder()
            .accuracy(Criteria.ACCURACY_GOOD)
            //.altitudeReguired()
            .costAllowed()
            .courseRequired()
            .speedRequired();
        m_builderExcellent = new Builder()
            .accuracy(Criteria.ACCURACY_EXCELLENT)
            .altitudeReguired()
            .costAllowed()
            .courseRequired()
            .speedRequired();
        m_builderBad = new Builder().accuracy(Criteria.ACCURACY_BAD).speedRequired();
        m_builderNone = new Builder().accuracy(Criteria.ACCURACY_BAD + 1);
        
        m_posTime = System.currentTimeMillis();
        
        m_locationExcellent = new LocationInformation(
                LAT, LON, Criteria.ACCURACY_EXCELLENT, 
                SPEED_MPS, COURSE, ALTITUDE, m_posTime);
        m_locationGood = new LocationInformation(
                LAT, LON, Criteria.ACCURACY_GOOD, 
                SPEED_MPS, COURSE, ALTITUDE, m_posTime);
        m_locationBad = new LocationInformation(
                LAT, LON, Criteria.ACCURACY_BAD, 
                SPEED_MPS, COURSE, ALTITUDE, m_posTime);
        m_locationNone = new LocationInformation(LAT, LON);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testValues() {
        assertEquals(Criteria.ACCURACY_EXCELLENT, m_locationExcellent.getAccuracy());
        assertTrue(m_locationExcellent.hasAltitude());
        assertEquals(ALTITUDE, m_locationExcellent.getAltitude());
        assertTrue(m_locationExcellent.hasCourse());
        assertEquals(COURSE, m_locationExcellent.getCourse());
        assertTrue(m_locationExcellent.hasSpeed());
        assertEquals(SPEED_MPS, m_locationExcellent.getSpeed(), 0.000001f);
        assertEquals(m_posTime, m_locationExcellent.getPositionTime());
        assertEquals(LAT, m_locationExcellent.getMC2Position().getMc2Latitude());
        assertEquals(LON, m_locationExcellent.getMC2Position().getMc2Longitude());
                
        assertFalse(m_locationNone.hasAltitude());
        assertEquals(0, m_locationNone.getAltitude());
        assertFalse(m_locationNone.hasCourse());
        assertEquals(0, m_locationNone.getCourse());
        assertFalse(m_locationNone.hasSpeed());
        assertEquals(0, m_locationNone.getSpeed(), 0.000001f);
    }
    
    public void testCriteriaCompare() {
        assertTrue(m_locationExcellent.isAtLeastAsGoodAs(m_builderExcellent.build()));
        assertTrue(m_locationExcellent.isAtLeastAsGoodAs(m_builderGood.build()));
        assertTrue(m_locationExcellent.isAtLeastAsGoodAs(m_builderBad.build()));
        assertTrue(m_locationExcellent.isAtLeastAsGoodAs(m_builderNone.build()));
        
        assertFalse(m_locationGood.isAtLeastAsGoodAs(m_builderExcellent.build()));
        assertTrue(m_locationGood.isAtLeastAsGoodAs(m_builderGood.build()));
        
        assertFalse(m_locationBad.isAtLeastAsGoodAs(m_builderGood.build()));
        
        assertFalse(m_locationNone.isAtLeastAsGoodAs(m_builderExcellent.build()));
        assertFalse(m_locationNone.isAtLeastAsGoodAs(m_builderGood.build()));
        assertFalse(m_locationNone.isAtLeastAsGoodAs(m_builderBad.build()));
        assertTrue(m_locationNone.isAtLeastAsGoodAs(m_builderNone.build()));
    }
    
    public void testToString() {
        assertNotNull(m_locationExcellent.toString());
    }
}
