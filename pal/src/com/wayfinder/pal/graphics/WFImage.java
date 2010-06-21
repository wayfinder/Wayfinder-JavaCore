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

package com.wayfinder.pal.graphics;

public abstract class WFImage {
    
    protected int[] argbData;
    protected int width;
    protected int height;
    
    protected WFImage(int width, int height, int color) {
        this.argbData = new int[width * height];
        for (int i = 0; i < this.argbData.length; i++) {
            this.argbData[i] = color;
        }
        this.width = width;
        this.height = height;
    }
    
    protected WFImage(int[] argbData, int width, int height) {
        this.argbData = argbData;
        this.width = width;
        this.height = height;
    }
    
    protected WFImage(int aWidth, int aHeight) {
        width = aWidth;
        height = aHeight;
    }
    
    protected WFImage() {
    }
    
    
    public int[] getARGBData() {
        if( (argbData == null) && hasNativeImage()) {
            int[] argb = new int[getHeight() * getWidth()];
            getNativeARGBData(argb, 0, getWidth(), 0, 0, getWidth(), getHeight());
            return argb;
        }
        return argbData;
    }
    

    public void getARGBData(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
        if(hasNativeImage()) {
            getNativeARGBData(rgbData, offset, scanlength, x, y, width, height);
        }
    }
    
    protected abstract void getNativeARGBData(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height);
    
    
    
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
    
    public abstract boolean hasNativeImage();
    
    public abstract void drawNativeImage(WFGraphics g, int x, int y);
    
    public abstract Object getNativeImage();
    
    public abstract boolean isWritable();
    
    public abstract WFGraphics getWFGraphics();
    
    
    public void paint(WFGraphics g, int x, int y) {
        if(hasNativeImage()) {
            drawNativeImage(g, x, y);
        }
        else if(this.argbData.length > 0) {
            //Nokia Series40 (6280) has a problem with drawRGB-method if 
            //one tries to draw something outside of the graphics boundaries. 
            //Therefore we need to calculate the boundaries to draw manually.
            
            int offset = 0;
            int scanLength = this.width;
            int imageWidth = this.width;
            int imageHeight = this.height;

            boolean renderLastRow = false;
            if(x < 0) {
                imageWidth += x;
                imageHeight -= 1;
                offset = -x;
                x = 0;
                renderLastRow = true;
            }
            if(x + imageWidth > g.getClipWidth()) {
                imageWidth = g.getClipWidth() - x;
            }
            
            if(y < 0) {
                imageHeight += y;
                offset += (-y * scanLength);
                y = 0;
            }
            if(y + imageHeight > g.getClipHeight()) {
                imageHeight = g.getClipHeight() - y;
            }
            
            if(scanLength > 0 && imageWidth > 0 && imageHeight > 0) {
                g.drawRGB(this.argbData, offset, scanLength, x, y, imageWidth, imageHeight, true);
                if(renderLastRow) {
                    g.drawRGB(this.argbData, offset + scanLength * imageHeight, imageWidth, x, y + imageHeight, imageWidth, 1, true);
                }
            }
        }
    }    
}
