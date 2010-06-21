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

package com.wayfinder.core.route.internal.nav2route;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.route.NavigatorRouteIterator;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.RoutePointRequestable;
import com.wayfinder.core.shared.route.Turn;
import com.wayfinder.core.shared.route.Waypoint;


/**
 * Extends {@link Route} to to an implementation used within the nav2
 * route following system. It is not intended for use outside the
 * com.wayfinder.core.route.internal-hierarchy.
 * 
 * <p>This class is thread-safe.</p>
 */
public final class Nav2Route extends Route {

    /**
     * The raw navigator route. Do not modify (hence package protected).
     */
    final byte[] m_rawRoute;

    /**
     * The string table from the route.
     * 
     * TODO: add protection by privatization and a get method?
     */
    final Vector m_stringTable;

    /**
     * The first {@link MiniMap} of the route.
     */
    final MiniMap m_firstMiniMap;

    /**
     * The {@link WaypointImpl}s of the route.
     */
    private final Vector m_waypoints;

    /**
     * The {@link Landmark}s of the route.
     */
    private final Vector m_landmarks;
    
    private final Waypoint m_firstTurnWpt;

    private final Position m_actualOrigin;

    private final Position m_actualDestination; 


    /**
     * Creates a new Nav2Route.
     * 
     * @param routeID
     * @param requestedOrigin
     * @param requestedDestination
     * @param boundingBox
     * @param waypoints
     */
    Nav2Route(String routeID,
              RoutePointRequestable requestedOrigin,
              RoutePointRequestable requestedDestination,
              BoundingBox boundingBox,
              Vector waypoints,
              Vector landmarks,
              byte[] rawRoute,
              Vector stringTable,
              MiniMap firstMiniMap) {
        super(routeID, requestedOrigin, requestedDestination, boundingBox);
        if (waypoints == null
            || waypoints.size() < 2) {
            throw new IllegalArgumentException();
        }

        m_waypoints = waypoints;
        Waypoint start = ((Waypoint) m_waypoints.elementAt(0));
        m_actualOrigin = start.getPosition();
        m_actualDestination = ((Waypoint) m_waypoints
                                          .elementAt(m_waypoints.size() - 1))
                              .getPosition();
        if (start.getTurn() == Turn.START_WITH_U_TURN) {
            m_firstTurnWpt = start;
        } else {
            m_firstTurnWpt = start.getNext();
        }

        m_landmarks = landmarks;
        m_rawRoute = rawRoute;
        m_stringTable = stringTable;
        m_firstMiniMap = firstMiniMap;
        
    }


    /**
     * Returns {@link Waypoint} k in the route. This is the interface for
     * the {@link Nav2RouteIterator}.
     * 
     * @throws ArrayIndexOutOfBoundsException if k is invalid.
     */
    WaypointImpl getWpt(int k) {
        // exception throwing guaranteed by Vector.elementAt()
        return (WaypointImpl) m_waypoints.elementAt(k);
    }

    /**
     * Get a new {@link Nav2RouteIterator} positioned at the start of the route. 
     */
    public Nav2RouteIterator newIterator() throws IOException {
        return new Nav2RouteIterator(this);
    }

    /**
     * Get a new {@link Nav2RouteIterator2} positioned at the start of the route. 
     */
    public Nav2RouteIterator2 newIterator2() throws IOException {
        return new Nav2RouteIterator2(this);
    }

    /**
     * Returns the landmarks. Used by route follower to update information
     * about valid landmarks. This is not the best design since the vector
     * is mutable, but this class is not visible outside of internals of the
     * route module.
     */
    public Vector getLandmarks() {
        return m_landmarks;
    }


    /**
     * See the contract for {@link Route#getFirstCoordinate()}.
     */
    public NavigatorRouteIterator getFirstCoordinate()
        throws IOException {

        return newIterator2();
    }

    /**
     * See the contract for {@link Route#getItinerary()}.
     */
    public Enumeration getItinerary() {
        // the enumeration is read-only.
        return m_waypoints.elements();
    }

    /**
     * See the contract for {@link Route#getFirstTurnWpt()}.
     */
    public Waypoint getFirstTurnWpt() {
        return m_firstTurnWpt;
    }

    /**
     * See the contract for {@link Route#getActualOrigin()}.
     */
    public Position getActualOrigin() {
        return m_actualOrigin;
    }

    /**
     * See the contract for {@link Route#getActualDestination()}.
     */
    public Position getActualDestination() {
        return m_actualDestination;
    }
}
