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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class PositionTest extends TestCase {
    public void testCreation() {
        Position p = new Position(608453664, 0); // approx Greenwich, UK
        assertTrue(p.isValid());
    }

    public void testCreateInvalid() {
        Position p = new Position(0,0);
        assertFalse(p.isValid());
        
        p = new Position();
        assertFalse(p.isValid());
    }

    public void testCreateMalmoe() {
        // where Jesper lives
        Position p = Position.createFromDecimalDegrees(55.61, 13.00);
        assertTrue(p.isValid());
    }


    public void testGetters() {
        final int mc2lat = 1234567;
        final int mc2lon = 9876543;
        Position p = new Position(mc2lat, mc2lon);
        assertEquals(mc2lat, p.getMc2Latitude());
        assertEquals(mc2lon, p.getMc2Longitude());
    }

    // private static final double TOL = 1E-9; // tolerance of error  
    private static final double EARTH_CIRCUMFERENCE = 2 * Math.PI * 6378137.0; 
    
    private static final double DEGREES_PER_METER =
        360 / EARTH_CIRCUMFERENCE;

    private static final double RADIANS_PER_METER =
        2 * Math.PI / EARTH_CIRCUMFERENCE;
    
    // The achievable tolerance is limited by the Position class using MC2
    // internally. Thus any unit less than 9mm will be lost in the constructor
    // anyway.
    private static final double TOL_ABS_DEG = 0.01 * DEGREES_PER_METER; 
    private static final double TOL_ABS_RAD = 0.01 * RADIANS_PER_METER;
    
    public void testMc2ToDecimalDegrees() {
        Position p = new Position(663453142, 155096041);
        double deglat = p.getDecimalLatitude();
        double deglon = p.getDecimalLongitude();
        checkAbsoluteError(55.61, deglat, TOL_ABS_DEG);
        checkAbsoluteError(13.00, deglon, TOL_ABS_DEG);
    }

    public void testMc2ToRadians() {
        Position p = new Position(663453142, 155096041);
        double radlat = p.getRadiansLatitude();
        double radlon = p.getRadiansLongitude();
        checkAbsoluteError(55.61*Math.PI/180, radlat, TOL_ABS_RAD);
        checkAbsoluteError(13.00*Math.PI/180, radlon, TOL_ABS_RAD);        
    }


    private static void checkRelativeError(double expected,
                                           double actual,
                                           double relative_tol) {
        double diff = expected - actual;
        double relative_error = diff/expected;
        assertTrue(relative_error < relative_tol);
    }
    
    private static void checkAbsoluteError(double expected,
                                           double actual,
                                           double absolute_tol) {
        double diff = expected - actual;
        assertTrue(Math.abs(diff) < absolute_tol);
    }
    
    public void testReadWrite() throws IOException {
        ByteArrayOutputStream byteInput = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(byteInput);        
        Position pos;
        pos = new Position(663453142, 155096041);
        pos.write(dout);
        pos = new Position(0, -155096041);
        pos.write(dout);
        pos = new Position(-450, 0);
        pos.write(dout);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteInput.toByteArray()));
        pos.read(din);
        assertEquals(663453142, pos.getMc2Latitude());
        pos.read(din);
        assertEquals(-155096041, pos.getMc2Longitude());
        pos.read(din);
        assertEquals(0, pos.getMc2Longitude());
    }
    
}
