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

import com.wayfinder.core.network.internal.xscoder.XsDataCodedInputStream;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.util.io.MaybeGZIPInputStream;
import com.wayfinder.pal.util.UtilFactory;

public class XsGzipResponseWrapper implements ResponseCallback {

    private final UtilFactory utilFactory;
    private final ResponseCallback wrappedResponse;

    public XsGzipResponseWrapper(UtilFactory utilFactory, ResponseCallback wrappedResponse ) {
        super();
        this.utilFactory = utilFactory;
        this.wrappedResponse = wrappedResponse; 
    }

    public void error(CoreError error) {
        wrappedResponse.error(error);
    }

    public void readResponse(InputStream in, long length) throws IOException {
        MaybeGZIPInputStream inStream = new MaybeGZIPInputStream(new XsDataCodedInputStream(in));
        if(inStream.isGzip()) {
            in = utilFactory.openGZIPInputStream(inStream);
        } else {
            in = inStream;
        }            
        wrappedResponse.readResponse(in, -1);
    }
}
