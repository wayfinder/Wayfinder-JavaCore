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

import java.io.IOException;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.search.SearchListener;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.search.provider.ProviderSearchListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.xml.XmlIterator;

class SearchPositionDescMC2Request implements MC2Request {

    private final RequestID m_reqID;
    private final ProviderSearchListener m_listener;
    private final SearchDescriptor m_desc;
    private final Position m_currentPosition;
    private final CallbackHandler m_callHandler;
    private final GeneralSettingsInternal m_settings;
    
    private final static boolean USE_TOPREGION_ID = false;
    
    SearchPositionDescMC2Request(GeneralSettingsInternal settings,
                                 RequestID reqID, 
                                 CallbackHandler callHandler, 
                                 ProviderSearchListener listener, 
                                 SearchDescriptor desc, 
                                 Position position) {
        
        m_settings = settings;
        m_reqID = reqID;
        m_callHandler = callHandler;
        m_listener = listener;
        m_desc = desc;
        m_currentPosition = position;
    }
    
    
    public String getRequestElementName() {
        return MC2Strings.tsearch_position_desc_request;
    }
    
    
    public void write(MC2Writer mc2w) throws IOException {
        mc2w.attribute(MC2Strings.alanguage, m_settings.getInternalLanguage().getXMLCode());
        m_currentPosition.write(mc2w);
    }
    

    public void parse(MC2Parser mc2p) 
            throws MC2ParserException, IOException {

        if(USE_TOPREGION_ID) {
            // cheat and only look at the top region id, then grab the rest from
            // the descriptor. Probably only marginally faster
            
            mc2p.children();
            
            //check and report status code 
            ServerError status = mc2p.getErrorIfExists();
            if (status != null) {
                error(status);
                return;
            }
            
            // try to find the top region and grab the rest from 
            if(mc2p.name() == MC2Strings.ttop_region) {
                mc2p.children();
                final int regionID = mc2p.valueAsInt();
                topRegionFound(regionID);
                return; // we are all done here
            }
            
            // top region was not sent, grab the first round 1 heading we see and
            // decide the top region from that
            while(mc2p.advance()) {
                if(mc2p.name() == MC2Strings.tsearch_hit_type) {
                    final int heading = mc2p.attributeAsInt(MC2Strings.aheading);
                    Provider prov = m_desc.getProviderWithHeading(heading);
                    if(prov != null) {
                        topRegionFound(prov.getTopRegionID());
                        return; // we are all done here
                    }
                }
            }
        } else {
            // parse all the providers
            
            final int nbrOfProviders = mc2p.attributeAsInt(MC2Strings.alength);
            mc2p.children();
            
            //check and report status code 
            ServerError status = mc2p.getErrorIfExists();
            if (status != null) {
                error(status);
                return;
            }
            
            Provider[] array = new Provider[nbrOfProviders];
            int i = 0;
            do {
                if(mc2p.nameRefEq(MC2Strings.tsearch_hit_type)) {
                    array[i++] = SearchDescriptorMC2Request.parseProvider(mc2p);
                }
            } while(mc2p.advance() && (i < array.length));
            sendProviderArray(array);
        }
    }
    
    
    private void topRegionFound(int regionID) {
        sendProviderArray(m_desc.getProvidersForRegion(regionID));
    }
    
    
    private void sendProviderArray(final Provider[] providerArray) {
        m_callHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.usingExternalProviders(m_reqID, providerArray);
            }
        });
    }
    
    
    public void error(CoreError coreError) {
        // an error in this request can probably be ignored since it's paired
        // with a search. If this request fails but the search works, no need
        // to bother the client...
        // m_listener.error(reqID, error)
    }
    
}
