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
import com.wayfinder.core.shared.util.IntVector;


public class CoordsArg extends TileFeatureArg{

    private int length;
    private int latitude;
    private int longitude;
    private IntVector iCoords;
    
    private int maxLat, minLat, maxLon, minLon;

    public CoordsArg(int type) {
        super(TileFeatureArg.COORDSARG, 0); // unknown name at this point

    }

    public CoordsArg(int aType, IntVector aCoords, int aLength, 
            int aMaxLat, int aMinLat, int aMaxLon, int aMinLon) {
        super(TileFeatureArg.COORDSARG, 0); // unknown name at this point
        iCoords = aCoords;
        length = aLength;
        maxLat = aMaxLat;
        minLat = aMinLat;
        maxLon = aMaxLon;
        minLon = aMinLon;
    }
  
    public int getEndLat() {
        return iCoords.get(length-2);
    }
    
    public int getEndLon() {
        return iCoords.get(length-1);
    }
    
    public IntVector getCoords() {
        return iCoords;
    }

    public int getMaxLat() {
        return maxLat;
    }
    
    public int getMinLat() {
        return minLat;
    }
    
    public int getMaxLon() {
        return maxLon;
    }
    
    public int getMinLon() {
        return minLon;
    }

    public boolean load( int type,BitBuffer bitBuffer, TileFeature tileMap, TileFeatureArg prevArg, boolean save ) {
        // Check if the previous coords last coordinate can be used as reference.
        if ( prevArg != null ) {
            // Use previous feature's last coordinate as reference.
            latitude = ((CoordsArg)prevArg).getEndLat();
            longitude = ((CoordsArg)prevArg).getEndLon();
        }
        else {
            latitude = 0;
            longitude = 0;
        }
        
        // Write the number of coordinates.
        int nbrBitsCoordSize = bitBuffer.nextBits( 4 );
        int nbrCoords = bitBuffer.nextBits( nbrBitsCoordSize );
              
        if ( nbrCoords == 0 ) {
            return true;
        }
               
        // Nbr bits needed for start diff.
        int nbrStartDiffBits = bitBuffer.nextBits( 4 );
        // Read startLatDiff.
        int startLatDiff = bitBuffer.nextSignedBits( nbrStartDiffBits );
        // Read startLonDiff.
        int startLonDiff = bitBuffer.nextSignedBits( nbrStartDiffBits );
                
        // Read bitsPerLatDiff.
        int bitsPerLatDiff = bitBuffer.nextBits(4);
        // Read bitsPerLonDiff.
        int bitsPerLonDiff = bitBuffer.nextBits(4);
        
        length = (nbrCoords << 1);        
        iCoords = new IntVector(length);
        
        // Calculate the starting coordinate.
        int prevLat = startLatDiff  + this.latitude;
        int prevLon = startLonDiff  + this.longitude;
        
        iCoords.add(prevLat);
        iCoords.add(prevLon);
        maxLat = minLat = prevLat;
        maxLon = minLon = prevLon;
        
        // Read the rest of the coords.
        for ( int i = 1; i < nbrCoords; i++ ) {
            // Lat
            int latDiff = bitBuffer.nextSignedBits( bitsPerLatDiff );
            int curLat = latDiff + prevLat;
            
            // Lon
            int lonDiff = bitBuffer.nextSignedBits( bitsPerLonDiff );
            int curLon = lonDiff + prevLon;
            // Update prev coordinates.
            prevLat = curLat;
            prevLon = curLon;
            
            /* Update the max Lat/Lon and min Lat/Lon coordinates */
            if(curLat > maxLat){ 
                maxLat = curLat; 
            } else if(curLat < minLat){ 
                minLat = curLat; 
            }                     
            if(curLon > maxLon){ 
                maxLon = curLon; 
            } else if(curLon < minLon){ 
                minLon = curLon; 
            }
            
            /* Add the coordinates to the vector */            
            iCoords.add(curLat);
            iCoords.add(curLon);
        }
        
        if(save) {          
            tileMap.addArgs(new CoordsArg(type, iCoords, length, maxLat, minLat, maxLon, minLon));
        }
        
        return true;
    }
}
