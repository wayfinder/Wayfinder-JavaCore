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
package com.wayfinder.core.internal;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.NetworkModule;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.impl.MC2Module;
import com.wayfinder.core.shared.internal.persistence.PersistenceModule;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.userdata.internal.UserModule;
import com.wayfinder.pal.PAL;


/**
 * Collection class for systems shared by multiple modules in the Core
 */
public final class SharedSystems {
    
    private final PAL m_pal;
    private final WorkScheduler m_scheduler;
    private final PersistenceModule m_PersistentModule;
    private final InternalSettingsInterface m_settingsIfc;
    private final InternalUserDataInterface m_usrDatIfc;
    private final InternalNetworkInterface m_networkIfc;
    private final MC2Interface m_mc2Ifc;

    public SharedSystems(ModuleData data) {
        m_pal = data.getPAL();
        m_scheduler = new WorkScheduler(m_pal.getConcurrencyLayer(), 
                                        WorkScheduler.POOL_LIMIT_DEFAULT);
        m_PersistentModule = new PersistenceModule(m_scheduler, m_pal.getPersistenceLayer());
        m_settingsIfc = GeneralSettingsHolder.createInternalSettingsInterface(m_scheduler, m_pal);
        
        // === INTERFACES THAT ARE MOSTLY INTERNAL ===
        
        m_usrDatIfc = UserModule.createUserModule(data, this);
        m_networkIfc = NetworkModule.createNetworkInterface(data, this, getUsrDatIfc());
        m_mc2Ifc = MC2Module.createIsabInterface(data, this, getNetworkIfc(), getUsrDatIfc());
    }
    
    
    /**
     * Returns the PAL currently in use
     * 
     * @return The {@link PAL} in use
     */
    public PAL getPAL() {
        return m_pal;
    }
    
    
    /**
     * Returns the thread pool
     * 
     * @return The {@link WorkScheduler}
     */
    public WorkScheduler getWorkScheduler() {
        return m_scheduler;
    }

    
    /**
     * Returns the persistent module, used for saving data
     * 
     * @return The {@link PersistenceModule}
     */
    public PersistenceModule getPersistentModule() {
        return m_PersistentModule;
    }
    
    
    /**
     * Returns the interface responsible to obtaining and changing global
     * settings
     * 
     * @return The {@link InternalSettingsInterface}
     */
    public InternalSettingsInterface getSettingsIfc() {
        return m_settingsIfc;
    }


    public InternalUserDataInterface getUsrDatIfc() {
        return m_usrDatIfc;
    }


    public InternalNetworkInterface getNetworkIfc() {
        return m_networkIfc;
    }


    public MC2Interface getMc2Ifc() {
        return m_mc2Ifc;
    }
    
}
