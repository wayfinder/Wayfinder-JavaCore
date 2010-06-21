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

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.WFMath;
import com.wayfinder.core.shared.util.WFUtil;
import com.wayfinder.core.shared.util.io.WFDataInputStream;


/**
 * <p>Representation of a Nav2 minimap.</p>
 * 
 * <p>A Nav2 minimap is a flat projection of a 32x32 km
 * square of earth surface. In this map, the coordinate system is cartesian
 * with 1 meter resolution. The coordinates will fit in 14 bits + sign which in
 * turn allows us to do our calculations with signed 32 bit integers.</p>
 * 
 * <p>x is east-west direction (longitude), y is north-south direction
 * (latitude).</p>
 * 
 * <p>This class is thread safe.</p>
 */
public final class MiniMap {

    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */

    private static final Logger LOG =
        LogFactory.getLoggerForClass(MiniMap.class);
    
    
    /**
     * Latitude of local origo, expressed in meters.
     */
    private final int m_origoX;

    /**
     * Longitude of local origo, expressed in meters.
     */
    private final int m_origoY;

    /**
     * The scale factor used to convert a longitude in WGS84 radians to x.
     * @see MiniMap#getX(float)
     */
    private final float m_scaleX;

    /**
     * Scale factor used to convert x to mc2 coordinates.
     * 
     * @see MiniMap#xToMC2(int)
     */
    private final float m_xToMC2Factor;

    /**
     * The scale factor used to convert a latitude in WGS84 radians to y.
     * This is the radius of the earth (in meters).
     * 
     * @see MiniMap#getY(float)
     */
    public static final float SCALEY = (float) WFUtil.EARTH_RADIUS;


    /**
     * The last point in the previous MiniMap expressed in the local
     * coordinate system of this MiniMap. This previous point might be
     * a full nav_route_point or the last part of a micro point.
     */
    final short m_prevPointX;

    /**
     * The last point in the previous MiniMap expressed in the local
     * coordinate system of this MiniMap. This previous point might be
     * a full nav_route_point or the last part of a micro point.
     */
    final short m_prevPointY;


    /**
     * Index of the first datum after the scale in the associated
     * short[] in the Nav2Route to which this MiniMap belongs
     */
    final int m_firstDatumBufPos;

    /**
     * The next MiniMap. null if this was the last minimap (during
     * initialization: last so far).
     */
    private MiniMap m_nextMiniMap;

    /**
     * The previous MiniMap.
     */
    private final MiniMap m_prevMiniMap;


    /**
     * what was the last speed limit in (any of) the previous minimap?
     */
    private final int m_lastSpeedBefore;

    /**
     * what was the last value of the flags in (any of) the previous
     * minimap?
     */
    private final int m_lastFlagsBefore;

    /**
     * what was the street name we were driving on when we crossed the
     * MiniMap border?
     */
    private final String m_lastStreetName;


    /**
     * @return prevPointX
     */
    public short getPrevPointX() {
        return m_prevPointX;
    }

    /**
     * @return prevPointY
     */
    public short getPrevPointY() {
        return m_prevPointY;
    }


    public synchronized MiniMap getNext() {
        return m_nextMiniMap;
    }

    /**
     * Set the reference to the next MiniMap. This is done during parsing when
     * the next MiniMap is encountered in the stream.
     * 
     * @param nextMM - the next MiniMap.
     */
    synchronized void setNext(MiniMap nextMM) {
        m_nextMiniMap = nextMM;
    }


    /**
     * <p>convert WGS84RadLongitude (in radians) to x in this MiniMap's
     * coordinate system.</p>
     * 
     * <p>The result may be too large to fit in 14 bits+sign (which is why
     * we return int) and in that case the route following arithmetic will
     * not fit in 32 unsigned bits.</p>
     * 
     * @param WGS84RadLongitude Longitude in radians. Range: [-Pi, +Pi].
     * @return WGS84RadLongitude as x-coordinate.
     */
    public int getX(float WGS84RadLongitude) {
        return (int) ((WGS84RadLongitude * m_scaleX) - m_origoX);
    }

    /**
     * return the MC2 (global) value corresponding to x in this
     * MiniMap's coordinate system.
     */
    public int xToMC2(int x) {
        return (int) ((x + m_origoX) * m_xToMC2Factor);
    }

    /**
     * convert WGS84RadLatitude (in radians) to y in this MiniMaps
     * coordinate system. See getX()
     */
    public int getY(float WGS84RadLatitude) {
        return (int) ((WGS84RadLatitude * SCALEY) - m_origoY);
    }

    /**
     * return the MC2 (global) value corresponding to y in this
     * MiniMap's coordinate system.
     */
    public int yToMC2(int y) {
        return (int) ((y + m_origoY) / WFUtil.MC2SCALE_TO_METER);
    }

    /**
     * <p>Creates a {@link Position} which is a global representation of the
     * local coordinates (x, y).</p>
     * 
     * <p>This method does not check that (x,y) is in range for this MiniMap.
     * The conversion error increases the farther away (x, y) is from the
     * MiniMap's origo due to increased distortion when mapping the curved
     * earth surface to a flat surface.</p>
     *  
     * @param x x-coordinate in this MiniMap.
     * @param y y-coordinate in this MiniMap.
     * 
     * @return a new Position object.
     */
    public Position xyToPosition(int x, int y) {
        if(LOG.isWarn()) {
            if (Math.abs(x) > 16000 || Math.abs(y) > 16000) {
                LOG.warn("MiniMap.xyToPosition()",
                         "(" + x + ", " + y
                         + ") is not intended for conversion with this MiniMap");
            }
        }

        // Position(int mc2Latitude, int mc2Longitude)
        //          y is latitude,   x is longitude, 
        return new Position(yToMC2(y), xToMC2(x));
    }


