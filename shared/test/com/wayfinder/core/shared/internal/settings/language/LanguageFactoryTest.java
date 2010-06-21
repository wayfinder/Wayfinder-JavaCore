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
package com.wayfinder.core.shared.internal.settings.language;

import com.wayfinder.core.junit.JUnitLogHelper;
import com.wayfinder.core.shared.internal.settings.language.CountryISOMap.ISOMap;
import com.wayfinder.core.shared.internal.settings.language.LanguageISOMap.LangISOMap;
import com.wayfinder.pal.softwareinfo.SoftwareInfo;

import junit.framework.TestCase;

public class LanguageFactoryTest extends TestCase {
    
    static {
        JUnitLogHelper.setTraceLogLevel();
    }

    public void testCreateLanguageFor() {
    }

    
    public void testCreateAllLanguages() {
        int[] allLangInts = LanguageFactory.createAllLanguagesAsIntArray();
        LanguageInternal[] allLangs = LanguageFactory.createAllLanguages();
        assertEquals(allLangInts.length, allLangs.length);
        for (int i = 0; i < allLangs.length; i++) {
            assertEquals(allLangInts[i], allLangs[i].getId());
        }
    }
    
    
    public void testGuessLanguageFromPlatformGoodCases() {
        
        // regular cases
        int[] languages = new int[]{
                SoftwareInfo.LANGUAGE_ISO_639_1,
                SoftwareInfo.LANGUAGE_ISO_639_2,
                SoftwareInfo.LANGUAGE_ISO_639_3,
                SoftwareInfo.LANGUAGE_NONE,
                SoftwareInfo.LANGUAGE_UNKNOWN
        };
        
        int[] countries = new int[] {
                SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_2,
                SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_3,
                SoftwareInfo.COUNTRY_NONE,
                SoftwareInfo.COUNTRY_UNKNOWN
        };
        
        for (int i = 0; i < languages.length; i++) {
            for (int j = 0; j < countries.length; j++) {
                testForMapping(languages[i], countries[j]);
            }
        }
        // failure cases when device language is messed up
        for (int i = 0; i < languages.length; i++) {
            testSingle(languages[i], 
                    null, // <- should not happen for ISO types 
                    SoftwareInfo.COUNTRY_NONE, 
                    null, 
                    LanguageFactory.DEFAULT_LANGUAGE);
        }
        
        
        
    }
    
    public void testGuessLanguageFromPlatformBadCases() {
        int[] languages = new int[]{
                SoftwareInfo.LANGUAGE_ISO_639_1,
                SoftwareInfo.LANGUAGE_ISO_639_2,
                SoftwareInfo.LANGUAGE_ISO_639_3,
                SoftwareInfo.LANGUAGE_NONE,
                SoftwareInfo.LANGUAGE_UNKNOWN
        };
        
        // failure cases when device language is messed up
        for (int i = 0; i < languages.length; i++) {
            testSingle(languages[i], 
                    null, // <- should not happen for ISO types 
                    SoftwareInfo.COUNTRY_NONE, 
                    null, 
                    LanguageFactory.DEFAULT_LANGUAGE);
        }
    }
    
    
    
    private void testForMapping(int langType, int countryType) {
        
        LangISOMap[] langMap;
        switch(langType) {
        case SoftwareInfo.LANGUAGE_ISO_639_1:
            langMap = LanguageISOMap.createISO6391Map();
            break;
            
        case SoftwareInfo.LANGUAGE_ISO_639_2:
            langMap = LanguageISOMap.createISO6392Map();
            break;
            
        case SoftwareInfo.LANGUAGE_ISO_639_3:
            langMap = LanguageISOMap.createISO6393Map();
            break;
            
        default:
            langMap = null;
        }
        
        if(langMap == null) {
            testSingle(langType, null, countryType, null, LanguageFactory.DEFAULT_LANGUAGE);
        } else {
            
            ISOMap[] countryMap;
            switch(countryType) {
            case SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_2:
                countryMap = CountryISOMap.createISO31661ALPHA_2Map();
                break;
                
            case SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_3:
                countryMap = CountryISOMap.createISO31661ALPHA_3Map();
                break;
                
            default:
                countryMap = null;
            }
            
            for (int i = 0; i < langMap.length; i++) {
                LangISOMap langISOMap = langMap[i];
                if(countryMap != null) {
                    for (int j = 0; j < countryMap.length; j++) {
                        ISOMap countryISOMap = countryMap[j];
                        int expected;
                        if(countryISOMap.getBaseLangCode() == langISOMap.getLangCode()) {
                            expected = countryISOMap.getCountryLangCode();
                        } else {
                            expected = langISOMap.getLangCode();
                        }
                        testSingle(langType, langISOMap.getISOCode(),
                                countryType, countryISOMap.getISOCode(), expected);
                    }
                } else {
                    testSingle(langType, langISOMap.getISOCode(),
                            countryType, null, langISOMap.getLangCode());
                }
            }
        }
    }
    
    
    private void testSingle(int langType, String langIsoCode,
                            int countryType, String countryIsoCode,
                            int expectedLanguage) {
        
        SoftwareInfo info = new SoftwareInfoImpl(
                langType, 
                langIsoCode, 
                countryType, 
                countryIsoCode);
        
        LanguageInternal lang = LanguageFactory.guessLanguageFromPlatform(info);
        assertEquals(expectedLanguage, lang.getId());
    }
    
    
    
    
    
    
    private static class SoftwareInfoImpl implements SoftwareInfo {
        
        private final int m_langType;
        private final String m_langISOCode;
        private final int m_countryType;
        private final String m_countryISOCode;
        
        private SoftwareInfoImpl(int langtype, String langISOCode, int countryType, String countryISOCode) {
            m_langType = langtype;
            m_langISOCode = langISOCode;
            m_countryType = countryType;
            m_countryISOCode = countryISOCode;
        }
        
        public int getLanguageType() {
            return m_langType;
        }
        
        public String getLanguage() {
            if(m_langType == LANGUAGE_NONE) {
                throw new IllegalStateException();
            }
            return m_langISOCode;
        }
        
        public int getCountryType() {
            return m_countryType;
        }

        public String getCountry() {
            if(m_countryType == COUNTRY_NONE) {
                throw new IllegalStateException();
            }
            return m_countryISOCode;
        }
    }
}
