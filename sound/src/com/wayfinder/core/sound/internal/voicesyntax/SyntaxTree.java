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
package com.wayfinder.core.sound.internal.voicesyntax;

import java.util.Stack;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.route.Waypoint;
import com.wayfinder.core.shared.util.IntVector;

/**
 * This class holds the logic for assembling the correct sounds based
 * on RouteReplyItems and distances.
 *
 * Use SyntaxLoader to load the syntax from a .syn-file.
 * 
 * 
 * @since 1.15.1
 */
public class SyntaxTree {
    
    private static final Logger LOG =
        LogFactory.getLoggerForClass(SyntaxTree.class);
    
    public static final int TYPE_NO_SOUND = -1;
    public static final int TYPE_TURN_INSTRUCTION = 0;
    public static final int TYPE_OFF_TRACK = 1;
    public static final int TYPE_DESTINATION_REACHED = 2;
    public static final int TYPE_SPEED_CAMERA = 3;
    //public static final int TYPE_CROSSING = 4;//not in syntax 
    public static final int TYPE_GPS_NOTIFICATION = 5;
    public static final int TYPE_LENGTH = 6;
    
    
    //Check to see if we are driving on the right or left side
    private static final int DRIVE_LEFT_SIDE = 0;
    private static final int DRIVE_RIGHT_SIDE = 1;
    
    private final SoundClip[] iClipArray;
    private final Macro[] iMacroArray;
    
    //private int[] iClipDurationArray ;
    
    private final int[] startMacroIdArray; 

    /**
     * Standard constructor
     * 
     * @param aClipArray The array of sound filenames
     * @param aMacroArray The array of Macros
     */
    SyntaxTree(SoundClip[] aClipArray, Macro[] aMacroArray) {
        iClipArray = aClipArray; //new String[TYPE_LENGTH];
        iMacroArray = aMacroArray; //new Macro[TYPE_LENGTH];
        //iClipDurationArray = new int[aClipArray.length];//just in case
        
        //find the start ids;
        startMacroIdArray = new int[TYPE_LENGTH];
        for (int i= 0; i< aMacroArray.length;i++) {
            if (aMacroArray[i].getMacroName().equals("SoundListNormal")) {
                startMacroIdArray[TYPE_TURN_INSTRUCTION] = i;
            } else if (aMacroArray[i].getMacroName().equals("SoundListSpeedCam")) {
                startMacroIdArray[TYPE_SPEED_CAMERA] = i;
            } else if (aMacroArray[i].getMacroName().equals("SoundListOffTrack")) {
                startMacroIdArray[TYPE_OFF_TRACK] = i;
            } else if (aMacroArray[i].getMacroName().equals("SoundListAtDest")) {
                startMacroIdArray[TYPE_DESTINATION_REACHED] = i;
            } else if(aMacroArray[i].getMacroName().equals("SoundListGpsChange")) {
                startMacroIdArray[TYPE_GPS_NOTIFICATION] = i;
            }
        }
    }

//    public String[] getClipPathsInternalArray() {
//        return iClipArray;
//    }    
    
//    public void setClipDurationNoCopyArray(int[] durationArray) {
//        iClipDurationArray = durationArray;
//    }
    
    //private int iDistToNextTurn, iDistToNextNextTurn;
    //private int iCurrentDist;
    private int iGPSConnectionStatus;
    
    /**
     * can be  nextWaypoit or nextWaypoit.getNext(); 
     */
    private Waypoint currentWpt;
    
    private int iCurrentXing;

    private int currentDist;
    
