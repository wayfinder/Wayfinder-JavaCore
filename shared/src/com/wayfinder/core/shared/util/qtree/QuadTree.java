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
/*
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.shared.util.qtree;

import java.util.Vector;

import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * Implementation of a quad tree data structure. 
 * 
 * 
 *
 */
public class QuadTree {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(QuadTree.class);
    
    
    /**
     * Minimum size allowed for covered area if the tree cover less will not
     * be divided more 
     * This is in order to no create too many division 
     * Currently fixed to 1000 which in MC2 represent ~10m
     */
    public final static int MIN_RADIUS = 1000;//in MC2 this is ~10m
    
    private QuadTree []iQTQuater;
    private BoundingBox iQT_bb;
    
    /* The maximal number of items in a quadrant*/
    private int iMaxItems;
    
    /* Reference to the node. */
    private QuadTreeNode iNode;
    
    /* The name of the quadrant */ 
    private String iName;
    
    /**
     * Create a quad tree with the bounding box, name and max number of items 
     * for a node, specified by the parameters. 
     * 
     * @param aMinLat
     * @param aMinLon
     * @param aMaxLat
     * @param aMaxLon
     * @param aMaxItems
     * @param aName
     */
    public QuadTree(int aMinLat, int aMinLon, int aMaxLat, int aMaxLon, int aMaxItems, String aName) {
        iQTQuater = new QuadTree[4];
        iQTQuater[0] = null;
        iQTQuater[1] = null;
        iQTQuater[2] = null;
        iQTQuater[3] = null;
        
        iQT_bb = new BoundingBox(aMaxLat, aMinLat, aMaxLon, aMinLon);
        iMaxItems = aMaxItems;
        iName = aName;
    }
    
    /**
     * Removes all of the elements from this list.
     */
    public synchronized void clear() {
        iQTQuater = null;
        iQTQuater = new QuadTree[4];
        iQTQuater[0] = null;
        iQTQuater[1] = null;
        iQTQuater[2] = null;
        iQTQuater[3] = null;
        iNode = null;
    }
    
    /**
     * Return the name of the quadrant.
     * 
     * @return the name of the quadrant.
     */
    public synchronized String getName() {
        return iName;
    }
    
    /**
     * Return the bounding box for the quadrant. 
     * 
     * @return the bounding box for the quadrant. 
     */
    BoundingBox getBoundingBox() {
        return iQT_bb;
    }
    
    /**
     * Add a node to the QuadTree. 
     * 
     * @param aNode the node to be added. 
     */
    public synchronized boolean addNode(QuadTreeNode aNode) {

        if(aNode.getMinLat() == iQT_bb.getSouthLatitude() && 
           aNode.getMaxLat() == iQT_bb.getNorthLatitude() &&
           aNode.getMinLon() == iQT_bb.getWestLongitude() &&
           aNode.getMaxLon() == iQT_bb.getEastLongitude()) {

            iNode = aNode;

        } else {
            
            int lat = (aNode.getMaxLat()/2+aNode.getMinLat()/2);         
            int lon = (aNode.getMaxLon()/2+aNode.getMinLon()/2);
            
            int quarterNbr = 0;
            if(lon > iQT_bb.getCenterLon())
                quarterNbr |= 1;
            if(lat < iQT_bb.getCenterLat())
                quarterNbr |= 2;

            if(iQTQuater[quarterNbr] == null) {
                createQTQuater(quarterNbr);
            }

            iQTQuater[quarterNbr].addNode(aNode);
        }
        return true;
    }
    
    /**
     * Add a entry to the QuadTree. 
     * 
     * @param aEntry the entry to be added into a node. 
     * @return true if the node has been split into sub-nodes, 
     *         false if the entry has been added into a existing node. 
     */
    public synchronized boolean addEntry(QuadTreeEntry aEntry, QTFileInterface fileLoader) {
        /*
        +--------+--------+
        | Q0     | Q1     | Quarter number in binary form: 00ab
        | 0000   | 0001   | a=1 for bottom quarters and a=0 for upper ones
        |        |        | b=1 for right quarters and a=0 for left ones
        +--------+--------+
        | Q2     | Q3     |
        | 0010   | 0011   |
        |        |        |
        +--------+--------+
        */
        int quarterNbr = 0;        
        if(aEntry.getLongitude() > iQT_bb.getCenterLon())
            quarterNbr |= 1;
        if(aEntry.getLatitude() < iQT_bb.getCenterLat())
            quarterNbr |= 2;
        
        /* Create new child node if necessary */
        if(iQTQuater[quarterNbr] == null) {
            createQTQuater(quarterNbr);                    
        }
        
        return iQTQuater[quarterNbr].internalAddEntry(aEntry, fileLoader);        
    }
    
