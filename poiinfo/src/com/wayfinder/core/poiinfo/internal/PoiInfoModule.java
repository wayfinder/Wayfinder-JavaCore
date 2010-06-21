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
package com.wayfinder.core.poiinfo.internal;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.poiinfo.PoiInfoInterface;
import com.wayfinder.core.poiinfo.PoiInfoListener;
import com.wayfinder.core.shared.RequestID;

/**
 * 
 *
 */
public class PoiInfoModule implements PoiInfoInterface {
    
    private final ModuleData m_module;
    private final SharedSystems m_sys;
    private final MC2Interface m_mc2;
    
    /**
     * @param module
     * @param sys
     * @param mc2Ifc
     */
    private PoiInfoModule(ModuleData module, SharedSystems sys, MC2Interface mc2Ifc) {
        m_module = module;
        m_sys = sys;
        m_mc2 = mc2Ifc;
    }

    public static PoiInfoInterface createPoiInfoInterface(
            ModuleData module, SharedSystems sys, MC2Interface mc2Ifc) {
        return new PoiInfoModule(module, sys, mc2Ifc);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.poiinfo.PoiInfoInterface#requestInfo(java.lang.String, com.wayfinder.core.poiinfo.PoiInfoListener)
     */
    public RequestID requestInfo(String itemID, PoiInfoListener listener) {
        if (itemID == null || listener == null) {
            throw new IllegalArgumentException();
        }
        
        final RequestID reqID = RequestID.getNewRequestID();
        m_mc2.pendingMC2Request(
                new PoiInfoMC2Request(
                        reqID, 
                        itemID, 
                        m_sys.getSettingsIfc().getGeneralSettings(), 
                        m_module.getCallbackHandler(), 
                        listener));
        
        return reqID;
    }

}
