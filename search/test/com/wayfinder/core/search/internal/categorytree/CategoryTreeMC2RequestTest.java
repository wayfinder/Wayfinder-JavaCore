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
/**
 * 
 */
package com.wayfinder.core.search.internal.categorytree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.StateMC2RequestListener;
import com.wayfinder.core.network.internal.mc2.impl.MC2ParserImpl;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.settings.language.LanguageFactory;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.util.WFBase64;

import junit.framework.TestCase;

/**
 * 
 */
public class CategoryTreeMC2RequestTest extends TestCase {
    
    private static final Position POS = new Position(614418932,1193046);  //London area
    
    private static final String CRC = "1234";
    
    private static final LanguageInternal LANG =  LanguageFactory.createLanguageFor(LanguageInternal.EN_UK);
    
    private static final String ELEMENT = "local_category_tree_request";

    public static String createResponseXml(String crc, byte[] catTable, byte[] lookTable, byte[] strTable) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><local_category_tree_reply crc=\""+crc+"\" transaction_id=\"ID0\">" +
        "<category_table length=\"" + catTable.length + "\">" + WFBase64.encode(catTable) + "</category_table>"+ 
        "<lookup_table length=\"" + lookTable.length + "\">" + WFBase64.encode(lookTable) + "</lookup_table>"+
        "<string_table length=\"" + strTable.length + "\">" + WFBase64.encode(strTable) + "</string_table>"+
        "</local_category_tree_reply></isab-mc2>";
    }

    public static String XML_RESPONSE_CRCOK = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><local_category_tree_reply transaction_id=\"ID0\">" +
        "<crc_ok/>"+
        "</local_category_tree_reply></isab-mc2>";
    
    public static String XML_RESPONSE_STATUS = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><local_category_tree_reply transaction_id=\"ID0\">" +
        "<status_code>999</status_code><status_message>dummy message</status_message>" +
        "<status_uri href=\"http://dummy_url\"/>" + //&#47;&#47;
        "</local_category_tree_reply></isab-mc2>";

    public static String createRequestXml(String crc, LanguageInternal lang, Position pos) {
        return "<" + ELEMENT +
        " transaction_id=\"ID0\" crc=\""+ crc + "\" language=\"" + lang.getXMLCode() + "\" version=\"1\">" +
        "<position_item position_system=\"MC2\">" +
        "<lat>" + pos.getMc2Latitude() + "</lat>" +
        "<lon>" + pos.getMc2Longitude()+ "</lon></position_item>" +
        "</" + ELEMENT + ">";
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreeMC2Request#getRequestElementName()}.
     */
    public void testGetRequestElementName() {
        StateMC2RequestListener listener = new StateMC2RequestListener();
        MC2Request request = new CategoryTreeMC2Request(POS, CRC, LANG, listener);
                
        assertFalse("requestDone shouldn't be called", listener.m_doneCalled);        
        assertEquals(ELEMENT, request.getRequestElementName());
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreeMC2Request#error(com.wayfinder.core.shared.error.CoreError)}.
     */
    public void testError() {
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, CRC, LANG, listener);
        
        CoreError dummyError = new CoreError("dummy error");
        request.error(dummyError);
        assertSame(dummyError, listener.m_error);
        assertNull(listener.m_result);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreeMC2Request#parse(com.wayfinder.core.network.internal.mc2.MC2Parser)}.
     * @throws IOException 
     * @throws CategoryTreeException 
     * @throws NoSuchElementException 
     * @throws MC2ParserException 
     */
    public void testParseSuccess() throws IOException, NoSuchElementException, CategoryTreeException, MC2ParserException {
        final CategoryTreeImpl catTree = new CategoryTreeImpl(
                LANG.getId(), POS, CRC, 
                MinimalTree.m_categoryTable, 
                MinimalTree.m_lookupTable,
                MinimalTree.m_stringTable);
        
        String xml = createResponseXml(CRC, 
                MinimalTree.m_categoryTable, 
                MinimalTree.m_lookupTable,
                MinimalTree.m_stringTable);
        
        MC2Parser mc2parser = new MC2ParserImpl(new ByteArrayInputStream(xml.getBytes("UTF-8")), "UTF-8");
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, "old_crc", LANG, listener);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
        request.parse(mc2parser);
        
        //we have a result and is different than null
        assertNotNull(listener.m_result);
        assertNull(listener.m_error);
        CategoryTreePersistenceRequestTest.assertCatTreeEquals(catTree, (CategoryTreeImpl)listener.m_result);
    }
    
    public void testParseBigSuccess() throws IOException, NoSuchElementException, CategoryTreeException, MC2ParserException {
        final CategoryTreeImpl catTree = new CategoryTreeImpl(
                LANG.getId(), POS, CRC, 
                UnitedKingdomTree.m_categoryTable, 
                UnitedKingdomTree.m_lookupTable,
                UnitedKingdomTree.m_stringTable);
        
        String xml = createResponseXml(CRC, 
                UnitedKingdomTree.m_categoryTable, 
                UnitedKingdomTree.m_lookupTable,
                UnitedKingdomTree.m_stringTable);
        
        MC2Parser mc2parser = new MC2ParserImpl(new ByteArrayInputStream(xml.getBytes("UTF-8")), "UTF-8");
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, null, LANG, listener);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
        request.parse(mc2parser);
        
        //we have a result and is different than null
        assertNotNull(listener.m_result);
        assertNull(listener.m_error);
        CategoryTreePersistenceRequestTest.assertCatTreeEquals(catTree, (CategoryTreeImpl)listener.m_result);
    }
    
    public void testParseCrcOK() throws IOException, NoSuchElementException, CategoryTreeException, MC2ParserException {

        MC2Parser mc2parser = new MC2ParserImpl(new ByteArrayInputStream(
                XML_RESPONSE_CRCOK.getBytes("UTF-8")), "UTF-8");
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, null, LANG, listener);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();

        request.parse(mc2parser);
        
        //we have a result and is different than null
        assertTrue(listener.m_doneCalled);
        assertNull(listener.m_result);
        assertNull(listener.m_error);
    }
    
    public void testParseStatus() throws IOException, NoSuchElementException, CategoryTreeException, MC2ParserException {

        MC2Parser mc2parser = new MC2ParserImpl(new ByteArrayInputStream(
                XML_RESPONSE_STATUS.getBytes("UTF-8")), "UTF-8");
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, "", LANG, listener);
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        request.parse(mc2parser);
        
        //we have a result and is different than null
        assertNull(listener.m_result);
        assertNotNull(listener.m_error);
        assertEquals(CoreError.ERROR_SERVER,listener.m_error.getErrorType());
        ServerError error = (ServerError)listener.m_error;
        assertEquals(999,error.getStatusCode());
        assertEquals("http://dummy_url",error.getStatusUri());
        assertEquals("dummy message",error.getInternalMsg());
    }
    
    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreeMC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)}.
     * @throws IOException 
     */
    public void testWrite() throws IOException {
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, null, 
                    LanguageFactory.createLanguageFor(LanguageInternal.FR),
                    listener);
        
        ((CategoryTreeMC2Request) request).updateCrcAndLanguage("8748743883", LANG);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        mc2Writer.startElement(request.getRequestElementName());
        mc2Writer.attribute("transaction_id", "ID0");
        
        request.write(mc2Writer);
        mc2Writer.endElement(request.getRequestElementName());
        //close to flush the data
        mc2Writer.close();
        
        assertFalse(listener.m_doneCalled);
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.toString(); //
        assertEquals(createRequestXml("8748743883",LANG,POS) , xml);
    }

    /**
     * Test method for {@link com.wayfinder.core.search.internal.categorytree.CategoryTreeMC2Request#write(com.wayfinder.core.network.internal.mc2.MC2Writer)}.
     * @throws IOException 
     */
    public void testWriteNoCrc() throws IOException {
        
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new CategoryTreeMC2Request(POS, null, LANG, listener); 
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MC2Writer mc2Writer = new MC2WriterImpl(out, "UTF-8");
        mc2Writer.startElement(request.getRequestElementName());
        mc2Writer.attribute("transaction_id", "ID0");
        request.write(mc2Writer);
        mc2Writer.endElement(request.getRequestElementName());
        //close to flush the data
        mc2Writer.close();
        
        assertFalse(listener.m_doneCalled);
        
        assertTrue("empty", out.size() > 0);
        
        //assume that the default platform encoding is UTF-8
        String xml = out.toString(); //
        assertEquals(createRequestXml("",LANG,POS) , xml);
    }
}
