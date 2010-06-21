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

public interface WFFont {
    
    public static final int STYLE_PLAIN     = 0;
    public static final int STYLE_BOLD      = 1;
    public static final int STYLE_ITALIC    = 2;
    public static final int STYLE_UNDERLINE = 3;
    
    public static final int SIZE_SMALL      = 0;
    public static final int SIZE_MEDIUM     = 1;
    public static final int SIZE_VERY_LARGE = 3;
    public static final int SIZE_LARGE      = 2;
    
    
    /**
     * Return the style used in this font. 
     * 
     * @return the style used in this font. 
     */
    public int getStyle();
    
    /**
     * Gets the standard height of a line of text in this font.
     * 
     * @return the height of the text in this font. 
     */
    public int getFontHeight();
    
    /**
     * Gets the total width for showing the specified String in this Font. 
     * 
     * @param str The string to be measured.  
     * @return The with of the string in pixels. 
     * @throws NullPointerException if str is null. 
     */
    public int getStringWidth(String str);

}
