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

package com.wayfinder.core.shared.util.wfmath_test;

import com.wayfinder.core.shared.util.WFMath;

import junit.framework.TestCase;

public class SinhTest extends TestCase {

    /**
     * this accuracy is achieved around x == 1.
     */
    private static final double SINH_ABS_ERROR = 1E-12;
    
    public void testZero() {
        assertEquals(0, WFMath.sinh(0), SINH_ABS_ERROR);
    }
    
    /**
     * From <a href="http://mathworld.wolfram.com/HyperbolicSine.html">http://mathworld.wolfram.com/HyperbolicSine.html</a>.
     *
     * Implementation in jWMMG:src2/Shared/com/wayfinder/util/WFMath.java r1.1
     * fails this test.
     */
    public void testLnGoldenRatio() {
        double x = 0.481211825059603447; // ln((1 + sqrt(5))/2)
        assertEquals(0.5, WFMath.sinh(x), SINH_ABS_ERROR);
        assertEquals(-0.5, WFMath.sinh(-x), SINH_ABS_ERROR);
    }

    /**
     * From
     * N. J. A. Sloane, (2008), The On-Line Encyclopedia of Integer Sequences,
     * http://www.research.att.com/~njas/sequences/A073742
     *
     * Implementation in jWMMG:src2/Shared/com/wayfinder/util/WFMath.java r1.1
     * fails this test.
     */
    public void testSinhOf1() {
        assertEquals(1.17520119364380145688238185059, WFMath.sinh(1),
                     SINH_ABS_ERROR);
        assertEquals(-1.17520119364380145688238185059, WFMath.sinh(-1),
                SINH_ABS_ERROR);
    }


    // expected:<3.62686040784 7018>
    // but was: <3.62686040784 2667> 
    public void testSinhOf2WithBadAccuracy() {
        double correct = 3.626860407847018;
        
        assertEquals(correct, WFMath.sinh(2), correct*1E-11);
    }

    public void testSinhOf10() {
        double correct = 11013.23287470339; 
        
        assertEquals(correct, WFMath.sinh(10), correct*1E-2);
    }
}
