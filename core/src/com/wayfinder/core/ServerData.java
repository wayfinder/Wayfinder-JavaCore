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
package com.wayfinder.core;

import com.wayfinder.core.shared.internal.ParameterValidator;


/**
 * Contains data required by the Core to connect to and talk with the server.
 * 
 */
public final class ServerData {

    private final String m_clientType;
    private final String m_versionNumber;
    private final String[] m_defaultHostArray;
    private final int[] m_defaultPortArray;

    
    /**
     * Standard constructor.
     * 
     * @param clientType The client type of the application 
     * @param versionNumber The version number of the application.
     * @param defaultHostNames An array containing host name (IP or DNS name) 
     * to connect, should not be null or empty 
     * @param defaultPorts An array containing the ports available for the given
     * host names if null default port will be used
     */
    public ServerData(String clientType, String versionNumber, String[] defaultHostNames, int[] defaultPorts) {
        if(ParameterValidator.isEmptyString(clientType)) {
            throw new IllegalArgumentException("Client type cannot be empty");
        } else if(ParameterValidator.isEmptyString(versionNumber)) {
            throw new IllegalArgumentException("Version number cannot be empty");
        } else if(ParameterValidator.isEmptyArray(defaultHostNames)) {
            //do't try 
            throw new IllegalArgumentException("Default urls cannot be empty");
        }
        
        m_clientType = clientType;
        m_versionNumber = versionNumber;
        
        // copy the url array, we cannot rely on the object being left alone
        // since the API may be sent outside of the company
        m_defaultHostArray = new String[defaultHostNames.length];
        System.arraycopy(defaultHostNames, 0, m_defaultHostArray, 0, defaultHostNames.length);
        // not try to generate them automatically as this will hide the url 
        // from the application
        
        if (defaultPorts == null || defaultPorts.length == 0) {
            m_defaultPortArray = new int[]{80};
        } else {
            m_defaultPortArray = new int[defaultPorts.length];
            System.arraycopy(defaultPorts, 0, m_defaultPortArray, 0, defaultPorts.length);
        }
    }
    

    /**
     * Returns the client type of the application.
     * 
     * @return the client type of the application.
     */
    public String getClientType() {
        return m_clientType;
    }
    

    /**
     * Returns the version number of the application.
     * 
     * @return the version number of the application.
     */ 
    public String getVersionNumber() {
        return m_versionNumber;
    }
    
    
    /**
     * Returns the default host names
     * 
     * @return An array with the default host names
     */
    public String[] getDefaultHostNames() {
        String[] returnedArray = new String[m_defaultHostArray.length];
        System.arraycopy(m_defaultHostArray, 0, returnedArray, 0, m_defaultHostArray.length);
        return returnedArray;
    }
    
    
    /**
     * Returns the default ports
     * 
     * @return An array with the default ports
     */
    public int[] getDefaultPorts() {
        int[] returnedArray = new int[m_defaultPortArray.length];
        System.arraycopy(m_defaultPortArray, 0, returnedArray, 0, m_defaultPortArray.length);
        return returnedArray;
    }
}
