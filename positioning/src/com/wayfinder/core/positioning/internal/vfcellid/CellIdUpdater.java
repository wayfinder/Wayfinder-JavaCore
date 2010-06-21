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
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2ReplyListener;
import com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2Request;
import com.wayfinder.core.positioning.internal.vfcellid.mc2.TGPPElement;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.pal.network.info.NetworkInfo;
import com.wayfinder.pal.network.info.TGPPInfo;
import com.wayfinder.pal.positioning.PositionProviderInterface;
import com.wayfinder.pal.positioning.UpdatesHandler;

/**
 * 
 *
 */
public class CellIdUpdater implements Runnable, PositionProviderInterface, CellIdMC2ReplyListener {
    
    private static final Logger LOG = LogFactory.getLoggerForClass(CellIdUpdater.class);
    
    private volatile boolean m_reschedule;
    
    /**
     * 1 minute
     */
    private static final long UPDATE_PERIOD = 60000;   //ms (1min)
    
    private final NetworkInfo m_netInfo;
    private final WorkScheduler m_ws;
    private final MC2Interface m_mc2Ifc;
    
    private UpdatesHandler m_coreHandler;
    
    private RequestID m_requestID;
    private long m_requestTime;
    
    public CellIdUpdater(MC2Interface mc2ifc, NetworkInfo netInfo, WorkScheduler ws) {
        m_netInfo = netInfo;
        m_ws = ws;
        m_mc2Ifc = mc2ifc;
        
        m_reschedule = true;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (!m_reschedule) return;
        
        MC2Request mc2req = createCellIdRequest();
        if (mc2req != null) {
            m_mc2Ifc.pendingMC2Request(mc2req);
        }
        
        m_ws.scheduleDelayed(this, UPDATE_PERIOD);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.positioning.PositionProviderInterface#getType()
     */
    public int getType() {
        return PositionProviderInterface.TYPE_NETWORK;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.positioning.PositionProviderInterface#resumeUpdates()
     */
    public void resumeUpdates() {
        if (LOG.isInfo()) {
            LOG.info("CellIdUpdater.resumeUpdates()", "VFCellID resume");
        }
        m_reschedule = true;
        //direct call instead of m_ws.schedule(this); as all calls are non blocking
        run();
        
        m_coreHandler.updateState(UpdatesHandler.PROVIDER_AVAILABLE);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.positioning.PositionProviderInterface#setUpdatesHandler(com.wayfinder.pal.positioning.UpdatesHandler)
     */
    public void setUpdatesHandler(UpdatesHandler coreHandler) {
        m_coreHandler = coreHandler;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.pal.positioning.PositionProviderInterface#stopUpdates()
     */
    public void stopUpdates() {
        if (LOG.isInfo()) {
            LOG.info("CellIdUpdater.stopUpdates()", "VFCellID stop");
        }
        m_reschedule = false;
        m_coreHandler.updateState(UpdatesHandler.PROVIDER_OUT_OF_SERVICE);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.internal.vfcellid.mc2.CellIdMC2ReplyListener#cellIdReplyDone(com.wayfinder.core.shared.RequestID, com.wayfinder.core.shared.Position, int)
     */
    public void cellIdReplyDone(RequestID reqID, Position pos, int radius) {
        if(LOG.isInfo()) {
            LOG.info("CellIdUpdater.cellIdReplyDone()", "gotPosition " + pos);
        }
        //extra check to be sure the position is good 
        //actually the server find will send back a status code
        //if it cannot find a position for that cellid
        if (pos.isValid()) {
            m_coreHandler.updatePosition(
                    Position.mc2ToDecimalDegrees(pos.getMc2Latitude()), 
                    Position.mc2ToDecimalDegrees(pos.getMc2Longitude()), 
                    UpdatesHandler.VALUE_UNDEF, //speed
                    UpdatesHandler.VALUE_UNDEF, //course
                    UpdatesHandler.VALUE_UNDEF, //altitude
                    radius,                     //might be VALUE_UNDEF
                    m_requestTime);
        }
        else {
            m_coreHandler.updateState(UpdatesHandler.PROVIDER_TEMPORARILY_UNAVAILABLE);
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.ResponseListener#error(com.wayfinder.core.shared.RequestID, com.wayfinder.core.shared.error.CoreError)
     */
    public void error(RequestID requestID, CoreError error) {
        switch (error.getErrorType()) {
        case CoreError.ERROR_GENERAL:
            if (LOG.isError()) {
                LOG.error("CellIdUpdater.error()", 
                        "reqID: " + requestID +
                        ", error type: " + error.getErrorType() +
                        ", msg: " + error.getInternalMsg());
            }
            stopUpdates();
            break;
            
        case CoreError.ERROR_UNEXPECTED:
            UnexpectedError unexErr = (UnexpectedError) error;
            if (LOG.isError()) {
                LOG.error("CellIdUpdater.error()", 
                        "reqID: " + requestID +
                        ", error type: " + unexErr.getErrorType() +
                        ", ex: "+unexErr.getCause());
            }
            stopUpdates();
            break;
            
        case CoreError.ERROR_NETWORK:
            NetworkError netErr = (NetworkError) error;
            if (LOG.isError()) {
                LOG.error("CellIdUpdater.error()", 
                        "reqID: " + requestID +
                        ", error type: " + netErr.getErrorType() +
                        ", msg: "+netErr.getInternalMsg());
            }
            m_coreHandler.updateState(UpdatesHandler.PROVIDER_TEMPORARILY_UNAVAILABLE);
            //try again later
            break;
            
        case CoreError.ERROR_SERVER:
            ServerError srvErr = (ServerError) error;
            if (LOG.isError()) {
                LOG.error("CellIdUpdater.error()", 
                        "reqID: " + requestID +
                        ", error type: " + srvErr.getErrorType() +
                        ", msg: "+srvErr.getInternalMsg() +
                        ", code: "+srvErr.getStatusCode() +
                        ", URI: "+srvErr.getStatusUri());
            }
            m_coreHandler.updateState(UpdatesHandler.PROVIDER_TEMPORARILY_UNAVAILABLE);
            break;
        }
    }

    MC2Request createCellIdRequest() {
        MC2WritableElement element = null;
        if (m_netInfo.getNetworkWAF() == NetworkInfo.WAF_3GPP) {
            TGPPInfo info = m_netInfo.get3GPPInfo();
            
            if (!info.supportsCurrentMCC() 
                    || !info.supportsCurrentMNC()
                    || !info.supportsLAC()
                    || !info.supportsCellID()) {
                return null;
            }
            m_requestTime = System.currentTimeMillis();
            m_requestID = RequestID.getNewRequestID();
//            element = new TGPPElement("214", "01", Integer.toHexString(65200), 
//                    Integer.toHexString(43524536), 100, TGPPInfo.TYPE_3GPP_UMTS); 
                
            element = new TGPPElement(
                    m_netInfo.get3GPPInfo().getCurrentMCC(), 
                    m_netInfo.get3GPPInfo().getCurrentMNC(), 
                    m_netInfo.get3GPPInfo().getLAC(), 
                    m_netInfo.get3GPPInfo().getCellID(), 
                    m_netInfo.getSignalStrength(), 
                    m_netInfo.get3GPPInfo().getNetworkType());
            CellIdMC2Request mc2req = new CellIdMC2Request(m_requestID, element, this);
            
            if(LOG.isInfo()) {
                LOG.info("CellIdUpdater.createCellIdRequest()", element.toString());
            }
            return mc2req;
        }
        
        return null;
    }
}
