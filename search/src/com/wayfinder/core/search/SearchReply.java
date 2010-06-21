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
package com.wayfinder.core.search;

import com.wayfinder.core.search.provider.ProviderSearch;
import com.wayfinder.core.search.provider.ProviderSearchReply;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.route.RoutePointRequestable;
import com.wayfinder.core.wfserver.resource.CachedResourceManager;


/**
 * This is the base class for all search replies. Each search type will extend
 * this interface to provide more detailed information.
 */
public interface SearchReply {
    
    /**
     * Signifies that this reply is actually a {@link ProviderSearchReply}
     */
    public static final int TYPE_PROVIDER = 0;
    
    // Do not re-use this number. Was used for MergedSearchReply.
    // public static final int TYPE_MERGED   = 1;
    
    /**
     * Signifies that this reply is actually a
     * {@link com.wayfinder.core.search.onelist.OneListSearchReply}.
     */
    public static final int TYPE_ONELIST   = 2;

    
    /**
     * Returns the type of the reply
     * 
     * @return One of the TYPE_* constants in this interface
     */
    public int getReplyType();

    
    /**
     * Returns a reference to the original search query
     * 
     * @return A {@link SearchQuery} object
     */
    public SearchQuery getOriginalSearchQuery();

    
    // --------------------------------------------------------------------
    /**
     * <p>Represents the a list of matches found by {@link ProviderSearch}.</p>
     * 
     * <p>This name and placement of this interface stems from legacy reasons,
     * and requirements for backward compatibility.</p>
     * 
     * <p>Each {@link MatchList} contains three types of data:
     * <ol>
     * <li>Top matches, also known as sponsored matches. These are matches that
     *     other companies has payed money to ensure that they are placed at the
     *     top of the list of results, or are otherwise made more visible to the
     *     end user. These can be retrieve by the methods:
     *     <ul>
     *     <li>{@link #getNbrOfTopMatches()} - retrieves the number
     *         of top matches returned from the server.</li>
     *     <li>{@link #getTopMatch(int)} - retrieves a {@link SearchMatch}
     *         containing a sponsored match</li>
     *     <li>{@link #getTextBeforeTopMatches()} - retrieves a text that
     *         should be shown before the list of top matches</li>
     *     </ul>
     * </li>
     * <li>Regular matches. These are standard run of the mill matches found
     *     by the server in the databases. These can be retrieve by the methods:
     *     <ul>
     *     <li>{@link #getNbrOfRegularMatches()} - retrieves the number
     *         of matches returned from the server.</li>
     *     <li>{@link #getRegularMatch(int)}- retrieves a {@link SearchMatch}
     *         containing a match</li>
     *     <li>{@link #getTextBeforeRegularMatches()} - retrieves a text that
     *         should be shown before the list of matches</li>
     *     </ul>
     * </li>
     * <li>Search areas. Some providers return areas as a response to a
     *     search query. These represent things like city parts and similar
     *     that can be used to further narrow down the search. These should be
     *     used together with the 
     *     {@link SearchQuery#createAreaQuery(SearchQuery, SearchReply.SearchArea)} method
     *     to reissue the narrowed query to the server. Search areas can be
     *     retrieved with the methods:
     *     <ul>
     *     <li>{@link #getNbrOfSearchAreas()} - retrieves the number
     *         of areas returned from the server.</li>
     *     <li>{@link #getSearchArea(int)}- retrieves a {@link SearchArea}
     *         containing an area</li>
     *     </ul>
     * </ol></p>
     * 
     */
    public static interface MatchList {

        /**
         * Returns the total number of matches found on the server. Note that
         * in many cases this can be greater than the number of downloaded
         * searches since the total number can be several thousands.
         * 
         * @return The total number of matches found on the server
         */
        public int getTotalNbrOfMatches();

        
        //---------------------------------------------------------------------
        // top hits - also known as sponsored hits
        
        
        /**
         * Returns the amount of top hits in the reply. The application using
         * the reply should take care to display these matches before any of
         * the regular matches.
         * 
         * @return The number of top hits in the reply
         */
        public int getNbrOfTopMatches();

        
        /**
         * Returns a specific top hit match from the heading
         * 
         * @param index The index of the match
         * @return The {@link SearchMatch} at the index
         */
        public SearchMatch getTopMatch(int index);

        
        /**
         * Returns the text to be shown before the sponsored matches.
         * <p>
         * 
         * @return The text as a String or null
         */
        public String getTextBeforeTopMatches();
        
        
        //---------------------------------------------------------------------
        // regular hits
        

