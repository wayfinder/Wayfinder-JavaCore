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
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.internal.topregion.TopRegionImpl;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.util.CharArray;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;

/**
 * Implements writing of XML request <code>compact_search_request</code> and
 * parsing of common entities of the corresponding reply. 
 */
abstract class CompactSearchMC2Request implements MC2Request {

    private static final int SEARCH_REQUEST_VERSION = 1;

    private static final Logger LOG = 
            LogFactory.getLoggerForClass(CompactSearchMC2Request.class);

    private final GeneralSettingsInternal m_settings;
    private final SearchQuery m_query;
    private final Provider m_provider;
    private final int m_startIndex;
    private final int m_endIndex;
    private final int m_round;


    /**
     * Constructor to be called by concrete implementations.
     * 
     * @param settings must not be null.
     * @param query the search query. Must not be null.
     * @param heading can be used to search only a specific provider. If null,
     * all providers in the round will be queried.
     * @param startIndex index of first match to get.
     * @param endIndex index of last match to get.
     * @param round what round (internal, external, ...). See the XML
     * specification for details on allowed values.
     */
    CompactSearchMC2Request( GeneralSettingsInternal settings,
                             SearchQuery query, 
                             Provider heading, 
                             int startIndex, 
                             int endIndex, 
                             int round) {
        
        m_settings = settings;
        m_query = query;
        m_provider = heading;
        m_startIndex = startIndex;
        m_endIndex = endIndex;
        m_round = round;
    }


    public final String getRequestElementName() {
        return MC2Strings.tcompact_search_request;
    }


    public final void write(MC2Writer mc2w) throws IOException {
        if(LOG.isDebug()) {
            LOG.debug("ProviderSearchMC2Request.write()", "Writing compact search request");
        }

        mc2w.attribute(MC2Strings.astart_index, m_startIndex);
        mc2w.attribute(MC2Strings.aend_index, m_endIndex);
        mc2w.attribute(MC2Strings.amax_hits, 20);

        mc2w.attribute(MC2Strings.alanguage, m_settings.getInternalLanguage().getXMLCode());
        // round 
        // round 0 = only search in internal Wayfinder databases - fast
        // round 1 = only search in external databases like Eniro - slow
        mc2w.attribute(MC2Strings.around, m_round);

        if(m_provider != null) {
            // aHeading is the wanted heading, also known as what service
            // we want to search in. -1 means all headings and is assumed
            // by the server if omitted.
            mc2w.attribute(MC2Strings.aheading, m_provider.getHeadingID());
        }
        mc2w.attribute(MC2Strings.aversion, SEARCH_REQUEST_VERSION);

        String what = m_query.getItemQueryStr(); 
        mc2w.startElement(MC2Strings.tsearch_item_query);
        mc2w.text(what != null ? what: "");
        mc2w.endElement(MC2Strings.tsearch_item_query);

        // attach category if exists
        Category cat = m_query.getCategory();
        if(cat != null) {
            /*
             * We no longer have custom categories or categories where we
             * search by an internal name.
             * 
             * We always use the category ID determined by the server and
             * the name is just for user display.
             * 
             * All category IDs are valid if they are sent from the server.
             * 
             * Guarding from categories that are not created by us is done
             * by SearchQuery.
             */
            int categoryID = cat.getCategoryID();
            mc2w.startElement(MC2Strings.tcategory_id);
            mc2w.text(categoryID);
            mc2w.endElement(MC2Strings.tcategory_id);
        }

        switch(m_query.getQueryType()) {
        case SearchQuery.SEARCH_TYPE_AREA:
            SearchAreaImpl area = (SearchAreaImpl) m_query.getSearchArea();
            mc2w.startElement(MC2Strings.tsearch_area);
            mc2w.attribute(MC2Strings.asearch_area_type, 
                    MC2Strings.enum_type_str(
                            MC2Strings.TYPE_SearchArea, 
                            area.getAreaType()));
            mc2w.elementWithText(MC2Strings.tname, area.getAreaName());
            mc2w.elementWithText(MC2Strings.tareaid, area.getAreaID());
            mc2w.endElement(MC2Strings.tsearch_area);
            break;

        case SearchQuery.SEARCH_TYPE_REGIONAL:
            // standard search by top region
            String where = m_query.getSearchAreaStr();
            mc2w.startElement(MC2Strings.tsearch_area_query);
            mc2w.text( ( where != null ) ? where : "");
            mc2w.endElement(MC2Strings.tsearch_area_query);

            TopRegionImpl tr = (TopRegionImpl) m_query.getTopRegion();
            mc2w.startElement(MC2Strings.ttop_region_id);
            mc2w.text(tr.getRegionID());
            mc2w.endElement(MC2Strings.ttop_region_id);
            break;

        case SearchQuery.SEARCH_TYPE_POSITIONAL:
            m_query.getPosition().write(mc2w);
            int radius = m_query.getSearchRadius();
            if(radius > 0) {
                mc2w.startElement(MC2Strings.tdistance);
                mc2w.text(radius);
                mc2w.endElement(MC2Strings.tdistance);
            }
            break;

        default:
            if(LOG.isError()) {
                LOG.error("ProviderSearchMC2Request.write()", 
                        "Got a SearchQuery with an unknown query type: "
                        + m_query.getQueryType());
            }
        }
    }


