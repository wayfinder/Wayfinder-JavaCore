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

import java.util.NoSuchElementException;

/**
 * Implements a double linked list.
 * 
 * Thread-safety: Partly thread safe. Thread-safety needs to be investigated
 * further. At least the constructor is not thread safe.
 */
public class LinkedList {
    private Entry m_header;
    private int m_size;

    /**
     * Constructs an empty list.
     */
    public LinkedList() {
        m_header = new Entry(null, null, null);
        m_header.m_next = m_header.m_previous = m_header;
    }


    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list or null if the list is empty.
     */
    public synchronized Object getFirst() {
        if (m_size == 0) {
            return null;
        }
        return m_header.m_next.m_element;
    }


    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list or null if the list is empty.
     */
    public synchronized Object getLast()  {
        if (m_size == 0) {
            return null;
        }
        return m_header.m_previous.m_element;
    }


    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list or null if the list is empty.
     */
    public synchronized Object removeFirst() {
        if (m_size == 0) {
            return null;
        }
        Object first = m_header.m_next.m_element;
        remove(m_header.m_next);
        return first;
    }


    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list or null if the list is empty.
     */
    public synchronized Object removeLast() {
        if (m_size == 0) {
            return null;
        }
        Object last = m_header.m_previous.m_element;
        remove(m_header.m_previous);
        return last;
    }


    /**
     * Inserts the given element at the beginning of this list.
     * 
     * @param o the element to be inserted at the beginning of this list.
     */
    public synchronized void addFirst(Object o) {
        addBefore(o, m_header.m_next);
    }


    /**
     * Appends the given element to the end of this list.  (Identical in
     * function to the <tt>add</tt> method; included only for consistency.)
     * 
     * @param o the element to be inserted at the end of this list.
     */
    public synchronized void addLast(Object o) {
        addBefore(o, m_header);
    }


    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that <tt>(o==null ? e==null
     * : o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    public synchronized boolean contains(Object o) {
        return indexOf(o) != -1;
    }


    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    public synchronized int size() {
        return m_size;
    }


    /**
     * Tests if the list has no elements.
     * 
     * @return true if and only if this list has no elements, false otherwise.
     */
    public synchronized boolean isEmpty() {
        return (m_size == 0);
    }


    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of
     * <tt>Collection.add</tt>).
     */
    public synchronized boolean add(Object o) {
        addBefore(o, m_header);
        return true;
    }


    /**
     * Removes the first occurrence of the specified element in this list.  If
     * the list does not contain the element, it is unchanged.  More formally,
     * removes the element with the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an
     * element exists).
     *
     * @param o element to be removed from this list, if present.
     * @return <tt>true</tt> if the list contained the specified element.
     */
    public synchronized boolean remove(Object o) {
        if (o == null) {
            for (Entry e = m_header.m_next; e != m_header; e = e.m_next) {
                if (e.m_element == null) {
                    remove(e);
                    return true;
                }
            }
        } else {
            for (Entry e = m_header.m_next; e != m_header; e = e.m_next) {
                if (o.equals(e.m_element)) {
                    remove(e);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Removes all of the elements from this list.
     */
    public synchronized void clear() {
        m_header.m_next = m_header.m_previous = m_header;
        m_size = 0;
    }


    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException if the specified index is is out of
     * range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public synchronized Object get(int index) {
        return entry(index).m_element;
    }


    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the specified index is out of
     *        range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public synchronized Object set(int index, Object element) {
        Entry e = entry(index);
        Object oldVal = e.m_element;
        e.m_element = element;
        return oldVal;
    }


    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *        range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    public synchronized void add(int index, Object element) {
        addBefore(element, (index==m_size ? m_header : entry(index)));
    }


    /**
     * Removes the element at the specified position in this list.  Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *        range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public synchronized Object remove(int index) {
        Entry e = entry(index);
        remove(e);
        return e.m_element;
    }


    /**
     * Return the indexed entry.
     */
    private Entry entry(int index) {
        if (index < 0 || index >= m_size)
            throw new IndexOutOfBoundsException("Index: " + index
                                                + ", Size: " + m_size);
        Entry e = m_header;
        if (index < (m_size >> 1)) {
            for (int i = 0; i <= index; i++)
                e = e.m_next;
        } else {
            for (int i = m_size; i > index; i--)
                e = e.m_previous;
        }
        return e;
    }


    // Search Operations

    /**
     * Returns the index in this list of the first occurrence of the
     * specified element, or -1 if the List does not contain this
     * element.  More formally, returns the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if
     * there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the
     *         specified element, or -1 if the list does not contain this
     *         element.
     */
    public synchronized int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Entry e = m_header.m_next; e != m_header; e = e.m_next) {
                if (e.m_element == null)
                    return index;
                index++;
            }
        } else {
            for (Entry e = m_header.m_next; e != m_header; e = e.m_next) {
                if (o.equals(e.m_element))
                    return index;
                index++;
            }
        }
        return -1;
    }


    /**
     * Returns the index in this list of the last occurrence of the
     * specified element, or -1 if the list does not contain this
     * element.  More formally, returns the highest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if
     * there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the last occurrence of the
     *         specified element, or -1 if the list does not contain this
     *         element.
     */
    public synchronized int lastIndexOf(Object o) {
        int index = m_size;
        if (o == null) {
            for (Entry e = m_header.m_previous; e != m_header; e = e.m_previous) {
                index--;
                if (e.m_element == null)
                    return index;
            }
        } else {
            for (Entry e = m_header.m_previous; e != m_header; e = e.m_previous) {
                index--;
                if (o.equals(e.m_element))
                    return index;
            }
        }
        return -1;
    }

    
    /**
     * Struct to represent an entry in the list
     */
    private static class Entry {
        // FIXME: make m_element final. Need to rework set() for that.
        Object m_element;
        Entry m_next;
        Entry m_previous;

        Entry(Object element, Entry next, Entry previous) {
            m_element = element;
            m_next = next;
            m_previous = previous;
        }
    }

    private Entry addBefore(Object o, Entry e) {
        Entry newEntry = new Entry(o, e, e.m_previous);
        newEntry.m_previous.m_next = newEntry;
        newEntry.m_next.m_previous = newEntry;
        m_size++;
        return newEntry;
    }

    private void remove(Entry e) {
        if (e == m_header)
            throw new NoSuchElementException();

        e.m_previous.m_next = e.m_next;
        e.m_next.m_previous = e.m_previous;
        m_size--;
    }


    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     *         in the correct order. The caller may modify the array freely.
     */
    public synchronized Object[] toArray() {
        Object[] result = new Object[m_size];
        toArray(result);
        return result;
    }
    
    
    /**
     * Populates the supplied array with the objects in the list.
     * 
     * @param objArr The array to populate with the objects
     */
    public synchronized void toArray(Object[] objArr) {
        if(objArr.length < m_size) {
            throw new IllegalArgumentException("Supplied array is too small");
        }
        int i = 0;
        for (Entry e = m_header.m_next; e != m_header; e = e.m_next) {
            objArr[i++] = e.m_element;
        }
    }
    
}
