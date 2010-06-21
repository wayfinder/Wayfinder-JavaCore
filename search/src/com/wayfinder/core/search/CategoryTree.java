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

package com.wayfinder.core.search;

import java.util.NoSuchElementException;

import com.wayfinder.core.shared.Position;

/**
 * <p>Holding class for the current category tree.</p>
 * 
 * <p>These objects are immutable and new object are created when the tree is
 * updated. Category data is exposed through iterators that provide the
 * features needed by clients. This solution was chosen instead of exposing
 * a tree of objects to the client because:
 * <ol><li>The client doesn't need to implement any tree-walking algorithms.</li>
 *     <li>Almost complete de-coupling of the client side API from the internal
 *     representation.</li>
 *     <li>Enables internal optimized storage so that the tree can scale to
 *     thousands of categories (available from for instance Pages Jaunes(tm),
 *     a French Yellow Pages provider.</li>
 * </ol></p>
 * 
 * <p>Despite the name, the categories for a directed cycle-free graph (DAG).
 * Certain categories can be sub categories of many category. E.g. "Bar"
 * may be a sub category of "Food&Drink" and "Nightlife". In tree terms, this
 * means that a node can have many parents and thus is not a proper tree.
 * The guarantee for the graph to be cycle-free simplifies client
 * implementation (no UI view loops).</p>
 * 
 * <p>Due to the multiple-parents relationship we don't provide
 * an API for the iterators to go backwards/upwards (towards the tree root).
 * Clients are expected to have their own stack connected to their
 * UI view system to provide user-expected "back"-functionality.</p> 
 * 
 * <p>Implementors must be thread-safe.</p>
 * 
 * @see CategoryTreeIterator
 */
public abstract class CategoryTree {

    /**
     * @return The position for which this category was requested from the
     * server.
     */
    public abstract Position getPosition();


    /**
     * <p>Returns an iterator positioned at the beginning of the list of top-level
     * categories. These categories can be said to be sub categories/children
     * of the root of the category tree. But you will never see this root.</p>
     * 
     * <p>There will always be at least one category, thus the returned
     * iterator will always be valid but might become invalid when you advance
     * it.</p>
     * 
     * @return a new iterator for top level. Never returns null.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    public abstract CategoryTreeIterator getRootLevelCategories()
        throws CategoryTreeException;
    
    
    /**
     * <p>Returns an iterator positioned at the beginning of the list of
     * sub categories of the parent category.</p>
     * 
     * <p>This iterator might be invalid since the parent might be a leaf
     * node without sub categories.</p>
     * 
     * @param parent The parent category. Must not be null.  
     * @return a new iterator for the list of sub categories.
     * Never returns null.
     * @throws NoSuchElementException if the category was not found in the tree.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    public abstract CategoryTreeIterator
    getSubCategoriesOf(HierarchicalCategory parent)
        throws NoSuchElementException, CategoryTreeException;
}
