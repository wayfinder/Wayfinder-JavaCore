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

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;

/**
 * Represent a user favorite. 
 * All favorite have a name and optional description, icon name, 
 * position and POI information.
 * <p>Note: The only way to create a favorite is trough {@link FavoriteInterface} 
 * methods. A favorite cannot be modified can only be replaced</p>
 * 
 * @see FavoriteInterface#replaceFavorite(Favorite, String, String)
 *  
 */
public interface Favorite {
    
    public static final String EMPTY_STRING = "";
    
    /**
     * @return the name of the favorite 
     * cannot be null
     */
    public String getName();

    /**
     * @return a description of the favorite
     * cannot be null
     */
    public String getDescription();

    /**
     * @return the name of icon for the favorite
     * cannot be null
     */
    public String getIconName();
    
    /**
     * @return the favorite position
     * cannot be null
     */
    public Position getPosition();

    /**
     * @return the info filed list of this favorite
     * 
     * Note: if the favorite dosen't have any info fields 
     * and an empty array will be returned instead of null
     */
    public InfoFieldList getInfoFieldList();
    
    
}
