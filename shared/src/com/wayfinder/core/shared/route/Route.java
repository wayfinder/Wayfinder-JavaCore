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

package com.wayfinder.core.shared.route;

import java.io.IOException;
import java.util.Enumeration;

import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.Position;

/**
 * <p>Abstract base for route info. Contains the data that needs to be made
 * visible to other modules and UI. The route module will extend to a concrete
 * implementation with additional data.</p>
 * 
 * <p>This class is thread-safe as long as the requirement on Vector argument to
 * constructor is fulfilled.</p>
 */
public abstract class Route {
    
    private final String m_routeID;
    private final RoutePointRequestable m_requestedOrigin;
    private final RoutePointRequestable m_requestedDestination;
    private final BoundingBox m_boundingBox;

    /**
     * 
     * @param routeID
     * @param requestedOrigin
     * @param requestedDestination
     * @param boundingBox
     * @param waypoints - the waypoints. At least two elements. Must not be
     *        modified after call.
     */
    protected Route(String routeID,
                    RoutePointRequestable requestedOrigin,
                    RoutePointRequestable requestedDestination,
                    BoundingBox boundingBox) {
        if (routeID == null
            || requestedOrigin == null
            || requestedDestination == null
            || boundingBox == null
            ) {
                throw new IllegalArgumentException();
        }
        m_routeID = routeID;
        m_requestedOrigin = requestedOrigin;
        m_requestedDestination = requestedDestination;
        m_boundingBox = boundingBox;
    }


    /**
     * The ID of the route in the MC2 server.
     * 
     * @return a non-empty string.
     */
    public String getRouteID() {
        return m_routeID;
    }


    // ----------------------------------------------------------------------
    // information on where route begins and ends
    
    /**
     * Return requested origin. Might differ from actual origin.
     * 
     * @return requested route origin that was provided in the
     *         {@link com.wayfinder.core.route.RouteRequest}.     
     */
    public RoutePointRequestable getRequestedOrigin() {
        return m_requestedOrigin;
    }


    /**
     * Return the requested destination. Might differ from actual destination.
     * E.g. when the user clicks on a
     * large building and is routed to the entrance.     
     * 
     * @return The RoutePointRequestable that was provided in the
     *         {@link com.wayfinder.core.route.RouteRequest} 
     */
    public RoutePointRequestable getRequestedDestination() {
        return m_requestedDestination;
    }


    /**
     * 
     * @return a  representing the actual origin.
     */
    public abstract Position getActualOrigin();


    /**
     * @return a RoutePointRequestable representing the actual destination. 
     */
    public abstract Position getActualDestination();


    /**
     * Get a BoundingBox covering the route. You can not rely on getting
     * a separate instance every time. That is only done until there is
     * immutable BoundingBox that we can return.
     * 
     * @return a BoundingBox covering the route
     */
    public BoundingBox getBoundingBox() {
        // BoundingBox is not immutable and must be cloned.
        // BoundingBox(int maxLat, int minLat, int maxLon, int minLon)
        return new BoundingBox(m_boundingBox.getMaxLatitude(),
                               m_boundingBox.getMinLatitude(),
                               m_boundingBox.getMaxLongitude(),
                               m_boundingBox.getMinLongitude());
    }


    // ----------------------------------------------------------------------
    // waypoints and coordinate data.

    /**
     * Convenience method if you don't want to follow the next and prev links
     * in Waypoint.
     * 
     * @return an Enumeration of all Waypoints including the start Waypoint.
     */
    public abstract Enumeration getItinerary();


    /**
     * <p>Returns the first real turn in the route. If the route is
     * really short this might be the Finally instruction.</p>
     * 
     * <p>Note that if the route starts with a uturn this method will return the
     * Waypoint of type {@link Turn#START_WITH_U_TURN}.
     * This is intentional as that is the first action the user should take.</p>
     *
     * @return the first real turn Waypoint.
     */
    public abstract Waypoint getFirstTurnWpt();

    
    /**
     * Returns a new {@link NavigatorRouteIterator} positioned at the first
     * coordinate of the route.
     * 
     * @return a new NavigatorRouteIterator.
     * @throws IOException - if an I/O error occurs which most likely means that
     *         the route has been corrupted.
     */
    public abstract NavigatorRouteIterator getFirstCoordinate()
        throws IOException;


    // ----------------------------------------------------------------------
    // internal and debugging

    /**
     * Return a string representation for debugging use.
     * 
     * @return a string representation for debugging use.
     */
    public String toString() {
        return m_routeID;
    }
}
