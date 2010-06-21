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

package com.wayfinder.core.route;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.RoutePointRequestable;
import com.wayfinder.core.shared.route.RouteSettings;

/**
 * <p>Encapsulation of a request for a route between origin A and destination
 * B.</p>
 * 
 * <p>Currently supports no other settings than transport mode.</p>
 */
public final class RouteRequest {
    // TODO: we might need to extend this class with an internal counterpart
    // later. But for now it can be final.
    
    //FIXME Should all of these really be exposed outside of the API?
    // Like traffic info update and truncated route?
    
    /**
     * Reroute is requested for unknown reason.
     */
    public static final int REASON_UNKNOWN = 0;

    /**
     * Reroute requested because the route was truncated and we need the next
     * part.
     */    
    public static final int REASON_TRUNCATED_ROUTE = 1;

    /**
     * Reroute requested because we went off track and need a new route from
     * the current position. 
     */    
    public static final int REASON_OFF_TRACK = 2;

    /**
     * Reroute requested to check if traffic information along the remaining
     * part of the route has changed. 
     */        
    public static final int REASON_TRAFFIC_INFO_UPDATE = 3;

    /**
     * Reroute requested because the user pressed some button to reroute. 
     */            
    public static final int REASON_USER_REQUEST = 4;
    
    private final RoutePointRequestable m_origin;
    private final RoutePointRequestable m_destination;
    
    private final Route m_currentRoute;
    private final int m_rerouteReason;
    
    private final RouteSettings m_routeSettings;
    
    
    /**
     * Private constructor. Use the factory method instead.
     * 
     * @param origin - see getRequestedOrigin()
     * @param destination - see getRequestedDestination()
     * @param transportMode - one of the TRANSPORT_* constants in this class
     * @param currentRoute - the current route, when requesting a reroute
     * @param rerouteReason - the reason for reroute, use the REASON_* constants
     */
    private RouteRequest(RoutePointRequestable origin,
            RoutePointRequestable destination, RouteSettings settings,
            Route currentRoute, int rerouteReason) {
        super();
        
        // data validation
        if(origin == null) {
            throw new IllegalArgumentException("Origin cannot be null");
        } else if(destination == null) {
            throw new IllegalArgumentException("Destination cannot be null");
        } else if(settings == null) {
            throw new IllegalArgumentException("Settings cannot be null");
        } else if(currentRoute != null) {
            // check valid reroute reason
            switch(rerouteReason) {
            case REASON_OFF_TRACK:
            case REASON_TRAFFIC_INFO_UPDATE:
            case REASON_TRUNCATED_ROUTE:
            case REASON_UNKNOWN:
            case REASON_USER_REQUEST:
                // OK!
                break;
            
            default:
                throw new IllegalArgumentException("Invalid reroute reason");
            }
        }
        
        m_origin = origin;
        m_destination = destination;
        m_routeSettings = settings;
        m_currentRoute = currentRoute;
        m_rerouteReason = rerouteReason;
    }
    
    
    /**
     * Creates a new route request.
     * 
     * @param origin See doc entry for {@link #getRequestedOrigin()}
     * @param destination See doc entry for {@link #getRequestedDestination()}
     * @param settings a {@link RouteSettings}
     * 
     * @return A {@link RouteRequest}
     */
    public static RouteRequest createRequest(
                                RoutePointRequestable origin,
                                RoutePointRequestable destination,
                                RouteSettings settings) {
        
        return new RouteRequest(origin, destination, settings, null, REASON_UNKNOWN);
    }
    
    
    /**
     * Creates a request for a reroute
     * 
     * @param origin Usually the current {@link Position} when navigating
     * @param currentRoute The current route
     * @param settings a {@link RouteSettings}
     * @param rerouteReason One of the REASON_* constants in this class
     * 
     * @return A {@link RouteRequest}
     */
    public static RouteRequest createRerouteRequest(
            RoutePointRequestable origin,
            Route currentRoute,
            RouteSettings settings,
            int rerouteReason) {
        
        if(currentRoute == null) {
            throw new IllegalArgumentException("Old route must be provided");
        }
        // rest checked in ctor...
        
        return new RouteRequest(
                origin, 
                currentRoute.getRequestedDestination(),
                settings,
                currentRoute,
                rerouteReason);
    }
    
    
    /**
     * Return requested origin. Might differ from actual origin.
     * 
     * @return The RoutePointRequestable that was provided to constructor.
     */
    public RoutePointRequestable getRequestedOrigin() {
        return m_origin;
    }
    
    /**
     * <p>Return the requested destination. Might differ from actual
     * destination. 
     * E.g. when the user clicks on a
     * large building and is routed to the entrance.     
     * 
     * @return The RoutePointRequestable that was provided to constructor.
     */
    public RoutePointRequestable getRequestedDestination() {
        return m_destination;
    }
    
    /**
     * Return the route settings.
     * 
     * @return The RouteSettings that was provided to the constructor.
     */
    public RouteSettings getRouteSettings() {
        return m_routeSettings;
    }

    /**
     * Return the current route, used when requesting a reroute.
     * 
     * @return The current route, or <b>null</b> if this request is for a fresh
     * route
     */
    public Route getRoute() {
        return m_currentRoute;
    }

    /**
     * Return the reroute reason string to be used in the route request for a 
     * reroute.
     * 
     * @return The reroute reason as it will appear in the XML route request, or
     * the <b>empty string</b> if this is a fresh route.
     */
    public int getRerouteReason() {
        return m_rerouteReason;
    }
    
    /**
     * Check if this is a reroute request
     * 
     * @return true if this is a reroute request
     */
    public boolean isReroute() {
        return m_currentRoute != null;
    }
        
}
