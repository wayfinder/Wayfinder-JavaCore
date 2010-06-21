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

import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.FavoriteNameComparator;
import com.wayfinder.core.favorite.internal.FavoriteInternal;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.util.Comparator;

import junit.framework.TestCase;


public class FavoriteNameComparatorTest extends TestCase {
    public void testCompare() {
        Favorite fav1 = new FavoriteInternal("Wayfinder", "Lund","star",null,null); 
        Favorite fav2 = new FavoriteInternal("Vodafone", "UK","red",null,null);
        Favorite fav3 = new FavoriteInternal("Wayfinder", "Malmo","star",new Position(200,300),null);

        Comparator comp = new FavoriteNameComparator();
        
        assertTrue(comp.compare(fav1, fav2) > 0);
        assertTrue(comp.compare(fav2, fav1) < 0);
        
        assertEquals(0, comp.compare(fav1, fav3));
        
    }

    public void testCanBeCompare() {
        Favorite realFav = new FavoriteInternal("Wayfinder", "Lund","star",null,null); 
        String fakeFav = "Vodafone";
        Favorite nullFav = null;
        
        Favorite dummyFav = new Favorite() {
            public String getDescription() {
                return "Dummy";
            }

            public String getIconName() {
                return null;
            }

            public InfoFieldList getInfoFieldList() {
                return null;
            }

            public String getName() {
                return null;
            }

            public Position getPosition() {
                return null;
            }
        };
        
        Comparator comp = new FavoriteNameComparator();
        
        assertTrue(comp.canBeCompared(realFav));
        assertFalse(comp.canBeCompared(fakeFav));
        assertFalse(comp.canBeCompared(nullFav));
        assertFalse(comp.canBeCompared(dummyFav));
        
    }

}
