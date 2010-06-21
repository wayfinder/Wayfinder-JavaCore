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
package com.wayfinder.core.search.internal.categorytree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.search.internal.SearchConstants;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceRequest;
import com.wayfinder.pal.persistence.SettingsConnection;


/**
 * Request for storage used for both loading and saving depending on which 
 * constructor is used.
 *  
 * 
 */
public class CategoryTreePersistenceRequest implements PersistenceRequest {

    private static final Logger LOG = LogFactory.getLoggerForClass(CategoryTreePersistenceRequest.class);
    
    /**
     * record id need by persistence module 
     *  
     */
    private static final int recordId = SearchConstants.PERSISTENCE_CATEGORIES_TREE;
    
    //grr... bad design 
    //one of this must be null
    LoadListener m_loadListener;
    CategoryTreeImpl m_catTreeToSave;
    
    /**
     * Ctr. for creating a write persistence request  
     * @param catTreeToSave The category tree that will be saved, cannot be null
     */
    CategoryTreePersistenceRequest(CategoryTreeImpl catTreeToSave) {
        m_catTreeToSave = catTreeToSave;
    }
    
    /**
     * Ctr. for creating a read persistence request  
     * @param loadListener LoadListener to be notified when read request has 
     * ended no matter the result, cannot be null
     */
    CategoryTreePersistenceRequest(LoadListener loadListener) {
        m_loadListener = loadListener;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.persistence.PersistenceRequest#error(CoreError)
     */
    public void error(CoreError coreError) {
        if (m_loadListener != null) {
            //if m_loadListener is not null this is a read request
            if(LOG.isWarn()) {
                LOG.warn("CategoryTreePersistenceRequest", "CategoryTree could not be restored " + coreError);
            }
            m_loadListener.loadDone(null);
        } else { 
            //if m_loadListener is null this is the error for write request
            if(LOG.isError()) {
                LOG.error("CategoryTreePersistenceRequest", "CategoryTree could not be saved " + coreError);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.persistence.PersistenceRequest#readPersistenceData(SettingsConnection)
     */
    public void readPersistenceData(SettingsConnection sConnection)
            throws IOException {
        DataInputStream dis = sConnection.getDataInputStream(recordId);
        try {
            CategoryTreeImpl tree = CategoryTreeImpl.read(dis);
            if(LOG.isInfo()) {
                LOG.info("CategoryTreePersistance.readPersistenceData()", "suceess");
            }
            m_loadListener.loadDone(tree);
        } finally {
            dis.close();
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.persistence.PersistenceRequest#writePersistenceData(SettingsConnection)
     */
    public void writePersistenceData(SettingsConnection sConnection)
            throws IOException {
        DataOutputStream dos = sConnection.getOutputStream(recordId);
        try {
            m_catTreeToSave.write(dos);
            if(LOG.isInfo()) {
                LOG.info("CategoryTreePersistance.writePersistenceData()", "success");
            }
        } finally {
            dos.close();
        }
    }
    
    /**
     * Internal use only, communicate from {@link CategoryTreePersistenceRequest} to 
     * {@link CategoryTreeHolder}
     * Could be move in a common package as is the same as the one use in favorite
     * 
     * 
     */
    static interface LoadListener {
        
        /**
         * Called after the restoring from storage no matter if was successfully or 
         * not  
         *  
         * @param obj The loaded Object if was successfully restored for storage or 
         * <code>null</code> if could not be restored  
         */
        void loadDone(Object obj);
    }
}
