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

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.route.Turn;
import com.wayfinder.core.shared.route.Waypoint;


/**
 * <p>Extension of {@link Waypoint} to an implementation used within the nav2
 * route following system. It is not intended for use outside the
 * com.wayfinder.core.route.internal-hierarchy.</p>
 * 
 * <p>This class is thread-safe.</p>
 */
public final class WaypointImpl extends Waypoint {

    private final WaypointImpl m_prev;
    private WaypointImpl m_next;
    private final int m_index;
    private final int m_distanceMetersFromPrev;


    /**
     * Create a new WaypointImpl.
     * 
     * @param driveOnRightSideBefore
     * @param position
     * @param turn
     * @param exitCount
     * @param distanceMetersToEnd
     * @param timeSecondsToEnd
     * @param roadNameAfter
     * @param speedLimitKmhAfter
     */
    WaypointImpl(boolean driveOnRightSideBefore,
                 Position position,
                 Turn turn,
                 int exitCount,
                 int distanceMetersToEnd,
                 int timeSecondsToEnd,
                 String roadNameAfter,
                 int speedLimitKmhAfter,
                 WaypointImpl prev,
                 int index
            ) {
        super(driveOnRightSideBefore,
              position,
              turn,
              exitCount,
              distanceMetersToEnd,
              timeSecondsToEnd,
              roadNameAfter,
              speedLimitKmhAfter);

        m_prev = prev;
        m_index = index;
        if (prev != null) {
            m_distanceMetersFromPrev =
                prev.getDistanceMetersToEnd() - distanceMetersToEnd;
        } else {
            // first Waypoint
            m_distanceMetersFromPrev = 0;
        }
    }


    public Waypoint getNext() {
        // Covariant return types was introduced in Java 1.5 which we don't use.
        return (Waypoint) getNextImpl();
    }

    public synchronized WaypointImpl getNextImpl() {
        return m_next;
    }

    /**
     * 
     * @param next - next WaypointImpl, must not be null.
     */
    synchronized void setNextImpl(WaypointImpl next) {
        if (next == null) {
            throw new IllegalArgumentException();
        }
        m_next = next;
        
        
    }

    public Waypoint getPrev() {
        // Covariant return types was introduced in Java 1.5 which we don't use.
        return (Waypoint) getPrevImpl();
    }

    public WaypointImpl getPrevImpl() {
        return m_prev;
    }


    /**
     * See {@link Waypoint#getDistanceMetersFromPrev()}
     */
    public int getDistanceMetersFromPrev() {
        return m_distanceMetersFromPrev;
    }


    /**
     * Returns the index of this WaypointImpl. The first WaypointImpl in the
     * route (the start wpt) is 0. This is used to find out which landmarks
     * are valid.
     * 
     * @return the index.
     */
    public int getIndex() {
        return m_index;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("WaypointImpl(");
        sb.append("index=").append(m_index);
        sb.append(", turn=").append(super.getTurn());
        sb.append(", exc=").append(super.getExitCount());
        sb.append(", distp=").append(m_distanceMetersFromPrev);
        sb.append(")");
        return sb.toString();
    }
}
