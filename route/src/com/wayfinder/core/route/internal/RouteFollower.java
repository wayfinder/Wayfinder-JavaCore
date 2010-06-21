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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.route.internal;

import java.io.IOException;
import java.util.Vector;

import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationListener;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.route.RouteRequest;
import com.wayfinder.core.route.internal.nav2route.Landmark;
import com.wayfinder.core.route.internal.nav2route.MiniMap;
import com.wayfinder.core.route.internal.nav2route.Nav2Route;
import com.wayfinder.core.route.internal.nav2route.Nav2RouteIterator;
import com.wayfinder.core.route.internal.nav2route.Nav2RouteIterator2;
import com.wayfinder.core.route.internal.nav2route.WaypointImpl;
import com.wayfinder.core.shared.GpsPosition;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.RouteSettings;
import com.wayfinder.core.shared.route.Turn;
import com.wayfinder.core.shared.route.Waypoint;
import com.wayfinder.core.shared.util.AngleTool;

/**
 * <p>RouteFollower does the actual route following.</p>
 *
 * <p>Each RouteFollower can follow only one route during its lifetime. When
 * going off-track or re-routing for some other reason, a new RouteFollower
 * must be constructed.</p>
 */
public class RouteFollower implements Runnable, LocationListener {

    private static final Logger LOG = 
        LogFactory.getLoggerForClass(RouteFollower.class);

    /*
     * when we say "segment" we mean the line between a point (in the form of a
     * Nav2RouteIterator and the next point
     */


    /**
     * Nav2: C++/Modules/include/NavTaskInternal.h MAX_DIST This is only used by
     * the old off track detection algorithm
     */
    public static final int MAX_SEGMENT_DISTANCE_FOR_ONTRACK = 60;

    /**
     * For segments closer than this distance (in meters) the segment selection
     * algorithm will use angle comparison when determining the closest (best)
     * segment
     */
    private static final int MAX_DISTANCE_FOR_ANGLE_COMPARE = 50;

    /**
     * Maximum extra penalty that can be added for a segment due to angle error
     */
    private static final int MAX_EXTRA_ANGLE_PENALTY = 20;

    /**
     * Allow two bad positions before searching the entire route for best
     * segment instead of just forward from last best segment.
     */
    private static final int MAX_OFF_TRACK_BEFORE_FULL_SEARCH = 2;


    /**
     * the numbers of offtrack postions in a row before considerred to be off
     * track for real.
     */
    private static final int MAX_OFF_TRACK_BEFORE_USER_OFFTRACK = 3;

    /**
     * Tuning variables used by the off track detection algorithm Perhaps we
     * could brand these depending on GPS device (position quality) and location
     * (map quality)?
     */

    // -------------------------------------------------------------

    /**
     * Maximum off track penalty. We are off track if iOffTrackPenalty >=
     * MAX_OFF_TRACK_PENALTY
     */
    private static final int MAX_OFF_TRACK_PENALTY = 12000;

    /**
     * Maximum penalty per position. This is used to limit the influence of a
     * single extreme position.
     */
    private static final int MAX_PENALTY_PER_POSITION = MAX_OFF_TRACK_PENALTY / 2;

    /**
     * Minimum speed in m/s (2 m/s = 7 km/h) required for off track
     * detection
     */
    private static final int MIN_SPEED_FOR_OFFTRACK = 2;

    /**
     * Minimum penalty for a position required to increase the total penalty
     * iOffTrackPenalty, otherwise it is reset
     */
    private static final int MIN_OFF_TRACK_PENALTY = 150;

    /**
     * Distance to route (in meters) limit for starting adding extra penalty,
     * i.e. if we are further away than this from the route, extra penalty is
     * added
     */

    private static final int MIN_DIST_FOR_EXTRA_PENALTY = 20;

    /**
     * Minimum change in distance to route between consecutive positions for off
     * track detection
     */
    private static final int MIN_DIFF_PERP = 2;

    /**
     * Maximum change in distance to route between consecutive positions for off
     * track detection
     */
    private static final int MAX_DIFF_PERP = 35;

    /**
     * Limit on the angle error for determining if we are driving in the wrong
     * direction
     */
    private static final int MIN_ANGLE_ERROR_FOR_OFF_TRACK = 120;

    /**
     * Upper distance limit in meters for on track
     */
    private static final int MAX_DISTANCE_FOR_ON_TRACK = 200;

    // ------------------------------------------------------------

    /**
     * Maximum distance to route to start snapping
     */
    private static final int MAX_DISTANCE_FOR_START_SNAP = 20;

    /**
     * Maximum angle error to start snapping
     */
    private static final int MAX_ANGLE_ERROR_FOR_START_SNAP = 30;

    /**
     * Minimum angle error required to break snapping of course
     */
    private static final int MIN_ANGLE_ERROR_FOR_BREAK_COURSE_SNAP = 60;

    /**
     * Minimum speed im m/s (7m/s ~= 25kmh) required to start snapping 
     * regardless of distance to route.
     */
    private static final int MIN_SPEED_FOR_START_SNAP = 7;

    /**
     * When we are this close to the end of the route we stop navigating.
     * 
     * Smaller value in Nav2: C++/Modules/include/NavTaskInternal.h GOAL_DIST =
     * 25
     */
    public static final int DISTANCE_TO_GOAL_STOP = 30;


    // -------------------------------------------------------------

    public static final int TRACK_ONTRACK = 0;

    /**
     * this is not currently used since we only do angle checking when the route
     * starts with a uturn.
     */
    public static final int TRACK_ONTRACK_WRONG_WAY = 1;

    /**
     * we are off track but not yet enough to search the whole route
     */
    public static final int TRACK_OFFTRACK_FEW = 2;

    public static final int TRACK_OFFTRACK = 3;


    // -------------------------------------------------------------

    public static final int ST_INVALID = 0;

    /**
     * The route following system is waiting for an initial position
     * with decent accuracy. Currently this means that
     * PosDataStore.getAccuracy() <= ACCURACY_GOOD. When that happens the
     * position in the route will be determined and the route
     * information in this object will be updated and the system will
     * be in state ST_FOLLOWING.
     */
    public static final int ST_WAITING_INITIAL_POS = 1;


    /**
     * The route following system is trying to determine the starting
     * position in route from the given position fix
     */
    public static final int ST_CALCULATING_INITIAL_POS = 2;

    /**
     * The route following system is following a route. Data can be
     * assumed to be valid.
     */
    public static final int ST_FOLLOWING = 3;

    public static final int ST_PAUSED = 4;

    private final Nav2Route m_route;

    private final RouteRequest m_request;

    private final RouteModule m_routeModule;

    private final RouteSettings m_routeSettings;
    
    private final SharedSystems m_sharedSys;


    // -------------------------------------------------------------
    // the internal variables are only touched by the worker thread
    // and then immutable objects are used to pass them to UI. 

    private int m_offTrackInRowCount = 0;

    /**
     * this is the internal state in RouteFollower, the user visible state 
     * may be different.
     */
    private int m_trackState;

    private int m_navState;
    
    private boolean m_running;


    /**
     * Current off track penalty
     */
    private int m_offTrackPenalty = 0;

    private int m_oldDistToSeg = 0;
    private int m_oldTotalDistanceLeft = 0;

    /**
     * Used by position snapping.
     */
    private Nav2RouteIterator2 m_closestSegmentNext;

    /**
     *  Snapped MC2 position
     */
    private Position m_snappedPos;

    /**
     *  Snapped course
     */
    private short m_snappedCourse;

    /**
     *  time in milliseconds for last traffic information update
     */
    private long m_lastTrafficInfoUpdateTime;

    /**
     * the next WPT from m_closestSegment
     */
    private Waypoint m_nextWPT;

    /**
     * distance (meters) to go to iNextWPT, always >= 0
     */
    private int m_nextWPTEDG;

    /**
     * time (seconds) to go to iNextWPT, always >= 0
     */
    private int m_nextWPTETG;

    private Position m_fakedPosition;
    private int m_fakedCourseDeg;

    private NavigationInfo m_lastNavInfoReturned;

    public RouteFollower(RouteModule routeModule, SharedSystems systems, Nav2Route route, RouteRequest request) {
        m_routeModule = routeModule;
        m_sharedSys = systems;
        m_route = route;
        m_request = request;
        m_routeSettings = request.getRouteSettings();
        m_trackState = TRACK_ONTRACK;
        m_nextWPTEDG = 0;
        m_nextWPTETG = 0;
        m_nextWPT = route.getFirstTurnWpt();
        m_lastTrafficInfoUpdateTime = System.currentTimeMillis();
        m_oldTotalDistanceLeft = m_nextWPT.getDistanceMetersToEnd();
        m_running = false;
    }

