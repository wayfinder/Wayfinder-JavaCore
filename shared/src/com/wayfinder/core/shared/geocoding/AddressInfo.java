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

package com.wayfinder.core.shared.geocoding;


/**
 * <p>AddressInfo represents structured address information resembling the
 * javax.microedition.location.AddressInfo in JSR-179.</p>
 * 
 * <p>We have not modeled the data with abstract fields but rather
 * straight-forward members and access functions. This decision is based on
 * that the number of supported address components now and in the foreseeable
 * future are rather limited. Should a need arise to support 10+ components,
 * a new class can be designed.</p>
 * 
 * <p>This interface is placed in the shared space since it is anticipated that
 * it will also be used by the search system in the future.</p>
 * 
 * <p>Implementors must make sure that:
 * <ol><li>The implementation by itself is thread-safe (construction, updating)</li>
 *     <li>The constructed object is not touched by core after it has been
 *         passed to UI with {@link com.wayfinder.core.geocoding.GeocodeInterface}
 *         or some other way.</li>
 * </ol></p>
 */
public interface AddressInfo {

    /**
     * Gets the street name.
     * 
     * @return the street name or "" if the street name is not known
     *         (lack of map data) or the position is too far away from any
     *         street. 
     */
    public String getStreet();


    /**
     * Gets the city part name.
     * 
     * @return the city part name or "" if the city part name is not known
     *         (lack of map data) or the position is too far away from any
     *         street. 
     */
    public String getCityPart();


    /**
     * Gets the city name.
     *
     * @return the city name or "" if the city name is not known (lack of map
     *         data - unlikely) or the position is too far away from any
     *         city.
     */
    public String getCity();


    /**
     * Gets the municipal name.
     *
     * @return the municipal name or "" if the municipal name is not known
     *         (lack of map data) or the position is too far away from any
     *         land mass with municipal structures. 
     */
    public String getMunicipal();


    /**
     * Gets the country name. In the United States, this will be state name,
     * e.g. Oklahoma due to the way our maps are structured.
     * 
     * @return the country name or "" if the position is too far away from a
     *         land mass in our maps.
     */
    public String getCountryOrState();


    /**
     * <p>Gets the corresponding top region ID.</p>
     * 
     * <p>In an upcoming release, you will be able to use this ID to retrieve
     * the corresponding {@link com.wayfinder.core.search.TopRegion}. This can
     * be used to pre-select country in search forms etc.</p>
     * 
     * <p>Note that if you just
     * want to execute the search, the search module already allows searching
     * around your position without first looking up the top region ID with
     * this interface.
     * See {@link com.wayfinder.core.search.SearchQuery#createPositionalQuery(String, com.wayfinder.core.search.Category, com.wayfinder.core.shared.Position, int, boolean)}.
     * </p>
     *
     * @return the top region ID or "" if the position is too far away from a
     *         land mass in our maps.
     */
    public String getTopRegionID();   
}
