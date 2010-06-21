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
package com.wayfinder.core.route.internal;

import java.io.IOException;

import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationListener;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.positioning.internal.InternalLocationProvider;
import com.wayfinder.core.route.internal.nav2route.Nav2Route;
import com.wayfinder.core.route.internal.nav2route.Nav2RouteIterator2;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;

/**
 * <p>Walk thru a route and return the coordinates one after one taking into
 * account the speed limit of the current road segment.</p>
 *
 * <p>This class extends {@link LocationProvider} for easy interfacing with
 * the RouteModule. Because feeding the position into the route module and
 * evaluate it is the easiest way to obtain all the forward information needed
 * to fill the {@link NavigationInfo} object.</p>
 * 
 * <p>Care must be taken so that the positions generated here do not leak
 * outside of RouteModule. Especially, they must never enter the
 * Positioning/Location module. There are numerous problems in the jWMMG
 * codebase related to this which requires a lot of UI code to check if
 * simulation is active before working with a positon - e.g. sending it
 * to the service window.</p>
 * 
 * <p>This class is NOT thread safe. Only one thread and the object's internally
 * created working threads must access it.</p>
 * 
 * <p><u>TODO</u>
 * <ol><li>Refactor the internal interface in RouteModule so that this class
 *         doesn't need to extend LocationProvider and thus clearly
 *         differentiate it from the Location module. We could add a separate
 *         method processSimulatedPosition(Position) to RouteModule.</li>
 *     <li>This object is re-usable (by means of start(Nav2Route)) but this
 *         opens up to misuse. For instance, there is no automatic stopping
 *         of the old simulation and you can get two threads working the same
 *         route. It would be better to create a new simulator for each new
 *         simulating run. The overhead of object creation is not an issue
 *         compared to the work done by actual simulating.</li>
 *     <li>Use the work scheduler instead of creating a own thread.</li>
 *     <li>Make the class thread safe.</li>
 * </ol></p>
 * 
 * 
 * 
 *
 */
class SimpleRouteSimulator extends InternalLocationProvider implements Runnable {
    
    private static final Logger LOG = 
        LogFactory.getLoggerForClass(SimpleRouteSimulator.class);
    
    //private final RouteModule m_module;
    
    private LocationListener m_listener;
    
    private boolean m_isRunning = false;
    private Nav2Route m_route;
    private Nav2RouteIterator2 m_startIter, m_crtIter;

    private int m_speedMPS;

    private int m_seglengthM;

    private int m_x;

    private int m_y;
    
    private double m_courseRad;
    
    public SimpleRouteSimulator(RouteModule module) {
        super(PROVIDER_TYPE_SIMULATOR, null);
        //m_module = module;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (m_crtIter != null && m_startIter != null) {
            long lastTime = System.currentTimeMillis();
            while (m_isRunning) {
                //m_crtIter.resetFrom(m_startIter);
                long dtms = System.currentTimeMillis() - lastTime;

                if (m_speedMPS == 0) {
                    m_speedMPS = 20;
                }

                // how far have we travelled (meters), dt * speed
                int distance;
                distance = (int) ((m_speedMPS * dtms) / 1000);

                if (distance < m_seglengthM) { // will ignore 0 length segments
                    // get difference x-, y-wise
                    // dx = distance * sin (course0r) (meters)
                    // dy = distance * cos (course0r) (meters)
            
                    int x = m_x + (int) (distance * Math.sin(m_courseRad));
                    int y = m_y + (int) (distance * Math.cos(m_courseRad));

                    int mc2lon = m_crtIter.getCurrentMiniMap().xToMC2(x);
                    int mc2lat = m_crtIter.getCurrentMiniMap().yToMC2(y);
                    
                    LocationInformation loc = 
                        new LocationInformation(
                                mc2lat, 
                                mc2lon, 
                                Criteria.ACCURACY_EXCELLENT, 
                                m_speedMPS, 
                                (short)Math.toDegrees(m_courseRad), 
                                0, System.currentTimeMillis());
                    
                    if (m_listener != null) {
                        m_listener.locationUpdate(loc, this);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        if (LOG.isDebug()) {
                            LOG.debug("SimpleRouteSimulator.run()", "simulator thread interrupted");
                        }
                    }

                } // not past segment end

                else {

                    // the iterator is at the end of the segment ==
                    // the beginning of the segment we're going to
                    m_x = m_crtIter.getX();
                    m_y = m_crtIter.getY();
                    m_speedMPS = (int)(m_crtIter.getSpeedLimitKmh() / 3.6f);
                    try {
                        m_crtIter.nextPoint(false);
                    }
                    catch (IOException ioe) {
                        
                    }
                    if (m_crtIter.isValid()) {
                        // new length, course is valid
                        m_seglengthM = m_crtIter.getSegmentLengthMeters();
                        m_courseRad = m_crtIter.getSegmentCourseRad();
                    }
                    else {
                        m_isRunning = false;
                    }

                    lastTime = System.currentTimeMillis();
                }
            }
        }
    }
    
    public void start(Nav2Route nav2Route, LocationListener listener, ConcurrencyLayer concurrencyLayer) {
        m_listener = listener;
        m_route = nav2Route;
        m_isRunning = true;
        //setState(PROVIDER_STATE_AVAILABLE);
        try {
            m_startIter = m_route.newIterator2();
            m_crtIter = m_route.newIterator2();
        }
        catch (IOException ioe) {
            if (LOG.isError()) {
                LOG.error("RouteModuleDUMMY.SimpleRouteSimulator.start(nav2Route)", ioe.toString());
            }
        }

        concurrencyLayer.startNewDaemonThread(this, "RouteSimulator");
    }
    
    public void stop() {
        if (m_isRunning) {
            m_isRunning = false;
            //setState(PROVIDER_STATE_OUT_OF_SERVICE);
        }
    }
    
    public boolean isRunning() {
        return m_isRunning;
    }
}
