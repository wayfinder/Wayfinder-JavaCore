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
package com.wayfinder.core.wfserver.resource.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.ResponseCallback;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.wfserver.resource.ResourceRequest;
import com.wayfinder.pal.error.PermissionsException;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.WFFileConnection;

/**
 * 
 *
 */
public class ResourceRequestTask implements Runnable, ResponseCallback {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(ResourceRequestTask.class);
    
    private final ResourceRequest m_firstRequest;
    private final CallbackHandler m_cbHandler;
    private final InternalNetworkInterface m_netIfc;
    private final PersistenceLayer m_persistence;
    private final String m_fullFilePath;
    private final CachedResourceManagerImpl m_resMgr;
    
    private Vector m_requests;

    public ResourceRequestTask(
            ResourceRequest request, 
            CallbackHandler cbHandler, 
            InternalNetworkInterface netIfc,
            PersistenceLayer persistence,
            CachedResourceManagerImpl resMgr) {
        m_firstRequest = request;
        m_netIfc = netIfc;
        m_cbHandler = cbHandler;
        m_persistence = persistence;
        m_resMgr = resMgr;
        
        StringBuffer sb = new StringBuffer();
        sb.append(m_persistence.getBaseFileDirectory());
        if (!m_persistence.getBaseFileDirectory().endsWith("/")) {
            sb.append("/");
        }
        sb.append(request.getDiskResourceFilePath());
        m_fullFilePath = sb.toString();
        
        m_requests = new Vector();
        m_requests.addElement(m_firstRequest);
    }
    
    /**
     * Add a request to an existing task since it needs the same resource as
     * some other previous request that's waiting to be run. When the
     * request is done they will all notify their respective listeners.
     *  
     * @param request
     */
    public synchronized void addRequest(ResourceRequest request) {
        m_requests.addElement(request);
    }
    
    public String getRequestedResourceName() {
        return m_firstRequest.getResourceName();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        
        if (m_firstRequest != null) {
            boolean foundData = false;
            if (m_resMgr.isResourceCached(
                    m_firstRequest.getResourceName(), m_firstRequest.getRequestType())) {
                
                if(LOG.isInfo()) {
                    LOG.info("ResourceRequestTask.run()", 
                            "found in cached files "+m_firstRequest.getResourceName());
                }
                try {
                    WFFileConnection file = m_persistence.openFile(m_fullFilePath);
                    DataInputStream din = file.openDataInputStream();
                    byte[] resData = new byte[din.available()];
                    din.readFully(resData);
                    foundData = true;
                    notifySuccess(resData);
                } catch (IOException e) {
                    if(LOG.isError()) {
                        LOG.error("ResourceRequestTask.run()", e);
                    }
                    error(new UnexpectedError(
                            "IOException while reading "+m_fullFilePath, e));
                } catch (PermissionsException e) {
                    if(LOG.isError()) {
                        LOG.error("ResourceRequestTask.run()", e);
                    }
                    error(new UnexpectedError(
                            "PermissionsException while opening "+m_fullFilePath, e));
                }
            }
            
            if (!foundData) {
                //download
                StringBuffer sb = new StringBuffer();
                sb.append(m_firstRequest.getServerResourceDirPath());
                sb.append(m_firstRequest.getResourceName());
                if(LOG.isWarn()) {
                    LOG.warn("ResourceRequestTask.run()", "res URL: "+sb.toString());
                }
                m_netIfc.pendingGetRequest(
                        sb.toString(), this, InternalNetworkInterface.PRIORITY_LOW);
            }
        }
    }



    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.ResponseCallback#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(final CoreError error) {
        m_cbHandler.callInvokeCallbackRunnable(new Runnable() {
            
            public void run() {
                Enumeration e = m_requests.elements();
                while (e.hasMoreElements()) {
                    ResourceRequest resReq = (ResourceRequest) e.nextElement();
                    RequestID reqID = resReq.getRequestID();
                    resReq.getResponseListener().error(reqID, error);
                }
            }
        });
    }



    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.ResponseCallback#readResponse(java.io.InputStream, long)
     */
    public void readResponse(InputStream in, long length) throws IOException {
        final byte[] resourceData;
        if (length == -1) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int nextByte;
            while ( (nextByte = in.read()) != -1) {
                baos.write(nextByte);
            }
            resourceData = baos.toByteArray();
        }
        else {
            resourceData = new byte[(int)length];
            DataInputStream din = new DataInputStream(in);
            din.readFully(resourceData);
        }
        
        //notify the listener
        notifySuccess(resourceData);
        
        try {
            WFFileConnection file = m_persistence.openFile(m_fullFilePath);
            if(LOG.isDebug()) {
                LOG.debug("ResourceRequestTask.readResponse()", 
                        "writing to: "+m_fullFilePath);
            }
            file.openDataOutputStream().write(resourceData);
            file.close();
            m_resMgr.addResourceToCacheList(
                    m_firstRequest.getResourceName(), m_firstRequest.getRequestType());
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("ResourceRequestTask.readResponse()", e);
            }
            error(new UnexpectedError(
                    "PermissionsException while opening "+m_fullFilePath, e));
        }
    }
    
    private void notifySuccess(final byte[] resourceData) {
        m_cbHandler.callInvokeCallbackRunnable(new Runnable() {
            
            public void run() {
                Enumeration e = m_requests.elements();
                while (e.hasMoreElements()) {
                    ResourceRequest resReq = (ResourceRequest) e.nextElement();
                    resReq.getResponseListener().resourceAvailable(resReq.getRequestID(), resReq, resourceData);
                }
            }
        });
        
    }
}
