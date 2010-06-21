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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldImpl;
import com.wayfinder.core.shared.poidetails.DetailField;
import com.wayfinder.core.shared.util.CharArray;
import com.wayfinder.core.shared.xml.LightXmlPullIterator;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2StatusListener;
import com.wayfinder.core.network.internal.mc2.MC2Strings;

final public class MC2ParserImpl extends LightXmlPullIterator implements MC2Parser  {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(MC2ParserImpl.class);

    /**
     * Creates a new MC2ParserImpl which reads from a stream with an (optional) 
     * specified encoding.
     * 
     * @param inStream the stream to read from.
     * 
     * @param enc the name of the encoding used to convert bytes from the
     * stream to characters.
     * See {@link InputStreamReader#InputStreamReader(InputStream, String)}
     * for details.<br>
     * If enc is null, the charset will be detected from
     * the XML PI (with UTF-8 as fall-back) according to the rules of
     * <i>Extensible Markup Language (XML) 1.0 (Third Edition), appendix F.1</i>. 
     * 
     * @throws IOException if an I/O error occurs.
     */
    public MC2ParserImpl(InputStream inStream, String enc) throws IOException {
        super(MC2ParserEntities.elementNameArray,
                MC2ParserEntities.attributeNameArray);
        /*
         * don't construct the Reader yourself and call MC2ParserImpl(Reader)
         * - it would circumvent the auto-detection routine in
         * com.wayfinder.core.shared.xml.XmlReader.setInput(InputStream inStream, String enc).
         */
        super.setInput(inStream, enc);
    }

    /**
     * <p>Creates a new MC2ParserImpl which reads from a {@link Reader}.</p>
     * 
     * <p>This can be used when the encoding is already known by some other
     * mechanism or when testing and you want to avoid the overhead of
     * creating bytes from strings and reading the bytes from a
     * ByteArrayInputStream.</p>
     *  
     * @param inReader the reader to read from.
     * @throws IOException if an I/O error occurs.
     */
    public MC2ParserImpl(Reader inReader) throws IOException {
        super(MC2ParserEntities.elementNameArray,
                MC2ParserEntities.attributeNameArray);
        super.setInput(inReader);
    }


    public boolean validationEnabled() {
        return LOG.isDebug();
    }

    ServerError parseDocumentStart(MC2Storage storage) throws IOException, MC2ParserException {
        if (!(children() && children())) { 
            // JLS 15.23, right
            // not evaluated if
            // left is false
            //root missing or root has no children
            throw new MC2ParserException("Document empty");
        }
        
        if (nameRefEq(MC2Strings.tstatus_code)) {
            if(LOG.isError()) {
                LOG.error("MC2ParserImpl.parseDocumentStart()", 
                          "Response document contains status code");
            }
            
            final int status_code = valueAsInt();
            advance();
            String status_message = value();
            String status_uri = null;
            MC2StatusListener[] listeners = storage.getMC2IsabStatusListeners();
            while (advance()) {
                if (nameRefEq(MC2Strings.tstatus_uri)) {
                    /*<!ELEMENT status_uri EMPTY>
                    <!ATTLIST status_uri href %HREF; #REQUIRED >
                    An optional URI that can be used to present the error to the user.*/
                    status_uri = attribute(MC2Strings.ahref);
                } else {
                    for (int i = 0; i < listeners.length; i++) {
                        if(listeners[i].handleDocumentStatusElement(name())) {
                            listeners[i].parseDocumentStatusInfo(this);
                            continue;
                        }
                    }
                }
                
                /*
                else if (m_xpi.name() == MC2Strings.tauth_token) {
                    new_auth_token = m_xpi.value();
                } else if (m_xpi.name() == MC2Strings.tserver_list) {
                    parse_server_list();
                    seenserverlist = true;
                } else if (m_xpi.name() == MC2Strings.tserver_auth_bob) {
                    parse_server_auth_bob();
                    seenauthbob = true;
                } */
            } // while
            
            if(LOG.isError()) {
                LOG.error("MC2ParserImpl.getErrorIfExists()", 
                        "Found status code: " + status_code + "(" + status_message + ")");
            }
            return new ServerError(status_code, status_message, status_uri);
        }
        return null; // OK!
    }


    int getNextTransactionID() throws IllegalStateException, IOException, MC2ParserException {
        CharArray ca =  attributeCharArray(MC2Strings.atransaction_id);
        if (ca == null) {
            return Integer.MIN_VALUE;
        }
        //remove first two char ID
        return MC2Strings.number_type(ca.subCharArray(2));
    }

    void advanceToDepth(int docDepth) throws IllegalStateException, IOException {
        do {
            advance();
        } while((getDepth() > docDepth) && (getDepth() > 0));
    }

