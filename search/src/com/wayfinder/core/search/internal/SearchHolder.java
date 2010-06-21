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
package com.wayfinder.core.search.internal;

import java.util.Enumeration;
import java.util.Vector;

import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.SearchError;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.StateMachine;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceModule;

class SearchHolder extends StateMachine {
    
    private static final int STATE_READ_FROM_DISK    = 1;
    private static final int STATE_CHECK_WITH_SERVER = 2;
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(SearchHolder.class);
    
    private final MC2Interface m_mc2ifc;
    private final Vector m_outstandingRequests;
    private final SharedSystems m_systems;
    private SearchDescriptor m_descriptor;
    
    SearchHolder(SharedSystems systems, MC2Interface mc2Ifc) {
        m_systems = systems;
        m_mc2ifc = mc2Ifc;
        m_outstandingRequests = new Vector();
        m_descriptor = new SearchDescriptor("", new Provider[0], Integer.MIN_VALUE);
    }
    

    /**
     * Add a new search request to the queue.
     * 
     * @param request the new request to add.
     */
    synchronized void scheduleRequest(SearchRequest request) {
        m_outstandingRequests.addElement(request);
        super.advance();
    }
    
    
    /**
     * <p>Send all the queued search requests.</p>
     * 
     * <p>This must only be called by the state machine and only if the state
     * is that a valid search descriptor is available.</p> 
     */
    synchronized void sendRequests() {
        Enumeration e = m_outstandingRequests.elements();
        while (e.hasMoreElements()) {
            SearchRequest sr = (SearchRequest) e.nextElement();
            sr.process(m_descriptor);
        }
        m_outstandingRequests.removeAllElements();
    }
    
    
    synchronized void failRequests(int errorCode) {
        SearchError error = new SearchError("Error while searching", errorCode);
        Enumeration e = m_outstandingRequests.elements();
        while (e.hasMoreElements()) {
            SearchRequest sr = (SearchRequest) e.nextElement();
            sr.requestFailed(error);
        }
        m_outstandingRequests.removeAllElements();
    }
    
    
    synchronized void failRequests(CoreError error) {
        Enumeration e = m_outstandingRequests.elements();
        while (e.hasMoreElements()) {
            SearchRequest sr = (SearchRequest) e.nextElement();
            sr.requestFailed(error);
        }
        m_outstandingRequests.removeAllElements();
    }
    
    
    synchronized void setNewSearchDescriptor(SearchDescriptor sd, boolean save) {
        m_descriptor = sd;
        if(LOG.isDebug()) {
            LOG.debug("SearchHolder.setNewSearchDescriptor()", 
                    "Got new descriptor");
        }
        
        if(save) {
            SearchDescriptorPersistenceRequest req = new SearchDescriptorPersistenceRequest(this, m_descriptor);
            m_systems.getPersistentModule().pendingWritePersistenceRequest(req, PersistenceModule.SETTING_SEARCH_DATA);
        }
        if(m_descriptor.isValidForLanguage(m_systems.getSettingsIfc().getGeneralSettings().getLanguage())) {
            sendRequests();
        }
        super.readyToAdvance();
        super.advance();
    }
    
    
    synchronized void noDescriptorUpdateAvailable() {
        super.readyToAdvance();
        super.advance();
    }
    
    
    synchronized void descriptorUpdateError(CoreError error) {
        if(m_descriptor.isValid()) {
            // if the descriptor is ok, go ahead with any available searches
            // if the error was due to unactivated client etc the error will
            // be reported back in the search request.
            // if it's a temporary network error or similar, we can still go
            // with the old descriptor
            noDescriptorUpdateAvailable();
        } else {
            // we got nothing. At all.
            // fail all outstanding requests
            failRequests(error);
        }
    }
    

    //-------------------------------------------------------------------------
    // state machine
    
    
    protected synchronized int getNextState(int oldstate) { 
        int nextState;
        switch(oldstate) {
        case STATE_DORMANT:
            // zzz... eh, what?
            // check to see if the descriptor can be read from disk
            nextState = STATE_READ_FROM_DISK;
            break;
            
        case STATE_READ_FROM_DISK:
            nextState = STATE_CHECK_WITH_SERVER;
            break;
            
        case STATE_CHECK_WITH_SERVER:
            if(!m_descriptor.isValid()) {
                failRequests(SearchError.SEARCH_FAILED_NO_DESCRIPTOR);
                nextState = STATE_DORMANT;
            } else {
                nextState = STATE_FULLY_UPDATED;
            }
            break;
            
        case STATE_FULLY_UPDATED:
            if(!m_descriptor.isValidForLanguage(m_systems.getSettingsIfc().getGeneralSettings().getLanguage())) {
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
            SearchDescriptorPersistenceRequest req = new SearchDescriptorPersistenceRequest(this);
            m_systems.getPersistentModule().pendingReadPersistenceRequest(req, PersistenceModule.SETTING_SEARCH_DATA);
            break;
            
        case STATE_CHECK_WITH_SERVER:
            if(LOG.isDebug()) {
                if(m_descriptor.isValid()) {
                    LOG.debug("SearchHolder.handleNextState()", 
                    "Checking with server if new search descriptor is available");
                } else {
                    LOG.debug("SearchHolder.handleNextState()", 
                    "Downloading search descriptor from server");
                }
            }
            
            // schedule request
            m_mc2ifc.pendingMC2Request(new SearchDescriptorMC2Request(
                    m_systems.getSettingsIfc().getGeneralSettings(),
                    m_descriptor, this));
            break;
            
        case STATE_FULLY_UPDATED:
            sendRequests();
            readyToAdvance();
            break;
            
        default:
            if(LOG.isError()) {
                LOG.error("TopRegionHolder.advanceToState()", 
                        "Tried to advance to unknown state");
            }
        }
    }

}
