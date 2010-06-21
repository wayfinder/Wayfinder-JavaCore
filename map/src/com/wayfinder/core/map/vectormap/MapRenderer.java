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

import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;


/**
 * Interface used to render the map. 
 * 
 * 
 */
public interface MapRenderer {
    
    /** 
     * 
     * Method that render the map on WFImage object that has been set
     * via the setMapDrawingContext(WFImage aMapImage) method.
     * 
     * @return the updated map image or null if no WFImage has been set. 
     */
    public WFImage renderMap();
    
    /**
     * Render the map on the WFGraphics object specified by the parameter. 
     * Calls to this method will trigger a re-rendering of the map even if
     * the map hasn't been moved since the last update. 
     * 
     * @param g
     */
    public void renderMap(WFGraphics g);
    
    /**
     * Lock the map canvas. The map will not be updated again until the 
     * unlockMap() method has been called.  
     * 
     */
    public void lockMap();
    
    /**
     * Unlock the map. The map are available for updates again. 
     */
    public void unlockMap();
    
    /**
     * Return the active screen X point used in the map.
     * 
     * @return the screen x position of the cursor. 
     */
    public int getActiveScreenPointX();
    
    /**
     * Return the active screen Y point used in the map. 
     * 
     * @return the screen y position of the cursor. 
     */
    public int getActiveScreenPointY();
    
    /**
     * Return the name of the current active map object. 
     * 
     * @return the name of the current active map object.
     */
    public String getActiveObjectName();

}
