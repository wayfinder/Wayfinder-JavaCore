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
package com.wayfinder.core.map.vectormap.internal.drawer;

import com.wayfinder.pal.graphics.WFFont;
import com.wayfinder.pal.graphics.WFGraphicsFactory;

/**
 * Collection of usable constants and some math
 */
public class Utils {
    
    public static final boolean EMULATOR = false;
    public static final boolean REQUEST_TRACE = false;
    
    public static final boolean CACHE_TRACE = false;
    public static final boolean WARN_ABOUT_LARGE_TILEMAPS = false;
    
    private static final float EARTH_RADIUS = 6378137.0f;
    private static final float POW2_32      = 4294967296.0f;
    
    public static final float NORTH_ROTATION_ANGLE = (float)(Math.PI/2);
    
    public static final int HIGHWAY_SIGN_TEXT_COLOR   = 0xFFFFFF;
    public static final int HIGHWAY_SIGN_BACKGROUND_COLOR = 0x4444FF;
    public static final int HIGHWAY_SIGN_OUTLINE_COLOR    = 0xFFFFFF;
    public static final int ROAD_TEXT_COLOR         = 0xFF000000;
    public static final int BLUE_BOX_COLOR          = 0xFFFFFFFF;
    
    /**
     * the border color for the cellID circle 
     */
    public static final int CELLID_CIRCLE_BORDER_COLOR = 0xAFB0AF;
    /**
     * the fill color for the cellID circle
     */
    public static final int CELLID_CIRCLE_FILL_COLOR = 0x44FFFFFF;

    
    public static final int FONT_BOLD_CONSTANT = 1;
    public static final int FONT_PLAIN_CONSTANT = 2;
    
    public static final float MC2SCALE_TO_METER = (float)(EARTH_RADIUS*2.0*Math.PI / POW2_32);
    public static final float MC2SCALETIMES2 = Utils.MC2SCALE_TO_METER * Utils.MC2SCALE_TO_METER;
    public static final float METER_TO_MC2SCALE = POW2_32 / ((float)(EARTH_RADIUS*2.0*Math.PI));
    public final static float SQRT3 = 1.732050807568877294f;
    
    public final static int NBR_TILES_BELOW_SHOW = 10;
    public static final int MAX_LEVEL = 15;
    public static final int ELEMENTS_PER_GEODATA = 5;
    public static final int CLEAN_FROM_IMPORTANCE = 2;
    
    public static final int MAX_INTEGER = Integer.MAX_VALUE;            
    public static final int MIN_INTEGER = Integer.MIN_VALUE;
    
    public static final float degreeToRadianFactor = (float)( Math.PI / 180 );
    public static final float radianTodegreeFactor = (float)( 180 / Math.PI );
    
    /**
     * For unpacked tilemap data. First comes HEADER_OFFSET_LENGTH header data, then the actual coordinates
     */
    public static final int HEADER_OFFSET_LENGTH = 11;
    
    public static final boolean TIME_APP = false;
    public static final boolean TIME_RMS = false;
    
    public static final float TWENTYFIVE_SEC_FACTOR = 250/36;
    public static final float FIFTEEN_SEC_FACTOR    = 150/36;
    
    
    public static final int FONT_SMALL          = 0;
    public static final int FONT_SMALL_BOLD     = 1;
    public static final int FONT_MEDIUM         = 2;
    public static final int FONT_MEDIUM_BOLD    = 3;
//    public static final int FONT_SMALL_HEIGHT = FONT_SMALL.getFontHeight();
//    public static final int FONT_SMALL_BOLD_HEIGHT = FONT_SMALL_BOLD.getFontHeight();
    
    private WFFont m_FontSmall;
    private WFFont m_FontSmallBold;
    private WFFont m_FontMedium;
    private WFFont m_FontMediumBold;
    
    private static Utils m_Instance = new Utils();
    private WFGraphicsFactory mFactory;
    
    private Utils() {
        
    }
    
    public static Utils createUtils() {        
        return m_Instance;
    }
    
