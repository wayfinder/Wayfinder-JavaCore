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
package com.wayfinder.core.network;

import java.util.Random;

import com.wayfinder.core.network.ServerError;

import junit.framework.TestCase;

public class ServerErrorTest extends TestCase {
    
    private final long RAND_SEED = 12349202L;
    
    public void testMC2Error() {
        new ServerError(200, "OK", "No this isn't really http xD");
    }

    public void testGetStatusCode() {
        Random rand = new Random(RAND_SEED); // hehe
        for(int i = 0; i < 10; i++) {
            final int code = rand.nextInt();
            ServerError error = new ServerError(code, "", "");
            assertEquals(code, error.getStatusCode());
        }
    }

    public void testGetStatusUri() {
        final String uri = "http://devserver/surprise.html";
        ServerError error = new ServerError(200, "", uri);
        assertEquals(uri, error.getStatusUri());
    }

    public void testGetInternalMsg() {
        final String message = "Oh noes, someone messed up";
        ServerError error = new ServerError(200, message, "");
        assertEquals(message, error.getInternalMsg());
    }

}
