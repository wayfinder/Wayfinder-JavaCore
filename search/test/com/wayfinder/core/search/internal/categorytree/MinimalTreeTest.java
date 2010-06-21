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

import java.util.NoSuchElementException;

import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.search.CategoryTreeIterator;
import com.wayfinder.core.search.HierarchicalCategory;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.settings.Language;

import junit.framework.TestCase;

/**
 * Tests the CategoryTreeImpl and CategoryTreeIterator with the minimal
 * tree allowed by the specification.
 */
public class MinimalTreeTest extends TestCase {

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
                                    MinimalTree.m_categoryTable,
                                    MinimalTree.m_lookupTable,
                                    MinimalTree.m_stringTable);
        m_cti = m_ct.getRootLevelCategories();
    }

    // basic data independant tests covered by another test class
    
    // ---------------------------------------------------------------------
    // test iterating through the binary category data blob.

    public void testRootLevel1()
        throws CategoryTreeException {

        assertTrue(m_cti.hasNext());
    }

    public void testRootLevelHasNextCorrect()
        throws CategoryTreeException {

        assertTrue(m_cti.hasNext());
        m_cti.next();
        assertFalse(m_cti.hasNext());
    }

    public void testRootLevelNbrCategoriesLeftCorrect()
        throws CategoryTreeException {

        assertEquals(1, m_cti.nbrCategoriesLeft());
        m_cti.next();
        assertEquals(0, m_cti.nbrCategoriesLeft());
    }

    public void testRootLevelData()
        throws CategoryTreeException {

        HierarchicalCategory hcat = m_cti.next();
        assertEquals(0, hcat.nbrSubCategories());
        assertEquals(0x12345678, hcat.getCategoryID());
        assertEquals("", hcat.getCategoryName());
        assertEquals("", hcat.getCategoryImageName());
    }

    public void testRootLevelNextSameAsCurrent()
        throws CategoryTreeException {

        // check the strong guarantee in CategoryTreeIterator
        HierarchicalCategory hcat = m_cti.next();
        assertSame(hcat, m_cti.current());
    }

    public void testRootLevelErrorCurrentBeforeFirstNext()
        throws CategoryTreeException {

        try {
            HierarchicalCategory hcat = m_cti.current();
            fail("CategoryTreeIterator.current() didn't throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    public void testRootLevelErrorAdvancePastEnd()
        throws CategoryTreeException {

        m_cti.next();
        try {
            m_cti.next();
            fail("CategoryTreeIterator.next() didn't throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }
}
