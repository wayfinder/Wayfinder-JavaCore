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
 * <p>This class formats distance, speed and time values.</p>
 * 
 * <p>This class is in core because we want rounding and transition between
 * short and long units to be the same across platforms.</p>
 * 
 * <p>This class formats only elapsed time (does not wrap around at 24H) and
 * only in limited ways.</p>
 * 
 * <p>For formatting clock time, please turn to your platform's locale
 * functions instead.</p>
 * 
 *  <p>This class is thread safe.</p>
 */
public class UnitsFormatter {

    /**
     * The configuration to use for formatting.
     */
    // final, so no synchronize needed
    private final UnitsFormatterSettings m_config;

    /**
     * Create a UnitsFormatter with a specified configuration.
     * 
     * @param settings - This method will call settings.getFrozenInstance() and
     * then the caller is free to modify settings as long as the protocol
     * specified in getFrozenInstance() is adhered to. 
     * @see UnitsFormatterSettings#getFrozenInstance()
     */
    public UnitsFormatter(UnitsFormatterSettings settings) {
        m_config = settings.getFrozenInstance();
    }


    /**
     * Formats a distance in meters according to the Wayfinder rounding rules
     * and the chosen unit system.
     * 
     * @param meters - the distance in meters.
     * @return a FormattingResult with the formatted distance.
     */
    public FormattingResult formatDistance(int meters) {
            switch (m_config.getUnitSystem()) {
            case UnitsFormatterSettings.UNITS_METRIC:
                return roundDistanceMetric(meters);

            case UnitsFormatterSettings.UNITS_MILES_FEET:
                return roundDistanceMilesFeet(meters);

            case UnitsFormatterSettings.UNITS_MILES_YARDS:
                return roundDistanceMilesYards(meters);
                
            }
            throw new RuntimeException("formatDistance(): unknown unit system");
    }
    

    /**
     * <p>Round a distance assuming metric units.</p>
     * 
     * <p>Round the distance like the XML Server. This differs from
     * <code>Nav2:DistancePrintingPolicy::convertDistanceMetric()</code> in that
     * distance is rounded to 10m when distance > 100m but
     * convertDistanceMetric() round to 5m until distance is >= 200 m.</p> 
     * 
     * @param meters - the distance to format.
     * @return a FormattingResult with the formatted distance.
     * @see UnitsFormatter#formatDistance(int)
     */
    public FormattingResult roundDistanceMetric(int meters) {
        int absmeters = Math.abs(meters);
        boolean longdistance = false;
        StringBuffer sb = new StringBuffer(15); // "-2000000000"
        
        if ( absmeters < 10){
            // no rounding
            sb.append(absmeters);
        } else if ( absmeters < 100 ) {
            // round to 5 m
            sb.append(WFMath.roundTo(absmeters, 5));
        } else if (absmeters + 5 < 1000 ) {
            // round to 10 m - thus usually 2 digits of precision: 890 m  
            sb.append(WFMath.roundTo(absmeters, 10));
        } else if (absmeters + 50 < 10000 ) {
            // round to 100m, use km - thus usually 2 digits of precision: 1.2 km 
                int hektometers = WFMath.roundTo(absmeters, 100) / 100;
                sb.append(hektometers / 10);
                sb.append(m_config.getDecimalMarker());
                sb.append(hektometers % 10);
                longdistance = true;
        } else {
            // rounded whole km
            sb.append(WFMath.roundTo(absmeters, 1000) / 1000);
            longdistance = true;
        }

        if (meters < 0) {
            sb.insert(0, '-');
        }

        return FormattingResult.createForDistance(m_config,
                sb.toString(),
                longdistance);
    }

