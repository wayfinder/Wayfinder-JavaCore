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

import java.util.Vector;

import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.MapObjectListener;
import com.wayfinder.core.map.vectormap.internal.WFTileMapHolder;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * Class used to check if any feature in the maps has been selected, ether by 
 * the active point in the map or if a point in the map has been "pressed". 
 * 
 * 
 */
public class MapOverlayManager {

    private static final Logger LOG = LogFactory
        .getLoggerForClass(MapOverlayManager.class);

    private MapDrawer m_MapDrawer;
    private RenderManager m_RenderManager; 
    private MapObjectListener m_MapObjectListener;    
    private Camera m_Camera;
    private WFTileMapHolder m_TileMapHolder;
    private TileMapControlThread m_TileMapControlThread;
    
    // The active MapObject of null if no MapObject is selected. 
    private MapObject m_ActiveMapObject = null;
    private boolean m_PolygonSelected = false;
    // The name of the active map feature or "" if nothing is selected. 
    private String m_ActiveObjectName = "";
    // The name of the selected POI or "" if no poi has been selected.
    private String m_ActivePoiText = "";
   
    private int m_HalfScreenWidth, m_HalfScreenHeight;
    
    //TODO make this configurable as it will depend on different device sizes 
    //resolution and touch sensitivity
    // The extra zone used when pressing on an object or a poi  
    private int touchPadding = 20;
    
    public MapOverlayManager(MapDrawer mapDrawer, Camera camera, RenderManager renderManager,
            WFTileMapHolder tileMapHolder, TileMapControlThread tileMapControlThread) {
        m_MapDrawer = mapDrawer;
        m_Camera = camera;
        m_RenderManager = renderManager; 
        m_TileMapHolder = tileMapHolder;
        m_TileMapControlThread = tileMapControlThread;
    }
    
    void setMapObjectListener(MapObjectListener listener) {
        m_MapObjectListener = listener;
    }
    
    void setDimension(int w, int h) {
        m_HalfScreenWidth = w/2;
        m_HalfScreenHeight = h/2;
    }
    
    String getActiveObjectName() {
        return m_ActiveObjectName;
    }
    
    MapObject getActiveMapObject() {
        return m_ActiveMapObject;
    }
    
    String getServerString() {
        String serverString = "";
        if (!m_ActiveObjectName.equals("")) {
            final int bool = (m_PolygonSelected) ? 1 : 0;
            final int lat = m_RenderManager.getActiveWorldLat();
            final int lon = m_RenderManager.getActiveWorldLon();            
            serverString = "C:"+lat+":"+lon+":"+bool+":"+m_ActiveObjectName;
        }
        return serverString; 
    }
    
    /*
     * Check if the active point if the map is over any features in the map. 
     * 
     * The selection order are:
     * 1. MapObject
     * 2. Pois
     * 3. Lines (streets)
     * 4. Polygons (bua, sea, country polygons etc.)
     *  
     * @return true if anything is selected in the map.  
     */
    boolean checkCursorSelection() {
        final MapDrawer mapDrawer = m_MapDrawer;
        Vector poiBoxes = mapDrawer.getPOIBoxes();
        Vector objectBoxes = mapDrawer.getObjectBoxes();
        Vector mapObjects = mapDrawer.getVisibleMapObjects();
        
        m_ActiveObjectName = "";
        m_PolygonSelected = false;
        
        // Check if a MapObject has been selected by the active point in the map
//        if(!checkSelectedMapObject(mapObjects)) {
            
        // Check if a POI has been selected by the active point in the map
        if (!checkSelectedPoiObject(poiBoxes)) {
            
            final RenderManager renderManager = m_RenderManager;
            // Check if a Street has been selected by the active point in the map 
            String lineText = getLineString(renderManager.getActiveWorldLat(), renderManager.getActiveWorldLon());
            if (lineText != null) {
                m_ActiveObjectName = lineText;
            } else if (objectBoxes != null) {
                // Check if the active point in the map is over a polygon (bua, sea, land polygon etc.)
                final int x = renderManager.getActiveScreenPointX();
                final int y = renderManager.getActiveScreenPointY();
                final int textLevel = -1;
                final int size = objectBoxes.size();
                for (int i=0; i<size; i++) {
                    ObjectBox ob = (ObjectBox) objectBoxes.elementAt(i);
                    if (x > ob.getStartX() && x < ob.getEndX() && y > ob.getEndY() && y < ob.getStartY()) {
                        if (textLevel < ob.getLevel()) {
                            m_ActiveObjectName = ob.getText();
                            m_PolygonSelected = true;
                            break;
                        }
                    } 
                }
            }                
        }
        
//        } else if(!m_ActivePoiText.equals("")) {
//            // If a MapObject has been selected and the poi info text is active then 
//            // we need to unselect the poi. 
//            if(m_MapObjectListener != null) {
//                m_MapObjectListener.poiUnSelected(m_ActivePoiText);
//            }
//            m_ActivePoiText = "";
//        }

        if(LOG.isDebug()) {
            LOG.debug("MapOverlayManager.checkCursorSelection()", m_ActiveObjectName);
        }
        
        return !m_ActiveObjectName.equals("");
    }
    
