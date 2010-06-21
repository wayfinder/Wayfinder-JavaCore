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

package com.wayfinder.core.map.mercatormap.internal;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.WFMath;

/**
 * <p>Tools for working with the projection of Wayfinder Mercator maps which is
 * a tiled bitmap format.</p>
 * 
 * <p>This class does not contain any actual representation of a position.
 * That would need:
 * <ol><li>Agreement on if a position should be represented by
 *         <ul><li>index number (as seem to be done in current mercator maps)
 *             <li>zoom level global pixel coordinates
 *                 (<code>index * MapConstants.PX_PER_TILE</code>)
 *             <li>a triplet of global x,y and zoom level indicator.
 *         </ul>
 *     <li>How to track that new zoom level settings make all positions
 *         expressed with old settings invalid.
 * </ol>
 * 
 * <p>Note that pixel coordinates is not the same as screen coordinates.
 * Screen coordinates only index what the user currently sees on the screen.
 * But global (per zoom level) pixel coordinates can be converted to screen
 * coordinates by addition and subtraction.</p>
 * 
 * <p>This class does not extend the MC2 position class
 * {@link com.wayfinder.core.shared.Position} because
 * <ol><li>the is-a-relationship does not hold unless the coordinates are
 *         global.
 *      <li>due to rounding, we don't have a truly 1:1 mapping anyway which
 *          would introduce many problems with which representation is
 *          authorative.
 *      <li>even without the above problems, the mercator projection is not
 *          truly global. It breaks down near the poles.
 * </ol></p>
 */
public abstract class MercatorPosition {
    /*   
     * FIXME: old comment. Validity unclear.
     * 
     * Mercator conversion methods used in web client 
     *
     * MC2ToXYCoord : function(lonX, latY){
     *     lonXRad = this.MC2ToRadians(lonX);
     *     latYRad = this.MC2ToRadians(latY);
     *     posX = ( lonXRad * this.totalTilesX * this.serverPixelSize ) / ( 2 * Math.PI);
     *     posY = Math.log( Math.tan(latYRad) + 1/Math.cos(latYRad) ) * this.totalTilesX * this.serverPixelSize  / (2 * Math.PI) ;
     *     return [posX, posY];
     * }
     *
     * XYCoordToMC2 : function (x, y){
     *     tempLon = x * 2 * Math.PI / (this.totalTilesX * this.serverPixelSize);
     *     tempLat = Math.atan ( this.sinh ( y * 2 * Math.PI / (this.totalTilesX * this.serverPixelSize) ) ) ;
     *     mc2Lat = tempLat * this.mc2factor * 180 / Math.PI ;
     *     mc2Lon = tempLon * this.mc2factor * 180 / Math.PI ;
     *     return [mc2Lon, mc2Lat];
     * }
     *
     * mc2factor = 11930464.7111
     */
    
    /**
     * <p>Converts a longitude from MC2 in XY Mercator coordinates</p>
     * @param aMC2Value the value that must be converted
     * @param aTotalTileX number of tiles on X axis for the zoomlevel in which the 
     * coordinates will be valid
     * @param aPixelSize the number of pixels that the tile has on X coordinate
     * @return
     *
     * <p>FIXME: probably does not work according to spec. For instance as of
     * 2009-06-24, wfeu:<pre>
     *     &lt;zoom_levels crc="df4c97b6" nbr_zoom_levels="15" pixel_size="180">
     *         &lt;zoom_level max_x="57420" max_y="57420"
     *                     min_x="-57600" min_y="-57600"
     *                     zoom_j2me="true" zoom_level_nbr="7"/></pre>
     *                     
     * but the call from WFMapInterface is<pre>
     *     lonToX(lon,
     *            MapConstants.getTotalTilesX(0, MapConstants.PX_PER_TILE), 
     *            MapConstants.PX_PER_TILE)</pre>
     *
     * here, <code>PX_PER_TILE = 180</code> but
     * <code>MapConstants.getTotalTilesX()</code> takes the difference between
     * <code>max_x</code> and <code>min_x</code> (as expected) and then DIVIDES
     * with  PX_PER_TILE. The effect will be that this method returns a
     * tile index and not a mercator pixel coordinate.</p>
     */
    public static int lonToX(int aMC2Value, int aTotalTileX, int aPixelSize){
        double lonXRad = Position.mc2ToRadians(aMC2Value);
        // FIXME BUG? radians is [-Pi,...,Pi] and since we allow negative
        // coordinates, we should divide by Pi, not 2*Pi. But if the
        // inverse function xToLon() has the same error, it will still work. 
        return (int)(( lonXRad * aTotalTileX * aPixelSize) / ( 2 * java.lang.Math.PI));
    }

    /**
     * <p>Converts a latitude from MC2 in XY Mercator coordinates</p>
     * @param aMC2Value the value that must be converted
     * @param aTotalTileY number of tiles on Y axis for the zoomlevel in which the 
     * coordinates will be valid
     * @param aPixelSize the number of pixels that the tile has on Y coordinate
     * @return
     *
     * <p>FIXME: Same issues as {@link MercatorPosition#lonToX(int, int, int)}
     * but for Y and tile height.</p>
     */
    public static int latToY(int aMC2Value, int aTotalTileY, int aPixelSize){
        double latYRad = Position.mc2ToRadians(aMC2Value);
        return (int)(WFMath.log(Math.tan(latYRad) +
                     1/Math.cos(latYRad)) * aTotalTileY * aPixelSize/(2*Math.PI));
    }
    
    /**
     * mc2factor from web client
     */
    private static final double MC2FACTOR = 2147483647.998;
    
    /**
     * Converts a longitude from XY Mercator in MC2 coordinates
     * @param aXValue the value that must be converted
     * @param aTotalTileX number of tiles on X axis for the zoomlevel in which the 
     * coordinates will be valid
     * @param aPixelSize the number of pixels that the tile has on Y coordinate
     * @return
     *
     * <p>FIXME: Same issues as {@link MercatorPosition#lonToX(int, int, int)}
     *
     */
    public static int xToLon(int aXValue, int aTotalTileX, int aPixelSize){
      double tempLon = (long)(aXValue << 1) * Math.PI / (aTotalTileX * aPixelSize);
      int lon = (int)(tempLon / Math.PI *  MC2FACTOR);
      return lon;
    }
    
    /**
     * Converts a latitude from XY Mercator in MC2 coordinates
     * @param aYValue the value that must be converted
     * @param aTotalTileY number of tiles on X axis for the zoomlevel in which the 
     * coordinates will be valid
     * @param aPixelSize the number of pixels that the tile has on Y coordinate
     * @return
     * 
     * <p>FIXME: Same issues as {@link MercatorPosition#lonToX(int, int, int)}
     * but for Y and tile height.</p>
     */
    public static int yToLat(int aYValue, int aTotalTileY, int aPixelSize){
        double tempLat = WFMath.atan(WFMath.sinh((long)(aYValue << 1) * Math.PI / (aTotalTileY * aPixelSize)));
        int lat = (int)(tempLat / Math.PI * MC2FACTOR);
        return lat;
    }    
}
