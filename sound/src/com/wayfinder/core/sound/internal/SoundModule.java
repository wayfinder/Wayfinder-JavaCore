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

import java.util.Vector;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.PAL;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;
import com.wayfinder.pal.sound.SoundException;
import com.wayfinder.pal.sound.SoundLayer;
import com.wayfinder.pal.sound.SoundPlayer;

public class SoundModule implements InternalSoundInterface, Runnable, SoundConductor {
    
    private static final Logger LOG =
        LogFactory.getLoggerForClass(SoundModule.class);
    
    final private SoundLayer soundPal;
    
    final private ConcurrencyLayer concurentLayer;
    
    private Vector toPlayList = new Vector(10);
    
    private Vector toPrepareList = new Vector(10);
    
    private boolean isMute;

    private Thread thread;

    public static InternalSoundInterface createSoundInterface(PAL pal) {
        return new SoundModule(pal.getSoundLayer(),pal.getConcurrencyLayer());
    }
    
    private SoundModule(SoundLayer soundPal, ConcurrencyLayer concurentLayer) {
        super();
        this.soundPal = soundPal;
        this.concurentLayer = concurentLayer;
    }
    
    private synchronized void start() {
        if (thread == null) {
            thread = concurentLayer.startNewDaemonThread(this, "SoundModule");
        }
    }

    public FutureSound loadSound(String filePath, int duration) {
        SoundPlayer sp = soundPal.create(filePath, duration);
        FutureSoundImpl futureSound  = new FutureSoundImpl(this,sp);
        addToPrepareList(futureSound);
        return futureSound;
    }

    public FutureSound loadSoundSequence(String[] filePaths, int[] durations) {
        SoundPlayer sp = soundPal.create(filePaths, durations);
        FutureSoundImpl futureSound  = new FutureSoundImpl(this,sp);
        addToPrepareList(futureSound);
        return futureSound;
    } 

    public void run() {
        // wait for new in the queue or for play
        // something to play
        // if current prepared start it and wait to finish
        // if other to play unprepare the current, prepare it and wait to finish
        // play
        // remove the resources

        FutureSoundImpl currentPrepared = null;
        try {
            while (true) {
                FutureSoundImpl sound = null;
                boolean playIt = false;
                
                // at the end of this there will either a sound to play
                // either a sound to prepare which is different than previous
                // prepared
                synchronized (this) {
                    while (true) {
                        if (toPlayList.size() > 0) {
                            sound = (FutureSoundImpl) toPlayList.elementAt(0);
                            toPlayList.removeElementAt(0);
                            playIt = true;
                            break;
                        } else if (toPrepareList.size() > 0 ) {
                            sound = (FutureSoundImpl) toPrepareList.elementAt(0);
                            if (sound != currentPrepared) {
                                playIt = false;
                                break;
                            } 
                        } else if (toPrepareList.size() == 0 && currentPrepared != null) {
                            //just signal unprepare of last prepared
                            sound = null;
                            playIt = false;
                            
                        }
                        wait();
                    }
                }
                
                //now do the work: prepare or play, or both  
                try {
                    if (sound != currentPrepared) {
                        if (currentPrepared != null) {
                            currentPrepared.unprepare();
                            currentPrepared = null;
                        }
                        if (sound != null) {
                            sound.prepare();
                        }
                    } 
                    if (playIt) {
                        sound.play();//this will also unprepare it
                    } else {
                        currentPrepared = sound;
                    }
                    
                } catch (SoundException e) {
                    if(LOG.isError()) {
                        LOG.error("SoundModule.run()", e);
                    }
                    
                } catch (IllegalStateException e) {
                    if(LOG.isError()) {
                        LOG.error("SoundModule.run()", e);
                    }
                }  
            }
        } catch (InterruptedException e) {
            if(LOG.isError()) {
                LOG.error("SoundModule.run()", "interrupted " +  e);
            }
        } finally {
            if (currentPrepared != null) {
                currentPrepared.unprepare();
                currentPrepared = null;
            } 
        }
    }

    public synchronized void setMute(boolean value) {
        if (isMute == value) return;
        isMute = value;
        if (value) {
            //check if thread was started
            if (thread != null) {
                thread.interrupt();
                thread = null;
                //TODO stop the current play if any
            }
            toPlayList.removeAllElements();
        } else {
            if (toPrepareList.size() > 0) start();
        }
    }
    
    private synchronized void addToPrepareList(FutureSoundImpl futureSound) {
        toPrepareList.addElement(futureSound);//insert on last position
        if (!isMute) {
            notifyAll();
            start();
        }
    }
   
    //SoundConductor
    public synchronized boolean cancel(FutureSoundImpl futureSound) {
        boolean result = (toPrepareList.removeElement(futureSound) ||
                toPlayList.removeElement(futureSound));    
        if (result && !isMute) {
            notifyAll();
        }
        return result;
    }

    public synchronized void post(FutureSoundImpl sound) {
        if (toPrepareList.removeElement(sound) && !isMute) {
            toPlayList.addElement(sound);
            notifyAll();
        }
    }
      
}
