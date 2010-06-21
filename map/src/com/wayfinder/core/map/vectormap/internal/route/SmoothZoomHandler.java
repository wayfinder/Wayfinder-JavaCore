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

package com.wayfinder.core.map.vectormap.internal.route;

import com.wayfinder.core.map.vectormap.internal.VectorMapModule;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLoader;
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.Waypoint;

/**
 * 
 * Helper class used to calculate which zoom level that should be used while doing the smooth zoom. 
 * 
 */
public class SmoothZoomHandler {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(SmoothZoomHandler.class);
    
    // Smooth zooming variables
    private int m_TargetZoomLevel   = -1;
    private int m_CurrentZoomLevel  = -1;
    private int m_LastRdSegSpdLimit = -1;
    
    private Waypoint m_NextWpt;
    private String m_CurrentRouteID = "N/A";
    
    private TileMapControlThread m_TileMapControlThread;
    private TileMapLoader m_TileMapLoader;
    private VectorMapModule m_VectorMapModule;
    
    public SmoothZoomHandler(TileMapControlThread tmControl, VectorMapModule mapModule) {
        m_TileMapControlThread = tmControl;
        m_TileMapLoader = tmControl.getTileMapLoader();
        m_VectorMapModule = mapModule;
    }
    
    /**
     * Return the current "smooth zoom" zoom level that should be used. 
     * 
     * @param navInfo
     * @return
     */
    public int doSmoothZoom(NavigationInfo navInfo) {
        
        
        m_TargetZoomLevel = getTargetZoomLevel(navInfo.getSpeedLimitKmh(), 
                                               navInfo.getDistanceMetersToNextWpt(), 
                                               navInfo.getRoute().getRouteID(), 
                                               navInfo.getNextWpt());
        
        if(m_CurrentZoomLevel != m_TargetZoomLevel) {
            m_CurrentZoomLevel = internalDoSmoothZoom(m_CurrentZoomLevel, m_TargetZoomLevel);
            
            /* Block the downloading of route tiles while smooth zooming. This to lower the
             * data download from the server. This will not visually effect the user experiace much. 
             * The maps that isn't shown will only be shown for a short while. */
            if(m_CurrentZoomLevel != m_TargetZoomLevel) {
                m_TileMapLoader.setOfflineModeForCachedLayer(true);
            } else {
                m_TileMapControlThread.refreshAllLayers();
            }
            m_VectorMapModule.requestMapUpdate();
        }
        
        return m_CurrentZoomLevel;
    }
    
