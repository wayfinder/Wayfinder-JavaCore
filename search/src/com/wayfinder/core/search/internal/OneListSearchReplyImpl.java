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

import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.SearchReply;
import com.wayfinder.core.search.onelist.OneListSearchReply;
import com.wayfinder.core.search.onelist.OneListSearchReply.MatchList;


/**
 * <p>Container class to pass search reply data to the client.</p>
 * 
 * <p>The responsibility for reply merging and processing lies with
 * {@link OneListSearchRequest} and we let this class be just a
 * data container. The rationale for this is that it is better to collect
 * all the processing code in one place. A more pure OO solution might have
 * called for this class to be able to construct itself from a previous
 * reply and new data.</p>
 * 
 * <p>This class is thread-safe as long as the conditions on constructor
 * parameters are observed.</p>
 */
final class OneListSearchReplyImpl
    extends SearchReplyImpl
    implements OneListSearchReply, MatchList {

    private final int m_estimatedTotalNbrOfMatches;

    /**
     * The search matches. The contents of this array must not be changed.
     * It is package protected for convenience when merging the search
     * replies together.
     */
    final OneListSearchMatchImpl[] m_searchMatches;

    
    /**
     * @param query the original search query.
     * @param estimatedTotalNbrOfMatches value for
     * {@link #getEstimatedTotalNbrOfMatches()}
     * @param searchMatches will be stored in a final variable.
     * Must be sorted in the order they should appear to users of this
     * reply.
     * Must not be changed by other code after the constructor call.
     * If there were no matches, send and empty array.
     */
    OneListSearchReplyImpl(SearchQuery query,
                           int estimatedTotalNbrOfMatches,
                           OneListSearchMatchImpl[] searchMatches) {
        super(query, SearchReply.TYPE_ONELIST);
        if (estimatedTotalNbrOfMatches < 0 || searchMatches == null) {
            throw new IllegalArgumentException();
        }

        m_estimatedTotalNbrOfMatches = estimatedTotalNbrOfMatches;
        m_searchMatches = searchMatches;
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchReplyImpl#containsMatches()
     */
    boolean containsMatches() {
        return (m_searchMatches.length > 0);
    }

    /* (non-Javadoc)
     * @see OneListSearchReply#getMatchList()
     */
    public OneListSearchReply.MatchList getMatchList() {
        return this;
    }


    // -----------------------------------------------------------------
    // MatchList interface

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.MatchList#getEstimatedTotalNbrOfMatches()
     */
    public int getEstimatedTotalNbrOfMatches() {
        return m_estimatedTotalNbrOfMatches;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.MatchList#getMatch(int)
     */
    public OneListSearchReply.SearchMatch getMatch(int index) {
        return m_searchMatches[index];
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearchReply.MatchList#getNbrOfMatches()
     */
    public int getNbrOfMatches() {
        return m_searchMatches.length;
    }
}