    /**
     * <p>Utility method to parse a XML entity <code>search_item</code> to a
     * {@link SearchMatchImpl}.</p>
     * 
     * <p>The parameters must not be null.</p>
     *
     * @param mc2Parser the parser to use. Must be positioned at the element
     * <code>search_item</code> to parse.
     * @param provider the provider in whose hit list this search_item was
     * contained.
     * @return a new SearchMatchImpl.
     * @throws MC2ParserException if the XML isn't what we expect.
     * @throws IOException if an I/O-error occurs.
     */
    protected final static SearchMatchImpl
    parse_search_item(MC2Parser mc2p, Provider provider) 
    throws MC2ParserException, IOException {

        // first the attributes
        
        // <!ATTLIST search_item search_item_type %search_item_type_t; #REQUIRED
        //                       image CDATA #IMPLIED
        //                       advert %bool; #IMPLIED >
        
        //CharArray c = xpi.attributeCharArray(MC2Strings.asearch_item_type);
        //final int search_item_type = MC2Strings.enum_type( MC2Strings.TYPE_SearchItem, c);
        final String itemImageName = mc2p.attribute(MC2Strings.aimage);
        
        // now the elements
        mc2p.children();
        final String itemName = mc2p.value();
        mc2p.advance();
        final String itemID = mc2p.value();
        
        String locationName = null;
        int lat = 0;
        int lon = 0;

        while (mc2p.advance())  {
            if (mc2p.nameRefEq(MC2Strings.tlocation_name)) {
                locationName = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.tlat)) {
                lat = mc2p.valueAsInt();
            } else if (mc2p.nameRefEq(MC2Strings.tlon)) {
                lon = mc2p.valueAsInt();
            }
        }

        // assume that the server has sent all necessary data
        // FIXME: coordinates not always sent! This will especially be the case
        // with external searches. This must be handled!
        Position position = new Position(lat, lon);
        
        return new SearchMatchImpl(itemID, itemName, itemImageName, locationName, position, provider);
    } // parse_search_item


    /**
     * <p>Utility method to parse a XML entity <code>search_area</code> to a
     * {@link SearchAreaImpl}.</p>
     * 
     * <p>The parameters must not be null.</p>
     *
     * @param mc2Parser the parser to use. Must be positioned at the element
     * <code>search_area</code> to parse.
     * @param provider the provider in whose hit list this search_area was
     * contained.
     * @return a new SearchAreaImpl.
     * @throws MC2ParserException if the XML isn't what we expect.
     * @throws IOException if an I/O-error occurs.
     */
    protected static final SearchAreaImpl
    parse_search_area(MC2Parser mc2p, Provider provider) 
    throws MC2ParserException, IOException {
  
        // <!ELEMENT search_area ( name, areaid, location_name?,
        //        lat?, lon?, boundingbox?, top_region_id?,
        //        search_area* )>

        // <!ATTLIST search_area search_area_type %search_area_type_t; #REQUIRED>

        //TODO: using a temporary varible reduce the jar size with 4bytes
        CharArray c = mc2p.attributeCharArray(MC2Strings.asearch_area_type);
        int search_area_type = MC2Strings.enum_type(MC2Strings.TYPE_SearchArea, c);

        mc2p.children();
        final String areaName = mc2p.value();
        mc2p.advance();
        final String areaID = mc2p.value();
        String locationName = null;

        if (mc2p.advance()) {
            if (mc2p.nameRefEq(MC2Strings.tlocation_name)) {
                locationName = mc2p.value();
            }
            while (mc2p.advance());// while not end of element search_area
        }
        // assume that the server has sent all necessary data
        return new SearchAreaImpl(areaID, search_area_type, areaName, locationName, provider);
    } // parse_search_area
}
