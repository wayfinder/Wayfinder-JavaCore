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
/**
 * 
 */
package com.wayfinder.core.positioning;

/**
 * Data structure that describing a provider. The class is used from the UI 
 * perspective to check what provider has sent a location update, or which 
 * provider was updated.
 * 
 * 
 */
public abstract class LocationProvider {
    
    /**
     * Type for internal GPS
     */
    public static final int PROVIDER_TYPE_INTERNAL_GPS  = 0;
    
    /**
     * Type for external (Bluetooth) GPS
     */
    public static final int PROVIDER_TYPE_EXTERNAL_GPS  = 1;
    
    /**
     * Type for cable GPS (COM)
     */
    public static final int PROVIDER_TYPE_CABLE_GPS     = 2;
    
    /**
     * Type for CellID
     */
    public static final int PROVIDER_TYPE_NETWORK       = 3;
    
    /**
     * Type marking any time of positioning simulator
     */
    public static final int PROVIDER_TYPE_SIMULATOR    = 99;
        
    /**
     * Provider is available, thus is available to get 
     * {@link LocationInformation} from
     */
    public static final int PROVIDER_STATE_AVAILABLE                = 0;

    /**
     * Provider is out of service. Meaning that there should be no expectation
     * that this state
     * changes in the future (basically an unavailable provider)
     */
    public static final int PROVIDER_STATE_OUT_OF_SERVICE           = 1;
        
    /**
     * Provider is temporary unavailable.  It can be expected that the provider
     * will become available.
     */
    public static final int PROVIDER_STATE_TEMPORARY_UNAVAILABLE    = 2;
    
    /**
     * Returns the type of provider.
     * 
     * @return one of the provider type constants.
     * @see #PROVIDER_TYPE_NETWORK
     * @see #PROVIDER_TYPE_INTERNAL_GPS
     * @see #PROVIDER_TYPE_EXTERNAL_GPS 
     * @see #PROVIDER_TYPE_CABLE_GPS
     * @see #PROVIDER_TYPE_SIMULATOR
     */
    public abstract int getType();
    
    /**
     * Returns the state of this provider.
     * 
     * @return one of the state constants for a LocationProvider
     * @see #PROVIDER_STATE_AVAILABLE 
     * @see #PROVIDER_STATE_OUT_OF_SERVICE
     * @see #PROVIDER_STATE_TEMPORARY_UNAVAILABLE
     */
    public abstract int getState();
    
    /**
     * @return The last {@link LocationInformation} returned by this provider
     */
    public abstract LocationInformation getLastKnownLocation();
    
    /**
     * The accuracy of this provider, the same as the accuracy of the last
     * known location.
     */
    public abstract int getAccuracy();
    
    /**
     * Compares this provider with another one, based on current accuracy and
     * current state. It can happen that a provider that usually has better
     * accuracy to be considered worse if the provider being compared to is
     * in a better state (e.g. CellID provider, available, is better than any 
     * GPS in a non-available state).
     * 
     * @param provider The provider to compare with.
     * @return <code>true</code> if better
     */
    public boolean isCurrentlyBetter(LocationProvider provider) {
        if (this.getState() == PROVIDER_STATE_AVAILABLE
                && provider.getState() == PROVIDER_STATE_AVAILABLE) {
            if (this.getAccuracy() < provider.getAccuracy()) return true;
            else return false;
        }
        else if (provider.getState() == PROVIDER_STATE_AVAILABLE
                && this.getState() != PROVIDER_STATE_AVAILABLE) return false;
        else if (this.getState() == PROVIDER_STATE_AVAILABLE) return true;
        return false;
    }
    
    /**
     * Compares this provider with another one but only taking into account
     * the type of provider. A GPS provider should be considered better than
     * a CellID provider when both are working properly.
     * 
     * @param provider
     * @return true if this provider is better than the parameter provider.
     * False if they are equal or the parameter provider is worse.
     */
    public boolean isUsuallyBetter(LocationProvider provider) {
        if (this.getType() < provider.getType()) return true;
        
        return false;
    }

    protected abstract String getName();
    
    protected static final String[] STATES = {
        "AVAILABLE",
        "OUT_OF_SERVICE",
        "TEMP_UNAVAILABLE"
    };
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append(" state: ");
        sb.append(STATES[getState()]);
        return sb.toString();
    }
}
