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

import junit.framework.TestCase;

public final class XsDataCoderTest extends TestCase {

    private static final String MESSAGE = "This is a test message";
    
    public void testProcessNextByte() {
        byte[] originalBuf = MESSAGE.getBytes();
        byte[] encodedBuf = new byte[originalBuf.length];
        
        XsDataCoder coder = new XsDataCoder();
        for (int i = 0; i < encodedBuf.length; i++) {
            encodedBuf[i] = coder.processNextByte(originalBuf[i]);
            // ensure that all bytes are encoded
            if(encodedBuf[i] == originalBuf[i]) {
                fail("some bytes are not encoded");
            }
        }

        // now attempt to decode it again
        XsDataCoder decoder = new XsDataCoder();
        byte[] decodedBuf = new byte[originalBuf.length];
        for (int i = 0; i < decodedBuf.length; i++) {
            decodedBuf[i] = decoder.processNextByte(encodedBuf[i]);
            // check that each byte is back to the original state
            assertEquals(originalBuf[i], decodedBuf[i]);
        }
    }
    
    
    
    public void testProcessNextBlock() {
        byte[] originalBuf = MESSAGE.getBytes();
        byte[] encodedBuf = new byte[originalBuf.length];
        System.arraycopy(originalBuf, 0, encodedBuf, 0, originalBuf.length);
        
        XsDataCoder coder = new XsDataCoder();
        for(int processed = 0; processed < originalBuf.length;) {
            final int offset = processed;
            final int length;
            if( (offset + 5) > originalBuf.length ) {
                length = (originalBuf.length - offset);
            } else {
                length = 5;
            }
            coder.processNextBlock(encodedBuf, offset, length);
            processed += length;
        }
        
        // check that all bytes have been encoded
        for (int i = 0; i < encodedBuf.length; i++) {
            if(encodedBuf[i] == originalBuf[i]) {
                fail("some bytes are not encoded");
            }
        }
        
        
        // now attempt to decode it again

        XsDataCoder decoder = new XsDataCoder();
        byte[] decodedBuf = new byte[encodedBuf.length];
        System.arraycopy(encodedBuf, 0, decodedBuf, 0, encodedBuf.length);
        
        for(int processed = 0; processed < encodedBuf.length;) {
            final int offset = processed;
            final int length;
            if( (offset + 5) > encodedBuf.length ) {
                length = (encodedBuf.length - offset);
            } else {
                length = 5;
            }
            decoder.processNextBlock(decodedBuf, offset, length);
            processed += length;
        }
        
        // check that each byte is back to the original state
        for (int i = 0; i < decodedBuf.length; i++) {
            assertEquals(originalBuf[i], decodedBuf[i]);
        }
        
        
    }

}
