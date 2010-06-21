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
package com.wayfinder.core.network.internal.xscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

public abstract class XsDataCodedStreamTemplate extends TestCase {
    
    private static final String MESSAGE = "This is an xs test";
    
    //-------------------------------------------------------------------------
    // standard stream operations
    
    public void testStreams() {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XsDataCodedOutputStream xsStream = createOutputStream(baos);
            xsStream.write(MESSAGE.getBytes());
            xsStream.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        try {
            XsDataCodedInputStream instream = 
                new XsDataCodedInputStream(new ByteArrayInputStream(baos.toByteArray()));
            for (int i = instream.read(); i != -1; i = instream.read()) {
                resultStream.write(i);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
        
        String newString = new String(resultStream.toByteArray());
        assertEquals(MESSAGE, newString);
    }
    

    //-------------------------------------------------------------------------
    // helper
    
    
    protected abstract XsDataCodedOutputStream createOutputStream(OutputStream stream);
    
    
}
