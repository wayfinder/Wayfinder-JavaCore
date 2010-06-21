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
package com.wayfinder.core.search.internal.categorytree;

import java.util.NoSuchElementException;

import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.search.CategoryTreeIterator;
import com.wayfinder.core.search.HierarchicalCategory;
import com.wayfinder.core.search.internal.categorytree.HierarchicalCategoryImpl;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.settings.Language;

import junit.framework.TestCase;

/**
 * Tests the sub categories aspects of
 * CategoryTreeImpl and CategoryTreeIterator with the minimal
 * tree allowed by the specification.
 */
public class MinimalTreeSubCategoriesTest extends TestCase {

    private final Position POSITION = new Position(0,0);
    private final String CRC = "crc";

    private CategoryTreeImpl m_ct;
    private HierarchicalCategory m_onlyCat;

    /**
     * The iterator under test.
     */
    private CategoryTreeIterator m_cti;


    protected void setUp() throws Exception {
        super.setUp();

        // MinimalTreeTest makes sure this is ok...
        m_ct = new CategoryTreeImpl(
                Language.EN_UK,
                POSITION, CRC,
                MinimalTree.m_categoryTable,
                MinimalTree.m_lookupTable,
                MinimalTree.m_stringTable);
        CategoryTreeIterator tmpIter = m_ct.getRootLevelCategories();
        m_onlyCat = tmpIter.next();
        
        m_cti = m_ct.getSubCategoriesOf(m_onlyCat);
    }


    public void testNbrCateogriesLeft() {
        assertEquals(0, m_cti.nbrCategoriesLeft());
    }

    public void testHasNext()
        throws CategoryTreeException {

        assertFalse(m_cti.hasNext());
    }

    public void testNext()
        throws CategoryTreeException {

        try {
            m_cti.next();
            fail("CategoryTreeIterator.next() didn't throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    /**
     * Since {@link CategoryTreeIterator#next()} will never succeed for this
     * tree (see {@link #testNext()}), {@link CategoryTreeIterator#current()}
     * must also fail.
     */
    public void testCurrent()
        throws CategoryTreeException {

        try {
            m_cti.current();
            fail("CategoryTreeIterator.current() didn't throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Try to find a subcategory that does not exist in the tree and check
     * that the correct exception is thrown.
     * 
     * @throws CategoryTreeException
     */
    public void testNonExistingSubCategory()
        throws CategoryTreeException {

        HierarchicalCategory dog =
            new HierarchicalCategoryImpl("fake", "fake", -1, 0);
        try {
            CategoryTreeIterator cti = m_ct.getSubCategoriesOf(dog);
            fail("CategoryTree.getSubCategoriesOf() didn't throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }
}
