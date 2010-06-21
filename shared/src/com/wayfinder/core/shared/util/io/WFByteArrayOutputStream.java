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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <p>Extended version of {@link ByteArrayOutputStream}.</p>
 * 
 * <p>This class provides some utility methods not found in ByteArrayOutputStream.
 * It also provides some methods that make the ByteArrayOutputStream re-usable.
 * The latter methods partly violate encapsulation of the super class and should
 * be used only in performance critical situations.</p>
 */
public class WFByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * Creates a new WFByteArrayOutputStream, with the specified initial buffer
     * capacity.
     * 
     * @param size - the initial buffer capacity in bytes.
     */
    public WFByteArrayOutputStream(int size) {
        super(size);
    }


    /**
     * Gets a reference to the internal buffer. This is dangerous and should
     * only be used in performance critical situations.
     * 
     * @return buf.
     */
    public byte[] getByteArray() {
        return buf;
    }

    
    /**
     * Resets the count of valid bytes in the buffer. Can be used to discard
     * parts of previous output.
     *  
     * @param count - the new count.
     * @throws IllegalArgumentException - if aCount < 0
     *         or aCount > super.buf.length.
     * @see ByteArrayOutputStream#reset()
     */
    public void setCount(int count) {
        if (count < 0 || count > buf.length) {
            throw new IllegalArgumentException("WFByteArrayOutputStream.setCount() "
                    + "count=" + count + " " + String.valueOf(buf)
                    + ".length=" + buf.length);
        }
        super.count = count;
    }    


    /**
     * Writes the complete contents of this byte array output stream
     * to the specified output stream argument, as if by calling the
     * output stream's write method using aOut.write(buf, 0, count).
     * From J2SE 1.5.0.
     *
     * @param out the output stream to which to write the data.
     * @throws IOException - if an I/O error occurs.
     */
    public void writeTo(OutputStream out)
        throws IOException
    {
        out.write(buf, 0, count);
    }


    /**
     * <p>Translates the contents of the buffer into a string using the
     * specified encoding.</p>
     *
     * <p>This is like ByteArrayOutputStream.toString() in J2SE 1.5.0, but
     * we use another name because most debug printing will want just
     * the object reference in printable form, not the entire
     * contents.</p>
     *
     * @param enc - a character-encoding name.
     * @return a new String translated from the buffer's contents.
     * @throws UnsupportedEncodingException - if the platform does not support
     *         encoding aEnc.
     */
    public String bufToString(String enc)
        throws UnsupportedEncodingException
    {
        return new String(buf, 0, count, enc);
    }    
}
