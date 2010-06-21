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
 * This class implements a Fibonacci heap data structure. 
 * <p>
 * This data structure is optimized to favor quick inserts into the heap, while
 * the running time hit is taken in the method {@link #removeHighestPrio()}
 * which has a running time of O(log n) due to recalculation of the heap.
 * <p>
 * This implementation is slightly tweaked to ensure that objects with
 * equal order that are removed and reinserted on the heap are cycled, so that
 * the same objects are not always at the top.
 * <p>
 * The heap is capable of storing any type of objects. It will completely leave
 * the decisions of type and how the objects are compared in regards to
 * priority to the comparator passed to the constructor.
 * <p>
 * <b>Be aware that this implementation is not internally synchronized for
 * performance reasons.</b>
 */
public final class FibonacciHeap implements PriorityQueue {

    private final Comparator m_comparator;
    private Node m_highestPrioNode;
    private int m_nbrOfNodes;


    /**
     * Constructs a FibonacciHeap object that contains no elements.
     * 
     * @param comparator The {@link Comparator} used to decide the priority of 
     * the objects.
     * @throws IllegalArgumentException if comparator is null
     */
    public FibonacciHeap(Comparator comparator) {
        if(comparator == null) {
            // disallow creation of the heap without a comparator or the
            // methods will fail later. Pointless to try and create a default
            // comparator for the heap since it handles Object
            throw new IllegalArgumentException("Heap cannot be created without a comparator");
        }
        m_comparator = comparator;
    }


    //-------------------------------------------------------------------------
    // Insertion


    /**
     * Inserts a new data element into the heap.
     *
     */
    public int insert(Object obj) {
        assertComparableObject(obj);
        Node node = new Node(obj);
        if (m_highestPrioNode != null) {
            // we already have something in the heap. insert this object
            // in the tree
            node.m_leftNode = m_highestPrioNode;
            node.m_rightNode = m_highestPrioNode.m_rightNode;
            m_highestPrioNode.m_rightNode = node;
            node.m_rightNode.m_leftNode = node;
            // check to see if this is the new high priority object
            // here we only check if it's higher than the current
            if (m_comparator.compare(obj, m_highestPrioNode.m_element) < 0) {
                m_highestPrioNode = node;
            }
        } else {
            // heap is empty, just insert it
            m_highestPrioNode = node;
        }
        m_nbrOfNodes++;
        return m_nbrOfNodes;
    }


    private void assertComparableObject(Object obj) {
        if(!m_comparator.canBeCompared(obj)) {
            throw new IllegalArgumentException("Object can not be compared");
        }
    }


    //-------------------------------------------------------------------------
    // Grabbing highest priority


    /**
     * Returns the element in the heap with the highest priority as determined 
     * by the {@link Comparator}. The element will not be removed.
     * 
     * @return the element with the highest priority
     */
    public Object findHighestPrio() {
        if(isEmpty()) {
            return null;
        }
        return m_highestPrioNode.m_element;
    }


    /**
     * Removes and returns the element in the heap with the highest priority as 
     * determined by the {@link Comparator}.
     * <p>
     * This method may be the heaviest method in the class since it will
     * recalculate the heap.
     * 
     * @return the element with the highest priority or <code>null</code> if
     * no elements are in the heap
     */
    public Object removeHighestPrio() {
        if(!isEmpty()) {
            final Node prioNode = m_highestPrioNode;
            
            Node x = prioNode.m_childNode;
            for(int numKids = prioNode.m_nodeDegree; numKids > 0; numKids--) {
                Node tempRight = x.m_rightNode;

                x.m_leftNode.m_rightNode = x.m_rightNode;
                x.m_rightNode.m_leftNode = x.m_leftNode;
                x.m_leftNode = m_highestPrioNode;
                x.m_rightNode = m_highestPrioNode.m_rightNode;
                m_highestPrioNode.m_rightNode = x;
                x.m_rightNode.m_leftNode = x;

                x = tempRight;
            }

            // remove the priority node from the tree
            prioNode.m_leftNode.m_rightNode = prioNode.m_rightNode;
            prioNode.m_rightNode.m_leftNode = prioNode.m_leftNode;

            if (prioNode == prioNode.m_rightNode) {
                m_highestPrioNode = null;
            } else {
                m_highestPrioNode = prioNode.m_rightNode;
                unifyTrees();
            }

            m_nbrOfNodes--;
            return prioNode.m_element;
        }
        return null;
    }


    private void unifyTrees() {
        Node tmpNode = m_highestPrioNode;
        int numRoots;
        if(tmpNode != null) {
            numRoots = 1;
            for(tmpNode = tmpNode.m_rightNode; tmpNode != m_highestPrioNode; tmpNode = tmpNode.m_rightNode) {
                numRoots++;
            }
        } else {
            numRoots = 0;
        }
        
        Node[] array = new Node[m_nbrOfNodes + 1];
        for(; numRoots > 0; numRoots--) {
            final Node next = tmpNode.m_rightNode;
            int d = tmpNode.m_nodeDegree;
            for(Node y = array[d]; y != null; y = array[++d]) {
                // Here we compare not only "if lesser than" but also 
                // "or is equal to" to ensure that
                // things that are reentered into the queue are rotated, giving
                // everyone a fair chance to get CPU time
                if (m_comparator.compare(tmpNode.m_element, y.m_element) >= 0) {
                    Node temp = y;
                    y = tmpNode;
                    tmpNode = temp;
                }

                // Link y with thisNode and remove it from the root list
                y.m_rightNode.m_leftNode = y.m_leftNode;
                y.m_leftNode.m_rightNode = y.m_rightNode;
                
                if(tmpNode.m_childNode != null) {
                    y.m_leftNode = tmpNode.m_childNode;
                    y.m_rightNode = tmpNode.m_childNode.m_rightNode;
                    tmpNode.m_childNode.m_rightNode = y;
                    y.m_rightNode.m_leftNode = y;
                } else {
                    tmpNode.m_childNode = y;
                    y.m_rightNode = y;
                    y.m_leftNode = y;
                }

                tmpNode.m_nodeDegree++;

                // We've handled this degree, go to next one.
                array[d] = null;
            }
            array[d] = tmpNode;
            tmpNode = next;
        }

        // build a new root list
        m_highestPrioNode = null;

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                if (m_highestPrioNode != null) {
                    array[i].m_leftNode.m_rightNode = array[i].m_rightNode;
                    array[i].m_rightNode.m_leftNode = array[i].m_leftNode;

                    array[i].m_leftNode = m_highestPrioNode;
                    array[i].m_rightNode = m_highestPrioNode.m_rightNode;
                    m_highestPrioNode.m_rightNode = array[i];
                    array[i].m_rightNode.m_leftNode = array[i];

                    if (m_comparator.compare(array[i].m_element, m_highestPrioNode.m_element) < 0) {
                        m_highestPrioNode = array[i];
                    }
                } else {
                    m_highestPrioNode = array[i];
                }
            }
        }
    }


    //-------------------------------------------------------------------------
    // Other public methods



    /* (non-Javadoc)
     * @see com.wayfinder.util.PriorityQueue#size()
     */
    public int size() {
        return m_nbrOfNodes;
    }



    /* (non-Javadoc)
     * @see com.wayfinder.util.PriorityQueue#isEmpty()
     */
    public boolean isEmpty() {
        return m_highestPrioNode == null;
    }



    /* (non-Javadoc)
     * @see com.wayfinder.util.PriorityQueue#clear()
     */
    public void clear() {
        // release the prio node (and thus the tree) into the void
        m_highestPrioNode = null;
        m_nbrOfNodes = 0;
    }


    /**
     * Struct to hold the data for an individual node
     */
    private static class Node {

        /**
         * Default constructor.  Initializes the right and left pointers,
         * making this a circular doubly-linked list.
         *
         * @param key initial key for node
         */
        private Node(Object obj) {
            m_rightNode = this;
            m_leftNode = this;
            m_element = obj;
        }

        private final Object m_element;
        private Node m_childNode;
        private Node m_leftNode;
        private Node m_rightNode;
        private int  m_nodeDegree;
    }
}
