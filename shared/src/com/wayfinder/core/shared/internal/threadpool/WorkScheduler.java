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

import java.util.Hashtable;
import java.util.Timer;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.FibonacciHeap;
import com.wayfinder.core.shared.util.PriorityQueue;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;



/**
 * This represents a shared thread pool. Other systems can queue up instances
 * of the Work or Runnable interfaces for execution by one of the available 
 * threads to save up system resources by removing the need to create temporary 
 * threads.
 * <p>
 * According to JTWI, the platform must support at least 10 threads, but may
 * support more. Since we cannot count on a platform actually supporting more
 * than this, this pool also acts as a way to keep control over how many threads
 * we are using at any given time.
 * <p>
 * This class also offers a means to control and balance the amount of threads 
 * in the application. By refraining from starting Threads outside this class,
 * it may be possible to dynamically lower the number of WorkThreads if the
 * application needs more dedicated Threads.
 * 
 * 
 */
public final class WorkScheduler {

    //-------------------------------------------------------------------------
    // static constants - limits

    /**
     * The default limit of threads in the scheduler
     * <p>
     * This constant has the value 3
     */
    public static final int POOL_LIMIT_DEFAULT = 3;
    
    
    //-------------------------------------------------------------------------
    // static constants - priorities
    
    
    /**
     * Actions that the very core of the application depends on may be given
     * this priority, though it is not recommended
     */
    public static final int PRIORITY_CRITICAL = Integer.MAX_VALUE;


    /**
     * User initiated actions should have this priority to ensure that they are
     * run as quickly as possible.
     */
    public static final int PRIORITY_HIGH   = Integer.MAX_VALUE / 2;


    /**
     * This is the normal priority
     */
    public static final int PRIORITY_NORMAL = 0;


    /**
     * Actions initiated by a background process (eg invisible to the user)
     * should have this priority.
     */
    public static final int PRIORITY_LOW = Integer.MIN_VALUE / 2;


    /**
     * Actions that are mundane or insignificant may be added with this
     * priority.
     */
    public static final int PRIORITY_MINIMAL = Integer.MIN_VALUE;
    
    
    //-------------------------------------------------------------------------
    // class variables

    private final ConcurrencyLayer m_cLayer;
    private final PriorityQueue m_workQueue;
    private final Hashtable m_workInProgress;
    private final int m_maxGlobalThreadCount;
    private final int m_maxThreadCount;
    private final WorkThread[] m_threadArray;
    private Timer m_sharedTimer;
    private int m_currentNbrOfThreads;
    private int m_nbrOfFreeThreads;
    private boolean m_workThreadsStopped;
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(WorkScheduler.class);


    //-------------------------------------------------------------------------
    // constructors


    /**
     * Standard constructor
     * <p>
     * This will create a new instance of the WorkSheduler that allows up to
     * maxNumberOfThreads threads for usage. Once the constructor has
     * completed, one thread will be active in the pool and instances of Work 
     * and Runnable can be queued immediately. If the workload on the scheduler
     * increases, more threads will be created until the limits have been 
     * reached.
     * <p>
     * The creator may set the limits of the scheduler to any number above zero,
     * though the following is recommended:
     * <ul>
     * <li>The creator should not create a lot of threads unless planning to
     * schedule a large number of tasks.
     * </ul>
     * @param cLayer The {@link ConcurrencyLayer} implementation from the
     * {@link PAL}
     * @param maxNumberOfThreads The maximum number of threads this pool will
     * have
     * @throws IllegalArgumentException if aMaxNumberOfThreads or 
     * aGlobalThreadLimit are less than 1
     * @throws IllegalThreadStateException if no threads at all could be started
     */
    public WorkScheduler(ConcurrencyLayer cLayer, int maxNumberOfThreads) 
    throws IllegalArgumentException, IllegalThreadStateException {
        m_cLayer = cLayer;
        m_maxThreadCount = maxNumberOfThreads;
        m_maxGlobalThreadCount = cLayer.getMaxNumberOfThreadsForPlatform();
        
        if(maxNumberOfThreads <= 0) {
            throw new IllegalArgumentException("Max number of threads for " +
            "the scheduler must be at least 1");
        } else if(m_maxGlobalThreadCount <= 0) {
            throw new IllegalArgumentException("Global thread limit of the " +
            "platform must be at least 1");
        }

        m_workQueue = new FibonacciHeap(new WorkPriorityComparator());
        m_workInProgress = new Hashtable();
        m_threadArray = new WorkThread[maxNumberOfThreads];

        if(LOG.isInfo()) {
            LOG.info("WorkScheduler.WorkScheduler()", "will attempt to start " + 
                    m_maxThreadCount + " threads in total. The global max nbr of " +
                    "threads are " + m_maxGlobalThreadCount);
        }
        
        // start one thread right away
        // startAdditionalThread();
    }

