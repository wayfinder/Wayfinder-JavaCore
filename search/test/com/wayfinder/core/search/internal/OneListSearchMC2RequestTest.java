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

import junit.framework.TestCase;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.impl.MC2ParserImpl;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.internal.OneListSearchMC2Request.RequestListener;
import com.wayfinder.core.search.internal.categorytree.HierarchicalCategoryImpl;
import com.wayfinder.core.search.internal.topregion.TopRegionImpl;
import com.wayfinder.core.search.internal.topregion.TopRegionImplTest;
import com.wayfinder.core.search.onelist.OneListSearchReply;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.internal.settings.language.LanguageFactory;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.poidetails.PoiDetail;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.core.shared.util.io.WFStringReader;

/**
 * 
 *
 */
public class OneListSearchMC2RequestTest extends TestCase {
    
    /**
     * 
     */
    private static final int MAX_NBR_ADDRESS_MATCHES = 15;

    private static final String ELEMENT = MC2Strings.tone_search_request;
    
    private static final Position POS = new Position(664744320, 157372032);
    
    private static final LanguageInternal LANG =  LanguageFactory.createLanguageFor(LanguageInternal.EN_UK);
    
    private static final String QUERY_STR = "hotel djingis khan";
    
    private static final String POSITIONAL_REQ_XML =
        "<"+ELEMENT+" transaction_id=\"ID0\" version=\""+OneListSearchMC2Request.SERVER_VERSION+"\" " +
        "language=\""+LANG.getXMLCode()+"\" " +
        "max_number_matches=\"100\" " +
        "round=\"0\" sorting=\""+OneListSearchMC2Request.DISTANCE_SORT+"\" "+
        "include_detail_fields=\"true\">"+
        "<search_match_query>"+QUERY_STR+"</search_match_query>" +
        "<category_list><category_id>118</category_id></category_list>" +
        "<position_item position_system=\"MC2\"><lat>"+POS.getMc2Latitude()+"</lat><lon>"+POS.getMc2Longitude()+"</lon></position_item>" +
        "<distance>5000</distance>" +
        "</"+ELEMENT+">";
    
    private static final String ADDRESS_REQ_XML = 
        "<"+ELEMENT+" transaction_id=\"ID0\" version=\""+OneListSearchMC2Request.SERVER_VERSION+"\" " +
        "language=\""+LANG.getXMLCode()+"\" " +
        "max_number_matches=\""+MAX_NBR_ADDRESS_MATCHES+"\" " +
        "round=\"0\" sorting=\""+OneListSearchMC2Request.ALFA_SORT+"\" "+
        "include_detail_fields=\"false\" "+
        "search_type=\"address\">" +
        "<search_match_query>Nobelvagen 15</search_match_query>" +
        "<query_location>Malmo</query_location>" +
        "<top_region_id>1</top_region_id>" +
        "</"+ELEMENT+">";
    
    private static final String REPLY_STATUS_XML = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><one_search_reply transaction_id=\"ID0\">" +
        "<status_code>999</status_code><status_message>dummy message</status_message>" +
        "<status_uri href=\"http://dummy_url\"/>" +
        "</one_search_reply></isab-mc2>";

    private static final String MATCH_ID = "c:7000323A:0:0:E";
    private static final int MATCH_LAT = 664760354;
    private static final int MATCH_LON = 157423960;
    
    //private static final int POI_DETAILS_LEVEL = GeneralSettings.INCLUDE_POI_DETAILS_NONE;

    /**
     * A human readable description of the location of the match. Not an exact
     * position but rather the name of the city area or similar.
     */
    protected static final String MATCH_LOCATION = "Möllevången, Lund";
    protected static final String MATCH_NAME = "Best Western Hotel Djingis Khan, Margaretavägen 7";

    protected static final String BRAND_IMAGE_NAME = "img_brand";
    protected static final String CATEGORY_IMAGE_NAME = "tat_hotel";
    protected static final String PROVIDER_IMAGE_NAME = "img_provider";
    
