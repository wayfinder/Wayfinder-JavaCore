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
package com.wayfinder.core.positioning.internal;

import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.positioning.internal.vfcellid.VFCellIDLocationProvider;
import com.wayfinder.pal.positioning.PositionProviderInterface;
import com.wayfinder.pal.positioning.UpdatesHandler;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class InternalLocationProviderTest extends TestCase {
    
    private PositionProviderInterface m_platformPosProvider;
    
    private ProviderUpdatesListener m_updListener;
    
    private boolean m_platformRunning;
    
    private InternalLocationProvider m_intGPS;
    private InternalLocationProvider m_extGPS;
    private InternalLocationProvider m_cell;
    private InternalLocationProvider m_vfcell;

    /**
     * @param name
     */
    public InternalLocationProviderTest(String name) {
        super(name);        
        
        m_platformPosProvider = new PositionProviderInterface() {
            
            public void stopUpdates() {
                m_platformRunning = false;
            }
            
            public void setUpdatesHandler(UpdatesHandler coreHandler) {
            }
            
            public void resumeUpdates() {
                m_platformRunning = true;
            }
            
            public int getType() {
                return TYPE_SIMULATOR;
            }
        };
        
        m_updListener = new ProviderUpdatesListener() {
            
            public void stateUpdated(InternalLocationProvider provider, int state) {
                // 
                
            }
            
            public void locationUpdated(InternalLocationProvider provider,
                    LocationInformation location) {
                // 
                
            }
        };
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_intGPS = InternalLocationProvider.createLocationProvider(
                    LocationProvider.PROVIDER_TYPE_INTERNAL_GPS, m_platformPosProvider);
        
        m_extGPS = InternalLocationProvider.createLocationProvider(
                LocationProvider.PROVIDER_TYPE_EXTERNAL_GPS, m_platformPosProvider);
        
        m_cell = InternalLocationProvider.createLocationProvider(
                LocationProvider.PROVIDER_TYPE_NETWORK, m_platformPosProvider);
        
        m_vfcell = InternalLocationProvider.createLocationProvider(
                InternalLocationProvider.PROVIDER_TYPE_NETWORK_VF, m_platformPosProvider);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCreateLocationProvider() {
//        m_intGPS = InternalLocationProvider.createLocationProvider(
//                LocationProvider.PROVIDER_TYPE_INTERNAL_GPS, m_platformPosProvider);
        assertTrue(m_intGPS instanceof InternalGPSLocationProvider);
        assertEquals(LocationProvider.PROVIDER_TYPE_INTERNAL_GPS, m_intGPS.getType());
        assertEquals("InternalGPS", m_intGPS.getName());

//        m_extGPS = InternalLocationProvider.createLocationProvider(
//                LocationProvider.PROVIDER_TYPE_EXTERNAL_GPS, m_platformPosProvider);
        assertTrue(m_extGPS instanceof ExternalGPSLocationProvider);
        assertEquals(LocationProvider.PROVIDER_TYPE_EXTERNAL_GPS, m_extGPS.getType());
        assertEquals("ExternalGPS", m_extGPS.getName());

//        m_cell = InternalLocationProvider.createLocationProvider(
//                LocationProvider.PROVIDER_TYPE_NETWORK, m_platformPosProvider);
        assertTrue(m_cell instanceof CellIDLocationProvider);
        assertEquals(LocationProvider.PROVIDER_TYPE_NETWORK, m_cell.getType());
        assertEquals("Cell-ID", m_cell.getName());

//        m_vfcell = InternalLocationProvider.createLocationProvider(
//                InternalLocationProvider.PROVIDER_TYPE_NETWORK_VF, m_platformPosProvider);
        assertTrue(m_vfcell instanceof VFCellIDLocationProvider);
        assertEquals(InternalLocationProvider.PROVIDER_TYPE_NETWORK_VF, m_vfcell.getType());
        assertEquals("Cell-VF", m_vfcell.getName());

        InternalLocationProvider p = InternalLocationProvider.createLocationProvider(123, null);
        assertNull(p);
    }

    public void testSetProviderUpdatesListener() {
        m_intGPS.setProviderUpdatesListener(m_updListener);
        assertSame(m_updListener, m_intGPS.m_updatesListener);
    }
    
    public void testUpdatePosition() {
        ProviderUpdatesListener listener = new ProviderUpdatesListener() {
            
            public void stateUpdated(InternalLocationProvider provider, int state) {
                
            }
            
            public void locationUpdated(InternalLocationProvider provider,
                    LocationInformation location) {
                assertSame(m_intGPS, provider);
                assertSame(m_intGPS.getLastKnownLocation(), location);
            }
        };
        
        m_intGPS.setProviderUpdatesListener(listener);
        m_intGPS.updatePosition(55, 13, 20, 135, 10, InternalLocationProvider.VALUE_UNDEF, System.currentTimeMillis());
        
        LocationInformation lastLoc = m_intGPS.getLastKnownLocation();
        
        assertEquals(55, lastLoc.getMC2Position().getDecimalLatitude(), 0.00001);
        assertEquals(13, lastLoc.getMC2Position().getDecimalLongitude(), 0.00001);
        
        assertEquals(Criteria.ACCURACY_NONE, m_intGPS.getAccuracy());
    }
    
    public void testUpdateState() {
        ProviderUpdatesListener listener = new ProviderUpdatesListener() {
            
            public void stateUpdated(InternalLocationProvider provider, int state) {
                assertSame(m_intGPS, provider);
                assertEquals(LocationProvider.PROVIDER_STATE_AVAILABLE, state);
            }
            
            public void locationUpdated(InternalLocationProvider provider,
                    LocationInformation location) {
            }
        };
        
        m_intGPS.setProviderUpdatesListener(listener);
        m_intGPS.updateState(LocationProvider.PROVIDER_STATE_AVAILABLE);
        
        assertEquals(LocationProvider.PROVIDER_STATE_AVAILABLE, m_intGPS.getState());
    }
    
    public void testResumeAndSuspend() {
        m_intGPS.resume();
        assertTrue(m_platformRunning);
        m_intGPS.suspend();
        assertFalse(m_platformRunning);
    }
}
