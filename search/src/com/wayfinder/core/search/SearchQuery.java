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

import com.wayfinder.core.search.SearchReply.SearchArea;
import com.wayfinder.core.search.internal.SearchModule;
import com.wayfinder.core.search.internal.category.CategoryHolder;
import com.wayfinder.core.search.internal.categorytree.CategoryTreeHolder;
import com.wayfinder.core.search.internal.topregion.TopRegionHolder;
import com.wayfinder.core.shared.Position;



/**
 * Represents a query made towards the server
 */
public final class SearchQuery {
    
    
    /**
     * Signifies that the search is of regional type. Search results will be
     * related to a specific region in the world.
     */
    public static final int SEARCH_TYPE_REGIONAL    = 0;

    
    /**
     * Search results returned by this query type will be concentrated around
     * the position sent as a parameter and sorted by distance to the position.
     */
    public static final int SEARCH_TYPE_POSITIONAL  = 1;
    
    
    /**
     * Search results returned by this query type will be related to a specific 
     * area in the world.
     */
    public static final int SEARCH_TYPE_AREA        = 2;
    
    /**
     * This query type is intended for (forward) geocoding, i.e. getting the
     * coordinates for a certain address. Quite similar to a {@link #SEARCH_TYPE_REGIONAL}
     * query, but it never uses a category.
     */
    public static final int SEARCH_TYPE_ADDRESS     = 3;
    
    /**
     * Signifies that the default value for radius on the server should be
     * used
     * @see #createPositionalQuery(String, Category, Position, int, boolean)
     * @see #getSearchRadius()
     */
    public static final int RADIUS_SERVER_DEFAULT = Integer.MIN_VALUE;
    
    private static final int MAX_NBR_MATCHES_DEFAULT = 100;
    

    // always allocated
    private final String m_itemQueryStr; // also known as the "what" string
    private final Category m_category;
    private final int m_queryType;
    private final boolean m_includeDetails;
    private final int m_maxNbrMatches;
    
    // for regional searches
    private final String m_searchAreaStr; // also known as the "where" string
    private final TopRegion m_topRegion;
    
    // for position based searches
    private final Position m_searchPosition;
    private final int m_searchRadius;
    
    // for search area searches
    private final SearchArea m_searchArea;
    
    
    //-------------------------------------------------------------------------
    // constructors and factory
    
    
    /**
     * Creates a new query for regional search.
     * <p>
     * Search results returned by this query type will be related to a specific 
     * region in the world and may be even more narrowed down by the use of a 
     * "where" string. When returned, results will be sorted alphabetically.
     * <p>
     * Please note that the object passed as {@link Category} and {@link TopRegion}
     * MUST be from the collections obtained using the {@link SearchInterface}
     * methods. If a foreign implementation of the interfaces is passed as
     * parameters, an {@link IllegalArgumentException} will be thrown.
     * 
     * @param itemQueryStr a String specifying a term to search for. If null no
     *                     filtering of name will be done
     * @param category a {@link Category} object, may be null if no specific 
     *                 {@link Category} is wanted
     * @param searchAreaStr a String specifying an area to search in, for 
     *                      example a city name 
     * @param topRegion The {@link TopRegion} for the country or state the search should 
     *                  take place in. <b>May not be null</b>
     * @throws IllegalArgumentException If a foreign implementation of 
     *                                  {@link Category} or {@link TopRegion} 
     *                                  is passed as parameters
     */
    public static SearchQuery createRegionalQuery(
            String itemQueryStr, Category category, 
            String searchAreaStr, TopRegion topRegion) {
        
        return new SearchQuery(itemQueryStr, category, 
                SEARCH_TYPE_REGIONAL, 
                false, 
                searchAreaStr, 
                topRegion, null, 
                RADIUS_SERVER_DEFAULT, null);
    }
    
    
    /**
     * @deprecated 
     * Use {@link #createPositionalQuery(String, Category, Position, int, boolean)}
     * instead.
     */
    public static SearchQuery createPositionalQuery(
            String itemQueryStr, Category category, 
            Position position, int radius) {
                return createPositionalQuery(itemQueryStr, category, position,
                        radius, false);
            }


