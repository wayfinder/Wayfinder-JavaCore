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
package com.wayfinder.core.wfserver.resource;

import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.wfserver.resource.internal.ImageResourceRequest;
import com.wayfinder.core.wfserver.resource.internal.OtherResourceRequest;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 * 
 *
 */
public abstract class ResourceRequest {
    
    /**
     * indicates that the request is made for some binary resource
     */
    public static final int RESOURCE_TYPE_OTHER = 0;
    
    /**
     * indicates that the request is made for an image
     */
    public static final int RESOURCE_TYPE_IMAGE = 1;
    
    /**
     * the name of the parent directory for caching resources
     */
    public static final String DIR_NAME_RESOURCES = "resource_cache";
    
    /**
     * the name of the directory for caching images
     */
    public static final String DIR_NAME_IMAGES = "images";
    
    /**
     * the name of the directory for caching other server resources
     */
    public static final String DIR_NAME_OTHER = "other";
    
    private final int m_reqType;
    private final String m_serverResDirPath;
    private final String m_resName;
    private final ResourceResponseListener m_responseListener;
    
    private RequestID m_requestID;
    
    /**
     * <p>
     * Create a request for a resource that should be available from the server.
     * </p>
     * <p>
     * <b>You don't need to supply the full URL of the resource</b>, just the 
     * relative directory path and the resource name. <br />
     * The full URL of the resource is obtained by appending the directory path 
     * (<b>which must have both a leading and a trailing'/'</b>) to the server 
     * URL and then also appending the resource name, the point being that the 
     * resource might have already been requested before and could therefore 
     * be cached on the local storage, and the resource name is used to find it
     * in storage.<br />
     * The server URL is defined inside the Core, you don't need to handle
     * it yourself. 
     * </p>
     * <p>
     * The resource binary data will be returned to the specified listener.
     * </p>
     * 
     * @param reqType the type of request, either {@link #RESOURCE_TYPE_IMAGE}
     * or {@link #RESOURCE_TYPE_OTHER}
     * @param serverResDirPath the dir path (on the server) of the requested 
     * resource. This is not an absolute path, but relative to the server URL.
     * Make sure that this contains both the leading and trailing "/".
     * @param resName the name of the requested resource.
     * @param responseListener the listener that will get either the binary
     * resource data or an error
     */
    public static ResourceRequest createResourceRequest(int reqType, String serverResDirPath,
            String resName, ResourceResponseListener responseListener) {
        if (reqType == RESOURCE_TYPE_IMAGE) {
            return new ImageResourceRequest(serverResDirPath, resName, responseListener);
        }
        else {
            return new OtherResourceRequest(serverResDirPath, resName, responseListener);
        }
    }
    
    protected ResourceRequest(int reqType, String serverResDirPath,
            String resName, ResourceResponseListener responseListener) {
        m_reqType = reqType;
        m_serverResDirPath = serverResDirPath;
        m_resName = resName;
        m_responseListener = responseListener;
    }

    public int getRequestType() {
        return m_reqType;
    }

    /**
     * 
     * @return the dir path (on the server) of the requested 
     * resource. This is not an absolute path, but relative to the server URL.
     * <b>It should contain both the leading and trailing "/"</b> so that the 
     * full URL will be correct.
     */
    public String getServerResourceDirPath() {
        return m_serverResDirPath;
    }

    /** 
     * @return the name of the resource (should be the file name, including
     * extension if that's the case)
     */
    public String getResourceName() {
        return m_resName;
    }

    /**
     * @return the listener that will get the binary data representing the 
     * resource or an error if something goes wrong
     */
    public ResourceResponseListener getResponseListener() {
        return m_responseListener;
    }
    
    /**
     * Set the request ID corresponding to this request
     * @param reqID
     */
    public void setRequestID(RequestID reqID) {
        m_requestID = reqID;
    }
    
    /** 
     * @return the request ID corresponding to this request
     */
    public RequestID getRequestID() {
        return m_requestID;
    }
    
    /** 
     * @return The path to the file where the resource should be located. The
     * path is relative to the base directory as obtained from
     * {@link PersistenceLayer#getBaseFileDirectory()}. <b>Doesn't contain the
     * leading "/"</b>.
     */
    public abstract String getDiskResourceFilePath();

    public int hashCode() {
        return 13;
    }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ResourceRequest))
            return false;
        ResourceRequest other = (ResourceRequest) obj;
        if (m_resName == null) {
            if (other.m_resName != null)
                return false;
        } else if (!m_resName.equals(other.m_resName))
            return false;
        if (m_responseListener != other.m_responseListener) {
            return false;
        }
        return true;
    }
    
    
}
