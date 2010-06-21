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
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.SearchReply.MatchList;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.search.provider.ProviderSearchListener;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

class ProviderExpandSearchRequest extends SearchRequest {
    
    private static final Logger LOG = LogFactory
    .getLoggerForClass(ProviderExpandSearchRequest.class);

    private final ProviderSearchListener m_listener;
    private final MC2Interface m_mc2Ifc;
    private final int m_listIndex;
    private final int m_nbrOfMoreHits;
    private final ProviderSearchReplyImpl m_lastReply;
    private final SharedSystems m_systems;
    
    ProviderExpandSearchRequest(
            RequestID id,
            ProviderSearchReplyImpl reply,
            int listIndex,
            int nbrOfMoreHits,
            ProviderSearchListener listener,
            MC2Interface mc2Ifc,
            CallbackHandler handler,
            SharedSystems systems) {
        
        super(id, handler, listener);
        m_systems = systems;

        m_listIndex = listIndex;
        m_nbrOfMoreHits = nbrOfMoreHits;
        m_lastReply = reply;
        m_listener = listener;
        m_mc2Ifc = mc2Ifc;
    }


    void process(SearchDescriptor desc) {
        findUsedProviders();
        expand();
    }


    private void findUsedProviders() {
        final Provider prov = (Provider) m_lastReply.getProviderOfList(m_listIndex);
        final Provider[] providerArray = new Provider[] { prov };
        getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.usingExternalProviders(getRequestID(), providerArray);
            }
        });
    }


    private void expand() {
        final Provider prov = (Provider) m_lastReply.getProviderOfList(m_listIndex);
        final MatchList list = m_lastReply.getMatchList(m_listIndex);
        
        final int startIndex = list.getNbrOfRegularMatches() + list.getNbrOfTopMatches();
        final int endIndex = startIndex + m_nbrOfMoreHits;
        
        ProviderExpandSearchMC2Request req = new ProviderExpandSearchMC2Request(
                m_systems.getSettingsIfc().getGeneralSettings(),
                this, m_lastReply, prov, m_listIndex, startIndex, endIndex);
        m_mc2Ifc.pendingMC2Request(req);
    }


    void replyReceived(final ProviderSearchReplyImpl reply) {
        getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.searchDone(getRequestID(), reply);
            }
        });
    }
}
