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


/**
 * <p>This class is a refined version of the data about landmarks that
 * can be found in a navigator route.</p>
 *
 * <p>For traffic information landmarks, systems outside the navigation
 * system will not use this directly. They will instead check
 * {@link NavigationInfo#isSpeedCameraActive()} and
 * {@link NavigationInfo#isOnDetour()}.</p> 
 *
 * <p>If support is added in the ui for displaying the "drive into
 * London" and "pass Main street on your right", a public counterpart
 * will be added. There has not been any request for this since 2005.</p>
 */
public final class Landmark {

    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */ 

    /**
     * The landmark starts on the road (list of segments) that ends at
     * Route.RouteReplyItems[iApproachWPTIdxStart]. In the binary
     * route data they are placed right after
     * (iApproachWPTIdxStart-1). For detours this means that
     * (iApproachWPTIdxStart-1) is the turn leading into the detour.
     *
     * We use numbers that are vector indices, not references to
     * RouteReplyItem since we want these numbers to impose a partial
     * order on the landmarks.
     *
     * WPT really means waypoint. Track/delta points (action ==
     * Nav2Route.RD_TRACK) are not considered here.
     */
    private final int iApproachWPTIdxStart;

    public int getApproachWPTIdxStart() {
        return iApproachWPTIdxStart;
    }


    /**
     * The landmark ends on the road (list of segments) that ends at
     * Route.RouteReplyItems[iApproachWPTIdxStart]. See
     * iApproachWPTIdxStart for techical details.
     *
     * For detours this means that (iApproachWPTIdxEnd-1) is the turn
     * back to the normal road from the detour.
     *
     * set during initial parsing of the route.
     */
    private int iApproachWPTIdxEnd;

    public synchronized int getApproachWPTIdxEnd() {
        return iApproachWPTIdxEnd;
    }

    /**
     * It is not possible to set iApproachWPTIdxEnd until the end marker
     * for the landmark has been found further down in the route. 
     */
    public synchronized void setApproachWPTIdxEnd(int approachWPTIdxEnd) {
        iApproachWPTIdxEnd = approachWPTIdxEnd;
    }
    
    /**
     * The landmark start at iApproachDistanceStart m from the WPT
     * iApproachWPTIdxStart. This distance is used because it is
     * calculated anyway as part of the normal navigation routine. If
     * we used the distance from the WPT is grouped with
     * (iApproachWPTIdxStart-1) an extra calculation would be needed.
     */
    private final int iApproachDistanceStart;

    public int getApproachDistanceStart() {
        return iApproachDistanceStart;
    }

    /**
     * The landmark ends at iApproachDistanceEnd m from the WPT
     * iApproachWPTIdxEnd. See iApproachDistanceStart.
     *
     * set during initial parsing of the route
     */
    private int iApproachDistanceEnd;

    public synchronized int getApproachDistanceEnd() {
        return iApproachDistanceEnd;
    }

    /**
     * It is not possible to set iApproachWPTIdxEnd until the end marker
     * for the landmark has been found further down in the route. 
     */
    public synchronized void setApproachDistanceEnd(int approachDistanceEnd) {
        iApproachDistanceEnd = approachDistanceEnd;
    }


    // data about the actual landmark
    /**
     * usually not very user friendly... Especially for traffic
     * related stuff.
     */
    private final String iDescription;

    public String getDescription() {
        return iDescription;
    }


    /**
     * used when finding the end of an extended landmark in the inital
     * parsing. The ids start at 0 and is incremented until for each
     * new landmark until the end of the route.
     */
    private final int iId;


    /**
     * true if flags.Landmark_t in navigatorroute landmark datum is
     * cameraLM, speedTrapLM, policeLM
     */
    private final boolean iIsLawEnforcement;

    /**
     * @return iIsLawEnforcement
     */
    public boolean isLawEnforcement() {
        return iIsLawEnforcement;
    }

    static final int FLAGS_TYPE_MASK = 0xE000; // bits 13-15
    static final int FLAGS_LANDMARK_T_MASK = 0x03E0; // bits 5-9

    /**
     * returns true flags.Type in navigatorroute landmark datum is 0
     * (disturbed route). Landmark_t is not considered.
     */
    private final boolean iIsDetour;

    /**
     * @return iIsDetour
     */
    public boolean isDetour() {
        return iIsDetour;
    }


    /**
     * @param aFlags flag field from navigatorroute
     */
    Landmark(int aApproachWPTIdxStart,
            int aApproachDistanceStart,
            int aFlags,
            String aDescription,
            int aId) {

        iApproachWPTIdxStart = aApproachWPTIdxStart;
        iApproachDistanceStart = aApproachDistanceStart;
        iDescription = aDescription;
        iId = aId;
        boolean detour = false;
        boolean lawenforcement = false;

        if ((aFlags & FLAGS_TYPE_MASK) == 0) {
            detour = true;
        }

        int landmarkt = (aFlags & FLAGS_LANDMARK_T_MASK) >> 5;
        if (landmarkt >= 10 && landmarkt <= 12) {
             lawenforcement = true;
        }

        iIsDetour = detour;
        iIsLawEnforcement = lawenforcement;
        
    }

    // --------------------------------------------------------------------
    // navigator route helpers. naming from route spec (not from our
    // code standard)

    /**
     * bits 0-13 in id_and_startstop in landmark datum is id
     */
    static final int ID_AND_STARTSTOP_ID_MASK = 0x3FFF; 

    static final int id_and_startstop_to_id(int id_and_startstop) {
        return id_and_startstop & ID_AND_STARTSTOP_ID_MASK;
    }

    static final int id_and_startstop_to_startstop(int id_and_startstop) {
        return (id_and_startstop & (~ID_AND_STARTSTOP_ID_MASK)) >> 14;
    }
    
    static boolean isStart(int id_and_startstop) {
        return (id_and_startstop_to_startstop(id_and_startstop) == 2);
    }

    static boolean isEnd(int id_and_startstop) {
        return (id_and_startstop_to_startstop(id_and_startstop) == 1);
    }

    static boolean isPoint(int id_and_startstop) {
        return (id_and_startstop_to_startstop(id_and_startstop) == 3);
    }


    // --------------------------------------------------------------------
    /**
     * Returns a string representation for debugging use.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(100);

        sb.append("Landmark {id:");
        sb.append(iId);
        sb.append(" ");
        sb.append(iApproachWPTIdxStart);
        sb.append("@");
        sb.append(iApproachDistanceStart);

        sb.append(" -- ");
        sb.append(iApproachWPTIdxEnd);
        sb.append("@");
        sb.append(iApproachDistanceEnd);

        sb.append(" ");
        sb.append(iDescription);
        sb.append(" dt:"); sb.append(iIsDetour);
        sb.append(" le:"); sb.append(iIsLawEnforcement);
        sb.append("}");

        return sb.toString();
    }
}
