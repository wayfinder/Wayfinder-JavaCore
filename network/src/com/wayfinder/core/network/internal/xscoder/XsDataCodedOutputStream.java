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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream that can write encoded data sent via /xsdata ifc to the xml server.
 *
 * This class sends encode the received data and write it ot  the underlying 
 * stream. There is no buffering. It is recommended to let this stream write to 
 * a buffered output stream.
 *
 * The class currently does not support the mark/reset
 * operations. Because that would require backing the encoder
 * accordingly.
 */
public abstract class XsDataCodedOutputStream extends OutputStream {

    /**
     * the underlying stream.
     */
    private final OutputStream m_outstream;

    /**
     * statefull encoder for data
     */
    private final XsDataCoder m_xsDataCoder;

    /**
     * @param outstream - the underlying stream to write to.
     */
    XsDataCodedOutputStream(OutputStream outstream) {
        m_outstream = outstream;
        m_xsDataCoder = new XsDataCoder();
    }
    
    
    /**
     * Creates an {@link XsDataCodedOutputStream}
     * <p>
     * This factory method allows the creator to decide upon a slight optmization
     * that may prove destructive if not used with care.
     * <p>
     * When writing to this stream, it's possible to disable the copying of data
     * and let the stream encode the data directly in the byte arrays passed
     * into the write methods. While this removes the need to copy the data,
     * it also means that the data in the byte arrays will be modified after
     * the call to write returns.
     * <p>
     * <b>If an external, non-Wayfinder, stream will write into this stream it's
     * highly recommended to stick with copying the arrays</b>
     * 
     * @param stream The {@link OutputStream} to write the encoded data to
     * @param copyBuffers if true, the data in byte arrays passed into the write 
     * methods will be copied before the data is encoded.
     * @return
     */
    public static XsDataCodedOutputStream createStream(OutputStream stream, boolean copyBuffers) {
        if(copyBuffers) {
            return new XsDataCodedCopyingOutputStream(stream);
        }
        return new XsDataCodedDirectAccessOutputStream(stream);
    }
    

    /**
     * calls aOut.close()
     */
    public void close()
        throws IOException {
        m_outstream.close();
    }
    
    /**
     * calls aOut.flush()
     */
    public void flush() throws IOException {
        m_outstream.flush();
    }

    // --------------------------------------------------------------------
    // write and skip that actually does work

    public void write(int b) throws IOException {
        m_outstream.write(m_xsDataCoder.processNextByte((byte) b));
    }

    
    /**
     * Encoded the given part of the b and than writes the result to the
     * underlying stream.
     * <p>
     * Warning! If copying of arrays is disabled, this will change the content 
     * of given part of b. See {@link #createStream(OutputStream, boolean)}
     * for more information.
     * 
     * @see java.io.OutputStream.write(byte[] b, int off, int len)
     */
    public final void write(byte[] b, int off, int len)
        throws IOException {
        handleBuffer(b, off, len);
    }
    
    
    /**
     * Delegates the handling of the array to the subclass.
     * <p>
     * Once the array has been handled, the subclass should call
     * {@link #encodeAndWriteToStream(byte[], int, int)} to encode and
     * send the contents
     * 
     * @param b The original byte array
     * @param off The offset
     * @param len The length
     * @throws IOException If something goes wrong :P
     */
    abstract void handleBuffer(byte[] b, int off, int len)
    throws IOException;
    
    
    /**
     * Encodes the byte array and sends it to the stream.
     * <p>
     * <b>This will change the contents of the provided byte array. If this
     * is not wanted, copy the array before passing it in</b>
     * 
     * @param b The byte array to modify
     * @param off The offset
     * @param len The length
     * @throws IOException If something goes wrong :P
     */
    final void encodeAndWriteToStream(byte[] b, int off, int len)
    throws IOException {
        m_xsDataCoder.processNextBlock(b, off, len);
        m_outstream.write(b, off, len);
    }
    

    /**
     * equivalent to write(b, 0, b.length)
     * <p>
     * Warning! If copying of arrays is disabled, this will change the content 
     * of given part of b. See {@link #createStream(OutputStream, boolean)}
     * for more information.
     */
    public final void write(byte[] b)
        throws IOException {
        write(b, 0, b.length);
    }
    
}
