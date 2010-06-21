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

/**
 * <p>An iterator to iterate over the list of sub categories for a category.</p>
 *
 * <p>You can never get hold of the virtual root. Also, you can not iterate
 * towards the root, because a category can have more than one parent so
 * the way back depends on how you got there.</p>
 *
 * <p>If an exception occurs, the iterator becomes invalid and the result
 * of future calls to its methods are only guaranteed to not destroy
 * other iterators. Thus, such calls can throw exceptions or return invalid
 * data.</p>
 *
 * <p>Implementors are not required to be thread-safe. This is due to
 * performance reasons. Each thread needing an iterator must get its own
 * instance from {@link CategoryTree}. It is thread-safe to let one thread
 * obtain the iterator and another thread use that iterator.</p>
 * 
 * @see CategoryTree
 */
public interface CategoryTreeIterator {

    /**
     * 
     * @return the number of sub categories left on this level. Returns >= 0.
     */
    public int nbrCategoriesLeft();

    /**
     * <p>More sub categories available?</p>
     * 
     * <p>Technically equal to (nbrSubCategoriesLeft() > 0)</p>
     * 
     * @return true if more sub categories are available. False otherwise.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    public boolean hasNext() throws CategoryTreeException;

    /**
     * <p>Advance to next sub category or throw exception.</p>
     * 
     * @return the next sub category in the list. 
     * @throws NoSuchElementException if there were no more sub categories.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    public HierarchicalCategory next()
        throws NoSuchElementException, CategoryTreeException; 

    /**
     * <p>Utility method to get the sub category the iterator is currently
     * positioned at.</p>
     * 
     * <p>After a call to
     * <code>HierarchicalCategory c = CategoryTreeIterator.next()</code>
     * then <code>c == CategoryTreeIterator.current()</code> until
     * <code>next()</code> is called again. Object reference identity
     * is guaranteed.</p>  
     * 
     * <p>If next() has not been called successfully at least
     * once, throws IllegalStateException. This guards against the case
     * where the list of sub categories was empty.</p>
     * 
     * @return the sub category the iterator is currently at.
     * @throws IllegalStateException if next() has not been called successfully.
     * @throws CategoryTreeException if a data inconsistency was detected.
     */
    public HierarchicalCategory current()
        throws IllegalStateException, CategoryTreeException;
}
