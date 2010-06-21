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

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.internal.topregion.TopRegionImpl;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;

/**
 * Represent a xml one_search request require a 
 * SearchQuery.SEARCH_TYPE_POSITIONAL type of query    
 * 
 * 
 */ 
final class OneListSearchMC2Request implements MC2Request {

    /**
     * 
     */
    private static final String SEARCH_ADDRESS = "address";

    private static final Logger LOG = 
            LogFactory.getLoggerForClass(OneListSearchMC2Request.class);

    //TODO: check if the version is correct
    public static final int SERVER_VERSION = 1;
    public static final String DISTANCE_SORT = "distance_sort";
    public static final String ALFA_SORT = "alfa_sort";
    
    private static final OneListSearchMatchImpl[] EMPY_MATCH_ARRAY = 
            new OneListSearchMatchImpl[0]; 

    private final RequestListener m_listener;
    private final SearchQuery m_query;
    private final int m_round;
    private final int m_maxNbrMatches;
    private final LanguageInternal m_language;
    private final String m_sorting;
    
    /**
     * Construct a mc2 request that can be posted on {@link MC2Interface}
     * @param listener {@link RequestListener} that get notified when the result 
     * succeed or fail, cannot be null.
     * @param query {@link SearchQuery} must be not null and of type 
     * {@link SearchQuery#SEARCH_TYPE_POSITIONAL}
     * @param round a positive number representing the search type
     * (e.g. 0 for internal search, 1 for external providers)  
     * @param sorting the sorting mode, must be one of {@link #ALFA_SORT}, 
     * {@link #DISTANCE_SORT} otherwise an IllegalArgumentException will be trown
     * @param langauge LanguageInternal for the server reply, cannot be null.   
     */
    OneListSearchMC2Request( LanguageInternal language,
                             RequestListener listener,
                             SearchQuery query, 
                             int round,
                             String sorting) {

        if (language  == null || listener == null || query == null) {
            throw new IllegalArgumentException("Language, listener and query param cannot be null");
        }
        //check early for problems to not have error when writing the request 
        //that will cause failure of all other merged requests 
        if (query.getQueryType() != SearchQuery.SEARCH_TYPE_POSITIONAL 
                && query.getQueryType() != SearchQuery.SEARCH_TYPE_ADDRESS) {
            throw new IllegalArgumentException("SearchQuery parameter must be of " +
            		"type SEARCH_TYPE_POSITIONAL or SEARCH_TYPE_ADDRESS");
        }
        
        if (query.getMaxNbrMatches() <= 0) {
            throw new IllegalArgumentException("MaxNbrMatches must be greater than 0");
        }
        
        if (sorting != ALFA_SORT && sorting != DISTANCE_SORT) {
            throw new IllegalArgumentException("Not an accepted sorting mode, " +
            		"must be either OneListSearchMC2Request.ALFA_SORT or " +
            		"OneListSearchMC2Request.DISTANCE_SORT");
        }

        // Don't check that round is valid - server will handle that and we
        // don't want checks to become out of sync.

        //needed to get the current language 
        m_language = language;
        m_listener = listener;
        m_query = query;
        m_round = round;
        //must be > 0
        m_maxNbrMatches = query.getMaxNbrMatches();
        m_sorting = sorting;
    }

    /* (non-Javadoc)
     * @see MC2Request#getRequestElementName() 
     */
    public final String getRequestElementName() {
        return MC2Strings.tone_search_request;
    }

    /* (non-Javadoc)
     * @see MC2Request#error(CoreError) 
     */
    public void error(CoreError coreError) {
        m_listener.requestFailed(coreError);
    }
    
