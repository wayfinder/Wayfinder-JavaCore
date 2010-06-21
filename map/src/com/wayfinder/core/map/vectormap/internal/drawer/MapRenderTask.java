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
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.util.WFUtil;

public class MapRenderTask {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(MapRenderTask.class);
    
    private static final int ID_SET_CENTER              = 0;
    private static final int ID_SET_SCALE               = 1;
    private static final int ID_SET_3D_MODE             = 2;
    private static final int ID_SET_ROTATION            = 3;
    private static final int ID_SET_DRAWAREA            = 4;
    private static final int ID_SET_ACTIVE_SCREEN_POINT = 5;
    private static final int ID_UPDATE_ROUTE_ID         = 6;
    private static final int ID_SET_GPS_POS             = 7;
    private static final int ID_SET_NAV_INFO            = 8;
    private static final int ID_SET_NEW_TMFD            = 9;
    private static final int ID_SET_FOLLOW_GPS_POS      = 10;
    private static final int ID_SET_WORLD_BOX           = 11;
    private static final int ID_SET_SERVER_POI_VISIBLE  = 12;
    private static final int ID_SET_COPYRIGHT_POS_Y     = 13;
    
    private RenderManager m_RenderManager;
    private Camera m_Camera;
    
    private int m_RequestedX, m_RequestedY, m_RequestedWidth, m_RequestedHeight;
    private int m_RequestedLat, m_RequestedLon;
    private float m_RequestedScale, m_RequestedRotation;
    private boolean m_RequestedUse3DMode, m_RequestedShouldFollow, m_RequestedNewRouteIsUsed, m_serverPOIvisible;
    private NavigationInfo m_RequestedNavInfo;
    private TileMapFormatDesc m_RequestedTmfd;    
    private String m_RequestedRouteID;
    private BoundingBox m_RequestedBB = null;
    private ScreenInfo m_RequestedScreenInfo = null;
    
    private int m_id;
    
    public MapRenderTask(RenderManager renderManager, Camera camera) {
        m_RenderManager = renderManager;
        m_Camera = camera;
    }
    
    void executeTask() {
        
        switch (m_id) {
            case ID_SET_CENTER:                
                m_RenderManager.setCenter(m_RequestedLat, m_RequestedLon);                
                break;
                
            case ID_SET_SCALE:
                m_Camera.setScale(m_RequestedScale);
                m_RenderManager.setIsMoving(true);
                break;
            
            case ID_SET_NAV_INFO:
                m_RenderManager.internalSetNavigationInfo(m_RequestedNavInfo);            
                break;
                
            case ID_SET_3D_MODE:
                m_Camera.set3DMode(m_RequestedUse3DMode);
                m_RenderManager.setIsMoving(true);
                break;
                
            case ID_SET_ROTATION:
                m_Camera.setRotation(m_RequestedRotation);
                m_RenderManager.setIsMoving(true);
                break;
                
            case ID_SET_DRAWAREA:
                m_RenderManager.setDrawArea(m_RequestedX, m_RequestedY, m_RequestedWidth, m_RequestedHeight);
                break;
                
            case ID_SET_ACTIVE_SCREEN_POINT:
                m_RenderManager.setActiveScreenPoint(m_RequestedX, m_RequestedY);            
                break;
                
            case ID_SET_GPS_POS:                
                m_RenderManager.internalSetGpsInformation(m_RequestedLat, m_RequestedLon, m_RequestedRotation);
                break;
                
            case ID_UPDATE_ROUTE_ID:
                if(LOG.isError()) {
                    if(m_RequestedRouteID == null)
                        LOG.error("MapRenderTask.executeTask()", "ID_UPDATE_ROUTE_ID= NULL, m_RequestedNewRouteIsUsed= "+m_RequestedNewRouteIsUsed);
                    else
                        LOG.error("MapRenderTask.executeTask()", "ID_UPDATE_ROUTE_ID= "+m_RequestedRouteID+" m_RequestedNewRouteIsUsed= "+m_RequestedNewRouteIsUsed);
                }
                m_RenderManager.updateRouteID(m_RequestedRouteID, m_RequestedNewRouteIsUsed);
                break;
                
            case ID_SET_FOLLOW_GPS_POS:                
                m_RenderManager.setFollowGpsPosition(m_RequestedShouldFollow);
                break;
                
            case ID_SET_NEW_TMFD:
                m_RenderManager.setNewTileMapFormatDesc(m_RequestedTmfd);               
                break;
                
            case ID_SET_WORLD_BOX:                
                float dp1 = WFUtil.distancePointsMeters(m_RequestedBB.getWestLongitude(), m_RequestedBB.getNorthLatitude(),
                        m_RequestedBB.getEastLongitude(), m_RequestedBB.getNorthLatitude());
                float dp2 = WFUtil.distancePointsMeters(m_RequestedBB.getCenterLon(), m_RequestedBB.getSouthLatitude(),
                        m_RequestedBB.getCenterLon(), m_RequestedBB.getNorthLatitude());
                float scale;
                
                if(dp1 > dp2)
                    scale = dp1/m_RequestedScreenInfo.getScreenWidth();
                else
                    scale = dp2/m_RequestedScreenInfo.getScreenHeight();
                 
                scale = scale * m_RequestedScreenInfo.getDPICorrection();
                m_Camera.set3DMode(false);
                m_RenderManager.setFollowGpsPosition(false);
                m_RenderManager.setCoordsCenterOnScreen(m_RequestedBB.getCenterLat(), m_RequestedBB.getCenterLon());
                m_Camera.setScale(scale);
                break;
                
            case ID_SET_SERVER_POI_VISIBLE:
                m_RenderManager.setShowServerPOIs(m_serverPOIvisible);
                break;
                
            case ID_SET_COPYRIGHT_POS_Y:
                m_RenderManager.internalSetCopyrightTextPosY(m_RequestedY);
                break;

            default:
                if(LOG.isError()) {
                    LOG.error("MapRenderTask.executeTask()", "Unknown request id= "+m_id);
                }
                break;
        }
    }
    
