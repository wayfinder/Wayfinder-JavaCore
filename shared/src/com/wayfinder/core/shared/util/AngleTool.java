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

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * <p>Collection of methods to calculate with angles.</p>
 * 
 * <p>For conversion between MC2 and radian units of angle,
 * see {@link Position#mc2ToRadians(int)}
 * and {@link Position#radiansToMc2(double)}.</p>
 */
public abstract class AngleTool {
    /*
     * Future extension: compassMinAngleDiffRad that returns a
     * signed difference indicating if you should turn clockwise
     * (navigation: +, math: -) or counterclockwise to get from angle1
     * to angle2.
     *
     * We can use d = compassMinAbsAngleDiffRad and then calculate b =
     * angle1 + d and c = angle1 - d, both modulo 2Pi. Then check which
     * is closer to angle2. Equality won't happen since we do this in
     * floating point. Another way is if statements for the
     * combinations of quadrants that the angles can be in.
     */

    private static final Logger LOG = LogFactory
            .getLoggerForClass(AngleTool.class);
    
    /**
     * <p>Returns the course represented by traveling lonDiffNormalized
     * in E-W direction and latDiff in N-S direction.</p>
     * 
     * <p>The course is in radians and according to the compass. I.e.
     * <ul><li>0 is due north
     *     <li>Pi/2 (90 degrees) is due east
     *     <li>Pi is due south
     *     <li>3Pi/2 is due west.
     * </ul>
     * Negative angles are not returned.</p>
     * 
     * <p>This method calculates as if lonDiffNormalized and latDiff
     * are in a square coordinate system. It doesn't use spherical
     * trigonometry. This of course introduces errors if the
     * differences are too big.</p>
     *
     * <p>Normally, you obtain lonDiffNormalized by taking actual longitude
     * difference and converting it to equatorial scale by dividing it
     * with cos(lat).</p>
     *
     * <p>If lonDiffNormalized and latDiff are 0, 0 is returned.</p>
     * 
     * <h3>Using compass course to get cartesian coordinates</h3>
     * <p>Compared to how angles are usually
     * depicted in textbooks for mathematics and graphics, the compass angles
     * rotates (in positive direction) clockwise instead of counter-clockwise
     * and the zero angle points along the positive y-axis instead of the 
     * positive x-axis. Thus, the equivalent angle, t, in a mathematically
     * oriented system is <code>t = (Pi/2 - compassCourse)</code>.</p>
     * 
     * <p>Since the following properties hold (in infinite precision):
     * <ul><li>cos(PI/2 - t) = sin(t)</li>
     *     <li>sin(PI/2 - t) = cos(t)</li>
     * </ul>
     * You can calculate a position given x0, y0, a compass course and a radius
     * as:
     * <ol><li>x = x0 + radius * sin(compassCourse)</li> 
     *     <li>y = y0 + radius * cos(compassCourse)</li>
     * </ol>
     * That is, swap the sin and cos functions compared to how you would
     * normally do it and you don't have to convert the compass course.
     * </p>
     *

     * <p>
     * 
     * @param lonDiffNormalized - the longitudinal difference.
     * @param latDiff - the latitudal difference.
     * @return the compass course in radians.
     */
    public static double compassCourseRad(int lonDiffNormalized,
                                          int latDiff) {
        final String fname = "AngleTool.compassCourseRad(): ";

        /*
         *  This little drawing will make it easier for you to
         *  understand the comments in the code
         *
         *
         *                N, +y   c
         *                ^      /
         *                | dx |/
         *  -tan (a)  --- |----|-------------- +tan (a) ... pi/2
         *                |   / 
         *    NW          |  /     NE        
         *             dy |o------------- angle a between y-axis and line c
         *                |/                 tan(a) = dx/dy
         *    W, -x ----- O -------> E, +x
         *               /|
         *                |
         *    SW       /  |        SE
         *                S, -y
         *           /c2
         *
         *   If dy > 0, dx < 0, we will be in the NW quadrant and a <
         *   0 but when dx < 0 and dy < 0 the course is c2 but arctan
         *   return values are always above the EW-axis.
         */

        /*
         * since dx and dy are ints we don't run into the ususal
         * problems with floating point where things can be close to
         * zero but not identical to zero.
        */
        int dx = lonDiffNormalized;
        int dy = latDiff;

        if (dx == 0 && dy == 0) {
            return 0;
        }

        if (dy == 0) {
            // avoid division by zero
            if (dx > 0) {
                // due east
                double ret = Math.PI / 2;
                if(LOG.isTrace()) {
                    LOG.trace(fname, "dy == 0, dx > 0, ret " + ret);
                }

                return ret;
            } else { // dx < 0, dx == dy == 0 handled above
                // due west
                double ret = 3*Math.PI / 2;
                if(LOG.isTrace()) {
                    LOG.trace(fname, "dy == 0, dx < 0, ret " + ret);
                }

                return 3*Math.PI / 2;
            }
        }

        /*
         * dy != 0, (i.e. |dy| >= 1) - compute angle +/- to positive y
         * axis (N-S axis). This means that the argument is the
         * inverse of the usual notations for arctan() where you do
         * arctan(dy/dx) to get the angle relative to the positive dx
         * axis.
         * 
         * Then make the angle [0, 360] by checking each quadrant.
         */

        double tan = ((double) dx)/dy;
        double dyangle = WFMath.atan(tan);

        if(LOG.isTrace()) {
            LOG.trace(fname, "dy " + dy + " dx " + dx
                             + " dx/dy " + tan
                             + " dyangle/Pi " + dyangle/Math.PI);
        }

        // dy != 0 !!! (because that's handled above)
        double ret;
        if (dy > 0 && dx >= 0) {
            /*
             * dy > 0 => N-wards
             * dx > 0 => E-wards
             *
             * thus, NE, dyangle > 0
            */
            ret = dyangle;

            if(LOG.isTrace()) {
                LOG.trace(fname, "NE, " + ret);
            }
        } else if (dy < 0 && dx >= 0) {
            /*
             * dy < 0 => S-wards
             * dx > 0 => E-wards
             *
             * thus, SE, dyangle < 0 because the negative angle in the
             * NW quadrant is returned by arctan
             */
            ret = Math.PI + dyangle; // Pi - abs(dyangle)

            if(LOG.isTrace()) {
                LOG.trace(fname, "SE, " + ret);
            }
        } else if (dy < 0 && dx <= 0) {
            /*
             * dy < 0 => S-wards
             * dx < 0 => W-wards
             *
             * thus SW, dyangle > 0 because dx/dy > 0, the angle in the
             * NE quadrant is returned by arctan
             */
            ret = Math.PI + dyangle;

            if(LOG.isTrace()) {
                LOG.trace(fname, "SW, " + ret);
            }
        } else {
            // (dy > 0 && dx <= 0)

            /*
             * dy > 0 => N-wards
             * dx < 0 => W-wards
             *
             * thus, NW, dyangle < 0 because the negative angle in NW
             * quadrant is returned
             */
            ret = 2*Math.PI + dyangle; // 2*Pi - abs(dyangle)

            if(LOG.isTrace()) {
                LOG.trace(fname, "NW, " + ret);
            }
        }

        return ret;
    }


    /**
     * <p>Calculate the obtuse (< Pi), possibly acute (sv: spetsig)
     * (< Pi/2), angle difference between two angles which are both
     * defined with respect to some common ray (line).</p>
     *
     * <p>If we have positive angles alpha and beta, both < 2*Pi,
     * with beta >= alpha,
     * this is the lowest value of d1 = (beta-alpha) and d2 =
     * (alpha + (2*Pi - beta)) == (2*Pi + alpha - beta).
     * In other words, the smallest turn
     * needed to change direction from alpha to beta.</p>
     *
     * <p>The parameters aAngle1 and aAngle2 must be positive and
     * 0 <= aAngle1,aAngle2 < 2*Pi but it is not required that
     * aAngle1 <= aAngle2.</p>
     * 
     * @param aAngle1 - first angle.
     * @param aAngle2 - second angle.
     * @return the obtuse angle difference.
     */
    public static double compassMinAbsAngleDiffRad(double aAngle1,
                                                   double aAngle2) {

        double alpha, beta;
        if (aAngle1 < aAngle2) {
            alpha = aAngle1;
            beta = aAngle2;
        }
        else {
            alpha = aAngle2;
            beta = aAngle1;
        }

        // now, alpha <= beta
        double d1 = beta - alpha; // >= 0 since beta >= alpha
        double d2 = 2*Math.PI + alpha - beta; // also >= 0
        double d = Math.min(d1, d2);

        return d;
    }
}
