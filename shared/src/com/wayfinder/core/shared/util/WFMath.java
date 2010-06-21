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

package com.wayfinder.core.shared.util;

/**
 * <p>This class contains utility mathematical constants and replacements for
 * needed functions that are absent in the CLDC 1.1 libraries.</p>
 * 
 * FIXME: should we move the replacement functions to the PAL so we can use
 * the native ones on platforms that are better equipped?
 */
public abstract class WFMath {

    /**
     * 2^32 doesn't fit in an int. This is only meant to be used by
     * the compiler to calculate other constants.
     */
    public static final double POW_2_32 = 4294967296.0;

    
    /**
     * <p>Computes the arctangent.</p>
     * 
     * <p>CLDC 1.1 doesn't include Math.atan() and this function acts as
     * a replacement.</p>
     * 
     * @param arg - argument to arctangent
     * @return angle in radians in range [-Pi/2, +Pi/2].
     */
    public static double atan(double arg) {
        // keep the calculation in a separate class since it is quite long
        // and we might want to change to another implementation.
        return Atan.atan(arg);
    }


    /**
     * Natural logarithm of 2.
     */
    public static final double LN_2 = 0.69314718055994530;
    
    /**
     * <p>Returns the natural logarithm of a double positive value.</p>
     * 
     * <p>Not guarantees on accuracy or method are provided. Practical testing
     * seems to indicate an absolute accuracy of 1E-13 for a <= 1E9.</p>
     * 
     * <p>Special cases:
     * <ul><li>log(NaN) returns NaN
     *     <li>log(a); a < 0 returns NaN.
     *     <li>log(Double.POSITIVE_INFINITY) returns Double.POSITIVE_INFINITY.
     *     <li>log(0) returns Double.NEGATIVE_INFINITY. 
     * 
     * @param a - a value.
     * @return the value ln a, the natural logarithm of a.
     */
    public static double log(double a){                
        // migrated from jWMMG - the origin of the algorithm is not known.
        if (Double.isNaN(a) || a < 0) {
            return Double.NaN;
        } else if (a == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        } else if (a == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        
        long lx;
        if (a > 1) {
            lx = (long)(0.75*a); // 3/4*x
        } else {
            lx = (long)(0.6666666666666666666666666666666/a); // 2/3/x
        }
        
        int ix;
        int power;
        if (lx > Integer.MAX_VALUE) {
            ix = (int) (lx >> 31);
            power = 31;
        } else {
            ix = (int) lx;
            power = 0;
        }
        
        while (ix != 0) {
            ix >>= 1;
            power++;
        }
        
        double ret;
        if (a > 1) {
            ret = lnInternal(a / ((long) 1<<power)) + power * LN_2;
        } else {
            ret = lnInternal(a * ((long) 1<<power)) - power * LN_2;
        }
        return ret;
    }
    
    /**
     * <p>Internal helper method for log calculations.</p>
     * 
     * <p>c. The best accuracy it's between 0.5 and 1.5.</p>
     * 
     * @param x a value between 0 and 2
     */
    private static double lnInternal(double x){
        double a =  1 - x;
        double s = -a;
        double t = a;
        
        for (int i = 2; i < 25; i++){
            t = t*a;
            s -= t/i;
        }
        return s;
    }    
    

    /**
     * <p>Returns the value of the first argument raised to the power of the second 
     * argument.</p>
     * 
     * <p>The power is calculated with repeated multiplication, not by taking
     * logarithms (since the ln() and exp() functions are missing from CLDC).
     * TODO: add assesment on accuracy.</p>
     * 
     *  <p>Special cases:
     *  <ul><li>pow(a, 0) returns 1.0
     *      <li>pow(0, b); b <> 0 returns +0;
     *      <li>pow(0, 0) throws IllegalArgumentException since for our use,
     *      the definition of 0<sup>0</sup> for various kind of zeros is not
     *      interesting. 
     *  </ul>
     * 
     * @param a base
     * @param b exponent
     * @return the value a<sup>b</sup>
     */
    public static double pow(double a, int b){
        if (a == 0.0) {
            if (b == 0) {
                throw new IllegalArgumentException("0^0 not supported");
            } else {
                return +0;
            }
        }
        if (b == 0) {
            return 1.0;
        }

        // a,b <> 0
        double result = a;
        int n = Math.abs(b);
        for(int i = 2; i <= n; i++){
            result *= a;
        }
        
        if (b < 0) {
            result = 1/result;
        }
        return result;
    }


    /**
     * <p>Rounds an integer to a whole number of integer units.</p>
     * 
     * <p>Example:<code>roundTo(143, 50) = 150</code></p>
     *
     * @param value - value to be rounded, >= 0.
     * @param unit - unit to round to, > 0.
     * @return the rounded value.
     * @throws IllegalArgumentException if value < 0 or unit < 1.
     */
    public static int roundTo(int value, int unit) {
        if (value < 0 || unit < 1) {
            throw new IllegalArgumentException();
        }
        
        return ((value + unit/2) / unit) * unit;
    }

    /**
     * <p>Returns the closest int to the argument. The function returns
     * code>(int) Math.floor(a + 0.5)</code>. This is the same specification
     * as for java.lang.Math from J2SE 1.5.</p>
     * 
     * @param a - a float to be rounded.
     * @return the value of a rounded to the nearest int value.
     */
    public static int round(float a) {
        return (int) Math.floor(a + 0.5f);
    }

    /**
     * <p>Returns the closest int to the argument. The function returns
     * code>(int) Math.floor(a + 0.5)</code>. This is the same specification
     * as for java.lang.Math from J2SE 1.5.</p>
     * 
     * @param a - a double to be rounded.
     * @return the value of a rounded to the nearest int value.
     */
    public static int round(double a) {
        return (int) Math.floor(a + 0.5f);
    }


    /**
     * <p>Returns the hyperbolic sine of a double value.</p>
     * 
     * <p>The hyperbolic sine of  x is defined to be
     * (<i>e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup></i>)/2; 
     * where e is Euler's number.</p>
     * 
     * <p>The function is computed using the 8th order Maclaurin series for
     * sinh. Unfortunately, despite the sinh function being entire (it is a
     * composition of the exponential function, which is enitre), the series
     * converge only slowly. Practical testing shows that the relative error
     * for x=2 is ≈ 1E-11 and for x=10 the accuracy is down to 1E-2 (which is
     * unusable). Thus, this function should only be used for small values of x.
     * It seems to have worked ok for the mercator map calculations. The
     * implentation in jWMMG was several orders of magnitude worse.</p>
     * 
     * TODO: Document the NaN, Inf and zero cases.
     * 
     * @param x - number or angle in radians.
     * @return An approximation of the hyperbolic sine of x.
     */
    public static double sinh(double x) {
        /* 
         * according to http://mathworld.wolfram.com/HyperbolicSine.html
         * 
         * the Maclaurin series for sinh is sum{n=0...Inf (x^(2*n+1) / (2*n+1)!)}
         * = x + x^3/6 + x^5/120 + ...
         *
         * For the divider to fit in an int, it must be max 12! (13! is 6.2E9)
         * thus (2n+1) <= 12; n <= 5. But then the last term affects the result
         * with at least 2E-9 (for x=1). We opt for a little more precision
         * to be on the safe side for Mercator calculations. In jWMMG, N==3.
         * 
         *  N=6 => 1/13! ≈ 1.61E-10
         *  N=7 => 1/15! ≈ 7.65E-13
         *  N=8 => 1/17! ≈ 2.81E-15
         *  N=9 => 1/19! ≈ 8.22E-18
         *         1/(2^63 - 1) ≈ 1.08E-19
         *
         */

        final int N = 8;

        // first term is for n=0: x^(2*0+1)/(2*0+1)! = x^1/1! = x
        double result = x;
        long divider = 1;
        
        for (int n = 1; n <= N; n++){
            divider *= (2*n + 1) * 2*n; //(2n+1)! = (2n+1)(2n)*(2n-1)! 
            result += pow(x, 2*n + 1) / divider; 
        }

        return result;
    }
}
