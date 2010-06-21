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

import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapInitialConfig;

public class Camera implements MapCameraInterface {
    
    private boolean hasChanged = false;
    private boolean isIn3DMode = false;
    
    private int skyHeight;
    
    private boolean ignoreOverflows = false;
    private ScreenInfo iScreenInfo = null;
    
    private float rotation = 0.0f;
    private float rotation_during_rendering = 0.0f;
    private long[] position = {0, 0};
    private long[] position_during_rendering = {0, 0};
    private float zoomLevel = 1.0f; // Note: This is ~1/scale
    private float iZoomLevelDuringRender = 1.0f;
    
    private long[] iCameraPosition3D = {0, 0, 0};
    
    private float currentPanningAngle;
    private float FOV_X;//(float)(5.0f * Math.PI / 6.0f); // 132 grader?
    private float FOV_Y;//(float)(5.0f * Math.PI / 6.0f); // 132 grader?
    private float pan_angle;
    
    private double coslat = 1.0;
        
    public static final float MIN_ZOOM = 0.3f;
    public static final float MAX_ZOOM = 24000.0f;
 
    private static final float SCALING_SPEED = 0.001f; //scaling per millisecond (hard to define)
    private static long FRAMETIME_THRESHOLD = 1000;
    
    private float[][] viewProj;
    private double[][] viewProjInverse;
   
    private long frameTime = 0;
    private long frameDuration = 0;
    private int m_GpsAdjust;
    
    private double[] up, right, forward;
    
    // North and south map limits
    private static final int NORTH_MAP_LIMIT_MC2 = 885000000;
    private static final int SOUTH_MAP_LIMIT_MC2 = -NORTH_MAP_LIMIT_MC2;
    
    // Precision limit when checking Camera limits. This is necessary due
    // to rounding errors when calculating the Camera's bounding box :(
    private static final int PRECISION_ACCEPTANCE = 100;
    
    // Indicate if camera has reached the north/south limit
    private boolean iCameraAtNorthLimit = false;
    private boolean iCameraAtSouthLimit = false;
    
    //*********************** CHANGE HERE *************************//
    //**** Parameters to change if different camera is desired ****//
    
    // Choose desired Field of view horizontally
    // the vertical FOV (FOVY) will be calculated depending on aspect ratio
    // Note: Small changes give better results!
    private static final double DESIRED_FOVX = Math.PI/5.0f;
//    private static final double DESIRED_FOVX = Math.PI/5.5f;
    
    // Choose desired panning angle
    // 0 degrees will point straight down, PI/2 degrees will point into the horizon
    // WARNING: No far clipping is used so make sure the FOVY and pan angles are compatible! (Do not have large pan angle with large FOV)
    // Note: Small changes give better results!
//    private static final double DESIRED_PAN_ANGLE = Math.PI/3.0f;
    private static final double DESIRED_PAN_ANGLE = Math.PI/3.4f;
    
    // Choose correction factor for 3D
    // Represents the distance to move the camera from the map plane in the negative view direction
    // This value cannot be calculated and must be set so that desired corresponding zoom will be used
    // Usage: Tweak the zoom in 3D to load a suitable size of the map
    private float ZOOM_CORRECTION = 6000.0f;
  
    // The maximum allowed step in pixels to move the map.
//    private final int MAX_TRANSLATION = 50;
   
    //*************************************************************//
    //*********************** CHANGE HERE *************************//
    
    /**
     * Creates a new instance of Camera
     *
     * @param initialZoom Initial zoom value.
     * @param initialCoordinate Starting coordinate
     * @param initialRotation Starting rotational angle for the map
     */
    public Camera() {
        up = new double[]{0,1,0};
        forward = new double[]{0,0,-1};
        right = new double[]{1,0,0};
        
        pan_angle = (float)(-Math.PI/6.0f);
        
        viewProj = new float[][] {{1, 0, 0, 0},
        {0, 1, 0, 0},
        {0, 0, 1, 0},
        {0, 0, 0, 1}};
        
        viewProjInverse = new double[][] {{1, 0, 0, 0},
        {0, 1, 0, 0},
        {0, 0, 1, 0},
        {0, 0, 0, 1}};
    }
    
    public void init(MapInitialConfig mapConfig) {
        
        position[0] = mapConfig.getStartLat();
        position[1] = mapConfig.getStartLon();
        zoomLevel = mapConfig.getStartZoom();
        rotation = mapConfig.getStartRotation();
        m_GpsAdjust = 0;
        skyHeight = 0;
        currentPanningAngle = 0.0f;
        
        hasChanged = true;
        frameDuration = 1;
        frameTime = System.currentTimeMillis();
    }
    
