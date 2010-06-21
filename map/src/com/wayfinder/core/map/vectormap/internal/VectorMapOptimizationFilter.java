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
package com.wayfinder.core.map.vectormap.internal;

import com.wayfinder.core.map.vectormap.internal.drawer.Camera;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;


/**
 * Class for checking if various map optimizations should apply. The idea is to
 * keep all checks in one class instead of spreading them around in the code.
 * 
 */

public class VectorMapOptimizationFilter {
    
    /**
     * Check if gap triangles should be skipped
     * 
     * @param camera - the camera
     * @param detailLevel - current detail level
     * @return true if triangles should be skipped, false otherwise
     */
    
    public static boolean skipGapTriangles(Camera camera, int detailLevel) {
        return camera.isMoving() && !camera.isIn3DMode() && detailLevel > 0;
    }
    
    /**
     * Check if start and end triangles should be skipped
     * 
     * @param camera - the camera
     * @param detailLevel - current detail level
     * @return true if triangles should be skipped, false otherwise
     */
    
    public static boolean skipStartAndEndTriangles(Camera camera, int detailLevel) {
        return camera.isMoving() && !camera.isIn3DMode() && detailLevel > 0;
    }
    
    /**
     * Check if overview maps should be skipped
     * 
     * @param tmfd - tile map format desc
     * @param zoomLevel - current zoom level
     * @return true if overview maps should be skipped, false otherwise
     */
    
    public static boolean skipOverViewMaps(TileMapFormatDesc tmfd, int zoomLevel) {
        return tmfd.getCurrentDetailLevel(tmfd.getLayerNbrFromID(0), zoomLevel) > tmfd.getOverviewMapDetailLevel();
    }

}
