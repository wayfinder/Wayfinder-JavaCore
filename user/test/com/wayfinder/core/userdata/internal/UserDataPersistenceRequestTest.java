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
package com.wayfinder.core.userdata.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.userdata.internal.hwkeys.HardwareKeyContainer;
import com.wayfinder.core.userdata.internal.hwkeys.KeyCollector;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class UserDataPersistenceRequestTest extends TestCase {
    
    private HardwareKeyContainer m_hwKeyContainer = 
        KeyCollector.createHardcodedHWKeyContainerWithIMEI("hardcodedimei"); 
    private UserImpl m_user = new UserImpl("1234567890", m_hwKeyContainer);
    
    private UserDataPersistenceRequest m_req;
    
    private SettingsConnection m_settingsConn;

    /**
     * @param name
     */
    public UserDataPersistenceRequestTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_req = new UserDataPersistenceRequest(null, m_user);
        m_settingsConn = MemoryPersistenceLayer.getPersistenceLayer().openSettingsConnection("user");
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
            
            DataInputStream din = m_settingsConn.getDataInputStream(
                    UserDataPersistenceRequest.RECORD_ID);
            
            int version = din.readInt();
            assertEquals(UserDataPersistenceRequest.VERSION, version);
            
            String uin = din.readUTF();
            assertEquals(m_user.getUIN(), uin);
        } catch (IOException e) {
            fail("IOException in write test: "+e);
        }
    }

    public void testReadData() {
        try {
            DataOutputStream dout = m_settingsConn.getOutputStream(UserDataPersistenceRequest.RECORD_ID);
            dout.writeInt(UserDataPersistenceRequest.VERSION);
            dout.writeUTF(m_user.getUIN());
            
            DataInputStream din = m_settingsConn.getDataInputStream(UserDataPersistenceRequest.RECORD_ID);
            String uin = m_req.readDataInternal(din);
            assertNotNull(uin);
            assertEquals(m_user.getUIN(), uin);
        } catch (IOException e) {
            fail("IOException in read test: "+e);
        }
    }
}
