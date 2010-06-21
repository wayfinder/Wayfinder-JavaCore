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

public class CompassMinAbsAngleDiffRadTest extends TestCase {
    public void testBothParamsZero() {
        assertEquals(0.0d, AngleTool.compassMinAbsAngleDiffRad(0, 0), 0);
    }
    
    public void testEitherParamIsZero() {
        final double ANGLE = Math.PI/5;
        
        assertEquals(ANGLE, AngleTool.compassMinAbsAngleDiffRad(ANGLE, 0), 0);
        assertEquals(ANGLE, AngleTool.compassMinAbsAngleDiffRad(0, ANGLE), 0);
    }
    
    public void testEqualParams() {
        final double ANGLE = Math.PI/5;
        
        assertEquals(0, AngleTool.compassMinAbsAngleDiffRad(ANGLE, ANGLE), 0);        
    }
    
    public void testAcute() {
        final double ANGLE1 = Math.PI/4;
        final double ANGLE2 = Math.PI/2;

        assertEquals(Math.PI/4,
                     AngleTool.compassMinAbsAngleDiffRad(ANGLE1, ANGLE2),
                     0);
    }

    /**
     * In this test the first angle has a higer magnitude.
     */
    public void testAcuteReversed() {
        final double ANGLE1 = Math.PI/4;
        final double ANGLE2 = Math.PI/2;

        assertEquals(Math.PI/4,
                     AngleTool.compassMinAbsAngleDiffRad(ANGLE2, ANGLE1),
                     0);
        
    }

    /**
     * angle2 - angle1 > Pi so (2 * Pi - diff) should be returned.
     */
    public void testAcuteAcrossZero() {
       final double ANGLE1 = Math.PI/4;
       final double ANGLE2 = 7*Math.PI/4;
       final double anglediff = ANGLE2 - ANGLE1;
       
       
       assertEquals(2*Math.PI - anglediff,
               AngleTool.compassMinAbsAngleDiffRad(ANGLE1, ANGLE2),
               0);
    }
}
