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
/*
 * Copyright, Wayfinder Systems AB, 2010
 */

package com.wayfinder.core.search.provider;

import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.ResponseListener;
import com.wayfinder.core.shared.error.CoreError;

import com.wayfinder.core.search.SearchListener;
import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchQuery;


/**
 * This interface extends the {@link SearchListener} and is intended for use
 * when doing searches through the {@link ProviderSearch}.
 * <p>
 * This interface should be implemented by the application using the Core
 * and should be passed along with the {@link SearchQuery} object to the
 * {@link ProviderSearch#search(SearchQuery, ProviderSearchListener)} method.
 * <p>
 * The implementor must be prepared to handle a series of calls to
 * methods in this interface followed by a call to
 * {@link ResponseListener#error(RequestID, CoreError)} and
 * thus the search  will only be partially complete. This can happen because
 * Core/Server may internally divide the search into several phases which
 * can fail individually.
 * <p>
 * Implementors must not rely on the call to
 * <code>error(...)</code> being the last call in the chain, although Core
 * will do a reasonable effort to not waste more resources on failed searches.
 * Implementors who don't want further updates after an error has occurred
 * should delete their reference to the <code>RequestID</code> and use
 * RequestID equality to filter out old results.
 * <p>
 * Implementors must also be prepared to handle that
 * <code>searchDone(...)</code> may be called after a call to
 * <code>error(...)</code>, even if this will not be common. 
 */
public interface ProviderSearchListener extends SearchListener {

    /**
     * Called when the core has determined which external providers the
     * search will take place in.
     * 
     * @param reqID A {@link RequestID} to identify the response
     * @param providers An array with {@link SearchProvider} objects. The
     * receiver is free to use the array as it wishes.
     */
    public void usingExternalProviders(RequestID reqID,
                                       SearchProvider[] providers);

    /**
     * <p>This method will be called when the search query requested by calling 
     * {@link ProviderSearch#search(SearchQuery, ProviderSearchListener)} has been
     * partially completed and the client has received some responses from the 
     * server. This method will not be called until matches are actually found,
     * but can be called multiple times with updated lists.</p>
     *
     * <p>The {@link ProviderSearchReply} object passed into the method will <u>only</u>
     * contain the headings that the server has sent a response for so far.</p>
     * 
     * <p>Once the search is completely done, 
     * {@link #searchDone(RequestID, ProviderSearchReply)} will 
     * be called to close the transaction instead of
     * <code>searchUpdated()</code>.</p>
     * 
     * @param reqID {@link RequestID} object that uniquely identifies the response.
     * Is intended to be matched against the object returned when calling
     * {@link ProviderSearch#search(SearchQuery, ProviderSearchListener)} 
     * @param reply The {@link ProviderSearchReply} containing the list of
     * <i>all search matches received so far</i> for the executed search query.
     * This includes matches delivered with previous calls to
     * <code>searchUpdated(...)</code> as a result of the ongoing call to
     * <code>search(...)</code>
     */
    public void searchUpdated(RequestID reqID, ProviderSearchReply reply);
    
    
    /**
     * This method will be called when the search query requested by calling 
     * {@link ProviderSearch#search(SearchQuery, ProviderSearchListener)} has been
     * completed and the client has received all responses from the server.
     * <p>
     * The {@link ProviderSearchReply} object passed into this method will <u>not</u>
     * be the same object as any object previously passed into the
     * {@link #searchUpdated(RequestID, ProviderSearchReply)} method.
     * <p>
     * Once this method has been called, no more calls for this request ID
     * will be done.
     * 
     * @param reqID Request ID object that uniquely identifies the response.
     * Is intended to be matched against the object returned when calling
     * {@link ProviderSearch#search(SearchQuery, ProviderSearchListener)} 
     * @param reply The {@link ProviderSearchReply} containing the complete response
     * from the server
     */
    public void searchDone(RequestID reqID, ProviderSearchReply reply);
}
