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
package com.wayfinder.core.favorite;

import com.wayfinder.core.shared.util.Comparator;

/**
 * Comparator used to order favorites by name lexicographically.
 *  
 */
public class FavoriteNameComparator implements Comparator {

    /**
     * Compare two favorites by name lexicographically
     * @param fav1 and fav2 are the 2 favorites to be compared.
     * They must not be null. They must be of type {@link Favorite}. Their names
     * must not be null.
     * 
     * @return the value 0 if favorites names are equal; 
     * <br/>a value less than 0 if fav1 name is lexicographically less than the fav2 name;  
     * <br/>and a value greater than 0 if fav1 name is lexicographically greater than the fav2 name.  
     * @throws NullPointerException if obj1 or obj2 are null or if their name are null
     * @throws ClassCastException if obj1 or obj2 are not of type Favorite 
     */
    public int compare(Object fav1, Object fav2) {
        return ((Favorite)fav1).getName().compareTo(((Favorite)fav2).getName());
    }

    /**
     * @return true if the give Object is a Favorite and has a name; 
     * false otherwise  
     */
    public boolean canBeCompared(Object fav) {
        return ((fav != null) 
                && (fav instanceof Favorite) 
                && (((Favorite)fav).getName() != null));
    }

}
