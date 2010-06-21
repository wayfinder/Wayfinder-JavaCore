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
/*
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.map.vectormap.internal.drawer;

import java.util.Hashtable;
import java.util.Vector;

import com.wayfinder.core.map.CopyrightHandler;
import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.MapObjectImage;
import com.wayfinder.core.map.MapObjectListener;
import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.MapInitialConfig;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.map.vectormap.internal.WFTileMapHolder;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.route.PredictRouteTileHandler;
import com.wayfinder.core.map.vectormap.internal.route.SmoothZoomHandler;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFGraphicsFactory;
import com.wayfinder.pal.graphics.WFImage;

/**
 * 
 * 
 *
 */
public class RenderManager implements MapRenderer {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(RenderManager.class);

    // If the scale is higher than this the map will not rotate when tracking
    private static final int MAX_ROTATE_SCALE = 500;
    
    private static final int MAX_MOVEMENT = 100;
    private static final int INITIAL_MOVEMENT_SPEED = 10;
    private static final int MOVEMENT_SPEED_INC = 20;
    
    // Cursor/map movement speed in pixels/s
    private int m_MovementSpeed = INITIAL_MOVEMENT_SPEED;
    private long m_MapUpdateTime = 0;
    
    private WFGraphicsFactory m_WFFactory = null;   
    private Camera m_Camera = null;
    private MapDrawer m_MapDrawer = null;
    private TileMapFormatDesc m_Tmfd = null;
    private MapKeyInterfaceImpl m_MapKeyInterface = null;
    private TileMapControlThread m_TileMapControlThread = null;
    private WFTileMapHolder m_TileMapHolder = null;
    private MapUpdaterThread m_MapUpdater = null;
    private PredictRouteTileHandler m_PredictRouteTileHandler = null;
    private NavigationInfo m_NavigationInfo = null;
    private SmoothZoomHandler m_SmoothZoomHandler = null;
    private MapOverlayManager m_MapOverlayManager = null;
    
    private WFImage m_RenderImage = null;
    private WFGraphics m_RenderImageGraphics = null;
    
    private Vector m_Tracking2Dstrings = new Vector();
    private boolean m_IsMapLocked = false;
    
    private int m_ScreenX, m_ScreenY, m_ScreenWidth, m_ScreenHeight;
    private int m_HalfScreenWidth, m_HalfScreenHeight;
    
    // Coordinates for the active point in the map. 
    private int []m_ActiveScreenPosition = new int[2];
    private int []m_ActiveWorldPosition = new int[2];
    
    // True if we should follow the gps position sent to the map
    private boolean m_FollowGpsPosition = false;
    // True if a gps position has been set to the map, false if not. 
    private boolean m_GpsPositionHasBeenSet = false;
    private int m_GpsLatitude = Integer.MAX_VALUE;
    private int m_GpsLongitude = Integer.MAX_VALUE;
    private float m_TrackingAngle = (float)Math.PI/2;
    
    /* True if new tiles has been added or the camera
     * has been moved. The map needs to be updated. */
    private boolean m_NewTilesHasArrived = false;
    private boolean m_CamHasChanged = false;    
    // True if the map is moving (panning)
    private boolean m_IsMovingMap = false;
    // True if the map are zooming
    private boolean m_IsZooming = false;
    // True if we should zoom to the speed of the road when follow the route, false if not. 
    private boolean m_AutoZoom = false;
    
    // The current used camera bounding box [min lat, max lat, min lon, max lon]
    private int []m_camBox = null;
    /* The camera bounding box wrapped in a BoundingBox, used
     * to calculate which copyright string to used */    
    private BoundingBox m_CamBoundingBox = new BoundingBox();
    
    private int m_copyrightTextPosY;
    
    /* True if we should check if any map feature is selected by
     * the active point in the map, false if not. To make the map
     * faster we only check for selected map feature when all text
     * has been downloaded. */    
    private boolean m_CheckCursorSelection = false;        
    // The x,y screen coordinates of the motion event 
    private int m_PointerX, m_PointerY;
    // True if we should check if a MapObject or POI has been "clicked on"
    private boolean m_CheckPointerPressed = false;
    
    private Hashtable m_BitmapImages = new Hashtable();
    
    // The background and line color of the grid. 
    private int m_gridBgColor, m_gridLineColor;
    // True if the grid should be shown, false if not. 
    private boolean m_isGridEnabled;
    
    private volatile MapObject m_MapSelectedObject;
    
    // Map layer constants
    public static final int ID_MAP_LAYER = 0;
    public static final int ID_ROUTE_LAYER = 1;
    public static final int ID_POI_LAYER = 2;
    public static final int ID_INFO_LAYER = 3;
    public static final int ID_ACP_LAYER = 4;
    
    public RenderManager() {}
        
