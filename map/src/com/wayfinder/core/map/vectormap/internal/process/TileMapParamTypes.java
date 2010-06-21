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

public class TileMapParamTypes {
    
    private static final int TILE = 0;
    private static final int BITMAP = 1;
    private static final int FORMAT_DESC_DAY = 2;
    private static final int FORMAT_DESC_NIGHT = 3;
    private static final int FORMAT_DESC_DAY_CRC = 4;
    private static final int FORMAT_DESC_NIGHT_CRC = 5;
    private static final int UNKNOWN = 6;
    
    
    public static int getParamType( String str ) {
        if(str.length() <1){
            return UNKNOWN;
        }
        switch ( str.charAt(0) ) {
            case 'G': // Features
            case 'T': // Strings
                return TILE;
            case 'B':
            case 'b':
                return BITMAP;
            case 'D':
                return FORMAT_DESC_DAY;
            case 'd':
                return FORMAT_DESC_NIGHT;
            case 'C':
                return FORMAT_DESC_DAY_CRC;
            case 'c':
                return FORMAT_DESC_NIGHT_CRC;
        }
        return UNKNOWN;
    }
    public static boolean isMapFormatDesc(String paramsStr ) {
        return getParamType( paramsStr ) == FORMAT_DESC_DAY || getParamType( paramsStr ) == FORMAT_DESC_DAY_CRC ||
               getParamType( paramsStr ) == FORMAT_DESC_NIGHT || getParamType( paramsStr ) == FORMAT_DESC_NIGHT_CRC;
    }
    
    public static boolean isMap( String paramsStr ) {
        return getParamType( paramsStr ) == TILE;
    }
    
    public static boolean isBitmap(String paramsStr ) {
            return getParamType( paramsStr ) == BITMAP;
    }
    
    public static boolean isTmfdDay( String paramsStr ) {
        return getParamType( paramsStr ) == FORMAT_DESC_DAY;
    }
    
    public static boolean isTmfdNight( String paramsStr ) {
        return getParamType( paramsStr ) == FORMAT_DESC_NIGHT;
    }
    
    public static boolean isTmfdDayCrc( String paramsStr ) {
        return getParamType( paramsStr ) == FORMAT_DESC_DAY_CRC;
    }
    
    public static boolean isTmfdNightCrc( String paramsStr ) {
        return getParamType( paramsStr ) == FORMAT_DESC_NIGHT_CRC;
    }
    
    public static boolean isTmfdCRC( String paramsStr ) {
        return getParamType( paramsStr ) == FORMAT_DESC_DAY_CRC || 
               getParamType( paramsStr ) == FORMAT_DESC_NIGHT_CRC; 
    }
    
    public static boolean hasValidParamType(String paramsStr) {
        return getParamType(paramsStr) != UNKNOWN;
    }
        
}