    public void requestSetCenter(int lat, int lon) {
        m_id = ID_SET_CENTER;
        m_RequestedLat = lat;
        m_RequestedLon = lon;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSetScale(float scale) {
        m_id = ID_SET_SCALE;
        m_RequestedScale = scale;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSet3DMode(boolean use3DMode) {
        m_id = ID_SET_3D_MODE;
        m_RequestedUse3DMode = use3DMode;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSetRotation(float angle) {
        m_id = ID_SET_ROTATION;
        m_RequestedRotation = angle;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSetDrawArea(int x, int y, int width, int height) {
        m_id = ID_SET_DRAWAREA;
        m_RequestedX = x;
        m_RequestedY = y;
        m_RequestedWidth = width;
        m_RequestedHeight = height;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSetActiveScreenPoint(int screenX, int screenY) {
        m_id = ID_SET_ACTIVE_SCREEN_POINT;
        m_RequestedX = screenX;
        m_RequestedY = screenY;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestUpdateRouteID(String routeID, boolean isUsed) {
        m_id = ID_UPDATE_ROUTE_ID;
        m_RequestedRouteID = routeID;
        m_RequestedNewRouteIsUsed = isUsed;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSetGpsPositions(int lat, int lon, float angle) {   
        m_id = ID_SET_GPS_POS;
        m_RequestedLat = lat;
        m_RequestedLon = lon;
        m_RequestedRotation = angle;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestUpdateNavigationInfo(NavigationInfo navInfo) {
        m_id = ID_SET_NAV_INFO;
        m_RequestedNavInfo = navInfo;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestNewTileMapFormatDesc(TileMapFormatDesc tmfd) {
        m_id = ID_SET_NEW_TMFD;
        m_RequestedTmfd = tmfd;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestFollowGpsPosition(boolean shouldTrack) {
        m_id = ID_SET_FOLLOW_GPS_POS;
        m_RequestedShouldFollow = shouldTrack;
        m_RenderManager.addMapTask(this);
    }

    public void requestSetWorldBox(BoundingBox bb, ScreenInfo si) {
        m_id = ID_SET_WORLD_BOX;
        m_RequestedBB = bb;
        m_RequestedScreenInfo = si;
        m_RenderManager.addMapTask(this);
    }
    
    public void requestSetServerPOIVisible(boolean shouldBeVisible) {
        m_id = ID_SET_SERVER_POI_VISIBLE;
        m_serverPOIvisible = shouldBeVisible;
        m_RenderManager.addMapTask(this);
    }
    
    public void setCopyrightPosY(int screenY) {
        m_id = ID_SET_COPYRIGHT_POS_Y;
        m_RequestedY = screenY;
        m_RenderManager.addMapTask(this);
    }
}
