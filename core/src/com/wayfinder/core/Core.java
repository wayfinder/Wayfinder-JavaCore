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

package com.wayfinder.core;

import com.wayfinder.core.favorite.FavoriteInterface;
import com.wayfinder.core.geocoding.GeocodeInterface;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.network.NetworkInterface;
import com.wayfinder.core.poiinfo.PoiInfoInterface;
import com.wayfinder.core.positioning.LocationInterface;
import com.wayfinder.core.route.RouteInterface;
import com.wayfinder.core.search.SearchInterface;
import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.core.sound.NavigationSoundInterface;
import com.wayfinder.core.sound.SoundInterface;
import com.wayfinder.core.userdata.UserDataInterface;
import com.wayfinder.core.wfserver.WFServerInterface;
import com.wayfinder.core.wfserver.resource.CachedResourceManager;


/**
 * Collection of all major interfaces available in the Core.
 */
public final class Core {

    private final NetworkInterface m_networkIfc;
    private final SearchInterface m_searchIfc;
    private final LocationInterface m_locationIfc;
    private final FavoriteInterface m_favoriteIfc;
    private final RouteInterface m_routeIfc;
    private final VectorMapInterface m_vectorMapIfc;
    private final UserDataInterface m_userDatIfc;
    private final WFServerInterface m_wfsrvIfc;
    private final GeneralSettings m_generalSettings;
    private final SoundInterface m_soundIfc;
    private final NavigationSoundInterface m_navigationSoundIfc;
    private final GeocodeInterface m_geocodeIfc;
    private final PoiInfoInterface m_poiInfoIfc;
    private final CachedResourceManager m_cachedRes;
    

    /**
     * Standard constructor.
     * 
     * @param networkIfc The main {@link NetworkInterface} 
     * @param searchIfc The main {@link SearchInterface}
     * @param locationIfc The main {@link LocationInterface}
     * @param favoriteIfc The main {@link FavoriteInterface}
     * @param routeIfc The main {@link RouteInterface}
     * @param soundIfc The main {@link SoundInterface}
     * @param navigationSoundIfc The main {@link NavigationSoundInterface}
     * @param geocodeIfc The main {@link geocodeIfc}
     * @param poiInfoIfc The main {@link PoiInfoInterface}
     * 
     */
    Core(NetworkInterface networkIfc, SearchInterface searchIfc,
         LocationInterface locationIfc, 
         FavoriteInterface favoriteIfc,
         RouteInterface routeIfc,
         VectorMapInterface vectorMapIfc,
         UserDataInterface userIfc,
         WFServerInterface wfsrvIfc,
         GeneralSettings settings, 
         SoundInterface soundIfc, 
         NavigationSoundInterface navigationSoundIfc,
         GeocodeInterface geocodeIfc, 
         PoiInfoInterface poiInfoIfc,
         CachedResourceManager cachedRes) {
        
        m_networkIfc = networkIfc;
        m_searchIfc = searchIfc;
        m_locationIfc = locationIfc;
        m_favoriteIfc = favoriteIfc;
        m_routeIfc = routeIfc;
        m_vectorMapIfc = vectorMapIfc;
        m_userDatIfc = userIfc;
        m_wfsrvIfc = wfsrvIfc;
        m_generalSettings = settings;
        m_soundIfc = soundIfc;
        m_navigationSoundIfc = navigationSoundIfc;
        m_geocodeIfc = geocodeIfc;
        m_poiInfoIfc = poiInfoIfc;
        m_cachedRes = cachedRes;
    }
    
    /**
     * Returns the {@link GeneralSettings} object.
     * <p>
     * This object allows the application to set things that affect the Core
     * on a server-level, such as the language used when putting together the
     * results in a reply.
     * 
     * @return The global {@link GeneralSettings} object
     */
    public GeneralSettings getGeneralSettings() {
        return m_generalSettings;
    }
    
    
    /**
     * Returns the main interface for the search functionality in the Java
     * Core
     * 
     * @return A reference to the core {@link SearchInterface}
     */
    public SearchInterface getSearchInterface() {
        return m_searchIfc;
    }
    
    
    /**
     * Returns the main interface for the positioning functionality in the
     * Java Core.
     * 
     * @return A reference to the core {@link LocationInterface}.
     */
    public LocationInterface getLocationInterface() {
        return m_locationIfc;
    }

    
    /**
     * Returns the main interface for the favorite functionality in the
     * Java Core.
     * 
     * @return A reference to the core {@link FavoriteInterface}.
     */
    public FavoriteInterface getFavoriteInterface() {
        return m_favoriteIfc;
    }


    /**
     * Returns the main interface for routing and navigation functionality in
     * the Java Core.
     * 
     * @return A reference to the core {@link RouteInterface}.
     */
    public RouteInterface getRouteInterface() {
        return m_routeIfc;
    }
    
    /**
     * Returns the main interface for networking state functionality in
     * the Java Core.
     * 
     * @return A reference to the core {@link NetworkInterface}.
     */
    public NetworkInterface getNetworkInterface() {
        return m_networkIfc;
    }
    
    /**
     * Returns the main interface for the vector map functionality in the
     * Java Core.  
     * 
     * @return A reference to the core {@link VectorMapInterface}.
     */
    public VectorMapInterface getVectorMapInterface() {
        return m_vectorMapIfc;
    }
    
    
    /**
     * Returns the main interface for activation and user data details
     * 
     * @return A reference to the core {@link UserDataInterface}.
     */
    public UserDataInterface getUserDataInterface() {
        return m_userDatIfc;
    }
    
    
    /**
     * Returns the main interface for the functionality that involves direct
     * interaction with the Wayfinder server.
     * 
     * @return A reference to the core {@link WFServerInterface}
     */
    public WFServerInterface getWfServerInterface() {
        return m_wfsrvIfc;
    }
    
    
    /**
     * Returns the main interface for controlling the sound systems on a
     * general level
     * 
     * @return A reference to the {@link SoundInterface}
     */
    public SoundInterface getSoundInterface() {
        return m_soundIfc;
    }

    
    /**
     * Returns the main interface for controlling the sounds played during
     * navigation
     * 
     * @return A reference to the {@link NavigationSoundInterface}
     */
    public NavigationSoundInterface getNavigationSoundInterface() {
        return m_navigationSoundIfc;
    }
    

    /**
     * Returns the main interface for geocoding and reverse geocoding.
     * 
     * @return A reference to the core {@link GeocodeInterface}.
     */
    public GeocodeInterface getGeocodeInterface() {
        return m_geocodeIfc;
    }
    
    
    /**
     * Returns the main interface for obtaining detailed information about
     * Points of interest (like the results returned in the search)
     * 
     *  @return A reference to the {@link PoiInfoInterface}
     */
    public PoiInfoInterface getPoiInfoInterface() {
        return m_poiInfoIfc;
    }
    
    
    /**
     * Returns the main interface for obtaining resources stored on the 
     * Wayfinder servers
     * 
     * @return The {@link CachedResourceManager}
     */
    public CachedResourceManager getCachedResourceManager() {
        return m_cachedRes;
    }
}
