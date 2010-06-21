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
 * Represents a collection of {@link TopRegion} objects
 */
public final class TopRegionCollection {
    
    private final TopRegion[] m_regionArray;
    
    TopRegionCollection(TopRegion[] regionArray) {
        m_regionArray = regionArray;
    }
    
    
    /**
     * Returns the number of {@link TopRegion} objects in this collection
     * 
     * @return The number of {@link TopRegion} objects in this collection
     */
    public int getNbrOfRegions() {
        return m_regionArray.length;
    }
    
    
    /**
     * Returns one of the {@link TopRegion} objects in this collection
     * 
     * @param index The index of the {@link TopRegion} to get
     * @return The {@link TopRegion} object
     */
    public TopRegion getTopRegion(int index) {
        return m_regionArray[index];
    }
    
    /**
     * Search for TopRegion with the given regionID
     * 
     * @param regionID previous obtained by calling {@link TopRegion#getRegionID()}
     * @return the TopRegion or null if it could not be found
     *   
     * @deprecated This is a temporary solution to give the client possibility 
     * to save persistently last used TopRegions. 
     */
    public TopRegion getTopRegionByID(int regionID) {
        for(int i = 0; i < m_regionArray.length; i++) {
            if (m_regionArray[i].getRegionID() == regionID) return m_regionArray[i];
        }
        return null;
    }
}
