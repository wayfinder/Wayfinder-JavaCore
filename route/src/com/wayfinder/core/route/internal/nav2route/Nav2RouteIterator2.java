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

import com.wayfinder.core.shared.route.NavigatorRouteIterator;
import com.wayfinder.core.shared.route.Waypoint;
import com.wayfinder.core.shared.util.AngleTool;


/**
 * <p>A Nav2RouteIterator that also keeps track of the length of the
 * current segment and its compass course.</p>
 *
 * <p>The angle and distance are only valid after the first
 * nextPoint(false) and are then updated as the iterator is
 * advanced. The angle is however only updated if getSegmentLength() >
 * 0 (since degenerate zero-length segments don't have angles).</p>
 *
 * <p>Note that angle 0 is due north or no difference in longitude
 * (x). Your normal math text book would have this angle to PI/2. Also
 * note that the range for angles are [0, 2*PI), angles are not
 * negative.</p>
 *
 * <p>This class is not thread safe since it is most likely used as local
 * variable in a single thread context.</p>
 * 
 * FIXME: if {@link Nav2RouteIterator2#getNextWPT()} is called our values
 * are not updated correctly. This is a documented problem in jWMMG but
 * doesn't affect real usage since if we stop at a WPT we use the distance/time
 * left from it and don't do calculations with segment length. To protect the
 * innocent the getNextWPT() has been left out of the public
 * interface {@link NavigatorRouteIterator}.</p>
 *
 */
public final class Nav2RouteIterator2
    extends Nav2RouteIterator
    implements NavigatorRouteIterator {

    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */

    /*
     * If the WFMath trigonometry is to burdensome we could port the
     * FastTrig module from Nav2 which use a system with angle 0, ..., 255
     * thus having a resolution (epsilon) corresponding to about 1.4
     * degrees which is probably good enough in many cases (have to assert
     * how long distance is required before the angle error results in a
     * lateral position error of more than the offtrack distance).
     */

    private double m_angleRad;
    private int m_segmentLengthMeters;


    /**
     * Create a new Nav2RouteIterator2.
     * 
     * @see Nav2RouteIterator#Nav2RouteIterator(Nav2Route)
     */
    Nav2RouteIterator2(Nav2Route nav2Route) throws IOException {
        super(nav2Route);
    }


    /**
     * <p>Override nextPoint() to update length and angle information while
     * going thru the route.</p>
     * 
     * <p>if breakForTDL == true and after return, isTDL() == true, angle
     * and distance are not updated.</p>
     */
    public void nextPoint(boolean breakForTDL) throws IOException {

        // values for current point. if last was TDL right after MM
        // border ix, iy was updated by enterDatum() to be valid in
        // the new coordinate system
        int x = m_x;
        int y = m_y;
        MiniMap m = m_currentMiniMap;

        super.nextPoint(breakForTDL);
        // don't update if we fell off.
        if (m_state == ST_INVALID) {
            return;
        }

        // changed minimap?
        if (m != m_currentMiniMap) {
            if (isTDL()) {
                // enterDatum has reset ix and iy from minimap and
                // they are not overwritten by another point (becuase
                // on that case we would be in that nav_route, mini or
                // micro datum

                // this also means that we haven't changed segment and
                // there is no coordinate to update from
                return;
            } else {
                // passed into new segment and ix, iy are the
                // coordinates of the first point on that segment. our
                // old x,y are not valid in the new coordinate system
                x = m_currentMiniMap.getPrevPointX();
                y = m_currentMiniMap.getPrevPointY();
            }
        } // changed minimap?

        // compute differences (new - old)
        int dx = m_x - x;
        int dy = m_y - y;
        m_segmentLengthMeters = (int) Math.sqrt(dx*dx + dy*dy);

        // don't update for degenerate segments
        if (m_segmentLengthMeters > 0) {
            m_angleRad = AngleTool.compassCourseRad(dx, dy);
        }
    }

    /**
     * you can also reset from a Nav2RouteIterator. In that case
     * iAngelRad and iSegmentLengthMeters won't be updated until
     * nextPoint() is called.
     * 
     * @param iter - the iterator to reset from.
     */
    public void resetFrom(Nav2RouteIterator2 iter) {
        super.resetFrom(iter);
        m_angleRad = iter.m_angleRad;
        m_segmentLengthMeters = iter.m_segmentLengthMeters;
    }    


    /**
     * See the contract for
     * {@link NavigatorRouteIterator#getSegmentCourseRad()}.
     */
    public double getSegmentCourseRad() {
        return m_angleRad;
    }

    /**
     * See the contract for
     * {@link NavigatorRouteIterator#getSegmentLengthMeters()}.
     */
    public int getSegmentLengthMeters() {
        return m_segmentLengthMeters;
    }


    // -----------------------------------------------------------------------
    // necessary adaptions to NavigatorRouteIterator

    public Waypoint getWpt() {
        // Covariant return types was introduced in Java 1.5 which we don't use.
        return (Waypoint) getWPT();
    }

    public boolean nextPoint() {
        try {
            nextPoint(false);
            if (isValid()) {
                return true;
            } else {
                // either already invalid or we fell off the end without finding
                // any coordinate data.
                return false;
            }
        } catch (IOException e) {
            m_state = ST_INVALID;
            return false;
        }
    }
}
