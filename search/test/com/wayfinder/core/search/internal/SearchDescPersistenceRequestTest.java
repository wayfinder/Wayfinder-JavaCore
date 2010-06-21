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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.search.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class SearchDescPersistenceRequestTest extends TestCase {
    
    private SearchDescriptor m_searchDesc;
    private SearchDescriptorPersistenceRequest m_req;
    private SettingsConnection m_settingsConn;

    /**
     * @param name
     */
    public SearchDescPersistenceRequestTest(String name) {
        super(name);
        Provider[] providers = new Provider[2];
        providers[0] = new Provider(0, 0, "Places", "WFSearch", Provider.ALL_REGIONS, "tat_places");
        providers[1] = new Provider(1, 1, "Eniro", "yellow pages", Provider.ALL_REGIONS, "eniro");
        m_searchDesc = new SearchDescriptor("CRC_OK", providers, Language.EN);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_req = new SearchDescriptorPersistenceRequest(null, m_searchDesc);
        m_settingsConn = MemoryPersistenceLayer.getPersistenceLayer().openSettingsConnection("search");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWriteDesc() {
        try {
            m_req.writePersistenceData(m_settingsConn);
            
            DataInputStream din = m_settingsConn.getDataInputStream(
                    SearchConstants.PERSISTENCE_SEARCH_DESCRIPTOR);
            
            int version = din.readInt();
            assertEquals(SearchDescriptor.DESC_CLIENT_VERSION, version);
            
            String crc = din.readUTF();
            assertEquals("CRC_OK", crc);
            
            int lang = din.readInt();
            assertEquals(Language.EN, lang);
            
            int len = din.readInt();
            Provider[] descProviders = m_searchDesc.getAllProvidersInternalArray();
            assertEquals(descProviders.length, len);
            for (int i = 0; i < len; i++) {
                int round = din.readInt();
                assertEquals(descProviders[i].getRound(), round);
                int heading = din.readInt();
                assertEquals(descProviders[i].getHeadingID(), heading);
                String name = din.readUTF();
                assertEquals(descProviders[i].getProviderName(), name);
                String type = din.readUTF();
                assertEquals(descProviders[i].getProviderType(), type);
                int topRegion = din.readInt();
                assertEquals(descProviders[i].getTopRegionID(), topRegion);
                String imageName = din.readUTF();
                assertEquals(descProviders[i].getProviderImageName(), imageName);
            }
        } catch (IOException e) {
            fail("IOExeption in write test: "+e);
        }
    }
    
    public void testReadDesc() {
        try {
            DataOutputStream dout = m_settingsConn.getOutputStream(
                    SearchConstants.PERSISTENCE_SEARCH_DESCRIPTOR);
            dout.writeInt(SearchDescriptor.DESC_CLIENT_VERSION);
            dout.writeUTF(m_searchDesc.getCRC());
            dout.writeInt(m_searchDesc.getLanguageID());
            Provider[] providers = m_searchDesc.getAllProvidersInternalArray();
            dout.writeInt(providers.length);
            for (int i = 0; i < providers.length; i++) {
                dout.writeInt(providers[i].getRound());
                dout.writeInt(providers[i].getHeadingID());
                dout.writeUTF(providers[i].getProviderName());
                dout.writeUTF(providers[i].getProviderType());
                dout.writeInt(providers[i].getTopRegionID());
                dout.writeUTF(providers[i].getProviderImageName());
            }
            
            SearchDescriptor sd = m_req.readDataInternal(
                    m_settingsConn.getDataInputStream(
                            SearchConstants.PERSISTENCE_SEARCH_DESCRIPTOR));
            assertNotNull(sd);
            assertEquals(m_searchDesc.getCRC(), sd.getCRC());
            assertEquals(m_searchDesc.getLanguageID(), sd.getLanguageID());
            Provider[] readProviders = sd.getAllProvidersInternalArray();
            assertEquals(providers.length, readProviders.length);
            for (int i = 0; i < providers.length; i++) {
                assertEquals(providers[i].getRound(), readProviders[i].getRound());
                assertEquals(providers[i].getHeadingID(), readProviders[i].getHeadingID());
                assertEquals(providers[i].getProviderName(), readProviders[i].getProviderName());
                assertEquals(providers[i].getProviderType(), readProviders[i].getProviderType());
                assertEquals(providers[i].getTopRegionID(), readProviders[i].getTopRegionID());
                assertEquals(providers[i].getProviderImageName(), readProviders[i].getProviderImageName());
            }
        } catch (IOException e) {
            fail("IOExeption in read test: "+e);
        }
    }
}
