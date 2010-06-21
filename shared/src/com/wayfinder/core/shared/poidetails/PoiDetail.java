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
package com.wayfinder.core.shared.poidetails;

import com.wayfinder.core.search.onelist.OneListSearchReply;


/**
 * Represent extra information associated with a POI.
 * Each field can null if that information is missing or has been received yet.
 * 
 * @see OneListSearchReply.SearchMatch#additionalInfoExists()
 *   
 * 
 */
public interface PoiDetail {
    
    /**
     * Return the average rating of all reviews for the POI as a 
     * {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Average Rating"</li>
     * <li>{@link DetailField#getValue()} contain the rating as a number e.g. 
     * "3"</li> 
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information
     */
    DetailField getAverageRating();
    
    /**
     * Return a url to a thumbnail image for the POI as a {@link DetailField} 
     * where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Poi Thumb"</li>
     * <li>{@link DetailField#getValue()} contain full url pointing to an image 
     * "http://assets1.qype.com/uploads/photos/0007/7369/Noodle_Time_Greenwich_mini.jpg"</li> 
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information
     */
    DetailField getPoiThumbnail();
    
    /**
     * Return the phone number of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Phone Number"</li>
     * <li>{@link DetailField#getValue()} contain a phone number e.g. 
     * "020 8293 5263"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getPhone();

    /**
     * Return the email address of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Email"</li>
     * <li>{@link DetailField#getValue()} contain a email address e.g. 
     * "contact@ritz.com"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getEmail();

    /**
     * Return the website url of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Website"</li>
     * <li>{@link DetailField#getValue()} contain a phone number e.g. 
     * "http://www.ritz.com"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getWebsite();

    /**
     * Return the address of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Address"</li>
     * <li>{@link DetailField#getValue()} contain the address e.g. 
     * "10-11 Nelson Road"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getStreetAddress();

    /**
     * Return the full address of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Address"</li>
     * <li>{@link DetailField#getValue()} contain the full address e.g. 
     * "10-11 Nelson Road, London SE10 9JB"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getFullAddress();
    
    /**
     * Return the open hours info of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Open hours"</li>
     * <li>{@link DetailField#getValue()} contain the open hours info as a text 
     * e.g. "Mon - Sat: 8:00 - 24:00, Sun: 10:00 - 24:00"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getOpenHours();

    /**
     * Return the description of the POI as a {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Description"</li>
     * <li>{@link DetailField#getValue()} contain the description e.g. 
     * "Market Coffee House serve breakfasts as well as soup, 
     * sandwiches, fish platters, muffins, tea cakes and hot drinks to 
     * eat in or take away"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getDescription();

    /**
     * Return the provider of this POI detail info in the following format:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "Information provided by"</li>
     * <li>{@link DetailField#getValue()} contain the name of the provider 
     * followed by provider website separated by a ';' 
     * (e.g. Qype.com;http://www.qype.com)</li> 
     * 
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getProviderInfo();

    /**
     * Return the url address of the POI on the provider website as a 
     * {@link DetailField} where:
     * <li>{@link DetailField#getName()} contains a label e.g.
     * "View On Qype"</li>
     * <li>{@link DetailField#getValue()} contain the full address e.g. 
     * "http://www.qype.co.uk/place/72679-Noodle-Time-London"</li>  
     *  
     * @return a {@link DetailField} with the data described above, or null 
     * if there is no such information 
     */
    DetailField getPoiUrl();

    /**
     * @return a {@link ImageGroup} containing the full url addresses to the 
     * images related with the POI from a provider. 
     */
    ImageGroup getImageGroup();

    /**
     * @return a {@link ReviewGroup} containing the reviews made to the POI 
     * from a provider.    
     */
    ReviewGroup getReviewGroup();

}
