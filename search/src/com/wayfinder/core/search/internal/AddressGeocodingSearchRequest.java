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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.search.internal;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.internal.OneListSearchMC2Request.RequestListener;
import com.wayfinder.core.search.onelist.OneListSearchListener;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.util.Comparator;
import com.wayfinder.core.shared.util.WFUtil;

/**
 * 
 *
 */
public class AddressGeocodingSearchRequest implements RequestListener {
    
    private final RequestID m_reqID;
    private final SearchQuery m_query;
    private final Comparator m_matchComparator;
    private final OneListSearchListener m_listener;

    private final MC2Interface m_mc2Ifc;
    private final CallbackHandler m_callHandler;
    private final SharedSystems m_systems;

    /**
     * @param reqID the {@link RequestID} to report back to the listener
     * @param query the query. Only queries created with 
     * {@link SearchQuery#createAddressGeocodingQuery(String, String, com.wayfinder.core.search.TopRegion, int)}
     * are accepted. Otherwise an {@link IllegalArgumentException} is thrown.
     * @param matchComparator a {@link Comparator} that defines how the results should be sorted
     * @param listener the listener to receive the search results.
     * @param mc2Ifc 
     * @param callHandler
     * @param systems
     */
    AddressGeocodingSearchRequest(RequestID reqID, SearchQuery query,
            Comparator matchComparator, OneListSearchListener listener,
            MC2Interface mc2Ifc, CallbackHandler callHandler, SharedSystems systems) {
        if (reqID == null || query == null || matchComparator == null 
                || listener == null || mc2Ifc == null || callHandler == null 
                || systems == null) {
            throw new IllegalArgumentException();
        }
        if (query.getQueryType() != SearchQuery.SEARCH_TYPE_ADDRESS) {
            throw new IllegalArgumentException("Only queries created with " +
            		"SearchQuery.createAddressGeocodingQuery() are supported!");
        }
        m_reqID = reqID;
        m_query = query;
        m_matchComparator = matchComparator;
        m_listener = listener;
        m_mc2Ifc = mc2Ifc;
        m_callHandler = callHandler;
        m_systems = systems;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.OneListSearchMC2Request.RequestListener#replyReceived(int, com.wayfinder.core.search.internal.OneListSearchMatchImpl[])
     */
    public void replyReceived(final int estimatedTotalNbrOfMatches,
            final OneListSearchMatchImpl[] searchMatches) {
        // the case of address geocoding is special with regards to a regular
        // search as we do just a round 0 search, and once we get the result,
        // the search is done

        // FIXME: the server ignores the sorting attribute in one_search_request,
        // (unless it gets a position, in which case it sorts by distance) so 
        // although we specifically ask for alfa_sort, we still need to sort 
        // these results
        WFUtil.insertionSort(searchMatches, m_matchComparator);
        
        m_callHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.searchDone(m_reqID, 
                        new OneListSearchReplyImpl(
                                m_query, 
                                estimatedTotalNbrOfMatches, 
                                searchMatches));
            }
        });
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.OneListSearchMC2Request.RequestListener#requestFailed(com.wayfinder.core.shared.error.CoreError)
     */
    public void requestFailed(final CoreError error) {
        m_callHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(m_reqID, error);
            }
        });    
    }
    
    void doRequest() {
        m_mc2Ifc.pendingMC2Request(
                new OneListSearchMC2Request(
                        m_systems.getSettingsIfc().getGeneralSettings().getInternalLanguage(), 
                        this, // listener 
                        m_query, // ADDRESS type query
                        0, // always round 0
                        OneListSearchMC2Request.ALFA_SORT)); //result sorting, always alphabetically for this one
    }

}
