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
package com.wayfinder.core.wfserver.resource.internal;

import java.util.Vector;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.Work;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.core.wfserver.resource.CachedResourceManager;
import com.wayfinder.core.wfserver.resource.ResourceRequest;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 * 
 *
 */
public class CachedResourceManagerImpl implements CachedResourceManager, Work {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(CachedResourceManagerImpl.class);
    
    private final ModuleData m_module;
    private final SharedSystems m_sys;
    private final InternalNetworkInterface m_netIfc;
    private final PersistenceLayer m_persistence;
    private final String m_baseDir;
    
    private LinkedList m_taskList;
    
    private Runnable m_currentTask;
    private boolean m_running;
    private boolean m_cacheOpen;
    
    /**
     * the list of cached image files, created when opening the cache
     */
    private Vector m_imageFiles;
    
    /**
     * the list of other cached resource files, created when opening the cache
     */
    private Vector m_otherResFiles;
    
    public static CachedResourceManager createCachedResourceManager(
            ModuleData module, 
            SharedSystems sys, 
            InternalNetworkInterface netIfc) {
        
        return new CachedResourceManagerImpl(module, sys, netIfc);
    }

    /**
     * @param module
     * @param sys
     * @param netIfc
     */
    private CachedResourceManagerImpl(ModuleData module, SharedSystems sys,
            InternalNetworkInterface netIfc) {

        m_module = module;
        m_sys = sys;
        m_netIfc = netIfc;
        m_persistence = m_module.getPAL().getPersistenceLayer();
        
        StringBuffer sb = new StringBuffer();
        sb.append(m_persistence.getBaseFileDirectory());
        if (!m_persistence.getBaseFileDirectory().endsWith("/")) {
            sb.append("/");
        }
        sb.append(ResourceRequest.DIR_NAME_RESOURCES);
        m_baseDir = sb.toString();
        
        m_taskList = new LinkedList();
        m_currentTask = null;
        
        m_running = false;
        
        m_cacheOpen = false;
        
        m_imageFiles = new Vector();
        m_otherResFiles = new Vector();

        buildCacheIndex();
    }
    
    /**
     * list the files available in each resource dir
     */
    private void buildCacheIndex() {
        if (!m_cacheOpen) {
            String imgDir = m_baseDir + "/" + ResourceRequest.DIR_NAME_IMAGES;
            String otherDir = m_baseDir + "/" + ResourceRequest.DIR_NAME_OTHER;

            String[] images = m_persistence.listFiles(imgDir, "");
            String[] otherRes = m_persistence.listFiles(otherDir, "");
            
            if(LOG.isTrace()) {
                LOG.trace("CachedResourceManagerImpl.openCache()", 
                        "cached "+images.length+" images");
                LOG.trace("CachedResourceManagerImpl.openCache()", 
                        "cached "+images.length+" other resources");
            }
            
            if (images != null && images.length > 0) {
                for (int i = 0; i < images.length; i++) {
                    m_imageFiles.addElement(images[i]);
                }
            }
            
            if (otherRes != null && otherRes.length > 0) {
                for (int i = 0; i < otherRes.length; i++) {
                    m_otherResFiles.addElement(otherRes[i]);
                }
            }

        }
        m_cacheOpen = true;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.resource.CachedResourceManager#requestResource(com.wayfinder.core.resource.ResourceRequest)
     */
    public synchronized RequestID requestResource(ResourceRequest request) {
        RequestID reqID = RequestID.getNewRequestID();
        request.setRequestID(reqID);
        if (m_taskList.size() == 0 && m_currentTask == null) {
            if(LOG.isTrace()) {
                LOG.trace("CachedResourceManagerImpl.requestResource()", 
                        "first task");
            }
            
            m_currentTask = new ResourceRequestTask(
                    request, 
                    m_module.getCallbackHandler(), 
                    m_netIfc,
                    m_persistence, 
                    this);
        }
        else {
            ResourceRequestTask task = findTask(request.getResourceName());
            if (task != null) {
                if(LOG.isTrace()) {
                    LOG.trace("CachedResourceManagerImpl.requestResource()", 
                            "found task, adding request");
                }
                task.addRequest(request);
            }
            else {
                if(LOG.isTrace()) {
                    LOG.trace("CachedResourceManagerImpl.requestResource()", 
                            "adding new task to queue");
                }
                task = new ResourceRequestTask(
                        request, 
                        m_module.getCallbackHandler(), 
                        m_netIfc,
                        m_persistence,
                        this);
                m_taskList.addLast(task);
            }            
        }
        if (!m_running) {
            m_running = true;
            m_sys.getWorkScheduler().schedule(this);
        }
        
        return reqID;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.threadpool.Work#getPriority()
     */
    public int getPriority() {
        return WorkScheduler.PRIORITY_LOW;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.threadpool.Work#run()
     */
    public void run() {
        m_currentTask.run();
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.threadpool.Work#shouldBeRescheduled()
     */
    public boolean shouldBeRescheduled() {
        m_currentTask = popTask();
        return (m_currentTask != null);
    }
    
    private synchronized Runnable popTask() {
        if (m_taskList.size() > 0) {
            return (Runnable) m_taskList.removeFirst();
        } else {
            m_running = false;
            return null;
        }
    }
    
    /**
     * Looks in the task list for a task that is supposed to get the same 
     * resource.
     * 
     * @param resName
     * @return
     */
    private synchronized ResourceRequestTask findTask(String resName) {
        Object[] taskArray = m_taskList.toArray();
        for (int i = 0; i < taskArray.length; i++) {
            if (((ResourceRequestTask) taskArray[i]).getRequestedResourceName().equals(resName)) {
                if(LOG.isDebug()) {
                    LOG.debug("CachedResourceManagerImpl.findTask()", 
                            "found existing task for resource "+resName);
                }
                
                return (ResourceRequestTask) taskArray[i];
            }
        }
        return null;
    }
    
    /**
     * Check if a resource is among the stored files
     * @param resName the resource name (and file name at the same time)
     * @param resType the type of resource, either 
     * {@link ResourceRequest#RESOURCE_TYPE_IMAGE} or
     * {@link ResourceRequest#RESOURCE_TYPE_OTHER}
     * @return
     */
    synchronized boolean isResourceCached(String resName, int resType) {
        if (resType == ResourceRequest.RESOURCE_TYPE_IMAGE) {
            return m_imageFiles.contains(resName);
        }
        else {
            return m_otherResFiles.contains(resName);
        }
    }
    
    /**
     * Add the file name of a resource to the cached resources list
     * @param resName the resource name (and file name at the same time)
     * @param resType 
     */
    synchronized void addResourceToCacheList(String resName, int resType) {
        if (resType == ResourceRequest.RESOURCE_TYPE_IMAGE) {
            m_imageFiles.addElement(resName);
        }
        else {
            m_otherResFiles.addElement(resName);
        }
    }
}
