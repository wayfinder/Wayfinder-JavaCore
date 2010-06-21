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

class CountryISOMap {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(CountryISOMap.class);
    
    private CountryISOMap() {}
    
    
    static int getCountryVariantForLanguage(int langID, int cISOType, String cISOCode) {
        if(ParameterValidator.isEmptyString(cISOCode)) {
            if(LOG.isError()) {
                LOG.error("CountryISOMap.getCountryVariantForLanguage()", 
                          "isoCode was null, returning base language");
            }
            return langID;
        }
        // right now we're only looking at Irish, but we've got a lot more
        // incoming. No support on server yet though, so probably dumb to
        // autodetect them now, let's wait until they actually work :D
        
        // for english it's (from what I know) New Zealand & India
        // probably with same deal as with Irish, eg same lang but different
        // voices
        
        int lang;
        switch(cISOType) {
        case SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_2:
            lang = readMapForISOMatch(createISO31661ALPHA_2Map(), cISOCode, langID);
            break;
            
        case SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_3:
            lang = readMapForISOMatch(createISO31661ALPHA_3Map(), cISOCode, langID);
            break;
            
        default:
            if(LOG.isWarn()) {
                LOG.warn("LanguageISOMap.getLanguageForISOCode()", 
                         "Unable to use the language ISO type, using base " +
                         "language");
            }
            lang = langID;
        }
        return lang;
    }
    

    private static int readMapForISOMatch(ISOMap[] mapping, String cISOCode, int langCode) {
        cISOCode = cISOCode.toLowerCase();
        for (int i = 0; i < mapping.length; i++) {
            ISOMap tmpMap = mapping[i];
            if(tmpMap.getISOCode().equals(cISOCode)) {
                // we have the country
                // only switch out the language IF the base language matches
                // otherwise the user will be upset that his Irish phone is set
                // to spanish but the app is set to irish...
                if(tmpMap.getBaseLangCode() == langCode) {
                    return tmpMap.getCountryLangCode();
                } else {
                    // not set to the base language, just return the language
                    // as-is
                    return langCode;
                }
            }
        }
        // no match found
        return langCode;
    }
    
    
    static ISOMap[] createISO31661ALPHA_2Map() {
        return new ISOMap[] {
                new ISOMap("ie", Language.EN_UK, Language.EN_IE)
        };
    }
    
    
    static ISOMap[] createISO31661ALPHA_3Map() {
        return new ISOMap[] {
                new ISOMap("irl", Language.EN_UK, Language.EN_IE)
        };
    }
    
    
    
    static class ISOMap {
        
        private final String m_isoCode;
        private final int m_baseLangCode;
        private final int m_countryLangCode;

        ISOMap(String isoCode, int baseLangCode, int countryLangCode) {
            m_isoCode = isoCode;
            m_baseLangCode = baseLangCode;
            m_countryLangCode = countryLangCode;
        }

        String getISOCode() {
            return m_isoCode;
        }
        
        int getBaseLangCode() {
            return m_baseLangCode;
        }
        
        int getCountryLangCode() {
            return m_countryLangCode;
        }
        
    }
 
}
