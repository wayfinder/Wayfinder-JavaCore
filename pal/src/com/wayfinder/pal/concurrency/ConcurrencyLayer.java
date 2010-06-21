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
package com.wayfinder.pal.concurrency;

import java.util.Timer;

public interface ConcurrencyLayer {
    
    /**
     * Signifies the limit of threads per application as specified in the
     * Java Technology for the Wireless Industry (JTWI) specification - JSR 185.
     * <p>
     * The JTWI actually only states that the JVM should support 10 threads,
     * but there are no guarantees that the JVM supports any more.
     * <p>
     * This constant has the value 10
     */
    public static final int THREAD_LIMIT_JTWI = 10;


    /**
     * Signifies the limit of threads per application on the BlackBerry
     * platform.
     * <p>
     * This constant has the value 16
     */
    public static final int THREAD_LIMIT_BLACKBERRY = 16;


    /**
     * Signifies that the limit of threads per application is virtually
     * unlimited on the platform.
     * <p>
     * While this in reality is an actual limit, actually reaching it wouldn't 
     * be recommended...
     * <p>
     * This constant has the value Integer.MAX_VALUE (2147483647)
     */
    public static final int THREAD_LIMIT_UNLIMITED = Integer.MAX_VALUE;
    
    
    /**
     * Returns the maximum allowed number of threads for an application on this
     * platform.
     * <p>
     * For convenience, one the THREAD_LIMIT constants can be returned
     * 
     * @return the maximum allowed number of threads for an application on this
     * platform.
     */
    public int getMaxNumberOfThreadsForPlatform();
    
    
    /**
     * Returns the current number of active threads for this application
     * 
     * @return the current number of active threads for this application
     */
    public int getCurrentNbrOfThreads();
    
    
    /**
     * Creates, starts and returns a new {@link Thread}
     * <p>
     * If possible, the newly created {@link Thread} should be run as a daemon
     * thread to avoid prolonging the lifespan of the application.
     * 
     * @param run The {@link Runnable} for the thread to execute
     * @param threadName Optional name of the thread. <b>May be null</b>
     * @return The {@link Thread} object
     */
    public Thread startNewDaemonThread(Runnable run, String threadName);
    
    
    /**
     * Creates, starts and returns a new {@link Timer} object.
     * <p>
     * <b>The returned {@link Timer} should run as a daemon, eg
     * prolong the life of the application</b>
     * 
     * @return The {@link Timer} object
     */
    public Timer startNewDaemonTimer();
    
}