    private static final String REPLY_MATCHES_XML = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2><isab-mc2>" + 
    		"   <one_search_reply transaction_id=\"ID0\">" + 
    		"      <search_list number_matches=\"1\" total_number_matches=\"1\">" + 
    		"         <search_match brand_image=\"" + BRAND_IMAGE_NAME + "\"" +
    		"                       category_image=\"" + CATEGORY_IMAGE_NAME + "\"" + 
    		"                       provider_image=\"" + PROVIDER_IMAGE_NAME + "\"" +
    		"                       search_match_type=\"pointofinterest\" " + 
    		"                       additional_info_exists=\"true\">" +
    		"            <name>" + MATCH_NAME + "</name>" + 
    		"            <itemid>" + MATCH_ID + "</itemid>" + 
    		"            <location_name>" + MATCH_LOCATION + "</location_name>" + 
    		"            <lat>" + MATCH_LAT + "</lat>" + 
    		"            <lon>" + MATCH_LON + "</lon>" + 
    		"            <category_list>" + 
    		"               <category_id>118</category_id>" + 
    		"            </category_list>" + 
    		"            <search_area search_area_type=\"citypart\">" + 
    		"               <name>Möllevången</name>" + 
    		"               <areaid>d:280071E3:0:0:C</areaid>" + 
    		"               <search_area search_area_type=\"city\">" + 
    		"                  <name>Lund</name>" + 
    		"                  <areaid>b:8F:0:0:B</areaid>" + 
    		"                  <search_area search_area_type=\"municipal\">" + 
    		"                     <name>Lund</name>" + 
    		"                     <areaid>a:90:0:0:1</areaid>" + 
    		"                  </search_area>" + 
    		"               </search_area>" + 
    		"            </search_area>" + 
    		"            <search_area search_area_type=\"zipcode\">" + 
    		"               <name>22240</name>" + 
    		"               <areaid>z:5000018A:0:0:A</areaid>" + 
    		"            </search_area>" + 
    		"            <detail_item numberfields=\"9\">" +
    		"            <detail_field detail_type=\"vis_house_nbr\">" + 
    		"               <fieldName>House number</fieldName>" + 
    		"               <fieldValue>7</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"vis_address\">" + 
    		"               <fieldName>address</fieldName>" + 
    		"               <fieldValue>Margaretavägen</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"vis_zip_code\">" + 
    		"               <fieldName>Post code</fieldName>" + 
    		"               <fieldValue>22241</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"vis_zip_area\">" + 
    		"               <fieldName>City</fieldName>" + 
    		"               <fieldValue>Lund</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"url\">" + 
    		"               <fieldName>URL</fieldName>" + 
    		"               <fieldValue>www.djingiskhan.nu</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"brandname\">" + 
    		"               <fieldName>Brandname</fieldName>" + 
    		"               <fieldValue>Best Western</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"supplier\">" + 
    		"               <fieldName>supplier</fieldName>" + 
    		"               <fieldValue>Tele Atlas</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"dont_show\">" + 
    		"               <fieldName>static_id</fieldName>" + 
    		"               <fieldValue>691360</fieldValue>" + 
    		"            </detail_field>" + 
    		"            <detail_field detail_type=\"text\">" + 
    		"               <fieldName>Category</fieldName>" + 
    		"               <fieldValue>Hotels</fieldValue>" + 
    		"            </detail_field>" + 
    		"            </detail_item>" +
    		"         </search_match>" + 
    		"      </search_list>" + 
    		"   </one_search_reply>" + 
    		"</isab-mc2>";
    
