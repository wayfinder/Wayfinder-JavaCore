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
/*
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.positioning.internal.vfcellid.mc2;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.util.CharArray;
import com.wayfinder.pal.positioning.UpdatesHandler;

/**
 * 
 *
 */
public class CellIdMC2Request implements MC2Request {
    
    private final RequestID m_reqID;
    private final MC2WritableElement m_reqElement;
    private final CellIdMC2ReplyListener m_listener;
    
    public CellIdMC2Request(RequestID reqID, MC2WritableElement reqElement, CellIdMC2ReplyListener listener) {
        m_reqID = reqID;
        m_reqElement = reqElement;
        m_listener = listener;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(CoreError coreError) {
        m_listener.error(m_reqID, coreError);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#getRequestElementName()
     */
    public String getRequestElementName() {
        return MC2Strings.tcell_id_request;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        
        int radius = UpdatesHandler.VALUE_UNDEF;   //just in case
        int lat = 0, lon = 0;
        
        CharArray ca = mc2p.attributeCharArray(MC2Strings.aouter_radius);
        if (ca != null) {
            radius = ca.intValue();
        }
        
        mc2p.children();
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        
        if (mc2p.nameRefEq(MC2Strings.tposition_item)) {
            mc2p.children();
            do {
                if (mc2p.nameRefEq(MC2Strings.tlat)) {
                    lat = mc2p.valueAsInt();
                }
                else if (mc2p.nameRefEq(MC2Strings.tlon)) {
                    lon = mc2p.valueAsInt();
                }
            } while (mc2p.advance());
            
            mc2p.advance();
        }
        
        m_listener.cellIdReplyDone(m_reqID, new Position(lat, lon), radius);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        
        mc2w.attribute(MC2Strings.aposition_system, MC2Strings.MC2);
        m_reqElement.write(mc2w);
    }

}