    /**
     * Creates new Utils object
     */
    public void init(WFGraphicsFactory aFactory) {
        mFactory = aFactory;        
        m_FontSmall = mFactory.getWFFont(WFFont.SIZE_SMALL, WFFont.STYLE_PLAIN);
        m_FontSmallBold = mFactory.getWFFont(WFFont.SIZE_SMALL, WFFont.STYLE_BOLD);
        m_FontMedium = mFactory.getWFFont(WFFont.SIZE_MEDIUM, WFFont.STYLE_PLAIN);
        m_FontMediumBold = mFactory.getWFFont(WFFont.SIZE_MEDIUM, WFFont.STYLE_BOLD);        
    }
    
    public static Utils get() {
        return m_Instance;
    }
    
    public WFFont getFont(int type) {
        
        switch (type) {
            case FONT_SMALL:
                return m_FontSmall;
                
            case FONT_SMALL_BOLD:
                return m_FontSmallBold;
                
            case FONT_MEDIUM:
                return m_FontMedium;
                
            case FONT_MEDIUM_BOLD:
                return m_FontMediumBold;            
        }        
        return null;
    }
    
    /**
     * Arccos
     *
     * @param x x value to find arc cos for
     * @return arccos of x
     */
    public static float acos(float x) {
        float f=asin(x);
        if(Float.isNaN(f))
            return f;
        return (float)Math.PI/2-f;
    }
    
    /**
     * Arcsin
     *
     * @return arcsin of x
     * @param x x value to find arc sin for
     */
    public static float asin(float x) {
        if( x<-1. || x>1. ) return Float.NaN;
        if( x==-1. ) return (float)-Math.PI/2;
        if( x==1 ) return (float)Math.PI/2;
        return atan(x/(float)Math.sqrt(1-x*x));
    }
    
    /**
     * 2D cross product. Returns a z-value.
     *
     * @param v 1st Vector
     * @param u 2nd Vector
     * @return The cross product
     */
    public static int cross2D(int[]v,int[]u){
        return v[0] * u[1] - v[1] * u[0];
    }
    
    /**
     * 2D cross product
     *
     * @param v a float array
     * @param u a float array
     * @return The cross product
     */
    public static float cross2D(float[]v,float[]u){
        return v[0] * u[1] - v[1] * u[0];
    }
    
    /**
     * Arctan
     *
     * @param x value to find arc tan for
     * @return arctan x
     */
    public static float atan(float x){
        boolean signChange=false;
        boolean Invert=false;
        int sp=0;
        float x2, a;
        // check the sign change
        if(x<0.) {
            x=-x;
            signChange=true;
        }
        // check the invertation
        if(x>1.) {
            x=1/x;
            Invert=true;
        }
        // process shrinking domain until x<PI/12
        while(x>(float)Math.PI/12) {
            sp++;
            a=x+SQRT3;
            a=1/a;
            x=x*SQRT3;
            x=x-1;
            x=x*a;
        }
        // calculation core
        x2=x*x;
        a=x2+1.4087812f;
        a=0.55913709f/a;
        a=a+0.60310579f;
        a=a-(x2*0.05160454f);
        a=a*x;
        // process until sp=0
        while(sp>0) {
            a=a+(float)Math.PI/6;
            sp--;
        }
        // invert
        if(Invert) a=(float)Math.PI/2-a;
        // sign change
        if(signChange) a=-a;
        //
        return a;
    }
    
