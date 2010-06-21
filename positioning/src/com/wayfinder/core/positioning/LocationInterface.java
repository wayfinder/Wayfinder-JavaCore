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
/**
 * 
 */
package com.wayfinder.core.positioning;

/**
 * 
 * 
 */
public interface LocationInterface {    

    
    /**
     * <p>Method for "warming" up the Locationing System.
     * This method will return fast, even if the initialization can take more time,
     * the positioning system will not block the caller of this method.</p>
     * 
     * <p>TODO: listener for fully initialized module?</p>
     *
     * <p>Threads: don't call this method from a {@link LocationListener}
     * call-back or dead-lock can occur. This will be fixed in the future.</p>
     */
    void initLocationSystem();


    /**
     * <p>Temporarily suspend retrieving new positions.</p>
     * 
     * <p>Call this to save system resources (CPU, battery) when you know
     * that the user will not see or hear the application for some time.
     * For instance if the application is suspended to the background and
     * can't be resumed programmatically.</p>
     * 
     * <p>Note that this affects all listeners.</p> 
     * 
     * <p>The implementor must try to minimize power consumption for location,
     * for instance by turning off the GPS. If possible, the location system
     * should be configured to require minimal time for re-start (warm start
     * instead of cold start).
     * Since this is platform dependent, it is not possible make any
     * guarantees. This method is provided on best-effort basis.</p> 
     *
     * <p>No guarantees are made on when calls to
     * {@link LocationListener} or
     * {@link ProviderStateListener} will stop.
     * Only that they eventually will.</p>
     * 
     * <p>A call to this method does not deregister any listeners. Thus, you do
     * not need to deregister your listeners and re-register them after resume()
     * to get updates.</p>
     * 
     * <p>If suspend() has already been called , suspend() has no effect.</p>
     * 
     * <p>Threads: don't call this method from a {@link LocationListener}
     * call-back or dead-lock can occur. This will be fixed in the future.</p>
     */
    void suspend();


    /**
     * <p>Reverse the behavior of {@link LocationInterface#suspend()},
     * re-start platform and start providing updates as soon as new locations
     * arrive from the platform.</p>
     * 
     * <p>If resume() has already been called , resume() has no effect.</p>
     *
     * <p>Threads: don't call this method from a {@link LocationListener}
     * call-back or dead-lock can occur. This will be fixed in the future.</p>
     */
    void resume();


    /**
     * <p>Adds a listener for locations. Asynchronus; events will be queued up in UI Event thread
     * (callbacks made using  CallbackHandler.callOnEventDispatcher(Runnable)).
     * Call this only from UI/Main Thread.</p>
     * <p>If the last known position is good enough for criteria the listener 
     * will be notified directly. The position can be very old if the 
     * positioning has stopped, but will give the impression that system is 
     * working, on the other side if the last position had a bad accuracy 
     * the event will not be fired even if there were better positions 
     * before so cannot be used as the last known position   
     * Because of this inconsistent behavior is probably better to remove this 
     * first notification 
     * </p>
     * 
     * <p>TODO: specify behaviour if the LocationListner is already registered.
     * Instead of just ignoring the request, the listener could be re-registered
     * with the new Criteria.</p>
     * 
     * @param criteria Criteria object to filter the positions got from the LocationInterface 
     * @param locationListener The listener 
     */
    void addLocationListener(Criteria criteria, LocationListener locationListener);
    
    /**
     * Removes an asynch Location listener. If events are queued up in the event queue they
     * might execute even after this method is called. So calling this method might not immediatelly
     * stop the UI from being updated, but it guarantees that no new LocationInformation is sent to 
     * the event thread. Call this only from UI/Main Thread.
     * 
     * @param locationListener
     */
    void removeLocationListener(LocationListener locationListener);
    
    /**
     * Synchronus, location events will be called from internal thread.<br>
     * <b>WARNING!</b> Do not block this method! Locationing system may block too!
     * This method should not be used by UI.
     * 
     * <p>TODO: specify behaviour if the LocationListner is already registered.
     * Instead of just ignoring the request, the listener could be re-registered
     * with the new Criteria.</p>
     * 
     * @param criteria {@link Criteria} to filter the {@link LocationInformation} that is received
     * @param locationListener the listener to be registered
     */
    void addSyncLocationListener(Criteria criteria, LocationListener locationListener);
    
    /**   
     * Removes a synch location listener
     * @param locationListener the registered listener
     */
    void removeSyncLocationListener(LocationListener locationListener);
    
    /**
     * Add listener for {@link LocationProvider} state. The listener will be notified when a provider
     * changes state (see:  {@link LocationProvider#getState()})
     * Events will be queued in the UI Event thread 
     * @param providerStateListener the listener to be registered
     */
    void addProviderStateListener(ProviderStateListener providerStateListener);
    
    /**
     * Remove {@link LocationProvider} state listener
     * @param providerStateListener the registered listener
     */
    void removeProviderStateListener(ProviderStateListener providerStateListener);
}
