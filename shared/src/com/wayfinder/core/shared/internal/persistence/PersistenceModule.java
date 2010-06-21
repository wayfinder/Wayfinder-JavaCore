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

import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.persistence.PersistenceLayer;

public class PersistenceModule implements Runnable {
    
    public static final String SETTING_USER_DATA   = "user_data";
    public static final String SETTING_MAP_DATA    = "map_data";
    public static final String SETTING_SEARCH_DATA = "search_data";

    private final WorkScheduler m_WorkScheduler;
    private final PersistenceLayer m_persistence;
    private LinkedList m_Tasks = new LinkedList();
    
    private boolean m_isRunning;
    
    public PersistenceModule(WorkScheduler work, PersistenceLayer persistence) {
        m_WorkScheduler = work;
        m_persistence = persistence;
    }
    
    /**
     * Adding a request to an internal queue and return immediately.  
     * Methods on given {@link PersistenceRequest} will be called later. 
     * 
     * @param requester 
     * @param type the type of the persistent data that should be loaded, defined in {@link PersistenceLayer}
     */
    public synchronized void pendingReadPersistenceRequest(PersistenceRequest requester, String type) {        
        PersistenceTask task = new PersistenceTask(requester, PersistenceTask.READ, m_persistence, type);
        m_Tasks.addLast(task);
        
        if(!m_isRunning) {
            m_isRunning = true;
            m_WorkScheduler.schedule(this);
        }
    }

    /**
     * Adding a request to an internal queue and return immediately.  
     * Methods on given {@link PersistenceRequest} will be called later. 
     * 
     * @param requester 
     * @param type the type of the persistent data that should be loaded, defined in {@link PersistenceLayer}
     */
    public synchronized void pendingWritePersistenceRequest(PersistenceRequest requester, String type) {
        PersistenceTask task = new PersistenceTask(requester, PersistenceTask.WRITE, m_persistence, type);
        m_Tasks.addLast(task);
        
        if(!m_isRunning) { 
            m_isRunning = true;
            m_WorkScheduler.schedule(this);
        }
    }
    
    /*
     * Internal method that checks if the request queue is empty or not. 
     * <p>
     * Return true if there are request in the request queue. 
     */
    private synchronized boolean shouldRun() {
        if(m_Tasks.size() == 0)
            m_isRunning = false;
        return m_isRunning;
    }

    public void run() {
        
        // Execute the PersistentTask in the order they have been added.
        while(shouldRun()) {        
            PersistenceTask task = (PersistenceTask)m_Tasks.removeFirst();                   
            task.executeTask();
        }        
    }
}
