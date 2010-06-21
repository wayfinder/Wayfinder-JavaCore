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

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.ModuleData;
import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.FavoriteInterface;
import com.wayfinder.core.favorite.FavoriteLoadListener;
import com.wayfinder.core.favorite.FavoriteSynchListener;
import com.wayfinder.core.favorite.FavoriteWriteErrorListener;
import com.wayfinder.core.favorite.ListModel;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldListImpl;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.util.ArrayList;
import com.wayfinder.pal.persistence.PersistenceLayer;

public class FavoriteModule implements FavoriteInterface, WriteExceptionHandler {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(FavoriteModule.class);

    private PersistenceLayer palPersistence;

    private CallbackHandler callback;

    private FavoritePersistence favPersistence;

    private MC2Interface mc2Ifc;
    
    FavoriteListModel favModel;
    
    boolean loading;
    
    boolean loaded;

    volatile boolean pendingSynch;
    
    FavoriteSynchListener pendingSynchListener;
    
    RequestID pendingRequestID;

    ArrayList deletedFav = new ArrayList();
    
    String serverCrc = FavoriteList.EMPTY_CRC;
    
    boolean clientChanges = false;
    
    volatile FavoriteWriteErrorListener writeErrorListener;
    
    public FavoriteModule(CallbackHandler callbackHandler,
            PersistenceLayer persistenceLayer, WorkScheduler workScheduler, MC2Interface mc2Ifc2) {
        this.callback = callbackHandler;
        this.palPersistence = persistenceLayer;
        this.favModel = new FavoriteListModel();
        this.favPersistence = new FavoritePersistence(workScheduler, palPersistence, this);
        this.mc2Ifc = mc2Ifc2; 
    }

    /**
     * @param mc2Ifc 
     * @param systems 
     * @param aModData
     * @return
     * @hide Not for public use
     */
    public static FavoriteInterface createFavoriteInterface(
            ModuleData modData, SharedSystems systems) {
        return new FavoriteModule(
                modData.getCallbackHandler(),
                modData.getPAL().getPersistenceLayer(),
                systems.getWorkScheduler(),
                systems.getMc2Ifc());
    }


    private void clientChangesTrue() {
        clientChanges = true;
    }
    

    // ---------------------------------------------------------------------
    // implementation of methods in FavoriteInterface. These are called
    // by client. setWriteErrorListener() is not here but further down.

    public ListModel getFavoriteListModel() {
        preload(null);
        return favModel;
    }


    public Favorite addFavorite(String name, String description, 
            String iconName, Position position, InfoFieldList infoFieldList) {
        preload(null);
        FavoriteInternal fav = new FavoriteInternal(name, description, iconName,
                position, convertToInfoFieldListImpl(infoFieldList));
        favPersistence.postAddFav(fav);
        favModel.addFav(fav);
        clientChangesTrue();
        return fav;
    }

    public boolean removeFavorite(Favorite favorite) {
        if (!(favorite instanceof FavoriteInternal)) {
            throw new IllegalArgumentException(
                    "Invalid Favorite was not created through core " 
                    + favorite.getClass().getName());
        }
        FavoriteInternal favInt = (FavoriteInternal) favorite;
        
        if (!favInt.markDeleted()) {
            return false;
        }

        preload(null);
        if (favInt.isOnServer()) {
            deletedFav.add(favInt);
        }
        favPersistence.postDelFav(favInt);
        favModel.removeFav(favInt);
        clientChangesTrue();
        return true;
    }

    //TODO inconsistent as this dosen't return the new favorite as add method
    public boolean replaceFavorite(Favorite oldFavorite, String name,
            String description, String iconName, Position position,  
            InfoFieldList infoFieldList) {
        
        if (!(oldFavorite instanceof FavoriteInternal)) {
            throw new IllegalArgumentException(
                    "Invalid Favorite was not created through core " 
                    + oldFavorite.getClass().getName());
        }

        FavoriteInternal oldFav = (FavoriteInternal) oldFavorite;

        FavoriteInternal newFav = new FavoriteInternal(
                name, description, iconName, position,
                convertToInfoFieldListImpl(infoFieldList));
        
        if (!oldFav.markDeleted()) {
            return false;
        }
        if (oldFav.isOnServer()) {
            deletedFav.add(oldFav);
        }

        preload(null);
        favPersistence.postReplaceFav(oldFav,newFav);
        favModel.replaceFav(oldFav,newFav);
        clientChangesTrue();
        return true;
    }
    
    public boolean replaceFavorite(Favorite oldFavorite, String name,
            String description) {
        return replaceFavorite(oldFavorite,name,description,
                oldFavorite.getIconName(),oldFavorite.getPosition(),
                oldFavorite.getInfoFieldList());
    }
//--- 

