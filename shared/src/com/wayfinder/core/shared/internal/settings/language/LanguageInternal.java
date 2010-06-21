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

import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.core.shared.settings.Language;

public final class LanguageInternal extends Language {

    private final String m_wfURLCode;
    private final String m_wfSoundSyntaxCode;
    private final String m_xmlCode;
    private final int m_langType;
    private final int m_measurementSystem;

    LanguageInternal(int langCode, 
                     String wfURLCode,
                     String wfSoundSyntaxCode,
                     String xmlCode, 
                     String nativeName, 
                     int langType,
                     int measurementSystem) {
        
        super(langCode, nativeName);
        
        m_wfURLCode = wfURLCode;
        m_wfSoundSyntaxCode = wfSoundSyntaxCode;
        m_xmlCode = xmlCode;
        m_langType = langType;
        m_measurementSystem = measurementSystem;
    }

    
    /**
     * Returns the WF URL code for this language.
     * 
     * This is added as parameter to each url in CW 
     * 
     * @return The WF code as a String
     */
    public String getWFURLCode() {
        return m_wfURLCode;
    }
    
    
    /**
     * Returns the WF SoundSyntax code for this language.
     * 
     * This is used to compose sound syntax file name   
     * 
     * @return The WF code as a String
     */
    public String getWFSoundSyntaxCode() {
        return m_wfSoundSyntaxCode;
    }
    
    
    /**
     * Returns the XML code for this language
     * 
     * @return The XML code as a String
     */
    public String getXMLCode() {
        return m_xmlCode;
    }


    /**
     * Returns the langtype of this language
     * 
     * @return one of the constants in {@link LangTypes}
     */
    public int getLangType() {
        return m_langType;
    }


    /**
     * <p>Returns default measurement system for this language.</p>
     * 
     * <p>We don't expose this in the public API ({@link Language}) because
     * measurement system is not really a property of a language but of a
     * locale. But internally it is ok to let a language represent a locale.</p>
     * 
     * @return One of <ul><li>{@link GeneralSettings#UNITS_METRIC}</li>
     *                     <li>{@link GeneralSettings#UNITS_IMPERIAL_UK}</li>
     *                     <li>{@link GeneralSettings#UNITS_IMPERIAL_US}</li>
     *                </ul>
     */
    public int getDefaultMeasurementSystem() {
        return m_measurementSystem;
    }
    
}
