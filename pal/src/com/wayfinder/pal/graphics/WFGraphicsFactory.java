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


public interface WFGraphicsFactory {
    
    /**
     * Returns a WFFont object
     * 
     * @param size One of the SIZE constants in WFFont
     * @param style One of the STYLE constants in WFFont
     * @return A WFFont object
     * @throws IllegalArgumentException If the height or style is not one of the
     * constants in WFFont
     */
    public abstract WFFont getWFFont(int size, int style) throws IllegalArgumentException;

    //-------------------------------------------------------------------------
    // Pictures
    
    /**
     * 
     * @param buf
     * @param offset
     * @param length
     * @return
     */
    public WFImage createWFImage(byte[] buf, int offset, int length);
    
    /**
     * 
     * @param width
     * @param height
     * @return
     */
    public WFImage createWFImage(int width, int height);
    
    /**
     * 
     * @param width
     * @param height
     * @param color
     * @return
     */
    public WFImage createWFImage(int width, int height, int color);
    
    /**
     * 
     * @param aResourceName
     * @return
     */
    public WFImage createWFImage(String aResourceName);
    
    /**
     * 
     * @param rgb
     * @param width
     * @param height
     * @param processAlpha
     * @return
     */
    public WFImage createWFImage(int[] rgb, int width, int height, boolean processAlpha);
    

}