    private void createQTQuater(int quarterNbr) {
        switch(quarterNbr) {
            case 0: 
                iQTQuater[0] = new QuadTree(iQT_bb.getCenterLat(),
                                            iQT_bb.getWestLongitude(),
                                            iQT_bb.getNorthLatitude(),
                                            iQT_bb.getCenterLon(),
                                            iMaxItems,
                                            iName+"0");
                break;
                
            case 1: 
                iQTQuater[1] = new QuadTree(iQT_bb.getCenterLat(),
                                            iQT_bb.getCenterLon(),
                                            iQT_bb.getNorthLatitude(),
                                            iQT_bb.getEastLongitude(),
                                            iMaxItems,
                                            iName+"1");
                break;
                
            case 2:
                iQTQuater[2] = new QuadTree(iQT_bb.getSouthLatitude(),
                                            iQT_bb.getWestLongitude(),
                                            iQT_bb.getCenterLat(),
                                            iQT_bb.getCenterLon(),
                                            iMaxItems,
                                            iName+"2");
                break;
                
            case 3: 
                iQTQuater[3] = new QuadTree(iQT_bb.getSouthLatitude(),
                                            iQT_bb.getCenterLon(),
                                            iQT_bb.getCenterLat(),
                                            iQT_bb.getEastLongitude(),
                                            iMaxItems,
                                            iName+"3");
                break;            
        }    
    }
    
    /*
     * Return true if the node has one or more children. 
     */
    private boolean hasChildren() {
        return (iQTQuater[0] != null || iQTQuater[1] != null || 
                iQTQuater[2] != null || iQTQuater[3] != null);
    }
    
    private boolean iHasBeenSplit = false;
    
    /*
     * Internal method for adding a entry to the current node or
     * recursively subdividing the items into one of the sub nodes.   
     * 
     * @param aEntry the entry to be added into a node
     * @return true if the node has been split into sub-nodes, 
     *         false if the entry has been added into a existing node.  
     * 
     */
    private boolean internalAddEntry(QuadTreeEntry aEntry, QTFileInterface fileLoader) {
        
        if(hasChildren()) {
            iHasBeenSplit = addEntry(aEntry, fileLoader);
        } else {        
            if(iNode == null) {
                iHasBeenSplit = true;
                iNode = new QuadTreeNode(iName,
                                         iQT_bb.getSouthLatitude(), 
                                         iQT_bb.getWestLongitude(),
                                         iQT_bb.getNorthLatitude(),
                                         iQT_bb.getEastLongitude());
            } else {
                if(iNode.shouldDataBeLoaded() && fileLoader != null) {
                    if(LOG.isInfo()) {
                        LOG.info("QuadTree.internalAddEntry()", "Load node from file, name: "+iNode.getName());
                    }
                    
                    // Load the node from file and add QuadTreeEntrys into the node.  
                    fileLoader.readNodeFromFile(iNode);
                }
            }
            
            /* Add the enty to the nodes list*/            
            iNode.addEntry(aEntry);
            
            /* If we have exceed the maximal number of item in the node we split 
             * up them into 4 sub-nodes. */
            if(iNode.getSize() >= iMaxItems && iNode.getRadius() > MIN_RADIUS ) {
                
                iHasBeenSplit = true;
                QuadTreeEntry qte = iNode.getAllEntrys();
                
                if(LOG.isTrace()) {
                    LOG.trace("QuadTree.internalAddEntry()", 
                            " iNbrOfEntrys= "+iNode.getSize()+
                            " iMaxItems= "+iMaxItems+
                            " iCacheRegion.size= "+iNode.getSize()+
                            " name= "+iName);
                }
                
                /* Go throw all the entrys in the node and move it down
                 * one level in the tree. */
                do {
                    QuadTreeEntry tmp = qte;
                    qte = qte.getNext();
                    tmp.setNext(null);
                    addEntry(tmp, fileLoader);                                
                } while (qte != null);
                  
                /* Clear and delete the node, the entrys has been split up in 
                 * new leaves one level down in the tree. */
                if(fileLoader != null)
                    fileLoader.deleteFile(iNode.getName());
                iNode.clear();
                iNode = null;
                
                if(fileLoader != null) {
                    /* Write all nodes to disc when we have split up a node into 4 sub nodes.*/ 
                    for(int i=0; i < 4; i++) {
                        if(iQTQuater[i] != null && iQTQuater[i].iNode != null) {
                            if(iQTQuater[i].iNode.getAllEntrys() != null) {
                                fileLoader.writeNodeToFile(iQTQuater[i].iNode);
                                iQTQuater[i].iNode.clear();
                            } 
                        } 
                    }
                }
            }
        }
        return iHasBeenSplit;
    }
    
