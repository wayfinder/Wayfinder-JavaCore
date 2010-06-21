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
package com.wayfinder.core.network.internal.mc2.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import com.wayfinder.core.ModuleData;
import com.wayfinder.core.shared.internal.InternalUser;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.xml.XmlWriterImpl;
import com.wayfinder.core.userdata.internal.InternalUserDataInterface;
import com.wayfinder.core.userdata.internal.hwkeys.HardwareKey;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;

final public class MC2WriterImpl extends XmlWriterImpl implements MC2Writer  {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(MC2WriterImpl.class);
    
    public MC2WriterImpl(OutputStream outstream, String encoding) throws IOException {
        super();
        setOutput(outstream, encoding);
    }
    
    
    /**
     * starts a new document and writes the <?xml-header,
     * DOCTYPE-header, the isab_mc2 root element, the element auth and
     *
     * This is used for all "normal" requests. Activation requires
     * special handling and use the method
     * writeActivationDocumentStart() instead
     * @param usrDatIfc 
     * @param isActivation true for activation request; 
     *        false for normal requests
     */
    void writeDocumentStart(SharedSystems systems,
            ModuleData modData, Enumeration attribEn, 
            InternalUserDataInterface usrDatIfc, boolean isActivation) throws IOException {

        // J2SE 1.4 Boolean.valueOf() is not in MIDP
        startDocument(null, null);
        docdecl(MC2Strings.tisab_mc2);

        startElement(MC2Strings.tisab_mc2);

        // auth element
        /*<!ELEMENT auth ( (auth_user, auth_passwd, user_service?) |
                (user_session_id, user_session_key, user_service) |
                auth_activate_request |
                (uin, auth_token) |
                (uin?, hardware_key+) )>
                
         <!ATTLIST auth indentingandlinebreaks %bool; "true"
            development %bool; "false"
            client_type CDATA #IMPLIED
            client_lang %language_t; #IMPLIED
            server_list_crc CDATA #IMPLIED
            server_auth_bob_crc CDATA #IMPLIED
            client_source CDATA #IMPLIED > */
        
        startElement(MC2Strings.tauth);

        if(LOG.isDebug()) {
            // add server logging if the build was made for debug
            // otherway the default value is false so I skip the attribute
            attribute(MC2Strings.adevelopment, true);
        }
        
        // indicates whether ignorable whitespace should be added to the reply
        // to make it more human readable. If false, the reply becomes smaller
        // and faster to parse
        attribute(MC2Strings.aindentingandlinebreaks, false);
        // not checked by server for external auth but it might be
        // done in the future
        attribute(MC2Strings.aclient_type, 
                           modData.getServerData().getClientType());
        
        attribute(MC2Strings.aclient_lang, 
                 systems.getSettingsIfc().getGeneralSettings()
                 .getInternalLanguage().getXMLCode());
        
        while(attribEn.hasMoreElements()) {
            MC2IsabAttribute attrib = (MC2IsabAttribute) attribEn.nextElement();
            attribute(attrib.getAttribute(), attrib.getValue());
        }
        
        if (isActivation) {
            startElement(MC2Strings.tauth_activate_request);
            endElement(MC2Strings.tauth_activate_request);
        } else {
            InternalUser usr = usrDatIfc.getInternalUser();
            if(usr.isActivated()) {
                elementWithText(MC2Strings.tuin, usr.getUIN());
            }
            Enumeration keyEnum = usr.getHardwareKeys();
            while(keyEnum.hasMoreElements()) {
                HardwareKey key = (HardwareKey) keyEnum.nextElement();
                startElement(MC2Strings.thardware_key);
                attribute(MC2Strings.atype, key.getKeyXMLType());
                text(key.getKey());
                endElement();
            }
        }
        // write_platform_property_elements();
        endElement(MC2Strings.tauth);
    } // writeDocumentStart
    
    
    /**
     * start a requerts element and write set his transaction_id attribute 
     * @param requestElementName the root element of the request (request_...)
     * @param transactionID the transaction ID of this request 
     * 
     * @throws IOException
     */
    void write_request_start(
            String requestElementName, int transactionID) throws IOException {
        
        startElement(requestElementName);
        attribute(MC2Strings.atransaction_id, "ID" + transactionID);        
    } // write_request_start
    
    
    void closeToDepth(int depth) throws IOException {
        for (int i = getDepth(); i > depth; i = getDepth()) {
            endElement();
        }
    }
    
    /**
     * close the document 
     * @throws IOException 
     */
    void writeDocumentEnd() throws IOException {
        endDocument();
        // has to close here to transmit the gzip footer
        close();
    }


    //-------------------------------------------------------------------------
    // convenience methods
    
  


    public void elementWithText(String aElementName, String aText)
    throws IOException {

        startElement(aElementName);
        text(aText);
        endElement();
    } // writeSimpleTextElement


    public void elementWithText(String aElementName, int aIntValue)
    throws IOException {
        startElement(aElementName);
        text(aIntValue);
        endElement();
    } // writeSimpleIntElement

}
