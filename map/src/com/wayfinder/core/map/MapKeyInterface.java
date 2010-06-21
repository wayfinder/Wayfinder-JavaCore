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
package com.wayfinder.core.map;

/**
 * Interface for interacting with the map through key inputs
 *
 */

public interface MapKeyInterface {
    
    public static final int ACTION_ZOOM_IN      = 0x1<<0;
    public static final int ACTION_ZOOM_OUT     = 0x1<<1;
    
    public static final int ACTION_MOVE_LEFT    = 0x1<<2;
    public static final int ACTION_MOVE_RIGHT   = 0x1<<3;
    public static final int ACTION_MOVE_UP      = 0x1<<4;
    public static final int ACTION_MOVE_DOWN    = 0x1<<5;
    
    /**
     * Invokes an action
     * 
     * @param action  the action to be invoked
     */
    public void actionInvoked(int action);
    
    /**
     * Stops an action
     * 
     * @param action  the action to be stopped
     */
    public void actionStopped(int action);
    
    /**
     * Called when the pointer is pressed
     * 
     * @param x  the x coordinate of the pointer
     * @param y  the y coordinate of the pointer
     */
    public void pointerPressed(int x, int y);
    
    /**
     * Called when the pointer is dragged
     * 
     * @param x  the x coordinate of the pointer
     * @param y  the y coordinate of the pointer
     */
    public void pointerDragged(int x, int y);
    
    /**
     * Called when the pointer is released
     * 
     * @param x  the x coordinate of the pointer
     * @param y  the y coordinate of the pointer
     */
    public void pointerReleased(int x, int y);
    
}
