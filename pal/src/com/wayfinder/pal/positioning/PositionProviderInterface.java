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
package com.wayfinder.pal.positioning;

/**
 * This interface should be implemented by each platform-level position 
 * provider. For example the class that handles position updates sent by
 * the internal GPS. Implementing this interface would allow the Core to
 * start or stop the platform-level provider.
 * 
 * 
 *
 */
public interface PositionProviderInterface {
    
    /**
     * getting positions from internal GPS
     */
    public static final int TYPE_INTERNAL_GPS = 0;
    
    /**
     * getting positions from external (BT) GPS
     */
    public static final int TYPE_EXTERNAL_GPS = 1;
    
    /**
     * getting positions from cable GPS
     */
    public static final int TYPE_CABLE_GPS = 2;
    
    /**
     * network-based location (Cell ID)
     */
    public static final int TYPE_NETWORK = 3;
    
    /**
     * simulator
     */
    public static final int TYPE_SIMULATOR = 99;

    
    /**
     * set where in Core updates from this provider are handled
     * @param coreHandler
     */
    public void setUpdatesHandler(UpdatesHandler coreHandler);
    
    /**
     * signal the platform to start updating this provider
     */
    public void resumeUpdates();
    
    /**
     * signal the platform to stop updating this provider
     */
    public void stopUpdates();
    
    /**
     * get provider type
     * @return one of the TYPE_* constants
     * 
     * @see {@link #TYPE_INTERNAL_GPS} {@link #TYPE_EXTERNAL_GPS},
     * {@link #TYPE_CABLE_GPS}, {@link #TYPE_NETWORK}, {@link #TYPE_SIMULATOR}
     */
    public int getType();
}