    private static final String REPLY_MATCHES_NOINFO_XML = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2><isab-mc2>" + 
            "   <one_search_reply transaction_id=\"ID0\">" + 
            "      <search_list number_matches=\"1\" total_number_matches=\"1\">" + 
            "         <search_match brand_image=\"" + BRAND_IMAGE_NAME + "\"" +
            "                       category_image=\"" + CATEGORY_IMAGE_NAME + "\"" + 
            "                       provider_image=\"" + PROVIDER_IMAGE_NAME + "\"" +
            "                       search_match_type=\"pointofinterest\">" + 
            "            <name>" + MATCH_NAME + "</name>" + 
            "            <itemid>" + MATCH_ID + "</itemid>" + 
            "            <location_name>" + MATCH_LOCATION + "</location_name>" + 
            "            <lat>" + MATCH_LAT + "</lat>" + 
            "            <lon>" + MATCH_LON + "</lon>" + 
            "            <category_list>" + 
            "               <category_id>118</category_id>" + 
            "            </category_list>" + 
            "            <search_area search_area_type=\"citypart\">" + 
            "               <name>Möllevången</name>" + 
            "               <areaid>d:280071E3:0:0:C</areaid>" + 
            "               <search_area search_area_type=\"city\">" + 
            "                  <name>Lund</name>" + 
            "                  <areaid>b:8F:0:0:B</areaid>" + 
            "                  <search_area search_area_type=\"municipal\">" + 
            "                     <name>Lund</name>" + 
            "                     <areaid>a:90:0:0:1</areaid>" + 
            "                  </search_area>" + 
            "               </search_area>" + 
            "            </search_area>" + 
            "            <search_area search_area_type=\"zipcode\">" + 
            "               <name>22240</name>" + 
            "               <areaid>z:5000018A:0:0:A</areaid>" + 
            "            </search_area>" + 
            "         </search_match>" + 
            "      </search_list>" + 
            "   </one_search_reply>" + 
            "</isab-mc2>";

    
    private static String createBadXmlReply(int totalMatches, int nbrMatches, String matchName, Position pos, String locationName, String id) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2><isab-mc2>" + 
        "<one_search_reply transaction_id=\"ID0\">" + 
        "<search_list number_matches=\""+nbrMatches+"\" total_number_matches=\""+totalMatches+"\">" + 
        "<search_match brand_image=\"\" category_image=\"tat_hotel\" provider_image=\"\" search_match_type=\"pointofinterest\">" + 
        //"<name>Best Western Hotel Djingis Khan, Margaretavägen 7</name>\n" +
        (matchName != null ? "<name>"+matchName+"</name>" : "") +
        //"<itemid>c:7000323A:0:0:E</itemid>\n" +
        (id != null ? "<itemid>"+id+"</itemid>" : "") +
        //"<location_name>Möllevången, Lund</location_name>\n" +
        (locationName != null ? "<location_name>"+locationName+"</location_name>" : "") +
        (pos != null ? "<lat>"+pos.getMc2Latitude()+"</lat><lon>"+pos.getMc2Longitude()+"</lon>" : "") + 
        "</search_match>" + 
        "</search_list>" + 
        "</one_search_reply>" + 
        "</isab-mc2>";
    }
    
    private SearchQuery m_query;        
    private String m_sorting = OneListSearchMC2Request.DISTANCE_SORT;

    /**
     * @param name
     */
    public OneListSearchMC2RequestTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_query = SearchQuery.createPositionalQuery(
                QUERY_STR, 
                new HierarchicalCategoryImpl("Hotel", "tat_hotel", 118, 0), 
                POS, 5000, true);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMC2Request#getRequestElementName()}.
     */
    public void testGetRequestElementName() {
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                // 
                
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                // 
                
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        assertEquals(ELEMENT, req.getRequestElementName());
    }
    
    CoreError m_dummyError;
    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMC2Request#error(com.wayfinder.core.shared.error.CoreError)}.
     */
    public void testError() {
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                assertSame(m_dummyError, error);
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                fail("OneListSearchMC2RequestTest.testParseStatus().new RequestListener() {...}.replyReceived() should not be reached");
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        m_dummyError =  new CoreError("dummy error");
        req.error(m_dummyError);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)}.
     * @throws IOException 
     */
    public void testWritePositionalSearch() throws IOException {
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testWrite().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                fail("OneListSearchMC2RequestTest.testWrite().new RequestListener() {...}.replyReceived() should not be reached");
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, OneListSearchMC2Request.DISTANCE_SORT);
        
        WFByteArrayOutputStream out = new WFByteArrayOutputStream(1000);

        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        mc2Writer.startElement(req.getRequestElementName());
        mc2Writer.attribute(MC2Strings.atransaction_id, "ID0");
        
        req.write(mc2Writer);
        mc2Writer.endElement(req.getRequestElementName());
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.bufToString("UTF-8"); //
        assertEquals(POSITIONAL_REQ_XML , xml);
    }
    
    public void testWriteAddressGeocodingSearch() throws IOException {
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testWrite().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                fail("OneListSearchMC2RequestTest.testWrite().new RequestListener() {...}.replyReceived() should not be reached");
            }
        }; 
        SearchQuery query = SearchQuery.createAddressGeocodingQuery(
                "Nobelvagen 15", "Malmo", TopRegionImplTest.TOP_REGION_SWEDEN, MAX_NBR_ADDRESS_MATCHES);
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, query, 0, OneListSearchMC2Request.ALFA_SORT);
        
        WFByteArrayOutputStream out = new WFByteArrayOutputStream(1000);

        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        mc2Writer.startElement(req.getRequestElementName());
        mc2Writer.attribute(MC2Strings.atransaction_id, "ID0");
        
        req.write(mc2Writer);
        mc2Writer.endElement(req.getRequestElementName());
        //close to flush the data
        mc2Writer.close();
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.bufToString("UTF-8"); //
        assertEquals(ADDRESS_REQ_XML , xml);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.OneListSearchMC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)}.
     * @throws IOException 
     * @throws MC2ParserException 
     * @throws IllegalStateException 
     */
    public void testParseGoodReply() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_MATCHES_XML));
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testParse().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                assertNotNull(searchMatches);
                assertEquals(1, estimatedTotalNbrOfMatches);
                assertEquals(1, searchMatches.length);
                OneListSearchReply.SearchMatch match = searchMatches[0];
                assertEquals(MATCH_ID, match.getMatchID());
                assertEquals(MATCH_LOCATION, match.getMatchLocation());
                assertEquals(MATCH_NAME,
                             match.getMatchName());
                Position matchPos = match.getPosition();
                assertEquals(MATCH_LAT, matchPos.getMc2Latitude());
                assertEquals(MATCH_LON, matchPos.getMc2Longitude());
                assertEquals(BRAND_IMAGE_NAME, match.getMatchBrandImageName());
                assertEquals(CATEGORY_IMAGE_NAME,
                             match.getMatchCategoryImageName());
                assertEquals(PROVIDER_IMAGE_NAME,
                             match.getMatchProviderImageName());
                
                PoiDetail detail = match.getFilteredInfo();
                assertNull(detail.getDescription());
                //TODO update the xml and the data
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        req.parse(mc2parser);
    }
    
    public void testParseGoodReplyNoInfoFields() throws IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_MATCHES_NOINFO_XML));
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testParse().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                assertNotNull(searchMatches);
                assertEquals(1, estimatedTotalNbrOfMatches);
                assertEquals(1, searchMatches.length);
                OneListSearchReply.SearchMatch match = searchMatches[0];
                assertEquals(MATCH_ID, match.getMatchID());
                assertEquals(MATCH_LOCATION, match.getMatchLocation());
                assertEquals(MATCH_NAME,
                             match.getMatchName());
                Position matchPos = match.getPosition();
                assertEquals(MATCH_LAT, matchPos.getMc2Latitude());
                assertEquals(MATCH_LON, matchPos.getMc2Longitude());
                assertEquals(BRAND_IMAGE_NAME, match.getMatchBrandImageName());
                assertEquals(CATEGORY_IMAGE_NAME,
                             match.getMatchCategoryImageName());
                assertEquals(PROVIDER_IMAGE_NAME,
                             match.getMatchProviderImageName());
                
                PoiDetail detail = match.getFilteredInfo();
                
                assertEquals(PoiDetailImpl.EMPTY_POI_DETAIL, detail);
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        req.parse(mc2parser);
    }
    
    public void testMatchNbrErrors() throws IOException, IllegalStateException {
        
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testMatchNbrErrors().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                fail("OneListSearchMC2RequestTest.testMatchNbrErrors().new RequestListener() {...}.replyReceived() should not be reached");
            }
        };
        SearchQuery query = SearchQuery.createAddressGeocodingQuery(
                "street", "city", TopRegionImplTest.TOP_REGION_SWEDEN, 5);
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, query, 0, m_sorting);
        
        MC2Parser mc2parser = new MC2ParserImpl(
                        new WFStringReader(
                                createBadXmlReply(10, 10, "match name", POS, 
                                                  "location name",
                                                  "c:7000323A:0:0:E")));
        try {
            mc2parser.childrenOrError();
            mc2parser.childrenOrError();
        } catch (MC2ParserException e1) {
            fail("MC2Parser exception should not be thrown at this stage");
        }
        
        try {
            req.parse(mc2parser);
            fail("MC2ParserException expected");
        }
        catch (MC2ParserException e) {
            // ok
        }
    }
    
    public void testOneMatchErrors() throws IOException {
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testOneMatchErrors().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                fail("OneListSearchMC2RequestTest.testOneMatchErrors().new RequestListener() {...}.replyReceived() should not be reached");
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(
                        createBadXmlReply(1, 1, null, POS, 
                                          null, null)));
        try {
            mc2parser.childrenOrError();
            mc2parser.childrenOrError();
        } catch (MC2ParserException e1) {
            fail("MC2Parser exception should not be thrown at this stage");
        }
        
        try {
            req.parse(mc2parser);
            fail("MC2ParserException expected");
        }
        catch (MC2ParserException e) {
            
        }
        
        try {
            req = new OneListSearchMC2Request(
                    null, null, null, 0, null);
            fail("IllegalArgumentException should have been thrown!");
        }
        catch (IllegalArgumentException e) {
            // lang, listener and query params are null
        }
        
        try {
            Category cat = new HierarchicalCategoryImpl("Hotel", "tat_hotel", 118, 0);
            req = new OneListSearchMC2Request(
                    LANG, reqListener, 
                    SearchQuery.createRegionalQuery(
                            QUERY_STR, cat, "Lund", TopRegionImplTest.TOP_REGION_SWEDEN), 
                    0, null);
            fail("IllegalArgumentException should have been thrown!");
        }
        catch (IllegalArgumentException e) {
            // query not of type SearchQuery.SEARCH_TYPE_POSITIONAL
        }
        
        try {
            req = new OneListSearchMC2Request(
                    LANG, reqListener, m_query, 0, null);
            fail("IllegalArgumentException should have been thrown!");
        }
        catch (IllegalArgumentException e) {
            // maxNbrMatches param is 0
        }
    }
    
    public void testMatchNoPos() throws IOException, IllegalStateException, MC2ParserException {
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                fail("OneListSearchMC2RequestTest.testMatchNoPos().new RequestListener() {...}.requestFailed() should not be reached");
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                assertEquals(0, estimatedTotalNbrOfMatches);
                assertEquals(0, searchMatches.length);
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        
        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(
                        createBadXmlReply(1, 1, "name", null, 
                                          "location", "c:000:000")));
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
        req.parse(mc2parser);
    }

    public void testParseStatus() throws IOException, MC2ParserException {

        MC2Parser mc2parser = new MC2ParserImpl(
                new WFStringReader(REPLY_STATUS_XML));
        
        RequestListener reqListener = new RequestListener() {
            
            public void requestFailed(CoreError error) {
                assertNotNull(error);
                assertEquals(CoreError.ERROR_SERVER, error.getErrorType());
                ServerError errSrv = (ServerError)error;
                assertEquals(999,errSrv.getStatusCode());
                assertEquals("http://dummy_url",errSrv.getStatusUri());
                assertEquals("dummy message",errSrv.getInternalMsg());
            }
            
            public void replyReceived(int estimatedTotalNbrOfMatches,
                    OneListSearchMatchImpl[] searchMatches) {
                fail("OneListSearchMC2RequestTest.testParseStatus().new RequestListener() {...}.replyReceived() should not be reached");
            }
        };
        MC2Request req = new OneListSearchMC2Request(
                LANG, reqListener, m_query, 0, m_sorting);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        req.parse(mc2parser);
    }
}
