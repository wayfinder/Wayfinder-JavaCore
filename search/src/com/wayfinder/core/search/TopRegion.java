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

import com.wayfinder.core.shared.internal.ParameterValidator;


/**
 * A TopRegion represents an area of the world, such as a country or a state.
 * 
 * <p>
 * Please note that unlike the {@link Category} class, it's not possible to
 * enter a custom TopRegion.
 * <p>
 * It's not intended for classes outside the Core to extend this class but
 * rather to obtain the required objects through the {@link SearchInterface}.
 * Also, there are several safeguards in place to prevent introduction of
 * foreign implementations into the Core.
 *
 */
public abstract class TopRegion {
    
    private final String m_regionName;
    
    
    /**
     * Constructor, should only be called by internal core classes
     * 
     * @param regionName The name of the region.
     */
    protected TopRegion(String regionName) {
        if(ParameterValidator.isEmptyString(regionName)) {
            throw new IllegalArgumentException("Region must have a name");
        }
        m_regionName = regionName;
    }
    
    
    /**
     * Returns the name of the TopRegion
     * 
     * @return The name of the top region
     */
    public final String getRegionName() {
        return m_regionName;
    }
    
    /**
     * Returns a unique identifier for the TopRegion that can be used to 
     * extract the TopRegion using  {@link TopRegionCollection#getTopRegionByID(int)}
     *  
     * @return an ID representing the TopRegion
     * 
     * @deprecated This is a temporary solution to give the client possibility 
     * to save persistently last used TopRegions.  
     */
    abstract public int getRegionID();    
    
    protected static TopRegionCollection createTopRegionCollectionInternal(TopRegion[] regionArray) {
        return new TopRegionCollection(regionArray);
    }

}
