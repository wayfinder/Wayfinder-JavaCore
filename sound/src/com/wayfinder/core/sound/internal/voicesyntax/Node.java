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

import java.io.IOException;

/**
 * This class represents a Node within a Macro.
 * The same class is used to represent all kinds of Nodes
 * 
 * 
 * @since 1.15.1
 */
class Node {
    
    /**
     * Signifies that the Node is a start node. There will not actually be
     * any nodes of this type
     */
    static final int TYPE_START_NODE    = 0x00;
    
    /**
     * Signifies that the Node contains a sound clip id
     */
    static final int TYPE_SOUND_CLIP    = 0x01;
    
    /**
     * Signifies that the Node contains a call to a new Macro 
     */
    static final int TYPE_MACRO_CALL    = 0x02;
    
    /**
     * Signifies that the Node contains a boolean evaluation
     */
    static final int TYPE_BOOLEAN       = 0x03;
    
    /**
     * Signifies that the Node is a try/catch node
     */
    static final int TYPE_TRY_CATCH     = 0x04;
    
    /**
     * Signifies that the Node contains instructions to change the actual
     * crossing
     */
    static final int TYPE_SELECT_XING   = 0x05;
    
    
    private final int iNodeType;
    
    /**
     * Signifies that there is no next ID for this node
     */
    static final int NEXT_ID_NOTHING    = -1;
    
    /**
     * Signifies that the tree should return to the previous macro
     */
    static final int NEXT_ID_RETURN     = -2;
    
    /**
     * Signifies that the tree should return to the previous try/catch node
     */
    static final int NEXT_ID_FAIL       = -3;
    
    private final int iNextOrTrueNodeID;
    
    Node(int aNodeType, int aNextNode) {
        iNodeType = aNodeType;
        iNextOrTrueNodeID = aNextNode;
    }
   
    /**
     * Returns the type of the node
     * @return One of the static TYPE constants
     */
    int getNodeType() {
        return iNodeType;
    }
    
    /**
     * Returns the next node. This also signifies the id of the true node
     * when in a node that has a false node id
     * @return The next/true node ID or one of the static NEXT constants
     */
    int getNextOrTrueNodeID() {
        return iNextOrTrueNodeID;
    }
    
    //-------------------------------------------------------------------------
    // Sound clip specific
    /**
     * Signifies that this is node should be interpreted as a timing marker
     */
    static final int SOUND_TIMING_MARKER = -3;
    
    private int iSoundClipID;
    
    /**
     * Sets the soundclip ID
     * @param aClipID The clip ID
     * @throws IOException If the node is not of type TYPE_SOUND_CLIP
     */
    void setSoundClipID(int aClipID) throws IOException {
        if(iNodeType != TYPE_SOUND_CLIP) {
            throw new IOException("Node: tried to set sound clip in node that is not correct type");
        }
        iSoundClipID = aClipID;
    }
    
    /**
     * Get the sound clip id
     * @return The sound clip ID
     * @throws IllegalStateException If the node is not of type TYPE_SOUND_CLIP
     */
    int getSoundClipID() throws IllegalStateException {
        if(iNodeType != TYPE_SOUND_CLIP) {
            throw new IllegalStateException("Node: tried to get sound clip from node that is not correct type");
        }
        return iSoundClipID;
    }
    
    //-------------------------------------------------------------------------
    // Macro call specific
    
    private int iMacroID;
    
    /**
     * Sets the macro ID
     * @param aMacroID The macro ID
     * @throws IOException If the node is not of type TYPE_MACRO_CALL
     */
    void setMacroID(int aMacroID) throws IOException {
        if(iNodeType != TYPE_MACRO_CALL) {
            throw new IOException("Node: tried to set macro id in node that is not correct type");
        }
        iMacroID = aMacroID;
    }
    
    /**
     * Get the macro id
     * @return The macro ID
     * @throws IllegalStateException If the node is not of type TYPE_MACRO_CALL
     */
    int getMacroID() throws IllegalStateException {
        if(iNodeType != TYPE_MACRO_CALL) {
            throw new IllegalStateException("Node: tried to get macro id from node that is not correct type");
        }
        return iMacroID;
    }
    
    //-------------------------------------------------------------------------
    // boolean specific
    
    /**
     * Signifies that the boolean variable is of type ZERO
     */
    static final int BOOL_VAR_ZERO = 0;
    
    /**
     * Signifies that the boolean variable is the distance
     */
    static final int BOOL_VAR_DIST = 1;
    
    /**
     * Signifies that the boolean variable is the next turn type
     */
    static final int BOOL_VAR_TURN = 2;
    
    /**
     * Signifies that the boolean variable is the next or nextnext turn
     */
    static final int BOOL_VAR_XING = 3;
    
    /**
     * Signifies that the boolean variable is the number of exits in a
     * roundabout
     */
    static final int BOOL_VAR_EXIT = 4;
    
    /**
     * Signifies that the boolean variable is what side you are driving on
     */
    static final int BOOL_VAR_SIDE = 5;
    
    /**
     * Sugnifies a boolean for detecting GPS Status condition
     * */
    static final int BOOL_VAR_GPSQ = 6;
    
    private int iVariable;
    
