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

import java.io.InputStream;
import java.io.EOFException;
import java.io.IOException;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * <p>A limited implementation of java.io.BufferedInputStream from JDK
 * 1.0. For unknown reasons this performance enhancer was left out of
 * the CLDC-specification.</p>
 *
 * <p>Since {@link InputStream#available()} does not work on Blackberry, this
 * class does not check available() before reading. Thus, <code>read(byte[],
 * int, int)</code> always block when the buffer is exhausted until data is
 * available unless <code>read()</code> on the underlying stream has returned -1,
 * in which case, -1 is returned without checking again.</p>
 *
 * <p>The class currently does not support the mark/reset operations.</p>
 *
 * <p>This class is not thread safe. The caller is responsible for
 * ensuring synchronization via client-side locking.</p>
 */
public class WFBufferedInputStream extends InputStream {
    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */
    
    private static final Logger LOG = LogFactory
        .getLoggerForClass(WFBufferedInputStream.class);


    /**
     * Default buffer size.
     */
    protected static final int DEFAULT_BUFSIZE = 512;

    /**
     * The input stream which this WFBufferedInputStream reads from.
     */
    protected final InputStream m_in;

    /**
     * The internal buffer.
     */
    protected final byte[] m_buf;

    /**
     * The index one greater than the index of the last valid byte in
     * the buffer. The buffer is not circular. iCount is 0 if there are no
     * bytes in the buffer.
     */
    protected int m_count;

    /**
     * <p>The current position in the buffer. This is the index of the
     * next character to be read from the buf array.</p>
     *
     * <p>This value is always in the range 0 through m_count.
     * <ul><li>If it is less than m_count, then buf[iNextPos] is the next byte to
     * be supplied as input.</li>
     * <li>If it is equal to m_count, then the next read or skip
     * operation will require more bytes to be read from the
     * input stream.</li></p>
     */
    protected int m_nextPos;

    /**
     * m_in.read(...) has returned -1 and it is useless to try to read
     * again.
     */
    protected boolean m_eof;
    
    
    /**
     * Equivalent to <code>WFBufferedInputStream(aIn, DEFAULT_BUFSIZE)</code>.
     * 
     * @param in - the underlying stream to read from.
     */
    public WFBufferedInputStream(InputStream in) {
        this(in, DEFAULT_BUFSIZE);
    }

    /**
     * Equivalent to <code>WFBufferedInputStream(aIn, aSize, null)</code>.
     * 
     * @param in - the underlying stream to read from.
     * @param bufferSize - the size of the buffer
     */
    public WFBufferedInputStream(InputStream in, int bufferSize) {
        this(in, bufferSize, null);
    }

    /**
     * Creates a WFBufferedInputStream.
     * 
     * @param in - the underlying stream to read from.
     * @param bufferSize - the size of the buffer
     * @param aDbgObjectId printed in debug printouts to make it
     * possible to see which buffer in the system is reading, when we
     * have several. The string "WFBufferedInputStream" is always
     * printed. If the parameter is null, Object.hashCode() will be
     * used to construct the name.
     */
    public WFBufferedInputStream(InputStream in, int bufferSize,
                                 String aDbgObjectId) {
        m_in = in;
        m_buf = new byte[bufferSize];
        OBJECTID = getObjectID(aDbgObjectId);

        if(LOG.isDebug()) {
            LOG.debug(OBJECTID + " WFBufferedInputStream.WFBufferedInputStream()",
                      "buffersize=" + bufferSize);
        }
        
        // iPos and m_count already 0 - OK
    }


    /**
     * <p>Returns the number of bytes that can be read from this input
     * stream without blocking.</p>
     *
     * <p>The <code>available()</code> method of WFBufferedInputStream returns
     * the sum of the number of bytes remaining to be read in the buffer
     * (<code>m_count - m_nextPos</code>)
     * and the result of calling the available method of the
     * underlying input stream.</p>
     *
     * <p>If {@link InputStream#available()} is unreliable,
     * this method might also return too small results.
     * So in practice this method might not be usable.
     */
    public int available()
        throws IOException {

        return m_count - m_nextPos + m_in.available();
    }


    /**
     * <p>See the general contract of {@link InputStream#read()}. Additionally,</p>
     * 
     * <p>If this method has previously returned -1, -1 is returned.</p>
     *
     * <p>If data is available in the buffer, return the next byte from
     * the buffer. The byte is onverted to int, without sign extension, so an
     * int between 0 and 255 will be returned.</p>
     *
     * <p>If no data is available, call fillBuffer(). And when fillBuffer()
     * returns,
     * return the first byte read, or -1 if no data could be read
     * (because <code>m_in.read()</code> returned -1.</p>
     *
     * <p>Note that calling this method does not guarantee that read()
     * will be called on the underlying stream.</p>
     */
    public int read()
        throws IOException {

        final String fname = OBJECTID + ".read(): ";

        if(LOG.isTrace()) {
            LOG.trace(fname, "");
        }
        
        if (m_eof) {
            if(LOG.isTrace()) {
                LOG.trace(fname, "eof previously, returning -1.");
            }
            return -1;
        }

        int nbrInBuf = m_count - m_nextPos;
        if (nbrInBuf > 0) {
            return m_buf[m_nextPos++] & 0xFF;
        }
        else {
            fillBuffer();
            return read(); // works even if eof was encountered
        }
    }

    /**
     * <p>See the general contract of
     * {@link InputStream#read(byte[], int, int)}. Additionally,</p>
     * 
     * <p>If this method has previously returned -1, -1 is returned.</p>
     *
     * <p>If data is available in the buffer, copy at most len bytes from
     * m_buf to b. Return the number of bytes copied.</p>
     *
     * <p>If no data is available, call fillBuffer() and when it returns,
     * copy at most len bytes from m_buf to b. Return the number of
     * bytes copied.</p>
     *
     * <p>If fillBuffer() returns -1 (indicating that no data could be
     * read), -1 is returned and b is not changed.</p>
     *
     * <p>Note that the method does not check available() and does not make
     * repeated calls to fillBuffer if more than one read would
     * required to obtain len bytes. This is consistent with
     * InputStream.read(byte[], int, int).</p>
     *
     * <p>Note that calling this method does not guarantee that read()
     * will be called on the underlying stream.</p>
     *
     * <p>For the error cases described for InputStream.read(byte[], int,
     * int) we don't guarantee that the exceptions will thrown before
     * reading.</p>
     *
     * <p>Note that on Sony Ericsson phones, calling
     * {@link java.io.InputStreamReader#read()} will boil down to
     * <code>(WFBuffered)InputStream.read(byte[], 0 ,1)</code>, so in that case,
     * it is important to not try to optimize away arraycopy by
     * reading directly from the stream. If you do, the whole
     * buffering will be defeated.</p>
     */
    public int read(byte[] b, int off, int len)
        throws IOException {

        String fname = OBJECTID + ".read(byte[], int, int)";

        if(LOG.isTrace()) {
            // use String.valueOf() to make FindBugs shut up.
            LOG.trace(fname, String.valueOf(b) + ", " + off + ", " + len);
        }

        if (m_eof) {
            if(LOG.isTrace()) {
                LOG.trace(fname, "eof previously, returning -1.");
            }
            return -1;
        }

        int nbrCopied = copyBufferedData(b, off, len);
        if (nbrCopied > 0) {
            return nbrCopied;
        }
        else {
            fillBuffer();
            return read(b, off, len); // works even if eof was
                                      // encountered
        }
    }

    /**
     * We override {@link InputStream#read(byte[])}, just to make sure that
     * the reading will be buffered even if some device manufacturer did the
     * stupid thing and implemented <code>read(byte[])</code> with a for-loop.
     */
    public int read(byte[] b)
        throws IOException {

        if(LOG.isTrace()) {
            LOG.trace(OBJECTID + ".read(byte[])", "b.length: " + b.length);
        }

        return read(b, 0, b.length);
    }


    /**
     * See the general contract of the {@link InputStream#skip(long)}.
     */
    public long skip(long n)
        throws IOException {

        String fname = OBJECTID + ".skip()";
        if(LOG.isTrace()) {
            LOG.trace(fname, String.valueOf(n));
        }

        if (n < 1) {
            return 0;
        }

        int nbrInBuf = m_count - m_nextPos;
        if (n <= nbrInBuf) {
            // enough to just throw away (a part of) the buffer
            m_nextPos += n;
            return n;
        }

        // must read as well
        n -= nbrInBuf;
        m_nextPos = m_count;
        if(LOG.isDebug()) {
            LOG.debug(fname, n + " needs to be skipped from stream.");
        }
        return nbrInBuf + m_in.skip(n);
    }


    /**
     * <p>This is method is provided as a convenience so you don't have to
     * create a
     * {@link java.io.DataInputStream} just to read a (big) chunk of bytes.</p>
     * 
     * <p>It is also
     * more efficient than <code>read(byte[] b, ...)</code> if the target array
     * is much larger than the buffer size. Because, if more than
     * m_buf.length bytes are needed, these will be read directly from
     * the underlying stream.</p>
     *
     * <p>Note that if several reads on the underlying stream are needed
     * to fill the destination buffer, the last read may be much
     * smaller than m_buf.length. If you read data that always has the
     * same length you might want to tweak the size of this class'
     * buffer to make that less frequent.</p>
     *
     * <p>This method blocks until one of the following conditions occurs:
     * <ol><li>len bytes of input data are available, in which case a normal
     *         return is made.</li>
     *     <li>End of file is detected, in which case an EOFException is
     *         thrown.</li>
     *     <li>An I/O error occurs, in which case an IOException other than
     *         EOFException is thrown.</li>
     * </ol>
     * </p>
     * <ul><li>If b is null, a NullPointerException is thrown.
     *     <li>If off is negative, or len is negative, or off+len is greater
     *         than the b.length, then an IndexOutOfBoundsException is
     *         thrown.
     *     <li>If len is zero, then no bytes are read.
     * </ul>    
     * <p>Otherwise, the
     * first byte read is stored into element b[off], the next one
     * into b[off+1], and so on. The number of bytes read is, at most,
     * equal to len.</p>
     * 
     * @param b - destination buffer.
     * @param off - offset at which to store the first byte read.
     * @param len - maximum number of bytes to read.
     * 
     * @throws EOFException - if end of file is detected before len bytes are
     * read.
     * @throws IOException - if an I/O error occurs on the underlying stream.
     */
    public void readFully(byte[] b, int off, int len)
        throws EOFException, IOException {

        String fname = OBJECTID + ".readFully()";
        if(LOG.isTrace()) {
            LOG.trace(fname, String.valueOf(b) + ", " + off + ", " + len);
        }

        // checks to make this stream behave like the CLDC DataInput
        if (off < 0 || len < 0 || off+len > b.length) {
            throw new IndexOutOfBoundsException();
        }

        if (len < 1) {
            return;
        }

        if (m_eof) {
            throw new EOFException();
        }

        int n = copyBufferedData(b, off, len);
        len -= n;
        if(LOG.isTrace()) {
            LOG.trace(fname, "after buffer copy needs: " + len);
        }
        off += n;

        while (len > 0) { // buffered was not enough
            n = m_in.read(b, off, len);

            if(LOG.isTrace()) {
                LOG.trace(fname, "read from stream: " + n);
            }

            if (n == -1) {
                m_eof = true;
                throw new EOFException();
            }
            len -= n;
            off += n;
        }
    }

    /**
     * Equivalent to <code>readFully(b, 0, b.length).
     *
     * @param b - destination buffer.
     * 
     * @throws EOFException - if end of file is detected before len bytes are
     * read.
     * @throws IOException - if an I/O error occurs on the underlying stream.
     */
    public void readFully(byte[] b)
        throws EOFException, IOException
    {
        readFully(b, 0, b.length);
    }


    /**
     * <p>Tests if this input stream supports the mark and reset
     * methods.</p>
     * 
     * @return false since mark/reset is currently not supported.
     */
    public boolean markSupported() {
        return false;
    }


    /**
     * <p>Closes this input stream.</p>
     * 
     * <p>Calls m_in.close(). The buffer is not
     * freed because that would require extra checks in other methods
     * to throw IOException instead of null pointer exception and
     * normally close() is called close to deallocation of the entire
     * object anyway.</p>
     */
    public void close()
        throws IOException {

        final String fname = OBJECTID + ".close(): ";

        if(LOG.isTrace()) {
            LOG.trace(fname, "");
        }

        m_eof = true; // even if close should "fail" we assume that no
                     // more reading can be done
        m_in.close();
        if(LOG.isDebug()) {
            LOG.debug(fname, "OK.");
        }
    }


    /**
     * <p>Tries to fill the internal buffer with data from the underlying
     * stream.</p>
     * 
     * <p>Calls m_in.read(b).
     * <ul><li>If no data can be read, set m_eof and return
     *         -1.
     *     <li>If data could be read, set m_nextPos = 0, m_count = return
     *         value from m_in.read(b) and return m_count.
     *     <li>If an IOException is thrown by m_in.read(),
     *         m_eof is set and the exception is propagated to the caller.
     * </ul></p>
     * 
     * <p>Note that any data in m_buf which has not been read is
     * overwritten. This might change in the future to be able to
     * provide more flexible buffering strategies.</p>
     * 
     * @return the number of bytes read, or -1 if end of stream was reached.
     * @throws IOException - if an I/O error occurs on the underlying stream.
     */
    protected int fillBuffer()
        throws IOException {

        final String fname = OBJECTID + ".fillBuffer(): ";

        int n;
        try {
            if(LOG.isDebug()) {
                LOG.debug(fname, "trying to read " + m_buf.length);
            }

            n = m_in.read(m_buf);
            if(LOG.isDebug()) {
                LOG.debug(fname, "read " + n
                          + " m_in.available() now: " + m_in.available());
            }

            if (n == -1) {
                m_eof = true;
            }
            else {
                m_nextPos = 0;
                m_count = n;
            }

            return n;
        }
        catch (IOException e) {
            if(LOG.isError()) {
                LOG.error(fname, "IOE: " + e);
            }

            m_eof = true;
            throw e;
        }
    }


    /**
     * <p>Internal helper method.</p>
     *
     * <p>Copies at most aLen bytes starting at m_buf[m_nextPos] and
     * advance m_nextPos. If m_buf is empty, nothing is copied. The
     * number of bytes copied is returned.</p>
     *
     * @param aTarget - the target buffer.
     * @param aOff - offset in aTarget.
     * @param aLen - maximum number of bytes to copy.
     * @return the number of bytes copied.
     */
    protected int copyBufferedData(byte[] aTarget, int aOff, int aLen) {
        int nbrInBuf = m_count - m_nextPos;

        if (nbrInBuf > 0) {
            // copy previously read data        
            if (aLen > nbrInBuf) {
                aLen = nbrInBuf;
            }
            System.arraycopy(m_buf, m_nextPos, aTarget, aOff, aLen);
            m_nextPos += aLen;
            return aLen;
        }
        else {
            return 0;
        }
    }


    // needed to trace even obfuscation is used - the default ctor may
    // break if a subclass should override hashCode()
    private final String OBJECTID;

    private String getObjectID(String overrideId) {
        StringBuffer sb = new StringBuffer(50);
        sb.append("WFBufferedInputStream(");
        if (overrideId == null) {
            sb.append("0x").append(Integer.toHexString(hashCode()));
        }
        else {
            sb.append(overrideId);
        }
        sb.append(")");
        
        return sb.toString();
    }
}
