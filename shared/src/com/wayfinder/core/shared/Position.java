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

package com.wayfinder.core.shared;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.network.internal.mc2.MC2Element;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.route.RoutePointRequestable;
import com.wayfinder.core.shared.util.WFMath;
import com.wayfinder.core.shared.util.WFUtil;
import com.wayfinder.core.shared.xml.XmlWriter;

/**
 *
 *  Class for holding a MC2 position.
 *  
 *  <h2>Summary</h2>
 *  <p>The MC2 coordinate system can be used as an integer representation
 *  of geographical coordinates (usually WGS84). Like any spherical system, it
 *  measures angles, not distances. To measure distance, you must compensate for
 *  the fact that longitude lines converge at the Earth's poles. The accuracy is
 *  at least 0,01m, thus map and GPS errors are always greater than the rounding
 *  error unless you multiply with large constants.</p>
 *  
 *  <h2>Gory details</h2>
 *  <p>The MC2 coordinate system is a spherical coordinate system with two
 *  dimensions, latitude and longitude. Latitude zero is the equator and
 *  longitude zero is the longitude near Greenwich. Positive axis are north and
 *  east.</p>
 *  
 *  <p>The latitude range is [Integer.MIN_VALUE/2, Integer.MAX_VALUE/2]
 *  corresponding to 90°S and 90°N. The longitude range is
 *  [Integer.MIN_VALUE, Integer.MAX_VALUE] corresponding to 180°W and almost
 *  180°E (since abs(Integer.MIN_VALUE) > Integer.MAX_VALUE).</p>
 *  
 *  <p>1 MC2 unit covers the same <i>angular</i> distance on both sides of the
 *  equator and both sides of the zero longitude. The least accuracy is around
 *  the equator and can be approximately calculated as 40075 km / 2^32 = 0,01m.</p> 
 *  
 *  <p>There is no assumption on what Geodetic System is used but Wayfinder APIs
 *  use WGS84, since that is what the GPS system use.</p>
 *  
 *  <h2>General warning</h2>
 *  <p>Avoid using coordinates near the end points of the valid range.
 *  Due to rounding this might result in valid input yielding invalid
 *  coordinates for output.</p>
 *  
 *  <p>Since the extreme points do not cover land where we will provide navigation
 *  services for the forseeable future, we currently do not intend to tighten
 *  this.</p>
 */
public class Position implements RoutePointRequestable, MC2Element, Serializable {

    public static final Position NO_POSITION = new Position(0,0);
    
    /**
     * Latitude in MC2
     */
    private int m_mc2Latitude;
    
    /**
     * Longitude in MC2
     */
    private int m_mc2Longitude;

    
    /**
     * 
     * @param mc2Latitude - the latitude in MC2 coordinates.
     * @param mc2Longitude - the longitude in MC2 coordinates.
     */
    public Position(int mc2Latitude, int mc2Longitude) {
        m_mc2Latitude = mc2Latitude;
        m_mc2Longitude = mc2Longitude;
    }
    
    
    /**
     * Creates a new invalid position
     */
    public Position() {
    }

    
    /**
     * Returns the latitude of this position.
     * 
     * @return the latitude of this position.
     */
    public int getMc2Latitude() {
        return m_mc2Latitude;
    }

    /**
     * Returns the longitude of this position.
     * 
     * @return the longitude of this position.
     */
    public int getMc2Longitude() {
        return m_mc2Longitude;
    }


