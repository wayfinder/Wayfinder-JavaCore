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
 * The class represents a hardware key in the Wayfinder environment.
 * <p>
 * A hardware key is basically a unique identifier that can be used to identify
 * a user without (too much) doubt. Any key used as an identifier should be
 * restricted to a single entity (eg user or device) and extreme care should be
 * taken to ensure that the key really is unique. (see documentation about
 * HARDWARE_KEY_TYPE_ESN below for further argument).
 * <p>
 * Starting with Wayfinder 8, the hardware keys will be used to lock a user
 * account to a certain device or simcard.
 * <p>
 * HardwareKey objects are constant; their values cannot be changed after they 
 * are created. Because HardwareKey objects are immutable they can be shared
 * freely within an application without any need to handle problems with
 * several threads and systems using the same HardwareKey objects.
 * 
 * 
 */
public final class HardwareKey {
    
    private final String m_keyXMLType;
    private final String m_keyData;
    
    
    /**
     * Creates an instance of HardwareKey.
     * <p>
     * This class is only intended as a container of the actual key
     * information and will not take any action to ensure that the type or the
     * key itself is valid or correctly formated. All validation and formating
     * is left to the caller of the constructor.
     * <p>
     * It is <b>HIGHLY</b> recommended to read the description information of
     * each HARDWARE_KEY_TYPE constant before creating an object of this type.
     * Sending incorrect (or even worse - non-unique) information to the
     * server may cause users a lot of problems.
     * 
     * @param keyType One of the HARDWARE_KEY_TYPE constants
     * @param keyData The key as a string.
     * @throws IllegalArgumentException if any parameter is invalid, null or 
     * the empty string
     */
    HardwareKey(int keyType, String keyData) {
        if(keyData == null) {
            throw new IllegalArgumentException("HardwareKey created with null value");
        } else if(keyData.length() == 0) {
            throw new IllegalArgumentException("HardwareKey created with empty string");
        }
        m_keyXMLType = getXMLType(keyType);
        m_keyData = keyData.trim();
    }

    
    /**
     * Returns the hardware key type for the XML server
     * 
     * @return The XML type as a String
     */
    public String getKeyXMLType() {
        return m_keyXMLType;
    }
    

    /**
     * Returns the actual key
     * 
     * @return The key
     */
    public String getKey() {
        return m_keyData;
    }
    
    
    /*
     *  +++++++++++ ADDING A HARDWARE KEY TYPE ++++++++++
     *  
     *  When adding a hardware key type, it is NOT enough to simply add it
     *  in the list below. It is EXTREMELY important that the server team
     *  is notified of (and approves) the addition of a new type.
     *  
     *  Also note that there will be a delay from the time the server team
     *  approves the change to the production cluster actually being able to
     *  handle the new type. It will usually require a server update for the
     *  production servers to be updated with the new type, so ensure that the
     *  server team is notified in good time.
     */
    
    
    
