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
import com.wayfinder.core.map.MapKeyInterface;


public class MapKeyInterfaceImpl implements MapKeyInterface {

    private static final long MAX_PRESSED_TIME = 200;
    
    private MapUpdaterThread m_MapUpdater;
    private Camera m_Camera;
    private RenderManager m_RenderManager;
    
    private int m_Actions = 0;
    private int []m_PointerScreenPos    = new int[2];
    private int []m_PointerOldScreenPos = new int[2];
    private boolean m_PointerIsActive   = false;
    
    private long m_PressedTime;
    
    public MapKeyInterfaceImpl(MapUpdaterThread mapUpdater, Camera camera, RenderManager renderManager) {
        m_MapUpdater = mapUpdater;
        m_Camera = camera;
        m_RenderManager = renderManager;
    }
    
    public void resetKeyEvents() {
        m_Actions = 0;
    }
    
    
    /**
     * Return true if any key has been pressed in the map. 
     * 
     * @return
     */
    boolean isKeyIsPressed() {
        return (m_Actions != 0);
    }
    
    /*
     * Method that takes action on the key and/or motion events
     * that has been send to the map via MapKeyInterface.  
     */
    void handleAction() {
        handleKeyActions();
        if(m_PointerIsActive) {
            handlePointerDragged();
        }        
    }
    
    
    /*
     * Handle key event sent to the map. 
     */
    private boolean handleKeyActions() {
        
        m_Camera.updateFrameTimer();
        
        if((m_Actions & MapKeyInterface.ACTION_ZOOM_IN) != 0 ) {
            m_RenderManager.zoomMap(false); 
            
        } else if((m_Actions & MapKeyInterface.ACTION_ZOOM_OUT) != 0 ) {
            m_RenderManager.zoomMap(true); 
            
        } else if((m_Actions & MapKeyInterface.ACTION_MOVE_LEFT) != 0) {
            int movement = m_RenderManager.getMovement();
            m_RenderManager.moveMap(-movement, 0);
            
        } else if((m_Actions & MapKeyInterface.ACTION_MOVE_RIGHT) != 0) {
            int movement = m_RenderManager.getMovement();
            m_RenderManager.moveMap(movement, 0);
            
        } else if((m_Actions & MapKeyInterface.ACTION_MOVE_UP) != 0) {            
            int movement = m_RenderManager.getMovement();            
            m_RenderManager.moveMap(0, -movement);
            
        } else if((m_Actions & MapKeyInterface.ACTION_MOVE_DOWN) != 0) {
            int movement = m_RenderManager.getMovement();
            m_RenderManager.moveMap(0, movement);
        }
        
        return true;
    }
    
    /*
     * Handle pointer event
     */
    private void handlePointerDragged() {
        final int dx = m_PointerOldScreenPos[0] - m_PointerScreenPos[0];
        final int dy = m_PointerOldScreenPos[1] - m_PointerScreenPos[1];
        m_RenderManager.moveMap(dx, dy);        
        m_PointerOldScreenPos[0] = m_PointerScreenPos[0];
        m_PointerOldScreenPos[1] = m_PointerScreenPos[1];                
    }
    
    // -----------------------------------------------------------------------------------------------
    // MapKeyInterface Methods 
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.MapKeyInterface#actionInvoked(int)
     */
    public void actionInvoked(int anAction) {
        m_Actions |= anAction;
        m_MapUpdater.updateMap();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.MapKeyInterface#actionStopped(int)
     */
    public void actionStopped(int anAction) {
        m_Actions &= ~anAction;
        m_RenderManager.resetMovementSpeed();
        m_Camera.mapChangeNotify();
        m_MapUpdater.updateMap();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.MapKeyInterface#pointerDragged(int, int)
     */
    public void pointerDragged(int x, int y) {
        m_PointerScreenPos[0] = x;
        m_PointerScreenPos[1] = y;
        m_PointerIsActive = true;
        m_MapUpdater.updateMap();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.MapKeyInterface#pointerPressed(int, int)
     */
    public void pointerPressed(int x, int y) {
        m_PressedTime = System.currentTimeMillis();
        m_PointerScreenPos[0] = x;
        m_PointerScreenPos[1] = y;
        m_PointerOldScreenPos[0] = x;
        m_PointerOldScreenPos[1] = y;
//        m_MapUpdater.updateMap();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.MapKeyInterface#pointerReleased(int, int)
     */
    public void pointerReleased(int x, int y) {
        m_PointerIsActive = false;
        m_PointerScreenPos[0] = 0;
        m_PointerScreenPos[1] = 0;
        m_PointerOldScreenPos[0] = 0;
        m_PointerOldScreenPos[1] = 0;
        
        final long t = (System.currentTimeMillis()-m_PressedTime); 
        if(t < MAX_PRESSED_TIME) {
            m_RenderManager.setPointerPressed(x, y);
        }
        
        m_RenderManager.resetMovementSpeed();
        m_Camera.mapChangeNotify();
        m_MapUpdater.updateMap();        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.MapKeyInterface#trackBallMoved(int, int)
     */
    public void trackBallMoved(int dx, int dy) {
        m_MapUpdater.updateMap();
    }    
}
