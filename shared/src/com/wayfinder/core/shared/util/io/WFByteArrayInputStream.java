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

import java.io.ByteArrayInputStream;

/**
 * <p>Extended version of {@link ByteArrayInputStream}.</p>
 * 
 * <p>This class provides some utility methods not found in
 * ByteArrayInputStream.
 * It also provides some methods that make the ByteArrayInputStream re-usable.
 * The latter methods partly violate encapsulation of the super class and should
 * be used only in performance critical situations.</p>
 */
public class WFByteArrayInputStream extends ByteArrayInputStream {
    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */
    
    /**
     * Creates a new WFByteArrayInputStream, using buf as its buffer array.
     * 
     * @param buf - the buffer to use. The buffer is NOT copied.
     * @see ByteArrayInputStream#ByteArrayInputStream(byte[])
     */
    public WFByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    /**
     * Creates a new WFByteArrayInputStream, using a part of buf as its
     * buffer array.
     * 
     * @param buf - the buffer to use. The buffer is NOT copied.
     * @param offset - offset of first byte in buf to read.
     * @param length - the maximum number of bytes to read from buf.
     */
    public WFByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);     
    }


    /**
     * Gets a reference to the buffer we read from.
     * 
     * @return a reference to the buffer we read from.
     */
    public byte[] getByteArray() {
        return buf;
    }


    /**
     * <p>Assure that internal buffer is large enough.</p>
     * 
     * <p>If buf.length < size, a new internal buffer will be created and used.
     * The data in the old buffer is NOT copied.</p>
     * 
     * <p>FIXME: since the new data is all zero - what is the use case for this
     * method?</p>
     * 
     * @param size - the size to be checked.
     */
    public void assureSize(int size) {
        if (buf.length < size) {
            setByteArray(new byte[size]);
        }
    }


    /**
     * Equivalent to
     * <code>setByteArray(aNewByteArray, 0, aNewByteArray.length)</code>.
     * 
     * @param aNewByteArray - the new array to use.
     */
    private void setByteArray(byte[] aNewByteArray) {
        setByteArray(aNewByteArray, 0, aNewByteArray.length);
    }


    /**
     * <p>Resets the WFByteArrayInputStream to use a different buffer.</p>
     * 
     * <p>Except in very performance critical situations, consider creating a
     * new WFByteArrayInputStream instead.</p> 
     * 
     * <p>Sets buf as the buffer array. The initial value of pos is
     * offset and the initial value of count is offset+length. The
     * buffer array is not copied. mark = pos.</p>
     *
     * <p>Note that if bytes are simply read from the resulting input
     * stream, elements buf[pos] through buf[pos+length-1] will be read;
     * however, if a reset operation is performed, then bytes buf[0]
     * through buf[pos-1] will then become available for input. This
     * is to be compatible with constructor
     * {@link ByteArrayInputStream#ByteArrayInputStream(byte[] buf, int offset, int length)}
     *
     * @param buf - the new buffer must not be null.
     * @param offset - offset of first byte in buf to read.
     * @param length - the maximum number of bytes to read from buf.
     * @throws IllegalArgumentException if offset >= buf.length or
     *         offset+length > buf.length. In these cases the object in not
     *         changed.
     * FINDBUGS: exclusion needed - this is a design choice.
     */
    private void setByteArray(byte[] buf, int offset, int length) {
        /*
         * This method was previously public. In jWMMG it is used only by a
         * restore method in com.wayfinder.io.CacheStorage. Given that most
         * objects stored in the cache are at least a few hundred bytes,
         * it does not seem reasonable that there is a significant performance
         * gain by re-using the ByteArrayInputStream objects.
         */
        if (offset >= buf.length || offset + length > buf.length) {
            throw new IllegalArgumentException("WFByteArrayInputStream.setByteArray() buf.length: " + buf.length
                                               + " offset: " + offset
                                               + " length: " + length);
        }

        pos = offset;
        mark = pos;
        count = offset + length;
        this.buf = buf;
    }


    /**
     * Resets the count value.
     * 
     * @deprecated Don't use this, it doesn't reset pos or mark.
     * @param aCount - the new value for count.
     * @throws IllegalArgumentException if aCount > buf.length
     */
    public void setCount(int aCount) {
        if (aCount > buf.length) {
            throw new IllegalArgumentException("WFByteArrayInputStream.setCount() buf.length: " + buf.length
                                               + " aCount: " + aCount);
        }

        count = aCount;
    }    
 

    /**
     * @return the current value of pos. Note that pos = 0 is buf[0]
     * even if you used setByteArray(byte[], offset, length)
     */
    public int getPos() {
        return pos;
    }

    /**
     * <p>Resets the read position.</p>
     * 
     * <p>Setting aNewPos == buf.length may seem strange, but
     * it is allowed to make it possible to set the stream in a "all
     * read"-state where the next call to available() will return 0,
     * and reading from the stream will return end-of-stream value
     * -1. The check for aNewPos > buf.length is just to catch
     * programmer errors earlier.</p>
     *
     * @param aNewPos - the new position for reading.
     * @throws IllegalArgumentException if aNewPos > buf.length or
     *         aNewPos < 0.
     */
    public void setPos(int aNewPos) {
        if (aNewPos > buf.length || aNewPos < 0) {
            throw new IllegalArgumentException("WFByteArrayInputStream.setPos() buf.length: " + buf.length
                                               + " aNewPos: " + aNewPos);
        }

        mark = pos = aNewPos;
    }

    /**
     * <p>Resets the read position, relative to current position.</p>
     * 
     * <p>calls <code>setPos(pos + aDelta)</code>.
     * I.e. sets position relative from
     * current position. aDelta = 0 is ok. For positive aDelta you can
     * also use skip().</p>
     * 
     * @param aDelta - relative change in position.
     * @see WFByteArrayInputStream#setPos(int)
     * @see ByteArrayInputStream#skip(long)
     */
    public void setPosRelative(int aDelta) {
        setPos(pos + aDelta);
    }
    
 
    /**
     * <p>Resets both pos and count.</p>
     * 
     * <p>see {@link WFByteArrayInputStream#setByteArray(byte[], int, int)} for
     * information on the behaviour if you call reset().</p>
     * 
     * @param offset - the new position to use.
     * @param length - the number bytes to read from offset.
     */
    public void setBounds(int offset, int length) {
        mark = pos = offset;
        count =  offset + length;
    }
}
