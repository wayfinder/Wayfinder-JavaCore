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

import com.wayfinder.core.shared.internal.route.NavRoutePoint;

/**
 * <p>Type-safe enum that represents the type of turn to take at a
 * {@link Waypoint}.</p>
 * 
 * <p>Normally, this pattern is discouraged in J2me (despite CLDC 1.1 doesn't
 * support Java 1.5 enums). But there are many values and this
 * data item influence everything from sound playing to resource handling
 * for pictograms. So we think that the increased readability and type-safety
 * is worth the price.</p>
 * 
 * <p>In this class we refer to the navigator route format and the data types
 * defined therein. Specification: "Route data V1.08".</p>
 * 
 */
public final class Turn {
    
    /**
     * The turn representing the starting point of the route.
     */
    public static final Turn START =              new Turn(NavRoutePoint.START);

    /**
     * You need to make a u-turn to get on the route. 
     */
    public static final Turn START_WITH_U_TURN =  new Turn(NavRoutePoint.START_WITH_U_TURN);


    /**
     * Continue straight ahead at the crossing.
     */
    public static final Turn AHEAD =              new Turn(NavRoutePoint.AHEAD);

    /**
     * Turn left at the crossing.
     */
    public static final Turn LEFT =               new Turn(NavRoutePoint.LEFT);

    /**
     * Turn right at the crossing.
     */
    public static final Turn RIGHT =              new Turn(NavRoutePoint.RIGHT);

    /**
     * Make a u-turn.
     */
    public static final Turn U_TURN =             new Turn(NavRoutePoint.U_TURN);


    /**
     * Drive into the roundabout.
     */
    public static final Turn ENTER_ROUNDABOUT =   new Turn(NavRoutePoint.ENTER_ROUNDABOUT);

    /**
     * Exit the roundabout at a certain exit.
     */
    public static final Turn EXIT_ROUNDABOUT =    new Turn(NavRoutePoint.EXIT_ROUNDABOUT);

    /**
     * Drive straight ahead in the roundabout.
     */
    public static final Turn AHEAD_ROUNDABOUT =   new Turn(NavRoutePoint.AHEAD_ROUNDABOUT);

    /**
     * Turn left in the roundabout. For user safety it should be stressed that
     * with right-hand-traffic you should go 3/4 around a four-way roundabout
     * and not try to drive against traffic direction. 
     */
    public static final Turn LEFT_ROUNDABOUT =    new Turn(NavRoutePoint.LEFT_ROUNDABOUT);

    /**
     * Turn right in the roundabout. For user safety it should be stressed that
     * with left-hand-traffic you should go 3/4 around a four-way roundabout
     * and not try to drive against traffic direction. 
     */
    public static final Turn RIGHT_ROUNDABOUT =   new Turn(NavRoutePoint.RIGHT_ROUNDABOUT);

    /**
     * Make a u-turn at the roundabout.
     */
    public static final Turn U_TURN_ROUNDABOUT =  new Turn(NavRoutePoint.U_TURN_ROUNDABOUT);


    /**
     * Take an on-ramp or something similar onto a larger road.
     * This is not a traditional crossing.
     */
    public static final Turn ON =                 new Turn(NavRoutePoint.ON);

    /**
     * Take an off-ramp or something similar.
     * This is not a traditional crossing. 
     */
    public static final Turn EXITAT =             new Turn(NavRoutePoint.EXITAT);

    /**
     * Take the off-ramp on your left hand side.
     */
    public static final Turn OFF_RAMP_LEFT =      new Turn(NavRoutePoint.OFF_RAMP_LEFT);

    /**
     * Take the off-ramp on your right hand side.
     */
    public static final Turn OFF_RAMP_RIGHT =     new Turn(NavRoutePoint.OFF_RAMP_RIGHT);


    /**
     * Stay on this road and do not take the exits.
     * The road splits up into two or more roads - drive what is indicated as
     * "ahead" on the signs even if this means changing course when the major
     * road turns and the smaller road is the one going ahead.
     */
    public static final Turn FOLLOW_ROAD =        new Turn(NavRoutePoint.FOLLOW_ROAD);

    /**
     * Follow the left lanes when the road splits up.
     */
    public static final Turn KEEP_LEFT =          new Turn(NavRoutePoint.KEEP_LEFT);

