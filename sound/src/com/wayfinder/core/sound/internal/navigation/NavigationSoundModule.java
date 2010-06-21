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
package com.wayfinder.core.sound.internal.navigation;

import java.io.IOException;

import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.route.internal.InternalRouteInterface;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface.InternalSettingsListener;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.internal.threadpool.Work;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.NavigationInfoListener;
import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.core.sound.NavigationSoundInterface;
import com.wayfinder.core.sound.internal.InternalSoundInterface;
import com.wayfinder.core.sound.internal.voicesyntax.SyntaxLoader;
import com.wayfinder.core.sound.internal.voicesyntax.SyntaxTree;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.sound.SoundException;
import com.wayfinder.pal.sound.SoundLayer;

/**
 * Loading the syntax file according to the core language when first route is 
 * initiated create and register a NavigationSoundHandler to the Navigation 
 * systems that will post turn sounds to the SoundModule   
 * 
 * 
 */
public class NavigationSoundModule implements NavigationSoundInterface, NavigationInfoListener, InternalSettingsListener {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(NavigationSoundModule.class);
    
    private static final int[] DISTANCE_METRIC_TABLE = {
        0,
        //25,
        //50,
        100,
        200,
        500,
        1000,
        2000
    };

    private static final int[] DISTANCE_IMPERIAL_UK = {
        0,
        //25,
        //50,
        91,
        182,
        402,
        803,
        1609,
    };

    private static final int[] DISTANCE_IMPERIAL_US = {
        0,
        //25,
        //50,
        61, //200 Feet
        152, // 500 Feet
        402, // Quarter of a mile
        803, // Half a mile
        1609 // Mile dudes
    };   
    
    private String m_dirPath = "";
    
    private String m_soundFileExtension = "";
    
    private final InternalRouteInterface m_routeIfc;
    
    private final InternalSoundInterface m_soundIfc;
    
    private final PersistenceLayer m_persistanceLayer;
    
    private final WorkScheduler m_sheduler;//used to load syntax;
    
    private LanguageInternal m_language;
    
    private int m_units;
    
    private NavigationSoundHandler m_handler;

    private SoundLayer m_soundLayer;

    private SyntaxTree m_syntaxTree;

    public static NavigationSoundInterface createNavigationInterface(
            InternalSoundInterface soundIfc, InternalRouteInterface routeIfc, SharedSystems systems) {
        return new NavigationSoundModule(soundIfc, routeIfc, 
                systems.getWorkScheduler(),
                systems.getSettingsIfc(),
                systems.getPAL().getPersistenceLayer(),
                systems.getPAL().getSoundLayer());
    }
    
    private NavigationSoundModule(InternalSoundInterface soundIfc, 
            InternalRouteInterface routeIfc, WorkScheduler scheduler,
            InternalSettingsInterface settingIfc,
            PersistenceLayer persistenceLayer, SoundLayer soundLayer) {
        
        if (soundIfc == null || routeIfc == null || scheduler == null || 
                settingIfc == null || persistenceLayer == null || soundLayer == null) {
            throw new IllegalArgumentException("NavigationSoundModule ctr all parameters must not be null");
        }
        m_soundIfc = soundIfc;
        m_routeIfc = routeIfc;
        m_sheduler = scheduler;
        m_persistanceLayer = persistenceLayer;
        m_soundLayer = soundLayer;
        
        GeneralSettingsInternal settings = settingIfc.getGeneralSettings();
        m_language = settings.getInternalLanguage();
        m_units = settings.getMeasurementSystem();
        m_routeIfc.addSyncNavInfoListener(this);
        
        settingIfc.registerSettingsListener(this);//watch for language, units changes  
    }
    
    private synchronized int[] getTurnDistances() {
        switch (m_units) {
        case GeneralSettings.UNITS_IMPERIAL_UK:  
            return DISTANCE_IMPERIAL_UK;
        case GeneralSettings.UNITS_IMPERIAL_US:
            return DISTANCE_IMPERIAL_US;
        case GeneralSettings.UNITS_METRIC:
            return DISTANCE_METRIC_TABLE;
        default:
            throw new IllegalArgumentException("Units must be one constant from  GeneralSettings");
        }
    }
    
