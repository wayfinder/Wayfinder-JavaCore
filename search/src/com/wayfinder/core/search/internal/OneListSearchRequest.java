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

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.onelist.OneListSearchListener;
import com.wayfinder.core.search.onelist.OneListSearchReply;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.Comparator;
import com.wayfinder.core.shared.util.WFUtil;

/**
 * <p>Implements the request flow and reply combining for OneListSearch.</p>
 */
final class OneListSearchRequest implements OneListSearchMC2Request.RequestListener {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(OneListSearchRequest.class);
    
    private final RequestID m_reqID;
    private final SearchQuery m_query;
    private final Comparator m_matchComp;
    private final OneListSearchListener m_listener;

    private final MC2Interface m_mc2Ifc;
    private final CallbackHandler m_callHandler;
    private final SharedSystems m_systems;
    
    private final String m_sorting;
    
    // protected by synchronization
    private int m_currentRound;
    private OneListSearchReplyImpl m_lastReply;


    /**
     * Search descriptor is not used for these searches. The XML specification
     * specifies round 0 and 1. (one_search_request.round: 
     * "Search round. Round 0 = Fast internal search,
     *  Round 1 = Slow external provider search.)"
     */
    private static final int NBR_ROUNDS = 2;
    
    /**
     * Creates a new instance.
     * 
     * @param id the RequestID to report back to listener.
     * @param query the search query. Only queries created with
     * {@link SearchQuery#createPositionalQuery(String, com.wayfinder.core.search.Category, com.wayfinder.core.shared.Position, int, boolean)}
     * are supported. Other queries will throw IllegalArgumentException.
     * @param matchComparator a {@link Comparator} that defines how the results 
     * should be sorted
     * @param listener the listener to receive search results.
     * @param mc2Ifc where to to post MC2 requests.
     * @param handler system call-back handler for generating listener
     * call-backs.
     * @param systems will be used to obtain settings.
     */
    OneListSearchRequest( RequestID id,
                          SearchQuery query, 
                          Comparator matchComparator,
                          OneListSearchListener listener,
                          MC2Interface mc2Ifc,
                          CallbackHandler handler, 
                          SharedSystems systems) {
        
        if (id == null || query == null || matchComparator == null 
                || listener == null || mc2Ifc == null || handler == null 
                || systems == null) {
            throw new IllegalArgumentException("One of the arguments is null.");
        }
        m_reqID = id;
        m_query = query;
        if (query.getQueryType() != SearchQuery.SEARCH_TYPE_POSITIONAL) {
            throw new IllegalArgumentException("OneListSearch.search(...) only" +
            		"supports queries created with SearchQuery.createPositionalQuery()");
        }
        m_matchComp = matchComparator;
        m_listener = listener;
        
        m_mc2Ifc = mc2Ifc;
        m_callHandler = handler;
        m_systems = systems;
        
        if (m_matchComp instanceof BasicSearchMatchImpl.PositionDistanceComparator) {
            m_sorting = OneListSearchMC2Request.DISTANCE_SORT;
        }
        else if (m_matchComp instanceof BasicSearchMatchImpl.MatchNameComparator) {
            m_sorting = OneListSearchMC2Request.ALFA_SORT;
        }
        else {
            m_sorting = OneListSearchMC2Request.DISTANCE_SORT;
        }

        synchronized (this) {
            m_currentRound = -1;
        }
    }
        
    
    /**
     * <p>Do necessary pre-processing and schedule a network request on
     * MC2Interface.</p>
     * 
     * <p>This method will not do time consuming tasks or block
     * on I/O so you do not need to call it in a separate thread.</p>
     * 
     * <p>Call this method only once.</p>
     */
    void schedule() {
        nextRound();
    }
    
    private synchronized void nextRound() {
        // need more rounds?
        if(++m_currentRound < NBR_ROUNDS) {
            m_mc2Ifc.pendingMC2Request(
                    new OneListSearchMC2Request(
                            m_systems.getSettingsIfc().getGeneralSettings().getInternalLanguage(),
                            this,
                            m_query,
                            m_currentRound,
                            m_sorting));
        }
    }
    
    /**
     * Method for the parser to deliver the parsed reply for a round.
     * 
     * @param estimatedTotalNbrOfMatches value of XML attribute
     * search_list.total_number_matches for the round executed.
     *
     * @param searchMatches the parsed OneListSearchMatchImpl:s. All elements
     * much have valid positions.
     * We assume that this is thread safe even when re-sorting is done in
     * another thread.
     * If there are no results, send an empty array.
     * The array must be in the order received from the server.
     */
    public void replyReceived(final int estimatedTotalNbrOfMatches,
                       final OneListSearchMatchImpl[] searchMatches) {
        
        if (searchMatches == null) {
            throw new IllegalArgumentException();
        }

        m_systems.getWorkScheduler().schedule(new Runnable() {
            public void run() {
                replyReceivedInternal(estimatedTotalNbrOfMatches,
                                      searchMatches);
            }
        });
    }

