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
package com.wayfinder.core.shared.internal.persistence;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;

import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class PersistenceModuleTest extends TestCase {
    
    private final ConcurrencyLayer m_cLayer;
    private final WorkScheduler m_ws;
    
    private PersistenceModule m_perModule;

    /**
     * @param name
     */
    public PersistenceModuleTest(String name) {
        super(name);
        
        m_cLayer = new ConcurrencyLayer() {
            
            public Timer startNewDaemonTimer() {
                return null;
            }
            
            public Thread startNewDaemonThread(Runnable run, String threadName) {
                return null;
            }
            
            public int getMaxNumberOfThreadsForPlatform() {
                return 1;
            }
            
            public int getCurrentNbrOfThreads() {
                return 0;
            }
        };
        
        m_ws = new WorkScheduler(m_cLayer, 1);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        m_perModule = new PersistenceModule(m_ws, MemoryPersistenceLayer.getPersistenceLayer());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddTasks() {
        PersistenceRequest reqWrite = new PersistenceRequest() {
            
            public void writePersistenceData(SettingsConnection sConnection)
                    throws IOException {
                DataOutputStream dout = sConnection.getOutputStream(0);
                dout.writeUTF("testing connection");
            }
            
            public void readPersistenceData(SettingsConnection sConnection)
                    throws IOException {
            }
            
            public void error(CoreError coreError) {
            }
        };
        
        PersistenceRequest reqRead = new PersistenceRequest() {
            
            public void writePersistenceData(SettingsConnection sConnection)
                    throws IOException {
            }
            
            public void readPersistenceData(SettingsConnection sConnection)
                    throws IOException {
                DataInputStream din = sConnection.getDataInputStream(0);
                String str = din.readUTF();
                assertEquals("testing connection", str);
            }
            
            public void error(CoreError coreError) {
            }
        };
        
        m_perModule.pendingWritePersistenceRequest(reqWrite, "test");
        m_perModule.pendingReadPersistenceRequest(reqRead, "test");
        
        m_perModule.run();
    }
}
