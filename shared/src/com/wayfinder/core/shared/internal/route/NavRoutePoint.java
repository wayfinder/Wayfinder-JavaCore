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

package com.wayfinder.core.shared.internal.route;

/**
 * This class contains code that needs to be shared between
 * {@link com.wayfinder.core.shared.route.Turn} and
 * {@link com.wayfinder.core.route.internal.nav2route.Action} but we want to
 * hide from users outside Core (thus in an internal package). We can't put
 * this code in Action because Action is allowed to depend on Turn but not
 * the other way around due to our stand-alone module policy.
 */
public abstract class NavRoutePoint {

    // these constants will be inlined by the compiler.

    /**
     * nav_route_point_end (0x0000) is not presented as a Waypoint, because the action
     * for the end location (coded into the preceding nav_route_point)
     * is FINALLY. This is an anomaly since that is an action that logically
     * happens right after the preceding turn and not something that is
     * approached like the other turns.
     */
    private static final int END =                0x0000;

    /**
     * nav_route_point_start
     */
    public static final int START =              0x0001;
    public static final int AHEAD =              0x0002;
    public static final int LEFT =               0x0003;
    public static final int RIGHT =              0x0004;
    public static final int U_TURN =             0x0005;
    public static final int STARTAT =            0x0006;
    public static final int FINALLY =            0x0007;
    public static final int ENTER_ROUNDABOUT =   0x0008;
    public static final int EXIT_ROUNDABOUT =    0x0009;
    public static final int AHEAD_ROUNDABOUT =   0x000A;
    public static final int LEFT_ROUNDABOUT =    0x000B;
    public static final int RIGHT_ROUNDABOUT =   0x000C;
    public static final int EXITAT =             0x000D;
    public static final int ON =                 0x000E;
    public static final int PARK_CAR =           0x000F;
    public static final int KEEP_LEFT =          0x0010;
    public static final int KEEP_RIGHT =         0x0011;
    public static final int START_WITH_U_TURN =  0x0012;
    public static final int U_TURN_ROUNDABOUT =  0x0013;
    public static final int FOLLOW_ROAD =        0x0014;
    public static final int ENTER_FERRY =        0x0015;
    public static final int EXIT_FERRY =         0x0016;
    public static final int CHANGE_FERRY =       0x0017;
    public static final int END_OF_ROAD_LEFT =   0x0018;
    public static final int END_OF_ROAD_RIGHT =  0x0019;
    public static final int OFF_RAMP_LEFT =      0x001A;
    public static final int OFF_RAMP_RIGHT =     0x001B; // dec 27

    // the special roundabouts - not used in Turn but here to avoid code being
    // spread out.

    /**
     * nav_route_point_exit_rdbt_8
     */
    public static final int EXIT_ROUNDABOUT_8 =  0x001C;
    /**
     * nav_route_point_exit_rdbt_16
     */
    public static final int EXIT_ROUNDABOUT_16 = 0x001D;

    /**
     * Mask for getting the bits representing what type of turn is encoded into
     * a nav_route_point. The turn is the lower 10 bits.
     */
    public static final int TURN_MASK = 0x03ff;
}
