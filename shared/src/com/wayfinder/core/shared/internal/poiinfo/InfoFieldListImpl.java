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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.shared.internal.poiinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.poiinfo.InfoField;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.util.CharArray;

/**
 * 
 */
public final class InfoFieldListImpl
    implements InfoFieldList, Serializable {

    public static final InfoFieldListImpl EMPTY_INFOFIELDLIST =
        new InfoFieldListImpl(new InfoFieldImpl[0]);
    
    protected InfoFieldImpl[] m_fieldArray;

    /**
     * @param infoFieldArray
     */
    public InfoFieldListImpl(InfoFieldImpl[] infoFieldArray) {
        m_fieldArray = infoFieldArray;
    }

    private InfoFieldListImpl() { }
    
    public String toString() {
        CharArray ca = new CharArray(100);
        ca.append("InfoFieldList[");
        if (m_fieldArray != null) {
            for (int i = 0; i < m_fieldArray.length; i++) {
                if (i != 0) {
                    ca.append(", ");    
                }
                ca.append(m_fieldArray[i].toString());
            }
        }
        ca.append("]");
        return ca.toString();
    }

    public void read(DataInputStream din) throws IOException {
        int n = din.readInt();
        if (n > 0) {
            read(n, din);
        } else {
            m_fieldArray = null;
        }
        
    }

    private void read(int size, DataInputStream din) throws IOException {
        m_fieldArray = new InfoFieldImpl[size];
        for (int i=0; i<m_fieldArray.length; i++) {
            m_fieldArray[i] = InfoFieldImpl.restore(din);
        }
    }

    public void write(DataOutputStream dout) throws IOException {
        if (m_fieldArray == null) {
            dout.writeInt(0);
        } else {
            dout.writeInt(m_fieldArray.length);
            for (int i=0; i<m_fieldArray.length; i++) {
                m_fieldArray[i].write(dout);
            }
        }
    }

    public InfoField getInfoFieldAt(int index) {
        return m_fieldArray[index];
    }

    /**
     * For Core internal use. Callers can use this to not have to cast and
     * assume that it works.
     * 
     * @param index
     * @return
     */
    public InfoFieldImpl getInfoFieldImplAt(int index) {
        return m_fieldArray[index];
    }

    public int getNbrInfoFields() {
        return (m_fieldArray != null) ? m_fieldArray.length : 0;
    }

    static public InfoFieldListImpl restore(DataInputStream din) throws IOException {
        int n = din.readInt();
        if (n > 0) {
            InfoFieldListImpl rez = new InfoFieldListImpl();
            rez.read(n, din);
            return rez;
        } else {
            return EMPTY_INFOFIELDLIST;
        }
    }

}
