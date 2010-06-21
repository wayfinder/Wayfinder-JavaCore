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
 * A class that holds a list of listeners. In order to not have type restriction
 * the type class for the listeners is {@link Object}.
 * 
 * The list dosen't allow duplicate (according to {@link Object#equals(Object)})
 * or null elements.
 * 
 * The main benefits that this class provides are that it is relatively
 * cheap in the case of no listeners, and it provides serialization for 
 * event-listener lists in a single place, as well as a degree of MT safety
 * (when used correctly).
 * 
 * All methods that modify the list are synchronized, the internal array is 
 * never modified it will be replaced when the list is modified. 
 * 
 * 
 * 
 */
public class ListenerList {
    
    /* A null array to be shared by all empty listener lists*/
    private final static Object[] NULL_ARRAY = new Object[0];
    
    /* The list of Listener, volatile to avoid thread caching*/
    protected volatile Object[] listenerArray = NULL_ARRAY;
    
    /**
     * Passes back the event listener list as an array
     * of Listener.  Note that for performance reasons, 
     * this implementation passes back the actual data 
     * structure in which the listener data is stored 
     * internally!  
     * This method is guaranteed to pass back a non-null
     * array, so that no null-checking is required in 
     * fire methods.  A zero-length array of Object should
     * be returned if there are currently no listeners.
     * 
     * <p>
     * <bold>WARNING<bold> Absolutely NO modification of
     * the data contained in this array should be made -- if
     * any such manipulation is necessary, it should be done
     * on a copy of the array returned rather than the array 
     * itself.
     * </p>
     */
    public Object[] getListenerInternalArray() {
        return listenerArray;
    }
    
    /**
     * @return true if there are no listener in the list false otherwise
     */
    public boolean isEmpty() {
        return (listenerArray == NULL_ARRAY);
    }
    
    /**
     * Adds the listener to the list, if wasn't already added. 
     * If there is already an equal listener but is different instance, 
     * the existing one will be replaced and the method will return true.
     * 
     * @param listener the listener to be added
     * 
     * @return true if was added, false otherwise
     */
    public synchronized boolean add(Object listener) {
        if (listener==null) {
            //TODO add a log message to help developers know they are 
            //probably doing something wrong
            return false;
        }
        if (listenerArray == NULL_ARRAY) {
            // if this is the first listener added, 
            // initialize the lists
            listenerArray = new Object[] {listener};
        } else {
            //check to not add twice same listener 
            for (int i = listenerArray.length - 1; i>=0; i--) {
                if (listenerArray[i].equals(listener)) {
                    if (listenerArray[i] == listener) {
                        //was already added 
                        return false;
                    } else {
                        //was added but is a different instance
                        //it will be replaced
                        //clone the array in order to avoid concurrency issues 
                        Object[] tmp = new Object[listenerArray.length];
                        System.arraycopy(listenerArray, 0, tmp, 0, listenerArray.length);
                        tmp[i] = listener;
                        listenerArray = tmp;
                        return true;
                    }
                    
                }
            }
            // Otherwise copy the array and add the new listener
            int i = listenerArray.length;
            Object[] tmp = new Object[i+1];
            System.arraycopy(listenerArray, 0, tmp, 0, i);
            tmp[i] = listener;
            listenerArray = tmp;
        }
        return true;
    }

    /**
     * Removes the listener from the list
     * @param listener the listener to be removed
     * 
     * @return true if the listener was found and removed, false otherwise
     */
    public synchronized boolean remove(Object listener) {
        if (listener == null) {
            //TODO add a log message to help developers know they are 
            //probably doing something wrong
            return false;
        }
        
        //most of the time there will a single listener 
        //check first for that to speed up   
        if (listenerArray.length == 1) {
            if (listenerArray[0].equals(listener)) {
                listenerArray = NULL_ARRAY;
            }
            return true;
        }
        
        // Is listener on the list?
        int index = -1;
        for (int i = listenerArray.length; i!=0;) {
            if (listenerArray[--i].equals(listener)) {
                index = i;
                break;
            }
        }
        // If so,  remove it
        if (index != -1) {
            Object[] tmp = new Object[listenerArray.length-1];
            // Copy the list up to index
            System.arraycopy(listenerArray, 0, tmp, 0, index);
            // Copy from past the index, up to
            // the end of tmp (which is one element
            // shorter than the old list)
            if (index < tmp.length) {
                System.arraycopy(listenerArray, index+1, tmp, index, 
                        tmp.length - index);
            }
            // set the listener array
            listenerArray = tmp;
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * @return a string representation of the ListenerList.
     */
    public String toString() {
        Object[] lList = listenerArray;
        StringBuffer sb = new StringBuffer(100 + 100*lList.length); 
        sb.append("ListenerList: ");
        sb.append(listenerArray.length).append(" listeners: [");
        for (int i = 0 ; i < lList.length ; i++) {
            sb.append(lList[i]).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