    /**
     * Creates a new query for a positional search.
     * <p>
     * Search results returned by this query type will be concentrated around
     * the position sent as a parameter and sorted by distance to the position.
     * <p>
     * Please note that the object passed as {@link Category}
     * MUST be from the collections obtained using the {@link SearchInterface}
     * methods. If a foreign implementation of the interfaces is passed as
     * parameters, an {@link IllegalArgumentException} will be thrown.
     * 
     * @param itemQueryStr a String specifying a term to search for. If null no
     *                     filtering of name will be done
     * @param category a {@link Category} object, may be null if no specific 
     *                 {@link Category} is wanted
     * @param position a {@link Position} for use as the base of the search
     * @param radius the radius in meters to search or 
     *               {@link #RADIUS_SERVER_DEFAULT} to use the default value on 
     *               the server.
     * @param includeDetails indicates if POI details should be included with the
     * search matches straight in the reply
     * in the search reply
     */
    public static SearchQuery createPositionalQuery(
            String itemQueryStr, Category category, 
            Position position, int radius, boolean includeDetails) {
        
        return new SearchQuery(itemQueryStr, category, 
                SEARCH_TYPE_POSITIONAL, 
                includeDetails, 
                null, 
                null, position, 
                radius, null);
    }
    
    
    /**
     * Creates a new query for area search.
     * <p>
     * This query type is intended to be used when a previous query has returned
     * a number of {@link SearchArea} objects and is mainly used for narrowing
     * down the search.
     * <p>
     * Search results returned by this query type will be related to a specific 
     * area in the world.
     * 
     * @param query The SearchQuery object used to obtain the {@link SearchArea} 
     *              object
     * @param area The {@link SearchArea} object to conduct the query in
     */
    public static SearchQuery createAreaQuery(SearchQuery query, SearchArea area) {
        return new SearchQuery(query.m_itemQueryStr, 
                               query.m_category,
                               SEARCH_TYPE_AREA,
                               false,
                               query.m_searchAreaStr,
                               query.m_topRegion,
                               query.m_searchPosition,
                               query.m_searchRadius, area);
    }
    
    /**
     * Create a query for address search (geocoding).
     * 
     * @param streetStr Street name and number
     * @param cityOrZipStr City name or postal code (optional, but it helps)
     * @param tr {@link TopRegion} representing the country or region where to
     * search
     * @param maxNbrMatches the maximum number of matches to return, must be >0
     * 
     * @return a SearchQuery for address searching
     */
    public static SearchQuery createAddressGeocodingQuery(
            String streetStr, 
            String cityOrZipStr,
            TopRegion tr, 
            int maxNbrMatches) {
        return new SearchQuery(
                streetStr, // street and number
                null, // category not used
                SEARCH_TYPE_ADDRESS, 
                false, // no details
                cityOrZipStr, // city or zip code
                tr, // top region
                null, // no position, it's the position we're after
                RADIUS_SERVER_DEFAULT, // doesn't matter
                null, // neither does the search area
                maxNbrMatches); 
    }
    
    
    private SearchQuery(String whatStr, Category category,  // always
    int queryType, // decides the type
    boolean includeDetails, // how many details to include in a match straight from the reply
    String whereStr, // for SEARCH_TYPE_REGIONAL and SEARCH_TYPE_ADDRESS
    TopRegion topRegion, Position position, // for SEARCH_TYPE_POSITIONAL
    int radius, SearchArea area) {
        this(whatStr, category, queryType, includeDetails,
                whereStr, topRegion, position, radius,
                area, MAX_NBR_MATCHES_DEFAULT);
    }


