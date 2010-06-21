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
package com.wayfinder.core.map;

import com.wayfinder.core.shared.util.qtree.QuadTreeEntry;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;

public abstract class MapObject implements QuadTreeEntry {

    private String m_ImageName;
    private QuadTreeEntry iNextEntry = null;
    private int m_MaxVisibleZoomLevel;
    
    private int m_minX;
    private int m_maxX;
    private int m_minY;
    private int m_maxY;
    
    
    
    /**
     * Create a new MapObject that should use image with the imageName
     * specified by the parameter. 
     * 
     * @param imageName the name of the image to be shown. 
     */
    public MapObject(String imageName) {
        m_ImageName = imageName;
        //Display MapObjects in map if the scale is equal or lower than 23 
        // (corresponds to ~800 m on the scale ruler)
        m_MaxVisibleZoomLevel = 23; 
    }
    
    /**
     * Create a new MapObject that should use image with the imageName
     * specified by the parameter.
     * 
     * @param imageName the name of the image to be shown.
     * @param maxVisibleZoomLevel the max zoom level when the MapObject should be 
     * visible on the map [1..24000].
     * @throws IllegalArgumentException if the zoomlevel is out of range. 
     */
    public MapObject(String imageName, int maxVisibleZoomLevel) {
        this(imageName);
        
        // Check so that the zoom level is valid. 
        if(maxVisibleZoomLevel < 1 || maxVisibleZoomLevel > 24000)
            throw new IllegalArgumentException("The zoomlevel is out of range!");
            
        m_MaxVisibleZoomLevel = maxVisibleZoomLevel;
    }
    
    /**
     * Method for drawing the map object. 
     * <p>
     * If this method is overloaded by a subclass the new drawing area must be 
     * set by calling {@link #setDrawArea(int, int, int, int)} or 
     *  {@link #setDrawArea(int, int, int)} method. Otherwise the collision 
     * detection will not work and touch zone will not work correctly.  
     * 
     * @param g the graphic object to draw the MapObject on. 
     * @param img the MapObject image
     * @param x screen X coordinate
     * @param y screen Y coordinate
     * @param imageAnchor the anchor point of the image 
     */
    public void draw(WFGraphics g, WFImage img, int x, int y, int imageAnchor) {
        setDrawArea(img.getWidth(), img.getHeight(), imageAnchor);
        g.drawImage(img, x, y, imageAnchor);
    }
    
    /**
     * Return the width of the area where the MapObject is drawn. 
     * 
     * @return the width of the area where the MapObject is drawn.
     * 
     * @deprecated as the value depends on last paint call and don't contain
     * inof   
     */
    public int getWidth() {
        return m_maxX - m_minX;
    }
    
    /**
     * Return the height of the area where the MapObject is drawn.
     * 
     * @return the height of the area where the MapObject is drawn.
     * 
     * @deprecated as the value depends on last paint call this info is used 
     * only internally   
     */
    public int getHeight() {
        return m_maxY - m_minY;
    }
    
    public int getMinX() {
        return m_minX;
    }

    public int getMaxX() {
        return m_maxX;
    }

    public int getMinY() {
        return m_minY;
    }

    public int getMaxY() {
        return m_maxY;
    }

    /**
     * Set the size of the map object. 
     * 
     * @param width the width in pixels. 
     * @param height the height in pixels. 
     * 
     * @deprecated call setDrawArea() instead, this will consider always the
     * drawing area centered 
     */
    public void setSize(int width, int height) { 
        m_maxX = width/2;
        m_minX = -m_maxX;
        
        m_maxY = height/2;
        m_minY = -m_maxY;
    }
    
    /**
     * Set the new drawing area which is relative to center 
     * 
     * If overwrite the paint method this must be called before exist the 
     * method. 
     * This will be use for collision detection and for defining the center of 
     * the touch zone     
     *  
     * @param minX - relative left x comparing to the "center" of the object 
     * (usually a value less or equal to 0) 
     * @param maxX - relative right x comparing to the "center" of the object
     * (usually a value bigger or equal to 0)
     * @param minY - relative top y comparing to the "center" of the object 
     * (usually a value less or equal to 0)
     * @param maxY - relative top y comparing to the "center" of the object 
     * (usually a value bigger or equal to 0)
     * 
     * Note: If the drawing is always the same this can be called only once
     *  
     * @see #draw(WFGraphics, WFImage, int, int, int)
     */
    public void setDrawArea(int minX, int maxX, int minY, int maxY) {
        m_minX = minX;
        m_maxX = maxX;
        m_minY = minY;        
        m_maxY = maxY;
    }

    /**
     * Dose same thing as {@link #setDrawArea(int, int, int, int)} but 
     * using full with and height and anchor instead of giving for 4 relative 
     * sizes 
     *  
     * @param width
     * @param height
     * @param anchor created using the constants from WFGraphics 
     * <code>(e.g. WFGraphics.ANCHOR_BOTTOM | WFGraphics.ANCHOR_HCENTER)</code> 
     * 
     * @see #setDrawArea(int, int, int, int)
     */
    public void setDrawArea(int width, int height, int anchor) {
        if ((anchor & WFGraphics.ANCHOR_LEFT) == WFGraphics.ANCHOR_LEFT) {
            m_minX = 0;
            m_maxX = width;
        } else if((anchor & WFGraphics.ANCHOR_RIGHT) == WFGraphics.ANCHOR_RIGHT) {
            m_minX = -width;
            m_maxX = 0;
        } else { //last case center or wrong value provided we just center it 
            m_maxX = width/2;
            m_minX = -m_maxX;
        }
        
        if((anchor & WFGraphics.ANCHOR_TOP) == WFGraphics.ANCHOR_TOP) {
            m_minY = 0;
            m_maxY = height;
        }
        else if ((anchor & WFGraphics.ANCHOR_BOTTOM) == WFGraphics.ANCHOR_BOTTOM) {
            m_minY = -height;
            m_maxY = 0;
        } else { //last case center or wrong value provided we just center it 
            m_maxY = height/2;
            m_minY = -m_maxY;
        }
    }
    
    public int getMaxVisibleZoomLevel() {
        return m_MaxVisibleZoomLevel;
    }
    
    /**
     * @return the name of the image for the map object. 
     */
    public String getImageName() {
        return m_ImageName;
    }
    
    public QuadTreeEntry getNext() {
        return iNextEntry;
    }
    
    public void setNext(QuadTreeEntry entry) {
        iNextEntry = entry;
    }
    
    /**
     * @return the latitude of the map object. 
     */
    public abstract int getLatitude();
    
    /**
     * @return the longitude of the map object. 
     */
    public abstract int getLongitude();
    
    /**
     * @return the name of the map object. 
     */
    public abstract String getName();
    
}
