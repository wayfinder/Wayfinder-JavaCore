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

import com.wayfinder.core.shared.util.ArrayList;

public class FavoriteSynchData {

    static final FavoriteInternal[] EMPTY_FAV_ARRAY = new FavoriteInternal[0];
    //--- request
    FavoriteInternal[] clientFavs = EMPTY_FAV_ARRAY;
    FavoriteInternal[] clientDeletedFavs = EMPTY_FAV_ARRAY;
    FavoriteInternal[] clientNewFavs = EMPTY_FAV_ARRAY;
    
    //--- response
    FavoriteInternal[] serverDeletedFavs = EMPTY_FAV_ARRAY;
    FavoriteInternal[] serverNewFavs = EMPTY_FAV_ARRAY;
    
    String serverCrc = FavoriteList.EMPTY_CRC;
    
    public FavoriteSynchData(ArrayList visibleFav, ArrayList deletedFav) {
        int size = visibleFav.size();
        ArrayList clientFavsTmp = new ArrayList(size);
        ArrayList clientNewFavsTmp = new ArrayList();
        
        for (int i=0; i < size; i++) {
            FavoriteInternal fav = (FavoriteInternal)visibleFav.get(i);
            switch (fav.getStatus()) {
            case FavoriteInternal.STATUS_ON_SERVER: 
                clientFavsTmp.add(fav);break;
            case FavoriteInternal.STATUS_NEW: 
                clientNewFavsTmp.add(fav);break;
            // intentional no default    
            }
        }
        clientFavs = getFavArray(clientFavsTmp);
        clientNewFavs = getFavArray(clientNewFavsTmp);
        clientDeletedFavs = getFavArray(deletedFav); 
    }

    private static FavoriteInternal[] getFavArray(ArrayList favList) {
        if (favList != null && favList.size() > 0) {
            FavoriteInternal[] res = new FavoriteInternal[favList.size()];
            favList.copyInto(res);
            return res;
        } else {
            return EMPTY_FAV_ARRAY;
        }
    }
    
    public FavoriteInternal findFavByID(int id) {
        for (int i=0; i< clientFavs.length; i++) {
            if ( id == clientFavs[i].getServerId()) {
                return clientFavs[i];
            }
        }
        return null;
    }
    
    public void setResult(String serverCrc, ArrayList serverDeletedFavs, ArrayList serverNewFavs) {
        this.serverCrc = serverCrc;
        this.serverDeletedFavs = getFavArray(serverDeletedFavs);
        this.serverNewFavs = getFavArray(serverNewFavs);
    }

    public boolean hasChanges() {
        return (serverDeletedFavs.length + serverNewFavs.length > 0);
    }
}