    void setScreenInfo(ScreenInfo aScreenInfo) {
        iScreenInfo = aScreenInfo;
        setFOV((float)DESIRED_FOVX);
        update();
    }
    
    void zoomIn3D() {
        ZOOM_CORRECTION = 6000;
    }
    
    void zoomOut3D() {
        ZOOM_CORRECTION = 12000;
    }
    
    /**
     * Sets the position of the Camera. Will take effect when
     * transformation matrices are updated in method update()
     *
     * @param lat latitude
     * @param lon longitude
     */
    public void setPosition(long lat, long lon) {
        if(!ignoreOverflows) {
            this.position[0] = lat<Integer.MIN_VALUE?Integer.MIN_VALUE:(lat>Integer.MAX_VALUE?Integer.MAX_VALUE:lat);
            this.position[1] = lon<Integer.MIN_VALUE?Integer.MIN_VALUE:(lon>Integer.MAX_VALUE?Integer.MAX_VALUE:lon);
        } else {
            this.position[0] = lat;
            this.position[1] = lon;
        }
        hasChanged = true;
    }
    
    void internalSetPosition(int worldLat, int worldLon, int screenX, int screenY) {
        
        if (isIn3DMode()) {            
            createMatrix(true);
            recalculateCameraVectors();
            setLookAt(worldLat, worldLon, screenX, screenY);
            hasChanged = true;
            
        } else {
            
            double scale = Utils.MC2SCALE_TO_METER / zoomLevel;
            double scaleInv = 1.0/(scale*iScreenInfo.getDPICorrection());
            
            double sin = Math.sin(rotation_during_rendering);
            double cos = Math.cos(rotation_during_rendering);
    
            /* 
             * Calculate the center lat coordinate, use the
             * 
             * worldLat = viewProjInverse[0][0]*screenX + 
             *            viewProjInverse[0][1]*screenY + 
             *            viewProjInverse[0][3] 
             * to calculate the center lat for the specified cursor coord.
             * 
             * see the getWorldCoordinate(int screenX, int screenY) method
             * */            
            double centerLat = worldLat - cos * scaleInv * screenX + 
                                        sin * scaleInv * screenY + 
                                       (m_GpsAdjust * -sin * scaleInv);
            
            /* Update the coslat before calculating the center lon coord */ 
            coslat = Math.cos(centerLat * 2 * Math.PI / 4294967296.0f);
                    
            /* 
             * Calculate the center lon coordinate, use the
             * 
             * worldLon = viewProjInverse[1][0]*screenX + 
             *            viewProjInverse[1][1]*screenY + 
             *            viewProjInverse[1][3]  
             *            
             * to calculate the center lon for the specified cursor coord.
             * 
             * see the getWorldCoordinate(int screenX, int screenY) method
             * */
            double centerLon = worldLon - ((sin / coslat) * scaleInv * screenX) - 
                                        ((cos / coslat) * scaleInv * screenY) + 
                                        (m_GpsAdjust * (cos / coslat) * scaleInv);
                        
            position[0] = (long)centerLat;
            position[1] = (long)centerLon;
            hasChanged = true;
        }
    }
    
    /**
     * Sets the zoom level, the zoom will be limited between {@link #MIN_ZOOM}
     * and {@link #MAX_ZOOM} 
     * @param scale zoom level
     * <p>
     * Note: {@link #shouldRender()} will return true after this if the old/new
     * zoom absolute difference is bigger than 0.001 
     * </p>
     */
    public void setScale(float scale) {
        //limit the zoom 
        //bad thing happen if this is not limited: 
        //drawing of map grid is blocking,
        //map objects are not drawn anymore
        if (scale > MAX_ZOOM) {
            scale = MAX_ZOOM;
        } else if (scale < MIN_ZOOM){
            scale = MIN_ZOOM;
        }
        if (Math.abs(zoomLevel - scale) > 0.001) {
            hasChanged = true;
        } 
        zoomLevel = scale;
    }
    
    /**
     * Returns the current zoom level
     *
     * @return current zoom level
     */
    public float getZoomLevel() {
        return iZoomLevelDuringRender;
    }
    
    
    
    /**
     * Sets the adjustment value that is used when tracking
     * This prevents the Camera from centering at the GPS-arrow
     * 
     * Note: this method assumes that the gpsAdjust specified are
     * inside the current bounding box for the screen!
     *
     * @param gpsAdjust pixels to offset from screen center
     */
    public void setGPSAdjust(int gpsAdjust){
        m_GpsAdjust = gpsAdjust;        
    }
    
