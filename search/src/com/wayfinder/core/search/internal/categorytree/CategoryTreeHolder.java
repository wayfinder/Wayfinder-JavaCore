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
package com.wayfinder.core.search.internal.categorytree;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryTree;
import com.wayfinder.core.search.CategoryTreeChangeListener;
import com.wayfinder.core.search.CategoryTreeUpdateRequestListener;
import com.wayfinder.core.search.SearchInterface;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CancelError;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.persistence.PersistenceModule;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.core.shared.util.ListenerList;


/**
 * Holder of the data: current category tree, and listeners.
 * Implementation of the category tree access/update flow.
 * 
 * Threading synchronization is addressed using Active Object pattern, base 
 * on the fact that all methods are called from Main/UI Thread and all listener
 * notification are done trough the same thread using {@link CallbackHandler}
 * 
 * @see SearchInterface#updateCategoryTree(Position, CategoryTreeUpdateRequestListener)
 * @see SearchInterface#addCategoryTreeChangeListener(CategoryTreeChangeListener)
 * @see SearchInterface#removeCategoryTreeChangeListener(CategoryTreeChangeListener)
 * @see SearchInterface#getCurrentCategoryTree()
 * 
 * 
 */
public class CategoryTreeHolder implements CategoryTreePersistenceRequest.LoadListener {
    
    /**
     * All event callbacks are done trough this
     */
    private final CallbackHandler m_callHandler;
    
    /**
     * Used for language detection
     */
    private final InternalSettingsInterface m_settingsIfc; 
    
    private final MC2Interface m_mc2ifc;

    private final PersistenceModule m_persistance;
    
    
    private CategoryTreeImpl m_currentCatTree;
    
    private ListenerList m_asynchChangeListeners;    
    
    
    private volatile boolean loading;
    
    private volatile boolean loaded;
    
    private CategoryTreeMC2Request pendingUpdateRequest;
    
    /**
     * Ctr
     *  
     * @param settingsIfc Setting used to obtain current language
     * @param persistance PersistenceModule used to restore/save cached tree
     * @param mc2ifc MC2Interface used to post server update requests
     * @param callHandler Use to execute code in Main/UI Thread
     */
    public CategoryTreeHolder(
            InternalSettingsInterface settingsIfc,
            PersistenceModule persistance,
            MC2Interface mc2ifc,
            CallbackHandler callHandler) {
        
        m_settingsIfc = settingsIfc;
        m_persistance = persistance;
        m_mc2ifc = mc2ifc;
        m_callHandler = callHandler;
        
        m_asynchChangeListeners = new ListenerList();
    }
    
    //--- API
    /**
     * Call this only from Main/UI Thread
     * @return The current CategoryTree or null if there is none
     * 
     * @see SearchInterface#getCurrentCategoryTree()
     */
    public CategoryTree getCurrentCategoryTree() {
        return m_currentCatTree;
    }

    /**
     * <p>Internal method for requesting a server update of the tree.</p>
     * <p>This will first load the cached tree, and after initiate the update.
     * If the cached tree was not loaded yet, the update will be postponed 
     * until that is done. If there is an older postponed update that will be 
     * cancel (quite hard to happen unless client request updates for each 
     * position)</p>   
     *  
     * @param reqID Identify the request
     * @param pos Position for category tree
     * @param updateListener The client listener
     * 
     * @see SearchInterface#updateCategoryTree(Position, CategoryTreeUpdateRequestListener)
     */
    public void updateCategoryTree(final RequestID reqID,
                                   Position pos,
                                   final CategoryTreeUpdateRequestListener updateListener) {
        if (pendingUpdateRequest != null) {
            //another request is pending but has not been started yet
            pendingUpdateRequest.error(new CancelError("A new update of the categorytree has been requested"));
            pendingUpdateRequest = null;
        }
        
//        // XXX: quick fix to return a real tree and not send broken data to srv
//        m_callHandler.callInvokeCallbackRunnable(
//                new Runnable() {
//                    public void run() {
//                        //success and new tree
//                        changeCategoryTree(createDummyCategoryTree());
//                            if (updateListener != null) {
//                                updateListener.categoryTreeUpdateDone(reqID, true);
//                            }
//                    }
//                });
//        
//        // XXX: End quick hack - original code below:
        
        if (loadIfNeeded()) {
            pendingUpdateRequest = createCategoryTreeRequest(reqID, pos, updateListener);
        } else {
            m_mc2ifc.pendingMC2Request(createCategoryTreeRequest(reqID, pos, updateListener));
        }
    }
    
    /**
     * Add a change listener that will be notified trough Main/UI Thread
     * 
     * Initiate loading from storage if was not started yet. 
     * @param changeListener The listener to be added
     * 
     * @see SearchInterface#addCategoryTreeChangeListener(CategoryTreeChangeListener)
     */
    public void addAsynchCategoryTreeChangeListener(
            CategoryTreeChangeListener changeListener) {
        loadIfNeeded();
        m_asynchChangeListeners.add(changeListener);
    }

    /**
     * Remove a change listener that was previous added 
     * @param changeListener The listener to be removed
     * 
     * @see SearchInterface#removeCategoryTreeChangeListener(CategoryTreeChangeListener)
     * @see ListenerList for special caseses behavior 
     */
    public void removeAsynchCategoryTreeChangeListener(
            CategoryTreeChangeListener changeListener) {
        m_asynchChangeListeners.remove(changeListener);
    }

//--- loading from disk    
    /**
     * Check loading status and initiate loading from disk if necessary 
     * @return false of the loading from disk has been completed,
     * false otherwise  
     */
    private boolean loadIfNeeded() {
        if (loaded) {
            return false;
        }
        if (!loading) {
            loading = true;

            m_persistance.pendingReadPersistenceRequest(
                    new CategoryTreePersistenceRequest(this), 
                    PersistenceModule.SETTING_SEARCH_DATA);
        }
        return true;
    }
    
