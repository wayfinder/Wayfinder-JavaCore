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

package com.wayfinder.core.shared.route;

import com.wayfinder.core.route.RouteInterface;


/**
 * <p>Implement this to receive updated navigation information.</p>
 * 
 * <p>We keep this separated from
 * {@link com.wayfinder.core.route.RouteListener} to facilitate making
 * a map-and-guide-application. Also, this helps to separate the one-shot
 * reporting of completed request (in line with how the other modules work) and
 * periodic updates.</p>
 * 
 * <p>This interface is placed in shared, since it will need to be implemented
 * by classes from other modules (e.g. sound system).</p>
 * 
 * @see RouteInterface#addNavigationInfoListener(NavigationInfoListener)
 */
public interface NavigationInfoListener {
    
    /**
     * <p>Called when new navigation information is available. The actual
     * information may be unchanged. E.g. if the position from the GPS has
     * changed only slightly. The implementor is responsible to check for the
     * changes it is interested in. It is hard for core to anticipate the needs.
     * Thus a "callback only if information X has changed" is not implemented.
     * </p>
     * 
     * <p>At the end of the navigation this will be notified and 
     * the {@link NavigationInfo#isFollowing()} set on true.
     * This can happen for 2 reason there was an error or the destination 
     * has been reached.</p>
     * 
     * <p>When the destination has been reached the navigation will be 
     * automatically be stopped and listener will be notified with  
     * {@link NavigationInfo#isFollowing()} set on false and
     * {@link NavigationInfo#isDestinationReached()} set on true.</p>
     * 
     * <p>If the navigation is stopped manually by calling 
     * {@link RouteInterface#clearRoute()} there will no last notification.</p>
     * 
     * @param info updated navigation information.
     */
    public void navigationInfoUpdated(NavigationInfo info);
}
