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

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.route.NavRoutePoint;
import com.wayfinder.core.shared.route.NavigatorRouteIterator;
import com.wayfinder.core.shared.route.Turn;

import com.wayfinder.core.shared.util.io.WFByteArrayInputStream;
import com.wayfinder.core.shared.util.io.WFDataInputStream;


/**
 * <p>An iterator that iterates over the datums of a nav2 route.</p>
 * 
 * <p>This class is not thread safe as it is intended to be used by one thread
 * only.</p>
 * 
 * @see Nav2RouteParser
 */
public class Nav2RouteIterator {

    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     * 
     * TODO: the order of things need to be cleaned up.
     */

    private static final Logger LOG = LogFactory
            .getLoggerForClass(Nav2RouteIterator.class);

    private final Nav2Route iNav2Route;

    // public ifc
    /**
     * which is the current MiniMap?
     */
    protected MiniMap m_currentMiniMap;

    public MiniMap getCurrentMiniMap() {
        return m_currentMiniMap;
    }

    /**
     * latitude in current MiniMap coordinate system
     */
    protected short m_x;

    public short getX() {
        return m_x;
    }

    /**
     * longitude in current MiniMap coordinate system
     */
    protected short m_y;

    public short getY() {
        return m_y;
    }


    /**
     * need at least short since uint8. This is 0 if there is no speed
     * limit or we don't know it. Speed is in km/h.
     */
    protected int iSpeedLimit;

    public static final int SPEED_WHEN_NO_SPEED_IN_ROUTE = 130;

    /**
     * <p>Returns speed limit in km/h.</p>
     *
     * <p>In the maps there is always a speed limit. Either the real one
     * or a default from our map supplier or a default we calculate
     * ourselves. This method also has a safe-guard so that it never
     * returns 0 even if the route is broken.</p>
     * 
     * <p>Implements {@link NavigatorRouteIterator#getSpeedLimitKmh()}.</p>
     */
    public int getSpeedLimitKmh() {
        if (iSpeedLimit <= 0) {
            return SPEED_WHEN_NO_SPEED_IN_ROUTE;
        }

        return iSpeedLimit;
    } // getSpeedLimit


    /**
     * <p>Implements {@link NavigatorRouteIterator#isWpt()}.</p>
     */
    public boolean isWpt() {
        return Action.isWpt(iRdAction);
    }

    /**
     * if isWPT() returns true this points to the RouteReplyItem
     * corresponding to the waypoint the iterator is positioned at.
     */
    protected WaypointImpl iWPT;

    // don't rename this, because it needs to be overridden in
    // Nav2RouteIterator2 but with a different return type and Java 1.3
    // does not support covariant return types.
    public WaypointImpl getWPT() {
        return iWPT;
    }


    /**
     * name of current street we're driving on
     */
    protected String iStreetName;

    public String getStreetName() {
        return iStreetName;
    }


    public boolean isTDL() {
        return Action.isTDL(iRdAction);
    }

    /**
     * if isTDL() returns true iSecondsToGo is the number of seconds left
     * to drive. x,y, streetname etc. is from previous
     * nav_route_point/nav_mini_track_point/nav_micro_delta_point
     */
    protected int iSecondsToGo;

    public int getSecondsToGo() {
        return iSecondsToGo;
    }

    /**
     * if isTDL() returns true iMetersToGo is the number of meters left
     * to drive. See also iSecondsToGo.
     */
    protected int iMetersToGo;

    public int getMetersToGo() {
        return iMetersToGo;
    }


    /**
     * there is no more data to read
     */
    protected static final int ST_INVALID = 0;


    /**
     * streams are positioned so that the next read is the action part
     * of a datum. x,y etc. are from previous datum read + historic
     * information
     */ 
    protected static final int ST_DATUMDONE = 1;

    /**
     * In a Mini datum.
     */
    protected static final int ST_MINI = 2;

    /**
     * In a Micro datum.
     */
    protected static final int ST_MICRO = 3;


    /**
     * this state is transient and used only inside getNextPoint. In
     * this state we continue to read datums until we found one with
     * coordinates.
     */
    protected static final int ST_READDATUM = 4;

    protected int m_state;


    /**
     * <p>Action part of the current route datum.</p>
     * 
     * <p>-1 before any reading has been done. We can't use 0 because we need
     * to distinguish then nav_route_point_end which is also 0.</p>
     * 
     * <p>We use int (32 bit) to
     * accomodate the unsigned 16 bit integer in the route
     * format. Must be read with readUnsignedShort().</p>
     */
    protected int iRdAction = -1;


