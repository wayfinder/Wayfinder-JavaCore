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

import java.util.*;

import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.MapObjectImage;
import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.internal.VectorMapOptimizationFilter;
import com.wayfinder.core.map.vectormap.internal.control.ConcavePolygon;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.process.TextPlacementInfo;
import com.wayfinder.core.map.vectormap.internal.process.TileFeature;
import com.wayfinder.core.map.vectormap.internal.process.TileFeatureData;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.IntVector;
import com.wayfinder.core.shared.util.qtree.QuadTree;
import com.wayfinder.core.shared.util.qtree.QuadTreeNode;
import com.wayfinder.pal.graphics.WFFont;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;

/**
 * Class for handling the actual drawing of vector data.
 */
public class MapDrawer {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(MapDrawer.class);
    
    private WFGraphics g = null;
    
    // Maximal number of MapObjects that each node in the QuadTree can hold
    private static final int MAX_NBR_MAP_OBJECTS_PER_NODE = 20;
    
    private QuadTree iMapObjectsQuadTree;
    private Vector iMapObjectQuadTreeNodes;
    private Vector iVisibleMapObjects;    
    private Hashtable iMapObjectImages = new Hashtable();    
    
    private Hashtable m_TrackingStringAdded;
    private Vector m_poiBoxes;
    private Vector objectBoxes;
    private Vector bitmapsToBeRendered;
    private Vector tracking2DTextBoundingBoxes;
    
    private float iCurrentZoomLevel;
    
    private Vector m_cityCenterObjects;
    
    private int clipLine;
    private long[] camBoxScreenCoords;
    private float[] clipNearVectorNorm;
    
    private boolean tracking = false;
    
    private float[] extrude = new float[2];
    private int[] screenMin = new int[2];
    private int[] screenMax = new int[2];
    private byte[] verticesToClip = null;
    private float[] featureToPoint = new float[2];
    private int[] textpos = new int[3];
    private int[] transformable = new int[2];
    
    private int[] m_vertices;
    private int[] m_pointsX;
    private int[] m_pointsY;

    private int[] iLineStartVertex = new int[2];
    private int[] iLineEndVertex = new int[2];
    
    // Storage for transformed end points of previous line
    private int[] iPrevLineTransformedEndPoints = new int[4];
    
    // Storage for start point of previous segment
    private int iPrevSegmentStartPointX;
    private int iPrevSegmentStartPointY;
    
    // Indicate that this is the first line of a polyline
    private boolean iFirstLineInPolyLine = false;
    
    // Indicate that we should draw a triangle at the end of the line
    private boolean iLastLineInPolyLine = false;
    
    // Indicate that we should draw a reversed gap triangle
    private boolean iDrawReversedGapTriangle = false;
    
    // indicate that the POIs from the server should be shown
    private boolean m_shouldShowServerPOIs = true;
    
    // Transformed vertices of gap triangle and reversed gap triangle
    private int[] iGapTriangleTransformedVertices = new int[6];
    private int[] iReversedGapTriangleTransformedVertices = new int[6];
    
    // Current detail level in map
    private int iCurrentDetailLevel;
    
    private RenderManager m_RenderManager;
    private TileMapFormatDesc m_Tmfd = null;
 
    private ScreenInfo iScreenInfo = null;
    
    // Buffers for storing screen coordinates
    private int m_screenCoordsBufferSize = 0;
    private int m_screenCoordsBufferCapacity = 256;
    private int[] m_screenXCoordsBuffer = new int[m_screenCoordsBufferCapacity];
    private int[] m_screenYCoordsBuffer = new int[m_screenCoordsBufferCapacity];
    
    /**
     * Creates a new MapDrawer
     * 
     * @param aRenderManager
     */
    public MapDrawer(RenderManager aRenderManager) {
        
        m_RenderManager = aRenderManager;
        
        iMapObjectsQuadTree = new QuadTree(Integer.MIN_VALUE/2, Integer.MIN_VALUE, 
                Integer.MAX_VALUE/2, Integer.MAX_VALUE, 
                MAX_NBR_MAP_OBJECTS_PER_NODE, "root");
        iMapObjectQuadTreeNodes = new Vector();
        iVisibleMapObjects = new Vector();
        
        bitmapsToBeRendered = new Vector();
        m_poiBoxes = new Vector();
        objectBoxes = new Vector();
        m_cityCenterObjects = new Vector();
        tracking2DTextBoundingBoxes = new Vector();        
        clipNearVectorNorm = new float[2];
        m_TrackingStringAdded = new Hashtable();
    }
    
    public void setScreenInfo(ScreenInfo si) {
        iScreenInfo = si;
        clipLine = iScreenInfo.getHalfScreenHeight();
        int halfWidth = iScreenInfo.getHalfScreenWidth();
        
        camBoxScreenCoords =  new long[] {
                -halfWidth, clipLine, halfWidth, clipLine
        };
    }
    
    public void setGraphics(WFGraphics g) {
        this.g = g;
        int halfWidth = iScreenInfo.getHalfScreenWidth();
        int halfHeight = iScreenInfo.getHalfScreenHeight();
        clipLine = halfHeight;
        camBoxScreenCoords =  new long[]{-halfWidth,clipLine,halfWidth,clipLine};
    }
    
