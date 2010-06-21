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

package com.wayfinder.core.map.vectormap;

import com.wayfinder.core.map.CopyrightHandler;
import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.Route;

/**
 * Interface for more detailed settings in the map. 
 * 
 */
public interface MapDetailedConfigInterface {
    
    /**
     * Method that allows you to disable server communication. If server
     * communication are disabled the map will load map tiles from the cache and
     * any pre-installed maps added to the map. 
     * 
     * @param shouldBeOffline true to disable server communication, false to 
     * allow server communication. 
     */
    public void setOfflineMode(boolean shouldBeOffline); 
    
    /**
     * Call to this method will set the route id in the map, i.e. the red line
     * will be visible on top of the map. 
     * <p>
     * Call to this method will not effect the route tile downloader function. It
     * will only enable or disable the red line in the map. 
     * 
     * @param routeID for the route to be shown or null to remove the red line. 
     * @return true if the current setting is used.
     */
    public boolean setRouteID(String routeID);
    
    /**
     * Call to this method will enable the route in the map. The red line for 
     * the route specified by the parameter will be visible on the map. 
     * 
     * <p>
     * If the route tile downloader is enabled via
     * {@link MapInitialConfig#enableRouteTileDownloader(boolean)}
     * the map will start to download the map tiles needed for the route and
     * save them in the map cache. 
     * 
     * <p>
     * If route is null the current used route will be removed from the map.
     * If no route has been set, the call will be ignored.
     * 
     * @param route the route that should be used or null to remove the route. 
     * @return true if the current setting is used.
     */
    public boolean setRoute(Route route); 
    
    /**
     * Set the current active navigation info object to the map. 
     * <p>
     * 
     * @param navInfo
     */
    public void setNavigationInfo(NavigationInfo navInfo);
    
    /**
     * Return the current used RouteID or null if no route is available. 
     * 
     * @return the current used RouteID or null if no route is available. 
     */
    public String getRouteID();
    
    /**
     * Returns the server string to be sent to the server when more information
     * about the current active coordinate is to be requested. 
     * <p>
     * The string is only valid until the next call to this method. 
     * <p>
     * Empty string means no active object is selected in the map. 
     * 
     * @return the server string or a empty string if no information can be found. 
     */
    public String getServerString();
    
    /**
     * Returns the server string to be sent to the server when more information
     * about the a map feature is to be requested. 
     * 
     * @param lat the latitude in MC2
     * @param lon the longitude in MC2
     * @param name the name of the feature
     * @param isPolygon true if the feature is a polygon, false if not. 
     * @return the server string for the information specified by the parameters. 
     */
    public String getServerString(int lat, int lon, String name, boolean isPolygon);
    
    /**
     * Set the position on the y-axis of the copyright text. The text
     * will be centered in the x-axis.  
     * 
     * @param y the top y postion of the copyright text. 
     */
    public void setCopyrightTextPositionY(int y);
    
    /**
     * Return the current used copyright handler from the map. 
     * 
     * @return the current used copyright handler from the map.
     */
    public CopyrightHandler getCopyrightHandler();
    
    
    /**
     * Set the ACP mode setting in the map. If the ACP poi layer doesn't exist
     * for the user, calls to this method will be ignored. 
     * 
     * @param aEnabled true to enable ACP pois, false if not. 
     */
    public void setDownloadACPEnabled(boolean aEnabled);
    
    
    /**
     * Switches visibility of POIs originating from the server. This will not
     * affect POIs added by the client 
     * (via {@link VectorMapInterface#addMapObject(MapObject, 
     * com.wayfinder.core.map.MapObjectImage)}).
     * 
     * @param isVisible true if POIs originating from the server should be
     * visible, false if they should be hidden.
     */
    public void setServerPOIsVisible(boolean isVisible);
    
    
    /**
     * Sets the visibility of the traffic info layer in the map. If the layer 
     * doesn't exist, calls to this method will be ignored.  
     * 
     * @param aIsVisible  true if the traffic info layer should be visible, false if not
     */
    public void setTrafficLayerVisible(boolean aIsVisible);
    
    
    /**
     * 
     * Set the update time for the traffic layer. If the traffic info layer 
     * doesn't exist, calls to this method will be ignored.
     * <p>
     * Update time 0 means that the traffic info layer won't be updated. 
     * 
     * @param aMinutes the update time in minutes.  
     */
    public void setTrafficInfoUpdateTime(int aMinutes);
    
    /**
     * Call to this method will trigger the map to be fully saved. The map 
     * must be started and set to be visible when this method is called. 
     * 
     * @return true if the cache is saved, false if not. 
     */
    public boolean saveCache();
    
    /**
     * Return a array of the poi categories available in the map. The information
     * available is the name of the category and a boolean that indicate if the poi
     * category is visible or not. A POI category can contain one or more poi types.  
     * <p>
     *
     * @return a array of the poi categories available in the map or null if no categories can be found. 
     * @see PoiCategory
     */
    public PoiCategory[] getPoiCategories();
    
    /**
     * Set a new configuration of the visibility of the poi categories.  
     * 
     * @param poiCategories the modified array of poi categories that has been retrieved via
     * the {@link MapDetailedConfigInterface#getPoiCategories()} method. 
     */
    public void setPoiCategories(PoiCategory []poiCategories);

}
