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
/**
 * 
 */
package com.wayfinder.core.positioning;


/**
 * Data class for setting criteria for getting position or provider updates.
 * The class resembles the Criteria class from Location API, but it has less
 * fields to set.
 *
 * This class is thread-safe by internal synchronization.
 */
public class Criteria {
    
    /**
     * 
     */
    public static final int ACCURACY_NONE       = Integer.MAX_VALUE;
    
    /**
     * Upper end of the accuracy interval, every accuracy larger 
     * than {@link #ACCURACY_BAD} can be considered as {@link #ACCURACY_NONE} 
     * The value of this constant is 50 and is in meters.
     */
    public final static int ACCURACY_BAD        = 50;
    
    /**
     * Every position with accuracy better (less) than {@link #ACCURACY_GOOD}
     * and worse (greater) than {@link #ACCURACY_EXCELLENT}. The value is 
     * 35 meters.
     */
    public final static int ACCURACY_GOOD       = 35;
    /**
     * Every position with accuracy better (less) than {@link #ACCURACY_EXCELLENT}.
     * The value is 15 meters.
     */
    public final static int ACCURACY_EXCELLENT  = 15;
    
    private final int accuracy;
    
    private final boolean costAllowed;
    
    private final boolean altitudeReguired;
    
    private final boolean speedRequired;
    
    private final boolean courseRequired;
    
    private Criteria(Builder builder) {
        accuracy = builder.accuracy;
        costAllowed = builder.costAllowed;
        altitudeReguired = builder.altitudeReguired;
        speedRequired = builder.speedRequired;
        courseRequired = builder.courseRequired;
    }
    
    /**
     * @return the required accuracy, always one of the ACCURACY_ constants
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * @return true if cost is allowed
     */
    public boolean isCostAllowed() {
        return costAllowed;
    }

    /**
     * @return <code>true</code> if the the current {@link Criteria} is set to require altitude
     */
    public boolean isAltitudeReguired() {
        return altitudeReguired;
    }

    /**
     * @return <code>true</code> if the speed is required to be returned by this provider
     */
    public boolean isSpeedRequired() {
        return speedRequired;
    }


    /**
     * @return <code>true</code> if the course is required to be returned by this provider
     */
    public boolean isCourseRequired() {
        return courseRequired;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Criteria) {
            Criteria c = (Criteria) obj;
            
            boolean sameCostSetting = ! (isCostAllowed() ^ c.isCostAllowed());
            boolean sameCourseSetting = ! (isCourseRequired() ^ c.isCourseRequired());
            boolean sameSpeedSetting = ! (isSpeedRequired() ^ c.isSpeedRequired());
            boolean sameAltSetting = ! (isAltitudeReguired() ^ c.isAltitudeReguired());
            boolean sameAccuracySetting = (getAccuracy() == c.getAccuracy());
            
            return (sameAccuracySetting 
                    && sameAltSetting
                    && sameCostSetting 
                    && sameCourseSetting 
                    && sameSpeedSetting);
        }
        else {
            return false;
        }
    }
    
    public int hashCode() {
        int hash = 1;
        if (isSpeedRequired()) hash *= 2;
        if (isCourseRequired()) hash *= 3;
        if (isAltitudeReguired()) hash *= 5;
        if (isCostAllowed()) hash *= 7;
        
        if (getAccuracy() <= ACCURACY_EXCELLENT) hash *= 11;
        else if (getAccuracy() <= ACCURACY_GOOD) hash *= 13;
        else if (getAccuracy() <= ACCURACY_BAD) hash *= 17;
        else hash *= 19;
        
        return hash;
    }
    
    public String toString() {
        return "Criteria: accuracy=" + accuracy;
    }
    
    public static class Builder {
        
        private int accuracy = ACCURACY_NONE;
        
        private boolean costAllowed;
        
        private boolean altitudeReguired;
        
        private boolean speedRequired;
        
        private boolean courseRequired;
        
        /** 
         * Set minimum accuracy required in meters, the value will be rounded 
         * to one of the {@link Criteria} ACCURACY_ constants
         * Default value is {@link Criteria#ACCURACY_NONE}
         * 
         * @param accuracy the value in meters  
         * 
         * @return the Builder itself so next method can be queued
         */
        public Builder accuracy(int accuracy) {
            if (accuracy <= ACCURACY_EXCELLENT) this.accuracy = ACCURACY_EXCELLENT;
            else if (accuracy <= ACCURACY_GOOD) this.accuracy = ACCURACY_GOOD;
            else if (accuracy <= ACCURACY_BAD) this.accuracy = ACCURACY_BAD;
            else this.accuracy = ACCURACY_NONE;
            return this;
        }
        
        
        /**
         * Indicates that the provider is allowed to incur monetary cost
         * 
         * @return the Builder itself so next method can be queued
         */
        public Builder costAllowed() {
            costAllowed = true;
            return this;
        }

        /**
         * Indicates whether the provider must provide altitude information. 
         * Not all fixes are guaranteed to contain such information. 
         * 
         * @return the Builder itself so next method can be queued
         */
        public Builder altitudeReguired() {
            altitudeReguired = true;
            return this;
        }
        
        /**
         * Indicates whether the provider must provide speed information.
         * <p>
         * WARNING: Not all fixes are guaranteed to contain such information, 
         * check it with {@link LocationInformation#hasSpeed()}
         * </p>
         * 
         * @return the Builder itself so next method can be queued
         */
        public Builder speedRequired() {
            speedRequired = true;
            return this;
        }
        
        /**
         * Indicates whether the provider must provide course information.
         * <p>
         * WARNING: Not all fixes are guaranteed to contain such information, 
         * check it with {@link LocationInformation#getCourse()}
         * </p>
         * @return the Builder itself so next method can be queued
         */
        public Builder courseRequired() {
            courseRequired = true;
            return this;
        }
        
        public Criteria build() {
            return new Criteria(this);
        }
    }
 }
