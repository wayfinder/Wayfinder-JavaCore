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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.wayfinder.core.map.MapDownloadListener;
import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.VectorMapModule;
import com.wayfinder.core.map.vectormap.internal.cache.CacheInterface;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLoader;
import com.wayfinder.core.map.vectormap.internal.control.TileMapNetworkHandler;
import com.wayfinder.core.map.vectormap.internal.control.TileMapRequestListener;
import com.wayfinder.core.map.vectormap.internal.drawer.Camera;
import com.wayfinder.core.map.vectormap.internal.process.RouteID;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;
import com.wayfinder.core.shared.route.NavigatorRouteIterator;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.Waypoint;
import com.wayfinder.core.shared.util.WFUtil;

/**
 * The RouteTileDownloader class iterate the route and calculating what 
 * TileMaps the vector map component will need to download to view the 
 * route on the screen. 
 * 
 * When the class has calculated what tiles it need it downlaod them
 * in block of 150 param strings. When the tiles has been downloaded
 * it will be saved to the read/write cache.  
 * 
 * The route is divided into block of 1500 param strings. When we have
 * iterate the route and found 1500 param strings we download them.
 * 
 * If we haven't reached the end of the route a new batch of 1500 
 * param strings will be downloaded 100 km before we run out of tiles
 * in the cache. 
 * 
 * 
 */
class RouteTileDownloader implements Runnable, TileMapRequestListener {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(RouteTileDownloader.class);
	
    private static final boolean GZIP = true;
    /* We send param string in block of 150 to the server */
    private static final int MAX_NBR_PARAMS_TO_SEND = 150;
    /* The maximal number of param strings to calculate for each block */
    private static final int MAX_NBR_PARAMS_TO_CALC = 1500;
    /* Time to wait before we start sending param strings to the server */
    private static final int NBR_OF_MS_DELAY        = 60000;
    
	private TileMapFormatDesc tmfd;
	private TileMapControlThread m_TileMapControl;
    private TileMapLoader m_TileMapLoader;
    private MapDownloadListener m_MapDownloadListener;
    private TileMapNetworkHandler m_TileMapNetworkHandler;
    private DownloadState m_PredState;
    private CacheInterface m_Cache;
    private SmoothZoomHandler m_SmoothZoomHandler;
    
    /* True if we want to interrupt the current route tiles calculations */
    private boolean m_Interrupted = false;
	
	/* The local camera that are used to calculate what tiles 
       that will be visible along the route */
    private Camera m_RouteCam;
    
    /* The real camera that used to view the map on the screen */
    private Camera m_RealCam;
    
    /* The iterator for the current route*/
    private NavigatorRouteIterator iter;
    
    /* The current route id that are used to download the route layer in the map */
    private RouteID m_routeID;
    private String m_RouteIDAsString;
   
    /* Nbr of added param strings */
    private int m_ParamStringCount;
    private int m_WrapperCount;
    
    /* The distance from the start of the route to the point when we 
       will continue to download param strings. */ 
    private int iCurrentDistanceLimit = 0;  
    private boolean iReadyToCalculateDistanceDiff = false;
	        
    private boolean iThreadInWait = false;
    
    /* True if the map view are visible */
    private boolean iIsVisible = false;
    
    /* The start time from when the route tiles calculation are finished or when we enter the map view. 
     * Whatever happens last. We will wait NBR_OF_MS_DELAY until we actually start to download the param strings */
    private long iStartTime = 0;
    
    /* True if we have started to download route tiles from the server */
    private boolean iIsDownloadingRouteTiles = false;
    
    private int m_ScreenHeight = 0;
    
    private int []lastxMax;
    private int []lastxMin;
    private int []lastyMax;
    private int []lastyMin;    
    private int []lastDetailLevel;
    private int []lastNbrImportence;
    
