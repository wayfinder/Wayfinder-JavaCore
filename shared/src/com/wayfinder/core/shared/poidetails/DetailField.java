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
package com.wayfinder.core.shared.poidetails;

import com.wayfinder.core.shared.util.CharArray;

/**
 * Represent a pair name&value containing various type of information.
 * Name can also act as language dependent label. 
 *  
 * 
 */
public class DetailField {
    
    //<!ENTITY % poi_detail_content_t "(text|phone_number|url|email_address|
    //                                  integer|float)" >
    private static final String[] CONTENT_TYPE_STRINGS = {
        "text",
        "phone_number",
        "url",
        "email_address",
        "integer",
        "float"
    };
    
    public static final int CONTENT_TYPE_TEXT = 0;
    public static final int CONTENT_TYPE_PHONE_NUMBER = 1;
    public static final int CONTENT_TYPE_URL = 2;
    public static final int CONTENT_TYPE_EMAIL_ADDR = 3;
    public static final int CONTENT_TYPE_INTEGER = 4;
    public static final int CONTENT_TYPE_FLOAT = 5;

    private final String m_name;
    
    private final String m_value;
    
    private final int m_contentType;

    public DetailField(String name, String value) {
        this(name, value, CONTENT_TYPE_TEXT);
    }
    
    /**
     * @param name
     * @param value
     * @param contentType
     */
    public DetailField(String name, String value, int contentType) {
        m_name = name;
        m_value = value;
        m_contentType = contentType;
    }

    public static int getContentTypeForString(CharArray ca) {
        //type is a required element for fav_info and should never be null but 
        //is not required for info_item so null case need to be treated
        if (ca == null) {
            return -1;
        }
        return ca.indexIn(CONTENT_TYPE_STRINGS);
    }

    /**
     * @return the name of the detail field act as a label for the value.
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the actual value of the field, can be a text, an url, a phone 
     * number, a rating number 
     */
    public String getValue() {
        return m_value;
    }
    
    /**
     * 
     * @return the content type of this field, one of the CONTENT_TYPE_* constants
     */
    public int getContentType() {
        return m_contentType;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return m_name + ": " + m_value + " (" + CONTENT_TYPE_STRINGS[m_contentType] + ")";
    }
}
