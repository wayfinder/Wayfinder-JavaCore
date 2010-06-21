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

package com.wayfinder.pal;

import com.wayfinder.pal.concurrency.ConcurrencyLayer;
import com.wayfinder.pal.debug.LogHandler;
import com.wayfinder.pal.hardwareinfo.HardwareInfo;
import com.wayfinder.pal.network.NetworkLayer;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.positioning.PositioningLayer;
import com.wayfinder.pal.softwareinfo.SoftwareInfo;
import com.wayfinder.pal.sound.SoundLayer;
import com.wayfinder.pal.util.UtilFactory;

/**
 * This class represents the entry point into the Platform Abstraction Layer
 * 
 */
public abstract class PAL {
    
    private final LogHandler m_logHandler;
    private final ConcurrencyLayer m_concurrencyInfo;
    private final NetworkLayer m_networkLayer;
    private final HardwareInfo m_hardwareInfo;
    private final PersistenceLayer m_persistenceLayer;
    private final SoftwareInfo m_softwareInfo;
    private final UtilFactory m_utilFactory;
    private final SoundLayer m_soundLayer;
    private final PositioningLayer m_posLayer;

    protected PAL(LogHandler loghandler, 
            ConcurrencyLayer cInfo, 
            NetworkLayer netLayer,
            HardwareInfo hardwareInfo,
            SoftwareInfo softInfo,
            PersistenceLayer perLayer,
            UtilFactory utilFactory,
            SoundLayer soundLayer, 
            PositioningLayer posLayer) {
        
        m_logHandler = loghandler;
        m_concurrencyInfo = cInfo;
        m_networkLayer = netLayer;
        m_hardwareInfo = hardwareInfo;
        m_softwareInfo = softInfo;
        m_persistenceLayer = perLayer;
        m_utilFactory = utilFactory;
        m_soundLayer = soundLayer;
        m_posLayer = posLayer;
    }
    
    
    /**
     * Returns a reference to the {@link LogHandler}
     * <p>
     * <b>Do not use this directly for log events. Use the {@link Logger} 
     * instead</b>
     * 
     * @return The {@link LogHandler}
     */
    public final LogHandler getLogHandler() {
        return m_logHandler;
    }
    
    
    /**
     * Returns a reference to the {@link ConcurrencyLayer}
     * <p>
     * If you need to create a {@link Thread}, do not use this directly. Use
     * {@link WorkScheduler#startThread(Runnable)} instead.
     * 
     * @return The {@link ConcurrencyLayer}
     */
    public ConcurrencyLayer getConcurrencyLayer() {
        return m_concurrencyInfo;
    }
    
    
    /**
     * Returns a reference to the {@link NetworkLayer}
     * 
     * @return The {@link NetworkLayer}
     */
    public NetworkLayer getNetworkLayer() {
        return m_networkLayer;
    }
    
    
    /**
     * Returns a reference to the {@link HardwareInfo}
     * <p>
     * This can be used to obtain information regarding the hardware capabilites
     * and info of the currently running platform.
     * 
     * @return The {@link HardwareInfo} implementation
     */
    public HardwareInfo getHardwareInfo() {
        return m_hardwareInfo;
    }
    
    
    /**
     * Returns a reference to the {@link SoftwareInfo}
     * <p>
     * This can be used to obtain information regarding the capabilities and
     * features of the currently running operating system and device firmware.
     * 
     * @return The {@link SoftwareInfo} implementation
     */
    public SoftwareInfo getSoftwareInfo() {
        return m_softwareInfo;
    }
    
    
    
    /**
     * Returns a reference to the {@link PersistenceLayer}
     * <p> 
     * 
     * @return The {@link PersistenceLayer} implementation
     */
    public PersistenceLayer getPersistenceLayer() {
        return m_persistenceLayer;
    }
    
    
    public UtilFactory getUtilFactory() {
        return m_utilFactory;
    }
    
    public SoundLayer getSoundLayer() {
        return m_soundLayer;
    }
    
    public PositioningLayer getPositioningLayer() {
        return m_posLayer;
    }
    
    /**
     * A hint to the platform that now would be a good time to invoke a
     * garbage collection. It's up to the implementing PAL part to decide
     * wether or not to move forward with the request or ignore it.
     * <p>
     * This method will be used by the Core instead of the ordinary  
     * {@link System#gc()} method.
     */
    public abstract void requestGC();

    /**
     * Create a file where all the debug messages will be logged.
     * The location and name of the file is specific to each platform.
     * The method donsen't block. Call this method before create the core 
     * to not loose any messages. 
     * 
     * <p>
     * WARNING: Debug message will be written directly to file, this can caused
     * small delays each time when there is a debug message
     */
    public void enableFileLoggingAsynch() {
        //TOOD add the method signature to PAL interface 
        //add startFileLogging() method signature to LogHandler to make it 
        //general for all platforms
        getConcurrencyLayer().startNewDaemonThread(
                new Runnable() {
                    public void run() {
                        getLogHandler().startFileLogging(); 
                    }
                }, "LogHandlerInitFile");
    }
}
