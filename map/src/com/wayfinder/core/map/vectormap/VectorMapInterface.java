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
package com.wayfinder.core.map.vectormap;

import com.wayfinder.core.map.MapDownloadListener;
import com.wayfinder.core.map.MapErrorListener;
import com.wayfinder.core.map.MapKeyInterface;
import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.MapObjectImage;
import com.wayfinder.core.map.MapObjectListener;
import com.wayfinder.core.map.MapStartupListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.pal.graphics.WFGraphicsFactory;

/**
 * The main interface for interact with the vector map. 
 * 
 * TODO: document the model "active position" and how the map determines what
 * the active position is. What happens if gps following is on and you call
 * setCenter()? Will it snap back?
 * 
 */
public interface VectorMapInterface {
   
    /**
     * Set a startup listener to the map. The listener will be called when
     * the map is fully initialized and ready to be shown on the screen. 
     * 
     * @param aStartupListener 
     */
    public void setStartupListener(MapStartupListener aStartupListener);
    
    /**
     * Set a map error listener to the map, the listener will be called if a 
     * critical error has occur that the map can't handle and therefore need to 
     * shut down. 
     * 
     * @param aErrorListener
     */
    public void setMapErrorListener(MapErrorListener aErrorListener);
    
    /**
     * Set a map object listener to the map, the listener will be called when 
     * the active point in the map is over a MapObject or a POI.
     * 
     * @param aMapObjectListener
     */
    public void setMapObjectListener(MapObjectListener aMapObjectListener);
    
    /**
     * Set a map download listener to the map, the listener will be called when 
     * the map has 
     * 
     * @param downloadListener
     */
    public void setMapDownloadListener(MapDownloadListener downloadListener);
    
    /**
     * Set a new map drawer interface to the map. The interface will be called
     * when a update of the map has been triggered and the map needs to re-draw
     * itself. 
     * 
     * @param aDrawerInterface
     */
    public void setMapDrawerInterface(MapDrawerInterface aDrawerInterface);
    
    /**
     * Return the detailed config interface used for advanced configuration of 
     * the map. 
     *  
     * @return the MapDetailedConfigInterface interface. 
     * @see MapDetailedConfigInterface
     */
    public MapDetailedConfigInterface getMapDetailedConfigInterface();
    
    /**
     * Return the map key interface used to control key, pointer and trackball
     * events in the map. 
     * 
     * @return the MapKeyInterface
     * @see MapKeyInterface
     */
    public MapKeyInterface getMapKeyInterface();
    

    // -----------------------------------------------------------------------
    /**
     * Request a update of the map. If the map are idle a call to this method 
     * will trigger a instant map update. If the map are already updating itself 
     * or the map are locked a new update will be put in a queue. 
     * <p>
     * 
     * There can only be one request of map update in the queue, so if multiple 
     * calls to requestMapUpdate() are made during a map update the result will 
     * only be that only one new update will be made.
     * <p>
     * 
     * When the map has update itself a callback to the {@link MapDrawerInterface} will
     * be made.  
     */
    public void requestMapUpdate();
        
    /**
     * Call to this method will prepare the map to be used by starting the
     * internal map threads, initialize the map cache etc. This method can
     * be used prepare the map to be shown on the screen. 
     * <p>
     * Multiple calls to this method will be ignored. <br>
     * 
     * @param aFactory platform specific implementation of the graphics used in the map. 
     * @param aDrawerInterface the interface used to control how the map should be rendered. 
     * @param aInitialConfig contains information that need to be set before the map can be started.
     */
    public void initializeMap(WFGraphicsFactory aFactory, MapDrawerInterface aDrawerInterface,
            MapInitialConfig aInitialConfig);
    
    /**
     * Should be called when we want to start the map component. Note that a UIN is required
     * in order to get the correct TileMapFormatDesc. If the user is not recognized by the
     * server it will respond with a default configuration.
     * 
     * The map component will do the final initialization.
     * 
     * @param aIDString
     */
    public void startMapComponent(String aIDString);
    
