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

public class LogTest extends TestCase {
    private static final double LOG_ABS_ERROR = 1E-13;
    
    public void testNaN() {
        double r = WFMath.log(Double.NaN);
        assertTrue(Double.isNaN(r));
    }
    
    public void testNegative() {
        double r = WFMath.log(-3);
        assertTrue(Double.isNaN(r));
    }
    
    public void testPositiveInfinity() {
        double r = WFMath.log(Double.POSITIVE_INFINITY);
        assertEquals(r, Double.POSITIVE_INFINITY, 0);        
    }
    
    public void testZero() {
        double r = WFMath.log(0);
        assertEquals(r, Double.NEGATIVE_INFINITY, 0);
    }

    public void testLog1() {
        assertEquals(0, WFMath.log(1), LOG_ABS_ERROR);
    }

    public void testLog2() {
        assertEquals(0.6931471805599453, WFMath.log(2), LOG_ABS_ERROR);
    }

    /**
     * log(E) is only accurate to 1E-13.
     */
    public void testLogE() {
        assertEquals(1, WFMath.log(Math.E), LOG_ABS_ERROR);
    }

    public void testLog10() {
        assertEquals(2.302585092994045, WFMath.log(10), LOG_ABS_ERROR);
    }

    public void testLog1E6() {
        assertEquals(13.81551055796427, WFMath.log(1E6), LOG_ABS_ERROR);
    }

    public void testLog1E8() {
        assertEquals(18.42068074395236, WFMath.log(1E8), LOG_ABS_ERROR);
    }

    public void testLog1E9() {
        assertEquals(20.72326583694641, WFMath.log(1E9), LOG_ABS_ERROR);
    }
    
    public void testLog1234567890() {
        assertEquals(20.93398685916206, WFMath.log(1235467890), 1E-3);
    }
}