    /**
     * Returns the current camera tracking offset
     *
     * @return current camera tracking offset
     */
    public int getGPSAdjust(){
        return m_GpsAdjust;
    }
    
    /**
     * Sets the frame time value
     */
    void updateFrameTimer() {
        frameDuration = System.currentTimeMillis()-frameTime;
        frameTime+=frameDuration;
        if(frameDuration>FRAMETIME_THRESHOLD) {
            frameDuration = FRAMETIME_THRESHOLD;
        }
    }
    
    /**
     * Translates the camera. Translation will take effect when
     * transformation matrices are updated in method update()
     *
     * @param dx - x axis translation in pixels
     * @param dy - y axis translation in pixels
     */
    void translate(int dx, int dy) {
        if (!isIn3DMode()) {
            long[] p = getWorldCoordinateInternal(dx, dy);
            setPosition(p[0], p[1]);
        } else {
            dy += m_GpsAdjust;
            long[] p = getWorldCoordinateInternal(dx, dy);
            setPosition(p[0], p[1]);
        }
        
        hasChanged = true;
    }
    
    /**
     * Sets camera rotation
     *
     * @param angle angle - radians
     */
    public void setRotation(float angle) {
        rotation = angle;
        hasChanged = true;
    }
    
    /**
     * Zooms camera to a point, specified by it's world
     * and screen position
     *
     * @param zoomOut true if zooming out
     * @param worldX world x for point
     * @param worldY world y for point
     * @param screenX screen x for point
     * @param screenY screen y for point
     */
    void scaleTo(boolean zoomOut, long worldX, long worldY, int screenX, int screenY) {
        float factor = 1 + frameDuration*SCALING_SPEED;
        if (!zoomOut) {
            factor = 1.0f / factor;
        }
        
        //change the zoomLevel
        setScale(zoomLevel*factor);
        
        if (isIn3DMode()) {
            createMatrix(true);
            recalculateCameraVectors();
            setLookAt(worldX, worldY, screenX, screenY);
        } else { 
            double scale = Utils.MC2SCALE_TO_METER / zoomLevel;
            double scaleInv = 1.0/(scale*iScreenInfo.getDPICorrection());
            
            double sin = Math.sin(rotation_during_rendering);
            double cos = Math.cos(rotation_during_rendering);

            /* 
             * Calculate the center lat coordinate, use the
             * 
             * worldLat = viewProjInverse[0][0]*screenX + 
             *            viewProjInverse[0][1]*screenY + 
             *            viewProjInverse[0][3] 
             * to calculate the center lat for the specified cursor coord.
             * 
             * see the getWorldCoordinate(int screenX, int screenY) method
             * */            
            double centerLat = worldX - cos * scaleInv * screenX + 
                                        sin * scaleInv * screenY + 
                                       (m_GpsAdjust * -sin * scaleInv);
            
            /* Update the coslat before calculating the center lon coord */ 
            coslat = Math.cos(centerLat * 2 * Math.PI / 4294967296.0f);
                    
            /* 
             * Calculate the center lon coordinate, use the
             * 
             * worldLon = viewProjInverse[1][0]*screenX + 
             *            viewProjInverse[1][1]*screenY + 
             *            viewProjInverse[1][3]  
             *            
             * to calculate the center lon for the specified cursor coord.
             * 
             * see the getWorldCoordinate(int screenX, int screenY) method
             * */
            double centerLon = worldY - ((sin / coslat) * scaleInv * screenX) - 
                                        ((cos / coslat) * scaleInv * screenY) + 
                                        (m_GpsAdjust * (cos / coslat) * scaleInv);
                        
            position[0] = (long)centerLat;
            position[1] = (long)centerLon;                  
        }
        
        hasChanged = true;
    }
    
    public boolean isMaxZoomedOut() {
        return (zoomLevel == MAX_ZOOM);
    }
    
    /**
     * Code for managing the rendering
     *
     **/
    
    /**
     * Called to force camera update
     */
    public void mapChangeNotify() {
        hasChanged = true;
    }
    
    /**
     * Returns true if the camera has changed since last check,
     * in which case the caller should re-render
     *
     * @return true if the camera has changed
     */
    private boolean shouldRender() {
        if(hasChanged) {
            hasChanged = false;  //reset
            return true;
        }
        return false;
    }
    
    /**
     * Get the mc2 latitude of the camera
     *
     * @return the latitude
     */
    public long getLatitude() {
        return position[0];
    }
    
    /**
     * Get the mc2 longitude of the camera
     *
     * @return the longitude
     */
    public long getLongitude() {
        return position[1];
    }
    
