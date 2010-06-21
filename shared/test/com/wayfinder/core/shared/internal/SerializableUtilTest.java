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

import junit.framework.TestCase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class SerializableUtilTest extends TestCase {

    private ByteArrayOutputStream m_baos;
    private DataOutputStream m_dos;

    private final static String TEST_STRING = "foobar";
    
    protected void setUp() throws Exception {
        super.setUp();
        m_baos = new ByteArrayOutputStream();
        m_dos = new DataOutputStream(m_baos);
    }

    public void testWriteNonNull() throws IOException {
        // we should mock away DataOutput here.
        SerializableUtil.writeString(m_dos, TEST_STRING);
        byte[] buf = m_baos.toByteArray();
        // see protocol for DataOutput.writeUTF()
        assertEquals(2 + TEST_STRING.length(), buf.length);
        DataInput din =
            new DataInputStream(new ByteArrayInputStream(buf));
        String s = din.readUTF();
        assertEquals(TEST_STRING, s);
    }

    public void testWriteNull() throws IOException {
        // we should mock away DataOutput here.
        SerializableUtil.writeString(m_dos, null);
        byte[] buf = m_baos.toByteArray();
        // see protocol for DataOutput.writeUTF()
        assertEquals(2, buf.length);
        assertEquals(0, buf[0]);
        assertEquals(0, buf[1]);
    }


    public void testReadNonNull() throws IOException {
        m_dos.writeUTF(TEST_STRING);
        DataInput din =
            new DataInputStream(new ByteArrayInputStream(m_baos.toByteArray()));
        String s = SerializableUtil.readString(din);
        assertEquals(TEST_STRING, s);
    }

    public void testReadNull() throws IOException {
        byte[] buf = new byte[]{0, 0};
        DataInput din =
            new DataInputStream(new ByteArrayInputStream(buf));
        String s = SerializableUtil.readString(din);
        assertNull(s);
    }
}
