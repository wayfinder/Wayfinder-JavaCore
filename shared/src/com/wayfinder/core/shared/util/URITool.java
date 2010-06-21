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

package com.wayfinder.core.shared.util;

import java.util.Hashtable;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * class to collect various tools to handle URI:s (mostly for service
 * window)
 */
public class URITool {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(URITool.class);
    
    /**
     * Terms enclosed in < > are non-terminal symbols in the RFC3986 grammar.
     *
     * <p>uri is assumed to contain a valid URI according to
     * RFC3986 ("Uniform Resource Identifier (URI): Generic Syntax")
     * syntax. It must not be null.</p>
     *
     * <p>Any part of the string to the right of (including)the first '#'
     * (must be a <fragment>) is thrown away. Then the part to the
     * left of (including) the left-most '?' is thrown away (must be
     * <scheme> : <hier-part>).</p>
     *
     * <p>The remaining string is assumed to be a HTTP query string
     * encoded in application/x-www-form-urlencoded as specified by
     * W3C HTML 4.01 specification, section 17.13.4. This is more
     * limited than RFC3986 since it can't contain the char '?'.</p>
     *
     * <p>The W3C Internationalization recommendations for URI encoding
     * (<code>http://www.w3.org/International/O-URL-code.html</code>) is
     * observed. I.e. non-ASCII chars are assumed to be written in
     * UTF-8 and then the individual bytes percent encoded.</p>
     *
     * <p><code>application/x-www-form-urlencoded</code> means that the data is a
     * sequence of name=value pairs:
     * <code>name=value&name=value&name=value...</code></p>
     * 
     * <p>For each name, value pair parsed, we add the value to the hash table
     * with the name as key. The parser does not check that the
     * syntax is correct and which entries end up in the table, when
     * parsing a syntactically invalid query string
     * e.g. <code>foo=1&bar&quux</code> is undefined.</p>
     *
     * <p>TODO:We might need to use another data structure. I'm unsure if it
     * is really legal to provide several values for the same name,
     * e.g. "...foo=1&bar=2&foo=3"</p>
     *
     * @param uri the URI to parse.
     * @param table the HashTable into which the result will be stored.
     */
    public static void parseHTTPQueryURL(String uri, Hashtable table) {
        final String fname = "URITool.parseHTTPQueryURL()";
        if(LOG.isTrace()) {
            LOG.trace(fname, uri);
        }

        StringBuffer sb = new StringBuffer(uri);
        int fragmentbegin = uri.indexOf('#');
        if (fragmentbegin != -1) {
            sb.setLength(fragmentbegin); // chops off # and after
        }

        // StringBuffer.indexOf() is not in CLDC
        int k = uri.indexOf('?');
        if ((k < 0) // no ?, thus no parameters to parse.
            // or if ? follows fragment begin (? is legal in fragment) 
            || (fragmentbegin >= 0 && k > fragmentbegin)) {
            return;
            
        }
        // k >= 0
        sb.delete(0, k + 1); // kill the '?' as well

        /*
         * now we won't modify the string anymore. Some things could
         * be used more generally if we converted the buffer to a new
         * String.
         *
         * Best would be a StringReadIterator which could operate on
         * both strings and StringBuffers. It would be smaller object
         * to construct.
         */

        if(LOG.isTrace()) {
            LOG.trace(fname, "query string: " + sb.toString());
        }

        WwwFormUrlEncodedParser parser =
            new WwwFormUrlEncodedParser(sb, table);
        parser.parse();
    }
    
    /**
     * <p>Utility method that creates the hashtable instead of requiring it as a
     * parameter.</p>
     * 
     * <p>Equivalent of calling parseHTTPQueryURL(uri, new Hashtable).</p>
     * 
     * @param uri - the URI to parse.
     * @return a new Hashtable with the query parameters.
     * @see URITool#parseHTTPQueryURL(String, Hashtable)
     */
    public static Hashtable parseHTTPQueryURL(String uri) {
        Hashtable hash = new Hashtable();
        parseHTTPQueryURL(uri, hash);
        return hash;
    }


