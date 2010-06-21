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

import com.wayfinder.core.shared.Position;

/**
 * <p>A point in the route where the user needs to take action, including the
 * start and end of the route.</p>
 * 
 * <p>In the MC2 XML API, this corresponds to a route_reply_item.</p>
 * 
 * <p>In the navigator route format, as specified in "Route data V1.08",
 * this corresponds to a turn point which is
 * a nav_route_point which is not a nav_route_point_delta.</p>
 * 
 * <p>The crossing information is not parsed or provided. The reason for this is
 * that the crossing information in the navigator route is very limited and
 * that is was not used in jWMMG-code. Turn pictograms where selected solely
 * on turn type, not taking crossing information into account.</p>
 * 
 * <p>This class is thread safe.</p>
 * 
 * <p>In jWMMG sources, this was implemented by class
 * <code>wmmg.data.route.RouteReplyItem.</code></p>
 */
public abstract class Waypoint {

    private final boolean m_driveOnRightSideBefore;

    private final Position m_position;
    private final Turn m_turn;
    private final int m_exitCount;

    private final int m_distanceMetersToEnd;
    private final int m_timeSecondsToEnd;
    private final String m_roadNameAfter;
    private final int m_speedLimitKmhAfter;


    /**
     * Creates a new Waypoint.
     * 
     * @param position - the position of the Waypoint.
     * @param turn - the type of turn = the action the user should take here.
     * @param driveOnRightSideBefore -
     *        see {@link Waypoint#getDriveOnRightSideBefore()}.
     * @param exitCount -
     *        see {@link Waypoint#getExitCount()}.
     * @param distanceMetersToEnd -
     *        see {@link Waypoint#getDistanceMetersToEnd()}.
     * @param timeSecondsToEnd -
     *        see {@link Waypoint#getTimeSecondsToEnd()}.
     * @param roadNameAfter -
     *        see {@link Waypoint#getRoadNameAfter()}
     * @param speedLimitKmhAfter -
     *        see {@link Waypoint#getSpeedLimitKmhAfter()}.
     * 
     */
    protected Waypoint(boolean driveOnRightSideBefore,                       
                       Position position,
                       Turn turn,
                       int exitCount,
                       int distanceMetersToEnd,
                       int timeSecondsToEnd,
                       String roadNameAfter,
                       int speedLimitKmhAfter
                       ) {
        if (position == null
            || turn == null
            || exitCount < 0
            || distanceMetersToEnd < 0
            || timeSecondsToEnd < 0
            || roadNameAfter == null
            || speedLimitKmhAfter <= 0
            ) {
            throw new IllegalArgumentException();
        }

        m_position = position;
        m_turn = turn;
        m_distanceMetersToEnd = distanceMetersToEnd;
        m_speedLimitKmhAfter = speedLimitKmhAfter;
        m_timeSecondsToEnd = timeSecondsToEnd;
        m_roadNameAfter = roadNameAfter;
        m_exitCount = exitCount;
        m_driveOnRightSideBefore = driveOnRightSideBefore;
    }
    

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // data for the path leading up to the turn
    
    /**
     * Returns the distance in meters from previous Waypoint.
     * 
     * @return an integer >= 0.
     */
    public abstract int getDistanceMetersFromPrev();

    /**
     * <p>Returns whether to drive on the right hand side or not on the road
     * leading up to the Waypoint.</p>
     * 
     * <p>It is more interesting to know the state
     * before than after, because if the pictogram contains an image of a road
     * or is offset to left/right edge, we want to display different pictogram
     * for right and left hand side traffic.</p>
     * 
     * @return true if the user should drive on the right hand side, otherwise
     *         false.
     */
    public boolean getDriveOnRightSideBefore() {
        // take the attribute from the previous flag field of a nav_route_point
        // while parsing. This is done wrong in jWMMG but can be blamed on the
        // attribute being ill-defined in our code. Possibly better in XML API
        // documentation.
        return m_driveOnRightSideBefore;
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // data for the actual turn.

    /**
     * @return the {@link Position} of the Waypoint. 
     */
    public Position getPosition() {
        return m_position;
    }

    /**
     * @return the {@link Turn} of the Waypoint.
     */
    public Turn getTurn() {
        return m_turn;
    }

    /**
     * <p>Return the exit count.</p>
     * 
     * <p>This is primarily of interest when we have roundabouts with more
     * than 4 exits  because then exit count indicates which exit to
     * take. It's also set for instructions like "Drive 1.1 km then
     * turn right into the 6:th street Gunnersbury Lane (A4000)" but
     * then there is really nothing useful to do with it.</p>
     * 
     * @return an integer >= 0.
     */
    public int getExitCount() {
        return m_exitCount;
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // data for the road after the turn.
    
    /**
     * Returns the distance in meters between this Waypoint and the end
     * of the route.
     * 
     * @return an integer >=0.
     */
    public int getDistanceMetersToEnd() {
        return m_distanceMetersToEnd;
    }

    /**
     * <p>Returns the estimated time in seconds to travel the rest of the
     * route.</p>
     * 
     * <p>This is calculated by the server when routing and thus the user's
     * current speed is not taken into account.</p>
     * 
     * @return an integer >=0.
     */
    public int getTimeSecondsToEnd() {
        return m_timeSecondsToEnd;
    }
        
    /**
     * Returns the name of the road that you enter after this turn. If the name
     * is not known, the empty string is returned.
     * 
     * @return the road name, never null.
     */
    public String getRoadNameAfter() {
        return m_roadNameAfter;
    }

    /**
     * Returns the speed limit in km/h after the turn.
     * This is used for smooth zooming in maps.
     * 
     * TODO: document default. And who's responsible for setting it.
     * 
     * @return an integer > 0.
     */
    public int getSpeedLimitKmhAfter() {
        return m_speedLimitKmhAfter;
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // prev/next

    /**
     * @return the next Waypoint in the itinerary or null if this is the final
     * Waypoint.
     */
    public abstract Waypoint getNext();


    /**
     * Returns the next Waypoint in the itinerary or null if this is the first
     * Waypoint. The first Waypoint is the start waypoint. Thus this method
     * returns null if and only if this == Route.getFirstTurnWpt().getPrev().
     * 
     * @return the previous Waypoint in the itinerary or null if this is the
     * first Waypoint.
     */
    public abstract Waypoint getPrev();
}
