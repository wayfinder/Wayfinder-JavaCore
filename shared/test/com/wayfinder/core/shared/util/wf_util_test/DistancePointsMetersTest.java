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

package com.wayfinder.core.shared.util.wf_util_test;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.WFUtil;

import junit.framework.TestCase;

public class DistancePointsMetersTest extends TestCase {
    private static final Position MALMO_CITY_HALL =
        new Position(663306456, 155189773);
    
    private static final Position KARLAVAGEN_STOCKHOLM =
        new Position(707949627, 215716156);
       
    public void testZero() {
        assertEquals(0, WFUtil.distancePointsMeters(0, 0, 0, 0));
    }
    
    public void  testMalmoStockholm() {      
        // approx 510 km as the crow flies (600 km road)

        Position p1 = MALMO_CITY_HALL;
        Position p2 = KARLAVAGEN_STOCKHOLM;
        
        double coslat = Math.cos((p1.getRadiansLatitude()
                                  + p2.getRadiansLatitude())/2);
        long latdiff = p1.getMc2Latitude() - p2.getMc2Latitude();
        double londiff_normal = coslat
                                * (p1.getMc2Longitude() - p2.getMc2Longitude());
        double distanceMC2 =
            Math.sqrt(latdiff*latdiff + londiff_normal*londiff_normal);
        int distanceMeters = (int) (distanceMC2 * 40075016.68557848d / 4294967296L);   
                     
        assertEquals(distanceMeters,
                     WFUtil.distancePointsMeters(MALMO_CITY_HALL,
                                                 KARLAVAGEN_STOCKHOLM));
    }
}