    //-------------------------------------------------------------------------
    // public methods - normal scheduling


    /**
     * Queues a Work for execution.
     * <p>
     * The Work.run() method will be executed as soon as a thread is available. 
     * The run() method of the Work is allowed to reassign the priority of the 
     * thread if so needed, <b>but is not allowed to run forever</b>
     * (eg within a while(true)-loop).
     * <p>
     * If a Work is required to run repeatedly, have the 
     * Work.shouldBeRescheduled() method return true while the Work needs to 
     * be run.
     * <p>
     * There are no guarantees as to exactly when the Work will be executed, 
     * only that the Work objects will be sorted according to priority.
     *
     * @param work The Work for execution
     * @throws IllegalStateException If the WorkScheduler is stopped
     */
    public synchronized void schedule(Work work) {
        if(m_workThreadsStopped) {
            throw new IllegalStateException("All workthreads stopped");
        }

        int queueSize = m_workQueue.insert(work);

        if(LOG.isTrace()) {
            LOG.trace("WorkScheduler.schedule()", "Current queuesize is: " + 
                queueSize + " and there are " + m_nbrOfFreeThreads + 
                " free workthreads and " + m_currentNbrOfThreads + " in total");
        }
        
        //FIXME Find a better criteria for starting a new thread
        if(queueSize > m_nbrOfFreeThreads) {
            startAdditionalThread();
        }

        if(LOG.isDebug()) {
            LOG.debug("WorkScheduler.schedule()", "There are currently " 
            + m_cLayer.getCurrentNbrOfThreads() + " threads in the application");
        }
        
        notifyAll();
    }


    /**
     * Attempts to create and start a new WorkThread
     * 
     * @throws IllegalThreadStateException if no threads at all exists in the
     * scheduler after the attempt was made
     */
    private void startAdditionalThread() {
        if( (m_currentNbrOfThreads < m_maxThreadCount) && platformAllowsMoreThreads() ) {
            if(LOG.isTrace()) {
                LOG.trace("WorkScheduler.startAdditionalThread()", 
                        "starting thread #" + m_currentNbrOfThreads);
            }
            for (int i = 0; i < m_threadArray.length; i++) {
                if(m_threadArray[i] == null) {
                    try {
                        m_threadArray[i] = new WorkThread(this, i);
                        m_cLayer.startNewDaemonThread(m_threadArray[i], m_threadArray[i].getName());
                        m_currentNbrOfThreads++;
                    } catch(Throwable t) {
                        m_threadArray[i] = null;
                        if(LOG.isError()) {
                            LOG.error("WorkScheduler.startAdditionalThread()",
                                    "caught throwable " + 
                                    t.toString() + " when starting thread " + 
                                    m_currentNbrOfThreads + " - " + t.getMessage());
                            LOG.logException(Logger.ERROR, 
                                    "WorkScheduler.startAdditionalThread()", 
                                    t);
                        }
                    }
                    break;
                }
            }
        }

        if(m_currentNbrOfThreads == 0) {
            if(LOG.isFatal()) {
                LOG.fatal("WorkScheduler.startAdditionalThread()", 
                        "WorkScheduler could not start any threads");
            }
            throw new IllegalThreadStateException("WorkScheduler was unable " +
            "to start any threads");
        }

        if(LOG.isDebug()) {
            int threadCount = m_cLayer.getCurrentNbrOfThreads();
            LOG.debug("WorkScheduler.startAdditionalThread()",
                    " managed to start " + 
                    m_currentNbrOfThreads + " thread(s) out of " + 
                    m_maxThreadCount + " requested. This process has now " + 
                    threadCount + " thread(s) in total");
            
            if(threadCount > ConcurrencyLayer.THREAD_LIMIT_BLACKBERRY) {
                LOG.error("WorkScheduler.startAdditionalThread()",
                "WARNING! There are more than 16 threads in the " +
                "application. This codebase will not run on the BlackBerry!");
            } else if(threadCount > ConcurrencyLayer.THREAD_LIMIT_JTWI) {
                LOG.error("WorkScheduler.startAdditionalThread()",
                "WARNING! There are more than 10 threads in the " +
                "application. This exceeds the limits of the JTWI specification!");
            }
        }
    }


