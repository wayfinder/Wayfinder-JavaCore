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

package com.wayfinder.pal.persistence;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface WFFileConnection {
    
    /**
     * Open and return a data input stream for a connection.
     * 
     * @return An open input stream 
     * @throws IOException 
     */
    public DataInputStream openDataInputStream() throws IOException;
    
    
    /**
     * Open and return a data output stream for a connection. 
     * If the path and file dosen't exist a new file will be created. 
     * <p>
     * Note: If the file exist will be overwritten 
     * use {@link #openDataOutputStream(boolean)} in order to append.
     * 
     * @return An open output stream
     * @throws IOException 
     */
    public DataOutputStream openDataOutputStream() throws IOException;

    /**
     * Open and return a data output stream for a connection.
     * If the path and file dosen't exist a new file will be created.
     * 
     * @param append true is the file should be appended
     * false if the file will be overwritten 
     * 
     * @return An open output stream
     * @throws IOException 
     */
    public DataOutputStream openDataOutputStream(boolean append) throws IOException;
    
    /**
     * Deletes the file or directory that has been specified when
     * creating the FileConnection 
     * 
     * 
     * @return true if the file or directory is successfully deleted, false otherwise. 
     */
    public boolean delete();
    
    /**
     * Return the total number of bytes in the file. 
     * 
     * @return the total number of bytes in the file. 
     */
    public int fileSize();
    
    /**
     * Close the file. 
     * 
     * @throws IOException
     */
    public void close() throws IOException;
    
    /**
     * Tests whether the file or directory denoted by this abstract pathname exists. 
     * 
     * @return true if the file or directory exist, false if not. 
     */
    public boolean exists();

}