    /**
     * Implements {@link CategoryTreePersistenceRequest.LoadListener} to be 
     * notified when the loading is done.
     * This will also check the current language to be the same with the one 
     * of restored tree. 
     * 
     * @param obj The category tree restored or null it could not be restored 
     * 
     * @see CategoryTreePersistenceRequest.LoadListener#loadDone(Object)
     */
    public void loadDone(final Object obj) {
        m_callHandler.callInvokeCallbackRunnable(
                new Runnable() {
                    public void run() {
                        CategoryTreeImpl catTree = (CategoryTreeImpl) obj;
                        if (catTree != null &&
                            catTree.getLanguageId() == getCurrentLanguage().getId()) {
                            //success and language ok
                            changeCategoryTree((CategoryTreeImpl) catTree);
                            loaded = true;
                            loading = false;
                        }
                        doPendingRequestIfAny();
                    }
                });
    }
    
    /**
     * Create a post a saving request of category tree to PersistenceModule  
     * @param catTree The category tree that will be saved
     */
    private void postSaving(CategoryTreeImpl catTree) {
        m_persistance.pendingWritePersistenceRequest(
                new CategoryTreePersistenceRequest(catTree), 
                PersistenceModule.SETTING_SEARCH_DATA);
    }

//--- server request
    /**
     * Check if there was an update request during loading and postponed 
     * because of that. Post that request for execution now.
     * call it from main thread
     */
    private void doPendingRequestIfAny() {
        if (pendingUpdateRequest != null) {
            pendingUpdateRequest.updateCrcAndLanguage(
                     getCurrentCrc(), getCurrentLanguage());
            m_mc2ifc.pendingMC2Request(pendingUpdateRequest);
            
            pendingUpdateRequest = null;
        }
    }


    /**
     * Utility method for creating a update request together with the handler for 
     * the response
     *   
     * @param reqID Identifiy the request
     * @param pos Position for category tree
     * @param updateListener The client listener
     * @return A {@link CategoryTreeMC2Request} that can be posted to 
     * {@link MC2Interface} 
     */
    private CategoryTreeMC2Request createCategoryTreeRequest(final RequestID reqID, Position pos,
            final CategoryTreeUpdateRequestListener updateListener) {
        
        String crc = null;
        if (this.m_currentCatTree != null) {
            crc = m_currentCatTree.getCrc();
        }
        LanguageInternal language = this.getCurrentLanguage();
        
        MC2RequestListener listener = new MC2RequestListener() {
            
            public void requestDone(final Object result,final CoreError error) {
                if (result!=null) { 
                    postSaving((CategoryTreeImpl)result);
                }
                m_callHandler.callInvokeCallbackRunnable(
                        new Runnable() {
                            public void run() {
                                if (error != null) {
                                    if (updateListener != null) {
                                        updateListener.error(reqID, error);
                                    }
                                } else if (result == null) {
                                    //success but no changes 
                                    if (updateListener != null) {
                                        updateListener.categoryTreeUpdateDone(reqID, false);
                                    }
                                } else {
                                    //success and new tree
                                    changeCategoryTree((CategoryTreeImpl)result);
                                    if (updateListener != null) {
                                        updateListener.categoryTreeUpdateDone(reqID, true);
                                    }
                                }
                            }
                        });
            }
        };
        
        return new CategoryTreeMC2Request(pos, crc, language, listener);
    }    
    
//--- notification; 
    /**
     * Will change the category tree and notify all listeners.
     * Call this only from UI/Main thread
     * 
     * @param catTree The new CategoryTreeImpl
     */
    void changeCategoryTree(CategoryTreeImpl catTree) {
        m_currentCatTree = catTree;
        //get the actual array of listeners  
        Object[] asynchListeners = m_asynchChangeListeners.getListenerInternalArray();
    
        //no synchronization needed as the array will not be changed 
        for(int i = 0; i < asynchListeners.length; i++) {
            ((CategoryTreeChangeListener)asynchListeners[i]).categoryTreeChanged(catTree);
        }
    }
//--- 
    /**
     * @return current language of the system
     */
    LanguageInternal getCurrentLanguage(){
        return m_settingsIfc.getGeneralSettings().getInternalLanguage();
    }

    /**
     * @return the current cat tree crc or null if there is none
     */
    String getCurrentCrc() {
        if (m_currentCatTree != null) {
            return m_currentCatTree.getCrc();
        } else {
            return null;
        }
    }

    /**
     * <p>Helper method to ensure only categories under Core's control
     * are used.</p>
     * 
     * <p>Used to prevent client from sub classing the public
     * data classes and making their own variants with unexpected behavior.
     * The client can not construct new objects from the public data classes
     * without sub classing, as those classes are abstract.</p>
     *    
     * @param category the category object to be checked.
     * @throws IllegalArgumentException if the category is of the wrong type. 
     */
    public static void assertIsInternalCategory(Category category) {
        if(!(category instanceof HierarchicalCategoryImpl)) {
            throw new IllegalArgumentException("Foreign implementations of HierarchicalCategory are not allowed.");
        }
    }


//--- dummy data
    /**
     * @return a static created tree for testing 
     */
    static CategoryTreeImpl createDummyCategoryTree() {
            return new CategoryTreeImpl(
                    Language.EN_UK,
                    new Position(0,0), "",
                    UnitedKingdomTreeData.m_categoryTable,
                    UnitedKingdomTreeData.m_lookupTable,
                    UnitedKingdomTreeData.m_stringTable);
    }
}
