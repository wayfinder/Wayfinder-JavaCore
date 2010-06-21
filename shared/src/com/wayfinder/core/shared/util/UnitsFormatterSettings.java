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

import com.wayfinder.core.shared.settings.GeneralSettings;

/**
 * <p>Configuration data for {@link UnitsFormatter}. Subclass this to an
 * adapter that serves as a bridge to your localization system.</p>
 * 
 * <p>If you implement this as a mutable object, you are responsible for the
 * thread-safety and you should probably use synchronized getters, cloners and
 * a synchronized block in the constructor to set any default non-final data.
 * This class itself is thread-safe.</p> 
 */
public abstract class UnitsFormatterSettings {

    /**
     * The metric unit system: m, km, km/h etc.
     */
    public static final int UNITS_METRIC = GeneralSettings.UNITS_METRIC;

    /**
     * The miles/feet unit system: miles, feet, miles/h
     */
    public static final int UNITS_MILES_FEET =
            GeneralSettings.UNITS_IMPERIAL_US;
    
    /**
     * Synonym for UNITS_MILES_FEET for legacy code.
     */
    public static final int UNITS_IMPERIAL_US = UNITS_MILES_FEET;

    /**
     * The miles/yards unit system: miles, yards, miles/h 
     */
    public static final int UNITS_MILES_YARDS =
        GeneralSettings.UNITS_IMPERIAL_UK;

    /**
     * Synonym for UNITS_MILES_YARDS for legacy code.
     */
    public static final int UNITS_IMPERIAL_UK = UNITS_MILES_YARDS;

    // DANGER: makes our code depend on the intricate details of the
    // value set in GeneralSettins. We really need an enum here (added to
    // product backlog)
    private static final int UNITS_MAX = GeneralSettings.UNITS_IMPERIAL_US;
    
    /**
     * The unit system of this configuration bundle. One of the
     * UNITS_-constants above.
     */
    private final int m_unitSystem;


    /**
     * <p>Creates a new UnitsFormatterConfig.</p>
     * 
     * <p>The new object will not have its strings initialized. Other users
     * than inheritors should use the static factory method instead.</p>
     * 
     * @param unitSystem - one of the UNITS_-constants.
     * 
     * @see UnitsFormatterSettings#createDefaultConfig(int)
     */
    protected UnitsFormatterSettings(int unitSystem) {
        if (unitSystem < UNITS_METRIC || unitSystem > UNITS_MAX) {
            throw new IllegalArgumentException("Invalid unit system.");
        }
        m_unitSystem = unitSystem;
    }


    /**
     * <p>Used to provide thread safety to the UnitsFormatter.</p>
     * 
     * <p>This method must return an instance of UnitsFormatterSettings (or
     * a subclass thereof) in which the return values of the get-methods do
     * no change (in a thread-safe way). The implementor is free to decide
     * if this should be done via cloning or some freezing of the object until
     * the formatters who use it all have gone out of scope.</p>
     * 
     * @return a frozen instance of UnitsFormatter.
     * @see com.wayfinder.core.shared.util.UnitsFormatter#UnitsFormatter(UnitsFormatterSettings)
     */
    protected abstract UnitsFormatterSettings getFrozenInstance();


    /**
     * Returns the unit system of this configuration.
     * 
     * @return one of the UNITS_-constants.
     */
    public int getUnitSystem() {
        return m_unitSystem;
    }


    /**
     * Returns the string for long distance. E.g. "miles".
     * 
     * @return the string for long distance.
     */
    public abstract String getLongDistance();
    
    /**
     * Returns the abbreviation for long distance. E.g. "mi", "km".
     * 
     * @return the abbreviation for long distance.
     */
    public abstract String getLongDistanceAbbr();

    /**
     * Returns the string for short distance. E.g. "yards", "feet".
     * 
     * @return the string for short distance.
     */
    public abstract String getShortDistance();

    /**
     * Returns the abbreviation for short distance. E.g. "yds", "ft", "m".
     * 
     * @return the abbreviation for short distance.
     */
    public abstract String getShortDistanceAbbr();

    /**
     * Returns the localized decimal marker.
     * 
     * @return the localized decimal marker.
     */
    public abstract String getDecimalMarker();

    /**
     * Returns the localized abbreviation for hours, e.g. "h".
     *
     * @return  the localized abbreviation for hours
     */
    public abstract String getHoursAbbr();

    /**
     * Returns the localized abbreviation for minutes, e.g. "m".
     *
     * @return the localized abbreviation for minutes
     */
    public abstract String getMinutesAbbr();

    /**
     * Returns the localized abbreviation for seconds, e.g. "s".
     *
     * @return the localized abbreviation for seconds
     */
    public abstract String getSecondsAbbr();

    /**
     * Returns the localized abbreviation for speed, e.g. "km/h", "mph"
     *
     * @return localized abbreviation for speed
     */
    public abstract String getSpeedAbbr();
}
