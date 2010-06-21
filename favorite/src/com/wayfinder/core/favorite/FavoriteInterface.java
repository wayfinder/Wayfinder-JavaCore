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
package com.wayfinder.core.favorite;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.poiinfo.PoiInfo;

// FIXME: document that you can only send in InfoFieldList implementations
// created by Core. For serialization safety. Of course it would possible to
// do conversion using the methods in ifc.

/**
 * Entry point for working with user favorites
 * <p>All the methods should be call from UI Thread, and all callbacks will be 
 * called in UI Thread.</p>
 * <p>Favorites will be loaded from storage asynchronous on first call of 
 * any method (including {@link #synchronizeFavorites(FavoriteSynchListener)}
 * and {@link #getFavoriteListModel()} and when done the ListModel will be 
 * notified. Even this is a very fast operation is advisable to call 
 * {@link #preload(FavoriteLoadListener)} at startup of the application
 * </p>
 *       
 * <p>None of the methods are blocking. The effect is visible immediately 
 * except for {@link #synchronizeFavorites(FavoriteSynchListener)} and 
 * {@link #preload(FavoriteLoadListener)} which are asynchronous.</p>
 * <p>During synchronization writing operations on favorites are allowed 
 * but in some cases denied see {@link #removeFavorite(Favorite)}</p>
 * <p>Synchronization with the server is not called automatically, is 
 * resposability </p>  
 * 
 * <p>The favorites are automatically sorted by name</p> 
 *  
 */
public interface FavoriteInterface {

    /**
     * Initiate the loading from persistence storage in advance 
     * 
     * @param listener listener that will be called when the list is loaded 
     * or if there was an error during loading
     * 
     * @return a unique RequestID for this asynchronous call
     * or null if the loading has already been initiated by   
     */
    RequestID preload(FavoriteLoadListener listener);

    /**
     * Create and add a favorite with the give data
     * Call it only from only from UI Event thread
     * Action will be immediately visible in ListModel
     * 
     * @param name name of the new favorite cannot be null
     * @param description description of the new favorite cannot be null
     * @param iconName the name of the image of new favorite cannot be null
     * @param position position of the new favorite cannot be null
     * @param infoFieldList POI information of new favorite or null if 
     * dosen't exist 
     * 
     * @return the Favorite object that has been created and added 
     */
    Favorite addFavorite(String name, String description, String iconName,
            Position position, InfoFieldList infoFieldList);
    
    /**
     * Remove a favorite
     * Call it only from only from UI Event thread
     * Action will be immediately visible in ListModel
     * 
     * @param favorite
     * @return false if the favorite doesn't exist or could not be deleted
     * <p>
     * Note: returning false can happen in 2 cases when the favorite object 
     * is not one received with getFavorites or when favorite is 
     * involved in synchronization process that is currently running 
     * (the favorite has already sent to the server and cannot be deleted)
     */
    boolean removeFavorite(Favorite favorite);
    
    /**
     * <p>Simplified version of
     * {@link #replaceFavorite(Favorite, String, String, String, Position, InfoFieldList)} 
     * if only change of name and/or description is required, the rest of the 
     * fields will be copied.</p>
     * 
     * @param oldFavorite favorite to be replaced cannot be null
     * @param name name for the new favorite cannot be null
     * @param description description for the new favorite cannot be null
     * 
     * @return true if the favorite could be replaced 
     * 
     * @see #replaceFavorite(Favorite, String, String, String, Position, InfoFieldList)
     */
    boolean replaceFavorite(Favorite oldFavorite, 
            String name, String description);
    
    /**
     * Replace an existing favorite with a new one created from the given data
     * This is the only way to modify a favorite. 
     * Call it only from only from UI Event thread   
     * Action will be immediately visible in ListModel
     * <p/>
     * Note: returning false can happen in 2 cases when the oldFavorite object 
     * is not one received from with getFavorites or when oldFavorite is 
     * involved in synchronization process that is currently running
     * 
     * @param oldFavorite favorite to be replaced cannot be null
     * @param name name for the new favorite cannot be null
     * @param description description for the new favorite cannot be null
     * @param iconName the name of the image for the new favorite cannot be null
     * @param position position for the new favorite cannot be null
     * @param infoFieldList POI information of new favorite or null if 
     * dosen't exist 
     * 
     * @return true if the favorite could be replaced 
     * 
     * @see #replaceFavorite(Favorite, String, String)
     */
    boolean replaceFavorite(Favorite oldFavorite, 
            String name, String description, String iconName, 
            Position position, InfoFieldList infoFieldList);
    
    /** 
     * Request for synchronize favorites with the server, the method 
     * will return immediately, and listener will be notified when the synch 
     * is done or failed.
     * Listener methods will be called in UI Event thread. 
     * 
     * @return the RequestID associated with this request 
     */
    RequestID synchronizeFavorites(FavoriteSynchListener listener); 
    
    
    /**
     * Get the favorite list model. 
     * 
     * <p>The calls to ListModel methods must be done from UI Thread.
     * The ListModel will be automatically updated when adding, removing 
     * favorites and when synchronize with the server. <p>
     * <p>Same instance will be returned each time.<p> 
     *  
     * @return a ListModel with favorite objects sorted alphabetically 
     * by the name
     */
    ListModel getFavoriteListModel();
    
    /**
     * Register a global listener that will be notified for errors that occurs 
     * during writing of changes to favorite list persistence.
     * <br/>
     * Saving operation are done asynchronous and the result dosen't affect 
     * the current runtime list instead the effects will be visible only after 
     * restarting (e.g. lost new favorites, doubled favorites)
     * <br/>
     * All methods from listener will be called trough Main/UI Thread.
     *   
     * @param listener the listener
     */
    void setWriteErrorListener(FavoriteWriteErrorListener listener);
    
}
