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

/**
 * The Work interface should be implemented by any class whose instances are 
 * intended to be executed by the WorkScheduler over and over again. This 
 * interface is designed to provide a common protocol for objects that wish to 
 * execute code by the shared thread pool. 
 *
 * 
 * @see java.lang.Runnable
 */
public interface Work extends Runnable {


    /**
     * The general contract of the method run is that it may take any action 
     * whatsoever, but may NOT contain any while(true) loops. If the contents
     * of the run method must be run repeatedly, take one of the following
     * approaches:
     * <ul>
     * <li>Create a separate thread for it. Note that this approach should
     * only be taken for things that are critical for the application.</li>
     * <li>Have the shouldBeRescheduled() return true for as long as the run()
     * method has to be reexecuted</li>
     * </ul>
     */
    void run();


    /**
     * Checks to see if the class should be rescheduled on the WorkScheduler
     * once the run()-method has been completed.
     * <p>
     * The class may expect that the class will be re-added in the WorkScheduler
     * queue for as long as this method returns true.
     * 
     * @return true if and only if the class should be rescheduled on the
     * queue
     */
    boolean shouldBeRescheduled();


    /**
     * Returns the priority of this Work. Works with higher priority will be
     * executed before works with lower priority.
     * <p>
     * It's recommended that Works that are rescheduled over and over have a
     * lower priority than those tasks that are run only once to clear the
     * queues as quickly as possible. The end decision is of course up to the
     * importance of the thing itself.
     * 
     * @return The priority.
     */
    int getPriority();

}