    public synchronized void initRouteFollower() throws IOException {
        m_trackState = TRACK_OFFTRACK;
        m_offTrackInRowCount = 0;
        m_firstSegment = (Nav2RouteIterator2) m_route.getFirstCoordinate();

        m_closestSegment = (Nav2RouteIterator) m_route.getFirstCoordinate();

        m_closestSegmentNext = (Nav2RouteIterator2) m_route
                .getFirstCoordinate();

        m_P1 = (Nav2RouteIterator) m_route.getFirstCoordinate();
        m_P2 = (Nav2RouteIterator) m_route.getFirstCoordinate();
        m_P3 = (Nav2RouteIterator2) m_route.getFirstCoordinate();

        m_navState = ST_CALCULATING_INITIAL_POS;
        m_running = true;
    }


    public synchronized void locationUpdate(LocationInformation locInfo, LocationProvider provider) {
        m_newLocation = locInfo;

        // check if we're paused. In that case we won't signal to
        // worker thread to process data
        if (m_navState == ST_PAUSED) {
            // #debug info
            //System.out.println(fname + "ST_PAUSED, returning");
            if (LOG.isTrace()) {
                LOG.trace("RouteFollower.updatePositionInfo(locInfo)", "ST_PAUSED");
            }
            return;
        } else {
            /*
             * we could refrain from notifying if state is ST_INVALID as
             * well, but this would be more work in the case that we
             * have a route, which is the normal case. The extra wake-up
             * and go back to sleep by the worker thread in the invalid
             * case is tolerable. And the code is easier to understand
             * and less error prone if the check if there is actual work
             * to do when waking up is in run().
             */
            
            m_newPositionAvailable = true;
            if (LOG.isInfo()) {
                LOG.info("RouteFollower.updatePositionInfo(locInfo)", locInfo.toString());
            }
            notifyAll(); // ok, will be discarded if worker
            // thread not waiting
        }
    }

    /*
     * we only need serialized access to GPS coordinates while we copy it to our
     * own format but serialized access to route data may be needed throughout
     * the calculation. This could mean very long delays in returning from
     * updatePositionInfo() and startFollowing(). So we use a two copies system
     * where the public methods store new data in one set of variables and
     * another set is used for the worker thread. Access in only synchronized
     * while copying is done.
     * 
     * FIXME: the above was true in the old code base, but when the code was
     * migrated, the migrator changed the code so that the synchronization
     * block covers all of the navigation calculations. Thus the
     * locationUpdated() can now be blocked for substantial time.
     */

    // ---------------------------------------------------
    // data provided by locationUpdated()

    /**
     * <p>New location information provided in locationUpdated(...).</p>
     * 
     * <p>Worker thread extracts the information it needs from it so we don't
     * need to split it in locationUpdated().</p> 
     */
    private LocationInformation m_newLocation;
    
    private boolean m_newPositionAvailable = false;


    // ---------------------------------------------------
    // copies for worker thread use.

    private LocationInformation m_curLocation;
    private Position m_evaluatedPosition;

    private float m_curWGS84RadLat;
    private float m_curWGS84RadLon;
    private short m_curCourseDeg;
    private float m_curSpeedMps;


    // ---------------------------------------------------

    private boolean m_destinationReached;

    /**
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        UnexpectedError error = null;
        try {
            while (isRunning()) {
                synchronized (this) {
                    /*
                     * now, either pause() has completed and cleared the new
                     * position flag or pause will not start to execute (and
                     * set iNDS.iState flag) until we have copied the stuff.
                     * After navigate_gps the NDS-state will be checked
                     * again and results discarded if we're paused
                     */
                    while (!m_newPositionAvailable && m_running) {
                        if (LOG.isTrace()) {
                            LOG.trace("RouteFollower.run()", "waiting for new position");
                        }
                        wait();
                        if (LOG.isTrace()) {
                            LOG.trace("RouteFollower.run()", "end wait");
                        }
                    } // while awakened but no position available
                    //it suppose to stop 
                    if (!m_running) break; 
                    
                    m_curLocation = m_newLocation;
                    m_evaluatedPosition = m_curLocation.getMC2Position(); 
                    m_curWGS84RadLat = (float) m_evaluatedPosition.getRadiansLatitude();
                    m_curWGS84RadLon = (float) m_evaluatedPosition.getRadiansLongitude();

                    m_curCourseDeg = (short) m_curLocation.getCourse();
                    m_curSpeedMps = m_curLocation.getSpeed();
                    m_newPositionAvailable = false;

                    // ----------------------------------------------------------
                    // the real navigation - only two efficient lines of code...
                    boolean closestSegmentValid = navigate_gps();
                    boolean userofftrack = false;

                    // perform off track detection if car mode
                    /*
                     * BUG: but if we are too far from any segment, not all data
                     * will be valid - some may even be null.
                     * 
                     * What we really
                     * want is to use a different off-track algorithm for
                     * pedestrian mode. For example a large allowed circle of
                     * error and checking if the user is walking in the wrong
                     * direction by calculating the course from several
                     * positions instead of the GPS course, which is unreliable.
                     * 
                     * Then the UI should govern if re-routing should be done
                     * automatically. Maybe we also want to be able to inhibit
                     * playing off-track sound. Displaying off-track pictogram
                     * or the latest received on-track info is already a UI
                     * task.
                     */
                    // 
                    if (m_routeSettings.getTransportMode() == RouteSettings.TRANSPORT_MODE_CAR) {
                        userofftrack = userOffTrack(closestSegmentValid);
                    }

                    // ----------------------------------------------------------


                    if (m_routeSettings.getTransportMode() == RouteSettings.TRANSPORT_MODE_CAR
                            && closestSegmentValid) {
                        findSnappedPosition();
                    }

                    /* findFakedPosition() differentiates on transport mode
                     * and offtrack itself to generate different positions.
                     * 
                     * if no faking, then faked data will be set to pos, course
                     * reported from GPS. 
                     */
                    findFakedPosition(closestSegmentValid, userofftrack);
                    
                    /*
                     * 
                     * 1) sync on RF to prevent pause, newRoute etc.
                     * 
                     * 2) check if there is already a new route pending -> return to
                     * wait without copy
                     * 
                     * 3) check if paused -> return to wait without copy
                     * 
                     * 4) if off-track, copy off-track state, notify sound system,
                     * check if re-route
                     * 
                     * 5) if ontrack, copy data, check if goal was reached, notify
                     * sound system
                     */

                    if (m_navState == ST_PAUSED) {
                        /*
                         * don't need to check iNDS.iFollowedRoute - if it
                         * differs then iNewRouteAvailable was true anyway
                         * or pause() was called after newRoute().
                         * 
                         * wait for next pos (might already be available).
                         */

                        // #mdebug
                        //                        System.out.println(fname + iNewRoute + " "
                        //                                + iNDS.getStateAsString()
                        //                                + " won't change NDS state");
                        // #enddebug
                        continue;
                        // NOT REACHED
                    }

                    //NavigationInfo navInfo = null;
                    
