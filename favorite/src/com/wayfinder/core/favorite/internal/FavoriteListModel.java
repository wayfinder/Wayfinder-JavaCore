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
package com.wayfinder.core.favorite.internal;


import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.FavoriteNameComparator;
import com.wayfinder.core.favorite.ListDataListener;
import com.wayfinder.core.favorite.ListModel;
import com.wayfinder.core.shared.util.ArrayList;
import com.wayfinder.core.shared.util.Comparator;
import com.wayfinder.core.shared.util.ListenerList;

/**
 * modification should be done only in UI/Main Thread
 * 
 */
public class FavoriteListModel implements ListModel {

    Comparator comparator = new FavoriteNameComparator();
    
    ArrayList elementArray = new ArrayList();
    
    ListenerList listenerList = new ListenerList();
    
//--- ListModel    
    public void addListDataListener(ListDataListener listener) {
        listenerList.add(listener);
    }
    
    public void removeListDataListener(ListDataListener listener) {
        listenerList.remove(listener);
    }
    
    public Object getElementAt(int index) {
        return elementArray.get(index);
    }

    public int getSize() {
        return elementArray.size();
    }
    
//--- Modification
    public void addFav(Favorite fav) {
        int index = this.addFavSilently(fav);
        
        if (!listenerList.isEmpty()) {
            Object[] listeners = listenerList.getListenerInternalArray();
            for(int i = 0; i < listeners.length; i++) {
                ((ListDataListener)listeners[i]).intervalAdded(index, index);
            }
        }
    }

    public void removeFav(Favorite fav) {
        int index = elementArray.indexOf(fav);
        if (index < 0) {
            return;
        } else {
            elementArray.remove(index);
        }
        
        if (!listenerList.isEmpty()) {
            Object[] listeners = listenerList.getListenerInternalArray();
            for(int i = 0; i < listeners.length; i++) {
                ((ListDataListener)listeners[i]).intervalRemoved(index, index);
            }
        }
    }

    public void replaceFav(Favorite oldFav, Favorite newFav) {
        //TODO sort it;
        int oldIndex = elementArray.indexOf(oldFav);
        int newIndex;
        if (oldIndex < 0) {
            return;
        } else {
            if (comparator.compare(oldFav, newFav) == 0) {
                //easy just replace it
                newIndex = oldIndex;
                elementArray.set(oldIndex, newFav);
            } else {
                elementArray.remove(oldIndex);
                newIndex = addFavSilently(newFav);
            }
        }
        
        if (!listenerList.isEmpty()) {
            int startIndex, endIndex;
            if (oldIndex <= newIndex) {
                startIndex = oldIndex;
                endIndex = newIndex;
            } else {
                startIndex = newIndex;
                endIndex = oldIndex;
            }
            Object[] listeners = listenerList.getListenerInternalArray();
            for(int i = 0; i < listeners.length; i++) {
                ((ListDataListener)listeners[i]).contentsChanged(startIndex, endIndex);
            }
        }        
    }

    public int removeFavSilently(Favorite fav) {
        int index = elementArray.indexOf(fav);
        if (index >= 0) {
            elementArray.remove(index);
        }
        return index;
    }
    
    public int addFavSilently(Favorite fav) {
        int start = 0;
        int end = elementArray.size() - 1;
        if (end < 0) {
            elementArray.add(fav);
            return 0;
        }
        while(start < end) {
            int index = (start + end)/2;
            if (comparator.compare(elementArray.get(index), fav) < 0) {
                start = index + 1; 
            } else { 
                end = index - 1;
            }
        }
        if (comparator.compare(elementArray.get(start), fav) <= 0) {
            start++; 
        }
        elementArray.add(start, fav);
        return start;
    }

    public void notifyChanges(int minIndex, int maxIndex) {
        if (!listenerList.isEmpty()) {
            Object[] listeners = listenerList.getListenerInternalArray();
            for(int i = 0; i < listeners.length; i++) {
                ((ListDataListener)listeners[i]).contentsChanged(minIndex, maxIndex);
            }
        }  
    }
    
    public void notifyFullChange() {
        if (!listenerList.isEmpty()) {
            int maxIndex = elementArray.size() - 1;
            Object[] listeners = listenerList.getListenerInternalArray();
            for(int i = 0; i < listeners.length; i++) {
                ((ListDataListener)listeners[i]).contentsChanged(0, maxIndex);
            }
        }  
    }
    
    ArrayList getInternalArray() {
        return elementArray;
    }
}
