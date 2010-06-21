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

import java.util.Hashtable;
import java.util.Vector;

import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.internal.process.TextPlacementInfo;
import com.wayfinder.core.map.vectormap.internal.process.TileFeature;
import com.wayfinder.core.map.vectormap.internal.process.TileFeatureData;
import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFGraphicsFactory;
import com.wayfinder.pal.graphics.WFImage;

public class TileMapWrapper {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapWrapper.class);
    
    private int nbrImportances;
    private int iLat;
    private int iLon;
    
    private int iLayerID;
    private int iDetailLevel;
    private int renderImportances = 0; // Bitcoded integer representing which importances should be rendered
    private int emptyImportances = -1;
    private int loadedMaps = 0;
    
    private int renderStringImportance = 0;
    
    private String iTileID;
    
    private Vector[] geoDataVector;
    private long []tileMapTimeStamp;
    private long []tileNoticeSortArray;
    
    public TileMapWrapper(int aLayerID, int aDetailLevel, int aLat, int aLon, 
            String aTileID, int aNbrOfImp, int aMaxNbrImp) {
        iLat = aLat;
        iLon = aLon;
        iLayerID = aLayerID;
        iDetailLevel = aDetailLevel;
        
        geoDataVector = new Vector[Utils.MAX_LEVEL+1];
        for(int i=0; i<geoDataVector.length; i++) {
            geoDataVector[i] = new Vector(30);
        }
        
        clearStrings();
        iTileID = aTileID;
        nbrImportances = aNbrOfImp;                
        tileMapTimeStamp = new long[aMaxNbrImp];
        tileNoticeSortArray = new long[aMaxNbrImp];
    }
    
    public String getTileID() {
        return iTileID;
    }
    
    /**
     * @return true if the all the importance for the TileMap are downloaded
     */
    public boolean isDone() {
        return (emptyImportances>=0)&&(~((loadedMaps^(emptyImportances))|0x1))<<(32-nbrImportances)==0;  
    }
    
    /**
     * @param importance
     * @param nbr
     */
    public void setTileNoticeSortValue(int importance, long nbr) {
        tileNoticeSortArray[importance] = nbr;
    }
    
    /**
     * @param importance
     * @return
     */
    public long getTileNoticeSortValue(int importance) {
        return tileNoticeSortArray[importance];
    }
    
    public boolean hasExpired(int updateTime){
        
        if(updateTime == 0) {
            return false;
        } else {
            for(int i=0; i<tileMapTimeStamp.length; i++) {
                if(tileMapTimeStamp[i]!=0 && (System.currentTimeMillis()-tileMapTimeStamp[i] > updateTime*60000L ))
                    return true;
            }
        }
        return false;       
    }
        
    //Should always be called in order
    public void addTileMap(TileMap tileMap, int importance, boolean isString) {
        if (!isString) {
            if (importance < tileMapTimeStamp.length)
                tileMapTimeStamp[importance] = tileMap.getTimestamp();
            loadedMaps |= (0x1<<importance);
            
            Vector newGeoData = tileMap.getGeoData();
            for (int i=0; i<newGeoData.size(); i++) {
                TileFeatureData tileFeatureData = (TileFeatureData)newGeoData.elementAt(i);
                tileFeatureData.setImportance(importance);              
                Vector geoData = geoDataVector[tileFeatureData.getLevel()];
                geoData.addElement(tileFeatureData);
            }
            
            // Sort the features in the tile in the right drawing order.  
            // Only done for map tiles (layerID 0) for detail level 0 and 1. 
            // For higher detail levels the visual effect is not visible so it's only
            // a waste of CPU time. 
            if (isDone() && (importance < nbrImportances) && (iLayerID == 0) && 
                  (tileMap.getTileMapParams().getDetailLevel() <= 1)) {
              
              long time = System.currentTimeMillis();
              nbrFeaturesToSort = 0;
              mergeSort();
              
              if(LOG.isTrace()) {
                    LOG.trace("TileMapWrapper.addTileMap()", 
                            "Features sorting time = "+(System.currentTimeMillis()-time)+" ms, nbrFeaturesToSort= "+nbrFeaturesToSort);
                }
            }
        } else {
            //It was a string
            int[] strIdx = tileMap.getStrIdxByFeatureIdx();
            String[] str = tileMap.getStringArray();
            renderStringImportance |= (0x1<<importance);
            
            for(int i=0; i<geoDataVector.length; i++) {
                Vector geoDatas = geoDataVector[i];
                synchronized(geoDatas) {
                    final int size = geoDatas.size();
                    for(int j=0; j<size; j++) {
                        TileFeatureData geoData = (TileFeatureData) geoDatas.elementAt(j);
                        
                        if(geoData.getImportance() == importance) {
                            int idx =-1;
                            try {
                                idx = strIdx[geoData.getFeatureIndex()];
                            }catch(ArrayIndexOutOfBoundsException e){
                                idx = -1;                               
                            }
                            
                            if(idx>-1) {
                                geoData.setText(str[idx]);
                            } else {
                                geoData.setText(null);
                            }
                        }
                    }
                }
            }
        }
    }
     
    // Temporary vector that holds the sorted elements until 
    // they can be added to the real vector again. 
    private Vector m_ms_combinedVector = new Vector();
    
    // For debug only
    private int nbrFeaturesToSort = 0;
    
    /*
     * Sort the features in the map in tile notice value order
     */
    public void mergeSort() {        
        for (int i=0; i<geoDataVector.length; i++) {
            Vector v = geoDataVector[i];
            final int size = v.size();
            nbrFeaturesToSort += size;
            tileNoticeMergeSort(v, 0, size-1);            
        }        
    } 

    /*
     * Sort the features in tile notice value order using merge sort. 
     */
    private void tileNoticeMergeSort(Vector v, int start, int end ) {
        
        // Check that it's items to sort...
        if ((end-start) >= 1 ) {            
            int middle = (start+end) / 2;
            
            // Split the vector in half and sort each half with recursive calls. 
            tileNoticeMergeSort(v, start, middle );
            tileNoticeMergeSort(v, middle+1, end );
            
            // Merge the sorted "sub-vectors" 
            merge(v, start, middle, end);
        }
    }
     
     /*
      * Merge two sorted "sub-vectors" into one sorted vector
      */
    private void merge(Vector v, int left, int middle, int right ) {
         
        int leftIndex = left;
        int rightIndex = (middle+1);
        m_ms_combinedVector.removeAllElements();

        // Merge arrays until reaching end of either
        while (leftIndex <= middle && rightIndex <= right) {            
            if(tileNoticeSortArray[((TileFeatureData) v.elementAt(leftIndex)).getImportance()] <= 
                tileNoticeSortArray[((TileFeatureData) v.elementAt(rightIndex)).getImportance()]) {
                
                m_ms_combinedVector.addElement(v.elementAt(leftIndex++)); 
            } else {
                m_ms_combinedVector.addElement(v.elementAt(rightIndex++));
            }
        }
      
        if(leftIndex == (middle+1)) {
           // If left vector is empty, copy in rest of right vector
           while ( rightIndex <= right )
               m_ms_combinedVector.addElement(v.elementAt(rightIndex++));
        } else {
            // The right vector is empty, copy in rest of left vector
            while ( leftIndex <= middle ) { 
                m_ms_combinedVector.addElement(v.elementAt(leftIndex++));
            }
        }

        // Copy the sorted TileFeatureData into original vector v. 
        int cnt = 0;
        for (int i = left; i <= right; i++) {
            Object obj = m_ms_combinedVector.elementAt(cnt);
            v.setElementAt(obj, i);
            cnt++;
        }
    }
    
    public void setRender(int importance, boolean shouldRender) {
        if (shouldRender) {
            renderImportances |= (0x1 << importance);
        } else {
            renderImportances &= ~(0x1 << importance);
        }
    }
    
    public void setAllRendered() {
        renderImportances = loadedMaps;
    }
    
    public boolean isGeoMapLoaded(int importance) {
        return ((loadedMaps & (0x1 << importance)) > 0);
    }
    
    public int getRender() {
        return renderImportances;
    }
    
    public int getRenderStringImportance() {
        return renderStringImportance;
    }
    
    public int getLodaded() {
        return loadedMaps;
    }
    
    public boolean shouldRender(int importance) {
        return ((renderImportances & (0x1 << importance)) > 0);
    }
 
    /**
     * 
     * @return the latitude tile index
     */
    public int getLatitude() {
        return this.iLat;
    }
    
    /**
     * 
     * @return the longitude tile index
     */
    
    public int getLongitude() {
        return this.iLon;
    }
    
    public int getLayerID() {
        return iLayerID;
    }
    
    public int getDetailLevel() {
        return iDetailLevel;
    }
    /**
     * Returns the geodatas that should be drawn at this draw level(order).
     * @param drawLevel Drawing level (order).
     * @return Geodatas that should be drawn at the specified draw level.
     */
    public Vector getGeoData(int drawLevel) {
        return geoDataVector[drawLevel];
    }
    
    public void setEmptyImportances(int emptyImportances) {
        this.emptyImportances = emptyImportances;
    }
    
    public int getEmptyImportances() {
        return emptyImportances;
    }
    
    public boolean isEmptyImportances(int importance) {     
        if(emptyImportances == -1)
            return false;
        else        
            return ((emptyImportances & (0x1 << importance)) > 0);
    }
    
    public void setNbrImportances(int nbrImp) {
        nbrImportances = nbrImp;
    }
    
    public int getNbrImportances() {
        return nbrImportances;
    }
    
  //------------ Test to add strings -----------------------------------------------------------------
    
    public void clearStrings() {
        m_textsPlaced = false;
        usedStrings.clear();
        m_textDrawObjects.removeAllElements();
    }
    
    private Vector m_textDrawObjects = new Vector();
    
    public Vector getTextDrawObjects() {
        return m_textDrawObjects;
    }
    
    private Hashtable usedStrings = new Hashtable();
    
    public boolean isUsedString(String s) {
        return usedStrings.containsKey(s);
    }
    
    private boolean m_textsPlaced = false;
        
    public boolean placeTexts(ScreenInfo screenInfo, WFGraphicsFactory graphicsFactory, TileMapFormatDesc tmfd, WFGraphics g) {
        
        // Return true if the texts are already placed
        if (m_textsPlaced) {
            return true;
        }
        
        boolean returnValue = false;
        for (int level=0; level<Utils.MAX_LEVEL; level++) {
            Vector geoDatas = geoDataVector[level];
            for (int i=0; i<geoDatas.size(); i++) {
                TileFeatureData geoData = (TileFeatureData) geoDatas.elementAt(i);
                String text = geoData.getText();
                TextPlacementInfo textPlacementInfo = geoData.getTextPlacementInfo();
                 
                if (geoData.getPrimitiveType() == TileFeature.LINE ) {                    
                    // Check if the name of the line and text placement info exists 
                    if (textPlacementInfo != null && textPlacementInfo.containsInfo()) {
                        int textWidth = 0, textHeight = 0;
                        int startX = textPlacementInfo.getStartX();
                        int startY = textPlacementInfo.getStartY();
                        int stopX = textPlacementInfo.getStopX();
                        int stopY = textPlacementInfo.getStopY();
                        int length = textPlacementInfo.getLength();
                        int centerX = startX/2 + stopX/2;
                        int centerY = startY/2 + stopY/2;
                        
                        boolean isHighwayText = !(geoData.getFeatureType() != 5 || geoData.getText().length() >= 6);
                        DrawBoundingBox textBoundingBox = null;
                        
                        if (!isHighwayText) {
                            textWidth = Utils.get().getFont(Utils.FONT_SMALL).getStringWidth(text);
                            textHeight = Utils.get().getFont(Utils.FONT_SMALL).getFontHeight();
                            
                            // If text doesn't fit within text placement area, don't draw
                            if (textWidth > length) {
                                continue; 
                            }
                            
                            // Calculate the text's bounding box
                            int dx = stopX - startX;
                            int dy = stopY - startY;
                            textBoundingBox = calculateRotatedTextBoundingBox(centerX, centerY, dx, dy, textHeight, textWidth);
                        } else {
                            textWidth = Utils.get().getFont(Utils.FONT_SMALL_BOLD).getStringWidth(text);
                            textHeight = Utils.get().getFont(Utils.FONT_SMALL_BOLD).getFontHeight();
                            
                            textBoundingBox = new DrawBoundingBox(centerX-(textWidth>>1)-3, centerY-(textHeight>>1)-3, 
                                    centerX+(textWidth>>1)+3, centerY+(textHeight>>1)+3);
                        }
                        
                        boolean cull = false;
                        
                        for (int j=0; j<m_textDrawObjects.size(); j++) {
                            DrawBoundingBox b = ((TextDrawObject) m_textDrawObjects.elementAt(j)).getBoundingBox();
                            if (textBoundingBox.intersectsWith(b)) {
                                cull = true;
                                break;
                            }
                        }
                        
                        if (cull) {
                            continue;
                        }
                        
                        double tanTheta = 0.0;
                        int[] textImage = null;
                        
                        if (g.supportRotatedTexts()) {
                            tanTheta = (double)(stopY - startY) / (double)(stopX - startX);
                        } else if (!isHighwayText) {
                            textImage = cretateTextImage(text, textWidth, textHeight, 
                                    textPlacementInfo, length, g, graphicsFactory, tmfd);
                        }
                     
                        TextDrawObject textDrawObject = new TextDrawObject(text, textBoundingBox, 
                                centerX, centerY, tanTheta, isHighwayText, textImage);
                        m_textDrawObjects.addElement(textDrawObject);                       
                        returnValue = true;
                    }
                }
            }
        }
        
        // We don't place texts until all are extracted. That's why
        // we know we don't have to re-place texts until we have 
        // moved the map.
        m_textsPlaced = true;
        return returnValue;
    }
           
    /**
     * Calculates a bounding box for a rotated text
     * 
     * @param centerX  x coordinate of the text's center point
     * @param centerY  y coordinate of the text's center point
     * @param dx  x component of vector aligned with the text
     * @param dy  y component of vector aligned with the text
     * @param textHeight  height of the text
     * @param textWidth  width of the text
     * 
     * @return a bounding box for the text
     */
    private DrawBoundingBox calculateRotatedTextBoundingBox(int centerX, int centerY, 
            int dx, int dy, int textHeight, int textWidth) {
        // Calculate points at start and end of the text
        float d = (float) Math.sqrt(dx*dx + dy*dy);
        float scale = (float)textWidth / d;
        int p1x = centerX + (int) (scale*dx)/2;
        int p1y = centerY + (int) (scale*dy)/2;
        int p2x = centerX - (int) (scale*dx)/2;
        int p2y = centerY - (int) (scale*dy)/2;
        
        // Normalized orthogonal vector
        float v2x = (float) dy / d;
        float v2y = (float) -dx / d;
        
        // Calculate the four rectangle corners
        int p11x = p1x + (int) (v2x*textHeight)/2;
        int p11y = p1y + (int) (v2y*textHeight)/2;
        int p12x = p1x - (int) (v2x*textHeight)/2;
        int p12y = p1y - (int) (v2y*textHeight)/2;
        int p21x = p2x + (int) (v2x*textHeight)/2;
        int p21y = p2y + (int) (v2y*textHeight)/2;
        int p22x = p2x - (int) (v2x*textHeight)/2;
        int p22y = p2y - (int) (v2y*textHeight)/2;
        
        // Determine the bounding box
        int minX, minY, maxX, maxY;
        if (p1x <= p2x) {
            minX = Math.min(p11x, p12x);
            maxX = Math.max(p21x, p22x);
        } else {
            minX = Math.min(p21x, p22x);
            maxX = Math.max(p11x, p12x);
        }
        if (p1y <= p2y) {
            minY = Math.min(p11y, p12y);
            maxY = Math.max(p21y, p22y);
        } else {
            minY = Math.min(p21y, p22y);
            maxY = Math.max(p11y, p12y);
        }
        
        return new DrawBoundingBox(minX, minY, maxX, maxY);
    }
    
    /**
     * Creates a text image from the specified text
     * 
     * @param text
     * @param stringWidth
     * @param stringHeight
     * @param textPlacementInfo
     * @param length
     * @param g
     * @param graphicsFactory
     * @param tmfd
     * 
     * @return a raw image representation of the rotated text
     */
    private int[] cretateTextImage(String text, int stringWidth, int stringHeight, 
            TextPlacementInfo textPlacementInfo, float length, WFGraphics g, 
            WFGraphicsFactory graphicsFactory, TileMapFormatDesc tmfd) {
        
        int startX = textPlacementInfo.getStartX();
        int startY = textPlacementInfo.getStartY();
        int stopX = textPlacementInfo.getStopX();
        int stopY = textPlacementInfo.getStopY();
                            
        stringWidth += 2;
        stringHeight += 2;
       
        WFImage image = graphicsFactory.createWFImage(stringWidth, stringHeight);
        WFGraphics imgGraphics = image.getWFGraphics();                            
        imgGraphics.setColor(0x00000000);
        imgGraphics.fillRect(0, 0, stringWidth, stringHeight);

        imgGraphics.setFont(Utils.get().getFont(Utils.FONT_SMALL));
        imgGraphics.setColor(0xFFFFFFFF);   
        
        // Draw and rotate image with outline of text before image of real text
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 3; k++) {
                if (j == 1 && k == 1) {
                    continue;
                }
                imgGraphics.drawText(text, j, k, WFGraphics.ANCHOR_TOP | WFGraphics.ANCHOR_LEFT);                                 
            }
        }

        int[] imgData = new int[stringWidth * stringHeight + 2];
        imgData[0] = stringWidth;
        imgData[1] = stringHeight;
        image.getARGBData(imgData, 2, stringWidth, 0, 0, stringWidth, stringHeight);                   
        
        int dx = stopX - startX;
        int dy = stopY - startY;
        float cosTheta = dx / length;
        float sinTheta = (float)Math.sqrt(1 - (cosTheta * cosTheta));
        if (dx < 0) {
            cosTheta = -cosTheta;
            sinTheta = -sinTheta;
        }
        if (dy < 0) {
            sinTheta = -sinTheta;
        }
        
        float abssin = Math.abs(sinTheta);
        
        int imgWidth = (int)(stringHeight * abssin + stringWidth * cosTheta);
        int imgHeight = (int)(stringHeight * cosTheta + stringWidth * abssin);
        int[] rotatedImgData = new int[imgHeight * imgWidth + 2];
        rotatedImgData[0] = imgWidth;
        rotatedImgData[1] = imgHeight;
        
        rotateImage(imgData, rotatedImgData, sinTheta, cosTheta, true, tmfd);
        // Draw and rotate image with real text
        imgGraphics.setColor(0x0);
        imgGraphics.fillRect(0, 0, stringWidth, stringHeight);
        imgGraphics.setColor(0xFFFFFFFF);                                                  
        imgGraphics.drawText(text, 1, 1, WFGraphics.ANCHOR_TOP | WFGraphics.ANCHOR_LEFT);
        image.getARGBData(imgData, 2, stringWidth, 0, 0, stringWidth, stringHeight);                                
        rotateImage(imgData, rotatedImgData, sinTheta, cosTheta, false, tmfd);

        return rotatedImgData;
    }
    
    private void rotateImage(int[] src, int[] dest, float sinTheta, float cosTheta, boolean isOutline, TileMapFormatDesc tmfd) {
        
        int srcWidth = src[0];
        int srcHeight = src[1];
        int destWidth = dest[0];
        int destHeight = dest[1];
        
        int halfSrcWidth = srcWidth >> 1;
        int halfSrcHeight = srcHeight >> 1;
        int halfdestWidth = destWidth >> 1;
        int halfdestHeight = destHeight >> 1;
        
        for (int xScreen = 0; xScreen < destWidth; xScreen++) {
            float xCentered = xScreen - halfdestWidth;
            float xCentered_cosTheta = xCentered * cosTheta;
            float xCentered_sinTheta = xCentered * sinTheta;
            
            for (int yScreen = 0; yScreen < destHeight; yScreen++) {
                final float yCentered = yScreen - halfdestHeight;
                float xRotated = xCentered_cosTheta + yCentered * sinTheta;
                float yRotated = -xCentered_sinTheta + yCentered * cosTheta;
                xRotated += halfSrcWidth;
                yRotated += halfSrcHeight;
                
                long xMagnified = (long)(xRotated * 0xffff);
                long yMagnified = (long)(yRotated * 0xffff);
                
                int xInteger = (int)(xMagnified >> 16);
                int yInteger = (int)(yMagnified >> 16);
                
                int xDecimal = (int)(xMagnified & 0xffff);
                int yDecimal = (int)(yMagnified & 0xffff);
                int xRemainder = 0x10000 - xDecimal;
                int yRemainder = 0x10000 - yDecimal;
                
                final int srcInArray = yInteger * srcWidth + xInteger + 2;
                
                int percentage_tl = (int)((long)xRemainder * (long)yRemainder >> 16);
                int percentage_tr = (int)((long)xDecimal * (long)yRemainder >> 16);
                int percentage_bl = (int)((long)xRemainder * (long)yDecimal >> 16);
                int percentage_br = (int)((long)xDecimal * (long)yDecimal >> 16);
                
                int pixel_tl;
                int pixel_tr;
                int pixel_bl;
                int pixel_br;
                
                if (yInteger<srcHeight && xInteger<srcWidth && xInteger>-1 && yInteger>-1) {
                    pixel_tl = src[srcInArray];
                } else {
                    pixel_tl = 0;
                }
                if (yInteger<srcHeight && xInteger+1<srcWidth && xInteger>-2 && yInteger>-1) {
                    pixel_tr = src[srcInArray + 1];
                } else {
                    pixel_tr = 0;
                }
                if (yInteger+1<srcHeight && xInteger<srcWidth && xInteger>-1 && yInteger>-2) {
                    pixel_bl = src[srcInArray + srcWidth];
                } else {
                    pixel_bl = 0;
                }
                if (yInteger+1<srcHeight && xInteger+1<srcWidth && xInteger>-2 && yInteger>-2) {
                    pixel_br = src[srcInArray + srcWidth + 1];
                } else {
                    pixel_br = 0;
                }
                
                int tl = pixel_tl & 0xff;
                int tr = pixel_tr & 0xff;
                int bl = pixel_bl & 0xff;
                int br = pixel_br & 0xff;
                
                int opacity = tl * percentage_tl + tr * percentage_tr + bl * percentage_bl + br * percentage_br >> 16;
                
                int destInArray = yScreen * destWidth + xScreen + 2;                
               
                int colour;
                if (isOutline)
                    colour = (opacity << 24) | tmfd.getBackgroundColor();
                else if(opacity > 50)
                    colour = (opacity << 24) | tmfd.getTextColor();
                else  //using the outline color calculated before                 
                  continue;                
                
                dest[destInArray] = colour;
            }
        }
    }
    
    /**
     * For debug only
     * 
     * @return
     */
    
    public int getLoadedMaps() {
        return ((0x1<<31) | loadedMaps);
    }
    
}
