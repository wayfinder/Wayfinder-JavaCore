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

package com.wayfinder.core.map.vectormap.internal;

import com.wayfinder.core.map.vectormap.PreInstalledMapsListener;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLoader;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 * 
 * Class for loading pre-installed map files form the folders specified. 
 * 
 */
public class PreInstalledMapLoader implements Runnable {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(PreInstalledMapLoader.class);
    
    private static final String FILTER = ".wfd";
    
    private TileMapLoader m_TileMapLoader;
    private VectorMapModule m_VectorMapModule;
    private PersistenceLayer m_persistenceLayer;
    private String []m_paths;
    private PreInstalledMapsListener m_PreInstalledMapsListener;
    
    public PreInstalledMapLoader(String []paths, TileMapLoader tileMapLoader, 
                                 VectorMapModule vmModule, PersistenceLayer perLayer,
                                 PreInstalledMapsListener listener) {
        m_TileMapLoader = tileMapLoader;
        m_VectorMapModule = vmModule;
        m_persistenceLayer = perLayer;
        m_paths = paths;
        m_PreInstalledMapsListener = listener;
    }

    public void run() {
        
        try {
            // Go throw all folders listed and load pre-installed maps. 
            for(int i=0; i<m_paths.length; i++) {
                // Get all ".wfd" files in the folder. 
                String []files = m_persistenceLayer.listFiles(m_paths[i], FILTER);
                
                // Add a "/" at the end of the path if it's missing. 
                if(!m_paths[i].endsWith("/")) {
                    m_paths[i] += "/";
                }
                
                // Add the map files to the map component if one or more map files has been found in the folder
                if(files != null && files.length > 0) {                
                    for(int j=0; j<files.length; j++) {
                        files[j] = m_paths[i]+files[j];
                    }                
                    m_TileMapLoader.setFileCaches(m_paths[i], files, m_PreInstalledMapsListener);                
                }
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("PreInstalledMapLoader.run()", e);
            }
        }
        
        // Give a callback when the loading of map files has been completed. 
        m_VectorMapModule.preInstalledMapsLoaded();
    }
}
