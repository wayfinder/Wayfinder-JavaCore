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

package com.wayfinder.core.search.onelist;

import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch;
import com.wayfinder.core.shared.RequestID;

/**
 * The One List search is the new search intended to be presented as a
 * flat list in the client without mapping the search matches to providers. 
 */
public interface OneListSearch {

    /**
     * <p>Initiates a new search.</p>
     * 
     * <p>Currently, only search queries of type
     * {@link SearchQuery#SEARCH_TYPE_POSITIONAL} and
     * {@link SearchQuery#SEARCH_TYPE_ADDRESS} are supported.</p>
     * 
     * <p>
     * Positional searches are the regular searches you do to find things around
     * a given position. For this search, supply a {@link SearchQuery} created
     * with {@link SearchQuery#createPositionalQuery(String, com.wayfinder.core.search.Category, com.wayfinder.core.shared.Position, int, boolean)}
     * </p>
     * <p>
     * Address searches are somewhat special. This search is intended for 
     * geocoding purposes, i.e. getting a position for a certain address.
     * Supply a {@link SearchQuery} created with 
     * {@link SearchQuery#createAddressGeocodingQuery(String, String, com.wayfinder.core.search.TopRegion, int)}.
     * <br />
     * <b>IMPORTANT:</b> Address search is limited to a round 0 search. Upon 
     * success, {@link AddressGeocodingListener#addressGeocodingDone(RequestID, OneListSearchReply)}
     * is called.
     * </p>
     * 
     * @param query The search query to send to the server.
     * @param listener The listener to receive the search results.
     * @return A RequestID to uniquely identify the request. 
     */
    public RequestID search(SearchQuery query,
            OneListSearchListener listener);
    
    
    /**
     * Request POI details for the given {@link SearchMatch}
     * 
     * @param match the SearchMatch to get the details for
     * @param listener a listener to be notified when the details are available
     * 
     * @return the {@link RequestID} identifying this request
     */
    public RequestID requestDetails(SearchMatch match, 
            MatchDetailsRequestListener listener);
}