    private synchronized void start(SyntaxTree tree) {
        m_syntaxTree = tree; 
        if(LOG.isInfo()) {
            LOG.debug("NavigationSoundModule.start()", m_syntaxTree.toString());
        }
        
        m_handler  = new NavigationSoundHandler(m_soundIfc, m_syntaxTree,  getTurnDistances());
        m_routeIfc.addSyncNavInfoListener(m_handler);
    }
    
    private synchronized boolean stop() {
        if (m_handler != null) {
            if(LOG.isInfo()) {
                LOG.info("NavigationSoundModule.stop()", "Stop the navigation sounds");
            }
            m_routeIfc.removeSyncNavInfoListener(m_handler);
            m_handler.resetRoute();
            m_handler = null;
            return true; 
        }
        return false;
    }
    
    class LoadSyntaxWorker implements Work {
        public int getPriority() {
            return 0;
        }

        public void run() {
            try {
                SyntaxTree tree = SyntaxLoader.loadSyntax(m_persistanceLayer, getDirPath(), getSoundExtension(), getSyntaxFileName());
                //TODO load & prepare each clip to find duration 
//                String[] paths = tree.getClipPathsInternalArray();
//                
//                int[] durations = m_soundLayer.getDuration(paths);
//                tree.setClipDurationNoCopyArray(durations);
                start(tree);
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("LoadSyntaxWorker.run()", "could not load syntax file " + e.toString());
                }
            }
//            } catch (SoundException e) {
//                if(LOG.isError()) {
//                    LOG.error("LoadSyntaxWorker.run()", "could not load all sound files " + e.toString());
//                }
//            }
            
        }

        public boolean shouldBeRescheduled() {
            return false;
        }
    }
    
    //hack to avoid starting on ctr. so start only when have a first navigationInfo
    //the sound will start with a small delay after first route
    public void navigationInfoUpdated(NavigationInfo info) {
        if(LOG.isDebug()) {
            LOG.debug("NavigationSoundModule.navigationInfoUpdated()", info.toString());
        }
        
        m_sheduler.schedule(new LoadSyntaxWorker());
        m_routeIfc.removeSyncNavInfoListener(this);
    }

    synchronized public void setSoundsAndSyntaxPath(String path, String soundExtension) {
        if (path == null) path = "";
        if (soundExtension == null) soundExtension="";
        if (!m_dirPath.equals(path) || !m_soundFileExtension.equals(soundExtension)) {
            m_dirPath = path;
            m_soundFileExtension = soundExtension;
            m_routeIfc.removeSyncNavInfoListener(this);
            if (stop()) { 
                if(LOG.isInfo()) {
                    LOG.info("NavigationSoundModule.setSoundsAndSyntaxPath()", "new path for sounds reload the sounds...");
                }
            }
            m_routeIfc.addSyncNavInfoListener(this);
        }
    }

    
    synchronized public void settingsUpdated(GeneralSettingsInternal settings) {
        if (!m_language.equals(settings.getInternalLanguage())) {
            m_routeIfc.removeSyncNavInfoListener(this);
            if (stop()) {
                if(LOG.isInfo()) {
                    LOG.info("NavigationSoundModule.settingsUpdated()", "new language detected reload the sounds syntax...");
                }
            }
            m_units = settings.getMeasurementSystem();
            m_language = settings.getInternalLanguage();
            m_routeIfc.addSyncNavInfoListener(this);
        } else if (m_units != settings.getMeasurementSystem()) {
            m_units = settings.getMeasurementSystem();
            //don't need to reload the syntax
            if (stop()) {
                if(LOG.isInfo()) {
                    LOG.info("NavigationSoundModule.settingsUpdated()", "new units detected restart the navigation handler...");
                }
                start(m_syntaxTree);
            }
        }
    }
    
    synchronized String getDirPath() {
        return m_dirPath;
    }
    
    synchronized String getSoundExtension() {
        return m_soundFileExtension;
    }
    
    synchronized String getSyntaxFileName() {
        return m_language.getWFSoundSyntaxCode();
    }
}