                    if (userofftrack) {
                        if (LOG.isTrace()) {
                            LOG.trace("RouteFollower.run()", "now OFF TRACK");
                        }
                        
                        m_navState = ST_FOLLOWING;

                        m_lastNavInfoReturned = 
                            NavigationInfo.createForOffTrack(
                                    m_route, 
                                    m_evaluatedPosition, 
                                    m_curSpeedMps);

                        // Check if we should reroute and if so, do it.
                        // Previously we did not reroute if the distance to
                        // last
                        // reroute point was less than 30 meters. This was
                        // to avoid
                        // reroute loops with the old off track algorithm if
                        // the
                        // start of the route was more than 60 meters from
                        // the
                        // actual GPS position. With the new algorithm this
                        // is not
                        // a problem, since it requires the speed to be at
                        // least
                        // MIN_SPEED_FOR_OFFTRACK and the position to move
                        // away
                        // from the route.

                        if (m_routeSettings.isAutoReroute()) {
                            
                            if (LOG.isTrace()) {
                                LOG.trace("RouteFollower.run()", "auto reroute for OFFTRACK");
                            }
                            
                            RouteRequest newReq = 
                                RouteRequest.createRerouteRequest(
                                        new GpsPosition(
                                                m_evaluatedPosition, 
                                                m_curCourseDeg), 
                                                m_route, 
                                                m_routeSettings, 
                                                RouteRequest.REASON_OFF_TRACK);
                            m_routeModule.reroute(newReq);
                        }
                    } // was off track
                    else {
                        m_navState = ST_FOLLOWING;
                        /*
                         * have we reached the destination? In that case we
                         * pause navigation (and thus don't care about off
                         * track in the future).
                         * 
                         * if the simulator is running we don't want to stop
                         * at 8m left or something like that because the
                         * users think it is silly. In the real world they
                         * don't want to off track just because they park at
                         * the other end of the street...
                         */
                        if ((m_nextWPT.getTurn() == Turn.FINALLY)
                                && (m_nextWPTEDG < DISTANCE_TO_GOAL_STOP)) {
                            
                            if (LOG.isTrace()) {
                                LOG.trace("RouteFollower.run()", 
                                        "destination reached with meters to go: "
                                        + m_nextWPTEDG
                                        + ", setting m_destinationReached=true");
                            }

                            // destination has been reached                            
                            m_destinationReached = true;
                        } // reached goal?

                        /*
                         *  PTUI is disabled because
                         *  
                         * 1) dead lock issues in route module
                         * 
                         * 2) not certain that keep-your-route messages from
                         *    the server are handled correctly.
                         * 
                         * 3) retrying is ill-defined
                         * 
                         * 4) bad traffic content so the user is very seldom
                         *    re-routed
                         *    
                         * 5) PTUI is de-scoped from Android Navigator 9.
                         * 
                         * The product backlog has an item to fix PTUI.
                         */                       
//                        else if (shouldMakeTrafficInfoUpdate()) {
//                            RouteRequest newReq = 
//                                RouteRequest.createRerouteRequest(
//                                        new GpsPosition(
//                                                m_evaluatedPosition, 
//                                                m_curCourse), 
//                                                m_route, 
//                                                m_routeSettings, 
//                                                RouteRequest.REASON_TRAFFIC_INFO_UPDATE);
//                            if (LOG.isWarn()) {
//                                LOG.warn("RouteFollower.run()", "traffic info update");
//                            }
//                            m_routeModule.reroute(newReq);
//                        }
                        
                        m_lastNavInfoReturned = getCrtInfo();
                    } // on track

                    //m_routeModule.updateListeners(m_lastNavInfoReturned);
                    // Pause the navigation when we have reached the
                    // destination. We have to
                    // do this after listeners have been notified to hear
                    // the voice
                    // instruction "You have reached your destination"
                    if (m_destinationReached) {
                        //listener will be notified in the final block
                        break;
                    } else {
                        m_routeModule.updateListeners(m_lastNavInfoReturned);
                    }
                } // syncronized (this) block
            } // run while following
        } catch (InterruptedException e) {
            if (LOG.isError()) {
                LOG.error("RouteFollower.run()", e);
            }
            error = new UnexpectedError("Route following has been interrupted", e); 
            // goodbye!
        } catch (IOException e) {
            // because the route parsing is based on streams that can
            // throw exceptions even if they shouldn't if we do things
            // right...
            if (LOG.isError()) {
                LOG.error("RouteFollower.run()", e);
            }
            error = new UnexpectedError("Route following has stopped because of an IOException", e);
        } catch (Throwable t) {
            if (LOG.isError()) {
                LOG.error("RouteFollower.run()", t);
            }
            error = new UnexpectedError("Route following has stopped because of an error", t);
        }
        synchronized (this) {
            //send last nav info and error only if the route follower was not 
            //stopped
            if (m_running) {
                // send an extra NavigationInfo with isFollowing() == false
                // this will also have destination reached
                m_running = false;
                m_newPositionAvailable = false;
                m_navState = ST_PAUSED;
                m_lastNavInfoReturned = getCrtInfo();
                
                m_routeModule.updateListeners(m_lastNavInfoReturned);
                if (error != null) {
                    m_routeModule.error(error);
                }
            }
        }
    } // run


    private NavigationInfo getCrtInfo() {
        return new NavigationInfo(
                m_route, 
                m_evaluatedPosition, 
                m_destinationReached, 
                false,                  //off track false when using this method
                (m_navState == ST_FOLLOWING), 
                m_closestSegment.getStreetName(), 
                m_LMOnDetour, 
                m_LMLawEnforcement, 
                m_closestSegment.getSpeedLimitKmh(), 
                m_nextWPT, 
                m_nextWPTEDG, 
                (m_nextWPTEDG + m_nextWPT.getDistanceMetersToEnd()), 
                (m_nextWPTETG + m_nextWPT.getTimeSecondsToEnd()), 
                m_snappedPos, m_snappedCourse, 
                m_curSpeedMps,
                m_fakedPosition,
                m_fakedCourseDeg);
    }

    /**
     * first segment in the current route
     */
    private Nav2RouteIterator2 m_firstSegment;

    //private boolean m_snapPosition;

    private boolean m_snapCourse;

    /**
     * tribute to Nav2 system
     * 
     * don't call without a route
     * 
     * If true is returned, m_closestSegment, iNextWPT, iNextWPTEDG are updated.
     * Also updateLandmarkStatus() will have been called.
     * 
     * Returns false if current position is outside all minimaps and true
     * otherwise. Note that if there was no suitable minimap ahead of us we
     * search all minimaps from the start of the route whic guarantees that all
     * minimaps are examined when false is returned.
     * 
     * iTrackState is not affected. Thus the caller handles the logic of
     * determining the next iTrackState and what the user sees in NavDataStore.
     */
    private boolean navigate_gps() throws IOException {

        int dist_to_seg = Integer.MAX_VALUE;

        // Nav2 returns if valid segment and speed < 1.0 m/s but BB
        // has problem with LocationAPI not giving speed so that has
        // to be handled first.

        switch (m_trackState) {
        case TRACK_ONTRACK:
        case TRACK_OFFTRACK_FEW:
            dist_to_seg = getClosestSegment(500);

            if (dist_to_seg >= 0) {
                /*
                 * Found a segment - accept it. NOTE: dist_to_seg might be far
                 * off the off track limit, this just tests for valid minimaps.
                 * By design we always go forward in the route unless we end up
                 * outside all forward minimaps.
                 */

                break;
            }
            // else if (dist_to_seg == -2) {
            // // outside all forward minimaps, go off track and try all
            // }
            // fall thru
        case TRACK_ONTRACK_WRONG_WAY:
            /*
             * We do not use any hysteresis when driving the wrong way for now.
             * Fall through and treat the same way as off track.
             */

        case TRACK_OFFTRACK:
            m_closestSegment.resetFrom(m_firstSegment);
            dist_to_seg = getClosestSegment(Integer.MAX_VALUE);

            if (dist_to_seg == -2) {
                return false;
            }
        }

        /*
         * Okidoki, now dist_to_seg != -2 and m_closestSegment,
         * m_GCS_distanceLeftOnSegment, m_GCS_distanceToSegment are set.
         * 
         * If dist_to_seg == -1 m_closestSegment and the others are still set
         * from last time.
         * 
         */

        // find the forward wpt and calculate the distance and time
        // left to it
        findNextWPT();

        // we don't do this if we're off
        // track. Up to UI to decide if it
        // wants to display old warnings
        // anyway
        updateLandmarkStatus(); 


        // Hack for ramps. Ramps are digitized at the physical
        // divider. This is non-intuitive to the driver. The turning
        // instruction for ramps is moved 100m earlier to improve
        // this.
        if (m_nextWPT.getTurn().isExitRamp()) {
            m_nextWPTEDG -= 100;

            // FIXME: fix time to go? Will only be 3-4 seconds anyway.

            if (m_nextWPTEDG < 0) {
                m_nextWPTEDG = 0;
            }


            if (LOG.isDebug()) {
                LOG.debug("RouteFollower.navigate_gps()", 
                        "Did ramp hack iNextWPTEDG: " + m_nextWPTEDG);
            }
        }

        return true;
        // test for off track, reached goal and navigation stop done
        // in worker thread
    } // navigate_gps


    /**
     * @deprecated PTUI is disabled due to incomplete implementation. Subject
     *             to change without notice.
     */    
