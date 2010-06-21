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
package com.wayfinder.core.search.internal.categorytree;

import java.io.IOException;
import java.io.Reader;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2RequestAdapter;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.util.io.WFBase64ReaderInputStream;
import com.wayfinder.core.shared.xml.XmlIterator;

/**
 * Represent a xml request for checking and getting new category tree
 * depending on position, language and previous category tree crc 
 * 
 * 
 */
public class CategoryTreeMC2Request extends MC2RequestAdapter {

    private static final Logger LOG = 
        LogFactory.getLoggerForClass(CategoryTreeMC2Request.class);
    
    public static final int SERVER_VERSION = 1;
    
    private Position m_position;
    private String m_previousCrc; 
    private LanguageInternal m_language;
    
    /**
     * @param pos must be not null
     * @param previousCrc previous category tree crc if any null or empty otherwise 
     * @param language cannot be null
     * @param listener cannot be null
     */
    public CategoryTreeMC2Request(Position position, String previousCrc, 
            LanguageInternal language, MC2RequestListener listener) {
        super(listener);
        m_position = position;
        m_previousCrc = previousCrc;
        m_language = language;
    }   
    
    public void updateCrcAndLanguage(String previousCrc, LanguageInternal language) {
        m_previousCrc= previousCrc;
        m_language = language;
    }

    /* (non-Javadoc)
     * @see MC2Request#getRequestElementName() 
     */
    public String getRequestElementName() {
        return MC2Strings.tlocal_category_tree_request;
    }

    /* (non-Javadoc)
     * @see MC2Request#parse(MC2Parser)
     */
    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        /*xml 2.2.2
        <!ELEMENT local_category_tree_reply ( (category_table, lookup_table,
                string_table)|crc_ok|( status_code,
                status_message, status_uri?,
                status_code_extended? ) ) >
                <!ATTLIST local_category_tree_reply transaction_id ID #REQUIRED
                crc CDATA #IMPLIED >*/
                 
        mc2p.nameOrError(MC2Strings.tlocal_category_tree_reply);

        //read this here even it could be null 
        //set it only after the parsing was complete
        String serverCrc = mc2p.attribute(MC2Strings.acrc);
        
        //must be have result ,crc_ok or status
        mc2p.childrenOrError();

        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
        } else if (mc2p.nameRefEq(MC2Strings.tcrc_ok)) {
            //crc check and no changes
            result(null);
            mc2p.advance();// advance to parent
        } else {
            /*<!ELEMENT category_table ( #PCDATA ) >
            <!ATTLIST category_table length %number; #REQUIRED >
            <!ELEMENT lookup_table ( #PCDATA ) >
            <!ATTLIST lookup_table length %number; #REQUIRED >
            <!ELEMENT string_table ( #PCDATA ) >
            <!ATTLIST string_table length %number; #REQUIRED >*/
            /*category_table  Node that contains the binary
            category table. The data is
            Base64 encoded.
            lookup_table
            Node that contains the binary
            lookup table. The data is Base64
            encoded.
            string_table
            Node that contains the binary
            string table. The data is Base64
            encoded.
            length integer Number of items in the table. Used to improve 
                allocation performance on client.
            crc string A checksum calculated of the entire tree. 
            The crc attribute is only avaiable if the request
            completes successfully.*/
            
            mc2p.nameOrError(MC2Strings.tcategory_table);
            byte[] categoryTable = parseBASE64Value(mc2p);
            
            mc2p.advanceOrError();
            mc2p.nameOrError(MC2Strings.tlookup_table);
            byte[] lookupTable = parseBASE64Value(mc2p);
            
            
            mc2p.advanceOrError();
            mc2p.nameOrError(MC2Strings.tstring_table);
            byte[] stringTable = parseBASE64Value(mc2p);
            
            //yeah success... build the tree and notify the listener
            CategoryTreeImpl tree = new CategoryTreeImpl(
                    m_language.getId(), m_position, serverCrc, 
                    categoryTable, lookupTable, stringTable);
            result(tree);
            
            mc2p.advance();// advance to parent
        }
        //final check that we parsed correctly
        mc2p.nameOrError(MC2Strings.tlocal_category_tree_reply);
    }
    
    /** 
     * utility method that decode base64 from element with text 
     * 
     * @param xpi the xml interator positioned on an element with base64 text.
     * and having a length attribute which represent the size of the decoded 
     * data.   
     * @return a byte array containing the parsed data
     * @throws IllegalStateException
     * @throws IOException
     * @throws MC2ParserException 
     */
    private static byte[] parseBASE64Value(XmlIterator xpi) throws IllegalStateException, IOException, MC2ParserException {
//  old version that use a big temp memory allocation  base64.length * 2 bytes  
//        String base64value = xpi.value();
//        if (base64value != null) {
//            //another temp memory allocation
//            byte[] base64 = base64value.getBytes();
//            return WFBase64.decode(base64);
//        } else {
//            return new byte[0];
//        }
        
        Reader reader = xpi.valueAsReader();
        if (reader != null) {
            int length = xpi.attributeCharArray(MC2Strings.alength).intValue();
            byte[] rez = new byte[length];
            if (rez.length != new WFBase64ReaderInputStream(reader).read(rez)){
                throw new MC2ParserException("Size mismatch invalid length or content for Base64");
            }
            reader.close();
            return rez;
        } else {
            return new byte[0];
        }
    }

    /* (non-Javadoc)
     * @see MC2Request#write(MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        /* xml 2.2.2
        <!ELEMENT local_category_tree_request ( position_item ) >
        <!ATTLIST local_category_tree_request transaction_id ID #REQUIRED
        crc CDATA #REQUIRED
        language %language_t; #REQUIRED
        version %number; #REQUIRED > 
        crc string  The crc from a previous local_category_tree_reply. Send
        empty attribute if no crc is available.
        language string The language in which the category names should be in. 
        version integer The version to use in the reply.*/
        
        if (m_previousCrc != null) {
            mc2w.attribute(MC2Strings.acrc, m_previousCrc);
        } else {
            //we need to set it even dosen't exist
            mc2w.attribute(MC2Strings.acrc, "");
        }
        mc2w.attribute(MC2Strings.alanguage, m_language.getXMLCode());
        mc2w.attribute(MC2Strings.aversion, SERVER_VERSION);
        if(m_position != null ) { 
            //let server complain if is not valid && m_position.isValid()
            m_position.write(mc2w);
        }
    }
}
