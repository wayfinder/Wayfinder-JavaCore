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

/**
 * 
 */
package com.wayfinder.core.geocoding.internal;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.geocoding.GeocodeInterface;
import com.wayfinder.core.geocoding.GeocodeListener;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;

/**
 * Module for implementing GeocodeInterface.
 */
public final class GeocodeModule implements GeocodeInterface {

    private final ModuleData m_moduleData;
    private final SharedSystems m_sharedSystems;
    private final MC2Interface m_mc2Ifc;


    /**
     * Method for core factory to create the module.
     * 
     * @param moduleData
     * @param sharedSystems
     * @param mc2Ifc
     * @return
     */
    public static GeocodeInterface
        createGeocodeInterface(ModuleData moduleData,
                               SharedSystems sharedSystems,
                               MC2Interface mc2Ifc) {
        return new GeocodeModule(moduleData, sharedSystems, mc2Ifc);
    }

    /**
     * Private constructor. Outsiders must use the static factory method
     * instead.
     * 
     * @param moduleData
     * @param sharedSystems
     * @param mc2Ifc
     */
    private GeocodeModule(ModuleData moduleData,
                               SharedSystems sharedSystems,
                               MC2Interface mc2Ifc) {
        m_moduleData = moduleData;
        m_sharedSystems = sharedSystems;
        m_mc2Ifc =  mc2Ifc;
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.geocoding.GeocodeInterface#reverseGeocode(com.wayfinder.core.shared.Position, com.wayfinder.core.geocoding.GeocodeListener)
     */
    public RequestID reverseGeocode(Position position, GeocodeListener listener) {
        if (position == null || listener == null || ! position.isValid()) {
            throw new IllegalArgumentException();
        }

        final RequestID id = RequestID.getNewRequestID();
        m_mc2Ifc.pendingMC2Request(
                new ExpandMC2Request(id,
                                    position,
                                    m_moduleData.getCallbackHandler(),
                                    m_sharedSystems.getSettingsIfc()
                                    .getGeneralSettings(),
                                    listener));

        return id;
    }
}
