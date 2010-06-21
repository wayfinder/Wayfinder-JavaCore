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
package com.wayfinder.core.shared;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.internal.Serializable;



/**
 * represents a boundingbox in pixel coordinats
 */
public class BoundingBox implements Serializable {
    
    /**
     * Coordinates are in Mercator pixels 
     */
    public int iMaxLat;
    public int iMinLat;
    public int iMaxLon;
    public int iMinLon;
    
    public int iDetailLevel;

    public BoundingBox() {
    }

    /**
     * @param maxLat north latitude
     * @param minLat south latitude
     * @param maxLon east longitude
     * @param minLon west longitude
     *
     * this is valid even though the y axis on the screen is zero at
     * the top of the screen.<br>
     *
     * Valid on all points on the earth except if the bb covers the
     * meridian (180 degrees E/W) opposite to the prime meridian (0,
     * at Greenwich) (not really a problem with the bb itself but with
     * the calculations done on it)
     */
    public BoundingBox(int maxLat, int minLat, int maxLon, int minLon) {
        iMaxLat = maxLat; 
        iMinLat = minLat;
        iMaxLon = maxLon;
        iMinLon = minLon;
    }

    public int getCenterLat() {
        return (iMaxLat/2+iMinLat/2);
    }

    public int getCenterLon() {
        return (iMaxLon/2+iMinLon/2);
    }

    /**
     * Set the parameters for the boundingbox
     * 
     * @param maxLat The maximum latitude
     * @param minLat The minimum latitude
     * @param maxLon The maximum longitude
     * @param minLon The maximum longitude
     */
    public void setBoundingBox(int maxLat, int minLat,
                               int maxLon, int minLon,
                               int detail) {
        //#debug
//        System.out.println("BoundingBox.setBoundingBox() " + maxLat + " " + iMinLat + " " + maxLon + " " + minLon + " detail " + detail);

        iMaxLat = maxLat;
        iMinLat = minLat;
        iMaxLon = maxLon;
        iMinLon = minLon;
        iDetailLevel = detail;
    }

    public void setMinLat(int aMinLat) {
        iMinLat = aMinLat;
    }
    
    public void setMaxLat(int aMaxLat) {
        iMaxLat = aMaxLat;
    }
    
    public void setMinLon(int aMinLon) {
        iMinLon = aMinLon;
    }
    
    public void setMaxLon(int aMaxLon) {
        iMaxLon = aMaxLon;
    }
    
    /**
     * name says it all.
     */
    public int getEastLongitude() {
        return iMaxLon;
    }

    /**
     * @see #getEastLongitude()
     */
    public int getWestLongitude() {
        return iMinLon;
    }

    /**
     * @see #getEastLongitude()
     */
    public int getNorthLatitude() {
        return iMaxLat;
    }

    /**
     * @see #getEastLongitude()
     */
    public int getSouthLatitude() {
        return iMinLat;
    }


    /**
     * Equivalent to {@link BoundingBox#getEastLongitude()}.
     * 
     * @return the eastern/maximum longitude.
     */
    public int getMaxLongitude() {
        return getEastLongitude();
    }

    /**
     * Equivalent to {@link BoundingBox#getWestLongitude()}.
     * 
     * @return the western/minimum longitude.
     */
    public int getMinLongitude() {
        return getWestLongitude();
    }
    
    /**
     * Equivalent to {@link BoundingBox#getNorthLatitude()}.
     * 
     * @return the northern/maximum latitude.
     */
    public int getMaxLatitude() {
        return getNorthLatitude();
    }
    
    /**
     * Equivalent to {@link BoundingBox#getSouthLatitude()}.
     * @return the southern/minimum latitude.
     */
    public int getMinLatitude() {
        return getSouthLatitude();
    }


