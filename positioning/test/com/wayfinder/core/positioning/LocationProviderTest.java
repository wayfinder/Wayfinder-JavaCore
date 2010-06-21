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


import com.wayfinder.core.positioning.internal.DummyLocationProvider;
import com.wayfinder.core.shared.Position;

import junit.framework.TestCase;

/**
 * 
 * 
 *
 */
public class LocationProviderTest extends TestCase {
    
    private LocationProvider m_best;    //InternalGPS, accuracy better than EXECELLENT
    private LocationProvider m_worse;   //InternalGPS, accuracy GOOD
    private LocationProvider m_NA;      //InternalGPS, temp unavailable
    
    private LocationProvider m_extGPS;  //ExternalGPS, available, acc EXCELLENT

    /**
     * @param name
     */
    public LocationProviderTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_best = new DummyLocationProvider();
        m_worse = new LocationProvider() {
            
            public int getType() {
                return PROVIDER_TYPE_INTERNAL_GPS;
            }
            
            public int getState() {
                return PROVIDER_STATE_AVAILABLE;
            }
            
            protected String getName() {
                return "not so good provider";
            }
            
            public LocationInformation getLastKnownLocation() {
                return new LocationInformation(
                        Position.decimalDegresToMc2(55.718197f),
                        Position.decimalDegresToMc2(13.190884f),
                        1,0,(short)0,0,System.currentTimeMillis());
            }
            
            public int getAccuracy() {
                return Criteria.ACCURACY_GOOD;
            }
        };
        
        m_NA = new LocationProvider() {
            
            public int getType() {
                return PROVIDER_TYPE_INTERNAL_GPS;
            }
            
            public int getState() {
                return PROVIDER_STATE_TEMPORARY_UNAVAILABLE;
            }
            
            protected String getName() {
                return "NA";
            }
            
            public LocationInformation getLastKnownLocation() {
                return new LocationInformation(
                        Position.decimalDegresToMc2(55.718197f),
                        Position.decimalDegresToMc2(13.190884f),
                        1,0,(short)0,0,System.currentTimeMillis());
            }
            
            public int getAccuracy() {
                return Criteria.ACCURACY_NONE;
            }
        };
        
        m_extGPS = new LocationProvider() {
            
            public int getType() {
                return PROVIDER_TYPE_EXTERNAL_GPS;
            }
            
            public int getState() {
                return PROVIDER_STATE_AVAILABLE;
            }
            
            protected String getName() {
                return "ExtGPS";
            }
            
            public LocationInformation getLastKnownLocation() {
                return new LocationInformation(
                        Position.decimalDegresToMc2(55.718197f),
                        Position.decimalDegresToMc2(13.190884f),
                        1,0,(short)0,0,System.currentTimeMillis());
            }
            
            public int getAccuracy() {
                return Criteria.ACCURACY_EXCELLENT;
            }
        };
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCurrentlyBetter() {
        assertTrue(m_best.isCurrentlyBetter(m_worse));
        assertTrue(m_best.isCurrentlyBetter(m_extGPS));
        assertTrue(m_best.isCurrentlyBetter(m_NA));
        
        assertFalse(m_worse.isCurrentlyBetter(m_best));
        
        assertFalse(m_worse.isCurrentlyBetter(m_extGPS));
        
        assertFalse(m_NA.isCurrentlyBetter(m_worse));
        
        assertFalse(m_NA.isCurrentlyBetter(m_NA));
    }
    
    public void testUsuallyBetter() {
        assertTrue(m_worse.isUsuallyBetter(m_extGPS));
        assertFalse(m_best.isUsuallyBetter(m_worse));
    }
    
    //just to get 100% coverage
    public void testToString() {
        assertNotNull(m_best.toString());
    }
}
