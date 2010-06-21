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
package com.wayfinder.core.search;


/**
 * <p>A category represents a collection of things (POIs usually)
 * that share a common attribute.</p>
 * 
 * <p>The categories are defined by the MC2 server. Categories have an ID.
 * These IDs are unique over time at the MC2 instance used
 * (see {@link com.wayfinder.core.ServerData}).
 * If a category is removed, its ID is no reused unless the new category
 * has the exact same semantics.</p>
 * 
 * <p>Note that if you change server instance (e.g. from production to test)
 * the category IDs are no longer valid.</p>
 *
 * <p>It's not intended for classes outside the Core to extend this class, but
 * rather to obtain the required objects through the {@link SearchInterface}.
 * Also, there are several safeguards in place to prevent introduction of
 * foreign implementations into the Core.</p>
 * 
 * <p>This class is thread-safe by virtue of being immutable and all fields
 * final.</p>
  */
public abstract class Category {
    
    private final String m_categoryName;
    private final String m_imageName;
    private final int m_categoryID;
    
    
    /**
     * Constructor, should only be called by internal core classes
     * 
     * 
     * @param categoryName The name of the Category.
     * @param imageName The name of the image used to represent the Category. 
     * @param categoryID The server's category ID.
     */
    protected Category(String categoryName,
                       String imageName,
                       int categoryID) { 

        // the specification for category tree allows zero length names
        // although the use cases for such names are very limited.
        if(categoryName == null) {
            throw new IllegalArgumentException("Category must have a name");
        } else if(imageName == null) {
            throw new IllegalArgumentException("Category must have an image");
        }
        m_categoryName = categoryName;
        m_imageName = imageName;
        m_categoryID = categoryID;
    }
    

    /**
     * Returns the visible name of the category
     * 
     * @return The name as a String. May return the empty string, but never
     * null.
     */
    public final String getCategoryName() {
        return m_categoryName;
    }
    
    /**
     * Returns the name of the image associated with this category. Please note
     * that the image name will not have a type suffix attached.
     * 
     * @return The image name as a String. May return the empty string (if
     * the category has no specific category), but never null.
     */
    public final String getCategoryImageName() {
        return m_imageName;
    }
        
    /**
     * <p>Returns the unique identifier for the Category.</p> 
     *  
     * @return an ID representing the Category
     * @see CategoryCollection#getCategoryByID(int)
     */
    public int getCategoryID() {
        return m_categoryID;
    }


    // ----------------------------------------------------------------------
    // Core internal

    /**
     * Internal Core method for creating category collection. Not to be
     * used by outsiders.
     *
     * @param catArray the array of categories to be put into the collection.
     * @return a new CategoryCollection containing the categories in catArray. 
     */
    protected static CategoryCollection createCategoryCollectionInternal(Category[] catArray) {
        return new CategoryCollection(catArray);
    }
}
