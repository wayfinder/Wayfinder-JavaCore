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

package com.wayfinder.core.map.vectormap.internal;

import com.wayfinder.core.map.CopyrightHandler;
import com.wayfinder.core.map.vectormap.MapDetailedConfigInterface;
import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.drawer.Camera;
import com.wayfinder.core.map.vectormap.internal.drawer.MapRenderTask;
import com.wayfinder.core.map.vectormap.internal.drawer.RenderManager;
import com.wayfinder.core.map.vectormap.internal.route.PredictRouteTileHandler;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.Route;

public class MapDetailedConfigInterfaceImpl implements MapDetailedConfigInterface {
    
    final private TileMapControlThread m_TileMapControlThread;
    final private RenderManager m_RenderManager;
    final private Camera m_Camera;
    final private VectorMapModule m_VectorMapModule; 
    private PredictRouteTileHandler m_PredictRouteTileHandler = null;
    
    public MapDetailedConfigInterfaceImpl(TileMapControlThread controlThread, RenderManager renderManager,
            Camera camera, VectorMapModule mapModule) {
        m_TileMapControlThread = controlThread;
        m_RenderManager = renderManager;
        m_Camera = camera;
        m_VectorMapModule = mapModule;
    }
    
    public void init(PredictRouteTileHandler rtHandler) {
        m_PredictRouteTileHandler = rtHandler;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#getCopyrightHandler()
     */
    public CopyrightHandler getCopyrightHandler() {
        return m_RenderManager.getCopyrightHandler();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#getServerString()
     */
    public String getServerString() {
        return m_RenderManager.getServerString();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#getServerString(int, int, java.lang.String, boolean)
     */
    public String getServerString(int lat, int lon, String name, boolean isPolygon) {
        if(name == null || name.equals(""))
            throw new IllegalArgumentException("Invalid name proveded: "+name);
        final int bool = (isPolygon) ? 1 : 0;
        return "C:"+lat+":"+lon+":"+bool+":"+name;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setDownloadACPEnabled(boolean)
     */
    public void setDownloadACPEnabled(boolean enabled) {
        m_TileMapControlThread.setDownloadACPEnabled(enabled);        
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setServerPOIsVisible(boolean)
     */
    public void setServerPOIsVisible(boolean isVisible) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetServerPOIVisible(isVisible);
        m_VectorMapModule.requestMapUpdate();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setOfflineMode(boolean)
     */
    public void setOfflineMode(boolean shouldBeOffline) {
        m_TileMapControlThread.getTileMapLoader().setOfflineMode(shouldBeOffline);        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setRoute(com.wayfinder.core.shared.route.Route)
     */
    public boolean setRoute(Route route) {
        
        if(route == null) {
            
            // Interrupt the current route
            if(m_PredictRouteTileHandler != null) {
                m_PredictRouteTileHandler.setNewRoute(null);
            }              
            
            // Set the new route id to the map loader
            boolean isUsed = m_TileMapControlThread.setRouteID(null);
            
            // Update the map renderer with the new route id.
            MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
            task.requestUpdateRouteID(null, isUsed);
            m_VectorMapModule.requestMapUpdate();
            return isUsed;
            
        } else {
            String routeID = route.getRouteID();
            // Set the new route id to the map loader
            boolean isUsed = m_TileMapControlThread.setRouteID(routeID);
            
            // Update the map renderer with the new route id. 
            MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
            task.requestUpdateRouteID(routeID, isUsed);
            
            // Start downloading route tiles for the new route. 
            if(m_PredictRouteTileHandler != null) {
                m_PredictRouteTileHandler.setNewRoute(route);
            }   
            m_VectorMapModule.requestMapUpdate();
            return isUsed;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setRouteID(java.lang.String)
     */
    public boolean setRouteID(String routeID) {
        boolean isUsed = m_TileMapControlThread.setRouteID(routeID);
        
        // Update the map renderer with the new route id. 
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestUpdateRouteID(routeID, isUsed);
        m_VectorMapModule.requestMapUpdate();
        return isUsed;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setNavigationInfo(com.wayfinder.core.shared.route.NavigationInfo)
     */
    public void setNavigationInfo(NavigationInfo navInfo) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestUpdateNavigationInfo(navInfo);
        m_VectorMapModule.requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#getRouteID()
     */
    public String getRouteID() {
        return m_TileMapControlThread.getRouteID();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setTrafficInfoUpdateTime(int)
     */
    public void setTrafficInfoUpdateTime(int minutes) {
        m_TileMapControlThread.setTrafficInfoUpdateTime(minutes);
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setTrafficLayerVisible(boolean)
     */
    public void setTrafficLayerVisible(boolean isVisible) {
        m_TileMapControlThread.setTrafficLayerVisible(isVisible);        
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#saveCache()
     */
    public boolean saveCache() {
        return m_TileMapControlThread.saveCache();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#getPoiCategories()
     */
    public PoiCategory[] getPoiCategories() {
        return m_TileMapControlThread.getPoiCategories();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setPoiCategories(com.wayfinder.core.map.vectormap.PoiCategory[])
     */
    public void setPoiCategories(PoiCategory []poiCat) {
        m_TileMapControlThread.setPoiCategories(poiCat);
        // Force a update of the map to enable the changes in the poi categories.              
        m_Camera.mapChangeNotify();
        m_VectorMapModule.requestMapUpdate();
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.MapDetailedConfigInterface#setCopyrightTextPositionY(int)
     */
    public void setCopyrightTextPositionY(int y) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.setCopyrightPosY(y);
        m_Camera.mapChangeNotify();
        m_VectorMapModule.requestMapUpdate();
    }

}
