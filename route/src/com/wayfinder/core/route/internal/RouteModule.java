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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.route.internal;

import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.ResponseCallback;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInterface;
import com.wayfinder.core.route.RouteListener;
import com.wayfinder.core.route.RouteRequest;
import com.wayfinder.core.route.internal.mc2.RouteMC2ReplyListener;
import com.wayfinder.core.route.internal.mc2.RouteMC2Request;
import com.wayfinder.core.route.internal.nav2route.Nav2Route;
import com.wayfinder.core.route.internal.nav2route.Nav2RouteParser;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.RouteError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.NavigationInfoListener;
import com.wayfinder.core.shared.util.ListenerList;
import com.wayfinder.core.shared.util.URITool;

/**
 * Actual core implementation of RouteInterface. The core start-up will create
 * exactly one instance and make it available to the UI.
 * This class will not be exported in the core sdk.
 */
public final class RouteModule 
implements InternalRouteInterface, RouteMC2ReplyListener, ResponseCallback {
    
    private static final Logger LOG = 
        LogFactory.getLoggerForClass(RouteModule.class);

    private final ModuleData m_moduleData;
    private final InternalNetworkInterface m_netIfc;
    private final MC2Interface m_mc2Ifc;
    private final LocationInterface m_locIfc;
    private final SharedSystems m_systems;
    private Criteria m_criteria;
    
    private final SimpleRouteSimulator m_simulator;
    
    private RouteListener m_listener;
    private boolean m_autoStartNavigation;
    private RouteRequest m_routeRequest;
    private String m_routeID;
    private BoundingBox m_routeBB;
    private RequestID m_reqID;
    
    private RouteFollower m_routeFollower;
    
    private ListenerList m_asyncNavInfoListeners;
    
    private ListenerList m_syncNavInfoListeners;
    
    private volatile NavigationInfo m_lastNavInfo;

    private Nav2Route m_parsedRoute;

    private NavigationEventRunnable lastEventPosted;
    /**
     * Creates the route module. This method will only be called by the core
     * start-up and does not itself enforce a singleton pattern.
     * @param moduleData
     * @param netIfc (internal) network interface
     * @param mc2Ifc (internal) MC2 interface (for XML requests)
     * @param locationIfc TODO
     * 
     * @return a new instance of the RouteModule
     */
    public static InternalRouteInterface createRouteInterface(
            SharedSystems systems, 
            ModuleData moduleData, 
            InternalNetworkInterface netIfc, 
            MC2Interface mc2Ifc, 
            LocationInterface locationIfc) { 
        
        RouteModule theModule = new RouteModule(
                systems, 
                moduleData, 
                netIfc, 
                mc2Ifc,
                locationIfc);
        
        return theModule;
    }


    private RouteModule(
            SharedSystems systems, 
            ModuleData moduleData, 
            InternalNetworkInterface netIfc, 
            MC2Interface mc2Ifc, 
            LocationInterface locationIfc) {
        
        m_systems = systems;
        m_moduleData = moduleData;
        m_netIfc = netIfc;
        m_mc2Ifc = mc2Ifc;
        m_locIfc = locationIfc;
        
        m_asyncNavInfoListeners = new ListenerList();
        m_syncNavInfoListeners = new ListenerList();
        
        m_criteria = new Criteria.Builder().accuracy(Criteria.ACCURACY_GOOD).
            costAllowed().courseRequired().speedRequired().build();
        
        m_simulator = new SimpleRouteSimulator(this);
    }
    
    /**
     * Get a new route and start navigation on it. If you just want the route
     * without starting navigation, use 
     * {@link #newRoute(RouteRequest, RouteListener)}
     * 
     * @param request 
     * @param listener
     */
    public synchronized RequestID navigate(RouteRequest request, RouteListener listener) {
        m_listener = listener;
        setAutoStartNavigation(true);
        
        if (m_routeFollower != null) {
            m_locIfc.removeSyncLocationListener(m_routeFollower);
            if (m_routeFollower.isRunning()) {
                m_routeFollower.stop();
            }
        }
        
        m_reqID = routeInternal(request);

        return m_reqID;
    }

    
    /**
     * Get a new route. If you want to start navigation, 
     * use {@link #navigate(RouteRequest, RouteListener)}
     * 
     * @param request
     * @param listener
     */
    public synchronized RequestID newRoute(RouteRequest request, RouteListener listener) {        
        m_listener = listener;
        setAutoStartNavigation(false);
        
        if (m_routeFollower != null) {
            m_locIfc.removeSyncLocationListener(m_routeFollower);
            if (m_routeFollower.isRunning()) {
                m_routeFollower.stop();
            }
        }
        
        m_reqID = routeInternal(request);
        
        return m_reqID;
    }


    /**
     * @param request
     * @return
     */
    private RequestID routeInternal(RouteRequest request) {
        
        if (m_simulator != null) {
            m_simulator.stop();
        }
        
        m_routeRequest = request;
        
        m_lastNavInfo = null;
        
        RequestID reqID = RequestID.getNewRequestID();
        RouteMC2Request xmlReq = new RouteMC2Request(
                m_systems.getSettingsIfc().getGeneralSettings(),
                reqID, request, this);
        m_mc2Ifc.pendingMC2Request(xmlReq, false);
        return reqID;
    }
    
    public void addNavigationInfoListener(final NavigationInfoListener listener) {
        if(m_asyncNavInfoListeners.add(listener)) {
            //at adding the listener is immediately notified about current status
            NavigationInfo navInfo = m_lastNavInfo;
            // async listener:
            if (navInfo != null) {
                    //fire the last known nav info directly as this method will 
                    //be called from UI/Thread 
                    listener.navigationInfoUpdated(navInfo);
            }
        }
    }

    public void removeNavigationInfoListener(NavigationInfoListener listener) {
        m_asyncNavInfoListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.route.internal.mc2.XmlRouteReplyListener#xmlRouteReplyDone(RequestID, XmlRouteReply)
     */
    public synchronized void routeMC2ReplyDone(RequestID reqID, String routeID, BoundingBox boundingBox) {
        m_routeID = routeID;
        m_routeBB = boundingBox;
        
        StringBuffer url = new StringBuffer();        
        //NOTE: the part before the "/" is handled by the Networking module
        url.append("/");
        url.append("navigatorroute.bin?r=");
        URITool.percentEncodeString(m_routeID,url);
        url.append("&protoVer=11&s=300000&lm=1&l="); // protover 0xB = 11
        URITool.percentEncodeString(
                m_systems.getSettingsIfc().getGeneralSettings().getInternalLanguage().getXMLCode()
                ,url);
        url.append("&rv=2"); // for lanes and signposts
        
        m_netIfc.pendingGetRequest(url.toString(), this);
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.ResponseCallback#readResponse(java.io.InputStream, long)
     */
    public synchronized void readResponse(final InputStream in, long length) throws IOException {
        if (LOG.isInfo()) {
            LOG.info("RouteModule.readResponse()", "content length: "+length);
        }
        
        try {
            m_parsedRoute = Nav2RouteParser.parseNavigatorRouteBin(
                    m_routeRequest, m_routeID, m_routeBB, in);
                m_moduleData.getCallbackHandler().callInvokeCallbackRunnable(
                        new Runnable() {
                            public void run() {
                                m_listener.routeDone(m_reqID, m_parsedRoute);
                            }
                        });
                
                if (m_routeRequest.isReroute() 
                        && m_routeRequest.getRerouteReason() 
                        == RouteRequest.REASON_TRAFFIC_INFO_UPDATE) {
                    if (m_routeFollower != null) {
                        m_locIfc.removeSyncLocationListener(m_routeFollower);
                        if (m_routeFollower.isRunning()) {
                            m_routeFollower.stop();
                        }
                    }
                }
                
                if (isAutoStartNavigation()) {
                    m_routeFollower = new RouteFollower(this, m_systems, m_parsedRoute, m_routeRequest);
                    m_routeFollower.initRouteFollower();
                    m_moduleData.getPAL().getConcurrencyLayer().startNewDaemonThread(m_routeFollower, "RouteFollower");
                    m_locIfc.addSyncLocationListener(m_criteria, m_routeFollower);
                }
        }
        catch (final IOException ioe) {
            if (LOG.isError()) {
                LOG.error("RouteModule.readResponse(in, length)", ioe.getMessage());
            }
            m_moduleData.getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
                public void run() {
                    m_listener.error(m_reqID, new UnexpectedError(ioe.getMessage(), ioe));
                }
            });
            
        }

    }
    
    public synchronized boolean follow() {
        try {
            if (!isAutoStartNavigation() && (m_parsedRoute != null)) {
                if (m_routeFollower != null) {
                    m_locIfc.removeSyncLocationListener(m_routeFollower);
                    if (m_routeFollower.isRunning()) {
                        m_routeFollower.stop();
                    }
                }
                m_routeFollower = new RouteFollower(this, m_systems,
                        m_parsedRoute, m_routeRequest);
                m_routeFollower.initRouteFollower();

                m_moduleData.getPAL().getConcurrencyLayer().startNewDaemonThread(m_routeFollower, "RouteFollower");
                m_locIfc.addSyncLocationListener(m_criteria, m_routeFollower);
                return true;
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("RouteModule.follow()", e.toString());
            }
            error(new UnexpectedError("Route could not start following becasue of an error", e));
        }
        return false;
    }
    
    public synchronized void clearRoute() {
        // FIXME: if a route request (especially a re-route) is already
        // scheduled this method will not stop that. 
        if (m_simulator != null) {
            m_simulator.stop();
        }
        
        if (m_routeFollower != null) {
            m_locIfc.removeSyncLocationListener(m_routeFollower);
            if (m_routeFollower.isRunning()) {
                m_routeFollower.stop();
            }
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.ResponseListener#error(com.wayfinder.core.shared.RequestID, com.wayfinder.core.shared.error.CoreError)
     */
    public void error(final RequestID requestID, final CoreError error) {
        if (LOG.isError()) {
            LOG.error("RouteModule.error(requestID, error)", error.toString());
        }
        
        boolean isErrorRouteUpToDate = false;
        //hack to get a status which don't represent an error
        //TODO: find a better solution for this hack
        if (error instanceof ServerError) {
            if (((ServerError) error).getStatusCode() 
                    == RouteError.ERRROUTE_UP_TO_DATE) {
                isErrorRouteUpToDate = true;
                if (LOG.isInfo()) {
                    LOG.info("RouteModule.error(requestID, error)", "Route is up to date!");
                }
            }
        }
        
        if (!isErrorRouteUpToDate) {
            m_moduleData.getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {

                public void run() {
                    m_listener.error(requestID, error);
                }
            });
        }
        
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.ResponseCallback#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(final CoreError error) {
        if (LOG.isError()) {
            LOG.error("RouteModule.error(error)", error.getInternalMsg());
        }
        
        m_moduleData.getCallbackHandler().callInvokeCallbackRunnable(new Runnable() {
            
            public void run() {
                m_listener.error(m_reqID, error);
            }
        });
        
    }

    /**
     * @param navInfo
     */
    //TODO remove the synchronization
    public void updateListeners(final NavigationInfo navInfo) {
        m_lastNavInfo = navInfo;
        CallbackHandler callHandler = m_moduleData.getCallbackHandler();
        if(LOG.isDebug()) {
            LOG.debug("RouteModule.updateListeners()",
                      "sending " + navInfo + " via " + callHandler);
        }

        // notify first asynch listeners
        
        //check first if there are any asynchronous listeners
        if (!m_asyncNavInfoListeners.isEmpty()) {
            //cancel last navigation notification if was not executed yet
            //maybe we should cancel anyway as it will be an old info
            if (lastEventPosted!=null) lastEventPosted.cancel();
            //notify all listeners in a single callback
            lastEventPosted = new NavigationEventRunnable(navInfo);
            callHandler.callInvokeCallbackRunnable(lastEventPosted);
        }
        
        //notify sync listeners
        
        //get the actual array of listeners  
        Object[] synchListeners = m_syncNavInfoListeners.getListenerInternalArray();
        
        //no synchronization needed as the array will not be changed 
        for(int i = 0; i < synchListeners.length; i++) {
            ((NavigationInfoListener)synchListeners[i]).navigationInfoUpdated(navInfo);
        }        
    }
    
    
    public synchronized NavigationInfo getLatestNavigationInfo() {
        return m_lastNavInfo;
    }


    /**
     * Reroute.
     * 
     * @param newReq
     */
    public synchronized void reroute(RouteRequest newReq) {
        if (LOG.isInfo()) {
            LOG.info("RouteModule.reroute()", "reason: "+newReq.getRerouteReason());
        }
        // this is used internally by the RouteFollower, so it means we were
        // already following a route, so it's ok to continue following the new
        // route once we get it
        setAutoStartNavigation(true);
        
        if (m_routeFollower != null 
                && m_routeFollower.isRunning()
                && newReq.isReroute()
                && newReq.getRerouteReason() != RouteRequest.REASON_TRAFFIC_INFO_UPDATE
                ) {
            m_locIfc.removeSyncLocationListener(m_routeFollower);
            m_routeFollower.stop();
        }
        
        m_reqID = routeInternal(newReq);
    }

    public synchronized void simulate() {
        // FIXME: crash if m_routeFollower is null. But we don't want to call
        // follow() as that will register the follower on the real position
        // source.
        if (m_simulator != null && m_parsedRoute != null) {
            m_simulator.start(m_parsedRoute,
                              m_routeFollower,
                              m_moduleData.getPAL().getConcurrencyLayer());
        }
    }


    public void addSyncNavInfoListener(NavigationInfoListener listener) {
        m_syncNavInfoListeners.add(listener);
    }


    public void removeSyncNavInfoListener(NavigationInfoListener listener) {
        m_syncNavInfoListeners.remove(listener);
    }

    private void setAutoStartNavigation(boolean autoStartNavigation) {
        m_autoStartNavigation = autoStartNavigation;
    }

    private boolean isAutoStartNavigation() {
        return m_autoStartNavigation;
    }
    
    private class NavigationEventRunnable implements Runnable {
        private volatile boolean isCancel;
        
        private final NavigationInfo navInfo;
        
        public NavigationEventRunnable(NavigationInfo navInfo) {
            super();
            this.navInfo = navInfo;
        }

        public void run() {
            if (isCancel) return;
            //synchronization not need because all methods that change 
            //m_asynchLocationListeners are called from Main/UI Thread 
            //and this run in Main/UI Thread
            
            //get the actual array of listeners  
            Object[] asynchListeners = m_asyncNavInfoListeners.getListenerInternalArray();
            
            //no synchronization needed as the array will not be changed 
            for(int i = 0; i < asynchListeners.length; i++) {
                ((NavigationInfoListener)asynchListeners[i]).navigationInfoUpdated(navInfo);
            }
        }
        
        public void cancel() {
            isCancel = true;
        }
    }
}