    private void createMatrix(boolean newZoom) {
        double scale;
        if(newZoom) {
            scale = Utils.MC2SCALE_TO_METER / zoomLevel;
        } else {
            scale = Utils.MC2SCALE_TO_METER / iZoomLevelDuringRender;
        }
        
        if(!isIn3DMode()) {
            double sin = Math.sin(rotation_during_rendering);
            double cos = Math.cos(rotation_during_rendering);
            
            long dx = -position_during_rendering[0];//+UIUtil.get().getHalfWidth();
            long dy = -position_during_rendering[1];//+UIUtil.get().getHalfHeight();
            final float dpiCorrection = iScreenInfo.getDPICorrection();
            
            double scaleInv = 1.0/(scale*dpiCorrection);
            
            viewProj[0][0] = (float)(cos * scale * dpiCorrection);
            viewProj[0][1] = (float)(sin * coslat * scale * dpiCorrection);
            viewProj[0][2] = 0;
            viewProj[0][3] = (float)((dx * cos + coslat * dy * sin) * scale * dpiCorrection);
            
            viewProj[1][0] = (float)(-sin * scale * dpiCorrection);
            viewProj[1][1] = (float)(cos * coslat * scale * dpiCorrection);
            viewProj[1][2] = 0;
            viewProj[1][3] = (float)(((dx * -sin + coslat * dy * cos) * scale * dpiCorrection) + m_GpsAdjust);
            
            viewProj[2][0] = 0;
            viewProj[2][1] = 0;
            viewProj[2][2] = 1;
            viewProj[2][3] = 0;
            
            viewProj[3][0] = 0;
            viewProj[3][1] = 0;
            viewProj[3][2] = 0;
            viewProj[3][3] = 1;
            
            viewProjInverse[0][0] = cos * scaleInv;
            viewProjInverse[0][1] = -sin * scaleInv;
            viewProjInverse[0][2] = 0;
            viewProjInverse[0][3] = -dx - (m_GpsAdjust * -sin * scaleInv);
            
            viewProjInverse[1][0] = (sin / coslat) * scaleInv;
            viewProjInverse[1][1] = (cos / coslat) * scaleInv;
            viewProjInverse[1][2] = 0;
            viewProjInverse[1][3] = -dy - (m_GpsAdjust * (cos / coslat) * scaleInv);
            
            viewProjInverse[2][0] = 0;
            viewProjInverse[2][1] = 0;
            viewProjInverse[2][2] = 1;
            viewProjInverse[2][3] = 0;
            
            viewProjInverse[3][0] = 0;
            viewProjInverse[3][1] = 0;
            viewProjInverse[3][2] = 0;
            viewProjInverse[3][3] = 1;
        } else {
           
            double dx = -iCameraPosition3D[0];//+UIUtil.get().getHalfWidth();
            double dy = -iCameraPosition3D[1];//+UIUtil.get().getHalfHeight();
            double dz = -iCameraPosition3D[2];//+UIUtil.get().getHalfHeight();
            
            double sinr = Math.sin(rotation_during_rendering-Math.PI);
            double cosr = Math.cos(rotation_during_rendering-Math.PI);
            
            double sinp = Math.sin(currentPanningAngle);
            double cosp = Math.cos(currentPanningAngle);
            
            double xScale = (1.0/Math.tan(FOV_X/2.0f))*iScreenInfo.getHalfScreenWidth();
            double yScale = (1.0/Math.tan(FOV_Y/2.0f))*iScreenInfo.getHalfScreenHeight();
            
            viewProj[0][0] = (float)(cosr*xScale);
            viewProj[0][1] = (float)(sinr*coslat*xScale);
            viewProj[0][2] = 0;
            viewProj[0][3] = (float)((cosr*dx + sinr*dy*coslat)*xScale);
            
            viewProj[1][0] = (float)(-sinr*cosp*yScale);
            viewProj[1][1] = (float)(cosp*cosr*coslat*yScale);
            viewProj[1][2] = (float)(sinp*yScale);
            final double u = (-sinr*dx+cosr*dy*coslat);
            viewProj[1][3] = (float)((cosp*u + sinp*dz)*yScale);
            
            final double e2 = sinr*sinp;
            viewProj[2][0] = (float)e2;
            final double d = -sinp*cosr*coslat;
            viewProj[2][1] = (float)d;
            viewProj[2][2] = (float)cosp;
            final double e = -sinp*u + cosp*dz;
            viewProj[2][3] = (float)e;
            
            viewProj[3][0] = (float)e2;
            viewProj[3][1] = (float)d;
            viewProj[3][2] = (float)cosp;
            viewProj[3][3] = (float)e;
        }
    }
    
