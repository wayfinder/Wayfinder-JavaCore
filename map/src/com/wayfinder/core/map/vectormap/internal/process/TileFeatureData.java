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

import com.wayfinder.core.map.vectormap.internal.control.ConcavePolygon;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;


/**
 * 
 * The TileFeatureData class holds all the information about a tile feature. 
 * 
 * Note that all arguments in this class are not used by all features in the map. 
 * 
 * 
 * 
 */
public class TileFeatureData {
    
    private static final Logger LOG = LogFactory
    .getLoggerForClass(TileFeatureData.class);

    private int m_featureIndex;
    private int m_level;
    private int m_featureType;
    private int[] m_importanceAndPrimitiveType = new int[2];
    private String m_text;
    private TextPlacementInfo m_textPlacementInfo;
    private int[] m_coordExtremes = new int[4];
    private int m_time;
    private int m_id = -1;
    private short m_duration = 0;
    
    public TileFeatureData(int primitiveType, int featureIndex, int featureType) {
        m_importanceAndPrimitiveType[1] = primitiveType;
        m_featureIndex = featureIndex;
        m_featureType = featureType;
    }
    
    // ---------------------------------------------------------------------------------
    // General for all primitive type
    
    /**
     * @return the importance and primitive type of the feature
     */
    public int[] getImportanceAndPrimitiveType() {
        return m_importanceAndPrimitiveType;
    }

    /**
     * @return the primitive type of the feature
     */
    public int getPrimitiveType() {
        return m_importanceAndPrimitiveType[1];
    }
    
    /**
     * @return the type of the feature
     */
    public int getFeatureType() {
        return m_featureType;
    }
    
    /**
     * @return
     */
    public int getFeatureIndex() {
        return m_featureIndex;
    }
    
    /**
     * @return 
     */
    public int getLevel() {
        return m_level;
    }
    
    /**
     * @return
     */
    public int getImportance() {
        return m_importanceAndPrimitiveType[0];
    }
    
    /**
     * @return
     */
    public String getText() {
        return m_text;
    }
    
    /**
     * Returns the text placement information
     * 
     * @return text placement information
     */
    public TextPlacementInfo getTextPlacementInfo() {
        return m_textPlacementInfo;
    }
    
    /**
     * @return
     */
    public int[] getCoordExtremes() {
        return m_coordExtremes;
    }

    /**
     * 
     * @return
     */
    public int getTime() {
        return m_time;
    }
    
    /**
     * 
     * @return
     */
    public int getPoiUniqueID() {
        return m_id;
    }
    
    /**
     * 
     * @return the duration of the events with resolution of 5 minutes. Duration 0 means that
     * it's a all day event. 
     */
    public short getDuration() {
        return m_duration;
    }
    
    // ----------------------------------------------------------------------------------
    // Used by the BITMAP primitive type
    
    private int m_maxScale = -1;
    private int m_bitmapIndex; 
    private int[] m_latlon = new int[2];
    
    public int getMaxScale() {
        return m_maxScale;
    }
    
    public int getBitmapIndex() {
        return m_bitmapIndex;
    }

    /**
     * @return longitude in MC2 coordinate.
     */
    public int getLatitude() {
        return m_latlon[0];
    }

    /**
     * @return Latitude in MC2 coordinate
     */
    public int getLongitude() {
        return m_latlon[1];
    }
    
    /**
     * @return Latitude and Longitude in MC2 coordinate
     */
    public int[] getLatLon() {
        return m_latlon;
    }

    // ----------------------------------------------------------------------------------
    // Used by the LINE and POLYGON primitive type
    
    private SimpleArg m_color;
    private int[] m_coords;
    
    /**
     * Returns the color used at the specified scale index.
     * @param scaleIndex
     * @return Color at the specified scale index.
     */
    public int getColor(int scaleIndex) {
        if (m_color == null) {
            // Yes, same as no color.
            return NO_BORDER_COLOR;
        }
        return m_color.getValue(scaleIndex);
    }
    
    public int[] getCoords() {
        return m_coords;
    }
    
    // ----------------------------------------------------------------------------------
    // Used by the POLYGON primitive type
    
    private ConcavePolygon m_concavePolygon;
    
    public ConcavePolygon getConcavePolygon() {
        return m_concavePolygon;
    }
    
    // ----------------------------------------------------------------------------------
    // Used by the LINE primitive type
    
    
    
    private SimpleArg m_borderColor = null;
    private SimpleArg m_widthArgs = null;
    private SimpleArg m_widthMetersArgs = null;
    
    // Indicate that no border color should be used
    public static final int NO_BORDER_COLOR = -1;

    public void setWidth(SimpleArg args) {
        m_widthArgs = args;
    }
    
    public int getWidth(int scaleIndex) {
        return m_widthArgs.getValue(scaleIndex);
    }
    
    public int getBorderColor(int scaleIndex) {
        if (m_borderColor == null) {
            return NO_BORDER_COLOR;
        }
        return m_borderColor.getValue(scaleIndex);
    }
    
    public int getWidthMeters(int scaleIndex) {
        if (m_widthMetersArgs == null) {
            return 0xFF;
        }
        return m_widthMetersArgs.getValue(scaleIndex);
    }
    
    
    // ----------------------------------------------------------------------------------
    // Set methods  
    /**
     * 
     * @param text
     */
    public void setText(String text) {
        m_text = text;
    }
    
    /**
     * Sets the draw level (order).
     * @param level the draw level (order)
     */
    public void setLevel(int level) {
        m_level = level;
    }
    
    public void setFeatureIndex(int index) {
        m_featureIndex = index;
    }
    
    public void setFeatureType(int type) {
        m_featureType = type;
    }
    
    public void setImportance(int imp) {
        m_importanceAndPrimitiveType[0] = imp;
    }
    
    /**
     * Sets the text placement information
     * 
     * @param textPlacementInfo  text placement information
     */
    public void setTextPlacementInfo(TextPlacementInfo textPlacementInfo) {
        m_textPlacementInfo = textPlacementInfo;
    }
    
    public void setBoundingBox(int maxLat, int minLat, int maxLon, int minLon) {
        int[] ce = m_coordExtremes;
        ce[0] = minLat;
        ce[1] = maxLat;
        ce[2] = minLon;
        ce[3] = maxLon;
    }
    
    public void setColor(SimpleArg color) {
        m_color = color;
    }
    
    void setCoords(int[] coords) {
        m_coords = coords;
    }
    
    public void setConcavePolygon(ConcavePolygon concavePolygon) {
        m_concavePolygon = concavePolygon;
    }

    public void setWidthMeter(SimpleArg arg) {
        m_widthMetersArgs = arg;
    }
    
    public void setBorderColor(SimpleArg borderColor) {
        m_borderColor = borderColor;
    }
    
    public void setMaxScale(int maxScale) {
        m_maxScale = maxScale;
    }
    
    public void setBitmapIndex(int index) {
        m_bitmapIndex = index;
    }
    
    public void setCenterLat(int latitude) {
        m_latlon[0] = latitude;
    }
    
    public void setCenterLon(int longitude) {
        m_latlon[1] = longitude;
    }
    
    public void setTime(int time) {
        m_time = time;
    }
    
    public void setId(int id) {
        m_id = id;
    }
    
    /**
     * Sets the duration for the event. The size of the duration is 2 bytes.
     *  
     * @param duration  the duration
     */
    public void setDuration(int duration) {
        m_duration = (short)duration;
    }

}
