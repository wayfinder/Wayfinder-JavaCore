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
import java.io.InputStream;
import java.io.OutputStream;

import com.wayfinder.pal.network.http.HttpEntity;


/**
 * Entity that delegates the process of content generation 
 * to a {@link PostContent}.
 * 
 * 
 */
public class EntityPostContent implements HttpEntity{

    private final PostContent postContent;
    
    protected final String contentType;
    protected final String contentEncoding;
    
    public EntityPostContent(PostContent postContent, String contentType,
            String contentEncoding) {
        super();
        if (postContent == null) {
            throw new IllegalArgumentException("PostContent cannot be null");
        }
        this.postContent = postContent;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    public void finish() throws IOException {
    }

    public InputStream getContent() throws IllegalStateException, IOException {
        //throw new UnsupportedOperationException("EntityPostContent does not implement getContent()");
        return null;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public long getContentLength() {
        return postContent.getContentLength();
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isChunked() {
        return false;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        postContent.writeTo(outstream);
    }

}
