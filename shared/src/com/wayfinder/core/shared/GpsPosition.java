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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.shared;

import java.io.IOException;

import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.xml.XmlWriter;

/**
 * FIXME: There is no real need for undefined course. If the course is not
 * known, use the super class Position instead. The naming is confusing -
 * this class has nothing to do with GPS - it is a position with course
 * information.
 * 
 * @see com.wayfinder.core.positioning.LocationInformation
 *
 */
public class GpsPosition extends Position {
    
    public static final short COURSE_UNDEF = -1;
    
    private final short m_course;
    
    public GpsPosition(int mc2Lat, int mc2Lon, short course) {
        super(mc2Lat, mc2Lon);
        m_course = course;
    }
    
    public GpsPosition(Position pos, short course) {
        this(pos.getMc2Latitude(), pos.getMc2Longitude(), course);        
    }

    /*
     * FIXME: document unit system (radians, degrees) and rename method
     * accordingly. Document value range and the undefined value.
     */
    public short getCourse() {
        return m_course;
    }

    public void write(MC2Writer mc2w) throws IOException {
        mc2w.startElement(MC2Strings.tposition_item);
        mc2w.attribute(MC2Strings.aposition_system, MC2Strings.MC2);
        mc2w.elementWithText(MC2Strings.tlat, getMc2Latitude());
        mc2w.elementWithText(MC2Strings.tlon, getMc2Longitude());
        if (m_course != COURSE_UNDEF) {
            mc2w.elementWithText(MC2Strings.tangle, m_course);
        }
        mc2w.endElement(MC2Strings.tposition_item);
    }
}