    /**
     * Matrix multiplication of 4x4 float matrices
     *
     * @param a Left 4x4 matrix
     * @param b Right 4x4 matrix
     * @return the product matrix
     */
    public static float[][] multiply(float[][]a,float[][]b) {
        float[][]c = new float[4][4];
        c[0][0] = a[0][0]*b[0][0] + a[0][1]*b[1][0] + a[0][2]*b[2][0] + a[0][3]*b[3][0];
        c[0][1] = a[0][0]*b[0][1] + a[0][1]*b[1][1] + a[0][2]*b[2][1] + a[0][3]*b[3][1];
        c[0][2] = a[0][0]*b[0][2] + a[0][1]*b[1][2] + a[0][2]*b[2][2] + a[0][3]*b[3][2];
        c[0][3] = a[0][0]*b[0][3] + a[0][1]*b[1][3] + a[0][2]*b[2][3] + a[0][3]*b[3][3];
        c[1][0] = a[1][0]*b[0][0] + a[1][1]*b[1][0] + a[1][2]*b[2][0] + a[1][3]*b[3][0];
        c[1][1] = a[1][0]*b[0][1] + a[1][1]*b[1][1] + a[1][2]*b[2][1] + a[1][3]*b[3][1];
        c[1][2] = a[1][0]*b[0][2] + a[1][1]*b[1][2] + a[1][2]*b[2][2] + a[1][3]*b[3][2];
        c[1][3] = a[1][0]*b[0][3] + a[1][1]*b[1][3] + a[1][2]*b[2][3] + a[1][3]*b[3][3];
        c[2][0] = a[2][0]*b[0][0] + a[2][1]*b[1][0] + a[2][2]*b[2][0] + a[2][3]*b[3][0];
        c[2][1] = a[2][0]*b[0][1] + a[2][1]*b[1][1] + a[2][2]*b[2][1] + a[2][3]*b[3][1];
        c[2][2] = a[2][0]*b[0][2] + a[2][1]*b[1][2] + a[2][2]*b[2][2] + a[2][3]*b[3][2];
        c[2][3] = a[2][0]*b[0][3] + a[2][1]*b[1][3] + a[2][2]*b[2][3] + a[2][3]*b[3][3];
        c[3][0] = a[3][0]*b[0][0] + a[3][1]*b[1][0] + a[3][2]*b[2][0] + a[3][3]*b[3][0];
        c[3][1] = a[3][0]*b[0][1] + a[3][1]*b[1][1] + a[3][2]*b[2][1] + a[3][3]*b[3][1];
        c[3][2] = a[3][0]*b[0][2] + a[3][1]*b[1][2] + a[3][2]*b[2][2] + a[3][3]*b[3][2];
        c[3][3] = a[3][0]*b[0][3] + a[3][1]*b[1][3] + a[3][2]*b[2][3] + a[3][3]*b[3][3];
        return c;
    }
    
    /**
     * Matrix multiplication of 4x4 double matrices
     *
     * @param a Left 4x4 matrix
     * @param b Right 4x4 matrix
     * @return the product matrix
     *
     */
    public static double[][] multiply(double[][]a,double[][]b) {
        double[][]c = new double[4][4];
        c[0][0] = a[0][0]*b[0][0] + a[0][1]*b[1][0] + a[0][2]*b[2][0] + a[0][3]*b[3][0];
        c[0][1] = a[0][0]*b[0][1] + a[0][1]*b[1][1] + a[0][2]*b[2][1] + a[0][3]*b[3][1];
        c[0][2] = a[0][0]*b[0][2] + a[0][1]*b[1][2] + a[0][2]*b[2][2] + a[0][3]*b[3][2];
        c[0][3] = a[0][0]*b[0][3] + a[0][1]*b[1][3] + a[0][2]*b[2][3] + a[0][3]*b[3][3];
        c[1][0] = a[1][0]*b[0][0] + a[1][1]*b[1][0] + a[1][2]*b[2][0] + a[1][3]*b[3][0];
        c[1][1] = a[1][0]*b[0][1] + a[1][1]*b[1][1] + a[1][2]*b[2][1] + a[1][3]*b[3][1];
        c[1][2] = a[1][0]*b[0][2] + a[1][1]*b[1][2] + a[1][2]*b[2][2] + a[1][3]*b[3][2];
        c[1][3] = a[1][0]*b[0][3] + a[1][1]*b[1][3] + a[1][2]*b[2][3] + a[1][3]*b[3][3];
        c[2][0] = a[2][0]*b[0][0] + a[2][1]*b[1][0] + a[2][2]*b[2][0] + a[2][3]*b[3][0];
        c[2][1] = a[2][0]*b[0][1] + a[2][1]*b[1][1] + a[2][2]*b[2][1] + a[2][3]*b[3][1];
        c[2][2] = a[2][0]*b[0][2] + a[2][1]*b[1][2] + a[2][2]*b[2][2] + a[2][3]*b[3][2];
        c[2][3] = a[2][0]*b[0][3] + a[2][1]*b[1][3] + a[2][2]*b[2][3] + a[2][3]*b[3][3];
        c[3][0] = a[3][0]*b[0][0] + a[3][1]*b[1][0] + a[3][2]*b[2][0] + a[3][3]*b[3][0];
        c[3][1] = a[3][0]*b[0][1] + a[3][1]*b[1][1] + a[3][2]*b[2][1] + a[3][3]*b[3][1];
        c[3][2] = a[3][0]*b[0][2] + a[3][1]*b[1][2] + a[3][2]*b[2][2] + a[3][3]*b[3][2];
        c[3][3] = a[3][0]*b[0][3] + a[3][1]*b[1][3] + a[3][2]*b[2][3] + a[3][3]*b[3][3];
        return c;
    }