    /**
     * Sets the tracking flag
     *
     * @param tracking true if tracking
     */
    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }
    
    
    private int []iNbrOfImportanceByLayerNbr;

    private int m_halfWidth;

    private int m_halfHeight;

    private boolean m_cameraIsPanned;
    /**
     * Set the number of importance for the current layer and scale. This
     * to only have to calculate it ones for each scale. 
     * 
     * @param tmfd, the current used TileMapFormatDesc.
     * @param aZoomLevel, the current zoomlevel from the Camera class
     */
    void init(TileMapFormatDesc tmfd, float aZoomLevel, WFGraphics aG) {
        
        setGraphics(aG);
        m_Tmfd = tmfd;
        
        if(iNbrOfImportanceByLayerNbr == null || 
                iNbrOfImportanceByLayerNbr.length < m_Tmfd.getNumberOfLayers()) {
            iNbrOfImportanceByLayerNbr = new int[m_Tmfd.getNumberOfLayers()];
        }
                
        if(iCurrentZoomLevel != aZoomLevel) {
            int layerID, detailLevel;
            final int zoomInt = (int) aZoomLevel;
            for(int layerNbr=0; layerNbr<m_Tmfd.getNumberOfLayers(); layerNbr++) {
                layerID = m_Tmfd.getLayerIDFromLayerNbr(layerNbr);
                detailLevel = m_Tmfd.getCurrentDetailLevel(layerNbr, zoomInt);
                iNbrOfImportanceByLayerNbr[layerNbr] = m_Tmfd.getNbrImportances(zoomInt, detailLevel, layerID);
            }
        }
        
        iCurrentDetailLevel = m_Tmfd.getCurrentDetailLevel(m_Tmfd.getLayerNbrFromID(0), (int)aZoomLevel);
        iCurrentZoomLevel = aZoomLevel;
        
        m_poiBoxes.removeAllElements();               
        //Clears the "bitmaps-to-be-rendered-queue"
        bitmapsToBeRendered.removeAllElements();
        iDrawedPoiBoxes.clear();        
        // Clears the Vector containing the bounding boxes for the polygons
        objectBoxes.removeAllElements();    
        // Resets text data for city centres               
        m_cityCenterObjects.removeAllElements();  
        m_TrackingStringAdded.clear();
    }
    
    
    public void initDrawing(Camera camera) {
        final ScreenInfo screenInfo = iScreenInfo;
        int halfWidth = screenInfo.getHalfScreenWidth();
        int halfHeight = screenInfo.getHalfScreenHeight();

        final long[] camBoxScreenCoords = this.camBoxScreenCoords;
        int clipLine = this.clipLine;
        camBoxScreenCoords[0] = -halfWidth;
        camBoxScreenCoords[1] = clipLine;
        camBoxScreenCoords[2] = halfWidth;
        camBoxScreenCoords[3] = clipLine;
        final float[] clipNearVectorNorm = this.clipNearVectorNorm;
        clipNearVectorNorm[0] = 0;
        clipNearVectorNorm[1] = 0;
        
        if (camera.isIn3DMode()) {
            long[] camP1 = camera.getWorldCoordinateInternal((int)camBoxScreenCoords[0], (int)camBoxScreenCoords[1]);
            camBoxScreenCoords[0] = camP1[0];
            camBoxScreenCoords[1] = camP1[1];
            
            long[] camP2 = camera.getWorldCoordinateInternal((int)camBoxScreenCoords[2], (int)camBoxScreenCoords[3]);
            camBoxScreenCoords[2] = camP2[0];
            camBoxScreenCoords[3] = camP2[1];
            
            // vector to use for clipping [x1-x2,y1-y2]
            clipNearVectorNorm[0] = camBoxScreenCoords[0] - camBoxScreenCoords[2];
            clipNearVectorNorm[1] = camBoxScreenCoords[1] - camBoxScreenCoords[3];
            Utils.normalize(clipNearVectorNorm);
            
            m_cameraIsPanned = true;
        } else {
//            if (g.supportsTransforms()) {
//                g.setTransformMatrix(createPalMatrix(camera.getTransform(), halfWidth, halfHeight));
//            }
            m_cameraIsPanned = false;
        }
        
        m_halfWidth = halfWidth;
        m_halfHeight = halfHeight;
    }
    
    /**
     * Draws the current vector data.
     * 
     * The init(TileMapFormatDesc tmfd, int aZoomLevel) method must be called 
     * before to update the current zoomlevel and number of importance for each
     * layer. 
     *
     * @param tmw  tilemap wrapper, contains the data to draw
     * @param level  draw order level (0-15)
     * @param pass  draw pass, pass 1 renders background objects, pass 2 renders text and roadss
     * @param startPass  draw pass to start with
     * @param camBox  camera's bounding box
     * @param isMoving  true if cameras is moving, false otherwise 
     * @param tracking2Dstrings  strings used when tracking in 2D mode
     * @param isOverview  true if drawing overview maps, false otherwise
     * @param camera  the camera
     */
    public void drawMap(TileMapWrapper tmw, int level, int pass,int startPass, int[] camBox, Vector tracking2Dstrings, Camera camera, int scaleIndex) {
        Vector geoDatas = tmw.getGeoData(level);
        final int size = geoDatas.size();
        if (size == 0) {
            return;
        }

        final TileMapFormatDesc tmfd = m_Tmfd;
        int numberOfImportance = iNbrOfImportanceByLayerNbr[tmfd.getLayerNbrFromID(tmw.getLayerID())];

        // Lookup frequently used member variables
        final boolean shouldShowServerPOIs = m_shouldShowServerPOIs;
        final int[] screenMin = this.screenMin;
        final int[] screenMax = this.screenMax;
        final int[] textpos = this.textpos;
        final Hashtable trackingStringAdded = m_TrackingStringAdded;
        final boolean tracking = this.tracking;
        final float zoomLevel = iCurrentZoomLevel;
        final int halfWidth = m_halfWidth;
        final int halfHeight = m_halfHeight;
        final boolean cameraIsPanned = m_cameraIsPanned;
        final WFGraphics g = this.g;
        
        float[][] newtransform = camera.getTransform();

        // For all features in the current level
        for (int i=0; i<size; i++) {
            
            TileFeatureData geoData = (TileFeatureData)geoDatas.elementAt(i);
            int[] ip = geoData.getImportanceAndPrimitiveType();
            int importance = ip[0];
            int primitiveType = ip[1];
            
            /* Don't draw more importance than are defined for the current scale */
            if(importance >= numberOfImportance) {
                continue;
            }
            
            /* Don't draw importance that are not yet set to be rendered */ 
            if(!tmw.shouldRender(importance)) {
                continue;
            }
            
            //--------------------------------------------------------------------------------------------
            // Draw bitmaps
            
            if(primitiveType == TileFeature.BITMAP) {
                /* Don't draw bitmap in pass 0 */
                if (pass==0 || !shouldShowServerPOIs) {
                    continue;
                }
                
                final int[] latlon = geoData.getLatLon();
                
                if (latlon[0] >= camBox[1] || 
                    latlon[0] <= camBox[0] || 
                    latlon[1] >= camBox[3] || 
                    latlon[1] <= camBox[2]) {
                    continue;
                }
                
                int maxScale = geoData.getMaxScale();
                /* 
                 * The current bitmaps should be shown on the screen 
                 * (i.e. the zoomlevel are lower then max scale). 
                 * 
                 * Or the maxScale are not set i.e. it's a city center bitmap
                 *  
                 */
                if (maxScale >= zoomLevel || maxScale == -1) {
                    
                    String url = TileMapControlThread.getString(geoData.getBitmapIndex());
                    WFImage bitMapImage = null;
                    
                    if (url != null) {
                        bitMapImage = m_RenderManager.getBitmapImage(url);
                    }
                    
                    if (bitMapImage != null) {

                    	screenMin[0] = latlon[0];
                        screenMin[1] = latlon[1];
                        screenMax[0] = latlon[0];
                        screenMax[1] = latlon[1];
                        if (camera.isIn3DMode()) {
                            applyTransform(screenMax, 2, newtransform);
                            applyTransform(screenMin, 2, newtransform);
                        } else {
                            applyTransformUnpanned(screenMax, 2, newtransform);
                            applyTransformUnpanned(screenMin, 2, newtransform);
                        }
                        
                        screenMin[0] += halfWidth;
                        screenMin[1] += halfHeight;
                        screenMax[0] += halfWidth;
                        screenMax[1] += halfHeight;
                        
                        String text = geoData.getText();
                        // City centre feature
                   	    if (text != null) {
                            final int featureType = geoData.getFeatureType(); 
           	            	if (featureType >= 23 && featureType <= 30) {
	       	                    // Use larger font for main city centres
    	                        WFFont font = null;
	                            if (featureType == 23) {
                        	        font = Utils.get().getFont(Utils.FONT_MEDIUM_BOLD);
                    	        } else {
                	                font = Utils.get().getFont(Utils.FONT_SMALL_BOLD);
            	                }
        	                    
    	                        int textWidth = font.getStringWidth(text);
	                            int textHeight = font.getFontHeight();
                            	int x = screenMin[0] - textWidth / 2;
                        	    int y = screenMin[1] + textHeight / 4; 
                    	        DrawBoundingBox bbox = new DrawBoundingBox(x, y, x+textWidth, y+textHeight);
                	            
            	                if (!checkCityCentresCollission(bbox)) {
        	                        CityCentreObject ccObj = new CityCentreObject(text, font, bbox);
    	                            m_cityCenterObjects.addElement(ccObj);
	                            }
                        	}
                        }
                        
                        screenMax[0] +=23;//Temporary hard code. These values are
                        screenMin[1] -=23;//adjusted when image is used later on.
                        
                        POIBox poiBox = new POIBox(screenMin[0], screenMin[1], screenMax[0], screenMax[1], 
                                new Position(latlon[0], latlon[1]), text, tmw, importance);
                        
                        // Adds the bitmap to the map
                        int[] poipoint = {latlon[0], latlon[1]};
                        final Vector bitmapsToBeRendered = this.bitmapsToBeRendered;
                        bitmapsToBeRendered.addElement(poipoint);
                        bitmapsToBeRendered.addElement(bitMapImage);
                        bitmapsToBeRendered.addElement(poiBox);
                        
                        m_poiBoxes.addElement(poiBox);
                    }
                }
                continue;
            }
             
            int[] coordExtremes = geoData.getCoordExtremes();
            int minX = coordExtremes[0];
            int maxX = coordExtremes[1];
            int minY = coordExtremes[2];
            int maxY = coordExtremes[3];
            
            /* Cull off-screen objects */
            if((minX>=camBox[1] || maxX<=camBox[0] || 
                     minY>=camBox[3] || maxY<=camBox[2])) {
                if (primitiveType == TileFeature.LINE) {
                    // Make sure no off-screen texts are left on-screen.
                    geoData.setTextPlacementInfo(null);
                }
                continue;
            }
            
            //--------------------------------------------------------------------------------------------
            // Draw Polygons
            if (primitiveType == TileFeature.POLYGON){
                if (pass == 1) {
                    /* Create objectBoxes that are used to draw the name of the bua in the blue box when
                     * we move the cursor over it */
                    final String text = geoData.getText();
                    if (text != null) {
                        screenMin[0] = minX;
                        screenMin[1] = minY;
                        screenMax[0] = maxX;
                        screenMax[1] = maxY;
                        if (camera.isIn3DMode()) {
                            applyTransform(screenMax, 2, newtransform);
                            applyTransform(screenMin, 2, newtransform);
                        } else {
                            applyTransformUnpanned(screenMax, 2, newtransform);
                            applyTransformUnpanned(screenMin, 2, newtransform);
                        }
    
                        screenMin[0] += halfWidth;
                        screenMin[1] += halfHeight;
                        screenMax[0] += halfWidth;
                        screenMax[1] += halfHeight;
                    
                        objectBoxes.addElement(new ObjectBox(screenMin[0], screenMin[1], screenMax[0], screenMax[1], text, geoData.getLevel()));                    
                    }
                    
                    final ConcavePolygon cp = geoData.getConcavePolygon();
                    if (cp!=null) {          
                        final int []coords = geoData.getCoords();
                        final int length = coords.length;
                        int[] vertices = this.m_vertices;
                        if (vertices == null || vertices.length < length) {
                            m_vertices = new int[length];
                            m_pointsX = new int[length >> 1];
                            m_pointsY = new int[length >> 1];
                            vertices = m_vertices;
                        }
                        System.arraycopy(coords, 0, vertices, 0, length);
                        if (cameraIsPanned) {
                            setVerticesToClip(coords);
                            drawConcavePolygonWithClipping(cp, vertices, length, geoData.getColor(scaleIndex), verticesToClip, newtransform);
//                            } else if (g.supportsTransforms()) {
//                                g.setColor(geoData.getColor(scaleIndex));
//                                g.fillPolygonT(vertices, length);
                        } else {
                            drawConcavePolygon(cp, vertices, length, geoData.getColor(scaleIndex), newtransform, startPass == 0 || startPass == -1);
                        }
                    }                
                }
            } 
            
            //--------------------------------------------------------------------------------------------------
            // Draw Lines
            else if (primitiveType == TileFeature.LINE ) {
                
                int width = geoData.getWidth(scaleIndex);
                int widthMeters = geoData.getWidthMeters(scaleIndex);
                width = getPixelWidth(camera, width, widthMeters);
                
                int color = geoData.getColor(scaleIndex);
                int borderColor = geoData.getBorderColor(scaleIndex);             
                String text = geoData.getText();
                int []coords = geoData.getCoords();

                //{longest length, x, y} 
                // Note: This variable is a cool variable to store the world 
                // coordinate for texts in 2D tracking. This is a tweak.
                textpos[0] = -1;
                textpos[1] = -1;
                textpos[2] = -1;
                
                TextPlacementInfo textPlacementInfo;           
                if (pass == 1 && (text != null)) {
                    textPlacementInfo = geoData.getTextPlacementInfo();
                    if (textPlacementInfo == null) {
                        textPlacementInfo = new TextPlacementInfo();
                    }
                } else {
                    textPlacementInfo = null;
                }
                geoData.setTextPlacementInfo(textPlacementInfo);
                
                boolean isOutline = (pass == 0 && borderColor != TileFeatureData.NO_BORDER_COLOR);
                if (camera.isIn3DMode()) {
                    setVerticesToClip(coords);
                }
                
                drawPolyLine(tmw.getLayerID(), coords, width, pass==0 ? borderColor:color, isOutline, 
                        verticesToClip, newtransform, camBoxScreenCoords, 
                        textpos, textPlacementInfo, camera);
                
                // Put texts to draw in a cool Vector
                if (tracking && (textpos[0]!=-1 && text!=null)) {
                    if(!trackingStringAdded.containsKey(text)) {
                        trackingStringAdded.put(text, text);
                        tracking2Dstrings.addElement(new TextPos(text, textpos[1], textpos[2]));
                    }
                }     
            }
        }
    }

    /**
     * Calculates the width in pixels from the current zoom level
     * 
     * @param camera
     * @param width
     * @param widthMeters
     * @return the width in pixels
     */
    private int getPixelWidth(Camera camera, int width, int widthMeters) {
        if (widthMeters != 0xFF) {
            int widthPixelsFromMeters = (int)((float)widthMeters / camera.getZoomLevel());
            // Use width in meters only if it's larger than width in pixels
            if (widthPixelsFromMeters < 0xFF && widthPixelsFromMeters > width) {
                width = widthPixelsFromMeters;
            }
        }
        return (int) (width * iScreenInfo.getDPICorrection());
    }
    
    private void setVerticesToClip(int []coords) {
        int nbrFeatures = coords.length >> 1;
        int nbrBytes = ((nbrFeatures) / 8) + ((nbrFeatures%8>0)?1:0);
        
        if (verticesToClip == null || verticesToClip.length < nbrBytes) {
            verticesToClip = new byte[nbrBytes];
        } else {
            for (int j = 0; j < verticesToClip.length; j++) {
                verticesToClip[j] = 0;
            }
        }
        
        for (int v = 0; v < coords.length; v+=2) {
            int x = coords[v];
            int y = coords[v+1];
            this.featureToPoint[0] = x-camBoxScreenCoords[2];
            this.featureToPoint[1] = y-camBoxScreenCoords[3];
            this.featureToPoint = Utils.normalize(this.featureToPoint);
            float cross = Utils.cross2D(clipNearVectorNorm,this.featureToPoint);
            if (cross < 0) {
                int featureNbr = v>>1;
                verticesToClip[featureNbr/8] |= (0x1<<(featureNbr%8));
            }
        }           
    }
    
    /**
     * Checks if a any city center collides with the specified
     * bounding box
     * 
     * @param bbox  bounding box to check
     * @return true if any city center collides, false otherwise
     */
    private boolean checkCityCentresCollission(DrawBoundingBox bbox) {       
        final int size = m_cityCenterObjects.size();
        for (int i=0; i<size; i++) {
            DrawBoundingBox compBbox = ((CityCentreObject) m_cityCenterObjects.elementAt(i)).getBoundingBox();
            if (compBbox.intersectsWith(bbox)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean collidesWithAnotherTracking2DString(int textWidth,int textHeight, int x , int y){
        int xMin = x;
        int xMax = x + textWidth;
        int yMin = y;
        int yMax = y + textHeight;
        final int size = tracking2DTextBoundingBoxes.size();
        for(int i=0;i<size;i++){
            Box box = (Box) tracking2DTextBoundingBoxes.elementAt(i);
            int otherXMin = box.getStartX();
            int otherXMax = box.getStartX() + box.getEndX();
            int otherYMin = box.getStartY();
            int otherYMax = box.getStartY() + box.getEndY();
            if(!(xMax < otherXMin || xMin > otherXMax || yMax < otherYMin || yMin > otherYMax)) {
                return true;
            }
        }
        tracking2DTextBoundingBoxes.addElement(new Box(xMin, yMin, xMax, yMax));
        return false;
    }
    
    /**
     * Draws the placed texts in the map.
     * 
     * @param textDrawObjects  text objects to be drawn
     * @param tmfd  instance of TileMapFormatDesc
     */
    public void drawPlacedTexts(Vector textDrawObjects, TileMapFormatDesc tmfd) {
        if (textDrawObjects == null) {
            throw new IllegalArgumentException("textDrawObjects is null");
        }
        
        for (int i=0; i<textDrawObjects.size(); i++) {
            TextDrawObject textDrawObject = (TextDrawObject) textDrawObjects.elementAt(i);
            int centerX = textDrawObject.getCenterX();
            int centerY = textDrawObject.getCenterY();
            
            g.setColor(tmfd.getTextColor());
            g.setFont(Utils.get().getFont(Utils.FONT_SMALL));
            
            int halfWidth = iScreenInfo.getHalfScreenWidth();
            int halfHeight = iScreenInfo.getHalfScreenHeight();
            
            if (textDrawObject.isHighwayText()) {
                String text = textDrawObject.getText();
                DrawBoundingBox b = textDrawObject.getBoundingBox();
                int x = b.getMinX() + halfWidth;
                int y = b.getMinY() + halfHeight;
                int boxWidth = b.getMaxX() - b.getMinX();
                int boxHeight = b.getMaxY() - b.getMinY();
                
                // Draw highway sign with road text
                WFFont highwayFont = Utils.get().getFont(Utils.FONT_SMALL_BOLD);
                g.setFont(highwayFont);
                g.setColor(Utils.HIGHWAY_SIGN_OUTLINE_COLOR);
                g.fillRect(x, y, boxWidth, boxHeight);
                
                g.setColor(Utils.HIGHWAY_SIGN_BACKGROUND_COLOR);
                g.fillRect(x+1, y+1, boxWidth-2, boxHeight-2);
                
                g.setColor(Utils.HIGHWAY_SIGN_TEXT_COLOR);
                x = centerX + halfWidth;
                y = centerY - highwayFont.getFontHeight()/2 + halfHeight;
                g.drawText(text, x, y, WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_TOP);
            } else {
                if (g.supportRotatedTexts()) {
                    String text = textDrawObject.getText();
                    double tanRotation = textDrawObject.getTanRotation();          
                    int x = centerX + halfWidth;
                    int y = centerY + halfHeight;                                                                
                    
                    // Draw text outline
//                    g.setColor(tmfd.getBackgroundColor());
//                    for (int j = x-1; j < x+2; j++) {
//                        for (int k = y-1; k < y+2; k++) {
//                            if (j == x && k == y) {
//                                continue;
//                            }
//                            g.drawRotatedText(text, j, k, tanRotation);                                   
//                        }
//                    }
                    
//                    if(LOG.isInfo()) {
//                        LOG.info("MapDrawer.drawTexts()", "text="+text+" ("+x+","+y+")");
//                    }
                    
                    // Draw text
                    g.setColor(tmfd.getTextColor());
                    g.drawRotatedText(text, x, y, tanRotation);
                } else {
                    int[] imgData = textDrawObject.getImageData();
                    int imgWidth = imgData[0];
                    int imgHeight = imgData[1];
                    int x = centerX - imgWidth/2 + halfWidth;
                    int y = centerY - imgHeight/2 + halfHeight;
                    g.drawRGB(imgData, 2, imgData[0], x, y, imgWidth, imgHeight, true);
                }
            }
        }
    }
    
    /* Array that holds bitmap collision detection */
    private IntVector iDrawedPoiBoxes = new IntVector(40);
    
    // vertices of a line (represented by two triangles) between two points
    private static int[] lineVertices = new int[8];

    /**
     * Draws a line between the points (x1,y1) and (x2,y2)
     *
     * @param x1 - start x
     * @param y1 - start y
     * @param x2 - end x
     * @param y2 - end y
     * @param lineWidth line width - pixels if > 0, meters if < 0
     * @param isOutline - true if outline, false otherwise
     * @param transform - camera transform
     * @param textPosition - text position (output argument)
     */
    private void drawLine(int x1, int y1, int x2, int y2, int lineWidth,
        boolean isOutline, float[][] transform, int[] textPosition, Camera cam) {
        
        double wInv = 0;
        
        // transformed coordinates
        int x1t, y1t, x2t, y2t;
        
        int halfWidth = m_halfWidth;
        int halfHeight = m_halfHeight;
        
        // handle 1px wide lines
        if (lineWidth  == 1) {
            wInv = 1.0 / (transform[3][0]*x1 + transform[3][1]*y1 + transform[3][3]);
            x1t = (int)((transform[0][0]*x1 + transform[0][1]*y1 + transform[0][3]) * wInv);
            y1t = (int)((transform[1][0]*x1 + transform[1][1]*y1 + transform[1][3]) * wInv);
            
            wInv = 1.0 / (transform[3][0]*x2 + transform[3][1]*y2 + transform[3][3]);
            x2t = (int)((transform[0][0]*x2 + transform[0][1]*y2 + transform[0][3]) * wInv);
            y2t = (int)((transform[1][0]*x2 + transform[1][1]*y2 + transform[1][3]) * wInv);
            
            g.drawLine(x1t+halfWidth, y1t+halfHeight, x2t+halfWidth, y2t+halfHeight, 1);
            return;
        }
        
        long dX = x1 - x2;
        long dY = y1 - y2;
        
        float comparableScale = -1; //Fix
        double lineLength = Math.sqrt(dX*dX+dY*dY);
        
        //If this road actually is longer than the previously largest segment
        if (tracking && !cam.isIn3DMode() && lineLength > textPosition[0]) {
            textPosition[0] = (int)lineLength;
            textPosition[1] = (int)(x1 - (dX>>1));
            textPosition[2] = (int)(y1 - (dY>>1));
        }
        
        float scale = -1;
        if (lineWidth > 0) {  //lineWidth in pixels
            lineWidth = lineWidth>>1;
            comparableScale = lineWidth;
            if (isOutline) {
                lineWidth += 2;
            }
            lineWidth = (int)(lineWidth * iCurrentZoomLevel);
            scale = (float)(((lineWidth>>1)*Utils.METER_TO_MC2SCALE) / (lineLength));
        } else if (lineWidth < 0) { //lineWidth in meters 
            lineWidth = -lineWidth;
            comparableScale = lineWidth / iCurrentZoomLevel;
            if (isOutline) {
                int lineWidthAddon = (int)(2 * iCurrentZoomLevel);
                if (lineWidthAddon < 2) {
                    lineWidthAddon = 2;
                }
                lineWidth += lineWidthAddon;
            }
            scale = (float)(((lineWidth>>1)*Utils.METER_TO_MC2SCALE) / (lineLength));
        } else {
            if (LOG.isError()) {
                LOG.error("MapDrawer.drawLine()", "lineWidth=0!");
            }
        }
        
        // The x and y increments from an endpoint needed to create a rectangle...
        float ddx = 0;
        float ddy = 0;
        
        ddx = dY * scale;
        ddy = dX * scale;
        
        if (isOutline) {
            if (comparableScale < 2) {
                return;
            }
            this.extrude[0] = ddx;
            this.extrude[1] = ddy;
            this.extrude = Utils.normalize(this.extrude);
            ddx += this.extrude[0]*1.5f;
            ddy += this.extrude[1]*1.5f;
        } else if (comparableScale < 1) {
            wInv = 1 / (transform[3][0]*x1 + transform[3][1]*y1 + transform[3][3]);
            x1t = (int)((transform[0][0]*x1 + transform[0][1]*y1 + transform[0][3]) * wInv);
            y1t = (int)((transform[1][0]*x1 + transform[1][1]*y1 + transform[1][3]) * wInv);
            
            wInv = 1 / (transform[3][0]*x2 + transform[3][1]*y2 + transform[3][3]);
            x2t = (int)((transform[0][0]*x2 + transform[0][1]*y2 + transform[0][3]) * wInv);
            y2t = (int)((transform[1][0]*x2 + transform[1][1]*y2 + transform[1][3]) * wInv);
            
            g.drawLine(x1t+halfWidth, y1t+halfHeight, x2t+halfWidth, y2t+halfHeight, 1);
            return;
        }
        
        final float coslat = (float) cam.getCoslat();
        final float coslatddx = ddx / coslat;
        final float coslatddy = ddy / coslat;
        final float adjust = 0.5f;
        
        // Calculate the line's vertices ((l_x1,l_y1),...,(l_x4,l_y4))
        // (l_x1,l_y1) and (l_x2,l_y2) derived from (x1,y1)
        // (l_x3,l_y3) and (l_x4,l_y4) derived from (x2,y2)
        
        // (l_x1,l_y1)
        lineVertices[0] = (int)Math.floor(x1-ddx+adjust);
        lineVertices[1] = (int)Math.floor(y1+coslatddy+adjust);
        
        // (l_x2,l_y2)
        lineVertices[2] = (int)Math.floor(x1+ddx+adjust);
        lineVertices[3] = (int)Math.floor(y1-coslatddy+adjust);
        
        // (l_x3,l_y3)
        lineVertices[4] = (int)Math.floor(x2-ddx+adjust);
        lineVertices[5] = (int)Math.floor(y2+coslatddy+adjust);
        
        // (l_x4,l_y4)
        lineVertices[6] = (int)Math.floor(x2+ddx+adjust);
        lineVertices[7] = (int)Math.floor(y2-coslatddy+adjust);
        
        boolean polyLineIsTurning = true;
        
        applyTransform(lineVertices, lineVertices.length, transform);
        
        // Calculate coordinates of triangle to fill gap between lines
        if (!iFirstLineInPolyLine) {
            // vectors for previous and current segment
            int v1x = x1 - iPrevSegmentStartPointX;
            int v1y = y1 - iPrevSegmentStartPointY;
            int v2x = x2 - x1;
            int v2y = y2 - y1;
            int crossProd = v1x*v2y - v1y*v2x;
            
            // Check which way the polyline is turning
            if (crossProd == 0) {
                // Polyline does not turn, i.e. no gap triangle needed
                polyLineIsTurning = false;
            } else if (crossProd < 0) {
                iGapTriangleTransformedVertices[0] = iPrevLineTransformedEndPoints[2];
                iGapTriangleTransformedVertices[1] = iPrevLineTransformedEndPoints[3];
                iGapTriangleTransformedVertices[2] = lineVertices[2];
                iGapTriangleTransformedVertices[3] = lineVertices[3];
                iGapTriangleTransformedVertices[4] = lineVertices[0];
                iGapTriangleTransformedVertices[5] = lineVertices[1];
                
                if (iDrawReversedGapTriangle) {
                    iReversedGapTriangleTransformedVertices[0] = iPrevLineTransformedEndPoints[0];
                    iReversedGapTriangleTransformedVertices[1] = iPrevLineTransformedEndPoints[1];
                    iReversedGapTriangleTransformedVertices[2] = lineVertices[0];
                    iReversedGapTriangleTransformedVertices[3] = lineVertices[1];
                    iReversedGapTriangleTransformedVertices[4] = lineVertices[2];
                    iReversedGapTriangleTransformedVertices[5] = lineVertices[3];
                }
            } else if (crossProd > 0) {
                iGapTriangleTransformedVertices[0] = iPrevLineTransformedEndPoints[0];
                iGapTriangleTransformedVertices[1] = iPrevLineTransformedEndPoints[1];
                iGapTriangleTransformedVertices[2] = lineVertices[0];
                iGapTriangleTransformedVertices[3] = lineVertices[1];
                iGapTriangleTransformedVertices[4] = lineVertices[2];
                iGapTriangleTransformedVertices[5] = lineVertices[3];
                
                if (iDrawReversedGapTriangle) {
                    iReversedGapTriangleTransformedVertices[0] = iPrevLineTransformedEndPoints[2];
                    iReversedGapTriangleTransformedVertices[1] = iPrevLineTransformedEndPoints[3];
                    iReversedGapTriangleTransformedVertices[2] = lineVertices[2];
                    iReversedGapTriangleTransformedVertices[3] = lineVertices[3];
                    iReversedGapTriangleTransformedVertices[4] = lineVertices[0];
                    iReversedGapTriangleTransformedVertices[5] = lineVertices[1];
                }
            }
        }
        
        // Save the end points of this line
        iPrevLineTransformedEndPoints[0] = lineVertices[4];
        iPrevLineTransformedEndPoints[1] = lineVertices[5];
        iPrevLineTransformedEndPoints[2] = lineVertices[6];
        iPrevLineTransformedEndPoints[3] = lineVertices[7];
        
        // Save the start point of this segment
        iPrevSegmentStartPointX = x1;
        iPrevSegmentStartPointY = y1;
        
        // Draw the line (two triangles)
        fillTriangleWithClipping(
                lineVertices[0] + halfWidth, lineVertices[1] + halfHeight,
                lineVertices[4] + halfWidth, lineVertices[5] + halfHeight,
                lineVertices[6] + halfWidth, lineVertices[7] + halfHeight);
        fillTriangleWithClipping(
                lineVertices[2] + halfWidth, lineVertices[3] + halfHeight,
                lineVertices[6] + halfWidth, lineVertices[7] + halfHeight,
                lineVertices[0] + halfWidth, lineVertices[1] + halfHeight);
        
        // Draw gap triangle(s)
        if (!VectorMapOptimizationFilter.skipGapTriangles(cam, iCurrentDetailLevel) && polyLineIsTurning && !iFirstLineInPolyLine) {
            fillTriangleWithClipping(
                    iGapTriangleTransformedVertices[0] + halfWidth, iGapTriangleTransformedVertices[1] + halfHeight,
                    iGapTriangleTransformedVertices[2] + halfWidth, iGapTriangleTransformedVertices[3] + halfHeight,
                    iGapTriangleTransformedVertices[4] + halfWidth, iGapTriangleTransformedVertices[5] + halfHeight);
            if (iDrawReversedGapTriangle) {
                fillTriangleWithClipping(
                        iReversedGapTriangleTransformedVertices[0] + halfWidth, iReversedGapTriangleTransformedVertices[1] + halfHeight,
                        iReversedGapTriangleTransformedVertices[2] + halfWidth, iReversedGapTriangleTransformedVertices[3] + halfHeight,
                        iReversedGapTriangleTransformedVertices[4] + halfWidth, iReversedGapTriangleTransformedVertices[5] + halfHeight);
            }
        }
        
        // Drawing of start and end triangles
        if (!VectorMapOptimizationFilter.skipStartAndEndTriangles(cam, iCurrentDetailLevel)) {
            // Draw extra start triangle if this is the first line in a polyline
            if (iFirstLineInPolyLine) {
                iLineStartVertex[0] = (int)Math.floor(x1+ddy+adjust);
                iLineStartVertex[1] = (int)Math.floor(y1+coslatddx+adjust);
                applyTransform(iLineStartVertex, iLineStartVertex.length, transform);
                
                fillTriangleWithClipping(
                        lineVertices[0] + halfWidth, lineVertices[1] + halfHeight,
                        lineVertices[2] + halfWidth, lineVertices[3] + halfHeight,
                        iLineStartVertex[0] + halfWidth, iLineStartVertex[1] + halfHeight);
            }
            
            // Draw extra end triangle if this is the last line in a polyline
            if (iLastLineInPolyLine) {
                iLineEndVertex[0] = (int)Math.floor(x2-ddy+adjust);
                iLineEndVertex[1] = (int)Math.floor(y2-coslatddx+adjust);
                applyTransform(iLineEndVertex, iLineEndVertex.length, transform);
                
                fillTriangleWithClipping(
                        lineVertices[4] + halfWidth, lineVertices[5] + halfHeight,
                        lineVertices[6] + halfWidth, lineVertices[7] + halfHeight,
                        iLineEndVertex[0] + halfWidth, iLineEndVertex[1] + halfHeight);
            }
        }
    }
    
    /**
     * Draws a 3d polyline with polygons
     * 
     * @param vertices - vertices of polyline
     * @param lineWidth - width of polyline
     * @param isOutline - true if this is an outline, false otherwise
     * @param verticesToClip - vertices to clip
     * @param transform - camera transform
     * @param camLine - camLine
     * @param camera - the camera
     */
    private void draw3dPolygonPolyLine(int[] vertices, int lineWidth, boolean isOutline, 
            byte[] verticesToClip, float[][]transform, long[] camLine, Camera camera) {
        
        final int nbrFeatures = ((vertices.length) >> 1) - 1;
        int []xPts = null;
        int []yPts = null;
        int nbrPonts = 0;
        
        // True if at least one point has been visible on the screen, false if not. 
        boolean pointHasBeenVisibleOnScreen = false;
        int ptsCnt = 0;
        
        for (int i=0; i<nbrFeatures; i++) {
            
            final int pos = (i<<1);
            int y1 = vertices[pos+1];
            int y2 = vertices[pos+3];
            int x1 = vertices[pos];
            int x2 = vertices[pos+2];

            // Check if p1 (x1,y1) and p2 (x2,y2) are outside the screen, i.e. will be clipped
            final boolean p1Outside = (verticesToClip[i/8] & (0x1<<(i%8))) > 0;
            final boolean p2Outside = (verticesToClip[(i+1)/8] & (0x1<<((i+1)%8))) > 0;
            
            if (!p1Outside && !p2Outside) { //Both points inside
                
                if(!pointHasBeenVisibleOnScreen) {
                    nbrPonts = (nbrFeatures-i)*4;
                    nbrPonts++;
                    ptsCnt = 1;
                    xPts = new int[nbrPonts];
                    yPts = new int[nbrPonts];
                    nbrPonts--;                                                        
                }
                
                // [x_1, y_1, x_2, y_2] in screen coords
                drawLineAsPolygon(x1, y1, x2, y2, lineWidth, transform, camera, xPts, yPts, ptsCnt, nbrPonts, 
                        !pointHasBeenVisibleOnScreen, isOutline);    
                pointHasBeenVisibleOnScreen = true;
                
            } else if ((p1Outside && !p2Outside) || (!p1Outside && p2Outside)) { //One point outside
                if(!pointHasBeenVisibleOnScreen) {
                    nbrPonts = (nbrFeatures-i)*4;
                    nbrPonts ++;
                    ptsCnt = 1;
                    xPts = new int[nbrPonts];
                    yPts = new int[nbrPonts];
                    nbrPonts--;                                                        
                }
                
                final long da = (x2-x1);
                final long db = (y2-y1);
                final long dc = (camLine[2]-camLine[0]);
                final long dd = (camLine[3]-camLine[1]);    
                final double factor = ((camLine[1]-y1)*dc + (x1-camLine[0])*dd) / (double)(db*dc - da*dd);

                final int newX = (int)(x1 + da * factor);
                final int newY = (int)(y1 + db * factor);

                if (!p1Outside) { // (x1,y1) outside the screen. 
                    x2 = newX;
                    y2 = newY;
                } else { // (x2,y2) outside the screen- 
                    x1 = newX;
                    y1 = newY;                            
                }
                // [x_1, y_1, x_2, y_2] in screen coords
                drawLineAsPolygon(x1, y1, x2, y2, lineWidth, transform, camera, xPts, yPts, ptsCnt, 
                        nbrPonts, !pointHasBeenVisibleOnScreen, isOutline);
                pointHasBeenVisibleOnScreen = true;
                
            } else { // both points outside. 
                
                if(pointHasBeenVisibleOnScreen) {
                    //FIXME:
                    // If at least one point has been visible on the screen we set the points that are outside 
                    // the screen to the last visible point. So when/if the road become visible on the
                    // screen again we continue the polygon from the last known point. This is not a 
                    // 100% system because we can end up in a situation where the road goes around the  
                    // corner of the screen when it's outside and when it become visible again the road will 
                    // be drawn incorrectly.  
                    xPts[ptsCnt] = xPts[ptsCnt+1] = xPts[ptsCnt-1];
                    yPts[ptsCnt] = yPts[ptsCnt+1] = yPts[ptsCnt-1];                        
                    xPts[(nbrPonts-ptsCnt)+1] = xPts[(nbrPonts-ptsCnt-1)+1] = xPts[(nbrPonts-ptsCnt+1)+1];
                    yPts[(nbrPonts-ptsCnt)+1] = yPts[(nbrPonts-ptsCnt-1)+1] = yPts[(nbrPonts-ptsCnt+1)+1];
                  
                } else {
                    // Just ignore, the beginning of the road are outside the screen.                            
                }
            }
            ptsCnt+=2;
        }
        
        if(!pointHasBeenVisibleOnScreen) {                   
            return;
        }
                            
        g.fillPolygon(xPts, yPts, xPts.length);
    }
    
    /**
     * Draws a 3d polyline with triangles
     * 
     * @param vertices - vertices of polyline
     * @param lineWidth - width of polyline
     * @param isOutline - true if this is an outline, false otherwise
     * @param verticesToClip - vertices to clip
     * @param transform - camera transform
     * @param camLine - camLine
     * @param textPosition - text position (output argument)
     * @param camera - the camera
     */
    private void draw3dTrianglePolyLine(int[] vertices, int lineWidth, 
            boolean isOutline, byte[] verticesToClip, float[][]transform, 
            long[] camLine, int[] textPosition, Camera camera) {
        
        int nbrFeatures = (vertices.length >> 1) - 1;
        for (int i=0; i<nbrFeatures; i++) {
            iFirstLineInPolyLine = (i == 0);
            iLastLineInPolyLine = (i == nbrFeatures-1);
            
            final int pos = (i<<1);
            int y1 = vertices[pos+1];
            int y2 = vertices[pos+3];
            int x1 = vertices[pos];
            int x2 = vertices[pos+2];

            // Check if p1 (x1,y1) and p2 (x2,y2) are outside the screen, i.e. will be clipped
            boolean p1Outside = (verticesToClip[i/8] & (0x1<<(i%8))) > 0;
            boolean p2Outside = (verticesToClip[(i+1)/8] & (0x1<<((i+1)%8))) > 0;
            
            // Draw an extra reversed gap triangle in 3D
            // FIXME: This should not really be necessary but (probably) due to clipping
            // the gap triangles are sometimes drawn incorrectly, leaving gaps in roads.
            // The temporary solution is to draw gap triangles in both directions (in 3D)
            iDrawReversedGapTriangle = true;

            if (!p1Outside && !p2Outside) { //Both points inside
                drawLine(x1, y1, x2, y2, lineWidth, isOutline, 
                        transform, textPosition, camera);
            } else if ((p1Outside && !p2Outside) || (!p1Outside && p2Outside)) { //One point outside
                long da = (x2-x1);
                long db = (y2-y1);
                long dc = (camLine[2]-camLine[0]);
                long dd = (camLine[3]-camLine[1]);

                double factor = ((camLine[1]-y1)*dc + (x1-camLine[0])*dd) / (double)(db*dc - da*dd);

                int newX = (int)(x1 + da * factor);
                int newY = (int)(y1 + db * factor);
                
                // Points will no longer coincide with previous/next segment after being clipped
                // We need to start a new polyline to avoid rendering problems
                iFirstLineInPolyLine = true;

                if (!p1Outside) {
                    drawLine(x1, y1, newX, newY, lineWidth, isOutline, 
                            transform, textPosition, camera);
                } else {
                    drawLine(newX, newY, x2, y2, lineWidth, isOutline, 
                            transform, textPosition, camera);
                }
            }
        }
    }
    
    /**
     * Draws a path using the specified coordinates and width
     * 
     * @param xCoords  x coordinates of the path
     * @param yCoords  y coordinates of the path
     * @param nbrCoords  number of coordinates
     * @param lineWidth  width of the line
     */
    private void drawPath(int[] xCoords, int[] yCoords, int nbrCoords, int lineWidth, int[] textPosition, Camera camera) {
        final WFGraphics g = this.g;
        // If the line is very thin we want to temporarily remove anti-aliasing
        // But only if it's turned on...
        boolean antialias = false;
        if (lineWidth < 2) {
            antialias = !camera.isMoving();
            if (antialias) {
                g.allowAntialias(false);
            }
        }

        if (tracking) {
            for (int i = 1; i<nbrCoords; i++) {
                final int dX = xCoords[i] - xCoords[i - 1];
                final int dY = yCoords[i] - yCoords[i - 1];
                final float lineLength = (float) Math.sqrt(dX*dX+dY*dY);
                if (lineLength > textPosition[0]) {
                    textPosition[0] = (int)lineLength;
                    textPosition[1] = (int)(xCoords[i - 1] - (dX >> 1));
                    textPosition[2] = (int)(yCoords[i - 1] - (dY >> 1));
                }
            }

        }

        final int halfScreenWidth = m_halfWidth;
        final int halfScreenHeight = m_halfHeight;
        for (int i=0; i<nbrCoords; i++) {
            xCoords[i] += halfScreenWidth;
            yCoords[i] += halfScreenHeight;
        }
        
        g.drawPath(xCoords, yCoords, nbrCoords, lineWidth);

        if (antialias) {
            g.allowAntialias(true);
        }
	}
    
    /**
     * Draws a 2d poly line with triangles
     * 
     * @param vertices - vertices of polyline
     * @param lineWidth - width of polyline
     * @param isOutline - true if this is an outline, false otherwise
     * @param transform - camera transform
     * @param textPosition - text position (output argument)
     * @param camera - the camera
     */
    private void draw2dTrianglePolyLine(int[] vertices, int lineWidth, 
            boolean isOutline, float[][]transform, int[] textPosition, 
            Camera camera) {
        
        for (int i = 0; i<vertices.length-2; i=i+2) {
            iFirstLineInPolyLine = (i == 0);
            iLastLineInPolyLine = (i >= vertices.length-4);
            drawLine(vertices[i], vertices[i+1], vertices[i+2], vertices[i+3], 
                    lineWidth, isOutline, transform, textPosition, camera);
        }
    }
    
    /**
     * Draws a polyline with the specified arguments
     * 
     * @param layerId  id of layer to be drawn
     * @param vertices  vertices of polyline
     * @param lineWidth  width of polyline
     * @param color  color of polyline
     * @param isOutline  true if this is an outline, false otherwise
     * @param verticesToClip  vertices to clip (only for 3d)
     * @param cameraTransform  camera transform
     * @param camLine  camLine
     * @param textPosition  text position (output argument)
     * @param textPlacementInfo  text placement information (output argument)
     * @param camera  the camera
     */
    private void drawPolyLine(int layerId, int[] vertices, int lineWidth, 
            int color, boolean isOutline, byte[] verticesToClip, float[][] cameraTransform, 
            long[] camLine, int[] textPosition, TextPlacementInfo textPlacementInfo, 
            Camera camera) {

        g.setColor(color);
        
        if (camera.isIn3DMode()) { // 3d mode
            // Draw route layer with triangles to improve visual quality
            if (g.supportsPolygon() && layerId != RenderManager.ID_ROUTE_LAYER) {
                draw3dPolygonPolyLine(vertices, lineWidth, isOutline, 
                        verticesToClip, cameraTransform, camLine, camera);
            } else {
                iDrawReversedGapTriangle = false;
                draw3dTrianglePolyLine(vertices, lineWidth, isOutline, 
                        verticesToClip, cameraTransform, camLine, textPosition, 
                        camera);
            }
        } else if (g.supportsPath()) { // draw 2d polylines using paths
            apply2dScreenCoordinatesTransform(vertices, cameraTransform);
            if (textPlacementInfo != null && !tracking) {
                calculateLineTextPlacementInfo(m_screenXCoordsBuffer, m_screenYCoordsBuffer, 
                        m_screenCoordsBufferSize, textPlacementInfo);
            }
            drawPath(m_screenXCoordsBuffer, m_screenYCoordsBuffer, m_screenCoordsBufferSize, lineWidth, textPosition, camera);
        } else { // draw 2d polylines using triangles
            iDrawReversedGapTriangle = false;
            draw2dTrianglePolyLine(vertices, lineWidth, isOutline, 
                    cameraTransform, textPosition, camera);
        }
    }
    
    private int []totPts = new int[8];
    
    private void drawLineAsPolygon (int x1, int y1, int x2, int y2, int lineWidth, float[][]transform, Camera cam, 
            int []xPts, int []yPts, int ptsCnt, int nbrPonts, boolean firstLineSegment, boolean isOutline) {
        
            final int halfWidth = iScreenInfo.getHalfScreenWidth();
            final int halfHeight = iScreenInfo.getHalfScreenHeight();
            
            final long dX = x1 - x2;
            final long dY = y1 - y2;
            
            float comparableScale = -1; //Fix
            
            final double lineLength = Math.sqrt(dX*dX+dY*dY);

            //If this road actually is longer than the previously largest segment
            if (tracking && lineLength > textpos[0]) {
                textpos[0] = (int)lineLength;
                textpos[1] = (int)(x1 - (dX>>1));
                textpos[2] = (int)(y1 - (dY>>1));
            }
            
            float scale = -1;
            if (lineWidth > 0) {  //lineWidth in pixels
                lineWidth = lineWidth>>1;
                comparableScale = lineWidth;                
                lineWidth = (int)(lineWidth * iCurrentZoomLevel);
                if (isOutline) {
                    lineWidth += 2;
                }                
                scale = (float)(((lineWidth>>1)*Utils.METER_TO_MC2SCALE) / (lineLength));
            } else if (lineWidth < 0) { //lineWidth in meters 
                lineWidth = -lineWidth;
                comparableScale = lineWidth / iCurrentZoomLevel;  
                
                if (isOutline) {
                    int lineWidthAddon = (int)(2 * iCurrentZoomLevel);
                    if (lineWidthAddon < 2) {
                        lineWidthAddon = 2;
                    }
                    lineWidth += lineWidthAddon;
                }
                
                scale = (float)(((lineWidth>>1)*Utils.METER_TO_MC2SCALE) / (lineLength));
            } else {
                if(LOG.isError()) {
                    LOG.error("MapDrawer.drawLine2()", "Line has zero width!");
                }
            }
            
            // The x and y increments from an endpoint needed to create a rectangle...            
            final float ddx = dY * scale;
            final float ddy = dX * scale;
            
            if (comparableScale < 1) {
//                if(LOG.isError()) {
//                    LOG.error("MapDrawer.drawLine3()", "comparableScale < 1");
//                }
                return;
            }
            
            final float coslat = (float) cam.getCoslat();
            final float coslatddy = ddy / coslat;
            final float adjust = 0.5f;
            
            // (l_x1,l_y1)
            totPts[0] = (int)Math.floor(x1-ddx+adjust);
            totPts[1] = (int)Math.floor(y1+coslatddy+adjust);
            
            // (l_x2,l_y2)
            totPts[2] = (int)Math.floor(x1+ddx+adjust);
            totPts[3] = (int)Math.floor(y1-coslatddy+adjust);
            
            // (l_x3,l_y3)
            totPts[4] = (int)Math.floor(x2-ddx+adjust);
            totPts[5] = (int)Math.floor(y2+coslatddy+adjust);
            
            // (l_x4,l_y4)
            totPts[6] = (int)Math.floor(x2+ddx+adjust);
            totPts[7] = (int)Math.floor(y2-coslatddy+adjust);
            
            
            applyTransform(totPts, totPts.length, transform);
            
            xPts[ptsCnt]                = totPts[0] + halfWidth;
            yPts[ptsCnt]                = totPts[1] + halfHeight;
            xPts[(nbrPonts-ptsCnt)+1]   = totPts[2] + halfWidth;            
            yPts[(nbrPonts-ptsCnt)+1]   = totPts[3] + halfHeight;
            ptsCnt++;
            xPts[ptsCnt]                = totPts[4] + halfWidth;
            yPts[ptsCnt]                = totPts[5] + halfHeight;
            xPts[(nbrPonts-ptsCnt)+1]   = totPts[6] + halfWidth;            
            yPts[(nbrPonts-ptsCnt)+1]   = totPts[7] + halfHeight;
            
            if(firstLineSegment) {
                final float coslatddx = ddx / coslat;
                iLineStartVertex[0] = (int)Math.floor(x1+ddy+adjust);
                iLineStartVertex[1] = (int)Math.floor(y1+coslatddx+adjust);
                applyTransform(iLineStartVertex, iLineStartVertex.length, transform);                
                xPts[0] = iLineStartVertex[0] + halfWidth;
                yPts[0] = iLineStartVertex[1] + halfHeight;
                
            } else {                            
                // vectors for previous and current segment
                int v1x = x1 - iPrevSegmentStartPointX;
                int v1y = y1 - iPrevSegmentStartPointY;
                int v2x = x2 - x1;
                int v2y = y2 - y1;
                int crossProd = v1x*v2y - v1y*v2x;
                
                // Check which way the polyline is turning
                if (crossProd < 0) { // The road turn left        
                    
                    int tmpPtsCnt = ptsCnt-1;                    
                    if(xPts[tmpPtsCnt-1] < xPts[tmpPtsCnt]) {
                        if(yPts[tmpPtsCnt-1] < yPts[tmpPtsCnt]) {
                            // Image 1                            
                            yPts[tmpPtsCnt] = yPts[tmpPtsCnt-1];
                            xPts[tmpPtsCnt-1] = xPts[tmpPtsCnt];
                        } else {
                            // Image 2
                            yPts[tmpPtsCnt-1] = yPts[tmpPtsCnt];
                            xPts[tmpPtsCnt] = xPts[tmpPtsCnt-1];
                        }
                    } else {
                        if(yPts[tmpPtsCnt-1] < yPts[tmpPtsCnt]) {
                            // Image 4
                            yPts[tmpPtsCnt] = yPts[tmpPtsCnt-1];
                            xPts[tmpPtsCnt-1] = xPts[tmpPtsCnt];
                        } else {
                            // Image 3
                            yPts[tmpPtsCnt-1] = yPts[tmpPtsCnt];
                            xPts[tmpPtsCnt] = xPts[tmpPtsCnt-1];
                        }
                    }                                        
                } else if (crossProd > 0) { // The road turn right
                    
                    int tmpPtsCnt = ptsCnt-1;
                    if(xPts[(nbrPonts-tmpPtsCnt+1)+1] < xPts[(nbrPonts-tmpPtsCnt)+1]) {
                        if(yPts[(nbrPonts-tmpPtsCnt+1)+1] < yPts[(nbrPonts-tmpPtsCnt)+1]) {
                            yPts[(nbrPonts-tmpPtsCnt)+1] = yPts[(nbrPonts-tmpPtsCnt+1)+1];
                            xPts[(nbrPonts-tmpPtsCnt+1)+1] = xPts[(nbrPonts-tmpPtsCnt)+1];
                        } else {
                            yPts[(nbrPonts-tmpPtsCnt+1)+1] = yPts[(nbrPonts-tmpPtsCnt)+1];
                            xPts[(nbrPonts-tmpPtsCnt)+1] = xPts[(nbrPonts-tmpPtsCnt+1)+1];
                        }
                    } else {
                        if(yPts[(nbrPonts-tmpPtsCnt+1)+1] < yPts[(nbrPonts-tmpPtsCnt)+1]) {
                            yPts[(nbrPonts-tmpPtsCnt+1)+1] = yPts[(nbrPonts-tmpPtsCnt)+1];
                            xPts[(nbrPonts-tmpPtsCnt)+1] = xPts[(nbrPonts-tmpPtsCnt+1)+1];
                        } else {
                            yPts[(nbrPonts-tmpPtsCnt)+1] = yPts[(nbrPonts-tmpPtsCnt+1)+1];
                            xPts[(nbrPonts-tmpPtsCnt+1)+1] = xPts[(nbrPonts-tmpPtsCnt)+1];
                        }
                    }                    
                    
                } else {
                    // Polyline does not turn, i.e. no points needs to be moved. 
                }
            }            
            // Save the start point of this segment
            iPrevSegmentStartPointX = x1;
            iPrevSegmentStartPointY = y1;
    }
    
    private void drawConcavePolygonWithClipping(ConcavePolygon cp, int[] vertices, int vLength, int color,byte[]verticesToClip,float[][]tr){
        g.setColor(color);
        short[][] ibuff = cp.getIndexBuffer();
        applyTransform(vertices, vLength, tr);
        for(int j=0;j<ibuff.length;j++){
            short[]tri=ibuff[j];
            
            int t0 = tri[0];
            int t1 = tri[1];
            int t2 = tri[2];
            
            byte v0c = (byte)(((verticesToClip[t0/8]&(0x1<<(t0%8)))>0)?1:0);
            byte v1c = (byte)(((verticesToClip[t1/8]&(0x1<<(t1%8)))>0)?1:0);
            byte v2c = (byte)(((verticesToClip[t2/8]&(0x1<<(t2%8)))>0)?1:0);
            
            t0<<=1;
            t1<<=1;
            t2<<=1;
            
            byte clipSum = (byte)(v0c + v1c + v2c);
            
            int halfWidth = m_halfWidth;
            int halfHeight = m_halfHeight;
            
            if(clipSum==3) {
                continue;
            } else if(clipSum==2) {
                //g�r om till en triangel
                int x1, y1, x2, y2, x3, y3;
                if(v0c==0) {
                    x1 = vertices[t0];
                    y1 = vertices[t0 + 1];
                    x2 = vertices[t1];
                    y2 = vertices[t1 + 1];
                    x3 = vertices[t2];
                    y3 = vertices[t2 + 1];
                } else if(v1c==0) {
                    x1 = vertices[t1];
                    y1 = vertices[t1 + 1];
                    x2 = vertices[t0];
                    y2 = vertices[t0 + 1];
                    x3 = vertices[t2];
                    y3 = vertices[t2 + 1];
                } else {
                    x1 = vertices[t2];
                    y1 = vertices[t2 + 1];
                    x2 = vertices[t1];
                    y2 = vertices[t1 + 1];
                    x3 = vertices[t0];
                    y3 = vertices[t0 + 1];
                }
                
                float factor1 = (clipLine - y1)/(float)(y2 - y1);
                float factor2 = (clipLine - y1)/(float)(y3 - y1);
                
                int newX2 = (int)(x1 + (x2 - x1) * factor1);
                int newX3 = (int)(x1 + (x3 - x1) * factor2);
                
                g.fillTriangle(x1+halfWidth, y1+halfHeight, newX2+halfWidth, clipLine+halfHeight, newX3+halfWidth, clipLine+halfHeight);
                
            } else if(clipSum==1) {
                
                int x1, y1, x2, y2, x3, y3;
                if(v0c==1) {
                    x1 = vertices[t2];
                    y1 = vertices[t2 + 1];
                    x2 = vertices[t1];
                    y2 = vertices[t1 + 1];
                    x3 = vertices[t0];
                    y3 = vertices[t0 + 1];
                } else if(v1c==1) {
                    x1 = vertices[t2];
                    y1 = vertices[t2 + 1];
                    x2 = vertices[t0];
                    y2 = vertices[t0 + 1];
                    x3 = vertices[t1];
                    y3 = vertices[t1 + 1];
                } else {
                    x1 = vertices[t1];
                    y1 = vertices[t1 + 1];
                    x2 = vertices[t0];
                    y2 = vertices[t0 + 1];
                    x3 = vertices[t2];
                    y3 = vertices[t2 + 1];
                }
                
                float factor1 = (clipLine - y3)/(float)(y1 - y3);
                int newX3 = (int)(x3 + (x1 - x3) * factor1);
                g.fillTriangle(x1+halfWidth, y1+halfHeight, x2+halfWidth, y2+halfHeight, newX3+halfWidth, clipLine+halfHeight);
                
                float factor2 = (clipLine - y3)/(float)(y2 - y3);
                int newX4 = (int)(x3 + (x2 - x3) * factor2);
                g.fillTriangle(x2+halfWidth, y2+halfHeight, newX4+halfWidth, clipLine+halfHeight, newX3+halfWidth, clipLine+halfHeight);
                //g�r om till tv� trianglar
            } else {
                int i1 = t0;
                int i2 = t0 + 1;
                int i3 = t1;
                int i4 = t1 + 1;
                int i5 = t2;
                int i6 = t2 + 1;
                g.fillTriangle(vertices[i1]+halfWidth, vertices[i2]+halfHeight, 
                                vertices[i3]+halfWidth, vertices[i4]+halfHeight, 
                                vertices[i5]+halfWidth, vertices[i6]+halfHeight);
            }
            
        }
    }
    
    private void drawConcavePolygon(ConcavePolygon cp, int[] vertices, 
            int vLength, int color, float[][]tr, boolean secondPass) {
        final WFGraphics g = this.g;
        g.setColor(color);

        applyTransformUnpanned(vertices, vLength, tr);
        
        int halfWidth = m_halfWidth;
        int halfHeight = m_halfHeight;
        
        if(g.supportsPolygon()) {
            int maxIndex = (vLength >> 1);
            int[] xPts = m_pointsX;
            int[] yPts = m_pointsY;
            for(int i = 0; i < maxIndex; i++) {
                final int vertX = (i << 1);
                xPts[i]    = vertices[vertX]     + halfWidth;
                yPts[i]    = vertices[vertX + 1] + halfHeight;
            }
            
            g.fillPolygon(xPts, yPts, maxIndex);
            
        } else {
            short[][] ibuff = cp.getIndexBuffer();
            final int size = ibuff.length;
            for(int j=0;j<size;j++){
                short[] ba = ibuff[j];
                int x1 = vertices[(ba[0]<<1)] + halfWidth;
                int y1 = vertices[(ba[0]<<1) + 1] + halfHeight;
                
                int x2 = vertices[(ba[1]<<1)] + halfWidth;
                int y2 = vertices[(ba[1]<<1) + 1] + halfHeight;
                
                int x3 = vertices[(ba[2]<<1)] + halfWidth;
                int y3 = vertices[(ba[2]<<1) + 1] + halfHeight;
    
                
                
                
                this.fillTriangleWithClipping(x1, y1, x2, y2, x3, y3);
                if(secondPass) {
                    this.fillTriangleWithClipping(x1 + 1, y1, x2 + 1, y2, x3 + 1, y3);
                }
            }
        }
    }
    
    private void fillTriangleWithClipping(int x1, int y1, int x2, int y2, int x3, int y3) {
        if(y1 < 0 || y2 < 0 || y3 < 0) {
            int pointsOutside = 0;
            if(y1 < 0) {
                pointsOutside ++;
            }
            if(y2 < 0) {
                pointsOutside ++;
            }
            if(y3 < 0) {
                pointsOutside ++;
            }
            
            if(pointsOutside == 1) {
                if(y1 < 0) {
                    int newX1 = this.clipLine(x1, y1, x2, y2, 0);
                    int newY1 = 0;

                    int newX = this.clipLine(x1, y1, x3, y3, 0);
                    int newY = 0;
                    g.fillTriangle(newX1, newY1, newX, newY, x3, y3);
                    
                    x1 = newX1;
                    y1 = newY1;
                }
                if(y2 < 0) {
                    int newX2 = this.clipLine(x2, y2, x3, y3, 0);
                    int newY2 = 0;

                    int newX = this.clipLine(x2, y2, x1, y1, 0);
                    int newY = 0;
                    g.fillTriangle(newX2, newY2, newX, newY, x1, y1);
                    
                    x2 = newX2;
                    y2 = newY2;
                }
                if(y3 < 0) {
                    int newX3 = this.clipLine(x3, y3, x1, y1, 0);
                    int newY3 = 0;

                    int newX = this.clipLine(x3, y3, x2, y2, 0);
                    int newY = 0;
                    g.fillTriangle(newX3, newY3, newX, newY, x2, y2);
                    
                    x3 = newX3;
                    y3 = newY3;
                }
            }
            else if(pointsOutside == 2) {
                if(y1 < 0 && y2 < 0) {
                    x1 = this.clipLine(x1, y1, x3, y3, 0);
                    y1 = 0;
                    
                    x2 = this.clipLine(x2, y2, x3, y3, 0);
                    y2 = 0;
                }
                else if(y1 < 0 && y3 < 0) {
                    x1 = this.clipLine(x1, y1, x2, y2, 0);
                    y1 = 0;
                    
                    x3 = this.clipLine(x3, y3, x2, y2, 0);
                    y3 = 0;
                }
                else if(y2 < 0 && y3 < 0) {
                    x2 = this.clipLine(x2, y2, x1, y1, 0);
                    y2 = 0;
                    
                    x3 = this.clipLine(x3, y3, x1, y1, 0);
                    y3 = 0;
                }
            }
            else if(pointsOutside == 3) {
                return;
            }
        }
        
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
    }

    private int clipLine(int currX, int currY, int prevX, int prevY, int minY) {
        currX = prevX + (currX - prevX) * (prevY - minY) / (prevY - currY);
        return currX;
    }
    
    final void applyTransform(int[] p, int length, float[][] transform){
        float transform30 = transform[3][0];
        float transform31 = transform[3][1];
        float transform33 = transform[3][3];
        float transform00 = transform[0][0];
        float transform01 = transform[0][1];
        float transform03 = transform[0][3];
        float transform10 = transform[1][0];
        float transform11 = transform[1][1];
        float transform13 = transform[1][3];
        
        for (int index = 0; index < length; index=index+2) {
            int px = p[index];
            int py = p[index+1];
            float wInv = 1 / (transform30*px + transform31*py + transform33);
            p[index] = (int)((transform00*px + transform01*py + transform03) * wInv);
            p[index+1] = (int)((transform10*px + transform11*py + transform13) * wInv);
        }
    }
    
    final static float[] s_palMatrix = new float[9];
    final static float[] createPalMatrix(float[][] cameraMatrix, float offsetX, float offsetY) {
        final float[] m = s_palMatrix;
        m[0] = cameraMatrix[0][0];
        m[1] = cameraMatrix[0][1];
        m[2] = cameraMatrix[0][3] + offsetX;
        m[3] = cameraMatrix[1][0];
        m[4] = cameraMatrix[1][1];
        m[5] = cameraMatrix[1][3] + offsetY;
        m[6] = cameraMatrix[3][0];
        m[7] = cameraMatrix[3][1];
        m[8] = cameraMatrix[3][3];
        return s_palMatrix;
    }
    
    final void applyTransformUnpanned(int[] p, int length, float[][] transform) {
        float transform00 = transform[0][0];
        float transform01 = transform[0][1];
        float transform03 = transform[0][3];
        float transform10 = transform[1][0];
        float transform11 = transform[1][1];
        float transform13 = transform[1][3];
        
        for (int index = 0; index < length; index=index+2){
            int px = p[index];
            int py = p[index+1];
            p[index] = (int)(transform00*px + transform01*py + transform03);
            p[index+1] = (int)(transform10*px + transform11*py + transform13);
        }
    }
    
//    /**
//     * Adds a bitmap image to the objects that will be rendered
//     * Returns an id
//     *
//     * @param center The coordinate (x,y)
//     * @param image The Image
//     * @param symbolID The ID of the added symbol
//     */
//    public void addBitmap(int[]center,WFImage image, int id) {
//      removeBitmapAt(id, center);
//        addedSymbols.addElement(new Integer(id));
//        addedSymbols.addElement(center);
//        addedSymbols.addElement(image);
//    }
//    /**
//     * Removes an added bitmap
//     *
//     * @param id the id to remove
//     */
//    public void removeBitmap(int id){
//        for(int i =0;i<addedSymbols.size();i+=3){
//            if(((Integer)addedSymbols.elementAt(i)).intValue() == id){
//                addedSymbols.removeElementAt(i);
//                addedSymbols.removeElementAt(i);
//                addedSymbols.removeElementAt(i);
//            }
//        }
//    }
//    
//    public void removeBitmapAt(int id, int[]center) {
//      for(int i =0;i<addedSymbols.size();i+=3){
//            if(((Integer)addedSymbols.elementAt(i)).intValue() == id &&
//                  ((int[])addedSymbols.elementAt(i+1))[0] == center[0] &&
//                  ((int[])addedSymbols.elementAt(i+1))[1] == center[1]){
//                addedSymbols.removeElementAt(i);
//                addedSymbols.removeElementAt(i);
//                addedSymbols.removeElementAt(i);
//            }
//        }
//    }
//    /**
//     * Removes all added bitmaps
//     */
//    public void removeAllBitmaps() {
//        addedSymbols.removeAllElements();
//    }
    
    
    
    public void setShowServerPOIs(boolean shouldShow) {
        m_shouldShowServerPOIs = shouldShow;
    }
    
    
    /**
     * Get the bounding boxes for the POI-icons
     *
     * @return the bounding boxes
     */
    public Vector getPOIBoxes() {
        return m_poiBoxes;
    }
    
    /**
     * Get the bounding boxes for the polygons
     *
     * @return the bounding boxes
     */
    public Vector getObjectBoxes(){
        return objectBoxes;
    }
    
    /**
     * Draws the city centre texts
     * 
     * @param textColor  color of the texts
     * @param backgroundColor  color of the background
     */
    public void drawCityCentreTexts(int textColor, int backgroundColor) {
        for (int i=0; i<m_cityCenterObjects.size(); i++) {
            CityCentreObject ccObj = (CityCentreObject) m_cityCenterObjects.elementAt(i);
            g.setFont(ccObj.getFont());
            int x = ccObj.getBoundingBox().getMinX();
            int y = ccObj.getBoundingBox().getMinY();
            String text = ccObj.getText();
            drawTextOutline(g, backgroundColor, text, x, y, WFGraphics.ANCHOR_TOP | WFGraphics.ANCHOR_LEFT);            
            g.setColor(textColor);
            g.drawText(text, x, y, WFGraphics.ANCHOR_TOP | WFGraphics.ANCHOR_LEFT);
        }
    }
    
    /**
     * Prepares the MapDrawer for 2d-tracking
     */
    public void init2DTracking() {
        g.setFont(Utils.get().getFont(Utils.FONT_SMALL));
        tracking2DTextBoundingBoxes.removeAllElements();
    }
    
    /**
     * Draws texts when tracking
     *
     * @param text  the text to draw
     * @param x  x coordinate of the text
     * @param y  y coordinate of the text
     * @param tmfd  instance of TileMapFormatDesc
     */
    public void drawTrackingTexts(String text, int x, int y, TileMapFormatDesc tmfd) {
        int textWidth = Utils.get().getFont(Utils.FONT_SMALL).getStringWidth(text);
        int textHeight = Utils.get().getFont(Utils.FONT_SMALL).getFontHeight();
        
        int textX = x - textWidth/2 + iScreenInfo.getHalfScreenWidth();
        int textY = y - 2*textHeight + iScreenInfo.getHalfScreenHeight();
        
        if (!collidesWithAnotherTracking2DString(textWidth, textHeight, textX, textY)) {       
            drawTextOutline(g, tmfd.getBackgroundColor(), text, textX, textY, WFGraphics.ANCHOR_TOP | WFGraphics.ANCHOR_LEFT);            
            g.setColor(tmfd.getTextColor());
            g.drawText(text, textX, textY, WFGraphics.ANCHOR_TOP | WFGraphics.ANCHOR_LEFT);
            g.setColor(0x000000);
            int triangleWidth = textHeight / 2;
            int triangleHeight = textHeight / 2;
            int xLeft = x - triangleWidth / 2 + iScreenInfo.getHalfScreenWidth();
            int xRight = x + triangleWidth / 2 + iScreenInfo.getHalfScreenWidth();
            int yTop = y - triangleHeight + iScreenInfo.getHalfScreenHeight();
            x += iScreenInfo.getHalfScreenWidth();
            y += iScreenInfo.getHalfScreenHeight();
            g.fillTriangle(xLeft, yTop, xRight, yTop, x, y);
        }
    }
    
    /**
     * Draws the outline of a text when needed
     *
     * @param color color of the outline
     * @param screenX x screen pos of text
     * @param screenY y screen pos of text
     */ 
    private void drawTextOutline(WFGraphics g, int color, String text, int screenX, int screenY, int anchor){
        g.setColor(color);
        for (int j = -1; j < 2; j++)
            for (int k = -1; k < 2; k++){
                if (j == 0 && k == 0)
                    continue;
                g.drawText(text, screenX + j, screenY + k, anchor);                                 
            }   
    }
    
    public void drawDateLine(boolean isLower, int[] cambox, Camera cam) {
        int x1 = cambox[1];
        int y;
        if(isLower) {
            y = Utils.MIN_INTEGER;
        } else {
            y = Integer.MAX_VALUE;
        }
        
        int[] c1 = cam.getScreenCoordinateInternal(x1, y);
        int x1s = c1[0]; int y1s = c1[1];
        int x2s = c1[0]; int y2s = c1[1];
        
        g.setColor(0x9c4c1c);
        int halfWidth = iScreenInfo.getHalfScreenWidth();
        int halfHeight = iScreenInfo.getHalfScreenHeight();
        g.drawLine(x1s + halfWidth, y1s + halfHeight, x2s + halfWidth, y2s + halfHeight, 1);
    }

    /**
     * Check if any road is marked by the acitve position in the map. 
     * 
     * @param worldX
     * @param worldY
     * @param tmw
     * @param zoomLevel
     * @return
     */
    String getLineString(long worldX, long worldY, TileMapWrapper tmw, float zoomLevel) {
        int scaleIndex = m_Tmfd.getScaleIndexFromZoomLevel(zoomLevel);
        for(int level=Utils.MAX_LEVEL; level>=0; level--) {
            Vector geoDatas = tmw.getGeoData(level);
            int size = 0;
            synchronized(geoDatas) {
                size = geoDatas.size();
            }
            
            for(int i=0; i<size; i++) {
                
                TileFeatureData geoData = (TileFeatureData) geoDatas.elementAt(i);
                
                if(geoData.getPrimitiveType() != TileFeature.LINE) {
                    continue;
                }

                if(geoData.getText() == null)
                    continue;
                
                int[] feature = geoData.getCoords();
                String text = geoData.getText();
                    
                for(int j=0; j<feature.length-2; j+=2) {
                    final long dx = (feature[j+2] - feature[j]);
                    final long dy = (feature[j+3] - feature[j+1]);
                    long dist2 = (dx*dx+dy*dy);

                    long dxp1 = (worldX    - feature[j]);
                    long dyp1 = (worldY    - feature[j+1]);
                    
                    if(dist2 < (dxp1*dxp1 + dyp1*dyp1)) {
                        continue;
                    }
                    
                    long dxp2 = (worldX    - feature[j+2]);
                    long dyp2 = (worldY    - feature[j+3]);  
                    
                    if(dist2 < (dxp2*dxp2 + dyp2*dyp2) ) {
                        continue;
                    }
                    
                    //If it's not between the lines

                    // Calculate distance to line in meters
                    long dot            = dx*dxp1       + dy*dyp1;
                    long len1square     = dx*dx         + dy*dy;
                    long len2square     = dxp1*dxp1     + dyp1*dyp1;
                    long lenSquare      = len1square    * len2square;
                    double cos2Theta    = (dot*dot)     /(double)lenSquare;
                    
                    double sin2Theta = 1.0-cos2Theta;
                    double distSquare = len1square * sin2Theta * Utils.MC2SCALETIMES2;
                    
                    int lineWidth = geoData.getWidth(scaleIndex);
                    if(lineWidth >0){  //PIXELS
                        lineWidth = (int)(lineWidth * zoomLevel * iScreenInfo.getDPICorrection());
                    }else if(lineWidth <0){ //METER
                        lineWidth = -lineWidth;
                    }
                    
                    if(distSquare<=(lineWidth*lineWidth))
                        return text;
                    //System.out.println("Dist: " + distSquare + " Width: " + (lineWidth*lineWidth));
                    
                }
            }
        }
        return null;
    }
    
    /**
     * Performs a 2d transformation of the specified vertices to screen coordinates, 
     * using the specified camera transform. Transformed coordinates are stored
     * in the screen coordinates buffers.
     * 
     * @param vertices  the vertices to be transformed
     * @param cameraTransform  the camera transform
     */
    private void apply2dScreenCoordinatesTransform(int[] vertices, float[][] cameraTransform) {
        m_screenCoordsBufferSize = vertices.length / 2;
        // Ensure capacity of screen coordinates buffer
        if (m_screenCoordsBufferSize > m_screenCoordsBufferCapacity) {
            m_screenCoordsBufferCapacity = Math.max(2*m_screenCoordsBufferCapacity, m_screenCoordsBufferSize);
            m_screenXCoordsBuffer = new int[m_screenCoordsBufferCapacity];
            m_screenYCoordsBuffer = new int[m_screenCoordsBufferCapacity];
            
            if(LOG.isInfo()) {
                LOG.info("MapDrawer.apply2dScreenCoordinatesTransform()", 
                        "Increased capacity of screen coordinate buffers to " + 
                        m_screenCoordsBufferCapacity);
            }
        }
        
        float transform00 = cameraTransform[0][0];
        float transform01 = cameraTransform[0][1];
        float transform03 = cameraTransform[0][3];
        float transform10 = cameraTransform[1][0];
        float transform11 = cameraTransform[1][1];
        float transform13 = cameraTransform[1][3];

        for (int i=0; i<vertices.length; i+=2) {
            int coordsIndex = i >> 1;
            int x = vertices[i];
            int y = vertices[i+1];
            int xt = (int)(transform00*x + transform01*y + transform03);
            int yt = (int)(transform10*x + transform11*y + transform13);
            m_screenXCoordsBuffer[coordsIndex] = xt;
            m_screenYCoordsBuffer[coordsIndex] = yt;
        }
    }
    
    /**
     * Calculates text placement information for a polyline. 
     * 
     * @param xCoords  x coordinates of the polyline
     * @param yCoords  y coordinates of the polyline
     * @param nbrCoords  number of coordinates in the polyline
     * @param textPlacementInfo  text placement (output parameter)
     */
    private void calculateLineTextPlacementInfo(int[] xCoords, int[] yCoords, 
            int nbrCoords, TextPlacementInfo textPlacementInfo) {
        int maxSqLength = Integer.MIN_VALUE;
        int startIndex = -1, stopIndex = -1;
        
        // Line has to consist of at least two points
        if (nbrCoords <= 1) {
            return;
        }
        
        for (int i=1; i<nbrCoords; i++) {
            int x = xCoords[i];
            int y = yCoords[i];
            int dx = x - xCoords[i-1];
            int dy = y - yCoords[i-1];
            int sqLength = dx*dx + dy*dy;
            if (sqLength > maxSqLength) {
                maxSqLength = sqLength;
                startIndex = i-1;
                stopIndex = i;
            }
        }
        
        int startX = xCoords[startIndex];
        int startY = yCoords[startIndex];
        int stopX = xCoords[stopIndex];
        int stopY = yCoords[stopIndex];
        textPlacementInfo.setPlacementInfo(startX, startY, stopX, stopY, (int)Math.sqrt(maxSqLength));
    }
    
    // ------------------------------------------------------------------------------------------------------
    // Draw bitmaps
    
    /**
     * Draws pending bitmaps
     *
     * @param tr The current transform
     */
    void drawPoiBitmaps(int[] camBox, float[][] tr) {
        // For all images in the map
        for (int i=0; i<bitmapsToBeRendered.size(); i+=3) {
            if (drawBitmap((int[])bitmapsToBeRendered.elementAt(i),(WFImage)bitmapsToBeRendered.elementAt(i+1), tr,true)) {
                m_poiBoxes.removeElement((POIBox)bitmapsToBeRendered.elementAt(i+2));
            }
        }
    }
    
    /**
     * Draw bitmap with the anchor at it's center coordinate
     */
    private boolean drawBitmap(int[]point,WFImage bitMapImage, float[][]tr, boolean useCollisionDetection){
        return drawBitmap(point, bitMapImage, tr, useCollisionDetection, WFGraphics.ANCHOR_VCENTER|WFGraphics.ANCHOR_HCENTER);
    }
    
    /**
     * Draw bitmap with the given anchor
     *
     * @param tr The current camera transform
     * @param point The coordinate (x,y)
     * @param bitMapImage The Image
     * @param useCollisionDetection, true if we want to use collision detection, false if not.
     *        We will always draw user added images on top.
     * @return boolean if a collition has happend
     */
    private boolean drawBitmap(int[]point,WFImage bitMapImage, float[][]tr, boolean useCollisionDetection, int anchor){
        this.transformable[0] = point[0];
        this.transformable[1] = point[1];
        applyTransform(transformable, transformable.length, tr);
        
        //TODO: when calculate x1, x2... use anchor in consideration
        //other way the collision detection will be shifted
        int margin = bitMapImage.getHeight()/2;
        int x1 = transformable[0]+iScreenInfo.getHalfScreenWidth()-margin;
        int y1 = transformable[1]+iScreenInfo.getHalfScreenHeight()-margin;
        int x2 = transformable[0]+iScreenInfo.getHalfScreenWidth()+margin;
        int y2 = transformable[1]+iScreenInfo.getHalfScreenHeight()+margin;
       
        /* Collision detection for bitmap images */
        boolean collides = false;
        if(useCollisionDetection) {
            int pbX1, pbX2, pbY1, pbY2;
            for(int i=0; i<iDrawedPoiBoxes.size(); i+=4) {
                pbX1 = iDrawedPoiBoxes.get(i);
                pbY1 = iDrawedPoiBoxes.get(i+1);
                pbX2 = iDrawedPoiBoxes.get(i+2);
                pbY2 = iDrawedPoiBoxes.get(i+3);
                
                
                if(!(x2 < pbX1 || x1 > pbX2 || y2 < pbY1 || y1 > pbY2)) {
                    collides = true;
                    break;
                }                       
            }
        }
        
        if(!collides) {
            iDrawedPoiBoxes.add(x1);
            iDrawedPoiBoxes.add(y1);
            iDrawedPoiBoxes.add(x2);
            iDrawedPoiBoxes.add(y2);
            g.drawImage(bitMapImage,transformable[0]+iScreenInfo.getHalfScreenWidth(),transformable[1]+iScreenInfo.getHalfScreenHeight(), anchor);
            return false;
        }
        else {
            return true; //a collision has been detected
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    // MapObjects
    
    private int m_MaxZoomLevelForMapObject = 0;

    /**
      * Add a map object to the map
      * 
      * @param mapObject the map object
      * @param mapObjectImage the MapObjectImage object to be used 
      */
     void addMapObject(MapObject mapObject, MapObjectImage mapObjectImage) {
         if (!iMapObjectImages.containsKey(mapObject.getImageName())) {
             iMapObjectImages.put(mapObject.getImageName(), mapObjectImage);    
         }  
         
         mapObject.setSize(mapObjectImage.getImage().getWidth(), mapObjectImage.getImage().getHeight());
         iMapObjectsQuadTree.addEntry(mapObject, null);
        
         
         if(mapObject.getMaxVisibleZoomLevel() > m_MaxZoomLevelForMapObject)
             m_MaxZoomLevelForMapObject = mapObject.getMaxVisibleZoomLevel();
     }
     
     void removeAllMapObjects() {
         iMapObjectsQuadTree.clear();
         iMapObjectImages.clear();
         iVisibleMapObjects.removeAllElements();
         m_MaxZoomLevelForMapObject = 0;
     }
     
     /**
      * Remove a map object from the map
      * 
      * @param object - the map object
      */
     void removeMapObject(MapObject object) {
         boolean removed = iMapObjectsQuadTree.removeEntry(object);
         if (!removed) {
             if(LOG.isWarn()) {
                 LOG.warn("MapDrawer.removeMapObject()", "not found " + object);
             }
         }
     }
     
     /**
      * Get the map objects current visible on the screen
      * 
      * @return - vector with visible map objects
      */
     Vector getVisibleMapObjects() {
         return iVisibleMapObjects;
     }
     
     /**
      * 
      * 
      * @param aName
      * @return
      */
     MapObjectImage getMapObjectImage(String aName) {
         return (MapObjectImage)iMapObjectImages.get(aName);
     }
     
     /**
      * Update the visble map objects on the screen. 
      * 
      * @param cameraBoundingBox
      * @param scale
      */
     void updateVisibleMapObjects(BoundingBox cameraBoundingBox, float scale) {
         
         iMapObjectQuadTreeNodes.removeAllElements();
         iVisibleMapObjects.removeAllElements();
         
         if (scale > m_MaxZoomLevelForMapObject) {
             return;
         }
        
         iMapObjectsQuadTree.getNodesInside(cameraBoundingBox, iMapObjectQuadTreeNodes); 
        
         for (int i=0; i<iMapObjectQuadTreeNodes.size(); i++) {
             QuadTreeNode node = (QuadTreeNode) iMapObjectQuadTreeNodes.elementAt(i);
            
             MapObject object = (MapObject) node.getAllEntrys();
            
             while (object != null) {
                 if (scale <= object.getMaxVisibleZoomLevel()) {
                     if (object.getLatitude() >= cameraBoundingBox.getSouthLatitude() && 
                             object.getLatitude() <= cameraBoundingBox.getNorthLatitude() &&
                             object.getLongitude() >= cameraBoundingBox.getWestLongitude() && 
                             object.getLongitude() <= cameraBoundingBox.getEastLongitude()) {
                        
                         iVisibleMapObjects.addElement(object);
                     }
                 }
                 object = (MapObject) object.getNext();
             }
         }
     }
     
    /**
     * Method for drawing the visible map objects on the screen. 
     * 
     * @param cameraTransform
     * @param activeMapObject
     */
    void drawMapObjects(float[][] cameraTransform, MapObject activeMapObject) {
        final int size = iVisibleMapObjects.size();
        
        //state if the activeMapObject is visible 
        boolean activeIsVisible = false;
        
        for(int i=0; i<size; i++) {
            MapObject object = (MapObject)iVisibleMapObjects.elementAt(i);            
            if(object != activeMapObject) { //don't need equals
                MapObjectImage imageObject = (MapObjectImage) iMapObjectImages.get(object.getImageName());
                //avoid NPE if image object is null we cannot draw it
                if (imageObject != null) { 
                    internalDrawMapObject(object, imageObject, cameraTransform);
                }
            } else {
                activeIsVisible = true;
            }
        }
        
        // Draw the current active map object last
        if(activeIsVisible && activeMapObject != null) {
            //FIXME this the image object can be null
            MapObjectImage imageObject = (MapObjectImage) iMapObjectImages.get(activeMapObject.getImageName());
            //avoid NPE if image object is null we cannot draw it
            if (imageObject != null) { 
                internalDrawMapObject(activeMapObject, imageObject, cameraTransform);
            }
        }
    }
     
    /*
     * Internal method for drawing map objects. 
     */
    private void internalDrawMapObject(MapObject object, MapObjectImage imageObject, float[][] cameraTransform) {
        transformable[0] = object.getLatitude();
        transformable[1] = object.getLongitude();
        applyTransform(transformable, transformable.length, cameraTransform);
         
        final int screenX = transformable[0]+iScreenInfo.getHalfScreenWidth();
        final int screenY = transformable[1]+iScreenInfo.getHalfScreenHeight();
        
        object.draw(g,imageObject.getImage(), 
                screenX, screenY, imageObject.getAnchor());
        
        //object.getMinX()... should be now set correctly in draw method 
        iDrawedPoiBoxes.add(screenX + object.getMinX());
        iDrawedPoiBoxes.add(screenY + object.getMinY());
        iDrawedPoiBoxes.add(screenX + object.getMaxX());
        iDrawedPoiBoxes.add(screenY + object.getMaxY());
    }
}




