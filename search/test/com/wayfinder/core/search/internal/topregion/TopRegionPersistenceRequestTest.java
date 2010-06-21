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
package com.wayfinder.core.search.internal.topregion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.internal.SearchConstants;
import com.wayfinder.core.search.internal.topregion.TopRegionPersistenceRequest.TopRegionPersistenceWrapper;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class TopRegionPersistenceRequestTest extends TestCase {
    
    private SettingsConnection m_settingsConn;
    private TopRegionPersistenceRequest m_req;
    private String m_CRC = "CRC_OK";
    private int m_lang = Language.EN;
    private TopRegionImpl[] m_topRegions;

    /**
     * @param name
     */
    public TopRegionPersistenceRequestTest(String name) {
        super(name);
        m_topRegions = new TopRegionImpl[2];
        m_topRegions[0] = new TopRegionImpl("Sweden", TopRegionImpl.TYPE_COUNTRY, 0);
        m_topRegions[1] = new TopRegionImpl("UK", TopRegionImpl.TYPE_COUNTRY, 1);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_settingsConn = MemoryPersistenceLayer.getPersistenceLayer().openSettingsConnection("search");
        m_req = new TopRegionPersistenceRequest(null, m_CRC, m_lang, m_topRegions);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWriteData() {
        try {
            m_req.writePersistenceData(m_settingsConn);
            
            DataInputStream din = m_settingsConn.getDataInputStream(SearchConstants.PERSISTENCE_TOP_REGIONS);
            
            int version = din.readInt();
            assertEquals(TopRegionPersistenceRequest.VERSION, version);
            
            String crc = din.readUTF();
            assertEquals(m_CRC, crc);
            
            int lang = din.readInt();
            assertEquals(m_lang, lang);
            
            int len = din.readInt();
            assertEquals(m_topRegions.length, len);
            for (int i = 0; i < len; i++) {
                TopRegionImpl tr = new TopRegionImpl(din);
                assertEquals(m_topRegions[i], tr);
            }
        } catch (IOException e) {
            fail("IOException in write test: "+e);
        }
    }
    
    public void testReadData() {
        DataOutputStream dout;
        try {
            dout = m_settingsConn.getOutputStream(
                    SearchConstants.PERSISTENCE_TOP_REGIONS);
            dout.writeInt(TopRegionPersistenceRequest.VERSION);
            dout.writeUTF(m_CRC);
            dout.writeInt(m_lang);
            dout.writeInt(m_topRegions.length);
            for (int i = 0; i < m_topRegions.length; i++) {
                m_topRegions[i].write(dout);
            }
            
            TopRegionPersistenceWrapper trpw = 
                m_req.readDataInternal(
                        m_settingsConn.getDataInputStream(
                                SearchConstants.PERSISTENCE_TOP_REGIONS));
            assertNotNull(trpw);
            assertEquals(m_CRC, trpw.m_crc);
            assertEquals(m_lang, trpw.m_lang);
            assertEquals(m_topRegions.length, trpw.m_topRegions.length);
            for (int i = 0; i < m_topRegions.length; i++) {
                assertEquals(m_topRegions[i], trpw.m_topRegions[i]);
            }
        } catch (IOException e) {
            fail("IOException in read test: "+e);
        }
    }
}