    /**
     * <p>Checks a latitude/longitude pair for validity according to specification
     * above.</p>
     * 
     * <p>In addition the coordinate (0,0) is defined as invalid since it
     * is normally the result of an uninitialized system or unknown position.
     * (0,0) is far off Africa's west coast and we don't have any navigation
     * service there.</p>
     * 
     * <p>Reference: mc2:Shared/include/MC2Coordinate.h 2006-12-05:
     * MC2Coordinate.isValid().</p>
     * 
     * @param mc2Latitude - the latitude to be checked for validity.
     * @param mc2Longitude - the longitude to be checked for validity.
     * 
     * @return true if the position is valid and false otherwise.
     */
    public static boolean isValid(int mc2Latitude, int mc2Longitude) {
        if (mc2Latitude == 0 && mc2Longitude == 0) {
            return false;
        }
        
        // Note: compared to mhedlunds original code, this code considers
        // the Integer.MIN_VALUE/2 to be valid like MC2.
        if ((mc2Latitude >= Integer.MIN_VALUE / 2)
            && (mc2Latitude <= Integer.MAX_VALUE / 2)) {
            // longitudes use full range, no need to test.
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if this Position is valid.
     * 
     * @return Position.isValid(getMc2Latitude(), getMc2Longitude()).
     */
    public boolean isValid() {
        return isValid(m_mc2Latitude, m_mc2Longitude);
    }


    // -----------------------------------------------------------------------
    // Decimal degrees stuff

    /**
     * Multiply an MC2 coordinate with this value to express it in decimal
     * degrees. Calculated as 360 / 2^32.
     */
    private static final double MC2_TO_DECIMAL_DEGREES =
        360d / WFMath.POW_2_32; // 8.381903171539306E-8, approx 1 / 11930464.7111

    /**
     * <p>Convert a MC2 coordinate to a value in decimal degrees.</p>
     * 
     * <p>We use negative decimal degrees for west longitudes and south latitudes
     * and positive for east and north. The range is [-180, +180] for longitudes
     * and [-90, +90] for latitudes. Because of rounding we don't guarantee
     * that the returned values are strictly within this interval at the
     * extreme points.</p>
     * 
     * <p>Note that this method does not check the validity of the input
     * (subject to change without notice).</p>
     * 
     * @param mc2 - the mc2 coordinate value to be converted.
     * @return the decimal degree value.
     * @see #decimalDegresToMc2(double)
     */
    public static double mc2ToDecimalDegrees(int mc2) {
        return mc2 * MC2_TO_DECIMAL_DEGREES;
    }

    /**
     * <p>Convert a value in decimal degrees to a MC2 coordinate.</p>
     *
     * <p>Note that this method does not check the validity of the input
     * (subject to change without notice).</p>
     * 
     * @param degrees - the decimal degree value to be converted.
     * @return the argument expressed in mc2 coordinates.
     * @see #mc2ToDecimalDegrees(int)
     */
    public static int decimalDegresToMc2(double degrees) {
        return (int) (degrees / MC2_TO_DECIMAL_DEGREES);
    }

    /**
     * @see Position#mc2ToDecimalDegrees(int)
     * @return the latitude in decimal degrees.
     */
    public double getDecimalLatitude() {
        return mc2ToDecimalDegrees(m_mc2Latitude);
    }

    /**
     * @see Position#mc2ToDecimalDegrees(int)
     * @return the longitude in decimal degrees.
     */
    public double getDecimalLongitude() {
        return mc2ToDecimalDegrees(m_mc2Longitude);
    }


    /**
     * <p>Factory method for creating a Position object from two decimal
     * degrees.</p>
     * 
     * <p>We don't define this as a constructor because it could cause confusion
     * on if the double arguments are decimal degrees or radians.</p>
     * 
     * <p>Since the internal representation is MC2, it is not guaranteed that
     * Position.getDecimalLatitude() == decimalLatitude and equivalent for
     * longitudes.</p>
     * 
     * <p>See mc2ToDecimalDegrees() for ranges.
     * This method does not check the validity of the input (subject to change
     * without notice).</p>
     * 
     * @param decimalLatitude - the latitude in decimal degrees.
     * @param decimalLongitude - the longitude in decimal degrees.
     * @return a new Position object.
     */
    public static Position createFromDecimalDegrees(double decimalLatitude,
                                                    double decimalLongitude) {
        final double DECIMAL_TO_MC2 = 1 / MC2_TO_DECIMAL_DEGREES; 
        int lat = (int) (decimalLatitude * DECIMAL_TO_MC2);
        int lon = (int) (decimalLongitude * DECIMAL_TO_MC2);
        return new Position(lat, lon);
    }


    // -----------------------------------------------------------------------
    // Radians stuff
    /**
     * <p>Multiply an MC2 coordinate with this value to express it in radians.</p>
     * 
     * <p>Calculated as 2*Math.PI / 2^32.</p>
     */
    private static final double MC2_TO_RADIANS =
        (2.0 * Math.PI) / WFMath.POW_2_32;

    /**
     * <p>Convert a MC2 coordinate to a value in radians.</p>
     * 
     * <p>We use negative radians for west longitudes and south latitudes
     * and positive for east and north. The range is [-Pi, +Pi] for longitudes
     * and [-Pi/2, +Pi/2] for latitudes. Because of rounding we don't guarantee
     * that the returned values are strictly within this interval at the
     * extreme points.</p>
     * 
     * <p>Note that this method does not check the validity of the input
     * (subject to change without notice).</p>
     * 
     * @param mc2 - the mc2 coordinate value to be converted.
     * @return the decimal degree value.
     */
    public static double mc2ToRadians(int mc2) {
        return mc2 * MC2_TO_RADIANS;
    }

    /**
     * <p>Convert a value in radians to a MC2 coordinate.</p>
     *
     * <p>Note that this method does not check the validity of the input
     * (subject to change without notice).</p>
     * 
     * @param radians - the radians value to be converted.
     * @return the argument expressed in mc2 coordinates.
     * @see Position#mc2ToRadians(int)
     */
    public static int radiansToMc2(double radians) {
        return (int) (radians / MC2_TO_RADIANS);
    }


    /**
     * @see Position#mc2ToRadians(int)
     * @return the latitude in radians.
     */
    public double getRadiansLatitude() {
        return mc2ToRadians(m_mc2Latitude);
    }

    /**
     * @see Position#mc2ToRadians(int)
     * @return the longitude in radians.
     */
    public double getRadiansLongitude() {
        return mc2ToRadians(m_mc2Longitude);
    }

    
    /**
     * Returns the distance between this position and the supplied Position
     * <p>The calculations are done as if you can't cross the 180° meridian or
     * the go over the poles.
     * Thus, if one position is at 150°E and the other at 150°W, the distance
     * will be calculated as if you travel 300°, instead of 60°. This is
     * ok since we don't provide navigation in those areas on Earth (Bering Sea,
     * Marshall Islands etc.) but this is an issue if you let users measure
     * distances freely on the map.</p>  
     * 
     * <p>MC2 are spherical coordinates. For speed, we use the
     * cos((lat1-lat2)/2)-formula and not the more exact great-circle or
     * rhumb line distance (which is how you would travel on a mercator
     * projection map).</p>
     * 
     * @see WFUtil#distancePointsMeters(Position, Position)
     * 
     * @param p The position to measure the distance to
     * @return The distance in meters
     */
    public int distanceTo(Position p) {
        return WFUtil.distancePointsMeters(this, p);
    }
    
    
    public Position getPosition() {
        return this;
    }
    
    public void write(MC2Writer mc2w) throws IOException {
        mc2w.startElement(MC2Strings.tposition_item);
        mc2w.attribute(MC2Strings.aposition_system, 
                MC2Strings.MC2);
        mc2w.elementWithText(MC2Strings.tlat, getMc2Latitude());
        mc2w.elementWithText(MC2Strings.tlon, getMc2Longitude());
        mc2w.endElement(MC2Strings.tposition_item);
    }
    

    public void parse(MC2Parser mc2p) throws IOException, MC2ParserException {
        /*<!-- Position Item -->
        <!ELEMENT position_item ( lat, lon, angle? )>
        <!ATTLIST position_item position_system %position_system_t; #REQUIRED>
        <!-- latitude and longitude WSG84 format: (N|S|E|W) D(D*)MMSS[.ddd] -->
        <!ELEMENT lat ( #PCDATA )>
        <!ELEMENT lon ( #PCDATA )>
        <!ELEMENT angle ( #PCDATA ) >*/

        // has to be at element <<lat>>
        //skip the check for the position system 
        mc2p.childrenOrError();
        m_mc2Latitude = mc2p.valueAsInt();
        mc2p.advanceOrError();
        // has to be at element <<lon>>
        m_mc2Longitude = mc2p.valueAsInt();
        
        while( mc2p.advance());
        //mc2parser.getXmlIterator().skipSiblings();//don't need angle
        mc2p.nameOrError(MC2Strings.tposition_item);
    }
    //public void 
    // -----------------------------------------------------------------------
    /**
     * for debugging use.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(40); // 2*10 + 20;
        sb.append("Position(").append(m_mc2Latitude).append(", ");
        sb.append(m_mc2Longitude).append(")");
        
        return sb.toString();
    }


    public void read(DataInputStream din) throws IOException {
        m_mc2Latitude = din.readInt();
        m_mc2Longitude = din.readInt();
    }

    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(m_mc2Latitude);
        dout.writeInt(m_mc2Longitude);
    }


}
