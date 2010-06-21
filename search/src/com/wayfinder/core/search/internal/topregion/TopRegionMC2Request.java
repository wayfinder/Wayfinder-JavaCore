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
package com.wayfinder.core.search.internal.topregion;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.xml.XmlIterator;
import com.wayfinder.core.shared.xml.XmlWriter;

final class TopRegionMC2Request implements MC2Request {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TopRegionMC2Request.class);

    private final TopRegionHolder m_regionHolder;
    private final String m_crc;
    private final GeneralSettingsInternal m_settings;
    
    TopRegionMC2Request(
            TopRegionHolder holder, 
            String crc, 
            GeneralSettingsInternal settings) {
        
        m_crc = crc;
        m_regionHolder = holder;
        m_settings = settings;
    }
    

    public String getRequestElementName() {
        return MC2Strings.ttop_region_request;
    }

    
    public void write(MC2Writer mc2w) throws IOException {
        
        mc2w.attribute(MC2Strings.atop_region_crc, m_crc);
        mc2w.startElement(MC2Strings.ttop_region_request_header);
        //shouldn't be needed really...
        mc2w.attribute(MC2Strings.aposition_system, MC2Strings.MC2);
        //iWriter.attribute("country", false);//will return an empty list from srv
        mc2w.elementWithText(MC2Strings.tlanguage, m_settings.getInternalLanguage().getXMLCode());
        mc2w.endElement(MC2Strings.ttop_region_request_header);
    }
    
    
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        final String crc = mc2p.attribute(MC2Strings.atop_region_crc);
        mc2p.children(); // element top_region_list
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        
        if (mc2p.nameRefEq(MC2Strings.ttop_region_crc_ok)) {
            if(LOG.isDebug()) {
                LOG.debug("TopRegionMC2Request.parse()", 
                             "List of top regions is up to date");
            }
            
            // we are done
            m_regionHolder.noUpdateAvailable();
            return;
        } 
        
        // <!ELEMENT top_region_list ( top_region* )>
        // <!ATTLIST top_region_list numberitems %number; #REQUIRED>
        final int nbrregions = mc2p.attributeAsInt(MC2Strings.anumberitems);
        TopRegionImpl[] topregions = new TopRegionImpl[nbrregions];
        mc2p.children();
        for (int i = 0; i < topregions.length; i++) {
            /*
            if (!xpi.attribute(MC2Strings.atop_region_type)
                    .equals("country")) {
                    throwExc("top_region_type=\"country\"");
                }*/
            mc2p.children();
            // all elements are required in DTD
            topregions[i] = parseTopRegion(mc2p);
            mc2p.advance();
        }
        m_regionHolder.updateTopRegionList(
                topregions, 
                crc, 
                m_settings.getInternalLanguage().getId(),
                true);
    }
    
    
    static TopRegionImpl parseTopRegion(XmlIterator xpi) throws IOException {
        int id = 0;
        String name = null;

        // depth: 5
        do {
            if (xpi.nameRefEq(MC2Strings.ttop_region_id)) {
                id = xpi.valueCharArray().intValue();
            } else if (xpi.nameRefEq(MC2Strings.tname_node)) {
                name = xpi.value();
            }
        } while (xpi.advance());

        // all elements are required in DTD
        return new TopRegionImpl(name, TopRegionImpl.TYPE_COUNTRY, id);
    }


    public void error(CoreError coreError) {
        //TODO report the error
        m_regionHolder.noUpdateAvailable();
    }
    
    
}
