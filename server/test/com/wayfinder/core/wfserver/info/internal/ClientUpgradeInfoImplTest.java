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

import com.wayfinder.core.wfserver.info.ClientUpgradeInfo;

import junit.framework.TestCase;

public class ClientUpgradeInfoImplTest extends TestCase {

    public void testClientUpgradeInfoImpl() {
        ClientUpgradeInfoImpl upgradeInfo = 
            new ClientUpgradeInfoImpl(true, false, "9.4.0", "market://details?id=com.vodafone.android.navigation");
        
        assertEquals("9.4.0", upgradeInfo.getLatestVersion());
        assertEquals("market://details?id=com.vodafone.android.navigation", upgradeInfo.getUpgradeUri());
        assertFalse(upgradeInfo.isForceUpgrade());
        assertTrue(upgradeInfo.isUpgradeAvailable());
        
        assertNotNull(upgradeInfo.toString());
    }

    public void testGetUpgradeUri() {
        ClientUpgradeInfo upgradeInfo = 
            new ClientUpgradeInfoImpl(true, true, "9.4.0", "http://market.google.com");
        assertEquals(upgradeInfo.getUpgradeUri(), "http://market.google.com");
        assertNotNull(upgradeInfo.toString());
    }

    public void testIsUpgradeAvailable() {
        ClientUpgradeInfoImpl upgradeInfo = 
            new ClientUpgradeInfoImpl(false, false, "9.4.0", null);
        assertFalse(upgradeInfo.isUpgradeAvailable());
        
        assertNotNull(upgradeInfo.toString());
    }

}