    /**
     * only one synch call is allowed at a time
     */
    public RequestID synchronizeFavorites(final FavoriteSynchListener listener) {
        //another request is pending or has not been completed yet
        if (pendingSynch) {
            return null;
        }
        
        //load favorite if were not loaded yet
        preload(null);
        
        //TODO handle this here by sending and error to the listener
        pendingSynch = true;
        pendingRequestID = RequestID.getNewRequestID();
        pendingSynchListener =  listener;

        //TODO check the status, postpone, cancel
        // check if the favorite were loaded 
        if (loaded) {
            initiatePendingSynch();//post request immediately
        } // else will be called after loading
        
        return pendingRequestID;
    }

    /**
     * this is allowed to run only once per runtime
     */
    public RequestID preload(final FavoriteLoadListener listener) {
        if (loaded || loading) {
            return null;
        }
        
        final RequestID reqID = RequestID.getNewRequestID();
        loading = true;
        favPersistence.postInitFav(new FavoriteLoadHandler() {
            public void handleException(final Exception e) {
                callback.callInvokeCallbackRunnable(new Runnable() {
                    public void run() {
                        if (listener != null) {
                            listener.error(reqID, new CoreError(
                                    "Could not load favorites from storage: "
                                            + e.toString()));
                        }
                        // nothing else to do
                        loading = false;
                        loaded = true;
                        initiatePendingSynch();
                    }
                });
            }

            public void handleList(final ArrayList favs, final String crc) {
                callback.callInvokeCallbackRunnable(new Runnable() {
                    public void run() {
                        favoritesLoaded(favs, crc);
                        if (listener != null) {
                            listener.loaded(reqID, favs.size());
                        }
                        loading = false;
                        loaded = true;
                        initiatePendingSynch();
                    }
                });
            }
        });
        return reqID;
    }
    

    // ---------------------------------------------------------------------
    // utilities for public interface

    /**
     * @throws IllegalArgumentException if list is not a InfoFieldListImpl.
     */
    InfoFieldListImpl convertToInfoFieldListImpl(InfoFieldList list) {
        try {
            return (InfoFieldListImpl) list;
        } catch (ClassCastException e) {
            String err = "Only InfoFieldList-implementations created by"
                         + " Core are allowed as parameters in"
                         + "FavoriteInterface. Param was: " + list.toString();
            throw new IllegalArgumentException(err);
        }
    }


    // ---------------------------------------------------------------------

    private void favoritesLoaded(ArrayList favs, String crc) {
        int initialSize = favModel.getSize();
        this.serverCrc = crc;
        
        for(int i=0, n=favs.size(); i < n; i++) {
            FavoriteInternal fav = (FavoriteInternal)favs.get(i);
            if (fav.isVisible()) {
                favModel.addFavSilently(fav);
            } else if (fav.getStatus() == FavoriteInternal.STATUS_DELETED){ 
                deletedFav.add(fav);
            }
        }
        //notify that the whole list has changed
        if (initialSize < favModel.getSize()) {
            favModel.notifyFullChange();
        }
    }
    
    private void initiatePendingSynch() {
        if (pendingSynch) {
            if (clientChanges || serverCrc.equals(FavoriteList.EMPTY_CRC)) {
                postFullSynchRequest(pendingRequestID, pendingSynchListener);
            } else {
                //do a crc check first if the we have no client
                //changes
                postCrcCheckRequest(pendingRequestID, pendingSynchListener);
            }
        }
    }
    
    private void postCrcCheckRequest(final RequestID reqID,
            final FavoriteSynchListener listener) {
        if(LOG.isInfo()) {
            LOG.info("FavoriteModule","post FavoriteCrcRequest reqID=" + reqID);
        }
        
        FavoriteCrcRequest crcRequest = new FavoriteCrcRequest(this.serverCrc,
                new MC2RequestListener() {
                    public void requestDone(final Object result,final CoreError error) {
                        if (error != null) {
                            if(LOG.isError()) {
                                LOG.error("FavoriteModule","done FavoriteCrcRequest error: " + error.toString());
                            }
                            
                        } else {
                            if(LOG.isInfo()) {
                                LOG.info("FavoriteModule","done FavoriteCrcRequest match: " + result.toString());
                            }
                        }
                        callback.callInvokeCallbackRunnable(new Runnable() {
                            public void run() {
                                if (error != null) {
                                    if (listener != null) {
                                        listener.error(reqID, error);
                                    }
                                    pendingSynch = false;
                                } else { 
                                    //check if crc match 
                                    if (((Boolean)result).booleanValue()) {
                                        //yupii no need to fully synchronize
                                        if (listener != null) {
                                            listener.synchronizeDone(reqID, false);
                                        }
                                        pendingSynch = false;
                                    } else {
                                        postFullSynchRequest(reqID,listener); 
                                    }
                                }
                            }
                        });
                    }
                });
        mc2Ifc.pendingMC2Request(crcRequest);

    }
    
