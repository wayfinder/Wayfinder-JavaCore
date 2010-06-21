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
package com.wayfinder.core;


/**
 * Since many methods in the Core are asynchronous, listeners are used to
 * return responses via callbacks.
 * <p>
 * For the UI, there is much that can be gained if these callbacks are performed 
 * in the event dispatcher thread. Populating a view will be faster and easier 
 * to do since there is no need for the UI to reschedule such a task back onto 
 * the dispatcher.
 * <p>
 * However, there are also situations where the event dispatcher is not 
 * available or even bad to use due to demands on platforms or products. One 
 * example is if the Core will be used on a BlackBerry background task where 
 * there is no event dispatcher at all.
 * <p>
 * Because of this, the decision on how the callbacks will be made will be up to 
 * the application that wishes to use the Core. The Core will in turn guarantee 
 * that the provided method will be used for all callbacks to the UI.
 * <p>
 * When the Core is created by an application, the application must provide the
 * core with a class that implements this interface which will be used for the
 * callbacks.
 */
public interface CallbackHandler {

    
    /**
     * Places the provided runnable on queue for later execution.
     * <p>
     * <b>This method must not block or throw exceptions.</b>
     * 
     * @param r The {@link Runnable} to queue for later execution
     */
    public void callInvokeCallbackRunnable(Runnable r);

}
