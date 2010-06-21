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

import java.io.IOException;

import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.util.CharArray;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.core.shared.xml.XmlIterator;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Strings;

final class ProviderSearchMC2Request extends CompactSearchMC2Request {


    private static final Logger LOG = 
            LogFactory.getLoggerForClass(ProviderSearchMC2Request.class);

    private final ProviderSearchRequest m_request;
    private final SearchQuery m_query;
    private final SearchDescriptor m_descriptor;
    private final ProviderSearchReplyImpl m_lastReply;

    ProviderSearchMC2Request(GeneralSettingsInternal settings,
                             ProviderSearchRequest request,
                             SearchDescriptor descriptor, 
                             SearchQuery query, 
                             ProviderSearchReplyImpl lastReply,
                             int startIndex, 
                             int endIndex, 
                             int round) {
        
        super(settings, query, null, startIndex, endIndex, round);
        
        m_request = request;
        m_descriptor = descriptor;
        m_query = query;
        m_lastReply = lastReply;
    }
    
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        if(LOG.isDebug()) {
            LOG.debug("ProviderSearchMC2Request.parse()", "Parsing compact search reply");
        }

        mc2p.children();
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        } 
        
        String globalAdResultsText = null;
        String globalAllResultsText = null;

        LinkedList headingList = new LinkedList();
        LinkedList providerList = new LinkedList();
        
        do {
            //            <!ELEMENT search_hit_list ( search_item* | search_area* )>
            //              <!ATTLIST search_hit_list numberitems %number; #REQUIRED
            //                total_numberitems %number; #REQUIRED
            //                starting_index %number; #REQUIRED
            //                ending_index %number; #REQUIRED
            //                heading %number; #REQUIRED >
            if (mc2p.nameRefEq(MC2Strings.tad_results_text)) {
                // global ad result text
                globalAdResultsText = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.tall_results_text)) {
                // global all results text
                globalAllResultsText = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.tsearch_hit_list)) {
                
                // found a list of search hits in one heading
                final int nbrOfItems = mc2p.attributeAsInt(MC2Strings.anumberitems); 
                if(LOG.isTrace()) {
                    LOG.trace("ProviderSearchMC2Request.parse()", 
                            "Found search_item_list, elements: " + nbrOfItems);
                }

                // total Nbr of hits on the server, not all are always returned
                final int totalNbrSearchItems = mc2p.attributeAsInt(MC2Strings.atotal_numberitems);

                // heading identifies the search provider
                final int heading = mc2p.attributeAsInt(MC2Strings.aheading);
                final Provider provider = m_descriptor.getProviderWithHeading(heading);
                if(provider == null) {
                    // unknown provider - ignore
                    continue;
                }
                providerList.add(provider);

                final int nbrOfTopHits;
                CharArray caTH = mc2p.attributeCharArray(MC2Strings.atop_hits);
                if (caTH != null) {
                    nbrOfTopHits = caTH.intValue();
                } else {
                    // no top hits available
                    nbrOfTopHits = 0;
                }

                // now for the actual results
                // unfortunately the areas and the matches does not have
                // separate counts so we cannot predefine the areas and populate
                // them directly
                final SearchMatchImpl[] topMatchArray;
                final SearchMatchImpl[] matchArray;
                final SearchAreaImpl[] areaArray;
                String adResultTxt = null;
                String allResultTxt = null;
                if (nbrOfItems == 0) {
                    if(LOG.isTrace()) {
                        LOG.trace("ProviderSearchMC2Request.parse()", 
                                "Heading " + heading + " has zero results");
                    }

                    topMatchArray = new SearchMatchImpl[0];
                    matchArray = new SearchMatchImpl[0];
                    areaArray = new SearchAreaImpl[0];
                } else {
                    if(LOG.isTrace()) {
                        LOG.trace("ProviderSearchMC2Request.parse()", 
                                "Parsing results for heading " + provider.getProviderName());
                    }

                    if (mc2p.children()) {
                        LinkedList topList = new LinkedList();
                        LinkedList regularList = new LinkedList();
                        LinkedList areaList = new LinkedList();
                        int nbrOfParsedItems = 0;

                        do {
                            if (mc2p.nameRefEq(MC2Strings.tsearch_item)) {
                                nbrOfParsedItems++;
                                LinkedList listToAdd;
                                if(nbrOfParsedItems > nbrOfTopHits) {
                                    listToAdd = regularList;
                                } else {
                                    listToAdd = topList;
                                }
                                listToAdd.add(parse_search_item(mc2p, provider));
                            } 
                            else if (mc2p.nameRefEq(MC2Strings.tsearch_area)) {
                                areaList.add(parse_search_area(mc2p, provider));
                            }
                            else if (mc2p.nameRefEq(MC2Strings.tad_results_text)) {
                                adResultTxt = mc2p.value();
                            }
                            else if (mc2p.nameRefEq(MC2Strings.tall_results_text)) {
                                allResultTxt = mc2p.value();
                            }
                        } while (mc2p.advance());

                        topMatchArray = new SearchMatchImpl[topList.size()];
                        topList.toArray(topMatchArray);

                        matchArray = new SearchMatchImpl[regularList.size()];
                        regularList.toArray(matchArray);

                        areaArray = new SearchAreaImpl[areaList.size()];
                        areaList.toArray(areaArray);

                    } else {
                        topMatchArray = new SearchMatchImpl[0];
                        matchArray = new SearchMatchImpl[0];
                        areaArray = new SearchAreaImpl[0];
                    }
                }

                MatchListImpl headingObj = new MatchListImpl(
                        topMatchArray,
                        matchArray,
                        areaArray,
                        adResultTxt,
                        allResultTxt,
                        totalNbrSearchItems);
                headingList.add(headingObj);

            } 
        } while (mc2p.advance());
        
        MatchListImpl[] headingArray = new MatchListImpl[headingList.size()];
        headingList.toArray(headingArray);
        
        Provider[] providerArray = new Provider[providerList.size()];
        providerList.toArray(providerArray);
        
        ProviderSearchReplyImpl reply;
        if(m_lastReply != null) {
            reply = new ProviderSearchReplyImpl(m_lastReply, headingArray, providerArray);
        } else {
            reply = new ProviderSearchReplyImpl(m_query, headingArray, providerArray);
        }
        m_request.replyReceived(reply);
    }
    
    
    public void error(CoreError coreError) {
        m_request.fail(coreError);
    }

    
}
