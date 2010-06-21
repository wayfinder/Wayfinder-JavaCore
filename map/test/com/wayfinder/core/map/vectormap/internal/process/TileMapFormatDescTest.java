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

import junit.framework.TestCase;

public class TileMapFormatDescTest extends TestCase {
    
    public void testScaleIndexFromZoomLevel() {
        int[] scaleLevelsTable = {1, 2, 3, 4, 5, 6, 7, 8, 12, 30, 60, 100, 600, 1000, 20000, 65535};
        float[] zoomLevels = {-5f, 0f, 0.5f, 1f, 1.2f, 1.8f, 2.3f, 3.2f, 3.7f, 4.4f, 5f, 5.5f, 6.7f, 
                7.2f, 8f, 8.1f, 9f, 12f, 17f, 30f, 48f, 60f, 79f, 100f, 220f, 600f, 900f, 1000f, 1800f, 
                2900f, 8400f, 14700f, 20000f, 34000f, 49000f, 65535f, 67000f};
        int[] expectedScaleIndeces = {0, 0, 0, 1, 1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 8, 9, 9, 10, 10, 11, 11, 
                12, 12, 13, 13, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15};
        
        TileMapFormatDescMock tmfd = new TileMapFormatDescMock();
        tmfd.loadScaleLevelsTable(scaleLevelsTable);
        
        for (int i=0; i<zoomLevels.length; i++) {
            float zoomLevel = zoomLevels[i];
            assertEquals(expectedScaleIndeces[i], tmfd.getScaleIndexFromZoomLevel(zoomLevel));
        }
    }
}
