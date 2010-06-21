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

import com.wayfinder.core.search.SearchReply.SearchArea;
import com.wayfinder.core.search.SearchReply.MatchList;
import com.wayfinder.core.search.SearchReply.SearchMatch;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.Comparator;
import com.wayfinder.core.shared.util.WFUtil;


final class MatchListImpl implements MatchList {
    
    private final SearchMatchImpl[] m_topMatchArray;
    private final SearchMatchImpl[] m_regularMatchArray;
    private final SearchAreaImpl[]  m_searchAreaArray;
    private final int m_totalHitsOnServer;
    private final String m_topMatchText;
    private final String m_regularMatchText;

    
    MatchListImpl(    SearchMatchImpl[] topMatchArray,
                      SearchMatchImpl[] regularMatchArray,
                      SearchAreaImpl[] searchAreaArray,
                      String topMatchText,
                      String regularMatchText,
                      int totalHits) {
        
        m_topMatchArray = topMatchArray;
        m_regularMatchArray = regularMatchArray;
        m_searchAreaArray = searchAreaArray;
        m_topMatchText = topMatchText;
        m_regularMatchText = regularMatchText;
        m_totalHitsOnServer = totalHits;
    }
    

    MatchListImpl(    
            MatchListImpl oldList,
            SearchMatchImpl[] topMatchArray,
            SearchMatchImpl[] regularMatchArray,
            SearchAreaImpl[] searchAreaArray,
            int nbrOfMoreHitsOnServer) {

        this(combineMatches(oldList.m_topMatchArray, topMatchArray),
             combineMatches(oldList.m_regularMatchArray, regularMatchArray),
             combineAreas(oldList.m_searchAreaArray, searchAreaArray),
             oldList.m_topMatchText,
             oldList.m_regularMatchText,
             oldList.m_totalHitsOnServer + nbrOfMoreHitsOnServer);
    }
    
    
    MatchListImpl(MatchListImpl list1, MatchListImpl list2) {
        this(list1, list2.m_topMatchArray, list2.m_regularMatchArray, list2.m_searchAreaArray, list2.m_totalHitsOnServer);
    }
    
    
    
    private final static SearchMatchImpl[] combineMatches(SearchMatchImpl[] arr1, SearchMatchImpl[] arr2) {
        SearchMatchImpl[] newArray = new SearchMatchImpl[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, newArray, 0, arr1.length);
        System.arraycopy(arr2, 0, newArray, arr1.length, arr2.length);
        return newArray;
    }
    
    
    private final static SearchAreaImpl[] combineAreas(SearchAreaImpl[] arr1, SearchAreaImpl[] arr2) {
        SearchAreaImpl[] newArray = new SearchAreaImpl[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, newArray, 0, arr1.length);
        System.arraycopy(arr2, 0, newArray, arr1.length, arr2.length);
        return newArray;
    }
    
    
    //---------------------------------------------------------------------
    // general info regarding the heading
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getTotalNbrOfMatches()
     */
    public int getTotalNbrOfMatches() {
        return m_totalHitsOnServer;
    }
    
    
    //---------------------------------------------------------------------
    // top hits - also known as sponsored hits
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getNbrOfTopMatches()
     */
    public int getNbrOfTopMatches() {
        return m_topMatchArray.length;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getTopMatch(int)
     */
    public SearchMatch getTopMatch(int index) {
        return m_topMatchArray[index];
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getTextBeforeTopMatches()
     */
    public String getTextBeforeTopMatches() {
        return m_topMatchText;
    }
    
    
    //---------------------------------------------------------------------
    // regular hits
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getNbrOfRegularMatches()
     */
    public int getNbrOfRegularMatches() {
        return m_regularMatchArray.length;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getRegularMatch(int)
     */
    public SearchMatch getRegularMatch(int index) {
        return m_regularMatchArray[index];
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getTextBeforeRegularMatches()
     */
    public String getTextBeforeRegularMatches() {
        return m_regularMatchText;
    }
    
    
    //---------------------------------------------------------------------
    // search areas
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getNbrOfSearchAreas()
     */
    public int getNbrOfSearchAreas() {
        return m_searchAreaArray.length;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.internal.SearchHeading#getSearchArea(int)
     */
    public SearchArea getSearchArea(int index) {
        return m_searchAreaArray[index];
    }
    
    
    //-------------------------------------------------------------------------
    // Package internal
    
    boolean containsMatches() {
        return (m_topMatchArray.length + m_regularMatchArray.length) > 0; // omit areas
    }
    
    
    
    //-------------------------------------------------------------------------
    // Utils...
    // TODO: The methods working with BasicSearchMatchImpl[] are not
    // provider-search-specific and could be moved elsewhere.
    
    
    static void sortByDistance(MatchListImpl list, Position position) {
        Comparator comp = new BasicSearchMatchImpl.PositionDistanceComparator(position);
        WFUtil.insertionSort(list.m_topMatchArray, comp);
        WFUtil.insertionSort(list.m_regularMatchArray, comp);
    }
    
    
    static void sortByName(MatchListImpl list) {
        sortName(list.m_topMatchArray);
        sortName(list.m_regularMatchArray);
    }
    
    private static void sortName(BasicSearchMatchImpl[] matches) {
        for(int p=1; p < matches.length; p++) {
            BasicSearchMatchImpl tmp = matches[p];
            // System.out.println("outer loop " + p + " " + tmp);

            int j = p;
            for(; j > 0 && (tmp.getMatchName().compareTo(matches[j-1].getMatchName()) < 0); j--) {
                // System.out.println("inner loop " + j);
                matches[j] = matches[j-1];
            }
            matches[j] = tmp;
        }
    }
}
