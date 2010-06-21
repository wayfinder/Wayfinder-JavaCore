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
package com.wayfinder.core.search.internal.topregion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.TopRegionCollection;
import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.internal.WFComparable;


/**
 * A TopRegion represents an area of the world, such as a country or a state.
 * 
 * <p>
 * Please note that unlike the {@link CategoryImpl} class, it's not possible to
 * enter a custom TopRegion.
 * 
 * 
 *
 */
public final class TopRegionImpl 
extends TopRegion
implements WFComparable, Serializable {
    
    private static final int COMPARE_BY_NAME = 0;
    private static final int COMPARE_BY_ID   = 1;
    
    
    /**
     * Top region of type country. See top_region_t in XML API.
     */
    static final int TYPE_COUNTRY = 0; 


    /**
     * Top region of type state in the United States. See top_region_t
     * in XML API.
     */
    static final int TYPE_US_STATE = 1; 


    /**
     * Top region of type international region, e.g. "Medicon
     * Valley". See top_region_t in XML API.
     */
    static final int TYPE_INTERNATIONAL_REGION = 2; 


    /**
     * Top region of type meta region, e.g. "Europe" See top_region_t
     * in XML API.
     */
    static final int TYPE_META_REGION = 3; 
    
    
    private final int m_regionID;
    private final int m_regionType;
    
    
    /**
     * Standard constructor for creating a TopRegion.
     * <p>
     * This is package protected since TopRegions should not be creatable
     * outside of the Core
     * 
     * @param regionName The name of the TopRegion, should be visible to the
     * end user
     * @param regionType One of the TYPE_* constants in this class
     * @param regionID The individual ID of the region, as sent from the server
     */
    TopRegionImpl(String regionName, int regionType, int regionID) {
        super(regionName);
        m_regionType = regionType;
        m_regionID = regionID;
    }
    
    
    /**
     * Returns the type of the region
     * 
     * @return One of the TYPE_* constants in this class
     */
    int getRegionType() {
        return m_regionType;
    }
    
    
    /**
     * Returns the ID of the TopRegion
     * 
     * @return The unique ID of the TopRegion
     */
    public int getRegionID() {
        return m_regionID;
    }
    
    
    static TopRegionCollection createTopRegionCollection(TopRegion[] regionArray) {
        return TopRegion.createTopRegionCollectionInternal(regionArray);
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
        if(obj instanceof TopRegionImpl) {
            TopRegionImpl otherTR = (TopRegionImpl)obj;
            switch (method) {
            case COMPARE_BY_NAME:
                return getRegionName().compareTo(otherTR.getRegionName());

            case COMPARE_BY_ID:
                if (m_regionID < otherTR.m_regionID) {
                    return -1;
                } else if(m_regionID == otherTR.m_regionID) {
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
        throw new IllegalStateException("Category objects are immutable. Use constructor TopRegionImpl(DataInputStream) instead");
    }
    
    
    public void write(DataOutputStream dout) throws IOException {
        dout.writeUTF(getRegionName());
        dout.writeInt(m_regionType);
        dout.writeInt(m_regionID);
    }
    
    
    TopRegionImpl(DataInputStream din) throws IOException {
        this(din.readUTF(), din.readInt(), din.readInt());
    }
    
    
    
    //-------------------------------------------------------------------------
    // Object methods
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_regionID;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     * 
     */
    public boolean equals(Object obj) {
        if(obj instanceof TopRegionImpl) {
            TopRegionImpl other = (TopRegionImpl) obj;
            return (m_regionID == other.m_regionID)
                && (m_regionType == other.m_regionType)
                &&  getRegionName().equals(other.getRegionName());
        }
        return false;
    }
    
}
