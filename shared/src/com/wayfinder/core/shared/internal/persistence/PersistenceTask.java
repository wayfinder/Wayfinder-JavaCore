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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.shared.internal.persistence;

import java.io.IOException;

import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

class PersistenceTask {

    private static final Logger LOG = LogFactory.getLoggerForClass(PersistenceTask.class);
    
    static final int READ   = 0;
    static final int WRITE  = 1;

    
    private int m_Action;
    private String m_Type;
    private PersistenceRequest m_Requester;
    private PersistenceLayer m_PersistenceLayer;
    
    PersistenceTask(PersistenceRequest requester, int action, PersistenceLayer persistence, String type) {
        m_Requester = requester;
        m_Action = action;
        m_PersistenceLayer = persistence;
        m_Type = type;
    }

    /**
     * Execute the tasks and call the PersistentRequest. 
     * <p>
     * 
     * Open the correct data stream from the persistent layer and call the listener 
     * with the correct stream passed as a parameter. 
     * 
     */
    void executeTask() {
        
        SettingsConnection sConnection = null;        
        try {
            sConnection = m_PersistenceLayer.openSettingsConnection(m_Type);
            //TODO better treat the case when the file dosen't exist 
            //instead of caused a FileNotFoundException 
            if(m_Action == READ) {
                m_Requester.readPersistenceData(sConnection);
            } else if(m_Action == WRITE) {
                m_Requester.writePersistenceData(sConnection);
            }
        } catch(Exception e) {
            if(LOG.isError()) {
                LOG.error("PersistenceTask.executeTask()", e);
            }
            m_Requester.error(new CoreError(e.toString()));
        } finally {
            try {
                if(sConnection != null)
                    sConnection.close();
            } catch(IOException e) {
                m_Requester.error(new CoreError(e.toString()));
                if(LOG.isError()) {
                    LOG.error("PersistenceTask.executeTask()", e.toString());
                }
            }
        }
    }
    
}
