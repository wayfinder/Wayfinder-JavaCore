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
package com.wayfinder.core.userdata.internal.hwkeys;


/**
 * Signals that no hardware keys could be read from the current platform. 
 * 
 * 
 */
public class NoHardwareKeysAvailableException extends Exception {
    
    private final int m_reason;
    
    /**
     * Standard constructor
     * 
     * @param reason One of the REASON_ constants in this class
     */
    NoHardwareKeysAvailableException(int reason) {
        super("No hardware keys available");
        m_reason = reason;
    }
    
    
    /**
     * Returns a more exact reason as to why no keys could be read.
     * <p>
     * Since it's up to each platform specific implementation to handle this in
     * a correct way, the reason may differ quite substationally between
     * platforms.
     * 
     * @return One of the REASON_ constants in this class.
     */
    public int getReason() {
        return m_reason;
    }
    
    //-------------------------------------------------------------------------
    // Constants
    
    /**
     * Signifies that no keys have been found on the platform because there
     * (by design) are no available keys to read.
     */
    public static final int REASON_NO_KEYS_ON_PLATFORM = 0;
    
    
    /**
     * Signifies that the bluetooth mac address is the only available key for
     * this platform, but it cannot be read due to an Exception being thrown.
     * <p>
     * The most likely cause for this is that bluetooth is currently turned off
     * on the device.
     */
    public static final int REASON_READ_PROBLEM_BLUETOOTH = 1;
}
