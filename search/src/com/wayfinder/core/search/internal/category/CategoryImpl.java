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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryCollection;
import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.internal.WFComparable;


/**
 * <p>Internal Core implementation of Category.</p>
 */
public final class CategoryImpl 
extends Category 
implements WFComparable, Serializable {
    
    private static final int COMPARE_BY_NAME = 0;
    private static final int COMPARE_BY_ID   = 1;

    
    /**
     * Constructor for creating a server generated category
     * 
     * <p>
     * This method is package protected to prevent outside creation
     * 
     * @param name The name of the Category.
     * @param imageName The image of the category.
     * @param categoryID The server ID of the category.
     * @throws IllegalArgumentException if aName or aImageName is null or empty.
     */
    CategoryImpl(String name, String imageName, int categoryID) {
        super(name, imageName, categoryID);
    }
    

    
    static CategoryCollection createCategoryCollection(Category[] catArray) {
        return Category.createCategoryCollectionInternal(catArray);
    }
    
    
    
    //-------------------------------------------------------------------------
    // WFComparable interface
    
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.WFComparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        return compareTo(obj, COMPARE_BY_NAME);
    } // compareTo(Object)


    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.WFComparable#compareTo(java.lang.Object, int)
     */
    public int compareTo(Object obj, int method) {
        if(obj instanceof CategoryImpl) {
            CategoryImpl othercat = (CategoryImpl)obj;
            switch (method) {
            case COMPARE_BY_NAME:
                return getCategoryName().compareTo(othercat.getCategoryName());

            case COMPARE_BY_ID:
                if (getCategoryID() < othercat.getCategoryID()) {
                    return -1;
                } else if(getCategoryID() == othercat.getCategoryID()) {
                    return 0;
                } else {
                    return 1;
                }
            
            default:
                throw new IllegalArgumentException("Illegal method for category comparison");
            }
        }
        throw new ClassCastException("Categories can only be compared with other categories");
    }
    
    
    
    //-------------------------------------------------------------------------
    // Serializable interface
    // slightly modified though. We cannot use the read(DIS) method and
    // guarantee immutablity at the same time. Since the objects are passed
    // outside of the Core, we must favor the immutability
    
    public void read(DataInputStream din) throws IOException {
        throw new IllegalStateException("Category objects are immutable. Use constructor CategoryImpl(DataInputStream) instead");
    }
    
    
    public void write(DataOutputStream dout) throws IOException {
        dout.writeUTF(getCategoryName());
        dout.writeUTF(getCategoryImageName());
        dout.writeInt(getCategoryID());
    }
    
    
    CategoryImpl(DataInputStream din) throws IOException {
        this(din.readUTF(), din.readUTF(), din.readInt());
    }
    

    
    //-------------------------------------------------------------------------
    // overridden Object methods
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getCategoryID();
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if(obj instanceof CategoryImpl) {
            CategoryImpl other = (CategoryImpl) obj;
            return (getCategoryID() == other.getCategoryID()) 
                 && super.getCategoryName().equals(other.getCategoryName()) 
                 && super.getCategoryImageName().equals(other.getCategoryImageName());
        }
        return false;
    }
}
