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

import com.wayfinder.core.shared.util.WFUtil;


/**
 * <p>Class for parsing C-strings (terminated with \0) encoded with UTF-8
 * from a byte buffer.</p>
 * 
 * <p>This format is used by many legacy formats at
 * Wayfinder. That is a bit strange since this isn't very efficient on
 * the Symbian platform either and the native Symbian string handles
 * are more like the java way of storing the length of the string
 * explicitly.</p>
 *
 * <p>This class is more efficient than CDataInputStream when you already
 * have the data you want to read in a buffer, perhaps mixed with
 * other data. But it is not as nice a CDataInputStream in terms of
 * abstraction.</p>
 *
 * <p>If the terminating null of the last string is missing, the string
 * will be returned anyway and getNextOffset() will return
 * m_buf.length + 1.</p>
 *
 * <p><i>This class is not thread safe. Use client-side locking.</i></p>
 */
public class UTF8CStringBufferParser {

    /**
     * <p>Creates a new parser.</p>
     * 
     * <p>For performance reasons (this code is used heavily in the map), the
     * buffer is not copied and the caller must not modify it. The operations
     * are undefined if buf has zero length. A buf that contains just a single
     * null byte ({0}) is however correct.</p>
     * 
     * <p>FINDBUGS: exclusion needed - this is a design choice.</p>
     * 
     * @param buf - the buffer with data to parse.
     */
    public UTF8CStringBufferParser(byte[] buf) {
        m_buf = buf;
    }


    /**
     * the buffer with data to decode.
     */
    protected byte[] m_buf;
    
    /**
     * @see UTF8CStringBufferParser#getLastNbrBytes()
     */
    protected int m_lastNbrBytes;

    /**
     * @see UTF8CStringBufferParser#getNextOffset() 
     */
    protected int m_nextOffset;



    /**
     * <p>Returns the number of bytes read by the last getString() or
     * getString(int).</p>
     * 
     * <p>This is useful if you have a DataInputStream
     * reading on the same buffer and need to skip() to position the
     * stream to read the first byte after the null-terminated string.<p>
     * 
     * <p>NOTE: if the trailing \0 is mising, this method will anyway return
     * the number of bytes read + 1 as if the trailing \0 was there. This is
     * not strictly correct, but since the buffer is exhausted it shouldn't
     * cause too much problems.</p>
     * 
     * @return the number of bytes read.
     */
    public int getLastNbrBytes() {
        return m_lastNbrBytes;
    }


    /**
     * <p>Returns the offset in the buffer, past the last terminating '\0',
     * encountered by getString() or getString(int).</p>
     * 
     * <p>This is useful if
     * you have your own methods for reading other things from the
     * buffer and want to update your index to point to the first byte
     * after the null-terminated string.</p>
     * 
     * @return the next offset.
     */
    public int getNextOffset() {
        return m_nextOffset;
    }
    

    /**
     * Returns getNextString(m_nextOffset).
     * 
     * @return the string at the next offset.
     * @see UTF8CStringBufferParser#getNextString(int)
     */
    public String getNextString() {
        return getNextString(m_nextOffset);
    }

    /**
     * Reads a null-terminated string starting at off and
     * updates m_lastNbrBytes and m_nextOffset.
     * 
     * @param off - the index of the first byte of the string.
     * @return the parsed string.
     */
    public String getNextString(int off) {
        int p = off;
        try {
            while (m_buf[p] != 0) {
                ++p;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            /*
             * p is now iBuf.length because the block would not  be
             * executed when the exception is thrown.
             *
             * If we rewrite it as while (iBuf[p++] != 0);
             * p would be (iBuf.length + 1) and we would have to
             * decrement it in this exception handler, because it was
             * iBuf.length when we tried to use it as index but the
             * increment still takes place. Or rather the increment
             * happens first and then the returned value used as array
             * index is (p-1).  (JLS3 15.14.2).
             *
             * This is more a question of style. I can't decide if
             * either alternative is more readable without a long
             * comment anyway.
             */  
        }
        int strlen = p - off;
        m_nextOffset = p + 1;
        m_lastNbrBytes = strlen + 1;
        
        /*
         * buggy on SEMC JP7, see documentation in WFUtil.
         */
        return WFUtil.UTF8BytesToString(m_buf, off, strlen);
    } // getNextString(int)
}
