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
package com.wayfinder.pal.persistence;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * Interface used to save and load module data and settings.  
 *
 */
public interface SettingsConnection {

    /**
     * Open a DataInputStream for the recordId specified by the parameter. 
     * 
     * @param recordId
     * @return a DataInputStream or null if the record doesn't exist. 
     * @throws IOException if an I/E exception occur when reading from the record. 
     */
    public DataInputStream getDataInputStream(int recordId) throws IOException;
    
    /**
     * Open a DataOutputStream for the settings record specified by the parameter.
     * The recordId act as a unique identifier for the setting for those cases when
     * we don't want to save all settings in one record.
     * <p>
     * Old data saved in recordId will be replaced. 
     * 
     * @param recordId the record/file where the data should be saved.
     * @return a DataOutputStream
     * @throws IOException if an I/O exception occur when reading from the record. 
     */
    public DataOutputStream getOutputStream(int recordId) throws IOException;
    
    /**
     * Return the next id of the next available record. 
     * 
     * @return the next id of the next available record.
     */
    public int getNextRecordId();
    
    /**
     * Return the max size (in bytes) that can be saved in the record. 
     * 
     * @return the max size (in bytes) that can be saved in the record.
     */
    public int getMaxSize();
    
    /**
     * Close the connection, will always be called. 
     */
    public void close() throws IOException;
    
}
