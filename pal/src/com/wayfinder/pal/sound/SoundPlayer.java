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
package com.wayfinder.pal.sound;

public interface SoundPlayer {
    
    /**
     * The returned value indicating that the requested time is unknown.
     */
    static final long TIME_UNKNOWN = -1;
    
    /**
     * Play the sound synchronously. 
     * This will block until the sound is played
     *  
     * @throws SoundException
     * @throws InterruptedException 
     */
    void play() throws SoundException, InterruptedException;
    

    /**
     * Allocate the resources in order that the sound can be played immediately .
     * Call when the sound is going to be played shortly. 
     * 
     * @throws SoundException - if the SoundPlayer cannot be prepared
     * @throws InterruptedException 
     * @throws IllegalStateException - if the SoundPlayer has been destroyed
     * @throws SecurityException - if the caller does not have security 
     * permission to prepare the SoundPlayer
     */
    void prepare() throws SoundException, InterruptedException;

    /**
     * Resets the SoundPlayer to its unprepared state. 
     * After calling this method, call again the prepare method. 
     * This deallocated all resources
     */
    void unprepare();
    
    /**
     * Can be called only after prepare
     * 
     * @return the length of the sound in milliseconds 
     * @throws IllegalStateException if the SoundPlayer has been destroyed
     * 
     * Notice: the duration is in milliseconds and not microseconds as in jsr135
     */
    int getDuration();
}