	RouteTileDownloader(TileMapControlThread tileMapControl, Camera realCam, VectorMapModule mapModule) {
		m_TileMapControl = tileMapControl;
		m_TileMapLoader = m_TileMapControl.getTileMapLoader();
		tmfd = m_TileMapControl.getTileMapFormatDesc();
		m_TileMapLoader.getTileMapNetworkHandler();
		m_TileMapNetworkHandler = m_TileMapLoader.getTileMapNetworkHandler();
		m_Cache = m_TileMapLoader.getCache();
		m_RealCam = realCam;
		m_PredState = new DownloadState();
		m_SmoothZoomHandler = new SmoothZoomHandler(tileMapControl, mapModule);
	}
	
	
	/* Holds the start lat/lon coordinates that will be used to calculate
	 * when we need to continue to download route tiles. We only download 
	 * param strings in batches of 1500 */
	private int startLat = 0;
    private int startLon = 0;
	
	/**
     * Init the route tile downloader with a new route. 
     *
     */
    boolean initRoute(Route route, boolean visible) {
//        System.out.println("TileMapDownloader.initRoute() routeID= "+ route.getRouteID());

        m_RouteIDAsString = route.getRouteID();
        m_routeID = new RouteID(m_RouteIDAsString);
        iter = null;
        init();
        iIsVisible = visible;
        try {
            iter = route.getFirstCoordinate();
            // Save the start coordinates of the route            
            startLat = iter.getMc2Latitude();
            startLon = iter.getMc2Longitude();
        } catch(IOException e) {
            if(LOG.isError()) {
                LOG.error("RouteTileDownloader.initRoute()", e);
            }
            return false;
        }
        return true;
    }
    
    /*
     * Init all the variables that are needed.  
     */
    private void init() {
    	
        m_MapDownloadListener = m_TileMapLoader.getMapDownloadListener();
        m_RouteCam = new Camera();
        m_RealCam.clone(m_RouteCam, 0);        
        m_RouteCam.set3DMode(true);
        m_RouteCam.setSkyHeight(m_RealCam.getSkyHeight());
        
        iRequestedParams = new Hashtable();
        
        lastxMin            = new int[tmfd.getNumberOfLayers()];
        lastxMax            = new int[tmfd.getNumberOfLayers()];
        lastyMin            = new int[tmfd.getNumberOfLayers()];
        lastyMax            = new int[tmfd.getNumberOfLayers()];        
        lastDetailLevel     = new int[tmfd.getNumberOfLayers()];
        lastNbrImportence   = new int[tmfd.getNumberOfLayers()];
        
        for(int i=0; i<tmfd.getNumberOfLayers(); i++) {
        	lastDetailLevel[i] = 0;
        	lastNbrImportence[i] = 0;
        	lastxMin[i] = Integer.MAX_VALUE;
        	lastxMax[i] = Integer.MAX_VALUE;
        	lastyMin[i] = Integer.MAX_VALUE;
        	lastyMax[i] = Integer.MAX_VALUE;
        }
        
        iIsDownloadingRouteTiles = false;
        iCurrentDistanceLimit = 0;
        iReadyToCalculateDistanceDiff = false;
        m_ParamStringCount = 0;
        m_WrapperCount = 0;
        m_Interrupted = false;
    }
    
    /**
     * Called before we continue downloading route tiles. 
     */
    void continueRoute(boolean visible) {
    	init();
    	startLat = iter.getMc2Latitude();
    	startLon = iter.getMc2Longitude();
    	iIsVisible = visible;
    }
    
    /**
     * Called when we want to interrupt the current route downloader. 
     * This happen when we do a re-route or clear the route. 
     */
    void interrupt() {
		m_Interrupted = true;		
		notifyThread();
	}
    
    /**
     * @param aVisible true when the map view are set to visible. 
     * The thread will we notified.  
     */
    void setVisible(boolean aVisible) {
    	iIsVisible = aVisible;
    	
    	if(iIsVisible)
    		notifyThread();    		
    }
    
    public void setScreenHeight(int screenHeight) {
        m_ScreenHeight = screenHeight;
    }
    
