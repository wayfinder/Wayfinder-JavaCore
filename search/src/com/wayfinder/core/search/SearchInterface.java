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
package com.wayfinder.core.search;

import com.wayfinder.core.search.onelist.OneListSearch;
import com.wayfinder.core.search.provider.ProviderSearch;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;


/**
 * <p>This is the main entry point for using the search functionality of the
 * Java Core APIs.</p>
 *
 * <p>A guide on how to use the various functions and what you need to
 * implement is available in the
 * <a href="package-summary.html">package documentation</a>. 
 */
public interface SearchInterface {
    
    
    
    /**
     * Obtains a {@link ProviderSearch} for doing provider-specific searches.
     * <p>
     * For more information on provider-based searches, see the documentation in
     * {@link com.wayfinder.core.search.provider}
     * 
     * @return A {@link ProviderSearch}
     */
    public ProviderSearch getProviderSearch();
    
    
    
    /**
     * Obtains a {@link OneListSearch} for doing OneList-searches
     * <p>
     * For more information on provider-based searches, see the documentation in
     * {@link com.wayfinder.core.search.onelist}
     * 
     * @return A {@link OneListSearch}
     */
    public OneListSearch getOneListSearch();
    
    

    /**
     * Loads the history of the most recently made searches.
     * 
     * @param listener The {@link SearchListener} to call when the history is
     * loaded
     * @throws IllegalArgumentException if listener is null
     */
    public void loadSearchHistory(SearchListener listener);
    
    
    
    /**
     * Loads the current list of {@link TopRegion} objects.
     * <p>
     * The first time this method is called, the object returned from it will
     * always return a valid object but with the size set to zero.
     * <p>
     * Once the list has been read from persistant storage, the provided
     * {@link TopRegionListener} will be called with an updated version of the
     * collection.
     * <p>
     * At the same time the server will be contacted to ensure that the currently
     * stored list in the client is the most recent one. If the server returns
     * a new list, the provided {@link TopRegionListener} will be called once
     * again with the updated list.
     * <p>
     * If a list has previously been loaded, a cached version will be returned
     * immediately.
     * <p>
     * The {@link TopRegion} objects in the provided collection will be sorted
     * alphabetically.
     * 
     * @param listener The {@link TopRegionListener} to provide list updates to.
     * @return A {@link RequestID} to identify the request
     */
    public RequestID loadTopRegions(TopRegionListener listener);
    
    
    
    /**
     * Determines the {@link TopRegion} for a given position
     * <p>
     * Once the {@link TopRegion} has been determined, 
     * {@link TopRegionListener#currentTopRegion(RequestID, Position, TopRegion)} 
     * will be called.
     * 
     * @param position The position to determine the TopRegion from
     * @param listener The {@link TopRegionListener} to receive the callback
     * @return A {@link RequestID} to identify the request
     */
    public RequestID determineTopRegionForPosition(Position position, TopRegionListener listener);
    
    
    
    /**
     * Loads the current list of {@link Category} objects.
     * <p>
     * Once the list has been read from persistant storage, the provided
     * {@link CategoryCollection} will be called with an updated version of the
     * collection.
     * <p>
     * At the same time the server will be contacted to ensure that the currently
     * stored list in the client is the most recent one. If the server returns
     * a new list, the provided {@link CategoryCollection} will be called once
     * again with the updated list.
     * <p>
     * If a list has previously been loaded, a cached version will be returned
     * immediately.
     * <p>
     * The {@link Category} objects in the provided collection will be sorted
     * alphabetically.
     * 
     * @param listener The {@link CategoryCollection} to provide list updates to.
     * @return A {@link RequestID} to identify the request
     */
    public RequestID loadCategories(CategoryListener listener);
    
    
    
    /**
     * Loads the current list of {@link Category} objects that are specific for
     * the location provided.
     * <p>
     * Once the list has been read from persistant storage, the provided
     * {@link CategoryCollection} will be called with an updated version of the
     * collection.
     * <p>
     * At the same time the server will be contacted to ensure that the currently
     * stored list in the client is the most recent one. If the server returns
     * a new list, the provided {@link CategoryCollection} will be called once
     * again with the updated list.
     * <p>
     * If a list has previously been loaded, a cached version will be returned
     * immediately.
     * <p>
     * The {@link Category} objects in the provided collection will be sorted
     * alphabetically.
     * 
     * @param position The {@link Position} used to determine the specific
     * categories. If null or invalid, this call will be the same as if doing
     * {@link #loadCategories(CategoryListener)}
     * @param listener The {@link CategoryCollection} to provide list updates to.
     * @return A {@link RequestID} to identify the request
     */
    public RequestID loadCategories(Position position, CategoryListener listener);
    
    
    
     /**
      * <p>Initiate a check on the server if there is another category tree for 
      * area pointed by the given position</p>
      * <p>When the check is done updateListener.categoryTreeUpdateDone() will
      * be called.</p> 
      * <p>NOTE: This intended for controlling the frequency of tree updates.
      * For listening to all changes to the category tree use 
      * {@link #addCategoryTreeChangeListener(CategoryTreeChangeListener)}</p>
      * 
      * <p>Category trees are position dependent. There is no generic tree.
      * so this method needs to be called at least once to have a category tree
      * </p>
      * 
      * @param pos The {@link Position} used to determine the specific
      * categories. position != null and position.isValid() must be true.
      * @param updateListener The {@link CategoryTreeUpdateRequestListener} to be 
      * notified when the request ends. 
      * @return A {@link RequestID} to identify the request 
      */
     public RequestID updateCategoryTree(Position pos, 
                 CategoryTreeUpdateRequestListener updateListener);
     
     
     /**
      * <p>Synchronous non blocking method to obtain the instance of the current
      * category tree.<p>
      * <p>WARNING: This can return <code>null</code> if there is no previous
      * category tree loaded yet and no successful update request.</p>
      * 
      * @return The current {@link CategoryTree} or <code>null</code> if there 
      * is none
      */
     public CategoryTree getCurrentCategoryTree();
     
     
     /**
      * <p>Register a listener for changes notification of the current 
      * category tree that can be obtain through 
      * {@link #getCurrentCategoryTree()}</p> 
      * <p>If there is no current tree, this will initiate loading from storage 
      * and changeListener will be notified if successful</p>
      * <p>WARNING: In order to allow multiple change listeners this method is 
      * in pair with 
      * {@link #removeCategoryTreeChangeListener(CategoryTreeChangeListener)}.
      * Don't forget to call the last one when the category tree is 
      * not needed/visible anymore!</p>
      * 
      * @param changeListener The {@link CategoryTreeChangeListener} to be notified
      * when the category tree is changed.
      */
     public void addCategoryTreeChangeListener(
             CategoryTreeChangeListener changeListener);

     
     /**
      * <p> Unregister a previous registered listener when we are not interested
      * in changes anymore. </p> 
      * @param listener The {@link CategoryTreeChangeListener} that was previous
      * registered
      * 
      * @see #addCategoryTreeChangeListener(CategoryTreeChangeListener)
      */
     public void removeCategoryTreeChangeListener(
             CategoryTreeChangeListener listener);
}
