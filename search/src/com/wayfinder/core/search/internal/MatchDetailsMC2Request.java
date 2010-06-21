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
package com.wayfinder.core.search.internal;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2RequestAdapter;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.poidetails.ImageGroup;
import com.wayfinder.core.shared.poidetails.Provider;
import com.wayfinder.core.shared.poidetails.Review;
import com.wayfinder.core.shared.poidetails.ReviewGroup;

/**
 * 
 *
 */
class MatchDetailsMC2Request extends MC2RequestAdapter {
    
    private final OneListSearchMatchImpl m_searchMatch;
    private final LanguageInternal m_language;
    
    /**
     * @param 
     * @param handler
     */
    MatchDetailsMC2Request(
            MC2RequestListener handler,
            OneListSearchMatchImpl match,
            LanguageInternal language) {
        super(handler);
        m_searchMatch = match;
        m_language = language;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#getRequestElementName()
     */
    public String getRequestElementName() {
        return MC2Strings.tpoi_detail_request;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException, IOException {
        //XML API v2.3.1
        /*
        <!ELEMENT poi_detail_reply ( detail_item | ( status_code, status_message,
                status_code_extended? ) ) >
                <!ATTLIST poi_detail_reply transaction_id ID #REQUIRED>
                <!ELEMENT detail_item ( detail_field* )>
                <!ATTLIST detail_item numberfields %number; #REQUIRED >
                <!ELEMENT detail_field ( fieldName, fieldValue ) >
                <!ATTLIST detail_field detail_type %poi_detail_t; #IMPLIED >
                <!ENTITY % poi_detail_t "(dont_show|text|street_address|full_address|zip_area)" >
         */
        mc2p.nameOrError(MC2Strings.tpoi_detail_reply);
        mc2p.childrenOrError();
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        mc2p.nameOrError(MC2Strings.tdetail_item);
        PoiDetailImpl detail = mc2p.parseDetailItem();
        while (mc2p.advance()) {
            if (mc2p.nameRefEq(MC2Strings.tresources)){
                parseResource(mc2p, detail);
            }
        }
        mc2p.nameOrError(MC2Strings.tpoi_detail_reply);
        
        result(detail);
    }

    
    private static void parseResource(MC2Parser mc2p, PoiDetailImpl detail) 
            throws IOException, IllegalStateException, MC2ParserException {
        // <!ELEMENT resources ( image_group*, review_group* ) >
        
        ImageGroup imageGroup = null; 
        ReviewGroup reviewGroup = null;
        
        if (mc2p.children()) {
            do {
                if (mc2p.nameRefEq(MC2Strings.timage_group)) {
                    //for the moment if there are multiple groups we just use 
                    //the first one 
                    if (imageGroup != null) continue;
                    
                    // <!ELEMENT image_group ( image* ) >
                    // <!ATTLIST image_group number_images %number; #REQUIRED
                    // provider_name CDATA #REQUIRED
                    // provider_image CDATA #REQUIRED >
                    // <!ELEMENT image ( EMPTY ) >
                    // <!ATTLIST image url CDATA #REQUIRED >
                    
                    int n = mc2p.attributeAsInt(MC2Strings.anumber_images);
                    Provider provider = new Provider(
                            mc2p.attribute(MC2Strings.aprovider_name),
                            mc2p.attribute(MC2Strings.aprovider_image));
                    
                    String[] imageUrlInternal = new String[n];
                    if (n > 0) {
                        mc2p.childrenOrError();
                        for(int i = 0; i < n; i++) {
                            mc2p.nameOrError(MC2Strings.timage);
                            imageUrlInternal[i] = mc2p.attribute(MC2Strings.aurl);
                            mc2p.advance();
                        }
                    }
                    imageGroup = new ImageGroup(provider, imageUrlInternal);
                    
                } else if (mc2p.nameRefEq(MC2Strings.treview_group)) {
                    //for the moment if there are multiple groups we just use 
                    //the first one 
                    if (reviewGroup != null) continue;

                    // <!ELEMENT review_group ( review* ) >
                    // <!ATTLIST review_group number_reviews %number; #REQUIRED
                    // provider_name CDATA #REQUIRED
                    // provider_image CDATA #REQUIRED >
                    // <!ELEMENT review ( #PCDATA ) >
                    // <!ATTLIST review rating %number; #REQUIRED
                    // date CDATA #REQUIRED
                    // reviewer CDATA #REQUIRED >
                    
                    int n = mc2p.attributeAsInt(MC2Strings.anumber_reviews);
                    
                    Provider provider = new Provider(
                            mc2p.attribute(MC2Strings.aprovider_name),
                            mc2p.attribute(MC2Strings.aprovider_image));
                    Review[] reviewInternal = new Review[n];
                        
                    if (n > 0) {
                        mc2p.childrenOrError();
                        for(int i = 0; i < n; i++) {
                            mc2p.nameOrError(MC2Strings.treview);
                            // <!ELEMENT review ( #PCDATA ) >
                            // <!ATTLIST review rating %number; #REQUIRED
                            // date CDATA #REQUIRED
                            // reviewer CDATA #REQUIRED >                            
                            String date = mc2p.attribute(MC2Strings.adate);
                            String reviewer = mc2p.attribute(MC2Strings.areviewer);
                            int ratingNumber = mc2p.attributeAsInt(MC2Strings.arating);
                            String text = mc2p.value();
                            reviewInternal[i] = new Review(date, reviewer, ratingNumber, text);
                            
                            mc2p.advance();
                        }
                    }
                    reviewGroup = new ReviewGroup(provider, reviewInternal);
                }
            } while (mc2p.advance());
        }
        mc2p.nameOrError(MC2Strings.tresources);
        detail.setResources(imageGroup, reviewGroup);
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        /*
        <!ELEMENT poi_detail_request ( item_id ) >
        <!ATTLIST poi_detail_request transaction_id ID #REQUIRED
                                        language %language_t; #REQUIRED>
        */
        //writer is positioned in inside poi_details_request and the 
        //transaction_id has been written
        mc2w.attribute(MC2Strings.alanguage, m_language.getXMLCode());
        mc2w.elementWithText(MC2Strings.titemid, m_searchMatch.getMatchID());
    }

}
