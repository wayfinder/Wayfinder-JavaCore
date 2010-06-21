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

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.StateMC2RequestListener;
import com.wayfinder.core.network.internal.mc2.impl.MC2ParserImpl;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.search.onelist.MatchDetailsRequestListener;
import com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.internal.settings.language.LanguageFactory;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.poidetails.ImageGroup;
import com.wayfinder.core.shared.poidetails.PoiDetail;
import com.wayfinder.core.shared.poidetails.Review;
import com.wayfinder.core.shared.poidetails.ReviewGroup;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.core.shared.util.io.WFStringReader;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class MatchDetailsMC2RequestTest extends TestCase {
    
    private static final LanguageInternal LANG =  LanguageFactory.createLanguageFor(LanguageInternal.EN_UK);
    
    private static final String MATCH_NAME = "Best Western Hotel Djingis Khan, Margaretavägen 7";
    private static final String ITEM_ID = "c:7000323A:0:0:E";
    private static final String LOCATION_NAME = "Möllevången, Lund";
    
    private static final String REQ_XML = 
        "<poi_detail_request transaction_id=\"ID0\" language=\"english\">" +
        "<itemid>" + ITEM_ID + "</itemid>" +
        "</poi_detail_request>";
    
    private static final String REPLY_STATUS_XML = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><poi_detail_reply transaction_id=\"ID0\">" +
        "<status_code>999</status_code><status_message>dummy message</status_message>" +
        "<status_uri href=\"http://dummy_url\"/>" +
        "</poi_detail_reply></isab-mc2>";
    
    private static final String REPLY_OK = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2><isab-mc2>" +
        "<poi_detail_reply transaction_id=\"ID4\">" +
        "<detail_item numberfields=\"7\">" +
            "<detail_field detail_content_type=\"text\" detail_type=\"street_address\">" +
                 "<fieldName>Address</fieldName>" +
                 "<fieldValue>10-11 Nelson Road</fieldValue>" +
              "</detail_field>"+
              "<detail_field detail_content_type=\"text\" detail_type=\"full_address\">"+
                "<fieldName>Address</fieldName>"+
                 "<fieldValue>10-11 Nelson Road, London SE10 9JB</fieldValue>"+
              "</detail_field>"+
              "<detail_field detail_content_type=\"phone_number\" detail_type=\"phone_number\">"+
                 "<fieldName>Phone Number</fieldName>"+
                 "<fieldValue>020 8293 5263</fieldValue>"+
              "</detail_field>"+
              "<detail_field detail_content_type=\"integer\" detail_type=\"average_rating\">"+
                 "<fieldName>Average Rating</fieldName>"+
                 "<fieldValue>3</fieldValue>"+
              "</detail_field>"+
              "<detail_field detail_content_type=\"text\" detail_type=\"provider_info\">"+
                 "<fieldName>Information Provided By</fieldName>"+
                 "<fieldValue>Qype.com;http://www.qype.com</fieldValue>"+
              "</detail_field>"+
              "<detail_field detail_content_type=\"url\" detail_type=\"poi_url\">"+
                 "<fieldName>View On Qype</fieldName>"+
                 "<fieldValue>http://www.qype.co.uk/place/72679-Noodle-Time-London</fieldValue>"+
              "</detail_field>"+
              "<detail_field detail_content_type=\"url\" detail_type=\"poi_thumb\">"+
                 "<fieldName>Poi Thumb</fieldName>"+
                 "<fieldValue>http://assets1.qype.com/uploads/photos/0007/7369/Noodle_Time_Greenwich_mini.jpg</fieldValue>"+
              "</detail_field>"+
           "</detail_item>"+
           "<resources number_image_groups=\"1\" number_review_groups=\"1\">"+
              "<image_group number_images=\"1\" provider_image=\"search_heading_qype\" provider_name=\"Qype\">"+
                 "<image url=\"http://assets1.qype.com/uploads/photos/0007/7369/Noodle_Time_Greenwich_thumb.jpg\"/>"+
              "</image_group>"+
              "<review_group number_reviews=\"4\" provider_image=\"search_heading_qype1\" provider_name=\"Qype1\">"+
                 "<review date=\"2007-11-01T14:27:35+01:00\" rating=\"4\" reviewer=\"theduck\">"+
                     "I love this place."+
                     "\nIf I am in greenwich it is somewhere I have to stop in on."+
                 "</review>"+
                 "<review date=\"2007-11-10T17:39:24+01:00\" rating=\"4\" reviewer=\"PoppyWomble\">"+
                     "A regular haunt when we lived near Greenwich, this noodle bar is a great place to get good food at reasonable prices (under Â£7 per head, depending on what you choose to drink)."+  
                 "</review>"+
                 "<review date=\"2007-11-13T23:38:40+01:00\" rating=\"4\" reviewer=\"hammond5986\">"+
                     "a more cheap and cheerful place could surely not be found anywhere else. "+   
                 "</review>"+
                 "<review date=\"2008-01-25T03:06:24+01:00\" rating=\"5\" reviewer=\"Aish\">"+
                     "There are many a noodle bars popping up all over London, but my favourite chain has got to be Noodle Time."+
                 "</review>"+
              "</review_group>" +
           "</resources></poi_detail_reply></isab-mc2>";
    
    private static final String REPLY_OK_NODETAILS = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><poi_detail_reply transaction_id=\"ID0\">" +
        "   <detail_item numberfields=\"0\">" +
        "   </detail_item>" +
        "</poi_detail_reply></isab-mc2>";
    
    private OneListSearchMatchImpl m_searchMatch;

    /**
     * @param name
     */
    public MatchDetailsMC2RequestTest(String name) {
        super(name);
        
        m_searchMatch = new OneListSearchMatchImpl(
                ITEM_ID, 
                LOCATION_NAME, 
                MATCH_NAME, 
                Position.NO_POSITION, "", "", "");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testWrite() throws IOException {
        StateMC2RequestListener listener = new StateMC2RequestListener();

        
        MatchDetailsMC2Request req = new MatchDetailsMC2Request(listener, m_searchMatch, LANG);
        
        WFByteArrayOutputStream out = new WFByteArrayOutputStream(1000);

        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        mc2Writer.startElement(req.getRequestElementName());
        mc2Writer.attribute(MC2Strings.atransaction_id, "ID0");
        req.write(mc2Writer);
        mc2Writer.endElement(req.getRequestElementName());
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        String xml = out.bufToString("UTF-8");
        assertEquals(REQ_XML , xml);
    }
    
    public void testParseStatus() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_STATUS_XML));
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MatchDetailsMC2Request req = new MatchDetailsMC2Request(
               listener, m_searchMatch, LANG);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        req.parse(mc2parser);
        
        assertNotNull(listener.m_error);
        assertEquals(CoreError.ERROR_SERVER, listener.m_error.getErrorType());
        ServerError errSrv = (ServerError)listener.m_error;
        assertEquals(999,errSrv.getStatusCode());
        assertEquals("http://dummy_url",errSrv.getStatusUri());
        assertEquals("dummy message",errSrv.getInternalMsg());
        
        assertNull(listener.m_result);
    }
    
    public void testParseOK() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_OK));
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MatchDetailsMC2Request req = new MatchDetailsMC2Request(
                listener, m_searchMatch, LANG);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        req.parse(mc2parser);
        
        assertNull(listener.m_error);
        PoiDetail detail = (PoiDetail)listener.m_result;
        assertNotNull(detail);
        
        
        //test fields
        
        assertNotNull(detail.getPoiThumbnail().toString());
        
        assertEquals("Address", detail.getStreetAddress().getName());
        assertEquals("10-11 Nelson Road", detail.getStreetAddress().getValue());
        assertEquals("10-11 Nelson Road, London SE10 9JB", detail.getFullAddress().getValue());
        assertEquals("Phone Number", detail.getPhone().getName());
        assertEquals("020 8293 5263", detail.getPhone().getValue());
        assertEquals("Average Rating", detail.getAverageRating().getName());
        assertEquals("3", detail.getAverageRating().getValue());
        
        assertEquals("Information Provided By", detail.getProviderInfo().getName());
        assertEquals("Qype.com;http://www.qype.com", detail.getProviderInfo().getValue());
        assertEquals("View On Qype", detail.getPoiUrl().getName());
        assertEquals("http://www.qype.co.uk/place/72679-Noodle-Time-London", detail.getPoiUrl().getValue());
        assertEquals("Poi Thumb", detail.getPoiThumbnail().getName());
        assertEquals("http://assets1.qype.com/uploads/photos/0007/7369/Noodle_Time_Greenwich_mini.jpg", detail.getPoiThumbnail().getValue());
        assertNull(detail.getOpenHours());
        
        //test image group
        ImageGroup imgGrp = detail.getImageGroup(); 
        assertNotNull(imgGrp);
        assertNotNull(imgGrp.toString());
        assertEquals(1 ,imgGrp.getNbrOfImages());
        
        assertEquals("http://assets1.qype.com/uploads/photos/0007/7369/Noodle_Time_Greenwich_thumb.jpg",imgGrp.getImageUrl(0));
        
        assertEquals("Qype", imgGrp.getProvider().getProviderName());
        assertEquals("search_heading_qype", imgGrp.getProvider().getProviderImageName());
        
        //test review group
        ReviewGroup revGrp = detail.getReviewGroup();
        assertNotNull(revGrp);
        assertNotNull(revGrp.toString());
        assertEquals(4 ,revGrp.getNbrOfReviews());
        assertEquals("Qype1", revGrp.getProvider().getProviderName());
        assertEquals("search_heading_qype1", revGrp.getProvider().getProviderImageName());
        Review rev = revGrp.getReview(2);
        
        assertNotNull(rev);
        assertNotNull(rev.toString());
        assertEquals("2007-11-13T23:38:40+01:00",rev.getDate());
        assertEquals(4,rev.getRatingNumber());
        assertEquals("hammond5986",rev.getReviewer());
        assertEquals("a more cheap and cheerful place could surely not be found anywhere else. ",rev.getText());
        
    }
    
    public void testParseOKNoDetails() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_OK_NODETAILS));
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MatchDetailsMC2Request req = new MatchDetailsMC2Request(
                listener, m_searchMatch, LANG);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        req.parse(mc2parser);
        
        assertNull(listener.m_error);
        PoiDetail detail = (PoiDetail)listener.m_result; 
        assertNotNull(detail);
        
        assertNull(detail.getAverageRating());
        
        assertNull(detail.getEmail());
        assertNull(detail.getPhone());
        assertNull(detail.getWebsite());
        
        assertNull(detail.getDescription());
        assertNull(detail.getOpenHours());
        
        assertNull(detail.getStreetAddress());
        assertNull(detail.getFullAddress());
        
        assertNull(detail.getPoiUrl());
        assertNull(detail.getProviderInfo());

        assertNull(detail.getImageGroup());
        assertNull(detail.getReviewGroup());
        
    }
}
