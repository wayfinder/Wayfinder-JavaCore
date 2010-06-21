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

import java.io.IOException;
import java.util.NoSuchElementException;

import com.wayfinder.core.search.CategoryTree;
import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.search.CategoryTreeIterator;
import com.wayfinder.core.search.HierarchicalCategory;
import com.wayfinder.core.shared.util.io.WFByteArrayInputStream;
import com.wayfinder.core.shared.util.io.WFDataInputStream;

/**
 * <p>Implements {@link com.wayfinder.core.search.CategoryTreeIterator}.</p>
 *
 * <p>For information about design choices, see {@link CategoryTree}
 * and {@link CategoryTreeIterator}.</p>
 * 
 * <p>This class is not thread safe. The specification in
 * CategoryTreeIterator explicitly allows this.</p>
 *
 * <p>The current implementation implements version 1 of the binary category
 * tree format as described in
 * <i>"Wayfinder Systems AB - MC2 The XML API. rev 2.2.2, 21st January 2010 3:02:
 *    Section 12.10 Local Category Tree Reply".</i>
 * </p>
 */
final class CategoryTreeIteratorImpl implements CategoryTreeIterator {

    /**
     * See category_table in the specification.
     */
    private final CategoryData m_categoryData;
    private final SubCategoryList m_subCategoryList;

    /**
     * See lookup_table in the specification.
     */
    private final LookupTable m_lookupTable;

    /**
     * See string_table in the specification.
     */
    private final StringTable m_stringTable;

    /**
     * last sub category read or null if none has been read yet.
     */
    private HierarchicalCategory m_currentSubCategory;


