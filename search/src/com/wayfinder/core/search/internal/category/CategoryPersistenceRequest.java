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
package com.wayfinder.core.search.internal.category;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceRequest;
import com.wayfinder.pal.persistence.SettingsConnection;

class CategoryPersistenceRequest implements PersistenceRequest {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(CategoryPersistenceRequest.class);

    static final int VERSION = 3;
    
    private final String m_crc;
    private final Position m_position;
    private final CategoryHolder m_holder;
    private final CategoryImpl[] m_categoryArray;
    private final int m_store;
    private final int m_language;
    
    
    CategoryPersistenceRequest(int store, CategoryHolder holder, String crc, int language, Position position, CategoryImpl[] catArray) {
        m_store = store;
        m_holder = holder;
        m_crc = crc;
        m_language = language;
        m_position = position;
        m_categoryArray = catArray;
    }
    
    
    CategoryPersistenceRequest(int store, CategoryHolder holder) {
        this(store, holder, null, 0, null, null);
    }
    

    public void writePersistenceData(SettingsConnection sConnection)
    throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("CategoryPersistenceRequest.writePersistenceData()", 
                      "Saving categories");
        }
        DataOutputStream dos = sConnection.getOutputStream(m_store);
        try {
            dos.writeInt(VERSION);
            dos.writeUTF(m_crc);
            dos.writeInt(m_language);
            dos.writeInt(m_position.getMc2Latitude());
            dos.writeInt(m_position.getMc2Longitude());
            dos.writeInt(m_categoryArray.length);
            for (int i = 0; i < m_categoryArray.length; i++) {
                m_categoryArray[i].write(dos);
            }
        } finally {
            dos.close();
        }
    }


    public void readPersistenceData(SettingsConnection sConnection)
    throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("CategoryPersistenceRequest.readPersistenceData()", 
                      "Reading categories");
        }
        
        DataInputStream dis = sConnection.getDataInputStream(m_store);
        try {
            PersistentCategoryDataWrapper data = readDataInternal(dis);
            if (data != null) {
                m_holder.updateCategoryList(
                        data.m_crc, data.m_lang, data.m_pos, data.m_categories, false);
            } else {
                m_holder.noUpdateAvailable();
            }
        } finally {
            dis.close();
        }
    }
    
    PersistentCategoryDataWrapper readDataInternal(DataInputStream dis) throws IOException {
        int version = dis.readInt();
        if (version == VERSION) {
            final String crc = dis.readUTF();
            final int lang = dis.readInt();
            final int mc2lat = dis.readInt();
            final int mc2lon = dis.readInt();
            Position pos = new Position(mc2lat, mc2lon);
            CategoryImpl[] array = new CategoryImpl[dis.readInt()];
            for (int i = 0; i < array.length; i++) {
                array[i] = new CategoryImpl(dis);
            }
            
            return new PersistentCategoryDataWrapper(crc, lang, pos, array);
        }
        else return null;
    }

    public void error(CoreError coreError) {
        if(m_categoryArray == null) {
            // read request
            if(LOG.isWarn()) {
                LOG.warn("CategoryPersistenceRequest.error()", 
                         "No category list found on disk");
            }
            
            m_holder.noUpdateAvailable();
        }
    }

    static class PersistentCategoryDataWrapper {
        final String m_crc;
        final int m_lang;
        final Position m_pos;
        final CategoryImpl[] m_categories;
        /**
         * @param version
         * @param crc
         * @param lang
         * @param pos
         * @param categories
         */
        PersistentCategoryDataWrapper(String crc, int lang, Position pos,
                CategoryImpl[] categories) {
            m_crc = crc;
            m_lang = lang;
            m_pos = pos;
            m_categories = categories;
        }
    }
}
