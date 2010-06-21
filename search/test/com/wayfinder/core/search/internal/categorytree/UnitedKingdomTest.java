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

package com.wayfinder.core.search.internal.categorytree;

import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.search.CategoryTreeIterator;
import com.wayfinder.core.search.HierarchicalCategory;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.settings.Language;

import junit.framework.TestCase;

/**
 * Some simple tests of the dummy tree for United Kingdom
 * ({@link UnitedKingdomTree}).
 */
public class UnitedKingdomTest extends TestCase {

    private final Position POSITION = new Position(0,0);
    private final String CRC = "crc";

    private CategoryTreeImpl m_ct;

    /**
     * The iterator under test.
     */
    private CategoryTreeIterator m_cti;

    protected void setUp() throws Exception {
        super.setUp();

        m_ct = new CategoryTreeImpl(Language.EN_UK,
                                    POSITION, CRC,
                                    UnitedKingdomTree.m_categoryTable,
                                    UnitedKingdomTree.m_lookupTable,
                                    UnitedKingdomTree.m_stringTable);
        m_cti = m_ct.getRootLevelCategories();
    }

    public void testNumberAtRootLevel() {
        assertEquals(14, m_cti.nbrCategoriesLeft());
    }

    public void testRootLevelList()
        throws CategoryTreeException {

        final int[] list = new int[] {18, 272, 86, 98, 118,
                                      111, 278, 267, 103, 107,
                                      5, 85, 9, 10003};
        for (int i=0; i < list.length; i++) {
            HierarchicalCategory hcat = m_cti.next();
            assertEquals(list[i], hcat.getCategoryID());
        }
    }
}
