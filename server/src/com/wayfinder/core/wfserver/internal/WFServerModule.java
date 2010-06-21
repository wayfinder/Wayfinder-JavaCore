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
package com.wayfinder.core.wfserver.internal;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.ModuleData;
import com.wayfinder.core.ServerData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.InternalUser;
import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface;
import com.wayfinder.core.shared.util.URITool;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.wfserver.WFServerInterface;
import com.wayfinder.core.wfserver.info.UpgradeCheckListener;
import com.wayfinder.core.wfserver.info.internal.ClientUpgradeInfoImpl;
import com.wayfinder.core.wfserver.info.internal.ServerInfoMC2Request;
import com.wayfinder.core.wfserver.info.internal.UpgradeCheckResponseHandler;
import com.wayfinder.core.wfserver.tunnel.TunnelException;
import com.wayfinder.core.wfserver.tunnel.TunnelFactory;
import com.wayfinder.core.wfserver.tunnel.TunnelRequest;
import com.wayfinder.core.wfserver.tunnel.TunnelResponse;
import com.wayfinder.core.wfserver.tunnel.TunnelResponseListener;

public final class WFServerModule 
implements WFServerInterface, TunnelFactory {
    



    private static final Logger LOG = LogFactory
    .getLoggerForClass(WFServerModule.class);
    
    private final CallbackHandler m_callhandler;
    private final MC2Interface m_mc2Ifc;
    
    /** 
     * used for adding common params to server url; 
     */
    private final ServerData m_serverData;
    private final InternalSettingsInterface m_settingsIfc;
    private final InternalUserDataInterface m_usrDatIfc;


    private WFServerModule(CallbackHandler callhandler, 
                           InternalNetworkInterface netIfc, 
                           MC2Interface mc2Ifc,
                           ServerData serverData, 
                           InternalSettingsInterface settingsIfc,
                           InternalUserDataInterface usrDatIfc) {
        m_callhandler = callhandler;
        m_mc2Ifc = mc2Ifc;
        m_serverData = serverData;
        m_settingsIfc = settingsIfc;
        m_usrDatIfc = usrDatIfc;
    }
    
    
    public static WFServerInterface createWFServerInterface(
            ModuleData modData,
            InternalNetworkInterface netIfc,  // for POI download later
            MC2Interface mc2Ifc,
            SharedSystems systems,
            InternalUserDataInterface usrDatIfc) {
        
        return new WFServerModule(modData.getCallbackHandler(), netIfc, mc2Ifc,
                modData.getServerData(),
                systems.getSettingsIfc(),
                usrDatIfc);
    }
    
    
    //-------------------------------------------------------------------------
    // WFServerInterface ifc

    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.WFServerInterface#tunnel(com.wayfinder.core.wfserver.tunnel.TunnelRequest, com.wayfinder.core.wfserver.tunnel.TunnelResponseListener)
     */
    public RequestID tunnel(TunnelRequest request, TunnelResponseListener listener) {
        if(request instanceof TunnelRequestImpl) {
            TunnelRequestImpl queryImpl = (TunnelRequestImpl) request;
            if(LOG.isInfo()) {
                LOG.info("WFServerModule.tunnel()", queryImpl.getUrl());
            }
            RequestID reqID = RequestID.getNewRequestID();
            TunnelMC2Request mc2Req = new TunnelMC2Request(reqID, queryImpl, m_callhandler, listener);
            m_mc2Ifc.pendingMC2Request(mc2Req);
            return reqID;
        }
        throw new IllegalArgumentException("Foreign implementations of " +
        		"TunnelRequest are not allowed. Use TunnelFactory to create " +
        		"a request");
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.WFServerInterface#tunnelSynchronous(com.wayfinder.core.wfserver.tunnel.TunnelRequest)
     */
    public TunnelResponse tunnelSynchronous(TunnelRequest request) throws TunnelException {
        TunnelBlocker blocker = new TunnelBlocker();
        tunnel(request, blocker);
        return blocker.getResponse();
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.WFServerInterface#getTunnelFactory()
     */
    public TunnelFactory getTunnelFactory() {
        return this;
    }
    
    
    // add POI image retrieval here...
    
    
    //-------------------------------------------------------------------------
    // TunnelFactory ifc
    
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelFactory#createGETQuery(java.lang.String)
     */
    public TunnelRequest createGETQuery(String url) {
        if(ParameterValidator.isEmptyString(url)) {
            throw new IllegalArgumentException("Url cannot be empty");
        }
        return new TunnelRequestImpl(TunnelRequestImpl.TYPE_HTTP_GET, appendCommonParamsToUrl(url), null);
    }
    
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelFactory#createPOSTQuery(java.lang.String, byte[])
     */
    public TunnelRequest createPOSTQuery(String url, byte[] postData) {
        validateParameters(url, postData);
        byte[] newArray = new byte[postData.length];
        System.arraycopy(postData, 0, newArray, 0, postData.length);
        return new TunnelRequestImpl(TunnelRequestImpl.TYPE_HTTP_POST, appendCommonParamsToUrl(url), newArray);
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.tunnel.TunnelFactory#createPOSTQueryNoDataCopy(java.lang.String, byte[])
     */
    public TunnelRequest createPOSTQueryNoDataCopy(String url, byte[] postData) {
        validateParameters(url, postData);
        return new TunnelRequestImpl(TunnelRequestImpl.TYPE_HTTP_POST, appendCommonParamsToUrl(url), postData);
    }
    
    
    
    private static void validateParameters(String url, byte[] postData) {
        if(ParameterValidator.isEmptyString(url)) {
            throw new IllegalArgumentException("Url cannot be empty");
        } else if(postData == null) {
            throw new IllegalArgumentException("Post data cannot be empty");
        }
    }
    
    /**
     * Create a new url by adding client type and user identification as ending 
     * parameters to each request url 
     * @return the new url
     */
    //TODO: take care to not add params before fragments 
    //scheme ":" hier-part [ "?" query ] [ "#" fragment ]
    private String appendCommonParamsToUrl(String originalUrl) {
        StringBuffer buf = new StringBuffer(originalUrl);
        if (originalUrl.indexOf('?') > 0) {
            buf.append('&');
        } else {
            buf.append('?');
        }
        //language
        buf.append("l=");
        buf.append(m_settingsIfc.getGeneralSettings().getInternalLanguage().getWFURLCode());
        
        //uin
        InternalUser user = m_usrDatIfc.getInternalUser();
        if (user.isActivated()) {
            buf.append("&u=");
            URITool.percentEncodeString(user.getUIN(), buf);
        }
        
        //server cluster 
        //TODO: HARDCODED for the moment, check is this is still needed
        buf.append("&s=eu");
        
        //client type
        buf.append("&c="); 
        URITool.percentEncodeString(m_serverData.getClientType(), buf);
        
        //client version
        buf.append("&v=");
        URITool.percentEncodeString(m_serverData.getVersionNumber(), buf);
        
        return buf.toString();
    }


    public RequestID clientUpgradeCheck(final UpgradeCheckListener listener) {
        final RequestID requestID = RequestID.getNewRequestID();
        
//        * @param clientId a unique identifier of the installed application for 
//        * the specific platform 
//        * (e.g. for Android is full package name, for J2ME is MIDlet-Name)
//        * @param clientVersion the installed version name of the application 
//        * (PackageInfo.versionName for Android, MIDlet-Version for J2ME)


        MC2Request request = new ServerInfoMC2Request(
                new UpgradeCheckResponseHandler(requestID, listener, m_callhandler),
                m_serverData.getClientType(),
                m_serverData.getVersionNumber());
        
        m_mc2Ifc.pendingMC2Request(request, false);
        
        return requestID;
    }
}
