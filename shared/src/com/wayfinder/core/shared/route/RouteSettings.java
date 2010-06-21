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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.shared.route;

/**
 * <p>This class holds the settings for the route when making a route request.</p>
 * 
 * <p>It does not control settings that are not connected to a particular route.
 * Examples of such unhandled settings are
 * <ul><li>PTUI (see
 * {@link com.wayfinder.core.shared.settings.GeneralSettings}).</li>
 *     <li>Position accuracy needed for route following.</li>
 *     <li>Fine-tuned control over off-track behaviour.</li>
 * </ul>
 * 
 * 
 *
 */
public final class RouteSettings {
    /*
     * corresponds to the route_settings element in the XML API
     * 
     * <!ELEMENT route_settings ( route_costA?,
     *                            route_costB?,
     *                            route_costC?,
     *                            language )>
     *
     * <!ATTLIST route_settings route_vehicle %route_vehicle_t; #REQUIRED
     *                          avoid_toll_road %bool; #IMPLIED
     *                          avoid_highway %bool; #IMPLIED >
     */
    
    
    /**
     * Signifies that the route is intended to be driven.
     */
    public static final int TRANSPORT_MODE_CAR = 0;

    
    /**
     * Signifies that the route is intended to be walked.
     */
    public static final int TRANSPORT_MODE_PEDESTRIAN = 1;
    
    
    /**
     * Signifies that the route should be optimized so that the shortest route
     * possible will be calculated, regardless of time
     */
    public static final int OPTIMIZE_DISTANCE = 0;
    
    /**
     * Signifies that the route should be optimized so that the fastest route
     * possible will be calculated, regardless of distance
     */
    public static final int OPTIMIZE_TIME     = 1;
    
    /**
     * Signifies that the route should be optimized so that the fastest route
     * possible will be calculated, regardless of distance.
     * <p>
     * This will also take disturbances into account, such as traffic, road
     * conditions and weather.
     * <p>
     * Please note that if the server account has no rights for disturbances,
     * this will be the same as {@link #OPTIMIZE_TIME}
     */
    public static final int OPTIMIZE_TIME_AND_TRAFFIC = 2;
    
    private final int m_transportMode;
    private final boolean m_avoidHighway;
    private final boolean m_avoidTollRoad;
    private final int m_optimization;
    private final boolean m_autoReroute;
    
    /**
     * @param transportMode Transport mode, either TRANSPORT_MODE_CAR or 
     * TRANSPORT_MODE_PEDESTRIAN.
     * @param avoidHighway Set if highways should be penalized when calculating
     * the route.
     * @param avoidTollRoad Set if toll-roads should be penalized when
     * calculating the route
     * @param optimization set what to optimize the route for. One of the
     * OPTMIZE_* constants
     */
    public RouteSettings(int transportMode, boolean avoidHighway,
            boolean avoidTollRoad, int optimization) {
        assertTransportModeValid(transportMode);
        m_transportMode = transportMode;
        assertOptimizationModeValid(optimization);
        m_optimization = optimization;
        m_avoidHighway = avoidHighway;
        m_avoidTollRoad = avoidTollRoad;
        m_autoReroute = true;
    }
    
    /**
     * @param transportMode
     * @param avoidHighway
     * @param avoidTollRoad
     * @param optimization
     * @param autoReroute
     */
    public RouteSettings(int transportMode, boolean avoidHighway,
            boolean avoidTollRoad, int optimization, boolean autoReroute) {
        assertTransportModeValid(transportMode);
        m_transportMode = transportMode;
        m_avoidHighway = avoidHighway;
        m_avoidTollRoad = avoidTollRoad;
        assertOptimizationModeValid(optimization);
        m_optimization = optimization;
        m_autoReroute = autoReroute;
    }
    
    private static void assertTransportModeValid(int transportMode) {
        switch(transportMode) {
        case TRANSPORT_MODE_CAR:
        case TRANSPORT_MODE_PEDESTRIAN:
            // OK!
            break;
            
        default:
            throw new IllegalArgumentException("Invalid transport mode");
        }
    }
    

    private static void assertOptimizationModeValid(int optMode) {
        switch(optMode) {
        case OPTIMIZE_DISTANCE:
        case OPTIMIZE_TIME:
        case OPTIMIZE_TIME_AND_TRAFFIC:
            // OK!
            break;
            
        default:
            throw new IllegalArgumentException("Invalid optimization mode");
        }
    }
    
    
    /**
     * Return the transport mode for which the route is to be calculated.
     * 
     * @return The transport mode for this route, either TRANSPORT_MODE_CAR
     * or TRANSPORT_MODE_PEDESTRIAN
     */
    public int getTransportMode() {
        return m_transportMode;
    }

    /**
     * See if highways can be taken into account when calculating the route.
     * 
     * @return if highways can be taken into account when calculating the route.
     */
    public boolean isAvoidHighway() {
        return m_avoidHighway;
    }

    /**
     * See if toll-roads can be taken into account when calculating the route.
     * 
     * @return if toll-roads can be taken into account when calculating the 
     * route.
     */
    public boolean isAvoidTollRoad() {
        return m_avoidTollRoad;
    }

    /**
     * @return one of the optimization constants.
     * @deprecated misspelled. Use {@link #getOptimization()} instead.
     */
    public int getOptmization() {
        return getOptimization();
    }

    /**
     * Get the route optimization setting.
     * 
     * @return one of the optimization constants.
     */
    public int getOptimization() {
        return m_optimization;
    }

    /**
     * @return the autoReroute
     */
    public boolean isAutoReroute() {
        return m_autoReroute;
    }
}