    private SearchQuery(String whatStr, Category category,  // always
                        int queryType, // decides the type
                        boolean includeDetails, // how many details to include in a match straight from the reply
                        String whereStr, // for SEARCH_TYPE_REGIONAL and SEARCH_TYPE_ADDRESS
                        TopRegion topRegion, Position position, // for SEARCH_TYPE_POSITIONAL
                        int radius, SearchArea area, // for SEARCH_TYPE_AREA
                        int maxNbrMatches) { // 
        m_itemQueryStr = whatStr;
        m_category = category;
        m_queryType = queryType;
        m_includeDetails = includeDetails;
        m_searchAreaStr = whereStr;
        m_topRegion = topRegion;
        m_searchPosition = position;
        m_searchRadius = radius;
        m_searchArea = area;
        m_maxNbrMatches = maxNbrMatches;
        assertDataValid(this);
    }
    
    
    //-------------------------------------------------------------------------
    // methods for checking data consistency
    
    
    /**
     * Checks to see if the data currently set in the query is valid.
     * <p>
     * Validity depends on the type of query. 
     * 
     * General rules for all search queries:
     * <ul>
     * <li>The what string may not be null or the empty string</li>
     * <li>A {@link Category} is optional</li>
     * <li>If a {@link Category} is set, the object must originate from the
     *     search module</li>
     * </ul>
     * 
     * Additional demands per query type:
     * <ul>
     * <li>{@link #SEARCH_TYPE_REGIONAL}</li>
     * <li>{@link #SEARCH_TYPE_ADDRESS}</li>
     *     <ul>
     *     <li>The where parameter string is optional</li>
     *     <li>A {@link TopRegion} MUST be set in the query</li>
     *     <li>The set {@link TopRegion} MUST be of the internal implementation 
     *         in the search module</li>
     *     </ul>
     * <li>{@link #SEARCH_TYPE_POSITIONAL}</li>
     *     <ul>
     *     <li>The {@link Position} object passed as a parameter MUST contain a 
     *         valid position</li>
     *     <li>The radius parameter must either be set to 
     *         {@link #RADIUS_SERVER_DEFAULT} or a number greater than 0.</li>
     *     </ul>
     * <li>{@link #SEARCH_TYPE_AREA}</li>
     *     <ul>
     *     <li>A {@link SearchArea} object MUST be set in the query</li>
     *     <li>The set {@link SearchArea} MUST be of the internal implementation 
     *         in the search module</li>
     *     </ul>
     * </ul>
     * 
     * @param query The {@link SearchQuery} to inspect
     */
    private static void assertDataValid(SearchQuery query) {
        // ok with empty item query string - will mean 100% match in the end
        
        // category must be null or internal class
        if(query.m_category != null) {
            try {
                CategoryHolder.assertIsInternalCategory(query.m_category);
            } catch (IllegalArgumentException e) {
                // was not a CategoryImpl. Either it is a
                // HierarchicalCategory and the test below will pass.
                // Or it is something else and the test below will also
                // throw an exception.
                CategoryTreeHolder.assertIsInternalCategory(query.m_category);
            }
        }
        
        if (query.m_maxNbrMatches <= 0) {
            throw new IllegalArgumentException("maxNbrMatches must be greater than 0");
        }
        
        switch(query.m_queryType) {
        case SEARCH_TYPE_REGIONAL:
        case SEARCH_TYPE_ADDRESS:
            // need a valid top region, but not a where str
            if(query.m_topRegion == null) {
                throw new IllegalArgumentException(
                        "TopRegion must be specified for regional or address geocoding searches");
            } else {
                TopRegionHolder.assertIsInternalTopRegion(query.m_topRegion);
            }
            break;
            
        case SEARCH_TYPE_POSITIONAL:
            // need a valid position
            if(query.m_searchPosition == null) {
                throw new IllegalArgumentException("Position must be specified for positional searches");
            } else if(!query.m_searchPosition.isValid()) {
                throw new IllegalArgumentException("Invalid position specified");
            }
            // check radius
            if(query.m_searchRadius != RADIUS_SERVER_DEFAULT && query.m_searchRadius < 1) {
                throw new IllegalArgumentException("Specified search radius must be greater than 0");
            }
            
            break;
            
        case SEARCH_TYPE_AREA:
            if(query.m_searchArea == null) {
                throw new IllegalArgumentException("SearchArea must be specified for area searches");
            } else {
                SearchModule.assertIsInternalSearchArea(query.m_searchArea);
            }
            break;
            
        default:
            throw new IllegalArgumentException("Invalid search query type");
        }
    }
    
    //-------------------------------------------------------------------------
    // methods always available
    
    
    /**
     * Returns the type of query. The value returned from this call decides
     * what other methods will return valid values.
     * 
     * @return One of the SEARCH_TYPE_* constants in this class
     */
    public int getQueryType() {
        return m_queryType;
    }
    
    /**
     * Indicates if POI details should be included in the matches for this query. 
     * @return <code>true</code> or <code>false</code>
     */
    public boolean includeDetails() {
        return m_includeDetails;
    }
    
    
    /**
     * Returns the string to match search items against.
     * <p>
     * This is also known as the "what" string
     * 
     * @return The item query as a string or null if nothing should be matched
     *         against the name
     */
    public String getItemQueryStr() {
        return m_itemQueryStr;
    }
    
    
    /**
     * Returns the {@link Category} to match search items against.
     * 
     * @return The {@link Category} if set or <code>null</code> if no specific
     * {@link Category} should be used when matching items.
     */
    public Category getCategory() {
        return m_category;
    }
    
