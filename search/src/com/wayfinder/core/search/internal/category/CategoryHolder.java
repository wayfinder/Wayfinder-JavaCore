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
package com.wayfinder.core.search.internal.category;

import java.util.Vector;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryCollection;
import com.wayfinder.core.search.CategoryListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.internal.StateMachine;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceModule;
import com.wayfinder.core.shared.settings.Language;

public final class CategoryHolder extends StateMachine {
    
    private static final int STATE_READ_FROM_DISK    = 1;
    private static final int STATE_CREATE_DUMMY_DATA = 2;
    private static final int STATE_CHECK_WITH_SERVER = 3;
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(CategoryHolder.class);
    
    private final CallbackHandler m_callbackHandler;
    private final MC2Interface m_mc2ifc;
    private final Vector m_currentStructs;
    private final SharedSystems m_systems;
    private final int m_store;
    
    private Position m_currentPosition;
    private Position m_newPosition;
    private CategoryImpl[] m_currentCategories;
    private String m_crc;
    private int m_language;


    public CategoryHolder(int store, SharedSystems systems, CallbackHandler callHandler, MC2Interface mc2ifc) {
        m_store = store;
        m_systems = systems;
        m_callbackHandler = callHandler;
        m_crc = "";
        m_language = Integer.MIN_VALUE;
        m_currentPosition = new Position();
        m_newPosition = new Position();
        m_mc2ifc = mc2ifc;
        m_currentCategories = new CategoryImpl[0];
        m_currentStructs = new Vector();
    }
    
    
    synchronized void updateCategoryList(String crc, int langID, Position pos, CategoryImpl[] array, boolean shouldSave) {
        m_currentCategories = array;
        m_crc = crc;
        m_language = langID;
        m_currentPosition = pos;
        if(shouldSave) {
            if(LOG.isTrace()) {
                LOG.trace("CategoryHolder.updateCategoryList()", "Saving new list");
            }
            CategoryPersistenceRequest req = new CategoryPersistenceRequest(m_store, this, m_crc, m_language, pos, array);
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
    
    
    synchronized void error(final CoreError error) {
        for (int i = 0; i < m_currentStructs.size(); i++) {
            final LisStruct struct = (LisStruct) m_currentStructs.elementAt(i);
            m_callbackHandler.callInvokeCallbackRunnable(new Runnable() {
                public void run() {
                    struct.m_listener.error(struct.m_reqID, error);
                }
            });
        }
    }
    
    
    private synchronized boolean newLanguageDetected() {
        int currLangID = m_systems.getSettingsIfc().getGeneralSettings().getLanguage().getId();
        return (m_language != currLangID);
    }
    
    
    //-------------------------------------------------------------------------
    // listeners
    
    public synchronized void addListenerForUpdate(RequestID id, CategoryListener listener) {
        addListenerForUpdate(id, listener, m_currentPosition);
    }
    
    
    public synchronized void addListenerForUpdate(RequestID id, CategoryListener listener, Position position) {
        m_currentStructs.addElement(new LisStruct(id, listener));
        m_newPosition = position;
        if(getCurrentState() == STATE_FULLY_UPDATED) {
            updateListeners();
        }
        super.advance();
    }
    
    private static class LisStruct {
        private final RequestID m_reqID;
        private final CategoryListener m_listener;
        
        private LisStruct(RequestID id, CategoryListener listener) {
            m_reqID = id;
            m_listener = listener;
        }
    }
    
    
    private synchronized void updateListeners() {
        if(!newLanguageDetected()) {
            if(m_currentCategories.length > 0) {
                final CategoryCollection col = CategoryImpl.createCategoryCollection(m_currentCategories);
                for (int i = 0; i < m_currentStructs.size(); i++) {
                    final LisStruct struct = (LisStruct) m_currentStructs.elementAt(i);
                    m_callbackHandler.callInvokeCallbackRunnable(new CATUpdater(struct, col));
                }
            }
        }
    }
    
    
    private synchronized void clearListeners() {
        m_currentStructs.removeAllElements();
    }
    
    
    private static class CATUpdater implements Runnable {
        
        private final CategoryCollection m_collection;
        private final LisStruct m_struct;
        
        private CATUpdater(LisStruct struct, CategoryCollection col) {
            m_struct = struct;
            m_collection = col;
        }

        public void run() {
            m_struct.m_listener.searchCategoriesUpdated(m_struct.m_reqID, m_collection);
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
            if(m_currentCategories.length == 0) {
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
            //FIXME to avoid checking on every update, is 100 km good? :/
            if(m_currentPosition.distanceTo(m_newPosition) > 100000) {
                if(LOG.isDebug()) {
                    LOG.debug("CategoryHolder.getNextState()", 
                              "New position detected, rechecking with server");
                }
                
                nextState = STATE_CHECK_WITH_SERVER;
            } else if(newLanguageDetected()) {
                if(LOG.isDebug()) {
                    LOG.debug("CategoryHolder.getNextState()", 
                              "New language detected, rechecking with server");
                }
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
            CategoryPersistenceRequest req = new CategoryPersistenceRequest(m_store, this);
            m_systems.getPersistentModule().pendingReadPersistenceRequest(req, PersistenceModule.SETTING_SEARCH_DATA);
            break;
            
        case STATE_CREATE_DUMMY_DATA:
            updateCategoryList("", Language.EN, null, createDummyData(), false);
            break;
            
        case STATE_CHECK_WITH_SERVER:
            if(LOG.isDebug()) {
                if(!ParameterValidator.isEmptyString(m_crc)) {
                    LOG.debug("CategoryHolder.handleNextState()", 
                    "Checking with server if new category list is available");
                } else {
                    LOG.debug("CategoryHolder.handleNextState()", 
                    "Downloading list of categories from server");
                }
            }
            
            // schedule request
            m_currentPosition = m_newPosition;
            m_mc2ifc.pendingMC2Request(new CategoryListMC2Request(
                    this, m_newPosition, m_crc,
                    m_systems.getSettingsIfc().getGeneralSettings()));
            break;
            
        case STATE_FULLY_UPDATED:
            clearListeners();
            break;
            
        default:
            if(LOG.isError()) {
                LOG.error("CategoryHolder.advanceToState()", 
                        "Tried to advance to unknown state");
            }
        }
    }
    

    public static void assertIsInternalCategory(Category category) {
        if(!(category instanceof CategoryImpl)) {
            throw new IllegalArgumentException("Foreign implementations of Category are not allowed.");
        }
    }


    private static CategoryImpl[] createDummyData() {
        // correct IDs for MC2 HEAD 2009-01-14
        CategoryImpl[] array = new CategoryImpl[] {
                new CategoryImpl("Airport", "btat_airport", 18),
                new CategoryImpl("ATM", "btat_atm", 152),
                new CategoryImpl("Bank", "btat_bank", 151),
                new CategoryImpl("Cinema", "btat_cinema", 98),

                new CategoryImpl("Doctor", "btat_hospital", 248),
                new CategoryImpl("Ferry terminal", "btat_ferryterminal", 128),
                new CategoryImpl("Golf course", "btat_golfcourse", 48),
                new CategoryImpl("Hotel", "btat_hotel", 118),
                new CategoryImpl("Local rail", "btat_railwaystation", 122),
                new CategoryImpl("Museum", "btat_historicalmonument", 22),
                new CategoryImpl("Nightlife", "btat_nightlife", 6),
                new CategoryImpl("Open parking area", "btat_openparkingarea", 237),
                new CategoryImpl("Parking garage","btat_parkinggarage", 236),
                new CategoryImpl("Pharmacy", "btat_pharmacy", 245),
                new CategoryImpl("Petrol station", "btat_petrolstation", 103),
                new CategoryImpl("Police station", "btat_speedtrap", 108),
                new CategoryImpl("Post office", "btat_postoffice", 107),
                new CategoryImpl("Rent a car", "btat_carrepair", 15),
                new CategoryImpl("Restaurant", "btat_restaurant", 85),
                new CategoryImpl("Shopping", "btat_grocerystore", 59),
                new CategoryImpl("Ski resort", "btat_skiresort", 40),
                new CategoryImpl("Theatre", "btat_theatre", 88),
                new CategoryImpl("Tourist information", "btat_touristoffice", 19),
                
                new CategoryImpl("Vehicle repair facility", "btat_carrepair", 104)  
        };
        return array;
    }

}
