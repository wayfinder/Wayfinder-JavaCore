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
package com.wayfinder.core.search.internal;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.search.CategoryListener;
import com.wayfinder.core.search.CategoryTree;
import com.wayfinder.core.search.CategoryTreeChangeListener;
import com.wayfinder.core.search.CategoryTreeUpdateRequestListener;
import com.wayfinder.core.search.SearchInterface;
import com.wayfinder.core.search.SearchListener;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.TopRegionListener;
import com.wayfinder.core.search.SearchReply.SearchArea;
import com.wayfinder.core.search.internal.BasicSearchMatchImpl.PositionDistanceComparator;
import com.wayfinder.core.search.internal.BasicSearchMatchImpl.MatchNameComparator;
import com.wayfinder.core.search.internal.category.CategoryHolder;
import com.wayfinder.core.search.internal.categorytree.CategoryTreeHolder;
import com.wayfinder.core.search.internal.topregion.CurrentTopRegionMC2Request;
import com.wayfinder.core.search.internal.topregion.TopRegionHolder;
import com.wayfinder.core.search.onelist.MatchDetailsRequestListener;
import com.wayfinder.core.search.onelist.OneListSearch;
import com.wayfinder.core.search.onelist.OneListSearchListener;
import com.wayfinder.core.search.onelist.OneListSearchReply;
import com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch;
import com.wayfinder.core.search.provider.ProviderSearch;
import com.wayfinder.core.search.provider.ProviderSearchListener;
import com.wayfinder.core.search.provider.ProviderSearchReply;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.Comparator;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.pal.util.StringCollator;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;

