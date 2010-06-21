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
package com.wayfinder.core.positioning.internal;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.ModuleData;
import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.network.internal.mc2.MC2Interface;
import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationInterface;
import com.wayfinder.core.positioning.LocationListener;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.positioning.ProviderStateListener;
import com.wayfinder.core.positioning.internal.vfcellid.CellIdUpdater;
import com.wayfinder.core.shared.util.ListenerList;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.positioning.PositionProviderInterface;
import com.wayfinder.pal.positioning.PositioningLayer;

/**
 * 
 * 
 * 
 *
 */
public class LocationModule implements LocationInterface, ProviderUpdatesListener {
    
    private static final Logger LOG = LogFactory.getLoggerForClass(LocationModule.class);
    
    private static final int POSITIONING_INITIALIZING = 0;
    private static final int POSITIONING_SUSPENDED = 1;
    private static final int POSITIONING_RUNNING = 2;
    
    private final ModuleData m_moduleData;
    private final PositioningLayer m_posLayer;
    private final SharedSystems m_systems;
    private final MC2Interface m_mc2Ifc;

    private ListenerList m_asynchLocationListeners;
    private ListenerList m_synchLocationListeners;
    private ListenerList m_stateListeners;

    private InternalLocationProvider[] m_providers;
    
    private InternalLocationProvider m_currentProvider;
    
    private LocationEventRunnable m_lastLocationEventPosted;
    private StatusEventRunnable m_lastStatusEventPosted;
    
    private int m_moduleState = POSITIONING_INITIALIZING;

    private CellIdUpdater m_vfcell;
    
    /**
     * 
     */
    private LocationModule(ModuleData moduleData, SharedSystems sys) {
        m_asynchLocationListeners = new ListenerList();
        m_synchLocationListeners = new ListenerList();
        m_stateListeners = new ListenerList();
        m_moduleData = moduleData;
        m_posLayer = m_moduleData.getPAL().getPositioningLayer();
        m_systems = sys;
        m_mc2Ifc = sys.getMc2Ifc();
    }
    
    public static LocationInterface createLocationInterface(
            ModuleData moduleData, SharedSystems sys) {
        return new LocationModule(moduleData, sys);
    }
    
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationInterface#initLocationSystem()
     */
    public void initLocationSystem() {
        if (m_moduleState == POSITIONING_INITIALIZING) {
            m_currentProvider = null;

            PositionProviderInterface[] posProviders = 
                m_moduleData.getPAL().getPositioningLayer().getPositionProviders();

            int totalNbrProviders = posProviders.length + 1; //add the VF cell id at the end

            m_providers = new InternalLocationProvider[totalNbrProviders];

            for (int i = 0; i < posProviders.length; i++) {
                m_providers[i] = 
                    InternalLocationProvider.createLocationProvider(
                            mapProviderType(posProviders[i]), posProviders[i]);
                posProviders[i].setUpdatesHandler(m_providers[i]);
                m_providers[i].setProviderUpdatesListener(this);
            }

            m_vfcell = new CellIdUpdater(
                    m_mc2Ifc, 
                    m_systems.getPAL().getNetworkLayer().getNetworkInfo(), 
                    m_systems.getWorkScheduler());
            m_providers[totalNbrProviders - 1] = 
                InternalLocationProvider.createLocationProvider(
                        InternalLocationProvider.PROVIDER_TYPE_NETWORK_VF, m_vfcell);
            m_vfcell.setUpdatesHandler(m_providers[totalNbrProviders - 1]);
            m_providers[totalNbrProviders - 1].setProviderUpdatesListener(this);
        }
    }
    
