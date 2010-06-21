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

package com.wayfinder.core.route;

import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.route.NavigationInfoListener;

// imports for javadoc to not make it unreadable with long package names.
import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.util.ListenerList;


/**
 * <p>Main entry point for using the route and navigation
 * functionality of the Java Core API.</p>
 * 
 * <p>Strictly speaking "routing" refers to calculating a route and
 * "navigation" and "route following" refers to mapping the current position
 * from the location
 * provider to a segment of the route and calculating next turn, distance to go,
 * etc. Beware that this terminology is not always strictly adhered to.</p>
 * 
 * <p>Non-core users can't send in positions to the route and navigation system.
 * The navigation subsystem will itself register with the
 * {@link com.wayfinder.core.positioning.LocationInterface} to obtain updated
 * positions for navigating.</p> 
 *
 * <p>Settings for a route are handled by {@link RouteRequest}. The route
 * module does not keep track of the last settings used and there is no
 * persistent storing of the settings. More settings functionality might be
 * added in a future release.</p>
 *
 * <p>Implementors must provide a thread safe implementation.</p> 
 */
public interface RouteInterface {

    /**
     * <p>Request a new route from the server.</p>
     * 
     * <p>If the route simulator is running, it is stopped. Then a request
     * is created internally, queued and the {@link RequestID} returned.</p>
     * 
     * <p>When the route has been calculated, downloaded and parsed ok,
     * {@link RouteListener#routeDone(RequestID, Route)}  will be called. If an
     * error occurs, e.g. routing not allowed for the current user account or
     * distance too long, {@link RouteListener#error(RequestID, CoreError)}
     * will be called. See also
     * {@link com.wayfinder.core.shared.error.RouteError}.</p>
     * 
     * <p>Navigation is NOT automatically started.</p>
     * 
     * <p>If a request is already pending, and this method is called again, the
     * result is undefined. The wrong route may be returned in
     * <code>RouteListener.routeDone()</code>. This is an error and will be fixed in a future
     * release.</p>
     * 
     * @param request The {@link RouteRequest} to send to the server.
     * @param listener The {@link RouteListener} which will receive the result. 
     * @return A {@link RequestID} to uniquely identify the request.
     * @see RouteListener
     * @see #navigate(RouteRequest, RouteListener)
     */
    public RequestID newRoute(RouteRequest request, RouteListener listener);

    /**
     * <p>Request a new route from the server and start navigation when the
     * route is downloaded.</p>
     * 
     * <p>Works like {@link #newRoute(RouteRequest, RouteListener)}. Additionally,
     * when the route is downloaded and parsed, navigation will start and
     * registered {@link NavigationInfoListener} will start to receive
     * updated navigation information.</p>
     *
     * @param request The {@link RouteRequest} to send to the server.
     * @param listener The {@link RouteListener} which will receive the result. 
     * @return A {@link RequestID} to uniquely identify the request.
     * @see RouteListener
     * @see #newRoute(RouteRequest, RouteListener)
     */
    public RequestID navigate(RouteRequest request, RouteListener listener);

    /**
     * <p>Start following the last downloaded route.</p>
     * 
     * <p>This method is intended to be used together with 
     * {@link #newRoute(RouteRequest, RouteListener)} which does not start
     * navigation once the route request is completed.</p>
     * 
     * <p>Since the calls to <code>RouteListener</code> are done asynchronously,
     * there is no guarantees that the route started is the one last reported
     * in {@link RouteListener#routeDone(RequestID, Route)}. Thus, the caller
     * must keep track of what routing requests are issued to avoid creating a
     * confusing situation for the end user.</p>
     *  
     * <p>In the following cases, this method has no effect and will return
     * false:
     * <ol><li>The last route was requested with <code>navigate(...)</code>.</li>
     *     <li>No route has yet been downloaded.</li>
     *     <li>In certain error cases.</li>
     * </ol></p> 
     *  
     * @return true if the call caused route following to start,
     * false otherwise. 
     */
    public boolean follow();

    /**
     * <p>Schedule to stop route following.</p>
     * 
     * <p>If the route simulator is running, stop it. If navigation is running,
     * stop navigation. New positions will not be evaluated.</p>
     * 
     * <p>It is not guaranteed when the stopping will occur. The caller must be
     * prepared to handle calls to registered {@link NavigationInfoListener}
     * even after this method returns.</p>
     * 
     * <p>This method has no effect on pending route requests. This contract
     * will probably be strengthened in a future release.</p>
     */
    public void clearRoute();


    /**
     * Register a <code>NavigationInfoListener</code> for updated navigation
     * information.</p>
     * 
     * <p>The listener is added to the set of listeners. Then,
     * {@link NavigationInfoListener#navigationInfoUpdated(NavigationInfo)} is
     * called with the latest available navigation information for the current
     * route. The call is made before this method returns and in the context
     * of the thread calling this method.</p> 
     * 
     * <p>It is not intended to register the same listener several times.
     * The listener is added if {@link ListenerList#add(Object)} would return
     * true. Note that these rules use both <code>equals()</code> and reference
     * identity.</p>
     *
     * <p>Future calls to the listener will be done thru
     * {@link CallbackHandler#callInvokeCallbackRunnable(Runnable)}.
     * The events will not accumulate; old events will be canceled 
     * automatically by new ones if were not processed.</p>
     *
     * <p>For more information about when call-backs occur and various
     * end of route and off-track conditions, see
     * {@link NavigationInfoListener}.</p>
     *
     * @param listener The {@link NavigationInfoListener} to be registered.
     */
    public void addNavigationInfoListener(NavigationInfoListener listener);

    /**
     * <p>Removes a <code>NavigationInfoListener</code> from the list of
     * recipients for updated navigation information.</p>
     * 
     * <p>It is NOT guaranteed that no more call-backs will be made on the
     * removed listener. We only guarantee that it eventually will stop being
     * called. The reason for this is that it is hard to guarantee timing
     * since already scheduled calls might be on the run queue. It is easier if
     * the UI just ignore events when not visible.</p>
     * 
     * <p>If the listener was not in the list, this method has no effect.</p>
     * 
     * @param listener listener to be removed.
     */
    public void removeNavigationInfoListener(NavigationInfoListener listener);


    /**
     * <p>Start a simulator that will generate navigation information events
     * along the route.</p>
     * 
     * <p>This is not intended for end user presentation as there are no
     * attempts to provide a realistic driving experience. We advice you to
     * only use it for smoke testing of your integration with Java Core API.</p>
     * 
     * <p>If a simulator is already running, or no route has been downloaded,
     * this method has no effect.</p>
     */
    public void simulate();
}
