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
import java.io.InputStream;
import java.util.Vector;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 * This class loads the PeterSv Syntax from the .syn file and reads out all the
 * Macros and nodes and constructs a SyntaxTree. 
 * 
 * 
 * @since 1.15.1
 */
public class SyntaxLoader {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(SyntaxLoader.class);
    
    public static final String SYNTAX_EXTENSION = ".syn";
    /**
     * Opens the file with the syntax, constructs a SyntaxLoader to
     * load the syntax from the stream. Then closes the stream and
     * returns the SyntaxTree.
     * <br/>
     * The file is loaded from application package using this file path:
     * <br/> 
     * <code> dirPath + language.getWFCode() + SYNTAX_EXTENSION 
     * //e.g. '/sounds/en.syn' 
     * </code>
     * @param dirPath path where both syn and sound files are located must end 
     * with directory separator '/'
     * @throws IOException if one is thrown while reading from the
     * stream or if the file don't respect the format. 
     * An attempt to close the stream is made.
     */
    public static SyntaxTree loadSyntax(
            PersistenceLayer persistenceLayer, String dirPath, String soundFileExtension,
            String syntaxFileName)
        throws IOException {

        InputStream is = null;
        try {
            is = persistenceLayer.getResourceAsStream(dirPath + syntaxFileName 
                                       + SYNTAX_EXTENSION);
            SyntaxLoader sl = new SyntaxLoader(is,dirPath,soundFileExtension);
            return sl.createSyntaxTree();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    if(LOG.isError()) {
                        LOG.error("SyntaxLoader.loadSyntax()", e);
                    }
                }
                is = null;
            }
        }
    }
    
    //location /languageCode.syn
    /**
     * Constructs a SyntaxLoader reading from aIS.
     */
    private SyntaxLoader(InputStream stream, String dirPath, String soundFileExtension) {
        super();
        iStream = stream;
        this.dirPath = dirPath;
        this.soundFileExtension = soundFileExtension;
    }

    private InputStream iStream;

    private final String dirPath;
    
    private final String soundFileExtension;
    /**
     * Creates and returns a SyntaxTree
     * 
     * @return a SyntaxTree
     * @throws IOException If the syntax file could not be loaded
     */
    public SyntaxTree createSyntaxTree() throws IOException {
        if(LOG.isInfo()) {
            LOG.info("SyntaxLoader.createSyntaxTree()", "start");
        }
        
        checkHeader();
        
        //Options, ignored for now
        while (readInt8() != 0) {}
        
        
        //Read the sound clips
        //This will read out the clip names, but ignore them since all
        //references will be made to the place in the array of filenames
        SoundClip[] soundClips = readSoundClips();
        
        //distance table not currently present in the syntax file
        
        // Read the macros
        int numMacros = readInt16();
        Macro[] macroArray = new Macro[numMacros];
        for (int i = 0 ; i < numMacros; ++i) {

            String macroName = readString();
            
            if(LOG.isTrace()) {
                LOG.trace("SyntaxLoader.createSyntaxTree()", "Reading macro: " + macroName);
            }

            Vector nodeVector = new Vector();
            
            /*
             * Read the nodes in the macro
             */
            int nodeType;
            int startNode = 0;
            iCurrentNodeStackSize = 0;
            boolean startNodeRead = false;
            do {
                Node node = null;
                nodeType = readInt8();
                switch (nodeType) {
                case Node.TYPE_START_NODE: //0
                    // Start node - last in a macro
                    if(startNodeRead) {
                        throw new IOException("Start node read twice in macro: " + macroName);
                    }
                startNodeRead = true;
                startNode = readStartNode();
                if(LOG.isTrace()) {
                    LOG.trace("SyntaxLoader.createSyntaxTree()", "Startnode: " + startNode);
                }
                break;
                
                case Node.TYPE_SOUND_CLIP:  //1
                    node = readSoundClipNode(soundClips);
                break;
                
                case Node.TYPE_MACRO_CALL:  //2
                    node = readMacroCallNode();
                break;
                
                case Node.TYPE_BOOLEAN:     //3
                    node = readBooleanNode();
                break;
                
                case Node.TYPE_TRY_CATCH:   //4
                    node = readTryCatchNode();
                break;
                
                case Node.TYPE_SELECT_XING: //5   
                    node = readSelectXingNode();
                break;
                
                default:
                    throw new IOException("Node with undefined type: " 
                            + nodeType + " found!");
                
                }
                
                if(nodeType != Node.TYPE_START_NODE) {
                    nodeVector.addElement(node);
                    iCurrentNodeStackSize = nodeVector.size();
                }
                
            } while (nodeType != Node.TYPE_START_NODE);
            
            if(LOG.isDebug()) {
                LOG.debug("SyntaxLoader.createSyntaxTree()","Read macro: " + macroName +
                    " with " + iCurrentNodeStackSize + " nodes " +
                    " with startnode: " + startNode);
            }

            //create the macro
            Node[] nodeArray = new Node[ iCurrentNodeStackSize ];
            nodeVector.copyInto(nodeArray);
            nodeVector = null;
            macroArray[i] = new Macro(nodeArray, startNode, macroName);
            
        }
        
        /*
         * Finally, create the SyntaxTree and set the variables
         */
        SyntaxTree daTree = new SyntaxTree(soundClips, macroArray);
        if(LOG.isDebug()) {
            LOG.debug("SyntaxLoader.createSyntaxTree()", "Loading successful");
        }
        
        return daTree;
    } // createSyntaxTree
    
    
    private static final String SYNTAX_HEADER = "WF_AUDIO_SYNTAX: 1\n";
    private static final int HEADER_LENGTH = SYNTAX_HEADER.length();
    
    /**
     * Checks the header of the syntax file
     * @throws IOException If the header is incorrect
     */
    private void checkHeader() throws IOException {
        byte[] buf = new byte[HEADER_LENGTH];
        readExact(buf,0,HEADER_LENGTH);
        String header = new String(buf);
        if(!header.equals(SYNTAX_HEADER)) {
            throw new IOException("SyntaxLoader: Header does not match!");
        }
        if(LOG.isDebug()) {
            LOG.debug("SyntaxLoader.checkHeader()", "Header OK!");
        }
        
    }
    
    private void readExact(byte[] buf,int start,int length) throws IOException {
        while (length > 0) {
            int now = iStream.read(buf,start,length);
            if (now <= 0) throw new IOException("End not expected");
            start += now;
            length -= now;
        }
    }
    
    private void skipExact(int length) throws IOException {
        while (length > 0) {
            long now = iStream.skip(length);
            if (now <= 0) throw new IOException("End not expected");
            length -= now;
        }
    }    
    /**
     * Reads out the names of the sound files
     * @return The names as a String array
     * @throws IOException If the names could not be read
     */
    private SoundClip[] readSoundClips() throws IOException {
        
        int numClips = readInt16();
        SoundClip[] clipVector = new SoundClip[numClips];
        
        for (int i = 0 ; i <numClips ; ++i) {
            skipString();
            String fileName = readString();
            int duration = readInt16();
            if(LOG.isDebug()) {
                LOG.debug("SyntaxLoader.readSoundClips()", "Read filename: " + fileName + " duration");
            }
            //add the full path to instead of just the name
            String path = dirPath + fileName + soundFileExtension;
            //read the duration
            
            clipVector[i]= new SoundClip(path, duration);
        }
        
        return clipVector;
    }

    
    //-------------------------------------------------------------------------
    // Reading of nodetypes
    
    
    /**
     * Reads the start node, also known as "SoundListNormal"
     * This should only be called once while parsing the file, if it is read
     * more than once, then the file is corrupt
     * 
     * @return The id of the start node
     * @param aStream The inputstream from the file
     * @throws IOException If stream is corrupted
     */
    private int readStartNode() throws IOException {
        int startnode = readInt16();
        checkValidNodeID(startnode);
        return startnode;
    }

    /**
     * Reads a Sound Clip node from the stream
     * 
     * @param aSoundClipArray The array of sound clips
     * @return A Node with the type Node.TYPE_SOUND_CLIP
     * @throws IOException If the node could not be read
     */
    private Node readSoundClipNode(SoundClip[] aSoundClipArray) throws IOException {
        int nextID = readInt16();
        int clipID = readInt16();
        
        checkValidNodeID(nextID);
        
        if(clipID != Node.SOUND_TIMING_MARKER) {
            if ( (clipID < 0) || (clipID >= aSoundClipArray.length) ) { 
                throw new IOException("SyntaxLoader: readSoundClipNode(), " +
                                      "clip id out of range: " + clipID);
            }
        }
        if(LOG.isDebug()) {
            LOG.debug("SyntaxLoader.readSoundClipNode()", "id: " + clipID + " with next id: "+ nextID);
        }

        Node n = new Node(Node.TYPE_SOUND_CLIP, nextID);
        n.setSoundClipID(clipID);
        return n;
    }
    
    /**
     * Reads a Macro Call Node from the stream
     * 
     * @return A Node with the type Node.TYPE_MACRO_CALL
     * @throws IOException If the node could not be read
     */
    private Node readMacroCallNode() throws IOException {
        int nextID = readInt16();
        int macroID = readInt16();
        
        checkValidNodeID(nextID);
        
        if(LOG.isTrace()) {
            LOG.trace("SyntaxLoader.readMacroCallNode()", "id: " + macroID + 
                    " with next id: " + nextID );
        }
        
        Node n = new Node(Node.TYPE_MACRO_CALL, nextID);
        n.setMacroID(macroID);
        return n; 
    }
    
    /**
     * Reads a Boolean Node from the stream
     * 
     * @return A Node with the type Node.TYPE_BOOLEAN
     * @throws IOException If the node could not be read
     */
    private Node readBooleanNode() throws IOException {
        int variable = readInt8();
        int relation = readInt8();
        int limit  = readInt16();
        
        int trueID = readInt16();
        int falseID = readInt16();
        
        checkValidNodeID(falseID);
        checkValidNodeID(trueID);
        
        if(LOG.isTrace()) {
            LOG.trace("SyntaxLoader.readBooleanNode()",
                    "variable: " + variable + 
                    " relation: " + relation + 
                    " limit: " + limit +
                    " trueID: " + trueID + 
                    " falseID " + falseID);
        }
        
        Node n = new Node(Node.TYPE_BOOLEAN, trueID);
        n.setBooleanVariable(variable);
        n.setBooleanRelation(relation);
        n.setBooleanLimit(limit);
        n.setBooleanFalseNodeID(falseID);
        return n;
    }
    
    /**
     * Reads a Try/Catch Node from the stream
     * 
     * @return A Node with the type Node.TYPE_TRY_CATCH
     * @throws IOException If the node could not be read
     */
    private Node readTryCatchNode() throws IOException {
        int trueID = readInt16();
        int falseID = readInt16();
        
        checkValidNodeID(falseID);
        checkValidNodeID(trueID);
        
        if(LOG.isTrace()) {
            LOG.trace("SyntaxLoader.readTryCatchNode()",
                    "trueID: " + trueID + 
                    " falseID: " + falseID);
        }
        
        Node n = new Node(Node.TYPE_TRY_CATCH, trueID);
        n.setTryCatchFailNode(falseID);
        return n;
    }
    
    /**
     * Reads a XING Node from the stream
     * 
     * @return A Node with the type Node.TYPE_SELECT_XING
     * @throws IOException If the node could not be read
     */
    private Node readSelectXingNode() throws IOException {
        int nextID = readInt16();
        int crossing = readInt8();
        checkValidNodeID(nextID);
        
        if(LOG.isTrace()) {
            LOG.trace("SyntaxLoader.readSelectXingNode()",
                    "nextID: " + nextID + 
                    " crossing: " + crossing );
        }
        
        Node n = new Node(Node.TYPE_SELECT_XING, nextID);
        n.setCrossing(crossing);
        return n;
    }
    
    
    //-------------------------------------------------------------------------
    // Utility methods
    
    /**
     * Reads one byte from the stream and makes a int8 out of it
     * Needed since the voice syntax is encoded in little endian while
     * the Java system uses big endian
     * FIXME: This could probably be a byte instead
     */
    private int readInt8() throws IOException {
        int low = iStream.read();
        if (low > 127) {
            low = 0xffffff00 | low;
        }
        return low;
    }
    
    /**
     * Reads two bytes from the stream and makes a int16 out of them
     * Needed since the voice syntax is encoded in little endian while
     * the Java system uses big endian
     * FIXME: This could probably be a short instead
     */
    private int readInt16() throws IOException {
        int low = iStream.read();
        int high = iStream.read();
        int val = (high<<8) + low;
        if (val > 32767) {
            val = 0xffff0000 | val;
        }
        return val;
    }
    
    /**
     * Reads a string from the stream
     * Needed since the voice syntax is encoded in little endian while
     * the Java system uses big endian
     */
    private String readString() throws IOException {
        int length = readInt8();
        byte buf[] = new byte[length];
        readExact(buf,0,length);
        return new String(buf);
    }
    
    /**
     * Skips the next String in the stream
     * Needed since the voice syntax is encoded in little endian while
     * the Java system uses big endian
     */
    private void skipString() throws IOException {
        int length = readInt8();
        this.skipExact(length);
    }
    
    private int iCurrentNodeStackSize;

    /**
     * Checks to see if the node id is valid
     * @param aNodeID The node id
     * @throws IOException If the node ID is invalid
     */
    private void checkValidNodeID(int aNodeID) throws IOException {
        if ( (aNodeID < -3) || (aNodeID >= iCurrentNodeStackSize)) {
            throw new IOException("Invalid node id found: " + aNodeID);
        }
    }

}