    /**
     * <p>Round a distance assuming miles/feet units.</p>
     * 
     * <p>Round the distance like
     * <code>Nav2:DistancePrintingPolicy::convertDistanceImperialFeet()</code></p>
     * 
     * @param meters - the distance to format.
     * @return a FormattingResult with the formatted distance.
     * @see UnitsFormatter#formatDistance(int)
     */
    public FormattingResult roundDistanceMilesFeet(int meters) {
        int absmeters = Math.abs(meters);
        boolean longdistance = false;
        StringBuffer sb = new StringBuffer(15); // "-2000000000"
        float miles = absmeters * METERS_TO_MILES;
        
        if (miles > 10) {
            // use whole miles
            sb.append((int) WFMath.round(miles));
            longdistance = true;
        } else if (miles > 0.15) {
            /*
             * use miles with one decimal
             * 
             * NOTE: different limit from when we're using yards.
             * 0.15 miles = 800 ft (approx)
             */
            
            int decimiles = (int) WFMath.round(10*miles);
            sb.append(decimiles / 10);
            sb.append(m_config.getDecimalMarker());
            sb.append(decimiles % 10);
            longdistance = true;
        } else {
            // use single feet.
            int feet = (int) (absmeters * METERS_TO_FEET);

            if (feet >= 600) {
                // round to 50 ft
                sb.append(WFMath.roundTo(feet, 50));
            } else if (feet >= 400) {
                // round to 25 feet
                sb.append(WFMath.roundTo(feet, 25));
            } else if (feet >= 200) {
                // round to 10 feet
                sb.append(WFMath.roundTo(feet, 10));
            } else if (feet >= 100) {
                // round to 5 feet
                sb.append(WFMath.roundTo(feet, 5));
            } else {
                // output number of feet
                sb.append(feet);
            }
        }

        if (meters < 0) {
            sb.insert(0, '-');
        }

        return FormattingResult.createForDistance(m_config,
                                                  sb.toString(),
                                                  longdistance);
    }

    /**
     * <p>Round a distance assuming miles/yards units.</p>
     * 
     * <p>Round the distance like
     * <code>Nav2:DistancePrintingPolicy::convertDistanceImperialYards()</code></p>
     * 
     * @param meters - the distance to format.
     * @return a FormattingResult with the formatted distance.
     * @see UnitsFormatter#formatDistance(int)
     */
    public FormattingResult roundDistanceMilesYards(int meters) {
        int absmeters = Math.abs(meters);
        boolean longdistance = false;
        StringBuffer sb = new StringBuffer(15); // "-2000000000"
        float miles = absmeters * METERS_TO_MILES;

        if (miles > 10) {
            // use whole miles
            sb.append((int) WFMath.round(miles));
            longdistance = true;
        } else if (miles > 0.5) {
            /*
             * use miles with one decimal
             * 
             * NOTE: different limit from when we're using feet.
             * 0.5 miles = 880 yards = 2640 ft
             */

            int decimiles = (int) WFMath.round(10*miles);
            sb.append(decimiles / 10);
            sb.append(m_config.getDecimalMarker());
            sb.append(decimiles % 10);
            longdistance = true;
        } else {
            int yards = (int) (absmeters * METERS_TO_YARDS);

            if (yards >= 200) {
                // round to 10 yards
                sb.append(WFMath.roundTo(yards, 10));
            } else if (yards >= 50) {
                // round to 5 yards
                sb.append(WFMath.roundTo(yards, 5));
            } else {
                // output number of yards
                sb.append(yards);
            }
        }

        if (meters < 0) {
            sb.insert(0, '-');
        }

        return FormattingResult.createForDistance(m_config,
                                                  sb.toString(),
                                                  longdistance);
    }


    // conversion factors
    /**
     * Conversion factor from meters (m) to miles (mi).
     * Multiply the distance in meters with this value to obtain distance in
     * miles.
     */
    public static final float METERS_TO_MILES = 1 / 1609.344f;

    /**
     * Conversion factor from meters (m) to feet (ft).
     * Multiply the distance in meters with this value to obtain distance in feet.
     */
    public static final float METERS_TO_FEET = 1 / 0.30480f;

    /**
     * Conversion factor from meters (m) to yards (yds).
     * Multiply the distance in meters with this value to obtain distance in
     * yards.
     */
    public static final float METERS_TO_YARDS = 1 / 0.91440f; // 3 ft

    
    // -----------------------------------------------------------------------
    // speed formatting
    /**
     * Formats a speed in meters per second according to the Wayfinder
     * rounding rules and the chosen unit system.
     * 
     * @param metersPerSecond - the speed in meters per second.
     * @return a FormattingResult with the formatted distance.
     */
    public FormattingResult formatSpeedMPS(float metersPerSecond) {
        String rounded_value;
        if (m_config.getUnitSystem() == UnitsFormatterSettings.UNITS_METRIC) {
            int kmh = (int) (metersPerSecond
                             * METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR); 
            rounded_value = Integer.toString(kmh);
        } else {
            // UK/US use mph
            int mph = (int) (metersPerSecond
                             * METERS_PER_SECOND_TO_MILES_PER_HOUR);
            rounded_value = Integer.toString(mph);
        }

        return new FormattingResult(rounded_value, m_config.getSpeedAbbr());

    }