    /**
     * <p>Merges this reply with any previous reply, generate the
     * {@link OneListSearchReply} and call the listeners.</p>
     * 
     * <p>This method is not re-entrant safe. But we make sure that only
     * one mc2 request is outstanding at any time.</p>
     * 
     * @param estimatedTotalNbrOfMatches see {@link #replyReceived(int, OneListSearchMatchImpl[])}.
     * @param searchMatches see {@link #replyReceived(int, OneListSearchMatchImpl[])}.
     */
    synchronized void replyReceivedInternal(int estimatedTotalNbrOfMatches,
                          final OneListSearchMatchImpl[] searchMatches) {
        //method is not private to avoid penalties because of inner class access 
        final int nbrNewMatches = searchMatches.length; 
        if(LOG.isDebug()) {
            LOG.debug("OneListSearchRequest.replyReceivedInternal()",
                      "round: " + m_currentRound
                      + (m_lastReply != null ? (" fetched previously: "
                                                + m_lastReply.getNbrOfMatches())
                                             : "")
                      + " new: " + nbrNewMatches);
        }

        
        // parameter checks done by replyReceived()
        OneListSearchMatchImpl[] combinedMatches;
        
        // Only positional query supported so we only sort by distance.
        // Anyway, the server currently only sorts by distance, so if we 
        // need alfa_sort, we still need a complete sort
        // FIXME: once the server supports both sort types, this can be removed
        if (m_matchComp instanceof BasicSearchMatchImpl.MatchNameComparator) {
            WFUtil.insertionSort(searchMatches, m_matchComp);
        }
        
        if(m_lastReply != null) {
            // total matches is reported per round
            estimatedTotalNbrOfMatches +=
                m_lastReply.getEstimatedTotalNbrOfMatches();

            int n = m_lastReply.getNbrOfMatches() + nbrNewMatches;
            combinedMatches = new OneListSearchMatchImpl[n];
            combinedMatches = merge(m_lastReply.m_searchMatches, searchMatches, m_matchComp);
        } 
        else {
            combinedMatches = searchMatches; 
        }
        
        // as the matches array will be stored into a final member,
        // the references in the array will be up to date as well.
        m_lastReply = new OneListSearchReplyImpl(m_query,
                                                 estimatedTotalNbrOfMatches,
                                                 combinedMatches);
        final boolean lastReply = (m_currentRound == NBR_ROUNDS - 1);
        final boolean containsMatches = m_lastReply.containsMatches();
        
        if(lastReply || containsMatches) {
            m_callHandler.callInvokeCallbackRunnable(new Runnable() {
                public void run() {
                    if(lastReply) {
                        m_listener.searchDone(m_reqID, m_lastReply);
                    } else {
                        m_listener.searchUpdated(m_reqID, m_lastReply);
                    }
                }
            });
        }
        nextRound();
    }
    
    /**
     * Merge two sorted arrays of matches while keeping the resulting array 
     * sorted.
     * @param array1 the first sorted array
     * @param array2 the second sorted array
     * @param comp a {@link Comparator} to compare the elements
     * 
     * @return a sorted array formed by merging the two arrays
     */
    private OneListSearchMatchImpl[] merge(
            OneListSearchMatchImpl[] array1, 
            OneListSearchMatchImpl[] array2,
            Comparator comp) {
        
        int totalLength = array1.length + array2.length;
        OneListSearchMatchImpl[] result = new OneListSearchMatchImpl[totalLength];
        
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < array1.length && j < array2.length) {
            if (comp.compare(array1[i], array2[j]) <= 0) {
                result[k] = array1[i];
                i++;
            }
            else {
                result[k] = array2[j];
                j++;
            }
            k++;
        }
        if (i == array1.length && j < array2.length) {
            System.arraycopy(array2, j, result, k, array2.length - j);
        }
        else if (i < array1.length && j == array2.length) {
            System.arraycopy(array1, i, result, k, array1.length - i);
        }
        
        return result;
    }


    /**
     * Error reporting interface for parser.
     *
     * @param error the error that occurred.
     */
    public void requestFailed(final CoreError error) {
        m_callHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(m_reqID, error);
            }
        });
    }
}