    /**
     * 
     * Return the node for the lat/lon index specified by the parameter. 
     * 
     * @param aLat in MC2
     * @param aLon in MC2
     * 
     * @return the node that holds the entrys with aLat/aLon
     */
    public synchronized QuadTreeNode getNode(int aLat, int aLon) {
        
        if(iNode != null) {            
            return iNode;            
        } else {
            
            int quarterNbr = 0; 
            if(aLon > iQT_bb.getCenterLon())
                quarterNbr |= 1;
            if(aLat < iQT_bb.getCenterLat())
                quarterNbr |= 2;
            
            if(iQTQuater[quarterNbr] != null) {
                return iQTQuater[quarterNbr].getNode(aLat, aLon);
            }            
        }        
        return null;
    }
    
    /**
     * Add the list of item in a node to the vector specified by the parameter. 
     * 
     * Each entry in the vector will contain a vector with the added entrys for 
     * one node.  
     * 
     * @param v
     */
    public synchronized void getAllNodes(Vector v) {
        
        iHasBeenSplit = false;
        
        if(iNode != null) {
            v.addElement(iNode);
        } else {
            for(int i=0; i<4; i++) {
                if(iQTQuater[i] != null) {
                    iQTQuater[i].getAllNodes(v);
                }
            }
        }
    }
    
    /**
     * Add nodes that contains loaded data into the vector specified by the 
     * parameter. 
     * 
     * @param v the vector where the data will be loaded into. 
     */
    public synchronized void getAllNodesThatContainsLoadedEntrys(Vector v) { 
        iHasBeenSplit = false;
        
        if(iNode != null) {
            if(iNode.getAllEntrys() != null) {                
                v.addElement(iNode);
            }
        } else {
            for(int i=0; i<4; i++) {
                if(iQTQuater[i] != null) {
                    iQTQuater[i].getAllNodesThatContainsLoadedEntrys(v);
                }
            }
        }
    }
    
    /**
     * 
     * Return the entry specified by the parameter if it exist, null if not. 
     * 
     * @param aLat
     * @param aLon
     */
    public synchronized QuadTreeEntry getEntry(int aLat, int aLon, String aID, QTFileInterface fileLoader) {
        
        if(iNode != null) {            
            if(iNode.getAllEntrys() == null) {
                long time = System.currentTimeMillis();
                if(fileLoader == null) {
                    if(LOG.isError()) {
                        LOG.error("QuadTree.getEntry()", "QTFileLoader == NULL!");
                    }
                    return null;
                }
                
                // Load the node from file and add the QuadTreeEntrys into the node. 
                fileLoader.readNodeFromFile(iNode); 
                
                if(LOG.isTrace()) {
                    LOG.trace("QuadTree.getEntry()", 
                            "LOAD NEW DATA! node.getName= "+iNode.getName()+" time= "+
                            (System.currentTimeMillis()-time)+" ms");
                }
            }
            return iNode.getEntry(aLat, aLon, aID);
        } else {
            int quarterNbr = 0; 
            if(aLon > iQT_bb.getCenterLon())
                quarterNbr |= 1;
            if(aLat < iQT_bb.getCenterLat())
                quarterNbr |= 2;
            
            if(iQTQuater[quarterNbr] != null) {
                return iQTQuater[quarterNbr].getEntry(aLat, aLon, aID, fileLoader);
            }            
        }
        
        return null;
    }
    
