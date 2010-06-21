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
package com.wayfinder.core.map.vectormap.internal.control;

import java.util.Vector;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.pal.util.UtilFactory;

/**
 *  Class for handling sending and receiving of map server requests.  
 */
public final class TileMapNetworkHandler {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapNetworkHandler.class);
    
    private final UtilFactory m_UtilFactory;
    private final WorkScheduler m_scheduler;
    private final InternalNetworkInterface m_NetworkInterface;
    private TileMapRequestListener m_TileMapRequestListener;
    
    // Holds the parameter strings that should be requested. 
    private Vector m_stringsForNextRequest;
    // Holds the outstanding param strings that are in progress of being requested
    // from the server
    private Vector m_stringsBeingRequested;
    
    
    /* True if we have sent a request to the server and waiting for the response, false if not.
     * This to make the handling of which tiles that has been downloaded and which that needs to
     * be requested again easier. 
     */
    private boolean m_HasOutgoingRequest;
    
    // the current counter of the backoff timer
    private long m_backoffWaitPeriod;
    
    public TileMapNetworkHandler(InternalNetworkInterface aNetworkInterface, 
            TileMapRequestListener tileMapRequestListener, UtilFactory utilFactory,
            WorkScheduler scheduler) {
        
        m_NetworkInterface = aNetworkInterface;
        m_UtilFactory = utilFactory;
        m_TileMapRequestListener = tileMapRequestListener;
        m_scheduler = scheduler;
        
        m_stringsForNextRequest = new Vector();
        m_HasOutgoingRequest  = false;
    }
    
    
    public synchronized void setTileMapRequestListener(TileMapRequestListener listener) {
        m_TileMapRequestListener = listener;
    }

    
    //-------------------------------------------------------------------------
    // backoff methods
    

    /**
     * Resets the timer for the exponential backoff between attempts to grab
     * new data from the network.
     * <p>
     * If the timer is already 0, this method will not have any effect.
     * <p>
     * If the timer is above zero, any currently pending request will immediately
     * be serviced. Please note that the request may still be delayed if the
     * current load on the Core network module is heavy.
     */
    public synchronized void resetExponentialBackoff() {
        m_backoffWaitPeriod = 0;
        // immediately transmit any outstanding requests
        schedulePendingRequest();
    }
    
    
    /**
     * Increase the the time to the next time we try to request tilemaps from the server.   
     * 
     */
    synchronized void increaseExponentialBackoff() {     
        if(m_backoffWaitPeriod < 32000L) {
            if(m_backoffWaitPeriod <= 0) {
                m_backoffWaitPeriod = 1000L;
            } else {
                m_backoffWaitPeriod *= 2L;
            }
        }
    }
    
    
    private long getBackoffTime() {
        return m_backoffWaitPeriod;
    }
    
    
    //-------------------------------------------------------------------------
    // requesting
    
    
    /**
     * Add a new parameter string to the request queue. 
     * 
     * @param paramString the parameter string to be requested. 
     */
    public synchronized void request(String paramString) {
        if(LOG.isDebug()) {
            LOG.debug("TileMapNetworkHandler.request()", paramString);
        }
        
        m_stringsForNextRequest.addElement(paramString);        
    }
    
    
    /**
     * Trigger that all added parameter strings are sent to the server. 
     * It we have outgoing request already we ignore the request. 
     */
    public synchronized void sendRequest() {     
        if(!m_HasOutgoingRequest && m_stringsForNextRequest.size() > 0) {
            m_HasOutgoingRequest = true;

//treat TMFD request special and send only one at once
//            for (int i=0, n=m_stringsForNextRequest.size();i<n;i++) {
//                final String param = (String)m_stringsForNextRequest.elementAt(i);
//                if (TileMapParamTypes.isMapFormatDesc(param)) {
//                    m_stringsForNextRequest.removeElementAt(i);
//                    
//                    // if backoff time is 0, it will go directly onto the queue
//                    m_scheduler.scheduleDelayed(new Runnable() {
//                        public void run() {
//                            // this delayed call can be superceded by a call to
//                            // resetExponentialBackoff() in which case 
//                            // schedulePendingRequest will do nothing
//                            scheduleSingleRequest(param);
//                        }
//                    }, getBackoffTime());                    
//                    return;
//                }
//            }
            
            m_stringsBeingRequested = m_stringsForNextRequest;
            m_stringsForNextRequest = new Vector();

            // if backoff time is 0, it will go directly onto the queue
            m_scheduler.scheduleDelayed(new Runnable() {
                public void run() {
                    // this delayed call can be superceded by a call to
                    // resetExponentialBackoff() in which case 
                    // schedulePendingRequest will do nothing
                    schedulePendingRequest();
                }
            }, getBackoffTime());
        }
    }
    
    
    /**
     * Creates a TileMapRequest and sends it to the network parts for handling
     */
    private synchronized void schedulePendingRequest() {
        if(m_stringsBeingRequested != null) {
            
            final TileMapRequest req = new TileMapRequest(this, 
                    m_stringsBeingRequested,  m_TileMapRequestListener, m_UtilFactory);
            m_stringsBeingRequested = null;
            
            m_NetworkInterface.pendingPostRequest(req.getConnectionURI(), req, req);
            if(LOG.isDebug()) {
                LOG.debug("TileMapNetworkHandler.sendRequest()", 
                        "m_UseXSGzip= "+req.usesXSGZIP()+
                        " m_Buffer.length= "+req.getBufferSize());
            }
        }
    }
    
    /**
     * Creates a TileMapRequest and sends it to the network parts for handling
     */
    private synchronized void scheduleSingleRequest(String param) {
         TileMapSingleRequest req = new TileMapSingleRequest(this, 
                    param,  m_TileMapRequestListener);

         m_NetworkInterface.pendingGetRequest(req.getConnectionURI(), req);
    }
    
    
    synchronized void setHasOutgoingRequest(boolean hasOutgoing) {
        m_HasOutgoingRequest = hasOutgoing;
        // Check if there are more request that needs to be sent to the server. 
        sendRequest();
    }

}
