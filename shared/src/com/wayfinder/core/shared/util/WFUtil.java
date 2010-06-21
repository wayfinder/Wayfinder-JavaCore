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

package com.wayfinder.core.shared.util;

import java.io.UnsupportedEncodingException;

import com.wayfinder.core.shared.Position;

/**
 * collection of misc utility functions that don't belong anywhere
 * else but has re-use potential throughout the system.
 */
public abstract class WFUtil {

    /**
     * <p>Replace the first occurence of a substring with the given replacement.</p>
     * 
     * <p>This is a simpler, non-regexp, version of String.replaceFirst() which
     * is in Java 1.5 but not in CLDC.</p>
     * 
     * <p>If <code>sourceString.indexOf(oldString) > 0</code>, create a new
     * string which will be sourceString but with the first occurrence of
     * oldString replaced with newString.</p>
     * 
     * <p>If oldString is not a substring of sourceString, just return the
     * reference to sourceString.</p>
     *  
     * @param sourceString the string to process.
     * @param oldString the string to replace.
     * @param newString the string to insert.
     * @return a string derived from sourceString by replacing first occurrence
     *         of oldString with newString.
     * @throws IllegalArgumentException if any parameter is null.
     */
    public static String replace(String sourceString, String oldString, String newString) throws IllegalArgumentException {
        if (sourceString == null || oldString == null || newString == null) {
            throw new IllegalArgumentException();
        }
        
        int index = sourceString.indexOf(oldString);
        
        // Not found, do nothing
        if (index == -1) {
            return sourceString;
        }
        
        // this can be made more efficient by extracting the char array once.
        StringBuffer buffer = new StringBuffer(sourceString.length()
                                               + newString.length());
        buffer.append(sourceString.substring(0, index));
        buffer.append(newString);
        buffer.append(sourceString.substring(index + oldString.length()));
        
        return buffer.toString();
    }

    
    /**
     * <p>Returns the index within this string of the first occurrence of the
     * specified character, starting the search at the specified index.</p> 
     * 
     * <p>CLDC 1.1 doesn't implement <code>StringBuffer.indexOf()</code> from
     * Java 5.0. This method solves that but in a less general way.
     * We often want to use string buffers instead of creating a lot of temp
     * strings.</p>
     *
     * <p>Returns the smallest value, <i>k</i> such that
     * <pre>(stringBuffer.charAt(k) == ch) && (k >= fromIndex)</pre>
     *
     * @param stringBuffer - the StringBuffer to search. Must not be null.
     * @param ch - the character to search for.
     * @param fromIndex - the index of the first character to examine. If it is
     * less than 0, the search will start from the beginning of the string as if
     * the parameter was 0.
     * @return the index of first occurence of ch, or -1 if it can't be found.
     */
    public static int stringBufferIndexOf(StringBuffer stringBuffer,
                                          char ch,
                                          int fromIndex) {
        int k = fromIndex < 0 ? 0 : fromIndex;
        int n = stringBuffer.length();
        for(; k < n && stringBuffer.charAt(k) != ch; k++);
        if (k == n) { // searched thru whole string without match
            return -1;
        }

        return k;
    }


    private static final String ENCODING_NAME_UTF_8 = "UTF-8";
    
    /**
     * <p>Convert bytes representing a string encoded in UTF-8 to a
     * {@link String} object.</p>
     * 
     * <p>With this method you don't need to bother with the checked exception
     * {@link UnsupportedEncodingException}, since it is converted into an
     * {@link Error} instead. The reason for this is that our application will
     * anyway not be able to recover from the problem. Much client-server
     * communication assumes UTF-8.</p>
     * 
     * <p>This method does not fully work on Sony Ericsson JP-7. See Eventum
     * #2409, #2231, #2365. The work-around code has not yet been migrated from
     * jWMMG.</p>
     * 
     * @param buf - the bytes to be decoded.
     * @param off - the index of the first byte to decode.
     * @param len - the number of bytes to decode.
     * @return a new String.
     * @see String#String(byte[], int, int, String)
     */
    public static String UTF8BytesToString(byte[] buf, int off, int len) {
        try {
            return new String(buf, off, len, ENCODING_NAME_UTF_8); 
        } catch (UnsupportedEncodingException e) {
            throw new Error("Encoding \"" + ENCODING_NAME_UTF_8
                            + "\" not supported: " + e.getMessage());
        }
    }