//    private boolean shouldMakeTrafficInfoUpdate() {       
//        long updateInterval = 
//            m_sharedSys.getSettingsIfc().getGeneralSettings().getPTUI();
//        boolean updateOn = 
//            m_sharedSys.getSettingsIfc().getGeneralSettings().isPTUIon();
//
//        if (updateOn
//                && System.currentTimeMillis() - m_lastTrafficInfoUpdateTime > updateInterval) {
//            m_lastTrafficInfoUpdateTime = System.currentTimeMillis();
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * Determine if current position is an off track position.
     * @throws IOException 
     */
    private boolean offTrackDetection(int distToSeg) throws IOException {
        int totalDistanceLeft = m_nextWPTEDG + 
        m_nextWPT.getDistanceMetersToEnd();

        // distance traveled along the route since last position, set to 0
        // if negative
        int distDiffAlong = 
            Math.max(0, m_oldTotalDistanceLeft - totalDistanceLeft);

        // difference in (perpendicular) distance to route since last
        // position
        int distDiffPerp = distToSeg - m_oldDistToSeg;

        // Normalized distance to route derivative. This indicates how fast
        // we are moving away from the route and is used as the primary
        // off track indicator
        int normDistDer = (1000 * distDiffPerp) / (distDiffAlong + 1);

        m_oldTotalDistanceLeft = totalDistanceLeft;
        m_oldDistToSeg = distToSeg;

        m_closestSegmentNext.resetFrom(m_closestSegment);

        //this can throw IOException 
        m_closestSegmentNext.nextPoint(false);

        // calculate angle error i.e. angle difference between current
        // course and
        // angle of current segment
        int segAngle = (int) Math.toDegrees(m_closestSegmentNext.getSegmentCourseRad());
        int angleError = Math.abs(segAngle - m_curCourseDeg);
        if (angleError > 180) {
            angleError = 360 - angleError;
        }

        // snap to route

        // Check if we should commence snapping of position and course.

        // We always require a smaller angle error than
        // MAX_ANGLE_ERROR_FOR_START_SNAP to start snapping.
        // If we are driving slower than MIN_SPEED_FOR_START_SNAP
        // we might be at a parking lot or similar and should not start
        // snapping until we are closer than MAX_DISTANCE_FOR_START_SNAP
        // since the user otherwise could have trouble
        // finding the route

        if ((m_curSpeedMps >= MIN_SPEED_FOR_START_SNAP || distToSeg <= MAX_DISTANCE_FOR_START_SNAP)
                && angleError <= MAX_ANGLE_ERROR_FOR_START_SNAP) {
            //m_snapPosition = true;
            m_snapCourse = true;
        }

        // check if we should break snapping of course
        if (m_snapCourse && m_curSpeedMps >= MIN_SPEED_FOR_OFFTRACK
                && angleError >= MIN_ANGLE_ERROR_FOR_BREAK_COURSE_SNAP) {
            // The angle error is relatively big and speed is high
            // enough to give
            // accurate positions. Break snapping of course to improve
            // navigation experience.
            m_snapCourse = false;
        }

        // Check current speed to see if we should continue
        // with off track detection. We need to have some
        // minimum speed to get precision in the positions
        // and avoid drifting.
        if (m_curSpeedMps >= MIN_SPEED_FOR_OFFTRACK) {

            // Check upper limit on distDiffPerp

            if (distDiffPerp > MAX_DIFF_PERP) {
                // This is unrealistic and most probable due to
                // large position disturbance or faulty segment selection
                // after
                // GPS connection recovery. Return, but don't reset
                // iOffTrackPenalty.
                return false;
            }

            // Check the angle error to see if we are driving in
            // the wrong direction
            if (angleError > MIN_ANGLE_ERROR_FOR_OFF_TRACK) {
                // we are driving in the wrong direction, give large penalty
                m_offTrackPenalty += MAX_PENALTY_PER_POSITION;

                return true;
            }

            // Check that we have moved at least MIN_DIFF_PERP meters
            // further away from the route since last position. This will
            // help to avoid giving big penalties for very small deviations.
            // Typically if distDiffPerp=1 and distDiffAlong=0,
            // we would get normDistDer = (1000*1)/(0+1) = 1000
            if (distDiffPerp >= MIN_DIFF_PERP) {

                // Check upper distance limit for on track

                if (distToSeg > MAX_DISTANCE_FOR_ON_TRACK) {
                    // We are very far from the route, off track immediately
                    m_offTrackPenalty += MAX_OFF_TRACK_PENALTY;
                    return true;
                }

                // Check if we have reached the minimum off track threshold
                // MIN_OFF_TRACK_PENALTY for normDistDer
                if (normDistDer >= MIN_OFF_TRACK_PENALTY) {
                    // add normDistDer to the penalty
                    if (distToSeg <= MIN_DIST_FOR_EXTRA_PENALTY) {
                        // we are closer than MIN_DIST_FOR_EXTRA_PENALTY to
                        // the route, add normal penalty
                        m_offTrackPenalty += Math.min(
                                MAX_PENALTY_PER_POSITION, normDistDer);
                    } else {
                        // we are further than MIN_DIST_FOR_EXTRA_PENALTY
                        // meters from the route
                        // increase the penalty by 100% for every 2 meters
                        // further away
                        m_offTrackPenalty += 
                            Math.min(MAX_PENALTY_PER_POSITION,
                                    normDistDer * (distToSeg 
                                            - MIN_DIST_FOR_EXTRA_PENALTY) / 2);
                    }
                    return true;
                }

            }

        }
        // We found a non off track position.
        // Reset the penalty since we require consecutive bad
        // positions for off track
        m_offTrackPenalty = 0;
        return false;
    }


    /**
     * Retrieve closest segment's coordinates and true position to calculate
     * snapped position. Sets the snapped course as the angle of the next
     * segment.
     */
    private void findSnappedPosition() {

        short x1, y1, xP, yP;
        m_closestSegmentNext.resetFrom(m_closestSegment);

        try {
            m_closestSegmentNext.nextPoint(false);
        } catch (IOException e) {
            if (LOG.isError()) {
                LOG.error("RouteFollower.findSnappedPosition()", e.toString());
            }
        }

        // FIXME: m_closestSegmentNext.isValid() should be checked. Now, this
        // only works if the iterator retains old values yielding a zero length
        // segment to snap to.
        
        MiniMap m = m_closestSegmentNext.getCurrentMiniMap();

        if (m != m_closestSegment.getCurrentMiniMap()) {
            // coordinates from m_closestSegment not valid in this MM
            // coordinate system
            x1 = m.getPrevPointX();
            y1 = m.getPrevPointY();
        } else {
            x1 = m_closestSegment.getX();
            y1 = m_closestSegment.getY();
        }

        xP = (short) m.getX(m_curWGS84RadLon);
        yP = (short) m.getY(m_curWGS84RadLat);

        m_snappedCourse = (short) Math.toDegrees(m_closestSegmentNext.getSegmentCourseRad());

        calculateSnappedPosition(x1, y1, m_closestSegmentNext.getX(),
                m_closestSegmentNext.getY(), xP, yP, m);
    }

    /**
     * <p>Performs the calculation of the snapped position. Updates iSnappedLon and
     * iSnappedLat.</p>
     * 
     * <p>For the math, see {@link RouteFollower#distancePointToSegment(short, short, short, short, short, short)}</p>
     */
    private void calculateSnappedPosition(short x1, short y1, short x2,
            short y2, short xP, short yP, MiniMap mMap) {

        int v12x = x2 - x1;
        int v12y = y2 - y1;
        int v1Px = xP - x1;
        int v1Py = yP - y1;

        int v12l;

        int v12dotv1P;

        int xL = xP;
        int yL = yP;

        // check for degenerate lines
        if (v12x == 0 && v12y == 0) {
            // L = P1 = P2

            if (LOG.isDebug()) {
                LOG.debug("RouteFollower.calculateSnappedPosition()", "v12 == 0 && v12y == 0, Degenerate line!");
            }

            xL = x1;
            yL = y1;
        } else {
            int v12lsqr = v12x * v12x + v12y * v12y;
            v12l = (int) Math.sqrt(v12lsqr);

            v12dotv1P = v12x * v1Px + v12y * v1Py;

            if (v12dotv1P < 0) {
                // L = P1
                xL = x1;
                yL = y1;
            } else if (v12dotv1P > v12lsqr) {
                // L = P2
                xL = x2;
                yL = y2;
            } else {
                // L is on the segment
                int v1Ll = v12dotv1P / v12l;
                xL = x1 + (x2 - x1) * v1Ll / v12l;
                yL = y1 + (y2 - y1) * v1Ll / v12l;
            }
        }

        // Transform to MC2 coordinates
        m_snappedPos = mMap.xyToPosition(xL, yL);
    }

    /**
     * data for the closest segment found. It might be more readable if we put
     * this in a container object.
     */
    private Nav2RouteIterator m_closestSegment;
    private int m_GCS_distanceLeftOnSegment;
    private int m_GCS_distanceToSegment;

    /**
     * Given iTrackState, iClosestSegmentValid and hold offs determine and set
     * the new value of iTrackState and return true if the user should be
     * informed that he is offtrack (which may also trigger a re-route).
     * 
     * If we want to separate wrong direction and really off track we change
     * this methods return to int. But currently NavDataStore doesn't note the
     * difference.
     * 
     * These calculations were previously split between navigate_gps and run().
     * Note that Nav2 do this by "abusing" the state.
     * @throws IOException 
     */
    private boolean userOffTrack(boolean closestSegmentValid) throws IOException {

        if (LOG.isDebug()) {
            LOG.debug("RouteFollower.userOffTrack()", 
                    "iClosestSegmentValid: " + closestSegmentValid 
                    + " m_GCS_distanceToSegment: " + m_GCS_distanceToSegment);
        }

        boolean offtrack = false;

        if (!closestSegmentValid || offTrackDetection(m_GCS_distanceToSegment)) {
            offtrack = true;
        }

        if (LOG.isDebug()) {
            LOG.debug("RouteFollower.userOffTrack()", "iOffTrackPenalty=" + m_offTrackPenalty);
        }

        if (offtrack) {

            ++m_offTrackInRowCount; // int, so won't wrap in practise

            if (m_offTrackPenalty >= MAX_OFF_TRACK_PENALTY) {

                if (LOG.isDebug()) {
                    LOG.debug("RouteFollower.userOffTrack()", 
                            "iOffTrackPenalty=" + m_offTrackPenalty
                            + " > " + MAX_OFF_TRACK_PENALTY);
                    LOG.debug("RouteFollower.userOffTrack()", "will off track the user");
                }

                m_trackState = TRACK_OFFTRACK;
                return true;
            }

            if (m_offTrackPenalty >= (MAX_OFF_TRACK_PENALTY / 2)) {
                /*
                System.out.println("iOffTrackPenalty=" + m_offTrackPenalty
                        + " > " + (MAX_OFF_TRACK_PENALTY / 2));
                System.out.println(fname + "will do full search next time");
                 */
                m_trackState = TRACK_OFFTRACK;
                return false;
            }

            m_trackState = TRACK_OFFTRACK_FEW;
            return false;
        } else {
            // Nav2: checks that user is travelling in a reasonable
            // direction so you don't on track just because you cross
            // the street you were supposed to be on


            if (LOG.isTrace()) {
                LOG.trace("RouteFollower.userOffTrack()", "back on track");
            }

            m_offTrackInRowCount = 0;
            m_trackState = TRACK_ONTRACK;
            return false;
        }
    } // userOffTrack

    // used by getClosestSegment() internally
    private Nav2RouteIterator m_P1;
    private Nav2RouteIterator m_P2;

    /**
     * must fit in 14 bits + sign, see calculations in distancePointToSegment
     */
    public static final int MAX_ORIGO_DIST = 16000;

    /**
     * If m_closestSegment is the last segment in the route, -1 is returned.
     * 
     * Otherwise check if iCurWGS84Lat, iCurWGS84Lat are valid in (= are inside)
     * the current minimap. If they are not, scan the forward minimaps until a
     * valid minimap is found. If none is found, return -2.
     * 
     * If the current minimap was valid we search segments starting with
     * m_closestSegment, otherwise start with the first segment that crosses into
     * the valid minimap.
     * 
     * finds the closest segment to m_curWGS84RadLat, m_curWGS84RadLat and set
     * m_closestSegment to point to it and m_GCS_distanceToSegment to the distance
     * to it (in meters).
     * 
     * Beware that if m_closestSegment crosses a minimap border, you can't do
     * m_closestSegment.nextPoint() and use the new and old coordinates in
     * calculations since the new coordinates are in the coordinate system of
     * the new minimap.
     * 
     * When the accumulated distance of the segments looked at exceeds
     * maxSegmentsM (in meters) the search is terminated.
     * 
     */
    private int getClosestSegment(int maxSegmentsM) throws IOException {

        m_P1.resetFrom(m_closestSegment);
        m_P2.resetFrom(m_closestSegment);
        m_P2.nextPoint(false);
        MiniMap p2minimap = m_P2.getCurrentMiniMap();

        if (!m_P2.isValid()) {
            return -1;
        }

        final int ST_EVALP1P2 = 0;
        final int ST_MINIMAPSCAN = 1;

        int state;
        int bestDist = Integer.MAX_VALUE; // not really now...
        int bestPenalty = Integer.MAX_VALUE;

        int tmpposx = p2minimap.getX(m_curWGS84RadLon);
        int tmpposy = p2minimap.getY(m_curWGS84RadLat);
        int nbrexamined = 0;

        // #mdebug
        /*
        System.out.println("RouteFollower.getClosestSegment(): START m_P1.x: "
                + m_P1.getX() + " m_P1.y: " + m_P1.getY() + " tmpposx: " + tmpposx
                + " tmpposy: " + tmpposy);

        // #enddebug
         */
        // gps position in coordinate system of p2minimap the flow is
        // too complicated for the compiler to see that they are
        // initialized (probably almost the halting problem)
        short xP = 0;
        short yP = 0;

        if (Math.abs(tmpposx) < MAX_ORIGO_DIST
                && Math.abs(tmpposy) < MAX_ORIGO_DIST) {
            xP = (short) tmpposx;
            yP = (short) tmpposy;
            state = ST_EVALP1P2;
            // if p1, p2 are in different minimaps ST_EVALP1P2 will
            // handle that
        } else {
            // not valid in this mini map, we have to find another one
            state = ST_MINIMAPSCAN;
        }

        boolean moreToDo = true;
        while (moreToDo) {

            switch (state) {
            case ST_EVALP1P2:
                // assume xP, yP ok

                short x1,
                y1;
                if (p2minimap != m_P1.getCurrentMiniMap()) {
                    // coordinates from p1 not valid in this MM
                    // coordinate system
                    x1 = p2minimap.getPrevPointX();
                    y1 = p2minimap.getPrevPointY();
                } else {
                    x1 = m_P1.getX();
                    y1 = m_P1.getY();
                }

                int dist = distancePointToSegment(x1, y1, m_P2.getX(), m_P2
                        .getY(), xP, yP);

                // penalty is at least the distance
                int penalty = dist;

//                System.out
//                        .println("RouteFollower.getClosestSegment(): ST_EVALP1P2 dist: "
//                                + dist + " aMaxSegmentsM: " + aMaxSegmentsM);

                // add extra angle penalty for segments close to the current
                // position
                if (dist < MAX_DISTANCE_FOR_ANGLE_COMPARE) {
                    m_P3.resetFrom(m_P1);
                    m_P3.nextPoint(false);

                    double headingDiff = 
                        AngleTool.compassMinAbsAngleDiffRad(
                                m_P3.getSegmentCourseRad(), 
                                Math.toRadians(m_curCourseDeg));

                    // 90 degree angle error will give MAX_EXTRA_ANGLE_PENALTY
                    penalty += (int) MAX_EXTRA_ANGLE_PENALTY
                    * Math.min(1, headingDiff * 2.0 / Math.PI);
                }

                // check if we found a new best segment
                if (penalty < bestPenalty) {
                    bestPenalty = penalty;
                    bestDist = dist;
                    m_closestSegment.resetFrom(m_P1);
                    m_GCS_distanceLeftOnSegment = m_DPTS_distanceLeftOnSegment;
                    m_GCS_distanceToSegment = dist;

                    // #debug
                    // System.out.println("RouteFollower.getClosestSegment(): ST_EVALP1P2:2 was better: "
                    // + dist);
                } // distance was better

                if (nbrexamined++ != 0) {
                    /*
                     * if the current segment is very long (more than
                     * aMaxSegmentsM) we would exhaust aMaxSegmentsM and thus
                     * get stuck on the first segment and if GPS has moved to
                     * next segment the distance would increase until we off
                     * track.
                     * 
                     * this check makes sure that we look at at least two
                     * segments (unless we already were at the last one - then
                     * the loop is terminated further down.
                     */
                    maxSegmentsM -= m_DPTS_segmentLength;
                    if (maxSegmentsM <= 0) {
                        moreToDo = false;
                        break; // meaningless to do rest of work.
                    }
                } // if not on segment starting at m_closestSegment

                m_P1.resetFrom(m_P2);
                m_P2.nextPoint(false);
                if (!m_P2.isValid()) {
                    moreToDo = false;
                    break;
                }
                // check for minimap change, check coordinates, if not
                // valid jump to scan state

                if (p2minimap != m_P2.getCurrentMiniMap()) {
                    p2minimap = m_P2.getCurrentMiniMap();

                    tmpposx = p2minimap.getX(m_curWGS84RadLon);
                    tmpposy = p2minimap.getY(m_curWGS84RadLat);

                    if (Math.abs(tmpposx) < MAX_ORIGO_DIST
                            && Math.abs(tmpposy) < MAX_ORIGO_DIST) {
                        xP = (short) tmpposx;
                        yP = (short) tmpposy;

                        // we will detect that p1 and p2 are in
                        // different minimaps and not use coordinates
                        // from p1
                    } else {
                        // not valid in p2minimap so we don't need to
                        // look at it more, ST_MINIMAPSCAN will start
                        // with the next one
                        state = ST_MINIMAPSCAN;
                    }
                }

                break;

            case ST_MINIMAPSCAN:
                // if we get here, position is not valid in p2minimap
                p2minimap = p2minimap.getNext();
                if (p2minimap == null) {
                    moreToDo = false;
                    break;
                }

                tmpposx = p2minimap.getX(m_curWGS84RadLon);
                tmpposy = p2minimap.getY(m_curWGS84RadLat);

                if (Math.abs(tmpposx) < MAX_ORIGO_DIST
                        && Math.abs(tmpposy) < MAX_ORIGO_DIST) {
                    xP = (short) tmpposx;
                    yP = (short) tmpposy;

                    p2minimap.setIteratorToFirstPoint(m_P2);
                    p2minimap.setIteratorToPrevPoint(m_P1);
                    state = ST_EVALP1P2;
                }
                // else, remain in this state

                break;
            } // switch
        } // while

        if (bestDist == Integer.MAX_VALUE) {
            // we haven't found any segments because the maps are only
            // 32 km square

            return -2;
        }


        if (LOG.isTrace()) {
            LOG.trace("RouteFollower.getClosestSegment()", 
                    "RouteFollower.getClosestSegment(): returning m_GCS_distanceToSegment "
                    + m_GCS_distanceToSegment
                    + " x: "
                    + m_closestSegment.getX()
                    + " y: "
                    + m_closestSegment.getY());
        }

        return m_GCS_distanceToSegment;
    }

    private int m_DPTS_distanceLeftOnSegment;
    private int m_DPTS_segmentLength;

    /**
     * see Nav2:C++/Modules/NavTask/DistanceCalc.cpp
     * 
     * Let l be a line from 1:(x1, y1) to 2:(x2, y2). Let L be the closest point
     * to P:(xP, yP) on l. Let v12 be a vector from 1 to 2 and v1P be a vector
     * from 1 to P and v2P be a vector from 2 to P.
     * 
     * Then the signed distance, d, from 1 to L is d = (v12 d* v1P) / (|v12|)
     * where d* is dot product and || is vector length. If d < 0, then 1 is the
     * closest point to P and if d > |v12|, then 2 is the closest. Otherwise,
     * the distance |vPL| is | v12 x v1P | / |v12| where x is 2D equivalent of
     * cross product.
     * 
     * If 1 is the closest point, return |v1P| and set
     * m_DPTS_distanceLeftOnSegment = |v12|
     * 
     * If 2 is the closest point, return |v2P| and set
     * m_DPTS_distanceLeftOnSegment = 0
     * 
     * Otherwise (on l and L is closest), return |vPL| and set
     * m_DPTS_distanceLeftOnSegment = |v12| - |v1L|
     * 
     * Always set m_DPTS_segmentLength = |v12|
     * 
     * The algorithm works only if x,y fits in 14 bits + sign. (-16383 < x <
     * 16383). The server never omits segments with coordiantes more than 14000m
     * from origon to have some margin to the off track condition.
     * 
     * There are no penalties added since those will probably have to be
     * re-tested when we using filtered routes.
     */
    private int distancePointToSegment(short x1, short y1, short x2, short y2,
            short xP, short yP) {

        // variables with names starting with v are vectors and x,y
        // are the x and y components of this vector. The variable
        // with suffix l is vector length

        // these values fit in 15 bits since they are 14 bit number +
        // another 14 bit number. Example: -16383 - (-16383) = 32766
        // = 0x7ffe
        // we use int to avoid unecessary promotions.
        int v12x = x2 - x1;
        int v12y = y2 - y1;
        int v1Px = xP - x1;
        int v1Py = yP - y1;

        // #mdebug
        /*
         * System.out.println("RouteFollower.distancePointToSegment() x1: " + x1
         * + " y1: " + y1 + " x2: " + x2 + " y2: " + y2 + " xP: " + xP + " yP: "
         * + yP + " v12x: " + v12x + " v12y: " + v12y + " v1Px: " + v1Px +
         * " v1Py: " + v1Py);
         */
        // #enddebug

        int v1Pl;
        int v12l;

        int v12dotv1P;

        // test for degenerate lines (should not appear normally)
        if (v12x == 0 && v12y == 0) {
            if (LOG.isDebug()) {
                LOG.debug("RouteFollower.distancePointToSegment()", 
                        "RouteFollower.distancePointToSegment(): " +
                "WARNING, degenerate line");
            }

            /*
             * v1Px,y may be negative, but the square is certain to be positive
             * and fit in 15*2=30 unsigned bits. Summing them yields at most 31
             * unsigned bits.
             * 
             */
            v1Pl = (int) Math.sqrt(v1Px * v1Px + v1Py * v1Py);
            m_DPTS_distanceLeftOnSegment = 0;
            m_DPTS_segmentLength = 0;
            return v1Pl;
            // NOT REACHED
        }

        // line has length > 0 ...
        /*
         * V12x,y may be negative, but the square is certain to be positive and
         * fit in 15*2=30 unsigned bits. Summing them yields at most 31 unsigned
         * bits
         */
        int v12lsqr = v12x * v12x + v12y * v12y;

        // no more than 15 bits (proof?) and > 0.
        m_DPTS_segmentLength = v12l = (int) Math.sqrt(v12lsqr);
        // #debug
        // System.out.println("RouteFollower.distancePointToSegment() m_DPTS_segmentLength: "
        // + m_DPTS_segmentLength);

        /*
         * This is really a full signed 32-bit number (15 bits + sign) * (15
         * bits + sign) = (30 bits + sign) (30 bits + sign) + (30 bits + sign) =
         * (31 bits + sign)
         */
        v12dotv1P = v12x * v1Px + v12y * v1Py;

        if (v12dotv1P < 0) {
            // outside and closest to 1
            v1Pl = (int) Math.sqrt(v1Px * v1Px + v1Py * v1Py);
            // we above the whole segment ahead of us
            m_DPTS_distanceLeftOnSegment = v12l;

            return v1Pl;
            // NOT REACHED
        }

        /*
         * length = v12dotv1P / v12l > v12l <=> v12dotv1P > (v12l)^2 because
         * v12l > 0
         */
        if (v12dotv1P > v12lsqr) {
            // outside and closest to 2

            int v2Px = xP - x2;
            int v2Py = yP - y2;
            // same argument as for v1Px,y in the case for degenerate
            // lines
            int v2Pl = (int) Math.sqrt(v2Px * v2Px + v2Py * v2Py);
            m_DPTS_distanceLeftOnSegment = 0; // past end

            return v2Pl;
        }

        /*
         * We are on the line. The distance is | v12 x v1P | / |v12|
         * 
         * x,y components for v12 and v1P fit it 15 bits. multiplying them gives
         * 30 bits + sign and adding this will be 31 bits + sign.
         * 
         * We could probably prove that with our margins we never hit
         * Integer.MIN_VALUE where -crossprod = crossprod.
         */
        int crossprod = v12x * v1Py - v12y * v1Px;
        if (crossprod < 0) {
            crossprod = -crossprod;
        }
        // in Nav2 crossprod and v12l is scaled before dividing to
        // improve accuracy. We should probably check how much
        // accuracy is gained...
        int vPLl = crossprod / v12l;

        // > 0, otherwise we have already returned.
        int v1Ll = v12dotv1P / v12l;
        m_DPTS_distanceLeftOnSegment = v12l - v1Ll;
        if (m_DPTS_distanceLeftOnSegment < 0) {
            // can happen due to rounding
            m_DPTS_distanceLeftOnSegment = 0;
        }

        return vPLl;
    } // distancePointToSegment


    // used by findNextWPT internally
    private Nav2RouteIterator2 m_P3;

    /**
     * 
     * starting with m_closestSegment, find the nearest forward WPT and calculate
     * the distance and time to it taking m_GCS_distanceLeftOnSegment into
     * account.
     * 
     * updates iNextWPT, iNextWPTEDG, iNextWPTETG
     */
    private void findNextWPT() throws IOException {

        /*
         * FIXME: pedestrians walks much slower than the speedlimit. The
         * TDL-datums are adjusted for this by the server but we need to use
         * different speed for the segments up to the TDL. When calculating the
         * server seems to use 5 km/h. The gps speed is usually not very
         * accurate at these low speeds.
         */
        
        m_P3.resetFrom(m_closestSegment);
        m_P3.nextPoint(false); // m_P3 could now be the end WPT
        // - in that case we terminate below
        // m_P3 now has valid segment length
        int distanceSum = m_GCS_distanceLeftOnSegment;

        if (m_closestSegment.isFirstSegmentAndStartsWithUturn()) {
            /*
             * we are at the first segment and it starts with a uturn. Check
             * direction. Too small segments are unreliable - this limit is
             * rather arbitarily chosen, but in Nav2, angle penalties are not
             * added on segments shorter than 5m if the angle difference is less
             * than 56 degrees (Nav2:C++/Modules/NavTask/DistanceCalc.cpp)
             */

            if (m_P3.getSegmentLengthMeters() > 5
                    && m_curCourseDeg != -1) {//AbstractPosDataStore.COORD_UNDEF) {
                double headingdiff = AngleTool.compassMinAbsAngleDiffRad(
                        m_P3.getSegmentCourseRad(), Math.toRadians(m_curCourseDeg));
                // #mdebug
                //                System.out.println(fname
                //                        + "first-with-uturn, headingdiff (deg): "
                //                        + Math.toDegrees(headingdiff));
                // #enddebug

                if (headingdiff > Math.toRadians(80)) {
                    // wrong way...
                    m_nextWPT = m_closestSegment.getWPT();
                    m_nextWPTEDG = 0;
                    m_nextWPTETG = 0;
                    // #debug
                    //                    System.out
                    //                    .println(fname
                    //                            + " first-with-uturn and the user is in wrong direction.");
                    return;

                    /* NOT REACHED */
                } else if (m_GCS_distanceLeftOnSegment 
                        == m_P3.getSegmentLengthMeters()) {
                    /*
                     * correct direction. To give additional feedback since the
                     * user might have travelled some distance before being able
                     * to turn we add distance to the next waypoint (the one
                     * after the TURN_START_WITH_U_TURN) so that the user sees
                     * it counting down. Continue calculating dist/time to next
                     * wpt normally.
                     * 
                     * This relies on that getClosestSegment() and
                     * Nav2RouteIterator2.getSegmentLengthMeters do the
                     * calculations the same way. It would feel better to let
                     * distancePointToSegment() indicate the status in a better
                     * way.
                     */
                    distanceSum += m_GCS_distanceToSegment;
                    // #debug
                    //                    System.out
                    //                    .println(fname
                    //                            + "first-with-uturn, correct direction but early. Added: "
                    //                            + m_GCS_distanceToSegment);
                } // opposite direction or not
            }
            // #mdebug
            else {
                /*
                 * else: segment too short or no current course, won't show
                 * uturn in this case, the user will likely be off tracked and
                 * re-routed and hopefully the first segment in the new route
                 * will be longer.
                 */
                //                System.out.println(fname
                //                        + "first-with-uturn but not checking, seg len: "
                //                        + m_P3.getSegmentLengthMeters());
            }
            // #enddebug
        }
        // else: not the first seg and starting with uturn, proceed
        // normally

        // time in 10ths of second, should be accurate enough
        int time10Sum;

        if (m_routeSettings.getTransportMode() == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
            time10Sum = (distanceSum * 36) / 5; //Route.PEDESTRIAN_SPEED_LIMIT;
        } else {
            time10Sum = (distanceSum * 36) / m_closestSegment.getSpeedLimitKmh();
        }

        if (m_P3.isWpt()) {
            m_nextWPT = m_P3.getWPT();
            m_nextWPTEDG = distanceSum;
            // doesn't overflow unless distance > 59000 km
            m_nextWPTETG = time10Sum / 10;
            // #mdebug
            //            System.out.println(fname + "end of closest segment was WPT "
            //                    + m_nextWPT.getIndex() + ", iNextWPTEDG " + m_nextWPTEDG);
            // #enddebug

            return;
        }

        /*
         * wasn't wpt so start at the end of the closest segment (to which m_P3
         * points now and search forward
         */
        int speedlimitkmh = m_P3.getSpeedLimitKmh();
        if (m_routeSettings.getTransportMode() == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
            speedlimitkmh = 5;//Route.PEDESTRIAN_SPEED_LIMIT;
        }
        m_P3.nextPoint(true);

        do {
            if (m_P3.isTDL()) {
                break;
            } else {
                distanceSum += m_P3.getSegmentLengthMeters();
                time10Sum += (m_P3.getSegmentLengthMeters() * 36)
                / speedlimitkmh;

                if (m_P3.isWpt()) {
                    break;
                }
            }

            speedlimitkmh = m_P3.getSpeedLimitKmh();
            if (m_routeSettings.getTransportMode() == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
                speedlimitkmh = 5;//Route.PEDESTRIAN_SPEED_LIMIT;
            }
            m_P3.nextPoint(true);
        } while (m_P3.isValid());

        if (m_P3.isTDL()) {
            // segmentDistanceSum is sum to segment that ended before TDL.
            int tdlMetersToGo = m_P3.getMetersToGo();
            int tdlSecondsToGo = m_P3.getSecondsToGo();

            m_P3.getNextWPT();
            // should always be ok
            m_nextWPT = m_P3.getWPT();
            m_nextWPTEDG =
                // between tdl and wpt
                tdlMetersToGo - m_nextWPT.getDistanceMetersToEnd()
                // and add the distance to the tdl
                + distanceSum;

            m_nextWPTETG = tdlSecondsToGo - m_nextWPT.getTimeSecondsToEnd()
            + (time10Sum / 10);

            // #mdebug
            //            System.out.println(fname + "TDL found, distanceSum: " + distanceSum
            //                    + " tdlMetersToGo: " + tdlMetersToGo + " left after wpt: "
            //                    + m_nextWPT.getTotalDistanceLeftAfter() + " iNextWPTEDG: "
            //                    + m_nextWPTEDG);
            // #enddebug
            return;

        }

        if (m_P3.isWpt()) {
            m_nextWPT = m_P3.getWPT();
            m_nextWPTEDG = distanceSum;
            m_nextWPTETG = time10Sum / 10;

            // #mdebug
            //            System.out.println(fname + "WPT " + m_nextWPT.getIndex()
            //                    + " found, distanceSum: " + distanceSum);
            // #enddebug

            return;
        }

        // #debug error
        //System.out.println(fname + "no WPT after m_closestSegment!");
    }

    private boolean m_LMLawEnforcement;
    private boolean m_LMOnDetour;

    /**
     * given iNextWPT and iNextWPTEDG scans for active landmarks. Currently we
     * only care about speed traps an detours which are either active or
     * inactive. If we want to handle other types of landmarks (e.g. drive into
     * built-up area) we might need to have a list of the currently active LMs
     * to export to gui layer. However, there is now no ui-support for anything
     * other than speed cams and detours.
     * 
     * updates m_LMLawEnforcement, m_LMOnDetour
     */
    private void updateLandmarkStatus() {
        /*
         * 
         * There is no way to have a static sort of the landmarks vector so we
         * before can look at a smaller part of it. However it is possible to do
         * some pre processing and let each RRI have a list of landmarks that
         * are active when approachingh it. This costs some memory. Currently
         * the number of landmarks in total is small so we don't think that
         * checking all of them is prohibitively expensive.
         * 
         * As long as "current waypoint and distance left" always move forward
         * we could keep a vector of candidates that gets smaller further on
         * when more and more landmarks expire. The vector would have to be
         * reset when we do a full search because no forward segments are found.
         * 
         * Another optimization is to keep a vector of landmarks that can be
         * interesting sometime between this and the previous WPT. This is
         * normally empty or consists of a very small number of elements.
         */

        m_LMLawEnforcement = false;
        m_LMOnDetour = false;
        Vector LMs = m_route.getLandmarks();
        int nextWPTIdx = ((WaypointImpl) m_nextWPT).getIndex();

        // since we don't care about other LMs than detours and speed
        // cameras we just go thru them and check for validity and
        // update the iLM-flags. We don't record them for future use
        // (needs to be done if a list of landmarks is to be exported)
        for (int i = 0; i < LMs.size(); ++i) {
            Landmark lm = (Landmark) LMs.elementAt(i);

            // is the LM valid
            if (lm.getApproachWPTIdxStart() > nextWPTIdx
                    || (lm.getApproachWPTIdxStart() == nextWPTIdx && lm
                            .getApproachDistanceStart() < m_nextWPTEDG)) {
                // not started yet
                continue;
            }

            if (lm.getApproachWPTIdxEnd() < nextWPTIdx
                    || (lm.getApproachWPTIdxEnd() == nextWPTIdx && lm
                            .getApproachDistanceEnd() > m_nextWPTEDG)) {
                // already ended
                continue;
            }

            // this LM is valid...
            if (lm.isLawEnforcement()) {
                m_LMLawEnforcement = true;
            }
            if (lm.isDetour()) {
                m_LMOnDetour = true;
            }
        } // for
    } // updateLandmarkStatus
    

    // --------------------------------------------------------------------
    // fake positions (experimental)

    
    /**
     * <p>Update m_fakedPosition and m_fakedCourseDeg.</p>
     * 
     * <p>For the following cases, no faking is done and the values from
     * m_evaluatedPosition, m_curCourseDeg are used:
     * <ol><li>in pedestrian mode.</li>
     *     <li>if userOffTrack is true (which run() doesn't evaluate for
     *         pedestrian mode).</li>
     *     <li>if closestSegmentValid is false (outside all minimap - no point
     *         to start faking from).</li>
     * </ol>
     *
     */
    private void findFakedPosition(boolean closestSegmentValid,
                                   boolean userOffTrack)
        throws IOException {

        // make sure we have updated so no NPE
        m_fakedPosition = m_evaluatedPosition;
        m_fakedCourseDeg = m_curCourseDeg;

        
        if ((m_routeSettings.getTransportMode() ==
             RouteSettings.TRANSPORT_MODE_PEDESTRIAN)
            || userOffTrack
            || ! closestSegmentValid) {

            if(LOG.isDebug()) {
                LOG.debug("RouteFollower.findFakedPosition()", "not faking.");
            }
            
            return;
        }

        
        Nav2RouteIterator2 fakeIter = m_route.newIterator2();
        fakeIter.resetFrom(m_closestSegment);
        fakeIter.nextPoint(false);
        
        if (! fakeIter.isValid()) {
            // we were at the end of the last segment.
            // we could fake along current course but navigation will anyway
            // stop because the goal is reached. And course info is only
            // reliable if speed is high enough (compare with
            // findSnappedPosition()

            if(LOG.isInfo()) {
                LOG.info("RouteFollower.findFakedPosition()",
                         "was at end of last segment, not faking.");
            }
            
            return;
        }


        // fakeIter now known to be valid.
        /*
         * totally unreasonable speeds sometimes reported on HTC Magic and Click
         * for the future we should centralize the filtering so that
         * NavigationSoundHandler.calculateTriggerDistance(...) and we act
         * on the same info. Additionally - this code is sensistive to errors
         * at intersection since we should then reduce the speed further or we
         * risk overshooting.
         */
        float fakedSpeed = m_curSpeedMps;
        if (fakedSpeed > (m_closestSegment.getSpeedLimitKmh() + 20) / 3.6) {
            fakedSpeed = m_closestSegment.getSpeedLimitKmh() / 3.6f;
        }

        if(LOG.isDebug()) {
            LOG.debug("RouteFollower.findFakedPosition()",
                      "speed conversion " + m_curSpeedMps
                      + " -> " + fakedSpeed);
        }
        
        
        int distanceToFakeM = (short) fakedSpeed;       
        
        if(LOG.isDebug()) {
            LOG.debug("RouteFollower.findFakedPosition()",
                      "will fake " + distanceToFakeM + " m.");
        }
        
        // current segment start and end coordinates
        short x1 = m_closestSegment.getX();
        short y1 =  m_closestSegment.getY();
        double courseRad = fakeIter.getSegmentCourseRad();
        MiniMap m = m_closestSegment.getCurrentMiniMap();
        if (m != fakeIter.getCurrentMiniMap()) {
            m = fakeIter.getCurrentMiniMap();
            x1 = m.getPrevPointX();
            y1 = m.getPrevPointY();
        }
        short x2 = fakeIter.getX();
        short y2 = fakeIter.getY();
        
        if (m_GCS_distanceLeftOnSegment >= distanceToFakeM) {
            // distance left on segment > 0 so segment cannot be degenerate
            // faked position, F, will be on this segment which ends at
            // P2 

            int distFromP1M = fakeIter.getSegmentLengthMeters()
                              - m_GCS_distanceLeftOnSegment
                              + distanceToFakeM;
            m_fakedPosition = calculatePositionAlongSegment(x1, y1,
                                  courseRad,
                                  distFromP1M,
                                  m);
            m_fakedCourseDeg = (short) Math.toDegrees(courseRad);

            return;
        }

        // need to exhaust this segment and travel along we find a segment to
        // stop in
        distanceToFakeM -= m_GCS_distanceLeftOnSegment; 
        do {
            x1 = x2;
            y1 = y2;
            fakeIter.nextPoint(false);
            if (! fakeIter.isValid()) {
                // previous segment was last
                if(LOG.isInfo()) {
                    LOG.debug("RouteFollower.findFakedPosition()",
                              "ran out of segments. Distance left to fake: "
                              + distanceToFakeM);
                    
                }
                m_fakedPosition = m.xyToPosition(x2, y2);
                m_fakedCourseDeg = (short) Math.toDegrees(courseRad);

                return;
            }

            // fake iter still valid
            courseRad = fakeIter.getSegmentCourseRad();
            if (m != fakeIter.getCurrentMiniMap()) {
                m = fakeIter.getCurrentMiniMap();
                x1 = m.getPrevPointX();
                y1 = m.getPrevPointY();
            }
            x2 = fakeIter.getX();
            y2 = fakeIter.getY();
            int segLengthM = fakeIter.getSegmentLengthMeters();
            if (segLengthM >= distanceToFakeM) {
                break; // and fake distanceToFakeM along the segment
            } else {
                distanceToFakeM -= segLengthM;
            }
        } while (true);
        
        m_fakedPosition = calculatePositionAlongSegment(x1, y1,
                courseRad,
                distanceToFakeM,
                m);
        m_fakedCourseDeg = (short) Math.toDegrees(courseRad);
    }


    /**
     * Caller is responsible for keeping distFromP1 small enough that the
     * calculated position is reasonably close to the MiniMap borders so that
     * the conversion to MC2 hasn't got too much error. 
     */
    private Position
    calculatePositionAlongSegment(short x1,
                                  short y1,
                                  double segmentCompassCourseRad,
                                  int distFromP1,
                                  MiniMap miniMap) {

        // see comments in com.wayfinder.core.shared.util.AngleTool
        // - this is really correct.
        
        int x2 = (int) (x1 + distFromP1 * Math.sin(segmentCompassCourseRad));
        int y2 = (int) (y1 + distFromP1 * Math.cos(segmentCompassCourseRad));
        if(LOG.isDebug()) {
            LOG.debug("RouteFollower.calculatePositionAlongSegment()",
                      "(x1, y1): (" + x1 + ", " + y1 + ") -> (x2, y2): "
                      + x2 + ", " + y2 + ")");
        }
        
        return miniMap.xyToPosition(x2, y2);
    }


    // --------------------------------------------------------------------
    // starting/stopping

    public synchronized void stop() {
    	if (LOG.isInfo()) {
        	LOG.info("RouteFollower.stop()", "stopping");
        }
        m_running = false;
        m_newPositionAvailable = false;
        m_navState = ST_PAUSED;
        notifyAll();
    }
    
    public synchronized boolean isRunning() {
        return m_running;
    }
}