    /**
     * Returns a float 4x4 unity matrix
     *
     * @return a 4x4 unity matrix
     */
    public static float[][] getUnityMatrix(){
        float[][]u = {{1f,0f,0f,0f},{0f,1f,0f,0f},{0f,0f,1f,0f},{0f,0f,0f,1f}};
        return u;
    }
    
    /**
     * Returns a DPI-corrected matrix
     *
     * @param s the dpi correction value
     * @return a DPI-corrected matrix
     */
    public static float[][] getDPIMatrix(float s){
        float[][]u = {{s,0f,0f,0f},{0f,s,0f,0f},{0f,0f,s,0f},{0f,0f,0f,1f}};
        return u;
    }
    
    /**
     * From Wayfinder sources.
     * @param x 
     * @return 
     */
    public static int getNbrBitsSignedGeneric( int x ){
        // Ugly fix. Pretend that one bit is needed for the value 0.
        if ( x == 0 ) {
            return 1;
        }
        
        if ( x < 0 ) {
            x = ~x + 1;
        }
        return getNbrBitsGeneric(x) + 1;
    }
    
    /**
     * From Wayfinder sources.
     * @param x 
     * @return 
     */
    public static int getNbrBitsGeneric( long x ) {
        // Ugly fix. Pretend that one bit is needed for the value 0.
        if ( x == 0 ) {
            return 1;
        }
        int r = 0;
        while ( x != 0 ) {
            x >>= 1;
            ++r;
        }
        
        return r;
    }
    
    /**
     * Returns a normalized vector
     *
     * @param v the Long array to normalize (Float)
     * @return a normalized vector
     */
    public static float[] normalize(float[] v){
        float len = length(v);
        v[0]/=len;
        v[1]/=len;
        return v;
    }
   
    
    /**
     * Returns a normalized vector
     *
     * @param v the Long array to normalize (Long)
     * @return a normalized vector
     *
     * @deprecated for performance use normalize(long[], float[] instead)
     */
    public static float[] normalize(long[] v){
        float[] target = new float[2];
        normalize(v, target);
        return target;
    }

    /**
     * Returns a normalized vector (in R^2)
     *
     * @param v the Long array to normalize (Long)
     * @return a normalized vector
     */
    public static void normalize(long[] v, float[] target){
        // FIXME: is double precision really needed here? Faster if
        // len is a float instead of a double
        double len = length(v);
        target[0] = (float)(v[0] / len);
        target[1] = (float) (v[1] / len);
    }

    
    /**
     * Returns a normalized vector
     *
     * @param v the array to normalize (Integer)
     * @return a normalized vector
     */
    public static float[] normalize(int[] v){
        float len = length(v);
        return new float[]{v[0] / len, v[1] / len};
    }
    
    
    /**
     * Returns the length of a vector
     *
     * @param v the vector (Long)
     * @return the length of the vector (Double)
     */
    public static double length(long[] v){
        return Math.sqrt(v[0] * v[0] + v[1] * v[1]);
    }
    
    
    /**
     * Returns the length of a vector
     *
     * @param v the vector (Float)
     * @return the length of the vector (Float)
     */
    public static float length(float[] v){
        return (float)Math.sqrt(v[0] * v[0] + v[1] * v[1]);
    }
    
    
    /**
     * Returns the length of a vector
     *
     * @param v the vector (Integer)
     * @return the length of the vector (Float)
     */
    public static float length(int[] v){
        return (float)Math.sqrt(v[0] * v[0] + v[1] * v[1]);
    }    
}
