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

import java.io.IOException;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.search.SearchError;
import com.wayfinder.core.search.TopRegionListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.xml.XmlIterator;

public final class CurrentTopRegionMC2Request implements MC2Request {

    private final RequestID m_reqID;
    private final Position m_currentPosition;
    private final TopRegionListener m_listener;
    private final CallbackHandler m_callHandler;
    private final GeneralSettingsInternal m_settings;
    
    public CurrentTopRegionMC2Request(
            RequestID reqID,
            Position position, 
            CallbackHandler handler, 
            TopRegionListener listener,
            GeneralSettingsInternal settings) {
        
        m_reqID = reqID;
        m_settings = settings;
        m_currentPosition = position;
        m_callHandler = handler;
        m_listener = listener;
    }
    
    
    public String getRequestElementName() {
        return MC2Strings.tsearch_position_desc_request;
    }
    
    
    public void write(MC2Writer mc2w) throws IOException {
        mc2w.attribute(MC2Strings.alanguage, m_settings.getInternalLanguage().getXMLCode());
        m_currentPosition.write(mc2w);
    }
    
    
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        mc2p.children();
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        
        do {
            // try to find the top region and grab the rest from 
            if(mc2p.nameRefEq(MC2Strings.ttop_region)) {
                mc2p.children();
                final TopRegionImpl region = TopRegionMC2Request.parseTopRegion(mc2p);
                m_callHandler.callInvokeCallbackRunnable(new Runnable() {
                    public void run() {
                        m_listener.currentTopRegion(m_reqID, m_currentPosition, region);
                    }
                });
                return;
            }
        } while(mc2p.advance());
        error(new SearchError("Could not determine top region for position", 
                              SearchError.TOPREGION_REQUEST_FAILED)); 
    }
    
    
    public void error(final CoreError coreError) {
        m_callHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(m_reqID, coreError);
            }
        });
    }
    
    
}