    public boolean getIntersection(BoundingBox bBox, BoundingBox intersectBox) {
        if (overlaps(bBox)) {
              // The bBoxes contains an intersection.
              if( iMinLat < bBox.iMinLat )
                 intersectBox.setMinLat( bBox.iMinLat );
              else
                 intersectBox.setMinLat( iMinLat );

              if( iMaxLat > bBox.iMaxLat )
                 intersectBox.setMaxLat( bBox.iMaxLat );
              else
                 intersectBox.setMaxLat( iMaxLat );

              if( iMinLon-bBox.iMinLon < 0 )
                 intersectBox.setMinLon( bBox.iMinLon );
              else
                 intersectBox.setMinLon( iMinLon );

              if( iMaxLon - bBox.iMaxLon > 0 )
                 intersectBox.setMaxLon( bBox.iMaxLon );
              else
                 intersectBox.setMaxLon( iMaxLon );

              return true;                  
        }       
        else
            return false;
        
    }
    
    public boolean overlaps(BoundingBox bBox) {
        return (!((iMinLat > bBox.iMaxLat)||(iMaxLat < bBox.iMinLat)
                ||(iMinLon > bBox.iMaxLon) ||(iMaxLon < bBox.iMinLon)));
    }
    
   /** 
    * Return true if the two bounding boxes intersects with each other. 
    * 
    * Note that if one of the bounding boxes are fully inside the other 
    * the method returns true. 
    * 
    * @param bBox
    */
   public boolean intersectWith(BoundingBox bBox) {        
       if((bBox.iMinLon >= iMinLon && bBox.iMinLon <= iMaxLon) ||
               (bBox.iMaxLon >= iMinLon && bBox.iMaxLon <= iMaxLon) ||
               (bBox.iMinLon <= iMinLon && bBox.iMaxLon >= iMaxLon)) {
                    if((bBox.iMinLat >= iMinLat && bBox.iMinLat <= iMaxLat) ||
                       (bBox.iMaxLat >= iMinLat && bBox.iMaxLat <= iMaxLat) ||
                       (bBox.iMinLat <= iMinLat && bBox.iMaxLat >= iMaxLat)) {              
                       return true; 
                    }
       }
       return false;
   }
    
    
    public boolean equals(Object aObject) {
        if (aObject instanceof BoundingBox) {
            BoundingBox bBox = (BoundingBox) aObject;
            return ((iMinLat == bBox.iMinLat) && (iMaxLat == bBox.iMaxLat) &&
                    (iMinLon == bBox.iMinLon) && (iMaxLon == bBox.iMaxLon));
        }
        return false;
    }
    
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + iMinLat;
        hash = hash * 31 + iMaxLat;
        hash = hash * 31 + iMinLon;
        hash = hash * 31 + iMaxLon;
        return hash;
    }
   
    /**
     * for debugging use
     * 
     * The obfuscator doesn't remove method when debug is disabled,
     * beacause is used maybe for creating a string exception but this 
     * is not visibile when  debug is disabled so I have ifdef:ed it 
     */
    public String toString() {
        return "BoundingBox, lat:(" + iMinLat + "," + iMaxLat
            + "), lon: ("+ iMinLon + "," + iMaxLon+")";
    }

    /**
     * Re-initiate this BoundingBox from a stream. Detail level will not be read.
     * 
     * @see com.wayfinder.core.shared.internal.Serializable#read(DataInputStream)
     */
    public void read(DataInputStream din) throws IOException {
        iMinLat = din.readInt();
        iMinLon = din.readInt();
        iMaxLat = din.readInt();
        iMaxLon = din.readInt();
    }
    
    /**
     * Write this BoundingBox to a stream. Detail level will not be written.
     * 
     * @see com.wayfinder.core.shared.internal.Serializable#write(DataOutputStream)
     */
    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(iMinLat);
        dout.writeInt(iMinLon);
        dout.writeInt(iMaxLat);
        dout.writeInt(iMaxLon);
    }
    
    public int getLonDiff() { 
       return iMaxLon - iMinLon; 
    }

    public int getHeight() {
       return iMaxLat - iMinLat;
    }
    
}
