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
package com.wayfinder.core.map.vectormap.internal.process;

import com.wayfinder.core.map.util.BitBuffer;

public class CoordArg extends TileFeatureArg{
	
    private int latitude;
    private int longitude;
        
    public CoordArg(int type, int name) {
        super(TileFeatureArg.COORDARG, name);

   }
    
   public CoordArg(int latitude, int longitude, int type ) {
       super(TileFeatureArg.COORDARG, 0); // unknown name at this point
       this.latitude = latitude;
       this.longitude = longitude;
   }

    public int getLatitude() {
        return this.latitude;
    }
    
    public int getLongitude() {
        return this.longitude;
    }
    
    public boolean load( int type, BitBuffer bitBuffer, TileFeature tileFeature, TileFeatureArg tfa, boolean save) {
        
        TileMap tileMap = tileFeature.getTileMap();
        
        bitBuffer.alignToByte();
        int diffLat = bitBuffer.nextShort();
        int diffLon = bitBuffer.nextShort();
        long lat = diffLat * tileMap.getMC2Scale() + tileMap.getReferenceCoord()[0];
        long lon = diffLon * tileMap.getMC2Scale() + tileMap.getReferenceCoord()[1];
        
        if(lat<Integer.MIN_VALUE) lat=Integer.MIN_VALUE+1;
        if(lon<Integer.MIN_VALUE) lon=Integer.MIN_VALUE+1;
        if(lat>Integer.MAX_VALUE) lat=Integer.MAX_VALUE-1;
        if(lon>Integer.MAX_VALUE) lon=Integer.MAX_VALUE-1;
        
        this.latitude = (int)lat;
        this.longitude = (int)lon;
                
        if(save)
            tileFeature.addArgs(new CoordArg(this.latitude, this.longitude, type));
        
        return true;
    }
    
    
}
