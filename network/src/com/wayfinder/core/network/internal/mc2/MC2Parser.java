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
package com.wayfinder.core.network.internal.mc2;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldImpl;
import com.wayfinder.core.shared.xml.XmlIterator;

public interface MC2Parser extends XmlIterator {

    boolean validationEnabled();

    /**
     * gets attribute with name attributeName as an int value
     * If the attribute is empty or not a
     * valid number MC2ParserException is thrown.
     * @throws IOException 
     * @throws IllegalStateException 
     * @throws MC2ParserException 
     */
    int attributeAsInt(String attributeName) throws IllegalStateException,
            IOException, MC2ParserException;

    /**
     * Get the value of current element as an int.
     * If the value is empty or is not a valid number a MC2ParserException is 
     * thrown except if the value is "inf", in which case -1 is returned.
     * @return -1 or a number 
     * @throws MC2ParserException
     * @throws IOException
     */
    int valueAsInt() throws MC2ParserException, IOException;

    /**
     * Check is a reply has a status element instead of real result
     * 
     * @return
     * @throws IllegalStateException
     * @throws IOException
     * @throws MC2ParserException
     */
    ServerError getErrorIfExists() throws IllegalStateException, IOException,
            MC2ParserException;

    void advanceOrError() throws IllegalStateException, IOException,
            MC2ParserException;

    void childrenOrError() throws IllegalStateException, IOException,
            MC2ParserException;

    void nameOrError(String expectedElement) throws IllegalStateException,
            IOException, MC2ParserException;
    
    /**
     * Parse an info_field element. 
     * The parser must be positioned at that element.
     *  
     * @return a {@link InfoFieldImpl}
     * @throws IllegalStateException
     * @throws IOException
     * @throws MC2ParserException
     */
    InfoFieldImpl parseInfoField() throws IllegalStateException,
            IOException, MC2ParserException;

    
    /**
     * Parse a detail_item element
     * @return a {@link DetailFieldListImpl}
     * @throws IllegalStateException
     * @throws IOException
     * @throws MC2ParserException
     */
    PoiDetailImpl parseDetailItem() throws IllegalStateException,
            IOException, MC2ParserException;

    boolean attributeAsBoolean(String attributeName) 
            throws IllegalStateException, IOException;
}
