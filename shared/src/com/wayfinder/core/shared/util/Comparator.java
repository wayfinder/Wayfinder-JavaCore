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
 * Defines comparison methods useable to impose a total ordering on a 
 * collection of objects.
 *
 * The ordering you can produce with these methods is the class's natural 
 * ordering, and the class's implementation of compare is its natural 
 * comparison method.
 * 
 * 
 */
public interface Comparator {


    /**
     * Compares two objects to determine the sorting order
     * <p>
     * 
     * @param obj1 The first object to check
     * @param obj2 The second object to check
     * 
     * @return  0 if the objects are equal
     *         -1 if anObj1 should be sorted before anObj2
     *          1 if anObj2 should be sorted before anObj1
     * 
     * @throws IllegalArgumentException
     */
    public int compare(Object obj1, Object obj2) throws IllegalArgumentException;


    /**
     * Returns true if this Object can be compared by the Comparator
     * 
     * @param obj An Object
     * @return true if and only if this Comparator can handle comparison of this
     * object
     */
    public boolean canBeCompared(Object obj);

}
