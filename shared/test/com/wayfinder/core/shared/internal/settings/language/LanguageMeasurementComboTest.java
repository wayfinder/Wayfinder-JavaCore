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

package com.wayfinder.core.shared.internal.settings.language;

import com.wayfinder.core.shared.internal.settings.language.LanguageFactory;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.core.shared.settings.LanguageHelper;

import junit.framework.TestCase;

public class LanguageMeasurementComboTest extends TestCase {

    private static class GeneralSettingsBridge extends GeneralSettings {

        public GeneralSettingsBridge(LanguageInternal language) {
            // same code as in GeneralSettingsHolder.GeneralSettingsInternal
            super(language, language.getDefaultMeasurementSystem());
        }

        /* (non-Javadoc)
         * @see com.wayfinder.core.shared.settings.GeneralSettings#commit()
         */
        public void commit() {
            // do nothing
        }
    }


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        // create the settings system
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testEnglish() {
        LanguageInternal lang = LanguageFactory.createLanguageFor(Language.EN); 
        GeneralSettingsBridge settings = new GeneralSettingsBridge(lang);
        
        assertEquals(GeneralSettings.UNITS_IMPERIAL_UK,
                     settings.getMeasurementSystem());
    }

    public void testGerman() {
        LanguageInternal lang = LanguageFactory.createLanguageFor(Language.DE); 
        GeneralSettingsBridge settings = new GeneralSettingsBridge(lang);
        
        assertEquals(GeneralSettings.UNITS_METRIC,
                     settings.getMeasurementSystem());
    }

    public void testLanguageChange() {
        LanguageInternal english = LanguageFactory.createLanguageFor(Language.EN); 
        GeneralSettingsBridge settings = new GeneralSettingsBridge(english);

        // testEnglish tests that we are correct.

        // like UI does it...
        Language german = LanguageHelper.getLanguage(Language.DE);
        settings.setLanguage(german);
        assertEquals(GeneralSettings.UNITS_METRIC,
                settings.getMeasurementSystem());
    }

    /**
     * Test that invoking setLanguage() with the language already set does
     * not revert the measurement system.
     */
    public void testSetSameLanguage() {
        LanguageInternal lang = LanguageFactory.createLanguageFor(Language.DE); 
        GeneralSettingsBridge settings = new GeneralSettingsBridge(lang);

        // testGerman asserts that we're metric.

        // A German on vacation in the UK might want to have voice prompts
        // in accordance with road signs.
        settings.setMeasurementSystem(GeneralSettings.UNITS_IMPERIAL_UK);
        
        // like UI does it...
        Language german = LanguageHelper.getLanguage(Language.DE);
        settings.setLanguage(german);

        // this language is already set so we should keep the old system. 
        assertEquals(GeneralSettings.UNITS_IMPERIAL_UK,
                settings.getMeasurementSystem());
    }
}
