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
package com.wayfinder.core.userdata;

import com.wayfinder.core.shared.RequestID;

public interface UserDataInterface {

    
    /**
     * Sets a specific uin in the core.
     * 
     * @param uin the uin to set, may not be null
     * @param listener that will be called back when the 
     * {@link com.wayfinder.core.shared.User} is available
     */
    public void setUIN(String uin, UserListener listener);
    
    
    /**
     * Sets the UIN to "1374673870"
     * <p>
     * Attention! This UIN only exists on the server HEAD instance
     * (seld-devtest2).
     * 
     * @deprecated Will be removed when Android has service window in place
     */
    public void setHardcodedUser();

    
    /**
     * Asynchronous method to get the {@link com.wayfinder.core.shared.User} 
     * set in the Core.
     * 
     * @param listener that will be called back when the 
     * {@link com.wayfinder.core.shared.User} is available
     */
    public void getUser(UserListener listener);
    
    
    /**
     * Asks the server to create and return a new user account
     * 
     * @param listener that will be called back when the 
     * {@link com.wayfinder.core.shared.User} is available
     */
    public RequestID obtainUserFromServer(UserListener listener);
    
    
}
