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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.geocoding.internal;

import java.io.IOException;
import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.geocoding.GeocodeInterface;
import com.wayfinder.core.geocoding.GeocodeListener;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.geocoding.AddressInfo;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.xml.XmlIterator;
import com.wayfinder.core.shared.xml.XmlWriter;

/**
 * <p>Implements sending of XML API expand_request and parsing of
 * expand_reply.</p>
 * 
 * <p>This object is not reusable for parsing several requests.</p>
 */
final class ExpandMC2Request implements MC2Request {

    private static final Logger LOG = LogFactory
        .getLoggerForClass(ExpandMC2Request.class);

    private final RequestID m_requestID;
    private final Position m_position;
    private final CallbackHandler m_callbackHandler;
    private final LanguageInternal m_language;
    private final GeocodeListener m_listener;


    /**
     * Creates a new ExpandMC2Request.
     * 
     * @param requestID - the requestID returned to UI by
     *        {@link GeocodeInterface#reverseGeocode(Position, GeocodeListener)}.
     * @param position - the {@link Position} to expand to an address.
     * @param listener - the listener that receives the result or an error.
     */
    ExpandMC2Request(RequestID requestID,
                  Position position, 
                  CallbackHandler callbackHandler,
                  GeneralSettingsInternal settings,
                  GeocodeListener listener) {

        m_requestID = requestID;
        m_position = position;
        m_callbackHandler = callbackHandler;
        m_language = settings.getInternalLanguage();
        m_listener = listener;
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#getRequestElementName()
     */
    public String getRequestElementName() {
        return MC2Strings.texpand_request;
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        
        mc2w.startElement(MC2Strings.texpand_request_header);
        mc2w.attribute(MC2Strings.ainclude_top_region_id, true);
        mc2w.startElement(MC2Strings.tsearch_preferences);
        mc2w.startElement(MC2Strings.tsearch_settings);
        
        mc2w.startElement(MC2Strings.tshow_search_item_municipal);
        mc2w.endElement(MC2Strings.tshow_search_item_municipal);
        
        mc2w.startElement(MC2Strings.tshow_search_item_city);
        mc2w.endElement(MC2Strings.tshow_search_item_city);
        
        mc2w.startElement(MC2Strings.tshow_search_item_city_part);
        mc2w.endElement(MC2Strings.tshow_search_item_city_part);
        
        mc2w.startElement(MC2Strings.tlanguage);
        mc2w.text(m_language.getXMLCode());
        mc2w.endElement(MC2Strings.tlanguage);
        
        mc2w.endElement(MC2Strings.tsearch_settings);
        mc2w.endElement(MC2Strings.tsearch_preferences); 
        mc2w.endElement(MC2Strings.texpand_request_header); 

        mc2w.startElement(MC2Strings.texpand_request_query);
        m_position.write(mc2w);
        mc2w.endElement(MC2Strings.texpand_request_query);
    }


    private static final String EMPTY = "";

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        final String FNAME = "ExpandMC2Request.parse()";

        if(LOG.isDebug()) {
            LOG.debug(FNAME, "from " + mc2p);
        }


        /* <!ELEMENT expand_reply ( // position when parse() is called
         *                         (
         *                              search_item_list | search_area_list |
         *                              companydata
         *                          )+
         *                          |
         *                          (status_code, status_message,
         *                           status_code_extended? )
         *  )>
         */

        mc2p.children();

        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        } 

        String streetName = EMPTY;
        String locationName = EMPTY;

        // only created if we're on new server that sends structured
        // search areas
        AddressInfoImpl addressInfo = null;

        find_and_parse_search_item:
        do {
            if (mc2p.nameRefEq(MC2Strings.tsearch_item_list)) {
                if (mc2p.children()) {
                    // then at least one search item
                    do {
                        if (mc2p.nameRefEq(MC2Strings.tsearch_item)) {
                            /* <!ELEMENT search_item ( name,
                             *                         itemid,
                             *                         streetnbr?,
                             *                         explicit_itemid?,
                             *                         location_name?,
                             *                         lat?,
                             *                         lon?,
                             *                         boundingbox?,
                             *                         search_area* )>
                             */
                            mc2p.children();
                            streetName = mc2p.value();
                            mc2p.advance();

                            do {
                                if (mc2p.nameRefEq(MC2Strings.tlocation_name)) {
                                    locationName = mc2p.value();
                                } else if (mc2p.nameRefEq(MC2Strings.tsearch_area)) {
                                    addressInfo = new AddressInfoImpl();
                                    parseSearchAreas(mc2p, addressInfo); // recursive
                                    break find_and_parse_search_item;        
                                }
                            } while (mc2p.advance());
                        }
                    } while (mc2p.advance());
                }
            }
        } while (mc2p.advance());