    /*
     * Wake up the thread
     */
    private synchronized void notifyThread() {			
		iThreadInWait = false;
		notifyAll();			
    }
    
    private synchronized void waitForThreadToWakeUp() {    	
		iThreadInWait = true;
		try {
		    while(iThreadInWait) {    
                wait();
            }
		} catch (InterruptedException e){
		    if(LOG.isError()) {
                LOG.error("RouteTileDownloader.waitForThreadToWakeUp()", e);
            }
		}
    }

    /**
     * 
     * 
     */
	public void run() {
	    m_PredState.setDownloading(true);		
	    long time2 = System.currentTimeMillis();
               
		calculateRouteTiles();
		
		if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.run()", "m_ParamStringCount= "+m_ParamStringCount+
                    " m_WrapperCount= "+m_WrapperCount+" time= "+(System.currentTimeMillis()-time2)+
                    " ms iter= "+iter);
        }
		
        /*
         * Set the thread to wait if the user isn't in the mapview. 
         */
		if(!iIsVisible && !m_Interrupted) {
			waitForThreadToWakeUp();
		}
		
		iStartTime = System.currentTimeMillis();
		
		if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.run()", "Waiting " + NBR_OF_MS_DELAY + " ms before starting download...");
        }
		
		/*
		 * Wait NBR_OF_MS_DELAY seconds before we start to download route tiles.
		 *  
		 * If the user are in the map view after NBR_OF_MS_DELAY we continue. 
		 */
		long time = 0;		
		while(!m_Interrupted && ((System.currentTimeMillis()-iStartTime) < NBR_OF_MS_DELAY)) {
			synchronized(this) {
				try {
					time = NBR_OF_MS_DELAY - (System.currentTimeMillis()-iStartTime);
	                wait(time);
	            } catch (InterruptedException e) {
	                if(LOG.isError()) {
                        LOG.error("RouteTileDownloader.run()", e);
                    }
	            }
			}
		}
		
		m_TileMapLoader.setOfflineMode(true);
		
		if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.run()", "Waiting for outgoing request to be downloaded " +
            		"before starting download");
        }
		
		/*
		 * Wait until all the outgoing request has been downloaded until we start the
		 * route predict downloading.  
		 */
		while(!m_Interrupted && (m_TileMapLoader.getNbrOfRequestedParams() > 0)) {			
			synchronized(this) {
				try {
	                wait(1000);
	            } catch (InterruptedException e) {
	                if(LOG.isError()) {
	                    LOG.error("RouteTileDownloader.run()", e);
	                }
	            }
			}
		}
		
		if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.run()", "Starting download of route tiles");
        }
        
        /* If we are still in the map view and the thread isn't interrupted
           then we start to download the tiles */
        if(iIsVisible && !m_Interrupted) {
            
            if(LOG.isInfo()) {
                LOG.info("RouteTileDownloader.run()", "send the first request...");
            }
            
            m_TileMapNetworkHandler.setTileMapRequestListener(this);
            iIsDownloadingRouteTiles = true;
            if(m_MapDownloadListener != null)
                m_MapDownloadListener.handleDownloadEvent(true);
            
            try {
                /* Keep the thread alive while downloading route tiles. */ 
                while(iIsDownloadingRouteTiles && !m_Interrupted) {
        			sendTileMapParams();
        			m_TileMapControl.refreshAllLayers();
        			if(iIsDownloadingRouteTiles)
        				waitForThreadToWakeUp();            	
                }
            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("RouteTileDownloader.run()", e);
                }
                m_Interrupted = true;
            }
        }
        
        m_TileMapNetworkHandler.setTileMapRequestListener(m_TileMapLoader);
        m_TileMapLoader.setOfflineMode(false);
        if(m_MapDownloadListener != null)
            m_MapDownloadListener.handleDownloadEvent(false);
        m_Interrupted = true;
        
        if(iter == null) {
        	iReadyToCalculateDistanceDiff = false;
        }
        m_PredState.setDownloading(false);        
        m_TileMapLoader.getCache().purgeData();
        
        if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.run()", "The download are finished iter= "+iter);
            LOG.info("RouteTileDownloader.run()", "nbrSend= "+m_TotNbrSend+" nbrReceived= "+m_NbrReceived);
        }
	}
	
	/* Holds the TileMapParams that has been send to the server. */
	private Hashtable iRequestedParams;
	private Hashtable iRequestedWrapper = new Hashtable(200);
	private BitBuffer bitBuffer = new BitBuffer(64);
	
	// For debug only
    private int m_NbrReceived = 0;
	private int m_TotNbrSend = 0;
	
	/**
	 * Sends the calculated param strings to the server. 
	 * 
	 * The variable iIsDownloadingRouteTiles will be true as long
	 * as there are more param strings to download. 
	 * 
	 */
    private void sendTileMapParams() {    	
    	
//        final String fname = "RouteTileDownloader.sendTileMapParams() "+ Thread.currentThread()+ ": ";
        
		if((iAddedWrappers.size() > 0 || iRequestedParams.size() > 0) && !m_Interrupted) {
		    
		    int cnt = 0;
	        TileMapParams param;
	        TileMapParams currentParam;
	        Enumeration iEnum;
	        
	        /* Re-send any request that has come back via the requestFailed method. */
	        if(iRequestedParams.size() > 0) {
	            iEnum = iRequestedParams.elements();
	            while(iEnum.hasMoreElements()) {
	                param = (TileMapParams)iEnum.nextElement();
	                m_TileMapNetworkHandler.request(param.getAsString());	                
	                cnt++;
	            }
	        }
		    
		    RouteTileWrapper wrapper;
		    String paramString;
		    iEnum = iAddedWrappers.elements();
		    
		    /* */
		    while(iEnum.hasMoreElements()) {
		        
		        if(cnt >= MAX_NBR_PARAMS_TO_SEND) {		            
		            break;
		        }
		        
		        wrapper = (RouteTileWrapper)iEnum.nextElement();
		        param = wrapper.getTileIDParam();
		        iAddedWrappers.remove(param.getTileID());
		        iRequestedWrapper.put(param.getTileID(), wrapper);
		        
		        for(int i=0; i<wrapper.getNumberOfImportances(); i++) {
		            bitBuffer.softReset();
		            
		            currentParam = param.cloneTileMapParams();		            
		            currentParam.setLanguageType(LangTypes.SWEDISH);
		            paramString = currentParam.getAsString(bitBuffer, i, TileMapParams.MAP);
		            m_TileMapNetworkHandler.request(paramString);	
		            
		            iRequestedParams.put(paramString, currentParam);		            
		            cnt++;
		            m_TotNbrSend++;
		            
		            if(tmfd.alwaysFetchStrings(param.getLayerID())) {
		                bitBuffer.softReset();
		                currentParam = param.cloneTileMapParams();
		                currentParam.setLanguageType(m_TileMapControl.getLanguage());
		                paramString = currentParam.getAsString(bitBuffer, i, TileMapParams.STRINGS);		                
		                m_TileMapNetworkHandler.request(paramString);
	                    	                    
	                    iRequestedParams.put(paramString, currentParam);
	                    cnt++;
	                    m_TotNbrSend++;
		            }
		        }
		    }
		    m_TileMapNetworkHandler.sendRequest();
		} else {		    
            iIsDownloadingRouteTiles = false;
        }    	
         
		m_RealCam.mapChangeNotify();
    }
	
	/**
     * Iterate the route and calculate what paramstrings that 
     * the map will need to view the route. 
     * 
     * 
     * @param useLimit
     */
    private void calculateRouteTiles() {
        
        if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.calculateRouteTiles()", "Start calculating route tiles...");
        }
    	
        //XXX: Make sure that this is the correct gps adjust!        
