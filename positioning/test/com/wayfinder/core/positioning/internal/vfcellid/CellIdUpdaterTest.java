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
package com.wayfinder.core.positioning.internal.vfcellid;

import com.wayfinder.core.network.NetworkError;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2Request;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.pal.network.info.NetworkException;
import com.wayfinder.pal.network.info.NetworkInfo;
import com.wayfinder.pal.network.info.TGPPInfo;
import com.wayfinder.pal.positioning.PositionProviderInterface;
import com.wayfinder.pal.positioning.UpdatesHandler;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class CellIdUpdaterTest extends TestCase {
    
    private NetworkInfo m_3gNetInfo;
    
    private RequestID m_reqID;

    private NetworkInfo m_unknownNetInfo;

    /**
     * @param name
     */
    public CellIdUpdaterTest(String name) {
        super(name);
        
        m_3gNetInfo = new NetworkInfo() {
            
            public int getSignalStrength() {
                return -80;
            }
            
            public int getRoamingState() {
                return ROAMING_STATE_HOME;
            }
            
            public int getRadioState() {
                return RADIO_STATE_ON;
            }
            
            public int getNetworkWAF() {
                return WAF_3GPP;
            }
            
            public TGPPInfo get3GPPInfo() throws IllegalStateException {
                return new TGPPInfo() {
                    
                    public boolean supportsLAC() {
                        return true;
                    }
                    
                    public boolean supportsHomeMNC() {
                        return true;
                    }
                    
                    public boolean supportsHomeMCC() {
                        return true;
                    }
                    
                    public boolean supportsCurrentMNC() {
                        return true;
                    }
                    
                    public boolean supportsCurrentMCC() {
                        return true;
                    }
                    
                    public boolean supportsCellID() {
                        return true;
                    }
                    
                    public int getNetworkType() {
                        return TYPE_3GPP_UMTS;
                    }
                    
                    public String getLAC() throws NetworkException {
                        return "064a";
                    }
                    
                    public String getHomeMNC() throws NetworkException {
                        return "10";
                    }
                    
                    public String getHomeMCC() throws NetworkException {
                        return "226";
                    }
                    
                    public String getCurrentMNC() throws NetworkException {
                        return "10";
                    }
                    
                    public String getCurrentMCC() throws NetworkException {
                        return "226";
                    }
                    
                    public String getCellID() throws NetworkException {
                        return "000f576b";
                    }
                };
            }

            public boolean isAirplaneMode() {
                // 
                return false;
            }
        };
        m_unknownNetInfo = new NetworkInfo() {
            
            public int getSignalStrength() {
                return SIGNAL_STRENGTH_UNKNOWN;
            }
            
            public int getRoamingState() {
                return ROAMING_STATE_UNKNOWN;
            }
            
            public int getRadioState() {
                return RADIO_STATE_UNKNOWN;
            }
            
            public int getNetworkWAF() {
                return WAF_UNKNOWN;
            }
            
            public TGPPInfo get3GPPInfo() throws IllegalStateException {
                return null;
            }

            public boolean isAirplaneMode() {
                return false;
            }
        };
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_reqID = RequestID.getNewRequestID();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCreateCellIdRequest() {
        CellIdUpdater updater = new CellIdUpdater(null, m_3gNetInfo, null);
        CellIdMC2Request mc2Req = (CellIdMC2Request) updater.createCellIdRequest();
        assertNotNull(mc2Req);
        updater = new CellIdUpdater(null, m_unknownNetInfo, null);
        mc2Req = (CellIdMC2Request) updater.createCellIdRequest();
        assertNull(mc2Req);
        
        NetworkInfo badInfo = new NetworkInfo() {
            
            public int getSignalStrength() {
                return SIGNAL_STRENGTH_UNKNOWN;
            }
            
            public int getRoamingState() {
                return ROAMING_STATE_UNKNOWN;
            }
            
            public int getRadioState() {
                return RADIO_STATE_UNKNOWN;
            }
            
            public int getNetworkWAF() {
                return WAF_3GPP;
            }
            
            public TGPPInfo get3GPPInfo() throws IllegalStateException {
                return new TGPPInfo() {
                    
                    public boolean supportsLAC() {
                        return false;
                    }
                    
                    public boolean supportsHomeMNC() {
                        return false;
                    }
                    
                    public boolean supportsHomeMCC() {
                        return false;
                    }
                    
                    public boolean supportsCurrentMNC() {
                        return false;
                    }
                    
                    public boolean supportsCurrentMCC() {
                        return false;
                    }
                    
                    public boolean supportsCellID() {
                        return false;
                    }
                    
                    public int getNetworkType() {
                        return 0;
                    }
                    
                    public String getLAC() throws NetworkException {
                        return null;
                    }
                    
                    public String getHomeMNC() throws NetworkException {
                        return null;
                    }
                    
                    public String getHomeMCC() throws NetworkException {
                        return null;
                    }
                    
                    public String getCurrentMNC() throws NetworkException {
                        return null;
                    }
                    
                    public String getCurrentMCC() throws NetworkException {
                        return null;
                    }
                    
                    public String getCellID() throws NetworkException {
                        return null;
                    }
                };
            }

            public boolean isAirplaneMode() {
                // 
                return false;
            }
        };
        updater = new CellIdUpdater(null, badInfo, null);
        mc2Req = (CellIdMC2Request) updater.createCellIdRequest();
        assertNull(mc2Req);
    }
    
    public void testStopUpdates() {
        UpdatesHandler updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
                assertEquals(PROVIDER_OUT_OF_SERVICE, state);
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                fail("CellIdUpdaterTest.testStopUpdates().new UpdatesHandler() " +
                		"{...}.updatePosition() not supposed to be called in this case");
            }
        };
        
        CellIdUpdater updater = new CellIdUpdater(null, m_unknownNetInfo, null);
        updater.setUpdatesHandler(updHandler);
        updater.stopUpdates();
    }
    
    public void testCellIdReplyDone() {
        CellIdUpdater updater = new CellIdUpdater(null, null, null);
        
        UpdatesHandler updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                assertEquals(Position.mc2ToDecimalDegrees(558006267), latitudeDeg, 0.000001);
                assertEquals(Position.mc2ToDecimalDegrees(281682173), longitudeDeg, 0.000001);
                assertEquals(VALUE_UNDEF, speedMps, 0.00001);
                assertEquals(VALUE_UNDEF, course, 0.00001);
                assertEquals(VALUE_UNDEF, altitude, 0.00001);
                assertEquals(100, accuracy);
                
            }
        };
        updater.setUpdatesHandler(updHandler);
        updater.cellIdReplyDone(m_reqID, new Position(558006267, 281682173), 100);
        
        updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
                assertEquals(PROVIDER_TEMPORARILY_UNAVAILABLE, state);
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                fail("CellIdUpdaterTest.testCellIdReplyDone().new UpdatesHandler() " +
                		"{...}.updatePosition() should not be reached");
            }
        };
        updater.setUpdatesHandler(updHandler);
        updater.cellIdReplyDone(m_reqID, new Position(), 0);
    }
    
    public void testGeneralError() {
        UpdatesHandler updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
                assertEquals(PROVIDER_OUT_OF_SERVICE, state);
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                fail("CellIdUpdaterTest.testGeneralError().new UpdatesHandler() " +
                		"{...}.updatePosition() not supposed to be called in this case");
            }
        };
        
        CellIdUpdater updater = new CellIdUpdater(null, m_unknownNetInfo, null);
        updater.setUpdatesHandler(updHandler);
        updater.error(m_reqID, new CoreError("error"));
    }
    
    public void testUnexpectedError() {
        UpdatesHandler updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
                assertEquals(PROVIDER_OUT_OF_SERVICE, state);
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                fail("CellIdUpdaterTest.testUnexpectedError().new UpdatesHandler() " +
                		"{...}.updatePosition() not supposed to be called in this case");
            }
        };
        
        CellIdUpdater updater = new CellIdUpdater(null, m_unknownNetInfo, null);
        updater.setUpdatesHandler(updHandler);
        updater.error(m_reqID, new UnexpectedError("error", new Throwable()));
    }
    
    public void testNetworkError() {
        UpdatesHandler updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
                assertEquals(PROVIDER_TEMPORARILY_UNAVAILABLE, state);
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                fail("CellIdUpdaterTest.testNetworkError().new UpdatesHandler() " +
                		"{...}.updatePosition() not supposed to be called in this case");
            }
        };
        
        CellIdUpdater updater = new CellIdUpdater(null, m_unknownNetInfo, null);
        updater.setUpdatesHandler(updHandler);
        updater.error(m_reqID, new NetworkError(
                "message", NetworkError.REASON_BLOCKED_BY_PERMISSIONS));
        
    }
    
    public void testServerError() {
        UpdatesHandler updHandler = new UpdatesHandler() {
            
            public void updateState(int state) {
                assertEquals(PROVIDER_TEMPORARILY_UNAVAILABLE, state);
            }
            
            public void updatePosition(double latitudeDeg, double longitudeDeg,
                    float speedMps, float course, float altitude, int accuracy,
                    long timestamp) {
                fail("CellIdUpdaterTest.testServerError().new UpdatesHandler() " +
                		"{...}.updatePosition() not supposed to be called in this case");
            }
        };
        
        CellIdUpdater updater = new CellIdUpdater(null, m_unknownNetInfo, null);
        updater.setUpdatesHandler(updHandler);
        updater.error(m_reqID, new ServerError(
                ServerError.ERRSERV_GENERAL_SERVER_ERROR, "message", "URI"));

    }
    
    public void testGetType() {
        CellIdUpdater updater = new CellIdUpdater(null, null, null);
        assertEquals(PositionProviderInterface.TYPE_NETWORK, updater.getType());
    }
}
