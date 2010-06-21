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

import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchReply;

//DO NOT REMOVE - these imports are needed for javadoc tool.
//It doesn't work to just import com.wayfinder.core.search.SearchReply
//(regardless on if you try to link with "MatchList"
//or "SearchReply.MatchList").
//Tested with JDK 1.6.0_17.
import com.wayfinder.core.search.SearchReply.SearchMatch;
import com.wayfinder.core.search.SearchReply.MatchList;


/**
 * Represents a reply from a search done through {@link ProviderSearch}
 * <p>
 * This type of reply will contain multiple {@link MatchList} collections, e.g.
 * one {@link MatchList} per {@link SearchProvider}.
 */
public interface ProviderSearchReply extends SearchReply {
    
    
    /**
     * Returns the number of {@link MatchList} returned in the search. Once
     * this number has been obtained, use {@link #getMatchList(int)} to
     * extract the individual headings from the reply
     * 
     * @return The number of headings returned in the search
     */
    public int getNbrOfMatchLists();

    
    /**
     * Returns a heading from the reply.
     * 
     * @param index The index of the heading to obtain.
     * @return The {@link MatchList} for the specified index
     * @throws IndexOutOfBoundsException If the index is greater than the
     * number returned from getNbrOfHeadings()
     * @throws NegativeArraySizeException If the index is lower than zero
     */
    public MatchList getMatchList(int index);
    
    
    /**
     * Returns the {@link SearchProvider} from whom the list originated
     * 
     * @param index The index of the heading
     * @return The {@link SearchProvider} for the specified index
     * @throws IndexOutOfBoundsException If the index is greater than the
     * number returned from getNbrOfHeadings()
     * @throws NegativeArraySizeException If the index is lower than zero
     */
    public SearchProvider getProviderOfList(int index);

}
