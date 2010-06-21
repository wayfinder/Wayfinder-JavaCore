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
package com.wayfinder.pal.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An entity that can be sent or received with an HTTP message.
 * Entities can be found in some
 * {@link HttpEntityEnclosingRequest requests} and in
 * {@link HttpResponse responses}, where they are optional.
 * 
 * Consider all entities streamed and not repeatable
 */
public interface HttpEntity {

    /**
     *     
     * @return
     * @throws IllegalStateException  if called twice 
     * @throws IOException if the stream could not be created
     */
    InputStream getContent() throws IllegalStateException, IOException;
    
    /**
     * Obtains the Content-Encoding header value, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity.
     * Wrapping entities that modify the content encoding should
     * adjust this header accordingly.
     *
     * @return  the Content-Encoding header value for this entity, or
     *          <code>null</code> if the content encoding is unknown
     */
    String getContentEncoding();
    
    /**
     * Tells the length of the content, if known.
     *
     * @return  the number of bytes of the content, or
     *          a negative number if unknown. If the content length is known
     *          but exceeds {@link java.lang.Long#MAX_VALUE Long.MAX_VALUE},
     *          a negative number is returned.
     */    
    long getContentLength();
    
    /**
     * Obtains the Content-Type header value, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity. 
     *      
     * @return  the Content-Type header value for this entity, or
     *          <code>null</code> if the content type is unknown
     */
    String getContentType();
    
    
    /**
     * Writes the entity content to the output stream.  
     * 
     * @param outstream the output stream to write entity content to
     * 
     * @throws IOException if an I/O error occurs
     */
    void writeTo(OutputStream outstream) throws IOException;
    
    
    /**
     * Tells about chunked encoding for this entity.
     * The primary purpose of this method is to indicate whether
     * chunked encoding should be used when the entity is sent.
     * For entities that are received, it can also indicate whether
     * the entity was received with chunked encoding.
     * <br/>
     * The behavior of wrapping entities is implementation dependent,
     * but should respect the primary purpose.
     *
     * @return  <code>true</code> if chunked encoding is preferred for this
     *          entity, or <code>false</code> if it is not
     */
    boolean isChunked();
    
    /**
     * This method is called to indicate that the content of this entity
     * is no longer required. All entity implementations are expected to
     * release all allocated resources as a result of this method 
     * invocation. Content streaming entities are also expected to 
     * dispose of the remaining content, if any. Wrapping entities should 
     * delegate this call to the wrapped entity.
     * <br/>
     * This method is of particular importance for entities being
     * received from a {@link HttpConnection connection}. The entity
     * needs to be consumed completely in order to re-use the connection
     * with keep-alive.
     *
     * @throws IOException if an I/O error occurs.
     *          This indicates that connection keep-alive is not possible.
     */
    void finish() throws IOException;
}
