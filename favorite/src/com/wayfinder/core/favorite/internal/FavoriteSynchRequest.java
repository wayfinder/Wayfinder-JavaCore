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
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.ArrayList;

public class FavoriteSynchRequest extends MC2RequestAdapter {

    private static final Logger LOG = 
        LogFactory.getLoggerForClass(FavoriteSynchRequest.class);
    
    FavoriteSynchData synchData;
    
    public FavoriteSynchRequest(FavoriteSynchData data, MC2RequestListener listener) {
        super(listener);
        this.synchData = data;
    }   

    public String getRequestElementName() {
        return MC2Strings.tuser_favorites_request;
    }
    
    /* xml 2.1.1
    <!ELEMENT user_favorites_reply ( (delete_favorite_id_list?,
            add_favorite_list?,
            auto_dest_favorite?) |
            ( status_code, status_message,
            status_code_extended? ) )>
    <!ATTLIST user_favorites_reply transaction_id ID #REQUIRED
            crc CDATA #REQUIRED >
    */
    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        
        mc2p.nameOrError(MC2Strings.tuser_favorites_reply);
        
        //read this here even it could be null 
        //set it only after the parsing was complete
        String serverCrc = mc2p.attribute(MC2Strings.acrc);
        ArrayList serverDeletedFavs = null;
        ArrayList serverNewFavs = null;
        
        //pass into content of user_favorites_reply
        if (mc2p.children()) {
            ServerError status = mc2p.getErrorIfExists();
            if (status != null) {
                error(status);
                return; //nothing else to parse
            }
            do {
                if (mc2p.nameRefEq(MC2Strings.tadd_favorite_list)) {
                    serverNewFavs = new ArrayList();
                    /*<!ELEMENT add_favorite_list ( favorite+ )>*/
                    mc2p.childrenOrError(); 
                    // at first favorite
                    do {
                        mc2p.nameOrError(MC2Strings.tfavorite);
                        FavoriteInternal fav = new FavoriteInternal();
                        fav.parse(mc2p);
                        serverNewFavs.add(fav);
                     } while (mc2p.advance());// while not end of add_favorite_list
                } else if (mc2p.nameRefEq(MC2Strings.tdelete_favorite_id_list)) {
                    serverDeletedFavs = new ArrayList();
                    /*<!ELEMENT delete_favorite_id_list ( favorite_id+ )>*/
                    mc2p.childrenOrError(); 
                    // at first favorite id
                    do {
                        /*<!ELEMENT favorite_id ( #PCDATA )>*/
                        mc2p.nameOrError(MC2Strings.tfavorite_id);
                        
                        int id = MC2Strings.number_type(mc2p.valueCharArray());
                        FavoriteInternal fav = synchData.findFavByID(id);
                        if (fav != null) {
                            serverDeletedFavs.add(fav);
                        } else {
                            if(LOG.isWarn()) {
                                LOG.warn("FavoriteSynchRequest.parse()", 
                                        "cannot found favorite with id " + id);
                            }
                        }
                    } while (mc2p.advance());
                    //while not end of delete_favorite_id_list
                }
            } while (mc2p.advance());// while not end of user_favorites_reply
        }
        synchData.setResult(serverCrc, serverDeletedFavs, serverNewFavs);
        
        result(synchData);
        // success !!!!!!!
        // notify about the modification crc, deleted, added, clean what have been sent
    }

    /* xml 2.1.1
    <!ELEMENT user_favorites_request ( (user_id | uin |
            (user_session_id, user_session_key) )?,
            favorite_id_list?,
            delete_favorite_id_list?,
            add_favorite_list?,
            auto_dest_favorite? )>
    <!ATTLIST user_favorites_request
            transaction_id ID #REQUIRED
            fetch_auto_dest %bool; "false"
            sync_favorites %bool; "true"
            position_system %position_system_t; "MC2"
            fav_info_in_desc %bool; "true" >
    
    <!ELEMENT auto_dest_favorite ( favorite? )>
    */
    public void write(MC2Writer mc2w) throws IOException {

        //by default is a full synchronization
        mc2w.attribute(MC2Strings.async_favorites, true);
        
        //default position system is MC2
        mc2w.attribute(MC2Strings.aposition_system, MC2Strings.MC2);

        //include POI info separately 
        mc2w.attribute(MC2Strings.afav_info_in_desc, false);
        
        // <!ELEMENT favorite_id_list ( favorite_id* )>
        // no favorite_id_list element makes server send all favorites
        if (synchData.clientFavs.length > 0) {
            mc2w.startElement(MC2Strings.tfavorite_id_list);
            for(int i=0; i < synchData.clientFavs.length; i++) {
                // <!ELEMENT favorite_id ( #PCDATA )>
                mc2w.elementWithText(MC2Strings.tfavorite_id,
                        synchData.clientFavs[i].getServerId());
            }
            mc2w.endElement(MC2Strings.tfavorite_id_list);
        } // else no existing favorites
        
        // <!ELEMENT delete_favorite_id_list ( favorite_id+ )>
        if (synchData.clientDeletedFavs.length > 0) {
            mc2w.startElement(MC2Strings.tdelete_favorite_id_list);
            for(int i=0; i < synchData.clientDeletedFavs.length; i++) {
                // <!ELEMENT favorite_id ( #PCDATA )>
                mc2w.elementWithText(MC2Strings.tfavorite_id,
                        synchData.clientDeletedFavs[i].getServerId());
            }
            mc2w.endElement(MC2Strings.tdelete_favorite_id_list);
        } // else no fav to delete        
            
        // <!ELEMENT add_favorite_list ( favorite+ )>
        if (synchData.clientNewFavs.length > 0) {
            mc2w.startElement(MC2Strings.tadd_favorite_list);
            for(int i=0; i < synchData.clientNewFavs.length; i++) {
                synchData.clientNewFavs[i].write(mc2w);
            }
            mc2w.endElement(MC2Strings.tadd_favorite_list);
        } // else no favorites to add
    }

}