    // ----------------------------------------------------------------------
    /**
     * <p>Decodes an URI escaped octet, e.g. "%41" into a character ("A").</p>  
     *
     * @param sBuf - StringBuffer to read chars from
     * @param pos - positon of first char after '%'
     *
     * @return reads two hex digits and return the decoded value [0, 255].
     *
     * @throws IndexOutOfBoundsException if there were not two
     * chars available. 
     * @throws IllegalArgumentException if the chars were not
     * valid hex digits.
     */
    public static int parsePctCodedByte(StringBuffer sBuf, int pos) {
        final String FNAME = "URITool.readPctCodedByte()";
        int result = 0;
        for (int i=0; i < 2; i++) {
            char c = sBuf.charAt(pos++);
            int v = Character.digit(c, 16);
            if (v < 0) {
                throw new IllegalArgumentException(FNAME
                        + ": " + c + "@" + (pos - 1));
            }
            result <<= 4;
            result += v;
        }

        return result;
    }


    /**
     * <p>Percent-encode a byte according to the rules of RFC3986,
     * section 2.1.</p>
     * 
     * <p>The three char representation % HEXDIG HEXDIG is appended to sb.
     * Upper case hexadecimal digits are used.</p>
     * 
     * <p>FIXME: warning about chars.</p>
     * 
     * @param b - the byte to be encoded.
     * @param sb - the string buffer to append the encoded representation to.
     */
    public static void percentEncode(byte b, StringBuffer sb) {
        final String hexDigits = "0123456789ABCDEF";
        sb.append("%");
        sb.append(hexDigits.charAt((b & 0xF0) >> 4));
        sb.append(hexDigits.charAt(b & 0xF));
    }

    /**
     * <p>Percent-encode a string according to the rules of RFC3986,
     * section 2 (especially 2.5, last para).</p>
     * 
     * <p>Characters outside the set of "unreserved characters"
     * (see {@link URITool#isUriUnreserved(char)}) are encoded to
     * UTF-8 and then the individual bytes are percent-encoded with
     * {@link URITool#percentEncode(byte, StringBuffer)}.</p>
     * 
     * <p>If s is null or <code>s.length() == 0</code>, nothing is written.</p>
     *  
     * @param s - string to encode.
     * @param sb - the StringBuffer to append to.
     * @return the number of chars appended.
     */
    public static int percentEncodeString(String s, StringBuffer sb) {
        if (s == null) {
            return 0;
        }
        
        int nbrchars = 0;
        for(int i=0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isUriUnreserved(c)) {
                sb.append(c);
                nbrchars += 1;
            } else {
                // FIXME: I think proper UTF-8 differs in handling the
                // char \u0000. We should check that, but it's
                // unlikely that it will appear
                if (c >= 0x0001 && c <= 0x007f) {
                    percentEncode((byte) c, sb);
                    nbrchars += 3;
                }
                else if (c <= 0x07ff) { // >= \u0080 or == \u0000
                    percentEncode((byte)(0xc0 | (0x1f & (c >> 6))), sb);
                    percentEncode((byte)(0x80 | (0x3f & c)), sb);
                    nbrchars += 6;
                }
                else {
                    percentEncode((byte)(0xe0 | (0x0f & (c >> 12))), sb);
                    percentEncode((byte)(0x80 | (0x3f & (c >>  6))), sb);
                    percentEncode((byte)(0x80 | (0x3f & c)), sb);
                    nbrchars += 9;
                }
            }
        }
        
