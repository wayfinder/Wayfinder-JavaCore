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
package com.wayfinder.core.shared.internal.poidetails;


import com.wayfinder.core.shared.poidetails.DetailField;
import com.wayfinder.core.shared.poidetails.ImageGroup;
import com.wayfinder.core.shared.poidetails.PoiDetail;
import com.wayfinder.core.shared.poidetails.ReviewGroup;
import com.wayfinder.core.shared.util.CharArray;

public class PoiDetailImpl implements PoiDetail {
    
    public static final PoiDetail EMPTY_POI_DETAIL = new PoiDetailImpl();

    // <!ENTITY % poi_detail_t
    // "(dont_show|text|street_address|full_address|phone_number|
    // url|email|poi_url|poi_thumb|average_rating|
    // description|open_hours|provider_info)" >
    private static final String[] TYPES_VALUES_ARRAY = {
        "phone_number",
        "email",
        "url",
        
        "street_address",
        "full_address",
        
        "open_hours",
        "description",
        
        "provider_info",
        "poi_url",
        
        "average_rating",
        "poi_thumb"
    };
    
    public static final int TYPE_PHONE = 0;
    public static final int TYPE_EMAIL = 1;
    public static final int TYPE_WEBSITE = 2;
    
    public static final int TYPE_STREET_ADDRESS = 3;
    public static final int TYPE_FULL_ADDRESS = 4;
    
    public static final int TYPE_OPEN_HOURS = 5;
    public static final int TYPE_DESCRIPTION = 6;

    public static final int TYPE_PROVIDER_INFO = 7;
    public static final int TYPE_POI_URL = 8;

    public static final int TYPE_AVERAGE_RATING = 9;
    public static final int TYPE_POI_THUMB = 10;
    
    private DetailField m_phone;
    private DetailField m_email;
    private DetailField m_website;
    
    private DetailField m_streetAddress;
    private DetailField m_fullAddress;
    
    private DetailField m_openHours;
    private DetailField m_description;

    private DetailField m_provider;
    private DetailField m_poiUrl;
    
    private DetailField m_averageRating;
    
    private DetailField m_poiThumbnail;
    
    private ImageGroup m_imageGroup;
    
    private ReviewGroup m_reviewGroup;

    
    
    public static int getTypeForString(CharArray ca) {
        //type is a required element for fav_info and should never be null but 
        //is not required for info_item so null case need to be treated
        if (ca == null) {
            return -1;
        }
        return ca.indexIn(TYPES_VALUES_ARRAY);
    }
    
    public void setField(int type, DetailField detailField){
        switch (type) {
            case TYPE_PHONE: m_phone = detailField; break;
            case TYPE_EMAIL: m_email = detailField; break;
            case TYPE_WEBSITE: m_website = detailField; break;
            case TYPE_STREET_ADDRESS: m_streetAddress = detailField; break;
            case TYPE_FULL_ADDRESS: m_fullAddress = detailField; break;
            case TYPE_OPEN_HOURS: m_openHours = detailField; break;
            case TYPE_DESCRIPTION: m_description = detailField; break;
            case TYPE_PROVIDER_INFO: m_provider = detailField; break;
            case TYPE_POI_URL: m_poiUrl = detailField; break;
            case TYPE_AVERAGE_RATING: m_averageRating = detailField; break; 
            case TYPE_POI_THUMB: m_poiThumbnail = detailField; break;
            //default: //unknown type ignore
        }
    }
    
    public void setResources(ImageGroup imageGroup, ReviewGroup reviewGroup) {
        m_imageGroup = imageGroup;
        m_reviewGroup = reviewGroup;
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getPhone()
     */
    public DetailField getPhone() {
        return m_phone;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getEmail()
     */
    public DetailField getEmail() {
        return m_email;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getWebsite()
     */
    public DetailField getWebsite() {
        return m_website;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getStreetAddress()
     */
    public DetailField getStreetAddress() {
        return m_streetAddress;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getFullAddress()
     */
    public DetailField getFullAddress() {
        return m_fullAddress;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getOpenHours()
     */
    public DetailField getOpenHours() {
        return m_openHours;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getDescription()
     */
    public DetailField getDescription() {
        return m_description;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getProviderInfo()
     */
    public DetailField getProviderInfo() {
        return m_provider;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getPoiUrl()
     */
    public DetailField getPoiUrl() {
        return m_poiUrl;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getAverageRating()
     */
    public DetailField getAverageRating() {
        return m_averageRating;
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.poidetails.PoiDetail#getPoiThumbnail()
     */
    public DetailField getPoiThumbnail() {
        return m_poiThumbnail;
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getImageGroup()
     */
    public ImageGroup getImageGroup() {
        return m_imageGroup;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.poidetails.PoiDetail#getReviewGroup()
     */
    public ReviewGroup getReviewGroup() {
        return m_reviewGroup;
    }
    
}
