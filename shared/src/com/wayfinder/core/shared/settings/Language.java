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

public abstract class Language {
    
    //-------------------------------------------------------------------------
    // constants for each language - only those we want public
    
    // If you add a language below, don't forget to add it to 
    // LanguageFactory.createAllLanguagesAsIntArray() as well!

    /**
     * Represents the Danish language
     */
    public static final int DA = 0;
    public static final int DE = 1;
    public static final int EL = 2;
    public static final int ES = 3;
    public static final int FR = 4;
    public static final int IT = 5;
    public static final int NL = 6;
    public static final int PT = 7;
    public static final int TR = 8;
    
    // english variants

    public static final int EN_UK = 100;
    public static final int EN_IE = 101;
    
    /**
     * @deprecated use EN_UK instead
     */
    public static final int EN = EN_UK;
    
    
    
    private final int m_id;
    private final String m_nativeName;
    
    
    protected Language(int id, String nativeName) {
        m_id = id;
        m_nativeName = nativeName;
    }
    
    
    public final int getId() {
        return m_id;
    }
    
    
    public final String getNativeName() {
        return m_nativeName;
    }
}
