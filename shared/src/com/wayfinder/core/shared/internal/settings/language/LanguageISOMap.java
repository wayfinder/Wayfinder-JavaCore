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

import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.softwareinfo.SoftwareInfo;

final class LanguageISOMap {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(LanguageISOMap.class);

    // this class will not be fun to create, but it won't be updated that
    // frequently anyway... at least the memory can be reclaimed afterwards...
    
    private LanguageISOMap() {}
    
    
    /**
     * Attempts to figure out the language based on the iso code reported
     * from the device
     * 
     * @param isoType One of the LANGUAGE_* constants in {@link SoftwareInfo}
     * @param isoCode The isocode from the device
     * 
     * @return One the language constants from {@link Language}
     */
    static int getLanguageForISOCode(int isoType, String isoCode) {
        if(ParameterValidator.isEmptyString(isoCode)) {
            if(LOG.isError()) {
                LOG.error("LanguageISOMap.getLanguageForISOCode()", 
                          "isoCode was null, returning " + LanguageFactory.getDefaultLangNameDebug());
            }
            return LanguageFactory.DEFAULT_LANGUAGE;
        }
        int lang;
        switch(isoType) {
        case SoftwareInfo.LANGUAGE_ISO_639_1:
            if(LOG.isTrace()) {
                LOG.trace("LanguageISOMap.getLanguageForISOCode()", 
                          "Language reported as ISO 639-1");
            }
            lang = readMapForISOMatch(createISO6391Map(), isoCode);
            break;

        case SoftwareInfo.LANGUAGE_ISO_639_2:
            if(LOG.isTrace()) {
                LOG.trace("LanguageISOMap.getLanguageForISOCode()", 
                          "Language reported as ISO 639-2");
            }
            lang = readMapForISOMatch(createISO6392Map(), isoCode);
            break;

        case SoftwareInfo.LANGUAGE_ISO_639_3:
            if(LOG.isTrace()) {
                LOG.trace("LanguageISOMap.getLanguageForISOCode()", 
                          "Language reported as ISO 639-3");
            }
            lang = readMapForISOMatch(createISO6393Map(), isoCode);
            break;
        
        default:
            if(LOG.isWarn()) {
                LOG.warn("LanguageISOMap.getLanguageForISOCode()", 
                         "Unable to use the language ISO type, defaulting to " +
                         LanguageFactory.getDefaultLangNameDebug());
            }
            lang = LanguageFactory.DEFAULT_LANGUAGE;
        }
        return lang;
    }
    
    
    private static int readMapForISOMatch(LangISOMap[] map, String isoCode) {
        isoCode = isoCode.toLowerCase();
        for (int i = 0; i < map.length; i++) {
            if(map[i].getISOCode().equals(isoCode)) {
                return map[i].getLangCode();
            }
        }
        
        if(LOG.isWarn()) {
            LOG.warn("LanguageISOMap.readMapForISOMatch()", 
                     "Device language (" + isoCode + ") " + 
                     "is not supported, defaulting to " + 
                     LanguageFactory.getDefaultLangNameDebug());
        }
        return LanguageFactory.DEFAULT_LANGUAGE;
    }
    
    
    //-------------------------------------------------------------------------
    // actual codes
    
    // package protected for JUnit test
    static LangISOMap[] createISO6391Map() {
        return new LangISOMap[] {
                new LangISOMap("da", Language.DA),
                new LangISOMap("de", Language.DE),
                new LangISOMap("el", Language.EL),
                new LangISOMap("en", Language.EN_UK),
                new LangISOMap("es", Language.ES),
                new LangISOMap("fr", Language.FR),
                new LangISOMap("it", Language.IT),
                new LangISOMap("nl", Language.NL),
                new LangISOMap("pt", Language.PT),
                new LangISOMap("tr", Language.TR),
        };
    }
    
    
    // package protected for JUnit test
    static LangISOMap[] createISO6392Map() {
        // a few languages have more than one code
        return new LangISOMap[] {
                new LangISOMap("dan", Language.DA),
                new LangISOMap("ger", Language.DE),
                new LangISOMap("deu", Language.DE),
                new LangISOMap("gre", Language.EL),
                new LangISOMap("ell", Language.EL),
                new LangISOMap("eng", Language.EN_UK),
                new LangISOMap("spa", Language.ES),
                new LangISOMap("fra", Language.FR),
                new LangISOMap("fre", Language.FR),
                new LangISOMap("ita", Language.IT),
                new LangISOMap("nld", Language.NL),
                new LangISOMap("dut", Language.NL),
                new LangISOMap("por", Language.PT),
                new LangISOMap("tur", Language.TR),
        };
    }
    
    
    // package protected for JUnit test
    static LangISOMap[] createISO6393Map() {
        return new LangISOMap[] {
                new LangISOMap("dan", Language.DA),
                new LangISOMap("deu", Language.DE),
                new LangISOMap("ell", Language.EL),
                new LangISOMap("eng", Language.EN_UK),
                new LangISOMap("spa", Language.ES),
                new LangISOMap("fra", Language.FR),
                new LangISOMap("ita", Language.IT),
                new LangISOMap("nld", Language.NL),
                new LangISOMap("por", Language.PT),
                new LangISOMap("tur", Language.TR),
        };
    }
    
    // package protected for JUnit test
    static class LangISOMap {
        
        private final String m_isoCode;
        private final int m_langCode;

        LangISOMap(String isoCode, int langCode) {
            m_isoCode = isoCode;
            m_langCode = langCode;
        }

        String getISOCode() {
            return m_isoCode;
        }
        
        int getLangCode() {
            return m_langCode;
        }
    }
    
}
