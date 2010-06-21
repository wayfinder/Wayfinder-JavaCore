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
/**
 * 
 */
package com.wayfinder.core.search.internal;

import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchReply.SearchArea;


final class SearchAreaImpl implements SearchArea {
    
    /**
     * SearchArea of type municipal.
     */
    static final int MUNICIPAL = 0; 


    /**
     * SearchArea of type city.
     */
    static final int CITY = 1; 


    /**
     * SearchArea of type citypart.
     */
    static final int CITYPART = 2; 


    /**
     * SearchArea of type other.
     */
    static final int OTHER = 3;


    private final String m_ID;
    private final int m_type;
    private final String m_name;
    private final String m_location;
    private final SearchProvider m_provider;
    
    
    SearchAreaImpl(String id, int type, String name, 
            String location, SearchProvider provider) {
        m_ID = id;
        m_type = type;
        m_name = name;
        m_location = location;
        m_provider = provider;
    }


    public String getAreaImageName() {
        return m_provider.getProviderImageName();
    }


    public String getAreaLocation() {
        return m_location;
    }


    public String getAreaName() {
        return m_name;
    }
    
    
    public SearchProvider getSearchProvider() {
        return m_provider;
    };


    String getAreaID() {
        return m_ID;
    }


    int getAreaType() {
        return m_type;
    }
    
}
