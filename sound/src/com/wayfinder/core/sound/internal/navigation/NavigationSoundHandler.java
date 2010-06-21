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
package com.wayfinder.core.sound.internal.navigation;

import com.wayfinder.core.sound.internal.FutureSound;
import com.wayfinder.core.sound.internal.InternalSoundInterface;
import com.wayfinder.core.sound.internal.voicesyntax.SyntaxResponse;
import com.wayfinder.core.sound.internal.voicesyntax.SyntaxTree;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.NavigationInfoListener;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.Waypoint;

public class NavigationSoundHandler implements NavigationInfoListener {
    
    private static final Logger LOG =
        LogFactory.getLoggerForClass(NavigationSoundHandler.class);
    
    // if we are more than this distance passed the trigger point,
    // skip posting the sound batch
    private static final int MAX_DISTANCE_PASSED_TRIGGER_POINT = 50;
    
    private static final int NO_DISTANCE = -1;
    
    private final int SECONDARY_MAXIM_DISTANCE = 150;
    
    private final int[] m_turnDistances;
    
    private final SyntaxTree m_syntaxTree;
    
    private final InternalSoundInterface m_soundPool;
    
    //stuff that change
    private Waypoint m_nextWpt;
    private int m_distToNextWaypoint;
    
    // current active turn instruction
    private FutureSound m_currentTurnInstruction;
    private SyntaxResponse m_currentTurnResponse;
    private int m_currentTurnDistance = NO_DISTANCE;
    private int m_currentTurnDistanceIndex; 

    // true if we should load a new turn instruction
    private boolean m_loadTurnInstruction = false;
    
    private boolean m_speedCamera;

    private Route m_currentRoute;
    
    
    public NavigationSoundHandler(InternalSoundInterface soundPool, 
            SyntaxTree syntaxTree, int[] turnDistances) {
        super();
        if (syntaxTree == null || turnDistances == null) {
            throw new IllegalArgumentException("NavigationSoundHandler ctr all parameters must not be null");
        }
        m_turnDistances = turnDistances;
        m_syntaxTree = syntaxTree;
        m_soundPool = soundPool;
        m_currentTurnDistanceIndex = turnDistances.length;
    }

    
    void resetRoute() {
        resetWaypoint();
        m_speedCamera = false;
        m_currentRoute = null;
    }
    
    private void resetWaypoint() { 
        cancelCurrenInstruction();
        m_currentTurnResponse = null;
        m_currentTurnDistance = NO_DISTANCE;
        m_currentTurnDistanceIndex = m_turnDistances.length;
        
        m_nextWpt = null;
        m_loadTurnInstruction = false;
        //just to be safe
        m_distToNextWaypoint = NO_DISTANCE;
    }

    /**
     * Get the distance to be used in the next turn instruction, according
     * to the last turn distance index and distance 
     * 
     * @return the distance
     */
    private int getNextTurnInstructionDistance(int currentDistance) {
        //decrease the index from where we start to search 
        m_currentTurnDistanceIndex--;
        //we search in reverse order from the longer distance to 0
        for (int i = m_currentTurnDistanceIndex; i >= 0; i--) {
            int turnDistance = m_turnDistances[i];
            if (turnDistance != NO_DISTANCE && turnDistance <= currentDistance) {
                //mark last used index to no be fired twice
                m_currentTurnDistanceIndex = i; 
                return turnDistance;
            }
        }
        //no distance where found which 
        return NO_DISTANCE;
    }
    
    /**
     * Get the distance to be used in the next turn instruction which is less 
     * than given distance 
     * 
     * @return the turn distance or NO_DISTANCE if there is none
     */
    private int getTurnInstructionDistance(int distance) {
        //we search in reverse order from the longer distance to 0
        for (int i = m_turnDistances.length-1; i >= 0; i--) {
            int turnDistance = m_turnDistances[i];
            if (turnDistance != NO_DISTANCE && turnDistance <= distance) {
                //TODO old code mark this a used to no be fired twice
                return turnDistance;
            }
        }
        //no distance where found which is impossible as currentDistance
        //cannot be negative
        return NO_DISTANCE;
    }
    
