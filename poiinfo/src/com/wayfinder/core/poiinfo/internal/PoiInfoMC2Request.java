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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.poiinfo.internal;

import java.io.IOException;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.poiinfo.PoiInfoListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldImpl;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldListImpl;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.poiinfo.PoiInfo;
import com.wayfinder.core.shared.util.ArrayList;
import com.wayfinder.core.shared.util.CharArray;

/**
 * 
 *
 */
public class PoiInfoMC2Request implements MC2Request {
    
    private static final Logger LOG = 
        LogFactory.getLoggerForClass(PoiInfoMC2Request.class);
    
    private final RequestID m_reqID;
    private final String m_itemID;
    private final LanguageInternal m_lang;
    private final CallbackHandler m_cbHandler;
    private final PoiInfoListener m_listener;
    
    /**
     * @param reqID the request ID returned by 
     * {@link PoiInfoModule#requestInfo(String, PoiInfoListener)}
     * @param itemID the ID string of the POI
     * @param settings
     * @param cbHandler
     * @param listener the listener that handles the result or an error
     */
    public PoiInfoMC2Request(RequestID reqID, String itemID, 
            GeneralSettingsInternal settings, CallbackHandler cbHandler, 
            PoiInfoListener listener) {
        m_reqID = reqID;
        m_itemID = itemID;
        m_lang = settings.getInternalLanguage();
        m_cbHandler = cbHandler;
        m_listener = listener;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(final CoreError coreError) {
        m_cbHandler.callInvokeCallbackRunnable(new Runnable() {
            
            public void run() {
                m_listener.error(m_reqID, coreError);
            }
        });
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#getRequestElementName()
     */
    public String getRequestElementName() {
        return MC2Strings.tpoi_info_request;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        /*
        <!ELEMENT poi_info_reply ( info_item* | ( status_code, status_message,
                                                    status_code_extended? ) ) >
            <!ATTLIST poi_info_reply transaction_id ID #REQUIRED>
            <!ELEMENT info_item ( typeName, itemName, lat?, lon?, category_list?,
                                    info_field*, search_item? )>
                <!ATTLIST info_item numberfields %number; #REQUIRED
                            heading %number; #IMPLIED >
                <!ELEMENT typeName ( #PCDATA )>
                <!ELEMENT itemName ( #PCDATA )>
                <!ELEMENT info_field ( fieldName, fieldValue ) >
                    <!ATTLIST info_field info_type %poi_info_t; #IMPLIED >
                    <!ELEMENT fieldName ( #PCDATA )>
                    <!ELEMENT fieldValue ( #PCDATA )>
         */
        
        final PoiInfo[] infoItemArray;
        if (mc2p.children()) {
        
            ServerError status = mc2p.getErrorIfExists();
            if (status != null) {
                error(status);
                return;
            }
        
            ArrayList infoItems = new ArrayList();
            do {
                if (mc2p.nameRefEq(MC2Strings.tinfo_item)) {
                    int nbrFields = mc2p.attributeAsInt(MC2Strings.anumberfields);
                    int heading = -1;
                    CharArray caHeading = mc2p.attributeCharArray(MC2Strings.aheading);
                    if (caHeading != null) {
                        heading = caHeading.intValue();
                    }
                    
                    mc2p.children();
                    
                    // must be typeName:
                    String typeName = mc2p.value();
                    mc2p.advance();
                    
                    //must be itemName:
                    String itemName = mc2p.value();
                    
                    int lat = 0, lon = 0;
                    InfoFieldImpl[] infoFields = null;
                    if (nbrFields > 0) {
                        infoFields = new InfoFieldImpl[nbrFields];
                    }
                    
                    int crtField = 0;
                    while (mc2p.advance()) {
                        if (mc2p.nameRefEq(MC2Strings.tinfo_field)) {
                            if (crtField < nbrFields) {
                                infoFields[crtField] = mc2p.parseInfoField(); 
                                crtField++;
                            } else {
                                if(LOG.isWarn()) {
                                    LOG.warn("PoiInfoMC2Request.parse()", 
                                            "more info fields than expected skip " + nbrFields);
                                }
                            }
                        }
                        else if (mc2p.nameRefEq(MC2Strings.tlat)) {
                            lat = mc2p.valueAsInt();
                        }
                        else if (mc2p.nameRefEq(MC2Strings.tlon)) {
                            lon = mc2p.valueAsInt();
                        }
                    }
                    
                    Position pos = new Position(lat, lon);
                    
                    final PoiInfo poiInfo = new PoiInfo(typeName, itemName, heading, 
                            pos, new InfoFieldListImpl(infoFields));
                    infoItems.add(poiInfo);
                }
            } while (mc2p.advance());
            infoItemArray = new PoiInfo[infoItems.size()];
            infoItems.copyInto(infoItemArray);
        } else {
            infoItemArray = new PoiInfo[0];
        }
        
        m_cbHandler.callInvokeCallbackRunnable(new Runnable() {
            public void run() {
                m_listener.requestInfoDone(m_reqID, infoItemArray);
            }
        });
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        /*
        <!ELEMENT poi_info_request ( search_item, language ) >
        <!ATTLIST poi_info_request transaction_id ID #REQUIRED
                    position_system %position_system_t; "MC2"
                    include_category_id %bool; "false"
                    include_full_search_item %bool; "false"
                    use_persistent_ids %bool; "false" >
        */
        
        mc2w.attribute(MC2Strings.aposition_system, MC2Strings.MC2);
        //xmlWriter.attribute(MC2Strings.ainclude_category_id, false);
        //xmlWriter.attribute(MC2Strings.ainclude_full_search_item, false);
        
        mc2w.startElement(MC2Strings.tsearch_item);
        // actually, just item_id matters
        mc2w.attribute(MC2Strings.asearch_item_type, MC2Strings.STREET_STRING);
        mc2w.elementWithText(MC2Strings.tname, MC2Strings.EMPTY_STRING);
        mc2w.elementWithText(MC2Strings.titemid, m_itemID);
        mc2w.endElement(MC2Strings.tsearch_item);
        
        mc2w.elementWithText(MC2Strings.tlanguage, m_lang.getXMLCode());
    }
}
