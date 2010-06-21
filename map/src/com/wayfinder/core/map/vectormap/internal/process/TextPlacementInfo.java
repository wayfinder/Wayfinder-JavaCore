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
package com.wayfinder.core.map.vectormap.internal.process;

/**
 * Container class for text placement information
 * 
 *
 */

public class TextPlacementInfo {
    
    private int m_startX;
    private int m_startY;
    private int m_stopX;
    private int m_stopY;
    private int m_length;
    private boolean m_containsInfo;
    
    /**
     * Constructs an empty instance of TextPlacementInfo
     */
    public TextPlacementInfo() {
        m_containsInfo = false;
    }
    
    /**
     * Sets the text placement information
     * 
     * @param startX  start x for text placement
     * @param startY  start y for text placement
     * @param stopX  stop x for text placement
     * @param stopY  stop y for text placement
     * @param length  length of text placement vector
     */
    public void setPlacementInfo(int startX, int startY, int stopX, int stopY, int length) {
        m_startX = startX;
        m_startY = startY;
        m_stopX = stopX;
        m_stopY = stopY;
        m_length = length;
        m_containsInfo = true;
    }
    
    /**
     * Resets the text placement information
     */
    public void resetInformation() {
        m_containsInfo = false;
    }
    
    
    /**
     * Returns the start x for text placement
     * 
     * @return start x for text placement
     */
    public int getStartX() {
        return m_startX;
    }
    
    /**
     * Returns the start y for text placement
     * 
     * @return start y for text placement
     */
    public int getStartY() {
        return m_startY;
    }
    
    /**
     * Returns the stop x for text placement
     * 
     * @return stop x for text placement
     */
    public int getStopX() {
        return m_stopX;
    }
    
    /**
     * Returns the stop y for text placement
     * 
     * @return stop y for text placement
     */
    public int getStopY() {
        return m_stopY;
    }
    
    /**
     * Returns the length of the text placement
     * vector
     * 
     * @return length of text placement vector
     */
    public int getLength() {
        return m_length;
    }
    
    /**
     * Checks if text placement information exists
     * 
     * @return true if text placement information exists,
     * false otherwise
     */
    public boolean containsInfo() {
        return m_containsInfo;
    }
    
    public String toString() {
        return "(" + m_startX + "," + m_startY + "," + m_stopX + "," + m_stopY + ")";
    }
}