    public void navigationInfoUpdated(NavigationInfo info) {
      
        /*
         * avoid exceptions, see
         * NavDataListener.updateNavigationInformation()
         *
         * assume that we don't need to remember error state and can
         * retry again on the nav update.
        */
        try {
            update(info);
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("NavigationSoundHandler.navigationInfoUpdated()" , e);
            }
            
        }
        
    }

    private void loadAndPlay(int macroType) {
        SyntaxResponse response = m_syntaxTree.getSyntaxResponse(
                macroType, null,0,null,0,0);
        if (response != null) {
            
            //TODO add a better method here that load and play the sound
            FutureSound sound = m_soundPool.loadSoundSequence(
                    response.getSoundsInternalArray(),
                    response.getDurationsInternalArray());
            sound.start();
        } else {
            if(LOG.isWarn()) {
                LOG.warn("SyntaxTree.getSyntaxResponse()", 
                        "No sound to play! for type=" + macroType);
            }
        }
    }
    
    private void cancelCurrenInstruction() {
        if (m_currentTurnInstruction != null) {
            m_currentTurnInstruction.cancel();
            m_currentTurnInstruction = null;
        }
    }
    
    private void update(NavigationInfo info) {
        
        if(LOG.isDebug()) {
            LOG.debug("NavigationSoundHandler.navigationInfoUpdated()", info.toString());
        }
        
        //check if route is following if not release resources
        if (!info.isFollowing()) {
            resetRoute(); 
            //check if destinationReached if so play it
            //assume that destination reached will only be sent once
            //release currentTurnInstruction
            if (info.isDestinationReached()) {
                loadAndPlay(SyntaxTree.TYPE_DESTINATION_REACHED);
                // don't play any more turn instructions after this
            }
            // don't play any turn instructions
            return;
        }
        
        if (info.getRoute() != m_currentRoute) {
            resetRoute();
            m_currentRoute = info.getRoute();
        }

        //TODO check if route has changed 
        //if so reset reset all; possible not necessary as turn has changed also
        
        //listen for speed cameras 
        //take care to play them only once
        if (info.isSpeedCameraActive() && !m_speedCamera) {
            loadAndPlay(SyntaxTree.TYPE_SPEED_CAMERA);
            //do not return play the rest as usual 
        }
        m_speedCamera = info.isSpeedCameraActive();
        
        
        //check if offtrack if so play it
        //assume that route follower will send only once offtrack
        //release currentTurnInstruction
        if (info.isOfftrack()) {
            //don't reset the waypoint as we could be back on track 
            //resetWaypoint();
            loadAndPlay(SyntaxTree.TYPE_OFF_TRACK);
            // don't play any more turn instructions after this
            return;
        }
        
        //check if waypoint is null if so nothing to do
        if (info.getNextWpt() == null) {
            resetWaypoint();
            return;
        }
        
        //check if waypoint has changed
        if (m_nextWpt != info.getNextWpt()) {
            resetWaypoint();
            m_nextWpt = info.getNextWpt();
            m_distToNextWaypoint = info.getDistanceMetersToNextWpt();
            m_loadTurnInstruction = true;
        } else {
            if (m_nextWpt == null)  {
                return;
            }
            //same waypoint different distance
            m_distToNextWaypoint = info.getDistanceMetersToNextWpt();
        }
        
        // load new turn instruction in advance
        if (m_loadTurnInstruction) {
            //cancel current prepared before loading a new one
            cancelCurrenInstruction();
            m_currentTurnDistance = loadTurnInstructionSoundBatch(m_distToNextWaypoint, m_nextWpt);
            m_loadTurnInstruction = false;
        }
        
        if (m_currentTurnDistance != NO_DISTANCE) {
            float speed = info.getSpeed();
            // check if trigger distance has been reached and if we should
            // play the turn instruction
            // calculate again the triggerDistance as the speed could have changed
            int triggerDistance = calculateTriggerDistance(
                        m_currentTurnDistance, m_nextWpt.getTurn().isExitRamp(),
                        m_currentTurnResponse.getTimeToStartAhead(), speed, info.getSpeedLimitKmh());
            if (hasReachedTriggerDistance(triggerDistance)) {
                if (shouldPlayTurnInstruction(triggerDistance)) {
                    m_currentTurnInstruction.start();
                } else {
                    // deallocate sound batch since it will not be played
                    cancelCurrenInstruction();
                }
                // Load next turn instruction 
                m_loadTurnInstruction = true;
            }
        } //else we don't try to load for this turn anymore 
    }


    /**
     * Get the sound batch for next turn instruction and return the distance for
     * next instruction.
     *    
     * @param distanceToWaypoint distance to next waypoint 
     * @param wpt next waypoint
     * 
     * @return the distance that should be played or NO_DISTANCE
     */
    private int loadTurnInstructionSoundBatch(int distanceToWaypoint, Waypoint wpt) {
        // Distance to be used in primary instruction, e.g. 100 if instruction is
        // "In 100 meters, turn left"
        int primaryInstructionDistance = getNextTurnInstructionDistance(distanceToWaypoint);
        
        //check if there are distance to play
        if (primaryInstructionDistance == NO_DISTANCE) return NO_DISTANCE;
        
        Waypoint nextWpt = wpt.getNext();
        // Don't load a "normal" turn instruction for the last turn in the route.
        // "You are at destination!"
        // Instead we should play "You have reached your destination"
        if ((primaryInstructionDistance == 0) && nextWpt == null) {
            // don't trigger any more turn instructions in this route
            return NO_DISTANCE;
        } 
        // Use secondary turn instruction if distance to next next turn <
        // SECONDARY_MAXIM_DISTANCE (150m)
        if (nextWpt != null
                && nextWpt.getDistanceMetersFromPrev() < SECONDARY_MAXIM_DISTANCE) {
            // Distance to be used in secondary turn instruction, e.g. 100 if
            // instruction is:
            // "In 200 meters turn left, then in a 100 meters turn right"
            int secondaryInstructionDistance = getTurnInstructionDistance(nextWpt
                    .getDistanceMetersFromPrev());

            // load the sound batch
            m_currentTurnResponse = m_syntaxTree.getSyntaxResponse(
                    SyntaxTree.TYPE_TURN_INSTRUCTION, wpt,
                    primaryInstructionDistance, nextWpt,
                    secondaryInstructionDistance, 0);
        } else {
            // avoid secondary instruction
            m_currentTurnResponse = m_syntaxTree.getSyntaxResponse(
                    SyntaxTree.TYPE_TURN_INSTRUCTION, wpt,
                    primaryInstructionDistance, null, 0, 0);
        }
        // Test is we have actually something to play
        // SyntaxTree could have no sounds for this turn
        if (m_currentTurnResponse != null) {
            m_currentTurnInstruction = m_soundPool.loadSoundSequence(
                            m_currentTurnResponse.getSoundsInternalArray(),
                            m_currentTurnResponse.getDurationsInternalArray());
            return primaryInstructionDistance;
        } else {
            if (LOG.isInfo()) {
                LOG.info("NavigationSoundHandler.loadTurnInstructionSoundBatch()",
                         "No sounds in SynntaxTree for turn " + m_nextWpt);
            }
            return NO_DISTANCE;
        }   
        
    }
    
    /**
     * Updates and returns the trigger distance
     * 
     * @param primaryInstructionDist - Distance to be used in primary instruction
     * @param isExitRamp - State if is a exit ramp turn
     * @param timeToStartAhead - Time ahead to start playing the sound batch
     * @param aSpeed - Current speed in m/s;
     * 
     * @return the trigger distance
     */
    
    private static int calculateTriggerDistance(int primaryInstructionDist, boolean isExitRamp, 
            int timeToStartAhead, float speedMPS, int speedLimitKmh) {
        
        int triggerDistance = primaryInstructionDist;
        
        if(LOG.isDebug()) {
            LOG.debug("NavigationSoundHandler.calculateTriggerDistance()", "dist=" + 
                    primaryInstructionDist + " timeahead=" + 
                    timeToStartAhead + " speed=" + speedMPS);
        }
        
        //FIXME: hack to avoid starting the sound to early because of the incorrect
        //limit the speed using segment maximum allowed speed
        if (speedLimitKmh <= 0) {
            speedLimitKmh = 130;
        };
        
        //check if speed is over the segment limit plus 20kmh
        //from test the speed when is wrong is actually over 100mps 
        if (speedMPS > ((speedLimitKmh + 20)/3.6)) {
            if(LOG.isWarn()) {
                LOG.warn("NavigationSoundHandler.calculateTriggerDistance()", 
                        "limit speed " + speedMPS + "mps to " + speedLimitKmh/5); 
            }            
            //reduce even more the speed as the driver will probably reduce 
            //the speed close to the turn where it's matters 
            speedMPS = ((float)speedLimitKmh)/5;

        }

        // Make the "Turn here" sound play 10 meters earlier to compensate
        // for the sound coming to late
        if (primaryInstructionDist == 0) {
            triggerDistance += 10;
        }
        
        if (isExitRamp) {
            // we are presumably on a freeway
            // add a hundred meters
            triggerDistance += 100; 
        }
        
        // Add 1 second worth of distance to cope with positioning delay
        triggerDistance += 1*speedMPS;//was (int) (10 + 1 * speedMPS);
       
         
        //finally add the extra time from soundbatch
        triggerDistance += (timeToStartAhead * speedMPS/1000);
        
        if(LOG.isDebug()) {
            LOG.debug("NavigationSoundHandler.calculateTriggerDistance()",  "triggerDistance=" + triggerDistance);
        }
        
        return triggerDistance;
    }
    
    /**
     * Check if a turn instruction should be played based
     * on much the trigger distance is passed. If we are too far
     * passed the trigger distance we want to skip the turn instruction
     * and play the next instead.
     * 
     * @return true if the turn instruction should be played,
     * false otherwise
     */
    private boolean shouldPlayTurnInstruction(int triggerDistance) {
        return (triggerDistance - m_distToNextWaypoint <= MAX_DISTANCE_PASSED_TRIGGER_POINT);
    }
    
    /**
     * Check if the trigger distance for playing
     * the next turn instruction has been reached
     * 
     * @return true if trigger distance is reached,
     * false otherwise
     */
    
    private boolean hasReachedTriggerDistance(int triggerDistance) {
        if (triggerDistance == NO_DISTANCE || m_distToNextWaypoint == NO_DISTANCE) {
            return false;
        } else if (m_distToNextWaypoint <= triggerDistance) {
            return true;
        }
        return false;
    }
    
}
