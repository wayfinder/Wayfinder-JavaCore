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
package com.wayfinder.core.search.internal.categorytree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.search.CategoryTreeIterator;
import com.wayfinder.core.search.HierarchicalCategory;
import com.wayfinder.core.search.internal.SearchConstants;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class CategoryTreePersistenceRequestTest extends TestCase {
    
    private static final Position POSITION = Position.createFromDecimalDegrees(51.5, 0.1);  //London area
    
    private static final String CRC = "crc";
    
    private CategoryTreeImpl m_tree;

    private SettingsConnection m_settingsConn;
    


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_settingsConn = MemoryPersistenceLayer.getPersistenceLayer().openSettingsConnection("search");
        m_tree = new CategoryTreeImpl(Language.EN_UK,
                POSITION, CRC,
                UnitedKingdomTree.m_categoryTable,
                UnitedKingdomTree.m_lookupTable,
                UnitedKingdomTree.m_stringTable);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreePersistenceRequest#error(com.wayfinder.core.shared.error.CoreError)}.
     */
    public void testError() {
        
        StateLoadListener listener = new StateLoadListener();
        
        CategoryTreePersistenceRequest req = new CategoryTreePersistenceRequest(listener);

        req.error(new CoreError("error"));
        //the result will be collected by the listener
        assertTrue(listener.doneCalled);
        assertNull(listener.doneResult);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreePersistenceRequest#readPersistenceData(com.wayfinder.pal.persistence.SettingsConnection)}.
     * @throws CategoryTreeException 
     * @throws NoSuchElementException 
     */
    public void testReadPersistenceData() throws IOException, NoSuchElementException, CategoryTreeException {
        StateLoadListener listener = new StateLoadListener();
        
        CategoryTreePersistenceRequest req = new CategoryTreePersistenceRequest(listener);
        
        DataOutputStream dout = m_settingsConn.getOutputStream(SearchConstants.PERSISTENCE_CATEGORIES_TREE);
        m_tree.write(dout);
        req.readPersistenceData(m_settingsConn);
        
        //the result will be collected by the listener
        assertTrue(listener.doneCalled);
        assertNotNull(listener.doneResult);
        assertCatTreeEquals(m_tree, (CategoryTreeImpl) listener.doneResult);
        
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreePersistenceRequest#writePersistenceData(com.wayfinder.pal.persistence.SettingsConnection)}.
     */
    public void testWritePersistenceData() throws IOException, NoSuchElementException, CategoryTreeException {
        CategoryTreePersistenceRequest req = new CategoryTreePersistenceRequest(
                m_tree);

        // write
        req.writePersistenceData(m_settingsConn);

        // check back what's been written
        DataInputStream din = m_settingsConn
                .getDataInputStream(SearchConstants.PERSISTENCE_CATEGORIES_TREE);

        CategoryTreeImpl restoredTree = CategoryTreeImpl.read(din);

        assertCatTreeEquals(m_tree, restoredTree);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreePersistenceRequest#readPersistenceData(com.wayfinder.pal.persistence.SettingsConnection)}.
     */
    public void testReadPersistenceDataBroken() throws IOException {
        StateLoadListener listener = new StateLoadListener();
        
        CategoryTreePersistenceRequest req = new CategoryTreePersistenceRequest(listener);
        
        DataOutputStream dout = m_settingsConn.getOutputStream(SearchConstants.PERSISTENCE_CATEGORIES_TREE);
        dout.writeChars("Hello World");
        try {
            req.readPersistenceData(m_settingsConn);
            fail("Wrong data didn't broke the result");
        } catch (IOException e){
            //success 
        }
        
        //done shouldn't bee called as it was an error
        assertFalse(listener.doneCalled);
    }

    public static void assertCatTreeEquals(CategoryTreeImpl expectedTree, CategoryTreeImpl actualTree) 
            throws NoSuchElementException, CategoryTreeException {
        
        assertEquals(expectedTree.getCrc(), actualTree.getCrc());
        assertEquals(expectedTree.getPosition().getMc2Latitude(), actualTree.getPosition()
                .getMc2Latitude());
        assertEquals(expectedTree.getPosition().getMc2Longitude(), actualTree.getPosition()
                .getMc2Longitude());
        assertEquals(expectedTree.getLanguageId(), actualTree.getLanguageId());

        CategoryTreeIterator tree1Iter = expectedTree.getRootLevelCategories();
        CategoryTreeIterator tree2Iter = actualTree.getRootLevelCategories();

        assertEquals(tree1Iter.nbrCategoriesLeft(), tree2Iter
                .nbrCategoriesLeft());

        // test first level of categories
        while (tree1Iter.hasNext()) {
            HierarchicalCategory refCat = tree1Iter.next();
            HierarchicalCategory testCat = tree2Iter.next();

            assertEquals(refCat.getCategoryName(), testCat.getCategoryName());
            assertEquals(refCat.getCategoryID(), testCat.getCategoryID());
            assertEquals(refCat.getCategoryImageName(), testCat
                    .getCategoryImageName());
            assertEquals(refCat.nbrSubCategories(), testCat.nbrSubCategories());

            if ((tree1Iter.hasNext() ^ tree2Iter.hasNext())) {
                fail("mismatch between reference and test iterators!");
            }
        }
    }
    
    static class StateLoadListener implements CategoryTreePersistenceRequest.LoadListener {
        //used in test
        private boolean doneCalled;
        private Object doneResult;
        
        public void loadDone(Object obj) {
            doneCalled = true;
            doneResult = obj;
        }
    }
}
