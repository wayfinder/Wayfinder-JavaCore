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
package com.wayfinder.core.shared.internal.settings;

import java.util.Enumeration;
import java.util.Vector;

import com.wayfinder.core.shared.internal.settings.language.LanguageFactory;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.pal.PAL;

public final class GeneralSettingsHolder implements InternalSettingsInterface {
    
    private final Vector m_internalListeners;
    private final PAL m_pal;
    private final WorkScheduler m_scheduler;
    private GeneralSettingsInternal m_currentSettings;

    private GeneralSettingsHolder(WorkScheduler scheduler, PAL pal){
        m_scheduler = scheduler;
        m_pal = pal;
        m_internalListeners = new Vector();
    }
    
    public static InternalSettingsInterface createInternalSettingsInterface(WorkScheduler scheduler, PAL pal) {
        return new GeneralSettingsHolder(scheduler, pal);
    }
    
    
    public synchronized GeneralSettingsInternal getGeneralSettings() {
        if(m_currentSettings == null) {
            m_currentSettings = createDefaultSettings();
        }
        return new GeneralSettingsInternal(m_currentSettings);
    }
    
    private GeneralSettingsInternal createDefaultSettings() {
        LanguageInternal lang = LanguageFactory.guessLanguageFromPlatform(m_pal.getSoftwareInfo());
        // two different settings to avoid havoc if the ui fipplar mit da
        // settings while core is working
        return new GeneralSettingsInternal(lang);
    }
    
    
    //-------------------------------------------------------------------------
    // internal
    

    public synchronized void registerSettingsListener(final InternalSettingsListener listener) {
        m_internalListeners.addElement(listener);
        
        m_scheduler.schedule(new Runnable() {
            public void run() {
                listener.settingsUpdated(getGeneralSettings());
            }
        });
    }
    

    public synchronized void removeSettingsListener(InternalSettingsListener listener) {
        m_internalListeners.removeElement(listener);
    }
    
    
    private synchronized void updateInternalListeners() {
        m_scheduler.schedule(new Runnable() {
            public void run() {
                Enumeration e = m_internalListeners.elements();
                GeneralSettingsInternal settings = getGeneralSettings();
                while (e.hasMoreElements()) {
                    final InternalSettingsListener listener = (InternalSettingsListener) e.nextElement();
                    listener.settingsUpdated(settings);
                }
            }
        });
    }
    
    
    private synchronized void updateSettings(GeneralSettingsInternal settings) {
        m_currentSettings = settings;
        updateInternalListeners();
    }
    
    
    public final class GeneralSettingsInternal extends GeneralSettings {

        /*
         * In the future we will be able to de-serialize setting from storage.
         * Then we must take care to not update the listeners until all the
         * settings are read back or the listeners will be stressed and might
         * act on the default values that then disappear.
         * 
         * Thus, commit needs to not be called until the last item is read back.
         */


        /**
         * Create GeneralSettingsInternal with a certain language, the default
         * measurement system for that language, and default values for
         * everything else.
         * 
         * @param lang - the language to set initially.
         */
        private GeneralSettingsInternal(LanguageInternal lang) {
            super(lang, lang.getDefaultMeasurementSystem());  
        }
        
        private GeneralSettingsInternal(GeneralSettingsInternal settings) {
            super(settings);
        }
        
        public LanguageInternal getInternalLanguage() {
            return (LanguageInternal) super.getLanguage();
        }
        
        public void commit() {
            updateSettings(new GeneralSettingsInternal(this));
        }
    }
}