    public ServerError getErrorIfExists() throws IllegalStateException, IOException, MC2ParserException {
        /**
         * requires that the current event is START_TAG. If the parser is
         * positioned on a status_code-element, that element and the
         * following status_message-element are parsed, istatus_code and
         * istatus_message are updated, iParsingResult is set to
         * PARSING_ONLY_STATUS (if we want to deal with user_login_reply,
         * it should be noted that status elements are always contained in
         * that element and followed by data) true is returned.
         * 
         * Otherwise the parser is not advanced and false is returned.
         *
         * BUG: Parts of DTD define e.g. <!ELEMENT route_reply ... |
         * (status_code, status_message, status_code_extended?) > But if
         * the status_code_extended is actually sent, the parser will be
         * misaligned, since it expects exactly two elements. This also
         * means that a status_uri element cannot be added. MC2 team will
         * mark the status_code_extended as not to be used.
         *
         * Parsing document start is not affected, since it does not use
         * this method.
         */
        // findbugs hit here, strings ARE compared using == for performance
        if (nameRefEq(MC2Strings.tstatus_code)) {
            final int status_code = valueAsInt();
            advance();
            String status_message = value();
            String status_uri = null;
            while (advance()) {
                if (nameRefEq(MC2Strings.tstatus_uri)) {
                    /*<!ELEMENT status_uri EMPTY>
                    <!ATTLIST status_uri href %HREF; #REQUIRED >
                    An optional URI that can be used to present the error to the user.*/
                    status_uri = attribute(MC2Strings.ahref);
                } 
            } // while
            
            if(LOG.isError()) {
                LOG.error("MC2ParserImpl.getErrorIfExists()", 
                        "Found status code: " + status_code + "(" + status_message + ")");
            }
            return new ServerError(status_code, status_message, status_uri);
        } else {
            return null;
        }
    }

    //-------------------------------------------------------------------------
    // helper methods


    public int valueAsInt() throws MC2ParserException, IOException {
        CharArray ca = valueCharArray();

        if (ca == null) {
            throw new MC2ParserException("No value for current element");
        }
        if (ca.equals("inf")) {
            return -1;
        }
        try {
           return ca.intValue();
        } catch (NumberFormatException e) {
           throw new MC2ParserException("Invalid number value");
        }
    } // valueInt


    public int attributeAsInt(String attributeName) throws IllegalStateException, IOException, MC2ParserException {
        CharArray ca = attributeCharArray(attributeName);
        if (ca == null) {
            throw new MC2ParserException("No attribute " + attributeName);
        }
        try {
            return ca.intValue();
        } catch (NumberFormatException e) {
            throw new MC2ParserException("Invalid number value for attribute " + attributeName);
        }
    } // attributeInt

    public void advanceOrError() throws IllegalStateException, IOException, MC2ParserException  {
        if (!advance()) {
            throw new MC2ParserException("More children expected for " + name());
        }
    }

    public void childrenOrError() throws IllegalStateException, IOException, MC2ParserException  {
        if (!children()) {
            throw new MC2ParserException("Children expected for " + name());
        }
    }


    public void nameOrError(String expectedElement)
            throws IllegalStateException, IOException, MC2ParserException {
        // findbugs hit here, strings ARE compared using == for performance
        if (! nameRefEq(expectedElement)) {
            throw new MC2ParserException("Expected element " + expectedElement + " found " + name());
        }
        
    }

    public InfoFieldImpl parseInfoField() throws IllegalStateException,
            IOException, MC2ParserException {
        // <!ELEMENT info_field ( fieldName, fieldValue ) >
        // <!ATTLIST info_field info_type %poi_info_t; #IMPLIED >
        //we have info fields
        int type = InfoFieldImpl.getTypeForString(
                attributeCharArray(MC2Strings.ainfo_type));

        
        /* BUG: if definition is changed to
         * info_field (fieldName, fieldValue, foo)
         * the call below will position us at foo
         * instead of at end-tag for info_field.
         * 
         * The next call to advance() will return false
         * and the outer loop will terminate - thus we will
         * only parse the first element.
         * 
         * while (mc2p.advance()); ?
         */
        
        childrenOrError();
        // should be at fieldName
        String key = value();
        advanceOrError();
        // should be at fieldValue
        String value = value();
        // exit from info_field childrens if there more  
        while(advance());
        nameOrError(MC2Strings.tinfo_field);
        
        return new InfoFieldImpl(type, key, value);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2Parser#parseDetailItem()
     */
    public PoiDetailImpl parseDetailItem() throws IllegalStateException,
            IOException, MC2ParserException {
        PoiDetailImpl poiDetail = new PoiDetailImpl();
        
        if (children()) {
            do {
                if (nameRefEq(MC2Strings.tdetail_field)) {
                    int type = PoiDetailImpl.getTypeForString(
                            attributeCharArray(MC2Strings.adetail_type));
                    int contentType = DetailField.getContentTypeForString(
                            attributeCharArray(MC2Strings.adetail_content_type));
                    childrenOrError();
                    // should be at fieldName
                    String name = value();
                    advanceOrError();
                    // should be at fieldValue
                    String value = value();
                    // exit from detail_field children if there are more  
                    while(advance());
                    poiDetail.setField(type, new DetailField(name, value, contentType));
                }
            }
            while (advance());
        }
        nameOrError(MC2Strings.tdetail_item);
        return poiDetail;
    }

    public boolean attributeAsBoolean(String attributeName) throws IllegalStateException, IOException {
        CharArray value = attributeCharArray(attributeName);
        if (value != null) {
            return value.booleanValue();
        }
        return false;
    }

}