    // -----------------------------------------------------------------------
    // simple distance calculation methods

    /**
     * <p>The radius of the Earth. Approximate 6.37E6</p>
     * 
     * <p>from
     * Nav2:C++/Targets/MapLib/Shared/include/GfxConstants.h and
     * Nav2:C++/Targets/MapLib/Shared/src/GfxConstants.cpp</p>
     */
    public static final int EARTH_RADIUS = 6378137;

    
    /**
     * One unit in the MC2 system is this many meters (for all
     * latitudes and at the equator for longitudes).
     *
     * approx 9.3307E-3
     */
    public static final double MC2SCALE_TO_METER =
        (2 * EARTH_RADIUS * Math.PI) / WFMath.POW_2_32;

    /**
     * <p>Returns the distance in meters between two positions. The
     * parameter set is for compatibility with legacy code.</p>
     * 
     * See {@link WFUtil#distancePointsMeters(Position, Position)} for
     * calculation details.
     * 
     * <p>The jWMMG version returned float. int actually gives you better
     * precision for very long distances. Circumference of Earth is 4.075E7 m.
     * Submeter precision is not considered relevant.</p>
     * 
     * @param lon1 - longitude in MC2 coordinates of first position.
     * @param lat1 - latitude in MC2 coordinates of first position.
     * @param lon2 - longitude in MC2 coordinates of second position.
     * @param lat2 - latitude in MC2 coordinates of second position.
     * @return the distance in meters.
     */
    public static int distancePointsMeters(int lon1, int lat1,
                                           int lon2, int lat2) {
        /*
         * if the lats are sent in correctly, the sum is max magnitude
         * Integer.MIN_VALUE,
         * 
         * Worst case: worst case (2 broken coordinates)
         * lat2 == lat1 == Integer.MIN_VALUE. Then sum is 2*Integer_MIN_VALUE
         * and sum/2 is Integer.MIN_VALUE and we are still ok.
         * 
         * but since we in Position don't guarantee that the conversion
         * functions keep strictly within the boundaries, we wan't to avoid
         * the risk for overflow.
         */
        int mid_lat = (int) (((long) lat1 + lat2)/2);
        double cos_lat = Math.cos(Position.mc2ToRadians(mid_lat));

        /*
         * same reason for long as above. If same worst case as above, then
         * delta_lat is 2*Integer.MIN_VALUE (-2^32) and then delta_lat^2 is
         * 2^64 which will not fit in a long.
         * 
         * If we want to get rid of the FP subtraction and one squaring,
         * we could to divide both deltas with 2 and multiply the result. If
         * we do that before multiplying with MC2SCALE_TO_METER (< 1) , accuracy
         * is still good enough.   
         *  
         */
        double delta_lat = (long) lat2 - lat1;
        double delta_lon_normalized = ((long) lon2 - lon1) * cos_lat;

        /*
         * 
         */
        return (int) (MC2SCALE_TO_METER
                        * Math.sqrt(delta_lat * delta_lat
                                    + delta_lon_normalized
                                      * delta_lon_normalized));
    }

    /**
     * Returns the distance in meters between two positions.
     * 
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
     * @param p1 - the first position.
     * @param p2 - the second position.
     * @return the distance in meters.
     * @see WFUtil#distancePointsMeters(int, int, int, int)
     */
    public static int distancePointsMeters(Position p1, Position p2) {
        return distancePointsMeters(p1.getMc2Longitude(), p1.getMc2Latitude(),
                                    p2.getMc2Longitude(), p2.getMc2Latitude());
    }
    
    public static void insertionSort(Object[] array, Comparator comp) {
        for(int p=1; p < array.length; p++) {
            Object tmp = array[p];
            // System.out.println("outer loop " + p + " " + tmp);

            int j = p;
            for(; j > 0 && (comp.compare(tmp, array[j - 1]) < 0); j--) {
                // System.out.println("inner loop " + j);
                array[j] = array[j-1];
            }
            array[j] = tmp;
        }
    }
}