    /*
     * Check if the active point in the map are over a POI. 
     * 
     * @return true if a poi has been selected, false if not. 
     * 
     */
    private boolean checkSelectedPoiObject(Vector poiBoxes) {
        if (poiBoxes == null) {
            return false;
        }
        
        boolean foundPoiObjectText = false;
        
        for (int i=0; i<poiBoxes.size(); i++) {
            POIBox poi = (POIBox) poiBoxes.elementAt(i);
            if (m_RenderManager.getActiveScreenPointX() >= poi.getStartX() && 
                    m_RenderManager.getActiveScreenPointX() <= poi.getEndX() && 
                    m_RenderManager.getActiveScreenPointY() >= poi.getStartY() && 
                    m_RenderManager.getActiveScreenPointY() <= poi.getEndY()) {
                
                TileMapWrapper tmw = poi.getTmw();
                if (tmw.getRenderStringImportance() == 0 && tmw.getLayerID() != 0) {
                    // Load the textmaps for all visible tiles on tmw.getLayerID
                    m_TileMapControlThread.loadTextMaps(tmw.getLayerID());
                } else if (poi.getText() != null) {
                    m_ActiveObjectName = poi.getText();                    
                    foundPoiObjectText = true;
                    
                    if (m_MapObjectListener != null) {
                        if (!poi.getText().equals(m_ActivePoiText)) {
                            Position worldPosition = poi.getWorldPosition();
                            
                            if (!m_ActivePoiText.equals("")) { 
                                m_MapObjectListener.poiUnSelected(m_ActivePoiText, worldPosition);
                            }
                            
                            m_ActivePoiText = poi.getText();
                            m_MapObjectListener.poiSelected(m_ActivePoiText, worldPosition);
                        }
                    }
                }
                break;
            }
        }
        
//        if (m_MapObjectListener != null) {
//            if(!foundPoiObjectText && !m_ActivePoiText.equals("")) {
//                m_MapObjectListener.poiUnSelected(m_ActivePoiText);
//                m_ActivePoiText = "";
//            }
//        }
        
        return foundPoiObjectText;
    }
    
    /*
     * Check if the active point in the map are over a MapObject. 
     * 
     * @return true if a MapObject has been selected, false if not. 
     * 
     */
    private boolean checkSelectedMapObject(Vector mapObjects) {
        
        if(mapObjects == null)
            return false;
        
        boolean foundMapObjectText = false;            
        final int size = mapObjects.size();
        
        int pointerX = m_RenderManager.getActiveScreenPointX();
        int pointerY = m_RenderManager.getActiveScreenPointY();
        
        for(int i=0; i<size; i++)  {
            final MapObject mo = (MapObject)mapObjects.elementAt(i);                
            int [] screenCoord = m_Camera.getScreenCoordinateInternal(mo.getLatitude(), mo.getLongitude());
            screenCoord[0] += m_HalfScreenWidth;
            screenCoord[1] += m_HalfScreenHeight;
            
            if(pointerX >= (screenCoord[0] + mo.getMinX()) && 
               pointerX <= (screenCoord[0] + mo.getMaxX()) &&
               pointerY >= (screenCoord[1] + mo.getMinY()) && 
               pointerY <= (screenCoord[1] + mo.getMaxY())) {

                m_ActiveObjectName = mo.getName();
                foundMapObjectText = true;
                
                if(m_MapObjectListener != null) {
                    if(!mo.equals(m_ActiveMapObject)) {
                        if(m_ActiveMapObject != null) {
                            m_MapObjectListener.mapObjectUnSelected(m_ActiveMapObject);
                            m_ActiveMapObject = null;
                        }
                        if(m_MapObjectListener.mapObjectSelected(mo)) {
                            m_ActiveMapObject = mo;
                            break;
                        }
                    }
                }
            }
        }
        
        if (!foundMapObjectText && m_ActiveMapObject != null) {
            if (m_MapObjectListener != null) {
                m_MapObjectListener.mapObjectUnSelected(m_ActiveMapObject);
            }
            m_ActiveMapObject = null;
        }
                
        return foundMapObjectText;
    }
    