    /**
     * Formats a speed in knots (nautical miles per hours) according to the
     * Wayfinder rounding rules and the chosen unit system. Additionally,
     * negative speeds are treated as 0. This is to make it easier to handle
     * the positioning system reporting -1 as "speed not yet calculated".
     * 
     * @param knots - the speed in knots.
     * @return a FormattingResult with the formatted distance.
     */
    public FormattingResult formatSpeedKnots(float knots) {
        float mps = knots * KNOTS_TO_METERS_PER_SECOND;
        if (knots < 0) {
            mps = 0;
        }
        return formatSpeedMPS(mps);
    }
    
    // conversion factors
    /**
     * Conversion factor from meters per second (m/s) to
     * kilometers per hour (km/h).
     * Multiply the speed in meters per second with this value to obtain
     * speed in kilometers per hours.
     */
    public static final float METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR = 3.6f;

    /**
     * Conversion factor from meters per second (m/s) to
     * miles per hour (mph).
     * Multiply the speed in meters per second with this value to obtain
     * speed in miles per hour.
     */
    public static final float METERS_PER_SECOND_TO_MILES_PER_HOUR =
        3600 * METERS_TO_MILES;
    /**
     * Conversion factor from knots (kt) (nautical miles per hour) to
     * meters per second (m/s).
     * Multiply the speed in knots with this value to obtain
     * speed in meters per second.
     *
     * reference: <a href="http://www.bipm.org/en/si/si_brochure/chapter4/table8.html">
     * International Bureau of Weights and Measures: Non-SI units accepted for use with the SI, and units based on fundamental constants. 8th ed.</a>  
     */
    public static final float KNOTS_TO_METERS_PER_SECOND = 1852f/3600;


    // -----------------------------------------------------------------------
    // time formatting

    /**
     * formats a time in seconds to format "HH:MM:SS", where
     * <ol><li>HH is number of hours, >= 0. If seconds is > 86400,
     *         HH will be more than 24.
     *     <li>MM is number of minutes, 0,...,59.
     *     <li>SS is number of seconds, 0,...,59.
     *     <li>The part ":SS" is only included if includeSeconds == true.
     *     <li>No calendar information is included - neither epoch, nor leap
     *         seconds. 
     * </ol>
     * 
     * @param seconds - a time in seconds
     * @param formatSeconds - true if second information should be included.
     * @return the formatted time.
     */
    public synchronized String formatTime(int seconds,
                                           boolean formatSeconds) {
        StringBuffer sb = new StringBuffer(30);
        int[] time = splitIntoHoursMinutesSeconds(seconds);
        int hours = time[0];
        int minutes = time[1];
        int sec = time[2];
        
        if(hours < 10) {
            sb.append('0');
        }
        sb.append(hours);
        sb.append(':');

        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);

        if (formatSeconds) {
            sb.append(':');
            if (sec < 10) {
                sb.append('0');
            }
            sb.append(sec);
        }
        
