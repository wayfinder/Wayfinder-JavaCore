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

import com.wayfinder.pal.graphics.WFFont;

/**
 * Represents a city centre in the map
 * 
 *
 */

public class CityCentreObject {

    private String m_text;
    private WFFont m_font;
    private DrawBoundingBox m_bbox;

    /**
     * Creates a city center object
     * 
     * @param text  text of the city center
     * @param font  font for drawing the city center text
     * @param bbox  bounding box of the city center text
     */
    public CityCentreObject(String text, WFFont font, DrawBoundingBox bbox) {
        m_text = text;
        m_font = font;
        m_bbox = bbox;
    }
    
    /**
     * Returns the city center text
     * 
     * @return  the city center text
     */
    public String getText() {
        return m_text;
    }
    
    /**
     * Returns the font for drawing the city center text
     * 
     * @return the font for drawing the city center text
     */
    public WFFont getFont() {
        return m_font;
    }
    
    /**
     * Returns the city center's bounding box
     * 
     * @return the city center's bounding box
     */
    public DrawBoundingBox getBoundingBox() {
        return m_bbox;
    }
}