    public void init(WFGraphicsFactory wfFactory, Camera camera, MapDrawer mapDrawer, 
             TileMapControlThread controlThread, WFTileMapHolder tileMapHolder, 
             MapKeyInterfaceImpl mapKeyInterface, MapUpdaterThread mapUpdater,
             SmoothZoomHandler zoomHandler, PredictRouteTileHandler predictRouteTileHandler,
             MapInitialConfig config) {
        m_WFFactory = wfFactory;
        m_Camera = camera;
        m_TileMapControlThread = controlThread;
        m_TileMapHolder = tileMapHolder;
        m_MapDrawer = mapDrawer;
        m_MapKeyInterface = mapKeyInterface;
        m_MapUpdater = mapUpdater;
        m_SmoothZoomHandler = zoomHandler;
        m_PredictRouteTileHandler = predictRouteTileHandler;
        m_MapOverlayManager = 
            new MapOverlayManager(m_MapDrawer, m_Camera, this, m_TileMapHolder, m_TileMapControlThread);
        
        m_isGridEnabled = config.isGridEnabled();
        m_gridBgColor = config.getGridBackgroundColor();
        m_gridLineColor = config.getGridLineColor();        
        setDrawArea(config.getX(), config.getY(), config.getWidth(), config.getHeight());
        m_copyrightTextPosY = m_ScreenHeight-Utils.get().getFont(Utils.FONT_SMALL).getFontHeight()-10;
    }
    
    public CopyrightHandler getCopyrightHandler() {
        return m_TileMapHolder.getCopyrightHandler();
    }
    
    public void setMapObjectListener(MapObjectListener listener) {
        m_MapOverlayManager.setMapObjectListener(listener);
    }
    
