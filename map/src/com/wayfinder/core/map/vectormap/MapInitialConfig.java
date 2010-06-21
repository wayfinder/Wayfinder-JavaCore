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

import com.wayfinder.core.map.vectormap.internal.cache.CacheConfiguration;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 * Help class used for setting map specific settings that needs to be set
 * when starting the map. 
 * <p>
 * The dimension of the map are mandatory to set. All other values are 
 * optional to set. 
 * 
 */
public final class MapInitialConfig {
    
    /**
     * Primary cache system. 
     */
    public static final int CACHE_PRIORITY_PRIMARY      = 0;
    
    /**
     * Cache system that will be used if the 
     * {@link MapInitialConfig#CACHE_PRIORITY_PRIMARY} will fail. 
     */
    public static final int CACHE_PRIORITY_SECONDARY    = 1;
    
    private LinkedList m_CacheConfigurations = new LinkedList();

    // start x, start y, width and height of the screen.
    // Mandatory values that has to be passed to the ctor. 
    private int x,y,width,height;
    
    private boolean mSupportPolygons;
    
    // WF Lund
    private int m_StartLat = 664744615;
    private int m_StartLon = 157374366;
    
    // 100 m on the scale ruler 
    private float m_StartZoom = 5.0f;
    
    // Rotation north
    private float m_StartRotation = (float)Math.PI/2;
    
    private String m_Language = "eng";
    
    private int m_gridBgColor = 0xFFb8cdac;
    private int m_gridLineColor = 0xFFb9d6c3;
    private boolean m_enableGrid = true;
    
    private String []m_PreInstalledMapsDirs;
    private PreInstalledMapsListener m_PreInstalledMapsListener; 
    
    private boolean m_EnableRouteTileDownloader = true;
    
    /**
     * 
     * 
     * @param aX the x offset
     * @param aY the y offset
     * @param aWidth the width of the map
     * @param aHeigh the height of the map
     */
    public MapInitialConfig(int aX, int aY, int aWidth, int aHeigh, 
            boolean supportPolygons) {
        x = aX;
        y = aY;
        width = aWidth;
        height = aHeigh;
        mSupportPolygons = supportPolygons;
        
        m_CacheConfigurations.add(
                new CacheConfiguration(CacheConfiguration.TYPE_SECONDARY_CACHE));
        m_CacheConfigurations.add(
                new CacheConfiguration(CacheConfiguration.TYPE_NO_CACHE));
    }
    
    /**
     * Call to this method will enable the file map cache. The map component
     * will try to use the file cache. The cached files will be located in 
     * a folder named <i>mapcache</i> that are located directly under the the
     * base directory specified in {@link PersistenceLayer#getBaseFileDirectory()}.
     * <p>
     * The file cache can be set to two modes:<br>
     * 1. saveCacheContinuously true means that the file cache will be fully saved
     * continuously and not only when the current used cache area become not visible. 
     * This will increase the file i/o access but it might be necessary on platforms
     * when it's not possible to determine when the application is closed. 
     * <p>
     * 2. saveCacheContinuously false means that the cache files will be fully saved
     * when the current used cache area become not visible. To be sure that all data
     * is saved the {@link VectorMapInterface#closeMapComponent()} must be called when
     * the application is closed. 
     * <p>
     * The cache can be fully saved by calling the 
     * {@link MapDetailedConfigInterface#saveCache()} method at any time when
     * the map is started and visible. 
     * 
     * 
     * @param priority if the file cache should be used as the 
     * {@link MapInitialConfig#CACHE_PRIORITY_PRIMARY} or the
     * {@link MapInitialConfig#CACHE_PRIORITY_SECONDARY} cache system.
     * 
     * @param saveCacheContinuously true to save the cache continuously 
     * 
     * @see PersistenceLayer#setBaseFileDirectory(String)
     */
    public void connectFileCache(int priority, boolean saveCacheContinuously) {
        CacheConfiguration cacheConfig = 
            new CacheConfiguration(CacheConfiguration.TYPE_FILE_CACHE, saveCacheContinuously);
        switch (priority) {
            case CACHE_PRIORITY_PRIMARY:
                m_CacheConfigurations.addFirst(cacheConfig);
                break;
                
            case CACHE_PRIORITY_SECONDARY:
                m_CacheConfigurations.add(1, cacheConfig);
                break;

            default:
                break;
        }        
    }
    