    /**
     * Returns the max number of matches for this query.
     * @return the max number of matches
     */
    public int getMaxNbrMatches() {
        return m_maxNbrMatches;
    }
    
    
    //-------------------------------------------------------------------------
    // methods only available for regional searches (including ADDRESS searches)
    
    
    /**
     * Returns the string representing the city or area to conduct the search
     * in. This is also known as the "where" string.
     * <p>
     * <b>This method is only available for queries of type 
     * {@link #SEARCH_TYPE_REGIONAL} and {@link #SEARCH_TYPE_ADDRESS}</b>
     * 
     * @return The search area query string (also known as the where string)
     * @throws IllegalStateException If the query is not of type 
     * {@link #SEARCH_TYPE_REGIONAL} or {@link #SEARCH_TYPE_ADDRESS}
     * @see #SEARCH_TYPE_REGIONAL
     * @see #SEARCH_TYPE_ADDRESS
     * @see #getQueryType()
     * @see #createRegionalQuery(String, Category, String, TopRegion)
     * @see #createAddressGeocodingQuery(String, String, TopRegion, int)
     */
    public String getSearchAreaStr() {
        assertIsRegionalSearch();
        return m_searchAreaStr;
    }
    
    
    /**
     * Returns the TopRegion representing the region the search will be
     * carried out in.
     * <p>
     * <b>This method is only available for queries of type 
     * {@link #SEARCH_TYPE_REGIONAL} and {@link #SEARCH_TYPE_ADDRESS} </b>
     * 
     * @return The TopRegion for the queried region.
     * @throws IllegalStateException If the query is not of type 
     * {@link #SEARCH_TYPE_REGIONAL} or {@link #SEARCH_TYPE_ADDRESS}
     * @see #SEARCH_TYPE_REGIONAL
     * @see #SEARCH_TYPE_ADDRESS
     * @see #getQueryType()
     * @see #createRegionalQuery(String, Category, String, TopRegion)
     * @see #createAddressGeocodingQuery(String, String, TopRegion, int)
     */
    public TopRegion getTopRegion() {
        assertIsRegionalSearch();
        return m_topRegion;
    }
    
    
    private void assertIsRegionalSearch() {
        if(m_queryType != SEARCH_TYPE_REGIONAL 
                && m_queryType != SEARCH_TYPE_ADDRESS) {
            throw new IllegalStateException("Query type is not regional or address");
        }
    }
    
    
    //-------------------------------------------------------------------------
    // methods only available for position based searches
    
    
    /**
     * Returns the {@link Position} to be used as the center for a positional
     * search.
     * <b>This method is only available for queries of type 
     * {@link #SEARCH_TYPE_POSITIONAL}</b>
     * 
     * @return The {@link Position} to base the search on
     * @throws IllegalStateException If the query is not of type {@link #SEARCH_TYPE_POSITIONAL}
     * @see #SEARCH_TYPE_POSITIONAL
     * @see #getQueryType()
     * @see #createPositionalQuery(String, Category, Position, int, boolean)
     */
    public Position getPosition() {
        assertIsPositionSearch();
        return m_searchPosition;
    }
    
    
    /**
     * Returns the radius around the {@link Position} returned from the 
     * {@link #getPosition()} method to search in.
     * <p>
     * This value is optional will return {@link #RADIUS_SERVER_DEFAULT} if
     * no specific radius has been set.
     * <p>
     * <b>This method is only available for queries of type 
     * {@link #SEARCH_TYPE_POSITIONAL}</b>
     * 
     * @return The radius in meters or {@link #RADIUS_SERVER_DEFAULT} if no
     * specific radius has been set.
     * @throws IllegalStateException If the query is not of type {@link #SEARCH_TYPE_POSITIONAL}
     * @see #SEARCH_TYPE_POSITIONAL
     * @see #getQueryType()
     * @see #createPositionalQuery(String, Category, Position, int, boolean)
     */
    public int getSearchRadius() {
        assertIsPositionSearch();
        return m_searchRadius;
    }
    
    
    
    private void assertIsPositionSearch() {
        if(m_queryType != SEARCH_TYPE_POSITIONAL) {
            throw new IllegalStateException("Query type is not position");
        }
    }
    
    
    //-------------------------------------------------------------------------
    // methods only available for area searches
    
    
    /**
     * Returns the {@link SearchArea} to conduct the search in.
     * <p>
     * <b>This method is only available for queries of type 
     * {@link #SEARCH_TYPE_AREA}</b>
     * 
     * @return the {@link SearchArea} to search in
     * @throws IllegalStateException If the query is not of type {@link #SEARCH_TYPE_AREA}
     * @see #SEARCH_TYPE_AREA
     * @see #getQueryType()
     * @see #createAreaQuery(SearchQuery, SearchReply.SearchArea)
     */
    public SearchArea getSearchArea() {
        assertIsAreaSearch();
        return m_searchArea;
    }
    
    
    private void assertIsAreaSearch() {
        if(m_queryType != SEARCH_TYPE_AREA) {
            throw new IllegalStateException("Query type is not area");
        }
    }
}
