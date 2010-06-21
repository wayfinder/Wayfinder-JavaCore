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
 * <p>Container class representing current state of navigation with regards to
 * to a route and the latest position the navigation system has processed.</p>
 * 
 * <p>If <code>isOffTrack() == true</code>, <u>only</u> the following methods
 * have defined return values. So, check <code>isOffTrack()</code> first
 * in your UI updating code.   
 * <ol><li>{@link NavigationInfo#getRoute()}
 *     <li>{@link NavigationInfo#getEvaluatedPosition()}
 *     <li>{@link NavigationInfo#isDestinationReached()}
 *     <li>{@link NavigationInfo#isOfftrack()}
 * </ol></p>
 *     
 * <p>This class is thread-safe.</p>     
 */
public class NavigationInfo {
    
    /*
     * See also C++/Targets/Nav2API/Export/include/Shared/UpdateNavigationInfo.h 
     */
    
    private final Position m_evaluatedPosition;
    private final float m_speed;

    private final Route m_route;
    private final boolean m_isDestinationReached;
    private final boolean m_isOffTrack;
    private final boolean m_isFollowing;

    private final String m_streetName;
    private final boolean m_isOnDetour;
    private final boolean m_isSpeedCameraActive;
    private final int m_speedLimitKmh;

    private final Waypoint m_nextWpt;
    private final int m_distanceMetersToNextWpt;

    private final int m_distanceMetersTotalRemaining;
    private final int m_timeSecondsTotalRemaining;

    private final Position m_snappedPosition;
    private final int m_snappedCourseDeg;

    private final Position m_fakedPosition;
    private final int m_fakedCourseDeg;
    

    /**
     * Creates a new NavigationInfo.
     * 
     * @param route
     * @param evaluatedPosition
     * @param isDestinationReached
     * @param isOffTrack
     * @param isFollowing
     * @param streetName
     * @param isOnDetour
     * @param isSpeedCameraActive
     * @param speedLimitKmh
     * @param nextWpt
     * @param distanceMetersToNextWpt
     * @param distanceMetersTotalRemaining
     * @param timeSecondsTotalRemaining
     * @param snappedPosition
     * @param snappedCourseDeg
     * @param speed (in m/s)
     * @param fakedPosition value to be returned by {@link #getFakedPosition()}
     * @param fakedCourseDeg value to be returned by {@link #getFakedCourseDeg()}
     */
    public NavigationInfo(Route route,
                          Position evaluatedPosition,
                          boolean isDestinationReached,
                          boolean isOffTrack,

                          boolean isFollowing,
                          String streetName,
                          boolean isOnDetour,
                          boolean isSpeedCameraActive,

                          int speedLimitKmh,
                          Waypoint nextWpt,

                          int distanceMetersToNextWpt,
                          int distanceMetersTotalRemaining,

                          int timeSecondsTotalRemaining,
                          Position snappedPosition, 
                          int snappedCourseDeg, 
                          float speed,

                          Position fakedPosition,
                          int fakedCourseDeg) {

        m_route = route;
        m_evaluatedPosition = evaluatedPosition;
        m_speed = speed;
        
        m_isDestinationReached = isDestinationReached; 
        m_isOffTrack = isOffTrack;
        m_isFollowing = isFollowing;

        m_streetName = streetName;
        m_isOnDetour = isOnDetour;
        m_isSpeedCameraActive = isSpeedCameraActive; 
        m_speedLimitKmh = speedLimitKmh;

        m_nextWpt = nextWpt;
        m_distanceMetersToNextWpt = distanceMetersToNextWpt; 

        m_distanceMetersTotalRemaining = distanceMetersTotalRemaining; 
        m_timeSecondsTotalRemaining = timeSecondsTotalRemaining;

        m_snappedPosition = snappedPosition;
        m_snappedCourseDeg = snappedCourseDeg;
        
        m_fakedPosition = fakedPosition;
        m_fakedCourseDeg = fakedCourseDeg; 
    }


    /**
     * Creates a new NavigationInfo indicating an off-track condition. 
     * @param route the route from which we are off-track.
     * @param evaluatedPosition the position that was evaluated.
     * @param speed speed in in m/s.
     * @return a new NavigationInfo object.
     */
    public static NavigationInfo createForOffTrack(Route route,
                                                   Position evaluatedPosition, 
                                                   float speed) {
        return new NavigationInfo(route,
                                  evaluatedPosition,
                                  false,        // boolean isDestinationReached
                                  true,         // boolean isOffTrack
                                  true,         // boolean isFollowing
                                  "",           // String streetName
                                  false,        // boolean isOnDetour
                                  false,        // boolean isSpeedCameraActive
                                  1,            // int speedLimitKmh (> 0)
                                  null,         // Waypoint nextWpt
                                  0,            // int DistanceMetersToNextWpt
                                  0,            // int distanceMetersTotalRemaining
                                  0,            // int timeSecondsTotalRemaining,
                                  null,         // Position snappedPosition,
                                  0,            // int snappedCourseDeg
                                  speed,        // speed
                                  null,         // fakedPosition
                                  0             // fakedCourseDeg
                                  );
    }
    

    // ------------------------------------------------------------------
    // information about the position used to calculate this NavigationInfo 
    
    /**
     * Returns the position that was used to calculate the location in the
     * route. This can be null. One example is when a new route is followed and
     * the system reports as if the user was positioned at the start of the
     * first segment.
     * 
     * @return the evaluated position.
     */
    public Position getEvaluatedPosition() {
        return m_evaluatedPosition;
    }

    /**
     * <p>Returns the speed (in m/s) that was reported together with the
     * position evaluated (see <code>getEvaluatedPosition()</code>).</p>
     * 
     * <p>The speed can be 0 if speed info was not reported from the GPS.</p>
     * 
     * @return the speed in m/s.
     * @see com.wayfinder.core.positioning.LocationInformation#getSpeed()
     */
    public float getSpeed() {
        return m_speed;
    }


    // ------------------------------------------------------------------
    // basic information about current route 

    /**
     * @return the route that we evaluated.
     */
    public Route getRoute() {
        return m_route;
    }

    /**
     * <p>Checks if the destination was reached.</p>
     * 
     * <p>If the destination is reached,
     * the route following will pause and not evaluate any more positions or
     * re-route if becoming off-track.
     * The reason for this is to avoid repeated off-tracks
     * when the the user is searching for a parking spot near the
     * destination.</p>
     * 
     * @return true if destination was reached, otherwise false.
     */
    public boolean isDestinationReached() {
        return m_isDestinationReached;
    }

    /**
     * Checks if the we are off-track.
     * 
     * @return true if we are off-track; otherwise false. 
     */
    public boolean isOfftrack() {
        return m_isOffTrack;
    }

    /**
     * Checks if we're following the route.
     * 
     * @return true if we're following.
     */
    public boolean isFollowing() {
        return m_isFollowing;
    }

    // ------------------------------------------------------------------
    // information about the segment of the route we're currently driving on

    /**
     * Returns the name of the street we're driving on.
     * 
     * @return the name of the street we're driving on. Never null. If the
     * street name is not known, or we are off-track, the empty string is
     * returned.
     */
    public String getStreetName() {
        return m_streetName;
    }

    /**
     * Checks if we're on a detour from the normal route.
     * Usually these are due to road construction or an accident.
     * 
     * @return true if the current segment of the route is on a detour;
     *         otherwise false.
     */
    public boolean isOnDetour() {
        return m_isOnDetour;
    }

    /**
     * <p>Checks if the speed camera warning should be active.</p>
     * 
     * <p>Returns true there are speed cameras on this segment of the route.
     * The server turns this "on" a suitable distance before the first
     * actual camera(s) and turn it off 50 or so meters after. This
     * means that cameras that are close enough will be seen as one
     * long camera.</p>
     * 
     * <p>Availability of speed camera information is dependent on the user's
     * subscription. There is currently no way of indicating that speed camera
     * warnings would be available on this route if the user had paid for them.
     * </p> 
     *
     * @return true if speed camera warning should be active; otherwise false.
     */
    public boolean isSpeedCameraActive() {
        return m_isSpeedCameraActive;
    }

    /**
     * Returns the speed limit (in km/h) of this segment of the route.
     * 
     * TODO: document default. And who's responsible for setting it.
     * 
     * @return an integer > 0.
     */
    public int getSpeedLimitKmh() {
        return m_speedLimitKmh;
    }

    
    // -----------------------------------------------------------------------
    // information about next waypoint

    /**
     * Returns the next {@link Waypoint}.
     * 
     * @return the next Waypoint.
     */
    public Waypoint getNextWpt() {
        return m_nextWpt;
    }

    /**
     * Returns the distance (in meters) left to drive to the next
     * {@link Waypoint}.
     * 
     * @return distance in meters, an integer >= 0.
     */
    public int getDistanceMetersToNextWpt() {
        return m_distanceMetersToNextWpt;
    }


    // -----------------------------------------------------------------------
    /*
     * information about next next waypoint. This is just a convenience
     * for porting legacy code. The information is readily available by
     * following the Wpt chain.
     */

    /**
     * Returns the next next {@link Waypoint}, i.e. getNextWpt().getNext().
     * If we are currently approaching the end Waypoint, null is returned.
     * 
     * @return the next next Waypoint.
     */
    public Waypoint getNextNextWpt() {
        if (m_nextWpt == null) {
            return null;
        } else {
            return m_nextWpt.getNext();
        }
    }

    /**
     * Returns the distance (in meters) between Waypoints <code>getNextWpt()</code> and
     * <code>getNextNextWpt()</code>. If <code>getNextNextWpt() == null</code>,
     * 0 is returned.
     * 
     * @return a distance in meters, >= 0. 
     */
    public int getDistanceMetersBtwNextAndNextNext() {
        if (m_nextWpt == null) {
            return 0;
        }
        
        Waypoint nextnextWpt = getNextNextWpt();
        if (nextnextWpt == null) {
            return 0;
        }
        
        return nextnextWpt.getDistanceMetersFromPrev();
    }


    // ------------------------------------------------------------------
    // totals

    /**
     * Returns the total distance left on the route.
     * 
     * @return distance in meters, an integer >= 0.
     */
    public int getDistanceMetersTotalRemaining() {
        return m_distanceMetersTotalRemaining;
    }

    /**
     * <p>Returns the estimated time left to go the rest of the route.</p>
     * 
     * <p>The time is calculated using the speed limit of the route. The user's
     * actual speed is not considered.</p>
     * 
     * TODO: document pedestrian.
     * 
     * @return time in seconds, an integer >= 0
     */
    public int getTimeSecondsTotalRemaining() {
        return m_timeSecondsTotalRemaining;
    }


    // ------------------------------------------------------------------
    // snap-to-route
    
    /**
     * <p>Returns the "snapped" position - the projection onto the route of the
     * real position. This can be used to fool the user that the GPS has
     * better accuracy than it really has.</p>
     *
     * <p>WARNING: this is <b>null</b> when off track.</p>
     *
     * @return a Position object representing the snapped position. 
     */
    public Position getSnappedPosition() {
        return m_snappedPosition;
    }

    /**
     * Returns the "snapped" course - the course of the segment snapped to by
     * <code>getSnappedPosition()</code>.
     * 
     * @return the compass course in degrees, >= 0.
     * @see com.wayfinder.core.positioning.LocationInformation#getCourse() 
     */
    public int getSnappedCourseDeg() {
        return m_snappedCourseDeg;
    }
    
    
    // ------------------------------------------------------------------
    // faked position. This is under evaluation for Android Navigator 9
    // and will be better integrated later.

    /**
     * <p>Returns a faked position, which is created by advancing along the route
     * a distance determined by estimated GPS lag. </p>
     *
     * <p>Faking will not be done if the user is offtrack or in pedestrian mode.</p>
     *
     * @return a Position object representing the faked position.
     * @deprecated under evaluation - subject to change without notice.
     */
    public Position getFakedPosition() {
        return m_fakedPosition;
    }

    /**
     * <p>Returns a faked course (in degrees) which is the course of the segment
     * used to calculate the faked position returned by
     * <code>getFakedPosition()</code>.</p>
     *
     * <p>Faking will not be done if the user is offtrack or in pedestrian mode.</p>
     *
     * @return the faked compass course in degrees.
     * @deprecated under evaluation - subject to change without notice.
     */
    public int getFakedCourseDeg() {
        return m_fakedCourseDeg;
    }


    // ------------------------------------------------------------------
    // internal utility

    public String toString() {
        StringBuffer sb = new StringBuffer(120);
        sb.append("NavigationInfo");
        if (!m_isFollowing) {
            sb.append(" not following");
        } else {
            if (m_route != null) { 
                sb.append(" route=").append(m_route);
            }
            if (m_isDestinationReached) {
                sb.append(", destreached");
            }
            if (m_isOffTrack) {
                sb.append(", offtrack");
            } 
            if (m_isSpeedCameraActive) {
                sb.append(", speedcamera");
            } 
            if (m_nextWpt != null) {
                sb.append(", wpt=").append(m_nextWpt);
                sb.append(", dist=").append(m_distanceMetersToNextWpt);
                sb.append(", speed=").append(m_speed);
                sb.append(", course=").append(m_snappedCourseDeg);
            }
        }
        
        return sb.toString();
    }
}
