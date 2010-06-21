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
 * Copyright, Wayfinder Systems AB, 2010
 */

package com.wayfinder.core.search.internal.categorytree;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;

/**
 * Tests the basic parts of {@link CategoryTreeImpl} that don't depend
 * on the actual category data.
 */
public class CategoryTreeImplBasicTest extends TestCase {

    private final Position POSITION = new Position(0,0);
    private final String CRC = "crc";

    private CategoryTreeImpl m_ct;

    protected void setUp() throws Exception {
        super.setUp();
        m_ct = new CategoryTreeImpl(Language.EN_UK,
                                    POSITION, CRC,
                                    MinimalTree.m_categoryTable,
                                    MinimalTree.m_lookupTable,
                                    MinimalTree.m_stringTable);
    }

    // ---------------------------------------------------------------------
    // test of the basic things that don't depend on the data format

    public void testPosition() {
        assertSame(POSITION, m_ct.getPosition());
    }

    public void testCrc() {
        assertEquals(CRC, m_ct.getCrc());
    }

    public void testLanguage() {
        assertEquals(Language.EN_UK, m_ct.getLanguageId());
    }

    public void testtoString() {
        String s = m_ct.toString();
        assertNotNull(s);
        assertTrue("Length of CategoryTreeImpl.toString() should not be 0.",
                   s.length() > 0);
    }    

    /**
     * Tests that the correct exception is thrown when data in a different
     * version of the format is used to try to deserialize an instance of
     * CategoryTreeImpl.
     * 
     * @throws IOException if there is an IO-error during writing of testdata.
     */
    public void testReadingOfWrongVersion()
        throws IOException {

        WFByteArrayOutputStream baos =
            new WFByteArrayOutputStream(500);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(CategoryTreeImpl.VERSION + 1);

        DataInputStream dis = new DataInputStream(
                new ByteArrayInputStream(baos.getByteArray()));
        try {
            CategoryTreeImpl.read(dis);
            fail("CategoryTreeImpl.read() did not check the version of stored data.");
        } catch (IOException e) {
            // OK
        }
        
    }
}
