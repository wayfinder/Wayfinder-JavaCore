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
package com.wayfinder.pal.network.info;


/**
 * Represents an interface
 * 
 * 
 *
 */
public interface NetworkInfo {
    
    public static final int WAF_UNKNOWN = -2;
    public static final int WAF_NONE    = -1;
    public static final int WAF_3GPP    = 0;
    public static final int WAF_CDMA    = 1;
    public static final int WAF_iDEN    = 2;
    
    
    /**
     * Returns the Wireless Access Family type of the currently used network
     * 
     * @return One of the WAF_* constants in this class
     */
    public int getNetworkWAF();
    
    
    /**
     * Returns the info object with more detailed information regarding
     * the current 3GPP network.
     * 
     * @return 
     * @throws IllegalStateException if the device is currently not using a
     * 3GPP network
     */
    public TGPPInfo get3GPPInfo() throws IllegalStateException;
    
    
    // CDMAInfo and IDENInfo commented out for now
    // may be added in the future
    // Important! CDMA and iDEN networks do NOT have the same network
    // properties as the 3GPP counterpart...
    
    // public CDMAInfo getCDMAInfo() throws IllegalStateException;

    // public IDENInfo getIDENInfo() throws IllegalStateException;
    
    
    /**
     * Signifies that the current radio signal strength is unknown
     */
    public static final int SIGNAL_STRENGTH_UNKNOWN = Integer.MIN_VALUE;
    
    
    /**
     * Retrieves current signal strength.
     * 
     * @return Signal level in dBm (typically between -121 dBm and -40 dBm) or
     * the constant SIGNAL_STRENGTH_UNKNOWN if the platform is unable to
     * determine the strength
     */
    public int getSignalStrength();
    
    
    
    //-------------------------------------------------------------------------
    // utility
    
    public static final int RADIO_STATE_ON        = 0;
    public static final int RADIO_STATE_OFF       = 1;
    public static final int RADIO_STATE_UNKNOWN   = 2;
    
    
    /**
     * Returns the current state of the wireless radio (eg if it's on or off)
     * 
     * @return On of the RADIO_STATE_* constants in this class
     */
    public int getRadioState();
    
    
    public static final int ROAMING_STATE_ROAMING   = 0;
    public static final int ROAMING_STATE_HOME      = 1;
    public static final int ROAMING_STATE_UNKNOWN   = 2;
    
    
    
    public int getRoamingState();
    
    /**
     * Return true if the phone is airplane/flight mode, which implies that 
     * all wireless are disabled. 
     * <p>NOTE: The vice versa is not true, if all the wireless are disabled 
     * the phone is not necessary in airplane mode.</p>   
     *   
     * @return true for airplane mode, false otherwise or if the state could not
     * be determined 
     */
    public boolean isAirplaneMode();

}