    /**
     * Queues a Runnable for execution with an assigned priority.
     * <p>
     * The Runnable.run() method will be executed as soon as a thread is 
     * available. The run method of the Runnable is allowed to reassign the 
     * priority of the thread if so needed, <b>but is not allowed to run 
     * forever</b> (eg within a while(true)-loop).
     * <p>
     * If a Runnable is required to run repeatedly, consider implementing the 
     * Work interface instead.
     * <p>
     * There are no guarantees as to exactly when the Runnable will be executed, 
     * only that the Runnable objects will be sorted according to priority.
     *
     * @param run The Runnable for execution
     * @param priority The priority of the Runnable
     * @throws IllegalThreadStateException If the WorkScheduler is stopped
     */
    public synchronized void schedule(Runnable run, int priority) {
        schedule(new WorkRunnable(run, priority));
    }
    
    
    /**
     * Queues a Runnable for execution with normal priority.
     * <p>
     * The Runnable.run() method will be executed as soon as a thread is 
     * available. The run method of the Runnable is allowed to reassign the 
     * priority of the thread if so needed, <b>but is not allowed to run 
     * forever</b> (eg within a while(true)-loop).
     * <p>
     * Calling this method is the same as calling<br>
     * <code>schedule(aRun, PRIORITY_NORMAL)</code>
     * <p>
     * If a Runnable is required to run repeatedly, consider implementing the 
     * Work interface instead.
     * <p>
     * There are no guarantees as to exactly when the Runnable will be executed, 
     * only that the Runnable objects will be sorted according to priority.
     *
     * @param run The Runnable for execution
     * @throws IllegalThreadStateException If the WorkScheduler is stopped
     */
    public synchronized void schedule(Runnable run) {
        schedule(run, PRIORITY_NORMAL);
    }


    //-------------------------------------------------------------------------
    // public methods - blocking scheduling


