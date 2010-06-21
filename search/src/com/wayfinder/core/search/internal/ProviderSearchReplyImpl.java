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

import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.search.provider.ProviderSearchReply;

class ProviderSearchReplyImpl 
extends SearchReplyImpl 
implements ProviderSearchReply {

    private final MatchListImpl[] m_listArray;
    private final Provider[] m_providerArray;
    
    
    ProviderSearchReplyImpl(SearchQuery query, MatchListImpl[] listArray, Provider[] providerArray) {
        super(query, TYPE_PROVIDER);
        m_listArray = listArray;
        m_providerArray = providerArray;
    }
    
    
    // for combining several rounds
    ProviderSearchReplyImpl(ProviderSearchReplyImpl reply, MatchListImpl[] newListArray, Provider[] newProviderArray) {
        this(reply.getOriginalSearchQuery(), combineLists(reply.m_listArray, newListArray), combineProviders(reply.m_providerArray, newProviderArray));
    }
    
    
    // for extending a single list
    ProviderSearchReplyImpl(ProviderSearchReplyImpl reply, int replaceIndex, MatchListImpl newList) {
        super(reply.getOriginalSearchQuery(), TYPE_PROVIDER);
        m_listArray = new MatchListImpl[reply.m_listArray.length];
        System.arraycopy(reply.m_listArray, 0, m_listArray, 0, m_listArray.length);
        m_listArray[replaceIndex] = newList;
        m_providerArray = reply.m_providerArray;
    }
    
    
    
    private static Provider[] combineProviders(Provider[] arr1, Provider[] arr2) {
        Provider[] newArray = new Provider[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, newArray, 0, arr1.length);
        System.arraycopy(arr2, 0, newArray, arr1.length, arr2.length);
        return newArray;
    }
    
    
    public MatchList getMatchList(int index) {
        if(index < 0 || index >= m_listArray.length) {
            throw new IllegalArgumentException("Heading index is out of bounds");
        }
        return m_listArray[index];
    }
    

    public int getNbrOfMatchLists() {
        return m_listArray.length;
    }
    

    public SearchProvider getProviderOfList(int index) {
        if(index < 0 || index >= m_providerArray.length) {
            throw new IllegalArgumentException("Heading index is out of bounds");
        }
        return m_providerArray[index];
    }
    
    
    boolean containsMatches() {
        for (int i = 0; i < m_listArray.length; i++) {
            if(m_listArray[i].containsMatches()) {
                return true;
            }
        }
        return false;
    }
}