    /**
     * Returns the names of the sounds that should be played next
     * 
     * @param macroType - The type off sound macro to play
     * @param nextWpt - The RouteReplyItem for the next turn
     * @param distToNextWpt - The distance to the next turn
     * @param nextNextWpt - The RouteReplyItem for the next next turn
     * @param distToNextNextWpt - The distance to the next next turn
     * @param GPSConnectionStatus - Current GPS connection status 
     * 
     * @return A SyntaxResponse containing the filenames of the sounds 
     * or null if there is nothing to play 
     */
    public SyntaxResponse getSyntaxResponse(int macroType,
                                Waypoint nextWpt, int distToNextWpt,
                                Waypoint nextNextWpt, int distToNextNextWpt,
                                int GPSConnectionStatus) {
        //#debug
        if(LOG.isDebug()) {
            LOG.debug("SyntaxTree.getSyntaxResponse()", "type=" + macroType);
        }
        
        if (macroType<0 || macroType >= TYPE_LENGTH) {
            throw new IllegalArgumentException("Invalid macroType use only TYPE constant values");
        }
        Macro currentMacro = iMacroArray[startMacroIdArray[macroType]];
        
        if (macroType == TYPE_TURN_INSTRUCTION) {
            this.currentWpt = nextWpt;
            this.currentDist = distToNextWpt;
        } else if (macroType == TYPE_GPS_NOTIFICATION) {
            iGPSConnectionStatus = GPSConnectionStatus;
        }
        
        int newNodeID = currentMacro.getStartNode();

        Node currentNode = null;
        
        boolean runComplete = false;
        
        IntVector soundClipIDs = new IntVector(10);
        
        Stack macroStack = new Stack();
        
        int timingMarkerIndex = 0;
        
        do {
            if (currentNode != null) {
                int nodeType = currentNode.getNodeType();
                
                switch (nodeType) {
                    case Node.TYPE_SOUND_CLIP:
                        int clipID = currentNode.getSoundClipID();
                        if(clipID == Node.SOUND_TIMING_MARKER) {
                            timingMarkerIndex = soundClipIDs.size();
                        } else {
                            soundClipIDs.add(clipID);
                        }
                       
                        newNodeID = currentNode.getNextOrTrueNodeID();
                        break;
                    case Node.TYPE_MACRO_CALL:
                        SyntaxState ss = new SyntaxState();
                        ss.iMacro = currentMacro;
                        ss.iNodeID = currentNode.getNextOrTrueNodeID();
                        macroStack.push(ss);
                        
                        currentMacro = iMacroArray[currentNode.getMacroID()];
                        newNodeID = currentMacro.getStartNode();
                        break;
                        
                    case Node.TYPE_BOOLEAN:
                        
                        if(evaluateBooleanCondition(currentNode)) {
                            newNodeID = currentNode.getNextOrTrueNodeID();
                            
                        } else {
                            newNodeID = currentNode.getBooleanFalseNodeID();
                        }
                        
                        break;
                        
                    case Node.TYPE_TRY_CATCH:
                        SyntaxState ssTC = new SyntaxState();
                        ssTC.iMacro     = currentMacro;
                        ssTC.iNodeID    = currentNode.getTryCatchFailNode();
                        ssTC.iSizeOfSounds = soundClipIDs.size();
                        ssTC.iTimingMarkerIndex = timingMarkerIndex;
                        
                        macroStack.push(ssTC);
                        newNodeID = currentNode.getNextOrTrueNodeID();
                        break;
                        
                    case Node.TYPE_SELECT_XING:
                        switch(currentNode.getCrossing()) {
                        case 1:
                            currentWpt     = nextWpt;
                            currentDist = distToNextWpt;
                            iCurrentXing    = 1;
                            break;
                            
                        case 2:
                            currentWpt     = nextNextWpt;
                            currentDist = distToNextNextWpt;
                            iCurrentXing    = 2;
                            break;
                            
                        default:
                            currentWpt = null;
                        }
                        if(currentWpt == null) {
                            newNodeID = Node.NEXT_ID_FAIL;
                        } else {
                            newNodeID = currentNode.getNextOrTrueNodeID();
                        }
                        break;
                        
                    default:
                        throw new IllegalStateException("Node type not recognized: " + nodeType);
                    }
                
                currentNode = null; //node handled
            
            }
            
            //Check to see what's next
            
            if(newNodeID >= 0) {
                //regular node
                currentNode = currentMacro.getNode(newNodeID);
            } else {
                switch(newNodeID) {
                case Node.NEXT_ID_FAIL:
                    //Oh no!
                    if(LOG.isDebug()) {
                        LOG.debug("SyntaxTree.getSyntaxResponse()", "fail");
                    }
                    
                    if(macroStack.isEmpty()) {
                        runComplete = true;
                        // we have failed out of the main macro
                        // erase all sounds
                        soundClipIDs.clear();
                        
                    } else {
                        SyntaxState ss = (SyntaxState)macroStack.pop();
                        currentMacro = ss.iMacro;
                        newNodeID = ss.iNodeID;
                        //erase all sounds added since last try/catch node
                        if (ss.iSizeOfSounds == 0 ) {
                            // if the latest try/catch node was at position 0, it means
                            // that we have a FAIL node in the main macro so we erase all sounds
                            soundClipIDs.clear();
                            timingMarkerIndex = 0; 
                            runComplete = true;
                        } else {
                            for(int i = soundClipIDs.size()-1; i >= ss.iSizeOfSounds; i--) {
                                soundClipIDs.removeLast();
                            }
                            //reverse the time 
                            timingMarkerIndex = ss.iTimingMarkerIndex;
                        }
                    }
                    break;
                case Node.NEXT_ID_RETURN:
                    //fall through
                case Node.NEXT_ID_NOTHING:
                    //fall through
                default:
                    //We are returning from a macro
                    if(macroStack.isEmpty()) {
                        runComplete = true;
                    } else {
                        SyntaxState ss = (SyntaxState)macroStack.pop();
                        currentMacro = ss.iMacro;
                        newNodeID = ss.iNodeID;
                    }
                    break;
                }
            }
        } while (!runComplete);
        
        
        //empty the variables
        currentWpt = null;
        currentDist = iCurrentXing = 0;

        //create the result
        int size = soundClipIDs.size();
        if (size == 0) {
            if(LOG.isDebug()) {
                LOG.debug("SyntaxTree.getSyntaxResponse()", "No sounds to play!");
            }
            return null;            
        }
        
        String[] soundFiles = new String[size];
        int[] soundDurations = new int[size];
        int timeToPlayAhead = 0;
        boolean timingMarkerAhead = (timingMarkerIndex>0);
        for (int i =0; i < size; i++) {
            int clipID = soundClipIDs.get(i);
            soundFiles[i] = iClipArray[clipID].getPath();
            soundDurations[i] = iClipArray[clipID].getDuration();
            if (timingMarkerAhead) {
                if (i == timingMarkerIndex) {
                    timingMarkerAhead = false; 
                } else {
                    timeToPlayAhead += soundDurations[i];
                }
            } 
        }
        
        SyntaxResponse rez = new SyntaxResponse(
                soundFiles, soundDurations, timeToPlayAhead);
        if(LOG.isDebug()) {
            LOG.debug("SyntaxTree.getSyntaxResponse()", rez.toString()); 
        }
             
        return rez;
    }
    