    /**
     * Queues a Work for execution.
     * <p>
     * The Work.run() method will be executed as soon as a thread is available. 
     * The run() method of the Work is allowed to reassign the priority of the 
     * thread if so needed, <b>but is not allowed to run forever</b> 
     * (eg within a while(true)-loop).
     * <p>
     * If a Work is required to run repeatedly, have the 
     * Work.shouldBeRescheduled() method return true while the Work needs to be 
     * run.
     * <p>
     * There are no guarantees as to exactly when the Work will be executed, 
     * only that the Work objects will be sorted according to priority.
     * <p>
     * <b>This method will block until the Work.run() method has been 
     * completed</b>
     *
     * @param work The Work for execution
     * @throws IllegalThreadStateException If the WorkScheduler is stopped
     */
    public synchronized void scheduleAndWait(Work work) {
        schedule(work);
        m_workInProgress.put(work, work);
        while(m_workInProgress.containsKey(work)) {
            try {
                wait();
            } catch (InterruptedException e) {
                if(LOG.isError()) {
                    LOG.error("WorkScheduler.scheduleAndWait()", e.toString());
                }
            }
        }
    }
    
    
    /**
     * Queues a Runnable for execution with an assigned priority.
     * <p>
     * The Runnable.run() method will be executed as soon as a thread is 
     * available. The run method of the Work is allowed to reassign the priority 
     * of the thread if so needed, <b>but is not allowed to run forever</b> 
     * (eg within a while(true)-loop).
     * <p>
     * If a Runnable is required to run repeatedly, consider implementing the 
     * Work interface instead.
     * <p>
     * There are no guarantees as to exactly when the Runnable will be executed, 
     * only that the Runnable objects will be sorted according to priority.
     * <p>
     * <b>This method will block until the Runnable.run() method has been 
     * completed</b>
     *
     * @param run The Runnable for execution
     * @param priority The priority of the Runnable
     * @throws IllegalThreadStateException If the WorkScheduler is stopped
     */
    public synchronized void scheduleAndWait(Runnable run, int priority) {
        scheduleAndWait(new WorkRunnable(run, priority));
    }
    
    
    /**
     * Queues a Runnable for execution with normal priority.
     * <p>
     * The Runnable.run() method will be executed as soon as a thread is 
     * available. The run method of the Runnable is allowed to reassign the priority 
     * of the thread if so needed, <b>but is not allowed to run forever</b> 
     * (eg within a while(true)-loop).
     * <p>
     * If a Runnable is required to run repeatedly, consider implementing the 
     * Work interface instead.
     * <p>
     * Calling this method is the same as calling<br>
     * <code>scheduleAndWait(aRun, PRIORITY_NORMAL)</code>
     * <p>
     * There are no guarantees as to exactly when the Runnable will be executed, 
     * only that the Runnable objects will be sorted according to priority.
     * <p>
     * <b>This method will block until the Runnable.run() method has been 
     * completed</b>
     *
     * @param run The Runnable for execution
     * @throws IllegalThreadStateException If the WorkScheduler is stopped
     */
    public synchronized void scheduleAndWait(Runnable run) {
        scheduleAndWait(run, PRIORITY_NORMAL);
    }


    /**
     * Schedules a {@link Runnable} for execution after a delay.
     * <p>
     * Once the delay has expired, the Runnable.run() method will be executed as 
     * soon as a thread is available. The run method of the Runnable is allowed 
     * to reassign the priority of the thread if so needed, <b>but is not 
     * allowed to run forever</b> (eg within a while(true)-loop).
     * <p>
     * If a Runnable is required to run repeatedly, consider implementing the 
     * Work interface instead.
     * <p>
     * There are no guarantees as to exactly when the Runnable will be executed, 
     * only that the Runnable will not be executed before the delay has passed.
     * <p>
     * If the delay is 0, calling this method will be the same as calling
     * {@link #schedule(Runnable)}.
     *
     * @param run The Runnable for execution
     * @param delay The delay to wait before the {@link Runnable} should be
     * executed. If 0, the Runnable will be placed on the regular queue
     * immediately.
     * @throws IllegalThreadStateException If the WorkScheduler is stopped
     * @throws IllegalArgumentException If delay is less than 0
     */
    public synchronized void scheduleDelayed(Runnable run, long delay) {
        if(m_workThreadsStopped) {
            throw new IllegalStateException("All workthreads stopped");
        }
        if(delay < 0) {
            throw new IllegalArgumentException("Cannot schedule with negative delay");
        } else if(delay == 0) {
            schedule(run);
        } else {
            if(m_sharedTimer == null) {
                m_sharedTimer = m_cLayer.startNewDaemonTimer();
            }
            m_sharedTimer.schedule(new WorkTimerTask(this, run), delay);
        }
    }
    

    //-------------------------------------------------------------------------
    // public methods for consideration


    /**
     * Allocates and starts a new Thread in the application with the supplied
     * Runnable and name
     * <p>
     * Instead of having another system create and start a Thread by itself, 
     * this method allows the WorkScheduler to keep the threads in check. 
     * Instead of a call to Thread.start() silently failing somewhere in the 
     * application, this allows the WorkScheduler to attempt to lower the 
     * number of WorkThreads to make room for the new Thread.
     * <p>
     * In essence, calling this method is the same as calling
     * <p>
     * <code>startThread(new Thread(aRun, aThreadName)</code>
     * 
     * @param run The runnable that should be passed to the Thread constructor.
     * Must not be null
     * @param threadName The name to pass to the Thread constructor. May be
     * null if no specific name is required
     * @throws IllegalStateException if the platform limit is reached and the
     * WorkScheduler was unable to lower the number of work threads
     */
    public synchronized Thread startThread(Runnable run, String threadName) {
        if(!platformAllowsMoreThreads()) {
            lowerNbrOfWorkThreads();
        }
        return m_cLayer.startNewDaemonThread(run, threadName);
    }


