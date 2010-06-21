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

package com.wayfinder.core.shared.internal;


/**
 * A very limited version of the java.util.Collections utility class
 * from J2SE 1.4.2.
 */
public class WFCollections {

    /**
     * You're not supposed to create any objects of this class - use
     * its static methods.
     */
    private WFCollections() {}

    // ====================================================================
    // sorting
    /**
     * sorts aArray in ascending order. The objects must be mutually
     * comparable (if you have objects of different classes in the
     * array)
     *
     * This is currently implemented as a simple insertation sort with
     * O(N^2)-performance - we shouldn't need to sort large amounts of
     * data in the client.
     * 
     * @param array - the array to be sorted in place.
     * @see WFCollections#sort(java.util.Vector)
     */
    public static void sort(WFComparable[] array) {
        // Code from M.A. Weiss: Data Structures & Problem Solving
        // using Java, p 225
        for(int p=1; p < array.length; p++) {
            WFComparable tmp = array[p];
            // System.out.println("outer loop " + p + " " + tmp);

            int j = p;
            for(; j > 0 && (tmp.compareTo(array[j-1]) < 0); j--) {
                // System.out.println("inner loop " + j);
                array[j] = array[j-1];
            }
            array[j] = tmp;
        }
    }

    /**
     * sort a {@link java.util.Vector} of {@link WFComparable}.
     *
     * @param v - the Vector to be sorted in place.
     * @see WFCollections#sort(WFComparable[])
     */
    public static void sort(java.util.Vector v) {
        // the copy into WFComparable[], sort and copy back would
        // require as much code, but might be used if we do a more
        // complex sort

        int vSize = v.size();
        for(int p=1; p < vSize; p++) {
            WFComparable tmp = (WFComparable) v.elementAt(p);
            // System.out.println("outer loop " + p + " " + tmp);

            int j = p;
            for(; j > 0; j--) {
                WFComparable e = (WFComparable) v.elementAt(j-1);

                if (tmp.compareTo(e) < 0) {
                    v.setElementAt(e, j);
                }
                else {
                    break;
                }
            }
            v.setElementAt(tmp, j);
        }
    }
}
