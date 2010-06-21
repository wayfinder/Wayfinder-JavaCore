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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.shared.util.io;

import java.io.IOException;
import java.io.Reader;

/**
 * <p>A character stream whose source is a string.</p>
 * 
 * <p>Implements a limited version of <code>java.io.StringReader</code>
 * from JDK 1.1, since that class is not included in CLDC 1.1.</p>
 * 
 * <p>This class is thread-safe by internal synchronization. See
 * {@link java.io.Reader#lock}.</p>
 */
public class WFStringReader extends Reader {

    private final String m_string;

    /**
     * Cached value of m_string.length().
     */
    private final int m_len;

    private int m_nextPos;
    private boolean m_closed;


    /**
     * Creates a new string reader.
     * 
     * @param s the string to read. Must not be null.
     */
    public WFStringReader(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }

        m_string = s;
        m_len = s.length();
        synchronized (lock) {
            m_nextPos = 0;
        }
    }

    // -------------------------------------------------------------------
    // closing the stream

    /* (non-Javadoc)
     * @see java.io.Reader#close()
     */
    public void close() throws IOException {
        // specification states that it is OK to close() a stream that
        // is already closed.
        synchronized (lock) {
            m_closed = true;
        }
    }

    /**
     * Throws an IOException if the reader is already closed.
     * 
     * Otherwise does nothing. This is an internal utility to ensure
     * that the protocol specified by {@link #close()} is
     * consistent across methods.
     */
    private void dieIfClosed() throws IOException {
        synchronized (lock) {
            if (m_closed) {
                throw new IOException("WFStringReader already closed.");
            }
        }
    }


    // -------------------------------------------------------------------
    // reading and skipping

    /* (non-Javadoc)
     * @see java.io.Reader#read()
     */
    public int read() throws IOException {
        /*
         * make single character reading more efficient since overriding
         * makes it possible to do it without creating/using an internal
         * char[]
         */
        synchronized (lock) {
            dieIfClosed();
            int avail = m_len - m_nextPos;
            if (avail > 0) {
                return m_string.charAt(m_nextPos++);
            } else {
                return -1;
            }
        }
    }

    /*
     * we don't override read(char[] cbuf)
     * as Reader will (probably) do the same as we would:
     * call read(cbuf, 0, cbuf.length).
     */

    /* (non-Javadoc)
     * @see java.io.Reader#read(char[], int, int)
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (lock) {
            /*
             * the specification for close() says we must throw IOE if
             * the stream was already closed.
             * This must be done even if there was more data available and
             * supersedes the normal return of -1 when read reaches eof. 
             */
            dieIfClosed();

            int avail = m_len - m_nextPos;
            if (avail > 0) {
                int n = Math.min(avail, len);
                int newNext = m_nextPos + n;
                /*
                 * throws its own exceptions if the params don't make sense
                 * 
                 * according to spec, the number of copied chars is
                 * newNext - m_nextPos ==
                 * m_nextPos + Math.min(avail, len) - m_nextPos ==
                 * Math.min(avail, len) which is what we want.
                 */
                m_string.getChars(m_nextPos, newNext, cbuf, off);
                m_nextPos = newNext;
                return n;
            } else {
                return -1;
            }
        }
    }


    /* (non-Javadoc)
     * @see java.io.Reader#skip(long)
     */
    /**
     * <p>Skips the specified number of characters.</p>
     * 
     * <p>If n is negative, this method skips backwards so that already
     * read characters can be read again. This is not supported by
     * {@link java.io.Reader#skip(long)} but is consistent with the
     * JDK 1.1-implementation of <code>StringReader</code>.</p>
     * 
     * <p>It is not possible to skip past the beginning or end of the string.
     * But, <code>skip()</code> always skips the requested number of
     * characters if possible. So you don't have to call skip in a loop until
     * accumulated skip count or end of stream is reached.</p>
     * 
     * @param n the number of characters to skip.
     * @return the number of characters skipped. If n < 0, the return value
     * is < 0 if it was possible to skip |n| characters backwards.
     * @throws IOException if an I/O error occurs.
     * 
     */
    public long skip(long n) throws IOException {
        /*
         * make skip() more efficient than Reader's repeated calls to
         * read(char[], int, int).
         */
        synchronized (lock) {
            dieIfClosed();
            if (n >= 0) {
                // skip forward
                int avail = m_len - m_nextPos;
                if (n < avail) {
                    m_nextPos += n;
                    return n;
                } else {
                    m_nextPos = m_len;
                    return avail;
                }
            } else {
                // skip backwards
                n = -n;
                int avail = m_nextPos;
                if (n < avail) {
                    m_nextPos -= n;
                    return n;
                } else {
                    m_nextPos = 0;
                    return avail;
                }
            }
        }
    }


    /**
     * <p>Always returns true as the next <code>read()</code> will never block.
     * </p>
     * 
     * <p>We always now exactly how much data is available without blocking
     * for any platform system. Note that this method doesn't say anything
     * about if more data is available - only that you will not block to find
     * out. Thus, the next read() may return -1.</p> 
     * 
     * @return always true.
     * @throws IOException if <code>close()</code> has been called.
     */
    public boolean ready() throws IOException {
        /*
         * The default implementation in {@link java.io.Reader} always return
         * false as it has no way of determining readyness.
         */

        dieIfClosed();
        return true;
    }


    // -------------------------------------------------------------------

    /**
     * <p>Tells whether this stream supports the <code>mark()</code> operation,
     * which it currently doesn't.</p>
     * 
     * <p>Subject to change without notice as mark/reset-support might be
     * added in a future release.</p>
     */
    public boolean markSupported() {
        return false;
    }
}
