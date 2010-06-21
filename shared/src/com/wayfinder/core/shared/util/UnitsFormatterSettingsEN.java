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

/**
 * <p>Default implementation of {@link UnitsFormatterSettings} which returns
 * compile time strings for English. This is intended for testing and
 * quick start use.</p>
 * 
 * <p>This class is thread-safe since it has no instance data of its own.</p>
 */
public final class UnitsFormatterSettingsEN extends UnitsFormatterSettings {

    /**
     * Create a new instance.
     * 
     * @param unitSystem - the unit system to use.
     * @see UnitsFormatterSettings#UnitsFormatterSettings(int)
     */
    public UnitsFormatterSettingsEN(int unitSystem) {
        super(unitSystem);
    }

    protected UnitsFormatterSettings getFrozenInstance() {
        return this; // we have no mutable data 
    }
    
    /**
     * Returns the string for long distance. E.g. "miles".
     * 
     * @return the string for long distance.
     */
    public synchronized String getLongDistance() {
        if (getUnitSystem() == UNITS_MILES_FEET
            || getUnitSystem() == UNITS_MILES_YARDS) {
            // STR_MILES
            return "miles";
        } else {
            // use metric as default for unknowns.
            // KILOMETERS
            return "km";
        }
    }
    
    /**
     * Returns the abbreviation for long distance. E.g. "mi", "km".
     * 
     * @return the abbreviation for long distance.
     */
    public synchronized String getLongDistanceAbbr() {
        if (getUnitSystem() == UNITS_MILES_FEET
            || getUnitSystem() == UNITS_MILES_YARDS) {
            // STR_MILES_ABBREVIATION
            return "mi";
        } else {
            // use metric as default for unknowns.
            // KILOMETERS
            return "km";
        }
    }

    /**
     * Returns the string for short distance. E.g. "yards", "feet".
     * 
     * @return the string for short distance.
     */
    public synchronized String getShortDistance() {
        switch (getUnitSystem()) {
        case UNITS_MILES_FEET:
            // STR_FEET
            return "feet";

        case UNITS_MILES_YARDS:
            // STR_YARDS
            return "yards";
            
        default: // UNITS_METRIC and default
            // STR_METERS_ABBREVIATION
            return "m";
        }
    }

    /**
     * Returns the abbreviation for short distance. E.g. "yds", "ft", "m".
     * 
     * @return the abbreviation for short distance.
     */
    public synchronized String getShortDistanceAbbr() {
        switch (getUnitSystem()) {
        case UNITS_MILES_FEET:
            // STR_FEET_ABBREVIATION
            return "ft";

        case UNITS_MILES_YARDS:
            // yards_abbr
            return "yds";
            
        default: // UNITS_METRIC and default
            // STR_METERS_ABBREVIATION
            return "m";
        }
    }

    /**
     * Returns the localized decimal marker. Currently, always initialized to
     * "." due to limited font support in old jWMMG-code.
     * (Subject to change without notice.)
     * 
     * @return the localized decimal marker.
     */
    public synchronized String getDecimalMarker() {
        return ".";
    }

    public synchronized String getHoursAbbr() {
        // wf_ROUTEV_ETG_HOURS
        return "h";
    }
    
    public synchronized String getMinutesAbbr() {
        // mins_abbr
        return "m";
    }
    
    public synchronized String getSecondsAbbr() {
        // STR_SECONDS_ABBREVIATION
        return "s";
    }

    public synchronized String getSpeedAbbr() {
        if (getUnitSystem() == UNITS_MILES_FEET
            || getUnitSystem() == UNITS_MILES_YARDS) {
            // wayfinder_mph_text
            return "mph";
        } else {
            // use metric as default for unknowns.
            // wayfinder_kmh_text
            return "km/h";
        }
    }    
}
