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
package com.wayfinder.core.userdata.internal.hwkeys;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.wayfinder.core.shared.util.CharArray;

public class HardwareKeyContainer {
    
    private final HardwareKey[] m_hardwareKeys;
    
    HardwareKeyContainer(HardwareKey[] aKeys) {
        m_hardwareKeys = aKeys;
    }
    
    
    /**
     * Returns an enumeration of all hardware keys available on the platform.
     * <p>
     * This method is guaranteed to never return null but there are no 
     * guarantees on how many keys or of what type they are.
     * 
     * @return an enumeration of HardwareKey objects
     * @see com.wayfinder.hardwarekeys.HardwareKey
     */
    public Enumeration getKeyEnumeration() {
        return new HardwareKeyEnumeration(m_hardwareKeys);
    }
    

    /**
     * Internal class to protect the Hardware keys from external manipulation.
     * Since the HardwareKey objects themselves are immutable, we only have to
     * protect the array from having it's elements changed or nulled out
     */
    private static class HardwareKeyEnumeration implements Enumeration {
        
        private int m_currentKeyNbr;
        private HardwareKey[] m_keys;
        
        private HardwareKeyEnumeration(HardwareKey[] aKeys) {
            m_keys = aKeys;
        }
        
        public boolean hasMoreElements() {
            return m_currentKeyNbr < m_keys.length;
        }
        
        public Object nextElement() {
            if(m_currentKeyNbr < m_keys.length) {
                return m_keys[m_currentKeyNbr++];
            }
            throw new NoSuchElementException();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        CharArray ca = new CharArray(10);
        ca.append("HardwareKeyContainer[");
        for(int i=0; i<m_hardwareKeys.length; i++){
            if (i>0) {
                ca.append(", ");   
            }
            ca.append(m_hardwareKeys[i].getKeyXMLType());
            ca.append(':');
            ca.append(m_hardwareKeys[i].getKey());
        }
        ca.append("]");
        return ca.toString();
    }
}
