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
package com.wayfinder.core.favorite.internal;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2RequestAdapter;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.util.CharArray;

public class FavoriteCrcRequest extends MC2RequestAdapter {

    private String favoritesCrc;
    
    public FavoriteCrcRequest(String crc, MC2RequestListener listener) {
       super(listener);
       this.favoritesCrc = crc;
    }
    
    public String getRequestElementName() {
        return MC2Strings.tuser_favorites_crc_request;
    }
    
    /*<!ELEMENT user_favorites_crc_reply EMPTY >
      <!ATTLIST user_favorites_crc_reply transaction_id ID #REQUIRED
            crc_match %bool; #REQUIRED >*/
    //probably can have a status as well but the xmldoc was not updated 
    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        mc2p.nameOrError(MC2Strings.tuser_favorites_crc_reply);

        boolean match = false;
        CharArray crc_match =  mc2p.attributeCharArray(MC2Strings.acrc_match);
        
        if (crc_match != null) {
            match = crc_match.booleanValue();
        }
        //pass into content of user_favorites_reply
        if (mc2p.children()) {
            ServerError status = mc2p.getErrorIfExists();
            if (status != null) {
                error(status);
                return; //nothing else to parse
            }
        } 
        result(booleanObject(match));
    }
    
    /*<!ELEMENT user_favorites_crc_request EMPTY >
      <!ATTLIST user_favorites_crc_request transaction_id ID #REQUIRED
          crc CDATA #REQUIRED >*/
    public void write(MC2Writer mc2w) throws IOException {
        mc2w.attribute(MC2Strings.acrc, favoritesCrc);
    }
    
    /**
     * Convert a primitive boolean in a Boolean object. 
     * Avoid creating boolean objects. 
     * @param value the boolean value that need to converted
     * @return one of the Boolean pre-created values 
     */
    static Boolean booleanObject(boolean value) {
        if (value) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