    /**
     * Return the list of available cache systems. 
     * 
     * @return the list of available cache systems
     */
    public LinkedList getCacheConfigurations() {
        return m_CacheConfigurations;
    }
    
    /**
     * Method to add pre-installed maps. 
     * 
     * @param folders a array of the full pathname to folder where pre-installed maps should be loaded from.<br>
     * Example {"/sdcard/maps/", "/sdcard/wayfinder/maps/"} 
     * @param listener called when the pre-installed maps has been loaded, can be null. 
     */
    public void addPreInstalledMaps(String []folders, PreInstalledMapsListener listener) {
        if(folders == null)
            throw new IllegalArgumentException("The folders array can't be null!");
        if(folders.length == 0)
            throw new IllegalArgumentException("The folders array can't have zero length!");
        
        m_PreInstalledMapsDirs = new String[folders.length]; 
        System.arraycopy(folders, 0, m_PreInstalledMapsDirs, 0, folders.length);
        m_PreInstalledMapsListener = listener;
    }
    
    public String []internalGetPreInstalledMapsFolders() {
        return m_PreInstalledMapsDirs;
    }
    
    public PreInstalledMapsListener getPreInstalledMapsListener() {
        return m_PreInstalledMapsListener;
    }
    
    /**
     * If enable is true the map will use the route tile downloader function. 
     * This means that when a new route is set via {@link MapDetailedConfigInterface#setRoute(com.wayfinder.core.shared.route.Route)}
     * the map will start calculate which map tiles that will be visible along the current route and start download them. 
     * The map tiles will be saved into the map cache and loaded from there when they are needed. 
     * This will lower the data consumption.  
     * <p>
     * The route tile downloader is false by default. 
     *
     * @param enable true to enable the route tile downloader, false if not.
     * 
     */
    public void enableRouteTileDownloader(boolean enable) {
         m_EnableRouteTileDownloader = enable;
    }
    
    public boolean useRouteTileDownloader() {
        return m_EnableRouteTileDownloader;
    }
    
    /**
     * The background and line color of the grid will be set to the ARGB value specified. 
     * <i>Note that alpha value only will be used if the device support it</i>. 
     * 
     * @param bgColor the background color. 
     * @param lineColor the color of the grid line. 
     */
    public void setGridBackgroundAndLineColors(int bgColor, int lineColor) {
        m_gridBgColor = bgColor;
        m_gridLineColor = lineColor;
    }
    
    /**
     * Set the background grid to be visible or not. The background grid 
     * is default set to be visible.  
     * 
     * @param visible true to enable the grid, false if not. 
     */
    public void setBackgroundGridVisible(boolean visible) {
        m_enableGrid = visible;
    }
    
    public boolean isGridEnabled() {
        return m_enableGrid;
    }
   
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public boolean supportPolygons() {
        return mSupportPolygons;
    }
    
    public void setStartLat(int aLat) {
        m_StartLat = aLat;
    }
    
    public int getStartLat() {
        return m_StartLat;
    }
    
    public void setStartLon(int aLon) {
        m_StartLon = aLon;
    }
    
    public int getStartLon() {
        return m_StartLon;
    }
    
    public void setStartZoom(int aZoom) {
        m_StartZoom = aZoom;
    }
    
    public float getStartZoom() {
        return m_StartZoom;
    }
    
    public void setStartRotation(float aRotation) {
        m_StartRotation = aRotation;
    }
    
    public float getStartRotation() {
        return m_StartRotation;
    }
    
    public void setLanguageAsISO639_3(String aLanguage) {
        m_Language = aLanguage;
    }
    
    public String getLanguageAsISO693_3() {
        return m_Language;
    }
    
    public int getGridBackgroundColor() {
        return m_gridBgColor;
    }
    
    public int getGridLineColor() {
        return m_gridLineColor;
    }
    
}
