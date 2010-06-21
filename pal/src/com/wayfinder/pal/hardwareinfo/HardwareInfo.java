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
package com.wayfinder.pal.hardwareinfo;

public interface HardwareInfo {
    
    
    /**
     * Returns the International Mobile Equipment Identity of a device
     * using a GPRS or iDEN network.
     * <p>
     * Please note that the IMEI and not the IMEISV should be returned
     * 
     * @return The IMEI as a String or <code>null</code> if the IMEI cannot be
     * read or if it's a CDMA device
     */
    public String getIMEI();
    
    
    /**
     * Returns the the International Mobile Subscriber Identity of the user's
     * SIM card.
     * <p>
     * An IMSI is usually 15 digits long, but can be shorter (for example MTN 
     * South Africa's IMSIs are 14 digits). The first 3 digits are the Mobile 
     * Country Code, and is followed by the Mobile Network Code (MNC), either 
     * 2 digits (European standard) or 3 digits (North American standard). 
     * The remaining digits are the mobile subscriber identification number 
     * (MSIN) within the network's customer base.
     * 
     * @return The IMSI as a String or <code>null</code> if the IMSI cannot be
     * read
     */
    public String getIMSI();
    
    
    /**
     * Represents the Electronic Serial Number of a device using a CDMA network
     * <p>
     * Electronic Serial Numbers (ESNs) were created by the FCC to 
     * uniquely identify mobile devices from the days of AMPS in the 
     * United States in the mid-1980s on. ESNs are mainly used with 
     * AMPS, TDMA and CDMA phones in the United States, compared to 
     * IMEI numbers used by all GSM phones.
     * <p>
     * An ESN is 32 bits long. It consists of three fields, including 
     * an 8-bit manufacturer code, an 18-bit unique serial number, 
     * and 6 bits that are reserved for later use, although in 
     * practice these 6 bits are combined into a 24-bit serial number 
     * field. Code 0x80 was reserved and now is used to represent 
     * pseudo ESNs (pESN) which are calculated from an MEID. 
     * <p>
     * <b>Pseudo-ESNs are not guaranteed to be unique. All implementations 
     * extracting ESN should take care not to send a pESN to the server, as it 
     * potentially could collide with other users.</b>
     * <p>
     * @return The ESN as a String or <code>null</code> if the ESN cannot be
     * read or if the currently used wireless radio is not CDMA
     */
    public String getESN();
    
    
    /**
     * Represents the PIN number of a BlackBerry smartphone
     * <p>
     * Every BlackBerry has a unique id called BlackBerry PIN. This is an 8 
     * character long hexadecimal value that is used to identify the device 
     * against the BlackBerry Enterprise Server.
     * <p>
     * Implementations should take special care checking the extracted PIN
     * from the device against the constant DeviceInfo.INVALID_DEVICE_ID
     * (-1). Devices reporting this value are not true BlackBerry devices,
     * but instead devices supporting the BlackBerry Built-In technology.
     * <p>
     * <b>If the device reports the PIN as DeviceInfo.INVALID_DEVICE_ID, the
     * PIN should NOT be added as a HardwareKey</b>
     *
     * @return The PIN number as a String or <code>null</code> if the device
     * is not a BlackBerry device
     */
    public String getBlackBerryPIN();
    
    
    /**
     * Represents the MAC address of the bluetooth hardware in the device
     *
     * @return The MAC address of the bluetooth radio or <code>null</code> if
     * the MAC address cannot be obtained
     */
    public String getBluetoothMACAddress();

}