        return nbrchars;
    }

    /**
     * <p>Returns true if a character is in the set of unreserved characters
     * in the grammar defined by RFC3986.</p>
     * 
     * <p><code>unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"</code></p>
     *
     * @param c - the character to check.
     * @return true if the char is in the unreserved set and false otherwise. 
     */
    public static boolean isUriUnreserved(char c) {
        if ((c >= 'A' && c <= 'Z')
            || (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9')
            || (c == '-') || (c == '_') || (c == '.') || (c == '~')) {
            return true;
        } else {
            return false;
        }
    }
    

    private final static String EMPTY_STRING = "";


    // ----------------------------------------------------------------------
    /**
     * <p>Inner helper class that does the actual parsing.</p>
     * 
     * <p>More convenient for constructing the parser than a lot of of methods
     * in URITool with lots of parameters.</p>
     */
    private static class WwwFormUrlEncodedParser {

        /**
         * @param aQueryBuf - the string corresponding to the <query>
         * part of an URI.
         * @param aHash - the hash to store the parsed query parameters into.
         */
        WwwFormUrlEncodedParser(StringBuffer aQueryBuf,
                                Hashtable aHash) {
            m_queryBuf = aQueryBuf;
            m_hash = aHash;
            parse();
        }

        final StringBuffer m_queryBuf;
        final Hashtable m_hash;
        int m_nextPos;
        
        private void parse() {
            final String FNAME = "WwwFormUrlEncodedParser.parse()";
            String name = null;
            String value = null;

            while (m_nextPos < m_queryBuf.length()) {
                readString(); // ok, since we have at least one char to read
                name = iRSString;
                // don't check that we get a = here
                readString();  // ok even if end of string (will be "")
                value = iRSString;
                if(LOG.isTrace()) {
                    LOG.trace(FNAME, name + "' = '" + value + "'");
                }
                m_hash.put(name, value);
            }
        }


        /**
         * the char that terminated reading in readString(). -1 if end
         * of string was reached. (currently unused)
         */
        int iRSterminator;
        
        int getIRSterminator() {
            return iRSterminator;
        }

        /**
         * string read by readString. Empty string if nothing was read.
         */
        String iRSString;

        /**
         * <p>Reads a sequence of ASCII chars stopping at '&', '=' and
         * end of string. Percent codes are decoded into bytes and the
         * resulting byte sequence is converted from UTF-8.</p>
         *
         * <p>Works even if we have already read the last character. Then
         * iRSString == EMPTY_STRING and iRSterminator == -1 upon
         * return.</p>
         */
        private void readString() {
            iRSString = EMPTY_STRING;
            iRSterminator = -1;

            int qbuflen = m_queryBuf.length() - m_nextPos;
            if (qbuflen == 0) {
                return;
            }

            /* worst case is that all remaining chars in iQueryBuf
             * belongs to the string and are ASCII chars without any
             * percent encodes. But if there are any percent encodes
             * or there are more than one string to read in the
             * buffer, there will be fewer bytes in bbuf than chars
             * remaining in iQueryBuf.
            */
            byte[] bbuf = new byte[qbuflen];
            int bbufnextpos = 0;

            // at least one char to read
            int c;
            do {
                c = m_queryBuf.charAt(m_nextPos++);
                if (c == '&' || c == '=') {
                    break;
                }
                if (c == '%') {
                    c = parsePctCodedByte(m_queryBuf, m_nextPos);
                    m_nextPos += 2;
                } 
                else if (c == '+') {
                    c = 0x20;
                }
                // we don't get here if we shouldn't add the char
                bbuf[bbufnextpos++] = (byte) c;
            } while (m_nextPos < m_queryBuf.length());

            // at least one. convert string if added bytes check c,
            // then check eos

            if (bbufnextpos > 0) {
                iRSString = WFUtil.UTF8BytesToString(bbuf, 0, bbufnextpos);
            }

            if (m_nextPos < m_queryBuf.length()) {
                iRSterminator = c;
            }
        }
    } // class WwwFormUrlEncodedParser


    /**
     * <p>Adds the correct parameter separator '&' or '?'.</p>
     * 
     * <p>If the URI already contains
     * parameters, '&' will be added, otherwise '?' will be
     * added. This is convenient for callers who doesn't have control
     * of the URI they want to add their own parameters to.</p>
     * 
     * <p>Call this method, then add your own parameter (or block of parameters
     * separated by '&') without leading '?'/'&'.
     * E.g. aURL.append("foo=bar&baz").
     *
     * @param uri is assumed to contain a valid URI according to
     * RFC3986 ("Uniform Resource Identifier (URI): Generic Syntax")
     * syntax. It must not be null. URL with fragments are handled.
     * @see URITool#parseHTTPQueryURL(String)
     */
    public static void
    addHTTPQueryURLParamSeparator(StringBuffer uri) {

        int fragmentbegin = WFUtil.stringBufferIndexOf(uri, '#', 0);
        int querybegin = WFUtil.stringBufferIndexOf(uri, '?', 0);
        if (querybegin == -1
            || (fragmentbegin != -1 && querybegin > fragmentbegin)) {
            uri.append('?'); // ? is legal in fragment
        }
        else {
            uri.append('&');
        }
    }
}