    /**
     * Remove a entry from the node.  
     * 
     * @param aLat - latitude coordinate of the entry. 
     * @param aLon - longitude coordinate of the entry. 
     * @param name - the name of the entry
     */
    public synchronized boolean removeEntry(int aLat, int aLon, String name) {        
        QuadTreeNode node = getNode(aLat, aLon);
        if(node != null) {
            return node.removeEntry(aLat, aLon, name);
        }        
        return false;
    }
    
    /**
     * Remove a entry from the node. The entry will be compared using equals.  
     * 
     * @param entry the entry to be removed
     * @return true if the object was found and removed 
     */
    public synchronized boolean removeEntry(QuadTreeEntry entry) {        
        QuadTreeNode node = getNode(entry.getLatitude(),entry.getLongitude());
        if(node != null) {
            return node.removeEntry(entry);
        }
        return false;
    }
    
    /**
     * Return a list with nodes that are inside the bounding box specified by 
     * the parameter. No nodes will be removed from the tree. 
     * 
     * @param bb the bounding box to compare with. 
     * @param v the vector where the removed node will be added to. 
     */
    public synchronized void getNodesInside(BoundingBox bb, Vector v) {
        if (iNode != null) {
            v.addElement(iNode);
        } else {            
            // For all 4 leaves of the node
            for (int i=0; i<4; i++) {
                // Check if the leaf exist
                if (iQTQuater[i] != null) {
                    // Check to see if the whole or a part of the region is inside the bounding box
                    if (iQTQuater[i].getBoundingBox().overlaps(bb) || iQTQuater[i].getBoundingBox().intersectWith(bb)) {
                        iQTQuater[i].getNodesInside(bb, v);
                    }
                }
            }
        }
    }
    
    public void appendTo(StringBuffer sb, String tabs) {
        sb.append(tabs).append("Quater ").append(iName);
        tabs = tabs + "\t";
        sb.append('\n').append(tabs).append(iQT_bb.toString());
        if (iNode != null) {
            sb.append("\n");
            iNode.appendTo(sb, tabs);
        }
        for(int i = 0; i<4; i++) {
            if (iQTQuater[i] != null) {
                sb.append("\n");
                iQTQuater[i].appendTo(sb,tabs);
            }
        }
    }
    
//    /**
//     * Return a list with nodes that are outside the bounding box specified by 
//     * the parameter. Any nodes outside the bounding box will be removed from 
//     * the tree. 
//     * 
//     * @param bb the bounding box to compare with. 
//     * @param v the vector where the removed node will be added to. 
//     * @return true if we have added a node. 
//     */
//    public boolean getNodesOutside(BoundingBox bb, Vector v) {
//        
//        if(iNode != null) {
//            if(iNode.getAllEntrys() != null) {
//                /* If the node contains data and the bounding box is fully outside
//                 * the node we remove it from the list. We don't have to check if
//                 * the node is fully inside the bounding box, it has already been done
//                 * before we get here. */
//                if(!iQT_bb.intersectWith(bb)) {
//                    v.addElement(iNode);                    
//                    //Don't clear the node here, it will be done when we have saved data 
//                    return true;
//                }
//            }
//        } else {
//            /* For all 4 leaves of the node. */
//            for(int i=0; i<4; i++) {
//                /* If the leaf exist. */
//                if(iQTQuater[i] != null) {
//                    /* If parts of the bounding box is outside the node we 
//                     * continue down in the tree. */
//                    if(!iQTQuater[i].getBoundingBox().overlaps(bb)) {                        
//                        if(iQTQuater[i].getNodesOutside(bb, v)) {
//                            /* Remove the node when we have removed the data inside it.*/
//                            //Don't delete the node, the data in the node will be unloaded but we save the node?
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }
}
