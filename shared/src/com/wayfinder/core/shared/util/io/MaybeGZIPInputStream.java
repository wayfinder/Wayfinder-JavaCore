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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.shared.util.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * An InputStream wrapper that allows to "peek" into the stream in order to read
 * the first two bytes. This is to determine if the content of the stream is
 * gzipped. These first two bytes are preserved so that they're still available
 * in read operations.
 *
 * 
 */
public class MaybeGZIPInputStream extends InputStream {

    /**
     * the stream
     */
    private InputStream iInStream;
    
    /**
     * the first two bytes from the stream (obtained with 2 consecutive read())
     */
    private int[] iFirstTwo;
    
    /**
     * how many bytes were already read from {@link #iInStream} (we only care 
     * about this until 2 bytes are read)
     */
    private int iReadBytes = 0;
    
    /**
     * check if the first two bytes have been processed by a read operation on
     * this wrapper stream; this is set to <code>false</code> after the first
     * two bytes have been read while trying to determine if we deal with a
     * gzipped stream, and subsequently set to <code>true</code> after those
     * 2 bytes have been dealt with. 
     * 
     * @see #isGzip()
     */
    private boolean iProcessedTwoBytes;
    
    
    /**
     * Standard constructor
     * <p>
     * Will immediately read the two first bytes in the provided stream
     * 
     * @param aInStream
     * @throws IOException 
     */
    public MaybeGZIPInputStream(InputStream aInStream) throws IOException {
        iInStream = aInStream;
        iFirstTwo = new int[2];
        iFirstTwo[0] = iInStream.read();
        iFirstTwo[1] = iInStream.read();
    }


    /* (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        if (!iProcessedTwoBytes) {
            return iInStream.available() + (2 - iReadBytes);
        }
        return iInStream.available();
    }


    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        // 
        iInStream.close();
    }


    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] aBuf, int aOffset, int aLen) throws IOException, IndexOutOfBoundsException {
        
        if (aOffset < 0 || aLen < 0 
                || aBuf.length < aOffset + aLen) {
            throw new IndexOutOfBoundsException();
        }
        
        if (!iProcessedTwoBytes) {
            //if the first two bytes in iInStream have been read, but not handled
            //in any way, add them to the buffer
            while (iReadBytes < 2 && aLen > 0) {
                int x = iFirstTwo[iReadBytes];
                if (x == -1) {
                    //this is to force possible future calls to any of the read()
                    //methods to just call the read() on iInStream, which will keep
                    //returning -1 anyway
                    iProcessedTwoBytes = true;
                    
                    if (iReadBytes == 0) return -1;
                    else return iReadBytes + 1;
                }
                aBuf[aOffset] = (byte)x;
                iReadBytes++;
                aLen--;
                aOffset++;
            }
            //mark that the first two bytes have been handled
            if (iReadBytes == 2) {
                iProcessedTwoBytes = true;
            }
            //handle the rest of the stream
            if (aLen == 0) return 2;
            int restLen = iInStream.read(aBuf, aOffset, aLen);
            if (restLen > -1) return 2 + restLen;
            else return 2;
        }
        else {
            //if we're past the first two bytes in the stream, just use the 
            //existing implementation
            return iInStream.read(aBuf, aOffset, aLen);
        }
    }


    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] aBuf) throws IOException {
        // 
        return read(aBuf, 0, aBuf.length);
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (!iProcessedTwoBytes) {
            //if the first two bytes in iInStream have been read but not handled
            if (iReadBytes < 2) {
                return iFirstTwo[iReadBytes++];
            }
            else {
                //mark that we're past those first 2 bytes
                iProcessedTwoBytes = true;
            }
        }
        return iInStream.read();
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long n) throws IOException {
        if (n <= 0) return 0;
        //similar idea to read(byte[], int, int)
        if (!iProcessedTwoBytes) {
            int skipped = 0;
            while (iReadBytes < 2 && n > 0) {
                int x = iFirstTwo[iReadBytes];
                if (x == -1) {
                    iProcessedTwoBytes = true;
                    return skipped;
                }
                iReadBytes++;
                skipped++;
                n--;
            }
            if (iReadBytes == 2) {
                iProcessedTwoBytes = true;
            }
            long restSkip = iInStream.skip(n);
            return restSkip + 2;
        }
        else return iInStream.skip(n);
    }
    
    /**
     * Will inspect the two first bytes in the stream and check if they are
     * the gzip magic bytes
     * 
     * @return true if it's a gzipped stream
     * @throws IOException
     */
    public boolean isGzip() {
        if (iFirstTwo[0] == 0x1F && iFirstTwo[1] == 0x8B) {
            return true;
        }
        return false;
    }

}