    /**
     * Allocates and starts a new Thread in the application with the supplied
     * Runnable
     * <p>
     * Instead of having another system create and start a Thread by itself, 
     * this method allows the WorkScheduler to keep the threads in check. 
     * Instead of a call to Thread.start() silently failing somewhere in the 
     * application, this allows the WorkScheduler to attempt to lower the 
     * number of WorkThreads to make room for the new Thread.
     * <p>
     * In essence, calling this method is the same as calling
     * <p>
     * <code>allocateThread(new Thread(aRun, null)</code>
     * 
     * @param run The runnable that should be passed to the Thread constructor.
     * Must not be null
     * @throws IllegalStateException if the platform limit is reached and the
     * WorkScheduler was unable to lower the number of work threads
     */
    public synchronized Thread startThread(Runnable run) {
        return startThread(run, null);
    }


    /**
     * Future method. Should attempt to lower the number of current work threads
     * This method should be blocking until the number of work threads have
     * been reduced.
     * <p>
     * The number of work threads should not be reduced below one.
     * 
     * @throws IllegalStateException if the platform limit is reached and the
     * WorkScheduler was unable to lower the number of work threads
     */
    private void lowerNbrOfWorkThreads() {
        //FIXME try to lessen the pressure by lowering the number of work threads
        if(LOG.isError()) {
            LOG.error("WorkScheduler.lowerNbrOfWorkThreads()", 
                    "WARNING - A THREAD WAS UNABLE TO START");
        }
        throw new IllegalStateException("Platform limit of threads reached " +
                "- unable to start new thread");
    }


    //-------------------------------------------------------------------------
    // public methods - shutdown


    /**
     * Effectively stops all working threads. This should be called before the
     * application exits to ensure that all threads are removed.
     * <p>
     * Once this method is called, the WorkScheduler can be considered "dead".
     * All pending work will be ignored. All calls to the scheduling methods 
     * will throw an IllegalStateException.
     * <p>
     * Uncompleted work can be retrieved via the getUnExecutedWorks() methods
     */
    public synchronized void stopThreads() {
        if(!m_workThreadsStopped) {
            if (m_workInProgress != null) {
                m_workInProgress.clear();
            }
            for(int i = 0; i < m_maxThreadCount; i++) {
                killThread(i);
            }
            if(m_sharedTimer != null) {
                m_sharedTimer.cancel();
                m_sharedTimer = null;
            }
            m_workThreadsStopped = true;
            notifyAll();
        }
    }
    
    
    /**
     * Stops a specified work thread in the array
     * 
     * @param threadArrayIndex The index in the array of the thread to stop
     */
    private void killThread(int threadArrayIndex) {
        if(m_threadArray != null &&
                threadArrayIndex >= 0 &&
                threadArrayIndex < m_threadArray.length) {

            WorkThread thread = m_threadArray[threadArrayIndex];

            if (thread != null) {
                if(LOG.isTrace()) {
                    LOG.trace("WorkScheduler.killThread()",
                            "killing " + thread.getName() + 
                            "(index " + threadArrayIndex + ")");
                }
                thread.terminate();
            }
        }
    }
    
    
    /**
     * This method will block until all threads in the WorkScheduler has
     * completed their current Work.
     * 
     * @throws IllegalStateException if called before stopThreads() is called
     */
    public synchronized void waitUntilThreadsAreDead() {
        if(!m_workThreadsStopped) {
            throw new IllegalStateException("WorkScheduler not stopped");
        }
        
        while(m_currentNbrOfThreads > 0) {
            if(LOG.isDebug()) {
                LOG.debug("WorkScheduler.waitUntilThreadsAreDead()",
                        "waiting for threads to die (" + m_currentNbrOfThreads + " remaining)");
            }
            
            try {
                wait();
            } catch (InterruptedException e) {
                if(LOG.isError()) {
                    LOG.error("WorkScheduler.waitUntilThreadsAreDead()", e.toString());
                }
            }
        }
        if(LOG.isDebug()) {
            LOG.debug("WorkScheduler.waitUntilThreadsAreDead()",
                    "All threads in WorkScheduler dead");
        }
        
    }
    
    
    /**
     * Returns all Works that where left in the queue when the WorkScheduler
     * was stopped. When this method returns, the internal queue will be
     * empty
     * 
     * @return an array with the not executed Works
     * @throws IllegalStateException if called before stopThreads() is called
     */
    public synchronized Work[] getUnExecutedWorks() {
        if(!m_workThreadsStopped) {
            throw new IllegalStateException("WorkScheduler not stopped");
        }
        int size = m_workQueue.size();
        Work[] works = new Work[size];
        for (int i = 0; i < size; i++) {
            works[i] = (Work) m_workQueue.removeHighestPrio();
        }
        return works;
    }
    
    
    /**
     * Executes all Works that where left in the queue when the WorkScheduler
     * was stopped. When this method returns, the internal queue will be
     * empty.
     * 
     * @throws IllegalStateException if called before stopThreads() is called
     */
    public synchronized void runRemainingWorks() {
        Work[] works = getUnExecutedWorks();
        for (int i = 0; i < works.length; i++) {
            try {
                works[i].run();
            } catch(Throwable t) {
                if(LOG.isError()) {
                    LOG.error("WorkScheduler.runRemainingWorks()", 
                            "error when run " + works[i].toString() + ":" + t.toString());
                }
            }
        }
    }


