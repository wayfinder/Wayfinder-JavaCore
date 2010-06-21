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

import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.pal.error.PermissionsException;

/**
 * PersistenceLayer implementation to be used in JUnit tests. You don't need
 * full PAL init to use this, this can be instantiated independently. It will
 * use byte arrays to simulate storage.
 * 
 * 
 *
 */
public class MemoryPersistenceLayer implements PersistenceLayer {
    
    public static PersistenceLayer getPersistenceLayer() {
        return new MemoryPersistenceLayer();
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#getBaseFileDirectory()
     */
    public String getBaseFileDirectory() {
        // 
        return "";
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String resource) throws IOException {
        // 
        return null;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#listFiles(java.lang.String, java.lang.String)
     */
    public String[] listFiles(String folder, String extension) {
        // 
        return new String[0];
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#openFile(java.lang.String)
     */
    public WFFileConnection openFile(String path) throws IOException,
            PermissionsException {
        // 
        return new MemoryFileConnection();
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#openSecondaryCacheStorage(java.lang.String)
     */
    public SecondaryCacheStorage openSecondaryCacheStorage(String name)
            throws IOException, PermissionsException {
        // 
        return null;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#openSettingsConnection(java.lang.String)
     */
    public SettingsConnection openSettingsConnection(String settingsType)
            throws PermissionsException {
        // 
        return new MemorySettingsConnection();
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.persistence.PersistenceLayer#setBaseFileDirectory(java.lang.String)
     */
    public void setBaseFileDirectory(String path) {
        // 

    }

}
