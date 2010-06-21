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

import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.pal.error.PermissionsException;

public interface PersistenceLayer {
    
    /**
     * Open a file or a directory with the full path specified by the parameter. 
     * <p>
     * Note: this will not create the file nor the path to it. 
     * Before reading from the file check existence by using 
     * {@link WFFileConnection#exists()}.
     *  
     * @param path the full path to the file or directory. 
     * @return the file wrapped in a {@link FileConnection}
     * @throws IOException if the file or directory doesn't exist. 
     * @throws PermissionsException if the operation is not allowed
     */
    public WFFileConnection openFile(String path) throws IOException, PermissionsException;
    
    /**
     * Obtain an InputStream from a resource file. Temporarily used to load data
     * for dummy implementation of RouteModule 
     * 
     * @param resource the name (not the path) of the resource file
     * @return an {@link InputStream} to read the resource data from
     * @throws IOException if the resource is not found
     */
    public InputStream getResourceAsStream(String resource) throws IOException;
    
    /**
     * Open a setting connection with setting type specified by the parameter. 
     * 
     * @param settingsType the setting type that identify the setting. 
     * @return a setting connection object. 
     */
    public SettingsConnection openSettingsConnection(String settingsType)
    throws PermissionsException;
    
    /**
     * Open a secondary cache storage with the name specified by the parameter. 
     * 
     * @param name the name to identify the cache storage with. 
     * @return a secondary cache storage object. 
     * @throws IOException if an I/O error occur.
     */
    public SecondaryCacheStorage openSecondaryCacheStorage(String name) 
    throws IOException, PermissionsException;
    
    /**
     * Set the base file directory that will be used in core.
     * The path should not be null and should end with '/' 
     * 
     * Note: This will be only used for specific methods like 
     * {@link #openSecondaryCacheStorage(String)} and 
     * {@link #openSettingsConnection(String)} 
     * but not general methods like 
     * {@link #openFile(String)} and {@link #listFiles(String, String)}  
     * 
     * @param path the full path to the base directory. 
     * e.g. <pre>
     * "C:/wayfinder/data/" //on j2se 
     * "file:///store/home/user/wayfinder/" //on BB
     * Context.getFilesDir().getAbsolutePath() + '/' //on Android 
     * </pre>
     * 
     */
    public void setBaseFileDirectory(String path);
    
    /**
     * Return the absolute path to the base directory used in the core. 
     * It will always end with '/'.
     * This is were all user related data will be stored.  
     * 
     * @return the absolute path to the base directory used in the core. 
     */
    public String getBaseFileDirectory();
    
    /**
     * Return a array of all file names extension from folder
     * specified by the parameters.
     * 
     * @param folder the full path to the folder where to list files. 
     * Should be not null and end with '/'
     * @param extension the extension of the file.
     * @return a array of file names that match the extension specified.
     */
    public String[] listFiles(String folder, String extension);

}
