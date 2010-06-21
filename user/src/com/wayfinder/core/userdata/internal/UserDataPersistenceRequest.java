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
package com.wayfinder.core.userdata.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.internal.persistence.PersistenceRequest;
import com.wayfinder.pal.persistence.SettingsConnection;

final class UserDataPersistenceRequest implements PersistenceRequest {
    
    static final int RECORD_ID = 0;
    static final int VERSION = 1;
    private final UserImpl m_user;
    private final UserDataStore m_store;
    
    UserDataPersistenceRequest(UserDataStore store) {
        this(store, null);
    }
    
    
    UserDataPersistenceRequest(UserDataStore store, UserImpl user) {
        m_store = store;
        m_user = user;
    }
    
    
    public void writePersistenceData(SettingsConnection sConnection)
    throws IOException {
        
        DataOutputStream dos = sConnection.getOutputStream(RECORD_ID);
        try {
            dos.writeInt(VERSION);
            dos.writeUTF(m_user.getUIN());
        } finally {
            dos.close();
        }
    }


    public void readPersistenceData(SettingsConnection sConnection)
    throws IOException {
        
        DataInputStream dis = sConnection.getDataInputStream(RECORD_ID);
        String uin = null;
        try {
            uin = readDataInternal(dis);
        } finally {
            dis.close();
        }
        
        if(ParameterValidator.isEmptyString(uin)) {
            m_store.noUINonDisk();
        } else {
            m_store.setUINfromDisk(uin);
        }
    }
    
    String readDataInternal(DataInputStream dis) throws IOException {
        if (dis.readInt() == VERSION) {
            String uin = dis.readUTF();
            return uin;
        }
        else return null;
    }

    public void error(CoreError coreError) {
        m_store.noUINonDisk();
    }
    
}
