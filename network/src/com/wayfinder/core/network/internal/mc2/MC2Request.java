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

import com.wayfinder.core.shared.error.CoreError;

/**
 * Represent an IsabMC2 XML request. 
 * Actual writing and parsing methods needs to be implemented     
 * 
 *
 */
public interface MC2Request {
    
    /**
     * @return the request element name that will be started before calling
     * {@link #write(MC2Writer)} 
     */
    String getRequestElementName();
    
    /**
     * Called when the module is ready to send the request.
     * The request element is started and the transaction ID attribute is 
     * already written
     * 
     * <p>Note: Take care to close any elements that you open. 
     * But do not close the request element </p> 
     * 
     * @param mc2w the {@link MC2Writer} where to write the content of the 
     * request element
     */
    void write(MC2Writer mc2w) throws IOException;
    
    /**
     * Called in case of success, the parser is positioned at the beginning of 
     * the reply element.
     *
     * TODO: document requirements on the parser - seems ok to stop parsing
     * once required data has been retrieved and caller will fix closing open
     * elements to the right level for next request.
     * 
     * TODO: is the parse() method required to be re-entrant? This has
     * consequences for if the requests that fail to parse can be retried or
     * not.
     *
     * @param mc2p the {@link MC2Parser} from where to parse the content of the 
     * response element
     */
    void parse(MC2Parser mc2p) throws MC2ParserException, IOException;
    
    /**
     * Called in case of failure this can be for 3 reasons;
     * <li> server general error IsabError status code   
     * <li> network error caused by the underlying network layer NetworkError
     * <li> exception unexpected error or bad configuration UnexpectedError
     *  
     * @param status
     */
    void error(CoreError coreError);
    
}