        return sb.toString();
    }

    /**
     * formats a time in seconds to format "HHh MMm SSs", where
     * <ol><li>HH is number of hours, >= 0. If seconds is > 86400,
     *         HH will be more than 24.
     *     <li>h is the string {@link UnitsFormatterSettings#getHoursAbbr()}.
     *     <li>MM is number of minutes, 0,...,59.
     *     <li>m is the string {@link UnitsFormatterSettings#getMinutesAbbr()}.
     *     <li>SS is number of seconds, 0,...,59.
     *     <li>s is the string {@link UnitsFormatterSettings#getSecondsAbbr()}.
     *     <li>The part ":SSs" is only included if includeSeconds == true.
     *     <li>No calendar information is included - neither epoch, nor leap
     *         seconds. 
     * </ol>
     * 
     * @param seconds - a time in seconds
     * @param formatSeconds - true if second information should be included.
     * @return the formatted time.
     */
    public synchronized String
    formatTimeWithUnitStrings(int seconds,
                              boolean formatSeconds) {
        StringBuffer sb = new StringBuffer(30);
        int[] time = splitIntoHoursMinutesSeconds(seconds);
        int hours = time[0];
        int minutes = time[1];
        int sec = time[2];
        
        if(hours < 10) {
            sb.append('0');
        }
        sb.append(hours);
        sb.append(m_config.getHoursAbbr());
        sb.append(' ');

        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        sb.append(m_config.getMinutesAbbr());

        if (formatSeconds) {
            sb.append(' ');
            if (sec < 10) {
                sb.append('0');
            }
            sb.append(sec);
            sb.append(m_config.getSecondsAbbr());
        }
        
        return sb.toString();
        
    }
    
    
    /**
     * Calculates hours, minutes and seconds from a time value in seconds.
     * If seconds is > 86400, the hours will be more than 24.
     * 
     * @param seconds - the number of seconds. Must be >= 0.
     * @return new int[]{hours, minutes, seconds}
     */
    public static int[] splitIntoHoursMinutesSeconds(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds - hours * 3600 ) / 60;
        int sec = seconds - hours * 3600 - minutes * 60;
        
        return new int[]{hours, minutes, sec};
    }
    

    // =======================================================================
    /**
     * <p>Container class returned by some of the formatting methods.</p>
     * 
     * <p>This is intended to give the UI flexibility when displaying without
     * returning hard-to-use arrays of String. The assumption is that formatting
     * is done a few times per display update and thus not performance
     * critical.</p>
     * 
     * <p>Since all fields are final (and no reference leak during
     * construction), this class is thread safe.</p>
     */
    public static class FormattingResult {

        /**
         * String representation of the rounded value (digits and decimal marker
         * only) in the destination unit.
         */
        private final String m_roundedValue;

        /**
         * Unit string of the destination unit.
         * 
         */
        private final String m_unit;
        
        /**
         * Unit abbreviation of the destination unit.
         */
        private final String m_unitAbbr;


        /**
         * Create a new FormattingResult using the same value for m_unit and
         * m_unitAbbr. This is used for speed information.
         *
         * @param roundedValue - the value in the destination unit rounded
         *        according to the rules specified in the formatting method.
         * @param unit - the unit string.
         */
        FormattingResult(String roundedValue, String unit) {
            this(roundedValue, unit, unit);
        }
        
        /**
         * Create a new FormattingResult.
         * 
         * @param roundedValue - the value in the destination unit rounded
         *        according to the rules specified in the formatting method.
         * @param unit - the unit string.
         * @param unitAbbr - the unit abbreviation string.
         */
        FormattingResult(String roundedValue,
                         String unit,
                         String unitAbbr) {

            m_roundedValue = roundedValue;
            m_unit = unit;
            m_unitAbbr = unitAbbr;
        }

        /**
         * Factory method for creating a FormattingResult for rounded
         * distances.
         * 
         * @param config - the unit string configuration to use.
         * @param roundedValue - the String representing the rounded value.
         * @param longDistance - use long distance units?
         * @return a new FormattingResult 
         */
        static FormattingResult
        createForDistance(UnitsFormatterSettings config,
                          String roundedValue,
                          boolean longDistance) {

            if (longDistance) {
                return new FormattingResult(roundedValue,
                                            config.getLongDistance(),
                                            config.getLongDistanceAbbr());
            } else {
                return new FormattingResult(roundedValue,
                                            config.getShortDistance(),
                                            config.getShortDistanceAbbr());            
            }            
        }
    

        /**
         * Returns the rounded value (numbers only, no unit string).
         * @return the rounded value as a String.
         */
        public String getRoundedValue() {
            return m_roundedValue;
        }

        /**
         * Returns the unit string.
         * @return the unit string.
         */
        public String getUnit() {
            return m_unit;
        }

        /**
         * Returns the unit abbreviation string.
         * @return the unit abbreviation string.
         */
        public String getUnitAbbr() {
            return m_unitAbbr;
        }
    }
}
