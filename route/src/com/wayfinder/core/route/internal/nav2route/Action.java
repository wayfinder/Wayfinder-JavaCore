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

import com.wayfinder.core.shared.internal.route.NavRoutePoint;
import com.wayfinder.core.shared.route.Turn;


/**
 * <p>Static code collector for handling of the action part of a nav_route_datum.
 * See {@link Nav2RouteParser}.</p>
 * 
 * <p>This class exists to make the parser class more readable. The jWMMG
 * version was ~1400 lines.</p>
 */
final class Action {

    /**
     * You are not supposed to create objects of this class.
     */
    private Action() {}

    /*
     * the action types. Unsigned 16 bit integers does not exist in java so
     * we use int to stay clear of sign extension problems. The data must be
     * read with DataInput.readUnsignedShort() and the top 16 bits must be zero.
     * 
     *  The constants are just there to make sure that the type test methods
     *  and the methods for generating strings for debugging are in line. 
     */

    /**
     * Special value invented by Java client to mark truncation points.
     */
    static final int SPECIAL_TRUNCATION = 0x03FD; 
    
    /**
     * <p>The action for a nav_route_point of type nav_route_point_delta (AKA
     * track point). cc == eee == 0.</p>
     * 
     * <p>The name "delta" is discouraged as it causes confusion with the micro
     * meta datum.</p>  
     */
    static final int TRACK =    0x03FE;

    static final int ORIGO =    0x8000; 
    static final int SCALE =    0x8001;
    static final int MINI =     0x8002;
    static final int MICRO =    0x8003;
    static final int TDL =      0x8006;
    static final int LANDMARK = 0x8007;
    static final int LANEINFO = 0x8008;
    static final int LANEDATA = 0x8009;
    static final int SIGNPOST = 0x800A;

    /**
     * For debugging use.
     */
    private static final String[] META_ACTION_STRINGS = {
        "origo",
        "scale",
        "mini",
        "micro",
        "reserved",
        "meta general",
        "tdl",
        "laneinfo",
        "lanedata",
        "signpost"
    };

    // -----------------------------------------------------------------------
    // syntactic sugar

    /**
     * Is the datum a nav_route_point?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero.
     */
    static boolean isNavRoutePoint(int action) {
        if ((action & 0x08000) == 0) {
            return true;
        }

        return false;        
    }

    /**
     * Is the datum an origo datum?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero. 
     */
    static boolean isOrigo(int action) {
        return (action == ORIGO);
    }

    /**
     * Is the datum a scale datum?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero. 
     */
    static boolean isScale(int action) {
        return (action == SCALE);
    }

    /**
     * Is the datum a mini datum (nav_route_point_mini_delta_points)?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero. 
     */
    static boolean isMini(int action) {
        return (action == MINI);
    }

    /**
     * Is the datum a micro datum (nav_route_point_micro_delta_points)?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero. 
     */
    static boolean isMicro(int action) {
        return (action == MICRO);
    }

    /**
     * Is the datum a TDL datum?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero. 
     */
    static boolean isTDL(int action) {
        return (action == TDL);
    }


    // -----------------------------------------------------------------------
    // turn handling

    /**
     * Is the datum a nav_route_point that is a Waypoint. I.e. a nav_route_point
     * which is not a TRACK and whose turn is one supported by us?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero.
     */
    static boolean isWpt(int action) {
        if (isNavRoutePoint(action)) {
            if ((action & NavRoutePoint.TURN_MASK) <= NavRoutePoint.EXIT_ROUNDABOUT_16) {
                return true;
            }
        }

        return false;        
    }

    /**
     * Is the datum a nav_route_point_end?
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero.
     */
    static boolean isEnd(int action) {
        return (isNavRoutePoint(action) && ((action & NavRoutePoint.TURN_MASK) == 0));
    }

    /**
     * Calculate the real exit count from action. Exit count is 3 bits and then
     * there are special actions for adding 8 and 16 to this. So we can handle
     * roundabouts with up to 23 exits.
     * 
     * @param action unsigned 16 bit integer. Bits 16-31 must be zero.
     */
    static final int calculateExitCount(int action) {
        int turn = action & NavRoutePoint.TURN_MASK;
        int eee = (action & 0x1c00) >> 10; // bits 12-10
        if (turn == NavRoutePoint.EXIT_ROUNDABOUT_8) {
            eee += 8;
        } else if (turn == NavRoutePoint.EXIT_ROUNDABOUT_16) {
            eee += 16;
        }
        
        return eee;
    }

    // -----------------------------------------------------------------------
    // debugging etc.

    /**
     * Return a string representation of the action field of a datum for
     * debugging use. 
     * 
     * @param action - unsigned 16 bit integer. Bits 16-31 must be zero.
     * @return a string.
     */
    static String toString(int action) {
        if ((action & 0xFFFF0000) != 0) {
            // route format uses 16 bit unsigned int. If high 16 bits
            // are set we mistakenly read it as a signed 16 bit and
            // sign extended.
            return "ERROR_16_MSB_SET 0x" + Integer.toHexString(action);
        } else if ((action & 0x8000) == 0) {
            Turn turn = Turn.getFromNavRoutePoint(action);
            if (turn != null) {
                return "nav_route_point_" + turn.toString(); 
            } else {
                switch (action) {
                case TRACK:
                    return "nav_route_point_track/delta";

                    // TODO: the special roundabouts. 

                default:
                    return "0x" + Integer.toHexString(action);   
                }
            }
        } else {
            int low_7 = (action & 0x007F);
            if (low_7 < META_ACTION_STRINGS.length) {
                return META_ACTION_STRINGS[low_7];
            } else {
                return "0x" + Integer.toHexString(action);
            }
        }   
    }
}