    /**
     * 
     * @return
     */
    public String getServerString() {
        return m_MapOverlayManager.getServerString(); 
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#getActiveObjectName()
     */
    public String getActiveObjectName() {
        return m_MapOverlayManager.getActiveObjectName();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#getActiveScreenPointX()
     */
    public int getActiveScreenPointX() {
        return m_ActiveScreenPosition[0];
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#getActiveScreenPointY()
     */
    public int getActiveScreenPointY() {     
        return m_ActiveScreenPosition[1];
    }
    
    /**
     * Return the world latitude coordinate of the active screen X point. 
     * 
     * @return the world latitude coordinate of the active screen X point.
     */
    public int getActiveWorldLat() {
        return m_ActiveWorldPosition[0];
    }
    
    /**
     * Return the world longitude coordinate of the active screen Y point.
     * 
     * @return the world longitude coordinate of the active screen Y point.
     */
    public int getActiveWorldLon() {
        return m_ActiveWorldPosition[1];
    }
    
    /*
     * Return true if the map needs to be updated one more time to enable all features in the map. 
     * For example strings and outlines on roads etc. 
     */
    private boolean mapNeedToBeUpdated() {        
        if(!m_FollowGpsPosition && (m_IsZooming || m_IsMovingMap)) {
            m_Camera.mapChangeNotify();
            return true;
        }        
        return false;
    }
    
    /**
     * Called from the MapUpdaterThread and are used to prepare the map to be drawn. 
     * 
     * @return
     */
    boolean updateMap() {
        
        m_Camera.setGPSAdjust(0);
        m_IsMovingMap = false;
        m_IsZooming = false;
        m_CheckCursorSelection = false;
        updateRequestedVariables();
        executeMapRenderTasks();
        
        /* Ignore any attempt to update the map before TileMapFormatDesc is loaded. */
        if(m_Tmfd != null) {
            
            m_TileMapControlThread.getTileMapLoader().setOfflineModeForCachedLayer(false);
            
            // Process any actions sent to the MapKeyInterface
            m_MapKeyInterface.handleAction();  
            
            // 
            if(m_FollowGpsPosition) {
                m_Camera.setGPSAdjust(m_ActiveScreenPosition[1]-m_HalfScreenHeight);
                m_ActiveScreenPosition[0] = m_HalfScreenWidth;
                
                // Update the GPS position if at least one GPS position has been set since we start the map. 
                if(m_GpsPositionHasBeenSet) {
                    int tracking_zoom = (int)m_Camera.getZoomLevel();
                    
                    //check if we have a nav info and if we follow a route 
                    if(m_AutoZoom && m_NavigationInfo != null && m_NavigationInfo.getNextWpt() != null) {
                        tracking_zoom = m_SmoothZoomHandler.doSmoothZoom(m_NavigationInfo);
                        m_Camera.setScale(tracking_zoom);
                    }
                    
                    m_Camera.setPosition(m_GpsLatitude, m_GpsLongitude);
                    
                    if(m_PredictRouteTileHandler != null)
                        m_PredictRouteTileHandler.checkCurrentDistanceDiff(m_GpsLatitude, m_GpsLongitude);
                    
                    // Only rotate in navigation views if scale is below rotation limit
                    if (tracking_zoom < MAX_ROTATE_SCALE) {
                        m_Camera.setRotation(m_TrackingAngle);
                    } else {
                        m_Camera.setRotation(Utils.NORTH_ROTATION_ANGLE);
                    }
                }
                m_IsMovingMap = true;                                
            }
            
            // Update the map camera
            m_CamHasChanged = m_Camera.update();
            final int zoomLevel = (int)m_Camera.getZoomLevel();
            updateActiveWorldPos();
            
            m_camBox = m_Camera.getCameraBoundingBoxInternal();            
            if(!m_IsZooming) {
                m_TileMapControlThread.updateTileMaps(m_camBox, zoomLevel);
            }
            
            // Update the loaded map status (add or remove maps)                       
            m_NewTilesHasArrived = m_TileMapHolder.update(zoomLevel, m_camBox);
            
            if(!m_FollowGpsPosition) {
                if(m_IsMovingMap || m_IsZooming) {
                    // Clear the processed strings if we have moved the map
                    m_TileMapHolder.clearStrings();
                } else {
                    /* To increase the performance we are only checking if any map feature is 
                     * marked by the active position in the map when it hasn't moved.
                     */
                    m_CheckCursorSelection = true;
                }                                
            }        
        }
        
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#renderMap()
     */
    public WFImage renderMap() {
        if(m_RenderImage == null || m_RenderImage.getWidth() < m_ScreenWidth || m_RenderImage.getHeight() < m_ScreenHeight) {
            m_RenderImage = m_WFFactory.createWFImage(m_ScreenWidth,m_ScreenHeight);
            m_RenderImageGraphics = m_RenderImage.getWFGraphics();
            renderBackgroundGrid(m_RenderImageGraphics);
        }
        
        // Only update the map if anything has changed since the last update. 
        if (m_CamHasChanged || m_NewTilesHasArrived) {
            renderMap(m_RenderImageGraphics);        
        }
        return m_RenderImage;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#renderMap(com.wayfinder.core.map.graphics.WFGraphics)
     */
    public void renderMap(WFGraphics g) {
        long time = System.currentTimeMillis();
        
        if (m_isGridEnabled) {
            renderBackgroundGrid(g);            
        }
        
        if (m_Tmfd != null) {
            internalRenderMap(g);
        }
        
        // Update the map rendering time, used to calculate how fast we should move the map. 
        m_MapUpdateTime = System.currentTimeMillis() - time;
    }
    
    /*
     * Internal method that are used to render the map on the WFGraphics object specified 
     * by the parameter. 
     */
    private void internalRenderMap(WFGraphics g) {
        
        // Lookup some frequently used member variables.
        final Camera camera = m_Camera;
        final float zoomLevel = camera.getZoomLevel();
        final Vector tracking2Dstrings = m_Tracking2Dstrings;
        final boolean isMovingMap = m_IsMovingMap;
        final TileMapFormatDesc tmfd = m_Tmfd;
        final MapDrawer mapDrawer = m_MapDrawer;
        final int scaleIndex = tmfd.getScaleIndexFromZoomLevel(camera.getZoomLevel());
        
        // set to decide later if antialiasing should be used
        camera.setCameraIsMoving(isMovingMap);
        
        if(!m_isGridEnabled) {
            g.setColor(tmfd.getBackgroundColor());
            g.fillRect(0, 0, m_ScreenWidth, m_ScreenHeight);
        }
        
        // Set anti-alias once for all features. (check drawPath() for exception).
        g.allowAntialias(!camera.isMoving());

        // Get the array of the visible tiles on the screen. 
        final Vector []tmwArray = m_TileMapHolder.getTileMaps();        
        final int[] camBox = m_camBox;
        mapDrawer.init(tmfd, zoomLevel, g);        
        tracking2Dstrings.removeAllElements();
        
        // Determine if we should draw outline on the roads or not.  
        int startPass=0;
        if (m_IsZooming || isMovingMap || m_FollowGpsPosition) {
            startPass = 1;
        }

        mapDrawer.initDrawing(camera);

        /*           
         * The map is drawn i Level order, from 0 to Utils.MAX_LEVEL
         * and for each level the map is drawn in layer number order
         * 
         * Current known layers:
         * layer nbr 0 route layerID 1
         * layer nbr 1 map layerID 0
         * layer nbr 2 poi layerID 2
         * layer nbr 3 traffic info layerID 3
         * layer nbr 4 ACP layerID 4 
         */ 
        for (int level=0; level<=Utils.MAX_LEVEL; level++) {
            for (int pass=startPass; pass<=1; pass++) {
                final int len = tmwArray.length;
                for (int t = 0; t < len; t++){
                    final Vector tmWrappers = tmwArray[t];
                    final int size = tmWrappers.size();
                    for (int i = 0; i < size; i++) { 
                        TileMapWrapper tmw = (TileMapWrapper)tmWrappers.elementAt(i);                        
                        if(!tmw.hasExpired(tmfd.getUpdateTimeForLayer(tmw.getLayerID()))) {
                            mapDrawer.drawMap(tmw, level, pass, startPass, camBox, tracking2Dstrings, camera, scaleIndex);
                        } else {
                            //XXX: Fix to avoid that the same tile are reseted MAX_LEVEL times
                            if(level == 0) {
                                if(LOG.isInfo()) {
                                    LOG.info("RenderManager.drawMap()", "layerID "+tmw.getLayerID()+" has expired");
                                }
                                m_TileMapControlThread.reloadTileID(tmw.getTileID());
                            }
                        }
                    }
                }
            }
        }        
        
        if (!isMovingMap && !m_FollowGpsPosition) {
            // Draw 2D, non-tracking, map strings
            if (m_TileMapHolder.updateStrings(m_WFFactory, g)) {
                drawTexts();
            }
            mapDrawer.drawCityCentreTexts(tmfd.getTextColor(), tmfd.getBackgroundColor());
        } else if (m_FollowGpsPosition) {
            // Draw tracking map strings
            mapDrawer.init2DTracking();
            final int size = tracking2Dstrings.size();
            if (camera.isIn3DMode()) {
                for (int i=0; i<size; i++) {
                    TextPos textPos = (TextPos) tracking2Dstrings.elementAt(i);
                    final int[] screenPos = camera.getScreenCoordinateInternal(textPos.getX(), textPos.getY());
                    mapDrawer.drawTrackingTexts(textPos.getText(), screenPos[0], screenPos[1], tmfd);
                    mapDrawer.drawTrackingTexts(textPos.getText(), textPos.getX(), textPos.getY(), tmfd);
                }
            } else {
                for (int i=0; i<size; i++) {
                    TextPos textPos = (TextPos) tracking2Dstrings.elementAt(i);
                    mapDrawer.drawTrackingTexts(textPos.getText(), textPos.getX(), textPos.getY(), tmfd);
                }
            }
        }
        
        // Update the list of MapObjects that are visible on the screen. 
        mapDrawer.updateVisibleMapObjects(m_CamBoundingBox, zoomLevel);

        if (m_CheckCursorSelection) {
            // Check if any MapObject, poi, road etc is marked by the active point in the map.
            m_MapOverlayManager.checkCursorSelection();
        }
        
        // Check if any MapObject or POI has been pressed
        if (m_CheckPointerPressed) {
            m_CheckPointerPressed = false;
            m_MapOverlayManager.checkPressedMapOrPoiObjects(m_PointerX, m_PointerY);
        }
        
        float[][] transform = camera.getTransform();
        // Draw added MapObjects
        mapDrawer.drawMapObjects(transform, m_MapSelectedObject);
        //instead of 
        //m_MapDrawer.drawMapObjects(m_Camera.getTransform(true), m_MapOverlayManager.getActiveMapObject());
        
        // Draw POI and city center bitmaps
        mapDrawer.drawPoiBitmaps(camBox, transform);
        
        // Fetch and draw copyright string
        g.setColor(tmfd.getTextColor());
        g.setFont(Utils.get().getFont(Utils.FONT_SMALL));
        m_CamBoundingBox.setBoundingBox(camBox[1], camBox[0], camBox[3], camBox[2], 0);
        final String crString = m_TileMapHolder.getCopyrightString(m_CamBoundingBox, Utils.get().getFont(Utils.FONT_SMALL), m_ScreenWidth);
        g.drawText(crString, m_ScreenWidth>>1, m_copyrightTextPosY, WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_TOP);
        
        if (mapNeedToBeUpdated()) {
            m_MapUpdater.updateMap();
        }        
    }
    
    private void drawTexts() {
        Vector []tileMapWrappersPerLayerNumberArray = m_TileMapHolder.getTileMaps();
        for (int i=0; i<tileMapWrappersPerLayerNumberArray.length; i++) {
            int size = tileMapWrappersPerLayerNumberArray[i].size();
            for (int j=0; j<size; j++) {
                TileMapWrapper tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[i].elementAt(j);
                if (m_TileMapHolder.getCurrentDetailLevel(i) == tmw.getDetailLevel()) {
                    m_MapDrawer.drawPlacedTexts(tmw.getTextDrawObjects(), m_Tmfd);
                }
            }
        }
    }
    
    /**
     * Return a poi-image from the map.
     * 
     * @param url, the name of the poi image (ex btat_trainstation.png)
     * @return poi image
     */
    WFImage getBitmapImage(String url) {      
        WFImage img = (WFImage)m_BitmapImages.get(url);
        if (img == null) {
            byte[] data = m_TileMapControlThread.getBitmapImage(url);
            if (data != null) {
                img = m_WFFactory.createWFImage(data,0,data.length);
                m_BitmapImages.put(url, img);
            }
        }
        return img;
    }
    
    /**
     * Called if any point in the map has been pressed by a motion/pointer event. 
     * A pointer-clicked event is sent if there are less then 200 ms between
     * a pointerPressed and pointerReleased event. 
     * 
     * @param x the screen x coordinate
     * @param y the screen y coordinate
     */
    void setPointerPressed(int x, int y) {        
        m_CheckPointerPressed = true;
        m_PointerX = x;
        m_PointerY = y;
    }

    /**
     * @return true if the map has been locked, false if not. 
     */
    public synchronized boolean isMapLocked() {
        return m_IsMapLocked;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#lockMap()
     */
    public synchronized void lockMap() {
        m_IsMapLocked = true;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapRenderer#unlockMap()
     */
    public synchronized void unlockMap() {
        m_IsMapLocked = false;
        m_MapUpdater.updatedIfNecessary();
    }

    /**
     * Set the draw area used in the map. 
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    void setDrawArea(int x, int y, int width, int height) {
        
        // Check if the dimension of the screen differs before changing anything. 
        if(m_ScreenX != x || m_ScreenY != y || m_ScreenWidth != width || m_ScreenHeight != height) {
            m_ScreenX = x;
            m_ScreenY = y;
            m_ScreenWidth = width;
            m_ScreenHeight = height;
            m_MapOverlayManager.setDimension(m_ScreenWidth, m_ScreenHeight);
            m_HalfScreenWidth = m_ScreenWidth / 2;
            m_HalfScreenHeight = m_ScreenHeight / 2;
            m_ActiveScreenPosition[0] = m_HalfScreenWidth;
            m_ActiveScreenPosition[1] = m_HalfScreenHeight;
            m_Camera.setGPSAdjust(0);
            ScreenInfo si = new ScreenInfo(m_ScreenWidth, m_ScreenHeight, true);
            m_Camera.setScreenInfo(si);
            m_MapDrawer.setScreenInfo(si);
            updateActiveWorldPos();
            if (m_PredictRouteTileHandler != null) {
                m_PredictRouteTileHandler.setScreenHeight(height);
            }
            
            // Check to see that the copyright string still are inside the new
            // draw area, otherwise set the position to the bottom of the screen. 
            if(m_copyrightTextPosY < m_ScreenY || 
                    m_copyrightTextPosY > m_ScreenHeight-Utils.get().getFont(Utils.FONT_SMALL).getFontHeight()) {
                m_copyrightTextPosY = m_ScreenHeight-Utils.get().getFont(Utils.FONT_SMALL).getFontHeight()-10;
            }
        }
    }
    
    /**
     * Return the width of the map. 
     * 
     * @return the width of the map.
     */
    public int getScreenWidth() {
        return m_ScreenWidth;
    }
    
    /**
     * Return the height of the map.
     * 
     * @return the height of the map.
     */
    public int getScreenHeight() {
        return m_ScreenHeight;
    }
    
    /**
     * Return true if we are following the GPS position, false if not. 
     * 
     * @return true if we are following the GPS position, false if not.
     */
    public boolean isFollowGpsPosition() {
        return m_FollowGpsPosition;
    }
    
    /**
     * Return true if the screenX and screenY point are inside the screen,
     * false if not. 
     * 
     * @param screenX screen X point. 
     * @param screenY screen Y point. 
     * @return true if screenX and screenY are inside the screen, false if not.  
     */
    public boolean insideScreen(int screenX, int screenY) {
        if(screenX > m_ScreenX && screenX < m_ScreenWidth &&
           screenY > m_ScreenY && screenY < m_ScreenHeight) {
            return true;
        }
        return false;
    }
    
    /**
     * Get the map movement (in pixels)
     * 
     * @return map movement
     */
    int getMovement() {
        int movement = (int)(m_MovementSpeed * m_MapUpdateTime) / 1000;
        return movement > MAX_MOVEMENT ? MAX_MOVEMENT : movement;
    }
    
    void resetMovementSpeed() {
        m_MovementSpeed = INITIAL_MOVEMENT_SPEED;
    }
    
    void moveMap(int dx, int dy) {
        
        if(dx != 0 || dy != 0) {
            m_Camera.translate(dx, dy);            
        }
        m_MovementSpeed += MOVEMENT_SPEED_INC;
        m_IsMovingMap = true;
        setFollowGpsPosition(false);
    }
    
    void zoomMap(boolean aZoomOut) {        
        m_Camera.scaleTo(aZoomOut,
                m_ActiveWorldPosition[0],
                m_ActiveWorldPosition[1],
                m_ActiveScreenPosition[0]-m_HalfScreenWidth,
                m_ActiveScreenPosition[1]-m_HalfScreenHeight);        
        m_IsZooming = true;
        m_AutoZoom = false;
    }

    /*
     * Updates the world coordinates for the cursor
     */
    private void updateActiveWorldPos() {
        long[] p = 
            m_Camera.getWorldCoordinateInternal(m_ActiveScreenPosition[0]-m_HalfScreenWidth, 
                                                m_ActiveScreenPosition[1]-m_HalfScreenHeight);
        m_ActiveWorldPosition[0] = (int)p[0];
        m_ActiveWorldPosition[1] = (int)p[1];
    }

    void setFollowGpsPosition(boolean shouldTrack) {
        m_FollowGpsPosition = shouldTrack;
        m_MapDrawer.setTracking(shouldTrack);
        m_AutoZoom = shouldTrack;
        if(!m_FollowGpsPosition) {
            m_Camera.setRotation(Utils.NORTH_ROTATION_ANGLE);
            m_Camera.setGPSAdjust(0);
        }
        
        // Don't download string tiles for the poi layer when we follow a gps position. 
        m_TileMapControlThread.setDownloadStringTile(ID_POI_LAYER, !m_FollowGpsPosition);
    }
    
    /**
     * Set the lat and lon coordinates at the center of the screen, this method
     * doesn't consider the active screen points set. 
     * 
     * @param lat
     * @param lon
     */
    void setCoordsCenterOnScreen(int lat, int lon) {
        m_Camera.setPosition(lat, lon);
        m_IsMovingMap = true;
    }
    
    /**
     * Set the lat and lon coordinate centered at the active screen point. 
     * 
     * @param lat
     * @param lon
     */
    void setCenter(int lat, int lon) {
        if(m_ActiveScreenPosition[0] != m_HalfScreenWidth || m_ActiveScreenPosition[1] != m_HalfScreenHeight) {
            m_Camera.internalSetPosition(lat, lon, 
                                 m_ActiveScreenPosition[0]-m_HalfScreenWidth, 
                                 m_ActiveScreenPosition[1]-m_HalfScreenHeight);
        } else {
            m_Camera.setPosition(lat, lon);
        }
        m_IsMovingMap = true;
    }
    
    void setIsMoving(boolean isMoving) {
        m_IsMovingMap = isMoving;
    }
    
    void setActiveScreenPoint(int screenX, int screenY) {
        m_ActiveScreenPosition[0] = screenX;
        m_ActiveScreenPosition[1] = screenY;        
        updateActiveWorldPos();
        m_Camera.mapChangeNotify();
    }
    
    void internalSetGpsInformation(int lat, int lon, float angle) {
        m_GpsLatitude = lat;
        m_GpsLongitude = lon;
        m_TrackingAngle = angle;
        m_GpsPositionHasBeenSet = true;
    }
    
    void internalSetNavigationInfo(NavigationInfo navInfo) {
        m_NavigationInfo = navInfo;
    }
    
    void setNewTileMapFormatDesc(TileMapFormatDesc tmfd) {
        m_Tmfd = tmfd;
        m_TileMapHolder.setNewTileMapFormatDesc(m_Tmfd);
        
        // "Allan the hack", for some reason the map is blank when we
        // start a fresh installed app and have pre-installed maps on
        // the memory card. I can't found the cause of the bug but to
        // change the zoom a little seems to solve the problem and I don't
        // have time to look more into it now. Feel free to try to fix the 
        // issue rather then complain that this is a ugly hack :)
//        if(m_Camera.getZoomLevel() > 0.4)
//            m_Camera.setScale(m_Camera.getZoomLevel()-0.1f);
//        else
//            m_Camera.setScale(m_Camera.getZoomLevel()+0.1f);
        
        if(!m_FollowGpsPosition) {
            // Enable downloading of POI strings if the user doesn't follow the gps position. 
            m_TileMapControlThread.setDownloadStringTile(ID_POI_LAYER, true);
        }
    }
    
    void updateRouteID(String routeID, boolean isUsed) {
        m_Camera.mapChangeNotify();
        // If the route id is null or the new route id is used we remove 
        // the current loaded route tiles from the map storage. 
        if(routeID == null || isUsed) {
            m_TileMapHolder.clearRouteLayer();
        }
        m_TileMapHolder.setRouteID(routeID);
        internalSetNavigationInfo(null);
    }
    
    // ----------------------------------------------------------------------------------------------------
    // Update MapRenderTask:s
    
    private LinkedList m_MapRenderTasks = new LinkedList();
    private LinkedList m_MapRenderTasksToExecute = new LinkedList();
    
    /*
     * Called from the MapRenderTask when a new task has been requested. 
     */
    void addMapTask(MapRenderTask task) {        
        synchronized (m_MapRenderTasks) {
            m_MapRenderTasks.addLast(task);
        }        
    }
    
    /*
     * Update and execute the MapRenderTasks added to the queue.  
     */
    private void executeMapRenderTasks() {
        // Update the array with new map tasks
        synchronized (m_MapRenderTasks) {
            while(m_MapRenderTasks.size() > 0) {
                m_MapRenderTasksToExecute.addLast((MapRenderTask)m_MapRenderTasks.removeFirst());
            }
        }
        
        // Execute the the map tasks
        MapRenderTask task;
        while(m_MapRenderTasksToExecute.size() > 0) {
            task = (MapRenderTask)m_MapRenderTasksToExecute.removeFirst();
            task.executeTask();
            task = null;
        }
    }
    
    // ----------------------------------------------------------------------------------------------------
    
    private boolean m_AddMapObjectRequested, m_RemoveMapObjectRequested;
    private boolean m_RemoveAllMapObjectsRequested;
    private Vector m_MapObjectsToAdd = new Vector();
    private Vector m_MapObjectsToRemove = new Vector();
    
    private synchronized void updateRequestedVariables() {
        
        if (m_RemoveAllMapObjectsRequested) {
            m_MapDrawer.removeAllMapObjects();
            m_RemoveAllMapObjectsRequested = false;
            m_Camera.mapChangeNotify();
        }
        
        if (m_RemoveMapObjectRequested) {
            final int size = m_MapObjectsToRemove.size();
            for (int i=0; i<size; i++) {
                MapObject mo = (MapObject)m_MapObjectsToRemove.elementAt(i);
                m_MapDrawer.removeMapObject(mo);
            }
            m_MapObjectsToRemove.removeAllElements();
            m_RemoveMapObjectRequested = false;
            m_Camera.mapChangeNotify();
        }
        
        if (m_AddMapObjectRequested) {
            final int size = m_MapObjectsToAdd.size();
            for (int i=0; i<size; i+=2) {
                MapObject mo = (MapObject)m_MapObjectsToAdd.elementAt(i);
                MapObjectImage moImg = (MapObjectImage)m_MapObjectsToAdd.elementAt(i+1);
                m_MapDrawer.addMapObject(mo, moImg);
            }
            m_MapObjectsToAdd.removeAllElements();
            m_AddMapObjectRequested = false;
            m_Camera.mapChangeNotify();
        }          
    }
    
    public synchronized void requestAddMapObject(MapObject mapObject, MapObjectImage mapObjectImage) {
        m_MapObjectsToAdd.addElement(mapObject);
        m_MapObjectsToAdd.addElement(mapObjectImage);
        m_AddMapObjectRequested = true;
    }
    
    public synchronized void requestRemoveMapObject(MapObject mapObject) {
        m_MapObjectsToRemove.addElement(mapObject);
        m_RemoveMapObjectRequested = true;
        //deselect automatically when object is removed 
        if (mapObject == m_MapSelectedObject) {
            m_MapSelectedObject = null;
        }
    }
    
    public synchronized void requestRemoveAllMapObjects() {
        m_RemoveAllMapObjectsRequested = true;
        //deselect automatically when object is removed 
        m_MapSelectedObject = null;
    }
    
    public void setSelectedMapObject(MapObject selectedObject) {
        m_MapSelectedObject = selectedObject;
    }

    public void setShowServerPOIs(boolean isVisible) {
        m_MapDrawer.setShowServerPOIs(isVisible);
        m_Camera.mapChangeNotify();
    }
    
    void internalSetCopyrightTextPosY(int screenY) {
        if(screenY >= m_ScreenY && screenY <= (m_ScreenHeight-Utils.get().getFont(Utils.FONT_SMALL).getFontHeight())) {
            m_copyrightTextPosY = screenY;
        }
    }
    
    // ---------------------------------------------------------------------------------------------------------
    // Background grid
    
    // Grid move offset
    private int m_gridMoveOffsetX = 0; 
    private int m_gridMoveOffsetY = 0;
    
    private int m_stepSize = 1;
    
    // Holds the coordinates where to draw the grid lines
    private int []m_gridVerticalLinesCoords = new int[400];
    private int []m_gridHorizontalLinesCoords = new int[400];    
    private int []sc = new int[4];
    
    private static final int MIN_GRID_SQUARE_SIZE = 1500;
    private static final int MIN_NBR_GRID_SQUARES = 2;
    private static final int MAX_NBR_GRID_SQUARES = 12;
    private static final int GRID_SQUARE_RATIO = MAX_NBR_GRID_SQUARES / MIN_NBR_GRID_SQUARES;
    
    /**
     * Render the background grid
     * 
     * @param g - graphics object
     */
    private void renderBackgroundGrid(WFGraphics g) {
        if (m_camBox == null) {
            return;
        }
        
        // Calculate the width of the camera bounding box
        final int cameraBboxWidth = m_camBox[3] - m_camBox[2];
        if (cameraBboxWidth == 0) {
            if(LOG.isWarn()) {
                LOG.warn("RenderManager.renderBackgroundGrid()", "something is wrong cameraBboxWidth is 0");
            }
            //don't draw anything 
            return;
        }
        int nbrGridLines = cameraBboxWidth / (MIN_GRID_SQUARE_SIZE*m_stepSize);
        
        while (nbrGridLines < MIN_NBR_GRID_SQUARES || nbrGridLines > MAX_NBR_GRID_SQUARES) {
            if (nbrGridLines > MAX_NBR_GRID_SQUARES) {
                m_stepSize *= GRID_SQUARE_RATIO;
            } else if (nbrGridLines < MIN_NBR_GRID_SQUARES) {
                m_stepSize /= GRID_SQUARE_RATIO;
            }
            nbrGridLines = cameraBboxWidth / (MIN_GRID_SQUARE_SIZE * m_stepSize);
        }
        
        final int gridSquareSize = MIN_GRID_SQUARE_SIZE * m_stepSize;
        
        // Calculate the offset, i.e. the number of mc2 units that the grid has been moved in x/y
        m_gridMoveOffsetX = m_ActiveWorldPosition[1] % gridSquareSize;
        m_gridMoveOffsetY = m_ActiveWorldPosition[0] % gridSquareSize;
        
        // Calculate world coordinates for the vertical lines of the grid
        int xcnt = 0;
        final int startX = m_ActiveWorldPosition[1] - m_gridMoveOffsetX;
        final int minX = Math.min(m_camBox[2], m_camBox[3]);
        final int maxX = Math.max(m_camBox[2], m_camBox[3]);
        for (int x=startX; x>=minX; x-=gridSquareSize) {
            m_gridVerticalLinesCoords[xcnt]   = m_camBox[0];
            m_gridVerticalLinesCoords[xcnt+1] = x;
            m_gridVerticalLinesCoords[xcnt+2] = m_camBox[1];
            m_gridVerticalLinesCoords[xcnt+3] = x;
            xcnt += 4;
            
            if (x - minX < gridSquareSize) {
                break;
            }
        }
        for (int x=startX; x<maxX; x+=gridSquareSize) {
            m_gridVerticalLinesCoords[xcnt]   = m_camBox[0];
            m_gridVerticalLinesCoords[xcnt+1] = x;
            m_gridVerticalLinesCoords[xcnt+2] = m_camBox[1];
            m_gridVerticalLinesCoords[xcnt+3] = x;
            xcnt += 4;
            
            if (maxX - x < gridSquareSize) {
                break;
            }
        }
        
        // Calculate world coordinates for horizontal lines of the grid
        int ycnt = 0;
        final int startY = m_ActiveWorldPosition[0] - m_gridMoveOffsetY;
        final int minY = Math.min(m_camBox[0], m_camBox[1]);
        final int maxY = Math.max(m_camBox[0], m_camBox[1]);
        for (int y=startY; y>=minY; y-=gridSquareSize) {
            m_gridHorizontalLinesCoords[ycnt]   = y;
            m_gridHorizontalLinesCoords[ycnt+1] = m_camBox[2];
            m_gridHorizontalLinesCoords[ycnt+2] = y;
            m_gridHorizontalLinesCoords[ycnt+3] = m_camBox[3];
            ycnt += 4;
        }
        for (int y=startY; y<=maxY; y+=gridSquareSize) {
            m_gridHorizontalLinesCoords[ycnt]   = y;
            m_gridHorizontalLinesCoords[ycnt+1] = m_camBox[2];
            m_gridHorizontalLinesCoords[ycnt+2] = y;
            m_gridHorizontalLinesCoords[ycnt+3] = m_camBox[3];
            ycnt += 4;
        }
        
        // Convert to screen coordinates
        final float[][] transform = m_Camera.getTransform();
        if (m_Camera.isIn3DMode()) {
            m_MapDrawer.applyTransform(m_gridVerticalLinesCoords, xcnt, transform);
            m_MapDrawer.applyTransform(m_gridHorizontalLinesCoords, ycnt, transform);
        } else {
            m_MapDrawer.applyTransformUnpanned(m_gridVerticalLinesCoords, xcnt, transform);
            m_MapDrawer.applyTransformUnpanned(m_gridHorizontalLinesCoords, ycnt, transform);
        }
        
        // Draw the grid
        g.setColor(m_gridBgColor);
        g.fillRect(0, 0, m_ScreenWidth, m_ScreenHeight);
        g.setColor(m_gridLineColor);
        
        for (int i=0; i<xcnt; i+=4) {            
            final int x1 = m_gridVerticalLinesCoords[i+2] + m_HalfScreenWidth;
            final int x2 = m_gridVerticalLinesCoords[i]   + m_HalfScreenWidth;                        
            final int y1 = m_gridVerticalLinesCoords[i+3] + m_HalfScreenHeight;
            final int y2 = m_gridVerticalLinesCoords[i+1] + m_HalfScreenHeight;            
            g.drawLine(x1, y1, x2, y2, 2);
        }
        
        for (int i=0; i<ycnt; i+=4) {            
            final int x1 = m_gridHorizontalLinesCoords[i+2] + m_HalfScreenWidth;
            final int x2 = m_gridHorizontalLinesCoords[i]   + m_HalfScreenWidth;            
            final int y1 = m_gridHorizontalLinesCoords[i+3] + m_HalfScreenHeight;
            final int y2 = m_gridHorizontalLinesCoords[i+1] + m_HalfScreenHeight;            
            g.drawLine(x1, y1, x2, y2, 2);
        }
        
        if (!m_Camera.isIn3DMode() && m_IsZooming) {
            fillVisibleTilesWithBackgroundColor(g);
        }
    }
    
    /*
     * Fill all MAP tiles that contains any features with the default background color to avoid 
     * that the grid is visible in the background. 
     * 
     * Since the most of the polygons in the tile are located in one of the highest importance 
     * there is a big chance that the background grid will be visible inside a tile that contains
     * data, e.g. the grid will be visible and roads will be drawn on top of the grid. This will 
     * be most visible when we zoom in the map. Therefore we go throw all visible MAP tiles and fill 
     * them with the default background color if they exist in the drawing queue.  
     * 
     * NOTE: The current implementation doesn't support to fill the tiles with background color
     * when we are in 3D mode.
     */
    private void fillVisibleTilesWithBackgroundColor(final WFGraphics g) {
        
        if(m_Tmfd == null)
            return;
        
        final int mapLayerNbr = m_Tmfd.getLayerNbrFromID(0);
        final Vector []tilesArray = m_TileMapHolder.getTileMaps();
        Vector mapTiles = tilesArray[mapLayerNbr];
        
        if(mapTiles != null) {
            g.setColor(m_Tmfd.getBackgroundColor());
            final float [][]tr = m_Camera.getTransform();
            final int size = mapTiles.size();
            
            // Go throw all map tiles and fill the tile size with the background color
            for(int i=0; i<size; i++) {
                TileMapWrapper tmw = (TileMapWrapper)mapTiles.elementAt(i);
                int mc2UnitPerTile = (int)m_Tmfd.getMc2UnitsPerTile(mapLayerNbr, tmw.getDetailLevel());
                
                sc[0] = (tmw.getLatitude()) * mc2UnitPerTile;
                sc[1] = tmw.getLongitude() * mc2UnitPerTile;
                sc[2] = (tmw.getLatitude()+1) * mc2UnitPerTile;
                sc[3] = (tmw.getLongitude()+1) * mc2UnitPerTile;      
                if(m_Camera.isIn3DMode())
                    m_MapDrawer.applyTransform(sc, sc.length, tr);                
                else
                    m_MapDrawer.applyTransformUnpanned(sc, sc.length, tr);
                sc[0] += m_HalfScreenWidth; // x1
                sc[1] += m_HalfScreenHeight; // y1
                sc[2] += m_HalfScreenWidth; // x2
                sc[3] += m_HalfScreenHeight; // y2
                
                int width = Math.abs(sc[2]-sc[0]);
                int height = Math.abs(sc[3]-sc[1]); 
                
                // Adjust the location the tiles so that the tiles on the
                // top of the screen also will be drawn with the background color. 
                sc[1] -= height;
                if(sc[0] < 0) {
                    width += sc[0];
                    sc[0] = 0;
                }
                if(sc[1] < 0) {
                    height += sc[1];
                    sc[1] = 0;
                }
                
                g.fillRect(sc[0], sc[1], width, height);
            }
        }    
    }    
}
