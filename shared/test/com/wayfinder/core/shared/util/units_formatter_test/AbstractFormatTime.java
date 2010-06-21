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

package com.wayfinder.core.shared.util.units_formatter_test;

import com.wayfinder.core.shared.util.UnitsFormatter;
import com.wayfinder.core.shared.util.UnitsFormatterSettings;
import com.wayfinder.core.shared.util.UnitsFormatterSettingsEN;

import junit.framework.TestCase;

public abstract class AbstractFormatTime extends TestCase {

    public AbstractFormatTime(String name) {
        super(name);
    }


    // ------------------------------------------------------------------
    // not real members but set up before each test case, so we
    // break the normal file organization

    protected UnitsFormatter m_uf1;
    protected UnitsFormatter m_uf2;

    private final int UNIT_SYSTEM = UnitsFormatterSettings.UNITS_METRIC; 

    protected void setUp() throws Exception {
        super.setUp();
        UnitsFormatterSettings ufs = new UnitsFormatterSettingsEN(UNIT_SYSTEM);
        m_uf1 = new UnitsFormatter(ufs);
        m_uf2 = new UnitsFormatter(ufs);
    }


    // ------------------------------------------------------------------
    // helpers

    protected void test(String hourStr, String minStr, String secStr) {
        int h = Integer.parseInt(hourStr, 10);
        int m = Integer.parseInt(minStr, 10);
        int s = Integer.parseInt(secStr, 10);

        testFormatting(toSeconds(h, m, s), hourStr, minStr, secStr);
    }

    /**
     * No overflow checks.
     * 
     */
    protected int toSeconds(int h, int m, int s) {
        return h * 3600 + m * 60 + s;
    }


    /**
     * <p>Test formatting with and without seconds.</p>
     * 
     * <p>Sub classes must implement this method so that it format
     * totalSeconds into a formatted time string, using the correct separators,
     * and use one of the assertation functions to compare the result.</p>
     * 
     * <p>Testing must be done both with and without including seconds in
     * the formatted string.</p>
     * 
     * @param seconds - time in number of seconds since 00:00:00.
     * @param hourStr - the expected string for hours.
     * @param minStr - the expected string for minutes.
     * @param secStr - the expected string for seconds.
     */
    protected abstract void testFormatting(int totalSeconds,
                                           String hourStr,
                                           String minStr,
                                           String secStr);


    // ------------------------------------------------------------------
    // actual test cases
  
    public void test000000() {
        test("00", "00", "00");
    }

    public void test154248() {
        test("15", "42", "48");
    }

    public void test235959() {
        test("23", "59", "59");
    }

    public void test085070() {
        testFormatting(toSeconds(8, 50, 70), "08", "51", "10");
    }

    public void test087350() {
        testFormatting(toSeconds(8, 73, 50), "09", "13", "50");
    }

    public void testHoursGt24() {
        // according to spec, the formatter does not wrap around the
        // 24H limit - because the intended use is to show elapsed time,
        // not clock time.
        test("30", "05", "01");
    }
}