    /**
     * Set 3D mode on or off for the camera
     *
     * @param enter3D 3D mode on/off
     */
    public void set3DMode(boolean enter3D) {
        if(enter3D) {
            setFOV((float)DESIRED_FOVX);
            setPan((float)DESIRED_PAN_ANGLE);
        } else {
            setPan(0);
        }
    }
    
    /**
     * Returns current view projection matrix, with or without perspective/pan
     *
     * @return View projection matrix
     */
    float[][] getTransform() {
        return viewProj;
    }
  
    /**
     * Returns the bounding box for the camera
     * Used for culling, clipping and selecting which tiles to draw
     *
     * @return bounding box {(int)minX, (int)maxX, (int)minY, (int)maxY}
     */
    private int[] cbb = new int[4];
    private long[] x = new long[4];
    private long[] y = new long[4];
    
    /**
     * Returns the bounding box for the Camera 
     *
     * @return camera bounding box - [min x, max x, min y, max y]
     */
     int[] getCameraBoundingBoxInternal() {
        
        //Time to fetch camera
        int camXMin, camXMax, camYMax, camYMin;
        if (isIn3DMode()) {
            //Tweaks to fill the whole screen. Might be more tiles than you bargained for...
            camYMin = -iScreenInfo.getHalfScreenHeight()+skyHeight;//+60;//-150;
            camYMax = iScreenInfo.getHalfScreenHeight();
            camXMax = iScreenInfo.getHalfScreenWidth();//+200;
            camXMin = -camXMax;//-200;
        } else {
            camXMax =  iScreenInfo.getHalfScreenWidth();
            camYMax =  iScreenInfo.getHalfScreenHeight();
            camYMin = -camYMax;
            camXMin = -camXMax;
        }
        
        long[] coord;        
        coord = getWorldCoordinateInternal(camXMin, camYMin);
        x[0] = coord[0]; y[0] = coord[1];
        coord = getWorldCoordinateInternal(camXMin, camYMax);
        x[1] = coord[0]; y[1] = coord[1];
        coord = getWorldCoordinateInternal(camXMax, camYMin);
        x[3] = coord[0]; y[3] = coord[1];
        coord = getWorldCoordinateInternal(camXMax, camYMax);
        x[2] = coord[0]; y[2] = coord[1];
        
        long minX = x[0];
        long minY = y[0];
        long maxX = x[0];
        long maxY = y[0];
        
        for(int i=1; i<4; i++) {
            long X = x[i], Y = y[i];
            if(X<minX) {minX = X;}
            if(X>maxX) {maxX = X;}
            if(Y<minY) {minY = Y;}
            if(Y>maxY) {maxY = Y;}
        }
        
        cbb[0] = minX<Integer.MIN_VALUE?Integer.MIN_VALUE:(int)minX;
        cbb[1] = maxX>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)maxX;
        cbb[2] = minY<Integer.MIN_VALUE?Integer.MIN_VALUE:(int)minY;
        cbb[3] = maxY>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)maxY;
        
        return cbb;
    }
     
    /**
     * Return a new int-array of the current used MC2 camera bounding box.
     * <p>
     * For external use only, inside the map module use 
     * getCameraBoundingBoxInternal()!  
     * 
     * @return
     */
    public int[] getCameraBoundingBox() {
        int []cambb = new int[4];
        System.arraycopy(cbb, 0, cambb, 0, cbb.length);        
        return cambb;
    }
    
    /**
     * Returns the world coordinate for a specified screen coordinate
     *
     * @param screenX Screen X coordinate
     * @param screenY Screen Y coordinate
     * @return World coordinates as a Long array
     */
    private long[] worldCoord = new long[2];
    
    /**
     * Returns the world coordinate for a specified screen coordinate. The 
     * screenX and screenY parameter must be adjust with halfScreenWidth 
     * and halfScreenHeight.  
     * 
     * @param screenX screen x pos
     * @param screenY screen y pos
     * @return world coordinate - long[2]
     */
    long[] getWorldCoordinateInternal(int screenX, int screenY) {
       
        if(!isIn3DMode()) {
            double worldX   = viewProjInverse[0][0]*screenX + viewProjInverse[0][1]*screenY + viewProjInverse[0][3];
            double worldY   = viewProjInverse[1][0]*screenX + viewProjInverse[1][1]*screenY + viewProjInverse[1][3];
            
            double Z        = viewProjInverse[3][0]*screenX + viewProjInverse[3][1]*screenY + viewProjInverse[3][3];
            
            double x = (worldX/Z);
            double y = (worldY/Z);

            worldCoord[0] = (long)x;
            worldCoord[1] = (long)y;
            return worldCoord;
        }
        
        double[] ray = getRay(screenX, screenY);
        
        double t = -iCameraPosition3D[2] / ray[2];
        long worldX = (long)(iCameraPosition3D[0] + t * ray[0]);
        long worldY = (long)((iCameraPosition3D[1] + (t * ray[1]) / coslat));
        
        worldCoord[0] = worldX;
        worldCoord[1] = worldY;
        return worldCoord;
        
    }
    
    
    private int[] screenCoord = new int[2];

    /**
     * Returns the screen coordinate for a specified world coordinate
     *
     * @param worldX World X coordinate in MC2
     * @param worldY World Y coordinate in MC2
     * @return Screen coordinates as an int array. Note that the screen coordinates
     *         that are returned has an offset with halfscreen width/height.  
     */
    int[] getScreenCoordinateInternal(long worldX, long worldY) {
       
        double screenX   = viewProj[0][0]*worldX + viewProj[0][1]*worldY + viewProj[0][3];
        double screenY   = viewProj[1][0]*worldX + viewProj[1][1]*worldY + viewProj[1][3];
        double Z         = viewProj[3][0]*worldX + viewProj[3][1]*worldY + viewProj[3][3];
        
        screenCoord[0] = (int)(screenX/Z);
        screenCoord[1] = (int)(screenY/Z);
        
        return screenCoord;
    }
    
    /**
     * Return the screen coordinates for the worldX and worldY specified by the 
     * parameter. Note that the screen coordinates has an offset with halfscreen
     * width / height. 
     * 
     * @param worldX world latitude in MC2
     * @param worldY world latitude in MC2
     * @return the screen coordinates [screenX, screenY] with a offset of halfscreen
     *         width / height. 
     */
    int[] getNewScreenCoordinateInternal(long worldX, long worldY) {
       
        double screenX   = viewProj[0][0]*worldX + viewProj[0][1]*worldY + viewProj[0][3];
        double screenY   = viewProj[1][0]*worldX + viewProj[1][1]*worldY + viewProj[1][3];
        double Z         = viewProj[3][0]*worldX + viewProj[3][1]*worldY + viewProj[3][3];
        
        return new int[]{ (int)(screenX/Z), (int)(screenY/Z) };
    }
    
    /**
     * Returns the current camera rotation
     *
     * @return rotation angle - radians
     */
    public float getRotation() {
        return rotation;
    }
    
    /**
     * Updates the coslat value using the current camera coordinate
     */
    private void updateCoslat() {
        coslat = Math.cos(position[0] * 2 * Math.PI / 4294967296.0f);
    }
    
    /**
     * Check if the camera is in 3D mode
     *
     * @return true if the camera is in 3D mode, false otherwise 
     */
    public boolean isIn3DMode() {
        return isIn3DMode;
    }
    
    /**
     * Sets the panning angle
     *
     * @param panAngle pan angle - radians
     */
    private void setPan(float panAngle) {
        this.pan_angle = panAngle;
        currentPanningAngle = pan_angle;
        hasChanged = true;
        
    }
    
    /**
     * Sets the FOV angle - currently not in use
     *
     * @param FOV  FOV angle - radians
     */
    private void setFOV(float FOV) {
        FOV_X = FOV;
        FOV_Y = 2*Utils.atan((float)Math.tan(FOV/2.0f)/iScreenInfo.getAspectRatio());
        hasChanged = true;
    }
    
    /**
     * Sets height of ''sky''
     *
     * @param skyHeight height of 'sky' - pixels
     */
    public void setSkyHeight(int skyHeight) {
        this.skyHeight = skyHeight;
    }
    
    /**
     * Returns height of ''sky''
     *
     * @return height of ''sky'' - pixels
     */
    public int getSkyHeight() {
        return skyHeight;
    }
    
    /**
     * Returns current coslat value
     *
     * @return current coslat value
     */
    double getCoslat() {
        return coslat;
    }
    
    
    /*** NEW REAL 3D CAMERA METHODS ***/
    
    private void setLookAt(long worldX, long worldY, int screenX, int screenY) {
        double[] move = getRay(screenX, screenY);
        
        iCameraPosition3D[0] = (long)(worldX    -(move[0] * zoomLevel * ZOOM_CORRECTION));
        iCameraPosition3D[1] = (long)(worldY    -((move[1] * zoomLevel * ZOOM_CORRECTION)/coslat));
        iCameraPosition3D[2] = (long)(          -(move[2] * zoomLevel * ZOOM_CORRECTION));
    }
    
    private void recalculateCameraVectors() {
        double panCos = Math.cos(currentPanningAngle), panSin = Math.sin(currentPanningAngle), rotCos = Math.cos(rotation), rotSin = Math.sin(rotation);
        
        forward[0] = -rotSin*(-panSin);
        forward[1] = rotCos*(-panSin);
        forward[2] = -panCos;
        
        up[0]      = -rotSin*(panCos);
        up[1]      = rotCos*(panCos);
        up[2]      = -panSin;
        
        right[0]   = rotCos;
        right[1]   = rotSin;
        right[2]   = 0;
    }
    
    private long[] VRP = new long[3];
    private double[] ray = new double[3];
    
    /**
     * Returns a vector from the camera to a point on the screen
     *
     * @param screenX screen x pos
     * @param screenY screen y pos
     * @return a vector from the camera to a point on the screen
     */
    private double[] getRay(int screenX, int screenY) {
        double x = screenX;
        double y = screenY;
        
        double focal = (iScreenInfo.getHalfScreenWidth() / Math.tan(FOV_X / 2.0f));
        VRP[0] = iCameraPosition3D[0] + (long)(forward[0]*focal);
        VRP[1] = iCameraPosition3D[1] + (long)(forward[1]*focal);
        VRP[2] = iCameraPosition3D[2] + (long)(forward[2]*focal);
        
        ray[0] = VRP[0] + x*right[0] + y*up[0] - iCameraPosition3D[0];
        ray[1] = VRP[1] + x*right[1] + y*up[1] - iCameraPosition3D[1];
        ray[2] = VRP[2] + x*right[2] + y*up[2] - iCameraPosition3D[2];
        
        norm(ray);
        return ray;
    }
    
    
    /**
     * Normalizes an array 
     *
     * @param v Double array to be normalized
     */
    public static void norm(double[] v){
        double lenInv = 1/Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0]*=lenInv;
        v[1]*=lenInv;
        v[2]*=lenInv;
    }
    
    
    /**
     * Updates coslat,camera vectors and transformation matrices
     *
     * @return true if program should render
     */
    public boolean update() {
        isIn3DMode = currentPanningAngle!=0;
        position_during_rendering[0] = position[0];
        position_during_rendering[1] = position[1];
        rotation_during_rendering = rotation;
        iZoomLevelDuringRender = zoomLevel;
        
        updateCoslat();
        recalculateCameraVectors();
        if(isIn3DMode()) {
            setLookAt(position_during_rendering[0], position_during_rendering[1],0,m_GpsAdjust);
        }
        
        createMatrix(false);
        boolean changed = false;
        long minValue = Utils.MIN_INTEGER; //-1700000000;
        
        int[] cbb = getCameraBoundingBoxInternal();
        
        if (cbb[0] < SOUTH_MAP_LIMIT_MC2) {
            long diff = SOUTH_MAP_LIMIT_MC2 - cbb[0];
            position[0] += diff;
            changed = true;
            iCameraAtSouthLimit = true;
        } else if (cbb[1] > NORTH_MAP_LIMIT_MC2) {
            long diff = cbb[1] - NORTH_MAP_LIMIT_MC2;
            position[0] -= diff;
            changed = true;
            iCameraAtNorthLimit = true;
        } else { 
            if (cbb[0] > SOUTH_MAP_LIMIT_MC2) {
                int diff = cbb[0] - SOUTH_MAP_LIMIT_MC2;
                if (diff > PRECISION_ACCEPTANCE) {
                    iCameraAtSouthLimit = false;
                } else {
                    position[0] -= diff;
                }
            }
            if (cbb[1] < NORTH_MAP_LIMIT_MC2) {
                int diff = NORTH_MAP_LIMIT_MC2 - cbb[1];
                if (diff > PRECISION_ACCEPTANCE) {
                    iCameraAtNorthLimit = false;
                } else {
                    position[0] += diff;
                }
            }
        }
        
        if (!ignoreOverflows) {
            if (position[1] > Integer.MAX_VALUE-1) {
                position[1] = minValue+2;
                changed = true;
            } else if (position[1] < minValue+1) {
                position[1] = Integer.MAX_VALUE-2;
                changed = true;
            }
        }
        
        if (changed) {
            position_during_rendering[0] = position[0];
            position_during_rendering[1] = position[1];
            updateCoslat();
            recalculateCameraVectors();
            if (isIn3DMode()) {
                setLookAt(position_during_rendering[0], position_during_rendering[1],0,m_GpsAdjust);
            }
            createMatrix(false);
        }
        
        return shouldRender();
    }
    
    public void clone(Camera c, long offset) {
        ScreenInfo si = new ScreenInfo(iScreenInfo.getScreenWidth(), iScreenInfo.getScreenHeight(), true);
        c.setScreenInfo(si);
        c.setPosition(position_during_rendering[0], position_during_rendering[1]+offset);        
        c.setRotation(rotation_during_rendering);
        c.setScale(iZoomLevelDuringRender);
        c.setPan(currentPanningAngle);
        c.setFOV(FOV_X);
        c.setGPSAdjust(m_GpsAdjust);
        c.setSkyHeight(skyHeight);
        c.update();
    }
    
    public void setIgnoreOverflows() {
        ignoreOverflows = true;
    }
    
    private boolean isMoving;
    
    void setCameraIsMoving(boolean aMoving) {
        isMoving = aMoving;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    /**
     * Check if camera has reached the north limit
     * 
     * @return true if north limit reached, false otherwise
     */    
    boolean atNorthLimit() {
        return iCameraAtNorthLimit;
    }
    
    /**
     * Check if camera has reached the south limit
     * 
     * @return true if south limit reached, false otherwise
     */    
    boolean atSouthLimit() {
        return iCameraAtSouthLimit;
    }

    // -----------------------------------------------------------------------------
    // MapCameraInterface methods. 
    //XXX: Please read the documentation for the methods below before using them!
    
    /* 
    * General information: The methods below are only to be used when converting
    * screen and world coordinates externally. They should NOT be used internally 
    * inside vector map module. The reason for that are the all coordinates have a 
    * offset with half screen width and half screen height. This because it's easier to 
    * rotate and move the map when the center point are located at (0,0). This can
    * of course be changed but it needs done. Until that we need to have two similar
    * converting methods. One used internally and one for external use.
    */
    
    /* 2^32/360 */
    private static final double DEC_DEG_TO_MC2 = 11930464.7111;
    
    private long[] worldCoord2 = new long[2];
//    private double[] wgs84worldCoord = new double[2];
    private int[]  screenCoord2 = new int[2];
    
    /**
     * For external use only, for internal calculations inside the vectormap 
     * module use getScreenCoordinateInternal<p>
     * 
     * Return a screen coordinate from a MC2 world coordinate. 
     * 
     * @param worldLat
     * @param worldLon 
     * @return [screenX, screenY] in pixels
     */
    public int[] getScreenCoordinate(long worldLat, long worldLon) {        
        screenCoord2 = getScreenCoordinateInternal(worldLat, worldLon);        
        int[]  sc = new int[2];
        sc[0] = screenCoord2[0] + iScreenInfo.getHalfScreenWidth();
        sc[1] = screenCoord2[1] + iScreenInfo.getHalfScreenHeight();        
        return sc;
    }

    /**
     * For external use only, for internal calculations inside the vectormap 
     * module use getScreenCoordinateInternal<p>
     * 
     * Return a screen coordinate from a wgs 84 world coordinate
     * 
     * @param worldLat in wgs 84
     * @param worldLon in wgs 84
     * @return [screenX, screenY] in pixels
     */
    public int[] getScreenCoordinateWGS84(double worldLat, double worldLon) {        
        int worldMc2Lat = (int)(worldLat * DEC_DEG_TO_MC2);
        int worldMc2Lon = (int)(worldLon * DEC_DEG_TO_MC2);
        
        return getScreenCoordinate(worldMc2Lat, worldMc2Lon);        
    }

    /**
     * For external use only, for internal calculations inside the vectormap 
     * module use getWorldCoordinateInternal<p>
     * 
     * Return a MC2 coordinate from a screen coordinate. 
     * 
     * @param screenX in pixel
     * @param screenY in pixel
     * @return [lat, lon] in MC2
     */
    public long[] getWorldCoordinate(int screenX, int screenY) {        
        screenX -= iScreenInfo.getHalfScreenWidth();
        screenY -= iScreenInfo.getHalfScreenHeight();
        
        return getWorldCoordinateInternal(screenX, screenY);
    }

    /**
     * For external use only, for internal calculations inside the vectormap 
     * module use getWorldCoordinateInternal<p>
     * 
     * Return the wgs 84 world coordinate from a screen coordinate. 
     * 
     * @param screenX in pixel
     * @param screenY in pixel
     * @return [lat, lon] in wgs 84
     */
    public double[] getWorldCoordinateWGS84(int screenX, int screenY) {        
        worldCoord2 = getWorldCoordinate(screenX, screenY);
        double[] wgs84worldCoord = new double[2];
        wgs84worldCoord[0] = (worldCoord2[0] / DEC_DEG_TO_MC2);
        wgs84worldCoord[1] = (worldCoord2[1] / DEC_DEG_TO_MC2);        
        return wgs84worldCoord;
    }   
}
