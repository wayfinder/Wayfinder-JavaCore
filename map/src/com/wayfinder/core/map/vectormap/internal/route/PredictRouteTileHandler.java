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
import com.wayfinder.core.map.vectormap.internal.drawer.Camera;
import com.wayfinder.core.map.vectormap.internal.route.RouteTileDownloader.DownloadState;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.route.Route;

/**
 * 
 */
public class PredictRouteTileHandler {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(PredictRouteTileHandler.class);
    
    private TileMapControlThread m_TileMapControl;
    private RouteTileDownloader m_RouteTileDownloader;
    private DownloadState m_DownloaderState;
    private WorkScheduler m_Scheduler;
    private VectorMapModule m_VectorMapModule;
    
    /* The real camera that used to view the map on the screen */
    private Camera m_RealCam;    
    
    private boolean m_IsVisible = false;
    private int m_ScreenHeight;
    
    private Route m_CurrentRoute;

    public PredictRouteTileHandler(WorkScheduler workScheduler, Camera camera, 
            TileMapControlThread tmControl, VectorMapModule mapModule) {    
    	
    	m_TileMapControl = tmControl;    
    	m_Scheduler = workScheduler;
    	m_RealCam = camera;
    	m_VectorMapModule = mapModule;
    	m_DownloaderState = null;
    	m_RouteTileDownloader = null;
    }
    
    /**
     * Set a new route to the RouteTileDownloader 
     * 
     * @param aRoute, the current route
     */
    public void setNewRoute(Route route) {
        
        m_CurrentRoute = route;
    	
    	/* Interrupt any previous active route downloader */
    	if(m_RouteTileDownloader != null) {
    		m_RouteTileDownloader.interrupt();
    		m_RouteTileDownloader = null;
    		m_DownloaderState = null;
    	}
    	
        //TODO: Hack check if the map was loaded otherwise a NPE will occurs 
        // in m_RouteTileDownloader.initRoute as it's need a TMFD 
    	// will be replaced by a better handling of map initialization
    	if (m_TileMapControl.getTileMapFormatDesc() == null) {
    	    if(LOG.isWarn()) {
                LOG.warn("PredictRouteTileHandler.setNewRoute()", 
                        "the map is not loaded, cannot predict tiles without TMFD");
            }
    	    return;
    	}
    	
    	/* Set the route to the route downloader and start the thread */
    	if(m_CurrentRoute != null) {
	    	m_RouteTileDownloader = new RouteTileDownloader(m_TileMapControl, m_RealCam, m_VectorMapModule);
	    	m_RouteTileDownloader.setScreenHeight(m_ScreenHeight);
	    	m_DownloaderState = m_RouteTileDownloader.getDownloadState();
	    	if(m_RouteTileDownloader.initRoute(m_CurrentRoute,m_IsVisible)) {	    	
    	    	m_DownloaderState.setDownloading(true);
    	    	m_Scheduler.schedule(m_RouteTileDownloader, WorkScheduler.PRIORITY_HIGH);
	    	}
    	}
    }
    
    /**
     * Sets the navigation mapview to visible. 
     * 
     * @param visible
     */
    public void setVisible(boolean visible) {
        
        if(LOG.isInfo()) {
            LOG.info("PredictRouteTileHandler.setVisible()", "visible= "+visible);
        }
        
        m_IsVisible = visible;
        
        if(m_RouteTileDownloader != null)
        	m_RouteTileDownloader.setVisible(visible);        
    }
    
    public void setScreenHeight(int height) {
        m_ScreenHeight = height;
    }
    
    /**
     * When we predict and download route tiles we only download max 1500 param strings
     * in one batch. This method checks if we have reached the limit when we need to download
     * the next batch of 1500 param strings. 
     * 
     * @param aCurrentLat, gps lat position (MC2)
     * @param aCurrentLon, gps lon position (MC2)
     */
    private int check_cnt=0;
    public void checkCurrentDistanceDiff(int currentLat, int currentLon) {
        if(m_CurrentRoute == null || 
                    m_RouteTileDownloader == null || m_DownloaderState == null)
        	return;
        
        /* We only need to make the check every 10th gps position. This to avoid a lot of calculations */
        if(check_cnt > 10 ) {
        	check_cnt = 0;
        	if(!m_DownloaderState.isDownloading() &&
        	        m_RouteTileDownloader.hasReachedDistanceLimit(currentLat, currentLon)) {	   
	        	m_RouteTileDownloader.continueRoute(m_IsVisible);
	        	m_DownloaderState.setDownloading(true);
	        	m_Scheduler.schedule(m_RouteTileDownloader, WorkScheduler.PRIORITY_HIGH);   
        	}        	
        }
        check_cnt++;
    }
}
