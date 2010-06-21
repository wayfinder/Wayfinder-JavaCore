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

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.search.internal.topregion.TopRegionImpl;
import com.wayfinder.core.search.provider.ProviderSearchListener;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

class ProviderSearchRequest extends SearchRequest {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(ProviderSearchRequest.class);
    
    private final SearchQuery m_query;
    private final ProviderSearchListener m_listener;
    private final MC2Interface m_mc2Ifc;
    private final SharedSystems m_systems;
    
    private SearchDescriptor m_desc;
    private int m_currentRound;
    private ProviderSearchReplyImpl m_lastReply;

    
    ProviderSearchRequest(RequestID id,
                          SearchQuery query, 
                          ProviderSearchListener listener,
                          MC2Interface mc2Ifc,
                          CallbackHandler handler,
                          SharedSystems systems) {
        
        super(id, handler, listener);
        
        m_query = query;
        m_systems = systems;
        m_desc = null;
        m_listener = listener;
        m_mc2Ifc = mc2Ifc;
        m_currentRound = -1;
    }
    
    
    void process(SearchDescriptor desc) {
        m_desc = desc;
        findUsedProviders();
        nextRound();
    }
    
    
    private void findUsedProviders() {
        switch(m_query.getQueryType()) {
        case SearchQuery.SEARCH_TYPE_POSITIONAL:
            m_mc2Ifc.pendingMC2Request(new SearchPositionDescMC2Request(
                    m_systems.getSettingsIfc().getGeneralSettings(),
                    getRequestID(), 
                    getCallbackHandler(), 
                    m_listener, 
                    m_desc, 
                    m_query.getPosition()), 
                    false, InternalNetworkInterface.PRIORITY_HIGH);
            break;
            
        case SearchQuery.SEARCH_TYPE_REGIONAL:
            // grab from descriptor
            // unchecked cast, ok since the SearchQuery object takes care of
            // the check during <init>
            TopRegionImpl impl = (TopRegionImpl) m_query.getTopRegion();
            final Provider[] providerArray = m_desc.getProvidersForRegion(impl.getRegionID());
            getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
                public void run() {
                    m_listener.usingExternalProviders(getRequestID(), providerArray);
                }
            });
            break;
            
        default:
            if(LOG.isError()) {
                LOG.error("ProviderSearchRequest.findUsedProviders()", 
                          "cannot determine providers for area search");
            }
        }
    }
    
    
    private void nextRound() {
        // need more rounds?
        if(m_currentRound < m_desc.getHighestRound()) {
            m_currentRound++;
            m_mc2Ifc.pendingMC2Request(
                    new ProviderSearchMC2Request(
                            m_systems.getSettingsIfc().getGeneralSettings(),
                            this, m_desc, m_query, m_lastReply, 0, 25, m_currentRound));
        }
    }
    
    
    void replyReceived(final ProviderSearchReplyImpl reply) {
        m_lastReply = reply;
        final boolean lastReply = (m_currentRound == m_desc.getHighestRound());
        final boolean containsMatches = m_lastReply.containsMatches();
        
        if(lastReply || containsMatches) {
            getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
                public void run() {
                    if(lastReply) {
                        m_listener.searchDone(getRequestID(), reply);
                    } else {
                        m_listener.searchUpdated(getRequestID(), reply);
                    }
                }
            });
        }
        nextRound();
    }
    
    
    void fail(final CoreError error) {
        getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(getRequestID(), error);
            }
        });
    }

}
