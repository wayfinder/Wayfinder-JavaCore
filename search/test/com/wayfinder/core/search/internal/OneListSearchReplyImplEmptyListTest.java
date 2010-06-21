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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.search.internal;

import com.wayfinder.core.search.onelist.OneListSearchReply;

import junit.framework.TestCase;

/**
 * Tests that {@link OneListSearchReplyImpl} behaves correct when there
 * were no search matches and it is initialized with an empty list.
 */
public class OneListSearchReplyImplEmptyListTest extends TestCase {

    private static final int ESTIMATED_TOTAL = 100; 

    private OneListSearchReplyImpl m_reply;
    private OneListSearchReply.MatchList m_matchList;


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        m_reply = new OneListSearchReplyImpl(
                OneListSearchReplyImplTest.QUERY,
                ESTIMATED_TOTAL,
                new OneListSearchMatchImpl[0]);
        m_matchList = m_reply.getMatchList();

    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchReplyImpl#containsMatches()}.
     */
    public final void testContainsMatches() {
        assertFalse(m_reply.containsMatches());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchReplyImpl#getMatchList()}.
     */
    public final void testGetMatchList() {
        assertNotNull(m_reply.getMatchList());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchReplyImpl#getEstimatedTotalNbrOfMatches()}.
     */
    public final void testGetEstimatedTotalNbrOfMatches() {
        assertEquals(ESTIMATED_TOTAL,
                     m_matchList.getEstimatedTotalNbrOfMatches());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchReplyImpl#getMatch(int)}.
     */
    public final void testGetMatch() {
        try {
            int index = 0;
            OneListSearchReply.SearchMatch match = m_matchList.getMatch(index);
            fail("getMatch(" + index + ") should have thrown an exception" +
                 " instead of returning " + match);
        } catch (IndexOutOfBoundsException e) {
            // see specification of OneListSearchReply.MatchList.getMatch(int) 
        }
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchReplyImpl#getNbrOfMatches()}.
     */
    public final void testGetNbrOfMatches() {
        assertEquals(0, m_matchList.getNbrOfMatches());
    }

    /**
     * Tests that a null list is not accepted instead of an empty one.
     */
    public final void testNullList() {
        try {
            OneListSearchReplyImpl reply =
                new OneListSearchReplyImpl(
                        OneListSearchReplyImplTest.QUERY,
                        ESTIMATED_TOTAL,
                        null);
            fail("OneListSearchReplyImpl(...) should not allow searchMatches == null");
        } catch (IllegalArgumentException e) {
        }        
    }
}