        /**
         * Returns the amount of regular matches in the reply.
         * 
         * @return The number of regular matches in the reply
         */
        public int getNbrOfRegularMatches();

        
        /**
         * Returns a specific regular match from the heading
         * 
         * @param index The index of the match
         * @return The {@link SearchMatch} at the index
         */
        public SearchMatch getRegularMatch(int index);

        
        /**
         * Returns the text to be shown before the regular matches.
         * <p>
         * 
         * @return The text as a String or null
         */
        public String getTextBeforeRegularMatches();

        
        //---------------------------------------------------------------------
        // search areas
        
        
        /**
         * Returns the amount of search areas in the reply.
         * 
         * @return The number of search areas in the reply
         */
        public int getNbrOfSearchAreas();

        
        /**
         * Returns a specific search area from the heading
         * 
         * @param index The index of the match
         * @return the {@link SearchArea} at the index
         */
        public SearchArea getSearchArea(int index);

    }
    
    
    // --------------------------------------------------------------------
    /**
     * Represents the basic data about an match found
     * with the SearchQuery used.
     * 
     * This interface is then further refined by the different type of
     * searches.
     * 
     * BasicSearchMatch implements the {@link RoutePointRequestable}
     * interface and can be used as origin and destination when requesting
     * a route.
     */
    public static interface BasicSearchMatch
        extends RoutePointRequestable {

        /**
         * Returns the ID of the match.
         * 
         * The ID is not static over time and will change with at least
         * every update of the maps at the server. Only use this ID in the
         * current session. Do not save it or try to use it to cross-reference
         * search matches with favorites.
         * 
         * @return The ID of the match. Never returns null.
         */
        public String getMatchID();

        /**
         * Returns the name of the match.
         * 
         * @return The name as a String. Never returns null.
         */
        public String getMatchName();

        /**
         * Returns a textual representation location of the match suitable
         * for display to user.
         * 
         * The server will construct this from city part and city name and
         * sometimes other data. There are no guarantees on the number of
         * elements used or level of detail.
         * 
         * @return The location as a String. Never returns null.
         */
        public String getMatchLocation();
    
        /**
         * Returns the position of the match
         * 
         * @return A {@link Position} object containing the coordinates of the
         * match
         */
        public Position getPosition();
    }
    

    /**
     * SearchMatch is a refinement of {@link BasicSearchMatch} returned
     * by {@link ProviderSearch}.
     * 
     * This name and placement of this interface stems from legacy reasons,
     * and requirements for backward compatibility.
     */
    public static interface SearchMatch extends BasicSearchMatch { 
        
        /**
         * Returns the {@link SearchProvider} of the match
         * 
         * @return A {@link SearchProvider}
         */
        public SearchProvider getSearchProvider();
        
        /**
         * Returns the name of the image representing the match.
         * <p>
         * This can be used with the {@link CachedResourceManager} to obtain
         * the image from the server.
         * 
         * @return The image name as a String
         */
        public String getMatchImageName();
    }
    
    
    // --------------------------------------------------------------------
    /**
     * Represents an area in the work that has been returned by a search
     * provider as a response to a query. 
     * <p>
     * This area can be used together with
     * {@link SearchQuery#createAreaQuery(SearchQuery, SearchReply.SearchArea)}
     * to narrow down the search.
     */
    public static interface SearchArea {
        
        /**
         * Returns the {@link SearchProvider} of the area
         * 
         * @return A {@link SearchProvider}
         */
        public SearchProvider getSearchProvider();
        
        
        /**
         * Returns the name of the area
         * 
         * @return The name as a String
         */
        public String getAreaName();
        
        
        /**
         * Returns the name of the image representing the area
         * <p>
         * This can be used with the {@link CachedResourceManager} to obtain
         * the image from the server.
         * 
         * @return The image name as a String
         */
        public String getAreaImageName();

        
        /**
         * Returns the location of the area
         * 
         * @return The location as a String
         */
        public String getAreaLocation();

    }

}