    /**
     * Close the map component. After call to this method the 
     * startMapComponent(String aIDString) method must be called to show the
     * map on the screen again. 
     */
    public void closeMapComponent();
    
    /**
     * Call to this method will release as many object as possible from the memory. 
     * After call to this method the setVisible(true) method must be called to
     * show the map again. 
     */
    public void purgeData();
    
    /**
     * Return true if the map is started and ready to be shown on the screen. 
     * 
     * @return true if the map is started, false if not. 
     */
    public boolean isMapStarted();
    
    
    // -----------------------------------------------------------------------
    /**
     * Set the draw area in the map
     * 
     * @param x the screen x coordinate of the left edge of the map
     * @param y the screen y coordinate of the top edge of the map
     * @param width the width of the map in pixels
     * @param height the height of the map in pixels
     */
    public void setDrawArea(int x, int y, int width, int height);
    
    /**
     * Set the visibility state of the map. If the map is set to be not visible
     * all calls to the map will be ignored. 
     * 
     * @param aIsVisible true if the map should be visible, false if not. 
     */
    public void setVisible(boolean aIsVisible);
    
    /**
     * Method that controls the colors used in the map. 
     * 
     * @param aUseNightColors true to use night colors, false if not. 
     */
    public void setNightMode(boolean aUseNightColors);
    
    /**
     * Return true if the map use night colors. 
     * 
     * @return true if the map use night colors.
     */
    public boolean isNightMode();
    
    /**
     * Sets the language in the map component. 
     * <p>
     * English will be used if now matching language was found. 
     *
     * @param aLanguage the language string in ISO-639-3 standard
     */
    public void setLanguageAsISO639_3(String aLanguage);
    
    /**
     * Set the center coordinate in the map to the mc2 coordinates specified
     * by the parameters. 
     * 
     * @param aLat latitude coordinate in MC2
     * @param aLon longitude coordinate in MC2
     */
    public void setCenter(int aLat, int aLon);
    

    /**
     * Set a new scale (zoom level) in the map. 
     * 
     * @param aScale the scale in meter/pixel
     */
    public void setScale(float aScale);
    
    /**
     * Return the current used scale in the map. 
     * 
     * @return the scale (meter/pixel)
     */
    public float getScale();
    


    /**
     * Return the active position from the map.
     * 
     * @return the active position in MC2
     * @see VectorMapInterface#setActiveScreenPoint(int, int)
     */
    public Position getActivePosition();

    /**
     * <p>Returns the name associated with the position returned from
     * {@link VectorMapInterface#getActivePosition()}.</p>
     * 
     * <p>Several map features can overlap. We search names at the position
     * in the following order, and return the name of the first one found:
     * <ol><li>{@link MapObject}</li>
     *     <li>POIs</li>
     *     <li>Lines (streets)</li>
     *     <li>Polygons (bua, sea, country polygons etc.)</li>
     * </ol>
     * 
     * <p>Note that this is not a proximity search or fuzzy reverse geo coding
     * function. If the active position is not over a MapObject, POI or street,
     * you will get the bua name. Not the name of the closest street/POI.</p> 
     * 
     * @return the name found or "" if there is no name available. E.g. when
     * the active coordinate is out in the sea or the map has not yet loaded
     * data for the position.
     */
    public String getActivePositionName();
    
    /**
     * Method the control the map mode. Sets to true to use the 3D map,
     * false to use 2D map. 
     * <p>
     * If the map is in 3D mode, all key actions will be ignored and the map
     * will be set to follow the GPS position.  
     * 
     * @param aUse3DMode true to use 3D mode, false to use 2D mode. 
     */
    public void set3DMode(boolean aUse3DMode);
    
    /**
     * Return true if the map is in 3D mode. 
     * 
     * @return true if the map is in 3D mode, false if not. 
     */
    public boolean isIn3DMode();
    

