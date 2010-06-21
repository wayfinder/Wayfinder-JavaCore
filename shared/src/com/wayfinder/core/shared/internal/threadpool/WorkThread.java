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
package com.wayfinder.core.shared.internal.threadpool;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.debug.Level;

/**
 * Represents a thread in the WorkScheduler thread pool.
 * 
 * 
 *
 */
final class WorkThread implements Runnable {

    private final WorkScheduler m_scheduler;
    private final String m_threadName;
    private final int m_threadNbr;
    private volatile boolean m_shouldDie;

    private static final Logger LOG = LogFactory
            .getLoggerForClass(WorkThread.class);

    /**
     * Standard constructor
     * 
     * @param scheduler The WorkScheduler owning this thread
     * @param threadNbr The number of this thread
     */
    WorkThread(WorkScheduler scheduler, int threadNbr) {
        m_threadName = "WorkThread-" + threadNbr;
        m_threadNbr = threadNbr;
        m_scheduler = scheduler;
    }
    
    
    String getName() {
        return m_threadName;
    }


    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        if(LOG.isDebug()) {
            LOG.debug("WorkThread.run()", m_threadName + " started");
        }
        
        while(!m_shouldDie) {
            if(LOG.isTrace()) {
                LOG.trace("WorkThread.run()", m_threadName + " fetching next work");
            }

            Work w = m_scheduler.getNextWork(m_threadNbr);
            if(w != null) {
                try {
                    if(LOG.isTrace()) {
                        LOG.trace("WorkThread.run()", 
                                m_threadName + " executing work: " 
                                + w.toString());
                    }

                    w.run();
                    
                    if(LOG.isTrace()) {
                        LOG.trace("WorkThread.run()", m_threadName + 
                                " finished executing work: " + w.toString());
                    }

                } catch(Throwable t) {
                    if(LOG.isError()) {
                        LOG.error("WorkThread.run()", 
                                m_threadName + " caught exception " + 
                                t.toString() + " from scheduled work: " 
                                + w.toString());
                        LOG.logException(Level.ERROR, "WorkThread.run()", t);
                    }
                }

                // in case the runnable changes the priority
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                if(w.shouldBeRescheduled()) {
                    m_scheduler.schedule(w);
                } else {
                    m_scheduler.workCompleted(w);
                }
            }
            Thread.yield();
        }
        m_scheduler.threadExiting(m_threadNbr);
        if(LOG.isDebug()) {
            LOG.debug("WorkThread.run()", m_threadName + " stopped");
        }
    }


    /**
     * Orders the WorkThread to stop execution and shutdown once the current
     * Work is completed
     */
    void terminate() {
        m_shouldDie = true;
    }
}
