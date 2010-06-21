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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.shared.util;

/**
 * Base64 decoder (and maybe encoder someday). IETF RFC 4648
 * 
 *
 */
public class WFBase64 {
    
    /*
     * The standard encoding table. The URL-safe version would replace 
     * '+' and '/' with '-' and '_' respectively.
     * 
     */
    private static final byte[] ENCODE_TABLE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 
        'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 
        'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', 
        '4', '5', '6', '7', '8', '9', '+', '/'
    };
    
    /**
     * The decoding table. Handles standard and URL-safe Base64 by mapping 
     * '+','/' and '-','_' to 62 and 63.
     */
    private static final byte[] DECODE_TABLE = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 
        58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 
        9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, 
        -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };
    
    private static final byte PAD = '=';
    
    public static byte[] decode(byte[] in) {
        return decode(in, 0, in.length);
    }
    
    public static byte[] decode(byte[] in, int offset, int len) {
        int maxOutLen = (int) ((float) len * 3 / 4);
        byte[] out = new byte[maxOutLen];
        int decoded = decodeTo(in, offset, len, out, 0, maxOutLen);
        if (decoded < maxOutLen) {
            byte[] trimmedOut = new byte[decoded];
            System.arraycopy(out, 0, trimmedOut, 0, decoded);
            return trimmedOut;
        }
        return out;
    }

    /**
     * Decode a Base64 encoded byte array.
     * @param bufIn the Base64 encoded byte array;
     * @param offsetIn the offset from which to start reading;
     * @param lengthIn how many bytes to read;
     * @return a byte array with the decoded binary data
     */
    public static int decodeTo(byte[] bufIn, int offsetIn, int lengthIn, byte[] bufOut, int offsetOut, int lengthOut) {
        
        if (bufIn == null || bufOut == null) {
            throw new NullPointerException();
        } else if ((offsetIn < 0) || (offsetIn > bufIn.length) || (lengthIn < 0) 
                || ((offsetIn + lengthIn) > bufIn.length) || ((offsetIn + lengthIn) < 0)
                || (offsetOut < 0) || (offsetOut > bufOut.length) || (lengthOut < 0) 
                || ((offsetOut + lengthOut) > bufOut.length) || ((offsetOut + lengthOut) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (lengthIn == 0 || lengthOut == 0) {
            return 0;
        }
        
        /* From RFC 2440 http://tools.ietf.org/html/rfc2440
         * 
         * Input data: 0x14fb9c03d97e 
         * Hex: 1 4 f b 9 c | 0 3 d 9 7 e 
         * 8-bit: 00010100 11111011 10011100 | 00000011 11011001 11111110 
         * 6-bit: 000101 001111 101110 011100 | 000000 111101 100111 111110 
         * Decimal: 5 15 46 28 0 61 37 62 
         * Output: F P u c A 9 l + 
         * 
         * Input data: 0x14fb9c03d9 
         * Hex: 1 4 f b 9 c | 0 3 d 9 
         * 8-bit: 00010100 11111011 10011100 | 00000011 11011001 
         *                                              pad with 00 
         * 6-bit: 000101 001111 101110 011100 | 000000 111101 100100 
         * Decimal: 5 15 46 28 0 61 36 
         *                      pad with = 
         * Output: F P u c A 9 k = 
         * 
         * Input data: 0x14fb9c03 
         * Hex: 1 4 f b 9 c | 0 3 
         * 8-bit: 00010100 11111011 10011100 | 00000011 
         *                                      pad with 0000 
         * 6-bit: 000101 001111 101110 011100 | 000000 110000 
         * Decimal: 5 15 46 28 0 48 
         *                  pad with = = 
         * Output: F P u c A w = =
         */
        
        int posOut = 0;
        int posIn = offsetIn;
        // compute output length; 4 bytes in input match 3 bytes in output
        int maxOutLen = (int) ((float) lengthIn * 3 / 4);
        
        byte[] decoded = new byte[maxOutLen];
        int threeOctets = 0;    //a group of 3 "proper" (8bit) bytes
        for (posIn = offsetIn; posIn < offsetIn + lengthIn && posIn < bufIn.length; posIn++) {
            byte b = bufIn[posIn];
            
            if (b == PAD) {
                break;
            }
            
            if (b >= 0 && b < DECODE_TABLE.length) {
                int result = DECODE_TABLE[b];
                threeOctets = (threeOctets << 6) + result;
            }
            
            if (posIn % 4 == 3) {
                decoded[posOut++] =  (byte) (threeOctets >> 16);
                decoded[posOut++] =  (byte) ((threeOctets >> 8) & 0xFF);
                decoded[posOut++] =  (byte) (threeOctets & 0xFF);
                threeOctets = 0;
            }
        }
        posIn--;
        // at least one shift left to do to align
        threeOctets = threeOctets << 6;
        switch (posIn % 4) {
        case 1:
            threeOctets = threeOctets << 6;
            decoded[posOut++] =  (byte) ((threeOctets >> 16));
            break;

        case 2:
            decoded[posOut++] =  (byte) (threeOctets >> 16);
            decoded[posOut++] =  (byte) ((threeOctets >> 8) & 0xFF);
            break;
        }
        
        if (posOut <= lengthOut) {
            System.arraycopy(decoded, 0, bufOut, offsetOut, posOut);
            return posOut;
        }
        else {
            throw new ArrayStoreException("The decoded data doesn't fit in the supplied output buffer!");
        }
    }
    
    public static byte[] decode(String s) {
        return decode(s.toCharArray(),0,s.length());
    }
    
    public static byte[] decode(char[] cbuf) {
        return decode(cbuf,0,cbuf.length);
    }
    
    /**
     * 
     * @param cbuf can be with or without ending '='
     * @param offset
     * @param len
     * @return the decoded data as a byte array
     */
    public static byte[] decode(char[] cbuf, int offset, int len) {
        int numGroups = len/4;
        int bytesInLastGroup = 0;

        if (4*numGroups != len) {
            //we don't have ending =
            bytesInLastGroup = (len%4)*3/4; //can be 1 or 2   
        } else {
            //we have ending = or is exact size
            if (len != 0) {
                if (cbuf[offset + len -1] == '=') {
                    bytesInLastGroup = 2;//one '='
                    numGroups--;
                    if (cbuf[offset + len -2] == '=') {
                        bytesInLastGroup = 1;
                    }
                }
            }
        }
        byte[] result = new byte[3*numGroups + bytesInLastGroup];

        // Translate all full groups from base64 to byte array elements
        int inCursor = offset, outCursor = 0;
        for (int i=0; i<numGroups; i++) {
            int ch0 = DECODE_TABLE[cbuf[inCursor++]];
            int ch1 = DECODE_TABLE[cbuf[inCursor++]];
            int ch2 = DECODE_TABLE[cbuf[inCursor++]];
            int ch3 = DECODE_TABLE[cbuf[inCursor++]];
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (bytesInLastGroup > 0) {
            int ch0 = DECODE_TABLE[cbuf[inCursor++]];
            int ch1 = DECODE_TABLE[cbuf[inCursor++]];
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (bytesInLastGroup == 2) {
                int ch2 = DECODE_TABLE[cbuf[inCursor++]];
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        return result;
    }//decode(char[] cbuf, int offset, int len) 
    
    public static String encode(byte[] in) {
        int fourB64Bytes = 0;
        StringBuffer sb = new StringBuffer();
        int posIn = 0;
        for (posIn = 0; posIn < in.length; posIn++) {
            int oneByte = in[posIn];    // in 0..255, not -128..127 
            if (oneByte < 0) {
                oneByte += 256;
            }
            fourB64Bytes = (fourB64Bytes << 8) + oneByte;
            if (posIn % 3 == 2) {
                sb.append((char)ENCODE_TABLE[(fourB64Bytes >> 18) & 0x3F]);
                sb.append((char)ENCODE_TABLE[(fourB64Bytes >> 12) & 0x3F]);
                sb.append((char)ENCODE_TABLE[(fourB64Bytes >> 6) & 0x3F]);
                sb.append((char)ENCODE_TABLE[fourB64Bytes & 0x3F]);
                fourB64Bytes = 0;
            }
        }
        posIn--;
        switch (posIn % 3) {
        case 0:
            fourB64Bytes = fourB64Bytes << 4;   //pad with 0000
            sb.append((char)ENCODE_TABLE[(fourB64Bytes >> 6) & 0x3F]);
            sb.append((char)ENCODE_TABLE[fourB64Bytes & 0x3F]);
            sb.append((char)PAD);
            sb.append((char)PAD);
            fourB64Bytes = 0;
            break;

        case 1:
            fourB64Bytes = fourB64Bytes << 2;   //pad with 00
            sb.append((char)ENCODE_TABLE[(fourB64Bytes >> 12) & 0x3F]);
            sb.append((char)ENCODE_TABLE[(fourB64Bytes >> 6) & 0x3F]);
            sb.append((char)ENCODE_TABLE[fourB64Bytes & 0x3F]);
            sb.append((char)PAD);
            fourB64Bytes = 0;
            break;

        }
        return sb.toString();
    }
}