    /**
     * Add a map object to the map
     * <p>
     * Note that you have to add a separate image to be used when
     * drawing the map object on the screen.
     * 
     * @param aMapObject the map object to be drawn
     * @param aMapObjectImage the image that should drawn for the MapObject
     * @return false if no image for the MapObject can be found.
     *  
     */
    public boolean addMapObject(MapObject aMapObject, MapObjectImage aMapObjectImage);
    
    /**
     * Removes the first occurrence of the argument from the map.
     * MapObject will be compared using equals method. 
     *  
     * The remove will be done asynchronous by an internal thread
     *  
     * @param aMapObject the map object to remove.
     */
    public void removeMapObject(MapObject aMapObject);
    
    /**
     * Direct selection of a map object, this will be brought to front   
     * 
     * Note: call this with null for deselection 
     * @param selectedMapObject
     */
    public void setSelectedMapObject(MapObject selectedMapObject);
    
    /**
     * Remove all added map objects from the map. 
     * 
     */
    public void removeAllMapObjects();
    

    /**
     * Set the GPS position and angle in the map. 
     * 
     * @param gpsLat GPS latitude in MC2 coordinates
     * @param gpsLon GPS longitude in MC2 coordinates
     * @param angle the angle in degree
     */
    public void setGpsPosition(int gpsLat, int gpsLon, float angle);
    

    /**
     * Set the rotation in the map. 
     * 
     * @param angle the rotation in degree. 
     */
    public void setRotation(float angle);
    
    /**
     * Return the rotation in degrees. <p>
     * The rotation goes clockwise from 0 to 360 degrees where 0 is north.
     * 
     * @return the rotation in degrees. 
     */
    public float getRotation();
    

    /**
     * Set the map in follow GPS position mode (true/false). If the argument
     * is true the map will follow the position sent via the setGpsPosition method. 
     * 
     * @param aShouldTrack true if the map should follow the gps position, 
     * false if not. 
     */
    public void setFollowGpsPosition(boolean aShouldTrack);
    
    /**
     * Return true if the map is following the GPS position. 
     * 
     * @return true if the map is following the GPS position, false if not. 
     */
    public boolean isFollowGpsPosition();

    
    /**
     * 
     * Zooms to display a world box with the corner coordinates specified by
     * the parameters.
     * 
     * <pre>
     * cornerLat1/cornerLon1
     *           ¤------------------¤
     *           |                  |
     *           |                  |
     *           |                  |
     *           |                  |
     *           |                  |
     *           ¤------------------¤        
     *                     cornerLat2/cornerLon2
     * </pre>  
     *   
     * 
     * @param cornerLat1 in MC2 coordinates
     * @param cornerLon1 in MC2 coordinates
     * @param cornerLat2 in MC2 coordinates
     * @param cornerLon2 in MC2 coordinates
     */
    public void setWorldBox(int cornerLat1, int cornerLon1, int cornerLat2, int cornerLon2);
    
    /**
     * Return true if there exist map content to draw. This method can be used
     * to detect if the screen are blank or not. This method assumes that the 
     * map layer are available, if not the method will always return false. 
     * <p> 
     * 
     * @return true if there exist map content to draw, false if not. 
     */
    public boolean hasEnoughMapPaintContent();
    
    /**
     * Return max(screenWidth, screenHeight) in meters from the current used
     * screen bounding box and zoom level.
     * 
     * @return max(screenWidth, screenHeight) in meters. 
     */
    public long getSearchRadiusMeters();

    // -----------------------------------------------------------------------
    // screen coordinate related stuff

    /**
     * Set the point on the screen that should be used as the active map point. 
     * The point specified by the parameters will be used when zooming, tracking,
     * to check if POI:s and/or MapObjects are selected etc. 
     * <p>
     * The active screen point are set by default to the center of the screen.
     * <p>
     * Note that when the application is set to follow the GPS position, only
     * screenY will be used.  
     * 
     * @param screenX screen coordinates in pixels
     * @param screenY screen coordinates in pixels
     * @return true if the new screen coordinates is used, 
     * false if they are outside the screen area.  
     */
    public boolean setActiveScreenPoint(int screenX, int screenY);

}
