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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.search;

import com.wayfinder.core.search.internal.categorytree.HierarchicalCategoryImpl;
import com.wayfinder.core.search.internal.topregion.TopRegionImplTest;
import com.wayfinder.core.shared.Position;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class SearchQueryTest extends TestCase {
    
    /**
     * 
     */
    private static final int MAX_NBR_ADDRESS_MATCHES = 15;
    private Category m_category;
    private TopRegion m_topRegion;
    private Position m_position;
    
    /**
     * @param name
     */
    public SearchQueryTest(String name) {
        super(name);
        m_category = new HierarchicalCategoryImpl("Hotel", "tat_hotel", 118, 0);
        m_topRegion = TopRegionImplTest.TOP_REGION_SWEDEN;
        m_position = Position.createFromDecimalDegrees(55.718197, 13.190884);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateAddressGeocodingQuery() {
        SearchQuery query = SearchQuery.createAddressGeocodingQuery(
                "Nobelvagen 15", "Malmo", m_topRegion, MAX_NBR_ADDRESS_MATCHES);
        assertEquals(SearchQuery.SEARCH_TYPE_ADDRESS, query.getQueryType());
        assertEquals("Nobelvagen 15", query.getItemQueryStr());
        assertEquals("Malmo", query.getSearchAreaStr());
        assertEquals(TopRegionImplTest.TOP_REGION_SWEDEN, query.getTopRegion());
        assertFalse(query.includeDetails());
        assertNull(query.getCategory());
    }
    
    public void testCreatePositionalQuery() {
        SearchQuery query = SearchQuery.createPositionalQuery(
                "khan", m_category, m_position, 5000, true);
        assertEquals(SearchQuery.SEARCH_TYPE_POSITIONAL, query.getQueryType());
        assertEquals("khan", query.getItemQueryStr());
        assertEquals(m_category.getCategoryID(), query.getCategory().getCategoryID());
        assertNotNull(query.getPosition());
        assertEquals(5000, query.getSearchRadius());
        assertTrue(query.includeDetails());
    }
    
    public void testAddressQueryExceptions() {
        SearchQuery query = null;
        try {
            query = SearchQuery.createAddressGeocodingQuery(
                    "Nobelvagen 15", "Malmo", null, MAX_NBR_ADDRESS_MATCHES);
            fail("TopRegion is null!");
        }
        catch (IllegalArgumentException e) {
            // ok
        }

        query = SearchQuery.createPositionalQuery(
                "khan", m_category, m_position, 5000, true);
        try {
            query.getTopRegion();
            fail("not a SEARCH_TYPE_ADDRESS query!");
        }
        catch (IllegalStateException e) {
            //ok
        }
    }
    
    public void testPositionalQueryExceptions() {
        SearchQuery query = null;
        try {
            query = SearchQuery.createPositionalQuery("khan", m_category, m_position, 0, false);
            fail("A 0m radius should not be accepted!");
        }
        catch (IllegalArgumentException e) {
            //ok
        }
        
        try {
            query = SearchQuery.createPositionalQuery("khan", m_category, null, 5000, false);
            fail("Position cannot be null");
        }
        catch (IllegalArgumentException e) {
            //ok
        }
        
        try {
            query = SearchQuery.createPositionalQuery("khan", m_category, Position.NO_POSITION, 5000, false);
            fail("Position must be valid");
        }
        catch (IllegalArgumentException e) {
            //ok
        }
        
        query = SearchQuery.createAddressGeocodingQuery(
                "Nobelvagen 15", "Malmo", m_topRegion, MAX_NBR_ADDRESS_MATCHES);
        try {
            query.getPosition();
            fail("not a SEARCH_TYPE_POSITIONAL query");
        }
        catch (IllegalStateException e) {
            //ok
        }

    }
}
