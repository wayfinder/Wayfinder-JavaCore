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
package com.wayfinder.core.network.internal.mc2.impl;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2StatusListener;


public final class MC2Module implements MC2Interface {

    private final ModuleData m_moduleData;
    private final MC2Storage m_storage;
    
    private final InternalNetworkInterface m_netIfc;
    private final SharedSystems m_systems;
    private final InternalUserDataInterface m_usrDatIfc;

    private MC2Module(ModuleData modData, SharedSystems systems, 
            InternalNetworkInterface netIfc, InternalUserDataInterface usrDatIfc) {
        m_moduleData = modData;
        m_usrDatIfc = usrDatIfc;
        m_systems = systems;
        m_netIfc = netIfc;
        m_storage = new MC2Storage();
    }
    
    
    /**
     * @param usrDatIfc 
     * @param aModData
     * @return
     * @hide Not for public use
     */
    public static MC2Interface createIsabInterface(ModuleData modData, 
            SharedSystems systems, InternalNetworkInterface netIfc, 
            InternalUserDataInterface usrDatIfc) {
        return new MC2Module(modData, systems, netIfc, usrDatIfc);
    }
    
    
    //-------------------------------------------------------------------------
    // posting of requests

    public void pendingMC2Request(MC2Request mc2Request) {
        pendingMC2Request(mc2Request, true, InternalNetworkInterface.PRIORITY_HIGH);
    }

    public void pendingMC2Request(MC2Request mc2Request, boolean joinable) {
        pendingMC2Request(mc2Request, joinable, InternalNetworkInterface.PRIORITY_HIGH);
    }

    public void pendingMC2Request(MC2Request mc2Request, boolean joinable, int priority) {
        if (joinable) {
            pendingJoinRequest(mc2Request, priority);
        } else {
            MC2Document doc = new MC2DocumentSingleRequest(m_systems, m_moduleData, m_storage, m_usrDatIfc, mc2Request);
            m_netIfc.pendingXmlRequest(doc, doc, priority);
        }
    }
    
    
    //-------------------
    // scheduling - too simple?
    
    private MC2DocumentMergeRequest currentMergeReqDoc; 
    
    private synchronized void pendingJoinRequest(MC2Request mc2Request, int priority) {
        if (currentMergeReqDoc != null && currentMergeReqDoc.offerRequest(mc2Request)) {
            //yupii we can merge
        } else {
            //current doc is null or is full or is already started to be sent
            //create a new merge doc 
            currentMergeReqDoc = new MC2DocumentMergeRequest(m_systems, m_moduleData, m_storage, m_usrDatIfc, mc2Request);
            //notice that priority will be of the first request.
            m_netIfc.pendingXmlRequest(currentMergeReqDoc, currentMergeReqDoc, priority);
        }
    }
    
    //-------------------------------------------------------------------------
    // MC2 document
    
    public void setMC2AuthAttribute(String attrib, String value) {
        m_storage.setMC2AuthAttribute(new MC2IsabAttribute(attrib, value));
    }
    
    
    public void addGlobalMC2StatusListener(MC2StatusListener listener) {
        m_storage.addMC2IsabStatusListener(listener);
    }
    
    
    public void removeGlobalMC2StatusListener(MC2StatusListener listener) {
        m_storage.removeMC2IsabStatusListener(listener);
    }
    
}