    /**
     * Return the zoom level used for the current road segment. 
     * 
     * @param currentRdSpeedLimit the speed limit (km/h) for the current road segment. 
     * @param distanceMetersToNextWpt the distance in meter to the next waypoint. 
     * @param routeID the route id for the route. 
     * @param nextWpt the next Waypoint object must not be null 
     * @return the target zoom level that should be used. 
     */
    public int getTargetZoomLevel(int currentRdSpeedLimit, int distanceMetersToNextWpt, String routeID, Waypoint nextWpt) {
        
        int targetZoomLevel = m_TargetZoomLevel;
        
        if(!nextWpt.equals(m_NextWpt)) {
            
//            if(LOG.isError()) {
//                LOG.error("NavigationZoomHandler.doSmoothZoom()", "NEW WPT!");
//            }
            
            m_NextWpt = nextWpt;
        } 
            
        final int nextRdSpeedLimit = m_NextWpt.getSpeedLimitKmhAfter();
        final int currentRdLength = m_NextWpt.getDistanceMetersFromPrev();
        
        // New route, set the correct zoom level.
        if(!routeID.equals(m_CurrentRouteID)) {
            
            String oldRouteID = m_CurrentRouteID;
            
            m_CurrentRouteID = routeID;
            m_CurrentZoomLevel = getZoomFromSpeed(currentRdSpeedLimit);
            targetZoomLevel = m_CurrentZoomLevel;
            
            if(LOG.isInfo()) {
                LOG.info("SmoothZoomHandler.getTargetZoomLevel()", "New ROUTE"+
                        " oldRouteID= "+oldRouteID+" newRouteID= "+m_CurrentRouteID+
                        " m_CurrentZoomLevel= "+m_CurrentZoomLevel+" m_TargetZoomLevel= "+targetZoomLevel);
            }
        }
        
        /*  
         * If the distance from the previous waypoint are shorter the 30 meter we 
         * don't change the zoom level. Thats to make it possible to predict what 
         * tiles we are needing to download for the route (Predict route tiles).
         */
        if((currentRdLength-distanceMetersToNextWpt) > 30) {
                                    
            /*
             * Change the zoom if it will take more than 25 second to travel to the next road. 
             */
            if ((currentRdSpeedLimit != m_LastRdSegSpdLimit) && 
                    (distanceMetersToNextWpt > currentRdSpeedLimit*Utils.TWENTYFIVE_SEC_FACTOR )){
                
                if(LOG.isDebug()) {
                    LOG.debug("SmoothZoomHandler.getTargetZoomLevel()", "ZoomOut"+ 
                            " targetZoomLevel changed from " + targetZoomLevel + 
                            " to " + getZoomFromSpeed(currentRdSpeedLimit));
                }
                
                targetZoomLevel = getZoomFromSpeed(currentRdSpeedLimit);                    
                m_LastRdSegSpdLimit = currentRdSpeedLimit;
            }
            /*
             * Always zoom in at next wpt when distance to it is shorter than 15 second.
             * distToNxWpt should be longer than 10 so that zoomInFlag can be turned on at the beginning
             * of the next road, as many times it will not be set to zero when approaching end of last road.               
             */
            else if ((distanceMetersToNextWpt >= 10) && 
                    (distanceMetersToNextWpt <= currentRdSpeedLimit*Utils.FIFTEEN_SEC_FACTOR)){
                
                int tmpTargetZoom = targetZoomLevel;

                if(nextRdSpeedLimit < 50)
                    targetZoomLevel = getZoomFromSpeed(30);
                else
                    targetZoomLevel = getZoomFromSpeed(50);
                
                if(LOG.isDebug()) {
                    if(tmpTargetZoom != targetZoomLevel) {
                        LOG.debug("SmoothZoomHandler.getTargetZoomLevel()", "ZoomIn"+
                                " targetZoomLevel changed from "+tmpTargetZoom+
                                " to " + targetZoomLevel);
                    }
                }
            }
        }
        return targetZoomLevel;
    }

    /*
     * Internal method used to zoom smoothly between the current used zoom level and the target zoom level. 
     * 
     */
    private int internalDoSmoothZoom(int currentZoomLevel, int targetZoomLevel) {
        
        int newZoom = currentZoomLevel;
        final int zoomDiff = Math.abs(currentZoomLevel-targetZoomLevel);
        
        if (currentZoomLevel > targetZoomLevel) {
            // Zoom IN to targetZoomLevel
            if (zoomDiff > 15)
                newZoom -= 5;
            else if (zoomDiff > 9)
                newZoom -= 3;
            else if (zoomDiff > 5)
                newZoom -= 2;
            else
                newZoom --;
            
        } else {            
            // Zoom OUT to targetZoomLevel
            if (zoomDiff > 15)
                newZoom += 5;
            else if (zoomDiff > 9)
                newZoom += 3;
            else if (zoomDiff > 5)
                newZoom += 2;
            else
                newZoom ++;
        }
        
        return newZoom;
    }
    
    /*
     * Return the zoom level that should be used for the speed 
     * specified by the parameter. 
     * <p>
     * The speed should be in km/h.
     */
    private int getZoomFromSpeed(int speed) {
        int targetScale;        
        if (speed < 55) {
            targetScale = 4;
        } else if(speed < 75) {
            targetScale = 10;
        } else if(speed < 95) {
            targetScale = 20;
        } else {
           targetScale = 40;
        }        
        return targetScale;
    }
    
}
