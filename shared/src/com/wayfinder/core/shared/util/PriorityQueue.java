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
 * 
 */
public interface PriorityQueue {


    /**
     * Sorts and inserts an object into the heap
     * 
     * @param obj the item to insert.
     * @return The number of objects after the insertion method
     * @throws IllegalArgumentException If anObj is not of a type that the
     * current Comparator can handle
     */
    public int insert(Object obj);


    /**
     * Find the highest prioritized item in the priority queue.
     * 
     * @return the highest prioritized item, or null if empty.
     */
    public Object findHighestPrio();


    /**
     * Removes and returns the highest prioritized item from the 
     * priority queue.
     * 
     * @return the highest prioritized item, or null if empty.
     */
    public Object removeHighestPrio();


    /**
     * Test if the priority queue is logically empty.
     * 
     * @return true if and only if the queue is empty
     */
    public boolean isEmpty();


    /**
     * Make the priority queue logically empty.
     */
    public void clear();


    /**
     * Returns the number of objects in the heap
     * 
     * @return The number of objects
     */
    public int size();
}
