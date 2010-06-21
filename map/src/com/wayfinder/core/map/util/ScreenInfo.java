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
package com.wayfinder.core.map.util;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

public class ScreenInfo {
    
    private static final Logger LOG = LogFactory
    .getLoggerForClass(ScreenInfo.class);
    
    private int iScreenHeight;
    private int iHalfScreenHeight;
    
    private int iScreenWidth;
    private int iHalfScreenWidth;
    
    private float iAspectRatio;
    private float iDPICorrection;
    
    private boolean iPortrait;
    
    // Please document me!
    private static final float DPI_CORRECTION_REFERENCE = 208f;
    
    public ScreenInfo(int width, int height, boolean portraitScreen) {
        iPortrait = portraitScreen;
        updateScreenSize(width, height);
    }
    
    public void updateScreenSize(int width, int height) {
        iScreenHeight  = height;
        iHalfScreenHeight = height >> 1;
        iScreenWidth   = width;
        iHalfScreenWidth = width >> 1;
        iAspectRatio   = iHalfScreenWidth / (float) iHalfScreenHeight;
        
        // Calculate DPI correction factor
    
//        if (height > width) {
//            iDPICorrection = (float)height / DPI_CORRECTION_REFERENCE;
//        } else {
//            iDPICorrection = (float)width / DPI_CORRECTION_REFERENCE;
//        }
        
        // Hardcoded DPI correction factor. This works well on Android for now.
        // Later we must find a more dynamic system.
        iDPICorrection = 2f;
    }
        
    public int getScreenHeight() {
        return iScreenHeight;
    }
    
    public int getHalfScreenHeight() {
        return iHalfScreenHeight;
    }
    
    public int getScreenWidth() {
        return iScreenWidth;
    }
        
    public int getHalfScreenWidth() {
        return iHalfScreenWidth;
    }
        
    public float getDPICorrection() {
        return iDPICorrection;
    }
    
    public float getAspectRatio() {
        return iAspectRatio;
    }
    
    public boolean isPortraitScreen() {
        return iPortrait;
    }
}
