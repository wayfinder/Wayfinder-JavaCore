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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.network.internal;

import java.util.Enumeration;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.ServerData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.shared.internal.InternalUser;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.Work;
import com.wayfinder.core.shared.internal.threadpool.WorkPriorityComparator;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.util.FibonacciHeap;
import com.wayfinder.core.shared.util.PriorityQueue;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.userdata.internal.hwkeys.HardwareKey;
import com.wayfinder.pal.network.http.HttpClient;
import com.wayfinder.pal.network.http.HttpEntity;
import com.wayfinder.pal.network.http.HttpEntityEnclosingRequest;
import com.wayfinder.pal.network.http.HttpHost;
import com.wayfinder.pal.network.http.HttpRequest;
import com.wayfinder.pal.util.UtilFactory;

/**
 * Implementation of server communication, use 
 * {@link com.wayfinder.core.shared.internal.threadpool.WorkScheduler} for 
 * executing request instead of own threads. The number of active Worker 
 * (thread of execution) is limited to 2 
 * 
 * @see #MAXIM_WORKER_COUNT
 *
 * 
 */
public class NetworkModule implements InternalNetworkInterface {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(NetworkModule.class);

    /*
     * pal networking
     */
    private final HttpClient httpClient;
    
    private final ServerData serverData;
    
    private final WorkScheduler workScheduler;
    
    private final UtilFactory utilFactory;
    
    /*
     * init in ctr. from server list
     */
    private HttpHost currentHost;
    

    /**
     * <p>The user agent header value for each http-request.</p>
     * 
     * <p>It's used for statistics.</p>
     * 
     * <p>Example <code>User-Agent: wf8-java-vf/8.8.4 (Sony-Ericsson/JP8_240x320; uin=1461896212)</code>
     */
    private final String userAgentHeaderValue;
    
    private final PriorityQueue requestQueue;
    
    /**
     * Maximum number of active Workers that executing the request,
     * this limit the number of concurrent threads allocated and also the number 
     * of concurrent server connection (which should not exceed 2 for the same 
     * host)
     */
    public final static int MAXIM_WORKER_COUNT = 2;
    
    private int workerCount = 0;
    private final InternalUserDataInterface m_usrDatIfc;
    
    private NetworkModule(ServerData serverData, HttpClient httpClient, 
            WorkScheduler workScheduler, InternalUserDataInterface usrDatIfc,
            UtilFactory utilFactory) {
        this.httpClient = httpClient;
        this.serverData = serverData;
        this.workScheduler = workScheduler;
        this.m_usrDatIfc = usrDatIfc;
        this.utilFactory = utilFactory;
        
        //determine the first host name & port
        String hostname = serverData.getDefaultHostNames()[0];
        int port = serverData.getDefaultPorts()[0];
        this.currentHost = httpClient.getHttpFactory().newHttpHost(hostname, port);
        
        //initializate the rest
        requestQueue = new FibonacciHeap(new WorkPriorityComparator());
        
        userAgentHeaderValue = serverData.getClientType() + "/" + serverData.getVersionNumber();
                                     
    }
 
    public static InternalNetworkInterface createNetworkInterface(ModuleData modData, 
            SharedSystems systems, InternalUserDataInterface usrDatIfc){
        ServerData serverData = modData.getServerData();
        HttpClient httpClient = systems.getPAL().getNetworkLayer().getHttpClient();
        WorkScheduler workScheduler = systems.getWorkScheduler();
        UtilFactory utilFactory = systems.getPAL().getUtilFactory();
        
        return new NetworkModule(serverData, httpClient, workScheduler, usrDatIfc, utilFactory);
    }
  
    //------------------- public & external ----------------------------------//
    public synchronized void shutdown() {
        requestQueue.clear();
        //cancel workers already added WorkScheduler
    }
    
    //------------------- internal -------------------------------------------// 
    public void pendingGetRequest(String uri, 
            ResponseCallback responseCallback) {
        pendingGetRequest(uri,responseCallback,PRIORITY_NORMAL);
    }

    public void pendingGetRequest(String uri,
            ResponseCallback responseCallback, int priority) {
        uri = appendIdentificationParamsToUri(uri);
        
        HttpRequest httpRequest = httpClient.getHttpFactory().newHttpGetRequest(uri);
        
        pendingRequestInternal(httpRequest,responseCallback, priority);
    }

    public void pendingPostRequest(String uri,
            ResponseCallback responseCallback, PostContent postContent) {
        
        uri = appendIdentificationParamsToUri(uri);
        if(LOG.isDebug()) {
            LOG.debug("NetworkModule.pendingPostRequest()", "uri " + uri + " post content " +  postContent);
        }
        
        pendingPostRequestClean(uri, responseCallback, postContent, PRIORITY_NORMAL);
    }

    public void pendingPostRequest(String uri,
            ResponseCallback responseCallback, PostContent postContent,
            int priority) {
        
        uri = appendIdentificationParamsToUri(uri);
        
        pendingPostRequestClean(uri, responseCallback, postContent, priority);
    }
    
