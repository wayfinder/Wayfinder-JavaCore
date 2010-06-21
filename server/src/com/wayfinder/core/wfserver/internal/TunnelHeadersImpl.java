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
package com.wayfinder.core.wfserver.internal;

import java.util.Vector;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.wfserver.tunnel.TunnelHeaders;


/**
 * Represents a collection of HTTP headers
 * 
 * 
 */
final class TunnelHeadersImpl implements TunnelHeaders {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TunnelHeadersImpl.class);
    
    private final Vector m_headerKeys;
    private final Vector m_headerValues;
    
    
    /**
     * Standard constructor.
     * <p>
     * After creation, this object will not contain any headers at all
     */
    TunnelHeadersImpl() {
        m_headerKeys = new Vector();
        m_headerValues = new Vector();
    }
    
    
    /**
     * Adds a header with value.
     * 
     * @param aKey The header key
     * @param aValue The header value
     */
    synchronized void addHeader(String aKey, String aValue) {
        m_headerKeys.addElement(aKey);
        m_headerValues.addElement(aValue);
    }
    
    
    /**
     * Attempts to update a previously set header with a new value. If the
     * header is not already set, it will be added.
     * 
     * @param aKey The header key
     * @param aValue The header value
     */
    synchronized void updateHeader(String aKey, String aValue) {
        final int index = findIndexOfKey(aKey);
        if(index >= 0) {
            m_headerValues.setElementAt(aValue, index);
        } else {
            addHeader(aKey, aValue);
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelHeaders#getHeaderValue(java.lang.String)
     */
    public synchronized String getHeaderValue(String aKey) {
        final int index = findIndexOfKey(aKey);
        if(index >= 0) {
            return (String) m_headerValues.elementAt(index);
        }
        return null;
    }
    
    
    private int findIndexOfKey(String aKey) {
        final int size = m_headerKeys.size();
        for (int i = 0; i < size; i++) {
            String str = (String) m_headerKeys.elementAt(i);
            if(str.equalsIgnoreCase(aKey)) {
                return i;
            }
        }
        return -1;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelHeaders#getKeyAt(int)
     */
    public synchronized String getKeyAt(int index) {
        if(index < size()) {
            return (String)m_headerKeys.elementAt(index);
        }
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelHeaders#getValueAt(int)
     */
    public synchronized String getValueAt(int index) {
        if(index < size()) {
            return (String)m_headerValues.elementAt(index);
        }
        return null;
    }

    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelHeaders#size()
     */
    public synchronized int size() {
        return m_headerKeys.size();
    }
    
    
    /**
     * Prints all the headers to standard out.
     */
    synchronized void printHeaders() {
        if(LOG.isDebug()) {
            for (int i = 0; i < size(); i++) {
                LOG.debug("TunnelHeadersImpl.printHeaders()", 
                        getKeyAt(i) + ": " + getValueAt(i));
            }
        }
    }
}

