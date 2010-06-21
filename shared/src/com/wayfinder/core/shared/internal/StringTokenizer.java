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

/**
 * Split a string into tokens. If you have the full J2SE API available, you can
 * instead use java.util.StringTokenizer or the package java.util.regex.
 * 
 * Thread safety: not assessed. Use with care.
 */
public class StringTokenizer {
    private static final StringTokenizer m_tokenizer = new StringTokenizer();
    private String m_string;
    private String[] m_tokens;
    private int m_index;

    private StringTokenizer() {
    }

    /**
     * Creates a new StringTokenizer.
     * 
     * @param string
     * @param token
     */
    public StringTokenizer(String string, String token) {
        m_string = string;
        m_tokens = new String[] {token};
        m_index = 0;
    }

    /**
     * Creates a new StringTokenizer.
     * 
     * @param string
     * @param tokens
     */
    public StringTokenizer(String string, String[] tokens) {
        m_string = string;
        m_tokens = tokens;
        m_index = 0;
    }


    public String[] getTokens() {
        m_index = 0;
        int tokenCount = 0;
        while(hasMoreTokens()) {
            nextToken();
            tokenCount ++;
        }
        m_index = 0;
        String[] tokens = new String[tokenCount];
        for(int i = 0; i < tokens.length; i ++) {
            tokens[i] = nextToken();
        }

        return tokens;
    }


    public boolean hasMoreTokens() {
        if(m_string != null) {
            if(m_index < m_string.length()) {
                return true;
            }

            for (int i = 0; i < m_tokens.length; i++) {
                if(m_string.indexOf(m_tokens[i], m_index) != -1) {
                    return true;
                }
            }
        }

        return false;
    }


    public String nextToken() {
        if(m_index < 0) {
            m_index = 0;
        }
        if(m_index > m_string.length() - 1) {
            throw new IllegalStateException("StringTokenizer.nextToken() index beyond length of string, no more tokens");
        }

        int tokenLength = 0;
        int nextTokenIndex = -1;
        for (int i = 0; i < m_tokens.length; i++) {
            int tmpIndex = m_string.indexOf(m_tokens[i], m_index);
            if(tmpIndex >= 0 && (nextTokenIndex < 0 || tmpIndex < nextTokenIndex)) {
                nextTokenIndex = tmpIndex;
                tokenLength = m_tokens[i].length();
            }
        }

        if(nextTokenIndex == -1) { //no token found
            nextTokenIndex = m_string.length();
        }

        String nextToken = m_string.substring(m_index, nextTokenIndex);
        m_index = nextTokenIndex + tokenLength;

        return nextToken; 
    }


    public static String[] getTokens(String string, String token) {
        synchronized(m_tokenizer) {
            m_tokenizer.m_string = string;
            m_tokenizer.m_tokens = new String[] { token };
            return m_tokenizer.getTokens();
        }
    }

    public static synchronized String[] getTokens(String string, String[] tokens) {
        synchronized(m_tokenizer) {
            m_tokenizer.m_string = string;
            m_tokenizer.m_tokens = tokens;
            return m_tokenizer.getTokens();
        }
    }


    public static int[] convertStringArrayToIntArray(String[] stringArray) {
        int[] intArray = new int[stringArray.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = Integer.parseInt(stringArray[i]);
        }

        return intArray;
    }
}