    //-------------------------------------------------------------------------
    // Package protected methods accessed by WorkThreads


    /**
     * Returns the next Work in the queue.
     * <p>
     * The Work returned is guaranteed to be the currently highest
     * prioritized one. If the queue is empty, this method will block until a
     * Work is placed in the queue.
     * <p>
     * If the WorkScheduler is stopped, this method returns null.
     * 
     * @param threadNbr The number of the WorkThread.
     * @return The Work with the highest priority or null if the 
     * WorkScheduler is stopped
     */
    synchronized Work getNextWork(int threadNbr) {
        m_nbrOfFreeThreads++;
        while(m_workQueue.isEmpty() && !m_workThreadsStopped) {
            //FIXME kill off threads if there are a lot waiting?
            // remember to avoid calling Thread.interrupt() in that case
            /*
            if(iFreeThreads > 3) {
                killThread(aThreadNbr);
                return null;
            }*/
            try {
                wait();
            } catch (InterruptedException e) {
                if(LOG.isError()) {
                    LOG.error("WorkScheduler.getNextWork()", e.toString());
                }
            }
        }
        m_nbrOfFreeThreads--;

        if(m_workThreadsStopped) {
            return null;
        } 
        return (Work) m_workQueue.removeHighestPrio();
    }


    /**
     * This method should be called when a Work is completed, regardless of the 
     * outcome of the Work.run() method
     * 
     * @param work The completed Work object
     */
    synchronized void workCompleted(Work work) {
        m_workInProgress.remove(work);
        notifyAll();
    }
    
    
    /**
     * This method is called by the workthreads when they expire. Note that it
     * is the last thing that is done by the workscheduler and it may take the
     * OS more time to actually expell the thread.
     * 
     * @param threadNumber The number of the thread in the array
     */
    synchronized void threadExiting(int threadNumber) {
        m_threadArray[threadNumber] = null;
        m_currentNbrOfThreads--;
        notifyAll();
    }
    
    
    //-------------------------------------------------------------------------
    // Package protected methods accessed by testclasses
    
    synchronized int getNumberOfWorkThreads() {
        return m_currentNbrOfThreads;
    }
    
    synchronized int getNumberOfScheduledWorks() {
        return m_workQueue.size();
    }

    //-------------------------------------------------------------------------
    // utility methods


    /**
     * Checks to see if the current platform can start more threads
     * 
     * @return true if and only if more threads can be created
     */
    private boolean platformAllowsMoreThreads() {
        return m_cLayer.getCurrentNbrOfThreads() < m_maxGlobalThreadCount;
    }
}
