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
package com.wayfinder.core.search.internal.topregion;

import java.util.Vector;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.TopRegionCollection;
import com.wayfinder.core.search.TopRegionListener;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.internal.StateMachine;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceModule;
import com.wayfinder.core.shared.settings.Language;

public final class TopRegionHolder extends StateMachine {
    
    private static final int STATE_READ_FROM_DISK    = 1;
    private static final int STATE_CREATE_DUMMY_DATA = 2;
    private static final int STATE_CHECK_WITH_SERVER = 3;
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TopRegionHolder.class);
    
    
    private final CallbackHandler m_callbackHandler;
    private final MC2Interface m_mc2ifc;
    private final Vector m_currentStructs;
    private final SharedSystems m_systems;
    private TopRegionImpl[] m_currentRegions;
    private String m_crc;
    private int m_languageID;
    
    public TopRegionHolder(SharedSystems systems, CallbackHandler callHandler, MC2Interface mc2ifc) {
        m_systems = systems;
        m_callbackHandler = callHandler;
        m_crc = "";
        m_languageID = Integer.MIN_VALUE;
        m_mc2ifc = mc2ifc;
        m_currentRegions = new TopRegionImpl[0];
        m_currentStructs = new Vector();
    }
    
    
    synchronized void updateTopRegionList(TopRegionImpl[] array, String crc, int langID, boolean shouldSave) {
        m_currentRegions = array;
        m_crc = crc;
        m_languageID = langID;
        if(shouldSave) {
            if(LOG.isTrace()) {
                LOG.trace("TopRegionHolder.updateTopRegionList()", "Saving new list");
            }
            TopRegionPersistenceRequest req = new TopRegionPersistenceRequest(this, m_crc, m_languageID, m_currentRegions);
            m_systems.getPersistentModule().pendingWritePersistenceRequest(req, PersistenceModule.SETTING_SEARCH_DATA);
        }
        updateListeners();
        super.readyToAdvance();
        super.advance();
    }
    
    
    synchronized void noUpdateAvailable() {
        super.readyToAdvance();
        super.advance();
    }
    
    
    private synchronized boolean newLanguageDetected() {
        int currLangID = m_systems.getSettingsIfc().getGeneralSettings().getLanguage().getId();
        return (m_languageID != currLangID);
    }
    
    
    //-------------------------------------------------------------------------
    // listeners
    
    
    public synchronized void addListenerForUpdate(RequestID id, TopRegionListener listener) {
        m_currentStructs.addElement(new LisStruct(id, listener));
        if(getCurrentState() == STATE_FULLY_UPDATED) {
            updateListeners();
        }
        super.advance();
    }


    private synchronized void updateListeners() {
        if(!newLanguageDetected()) {
            final TopRegionCollection col = TopRegionImpl.createTopRegionCollection(m_currentRegions);
            for (int i = 0; i < m_currentStructs.size(); i++) {
                final LisStruct struct = (LisStruct) m_currentStructs.elementAt(i);
                m_callbackHandler.callInvokeCallbackRunnable(new TRUpdater(struct, col));
            }
        }
    }
    
    
    private static class LisStruct {
        private final RequestID m_reqID;
        private final TopRegionListener m_listener;
        
        private LisStruct(RequestID id, TopRegionListener listener) {
            m_reqID = id;
            m_listener = listener;
        }
    }

    
    private synchronized void clearListeners() {
        m_currentStructs.removeAllElements();
    }
    
    
    private static class TRUpdater implements Runnable {
        
        private final TopRegionCollection m_collection;
        private final LisStruct m_struct;
        
        private TRUpdater(LisStruct struct, TopRegionCollection col) {
            m_struct = struct;
            m_collection = col;
        }

        public void run() {
            m_struct.m_listener.topregionsUpdated(m_struct.m_reqID, m_collection);
        }
    }
    
    
    
    //-------------------------------------------------------------------------
    // state machine
    
    
    protected synchronized int getNextState(int oldstate) {
        int nextState;
        switch(oldstate) {
        case STATE_DORMANT:
            // zzz... eh, what?
            // check to see if the categories can be read from disk
            nextState = STATE_READ_FROM_DISK;
            break;
            
        case STATE_READ_FROM_DISK:
            // did we get any data? if not create dummy
            if(m_currentRegions.length == 0) {
                nextState = STATE_CREATE_DUMMY_DATA;
            } else {
                nextState = STATE_CHECK_WITH_SERVER;
            }
            break;
            
        case STATE_CREATE_DUMMY_DATA:
            nextState = STATE_CHECK_WITH_SERVER;
            break;
            
        case STATE_CHECK_WITH_SERVER:
        case STATE_FULLY_UPDATED:
            if(newLanguageDetected()) {
                nextState = STATE_CHECK_WITH_SERVER;
            } else {
                nextState = STATE_FULLY_UPDATED;
            }
            break;
            
        default:
            if(LOG.isError()) {
                LOG.error("CategoryHolder.getNextState()", 
                        "Unable to determine next state");
            }
            nextState = oldstate;
        }
        
        return nextState;
    }
    
    
    protected synchronized void handleNextState(int state) {
        switch(state) {
        case STATE_READ_FROM_DISK:
            TopRegionPersistenceRequest req = new TopRegionPersistenceRequest(this);
            m_systems.getPersistentModule().pendingReadPersistenceRequest(req, PersistenceModule.SETTING_SEARCH_DATA);
            break;
            
        case STATE_CREATE_DUMMY_DATA:
            updateTopRegionList(createDummyData(), "", Language.EN, false);
            break;
            
        case STATE_CHECK_WITH_SERVER:
            if(LOG.isDebug()) {
                if(!ParameterValidator.isEmptyString(m_crc)) {
                    LOG.debug("TopRegionHolder.handleNextState()", 
                    "Checking with server if new top region list is available");
                } else {
                    LOG.debug("TopRegionHolder.handleNextState()", 
                    "Downloading list of top regions from server");
                }
            }
            
            // schedule request
            m_mc2ifc.pendingMC2Request(new TopRegionMC2Request(
                    this, m_crc,
                    m_systems.getSettingsIfc().getGeneralSettings()));
            break;
            
        case STATE_FULLY_UPDATED:
            clearListeners();
            break;
            
        default:
            if(LOG.isError()) {
                LOG.error("TopRegionHolder.advanceToState()", 
                        "Tried to advance to unknown state");
            }
        }
    }

    

    public static void assertIsInternalTopRegion(TopRegion topRegion) {
        if(!(topRegion instanceof TopRegionImpl)) {
            throw new IllegalArgumentException("Foreign implementations of TopRegion are not allowed.");
        }
    }


    private static TopRegionImpl[] createDummyData() {
        TopRegionImpl[] array = new TopRegionImpl[] {
                new TopRegionImpl("Andorra", TopRegionImpl.TYPE_COUNTRY, 68),
                new TopRegionImpl("Austria", TopRegionImpl.TYPE_COUNTRY, 63),
                new TopRegionImpl("Belgium", TopRegionImpl.TYPE_COUNTRY, 6),
                new TopRegionImpl("Denmark", TopRegionImpl.TYPE_COUNTRY, 3),
                new TopRegionImpl("Finland", TopRegionImpl.TYPE_COUNTRY, 4),
                new TopRegionImpl("France", TopRegionImpl.TYPE_COUNTRY, 64),
                new TopRegionImpl("Germany", TopRegionImpl.TYPE_COUNTRY, 2),
                new TopRegionImpl("Greece", TopRegionImpl.TYPE_COUNTRY, 77),
                new TopRegionImpl("Hungary", TopRegionImpl.TYPE_COUNTRY, 74),
                new TopRegionImpl("Ireland", TopRegionImpl.TYPE_COUNTRY, 61),
                new TopRegionImpl("Italy", TopRegionImpl.TYPE_COUNTRY, 67),
                new TopRegionImpl("Liechtenstein", TopRegionImpl.TYPE_COUNTRY, 66),
                new TopRegionImpl("Luxembourg", TopRegionImpl.TYPE_COUNTRY, 8),
                new TopRegionImpl("Monaco", TopRegionImpl.TYPE_COUNTRY, 69),
                new TopRegionImpl("Netherlands", TopRegionImpl.TYPE_COUNTRY, 7),
                new TopRegionImpl("Norway", TopRegionImpl.TYPE_COUNTRY, 5),
                new TopRegionImpl("Portugal", TopRegionImpl.TYPE_COUNTRY, 70),
                new TopRegionImpl("Spain", TopRegionImpl.TYPE_COUNTRY, 65),
                new TopRegionImpl("Sweden", TopRegionImpl.TYPE_COUNTRY, 1),
                new TopRegionImpl("Switzerland", TopRegionImpl.TYPE_COUNTRY, 62),
                new TopRegionImpl("United Kingdom", TopRegionImpl.TYPE_COUNTRY, 0),
                
        };
        return array;
    }

}
