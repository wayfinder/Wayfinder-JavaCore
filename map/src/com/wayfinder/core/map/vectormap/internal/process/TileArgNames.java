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

public class TileArgNames {
    
      public static final int COORDS       = 0;
      public static final int COORD        = 1;
      /* Not used */
      public static final int NAME_TYPE    = 2;
      public static final int LEVEL        = 3;
      /* Not used, Level will be used*/
      public static final int LEVEL_1      = 4;
      public static final int IMAGE_NAME   = 5;
      public static final int RADIUS       = 6;
      public static final int COLOR        = 7;
      public static final int MIN_SCALE    = 8;
      public static final int MAX_SCALE    = 9;
      public static final int BORDER_COLOR = 10;
      
      /* Not used */
      public static final int FONT_TYPE    = 11;
      /* Not used */
      public static final int FONT_SIZE    = 12;
      
      public static final int WIDTH        = 13;
      public static final int BORDER_WIDTH = 14;
      public static final int WIDTH_METERS = 15;
      
      public static final int TIME         = 17;
      public static final int ID           = 18;
      
      /* This argument determines the events duration, with resolution of 5 minutes */ 
      public static final int DURATION     = 19;
      
    /** Creates a new instance of TileArgNames */
    public TileArgNames() {
    }
    
}
