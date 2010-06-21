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
package com.wayfinder.core.shared.settings;

import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;

/**
 * <p>Settings that affect several core modules.</p>
 * 
 * <p>This class is NOT thread safe.</p>
 */
public abstract class GeneralSettings {

    /*
     * TODO: Since we only allow LanguageInternal in setLanguage() we should
     * move the language stuff to GeneralSettingsInternal and converting
     * getLanguage() and setLanguage() to abstract methods.
     * 
     * But first we must check with UI that they don't subclass this class,
     * since we need to change the ctors as well and thus causing a compatibility
     * break.
     */
    
    /**
     * The metric system.
     * (m, km and so on)
     */
    public static final int UNITS_METRIC      = 0;
    
    /**
     * The imperial system used in the UK
     * (miles, yards and so on...)
     */
    public static final int UNITS_IMPERIAL_UK = 1;
    
    /**
     * The imperial system used in the US
     * (miles, feet and so on...)
     */
    public static final int UNITS_IMPERIAL_US = 2;
    
    public static final long DEFAULT_TRAFFIC_INFO_UPDATE_INTERVAL = 600000; //10min
        
    private Language m_language;
    private int m_measurementsystem;
    
    /**
     * traffic info update interval, set to 0 to turn off
     */
    private long m_PTUI;

    
    /**
     * Creates a settings object with the provided parameters
     * 
     * @param language The {@link Language}
     * @param measurementSystem One of the UNITS_* constants in this class
     */
    protected GeneralSettings(Language language, int measurementSystem) {
        m_language = language;
        m_measurementsystem = measurementSystem;
        m_PTUI = DEFAULT_TRAFFIC_INFO_UPDATE_INTERVAL;
    }
    
    
    /**
     * Creates a copy of the provided settings object
     * 
     * @param settings The {@link GeneralSettings} to clone
     */
    protected GeneralSettings(GeneralSettings settings) {
        m_language = settings.m_language;
        m_measurementsystem = settings.m_measurementsystem;
        m_PTUI = settings.m_PTUI;
    }
    
    // language
    
    public final Language getLanguage() {
        return m_language;
    }
    
    
    /**
     * <p>Change the set language.</p>
     * 
     * <p>If you set the language to something else than what is currently set,
     * the measurement system is also reset to the default measurement system
     * for that language. If you wish to retain the current measurement system
     * instead (which would be surprising for most users), store the value of
     * <code>getMeasurementSystem()</code> and call
     * <code>setMeasurementSystem(int)</code> before calling
     * <code>commit()</code>.
     * </p> 
     * 
     * @param lang - the language to set.
     */
    public final void setLanguage(Language lang) {
        if(lang instanceof LanguageInternal) {
            if (m_language.getId() != lang.getId()) {
                // language changed - reset measurement system
                m_measurementsystem = ((LanguageInternal) lang)
                                      .getDefaultMeasurementSystem();
            }

            m_language = lang;
        } else {
            throw new IllegalArgumentException("Foreign subclasses of Language " +
            		"is not allowed, use the static methods in Language to " +
            		"obtain valid classes");
        }
    }
    
    
    // measurement system

    public final int getMeasurementSystem() {
        return m_measurementsystem;
    }
    
    
    public final void setMeasurementSystem(int system) {
        // ensure that it's a valid figure
        switch(system) {
        case UNITS_METRIC:
        case UNITS_IMPERIAL_UK:
        case UNITS_IMPERIAL_US:
            // OK!
            break;
            
        default:
            throw new IllegalArgumentException("Not a valid measurement system");
        
        }
        m_measurementsystem = system;
    }
    
    /**
     * @deprecated PTUI is disabled due to incomplete implementation. Subject
     *             to change without notice.
     */
    public long getPTUI() {
        return m_PTUI;
    }


    /**
     * @deprecated PTUI is disabled due to incomplete implementation. Subject
     *             to change without notice.
     */
    public void setPTUI(long pTUI) {
        m_PTUI = pTUI;
    }


    /**
     * @deprecated PTUI is disabled due to incomplete implementation. Subject
     *             to change without notice.
     */
    public boolean isPTUIon() {
        return (m_PTUI > 0);
    }


    // copy into
    public abstract void commit();
}
