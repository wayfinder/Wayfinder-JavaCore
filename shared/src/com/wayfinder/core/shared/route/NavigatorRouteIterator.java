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

//imports for javadoc usage to avoid long package names
import com.wayfinder.core.shared.Position;


/**
 * <p>The public iterator interface to the route data. This provides especially
 * the map with detailed route data.</p>
 * 
 * <p>At start, the iterator is positioned on the first point in the route and
 * <code>isValid()</code> returns true.</p>
 * 
 * <p>Implementors are not required to be thread-safe because in most cases
 * the iterator is used only in one thread and in performance critical
 * situations. Users who utilize multiple threads are expected to synchronize
 * on the iterator or use some other synchronization object common to the
 * threads.</p>
 */
public interface NavigatorRouteIterator {

    /*
     * This is an interface because there is a separate inheritance hierarchy
     * for the implementation of the iterator (which is internal to route
     * module).
     * 
     * This interface combines the parts of wmmg.data.route.Nav2RouteIterator
     * and wmmg.data.route.Nav2RouteIterator2 that are used outside the route
     * following system.
     */
    
    /**
     * <p>Checks if the iterator is still valid.</p>
     *
     * @return if the iterator has valid data; otherwise false. 
     */
    public boolean isValid();

    /**
     * <p>Advance to the next point.</p>
     *  
     * <p>If <code>isValid()</code> returns false, this method immediately
     * returns false.</p>
     * 
     * <p>Otherwise, try to advance to the next point with coordinates.
     * If this succeeds, update the data and return true.</p>
     * 
     * <p>If there were no more points with coordinates, return false. In this
     * case, the next call to <code>isValid()</code> will return false.</p>
     * 
     * <p>Please note that the route might contain zero length segments. In
     * this case several consecutive points might have the same coordinates.
     * You need to filter these out yourself if you only want distinct
     * coordinates.</p>
     * 
     * @return true if data was updated; otherwise false.
     */
    public boolean nextPoint();


    // -----------------------------------------------------------------------
    // data for current point

    /**
     * <p>Returns the latitude of the current point in MC2 coordinate system.</p>
     * 
     * <p>For performance reasons, we don't return a new {@link Position}
     * object. Because the iterator is likely to be used in a tight loop
     * and we must create a new Position object in order to return one.</p>
     * 
     * @return the mc2 latitude as an integer.
     */
    public int getMc2Latitude();

    /**
     * <p>Returns the longitude of the current point in MC2 coordinate system.</p>
     * 
     * <p>For performance reasons, we don't return a new {@link Position}
     * object. Because the iterator is likely to be used in a tight loop
     * and we must create a new Position object in order to return one.</p>
     * 
     * @return the mc2 longitude as an integer.
     */
    public int getMc2Longitude();


    /**
     * Returns the current speed limit in km/h.
     * 
     * @return an integer > 0.
     */
    public int getSpeedLimitKmh();
    
    /**
     * Checks if the current point is a {@link Waypoint}.
     * 
     * @return true if the current point is a Waypoint; otherwise false.
     */
    public boolean isWpt();
    
    /**
     * Returns the {@link Waypoint} the iterator is positioned at.
     * 
     * @return the Waypoint the iterator is positioned at. If the iterator is
     *         not positioned at a Wpt, null is returned.
     */
    public Waypoint getWpt();


    /**
     * <p>Returns the compass course (in radians) of the segment ending at the
     * current point.</p>
     * 
     * <p>If the segment is zero length, this method returns the previous value
     * until a non-zero-length segment is traversed. The reason for this is to
     * avoid a jerky "current course" at zero-length segments.</p>
     * 
     * <p>If <code>nextPoint()</code> has never returned true, this method
     * returns 0.</p>
     * 
     * <p>Beware that segments with small lengths have very limited angular
     * precision since the route precision is on the scale of meters. You might
     * want to discard courses for segments that are not at least 100m or so.</p>
     *  
     * @return the compass course as specified above.
     * @see com.wayfinder.core.shared.util.AngleTool#compassCourseRad(int, int)
     */
    public double getSegmentCourseRad();

    /**
     * <p>Returns the length (in meters) of the segment ending at the current
     * point.</p>
     *
     * <p>If <code>nextPoint()</code> has never returned true, this method
     * returns 0.</p>
     * 
     * @return the length of the segment, >= 0.
     */
    public int getSegmentLengthMeters();
}
