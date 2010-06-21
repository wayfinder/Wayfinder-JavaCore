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

import com.wayfinder.core.search.HierarchicalCategory;

/**
 * <p>Implements {@link com.wayfinder.core.search.HierarchicalCategory}.</p>
 * 
 * <p>This class is thread safe by virtue of all fields being final.</p>
 * 
 * @see CategoryTreeImpl
 * @see CategoryTreeIteratorImpl
  */
public class HierarchicalCategoryImpl extends HierarchicalCategory {

    private final int m_nbrSubCategories;
    
    /**
     * <p>Constructs a new HierarchicalCategory.</p>
     * 
     * @param categoryName the name of the category.
     * @param imageName the name of the image used as an icon for this category.
     * @param categoryID The server's category ID.
     * @param nbrSubCategories we cache this in this object to provide a
     * cleaner interface to the client. So that they don't need to query the
     * iterator for this information.
     */
    public HierarchicalCategoryImpl(String categoryName,
                                    String imageName,
                                    int categoryID,
                                    int nbrSubCategories) {
        super(categoryName, imageName, categoryID);
        m_nbrSubCategories = nbrSubCategories; 
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.search.HierarchicalCategory#nbrSubCategories()
     */
    public int nbrSubCategories() {
        return m_nbrSubCategories;
    }
}
