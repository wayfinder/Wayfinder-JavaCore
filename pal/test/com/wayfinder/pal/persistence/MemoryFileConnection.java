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
package com.wayfinder.pal.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * WFFileConnection implementation to be used for JUnit tests. The idea is to
 * mainly test write/read consistency, so first write to the connection then
 * attempt to read from it.
 * 
 * 
 *
 */
public class MemoryFileConnection implements WFFileConnection {
    
    private byte[] m_buffer;
    private ByteArrayInputStream m_bais;
    private ByteArrayOutputStream m_baos;
    
    private boolean exists;
    
    public MemoryFileConnection() {
        m_buffer = new byte[0];
        m_bais = new ByteArrayInputStream(m_buffer);
        m_baos = new ByteArrayOutputStream();
        exists = true;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#close()
     */
    public void close() throws IOException {
        // 

    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#delete()
     */
    public boolean delete() {
        exists = false;
        return true;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#exists()
     */
    public boolean exists() {
        return exists;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#fileSize()
     */
    public int fileSize() {
        //update buffer
        m_buffer = m_baos.toByteArray();
        return m_buffer.length;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#openDataInputStream()
     */
    public DataInputStream openDataInputStream() throws IOException {
        if (m_baos != null) {
            m_buffer = m_baos.toByteArray();
            m_bais = new ByteArrayInputStream(m_buffer);
            return new DataInputStream(m_bais);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#openDataOutputStream()
     */
    public DataOutputStream openDataOutputStream() throws IOException {
        m_baos = new ByteArrayOutputStream();
        return new DataOutputStream(m_baos);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.WFFileConnection#openDataOutputStream(boolean)
     */
    public DataOutputStream openDataOutputStream(boolean append)
            throws IOException {
        if (append) {
            return new DataOutputStream(m_baos);
        }
        else {
            return openDataOutputStream();
        }
    }
}