    // better make sure
    private int mapProviderType(PositionProviderInterface platformProvider) {
        switch (platformProvider.getType()) {
        case PositionProviderInterface.TYPE_INTERNAL_GPS:
            return LocationProvider.PROVIDER_TYPE_INTERNAL_GPS;
            
        case PositionProviderInterface.TYPE_EXTERNAL_GPS:
            return LocationProvider.PROVIDER_TYPE_EXTERNAL_GPS;
            
        case PositionProviderInterface.TYPE_CABLE_GPS:
            return LocationProvider.PROVIDER_TYPE_CABLE_GPS;
            
        case PositionProviderInterface.TYPE_NETWORK:
            return LocationProvider.PROVIDER_TYPE_NETWORK;
            
        default:
            return LocationProvider.PROVIDER_TYPE_SIMULATOR;
        }
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationInterface#resume()
     */
    public void resume() {
        if (m_moduleState == POSITIONING_SUSPENDED
                || m_moduleState == POSITIONING_INITIALIZING) {
            m_moduleState = POSITIONING_RUNNING;
            
            // resume updates at PAL level
            m_posLayer.resumeUpdates();
            
            // VF cell ID is started separately
            m_vfcell.resumeUpdates();
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationInterface#suspend()
     */
    public void suspend() {
        m_moduleState = POSITIONING_SUSPENDED;
        
        // stop updates at PAL level
        m_posLayer.stopUpdates();
        
        // stop VF cell id
        m_vfcell.stopUpdates();
    }
    
    public void addLocationListener(Criteria criteria, 
            final LocationListener locationListener) {
        ListenerWrapper wr = new ListenerWrapper(locationListener, criteria);
        if (m_asynchLocationListeners.add(wr)) {
            //provide last known position
            //this can be very old if the positioning has stopped 
            //but will give the impression that system is working
            //on the other side if the last position had a bad accuracy 
            //the event will not be fired even if there were better positions 
            //before so cannot be used as the last known position   
            //Because of this inconsistent behavior is probably better to 
            //remove this call
            
            InternalLocationProvider provider = m_currentProvider;
            if (provider != null) {
                LocationInformation loc = provider.getLastKnownLocation();
                if (loc != null && loc.isAtLeastAsGoodAs(criteria)) {
                    //This method will be called form UI/Main thread only
                    //so we can call the event directly 
                    locationListener.locationUpdate(
                            loc, provider);
                }
            }
        }
    }


    public void removeLocationListener(LocationListener locationListener) {
        // it's ok to do it like this
        // see the definition of ListenerWrapper.equals()
        ListenerWrapper wr = new ListenerWrapper(locationListener, null);
        m_asynchLocationListeners.remove(wr);
    }
    
    public void addSyncLocationListener(Criteria criteria, LocationListener locationListener) {
        ListenerWrapper wr = new ListenerWrapper(locationListener, criteria);
        //ListenerList take care of synchronization
        m_synchLocationListeners.add(wr);
        //don't provide last know location as for asynch Listeners
    }


    public void removeSyncLocationListener(LocationListener locationListener) {
        ListenerWrapper wr = new ListenerWrapper(locationListener, null);
        //ListenerList take care of synchronization
        m_synchLocationListeners.remove(wr);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationInterface#addProviderListener(com.wayfinder.core.positioning.ProviderListener)
     */
    public void addProviderStateListener(ProviderStateListener providerListener) {
        m_stateListeners.add(providerListener);
    }

    
    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.LocationInterface#removeProviderListener(com.wayfinder.core.positioning.ProviderListener)
     */
    public void removeProviderStateListener(ProviderStateListener providerListener) {
        m_stateListeners.remove(providerListener);
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.internal.ProviderUpdatesListener#locationUpdated(com.wayfinder.core.positioning.LocationProvider, com.wayfinder.core.positioning.LocationInformation)
     */
    public void locationUpdated(InternalLocationProvider provider,
            LocationInformation location) {
        if (m_moduleState == POSITIONING_SUSPENDED) {
            if (LOG.isDebug()) {
                LOG.debug("LocationModule.locationUpdated()", "module in suspended state!");
            }
            return;
        }
        
        if (LOG.isDebug()) {
            LOG.debug("LocationModule.locationUpdated()", "to: "+location+" by "+provider+", crt provider is: "+m_currentProvider);
        }
        
        //the first provider to send a position will be set as current:
        if (m_currentProvider == null) {
            m_currentProvider = provider;
            updateAllLocationListeners(location, provider);
            if (m_currentProvider.getState() == LocationProvider.PROVIDER_STATE_AVAILABLE) {
                stopOtherProviders(m_currentProvider);
            }
        }
        else {
            if (m_currentProvider != provider) {
                // If another provider than the current one offers better 
                // positions at this time, set that as current provider
                // and update listeners with the position it sends.
                // For example, the current provider is GPS, but it's 
                // unavailable. A cell id provider has an updated position,
                // so it should be used because it's available.
                if (provider.isCurrentlyBetter(m_currentProvider)) {
                    if (LOG.isInfo()) {
                        LOG.info("LocationModule.locationUpdated()", 
                                "found better provider, set as crt: "+provider);
                    }
                    m_currentProvider = provider;
                    updateAllLocationListeners(location, provider);
                    if (m_currentProvider.getState() == LocationProvider.PROVIDER_STATE_AVAILABLE) {
                        stopOtherProviders(m_currentProvider);
                    }
                }
            }
            else {
                // just an update of the current provider
                updateAllLocationListeners(location, provider);
            }
        }
    }
    
    private void updateAllLocationListeners(LocationInformation loc, 
            LocationProvider provider) {
        CallbackHandler callHandler = m_moduleData.getCallbackHandler();
        if(LOG.isDebug()) {
            LOG.debug("LocationModule.updateAllLocationListeners()",
                      "sending " + loc + " with " + loc.getMC2Position()
                      + " via " + callHandler);
        }

        //check first if there are any asynchronous listeners
        if (!m_asynchLocationListeners.isEmpty()) {
            //cancel last position notification if was not executed yet
            //maybe we should cancel anyway as it will be an old position
            if (m_lastLocationEventPosted!=null) m_lastLocationEventPosted.cancel();
            //notify all listeners in a single callback
            m_lastLocationEventPosted = new LocationEventRunnable(loc,provider);
            callHandler.callInvokeCallbackRunnable(m_lastLocationEventPosted);
        }
        
        //get the actual array of listeners  
        Object[] synchListeners = m_synchLocationListeners.getListenerInternalArray();
        
        //no synchronization needed as the array will not be changed 
        for(int i = 0; i < synchListeners.length; i++) {
            ListenerWrapper wr = (ListenerWrapper) synchListeners[i];
            if (loc.isAtLeastAsGoodAs(wr.m_criteria)) {
                wr.m_listener.locationUpdate(loc, provider);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.positioning.internal.ProviderUpdatesListener#stateUpdated(com.wayfinder.core.positioning.LocationProvider, int)
     */
    public void stateUpdated(InternalLocationProvider provider, int state) {
        if (m_moduleState == POSITIONING_SUSPENDED) {
            if (LOG.isDebug()) {
                LOG.debug("LocationModule.stateUpdated()", "module in suspended state!");
            }
            return;
        }
        
        if (LOG.isInfo()) {
            LOG.info("LocationModule.stateUpdated()", "provider: "+provider);
        }
        
        if (m_currentProvider != null && provider == m_currentProvider) {
            if (state != LocationProvider.PROVIDER_STATE_AVAILABLE) {
                // the current provider has changed state to unavailable,
                // so try and start other providers
                startOtherProviders(m_currentProvider);
            }
            else {
                stopOtherProviders(m_currentProvider);
            }
        }
        updateAllStateListeners(provider);
    }
    
    private void updateAllStateListeners(final LocationProvider provider) {
        CallbackHandler callHandler = m_moduleData.getCallbackHandler();

        if (!m_stateListeners.isEmpty()) {
            //cancel last status notification if was not executed yet
            if (m_lastStatusEventPosted!=null) m_lastStatusEventPosted.cancel();
            //notify all listeners in a single callback
            m_lastStatusEventPosted = new StatusEventRunnable(provider);
            callHandler.callInvokeCallbackRunnable(m_lastStatusEventPosted);
        }
    }
    
    /**
     * Stops other providers if they might not provide better accuracy
     * (e.g. the new current provider is a GPS type provider, in which case
     * cellID type providers are not useful anymore; however if the current
     * provider is a cellID type, then do not stop GPS-type providers even if
     * they currently don't supply positions or are unavailable, as they're 
     * corresponding providers at platform level might become available and
     * we need to be notified of any change in status from them)
     * @param currentProvider
     */
    private void stopOtherProviders(InternalLocationProvider currentProvider) {
        if (LOG.isWarn()) {
            LOG.warn("LocationModule.stopOtherProviders()", "crt: "+currentProvider);
        }
        for (int i = 0; i < m_providers.length; i++) {
            if (LOG.isDebug()) {
                LOG.debug(
                        "LocationModule.stopOtherProviders()", 
                        "crt: "+currentProvider+
                        ", other: "+m_providers[i]+
                        ", crt is usually better: "+currentProvider.isUsuallyBetter(m_providers[i]));
            }
            
            if (m_providers[i] != currentProvider 
                    && m_providers[i] != null
                    && currentProvider.isUsuallyBetter(m_providers[i])) {
                m_providers[i].suspend();
            }
        }
    }

    private void startOtherProviders(InternalLocationProvider currentProvider) {
        if (LOG.isWarn()) {
            LOG.warn("LocationModule.startOtherProviders()", "crt: "+currentProvider);
        }
        for (int i = 0; i < m_providers.length; i++) {
            if (m_providers[i] != currentProvider) {
                m_providers[i].resume();
            }
        }
    }
    
    
    private static class ListenerWrapper {
        private final LocationListener m_listener;
        private final Criteria m_criteria;
        /**
         * @param listener
         * @param criteria
         */
        public ListenerWrapper(LocationListener listener, Criteria criteria) {
            m_listener = listener;
            m_criteria = criteria;
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof ListenerWrapper) {
                ListenerWrapper wrap = (ListenerWrapper) obj;
                return m_listener == wrap.m_listener;
            }
            return false;
        }
        
        public int hashCode() {
            return m_listener.hashCode();
        }
        
        public String toString() {
            return "ListenerWrapper " + m_listener.toString() + " " + m_criteria.toString(); 
        }
    }
    
    private abstract class EventRunnable implements Runnable{
        volatile boolean m_isCancel;
        protected final LocationProvider m_provider;
        
        protected EventRunnable(LocationProvider provider) {
            m_provider = provider;
        }
        
        public void cancel() {
            m_isCancel = true;
        }
    }

    private class LocationEventRunnable extends EventRunnable {
        private final LocationInformation m_loc;
        
        public LocationEventRunnable(LocationInformation loc,
                LocationProvider provider) {
            super(provider);
            m_loc = loc;
        }

        public void run() {
            if (m_isCancel) return;

            if(LOG.isDebug()) {
                LOG.debug("LocationEventRunnable.run()", "notify " + m_asynchLocationListeners.toString() );
            }

            Object[] asynchListeners = m_asynchLocationListeners.getListenerInternalArray();
            //no synchronization needed as the array will not be changed
            for(int i = 0; i < asynchListeners.length; i++) {
                ListenerWrapper wr = (ListenerWrapper)asynchListeners[i];
                if (m_loc.isAtLeastAsGoodAs(wr.m_criteria)) {
                    wr.m_listener.locationUpdate(m_loc, m_provider);
                }
            }
        }
    }
    
    private class StatusEventRunnable extends EventRunnable {
        
        public StatusEventRunnable(LocationProvider provider) {
            super(provider);
        }

        public void run() {
            if (m_isCancel) return;
            
            if(LOG.isDebug()) {
                LOG.debug("StatusEventRunnable.run()", "notify " + m_stateListeners.toString() );
            }
            
            Object[] statusListeners = m_stateListeners.getListenerInternalArray();
            
            for (int i = 0; i < statusListeners.length; i++) {
                ((ProviderStateListener) statusListeners[i]).providerStateChanged(m_provider);
            }
        }
        
    }
}
