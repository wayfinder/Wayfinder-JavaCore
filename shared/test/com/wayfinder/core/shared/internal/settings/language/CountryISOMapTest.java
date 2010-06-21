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
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.softwareinfo.SoftwareInfo;

import junit.framework.TestCase;

public class CountryISOMapTest extends TestCase {
    
    static {
        JUnitLogHelper.setTraceLogLevel();
    }

    
    public void testGetCountryVariantForLanguage_ISO_3166_1_ALPHA_2() {
        testRealMapping(SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_2,
                    CountryISOMap.createISO31661ALPHA_2Map());
        testInvalidMapping(SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_2);
    }
    
    
    public void testGetCountryVariantForLanguage_ISO_3166_1_ALPHA_3() {
        testRealMapping(SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_3,
                    CountryISOMap.createISO31661ALPHA_3Map());
        testInvalidMapping(SoftwareInfo.COUNTRY_ISO_3166_1_ALPHA_3);
    }
    
    
    public void testGetCountryVariantForUnknownISOType() {
        int langCode = CountryISOMap.getCountryVariantForLanguage(Language.EN_UK,
                SoftwareInfo.COUNTRY_NONE, "xyz");
        assertEquals(Language.EN_UK, langCode);
        langCode = CountryISOMap.getCountryVariantForLanguage(Language.EN_UK, 
                SoftwareInfo.COUNTRY_UNKNOWN, "xyz");
        assertEquals(Language.EN_UK, langCode);
        langCode = CountryISOMap.getCountryVariantForLanguage(Language.EN_UK, 
                Integer.MAX_VALUE, "xyz");
        assertEquals(Language.EN_UK, langCode);
    }
    
    
    
    private void testRealMapping(int countryIsoCode, ISOMap[] mapping) {
        int[] allLangCodes = LanguageFactory.createAllLanguagesAsIntArray();
 
        for (int i = 0; i < mapping.length; i++) {
            ISOMap tmpMap = mapping[i];
            for (int j = 0; j < allLangCodes.length; j++) {
                final int tmpLangCode = allLangCodes[j];
                final int newLangCode = CountryISOMap.getCountryVariantForLanguage(
                        tmpLangCode, 
                        countryIsoCode, 
                        tmpMap.getISOCode());
                
                if(tmpLangCode == tmpMap.getBaseLangCode()) {
                    assertEquals(tmpMap.getCountryLangCode(), newLangCode);
                } else {
                    assertEquals(tmpLangCode, newLangCode);
                }
            }
        }
    }
    
    
    private void testInvalidMapping(int isoType) {
        String[] nonValidCodes = {
                null,
                "*/*",
                "123",
                "qzx",
                "§-."
        };
        for (int i = 0; i < nonValidCodes.length; i++) {
            final int langCode = CountryISOMap.getCountryVariantForLanguage(
                    Language.EL, isoType, nonValidCodes[i]);
            assertEquals(Language.EL, langCode);
        }
    }

}