    /**
     * Sets the boolean variable
     * @param aVariable One of the static BOOL_VAR constants
     * @throws IOException If the node is not of type TYPE_BOOLEAN
     */
    void setBooleanVariable(int aVariable) throws IOException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IOException("Node: tried to set boolean variable in node that is not correct type");
        }
        iVariable = aVariable;
    }
    
    /**
     * Get the boolean variable
     * @return One of the static BOOL_VAR constants
     * @throws IllegalStateException If the node is not of type TYPE_BOOLEAN
     */
    int getBooleanVariable() throws IllegalStateException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IllegalStateException("Node: tried to get boolean variable from node that is not correct type");
        }
        return iVariable;
    }

    /**
     * Signifies that the boolean variable should be checked if it's equal to
     * the limit
     */
    static final int BOOL_REL_EQUAL             = 0;
    
    /**
     * Signifies that the boolean variable should be checked if it's not equal
     * to the limit
     */
    static final int BOOL_REL_NOT_EQUAL         = 1;
    
    /**
     * Signifies that the boolean variable should be checked if it's less than
     * the limit
     */
    static final int BOOL_REL_LESS_THAN         = 2;
    
    /**
     * Signifies that the boolean variable should be checked if it's greater
     * than the limit
     */
    static final int BOOL_REL_GREATER           = 3;
    
    /**
     * Signifies that the boolean variable should be checked if it's less than
     * or equal to the limit
     */
    static final int BOOL_REL_LESS_OR_EQUAL     = 4;
    
    /**
     * Signifies that the boolean variable should be checked if it's greater
     * than or equal to the limit
     */
    static final int BOOL_REL_GREATER_OR_EQUAL  = 5;
    
    private int iRelation;
    
    /**
     * Sets the boolean relation
     * @param aVariable One of the static BOOL_REL constants
     * @throws IOException If the node is not of type TYPE_BOOLEAN
     */
    void setBooleanRelation(int aRelation) throws IOException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IOException("Node: tried to set boolean relation in node that is not correct type");
        }
        iRelation = aRelation;
    }
    
    /**
     * Get the boolean relation
     * @return One of the static BOOL_REL constants
     * @throws IllegalStateException If the node is not of type TYPE_BOOLEAN
     */
    int getBooleanRelation() throws IllegalStateException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IllegalStateException("Node: tried to get boolean relation from node that is not correct type");
        }
        return iRelation;
    }

    private int iLimit;
    
    
    /**
     * Sets the boolean limit
     * @param aLimit The limit value
     * @throws IOException If the node is not of type TYPE_BOOLEAN
     */
    void setBooleanLimit(int aLimit) throws IOException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IOException("Node: tried to set boolean limit in node that is not correct type");
        }
        iLimit = aLimit;
    }
    
    /**
     * Get the boolean limit
     * @return The limit value
     * @throws IllegalStateException If the node is not of type TYPE_BOOLEAN
     */
    int getBooleanLimit() throws IllegalStateException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IllegalStateException("Node: tried to get boolean limit from node that is not correct type");
        }
        return iLimit;
    }
    
    private int iFalseNodeID;
    
    
    /**
     * Sets the false node ID
     * @param aNodeID The node id
     * @throws IOException If the node is not of type TYPE_BOOLEAN
     */
    void setBooleanFalseNodeID(int aNodeID) throws IOException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IOException("Node: tried to set boolean false id in node that is not correct type");
        }
        iFalseNodeID = aNodeID;
    }
    
    /**
     * Get the false node ID
     * @return The false node ID
     * @throws IllegalStateException If the node is not of type TYPE_BOOLEAN
     */
    int getBooleanFalseNodeID() throws IllegalStateException {
        if(iNodeType != TYPE_BOOLEAN) {
            throw new IllegalStateException("Node: tried to get boolean false id from node that is not correct type");
        }
        return iFalseNodeID;
    }
    
    //-------------------------------------------------------------------------
    // try catch specific
    
    private int iTryFailNodeID;
    
    /**
     * Sets the try/catch node ID
     * @param aNodeID A try/catch node ID
     * @throws IOException If the node is not of type TYPE_TRY_CATCH
     */
    void setTryCatchFailNode(int aNodeID) throws IOException {
        if(iNodeType != TYPE_TRY_CATCH) {
            throw new IOException("Node: tried to set try/catch fail id in node that is not correct type");
        }
        iTryFailNodeID = aNodeID;
    }
    
    /**
     * Get the fail node ID
     * @return The fail node ID
     * @throws IllegalStateException If the node is not of type TYPE_TRY_CATCH
     */
    int getTryCatchFailNode() throws IllegalStateException {
        if(iNodeType != TYPE_TRY_CATCH) {
            throw new IllegalStateException("Node: tried to get try/catch fail id from node that is not correct type");
        }
        return iTryFailNodeID;
    }
    
    //-------------------------------------------------------------------------
    // crossing specific
    
    private int iCrossing;
    
    /**
     * Sets the crossing (XING) number
     * @param aNodeID The crossing (XING) number
     * @throws IOException If the node is not of type TYPE_SELECT_XING
     */
    void setCrossing(int aCrossing) throws IOException {
        if(iNodeType != TYPE_SELECT_XING) {
            throw new IOException("Node: tried to set crossing in node that is not correct type");
        }
        iCrossing = aCrossing;
    }
    
    /**
     * Get the crossing (XING) number
     * @return The crossing (XING) number
     * @throws IllegalStateException If the node is not of type TYPE_SELECT_XING
     */
    int getCrossing() throws IllegalStateException {
        if(iNodeType != TYPE_SELECT_XING) {
            throw new IllegalStateException("Node: tried to get crossing from node that is not correct type");
        }
        return iCrossing;
    }
}
