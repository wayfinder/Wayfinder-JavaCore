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

package com.wayfinder.core.shared.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Utility class for serialization.
 * 
 * @see com.wayfinder.core.shared.util.WFDataInputStream
 */
public final class SerializableUtil {
    /**
     * The empty string.
     */
    static final String EMPTY_STRING = "";
    
    /**
     * Don't create objects of this class.
     */
    private SerializableUtil() {}
    
    /**
     * Writes a string. If the string is null, EMPTY_STRING is written. Uses
     * DataOutput.writeUTF() for the write.
     *
     * @param dout - the output to write to. 
     * @param value - the string to write
     * @throws IOException - if there is an exception thrown from
     * DataOutput.writeUTF().
     * @see DataOutput#writeUTF(String)
     */
    public static void writeString(DataOutput dout, String value)
        throws IOException {
        if (value == null) {
            dout.writeUTF(EMPTY_STRING);
        } else {
            dout.writeUTF(value);
        }
    }

    /**
     * The inverse of writeString().
     * 
     * Invokes din.readUTF(). If the resulting string has zero length, null is
     * returned instead of the empty string. Otherwise, the read string is
     * returned.
     *  
     * @param din - the input to read from.
     * @return the string read or null if the string was zero length.
     * @throws IOException
     * @see writeString
     * @see DataInput#readUTF()
     */
    public static String readString(DataInput din) throws IOException {
        String value = din.readUTF();
        if (value.length() == 0) {
            return null;
        } else {
            return value;
        }
    }
}
