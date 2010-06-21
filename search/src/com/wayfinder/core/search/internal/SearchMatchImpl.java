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


import java.io.IOException;

import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchReply.SearchMatch;
import com.wayfinder.core.shared.Position;

/**
 * <p>Internal representation of search matches from provider search.</p>
 * 
 * <p>This class is thread safe.</p>
 */
final class SearchMatchImpl
    extends BasicSearchMatchImpl
    implements SearchMatch, MC2WritableElement {
    
    private final String m_matchImageName;
    private final SearchProvider m_provider;


    /**
     * Constructs a new instance.
     * 
     * @param matchID value for {@link #getMatchID()}.
     * @param matchName value for {@link #getMatchName()}.
     * @param matchImageName value for {@link #getMatchImageName()}.
     * @param matchLocation value for {@link #getMatchLocation()}.
     * @param position value for {@link #getPosition()}.
     * @param provider value for {@link #getSearchProvider()}.
     */
    SearchMatchImpl(String matchID,
                    String matchName, 
                    String matchImageName,
                    String matchLocation, 
                    Position position,
                    SearchProvider provider) {
        
        super(matchID, matchLocation, matchName, position);
        m_matchImageName = matchImageName;
        m_provider = provider;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchMatch#getMatchImageName()
     */
    public String getMatchImageName() {
        return m_matchImageName;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.SearchReply.SearchMatch#getSearchProvider()
     */
    public SearchProvider getSearchProvider() {
        return m_provider;
    }
    
    // -------------------------------------------------------------------
    // MC2WritableElement
    public void write(MC2Writer mc2w) throws IOException {

        mc2w.startElement(MC2Strings.tsearch_item);
        // server only searches by itemid and doesn't care about
        // the real type
        mc2w.attribute(MC2Strings.asearch_item_type, MC2Strings.STREET_STRING);
        mc2w.elementWithText(MC2Strings.tname, MC2Strings.EMPTY_STRING);
        mc2w.elementWithText(MC2Strings.titemid, getMatchID());
        mc2w.endElement(MC2Strings.tsearch_item);
    }


}