public final class SearchModule
implements SearchInterface, ProviderSearch, OneListSearch {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(SearchModule.class);
    
    private final ModuleData m_moduleData;
    private final SharedSystems m_sharedSystems;
    private final MC2Interface m_mc2Ifc;
    
    private final TopRegionHolder m_regionHolder;
    private final CategoryHolder m_genericCatHolder;
    private final CategoryHolder m_positionCatHolder;
    private final SearchHolder m_searchHolder;
    private final CategoryTreeHolder m_categoryTreeHolder;

    
    SearchModule(ModuleData modData, SharedSystems systems) {
        m_moduleData = modData;
        m_sharedSystems = systems;
        m_mc2Ifc = systems.getMc2Ifc();
        m_regionHolder = new TopRegionHolder(m_sharedSystems, modData.getCallbackHandler(), m_mc2Ifc);
        m_genericCatHolder = new CategoryHolder(SearchConstants.PERSISTENCE_CATEGORIES_GENERIC, m_sharedSystems, modData.getCallbackHandler(), m_mc2Ifc);
        m_positionCatHolder = new CategoryHolder(SearchConstants.PERSISTENCE_CATEGORIES_POSITION, m_sharedSystems, modData.getCallbackHandler(), m_mc2Ifc);
        m_searchHolder = new SearchHolder(m_sharedSystems, m_mc2Ifc);
        m_categoryTreeHolder = new CategoryTreeHolder(
                m_sharedSystems.getSettingsIfc(),
                m_sharedSystems.getPersistentModule(),
                m_mc2Ifc, modData.getCallbackHandler());
    }
    
    
    /**
     * @param modData
     * @return
     * @hide Not for public use
     */
    public static SearchInterface createSearchInterface(ModuleData modData, SharedSystems systems) {
        return new SearchModule(modData, systems);
    }
    
    
    //-------------------------------------------------------------------------
    // SearchInterface ifc
    
    // search
    
    public OneListSearch getOneListSearch() {
        return this;
    }


    public ProviderSearch getProviderSearch() {
        return this;
    }
    
    
    public void loadSearchHistory(SearchListener listener) {
        if(LOG.isWarn()) {
            LOG.warn("SearchModule.loadSearchHistory()", 
                    "No implementation yet");
        }
    }
    
    
    // top regions
    
    
    public RequestID loadTopRegions(TopRegionListener listener) {
        final RequestID id = RequestID.getNewRequestID();
        m_regionHolder.addListenerForUpdate(id, listener);
        return id;
    }
    
    
    public RequestID determineTopRegionForPosition(Position position, TopRegionListener listener) {
        if(position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        } else if(!position.isValid()) {
            throw new IllegalArgumentException("Position is not valid");
        }
        final RequestID id = RequestID.getNewRequestID();
        m_mc2Ifc.pendingMC2Request(new CurrentTopRegionMC2Request( id,
                position, m_moduleData.getCallbackHandler(), 
                listener, m_sharedSystems.getSettingsIfc().getGeneralSettings()));
        return id;
    }
    
    
    
    
    // categories
    
    
    public RequestID loadCategories(CategoryListener listener) {
        final RequestID id = RequestID.getNewRequestID();
        m_genericCatHolder.addListenerForUpdate(id, listener);
        return id;
    }
    
    
    public RequestID loadCategories(Position position, CategoryListener listener) {
        if( (position != null) && position.isValid() ) {
            final RequestID id = RequestID.getNewRequestID();
            m_positionCatHolder.addListenerForUpdate(id, listener, position);
            return id;
        } 
        return loadCategories(listener);
    }

    //category tree new interface

    public CategoryTree getCurrentCategoryTree() {
        return m_categoryTreeHolder.getCurrentCategoryTree();
    }
    
    public void addCategoryTreeChangeListener(
            CategoryTreeChangeListener listener) {
        m_categoryTreeHolder.addAsynchCategoryTreeChangeListener(listener);
    }

    public void removeCategoryTreeChangeListener(
            CategoryTreeChangeListener listener) {
        m_categoryTreeHolder.removeAsynchCategoryTreeChangeListener(listener);
    }

    public RequestID updateCategoryTree(Position pos,
            CategoryTreeUpdateRequestListener updateListener) {
        if (pos == null) {
            throw new IllegalArgumentException("Position paramter cannot be null");
        }
        final RequestID id = RequestID.getNewRequestID();
        m_categoryTreeHolder.updateCategoryTree(id, pos, updateListener);
        return id;
    }

    //-------------------------------------------------------------------------
    // ProviderSearch ifc


    public RequestID search(SearchQuery query, ProviderSearchListener listener) {
        final RequestID id = RequestID.getNewRequestID();
        m_searchHolder.scheduleRequest(new ProviderSearchRequest(
                id, query, listener, m_mc2Ifc, 
                m_moduleData.getCallbackHandler(), m_sharedSystems));
        return id;
    }
    

    public RequestID expandMatchList(ProviderSearchReply reply, int listIndex, int nbrOfMoreHits, ProviderSearchListener listener) {
        final RequestID id = RequestID.getNewRequestID();
        ProviderSearchReplyImpl replyInternal;
        if(reply instanceof ProviderSearchReplyImpl) {
            replyInternal = (ProviderSearchReplyImpl) reply;
        } else {
            throw new IllegalArgumentException("Foreign implementations of ProviderSearchReply are not allowed");
        }
        
        m_searchHolder.scheduleRequest(
                new ProviderExpandSearchRequest(
                        id, replyInternal, listIndex, nbrOfMoreHits, listener, 
                        m_mc2Ifc, m_moduleData.getCallbackHandler(), m_sharedSystems));
        return id;
    }

    
    //-------------------------------------------------------------------------
    // OneListSearch ifc
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearch#search(com.wayfinder.core.search.SearchQuery, com.wayfinder.core.search.onelist.OneListSearchListener)
     */
    public RequestID search(SearchQuery query, OneListSearchListener listener) {
        final RequestID id = RequestID.getNewRequestID();

        /*
         * We have decided to not adapt SearchHolder to handle these searches
         * and a separate state machine is not needed as we are not
         * dependent on the search descriptor.
         * 
         * Checking for  non-null listener is delegated to OneListSearchRequest
         * ctor.
         */
        if (query.getQueryType() == SearchQuery.SEARCH_TYPE_POSITIONAL) {
            Comparator comp = 
                new PositionDistanceComparator(
                        query.getPosition());
            
            new OneListSearchRequest(id,
                    query,
                    comp,
                    listener,
                    m_mc2Ifc,
                    m_moduleData.getCallbackHandler(), m_sharedSystems).schedule();
            return id;
        }
        else if (query.getQueryType() == SearchQuery.SEARCH_TYPE_ADDRESS) {
            Comparator comp = new MatchNameComparator(
                    m_sharedSystems.getPAL().getUtilFactory().getStringCollator(
                            StringCollator.STRENGTH_TERTIARY));
            
            new AddressGeocodingSearchRequest(
                    id, 
                    query, 
                    comp, 
                    listener, 
                    m_mc2Ifc, 
                    m_moduleData.getCallbackHandler(), m_sharedSystems).doRequest();
            return id;
        }
        else {
            throw new IllegalArgumentException("Unsupported query type!");
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.onelist.OneListSearch#requestDetails(com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch, com.wayfinder.core.search.onelist.MatchDetailsRequestListener)
     */
    public RequestID requestDetails(final OneListSearchReply.SearchMatch match,
            final MatchDetailsRequestListener listener) {
        final RequestID requestID = RequestID.getNewRequestID();
        
        if (!(match instanceof OneListSearchMatchImpl)) {
            throw new IllegalArgumentException("Not a valid Search match");
        }
        final OneListSearchMatchImpl oneListSearchMatch = (OneListSearchMatchImpl)match;
        
        if (oneListSearchMatch.additionalInfoExists()) {
            MC2RequestListener handler = new MatchDetailsResponseHandler(
                    requestID,oneListSearchMatch, listener, 
                    m_moduleData.getCallbackHandler());
            
            MC2Request request = new  MatchDetailsMC2Request(handler,
                    oneListSearchMatch, 
                    m_sharedSystems.getSettingsIfc().getGeneralSettings().getInternalLanguage());
            
            m_mc2Ifc.pendingMC2Request(request);
        } else {
            //we already have the info
            m_moduleData.getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
                public void run() {
                    listener.matchDetailsUpdated(requestID, oneListSearchMatch);
                }
            });
        }

        return requestID;
    }

    //-------------------------------------------------------------------------
    // data validation and casting :D
    
    public static void assertIsInternalSearchArea(SearchArea area) {
        if(!(area instanceof SearchAreaImpl)) {
            throw new IllegalArgumentException("Foreign implementations of SearchArea are not allowed.");
        }
    }

}
