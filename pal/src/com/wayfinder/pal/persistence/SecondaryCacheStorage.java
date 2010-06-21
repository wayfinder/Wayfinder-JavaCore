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
import java.io.IOException;

/**
 * A interface used for the secondary map cache. The secondary map cache will 
 * contain 10 pages where each page has the size specified by {@link SecondaryCacheStorage#getMaxPageSize()}. 
 * 
 * 
 * 
 *
 */
public interface SecondaryCacheStorage {
    
    /**
     * Return the number of bytes that the cache storage can contain. 
     * <p>
     * The number of bytes returned will be cached in the memory before it's saved to
     * the cache storage via {@link SecondaryCacheStorage#writeToStorage(byte[], int, int)}
     * 
     * @return the number of bytes that the cache storage can contain. 
     */
    public int getMaxPageSize();
    
    /**
     * Open and return an input stream for the cache storage. 
     * 
     * @return a input stream. 
     * @throws IOException if an I/O error occurs. 
     */
    public DataInputStream getDataInputStream() throws IOException;
    
    /**
     * 
     * Writes length bytes from the specified byte array starting at offset offset to this output stream. 
     * 
     * @param data the data. 
     * @param offset the start offset in the data.
     * @param length the number of bytes to write. 
     * @return the number of bytes that has been written to the output stream. 
     * @throws IOException if an I/O error occurs.
     */
    public boolean writeToStorage(byte []data, int offset, int length) throws IOException ;
    
    /**
     * Close the cache storage. 
     * 
     * @return true if the cache storage was successfully closed, false if not. 
     */
    public boolean close();
    
    /**
     * Return the number of bytes saved in the cache storage. 
     * 
     * @return the number of bytes saved in the cache storage.
     */
    public int size();
    
    /**
     * Delete the cache storage. 
     * 
     * @return true if the cache storage was successfully deleted, false if not. 
     */
    public boolean delete();
    
}
