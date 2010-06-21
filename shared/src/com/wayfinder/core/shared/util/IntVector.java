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
 * Implements a growable array of ints. There is no requirement or enforcement
 * on the int elements to be unique.
 *  
 * The size will grow as elements are added but it will never shrink.
 *
 * Thread-safety: Notice that even if this is called Vector the operations are
 * not synchronized. Use client-side locking. Rationale: client-side locking
 * is needed anyway since you need to lock a sequence of "add element if it
 * does not already exists".   
 */
public class IntVector {

    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */
    
    /**
     * storage for the elements. Never null.
     */
    private int[] m_array;
    private int m_nextFreeIndex;
    
    // TODO: make it possible for users to tweak this value.
    /**
     * when expanding the array, this much extra space will be added for future
     * growing. E.g. if array is full and ensureCapacity(1) is called, the new
     * array will have size (size() + 1 + m_capacityIncrement). 
     */
    private static final int CAPACITY_INCREMENT = 5;

    
    /**
     * Constructs a IntVector with a specified initial capacity.    
     * 
     * @param capacity - initial capacity. capacity >= 1
     */
    public IntVector(int capacity) {
        m_array = new int[capacity];
    }

    /**
     * Add an element to the vector.
     * 
     * @param i - the element to add.
     */
    public void add(int i) {
        ensureCapacity(1);
        
        m_array[m_nextFreeIndex++] = i;
    }


    /**
     * Adds several elements to the vector at once.
     * 
     * @param i array of elements to add. i != null but an array with size 0 is
     * ok.
     */
    public void add(int[] i) {
        this.ensureCapacity(i.length);
        
        System.arraycopy(i, 0, m_array, m_nextFreeIndex, i.length);
        m_nextFreeIndex += i.length;
    }


    /**
     * Optimized method that append all ints of specified IntVector at the end
     * of this IntVector.
     * 
     * The behavior of this operation is undefined if the specified IntVector 
     * is modified while the operation is in progress.
     * 
     * @param vector ints to be appended
     * @return true if this IntVector changed as a result of the call 
     * (intV is not empty).
     * 
     * @throws NullPointerException - if the specified IntVector is null
     */
    public boolean add(IntVector vector) {
        int size = vector.size(); // throws NPE as specified
        if (size == 0) {
            return false;
        }
        
        ensureCapacity(size);
        System.arraycopy(vector.m_array, 0, m_array, m_nextFreeIndex, size);
        m_nextFreeIndex += size;
        return true;
    }

    /**
     * remove the int at the specified index 
     * @param index
     * @return the int value removed
     */
    public int remove(int index) {
        if (index < 0 || index >= m_nextFreeIndex) {
            throw new ArrayIndexOutOfBoundsException("Index not found");
        }
        m_nextFreeIndex--;
        int elem = m_array[index];
        //check if was not the last one
        if (index < m_nextFreeIndex){
            //copy the over it starting to index
            System.arraycopy(m_array, index + 1, 
                    m_array, index, m_nextFreeIndex - index);
        }
        return elem;
    }
    
    /**
     * remove the last element added similar with calling 
     * <code>intVector.remove(intVector.size() - 1)</code> 
     *
     * @return the int value removed
     */
    public int removeLast() {
        if (m_nextFreeIndex <= 0) {
            throw new ArrayIndexOutOfBoundsException("Cannot remove from and empty IntVector");
        }
        m_nextFreeIndex--;
        return m_array[m_nextFreeIndex];
    }

    /**
     * Gets the element at specified index.
     * 
     * @param index - index of element to get.
     * @throws IndexOutOfBoundsException if index < 0 or no element is stored
     * at index.
     * 
     * @return the sought element.
     */
    public int get(int index) {
        if(index < 0 || index >= m_nextFreeIndex) {
            String s = "IntVector.get(" + index
                       + ") index not within bounds [0 - "
                       + (m_nextFreeIndex - 1)
                       + "]"; 
            throw new IndexOutOfBoundsException(s);
        }
        
        return m_array[index];
    }


    /**
     * Gets a range of elements. 
     * 
     * @param index - index of first element
     * @param numberOfElements - number of elements to return. 
     * @return a new array with copies of the elements.
     * The caller is allowed to modify the returned array.
     * @throws IndexOutOfBoundsException if there are not elements stored in
     * [index, index + numberOfElements - 1].
     * 
     * FIXME: IOOB is only thrown if index is past m_nextFreeIndex. If the
     * requested end index (index + numberOfElements) is too large a different
     * behaviour exists. Unclear what users expect. 
     */
    public int[] get(int index, int numberOfElements) {
        if(index < 0 || index >= m_nextFreeIndex) {
            String s = "IntVector.get(" + index + ", " + numberOfElements
                       + ") indexStart not within bounds [0 - "
                       + (m_nextFreeIndex - 1)
                       + "]";
            throw new IndexOutOfBoundsException(s);
        }

        if(index + numberOfElements > m_nextFreeIndex) {
            numberOfElements = m_nextFreeIndex - index;
        }
        
        int[] retArray = new int[numberOfElements];
        System.arraycopy(m_array, index, retArray, 0, retArray.length);

        return retArray;
    }


    /**
     * Returns the number of elements currently stored.
     * 
     * @return the number of elements currently stored.
     */
    public int size() {
        return m_nextFreeIndex;
    }
    /**
     * Makes sure that the array can hold at least capacity more elements more
     * than current size() (not initial capacity).
     * 
     * If needed the array is internally re-allocated so that room is
     * available.
     * 
     * @param capacity
     */
    public void ensureCapacity(int capacity) {
        int newArrayLength = m_nextFreeIndex + capacity;
        if(newArrayLength > m_array.length) {
            int[] newArray = new int[newArrayLength + CAPACITY_INCREMENT];
            System.arraycopy(m_array, 0, newArray, 0, m_nextFreeIndex);
            m_array = newArray;    
        }
    }


    /**
     * Check if a certain value is among the elements.
     * 
     * @param value the element to check for.
     * @return true if value is present at least once and false otherwise.
     */
    public boolean contains(int value) {
        for (int i = 0; i < m_nextFreeIndex; i++) {
            if(m_array[i] == value) {
                return true;
            }
        }

        return false;
    }


    /**
     * Removes all elements.
     */        
    public void clear() {
        m_nextFreeIndex = 0;
    }


    /**
     * Returns a reference to the elements. This is very dangerous and only to
     * be used in performance critical situations.
     * 
     * FINDBUGS: exclusion needed. This is a design choice.
     * 
     * @return an array of int.
     */
    public int[] getArray() {
        return m_array;
    }


    /**
     * for debugging use.
     * 
     *  @return a string representation of this IntVector and its elements.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(30 + 12 * m_nextFreeIndex);
        sb.append("IntVector {");
        for (int i = 0; i < m_nextFreeIndex; i++) {
            sb.append(m_array[i]);
            sb.append(", ");
        }
        sb.append('}');
        
        return sb.toString();
    }
}
