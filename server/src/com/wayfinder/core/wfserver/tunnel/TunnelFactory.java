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
package com.wayfinder.core.wfserver.tunnel;


/**
 * Used to create tunnel requests through the Wayfinder server
 */
public interface TunnelFactory {

    /**
     * Creates a GET tunneling request
     * 
     * 
     * @param url The url to get
     * @return A {@link TunnelRequest}
     * 
     * NOTE: The following parameters: language, uin, server cluster, 
     * client type&version will be added at the end of url 
     * e.g. ?l=en&uin=13234533&s=eu&c=jn-8-br-2&v=0.0.1
     */
    public TunnelRequest createGETQuery(String url);

    
    /**
     * Creates a POST tunneling request
     * 
     * <p>
     * <b>Attention! For reasons of memory consumption, it may be more efficient
     * to use the {@link #createPOSTQueryNoDataCopy(String, byte[])} method</b>
     * 
     * 
     * @param url The URL of the destination
     * @param postData The data to send as post
     * @return A {@link TunnelRequest} object
     * 
     * NOTE: The following parameters: language, uin, server cluster, 
     * client type&version will be added at the end of url 
     * e.g. ?l=en&uin=13234533&s=eu&c=jn-8-br-2&v=0.0.1
     */
    public TunnelRequest createPOSTQuery(String url, byte[] postData);

    
    /**
     * Creates a POST tunneling request
     * <p>
     * <b>Warning! This method will not make a copy of the array passed as
     * postData. While this is better for reasons of memory consumption, it may
     * cause unwanted effects if the array is modified after the request is
     * created!</b>
     * 
     * @param url The URL of the destination
     * @param postData The data to send as post
     * @return A {@link TunnelRequest} object
     * 
     * NOTE: The following parameters: language, uin, server cluster, 
     * client type&version will be added at the end of url 
     * e.g. ?l=en&uin=13234533&s=eu&c=jn-8-br-2&v=0.0.1
     */
    public TunnelRequest createPOSTQueryNoDataCopy(String url, byte[] postData);

}