    /**
     * Follow the right lanes when the road splits up.
     */
    public static final Turn KEEP_RIGHT =         new Turn(NavRoutePoint.KEEP_RIGHT);


    /**
     * At the end of the road, turn left.
     */
    public static final Turn END_OF_ROAD_LEFT =   new Turn(NavRoutePoint.END_OF_ROAD_LEFT);

    /**
     * At the end of the road, turn right.
     */
    public static final Turn END_OF_ROAD_RIGHT =  new Turn(NavRoutePoint.END_OF_ROAD_RIGHT);


    /**
     * Board a ferry.
     */
    public static final Turn ENTER_FERRY =        new Turn(NavRoutePoint.ENTER_FERRY);

    /**
     * Step/drive off the ferry. This is a relevant instruction when the ferry
     * does multiple stops. For instance Stockholm–Mariehamn–Helsingfors.
     */
    public static final Turn EXIT_FERRY =         new Turn(NavRoutePoint.EXIT_FERRY);

    /**
     * Change to another ferry. This instruction will seldom be omitted as there
     * is usually a significant walking/driving distance to the other ferry's
     * terminal.
     */
    public static final Turn CHANGE_FERRY =       new Turn(NavRoutePoint.CHANGE_FERRY);


    /**
     * Park the car here and take the rest of the route on foot.
     */
    public static final Turn PARK_CAR =           new Turn(NavRoutePoint.PARK_CAR);

    /**
     * The turn representing the end point of the route.
     */
    public static final Turn FINALLY =            new Turn(NavRoutePoint.FINALLY);


    /**
     * The purpose of this turn type is not known.
     */
    public static final Turn STARTAT =            new Turn(NavRoutePoint.STARTAT);

    /*
     * exit_rdbt_8 (0x001C), exit_rdbt_16 (0x001D) are converted upon reading.
     * nav_route_poNavRoutePoint.delta (0x03FE) is not a turn.
     */


    private static final Turn[] VALUES = {
        /*
         * This must be kept in order of the values in NavRoutePoint since
         * we want to use it as a lookup table when mapping
         * nav_route_point.action to our objects. And we want to do this
         * without O(n) search thru the table each time.
         */
        START, // 1
        AHEAD,
        LEFT,
        RIGHT,
        U_TURN,
        STARTAT,
        FINALLY,
        ENTER_ROUNDABOUT,
        EXIT_ROUNDABOUT,
        AHEAD_ROUNDABOUT,
        LEFT_ROUNDABOUT,
        RIGHT_ROUNDABOUT,
        EXITAT,
        ON,
        PARK_CAR,
        KEEP_LEFT,
        KEEP_RIGHT,
        START_WITH_U_TURN,
        U_TURN_ROUNDABOUT,
        FOLLOW_ROAD,
        ENTER_FERRY,
        EXIT_FERRY,
        CHANGE_FERRY,
        END_OF_ROAD_LEFT,
        END_OF_ROAD_RIGHT,
        OFF_RAMP_LEFT,
        OFF_RAMP_RIGHT // 0x1b dec 27
    };

    /**
     * primarily for debugging use.
     */
    private static final String[] STRINGS = {
        /*
         * This must be kept in order of the values in NavRoutePoint since
         * we want to use it as a lookup table when mapping
         * m_navigatorRoutePointType (which is masked version of
         * nav_route_point.action) to strings for printing.
         */
        "start", // 1, index 0
        "ahead",
        "left",
        "right",
        "u_turn",
        "startat",
        "finally",
        "enter_roundabout",
        "exit_roundabout",
        "ahead_roundabout",
        "left_roundabout",
        "right_roundabout",
        "exitat",
        "on",
        "park_car",
        "keep_left",
        "keep_right",
        "start_with_u_turn",
        "u_turn_roundabout",
        "follow_road",
        "enter_ferry",
        "exit_ferry",
        "change_ferry",
        "end_of_road_left",
        "end_of_road_right",
        "off_ramp_left",
        "off_ramp_right" // 0x1b dec 27, index 26
    };
    
