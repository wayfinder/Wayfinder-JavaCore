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
package com.wayfinder.core.shared.internal.poiinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.poiinfo.InfoField;
import com.wayfinder.core.shared.util.CharArray;

/**
 * An info field part of the extra information associated with a POI or 
 * a Favorite.
 * Is compose from a type which is one of given constants values,
 * and a key value pair which can be displayed.
 *       
 * 
 */
public class InfoFieldImpl implements InfoField {
    
    /**
     * constants used in xml parsing/reading moved from MC2Strings 
     * in order to keep them connected with the constants and avoid 
     * the need for a mapping   
     */
    private static final String[] TYPES_VALUES_ARRAY = {
        "text", //also use for unknown/future type
        "phone_number",
        "mobile_phone",
        "image_url",
        "url",
        "email",
    };
    
    private int type;

    private String key;
    
    private String value;

    private InfoFieldImpl() {
    }
    
    public InfoFieldImpl(int type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
    public static int getTypeForString(CharArray ca) {
        //type is a required element for fav_info and should never be null but 
        //is not required for info_item so null case need to be treated
        if (ca == null) {
            return TYPE_TEXT;
        }
        int index = ca.indexIn(TYPES_VALUES_ARRAY);
        if (index == -1) {
            return TYPE_TEXT;
        } else {
            return index;
        }
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.poiinfo.InfoField#getTypeString()
     */
    public String getTypeString() {
        return TYPES_VALUES_ARRAY[type];
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.poiinfo.InfoField#getType()
     */
    public int getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.poiinfo.InfoField#getKey()
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.poiinfo.InfoField#getValue()
     */
    public String getValue() {
        return value;
    }
    


    public String toString() {
        return getTypeString() + ": " + key + "=" + value;
    }

    void read(DataInputStream din) throws IOException {
        type = din.readInt();
        key = din.readUTF();
        value = din.readUTF();
    }

    void write(DataOutputStream dout) throws IOException {
        dout.writeInt(type);
        dout.writeUTF(key);
        dout.writeUTF(value);
    }

    static InfoFieldImpl restore(DataInputStream din) throws IOException {
        InfoFieldImpl rez = new InfoFieldImpl();
        rez.read(din);
        return rez;
    }


}
