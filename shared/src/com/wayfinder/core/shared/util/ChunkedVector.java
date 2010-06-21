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

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * A vector with a fixed size that keeps track of how many elements
 * (sequentially) that are not null. Also has a interface to get part
 * of the vector as an array.
 *
 * use case: when searching the server can say that there are 54
 * matches in total and send the first 10 who are stored in a
 * ChunkedVector. The ui displays these on the screen. When the user
 * wants to see the next 10 a request is sent to the server which
 * sends match 10-19. These are stored in the ChunkedVector in
 * addition to the 10 elements we already got. If the user wants to
 * see the 10 previous matches, these are already downloaded and we
 * don't have to query the server again.
 *
 * This class is thread-safe.
 */
public class ChunkedVector {
    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */

    private static final Logger LOG = LogFactory
        .getLoggerForClass(ChunkedVector.class);

    /**
     * storage for elements
     */
    private Object[] m_vector;

    /**
     * the last position that is not null or -1 if no elements present
     */
    private int m_fillEndIndex;


    /**
     * Creates a ChunkedVector with a capacity of aCapacity
     * objects. The elements will be indexed [0 ... aCapacity-1]
     *
     * @param aCapacity >= 0. If the capacity is 0 you can't add any
     * objects, getNbrElementsPresent() will always return 0 and the
     * behavior of getElements() and getElementAt() are undefined.
     */
    public ChunkedVector(int aCapacity) {
        final String FNAME = "ChunkedVector.ChunkedVector()";
        if(LOG.isTrace()) {
            LOG.trace(FNAME,
                      "capactity=" + aCapacity);
        }
        
        if (aCapacity < 0) {
            throw new IllegalArgumentException(FNAME + " invalid capacity: "
                                               + aCapacity);
        }

        synchronized (this) { 
            m_vector = new Object[aCapacity];
            m_fillEndIndex = -1;
        }
    }


    /**
     * @return the capacity of this ChunkedVector.
     */
    public synchronized int getCapacity() {
        return m_vector.length;
    }

    /**
     * @return the maximum occupied index, -1 if pushFillEnd hasn't
     * been called yet.
     */
    public synchronized int getFillEndIndex() {
        return m_fillEndIndex;
    }

    /**
     * @return the number of items currently in this ChunkedVector.
     */
    public synchronized int getNbrElementsPresent() {
        return m_fillEndIndex + 1;
    }



    /**
     * Returns an array with the elements added to ChunkedVector. 
     * 
     * You may modify the returned array, but not the elements in it. You must
     * call pushFillEnd() at least once before calling this method.
     * 
     * @param aBegin - index of first element. 0 <= aBegin <=
     * getFillEndIndex() <= aEnd.
     * @param aEnd - index of last element to return. See aBegin for
     * constraints.
     * @return Object[] with references to the contained elements.
     * @throws IllegalArgumentException - if the conditions stated for aBegin
     * are not satisfied or getFillEndIndex() < 0. 
     */
    public synchronized Object[] getElements(int aBegin, int aEnd) {
        if (aBegin < 0 || aEnd < 0 || aBegin > aEnd || aEnd > m_fillEndIndex) {
            throw new IllegalArgumentException("ChunkedVector.getElements(): "
                                               + aBegin + ", " + aEnd);
        }

        int nbrElements = 1 + aEnd - aBegin;
        Object[] tmp = new Object[nbrElements];
        System.arraycopy(m_vector, aBegin, tmp, 0, nbrElements);
        return tmp;
    }


    /**
     * @return a single element
     * @param aIndex - index of element 0 <= aIndex <= getFillEndIndex()
     */
    public synchronized Object getElementAt(int aIndex) {
        if (aIndex < 0 || aIndex > m_fillEndIndex) {
            throw new IllegalArgumentException("ChunkedVector.getElementAt(): "
                                               + aIndex);
        }

        return m_vector[aIndex];
    }


    /**
     * Inserts a new element after the element at getFillEndIndex().
     * 
     * @param element - the new element to add.
     * @throws ArrayIndexOutOfBoundsException if the vector is alreay full. 
     */
    public synchronized void pushFillEnd(Object element) {
        if(LOG.isTrace()) {
            LOG.trace("ChunkedVector.pushFillEnd()",
                      element.toString() + " @position "
                      + (m_fillEndIndex + 1));
        }
        
        m_vector[m_fillEndIndex + 1] = element; // will throw AIOOBE if past end
        // still ok
        m_fillEndIndex++;
    }


    /**
     * for debugging use
     * 
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("ChunkedVector (capacity=");
        sb.append(getCapacity());
        sb.append(" iFillEndIndex=");
        sb.append(m_fillEndIndex);
        sb.append(" {\n");

        for(int i=0; i < m_vector.length; i++) {
            if (m_vector[i] == null) {
                sb.append("null, ");
            } else {
                sb.append(i); sb.append(": ");
                sb.append(m_vector[i].toString());
                sb.append('\n');
            }

        }
        sb.append("}");

        return sb.toString();
    }
}
