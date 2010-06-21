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

import java.io.IOException;
import java.util.Enumeration;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.userdata.UserListener;
import com.wayfinder.core.userdata.internal.hwkeys.HardwareKey;

class ActivateMC2Request implements MC2Request {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(ActivateMC2Request.class);

    private final RequestID m_reqID;
    private final UserModule m_module;
    private final UserListener m_listener;
    private final UserImpl m_user;
    private final CallbackHandler m_callbackHandler;
    
    ActivateMC2Request(RequestID reqid,
                       UserModule module, 
                       UserListener listener, 
                       UserImpl user,
                       CallbackHandler callHandler) {
        
        m_reqID = reqid;
        m_module = module;
        m_listener = listener;
        m_user = user;
        m_callbackHandler = callHandler;
    }
    
    
    public String getRequestElementName() {
        return "activate_request";
    }
    
    
    public void write(MC2Writer mc2w) throws IOException {
        Enumeration keyEnum = m_user.getHardwareKeys();
        while(keyEnum.hasMoreElements()) {
            HardwareKey key = (HardwareKey) keyEnum.nextElement();
            mc2w.startElement(MC2Strings.thardware_key);
            mc2w.attribute(MC2Strings.atype, key.getKeyXMLType());
            mc2w.text(key.getKey());
            mc2w.endElement();
        }
    }
    

    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        mc2p.nameOrError(MC2Strings.tactivate_reply);

        String uin = mc2p.attribute(MC2Strings.auin);
        if(LOG.isDebug()) {
            LOG.debug("ActivateMC2Request.parse()", "Found uin " + uin);
        }
        
        mc2p.children();

        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        
        m_module.setUIN(uin, m_listener);
    }

    
    public void error(final CoreError coreError) {
        if(LOG.isError()) {
            LOG.error("ActivateMC2Request.error()", 
                      coreError.getInternalMsg());
        }

        m_callbackHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(m_reqID, coreError);
            }
        });
    }
}
