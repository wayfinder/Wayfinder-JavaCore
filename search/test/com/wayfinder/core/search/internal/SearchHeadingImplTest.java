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

import com.wayfinder.core.search.internal.MatchListImpl;
import com.wayfinder.core.search.internal.SearchMatchImpl;
import com.wayfinder.core.shared.Position;

import junit.framework.TestCase;

public class SearchHeadingImplTest extends TestCase {

    private static final String HEADING_NAME = "Eniro";
    private static final String HEADING_TYPE = "Personer";
    private static final String HEADING_IMG =  "btat_eniro";

    
    public void testSearchHeading() {
        MatchListImpl heading = new MatchListImpl(new SearchMatchImpl[0],
                                                  new SearchMatchImpl[0],
                                                  new SearchAreaImpl[0],
                                                  "top",
                                                  "regular",
                                                  0);
        
        assertEquals(0, heading.getNbrOfRegularMatches());
        assertEquals(0, heading.getTotalNbrOfMatches());
    }
    

    public void testGetMatch() {
        
        
        
        final int NBR_OF_MATCHES = 10;
        SearchMatchImpl[] matches = new SearchMatchImpl[NBR_OF_MATCHES];
        for (int i = 0; i < NBR_OF_MATCHES; i++) {
            final String nbrStr = Integer.toString(i);
            matches[i] = new SearchMatchImpl("1",nbrStr,
                                         "image",
                                         "Street" + nbrStr,
                                         new Position(0, 0), null);
        }
        
        MatchListImpl heading = new MatchListImpl(
                new SearchMatchImpl[0],
                matches,
                new SearchAreaImpl[0],
                "top",
                "regular",
                matches.length * 2);
        
        assertEquals(matches.length, heading.getNbrOfRegularMatches());
        assertEquals(matches.length * 2, heading.getTotalNbrOfMatches());
        
        for (int i = 0; i < heading.getNbrOfRegularMatches(); i++) {
            assertEquals(matches[i], heading.getRegularMatch(i));
        }
        
    }


}
