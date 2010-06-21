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
package com.wayfinder.core.shared.util;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * Represent a String with the difference that all the write methods modified 
 * the current object instead of creating a new one. 
 *     
 * Should be used instead of temporary Strings or BufferStrings in order to 
 * reduce the number new object.
 * The object can and shoud be reuse in order to reduce to object allocations
 * @see #empty()
 *  
 * 
 */
public class CharArray {
    /*
     * migrated from jWMMG so old code does not completely follow the code
     * standard.
     */

    private static final Logger LOG =
        LogFactory.getLoggerForClass(CharArray.class);

    /** 
     * the buffer where the array is stored staring from <code>offset</code> 
     * and ending after <code>length</code> chars 
     * use direct access to class fields only for reading
     */
    private char[] m_buffer;
    private int m_startPos;
    private int m_endPos;

    /**
     * create a CharArray with a initial size
     * @param aInitialSize the size to be allocate
     * <br>Notice: set a initial size big enough in order that the internal buffer 
     * to not be reallocate
     */
    public CharArray(int aInitialSize) {
        m_buffer = new char[aInitialSize];
        m_startPos = 0;
        m_endPos = 0;
    }

    /**
     * reset the CharArray to initial state. This can be used for performance
     * reasons when you don't want to create new objects.
     */
    public void empty() {
        m_endPos = 0;
        m_startPos = 0;
    }

    /**
     * set start & end bounds in order to get a part of the initial array
     * 
     * @param aStartPos
     * @param aEndPos
     * <p>Notice: the StartPos and EndPos are absolute values</p>
     */
    public void setBounds(int aStartPos, int aEndPos) {
        m_startPos = aStartPos;
        m_endPos = aEndPos;
    }

    /**
     * set a new offset an return this    
     * @param aOffset - the new offset relative to the old offset
     * @return this
     * <p>Notice: This method modify the current object instead of allocating 
     * a new one</p>
     * @throws IndexOutOfBoundsException if the new offset is past the end.
     */
    public CharArray subCharArray(int aOffset) {
        //!! start from the left offset
        m_startPos += aOffset;
        if (m_startPos > m_endPos ) { 
            throw new IndexOutOfBoundsException();
        }

        return this;
    }

    /**
     * resets the start and end of the CharArray and returns it.
     * 
     * @param start - the new start position
     * @param length - the new length of the CharArray. 
     * @return this
     */
    public CharArray subCharArray(int start, int length) {
        m_startPos += start;
        m_endPos = m_startPos + length;
        if (m_startPos > m_endPos ) { 
            throw new IllegalArgumentException("invalid (start, end): " + start
                    + ", " + length);
        }
        return this;
    }

    private void ensureSize(int aEndPos) {
        if (aEndPos >= m_buffer.length) {
            char[] bigger = new char[aEndPos * 4 / 3 + 4];
            System.arraycopy(m_buffer, 0, bigger, 0, m_buffer.length);
            m_buffer = bigger;

            if(LOG.isTrace()) {
                LOG.trace("CharArray.ensureSize()", "buffer size increase to: " + m_buffer.length);
            }
        }
    }

    /**
     * add the char at end of the array.
     * @param aChar
     */
    public void append(char aChar) {
        ensureSize(m_endPos);
        m_buffer[m_endPos++] = aChar;
    }

    /**
     * add String at end of the array.
     * @param aString
     */
    public void append(String aString) {
        if (aString == null || (aString.length() == 0)) {
            //nothing to append
            return;
        }

        ensureSize(m_endPos + aString.length());
        aString.getChars(0, aString.length(), m_buffer, m_endPos);
        m_endPos += aString.length();
    }

