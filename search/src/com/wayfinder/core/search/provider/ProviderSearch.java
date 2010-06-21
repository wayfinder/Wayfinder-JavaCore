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
package com.wayfinder.core.search.provider;

import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.SearchReply.MatchList;
import com.wayfinder.core.shared.RequestID;


/**
 * The provider search is a method of search where multiple providers are 
 * searched at once and the matches are returned separated into one 
 * {@link MatchList} per provider.
 * <p>
 * To find out what provider is responsible for each list, the application can
 * use the {@link ProviderSearchReply#getProviderOfList(int)} method
 */
public interface ProviderSearch {

    /**
     * Initiates a new search.
     * <p>
     * The search module will send the contents of the provided 
     * {@link SearchQuery} to the server and call the provided
     * {@link ProviderSearchListener} once the results have been received.
     * <p>
     *
     * @param query The {@link SearchQuery} to send to the server
     * @param listener The {@link ProviderSearchListener} which will receive the
     * results. 
     * @return A {@link RequestID} to uniquely identify the request
     * @throws IllegalArgumentException if query or listener is null
     */
    public RequestID search(SearchQuery query, ProviderSearchListener listener);
    
    
    
    /**
     * Used if a specific searchheading should be expanded with more matches
     * from the database.
     * 
     * @param reply The {@link ProviderSearchReply} containing the heading to expand
     * @param listIndex The index of the list to expand
     * @param nbrOfMoreHits The number of more hits to download from the server
     * @param listener The {@link ProviderSearchListener} to call when the 
     * result is ready
     * @return A {@link RequestID} to uniquely identify the request
     * @throws IllegalArgumentException if reply or listener is null
     * @throws IllegalArgumentException if headingIndex or nbrOfMoreHits is less
     * than 0
     */
    public RequestID expandMatchList(ProviderSearchReply reply, int listIndex, int nbrOfMoreHits, ProviderSearchListener listener);

    
}
