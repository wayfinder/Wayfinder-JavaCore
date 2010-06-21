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
import com.wayfinder.core.shared.internal.settings.language.LanguageISOMap.LangISOMap;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.pal.softwareinfo.SoftwareInfo;

import junit.framework.TestCase;

public class LanguageISOMapTest extends TestCase {
    
    static {
        JUnitLogHelper.setTraceLogLevel();
    }
    
    
    public void testGetLanguageForISOCode_ISO_639_1() {
        testRealMapping(SoftwareInfo.LANGUAGE_ISO_639_1, LanguageISOMap.createISO6391Map());
        testInvalidMapping(SoftwareInfo.LANGUAGE_ISO_639_1);
    }


    public void testGetLanguageForISOCode_ISO_639_2() {
        testRealMapping(SoftwareInfo.LANGUAGE_ISO_639_2, LanguageISOMap.createISO6392Map());
        testInvalidMapping(SoftwareInfo.LANGUAGE_ISO_639_2);
    }
    

    public void testGetLanguageForISOCode_ISO_639_3() {
        testRealMapping(SoftwareInfo.LANGUAGE_ISO_639_3, LanguageISOMap.createISO6393Map());
        testInvalidMapping(SoftwareInfo.LANGUAGE_ISO_639_3);
    }
    
    
    public void testGetLanguageForUnknownISOType() {
        int langCode = LanguageISOMap.getLanguageForISOCode(SoftwareInfo.LANGUAGE_UNKNOWN, "xyz");
        assertEquals(Language.EN_UK, langCode);
        langCode = LanguageISOMap.getLanguageForISOCode(SoftwareInfo.LANGUAGE_NONE, "xyz");
        assertEquals(Language.EN_UK, langCode);
        langCode = LanguageISOMap.getLanguageForISOCode(Integer.MAX_VALUE, "xyz");
        assertEquals(Language.EN_UK, langCode);
    }
    
    
    
    private void testRealMapping(int isoType, LangISOMap[] mapping) {
        for (int i = 0; i < mapping.length; i++) {
            LangISOMap tmpMap = mapping[i];
            final int langCode = LanguageISOMap.getLanguageForISOCode(isoType, tmpMap.getISOCode());
            assertEquals(tmpMap.getLangCode(), langCode);
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
            final int langCode = LanguageISOMap.getLanguageForISOCode(isoType, nonValidCodes[i]);
            assertEquals(Language.EN_UK, langCode);
        }
    }

}
