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
package com.wayfinder.core.wfserver.info.internal;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2RequestAdapter;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.wfserver.info.ClientUpgradeInfo;

/**
 * Represent a xml request for checking if there is a new client version
 * available 
 * 
 * 
 */
public class ServerInfoMC2Request extends MC2RequestAdapter {
    
    private final String m_currentVersion;
    private final String m_clientType;
    
    /**
     * @param listener the {@link MC2RequestListener} that will be notified when 
     * request is finish; cannot be null
     * 
     * @param clientType the server client type of the application as known by
     * server, should be unique for each application name/package; 
     * cannot be null   
     *   
     * @param currentVersion the client version of the application as 
     * known by the server should be the same with installed version name of 
     * the application (PackageInfo.versionName for Android, 
     * MIDlet-Version for J2ME); cannot be null
     * 
     * @throws IllegalArgumentException if any of the parameters are null
     */
    public ServerInfoMC2Request(MC2RequestListener listener, String clientType,
            String currentVersion) {
        super(listener);
        if (clientType == null || currentVersion == null) {
            throw new  IllegalArgumentException("One of the ctr. param is null");
        }
        this.m_currentVersion = currentVersion;
        this.m_clientType = clientType;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#getRequestElementName()
     */
    public String getRequestElementName() {
        return MC2Strings.tserver_info_request;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        // <!ELEMENT server_info_reply ( client_type_info |
        // ( status_code, status_message,
        // status_uri? ) ) >
        // <!ATTLIST server_info_reply transaction_id ID #REQUIRED >
        // <!ELEMENT client_type_info EMPTY>
        // <!ATTLIST client_type_info upgrade_available %bool; #REGUIRED
        // latest_version CDATA #REQUIRED
        // force_upgrade %bool: #REQUIRED
        // upgrade_id CDATA #IMPLIED >
        
        mc2p.nameOrError(MC2Strings.tserver_info_reply);
        
        mc2p.childrenOrError();
        
        ServerError error = mc2p.getErrorIfExists();
        
        if (error != null) {
            error(error);
        } else {
            mc2p.nameOrError(MC2Strings.tclient_type_info);
            String latestVersion = mc2p.attribute(MC2Strings.alatest_version);
            if (latestVersion != null && latestVersion.length() > 0) {
                boolean upgradeAvailable = mc2p.attributeAsBoolean(MC2Strings.aupgrade_available);
                boolean forceUpgrade = mc2p.attributeAsBoolean(MC2Strings.aforce_upgrade);
                String upgradeUri = mc2p.attribute(MC2Strings.aupgrade_id);

                ClientUpgradeInfo info = new ClientUpgradeInfoImpl(
                        upgradeAvailable, forceUpgrade, latestVersion, upgradeUri);
                result(info);
            } else {
                // else client was not found
                result(null);
            }
            mc2p.advance();
        }
        
        mc2p.nameOrError(MC2Strings.tserver_info_reply);
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        // <!ELEMENT server_info_request EMPTY>
        // <!ATTLIST server_info_request transaction_id ID #REQUIRED
        // client_type CDATA #REQUIRED
        // client_type_options CDATA #IMPLIED
        // client_version CDATA #REQUIRED >
        mc2w.attribute(MC2Strings.aclient_type, m_clientType);
        mc2w.attribute(MC2Strings.aclient_version, m_currentVersion);
    }
}
