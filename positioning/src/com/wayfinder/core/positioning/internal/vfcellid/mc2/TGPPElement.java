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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.positioning.internal.vfcellid.mc2;

import java.io.IOException;

import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.xml.XmlWriter;
import com.wayfinder.pal.network.info.NetworkInfo;
import com.wayfinder.pal.network.info.TGPPInfo;

/**
 * 
 *
 */
public class TGPPElement implements MC2WritableElement {
    
    private final String m_mcc;
    private final String m_mnc;
    private final String m_lac;
    private final String m_cellID;
    private final int m_signal;
    private final int m_netType;
    
    public TGPPElement(String mcc, String mnc, String lac, String cellID, int signalStrength, int netType) {
        m_mcc = mcc;
        m_mnc = mnc;
        m_lac = lac;
        m_cellID = cellID;
        m_signal = signalStrength;
        m_netType = netType;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.mc2.MC2WritableElement#write(com.wayfinder.core.network.internal.mc2.MC2Writer)
     */
    public void write(MC2Writer mc2w) throws IOException {
        
        mc2w.startElement(MC2Strings.ttgpp);
        mc2w.attribute(MC2Strings.ac_mcc, m_mcc);
        mc2w.attribute(MC2Strings.ac_mnc, m_mnc);
        mc2w.attribute(MC2Strings.alac, m_lac);
        mc2w.attribute(MC2Strings.acell_id, m_cellID);

        if (m_netType == TGPPInfo.TYPE_3GPP_GPRS) {
            mc2w.attribute(MC2Strings.anetwork_type, "GPRS");
        }
        else if (m_netType == TGPPInfo.TYPE_3GPP_UMTS) {
            mc2w.attribute(MC2Strings.anetwork_type, "UMTS");
        }
        
        if (m_signal != NetworkInfo.SIGNAL_STRENGTH_UNKNOWN) {
            mc2w.attribute(MC2Strings.asignal_strength, m_signal);
        }
        
        mc2w.endElement(MC2Strings.ttgpp);
    }
    
    public String toString() {
        return "mcc:" + m_mcc 
              + " mnc:" + m_mnc
              + " lac:" + m_lac
              + " cell_id:" + m_cellID
              + " type:" + m_netType;
    }

}