    /**
     * <pre>
     * Evaluates a boolean node condition.
     * This has three steps:
     * 
     * 1. What are we checking? This can be one of six things:
     *          a) DIST - The distance to the turn
     *          b) TURN - The type of the turn
     *          c) SIDE - If we are driving on the left or right side
     *          d) EXIT - What exit in the roundabout
     *          e) XING - What crossing we are looking at (e.g. is it the next 
     *                    or the next next crossing
     *          f) QGPS - GPS status
     *          g) ZERO - Basically a check to see if a value is Zero
     *          
     * 2. The limit, namely the value that we check against
     * 
     * 3. The relation between step one and two.
     * 
     * Example:
     *      A boolean node containing:
     *              boolean variable = Node.BOOL_VAR_DIST
     *              limit = 200
     *              boolean relation = Node.BOOL_REL_LESS_OR_EQUAL
     *              
     *      would check to see if the current distance <= 200          
     *          
     *  </pre>        
     */
    private boolean evaluateBooleanCondition(Node aNode) {
        //Select variable
        int variable;
        switch(aNode.getBooleanVariable()) {
            case Node.BOOL_VAR_DIST:
                variable = currentDist;
                break;
            
            case Node.BOOL_VAR_TURN:
                // Check the type of the next turn
                if (currentWpt == null) {
                    //TODO: check this if can happen  
                    //even if we're on the last road segment, just before reaching 
                    //the destination this should not be null but TURN_FINALLY 
                    throw new IllegalStateException("iCurrentRRI is null");
                } else {
                    variable = currentWpt.getTurn().getVoiceSyntaxTurnType(); 
                }
                if(LOG.isTrace()) {
                    LOG.trace("SyntaxTree.evaluateBooleanCondition()", "Turn: " + variable);
                }
                break;
                
            case Node.BOOL_VAR_SIDE:
                //Check to see if we are driving on the right or left side
                if(currentWpt.getDriveOnRightSideBefore()) {
                    //TODO old was iCurrentRRI.getDriveOnRightSide() 
                    //check if same behavior
                    variable = DRIVE_RIGHT_SIDE;
                } else {
                    variable = DRIVE_LEFT_SIDE;
                }
                break;
                
            case Node.BOOL_VAR_EXIT:
                variable = currentWpt.getExitCount();
                break;
                
            case Node.BOOL_VAR_XING:
                variable = iCurrentXing;
                break;
            case Node.BOOL_VAR_GPSQ:
                variable = iGPSConnectionStatus;
                break;
            case Node.BOOL_VAR_ZERO:
                //Fall through 
            default:
                variable = 0;
                break;    
        }
        
        //Do the actual match
        int limit = aNode.getBooleanLimit();
        if(LOG.isDebug()) {
            LOG.debug("SyntaxTree.evaluateBooleanCondition()","limit: " + limit 
                    + "; realation: " + aNode.getBooleanRelation());
        }

        switch (aNode.getBooleanRelation()) {
           case Node.BOOL_REL_EQUAL:
              return variable == limit;
           case Node.BOOL_REL_NOT_EQUAL:
              return variable != limit;
           case Node.BOOL_REL_LESS_THAN:
              return variable < limit;
           case Node.BOOL_REL_GREATER:
              return variable > limit;
           case Node.BOOL_REL_LESS_OR_EQUAL:
              return variable <= limit;
           case Node.BOOL_REL_GREATER_OR_EQUAL:
              return variable >= limit;
           default:
              return false;
        }
    }



}    
