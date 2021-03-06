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
package com.wayfinder.core.network.internal.xscoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class XsDataCodedCopyingOutputStreamTest extends XsDataCodedStreamTemplate {

    
    public void testEncodeAndWriteToStream() throws IOException {
        byte[] originalBuf = new byte[10];
        for (int i = 0; i < originalBuf.length; i++) {
            originalBuf[i] = (byte) i;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XsDataCodedCopyingOutputStream stream = new XsDataCodedCopyingOutputStream(baos);
        byte[] tmpBuf = new byte[originalBuf.length];
        System.arraycopy(originalBuf, 0, tmpBuf, 0, originalBuf.length);
        stream.write(tmpBuf, 0, tmpBuf.length);
        stream.close();
        
        // check that buffer is intact after operation
        for (int i = 0; i < tmpBuf.length; i++) {
            assertEquals(originalBuf[i], tmpBuf[i]);
        }
        
        // check that the bytes where written
        byte[] writtenBuf = baos.toByteArray();
        assertEquals(tmpBuf.length, writtenBuf.length);
    }
    

    protected XsDataCodedOutputStream createOutputStream(OutputStream stream) {
        return XsDataCodedOutputStream.createStream(stream, true);
    }

}
