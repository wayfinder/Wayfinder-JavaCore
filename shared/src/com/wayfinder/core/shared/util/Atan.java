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
package com.wayfinder.core.shared.util;

/**
 * <p>Implements the arctangent (arctan, atan, tan^-1) function according to the
 * method in Seventh Edition of UNIX by Bell Laboratories Jan. 1979. The
 * implementation therein was (probably) taken from
 * Cheney; Hart; Lawson; Maehly; Mesztenyi: Computer Approximations.
 * Wiley, New York. 1968.</p>
 * 
 * Since the algorithm is in many numerical methods textbooks and the original
 * paper was published in academia, it is assumed that this code doesn't have
 * license issues. 
 */
final class Atan {

    /**
     * You are not supposed to create instances of this class.
     */
    private Atan() {
    }
    

    private static final double sq2p1 = 2.414213562373095048802e0;
    private static final double sq2m1 =  .414213562373095048802e0;
    private static final double p4    =  .161536412982230228262e2;
    private static final double p3    =  .26842548195503973794141e3;
    private static final double p2    =  .11530293515404850115428136e4;
    private static final double p1    =  .178040631643319697105464587e4;
    private static final double p0    =  .89678597403663861959987488e3;
    private static final double q4    =  .5895697050844462222791e2;
    private static final double q3    =  .536265374031215315104235e3;
    private static final double q2    =  .16667838148816337184521798e4;
    private static final double q1    =  .207933497444540981287275926e4;
    private static final double q0    =  .89678597403663861962481162e3;
    private static final double PIO2  = 1.5707963267948966135E0;

    /**
     * 
     * @param arg - argument in range [-0.414..., +0.414...].
     */
    private static double xatan(double arg) {
        // 20 arithmetic floating point operations.
        double argsq, value;

        argsq = arg*arg;
        value = ((((p4*argsq + p3)*argsq + p2)*argsq + p1)*argsq + p0);
        value = value/(((((argsq + q4)*argsq + q3)*argsq + q2)*argsq + q1)*argsq + q0);

        return value*arg;
    }

    /**
     * Reduce the argument and call xatan().
     * 
     * @param arg - positive argument
     */
    private static double satan(double arg) {
        double ret;
        if (arg < sq2m1) {
            ret = xatan(arg);
        } else if(arg > sq2p1) {
            ret = PIO2 - xatan(1/arg);
        } else {
            ret = PIO2/2 + xatan((arg-1)/(arg+1));
        }
        
        return ret;
    }

    /**
     * Computes the value of inverse tangent function of the given
     * argument.
     * 
     * @param arg - argument to arctangent
     * @return angle in radians in range [-Pi/2, +Pi/2].
     */
    static double atan(double arg) {
        double ret;
        
        if(arg > 0) {
            ret = satan(arg);
        } else {
            ret = -satan(-arg);
        }
        
        return ret;
    }    
}
