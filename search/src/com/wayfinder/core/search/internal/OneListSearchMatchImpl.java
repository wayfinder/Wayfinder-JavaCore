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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.search.internal;

import java.io.IOException;

import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.poidetails.PoiDetail;

/**
 * <p>This class is thread safe by virtue of all fields being final.</p>
 */
final class OneListSearchMatchImpl
    extends BasicSearchMatchImpl
    implements SearchMatch, MC2WritableElement {

    private final String m_brandImageName;
    private final String m_categoryImageName;
    private final String m_providerImageName;
    
    private boolean m_hasAdditionalInfo;

    private PoiDetail m_infoFieldList = PoiDetailImpl.EMPTY_POI_DETAIL; 

    /**
     * Constructs a new instance.
     * 
     * @param matchID see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param matchName see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param matchLocation see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param position see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param brandImageName value for {@link #getMatchBrandImageName()}. Must not be null.
     * @param categoryImageName value for {@link #getMatchCategoryImageName()}. Must not be null.
     * @param providerImageName value for {@link #getMatchProviderImageName()}. Must not be null.
     */
    OneListSearchMatchImpl(String matchID,
                           String matchLocation,
                           String matchName,
                           Position position,
                           String brandImageName,
                           String categoryImageName,
                           String providerImageName) {
        super(matchID, matchLocation, matchName, position);
        if (brandImageName == null
            || categoryImageName ==  null
            || providerImageName == null) {
            throw new IllegalArgumentException();
        }

        m_brandImageName = brandImageName;
        m_categoryImageName = categoryImageName;
        m_providerImageName = providerImageName;
    }

    /**
     * Constructs a new instance.
     * 
     * @param matchID see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param matchLocation see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param matchName see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param position see {@link BasicSearchMatchImpl#BasicSearchMatchImpl(String, String, String, Position)}
     * @param brandImageName value for {@link #getMatchBrandImageName()}. Must not be null.
     * @param categoryImageName value for {@link #getMatchCategoryImageName()}. Must not be null.
     * @param providerImageName value for {@link #getMatchProviderImageName()}. Must not be null.
     * @param detail additional details, can be empty
     * @param hasAdditionalInfo flag that indicates if the detailFieldList already 
     * contains all the possible data for this match
     */
    OneListSearchMatchImpl(String matchID,
                           String matchLocation,
                           String matchName,
                           Position position,
                           String brandImageName,
                           String categoryImageName,
                           String providerImageName,
                           PoiDetail detail, 
                           boolean hasAdditionalInfo) {
        this(matchID, matchLocation, matchName, position, brandImageName, categoryImageName, providerImageName);
        if (detail != null) {
            m_infoFieldList = detail;
        } //else remain InfoFieldListImpl.EMPTY_INFOFIELDLIST
        m_hasAdditionalInfo = hasAdditionalInfo;
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch#getMatchBrandImageName()
     */
    public String getMatchBrandImageName() {
        return m_brandImageName;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch#getMatchCategoryImageName()
     */
    public String getMatchCategoryImageName() {
        return m_categoryImageName;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch#getMatchProviderImageName()
     */
    public String getMatchProviderImageName() {
        return m_providerImageName;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch#getFilteredInfo()
     */
    public PoiDetail getFilteredInfo() {
        return m_infoFieldList;
    }
    
    /**
     * This is intended to be used from inside the Core, after making a POI details request
     * and more detail_fields have become available for this search match.
     * @param list the (complete) details list
     */
    void setFullInfo(PoiDetailImpl detail) {
        m_infoFieldList = detail;
        m_hasAdditionalInfo = false;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch#getCurrentDeatilsLevel()
     */
    public boolean additionalInfoExists() {
        return m_hasAdditionalInfo;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2WritableElement#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        mc2w.startElement(MC2Strings.tsearch_match);
        // server only searches by itemid and doesn't care about
        // the real type
        mc2w.attribute(MC2Strings.asearch_match_type, MC2Strings.STREET_STRING);
        mc2w.elementWithText(MC2Strings.tname, getMatchName());
        mc2w.elementWithText(MC2Strings.titemid, getMatchID());
        mc2w.elementWithText(MC2Strings.tlocation_name, getMatchLocation());
        mc2w.endElement(MC2Strings.tsearch_match);
    }
}