//        final int gpsAdjust = m_RealCam.getGPSAdjust();
        final int gpsAdjust = (m_ScreenHeight*2)/3;
        
        int detailLevel=0, nbrImportances;
        int zoomLevel, layerID, lat, lon;
        int[] camBoundingBox;
        int []tileIndices = new int[4];
        double angle;
        
        BitBuffer bitbuffer = new BitBuffer(64);
    	
    	int lengthFromLastWpt = 0;
    	int totalLengthToNextWpt = 0;
    	
        int newDetailLevel;
    	
        //For all coordinats in the route
        while(iter.isValid() && !m_Interrupted) {
            
        	/* Holds the distance in meter from the previous wpt */
        	lengthFromLastWpt += iter.getSegmentLengthMeters();
        	
        	Waypoint nextWpt = iter.getWpt().getNext();
        	
        	//TODO something is wrong in this cycle 
        	//this line try to fix sending null parameter to 
        	//m_SmoothZoomHandler.getTargetZoomLevel
        	if (nextWpt == null) break;
        	
        	if(iter.isWpt()) {
        	    lengthFromLastWpt = 0;
        	    
        	    //if(nextWpt != null) { old code see TODO above
                    totalLengthToNextWpt = nextWpt.getDistanceMetersFromPrev();
        	    //}
        	}
        	
        	int distanceMetersToNextWpt = (totalLengthToNextWpt-lengthFromLastWpt);
        	int zoom = m_SmoothZoomHandler.getTargetZoomLevel(iter.getSpeedLimitKmh(), 
        	                                                  distanceMetersToNextWpt, 
        	                                                  m_RouteIDAsString, 
        	                                                  nextWpt);
        	
        	
            lat = iter.getMc2Latitude();
            lon = iter.getMc2Longitude();
            
            angle = iter.getSegmentCourseRad();
            angle += (Math.PI/2);
            if(angle<0){
                angle+= (2*Math.PI);
            }
            
            m_RouteCam.setPosition(lat,lon);
            m_RouteCam.setRotation((float)angle);
            m_RouteCam.setScale(zoom);
            m_RouteCam.setGPSAdjust(gpsAdjust);
            m_RouteCam.update();
            
            zoomLevel = (int)m_RouteCam.getZoomLevel();         
            camBoundingBox = m_RouteCam.getCameraBoundingBox();
                        
            // For all layers
            for(int layerNbr = 0; layerNbr <tmfd.getNumberOfLayers(); layerNbr++ ){
            	
                layerID = tmfd.getLayerIDFromLayerNbr(layerNbr);
                
                /* If the layer isn't visible or if the update time is higher then 0 i.e. the 
                   layer shouldn't be cached we skip to download the layer. */ 
                if(tmfd.getUpdateTimeForLayer(layerID) > 0 || !tmfd.visibleLayer(layerID)) {
                    continue;
                }
                
                newDetailLevel = tmfd.getCurrentDetailLevel(layerID ,zoomLevel);               
                detailLevel = newDetailLevel;
                
                tmfd.getTileIndex(camBoundingBox,layerID,detailLevel,tileIndices);
                nbrImportances = tmfd.getNbrImportances(zoomLevel,detailLevel,layerID);
                
                // Test if the tile indices is the same as the last ones
                if(!sameTileMapsAsPrevious(tileIndices[0], tileIndices[1],tileIndices[2],tileIndices[3],
                		detailLevel,nbrImportances,layerNbr,newDetailLevel)) {
                	
                    // For all tile indices 
                    for(int latIdx=tileIndices[0]; latIdx<=tileIndices[1]; latIdx++) {
                        for(int lonIdx=tileIndices[2]; lonIdx<=tileIndices[3]; lonIdx++) {
                        	
                        	TileMapParams tileIDParam = new TileMapParams();
                        	tileIDParam.setParams(tmfd.getServerPrefix(),
                                    GZIP,
                                    layerID,
                                    TileMapParams.MAP,
                                    0, // Importance 0 used for tile id. 
                                    LangTypes.SWEDISH, // always Swedish for geo data
                                    latIdx, 
                                    lonIdx, 
                                    detailLevel,
                                    m_routeID,
            						"" /*tileID*/);                    
                        	bitbuffer.softReset();
                        	String tileID = tileIDParam.getAsString(bitbuffer);
                        	tileIDParam.setTileID(tileID);
                        	
                        	if(!iAddedWrappers.containsKey(tileID) && !m_TileMapLoader.existInCache(tileIDParam)) {                        	    
                        	    RouteTileWrapper wrapper = new RouteTileWrapper(nbrImportances, tileIDParam);
                                iAddedWrappers.put(tileID, wrapper);
                                
                                m_WrapperCount++;
                                m_ParamStringCount += nbrImportances;
                                if(tmfd.alwaysFetchStrings(layerID)) {
                                    m_ParamStringCount += nbrImportances;
                                }
                            
//                                System.out.println("calculateRouteTiles()"+
//                                      " add new wrapper, tileID= "+tileID+
//                                      " nbrImp= "+nbrImportances+
//                                      " lat= "+tileIDParam.getTileIndexLat()+
//                                      " lon= "+tileIDParam.getTileIndexLon()+
//                                      " layer= "+tileIDParam.getLayerID()+
//                                      " iParamStringCount= "+iParamStringCount);
                        	}                        	
                        }
                    }
                }
            }
             
            
            // Move to the next point in the route.
            iter.nextPoint();
            
            /* Break the loop if we want to split up the iteration of the route */
            if(m_ParamStringCount > MAX_NBR_PARAMS_TO_CALC) {                
                iCurrentDistanceLimit = 
                	    WFUtil.distancePointsMeters(startLat, startLon, lat, lon);
                
                /* Sets the limit when to continue downloading the route to to 100 km before the maps runs out... */
                if(iCurrentDistanceLimit > 100000)
                    iCurrentDistanceLimit -= 100000;
                
                iReadyToCalculateDistanceDiff = true;                
                return;
            }     
        }
        /* if there are no more coordinates to calculate the we don't have to save the iterator 
         * and check if we need to download more tiles */
        iReadyToCalculateDistanceDiff = false;
        iter = null;
        
        if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.calculateRouteTiles()", "Finished calculating route tiles");
        }
    }
    
    // ------------------------------------------------------------------------------
    
    /**
     * 
     * The method returns true if we know for sure that the tile indices are the same as for the previous coordinates.
     * 
     * @param aXMin, min lat (MC2)
     * @param aXMax, max lat (MC2)
     * @param aYMin, min lon (MC2)
     * @param aYMax, max lon (MC2)
     * @param aDetailLevel, current detail level
     * @param aNbrImp, current number of importance
     * @param layerNbr, current layer number
     * @param realDetailLevel, the real new detail level. (not any smooth zoom detail level) 
     * @return
     */
    private boolean sameTileMapsAsPrevious(int xMin, int xMax, int yMin, int yMax, int detailLevel, 
    		int nbrImp, int layerNbr, int realDetailLevel) {                  
        
    	/* Returns always false for any smooth zoom detail level. That's to keep track of what detail level that was the
    	 * latest real detail level. */
    	if(detailLevel != realDetailLevel)
    		return false;
    	
        if(xMin == lastxMin[layerNbr] && xMax == lastxMax[layerNbr] && yMin == lastyMin[layerNbr] && yMax == lastyMax[layerNbr] &&
                detailLevel == lastDetailLevel[layerNbr] && nbrImp == lastNbrImportence[layerNbr]) {                    
            return true;
        } else {
            lastxMin[layerNbr] = xMin;
            lastxMax[layerNbr] = xMax;
            lastyMin[layerNbr] = yMin;
            lastyMax[layerNbr] = yMax;
            lastDetailLevel[layerNbr] = detailLevel;
            lastNbrImportence[layerNbr] = nbrImp;
            return false;
        }
    }

    // -----------------------------------------------------------------------------------
    // Handles tilesmaps that has been send from the server (see TileMapRequestListener)
    
    /*
     * Called when the current outgoing request failed. 
     */
	public void requestFailed(String[] paramStrings) {
	    if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.requestFailed()", "Request failed");
        }
		
	    m_TileMapNetworkHandler.resetExponentialBackoff();
		
		if(m_Interrupted)
			return;
		
		//iRequestedParams.clear();
		if(iIsDownloadingRouteTiles)
			notifyThread();
		
	}
	
	/* Holds the wrappers (one wrapper for each tile and layer) that has been
	 * calculated that we need. */
	private Hashtable iAddedWrappers = new Hashtable();

	/*
	 * (non-Javadoc)
	 * @see com.wayfinder.map.vectormap.control.TileMapRequestListener#requestReceived(java.lang.String, byte[], boolean)
	 */
	public void requestReceived(String paramString, byte[] tiledata, boolean fromCache) {
		
		if(m_Interrupted) {
			return;
		}
		
		TileMapParams param = (TileMapParams)iRequestedParams.remove(paramString);
		if(param == null) {
		    if(LOG.isError()) {
                LOG.error("RouteTileDownloader.requestReceived()", "PARAM==NULL, paramString= "+paramString);
            }
		    return;
		}
		
		RouteTileWrapper wrapper = (RouteTileWrapper)iRequestedWrapper.get(param.getTileID());
		if(wrapper == null) {
		    if(LOG.isError()) {
                LOG.error("RouteTileDownloader.requestReceived()", "WRAPPER==NULL, paramString= "+paramString);
            }
		    return;
		}
		
		wrapper.increaseNumberReceived();
		wrapper.addData(param, tiledata);
		m_NbrReceived++;
		
		/* Check if the tile are complete. */
		if(wrapper.isDone(tmfd.alwaysFetchStrings(param.getLayerID()))) {			
			try {
			    iRequestedWrapper.remove(param.getTileID());
				m_Cache.writeDataToCache(wrapper.getData(), 
										wrapper.getParamsToCache(), 
										param, 
										wrapper.getTotalSize(), 
										wrapper.getNbrOfImpToCache(), 
										(short)-1);	
			} catch (Exception e) {
			    if(LOG.isError()) {
                    LOG.error("RouteTileDownloader.requestReceived()", e);
                }
			}			
			wrapper.clear();
		}
		
		if (LOG.isInfo()) {
            LOG.info("RouteTileDownloader.requestReceived()", "paramString=" + paramString);
        }
		
		// If we have received all paramStrings then we notify that we want to request more 
		if(iRequestedParams.size() == 0 && iIsDownloadingRouteTiles) {
			notifyThread();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.wayfinder.map.vectormap.control.TileMapRequestListener#requestReceived(com.wayfinder.map.vectormap.process.TileMapParams, com.wayfinder.map.vectormap.process.BitBuffer, boolean)
	 */
	public void requestReceived(TileMapParams desc, BitBuffer buf, boolean fromCache) {
//		System.out.println("RouteTileDownloader.requestReceived: FROM PRE-INSTALLED CACHE");				
	}	
	
	// -------------------------------------------------------------------------------------
	
	/**
	 * Return true if we have reached the limit when we need to continue to download route tiles. 
	 */
	boolean hasReachedDistanceLimit(int currentLat, int currentLon) {		
		if(iReadyToCalculateDistanceDiff) {
            /* Calculate the distance from the start point to the current point */
            int distPoint = (int)WFUtil.distancePointsMeters(startLat, startLon, currentLat, currentLon);
            return (distPoint > iCurrentDistanceLimit);
		}
		return false;
	}
	
	DownloadState getDownloadState() {
	    return m_PredState;
	}
	
	
	static final class DownloadState {
	    
	    private boolean iDownloading;
	    
	    synchronized void setDownloading(boolean aDownloading) {
	        iDownloading = aDownloading;
	    }
	    
	    synchronized boolean isDownloading() {
	        return iDownloading;
	    }
	}
}
