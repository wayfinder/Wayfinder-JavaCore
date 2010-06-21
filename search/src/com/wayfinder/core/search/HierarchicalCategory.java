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
package com.wayfinder.core.search;

/**
 * <p>A category than is defined within a hierarchy of categories.</p>
 * 
 * <p>For example: "Italian Restaurants" is a sub category of
 * "Restaurants".</p>
 * 
 * <p>There can be many objects with the same name, ID and image name but
 * different value of {@link #nbrSubCategories()} since that is a property
 * of the hierarchy in which the category is contained. However, the MC2
 * server only cares about the ID when searching.</p>
 * 
 * <p>Clients don't create instances of this class. They get them 
 * as return values from methods in CategoryTreeIterator. The objects have
 * value semantics and there can be many objects describing the same category
 * in memory at the same time.</p>
 * 
 * <p>Implementors must be thread-safe.</p>
 * 
 * @see CategoryTree
 * @see CategoryTreeIterator
 */
public abstract class HierarchicalCategory extends Category {

    /**
     * <p>Constructs a new HierarchicalCategory.</p>
     * 
     * <p>Not indented to be called from outside of Core.</p>
     * 
     * @param categoryName the name of the category.
     * @param imageName the name of the image used as an icon for this category.
     * @param categoryID The server's category ID.
     */
    protected HierarchicalCategory(String categoryName,
                                String imageName,
                                int categoryID) {
        super(categoryName, imageName, categoryID);
    }


    /**
     * Returns the number of sub categories.
     * 
     * @return the number of sub categories to this category or 0 if this
     * category is not part of an hierarchy. Never returns < 0.
     */
    public abstract int nbrSubCategories();
}
