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

import com.wayfinder.core.shared.internal.StateMachine;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceModule;
import com.wayfinder.core.userdata.internal.hwkeys.HardwareKeyContainer;
import com.wayfinder.core.userdata.internal.hwkeys.KeyCollector;
import com.wayfinder.pal.hardwareinfo.HardwareInfo;

final class UserDataStore extends StateMachine {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(UserDataStore.class);
    
    private static final int STATE_READ_HWKEYS       = 1;
    private static final int STATE_READ_FROM_DISK    = 2;
    
    private final HardwareInfo m_hardInfo;
    private final PersistenceModule m_persModule;
    
    private UserImpl m_currentUser;
    private HardwareKeyContainer m_hwkeycont;
    
    UserDataStore(HardwareInfo info, PersistenceModule persMod) {
        m_hardInfo = info;
        m_persModule = persMod;
    }
    
    

    //-------------------------------------------------------------------------
    // internal workings - all methods synchronous
    
    
    synchronized UserImpl setUIN(String uin, boolean override, boolean save) throws InterruptedException {
        //get the current user before trying to use it
        //that will collect the hardware keys and try to read the current user
        //avoid NPE for m_currentUser and m_hwkeycont
        UserImpl oldUser = getUser();
        
        if(override || !oldUser.isActivated()) { 
            if(LOG.isDebug()) {
                LOG.debug("UserDataStore.setUIN()", "New UIN is " + uin);
            }
            m_currentUser = new UserImpl(uin, m_hwkeycont);
            if(save) {
                if(LOG.isTrace()) {
                    LOG.trace("UserDataStore.setUIN()", 
                              "Storing UIN on disk");
                }
                UserDataPersistenceRequest req = new UserDataPersistenceRequest(this, m_currentUser);
                m_persModule.pendingWritePersistenceRequest(req, PersistenceModule.SETTING_USER_DATA);
            }
            readyToAdvance();
        } else {
            if(LOG.isWarn()) {
                LOG.warn("UserDataStore.setUIN()",
                         "Core already activated, ignoring new UIN");
            }
        }
        return getUser();
    }
    
    
    synchronized void noUINonDisk() {
        if(LOG.isWarn()) {
            LOG.warn("UserDataStore.noUINonDisk()", 
                     "No UIN available on disk, core is not activated");
        }
        
        m_currentUser = new UserImpl(m_hwkeycont);
        readyToAdvance();
        advance();
    }

    synchronized void setUINfromDisk(String uin) {
        if(LOG.isInfo()) {
            LOG.info("UserDataStore.setUIFromDisk()", 
                     "UIN available on disk, core is activated");
        }
        
        m_currentUser = new UserImpl(uin, m_hwkeycont);
        readyToAdvance();
        advance();
    }    
    
    synchronized UserImpl set_HARDCODED_UIN_Internal() {
        m_hwkeycont = KeyCollector.createHardcodedHWKeyContainerWithIMEI("hardcodedimei");
        
        String uin = "[SET ME]"; 
        // devtest cluster user for this faked IMEI
        m_currentUser = new UserImpl(uin, m_hwkeycont);
        // wf-eu cluster user for this faked IMEI
        // m_currentUser = new UserImpl("[SET ME]", m_hwkeycont);
        
        if(LOG.isWarn()) {
            LOG.warn("UserDataStore.set_HARDCODED_UIN_Internal()", 
                     "HARDCODED UIN:" + uin +" HWKey:"  + m_hwkeycont + 
                     " now set in Core");
        }
        
        return m_currentUser;
    }
    
    synchronized UserImpl getUser() {
        advance();
        try {
            waitUntilReady();
        } catch (InterruptedException ex) {
            if(LOG.isError()) {
                LOG.error("UserDataStore.getUser()", ex.toString());
            }
            //hack to avoid throwing InterruptedException
            //because we called advance and hardware key are not collected in a 
            //different thread those will not be null 
            return new UserImpl(m_hwkeycont);
        }
        return m_currentUser;
    }
    
    //-------------------------------------------------------------------------
    // state
    
    
    protected synchronized int getNextState(int lastState) {
        int nextState;
        if(m_hwkeycont == null) {
            nextState = STATE_READ_HWKEYS;
        } else if(m_currentUser == null){
            nextState = STATE_READ_FROM_DISK;
        } else {
            nextState = STATE_FULLY_UPDATED;
        }
        return nextState;
    }
    
    
    
    protected synchronized void handleNextState(int state) {
        switch(state) {
        case STATE_READ_HWKEYS:
            if(LOG.isTrace()) {
                LOG.trace("UserDataStore.handleNextState()", 
                          "Reading hardware keys");
            }
            KeyCollector keyCol = new KeyCollector(m_hardInfo);
            m_hwkeycont = keyCol.grabAllHardwareKeys();
            super.readyToAdvance();
            super.advance();
            break;
            
        case STATE_READ_FROM_DISK:
            if(LOG.isTrace()) {
                LOG.trace("UserDataStore.handleNextState()", 
                          "Checking disk for stored user data");
            }
            UserDataPersistenceRequest req = new UserDataPersistenceRequest(this);
            m_persModule.pendingReadPersistenceRequest(req, PersistenceModule.SETTING_USER_DATA);
            break;
            
        case STATE_FULLY_UPDATED:
            if(LOG.isTrace()) {
                LOG.trace("UserDataStore.handleNextState()", 
                          "Fully updated");
            }
            notifyAll();
            break;
        
        default:
            if(LOG.isError()) {
                LOG.error("UserDataStore.handleNextState()", 
                          "Cannot handle unknown state!");
            }
        }
    }
    
    private synchronized void waitUntilReady() throws InterruptedException {
        while(super.getCurrentState() != STATE_FULLY_UPDATED) {
            if(LOG.isDebug()) {
                LOG.debug("UserDataStore.waitUntilReady()", String.valueOf(super.getCurrentState()));
            }
            wait();
        }
    }
    
}
