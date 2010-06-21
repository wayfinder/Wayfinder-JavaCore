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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.util.ArrayList;


public class FavoriteList implements Serializable {
    
    public static final int VERSION = 6;

    static final int OPERATION_SET_CRC = 1;
    static final int OPERATION_ADD_FAV = 2;
    static final int OPERATION_SET_FAV_STATUS = 3;
    
    static final String EMPTY_CRC = "";
    
    private ArrayList favArray = new ArrayList();
    
    private String serverCrc = EMPTY_CRC;
    
    private int nbrOfPhatoms;
    
    public void resetCrc() {
        serverCrc = EMPTY_CRC;
    }

    public void read(DataInputStream din) throws IOException {
        // version check   
        if (FavoriteList.VERSION != din.readInt()) {
            //here can go any import code form old version
            throw new IOException("Favorite list invalid version expected " + FavoriteList.VERSION);
        }
        serverCrc = din.readUTF();
        
        // Size
        int size = din.readInt();
        favArray.ensureCapacity(size);

        for(int i = 0; i < size; i++) {
            // Create empty favorite
            FavoriteInternal fav = new FavoriteInternal();
            // Read
            fav.read(din);
            fav.setPersistanceIndex(i);
            favArray.add(fav);
            if (fav.isPhantom()) {
                nbrOfPhatoms++;
            }
        }
    }
    
    public int readChanges(DataInputStream din) throws IOException {
        //read and apply the changes
        int count = 0;
        while (true) {
            int operation;
            try {
                operation = din.readInt();
                count++;
            } catch (EOFException e) {
                //end of changesFile reached 
                //no more changes
                break;
            }
            switch (operation) {
                case OPERATION_SET_CRC:
                    serverCrc = din.readUTF();
                    break;
                case OPERATION_ADD_FAV:
                    FavoriteInternal fav = new FavoriteInternal();
                    fav.read(din);
                    if (fav.isPhantom()) {
                        nbrOfPhatoms++;
                    }
                    fav.setPersistanceIndex(favArray.size());
                    favArray.add(fav);
                    break;
                case OPERATION_SET_FAV_STATUS:
                    int index = din.readInt();
                    int status = din.readInt();
                    //TODO add check for index and status
                    FavoriteInternal favI = (FavoriteInternal)favArray.get(index);
                    if ((!favI.isPhantom()) && 
                        (status == FavoriteInternal.STATUS_PHANTOM)) {
                        nbrOfPhatoms++;
                    }
                    favI.setStatus(status);
                    break;
                default:
                    throw new IOException("Favorile list corrupted persistent data");
            }
        }
        return count;
    }

    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(VERSION);
        dout.writeUTF(serverCrc);
        // Size
        int n = favArray.size();
        dout.writeInt(n);
        for (int i = 0; i < n; i++) {
            FavoriteInternal fav = (FavoriteInternal)favArray.get(i);
            fav.write(dout);
        }
    }
    
    public void removePhantoms() {
        if (nbrOfPhatoms == 0) return; //nothing to clean
        int n = favArray.size();
        ArrayList newFavArray = new ArrayList(n-nbrOfPhatoms);
        
        for(int i = 0, j = 0; i < n; i++) {
            FavoriteInternal fav = (FavoriteInternal)favArray.get(i);
            if (!((FavoriteInternal)favArray.get(i)).isPhantom()) {
                fav.setPersistanceIndex(j++);
                newFavArray.add(fav);
            }
        }

        nbrOfPhatoms = 0;
        favArray = newFavArray;
    }

    //--- access
    public int size() {
        return favArray.size();
    }

    public String getServerCrc() {
        return serverCrc;
    }

    public ArrayList getInternalArray() {
        return favArray;
    }

}   