    /**
     *
     */
    public void nextPoint(boolean breakForTDL)
        throws IOException {

        final String fname = "Nav2RouteIterator.nextPoint() ";

        if(LOG.isTrace()) {
            LOG.trace(fname, "entering, " + this);
        }

        if (m_state == ST_INVALID) {
            return;
        }

        boolean moreToDo;
        do {
            moreToDo = false; // common case is termination ok

            switch (m_state) {
            case ST_MINI:
                // at least one more part to read.  FIXME: if we are
                // at first nav_mini_track_point we don't update the
                // speed since we would have to read ahead and then
                // back up
                m_x = iRDIS.readShort();
                m_y = iRDIS.readShort();
                if (remainingInDatum() == 2) {
                    // have read second nav_mini_track_point, only
                    // speed limits left
                    iRDIS.skipBytesForced(1);
                    iSpeedLimit = iRDIS.readUnsignedByte();
                    // at end of datum
                    m_state = ST_DATUMDONE;
                }
                break;

            case ST_MICRO:
                int dx = iRDIS.readByte();
                int dy = iRDIS.readByte();
                if (dx == 0 && dy == 0) {
                    skipRestOfDatum();

                    // but now we haven't changed the point, so we
                    // have to read more datums
                    moreToDo = true;
                    m_state = ST_READDATUM;
                }
                else {
                    m_x += dx;
                    m_y += dy;
                    if (remainingInDatum() == 0) {
                        m_state = ST_DATUMDONE;
                    }
                }
                break;

            case ST_DATUMDONE:
                m_state = ST_READDATUM;
                // FINDBUGS: fixing the fall-thru warning requires re-writing
                // the loop logic so that states are not dependant on default
                // being to terminate.
                // That might introduce new bugs and this code works.
                // So we just exclude the warning.

                // fall thru
                
            case ST_READDATUM:
                enterDatum();
                if (m_state == ST_INVALID) {
                    if(LOG.isDebug()) {
                        LOG.debug(fname, "there was no more data to read");
                    }
                    break; // loop will terminate on moreToDo
                }

                if (Action.isMini(iRdAction)) {
                    m_state = ST_MINI;
                    moreToDo = true;
                }
                else if (Action.isMicro(iRdAction)) {
                    m_state = ST_MICRO;
                    moreToDo = true;
                }
                else if (breakForTDL && isTDL()) {
                    // parse TDL
                    iRDIS.skipBytesForced(2); // uint16 reserved
                    // time, dist are uint32 but distance
                    // Integer.MAX_VALUE is 50 times around the earth
                    // so we don't expect to use the whole range
                    // anyway and can stick to int instead of long
                    iSecondsToGo = iRDIS.readInt();
                    iMetersToGo = iRDIS.readInt();
                    m_state = ST_DATUMDONE;
                }
                else if (Action.isNavRoutePoint(iRdAction)) {
                    parseNavRoutePoint();
                    m_state = ST_DATUMDONE;
                }
                else {
                    // enterDatum handles mini map changes so this is
                    // a datum we don't handle or TDL but we weren't
                    // supposed to stop for it, skip it

                    skipRestOfDatum();

                    if(LOG.isTrace()) {
                        LOG.trace(fname, "skipping " + Action.toString(iRdAction));
                    }
                    moreToDo = true;
                }
                break;

            default:
                // will not happen unless there is a programming error
                throw new Error("invalid state " + m_state);
            } // switch
        } while (moreToDo);

        if(LOG.isTrace()) {
            LOG.trace(fname, "exiting, " + this);
        }
    } // getNextPoint


    /**
     * scans forward and stops at the next WPT. All non-TDL-data is
     * updated. If the iterator is already on a WPT it is not
     * advanced. If there are no WPTs ahead (which means the iterator
     * will be invalid on return) the speedlimit and flags might not
     * be correct since we skip all datums that are not WPTs even
     * though they contain speed/flag information. The reason for this
     * is that, if a WPT is found, speed, flags and streetname is
     * retrieved from the WPT anyway since the iterator is positioned
     * at the start of the segment which near end is in the WPT.
     */
    public void getNextWPT()
        throws IOException {

        if (isWpt()) {
            return;
        }

        boolean moreToDo;
        do {
            moreToDo = true; // common case is no wpt yet

            switch (m_state) {
            case ST_MINI:
            case ST_MICRO:
                skipRestOfDatum();
                m_state = ST_DATUMDONE;
                // break and let the loop go again and go into the
                // correct state. This might be less efficient but
                // avoid complaints from FindBugs.
                break;

            case ST_DATUMDONE:
                m_state = ST_READDATUM;
                // see state ST_MICRO
                break;
                
            case ST_READDATUM:
                enterDatum();
                if (m_state == ST_INVALID) {
                    moreToDo = false;
                    break;
                }

                if (Action.isWpt(iRdAction)) {
                    parseNavRoutePoint();
                    m_state = ST_DATUMDONE;
                    moreToDo = false;

                    break;
                }

                // enterDatum handles mini map changes so this is
                // a datum we don't handle
                skipRestOfDatum();
                break;

            default:
                // will not happen unless there is a programming error
                throw new Error("invalid state " + m_state);
            } // switch
        } while (moreToDo);
    } // getNextWPT


