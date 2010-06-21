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

import com.wayfinder.core.search.SearchReply;

//DO NOT REMOVE - import of BasicSearchMatch is needed for javadoc tool.
//It doesn't work to just import com.wayfinder.core.search.SearchReply
//(regardless on if you try to link with "MatchList"
//or "SearchReply.MatchList").
//Tested with JDK 1.6.0_17.
import com.wayfinder.core.search.SearchReply.BasicSearchMatch;

import com.wayfinder.core.shared.poidetails.PoiDetail;

/**
 * <p>A reply to a search done with {@link OneListSearch}.</p>
 *
 * <p>There is only one MatchList as providers are not used.</p>
 *
 * <p>Implementors must be thread safe.</p>
 */
public interface OneListSearchReply extends SearchReply {


    // --------------------------------------------------------------------
    /**
     * <p>Represents the a list of matches found by {@link OneListSearch}.</p>
     */
    public static interface MatchList {

        /**
         * <p>Returns the server's estimate of the total number of matches
         * matching the search query.</p>
         * 
         * <p>It is not guaranteed that it is possible to get all of these
         * matches.</p>
         * 
         * @return the estimated total number of matches.
         */
        public int getEstimatedTotalNbrOfMatches();

        /**
         * Returns the number of matches in this list. 
         * 
         * @return the number of matches in this list, always >= 0.
         */
        public int getNbrOfMatches();

        /**
         * <p>Get a match.</p>
         * 
         * <p>Matches are indexed 0 ... getNbrOfMatches() - 1.</p>
         * 
         * @param index index of match to get.
         * @return the requested match.
         * @throws IndexOutOfBoundsException if the index was invalid.
         */
        public SearchMatch getMatch(int index);
    }

    //---------------------------------------------------------------------
    /**
     * <p>SearchMatch is a refinement of {@link BasicSearchMatch} returned
     * by {@link OneListSearch}.</p>
     * 
     * <p>Planned additions for coming sprints:
     * <ol><li>POI-info for a match as included with search reply.</li>
     *     <li>Some kind of get-more-matches-support.</li>
     * </ol></p>
     */
    public static interface SearchMatch
        extends BasicSearchMatch {
        
        /**
         * Returns the name of the image associated with the category
         * of this search match. Please note
         * that the image name will not have a type suffix attached.
         * 
         * @return The image name as a String. May return the empty string (if
         * the match has no specific category), but never null.
         * @see com.wayfinder.core.wfserver.resource.ResourceRequest
         */
        public String getMatchCategoryImageName();

        /**
         * Returns the name of the image associated with the brand
         * of this search match. Please note
         * that the image name will not have a type suffix attached.
         * 
         * The brand icon is only present for well-known brands (such as
         * Mc Donald's) or where brand owners have paid to make their
         * brand logotype show up instead of the generic icon for the
         * category.
         * 
         * @return The image name as a String. May return the empty string (if
         * the match has no specific brand), but never null.
         * @see com.wayfinder.core.wfserver.resource.ResourceRequest
         */
        public String getMatchBrandImageName();
    
        /**
         * Returns the name of the image associated with the provider
         * (data source) of this search match. Please note
         * that the image name will not have a type suffix attached.
         * 
         * @return The image name as a String. May return the empty string (if
         * the match's data source has not paid for being visualized),
         * but never null.
         * @see com.wayfinder.core.wfserver.resource.ResourceRequest
         */
        public String getMatchProviderImageName();

        /**
         * <p>Returns a filtered list of extra information (such as opening
         * hours) for this search match.</p>
         * 
         * <p>The filtering is defined by the server. It is possible to
         * find out if more information is available from {@link #additionalInfoExists()}.
         * If there is, you can get extra info through
         * {@link OneListSearch#requestDetails(SearchMatch, MatchDetailsRequestListener)} 
         * and passing in this SearchMatch as argument. 
         * This will be improved in a future release.</p>
         * 
         * @return a {@link PoiDetail}, possibly empty. Never returns null.
         */
        public PoiDetail getFilteredInfo();
        
        /**
         * Indicates if there are other details on this search match that can
         * be obtained with {@link OneListSearch#requestDetails(SearchMatch, MatchDetailsRequestListener)}
         * @return
         */
        public boolean additionalInfoExists();
    }


    //---------------------------------------------------------------------
    // methods in this interface.

    /**
     * Get the list of search matches.
     * 
     * @return a reference to the {@link MatchList}.
     */
    public MatchList getMatchList();
}
