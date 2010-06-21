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
package com.wayfinder.core.network.internal.mc2.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import com.wayfinder.core.network.internal.mc2.MC2StatusListener;

class MC2Storage {
    
    private final Hashtable m_attribTable;
    private final Hashtable m_statListenerTable;
    
    MC2Storage() {
        m_attribTable = new Hashtable();
        m_statListenerTable = new Hashtable();
    }
    
    
    synchronized void setMC2AuthAttribute(MC2IsabAttribute attrib) {
        if(attrib.getValue() == null) {
            m_attribTable.remove(attrib);
        } else {
            m_attribTable.put(attrib, attrib);
        }
    }
    
    
    synchronized Enumeration getMC2IsabAttributes() {
        return m_attribTable.elements();
    }
    
    
    synchronized void addMC2IsabStatusListener(MC2StatusListener listener) {
        m_statListenerTable.put(listener, listener);
    }
    
    
    synchronized void removeMC2IsabStatusListener(MC2StatusListener listener) {
        m_statListenerTable.remove(listener);
    }
    
    
    synchronized MC2StatusListener[] getMC2IsabStatusListeners() {
        // this is not efficient, but the server does not respond with global
        // status codes that often and I don't see that many listeners 
        // registered
        MC2StatusListener[] array = new MC2StatusListener[m_statListenerTable.size()];
        Enumeration e = m_statListenerTable.elements();
        for (int i = 0; i < array.length; i++) {
            array[i] = (MC2StatusListener) e.nextElement();
        }
        return array;
    }
    

}