    /* (non-Javadoc)
     * @see MC2Request#write(MC2Writer)
     */
    public final void write(MC2Writer mc2w) throws IOException {
        /*
         * xml 2.3.0
        <!ELEMENT one_search_request 
            ( search_match_query?, category_list?,
            ( position_item, distance? ) |
            ( query_location, top_region_id ) ) >
        <!ATTLIST one_search_request transaction_id ID #REQUIRED
            max_number_matches %number; #REQUIRED
            language %language_t; #REQUIRED
            round %number; #REQUIRED
            version %number; #REQUIRED
            include_info_fields %bool; #IMPLIED
            position_system %position_system_t; "MC2"
            sorting %sorting_t; #REQUIRED 
            search_type %search_for_type_t; "all">
        <!ENTITY % sorting_t "(alfa_sort|distance_sort)">
        <!ENTITY % search_for_type_t "(address|all)">
         */
        if(LOG.isDebug()) {
            LOG.debug("OneListSearchMC2Request.write()", "Writing compact search request");
        }
        //writer is positioned in inside one_search_request and the 
        //transaction_id has been written
        
        mc2w.attribute(MC2Strings.aversion, SERVER_VERSION);
        //language
        mc2w.attribute(MC2Strings.alanguage, m_language.getXMLCode());
        
        mc2w.attribute(MC2Strings.amax_number_matches, m_maxNbrMatches);
        mc2w.attribute(MC2Strings.around, m_round);
        mc2w.attribute(MC2Strings.asorting, m_sorting);
        mc2w.attribute(MC2Strings.ainclude_detail_fields, m_query.includeDetails());
        if (m_query.getQueryType() == SearchQuery.SEARCH_TYPE_ADDRESS) {
            mc2w.attribute(MC2Strings.asearch_type, SEARCH_ADDRESS);
        }
        
        String query = m_query.getItemQueryStr();
        if (query != null) {
            /*<!ELEMENT search_match_query ( #PCDATA )>*/
            mc2w.elementWithText(MC2Strings.tsearch_match_query, query);
        }
        Category category = m_query.getCategory();
        if (category != null) {
            //even there can only be a single category we still have to start a list 
            /*<!ELEMENT category_list ( category_id+ ) >
              <!ELEMENT category_id ( #PCDATA ) >
             */
            mc2w.startElement(MC2Strings.tcategory_list);
            mc2w.elementWithText(MC2Strings.tcategory_id, 
                    category.getCategoryID());
            mc2w.endElement(MC2Strings.tcategory_list);
        }
        
        if (m_query.getQueryType() == SearchQuery.SEARCH_TYPE_POSITIONAL) {
            /*<!ELEMENT position_item ( lat, lon, angle? )>
                <!ATTLIST position_item position_system %position_system_t; #REQUIRED>*/
            //position is a must
            m_query.getPosition().write(mc2w);

            int distance = m_query.getSearchRadius();
            if (distance != SearchQuery.RADIUS_SERVER_DEFAULT) {
                /*<!ELEMENT distance ( #PCDATA )>*/
                mc2w.elementWithText(MC2Strings.tdistance, distance);
            }
        }
        else if (m_query.getQueryType() == SearchQuery.SEARCH_TYPE_ADDRESS) {
            String where = (m_query.getSearchAreaStr() != null ? m_query.getSearchAreaStr() : MC2Strings.EMPTY_STRING);
            mc2w.elementWithText(MC2Strings.tquery_location, where);
            mc2w.elementWithText(MC2Strings.ttop_region_id, 
                    ((TopRegionImpl) m_query.getTopRegion()).getRegionID());
        }
    }

    /* (non-Javadoc)
     * @see MC2Request#parse(MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        mc2p.nameOrError(MC2Strings.tone_search_reply);
        /* 
         * xml 2.3.0
        <!ELEMENT one_search_reply ( search_list |
                ( status_code, status_message,
                status_uri? ) ) >
        <!ATTLIST one_search_reply transaction_id ID #REQUIRED >
        
        <!ELEMENT search_list ( search_match* )>
        <!ATTLIST search_list number_matches %number; #REQUIRED
                total_number_matches %number; #REQUIRED >
         */
        if(LOG.isDebug()) {
            LOG.debug("OneListSearchMC2Request.parse()", "Parsing compact search reply");
        }
        
