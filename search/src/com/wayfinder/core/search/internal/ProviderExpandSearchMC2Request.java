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

final class ProviderExpandSearchMC2Request extends CompactSearchMC2Request {

    private static final Logger LOG = 
            LogFactory.getLoggerForClass(ProviderExpandSearchMC2Request.class);

    private final ProviderExpandSearchRequest m_request;
    private final ProviderSearchReplyImpl m_lastReply;
    private final int m_providerIndex;

    ProviderExpandSearchMC2Request(GeneralSettingsInternal settings,
                                   ProviderExpandSearchRequest request,
                                   ProviderSearchReplyImpl lastReply,
                                   Provider prov,
                                   int providerIndex, 
                                   int startIndex, 
                                   int endIndex) {
        
        super(settings, lastReply.getOriginalSearchQuery(), prov, startIndex, endIndex, prov.getRound());
        m_request = request;
        m_lastReply = lastReply;
        m_providerIndex = providerIndex;
    }
    
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        if(LOG.isDebug()) {
            LOG.debug("ProviderExpandSearchMC2Request.parse()", "Parsing compact search reply");
        }

        mc2p.children();
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        } 
        
        MatchListImpl newMatchList = null;
        do {
            //            <!ELEMENT search_hit_list ( search_item* | search_area* )>
            //              <!ATTLIST search_hit_list numberitems %number; #REQUIRED
            //                total_numberitems %number; #REQUIRED
            //                starting_index %number; #REQUIRED
            //                ending_index %number; #REQUIRED
            //                heading %number; #REQUIRED >
            if (mc2p.nameRefEq(MC2Strings.tsearch_hit_list)) {
                
                // found a list of search hits in one heading
                final int nbrOfItems = mc2p.attributeAsInt(MC2Strings.anumberitems); 
                if(LOG.isTrace()) {
                    LOG.trace("ProviderSearchMC2Request.parse()", 
                            "Found search_item_list, elements: " + nbrOfItems);
                }

                // total Nbr of hits on the server, not all are always returned
                final int totalNbrSearchItems = mc2p.attributeAsInt(MC2Strings.atotal_numberitems);

                // heading identifies the search provider
                final Provider provider = (Provider) m_lastReply.getProviderOfList(m_providerIndex);
                if(mc2p.attributeAsInt(MC2Strings.aheading) != provider.getHeadingID()) {
                    if(LOG.isError()) {
                        LOG.error("ProviderExpandSearchMC2Request.parse()", 
                                "Got response for wrong heading");
                    }
                    //FIXME not sure what to do here really
                    continue;
                }
                
                
                
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
                if (totalNbrSearchItems == 0) {
                    if(LOG.isTrace()) {
                        LOG.trace("ProviderSearchMC2Request.parse()", 
                                "Heading " + provider.getProviderName() + " has zero results");
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
                
                
                // we are all done here
                // merge the lists and replace the old reply
                
                MatchListImpl oldList = (MatchListImpl) m_lastReply.getMatchList(m_providerIndex);
                newMatchList = new MatchListImpl(oldList, topMatchArray, matchArray, areaArray, totalNbrSearchItems);
            } 
        } while (mc2p.advance());
        
        
        ProviderSearchReplyImpl reply;
        if(newMatchList == null) {
            // no reply found :/
            reply = m_lastReply;
        } else {
            reply = new ProviderSearchReplyImpl(m_lastReply, m_providerIndex, newMatchList);
        }
        m_request.replyReceived(reply);
    }
    
    
    public void error(CoreError coreError) {
        m_request.requestFailed(coreError);
    }
}