    /**
     * compare with a string
     * @param aString the string to compare
     * @return true if the char arrays are equal,
     *         false if not 
     */
    public boolean equals(String aString) {
        if (aString == null) {
            return false;
        }

        int i = m_endPos - m_startPos;
        if (aString.length() == i) {
            int j = m_endPos;
            while (i!= 0) {
                if (aString.charAt(--i) != m_buffer[--j]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * search for a match in an array of strings.
     * @param aStringArray the array of possible strings.   
     * @return the position in aStringArray if is found,
     *         -1 if not.
     */
    public int indexIn(String[] aStringArray) {
        if (aStringArray == null) {
            return -1;
        }
        int i = aStringArray.length;
        while (i!=0) {
            if (equals(aStringArray[--i])) {
                return i;
            }
        }
        return -1; // if was not found
    }

    /**
     * convert to a boolean value.
     * @return the boolean value represented. 
     * <p>Notice: return false if dosen't represent a boolean value</p>
     */
    public boolean booleanValue() {
        return equals("true");
    }

    /**
     * convert to a int value.
     * @return the int value represented.
     * @throws NumberFormatException if dosn't represent a int value.
     */
    public int intValue() throws NumberFormatException {
        if (m_endPos == 0) {
            throw new NumberFormatException("iEndPos == 0");
        }

        int result = 0;
        int digit;
        boolean negative = false;
        int j = m_startPos;
        if (m_buffer[j] == '-') {
            negative = true;
            j++;
        }
        for(;j !=m_endPos; j++) {
            digit = Character.digit(m_buffer[j], 10);
            if (digit < 0) {
                throw new NumberFormatException(this.toString());
            }
            if (result == 0) {
                result = digit;
            } else {
                digit += result << 1; //r*2 + digit
                result <<= 3; //r*8
                result += digit; //r*8 + r*2 + digit
            }
        }
        if (negative) {
            result = -result;
        }
        return result;
    }

    /**
     * convert from a hex representation to an int value. The text in the
     * CharArray must not start with "0x". Only a string like "ABCDE7" is
     * handled.
     * @return the int value represented.
     * @throws NumberFormatException if dosen't represent a int value.
     */
    public int intValueFromHex() throws NumberFormatException {
        if (m_endPos == 0) {
            throw new NumberFormatException("iEndPos == 0");
        }
        int result = 0;
        int digit;
        boolean negative = false;
        int j = m_startPos;
        if (m_buffer[j] == '-') {
            negative = true;
            j++;
        }
        for(;j !=m_endPos; j++) {
            digit = Character.digit(m_buffer[j], 16);
            if (digit < 0) {
                throw new NumberFormatException(this.toString());
            }
            if (result == 0) {
                result = digit;
            } else {
                result <<= 4; //r*16
                result += digit; //r*16 + digit
            }
        }
        if (negative) {
            result = -result;
        }
        return result;
    }

    /**
     * @return the length of the representable String.
     */
    public int length() {
        return m_endPos - m_startPos;
    }

    /**
     * @param aIndex the position. 
     * @return the character at index position.
     * <p>Notice:the index must be relative to current offset</p> 
     */
    public char charAt(int aIndex) {
        return m_buffer[m_startPos+aIndex];
    }

    /**
     * @return the String representation.
     */
    public String toString() {
        if (m_endPos != 0) {
            return new String(m_buffer, m_startPos,m_endPos - m_startPos);
        } else {
            return "";
        }
    }
    /**
     * approx copy of the method from JDK1.2 :-D
     * @param string1
     * @param string2
     * @return 0 if the strings are ignorecase equal
     * @see java.lang.String#compareToIgnoreCase from JDK 1.2 or grater
     */
    public static int compareIgnoreCase(String string1, String string2) {
        if (string1 == null || string2 == null) {
            return 0;
        }
        int len1 = string1.length();
        int len2 = string2.length();
        for (int i = 0; i < len1 && i < len2; i ++) {
            char char1 = string1.charAt(i);
            char char2 = string2.charAt(i);
            if (char1 != char2) {
                char1 = Character.toUpperCase(char1);
                char2 = Character.toUpperCase(char2);
                if (char1 != char2) {
                    // as stated by the javadoc,
                    // Note that
                    // Character.isUpperCase(Character.toUpperCase(codePoint))
                    // does not always return true                    
                    // that's why use double conversion
                    char1 = Character.toLowerCase(char1);
                    char2 = Character.toLowerCase(char2);
                    if (char1 != char2) {
                        return char1 - char2;
                    }
                }
            }
        }
        return len1 - len2;
    }

    /**
     * search for the first occurrence of the given char 
     * 
     * @param aChar
     * @return returns the first index of the char c or -1 if it not found
     */
    public int indexOf(char aChar) {
        for (int i = m_startPos; i < m_endPos; i ++) {
            if (m_buffer[i] == aChar) {
                return i - m_startPos;
            }
        }

        return -1;
    }

    /**
     * @return the start position in the buffer.
     */
    public int getStart() {
        return m_startPos;
    }

    /**
     * @return the end position in the buffer.
     */
    public int getEnd() {
        return m_endPos;
    }
    
    /**
     * @return the internal buffer  
     */
    public char[] getInternalBuffer(){
        return m_buffer;
    }

    /**
     * removes all the chars <= ' ' from the beginning and end of the
     * chararray. Tabs and other whitespace are not affected.
     * @return this
     */
    public CharArray trim() {
        while (m_endPos > 0 && m_buffer[m_endPos - 1] <= ' ') {
            m_endPos--;
        }
        if (m_endPos > m_startPos) {
            while (m_buffer[m_startPos] <= ' ') {
                m_startPos++;
            }
        }

        return this;
    }

    /**
     * Returns true if the char array starts with the given string.
     * If the given string is longer than the char array, returns false.
     * @param aString the string to be checked for start
     * @throws NullPointerException if aString is null
     * @return true if aString == ""
     */
    public boolean startsWith(String aString) {
        if (aString.length() > m_endPos - m_startPos)
            return false;
        for (int i = 0; i < aString.length(); i ++) {
            if (m_buffer[m_startPos + i] != aString.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
