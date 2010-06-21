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

import com.wayfinder.core.shared.Position;

/**
 * Data class keeping information about a location read from one of the location providers.
 * The accuracy value can be used as radius for CellID, its value being set in meters.
 *
 * This class is thread safe since all fields are final.
 */
public class LocationInformation {
    
    public static final int VALID_SPEED     = 0x1 << 0;
    public static final int VALID_COURSE    = 0x1 << 1;
    public static final int VALID_ALTITUDE  = 0x1 << 2;
    
    private final Position m_MC2Position;
    private final int m_accuracy;
    private final int m_altitude;
    private final long m_positionTime;
    private final float m_speed;
    private final short m_course;
    
    /**
     * flags indicating which of the location "details", namely speed,
     * course and altitude are valid
     */
    private int m_validDetailsFlags;
    
    

    /**
     * Creates new LocationInformation object with a certain position, all the rest
     * of the fields having default values.
     * @param mc2Latitude
     * @param mc2Longitude
     */
    public LocationInformation(int mc2Latitude, int mc2Longitude) {
        m_MC2Position = new Position(mc2Latitude, mc2Longitude);
        m_accuracy   = Integer.MAX_VALUE;
        m_speed     = 0;
        m_altitude  = 0;
        m_positionTime  = 0;
        m_course    = 0;
        m_validDetailsFlags = 0;
    }
    
    /**
     * Creates new LocationInformation object
     * @param mc2Latitude Latitude in MC2
     * @param mc2Longitude Longitude in MC2
     * @param accuracy Accuracy in meters (can be used as radius/error)
     * @param speed Speed in m/s, set -1 if not available
     * @param course Course, set -1 if not avaiable
     * @param altitude Altitude, set -1 if not available
     * @param positionTime Time (System.currentTimeMillis()) set each time
     * a position is reported and fulfills the given {@link Criteria} 
     */
    public LocationInformation(int mc2Latitude, int mc2Longitude, int accuracy, float speed, short course, int altitude, long positionTime) {
        m_MC2Position = new Position(mc2Latitude, mc2Longitude);
        m_accuracy = accuracy;
        m_speed = speed;
        m_course = course;
        m_altitude = altitude;
        m_positionTime = positionTime;
        
        m_validDetailsFlags = 0;
        if (m_course >= 0) m_validDetailsFlags |= VALID_COURSE;
        if (m_altitude >= 0) m_validDetailsFlags |= VALID_ALTITUDE;
        if (m_speed >= 0) m_validDetailsFlags |= VALID_SPEED;
    }

    /**
     * @return the MC2Position
     */
    public Position getMC2Position() {
        return m_MC2Position;
    }

    /**
     * @return the accuracy in meters
     */
    public int getAccuracy() {
        return m_accuracy;
    }

    /**
     * @return the altitude
     */
    public int getAltitude() {
        if (!hasAltitude()) return 0;
        
        return m_altitude;
    }
    
    public boolean hasAltitude() {
        return ((m_validDetailsFlags & VALID_ALTITUDE) == VALID_ALTITUDE);
    }

    /**
     * @return the positionTime
     */
    public long getPositionTime() {
        return m_positionTime;
    }

    /**
     * The speed is in m/s.
     * @return the speed 
     */
    public float getSpeed() {
        if (!hasSpeed()) return 0;
        
        return m_speed;
    }
    
    public boolean hasSpeed() {
        return ((m_validDetailsFlags & VALID_SPEED) == VALID_SPEED);
    }

    /**
     * @return the course
     */
    public int getCourse() {
        if (!hasCourse()) return 0;
        
        return m_course;
    }
    
    public boolean hasCourse() {
        return ((m_validDetailsFlags & VALID_COURSE) == VALID_COURSE);
    }
    
    public boolean isAtLeastAsGoodAs(Criteria criteria) {
        if (criteria.isAltitudeReguired() && !hasAltitude()) return false;
        if (criteria.isCourseRequired() && !hasCourse()) return false;
        if (criteria.isSpeedRequired() && !hasSpeed()) return false;
        
        if (m_accuracy <= criteria.getAccuracy()) return true;
        
        return false;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("LocationInformation(");
        sb.append(m_MC2Position.getMc2Latitude());
        sb.append(", ").append(m_MC2Position.getMc2Longitude());
        sb.append(", acc=").append(m_accuracy);
        sb.append(", speed=").append(m_speed);
        sb.append(", course=").append(m_course);
        sb.append(", utc=").append(m_positionTime);
        sb.append(")");
        return sb.toString();
    }
}
