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

package com.wayfinder.core.map;

import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.shared.Position;



/**
 * 
 * Listener interface used to get notified when the active point in the map 
 * (cursor or gps position) is over an added MapObject or POI. The active point
 * is default set to the center of the screen but it can be changed via the 
 * {@link VectorMapInterface#setActiveScreenPoint(int, int)} method.  
 * 
 *
 */
public interface MapObjectListener {
   
    /**
     * Called when the active point in the map is over an added map object 
     * in the map. <p>
     * 
     * Since more than one MapObject can be selected at the same time the implementation of this
     * method can choose to use the map object or not. Return true if the MapObject is handled 
     * or false to try the next (if there exist any more selected map objects).
     * 
     * @param mapObject  the MapObject that has been selected
     * @return true if MapObject is handled, false otherwise
     */
    public boolean mapObjectSelected(MapObject mapObject);
    
    /**
     * Called when a selected MapObject has been unselected. 
     * 
     * @param aMapObject  the MapObject that has been unselected. 
     */
    public void mapObjectUnSelected(MapObject aMapObject);
    
    /**
     * 
     * Called when a MapObject has been pressed on the screen. Only available if the
     * device support touch events. <p>
     * 
     * Since more than one MapObject can be selected at the same time the implementation of this
     * method can choose to use the map object or not. Return true if the MapObject is handled 
     * or false to try the next (if there exist any more selected map objects). 
     *  
     * @param mapObject  the MapObject that has been pressed 
     * @return true if the MapObject has been handled, false otherwise  
     */
    public boolean mapObjectPressed(MapObject mapObject);
    
     
    /**
     * Called when a POI has been selected by the active point in the map. 
     * 
     * @param name the name of the POI that has been selected
     * @param position the position of the POI that has been selected
     */
    public void poiSelected(String name, Position position);
    
    /**
     * Called when a selected POI has been marked as unselected. 
     * 
     * @param name the name of the POI that has been unselected
     * @param position the position of the POI that has been unselected
     */
    public void poiUnSelected(String name, Position position);
    
    /**
     * Called when a POI has been pressed on the screen. Only available if the device 
     * support touch events. 
     * 
     * @param name the name of the POI that has been pressed.
     * @param position the position of the POI that has been pressed 
     * @return true if the POI event has been handled, false if not. 
     */
    public boolean poiObjectPressed(String name, Position position);
    
}
