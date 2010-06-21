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
package com.wayfinder.core.map.vectormap;

/**
 * 
 * 
 */
public interface MapCameraInterface {

    /**
     * Returns the screen coordinate for a specified MC2 world coordinate
     *
     * @param worldLat World Latitude coordinate in MC2
     * @param worldLon World Longitude coordinate in MC2
     * @return Screen coordinates in pixels [X,Y]
     */
    public int[] getScreenCoordinate(long worldLat, long worldLon);
    
    /**
     * Returns the screen coordinate for a specified WGS 84 world coordinate
     *
     * @param worldLat World Latitude coordinate in WGS 84
     * @param worldLon World Longitude coordinate in WGS 84
     * @return Screen coordinates in pixels [X,Y]
     */
    public int[] getScreenCoordinateWGS84(double worldLat, double worldLon);
    
    /**
     * Returns the MC2 world coordinate for a specified screen coordinate
     * 
     * @param screenX screen x position i pixel.
     * @param screenY screen y position i pixel.
     * @return world coordinate in MC2 [Lat,Lon]
     */
    public long[] getWorldCoordinate(int screenX, int screenY);
    
    /**
     * Returns the WGS 84 world coordinate for a specified screen coordinate
     * 
     * @param screenX screen x position i pixel.
     * @param screenY screen y position i pixel.
     * @return world coordinate in WGS 84 [Lat,Lon]
     */
    public double[] getWorldCoordinateWGS84(int screenX, int screenY);     
}
