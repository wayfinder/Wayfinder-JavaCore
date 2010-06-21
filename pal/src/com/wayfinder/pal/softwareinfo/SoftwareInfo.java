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
package com.wayfinder.pal.softwareinfo;

public interface SoftwareInfo {
    
    
    //-------------------------------------------------------------------------
    // locale
    
    // language constants
    
    /**
     * Unable to read the currently set language from the platform's locale
     */
    public static final int LANGUAGE_NONE      = -2;
    
    /**
     * The language code can be read from the platform, but the PAL is unable
     * to determine what format it's in.
     */
    public static final int LANGUAGE_UNKNOWN   = -1;
    
    
    public static final int LANGUAGE_ISO_639_1 = 0;
    public static final int LANGUAGE_ISO_639_2 = 1;
    public static final int LANGUAGE_ISO_639_3 = 2;
    
    
    // country constants
    
    
    /**
     * Unable to read the currently set country from the platform's locale
     */
    public static final int COUNTRY_NONE = -2;
    
    /**
     * The country code can be read from the platform, but the PAL is unable
     * to determine what format it's in.
     */
    public static final int COUNTRY_UNKNOWN = -1;
    
    
    /**
     * Country code will be reported as 2-letter ISO 3166-1 standard
     */
    public static final int COUNTRY_ISO_3166_1_ALPHA_2 = 0;
    
    
    /**
     * Country code will be reported as 3-letter ISO 3166-1 standard
     */
    public static final int COUNTRY_ISO_3166_1_ALPHA_3 = 1;
    
    
    /**
     * Returns the type of the language returned in the {@link #getLanguage()}
     * method.
     * <p>
     * If the PAL is unable to determine the type of the platform language, it 
     * will return {@link #LANGUAGE_UNKNOWN}
     * <p>
     * If the PAL cannot read the language at all, it will return 
     * {@link #LANGUAGE_NONE}
     * 
     * @return One of the LANGUAGE_* constants in this class
     */
    public int getLanguageType();
    
    
    /**
     * Returns the language currently set in the platform.
     * 
     * @return The currently set language
     * @throws IllegalStateException if {@link #getLanguageType()} returns 
     * {@link #LANGUAGE_NONE}
     */
    public String getLanguage();
    
    
    /**
     * Returns the type of the country returned in the {@link #getCountry()}
     * method.
     * <p>
     * If the PAL is unable to determine the type of the platform country, it 
     * will return {@link #COUNTRY_UNKNOWN}
     * <p>
     * If the PAL cannot read the country at all, it will return 
     * {@link #LANGUAGE_NONE}
     * 
     * @return One of the COUNTRY_* constants in this class
     */
    public int getCountryType();
    
    
    /**
     * Returns the country currently set in the platform's locale
     * 
     * @return The country currently set in the platform's locale
     * @throws IllegalStateException if {@link #getLanguageType()} returns 
     * {@link #LANGUAGE_NONE}
     */
    public String getCountry();

}
