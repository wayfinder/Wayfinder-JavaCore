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

import java.io.InputStream;
import java.io.IOException;

/**
 * Stream that can read encoded data sent via /xsdata ifc at the xml server.
 *
 * This class sends read requests to the underlying stream and then
 * decode the received data. There is no buffering. It is recommended
 * to let this stream read from a buffered input stream.
 *
 * The class currently does not support the mark/reset
 * operations. Because that would require backing the encoder
 * accordingly.
 */
public final class XsDataCodedInputStream extends InputStream {

    /**
     * the underlying stream.
     */
    private final InputStream m_instream;

    /**
     * stateful decoder for data
     */
    private final XsDataCoder m_xsDataCoder;

    /**
     * @param instream - the underlying stream to read from.
     */
    public XsDataCodedInputStream(InputStream instream) {
        m_instream = instream;
        m_xsDataCoder = new XsDataCoder();
    }

    /**
     * @returns iIn.available()
     */
    public int available()
        throws IOException {

        return m_instream.available();
    }

    /**
     * calls aIn.close()
     */
    public void close()
        throws IOException {

        m_instream.close();
    }

    /**
     * @returns false - see class documentation
     */
    public boolean markSupported() {
        return false;
    }


    // --------------------------------------------------------------------
    // read and skip that actually does work

    /**
     * Let b = iIn.read(). If b == -1, -1 is returned (indicating
     * eof). Otherwise, the received byte is decoded is
     * returned. Since the encoding only deals with the lower 8 bits,
     * the returned value is not negative, specifically not -1.
     */
    public int read()
        throws IOException {

        int b = m_instream.read();
        if (b == -1) {
            return -1;
        }
        else {
            return m_xsDataCoder.processNextByte((byte) b) & 0xff;
        }
    }

    /**
     * Calls n = iIn.read(b, off, len). If the call does not throw an
     * exception and n is not -1, the bytes in b will then be
     * decoded. Then n is returned.
     *
     * @see java.io.InputStream.read(byte[] b, int off, int len)
     */
    public int read(byte[] b, int off, int len)
        throws IOException {

        int n = m_instream.read(b, off, len);
        if (n > 0) {
            m_xsDataCoder.processNextBlock(b, off, n);
        }

        return n;
    }

    /**
     * equivalent to read(b, 0, b.length)
     */
    public int read(byte[] b)
        throws IOException {

        return read(b, 0, b.length);
    }


    /*
      we let skip() in InputStream call read(). This is not the most
      efficient but we don't skip() a lot and it is more flexible if
      we change the coding in the future so that we can't just advance
      the key pointer.
    */
}