    /**
     * Represents the International Mobile Equipment Identity of a device
     * using a GPRS or iDEN network.
     * <p>
     * The International Mobile Equipment Identity or IMEI is a number 
     * unique to every GSM and UMTS mobile phone. 
     * <p>
     * Unlike the Electronic Serial Number or MEID of CDMA and other 
     * wireless networks, the IMEI is only used to identify the device, 
     * and has no permanent or semi-permanent relation to the 
     * subscriber. Instead, the subscriber is identified by 
     * transmission of an IMSI number, which is stored on a SIM card 
     * which can (in theory) be transferred to any handset.
     */
    static final int HARDWARE_KEY_TYPE_IMEI = 0;
    
    
    /**
     * Represents the MAC address of the bluetooth hardware in the device
     */
    static final int HARDWARE_KEY_TYPE_BTMAC = 1;
    
    
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
     */
    static final int HARDWARE_KEY_TYPE_BBPIN = 2;
    
    
    /**
     * Represents the International Mobile Subscriber Identity of the user's
     * SIM card.
     * <p>
     * International Mobile Subscriber Identity, or IMSI [im-zee], is a unique 
     * number associated with all GSM and Universal Mobile Telecommunications 
     * System (UMTS) network mobile phone users. It is stored in the Subscriber 
     * Identity Module (SIM) inside the phone and is sent by the phone to the 
     * network.
     * <p>
     * An IMSI is usually 15 digits long, but can be shorter (for example MTN 
     * South Africa's IMSIs are 14 digits). The first 3 digits are the Mobile 
     * Country Code, and is followed by the Mobile Network Code (MNC), either 
     * 2 digits (European standard) or 3 digits (North American standard). 
     * The remaining digits are the mobile subscriber identification number 
     * (MSIN) within the network's customer base.
     */
    static final int HARDWARE_KEY_TYPE_IMSI = 3;
    
    
    /**
     * Represents the unique subscriber number. This is only retrievable from
     * SEMC devices JP-7.2 or higher and is actually a hash of some kind of the 
     * user's IMSI number.
     */
    static final int HARDWARE_KEY_TYPE_SEMC_SUBSCRIB = 4;
    
    
    /**
     * Represents the serial number of a Sony Ericsson HGE 100 device, also
     * known as the "Fingal"
     */
    static final int HARDWARE_KEY_TYPE_FINGAL_ID = 5;
    
    
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
     */
    static final int HARDWARE_KEY_TYPE_ESN = 6;
    
    
    /**
     * MSISDN is a number uniquely identifying a subscription in a GSM or UMTS 
     * mobile network. Simply put, it is the telephone number to the SIM card 
     * in a mobile/cellular phone. The abbreviation has several interpretations, 
     * most common one being "Mobile Subscriber Integrated Services Digital 
     * Network Number".
     * <p>
     * MSISDN is maximized to 15 digits, prefixes not included (e.g. 00 
     * prefixes an international MSISDN when dialling from Sweden).
     * <p>
     * In GSM and its variant DCS 1800, MSISDN is built up as
     * <ul>
     * <li>MSISDN = CC + NDC + SN</li>
     * <li>CC = Country Code</li>
     * <li>NDC = National Destination Code, identifies one or part of a PLMN</li>
     * <li>SN = Subscriber Number</li>
     * </ul>
     * <p>
     * In the GSM variant PCS 1900, MSISDN is built up as
     * <ul>
     * <li>MSISDN = CC + NPA + SN</li>
     * <li>CC = Country Code</li>
     * <li>NPA = Number Planning Area</li>
     * <li>SN = Subscriber Number</li>
     * </ul>
     */
    static final int HARDWARE_KEY_TYPE_MSISDN = 7;
    
    
    /**
     * Symbolizes a generated key that is used for platforms and devices where
     * it's not possible to get hold of a real key.
     * <p>
     * <b>At NO point is the client allowed to generate a key on it's own. This
     * type of key is generated on the server side to guarantee that serveral
     * users don't collide. The client MUST retreive a dummy key through a
     * request to the server.</b>
     * <p>
     * If this type of key is used, the client must ensure that the same key
     * is sent on every session and should not re-request the key on every
     * startup.
     */
    static final int HARDWARE_KEY_TYPE_FAKE = 8;
    
    
    /**
     * Returns the XML type for the keytype
     * 
     * @param aKeyType one of the HARDWARE_KEY_TYPE constants in this class
     * @return The XML type as a string
     * @throws IllegalArgumentException If aKeyType is an invalid type
     */
    private static String getXMLType(int aKeyType) {
        switch(aKeyType) {
        case HARDWARE_KEY_TYPE_IMEI:    
            return "imei";
            
        case HARDWARE_KEY_TYPE_BTMAC:   
            return "btmac";
            
        case HARDWARE_KEY_TYPE_BBPIN:   
            return "bbpin";
            
        case HARDWARE_KEY_TYPE_IMSI:    
            return "imsi";
            
        case HARDWARE_KEY_TYPE_SEMC_SUBSCRIB: 
            return "semc_subscrib";
        
        case HARDWARE_KEY_TYPE_FINGAL_ID:
            return "fingal_id";
            
        case HARDWARE_KEY_TYPE_ESN:
            return "esn";
            
        case HARDWARE_KEY_TYPE_MSISDN:
            return "phone_msisdn";
            
        case HARDWARE_KEY_TYPE_FAKE:
            return "fake";
        }
        
        throw new IllegalArgumentException("HardwareKey created with invalid key type");
    }
}
