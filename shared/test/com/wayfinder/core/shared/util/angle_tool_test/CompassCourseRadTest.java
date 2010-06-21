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

package com.wayfinder.core.shared.util.angle_tool_test;

import junit.framework.TestCase;

import com.wayfinder.core.shared.util.AngleTool; 

public class CompassCourseRadTest extends TestCase {
    private static final double TOL_ABS = 1E-15;

    public void testZero() {
        double course = AngleTool.compassCourseRad(0, 0);
        assertEquals(0, course, TOL_ABS);
    }
    
    public void testNorth() {
        double course = AngleTool.compassCourseRad(0, 20);
        assertEquals(0, course, TOL_ABS);
    }

    public void testEast() {
        double course = AngleTool.compassCourseRad(5, 0);
        assertEquals(Math.PI/2, course, TOL_ABS);
    }

    public void testSouth() {
        double course = AngleTool.compassCourseRad(0, -1);
        assertEquals(Math.PI, course, TOL_ABS);
    }

    public void testWest() {
        double course = AngleTool.compassCourseRad(-1000, 0);
        assertEquals(3*Math.PI/2, course, TOL_ABS);
    }

    public void testNE() {
        double course = AngleTool.compassCourseRad(20, 20);
        assertEquals(Math.PI/4, course, TOL_ABS);        
    }

    public void testSE() {
        double course = AngleTool.compassCourseRad(20, -20);
        assertEquals(3*Math.PI/4, course, TOL_ABS);        
    }

    public void testSW() {
        double course = AngleTool.compassCourseRad(-20, -20);
        assertEquals(5*Math.PI/4, course, TOL_ABS);        
    }

    public void testNW() {
        double course = AngleTool.compassCourseRad(-20, 20);
        assertEquals(7*Math.PI/4, course, TOL_ABS);        
    }
}
