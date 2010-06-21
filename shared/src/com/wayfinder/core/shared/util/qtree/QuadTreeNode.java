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

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * Class that holds the information about a node in the QuadTree. 
 * 
 * Each node in the quad tree has a bounding box and a name. To each
 * node in the tree there can be one or more entrys added. The maximal
 * number of entrys allowed in each node are decided when creating the 
 * QuadTree. 
 * 
 * 
 */
public class QuadTreeNode {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(QuadTreeNode.class);
    
    private String m_name;
    private int m_size;
    
    private int iMinLat, iMinLon, iMaxLat, iMaxLon;
    private QuadTreeEntry m_head = null;
    private QuadTreeEntry m_tail = null;
    
    /* True if data has been added to the node at any time. This variable is
     * used to determine if there are saved data that can be loaded from file. 
     * The m_HasBeenChanged variable only check if data has been added. */
    private boolean iHasEntrysAdded = false;
    
    /* Return true if a entry has been added to the node. */
    private boolean m_HasBeenChanged;
    
    public QuadTreeNode() {        
        iMinLat = iMinLon = iMaxLat = iMaxLon = -1;
        clear();
    }
    
    public QuadTreeNode(String aName, int aMinLat, int aMinLon, int aMaxLat, int aMaxLon) {
        m_name = aName;
        iMinLat = aMinLat;
        iMinLon = aMinLon;
        iMaxLat = aMaxLat;
        iMaxLon = aMaxLon;
    }
    
    /**
     * Add a entry to the node. 
     * 
     * @param entry the quad tree entry to be added. 
     */
    public void addEntry(QuadTreeEntry entry) {       
    	if (m_head == null) {                    
            m_head = entry;
            m_tail = entry;            
            //late clean of next references
            entry.setNext(null);
    	} else if (entry != m_head && entry != m_tail && entry.getNext() == null) {
            entry.setNext(m_head);
            m_head = entry;
        } else {
            // Entry already exists in the node. Adding the same object will cause
            // loops when traversing the list.
            if (LOG.isError()) {
                LOG.error("QuadTreeNode.addEntry()", "Object already exists in the node!");
            }
            return;
        }
        m_HasBeenChanged = true;
        m_size++;
        iHasEntrysAdded = true;
    }
    
    /**
     * Return true if data should be loaded
     */
    public boolean shouldDataBeLoaded() {
        return (m_head == null && iHasEntrysAdded);
    }
    
    /**
     * Clear any added data from the node. The node will remain as
     * a empty unloaded node. 
     * 
     */
    public void clear() {
        m_head = null;
        m_tail = null;
        m_size = 0;
        m_HasBeenChanged = false;
    }
    
    /**
     * Return true if a entry has been added to the node. 
     * 
     * @return true if a entry has been added to the node. 
     */
    public boolean hasBeenChanged() {
        return m_HasBeenChanged;
    }
    
    /**
     * Return the total number of entrys in the node. 
     * 
     * @return the total number of entrys in the node. 
     */
    public int getSize() {
        return m_size;
    }
    
    public QuadTreeEntry getAllEntrys() {
        return m_head;
    }
        
    /**
     * Return the entry with the values specified by the parameters or null
     * if no matching entry exist. 
     * 
     * @param aLat the latitude for the entry.
     * @param aLon the longitude of the entry.
     * @param aID the id of the entry. 
     * @return the entry if it exist or null if not. 
     */
    public QuadTreeEntry getEntry(int aLat, int aLon, String aID) {
        
        //XXX: The data should have been loaded. See QuadTree.getEntry.  
        if (m_head == null) {
            return null;
        }
        
        QuadTreeEntry qte = m_head;
        QuadTreeEntry tmp;
        
        do {                
            tmp = qte;
            qte = qte.getNext();
            
            if(tmp.getName().equals(aID)) {
                return tmp;
            }            
        } while (qte != null);
        
        return null;
    }
    
    /**
     * 
     * Remove a item from the node. 
     * 
     * @param aLat latitude for the item
     * @param aLon longitude for the item
     * @param aID name of the item
     * 
     * @return true if the object could be found and removed  
     */
    public boolean removeEntry(int aLat, int aLon, String aID) {           
        if(m_head != null) {
            QuadTreeEntry qte = m_head;            
            if(qte.getName().equals(aID)) {
                if(qte.getNext() != null) {
                	m_head = qte.getNext();
                    //clear the next reference of the removed entry 
                    qte.setNext(null);
                    m_size--;
                } else {
                    clear();
                }
                return true;
            } else {
            	QuadTreeEntry next = qte.getNext();
                while (next != null) {
                    if(next.getName().equals(aID)) {
                        qte.setNext(next.getNext());
                        if (qte.getNext() == null) {
                            m_tail = qte;
                        }
                        //clear the next reference of the removed entry 
                        next.setNext(null);
                        m_size--;
                        return true;
                    }
                    qte = next;
                    next = next.getNext();
                } 
            }
        }      
        if(LOG.isInfo()) {
            LOG.info("QuadTreeNode.removeEntry()", aID + " was not found");
        }
        return false;
    }
    
    /**
     * Remove an item from the node. 
     * Comparing will be done using equals. 
     * 
     * @param entry the entry to be remove
     * @return true if the object could be found and removed  
     */
    public boolean removeEntry(QuadTreeEntry entry) {           
        if(m_head != null) {
            QuadTreeEntry qte = m_head;            
            if(qte.equals(entry)) {
                if(qte.getNext() != null) {
                	m_head = qte.getNext();
                    //clear the next reference of the removed entry 
                    qte.setNext(null);
                    m_size--;
                } else {
                    clear();
                }
                return true;
            } else {
                QuadTreeEntry next = qte.getNext();
                while (next != null) {
                    if (qte.getNext().equals(entry)) { 
                        qte.setNext(next.getNext());
                        if (qte.getNext() == null) {
                            m_tail = qte;
                        }
                        //clear the next reference of the removed entry 
                        next.setNext(null);
                        m_size--;
                        return true;
                    }
                    qte = next;
                    next = next.getNext();
                } 
            }
        }
        if(LOG.isInfo()) {
            LOG.info("QuadTreeNode.removeEntry()", entry + 
                    " was not found");
        }
        return false;
    }    
    
    public int getMinLat() {
        return iMinLat;
    }
    
    public int getMinLon() {
        return iMinLon;
    }
    
    public int getMaxLat() {
        return iMaxLat;
    }
    
    public int getMaxLon() {
        return iMaxLon;
    }
    
    public String getName() {
        return m_name;
    }


    public int getRadius() {
        return iMaxLon - iMinLon;
    }
    
    public void appendTo(StringBuffer sb, String tabs) {
        sb.append(tabs);
        sb.append("Node ").append(m_name);
        sb.append(" BB[[").append(iMinLat).append(',').append(iMinLon).append("],[");
        sb.append(iMaxLat).append(',').append(iMaxLon).append("]]");
        QuadTreeEntry next = m_head;
        while (next != null) {
            sb.append("\n").append(tabs).append(next.toString());
            next = next.getNext();
        }
    }
    
}