        // analyze collected data
        if(LOG.isDebug()) {
            LOG.debug(FNAME, "streetName=" + streetName
                      + " locationName=" + locationName);
        }

        if (addressInfo == null) {
            // old server. Is also for safety - split the location name
            if(LOG.isDebug()) {
                LOG.debug(FNAME, "no structured adress information sent. Parsing location_name instead");
            }

            addressInfo = new AddressInfoImpl();
            int start = 0;
            int elementNbr = 0;
            final String separator = ", ";
            while (start < locationName.length() && elementNbr <= 3) {
                int k = locationName.indexOf(separator, start);
                if (k == -1) {
                    k = locationName.length();
                }

                String s = locationName.substring(start, k);
                start = k + separator.length();
                if (elementNbr == 3) {
                    addressInfo.setCityPart(s);
                }
                if (elementNbr == 2) {
                    addressInfo.setCity(s);
                }
                if (elementNbr == 1) {
                    addressInfo.setMunicipal(s);
                }
                if (elementNbr == 0) {
                    addressInfo.setTopRegion(s, EMPTY);
                }

                elementNbr++;
            }

            if (elementNbr == 0) {
                error(new CoreError(FNAME
                        + " no structured address info and unparsable location_name '"
                        + locationName
                        + "' pos: "
                        + m_position.toString()));
            }
        } // parsing of location_name as a fall back

        addressInfo.setStreet(streetName);        

        // send the result
        final AddressInfo addressInfo2 = addressInfo;
        m_callbackHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.reverseGeocodeDone(m_requestID, addressInfo2);
            }
        });
    }

    /**
     * <p>Parse the hierarchical search area structure.</p>
     * 
     *  <p>Assumes that xpi is positioned at the start tag for the search_area
     *  element.</p>
     *  
     *  <p>See also CompactSearchMC2Request#parse_search_area(MC2Parser, XmlIterator, Provider)
     *  (not linked - is protected).</p>
     *  
     * @param xpi - the iterator used for parsing.
     * @param address - the address to fill with data.
     */
    private void parseSearchAreas(XmlIterator xpi,
                                  AddressInfoImpl address)
        throws IOException {

        /* <!ELEMENT search_area ( name,
         *                         areaid,
         *                         location_name?,
         *                         lat?, lon?, boundingbox?,
         *                         top_region_id?,
         *                         search_area* )>
         *                         
         * <!ATTLIST search_area search_area_type %search_area_type_t; #REQUIRED>
         */        

        String type = xpi.attribute(MC2Strings.asearch_area_type); // can be null
        xpi.children(); // empty search_area not allowed
        String name = EMPTY;
        String topRegionID = EMPTY;
        do {
            if (xpi.nameRefEq(MC2Strings.tname)) {
                name = xpi.value();
            } else if (xpi.nameRefEq(MC2Strings.ttop_region_id)) {
                topRegionID = xpi.value();
            } else if (xpi.nameRefEq(MC2Strings.tsearch_area)) {
                parseSearchAreas(xpi, address);
            }
        } while (xpi.advance());
        
        // note: will print from deepest level, e.g. country first.
        if(LOG.isDebug()) {
            LOG.debug("ExpandMC2Request.parseSearchAreas()",
                      "name=" + name + " type=" + type);
        }

        // readability suffers if we use MC2Strings.enumType() since
        // we don't have good constants for search_area types.
        if ("city".equals(type)) {
            address.setCity(name);
        } else if ("citypart".equals(type)) {
            address.setCityPart(name);
        } else if ("country".equals(type)) {
            // the server will not send a country without a top region ID
            address.setTopRegion(name, topRegionID);
        } else if ("municipal".equals(type)) {
            address.setMunicipal(name);
        }
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(final CoreError coreError) {
        if(LOG.isError()) {
            LOG.error("ExpandMC2Request.error()", coreError.getInternalMsg());
        }

        m_callbackHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.error(m_requestID, coreError);
            }
        });
    }        
}
