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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.geocoding.internal;

import com.wayfinder.core.shared.geocoding.AddressInfo;

/**
 * <p>Updatable structured address information.</p>
 * 
 * <p>The objects are mutable since that simplifies parsing in
 * {@link ExpandMC2Request}. Thread-safety is achieved by correct
 * synchronization and that core does not touch the object after it has been
 * passed to the UI.</p>
 * 
 * <p>This class can be moved to shared space (and protection lowered) if we
 * want to use the structured addressinfo in search as well.</p>
 */
final class AddressInfoImpl implements AddressInfo {

    private String m_street;
    private String m_cityPart;
    private String m_city;
    private String m_municipal;
    private String m_topRegionName;
    private String m_topRegionID;


    /**
     * Default constructor which initializes all members to the empty string.
     */
    AddressInfoImpl() {
        synchronized (this) {
            m_street = m_cityPart = m_city = m_municipal = "";
            m_topRegionName = m_topRegionID = "";
        }
    }


    public synchronized String getStreet() {
        return m_street;
    }

    /**
     * Sets the street name.
     * 
     * @param street - must not be null.
     */
    synchronized void setStreet(String street) {
        m_street = street;
    }


    public synchronized String getCityPart() {
        return m_cityPart;
    }

    /**
     * Sets the city part name.
     * 
     * @param cityPart - must not be null.
     */
    synchronized void setCityPart(String cityPart) {
        m_cityPart = cityPart;
    }


    public synchronized String getCity() {
        return m_city;
    }

    /**
     * Sets the city name.
     * 
     * @param city - must not be null.
     */
    synchronized void setCity(String city) {
        m_city = city;
    }


    public synchronized String getMunicipal() {
        return m_municipal;
    }

    /**
     * Sets the municipal name.
     * 
     * @param municipal - must not be null.
     */
    synchronized void setMunicipal(String municipal) {
        m_municipal = municipal;
    }


    public synchronized String getCountryOrState() {
        return m_topRegionName;
    }

    public synchronized String getTopRegionID() {
        return m_topRegionID;
    }

    /**
     * <p>Sets both top region name and ID to lessen the chance that they get
     * out of sync.</p>
     * 
     * @param topRegionName - the name of the top region. 
     * @param topRegionID - the ID of the top region.
     */
    synchronized void setTopRegion(String topRegionName,
                                   String topRegionID) {
        m_topRegionName = topRegionName;
        m_topRegionID = topRegionID;
    }


    // ---------------------------------------------------------------------
    // debugging
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        final String c = ",";

        sb.append("AddressInfoImpl {");
        sb.append(m_street); sb.append(c);
        sb.append(m_cityPart); sb.append(c);
        sb.append(m_city); sb.append(c);
        sb.append(m_municipal); sb.append(c);
        sb.append(m_topRegionName);
        sb.append("("); sb.append(m_topRegionID); sb.append(")}");
        
        return sb.toString();
    }
}
