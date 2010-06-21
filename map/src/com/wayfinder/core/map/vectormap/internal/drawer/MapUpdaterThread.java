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
package com.wayfinder.core.map.vectormap.internal.drawer;

import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapDrawerInterface;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;

/**
 * 
 * The class updates the map, see the updateMap() method for more info.
 * 
 * 
 * 
 */
public class MapUpdaterThread {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(MapUpdaterThread.class);

    private static Object UPDATE_MONITOR = new Object();
    /* Wait minimum 30 ms between two map updates */ 
    private static final int DEFAULT_TIMEOUT = 30;
    private static Object WAIT_OBJECT = new Object();

    private MapDrawerInterface m_MapCanvas = null;
    private RenderManager m_RenderManager = null;
    private MapCameraInterface m_Camera = null;

    private boolean m_IsRunning = true;
    private boolean m_Visible = false;

    private boolean m_NeedUpdate;

    public MapUpdaterThread(Camera camera) {
        m_Camera = camera;
    }

    public void init(MapDrawerInterface aMapCanvas,
            RenderManager aRenderManager, MapKeyInterfaceImpl mapKeyInterface,
            ConcurrencyLayer currlayer) {
        m_MapCanvas = aMapCanvas;
        m_RenderManager = aRenderManager;
        startThread(currlayer);
    }

    public void setVisible(boolean aVisible) {
        m_Visible = aVisible;
    }

    public void setMapDrawerInterface(MapDrawerInterface aMapCanvas) {
        m_MapCanvas = aMapCanvas;
    }

    /**
     * Notify a map update
     * 
     * @param menuIsOpen
     *            , true if the menu are up and we just want to update the
     *            overlay info. false if we want to render/update the map and
     *            draw the overlay info
     */
    public void updateMap() {
//        LOG.debug("UpdateThread", "Update requested");
        m_NeedUpdate = true;
        notifyUpdate();
    }

    /**
     * Check if the map needs to be updated. This method is used to check if a
     * request has been maid when the map is locked due to rendering.
     */
    void updatedIfNecessary() {
    }

    /*
     * Notify the thread...
     */
    private void notifyUpdate() {
        synchronized (UPDATE_MONITOR) {
            UPDATE_MONITOR.notifyAll();
        }
    }

    /**
     * Close the map updater thread.
     */
    public void close() {
        m_IsRunning = false;
        m_Visible = false;
        notifyUpdate();
    }

//    private int m_updateCounter;

    /*
     * Start the map updating thread
     */
    private void startThread(ConcurrencyLayer currLayer) {

        Runnable updateRunner = new Runnable() {

            /**
             * The method update the map.
             * 
             * Note that the iUpdateMap and iUpdateSpinningLogo are volatile so
             * they will not be cached in the registers. So no need to
             * synchronize.
             * 
             * 1.0: While the thread is alive.
             * 
             * 2.0: While the map is visible and the map needs to be updated
             * 
             * 2.1: If the map needs to be updated, the map and the overlay
             * information will be updated. 2.2: If the map doesn't need to be
             * updated and the map is downloading tiles. We update the spinning
             * logo.
             * 
             * 3.1: If the any key is pressed or the update map event has been
             * triggered we wait DEFAULT_TIMEOUT and update the map again. 3.2:
             * If just the spinning logo needs to be updated we wait
             * SPIN_LOGO_TIMEOUT and update the spinning logo again.
             * 
             * 4.0: If no new event has happen we put the map update thread to
             * wait until the map needs to be updated again.
             * 
             * 5.0 Rewritten... TODO
             * 
             */
            public void run() {
                while (m_IsRunning) {

                    /* Put the thread to wait when no new event has happen. */
                    synchronized (UPDATE_MONITOR) {
                        try {
                            if (!m_NeedUpdate) {
                                UPDATE_MONITOR.wait();
                            }
                        } catch (InterruptedException e) {
                            if (LOG.isError()) {
                                LOG.error(
                                        "MapUpdaterThread.internalUpdateMap()",
                                        "e2= " + e);
                            }
                        }
                    }

                    if (m_Visible) {
                        if (!m_RenderManager.isMapLocked()) {
                            // Update the map
//                            LOG.debug("UpdateThread", "Run update" + ++m_updateCounter);
                            m_NeedUpdate = false;
                            if (m_RenderManager.updateMap()) {
                                m_MapCanvas.updateScreen(m_RenderManager,
                                        m_Camera);
                            }
                        }
                    }

                    /* Wait iTimeout ms between the map updates */
                    synchronized (WAIT_OBJECT) {
                        try {
                            WAIT_OBJECT.wait(DEFAULT_TIMEOUT);
                        } catch (Exception e) {
                            if (LOG.isError()) {
                                LOG.error(
                                        "MapUpdaterThread.internalUpdateMap()",
                                        e);
                            }
                        }
                    }
                }
            }
        };
        Thread t = currLayer.startNewDaemonThread(updateRunner, "TMUpdater");
        t.setPriority(Thread.MAX_PRIORITY);
    }

}
