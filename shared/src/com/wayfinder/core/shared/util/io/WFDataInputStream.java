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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

// imports to avoid long package names in javadoc
import java.io.DataInput;

/**
 * An extended version of {@link DataInputStream} that can read additional
 * data types.
 */
public class WFDataInputStream extends DataInputStream {

    /**
     * Creates a WFDataInputStream that reads from the specified input stream.
     * 
     * @param in - the input stream to read from.
     */
    public WFDataInputStream(InputStream in) {
        super(in);
    }


    /**
     * <p>Skips n bytes from the input. If the end of file is reached before
     * n bytes have been skipped, an EOFException is thrown.</p>
     * 
     * <p>This provides a much stronger guarantee and a different behaviour than
     * {@link DataInputStream#skipBytes(int)} which is why we use a different
     * method name.</p>
     * 
     * @param n - the number of bytes to be skipped.
     * @throws EOFException if the end of file is reached before n bytes have
     *         been skipped.
     * @throws IOException if an I/O error occurs.
     */
    public void skipBytesForced(int n) throws IOException {
        int skipped = skipBytes(n);

        if (skipped < n) {
            /*
             * this can be made more efficient for large n but our hope is
             * really that normal skipBytes() works. We can't use skip()
             * because we need to check for EOF and available() is not reliable
             * for that purpose.
             */

            while (skipped < n) {
                int b = in.read();
                if (b == -1) {
                    throw new EOFException("Left to skip: " + (skipped - n));
                }
                skipped--;
            }
        }
    }
    

    /**
     * <p>Reads four bytes and return a long value which is guaranteed to
     * between 0 and (2^32 - 1) (inclusive). Thus, the highest 32 bits of the
     * return value are always zero.</p>
     *
     * For byte order, see {@link DataInput#readInt()}.
     * 
     * @return the unsigned integer read.
     * @throws IOException - if an I/O error occurs.
     */
    public long readUnsignedInt() throws IOException {
        int us1 = readUnsignedShort(); // higher 16 bits of int32, max 65535
        int us2 = readUnsignedShort(); // lower 16 bits of int32
        long l1 = ((long) us1 << 16) | us2;
        return l1;
    }
}
