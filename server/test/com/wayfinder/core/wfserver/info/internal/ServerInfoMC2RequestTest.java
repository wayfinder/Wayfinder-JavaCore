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
package com.wayfinder.core.wfserver.info.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.network.internal.mc2.StateMC2RequestListener;
import com.wayfinder.core.network.internal.mc2.impl.MC2ParserImpl;
import com.wayfinder.core.network.internal.mc2.impl.MC2WriterImpl;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.util.WFBase64;
import com.wayfinder.core.shared.util.io.WFStringReader;
import com.wayfinder.core.wfserver.info.ClientUpgradeInfo;

import junit.framework.TestCase;

public class ServerInfoMC2RequestTest extends TestCase {

    private static final String ELEMENT = "server_info_request";


    public static String XML_RESPONSE_STATUS = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><server_info_reply transaction_id=\"ID0\">" +
        "<status_code>999</status_code><status_message>dummy message</status_message>" +
        "<status_uri href=\"http://dummy_url\"/>" + //&#47;&#47;
        "</server_info_reply></isab-mc2>";

    public static String XML_RESPONSE_UPGRADE = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><server_info_reply transaction_id=\"ID0\">" +
        "<client_type_info force_upgrade=\"false\" latest_version=\"9.3.3\" upgrade_available=\"true\" upgrade_id=\"market://details?id=com.vodafone.android.navigation\"/>" +
        "</server_info_reply></isab-mc2>";

    public static String XML_RESPONSE_FORCE = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><server_info_reply transaction_id=\"ID0\">" +
        "<client_type_info force_upgrade=\"true\" latest_version=\"9.5.0\" upgrade_available=\"true\" upgrade_id=\"market://details?id=com.vodafone.android.navigation\"/>" +
        "</server_info_reply></isab-mc2>";

    public static String XML_RESPONSE_NOTFOUND = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?><!DOCTYPE isab-mc2>" +
        "<isab-mc2><server_info_reply transaction_id=\"ID0\">" +
        "<client_type_info force_upgrade=\"false\" upgrade_available=\"false\" />" +
        "</server_info_reply></isab-mc2>";

    public static String XML_REQUEST = 
        "<server_info_request transaction_id=\"ID0\" client_type=\"vf-android\" client_version=\"9.3.0\"/>";
    
    public void testServerInfoMC2Request() {
        StateMC2RequestListener listener = new StateMC2RequestListener();
        try {
            new ServerInfoMC2Request(listener, null, "9.3.0");
            fail("Param checking");
        } catch (IllegalArgumentException e) {
            //success
        }
        
        try {
            new ServerInfoMC2Request(listener, "android_vf", null);
            fail("Param checking");
        } catch (IllegalArgumentException e) {
            //success
        }
    }

    public void testError() {
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        ServerInfoMC2Request request = new ServerInfoMC2Request(listener, "android_vf", "9.4.0");
        
        CoreError error = new CoreError("something");
        request.error(error);
        
        assertSame(error, listener.m_error);
        assertNull(listener.m_result);
    }

    public void testGetRequestElementName() {
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        ServerInfoMC2Request request = new ServerInfoMC2Request(listener, "", "");
        
        assertEquals(ELEMENT, request.getRequestElementName());
        
        assertFalse(listener.m_doneCalled);
    }

    public void testParseStatus() throws UnsupportedEncodingException, 
            IOException, IllegalStateException, MC2ParserException {
        
        MC2Parser mc2parser = new MC2ParserImpl(new WFStringReader(XML_RESPONSE_STATUS));
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new ServerInfoMC2Request(listener, "vf-android", "9.3.0");
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        request.parse(mc2parser);
        
        assertNull(listener.m_result);
        assertNotNull(listener.m_error);
        assertEquals(CoreError.ERROR_SERVER,listener.m_error.getErrorType());
        ServerError error = (ServerError)listener.m_error;
        assertEquals(999,error.getStatusCode());
        assertEquals("http://dummy_url",error.getStatusUri());
        assertEquals("dummy message",error.getInternalMsg());
    }
    
    public void testParseUpgrade() throws UnsupportedEncodingException, 
            IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(new WFStringReader(XML_RESPONSE_UPGRADE));
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new ServerInfoMC2Request(listener, "vf-android", "9.3.0");
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        request.parse(mc2parser);
        
        assertNotNull(listener.m_result);
        assertNull(listener.m_error);
        
        ClientUpgradeInfo info = (ClientUpgradeInfo)listener.m_result;
        assertEquals("market://details?id=com.vodafone.android.navigation",info.getUpgradeUri());
        assertFalse(info.isForceUpgrade());
        assertEquals("9.3.3",info.getLatestVersion());
    }

    
    public void testParseUpgradeForce() throws UnsupportedEncodingException, 
            IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(new WFStringReader(XML_RESPONSE_FORCE));
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new ServerInfoMC2Request(listener, "vf-android", "9.3.0");
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        request.parse(mc2parser);
        
        assertNotNull(listener.m_result);
        assertNull(listener.m_error);
        
        ClientUpgradeInfo info = (ClientUpgradeInfo)listener.m_result;
        assertEquals("market://details?id=com.vodafone.android.navigation",info.getUpgradeUri());
        assertTrue(info.isForceUpgrade());
        assertEquals("9.5.0",info.getLatestVersion());
    }

    
    public void testParseNotFound() throws UnsupportedEncodingException, 
            IOException, IllegalStateException, MC2ParserException {
        MC2Parser mc2parser = new MC2ParserImpl(new WFStringReader(XML_RESPONSE_NOTFOUND));
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new ServerInfoMC2Request(listener, "vf-android", "9.3.0");
        
        mc2parser.childrenOrError();
        mc2parser.childrenOrError();
  
        request.parse(mc2parser);
        
        assertNull(listener.m_result);
        assertNull(listener.m_error);
        
        assertTrue(listener.m_doneCalled);
    }    
    
    
    public void testWrite() throws IOException {
        StateMC2RequestListener listener = new StateMC2RequestListener();
        
        MC2Request request = new ServerInfoMC2Request(listener, "vf-android", "9.3.0");
        
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
        String xml = out.toString();
        assertEquals(XML_REQUEST , xml);
        
    }
}
