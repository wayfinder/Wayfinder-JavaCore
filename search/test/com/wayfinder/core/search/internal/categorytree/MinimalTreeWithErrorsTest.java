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
import com.wayfinder.core.shared.Position;

import junit.framework.TestCase;

/**
 * Tests that the errors in {@link MinimalTreeWithErrors} cause the right
 * exceptions in {@link CategoryTreeIteratorImpl}.
 */
public class MinimalTreeWithErrorsTest extends TestCase {

    /**
     * The iterator under test.
     */
    private CategoryTreeIteratorImpl m_cti;


    protected void setUp() throws Exception {
        super.setUp();
    }


    public void testSubCategoriesOf()
        throws Exception {

        setUpStdBrokenTable();
        try {
            m_cti.subCategoriesOf(MinimalTreeWithErrors
                                  .ID_WITH_TRUNCATED_OFFSET);
            fail("subCategoriesOf() did not throw CategoryTreeException.");
        } catch (CategoryTreeException e) {
        }
    }

    public void testInvalidOffsetToStrings()
        throws Exception {

        setUpStdBrokenTable();
        try {
            m_cti.next(); // should fail when trying to read the
                          // strings for category construction
            fail("next() did not throw CategoryTreeException"
                 + " despite broken string offet.");
        } catch (CategoryTreeException e) {
        }
    }

    /**
     * Tests the error where the category tree is zero bytes long.
     */
    public void testTruncatedCategoryTable1() {
        try {
            m_cti = new CategoryTreeIteratorImpl(
                    new byte[] {0}, // will be a read error when trying to
                                    // get # top level categories
                    MinimalTreeWithErrors
                    .m_lookupTable,
                    MinimalTreeWithErrors
                    .m_stringTable);

            fail("ctor CategoryTreeIteratorImpl(byte[], byte[], byte[])"
                 + " did not throw CategoryTreeException.");
        } catch (CategoryTreeException e) {
        }
    }

    /**
     * Tests with a category table that is truncated in the data for the first
     * category.
     */
    public void testTruncatedCategoryTable2()
        throws Exception {

        m_cti = new CategoryTreeIteratorImpl(
                MinimalTreeWithErrors
                .m_categoryTableShort,
                MinimalTreeWithErrors
                .m_lookupTable,
                MinimalTreeWithErrors
                .m_stringTable);   

        try {
            m_cti.next(); // should fail when trying to read the
                          // strings for category construction
            fail("next() did not throw CategoryTreeException"
                 + " despite truncated table.");
        } catch (CategoryTreeException e) {
        }
    }


    // -------------------------------------------------------------------
    private void setUpStdBrokenTable()
        throws CategoryTreeException {

        m_cti = new CategoryTreeIteratorImpl(
                MinimalTreeWithErrors
                .m_categoryTable,
                MinimalTreeWithErrors
                .m_lookupTable,
                MinimalTreeWithErrors
                .m_stringTable);
    }
}
