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
 * Represents information about a 3GPP network.
 * 
 * 
 *
 */
public interface TGPPInfo {
    
    //-------------------------------------------------------------------------
    // Info regarding current network
    
    
    /**
     * Signifies that it's unknown to the platform if this is a GPRS or UMTS
     * network.
     */
    public static final int TYPE_3GPP_UNKNOWN = Integer.MIN_VALUE;
    
    
    /**
     * Signifies that the current network is a GPRS (2G) network. This includes 
     * the EDGE subfamily.
     */
    public static final int TYPE_3GPP_GPRS = 1;
    
    
    /**
     * Signifies that the current network is a UMTS (3G) network.
     */
    public static final int TYPE_3GPP_UMTS = 2;
    
    
    /**
     * Returns the type of the current network.
     * 
     * @return One of the TYPE_* constants in this class
     */
    public int getNetworkType();
    
    
    //-------------------------------------------------------------------------
    // checks to see if info is obtainable
    
    
    /**
     * Checks to see if the platform is capable of reporting the Mobile
     * Country Code (MCC) of the network it's currently camping in.
     * 
     * @return true if and only if the implementation can return a valid MCC
     * for the current network
     */
    public boolean supportsCurrentMCC();
    
    
    /**
     * Checks to see if the platform is capable of reporting the Mobile
     * Network Code (MNC) of the network it's currently camping in.
     * 
     * @return true if and only if the implementation can return a valid MNC
     * for the current network
     */
    public boolean supportsCurrentMNC();
    
    
    /**
     * Checks to see if the platform is capable of reporting the Mobile
     * Country Code (MCC) of the home network.
     * 
     * @return true if and only if the implementation can return a valid MCC
     * for the current network
     */
    public boolean supportsHomeMCC();
    
    
    /**
     * Checks to see if the platform is capable of reporting the Mobile
     * Network Code (MNC) of the home network.
     * 
     * @return true if and only if the implementation can return a valid MNC
     * for the current network
     */
    public boolean supportsHomeMNC();
    
    
    /**
     * Checks to see if the platform is capable of reporting the Location Area
     * Code (LAC) of the network it's currently camping in.
     * 
     * @return true if and only if the implementation can return a valid LAC
     * for the current network
     */
    public boolean supportsLAC();
    
    
    /**
     * Checks to see if the platform is capable of reporting the ID of the
     * currently used network cell.
     * 
     * @return true if and only if the implementation can return a valid Cell ID
     * for the current network
     */
    public boolean supportsCellID();
    
    
    //-------------------------------------------------------------------------
    // Info regarding current network
    
    /**
     * Returns the Mobile Country Code (MCC) of the network it's currently 
     * camping in. The MCC will be reported as a 3 digit decimal number.
     * <p>
     * <b>This method will only return a valid number if supportsCurrentMCC()
     * returns true</b>
     * 
     * @return The MCC as a 3 digit decimal String
     * @throws NetworkException if the platform is not capable of reporting the
     * Mobile Country Code
     */
    public String getCurrentMCC() throws NetworkException;
    
    
    /**
     * Returns the Mobile Network Code (MNC) of the network it's currently 
     * camping in. The MNC will be reported as either a 2 or 3 digit decimal 
     * number depending on the current network.
     * <p>
     * <b>This method will only return a valid number if supportsCurrentMNC()
     * returns true</b>
     * 
     * @return The MNC as a 2 or 3 digit decimal String
     * @throws NetworkException if the platform is not capable of reporting the
     * Mobile Network Code
     */
    public String getCurrentMNC() throws NetworkException;
    
    
    /**
     * Returns the Location Area Code (LAC) of the network it's currently 
     * camping in. The LAC will be reported as a hexadecimal number with '0x' 
     * prefix.
     * <p>
     * <b>This method will only return a valid number if supportsLAC()
     * returns true</b>
     * 
     * @return The LAC as 4 digit hexadecimal number with '0x' prefix
     * @throws NetworkException if the platform is not capable of reporting the
     * Location Area Code
     */
    public String getLAC() throws NetworkException;
    
    
    /**
     * Returns the Cellular ID (CellID) of the network it's currently 
     * camping in.
     * <ul>
     * <li>For GPRS and EDGE type networks, the cellid will be reported as a 
     * four digit hexadecimal number with '0x' prefix</li>
     * <li>For UMTS type networks, the cellid will be reported as a eight digit
     * hexadecimal number with '0x' prefix. The top 4 digits (top 16 bits) are
     * the Base Station Identity Code while the lower 4 digits (lower 16 bits)
     * are the cell id.</li>
     * </ul>
     * <p>
     * <b>This method will only return a valid number if supportsCellID()
     * returns true</b>
     * 
     * @return The cell ID as 4 or 8 digit hexadecimal number plus '0x' prefix
     * @throws NetworkException if the platform is not capable of reporting the
     * Location Area Code
     */
    public String getCellID() throws NetworkException;
    
    
    
    //-------------------------------------------------------------------------
    // Info regarding home network

    
    /**
     * Returns the Mobile Country Code (MCC) of the home network. The MCC will 
     * be reported as a 3 digit decimal number.
     * <p>
     * <b>This method will only return a valid number if supportsHomeMCC()
     * returns true</b>
     * 
     * @return The MCC as a 3 digit decimal String
     * @throws NetworkException if the platform is not capable of reporting the
     * Mobile Country Code
     */
    public String getHomeMCC() throws NetworkException;
    
    
    /**
     * Returns the Mobile Network Code (MNC) of the home network. The MNC will 
     * be reported as either a 2 or 3 digit decimal 
     * number depending on the current network.
     * <p>
     * <b>This method will only return a valid number if supportsHomeMNC()
     * returns true</b>
     * 
     * @return The MNC as a 2 or 3 digit decimal String
     * @throws NetworkException if the platform is not capable of reporting the
     * Mobile Network Code
     */
    public String getHomeMNC() throws NetworkException;
    
}