    public void pendingPostRequestClean(String uri,
            ResponseCallback responseCallback, PostContent postContent) {
        pendingPostRequestClean(uri, responseCallback, postContent, PRIORITY_NORMAL);
    }

    public void pendingPostRequestClean(String uri,
            ResponseCallback responseCallback, PostContent postContent,
            int priority) {
        
        HttpEntityEnclosingRequest httpRequest = httpClient.getHttpFactory().newHttpPostRequest(uri);
        
        HttpEntity entity = new EntityPostContent(postContent,DEFAULT_CONTENT_TYPE,null);
        
        httpRequest.setEntity(entity);
        
        pendingRequestInternal(httpRequest,responseCallback, priority);
    }
    
    public void pendingXmlRequest(ResponseCallback responseCallback, 
            PostContent postContent, int priority) {
        String uri = XML_USE_XS_ENCODING?XML_URI_ENCODED:XML_URI_PLAIN;
        
        HttpEntityEnclosingRequest httpRequest = httpClient.getHttpFactory().newHttpPostRequest(uri);
        
        HttpEntity entity;
        if (XML_USE_XS_ENCODING) {
            entity = new XsGzipPostEntity(utilFactory, postContent);
            responseCallback = new XsGzipResponseWrapper(utilFactory, responseCallback);
        } else {
            entity = new EntityPostContent(postContent,DEFAULT_CONTENT_TYPE,null);
        }
        
        httpRequest.setEntity(entity);
        
        pendingRequestInternal(httpRequest,responseCallback, priority);
    }
    
    
    //--------------------- headers identification ---------------------------//
    /**
     * Create a new uri by adding client type and user identification as ending 
     * parameters to each request uri 
     * @return the new uri
     *
     */
    private String appendIdentificationParamsToUri(String originalUri) {
        //TODO: when having Authentication data add also user uin and hwd keys
        //uin is require in map for "Controlled POI" layer that contains 
        //pois that the user have payed for
        //e.g. "uin=663599701&hwd=354005020964871&hwdt=imei&c=bn-8-bb-br"
        
        //add the client type to have nice colors in map :P
        //check if there are any other parameters in the url
        String params = getIdentificationUriParams();
        if (originalUri.indexOf('?') >= 0) {
            return originalUri + "&" +  params;
        } else {
            return originalUri + "?" + params;
        }
    }
    
    
    private String getIdentificationUriParams() {
        InternalUser usr = m_usrDatIfc.getInternalUser();
        
        StringBuffer sb = new StringBuffer();
        if(usr.isActivated()) {
            sb.append("uin=");
            sb.append(usr.getUIN());
        }
        Enumeration e = usr.getHardwareKeys();
        while (e.hasMoreElements()) {
            HardwareKey key = (HardwareKey) e.nextElement();
            sb.append("&hwd=");
            sb.append(key.getKey());
            sb.append("&hwdt=");
            sb.append(key.getKeyXMLType());
        }
        sb.append("&c=");
        sb.append(serverData.getClientType());
        return sb.toString();
    }
    

    /**
     * <p>Sets request headers common for all requests.</p>
     * 
     * <p>The following headers are set:
     * <ol><li>User-Agent:  - see m_userAgentHeaderValue.</li>
     *     <li>Accept: * / * - our servers don't have a content-degradation
     *         mechanism and we want to avoid proxies trying to block
     *         content because of perceived client limitation.</li>
     * </ol></p>
     */
    private void setCommonHeaders(HttpRequest httpRequest) {
        /*
         * RFC 2068 (HTTP/1.1) says that header literals are case insensitive,
         * but we write as in spec for minimal surprise.
         * 
         *  Note that at least the 1.5r2 emulator then converts to lower case
         *  anyway.
         */
        httpRequest.setHeader("Accept", "*/*");
        httpRequest.setHeader("User-Agent", this.userAgentHeaderValue);
    }
    
    //-------------------- Heap ----------------------------------------------//
    /**
     * Sets the common headers to the request. Then, if there are free workers,
     * schedule the request on the WorkScheduler. Otherwise, add it to
     * requestQueue.
     * 
     * @param httpRequest - the HttpRequest to make ready for execution.
     */
    private void pendingRequestInternal(HttpRequest httpRequest,
            ResponseCallback responseCallback, int priority) {

        setCommonHeaders(httpRequest);
        Work work = new RequestWork(this, httpClient, currentHost, httpRequest, responseCallback, priority);
        
        synchronized (this) {
            if (workerCount < MAXIM_WORKER_COUNT) {
                workScheduler.schedule(work); 
                workerCount++;
            } else {
                requestQueue.insert(work);
            }
        }
    }
    
    /**
     * Signal that a worker has ended 
     * if the are more the next one will be added to the Scheduler
     */
    synchronized void workerEnding() {
        Work work = (Work) requestQueue.removeHighestPrio();
        if (work == null) {
            workerCount--;
        } else {
            workScheduler.schedule(work);    
        }
    }

}
