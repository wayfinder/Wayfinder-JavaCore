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
package com.wayfinder.core.search.internal.category;

import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.internal.category.CategoryImpl;

import junit.framework.TestCase;

public class CategoryImplTest extends TestCase {
    
    private static final String CATEGORY_NAME = "Restaurant";
    private static final String CATEGORY_IMG_NAME = "tat_restaurant";
    private static final int    CATEGORY_ID   = 23;
    
    
    public void testStandardCategory() {
        CategoryImpl cat = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        assertEquals(CATEGORY_NAME, cat.getCategoryName());
        assertEquals(CATEGORY_IMG_NAME, cat.getCategoryImageName());
        assertEquals(CATEGORY_ID, cat.getCategoryID());
    }
    
    
    public void testStandardCategoryInvalidParams() {
        String[][] params = {
                { CATEGORY_NAME, null },
                { null, CATEGORY_IMG_NAME },
                { null, null }
        };
        
        for (int i = 0; i < params.length; i++) {
            String[] current = params[i];
            try {
                new CategoryImpl(current[0], current[1], CATEGORY_ID);
                fail("Category should throw an exception for invalid parameters");
            } catch(IllegalArgumentException iae) {
                // passed the test
            }
        }
    }
    
    
    public void testHashCodeStandardCategory() {
        Category cat = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        assertEquals(CATEGORY_ID, cat.hashCode());
    }
    
    
    public void testEqualsForEqualStandardCategories() {
        Category cat1 = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        Category cat2 = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        assertTrue(cat1.equals(cat2));
    }
    
    
    public void testEqualsForNONEqualStandardCategories() {
        Category cat1 = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        Category cat2 = new CategoryImpl("apbur", "tat_apbur", 45);
        assertFalse(cat1.equals(cat2));
    }
    
    
    public void testEqualsForEqualCustomCategories() {
        Category cat1 = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        Category cat2 = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        assertTrue(cat1.equals(cat2));
    }
    
    
    public void testEqualForDifferentClasses() {
        Category cat1 = new CategoryImpl(CATEGORY_NAME, CATEGORY_IMG_NAME, CATEGORY_ID);
        String str = "Hello category";
        assertFalse(cat1.equals(str));
    }
    
    
}