    /**
     * 
     * @param categoryTable Do not touch this after constructing the object.
     * @param lookupTable See lookup_table in the specification. Do not touch this after constructing the object.
     * @param stringTable Do not touch this after constructing the object.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    CategoryTreeIteratorImpl(byte[] categoryTable,
                             byte[] lookupTable,
                             byte[] stringTable)
        throws CategoryTreeException {

        try {
            m_categoryData = new CategoryData(categoryTable);
            m_subCategoryList = new SubCategoryList(categoryTable);
            // m_subCategoryList is now initialized to top level list

            m_lookupTable = new LookupTable(lookupTable);
            m_stringTable = new StringTable(stringTable);
        } catch (IOException e) {
            throw new CategoryTreeException(e);
        } catch (RuntimeException e) {
            // for symmetry - with the current implementation
            // of the tables, there will not be any convertable
            // RuntimeExceptions thrown.
            throw convertRuntimeException(e);
        }
    }

    
    // ---------------------------------------------------------------------
    // CategoryTreeIterator interface

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.CategoryTreeIterator#current()
     */
    public HierarchicalCategory current() throws IllegalStateException {
        if (m_currentSubCategory != null) {
            return m_currentSubCategory;
        } else {
            // nothing read yet.
            throw new IllegalStateException();
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.CategoryTreeIterator#hasNext()
     */
    public boolean hasNext() {
        // m_subCategoryList is initialized from the ctor.
        return (m_subCategoryList.m_nbrElementsLeft > 0);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.CategoryTreeIterator#nbrCategoriesLeft()
     */
    public int nbrCategoriesLeft() {
        // SubCategoryList guarantees that this is >= 0.
        return m_subCategoryList.m_nbrElementsLeft;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.CategoryTreeIterator#next()
     */
    public HierarchicalCategory next()
        throws CategoryTreeException, NoSuchElementException {
        try {
            int byteIndex = m_subCategoryList.readNext(); 
            // either ok or NoSuchElementException thrown
            m_categoryData.readCategoryDataAt(byteIndex);
            String categoryName =  m_stringTable.readString(
                    m_categoryData.m_categoryNameByteIndex);
            String imageName =  m_stringTable.readString(
                    m_categoryData.m_imageNameByteIndex);

            m_currentSubCategory =
                new HierarchicalCategoryImpl(categoryName,
                                             imageName,
                                             m_categoryData.m_categoryID,
                                             m_categoryData.m_nbrSubCategories);
            return m_currentSubCategory; 

        } catch (IOException e) {
            throw new CategoryTreeException(e);
        } catch (RuntimeException e) {
            throw convertRuntimeException(e);
        }
    }


    // ---------------------------------------------------------------------
    // package internal

    /**
     * <p>Re-position the iterator before the first sub category
     * of categoryID.</p>
     *
     * @param categoryID the category for which we want the list of
     * sub categories.
     * @throws NoSuchElementException if the category does not exist in
     * m_lookupTable.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    void subCategoriesOf(int categoryID)
        throws NoSuchElementException, CategoryTreeException {

        try {
            m_currentSubCategory = null; // safe-guard
            m_lookupTable.findCategory(categoryID);
            m_categoryData.readCategoryDataAt(m_lookupTable.m_categoryTableByteIndex);
            m_subCategoryList.setStartOfList(m_categoryData.m_subCategoryListByteIndex,
                                             m_categoryData.m_nbrSubCategories);
        } catch (IOException e) {
            throw new CategoryTreeException(null, e);
        } catch (RuntimeException e) {
            throw convertRuntimeException(e);
        }
    }


    // ---------------------------------------------------------------------

    /*
     * Instances of the internal table abstractions could be shared between
     * instances of the iterator if they are thread-safe.
     * 
     * We think that the performance overhead of that locking would exceed
     * the penalty of the memory for the references and the stream data
     * (the stream objects are lot of code but data is a few integers for size
     * and position.
     */
    
    /**
     * See lookup_table in the specification.
     */
    private static class LookupTable {
        private final WFByteArrayInputStream m_bais;
        private final WFDataInputStream m_dis;

        // int m_tableIndex;
        int m_categoryID;
        int m_categoryTableByteIndex;
        
        LookupTable(byte[] lookupTable) {
            m_bais = new WFByteArrayInputStream(lookupTable);
            m_dis = new WFDataInputStream(m_bais);
        }
        
        /**
         * Update m_categoryID and m_categoryTableByteIndex. 
         * 
         * @param categoryID
         * @throws NoSuchElementException
         * @throws IOException
         */
        void findCategory(int categoryID)
            throws NoSuchElementException, IOException {

            // TODO: binary search for speed

            // By specification there are >= 1 elements.
            
            m_bais.setPos(0);
            do {
                m_categoryID = m_dis.readInt();
                // we must read this anyway if we need to skip if
                // it was not the category we were looking for.
                m_categoryTableByteIndex = m_dis.readInt();
                if (m_categoryID == categoryID) {
                    return;
                }
            } while (m_bais.available() > 0);
            // exhausted table without finding.
            throw new NoSuchElementException();
        }
    }


    // category table
    /*
     * while reading we advance through the list:
     *   #elements
     *   current pos (byte or index)
     *   -> from that we can determine hasNext()
     * 
     * at the same time we need to jump into other offsets to get the sub
     * category data.
     *   * start at given pos
     *   * read id, str table indices, #subcats
     *   * save the offset of list for future use.
     * 
     * So it makes sense to keep the data structure for the current list
     * separate from the data for the sub category just read.
     */

    /**
     * See category in the specification (..., uint16, 0*int32).
     */    
    private static class SubCategoryList { 
        private final WFByteArrayInputStream m_bais;
        private final WFDataInputStream m_dis;

        /**
         * The implementation of SubCategoryList guarantees that this is >= 0
         * unless it is touched from the outside.
         */
        int m_nbrElementsLeft;
        
        /**
         * Initializes the list to the top_level_list.
         * 
         * @param categoryTable
         * @throws IOException if there was an error when we try to read
         * list data with {@link WFDataInputStream}.
         */
        SubCategoryList(byte[] categoryTable)
            throws IOException {

            m_bais = new WFByteArrayInputStream(categoryTable);
            m_dis = new WFDataInputStream(m_bais);
            m_nbrElementsLeft = m_dis.readUnsignedShort();
        }
        

        void setStartOfList(int categoryTableByteIndex,
                            int nbrElements) {
            m_bais.setPos(categoryTableByteIndex);
            m_nbrElementsLeft = nbrElements;
        }

        /**
         * 
         * @return the offset into category_table of the next sub category. 
         * @throws NoSuchElementException
         * @throws IOException
         */
        int readNext()
            throws NoSuchElementException, IOException {

            if (m_nbrElementsLeft > 0) {
                int id = m_dis.readInt();
                m_nbrElementsLeft--;

                return id;
            } else {
                throw new NoSuchElementException(); 
            }
        }
    }


    /**
     * See category in the specification (id, string index, #sub categories).
     */    
    private static class CategoryData {
        private final WFByteArrayInputStream m_bais;
        private final WFDataInputStream m_dis;

        int m_categoryID; 
        int m_categoryNameByteIndex;
        int m_imageNameByteIndex;

        /**
         * uint16 in specification.
         */
        int m_nbrSubCategories;

        int m_subCategoryListByteIndex;
        
        CategoryData(byte[] categoryTable) {
            m_bais = new WFByteArrayInputStream(categoryTable);
            m_dis = new WFDataInputStream(m_bais);
        }

        void readCategoryDataAt(int categoryTableByteIndex)
            throws IOException {
        
            m_bais.setPos(categoryTableByteIndex);
            m_categoryID = m_dis.readInt();
            m_categoryNameByteIndex = m_dis.readInt();
            m_imageNameByteIndex = m_dis.readInt();
            m_nbrSubCategories = m_dis.readUnsignedShort();
            m_subCategoryListByteIndex = m_bais.getPos();
        }
    }


    /**
     * See string_table in the specification.
     */
    private static class StringTable {
        private final WFByteArrayInputStream m_bais;
        private final WFDataInputStream m_dis;

        StringTable(byte[] stringTable) {
            m_bais = new WFByteArrayInputStream(stringTable);
            m_dis = new WFDataInputStream(m_bais);
        }

        /**
         * 
         * @param stringTableByteIndex The index from category_table which
         * points to the first byte of the C-string. The length indicator
         * needed for {@link java.io.DataInput#readUTF()} is two bytes before.
         * @return The string at the index. Never null, but can have zero
         * length.
         * @throws IOException if there was an reading error from
         * {@link DataInput#readUTF()}.
         */
        String readString(int stringTableByteIndex)
            throws IOException {
            m_bais.setPos(stringTableByteIndex - 2);
            return m_dis.readUTF();
        }
    }


    // ---------------------------------------------------------------------
    // internal utilities


    /**
     * Convert some {@link RuntimeException} to our checked exception
     * {@link CategoryTreeException}. If no conversion takes place,
     * the original RuntimeException is returned.
     * 
     * @throws CategoryTreeException if exception conversion takes place. The
     * new exception is thrown with the original exception as cause.
     */
    private RuntimeException convertRuntimeException(RuntimeException e)
        throws CategoryTreeException {

        if (e instanceof IllegalArgumentException
            || e instanceof IndexOutOfBoundsException) {
            // from WFByteArrayInputStream.setPos()
            throw new CategoryTreeException(e);
        } else {
            return e;
        }
    }
}