    /**
     * assumes that iRDIS is positioned before the action part of a
     * route datum.
     *
     * if iterator is invalid returns immediately.
     *
     * if there is no more data to read, set m_State = ST_INVALID and
     * return
     *
     * else calls iRDIS.readUnsignedShort() into iRdAction. If
     * iRdAction is now an origo, we passed the minimap border. In
     * that case iCurrentMiniMap is set to iCurrentMiniMap.getNext(),
     * ix and iy are updated from iCurrentMiniMap.prevPointX and then
     * data is skipped up to iCurrentMiniMap.iFirstDatumPos and
     * enterDatum() is called recursively. The ix, iy updating is
     * often overwritten since the first datum after a MiniMap-border
     * is often at least a Mini datum, but there are no guarantees for
     * that.
     *
     * if iRdAction didn't become origo, iLastDatumPos will be set to
     * the current position - 2 and enterDatum() terminates. The
     * caller should check iRdAction and set state accordingly.
     */
    protected void enterDatum()
        throws IOException {

        if (m_state == ST_INVALID) {
            return;
        }
        else if (iBAIS.available() == 0) {
            m_state = ST_INVALID;
            return;
        }
        else if (Action.isEnd(iRdAction)) {
            // we have read and returned the end point and now there is
            // only another EPT left.
            m_state = ST_INVALID;
            return;
        }
        else if (iRdAction == Action.SPECIAL_TRUNCATION) {
            // we have read the special truncation point and must not wander
            // into the part of the route which have only coordinates around
            // the waypoints.
            m_state = ST_INVALID;
            return;
        }
        else {
            iRdAction = iRDIS.readUnsignedShort();
            // System.out.println("Nav2RouteIterator.enterDatum() action "
            //                    + Nav2Route.datumAction2String(iRdAction)
            //                    + "@" + (iBAIS.getPos() - 2));

            if (Action.isOrigo(iRdAction)) {
                m_currentMiniMap = m_currentMiniMap.getNext();
                m_x = m_currentMiniMap.m_prevPointX;
                m_y = m_currentMiniMap.m_prevPointY;
                iBAIS.setPos(m_currentMiniMap.m_firstDatumBufPos);
                enterDatum();
            }
        }
    } // enterDatum


    /**
     * advance iBAIS to before the action of the next datum. If we are
     * at a datum border the position is not changed
     */
    protected void skipRestOfDatum()
        throws IOException {

        int skip = remainingInDatum(); // 0 if at border

        if(LOG.isTrace()) {
            LOG.trace("Nav2RouteIterator.skipRestOfDatum()", "skipping " + skip);
        }

        iRDIS.skipBytesForced(skip);
    } // skipRestOfDatum


    /**
     * <p>Returns number of bytes remaining in this datum. Doesn't take
     * state into account so it will give positive values even though
     * buffer may be exhausted.</p>
     *
     * <p>When the BAIS position is at a datum border it will return 0,
     * not NBR_BYTES_PER_DATUM. This choice is made to make it easier
     * when reading mini and micro datums. I.e. this method returns
     * values
     * (n={@link Nav2RouteParser#NBR_BYTES_PER_DATUM}): n-1, n-2, n-3, ..., 1, 0
     * </p>
     */
    protected int remainingInDatum() {

        int pos = iBAIS.getPos();
        if ((pos % Nav2RouteParser.NBR_BYTES_PER_DATUM) == 0) {
            return 0;
        }

        int datumnbr = pos / Nav2RouteParser.NBR_BYTES_PER_DATUM;
        return (datumnbr + 1) * Nav2RouteParser.NBR_BYTES_PER_DATUM - pos;
    }