    // ----------------------------------------------------------------------
    /**
     * Returns the correct Turn for a nav_route_point action field. For the
     * special roundabouts exits, EXIT_ROUNDABOUT will be returned. 
     *  
     * @param nav_route_point_action - the value of nav_route_point.action 
     *        Only the lower 10 bits are considered.
     *
     * @return the corresponding Turn or null if the action value is not
     *         supported (end point, new turns or some meta data structure).
     */
    public static Turn getFromNavRoutePoint(int nav_route_point_action) {
        int turn = nav_route_point_action & NavRoutePoint.TURN_MASK;
        if (turn < 1) {
            return null;
        } else if (turn == NavRoutePoint.EXIT_ROUNDABOUT_8
                   || turn == NavRoutePoint.EXIT_ROUNDABOUT_16) {
            return EXIT_ROUNDABOUT;
        } else if (turn > VALUES.length) {
            return null;
        } else {
            return VALUES[--turn];
        }
    }


    /**
     * <p>Get an array with all the values. The array is a copy which you are
     * allowed to modify. It would be nicer with a real Enumeration.</p>
     * 
     * <p>This is intended for debugging use and resource construction.</p>
     * 
     * @return an array of Turn.
     */
    public static Turn[] getAllValues() {
        Turn[] r = new Turn[VALUES.length];
        System.arraycopy(VALUES, 0, r, 0, r.length);
        return r;
    }


    // ----------------------------------------------------------------------
    // instance stuff
    
    private final int m_navigatorRoutePointType;

    private Turn(int navigatorRoutePointType) {
        m_navigatorRoutePointType = navigatorRoutePointType;
    }


    /**
     * <p>Returns a unique, constant id for this turn.</p>
     * 
     * <p>We guarantee that this value will never change for a certain turn
     * type so you can use it directly to identify resources in bundles without
     * writing a large switch-ladder. Additionally, the ids are guaranteed to 
     * be sequential without gaps.</p>
     * 
     * <p>The values have changed compared with the
     * <code>jWMMG:wmmg.data.route.RouteReplyItem.getType()</code> This change
     * is for efficiency reasons.
     * 
     * @return the identifier integer (>= 0).
     */
    public int getId() {
        // currently sequential, so we don't need any magic
        return m_navigatorRoutePointType;
    }

    /**
     * <p>Returns the turn type used in evaluating the conditions in the voice
     * syntax system.</p>
     * 
     * <p>The possible return values are a subset of the possible
     * nav_route_point-types.</p> 
     * 
     * @return the turn type for sounds; -1 if no corresponding turn type
     * is defined for the voice syntax.
     */
    public int getVoiceSyntaxTurnType() {
        // the cases which are not treated separately in the syntax
        switch (m_navigatorRoutePointType) {
        case NavRoutePoint.FOLLOW_ROAD:
            return NavRoutePoint.AHEAD;

        case NavRoutePoint.END_OF_ROAD_LEFT:
            return NavRoutePoint.LEFT;
            
        case NavRoutePoint.END_OF_ROAD_RIGHT:
            return NavRoutePoint.RIGHT;

        case NavRoutePoint.START_WITH_U_TURN:
            return NavRoutePoint.U_TURN;

        default:
            return m_navigatorRoutePointType;
        }
    }    

    /**
     * Checks if the turn type is a ramp type.
     * 
     * @return true if this turn type is a ramp; otherwise false.
     */
    public boolean isExitRamp() {
        /*
         * From jWMMG-code:
         * 
         * FIXME: check sound system and Point.cpp
         * Point::isWptExistRamp() where true is returned only at Nav2
         * EXITAT (0x000d).
         */
        return (m_navigatorRoutePointType == NavRoutePoint.OFF_RAMP_LEFT
                || m_navigatorRoutePointType == NavRoutePoint.OFF_RAMP_RIGHT
                || m_navigatorRoutePointType == NavRoutePoint.EXITAT);
    }


    /**
     * Returns a string representation of the turn type.
     */
    public String toString() {
        int nav_route_point = m_navigatorRoutePointType;
        if (nav_route_point < 1 || nav_route_point > STRINGS.length) {
            return "unknown";
        }

        // 0x0 is end but we don't use that - see the NavRoutePoint definitions.
        return STRINGS[--nav_route_point];
    }
}