    private void postFullSynchRequest(final RequestID reqID,
            final FavoriteSynchListener listener) {
        if(LOG.isInfo()) {
            LOG.info("FavoriteModule","post FavoriteSynchRequest reqID=" + reqID);
        }
        //all changes will be sent
        clientChanges = false;
        FavoriteSynchData data = new FavoriteSynchData(
                favModel.getInternalArray(), deletedFav);
        //delete favs that will be sent to server are removed 
        //will be added back if the request fails
        deletedFav.clear(); 
        //new favs that are sent to server are marked 
        //will be marked back as STATUS_NEW if failed  
        for(int i=0; i < data.clientNewFavs.length; i++) {
            data.clientNewFavs[i].setStatus(FavoriteInternal.STATUS_NEW_SENDING);
        }  
        
        FavoriteSynchRequest synchRequest = new FavoriteSynchRequest(data,
                new MC2RequestListener(){
                    public void requestDone(Object result, final CoreError error) {
                        final FavoriteSynchData data = (FavoriteSynchData)result;
                        if (error != null) {
                            if(LOG.isError()) {
                                LOG.error("FavoriteModule",
                                        "done FavoriteSynchRequest error: " + error.toString());
                            }
                            
                        } else {
                            if(LOG.isInfo()) {
                                LOG.info("FavoriteModule",
                                        "done FavoriteSynchRequest changes: " + data.hasChanges());
                            }
                        }                        
                        callback.callInvokeCallbackRunnable(new Runnable() {
                            public void run() {
                                synchronizeDone(error, data);
                                if (listener != null) {
                                    if (error == null) {
                                        listener.synchronizeDone(reqID, 
                                                data.hasChanges());
                                    } else {
                                        listener.error(reqID, error);
                                    }
                                    pendingSynch = false;
                                }
                            }
                        });
                    }
                });
        mc2Ifc.pendingMC2Request(synchRequest);
    }

    
    private void synchronizeDone(CoreError error, FavoriteSynchData data) {
        if (error == null) {
            if(LOG.isInfo()) {
                LOG.info("FavoriteModule.synchronizeDone()", "success");
            }
            favPersistence.postSynchChanges(data);
            
            serverCrc = data.serverCrc;
            int minIndex = favModel.getSize();
            int maxIndex = 0;
            for(int i=0; i < data.clientDeletedFavs.length; i++){
                data.clientDeletedFavs[i].setStatus(
                        FavoriteInternal.STATUS_PHANTOM);
            }
            for(int i=0; i < data.serverDeletedFavs.length; i++) {
                FavoriteInternal fav = data.serverDeletedFavs[i];
                fav.setStatus(FavoriteInternal.STATUS_PHANTOM);
                int index = favModel.removeFavSilently(fav);
                if (index != -1) {
                    if (index < minIndex) {
                        minIndex = index;
                    } 
                    if (index >= maxIndex) {
                        maxIndex = index;
                    } else {
                        //shift all index with 1
                        maxIndex--;
                    }
                }                
            }
            if (data.serverDeletedFavs.length > 0) {
                //add back the shifted indexes
                maxIndex += data.serverDeletedFavs.length - 1;
            }
            
            for(int i=0; i < data.clientNewFavs.length; i++) {
                FavoriteInternal fav = data.clientNewFavs[i];
                fav.setStatus(FavoriteInternal.STATUS_PHANTOM);
                favModel.removeFavSilently(fav);
                //no need to update will be include in serverNewFavs
            }
            for(int i=0; i < data.serverNewFavs.length; i++) {
                int index = favModel.addFavSilently(data.serverNewFavs[i]);
                if (index < minIndex) {
                    minIndex = index;
                } 
                if (index >= maxIndex) {
                    maxIndex = index;
                } else {
                    //shift maxim index as we progress
                    maxIndex++;
                }
            }
            
            if (minIndex < maxIndex) { 
                favModel.notifyChanges(minIndex, maxIndex);
            }
        } else {
            clientChanges = true;
            if(LOG.isError()) {
                LOG.error("FavoriteModule.synchronizeDone()", error.toString());
            }
            //revert the status STATUS_NEW_SENDING to STATUS_NEW
            for(int i=0; i < data.clientNewFavs.length; i++) {
                data.clientNewFavs[i].setStatus(FavoriteInternal.STATUS_NEW);
            }         
            deletedFav.add(data.clientDeletedFavs);
        }
    }

    public void setWriteErrorListener(FavoriteWriteErrorListener listener) {
        this.writeErrorListener = listener;
    }

    public void handleSavingSynchError(final Exception e) {
        if (writeErrorListener == null) return;
        callback.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                if (writeErrorListener != null) {
                    writeErrorListener.errorWhenSavingSynch(e);
                }
            }
        });
    }

    public void handleWriteChangeError(final FavoriteInternal fav, final Exception e) {
        if (writeErrorListener == null) return;
        callback.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                if (writeErrorListener != null) {
                    writeErrorListener.errorWhenSavingChange(fav, e);
                }
            }
        });
    }

}