    /**
     * aIter's buffer position is set to iFirstDatumPos, speed,
     * streetname and flags are updated from this MiniMap, state is
     * set to ST_DATUMDONE.
     *
     * ix, iy is set from prevPointX, prevPointY (to handle micro
     * points following a MiniMap)
     *
     * finally getNextPoint() is called. This means that the iterator
     * will be positioned on the first datum with coordinates.
     */
    public void setIteratorToFirstPoint(Nav2RouteIterator iterator)
        throws IOException {

        iterator.m_currentMiniMap = this;
        iterator.m_x = m_prevPointX; // overwritten later 
        iterator.m_y = m_prevPointY;
        iterator.iSpeedLimit = m_lastSpeedBefore;
        // if first datum is a wpt aIter.iWPT will be set from it and
        // otherwise it is not interesting
        iterator.iStreetName = m_lastStreetName;
        // TDL data only valid if on a TDL-datum in which case it will
        // be updated.
        iterator.m_state = Nav2RouteIterator.ST_DATUMDONE;
        // iRdAction will be read
        iterator.iBAIS.setPos(m_firstDatumBufPos);
        iterator.nextPoint(false);
    } // setIteratorToFirstPoint


    /**
     * uses the values of speed, streetname and flags to set the
     * iterator to a faked track point just before the mini map
     * border. ix, iy are set to 0 because we don't have x,y in the
     * preceeding map's coordinate system. This means that aIter
     * should only be used to remember last position, used to scan
     * forward for WPTs etc.
     *
     * Don't call this on the very first mini map.
     */
    public void setIteratorToPrevPoint(Nav2RouteIterator iterator) {
        iterator.m_currentMiniMap = m_prevMiniMap;
        iterator.m_x = 0;
        iterator.m_y = 0;
        iterator.iSpeedLimit = m_lastSpeedBefore;
        // if first datum is a wpt aIter.iWPT will be set from it and
        // otherwise it is not interesting
        iterator.iStreetName = m_lastStreetName;
        // TDL data only valid if on a TDL-datum which this is not
        iterator.m_state = Nav2RouteIterator.ST_DATUMDONE;
        // fake
        iterator.iRdAction = Action.TRACK;

        // if aIter.nextPoint() is called it should see the origo.
        iterator.iBAIS.setPos(m_firstDatumBufPos
                           - 2 * Nav2RouteParser.NBR_BYTES_PER_DATUM);
    }


    /**
     * <p>Create a MiniMap by parsing Nav2 route data.</p>
     * 
     * <p>Assumes that aWFDIS is positioned at the first byte following
     * an action short of type origo (0x8000) and parse the origo
     * datum and the following scale. Does not hold any reference to
     * aWFDIS after the method has determined.</p>
     *
     * <p>After termination aWFDIS will be positioned after the last byte
     * of the SCALE datum.</p>
     *
     * @param aPrevMiniMap is allowed to null (first MiniMap in route)
     * @param firstDatumBufPos - byte offset of first datum after the MiniMap.
     *        Must be the offset of the
     *        action uint + 2 * Nav2RouteParser.NBR_BYTES_PER_DATUM.  
     */
    MiniMap(WFDataInputStream aWFDIS,
            MiniMap aPrevMiniMap,
            int aLastSpeed,
            int aLastFlags,
            String aLastStreetName,
            int firstDatumBufPos) 
        throws IOException {

        m_lastSpeedBefore = aLastSpeed;
        m_lastFlagsBefore = aLastFlags;
        m_lastStreetName = aLastStreetName;

        aWFDIS.readShort(); // next_origo, skipped because undefined
                            // for last MiniMap (server bug?)
        m_origoX = aWFDIS.readInt();
        m_origoY = aWFDIS.readInt();
        
        if (! Action.isScale(aWFDIS.readUnsignedShort())) {
            throw new IOException("MiniMap.MiniMap(): no SCALE after ORIGO");
        }
        m_prevPointX = aWFDIS.readShort();
        m_prevPointY = aWFDIS.readShort();

        // YES, in this order, see Nav2:C++/Modules/NavTask/RouteDatum.h
        // MetaPointScale::fill()
        // spec is currently not updated
        int scale_x2 = aWFDIS.readUnsignedShort();
        long scale_x1 = aWFDIS.readUnsignedInt();

        // scale_x2 has max 16 bits of value
        // Nav2:C++/Modules/NavTask/Point.h Point::getScaleX()
        m_scaleX = ((float) scale_x1) +  (((float) scale_x2) / 0x10000);

        m_xToMC2Factor = 4294967296.0f / (2 * (float) Math.PI * m_scaleX);

        m_firstDatumBufPos = firstDatumBufPos;

        if (aPrevMiniMap != null) {
            aPrevMiniMap.setNext(this);
        }
        m_prevMiniMap = aPrevMiniMap;
    } // MiniMap(short[], int)


    /**
     * Returns a string representation for debugging use.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(100);

        sb.append("wmmg.data.route.MiniMap {");
        sb.append("iOrigoX: ");
        sb.append(m_origoX);
        sb.append(" iOrigoY: ");
        sb.append(m_origoY);
        sb.append(" iScaleX: ");
        sb.append(m_scaleX);
        sb.append(" prevPointX: ");
        sb.append(m_prevPointX);
        sb.append(" prevPointY: ");
        sb.append(m_prevPointY);
        sb.append(" data range [");
        sb.append(m_firstDatumBufPos);
        sb.append("]}");

        return sb.toString();
    }
}
