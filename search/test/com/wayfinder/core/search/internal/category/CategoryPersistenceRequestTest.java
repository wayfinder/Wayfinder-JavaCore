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
package com.wayfinder.core.search.internal.category;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.wayfinder.core.search.internal.category.CategoryPersistenceRequest.PersistentCategoryDataWrapper;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

/**
 * 
 *
 */
public class CategoryPersistenceRequestTest extends TestCase {
    
    private int m_store = 0;
    private String m_CRC = "CRC_OK";
    private int m_lang = Language.EN;
    private Position m_pos = Position.createFromDecimalDegrees(48.8738, 2.2950);
    private CategoryImpl[] m_categories = new CategoryImpl[2];
    
    private CategoryPersistenceRequest m_req;
    
    private SettingsConnection m_settings;

    /**
     * @param name
     */
    public CategoryPersistenceRequestTest(String name) {
        super(name);
        m_categories[0] = new CategoryImpl("custom1", "tat_custom1", -1);
        m_categories[1] = new CategoryImpl("Restaurant", "tat_restaurant", 23);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_settings = MemoryPersistenceLayer.getPersistenceLayer().openSettingsConnection("search");
        m_req = new CategoryPersistenceRequest(m_store, null, m_CRC, m_lang, m_pos, m_categories);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testWriteData() {
        try {
            m_req.writePersistenceData(m_settings);
            DataInputStream din = m_settings.getDataInputStream(0);
            int version = din.readInt();
            assertEquals(CategoryPersistenceRequest.VERSION, version);
            
            String crc = din.readUTF();
            assertEquals("CRC_OK", crc);
            
            int lang = din.readInt();
            assertEquals(m_lang, lang);
            
            int lat = din.readInt();
            assertEquals(m_pos.getMc2Latitude(), lat);
            
            int lon = din.readInt();
            assertEquals(m_pos.getMc2Longitude(), lon);
            
            int len = din.readInt();
            assertEquals(m_categories.length, len);
            
            for (int i = 0; i < len; i++) {
                CategoryImpl cat = new CategoryImpl(din);
                assertEquals(m_categories[i], cat);
            }
        } catch (IOException e) {
            fail("IOException while testing write: " + e);
        }
    }
    
    public void testReadData() {
        try {
            DataOutputStream dout = m_settings.getOutputStream(0);
            dout.writeInt(CategoryPersistenceRequest.VERSION);
            dout.writeUTF(m_CRC);
            dout.writeInt(m_lang);
            dout.writeInt(m_pos.getMc2Latitude());
            dout.writeInt(m_pos.getMc2Longitude());
            dout.writeInt(m_categories.length);
            for (int i = 0; i < m_categories.length; i++) {
                m_categories[i].write(dout);
            }
            
            PersistentCategoryDataWrapper data = m_req.readDataInternal(m_settings.getDataInputStream(0));
            assertNotNull(data);
            assertEquals(m_CRC, data.m_crc);
            assertEquals(m_lang, data.m_lang);
            assertEquals(m_pos.getMc2Latitude(), data.m_pos.getMc2Latitude());
            assertEquals(m_pos.getMc2Longitude(), data.m_pos.getMc2Longitude());
            assertEquals(m_categories.length, data.m_categories.length);
            for (int i = 0; i < m_categories.length; i++) {
                assertEquals(m_categories[i], data.m_categories[i]);
            }
        } catch (IOException e) {
            fail("IOException while testing read: " + e);
        }
    }
}
