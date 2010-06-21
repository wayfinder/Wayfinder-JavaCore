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

package com.wayfinder.core.search.internal;

import junit.framework.TestCase;

/**
 * Tests that {@link OneListSearchMatchImpl} does not allow null parameters
 * to its constructor.
 */
public class OneListSearchMatchImpNullCtorlTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        // don't create any object for testing - each test method needs
        // to call the constructor.
    }

    /**
     * Tests that the constructor does not allow null matchID. 
     */
    public final void testNullCtorParam1() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        null,
                        BasicSearchMatchImplTest.MATCH_LOCATION,
                        BasicSearchMatchImplTest.MATCH_NAME,
                        BasicSearchMatchImplTest.MATCH_POSITION,
                        OneListSearchMatchImplTest.BRAND_IMAGE_NAME,
                        OneListSearchMatchImplTest.CATEGORY_IMAGE_NAME,
                        OneListSearchMatchImplTest.PROVIDER_IMAGE_NAME);
                fail("OneListSearchMatchImpl ctor must not allow null matchID");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null matchLocation. 
     */
    public final void testNullCtorParam2() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        BasicSearchMatchImplTest.MATCH_ID,
                        null,
                        BasicSearchMatchImplTest.MATCH_NAME,
                        BasicSearchMatchImplTest.MATCH_POSITION,
                        OneListSearchMatchImplTest.BRAND_IMAGE_NAME,
                        OneListSearchMatchImplTest.CATEGORY_IMAGE_NAME,
                        OneListSearchMatchImplTest.PROVIDER_IMAGE_NAME);
                fail("OneListSearchMatchImpl ctor must not allow null matchLocation");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null matchName. 
     */
    public final void testNullCtorParam3() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        BasicSearchMatchImplTest.MATCH_ID,
                        BasicSearchMatchImplTest.MATCH_LOCATION,
                        null,
                        BasicSearchMatchImplTest.MATCH_POSITION,
                        OneListSearchMatchImplTest.BRAND_IMAGE_NAME,
                        OneListSearchMatchImplTest.CATEGORY_IMAGE_NAME,
                        OneListSearchMatchImplTest.PROVIDER_IMAGE_NAME);
                fail("OneListSearchMatchImpl ctor must not allow null matchName");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null position. 
     */
    public final void testNullCtorParam4() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        BasicSearchMatchImplTest.MATCH_ID,
                        BasicSearchMatchImplTest.MATCH_LOCATION,
                        BasicSearchMatchImplTest.MATCH_NAME,
                        null,
                        OneListSearchMatchImplTest.BRAND_IMAGE_NAME,
                        OneListSearchMatchImplTest.CATEGORY_IMAGE_NAME,
                        OneListSearchMatchImplTest.PROVIDER_IMAGE_NAME);
                fail("OneListSearchMatchImpl ctor must not allow null position");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null brandImageName. 
     */
    public final void testNullCtorParam5() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        BasicSearchMatchImplTest.MATCH_ID,
                        BasicSearchMatchImplTest.MATCH_LOCATION,
                        BasicSearchMatchImplTest.MATCH_NAME,
                        BasicSearchMatchImplTest.MATCH_POSITION,
                        null,
                        OneListSearchMatchImplTest.CATEGORY_IMAGE_NAME,
                        OneListSearchMatchImplTest.PROVIDER_IMAGE_NAME);
                fail("OneListSearchMatchImpl ctor must not allow null brandImageName");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null categoryImageName. 
     */
    public final void testNullCtorParam6() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        BasicSearchMatchImplTest.MATCH_ID,
                        BasicSearchMatchImplTest.MATCH_LOCATION,
                        BasicSearchMatchImplTest.MATCH_NAME,
                        BasicSearchMatchImplTest.MATCH_POSITION,
                        OneListSearchMatchImplTest.BRAND_IMAGE_NAME,
                        null,
                        OneListSearchMatchImplTest.PROVIDER_IMAGE_NAME);
                fail("OneListSearchMatchImpl ctor must not allow null categoryImageName");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests that the constructor does not allow null providerImageName. 
     */
    public final void testNullCtorParam7() {
        try {
            OneListSearchMatchImpl o =
                new OneListSearchMatchImpl(
                        BasicSearchMatchImplTest.MATCH_ID,
                        BasicSearchMatchImplTest.MATCH_LOCATION,
                        BasicSearchMatchImplTest.MATCH_NAME,
                        BasicSearchMatchImplTest.MATCH_POSITION,
                        OneListSearchMatchImplTest.BRAND_IMAGE_NAME,
                        OneListSearchMatchImplTest.CATEGORY_IMAGE_NAME,
                        null);
                fail("OneListSearchMatchImpl ctor must not allow null providerImageName");
                                            
        } catch (IllegalArgumentException e) {
        }
    }

}
