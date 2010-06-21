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
package com.wayfinder.core.sound.internal;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.sound.SoundException;
import com.wayfinder.pal.sound.SoundPlayer;

public class FutureSoundImpl implements FutureSound {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(FutureSoundImpl.class);
    
    private SoundConductor conductor;
    
    private SoundPlayer soundPlayer;
    
    private volatile boolean isCancel; 
    
    public FutureSoundImpl(SoundConductor conductor, SoundPlayer soundPlayer) {
        this.conductor = conductor;
        this.soundPlayer = soundPlayer;
    }

    public boolean cancel() {
        isCancel = true;
        return conductor.cancel(this);
    }

    public void start() {
        conductor.post(this);
    }
    
    public int getDuration() {
        if(LOG.isTrace()) {
            LOG.trace("FutureSoundImpl.getDuration()", soundPlayer.toString());
        }
        
        return soundPlayer.getDuration();
    }
    
    public String toString() {
        return soundPlayer.toString();
    }
    
    //internal methods
    void prepare() throws SoundException, InterruptedException {
        if (isCancel) return;
        if(LOG.isTrace()) {
            LOG.trace("FutureSoundImpl.prepare()", soundPlayer.toString());
        }
        soundPlayer.prepare();
    }
    
    void unprepare() {
        if(LOG.isTrace()) {
            LOG.trace("FutureSoundImpl.unprepare()", soundPlayer.toString());
        }
        soundPlayer.unprepare();
    }
    
    void play() throws SoundException, InterruptedException {
        if (isCancel) {
            if(LOG.isTrace()) {
                LOG.trace("FutureSoundImpl.play.unprepare()", soundPlayer.toString());
            }
            soundPlayer.unprepare();
        } else {
            if(LOG.isTrace()) {
                LOG.trace("FutureSoundImpl.play()", soundPlayer.toString());
            }
            soundPlayer.play();
        }
    }
}