        mc2p.childrenOrError();
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
        } else {
            mc2p.nameOrError(MC2Strings.tsearch_list);
            int nbr = mc2p.attributeAsInt(MC2Strings.anumber_matches);
            int estimatedTotal = mc2p.attributeAsInt(MC2Strings.atotal_number_matches);
            
            OneListSearchMatchImpl[] matches;
            if (nbr > 0) {
                if (nbr <= m_maxNbrMatches) {
                    mc2p.childrenOrError();
                    matches = new OneListSearchMatchImpl[nbr];
                    int n = 0;
                    do {
                        if (n < nbr) {
                            OneListSearchMatchImpl match = parse_search_match(mc2p);
                            if (match != null) {
                                matches[n++] = match;
                            } else {
                                if(LOG.isInfo()) {
                                    LOG.info("OneListSearchMC2Request.parse()", "match without position, ignored");
                                }
                            }
                        } else {
                            if(LOG.isWarn()) {
                                LOG.warn("OneListSearchMC2Request.parse()", "there are more results than requested, skip them");
                            }
                        }
                    } while (mc2p.advance());
                    
                    //cope with the matches that didn't have a position
                    //in an ideal world this should never happen 
                    if (n < nbr) {
                      //create a clean array if needed
                        OneListSearchMatchImpl[] tmp = matches;
                        matches = new OneListSearchMatchImpl[n];
                        System.arraycopy(tmp, 0, matches, 0, n);
                        
                        //adjust the total number as we have ignored some result
                        estimatedTotal -= nbr - n;
                    }
                } else {
                    throw new MC2ParserException("Nbr " + nbr +" of matches bigegr than max requested " +  m_maxNbrMatches);
                }
            } else {
                //if no result create an empty array
                matches = EMPY_MATCH_ARRAY;
            }
            //skip all other elements and go back to parent; 
            //it should be only one call of advance()
            mc2p.advance();
            m_listener.replyReceived(estimatedTotal, matches);
        }
        mc2p.nameOrError(MC2Strings.tone_search_reply);
    }
    
    
    // ------------------------------------------------------------------
    // helper methods for parser

    /**
     * <p>Assume that m_xpi.name == search_match.</p>
     * 
     * @return the parsed OneListSearchMatchImpl or null if the match didn't 
     * have a position 
     */
    static OneListSearchMatchImpl parse_search_match(MC2Parser mc2p)
        throws MC2ParserException, IOException {        

        /*
         * xml 2.3.0
        <!ELEMENT search_match ( name, itemid,
            location_name, lat?, lon?, category_list?,
            search_area*, detail_field* )>
        <!ATTLIST search_match search_match_type %search_match_type_t; #REQUIRED
            category_image CDATA #REQUIRED
            provider_image CDATA #REQUIRED
            brand_image CDATA #REQUIRED >
            
        <!ENTITY % search_match_type_t "(street|pointofinterest|misc|person|
            other)">
        */
        
        mc2p.nameOrError(MC2Strings.tsearch_match);
        
        String matchID = null;
        String matchLocation = null; 
        String matchName = null;

        // lat, lon are optional but we skip matches without position
        // in the future these will be filtered out by the server.
        Position position = null;

        // <!ATTLIST search_match search_match_type %search_match_type_t; #REQUIRED
        //           category_image CDATA #REQUIRED
        //           provider_image CDATA #REQUIRED
        //           brand_image CDATA #REQUIRED
        //           additional_info_exists %bool; #REQUIRED >
        
        // XML spec for search_match attributes
        //   Images are without file extension and are empty if there is no such
        //   image for the search_match.
        final String brandImageName = mc2p.attribute(MC2Strings.abrand_image);
        final String categoryImageName = mc2p.attribute(MC2Strings.acategory_image);
        final String providerImageName = mc2p.attribute(MC2Strings.aprovider_image);
        final boolean additionalInfoExists = mc2p.attributeAsBoolean(MC2Strings.aadditional_info_exists);
        
        /*
        <!ELEMENT search_match ( name, itemid,
                location_name, lat?, lon?, 
                category_list?,     //skipped
                search_area*,       //skipped
                detail_item? )>     //included if include_detail_fields is true
         */
        
        // there is at least 3 subelements 
        mc2p.childrenOrError();
        
        //create this only if we expect info fields 
        PoiDetailImpl detail = null; 
        
        do {
            if (mc2p.nameRefEq(MC2Strings.tname)) { // FIXME: MC2Strings
                matchName = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.titemid)) {
                matchID = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.tlat)) {
                int lat = mc2p.valueAsInt();
                if (mc2p.advance() && mc2p.nameRefEq(MC2Strings.tlon)) {
                    int lon = mc2p.valueAsInt();    
                    position = new Position(lat, lon);
                }
            } else if (mc2p.nameRefEq(MC2Strings.tlocation_name)) {
                matchLocation = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.tdetail_item)) {
                detail = mc2p.parseDetailItem();
            }
        } while (mc2p.advance());

        mc2p.nameOrError(MC2Strings.tsearch_match);
        
        if (position == null) {
            return null;
        } else if (matchID == null
                   || matchLocation == null
                   || matchName == null) {
            throw new MC2ParserException("Invalid data for search_match");
        } else {
            return new OneListSearchMatchImpl(matchID,
                    matchLocation,
                    matchName,
                    position,
                    brandImageName,
                    categoryImageName,
                    providerImageName,
                    detail, 
                    additionalInfoExists);
        }
    }
    // ------------------------------------------------------------------

    /**
     * Internal use only, communicate from {@link OneListSearchMC2Request} to 
     * {@link OneListSearchRequest}. This is a declared interface to aid
     * JUnit-testing.
     */
    static interface RequestListener {
        
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
        void replyReceived(int estimatedTotalNbrOfMatches,
                           final OneListSearchMatchImpl[] searchMatches);
        
        
        /**
         * Error reporting interface for parser.
         *
         * @param error the error that occurred.
         */
        void requestFailed(CoreError error);
        
    }
}
