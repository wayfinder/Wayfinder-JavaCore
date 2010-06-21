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
package com.wayfinder.core.network.internal;

import com.wayfinder.core.network.NetworkInterface;

/**
 * Internal network interface for making server requests. All requests are 
 * executed asynchronous.
 * 
 * The requests will be queued and executed according to the priority.
 * Depending on the configuration multiple request can run in the same time, but
 * usually no more than 2 at once. 
 *   
 * 
 */
public interface InternalNetworkInterface extends NetworkInterface {
    
    
    public static final String DEFAULT_CONTENT_TYPE = "application/binary";
    
    public static final boolean XML_USE_XS_ENCODING = true;
    public static final String  XML_URI_PLAIN = "/xmlfile";
    public static final String  XML_URI_ENCODED = "/xsdata";
    
    /**
     * Actions that the very core of the application depends on may be given
     * this priority, though it is not recommended
     */
    public static final int PRIORITY_CRITICAL = Integer.MAX_VALUE;

    /**
     * User initiated actions should have this priority to ensure that they are
     * run as quickly as possible.
     */
    public static final int PRIORITY_HIGH   = Integer.MAX_VALUE / 2;

    /**
     * This is the normal priority
     */
    public static final int PRIORITY_NORMAL = 0;


    /**
     * Actions initiated by a background process (eg invisible to the user)
     * should have this priority.
     */
    public static final int PRIORITY_LOW = Integer.MIN_VALUE / 2;


    /**
     * Actions that are mundane or insignificant may be added with this
     * priority.
     */
    public static final int PRIORITY_MINIMAL = Integer.MIN_VALUE;
    
    /**
     * Post a request to the network, the method return immediately,
     * the methods from callback will be called from another thread.
     * 
     * The system will choose the server address and add the identification data
     * as parameters to the uri.
     * 
     * @param uri the relative uri with parameters 
     * @param responseCallback
     */
    void pendingGetRequest(String uri, ResponseCallback responseCallback);
    
    /**
     * Post a request to the network, the method return immediately,
     * the methods from callback will be called from another thread.
     *
     * The system will choose the server address and add the identification data
     * as parameters to the uri.
     * 
     * @param uri the relative uri with parameters
     * @param responseCallback
     * @param priority one of the PRIORITY constants 
     */
    void pendingGetRequest(String uri, ResponseCallback responseCallback, int priority);

    /**
     * Post a request to the network, the method return immediately,
     * the methods from callback will be called from another thread.
     *
     * The system will choose the server address and add the identification data
     * as parameters to the uri.
     *
     * @param uri the relative uri with parameters
     * @param responseCallback
     * @param postContent provide the content to be sent to the server 
     */
    void pendingPostRequest(String uri, ResponseCallback responseCallback, 
            PostContent postContent);
   
    
    /**
     * Post a request to the network, the method return immediately,
     * the methods from callback will be called from another thread.
     *
     * The system will choose the server address and add the identification data
     * as parameters to the uri.
     * 
     * @param uri the relative uri with parameters
     * @param responseCallback
     * @param postContent provide the content to be sent to the server 
     * @param priority one of the PRIORITY constants 
     */
    void pendingPostRequest(String uri, ResponseCallback responseCallback, 
            PostContent postContent, int priority);
    
 
    /**
     * Similar with 
     * {@link #pendingPostRequest(String, ResponseCallback, PostContent)}, but 
     * this will not change the uri.
     * 
     */
    void pendingPostRequestClean(String uri, ResponseCallback responseCallback, 
            PostContent postContent);
    /**
     * Similar with 
     * {@link #pendingPostRequest(String, ResponseCallback, PostContent, int),
     * but this will not change the uri.
     */
    void pendingPostRequestClean(String uri, ResponseCallback responseCallback, 
            PostContent postContent, int priority);
    
    /**
     * Use it with xml requests
     * @param responseCallback
     * @param postContent
     * @param priority
     */
    public void pendingXmlRequest(ResponseCallback responseCallback, 
            PostContent postContent, int priority); 
}
