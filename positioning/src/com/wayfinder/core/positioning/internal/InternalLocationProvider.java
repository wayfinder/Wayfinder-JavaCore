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
package com.wayfinder.core.positioning.internal;

import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.positioning.internal.vfcellid.VFCellIDLocationProvider;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.positioning.PositionProviderInterface;
import com.wayfinder.pal.positioning.UpdatesHandler;

public class InternalLocationProvider 
extends LocationProvider 
implements UpdatesHandler {
    
    private static final Logger LOG = LogFactory.getLoggerForClass(InternalLocationProvider.class);
    
    protected static final int PROVIDER_TYPE_NETWORK_VF = 4;
    
    /**
     * One of the PROVIDER_TYPE constants.
     */
    protected final int m_type;
    
    /**
     * The provider state.
     */
    protected int m_state = PROVIDER_STATE_OUT_OF_SERVICE;
    
    /**
     * The last LocationInformation that was returned
     */
    protected LocationInformation m_lastKnownLocation;
    
    protected int m_accuracy = Criteria.ACCURACY_NONE;
    
    /**
     * The position provider from the PAL, which is directly linked to the
     * underlying platform.
     */
    protected final PositionProviderInterface m_platformPosProvider;
    
    /**
     * 
     */
    protected ProviderUpdatesListener m_updatesListener;
    
    public static InternalLocationProvider createLocationProvider(int type, 
            PositionProviderInterface platformPosProvider) {
        switch (type) {
        case PROVIDER_TYPE_INTERNAL_GPS:
            return new InternalGPSLocationProvider(platformPosProvider);
            
        case PROVIDER_TYPE_EXTERNAL_GPS:
            return new ExternalGPSLocationProvider(platformPosProvider);
            
        case PROVIDER_TYPE_NETWORK:
            return new CellIDLocationProvider(platformPosProvider);
            
        case PROVIDER_TYPE_NETWORK_VF:
            return new VFCellIDLocationProvider(platformPosProvider);
        }
        
        return null;
    }
    
    protected InternalLocationProvider(int type, PositionProviderInterface platformPosProvider) {
        m_type = type;
        m_platformPosProvider = platformPosProvider;
    }
    
    public void setProviderUpdatesListener(ProviderUpdatesListener updListener) {
        m_updatesListener = updListener;
    }
    
    public void updatePosition(double latitudeDeg, double longitudeDeg,
            float speedMps, float course, float altitude, int accuracy,
            long timestamp) {
        
        int mc2Lat = Position.decimalDegresToMc2(latitudeDeg);
        int mc2Lon = Position.decimalDegresToMc2(longitudeDeg);
        
        if (accuracy == VALUE_UNDEF) accuracy = Criteria.ACCURACY_NONE;
        m_accuracy = accuracy;
        
        m_lastKnownLocation = new LocationInformation(
                mc2Lat, 
                mc2Lon, 
                accuracy, 
                speedMps, 
                (short) course, 
                (int) altitude, 
                timestamp);
        
        if (LOG.isDebug()) {
            LOG.debug("InternalLocationProvider.updatePosition()", ""+this+", location: "+m_lastKnownLocation);
        }
        
        if (m_updatesListener != null) {
            m_updatesListener.locationUpdated(this, m_lastKnownLocation);
        }
    }

    public void updateState(int state) {
        
        if (LOG.isDebug()) {
            LOG.debug("InternalLocationProvider.updateState()", ""+this+", new state: "+STATES[state]);
        }
        if (state != m_state) {
            m_state = state;
            if (m_updatesListener != null) {
                m_updatesListener.stateUpdated(this, m_state);
            }
        }
    }
    
    public void resume() {
        m_platformPosProvider.resumeUpdates();
    }
    
    public void suspend() {
        m_platformPosProvider.stopUpdates();
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationProvider#getLastKnownLocation()
     */
    public LocationInformation getLastKnownLocation() {
        return m_lastKnownLocation;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationProvider#getState()
     */
    public int getState() {
        return m_state;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationProvider#getType()
     */
    public int getType() {
        return m_type;
    }
    
    protected String getName() {
        return "Abstract provider";
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationProvider#getAccuracy()
     */
    public int getAccuracy() {
        return m_accuracy;
    }
}
