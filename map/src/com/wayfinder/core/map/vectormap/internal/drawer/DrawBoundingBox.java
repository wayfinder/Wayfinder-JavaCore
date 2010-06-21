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
package com.wayfinder.core.map.vectormap.internal.drawer;

/**
 * Represents a screen coordinates bounding box
 * 
 *
 */

public class DrawBoundingBox {
    
    private int m_minX;
    private int m_minY;
    private int m_maxX;
    private int m_maxY;
    
    /**
     * Creates a bounding box with the specified coordinates
     * 
     * @param minX  min x coordinate
     * @param minY  min y coordinate
     * @param maxX  max x coordinate
     * @param maxY  max y coordinate
     */
    public DrawBoundingBox(int minX, int minY, int maxX, int maxY) {
        m_minX = minX;
        m_minY = minY;
        m_maxX = maxX;
        m_maxY = maxY;
    }
    
    /**
     * Returns the minimum x coordinate
     * 
     * @return the min x coordinate
     */
    public int getMinX() {
        return m_minX;
    }
    
    /**
     * Returns the minimum y coordinate
     * 
     * @return the min y coordinate
     */
    public int getMinY() {
        return m_minY;
    }
    
    /**
     * Returns the maximum x coordinate
     * 
     * @return the max x coordinate
     */
    public int getMaxX() {
        return m_maxX;
    }
    
    /**
     * Returns the maximum y coordinate
     * 
     * @return the max y coordinate
     */
    public int getMaxY() {
        return m_maxY;
    }
    
    /**
     * Checks if this boundingbox intersects with
     * the one specified as parameter
     * 
     * @param b  boundingbox to check intersection with
     * @return true if boundingbox intersects, false otherwise 
     */
    public boolean intersectsWith(DrawBoundingBox b) {
        return m_maxX > b.getMinX() && m_minX < b.getMaxX() 
        && m_maxY > b.getMinY() && m_minY < b.getMaxY();
    }
}
