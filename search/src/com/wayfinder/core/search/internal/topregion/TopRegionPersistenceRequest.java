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
package com.wayfinder.core.search.internal.topregion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.internal.SearchConstants;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceRequest;
import com.wayfinder.pal.persistence.SettingsConnection;

final class TopRegionPersistenceRequest implements PersistenceRequest {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TopRegionPersistenceRequest.class);

    static final int VERSION = 2;
    private final String m_crc;
    private final int m_language;
    private final TopRegionHolder m_holder;
    private final TopRegionImpl[] m_topRegionArray;
    
    
    TopRegionPersistenceRequest(TopRegionHolder holder, String crc, int langID, TopRegionImpl[] regionArray) {
        m_holder = holder;
        m_crc = crc;
        m_language = langID;
        m_topRegionArray = regionArray;
    }
    
    
    TopRegionPersistenceRequest(TopRegionHolder holder) {
        this(holder, null, Integer.MIN_VALUE, null);
    }
    

    public void writePersistenceData(SettingsConnection sConnection)
    throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("TopRegionPersistenceRequest.writePersistenceData()", 
                      "Saving top regions");
        }
        DataOutputStream dos = sConnection.getOutputStream(SearchConstants.PERSISTENCE_TOP_REGIONS);
        try {
            dos.writeInt(VERSION);
            dos.writeUTF(m_crc);
            dos.writeInt(m_language);
            dos.writeInt(m_topRegionArray.length);
            for (int i = 0; i < m_topRegionArray.length; i++) {
                m_topRegionArray[i].write(dos);
            }
        } finally {
            dos.close();
        }
    }


    public void readPersistenceData(SettingsConnection sConnection)
    throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("TopRegionPersistenceRequest.readPersistenceData()", 
                      "Reading top regions");
        }
        
        DataInputStream dis = sConnection.getDataInputStream(SearchConstants.PERSISTENCE_TOP_REGIONS);
        try {
            TopRegionPersistenceWrapper trpw = readDataInternal(dis);
            if(trpw != null) {
                if(LOG.isDebug()) {
                    LOG.debug("TopRegionPersistenceRequest.readPersistenceData()", 
                              "List of top regions found on disk");
                }
                m_holder.updateTopRegionList(trpw.m_topRegions, trpw.m_crc, trpw.m_lang, false);
            } else {
                m_holder.noUpdateAvailable();
            }
        } finally {
            dis.close();
        }
    }
    
    TopRegionPersistenceWrapper readDataInternal(DataInputStream dis) throws IOException {
        int version = dis.readInt();
        if (version == VERSION) {
            final String crc = dis.readUTF();
            final int langID = dis.readInt();
            TopRegionImpl[] array = new TopRegionImpl[dis.readInt()];
            for (int i = 0; i < array.length; i++) {
                array[i] = new TopRegionImpl(dis);
            }
            
            return new TopRegionPersistenceWrapper(crc, langID, array);
        }
        else return null;
    }

    public void error(CoreError coreError) {
        if(m_topRegionArray == null) {
            if(LOG.isWarn()) {
                LOG.warn("TopRegionPersistenceRequest.error()", 
                         "No top region list found on disk");
            }
            // read request
            m_holder.noUpdateAvailable();
        }
    }

    static class TopRegionPersistenceWrapper {
        final String m_crc;
        final int m_lang;
        final TopRegionImpl[] m_topRegions;
        
        /**
         * @param crc
         * @param lang
         * @param topRegions
         */
        public TopRegionPersistenceWrapper(String crc, int lang,
                TopRegionImpl[] topRegions) {
            super();
            m_crc = crc;
            m_lang = lang;
            m_topRegions = topRegions;
        }
    }
}
