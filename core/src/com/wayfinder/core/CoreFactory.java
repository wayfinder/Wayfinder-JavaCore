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
import com.wayfinder.core.favorite.internal.FavoriteModule;
import com.wayfinder.core.geocoding.GeocodeInterface;
import com.wayfinder.core.geocoding.internal.GeocodeModule;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.map.vectormap.internal.VectorMapModule;
import com.wayfinder.core.poiinfo.PoiInfoInterface;
import com.wayfinder.core.poiinfo.internal.PoiInfoModule;
import com.wayfinder.core.positioning.LocationInterface;
import com.wayfinder.core.positioning.internal.LocationModule;
import com.wayfinder.core.route.internal.InternalRouteInterface;
import com.wayfinder.core.route.internal.RouteModule;
import com.wayfinder.core.search.SearchInterface;
import com.wayfinder.core.search.internal.SearchModule;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.sound.NavigationSoundInterface;
import com.wayfinder.core.sound.internal.InternalSoundInterface;
import com.wayfinder.core.sound.internal.SoundModule;
import com.wayfinder.core.sound.internal.navigation.NavigationSoundModule;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.wfserver.WFServerInterface;
import com.wayfinder.core.wfserver.internal.WFServerModule;
import com.wayfinder.core.wfserver.resource.CachedResourceManager;
import com.wayfinder.core.wfserver.resource.internal.CachedResourceManagerImpl;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.mc2.MC2Interface;

public final class CoreFactory {

    
    /**
     * Creates a full instance of the Core, containing all the functionallity.
     * <p>
     * Note that the Core will only be created, nothing more.
     * 
     * @param data A reference to a {@link ModuleData} object with init data
     * for the Core
     * @return A {@link Core} object
     */
    public static Core createFullCore(ModuleData data) {
        if(data == null) {
            throw new IllegalArgumentException("Must provide a valid ModuleData object");
        }
        // setup of logging framework MUST be done before anything else.
        // or bad times are ahead...
        // for release builds, this call will be removed by proguard due to
        // inclusion in assumenosideeffects filter
        //LogFactory.initLogFrameWork(data.getPAL().getLogHandler(), Logger.INFO);
        LogFactory.initLogFrameWork(data.getPAL().getLogHandler(), Logger.DEBUG);
        
        // create objects that will be shared among the modules
        SharedSystems systems = new SharedSystems(data);
        
        // === INTERFACES THAT ARE MOSTLY INTERNAL ===
        
        //to be use as parameter when creating other modules that require it
        //stripped down interface UserDataInterface will be passed to Core ctr.
        InternalUserDataInterface usrDatIfc = systems.getUsrDatIfc();
        //stripped down interface NetworkInterface will be passed to Core ctr.
        InternalNetworkInterface networkIfc = systems.getNetworkIfc();
        MC2Interface mc2Ifc = systems.getMc2Ifc();

        // === PUBLIC INTERFACES ===
        
        SearchInterface searchIfc = SearchModule.createSearchInterface(data, systems);
        LocationInterface locationIfc = LocationModule.createLocationInterface(data, systems);
        FavoriteInterface favoriteIfc = FavoriteModule.createFavoriteInterface(data, systems);
        InternalRouteInterface routeIfc = RouteModule.createRouteInterface(systems, data, networkIfc, mc2Ifc, locationIfc);
        VectorMapInterface vmapIfc = VectorMapModule.createVectorMapInterface(systems, networkIfc);
        WFServerInterface wfsrvIfc = WFServerModule.createWFServerInterface(
                data, networkIfc, mc2Ifc, 
                systems, usrDatIfc);
        
        InternalSoundInterface soundIfc = SoundModule.createSoundInterface(data.getPAL());
        NavigationSoundInterface navigationSoundIfc = NavigationSoundModule.createNavigationInterface(
                soundIfc, routeIfc, systems);
        
        // XXX: haxx to make client work on J2SE       
        // NavigationSoundInterface navigationSoundIfc = null;

        GeocodeInterface geocodeIfc =
            GeocodeModule.createGeocodeInterface(data, systems, mc2Ifc);
        
        PoiInfoInterface poiInfoIfc = 
            PoiInfoModule.createPoiInfoInterface(data, systems, mc2Ifc);
        
        CachedResourceManager cachedRes = 
            CachedResourceManagerImpl.createCachedResourceManager(data, systems, networkIfc);

        return new Core(networkIfc, 
                searchIfc, 
                locationIfc, 
                favoriteIfc, 
                routeIfc, 
                vmapIfc, 
                usrDatIfc, 
                wfsrvIfc,
                systems.getSettingsIfc().getGeneralSettings(),
                soundIfc,
                navigationSoundIfc,
                geocodeIfc, 
                poiInfoIfc,
                cachedRes);
    }
    
    /**
     * Create a standalone instance of the vector map.  
     * <p>
     * 
     * @param data A reference to a {@link ModuleData} object with init data
     * for the Core
     * @return A {@link VectorMapInterface} object
     */
    public static VectorMapInterface createStandaloneMapLib(ModuleData data) {
        
        LogFactory.initLogFrameWork(data.getPAL().getLogHandler(), Logger.WARN);
        
        // create objects that will be shared among the modules
        SharedSystems systems = new SharedSystems(data);
        
        //to be use as parameter when creating other modules that require it
        InternalNetworkInterface networkIfc = systems.getNetworkIfc();
        
        return VectorMapModule.createVectorMapInterface(systems, networkIfc);
    }
    
}
