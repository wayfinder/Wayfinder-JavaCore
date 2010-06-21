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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.wayfinder.pal.network.http.HttpEntity;

import junit.framework.TestCase;

public class EntityPostContentTest extends TestCase {
    
    static final String contentType = "text/xml";
    
    PostContent postContent;
    
    protected void setUp() throws Exception {
        super.setUp();
        postContent = new ByteArrayPostContent();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        postContent = null;
    }
    
    public void testConstructor() {
        HttpEntity entity1 = new EntityPostContent(postContent, contentType, null);
        assertEquals(contentType, entity1.getContentType());
        assertNull(entity1.getContentEncoding());

        try {
            new EntityPostContent(null, "text/xml", "gzip");
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
            //expected
        }
    }
    
    public void testWriteTo() throws Exception {
        HttpEntity entity2 = new EntityPostContent(postContent, null, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        entity2.writeTo(out);
        
        String content = new String(out.toByteArray());
        
        assertEquals(content, inputdata);
    }
    
    static final String inputdata = "<isab></isab>";
    
    static class ByteArrayPostContent implements PostContent {

        byte[] data = "<isab></isab>".getBytes();
        
        public long getContentLength() {
            return -1;
        }

        public void writeTo(OutputStream outstream) throws IOException {
           outstream.write(data);
        }
        
    }

}
