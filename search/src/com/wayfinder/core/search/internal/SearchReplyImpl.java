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
package com.wayfinder.core.search.internal;

import com.wayfinder.core.search.SearchReply;
import com.wayfinder.core.search.SearchQuery;

abstract class SearchReplyImpl implements SearchReply {
    
    private final SearchQuery m_originalQuery;
    private final int m_replyType;
    
    /**
     * Creates a new instance.
     *
     * @param query the original search query. Must not be null.
     * @param type value for {@link #getReplyType()}.
     * Must be one of the declared type in {@link SearchReply}.
     */
    SearchReplyImpl(SearchQuery query, int type) {
        if (query == null
            || (type != TYPE_PROVIDER && type != TYPE_ONELIST)) {
            throw new IllegalArgumentException();
        }

        m_originalQuery = query;
        m_replyType = type;
    }
    
    
    //-------------------------------------------------------------------------
    // public methods
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchReply#getOriginalRequest()
     */
    public final SearchQuery getOriginalSearchQuery() {
        return m_originalQuery;
    }


    public final int getReplyType() {
        return m_replyType;
    }
    
    
    abstract boolean containsMatches();
    
    
    protected final static MatchListImpl[] combineLists(MatchListImpl[] arr1, MatchListImpl[] arr2) {
        MatchListImpl[] newArray = new MatchListImpl[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, newArray, 0, arr1.length);
        System.arraycopy(arr2, 0, newArray, arr1.length, arr2.length);
        return newArray;
    }

}