    /*
     * Return the name of the street that are under the coordinates specified by
     * the parameters, or null if no name can be found. 
     */
    private String getLineString(long worldX, long worldY) {
        Vector []tileMapWrappersPerLayerNumberArray = m_TileMapHolder.getTileMaps();
        String s = null;
        TileMapWrapper tmw = null;
        for(int i=0; i<tileMapWrappersPerLayerNumberArray.length; i++) {
            int size = tileMapWrappersPerLayerNumberArray[i].size();
            if(size > 0) {
                for(int j=0; j<size; j++) {
                    tmw = (TileMapWrapper)tileMapWrappersPerLayerNumberArray[i].elementAt(j);
                    s = m_MapDrawer.getLineString(worldX, worldY, tmw, m_Camera.getZoomLevel());
                    if(s!=null)
                        return s;
                }
            }
        } 
        return null;        
    }
    

    /**
     * The method check if a MapObject or a POI has been pressed in the map. 
     * Callback to the MapObjectListener will be maid if a object has been selected.
     * The order in which are checked is the revert order or drawing so in case 
     * are some draw over each other, the most visible one is selected 
     * 
     */
    void checkPressedMapOrPoiObjects(int pointerX, int pointerY) {
        
        // No need to check if a map object is pressed if we don't have a registered listener. 
        if(m_MapObjectListener == null)
            return;
        
        Vector mapObjects = m_MapDrawer.getVisibleMapObjects();
        Vector poiObjects = m_MapDrawer.getPOIBoxes();
        boolean foundMapObject = false;
        
        // Check if a MapObject has been pressed
        if (mapObjects != null) {
            final int size = mapObjects.size();
            //check the map object in the reverse order of drawing 
            //so in case are some draw over each other, the most visible one is 
            //selected  
            for(int i=size-1; i >= 0; i--)  {
                final MapObject mo = (MapObject)mapObjects.elementAt(i);                
                int [] screenCoord = m_Camera.getScreenCoordinateInternal(mo.getLatitude(), mo.getLongitude());
                screenCoord[0] += m_HalfScreenWidth;
                screenCoord[1] += m_HalfScreenHeight;
                                
                if(pointerX >= (screenCoord[0] + mo.getMinX() - touchPadding) && 
                   pointerX <= (screenCoord[0] + mo.getMaxX() + touchPadding) &&
                   pointerY >= (screenCoord[1] + mo.getMinY() - touchPadding) && 
                   pointerY <= (screenCoord[1] + mo.getMaxY() + touchPadding)) {
                    
                    foundMapObject = true;
                    if(m_MapObjectListener.mapObjectPressed(mo)) {
                        break;
                    }
                }
            }
        }
        
        // Check if a poi object has been pressed
        if(!foundMapObject && poiObjects != null) {            
            final int size = poiObjects.size();

            for(int i=0; i<size; i++)  {
                POIBox poi = (POIBox)poiObjects.elementAt(i);                
                if (pointerX >= (poi.getStartX()-touchPadding) && pointerX <= (poi.getEndX()+touchPadding) && 
                    pointerY >= (poi.getStartY()-touchPadding) && pointerY <= (poi.getEndY()+touchPadding)) {
                    
                    // Load the POI texts if they hasn't been loaded yet.
                    if (poi.getTmw().getRenderStringImportance() == 0 && poi.getTmw().getLayerID() != 0) {
                        m_TileMapControlThread.loadTextMaps(poi.getTmw().getLayerID());
                        break;
                    }
                    
                    Position worldPosition = poi.getWorldPosition();
                    if (m_MapObjectListener.poiObjectPressed(poi.getText(), worldPosition)) {
                        break;
                    }                    
                }
            }
        }
    }

}
