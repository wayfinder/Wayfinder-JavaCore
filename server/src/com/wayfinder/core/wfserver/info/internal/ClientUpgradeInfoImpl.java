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

public class ClientUpgradeInfoImpl implements ClientUpgradeInfo {

    final boolean m_upgradeAvailable;
    final boolean m_forceUpgrade;
    final String m_latestVersion;
    final String m_upgradeUri;
    
    public ClientUpgradeInfoImpl(boolean upgradeAvailable, boolean forceUpgrade,
            String latestVersion, String upgradeUri) {
        super();
        m_upgradeAvailable = upgradeAvailable;
        m_forceUpgrade = forceUpgrade;
        m_latestVersion = latestVersion;
        m_upgradeUri = upgradeUri;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.info.ClientUpgradeInfo#isForceUpgrade()
     */
    public boolean isForceUpgrade() {
        return m_forceUpgrade;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.info.ClientUpgradeInfo#getLatestVersion()
     */
    public String getLatestVersion() {
        return m_latestVersion;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.wfserver.info.ClientUpgradeInfo#getUpgradeUri()
     */
    public String getUpgradeUri() {
        return m_upgradeUri;
    }
    
    public boolean isUpgradeAvailable() {
        return m_upgradeAvailable;
    }

    public String toString() {
        return "ClientUpgradeInfoImpl("
                    + " uri:" + m_upgradeUri 
                    + " ver:" + m_latestVersion
                    + (m_forceUpgrade?" force":" noforce")
                    + ")";
    }
}
