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

/**
 * 
 * 
 * 
 * @since 1.15.1
 */
class Macro {
    
    private final Node[] iNodes;
    private final int iStartNode;
    private final String iMacroName;
    
    /**
     * Creates a macro intended for use in the SyntaxTree
     * 
     * @param aNodes The Nodes contained in the Macro
     * @param aStartNode The number of the starting node
     * @param aMacroName The name of the macro
     */
    Macro(Node[] aNodes, int aStartNode, String aMacroName) {
        iNodes = aNodes;
        iStartNode = aStartNode;
        iMacroName = aMacroName;
    }
    
    /**
     * Returns the number of the starting node
     */
    int getStartNode() {
        return iStartNode;
    }
    
    /**
     * Returns a node in the Macro
     *  
     * @param aNodeID The ID of the Node
     * @return A Node
     */
    Node getNode(int aNodeID) {
        return iNodes[aNodeID];
    }
    
    /**
     * Returns the name of the Macro
     * This is only intended to be used while debugging
     */
    String getMacroName() {
        return iMacroName;
    }

}
