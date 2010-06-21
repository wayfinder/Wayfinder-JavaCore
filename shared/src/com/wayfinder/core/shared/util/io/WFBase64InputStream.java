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
package com.wayfinder.core.shared.util.io;

import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.core.shared.util.WFBase64;

/**
 * A wrapper over the decoding functionality to allow decoding from an 
 * input stream.
 * 
 * 
 *
 */
public class WFBase64InputStream extends InputStream {
    
    private static final int B64_BUFFER_SIZE = 10248;
    private static final int DECODED_BUFFER_SIZE = 7686;

    /**
     * the underlying, base64 encoded, InputStream
     */
    protected InputStream m_in;
    
    /**
     * signal EOF
     */
    protected boolean m_eof = false;
    
    /**
     * the outgoing buffer (with decoded bytes)
     */
    protected byte[] m_outBuf;
    
    /**
     * the offset in the outgoing buffer
     */
    protected int m_outBufOffset;
    
    protected byte[] m_inBuf;
    
    /**
     * the number of valid bytes (from the latest decoding operation) in the 
     * outgoing buffer; since we reuse the outgoing buffer, there might be
     * leftovers that were not overwritten
     */
    protected int m_outBufValid;
    
    public WFBase64InputStream(InputStream in) {
        m_in = in;
        m_outBuf = new byte[DECODED_BUFFER_SIZE];
        m_inBuf = new byte[B64_BUFFER_SIZE];
        m_outBufValid = 0;
        m_outBufOffset = 0;
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (m_outBufOffset == m_outBufValid) {
            int n = fillBuffer();
            if (n <= 0) {
                m_eof = true;
                return -1;
            }
        }
        // read() should return a value in the range 0..255, but since
        // until now we used byte[] in the decode operation, values in the
        // range 128..255 appear as -128..-1
        if (m_outBuf[m_outBufOffset] >= 0) {
            return m_outBuf[m_outBufOffset++];
        }
        else {
            return (256 + m_outBuf[m_outBufOffset++]);
        }
    }

    public int available() throws IOException {
        if (m_eof) return -1;
        return super.available();
    }

    public void close() throws IOException {
        if (m_in != null) {
            m_in.close();
        }
        m_eof = true;
        super.close();
    }

    public long skip(long n) throws IOException {
        if (m_eof) return 0;
        int skipped = 0;
        while (skipped < n) {
            int c = read();
            if (c < 0) {
                m_eof = true;
                break;
            }
            else {
                skipped++;
            }
        }
        return skipped;
    }

    public boolean markSupported() {
        return false;
    }

    public int read(byte[] b, int offset, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((offset < 0) || (offset > b.length) || (len < 0) ||
                   ((offset + len) > b.length) || ((offset + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        
        if (m_eof) {
            return -1;
        }
        
        int startOffset = offset;
        
        // see if there are any leftover decoded bytes from a previous read that
        // didn't consume all the decoded output
        if (m_outBufOffset < m_outBufValid) {
            int bytesLeft = m_outBufValid - m_outBufOffset;
            System.arraycopy(m_outBuf, m_outBufOffset, b, offset, bytesLeft);
            len -= bytesLeft;
            offset += bytesLeft;
        }
        
        // get the next chunks of bytes and copy the decoded data to b
        while (len > 0) {
            int n = fillBuffer();
            if (n > 0) {
                System.arraycopy(m_outBuf, 0, b, offset, n);
                len -= n;
                offset += n;
                m_outBufOffset = n;
            }
            else {
                m_eof = true;
                break;
            }
        }
        return (offset - startOffset);
    }

    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }
    
    /**
     * tries to read the next four bytes of encoded data and fill the outgoing
     * buffer with the next 3 bytes of decoded data
     * 
     * @return the actual number of decoded bytes in the outgoing buffer (less
     * than or equal to 3)
     * 
     * @throws IOException
     */
    protected int fillBuffer() throws IOException {
        m_outBufOffset = 0;
        int n = m_in.read(m_inBuf);
        if (n >= 0) {
            m_outBufValid = WFBase64.decodeTo(m_inBuf, 0, n, m_outBuf, 0, m_outBuf.length);
            return m_outBufValid;
        }
        else {
            return -1;
        }
    }
}
