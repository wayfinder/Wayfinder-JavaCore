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
import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.softwareinfo.SoftwareInfo;

public final class LanguageFactory {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(LanguageFactory.class);
    
    final static int DEFAULT_LANGUAGE = Language.EN_UK;
    
    static String getDefaultLangNameDebug() {
        return createLanguageFor(DEFAULT_LANGUAGE).getNativeName();
    }
    
    private LanguageFactory() {}
    
    // create the language objects when we need them instead of holding loads
    // of static objects in memory...
    
    /*
     * NOTE: When adding a new language, review
     * LanguageInternal.getDefaultMeasurementSystem() to make sure that it does
     * the correct thing for your new language.
     */

    public static LanguageInternal createLanguageFor(int id) {
        switch(id) {
        case Language.DA:
            return new LanguageInternal(id, 
                    "DA",
                    "DA",
                    "danish", 
                    "Dansk", 
                    LangTypes.DANISH,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.DE:
            return new LanguageInternal(id, 
                    "DE",
                    "DE",
                    "german", 
                    "Deutsch", 
                    LangTypes.GERMAN,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.EL:
            return new LanguageInternal(
                    id, 
                    "EL",
                    "EL",
                    "greek",
                    "\u0395\u03BB\u03BB\u03B7\u03BD\u03B9\u03BA\u03AC",
                      LangTypes.GREEK,
                      GeneralSettings.UNITS_METRIC);
            
        case Language.EN_UK:
            return new LanguageInternal(
                    id, 
                    "EN",
                    "EN",
                    "english",
                    "English (UK)",
                    LangTypes.ENGLISH,
                    GeneralSettings.UNITS_IMPERIAL_UK);
            
        case Language.EN_IE:
            return new LanguageInternal(
                    id, 
                    "EN",
                    "IE",
                    "english",
                    "English (Ireland)",
                    LangTypes.ENGLISH,
                    // republic of ireland officially switched to metric 1970s - 2005
                    // funny enough, a beer is still "a pint" :)
                    GeneralSettings.UNITS_METRIC);
            
        case Language.ES:
            return new LanguageInternal(id, 
                    "ES",
                    "ES",
                    "spanish", 
                    "Espa\u00F1ol", 
                    LangTypes.SPANISH,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.FR:
            return new LanguageInternal(id, 
                    "FR",
                    "FR",
                    "french", 
                    "Fran\u00E7ais", 
                    LangTypes.FRENCH,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.IT:
            return new LanguageInternal(id, 
                    "IT",
                    "IT",
                    "italian", 
                    "Italiano", 
                    LangTypes.ITALIAN,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.NL:
            return new LanguageInternal(id, 
                    "NL",
                    "NL",
                    "dutch", 
                    "Nederlands", 
                    LangTypes.DUTCH,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.PT:
            return new LanguageInternal(id, 
                    "PT",
                    "PT",
                    "portuguese", 
                    "Portugu\u00EAs",
                    LangTypes.PORTUGUESE,
                    GeneralSettings.UNITS_METRIC);
            
        case Language.TR:
            return new LanguageInternal(id, 
                    "TR",
                    "TR",
                    "turkish", 
                    "T\u00FCrk", 
                    LangTypes.TURKISH,
                    GeneralSettings.UNITS_METRIC);
        }
        throw new IllegalArgumentException("Unknown language code");
    }
    
    
    
    static int[] createAllLanguagesAsIntArray() {
        // oh how I would like to reflect here :P
        return new int[] {
                Language.DA,
                Language.DE,
                Language.EL,
                Language.EN_IE,
                Language.EN_UK,
                Language.ES,
                Language.FR,
                Language.IT,
                Language.NL,
                Language.PT,
                Language.TR,
        };
    }
    
    
    static LanguageInternal[] createAllLanguages() {
        int[] array = createAllLanguagesAsIntArray();
        LanguageInternal[] langArray = new LanguageInternal[array.length];
        for (int i = 0; i < langArray.length; i++) {
            langArray[i] = createLanguageFor(array[i]);
        }
        return langArray;
    }
    
    
    public static LanguageInternal guessLanguageFromPlatform(SoftwareInfo infoIfc) {
        int langCode;
        // first grab main language
        final int langType = infoIfc.getLanguageType();
        switch(langType) {
        case SoftwareInfo.LANGUAGE_ISO_639_1:
        case SoftwareInfo.LANGUAGE_ISO_639_2:
        case SoftwareInfo.LANGUAGE_ISO_639_3:
            String langIsoCode = infoIfc.getLanguage();
            if(ParameterValidator.isEmptyString(langIsoCode)) {
                if(LOG.isError()) {
                    LOG.error("LanguageFactory.guessLanguageFromPlatform()", 
                              "Language type reports valid, but actual language code was empty string " +
                              "- defaulting to " + getDefaultLangNameDebug());
                }
                langCode = DEFAULT_LANGUAGE;
            } else {
                langCode = LanguageISOMap.getLanguageForISOCode(langType, langIsoCode);
            }
            break;

        case SoftwareInfo.LANGUAGE_UNKNOWN:
            if(LOG.isWarn()) {
                LOG.warn("LanguageFactory.guessLanguageFromPlatform()",  
                        "Unknown format on reported device language, " +
                "defaulting to " + getDefaultLangNameDebug());
            }
            langCode = DEFAULT_LANGUAGE;
            break;

        default:
            if(LOG.isError()) {
                LOG.error("LanguageFactory.guessLanguageFromPlatform()",  
                        "Unable to read language set in device, " +
                "defaulting to " + getDefaultLangNameDebug());
            }
            langCode = DEFAULT_LANGUAGE;
        }
        
        if(LOG.isDebug()) {
            LanguageInternal langTmp = LanguageFactory.createLanguageFor(langCode);
            LOG.debug("LanguageFactory.guessLanguageFromPlatform()", 
                      "Base language set to " + langTmp.getNativeName());
        }
        
        // now take a look at the country to refine
        final int countryType = infoIfc.getCountryType();
        if(countryType != SoftwareInfo.COUNTRY_NONE) {
            // something is reported :D
            langCode = CountryISOMap.getCountryVariantForLanguage(langCode, countryType, infoIfc.getCountry());
        }
        
        final LanguageInternal returnedLanguage = LanguageFactory.createLanguageFor(langCode);
        if(LOG.isDebug()) {
            LOG.debug("LanguageFactory.guessLanguageFromPlatform()", 
                      "Country language set to " + returnedLanguage.getNativeName());
        }
        return returnedLanguage;
    }
    
}
