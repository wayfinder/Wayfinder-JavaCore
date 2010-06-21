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

import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.search.internal.SearchDescriptor.Provider;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.util.CharArray;
import com.wayfinder.core.shared.xml.XmlIterator;
import com.wayfinder.core.shared.xml.XmlWriter;

class SearchDescriptorMC2Request implements MC2Request {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(SearchDescriptorMC2Request.class);
    
    private final GeneralSettingsInternal m_settings;
    private final SearchDescriptor m_currentDesc;
    private final SearchHolder m_holder;
    
    SearchDescriptorMC2Request(GeneralSettingsInternal settings, 
                               SearchDescriptor desc, SearchHolder holder) {
        m_settings = settings;
        m_currentDesc = desc;
        m_holder = holder;
    }
    

    public String getRequestElementName() {
        return MC2Strings.tsearch_desc_request;
    }


    public void write(MC2Writer mc2w) throws IOException {
        mc2w.attribute(MC2Strings.acrc, m_currentDesc.getCRC());
        mc2w.attribute(MC2Strings.alanguage, m_settings.getInternalLanguage().getXMLCode());
        mc2w.attribute(MC2Strings.adesc_version, SearchDescriptor.DESC_SERVER_VERSION);
    }
    
    
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        
        if(LOG.isDebug()) {
            LOG.debug("SearchDescriptorMC2Request.parse()", 
                    "checking if new provider list is available");
        }
        final String crc = mc2p.attribute(MC2Strings.acrc);

        // this will generate an XML warning if the list of providers is up to 
        // date, because then no nbr_services attribute will exist in the XML
        CharArray ca = mc2p.attributeCharArray(MC2Strings.alength);
        int nbrOfProviders = 0;
        if (ca != null) {
            nbrOfProviders = MC2Strings.number_type(ca);
        }
         
        // entering the actual descriptor response or "crc ok" message
        mc2p.children();
        
        if(mc2p.nameRefEq(MC2Strings.tcrc_ok)) {
            if(LOG.isDebug()) {
                LOG.debug("SearchDescriptorMC2Request.parse()", 
                        "Current list of providers is valid");
            }
            // we are done
            m_holder.noDescriptorUpdateAvailable();
            return;
        }

        // new provider list, parse it
        if(LOG.isDebug()) {
            LOG.debug("SearchDescriptorMC2Request.parse()", 
                    "new list of search providers available");
        }
        
        Provider[] providerArray = new Provider[nbrOfProviders];
        for (int i = 0; i < nbrOfProviders; i++) {
            providerArray[i] = parseProvider(mc2p);
            if(LOG.isTrace()) {
                LOG.trace("SearchDescriptorMC2Request.parse()", providerArray[i].toString());
            }
            mc2p.advance();
        }
        m_holder.setNewSearchDescriptor(new SearchDescriptor(crc, providerArray, m_settings.getLanguage().getId()), true);
    }
    
    
    static Provider parseProvider(XmlIterator xpi) 
    throws IOException, MC2ParserException {
        
        final int round;
        CharArray ca = xpi.attributeCharArray(MC2Strings.around);
        if (ca != null) {
            round = ca.intValue();
        } else {
            throw new MC2ParserException("No round attribute");
        }
        
        final int heading;
        ca = xpi.attributeCharArray(MC2Strings.aheading);
        if (ca != null) {
            heading = ca.intValue();
        } else {
            throw new MC2ParserException("No heading attribute");
        }
        
        xpi.children();
        String name = xpi.value();
        int topRegionID = Provider.ALL_REGIONS;
        String imageName = null;
        String type = null;
        while (xpi.advance()) {
            if (xpi.nameRefEq(MC2Strings.ttype)) {
                type = xpi.value();
            } else if (xpi.nameRefEq(MC2Strings.ttop_region_id)) {
                topRegionID = xpi.valueCharArray().intValue();
            } else if (xpi.nameRefEq(MC2Strings.timage_name)) {
                imageName = xpi.value();
            }
        }
        if(imageName == null) {
            imageName = "";
        }
        if(type == null) {
            type = "";
        }
        
        return new Provider(round, heading, name, type, topRegionID, imageName);
    }
    
    
    public void error(CoreError coreError) {
        if(LOG.isError()) {
            LOG.error("SearchDescriptorMC2Request.error()", 
                    "Got a CoreError (" + coreError.getInternalMsg() +") in SearchDescriptorMC2Request");
        }
        m_holder.descriptorUpdateError(coreError);
    }

}