    /**
     * internal helper function for getNextPoint() and getNextWPT()
     *
     * assumes that iRdAction was just read and
     * Nav2Route.isNavRoutePoint(iRdAction) is true
     */
    protected void parseNavRoutePoint()
        throws IOException {

        if(LOG.isTrace()) {
            LOG.trace("Nav2RouteIterator.parseNavRoutePoint()",
                      "iRdAction: " + Action.toString(iRdAction));
        }

        // skip flags for now
        iRDIS.skipBytesForced(1);
        iSpeedLimit = iRDIS.readUnsignedByte();
        m_x = iRDIS.readShort();
        m_y = iRDIS.readShort();

        if (Action.isWpt(iRdAction)) {
            iWPT = iNav2Route.getWpt(iRDIS.readUnsignedShort());
            iStreetName = iWPT.getRoadNameAfter();
            iRDIS.skipBytesForced(2); // street name index
        }
        else {
            // nav_route_point_delta or turn we don't
            // support, but we want the street name. The
            // case of the broken index at end points is
            // handled by the WPT case above
            iRDIS.skipBytesForced(2); // meters (used as RRI index)
            iStreetName = (String) iNav2Route.m_stringTable
                .elementAt(iRDIS.readUnsignedShort());
        }
    } // parseNavRoutePoint


    /**
     * returns false if the iterator has fallen off the end of the route
     */
    public boolean isValid() {
        return (m_state != ST_INVALID);
    }


    /**
     * Is this iterator positioned on the start of the first segment
     * in the route and does the route start with a uturn?
     */
    public boolean isFirstSegmentAndStartsWithUturn() {
        return (isWpt()
                && (getWPT().getTurn() == Turn.START_WITH_U_TURN));
    }


    /**
     * <p>Returns x as a global longitude in the MC2 coordinate system.</p>
     * 
     * <p>Implements {@link NavigatorRouteIterator#getMc2Longitude()}.</p>
     */
    public int getMc2Longitude() {
        return m_currentMiniMap.xToMC2(m_x);
    }


    /**
     * <p>Returns y as a global latitude in the MC2 coordinate system.</p>
     * 
     * <p>Implements {@link NavigatorRouteIterator#getMc2Latitude()}.</p>
     */
    public int getMc2Latitude() {
        return m_currentMiniMap.yToMC2(m_y);
    }


    /**
     * set this iterator to point to the same thing and have the same
     * state as aIter. This method requires that the iterators are
     * working on the same route.
     */
    public void resetFrom(Nav2RouteIterator aIter) {
        if (aIter.iNav2Route != iNav2Route) {
            throw new IllegalArgumentException("Nav2RouteIterator.resetFrom() different routes");
        }

        m_currentMiniMap = aIter.m_currentMiniMap;
        m_x = aIter.m_x;
        m_y = aIter.m_y;
        iSpeedLimit = aIter.iSpeedLimit;
        iWPT = aIter.iWPT;
        iStreetName = aIter.iStreetName;
        iSecondsToGo = aIter.iSecondsToGo;
        iMetersToGo = aIter.iMetersToGo;
        m_state = aIter.m_state;
        iRdAction = aIter.iRdAction;

        iBAIS.setPos(aIter.iBAIS.getPos());
    } // resetFrom


    private static final String[] ST_STRINGS = {
        "ST_INVALID",
        "ST_DATUMDONE",
        "ST_MINI",
        "ST_MICRO",
        "ST_READDATUM"
    };
    
    /**
     * Return a string representation of m_state for debugging use for
     * subclasses.
     * 
     * @return a string.
     */
    protected String stateToString() {
        // exception if invalid state, but in that case we
        // have probably made some mistake anyway.
        return ST_STRINGS[m_state];
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(100);

        sb.append("Nav2RouteIterator {");
        sb.append(" ");
        sb.append(ST_STRINGS[m_state]);
        sb.append(" ix: "); sb.append(m_x);
        sb.append(" iy: "); sb.append(m_y);
        sb.append(" pos: "); sb.append(iBAIS.getPos());
        sb.append(" iNavRoute: ");
        sb.append(iNav2Route);
        sb.append("}");

        return sb.toString();
    }


    // ----------------------------------------------------------------------
    // internal ifc

    protected WFByteArrayInputStream iBAIS;
    protected WFDataInputStream iRDIS;

    /**
     * this constructor always returns an iterator positioned on the
     * first point, not the first datum in case the first datum is
     * TDL. We should probably have some options. Currently all
     * iterators are constructed by Route.getFirstCoordinate() so this
     * behavious is not a problem
     */
    Nav2RouteIterator(Nav2Route aNav2Route)
        throws IOException {

        iNav2Route = aNav2Route;
        iBAIS = new WFByteArrayInputStream(aNav2Route.m_rawRoute);
        iRDIS = new WFDataInputStream(iBAIS);
        iRDIS.skipBytesForced(3 * Nav2RouteParser.NBR_BYTES_PER_DATUM); // start, origo, scale
        // we are now at the first datum after first minimap start
        // which is always a full nav_route_point and corresponds to
        // the start WPT.
        m_currentMiniMap = aNav2Route.m_firstMiniMap;
        m_state = ST_DATUMDONE;

        nextPoint(false); // enters first datum
    }
}
