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

import java.io.IOException;

import com.wayfinder.core.network.NetworkError;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.Work;
import com.wayfinder.pal.error.PermissionsException;
import com.wayfinder.pal.network.http.HttpClient;
import com.wayfinder.pal.network.http.HttpEntity;
import com.wayfinder.pal.network.http.HttpHost;
import com.wayfinder.pal.network.http.HttpRequest;
import com.wayfinder.pal.network.http.HttpResponse;
import com.wayfinder.pal.network.http.StatusLine;

class RequestWork implements Work {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(RequestWork.class);
    
    private final HttpRequest m_request;
    private final ResponseCallback m_responseCallback;
    private final int m_priority;
    private final HttpClient httpClient;
    private final HttpHost m_httpHost;
    private final NetworkModule m_module;
    
    RequestWork(NetworkModule module,
                        HttpClient m_client,
                        HttpHost m_host,
                        HttpRequest request,
                        ResponseCallback responseCallback, 
                        int priority) {
        super();
        m_module = module;
        httpClient = m_client;
        m_httpHost = m_host;
        this.m_request = request;
        this.m_responseCallback = responseCallback;
        this.m_priority = priority;
    }

    public void run() {
        CoreError error = null;
        HttpEntity entity = null;
        try {
            HttpResponse response = httpClient.execute(m_httpHost, m_request);
            StatusLine statusline = response.getStatusLine(); 
            int code = statusline.getStatusCode();
            entity = response.getEntity();
            //TODO unzip unxor.... by wrapping the entity
            if (code == 200 && entity != null) {
                m_responseCallback.readResponse(
                        entity.getContent(), entity.getContentLength());
            } else {
                /*
                 * the toString() might not do what we want. Especially in
                 * Android PAL where com.wayfinder.pal.android.network.http.
                 * AndroidHttpResponse implements HttpResponse, HttpEntity,
                 * StatusLine.
                 */
                String errorStr = "NetworkError: HTTP status in reply not OK: ";
                errorStr += code;
                errorStr += " \"";
                errorStr += statusline.getReasonPhrase();
                errorStr += "\". Request: ";
                errorStr += m_request.getRequestMethod();
                errorStr += " ";
                errorStr += m_httpHost.toURI();
                errorStr += m_request.getRequestUri();
                
                error = new NetworkError(errorStr, NetworkError.REASON_NETWORK_FAILURE);
            }                
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("NetworkModule.RequestWork.run()", e);
            }
            error = new NetworkError( e, NetworkError.REASON_NETWORK_FAILURE);
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("NetworkModule.RequestWork.run()", e);
            }
            error = new NetworkError(e, NetworkError.REASON_BLOCKED_BY_PERMISSIONS);
        } catch (Throwable t) {
            if(LOG.isError()) {
                LOG.error("NetworkModule.RequestWork.run()", t);
            }
            error = new UnexpectedError("Error during networking", t);
        } finally {
            if (entity != null) {
                try {
                    //consume the input stream and than close it 
                    //release the underling connection
                    entity.finish();
                } catch (IOException e) {
                    //nothing to do
                    if(LOG.isError()) {
                        LOG.error("RequestWork.run()", "Finish the entity " + e.toString());
                    }
                }
            }
            //close all;
        }
        if (error != null) {
            m_responseCallback.error(error);
        } //else  will be better to notify here the success after the connection where closed
       
    }

    public int getPriority() {
        return m_priority;
    }

    public boolean shouldBeRescheduled() {
        //do retrying here if needed
        m_module.workerEnding();
        return false;
    }
}
