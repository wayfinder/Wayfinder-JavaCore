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
package com.wayfinder.core.userdata.internal;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.User;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.InternalUser;
import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.userdata.UserListener;

public final class UserModule implements InternalUserDataInterface {

    private static final Logger LOG = LogFactory.getLoggerForClass(UserModule.class);
    
    public static InternalUserDataInterface createUserModule(ModuleData modData, SharedSystems systems) {
        return new UserModule(modData, systems);
    }
    
    private final ModuleData m_modData;
    private final SharedSystems m_systems;
    private final UserDataStore m_store;
    
    
    private UserModule(ModuleData modData, SharedSystems systems) {
        m_modData = modData;
        m_systems = systems;
        m_store = new UserDataStore(
                modData.getPAL().getHardwareInfo(), 
                systems.getPersistentModule());
    }
    
    
    //-------------------------------------------------------------------------
    // UserDataInterface ifc
    

    public void setHardcodedUser() {
        m_systems.getWorkScheduler().schedule(new Runnable() {
            public void run() {
                m_store.set_HARDCODED_UIN_Internal();
            }
        });
    }
    
    
    public void setUIN(final String uin, final UserListener listener) {
        if(ParameterValidator.isEmptyString(uin)) {
            throw new IllegalArgumentException("UIN cannot be empty");
        }
        
        m_systems.getWorkScheduler().schedule(new Runnable() {
            public void run() {
                User usr;
                try {
                    usr = m_store.setUIN(uin, true, true);
                    if(listener != null) {
                        m_modData.getCallbackHandler().callInvokeCallbackRunnable(
                                new UserReporter(usr, listener));
                    }
                } catch (final Throwable t) {
                    if(listener != null) {
                        m_modData.getCallbackHandler().callInvokeCallbackRunnable(
                                new Runnable() {
                                    public void run() {
                                        listener.error(RequestID.getNewRequestID(), 
                                                new UnexpectedError(t.toString(),t));
                                        
                                    }
                                });
                    }
                }
            }
        });
    }
    
    
    public void getUser(final UserListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("UserListener cannot be null");
        }
        //FIXME: don't create a runnable if we have already the user
        //or if there is already a request running.
        m_systems.getWorkScheduler().schedule(new Runnable() {
            public void run() {
                if(LOG.isInfo()) {
                    LOG.info("UserModule.getUser","runnable started");
                }
                try {
                    User usr = m_store.getUser();
                    m_modData.getCallbackHandler().callInvokeCallbackRunnable(
                            new UserReporter(usr, listener));
                } catch (final Throwable t) {
                    //report the errors
                    if(LOG.isError()) {
                        LOG.error("UserModule.getUser",t);
                    }
                    m_modData.getCallbackHandler().callInvokeCallbackRunnable(
                            new Runnable() {
                                public void run() {
                                    listener.error(RequestID.getNewRequestID(),
                                            new UnexpectedError(t.toString(),t));
                                }
                            });
                } 
            }
        });
    }
    
    private static final class UserReporter implements Runnable {
        
        private final User m_user;
        private final UserListener m_listener;
        
        public UserReporter(User usr, UserListener listener) {
            m_user = usr;
            m_listener = listener;
        }
        
        public void run() {
            m_listener.currentUser(m_user);
        }
    }
    
    
    public RequestID obtainUserFromServer(final UserListener listener) {
        final RequestID reqID = RequestID.getNewRequestID();
        Runnable r = new Runnable() {
            public void run() {
                UserImpl user = m_store.getUser();
                ActivateMC2Request req 
                                    = new ActivateMC2Request(
                                             reqID,
                                             UserModule.this,
                                             listener,
                                             user,
                                             m_modData.getCallbackHandler());
                m_systems.getMc2Ifc().pendingMC2Request(req, false);
            }
        };
        m_systems.getWorkScheduler().schedule(r);
        return reqID;
    }
    
    
    //-------------------------------------------------------------------------
    // InternalUserDataInterface ifc
    
    
    /**
     * This method is blocking
     */
    public InternalUser getInternalUser() {
        return m_store.getUser();
    }

}
