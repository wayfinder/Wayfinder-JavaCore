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
package com.wayfinder.core.search.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceRequest;
import com.wayfinder.pal.persistence.SettingsConnection;

class SearchDescriptorPersistenceRequest implements PersistenceRequest {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(SearchDescriptorPersistenceRequest.class);
    
    private final SearchDescriptor m_desc;
    private final SearchHolder m_holder;

    SearchDescriptorPersistenceRequest(SearchHolder holder, SearchDescriptor desc) {
        m_desc = desc;
        m_holder = holder;
    }
    
    
    SearchDescriptorPersistenceRequest(SearchHolder holder) {
        this(holder, null);
    }
    
    
    //-------------------------------------------------------------------------
    // write
    
    
    public void writePersistenceData(SettingsConnection sConnection)
    throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("SearchDescriptorPersistenceRequest.writePersistenceData()", 
                    "Saving search descriptor");
        }
        DataOutputStream dos = sConnection.getOutputStream(SearchConstants.PERSISTENCE_SEARCH_DESCRIPTOR);
        try {
            dos.writeInt(SearchDescriptor.DESC_CLIENT_VERSION);
            dos.writeUTF(m_desc.getCRC());
            dos.writeInt(m_desc.getLanguageID());

            Provider[] array = m_desc.getAllProvidersInternalArray();
            dos.writeInt(array.length);
            for (int i = 0; i < array.length; i++) {
                writeProvider(dos, array[i]);
            }
            dos.flush();
        } finally {
            dos.close();
        }
    }
    
    
    private static void writeProvider(DataOutputStream dos, Provider prov) 
    throws IOException {
        
        dos.writeInt(prov.getRound());
        dos.writeInt(prov.getHeadingID());
        dos.writeUTF(prov.getProviderName());
        dos.writeUTF(prov.getProviderType());
        dos.writeInt(prov.getTopRegionID());
        dos.writeUTF(prov.getProviderImageName());
    }
    
    
    //-------------------------------------------------------------------------
    // read
    

    public void readPersistenceData(SettingsConnection sConnection)
    throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("SearchDescriptorPersistenceRequest.readPersistenceData()", 
                    "Reading search descriptor");
        }

        DataInputStream dis = sConnection.getDataInputStream(SearchConstants.PERSISTENCE_SEARCH_DESCRIPTOR);
        try {
            SearchDescriptor sd = readDataInternal(dis);
            if(sd != null) {
                if(LOG.isDebug()) {
                    LOG.debug("SearchDescriptorPersistenceRequest.readPersistenceData()", 
                              "SearchDescriptor found on disk");
                }
                m_holder.setNewSearchDescriptor(sd, false);
            } else {
                m_holder.noDescriptorUpdateAvailable();
            }
        } finally {
            dis.close();
        }
    }
    
    private static Provider readProvider(DataInputStream dis) 
    throws IOException {
        
        return new Provider(
                dis.readInt(), 
                dis.readInt(),
                dis.readUTF(),
                dis.readUTF(), 
                dis.readInt(), 
                dis.readUTF());
    }
    
    SearchDescriptor readDataInternal(DataInputStream dis) throws IOException {
        if(dis.readInt() == SearchDescriptor.DESC_CLIENT_VERSION) {
            final String crc = dis.readUTF();
            final int langID = dis.readInt();

            Provider[] array = new Provider[dis.readInt()];
            for (int i = 0; i < array.length; i++) {
                array[i] = readProvider(dis);
            }
            return new SearchDescriptor(crc, array, langID);
        }
        else return null;
    }
    
    public void error(CoreError coreError) {
        if(m_desc == null) {
            // read request
            if(LOG.isWarn()) {
                LOG.warn("SearchDescriptorPersistenceRequest.error()", 
                         "No search descriptor found on disk");
            }
            
            m_holder.noDescriptorUpdateAvailable();
        }
    }
}
