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
 * <p>Our variant of the java.lang.Comparable ifc from J2SE 1.4.2.</p>
 * 
 * <p>To cut back on number of classes and objects this ifc also has extra
 * methods to be used instead of creating classes that implement the
 * Comparator ifc. This should be sufficient for the simple sorting
 * tasks in our application although not as flexible as a real
 * comparator class.</p>
 *
 * <p>Beware of potential pitfalls if you implement this inconsistent
 * with overriding of {@link Object#equals()}.</p>
 */
public interface WFComparable {

    /**
     * For the conditions on the implementation, see
     * java.lang.Comparable.compareTo()
     *
     * @param o - the object to compare with.
     * @return <ul><li>< 0 iff this object is LESS THAN o
     *             <li>0 iff this object is EQUAL TO o
     *             <li>> 0 iff this object is GREATER THAN o
     *         </ul>
     * 
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this object.
     */
    public int compareTo(Object o);

    /**
     * <p>Compare using the specified aMethod which is defined by the
     * implementing class.</p>
     * 
     * <p>For instance for a book class, 0 could mean
     * comparision on title, and 1 comparision on author name. Can also
     * be used for chaining order between ascending and descending but
     * preferably this should be done by the sorting algorithm instead.</p>
     * 
     * @param o - the object to compare with.
     * @param aMethod - what method to use when comparing. The values are
     *        defined by the implementor.
     * @return <ul><li>< 0 iff this object is LESS THAN o
     *             <li>0 iff this object is EQUAL TO o
     *             <li>> 0 iff this object is GREATER THAN o
     *         </ul>
     * 
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this object.
     */
    public int compareTo(Object o, int aMethod);
}
