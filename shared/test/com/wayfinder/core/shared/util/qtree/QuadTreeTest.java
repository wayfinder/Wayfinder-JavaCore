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
package com.wayfinder.core.shared.util.qtree;

import java.util.Vector;

import com.wayfinder.core.shared.BoundingBox;

import junit.framework.TestCase;

public class QuadTreeTest extends TestCase {
    
    private static final int maxLat = 200*QuadTree.MIN_RADIUS;
    private static final int minLat = -maxLat + 100;
    private static final int maxLon = 300*QuadTree.MIN_RADIUS;
    private static final int minLon = -maxLon + 50;
    private static final int maxNbrItem = 5;
    private static final String name = "foobar";
    
    // Test to create a QuadTree
    public void testCreate() {
        
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        
        assertEquals(name, qt.getName());
        
        BoundingBox bb = new BoundingBox(maxLat, minLat, maxLon, minLon);
        assertEquals(bb, qt.getBoundingBox());
        
    }
    
    // Add and remove a quadtree entry and check that the node is added and removed successfully 
    public void testQuadTreeEntry() {
        
        int lat = 50;
        int lon = 100;
        String name = "TestEntry";
        
        QTEntry entry = new QTEntry(lat, lon, name);
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        
        qt.addEntry(entry, null);        
        QuadTreeEntry e1 = qt.getEntry(lat, lon, name, null);
        assertEquals(e1, entry);
        
        qt.removeEntry(lat, lon, name);
        QuadTreeEntry e2 = qt.getEntry(lat, lon, name, null);
        assertEquals(null, e2);
        
    }
    
    // Create a node and check that the same node that has been added are returned. 
    public void testQuadTreeNode() {
        
        String name = "TestNode";
        
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        
        QuadTreeNode node = new QuadTreeNode(name, minLat, minLon, maxLat, maxLon);
        assertEquals(minLat, node.getMinLat());
        assertEquals(maxLat, node.getMaxLat());
        assertEquals(minLon, node.getMinLon());
        assertEquals(maxLon, node.getMaxLon());
        assertEquals(name, node.getName());
        
        qt.addNode(node);
        int cLat = (minLat/2)+(maxLat/2);
        int cLon = (minLon/2)+(maxLon/2);
        QuadTreeNode n = qt.getNode(cLat, cLon);
        assertEquals(node, n);
        
    }
    
    public void testAppendEntry() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        String name = "Entry";
        
        // Add less then 'maxNbrItem' and check that only one node is created. 
        for(int i=1; i<maxNbrItem; i++) {
            QTEntry e = new QTEntry(minLat+i*QuadTree.MIN_RADIUS, minLon+i*QuadTree.MIN_RADIUS, name+i);
            qt.addEntry(e, null);
        }
        
        Vector v = new Vector();
        qt.getAllNodes(v);
        assertEquals(1, v.size());
        
        // Continue adding entry and check that there are more then one node created. 
        for(int i=maxNbrItem; i<2*maxNbrItem; i++) {
            QTEntry e = new QTEntry(minLat+i*QuadTree.MIN_RADIUS, minLon+i*QuadTree.MIN_RADIUS, name+i);
            qt.addEntry(e, null);
        }
        
        v.removeAllElements();
        qt.getAllNodes(v);
        
        boolean same = ((1==v.size()) && v.size() > 0);
        assertFalse(same);
        
        
        
    }
    
    // Clear the quad tree and check that there are no nodes in the quadtree
    public void testClearQuadTree() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        String name = "Entry";
        int cLat = (minLat/2)+(maxLat/2);
        int cLon = (minLon/2)+(maxLon/2);
        
        QTEntry entry = new QTEntry(cLat, cLon, name);
        qt.addEntry(entry, null);
        qt.clear();
        
        Vector v = new Vector();
        qt.getAllNodesThatContainsLoadedEntrys(v);
        assertEquals(0, v.size());
        
    }
    
    public void testAddSamePosition20Times() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        int cLat = (minLat/2)+(maxLat/2);
        int cLon = (minLon/2)+(maxLon/2);
        
        for (int i = 0; i <20 ; i++) {
            QTEntry entry = new QTEntry(cLat, cLon, name);
            qt.addEntry(entry, null);
        }
    }
    
    /**
     * testing if removing of non existing entry don't throw an exception 
     * (e.g. NPE)  
     */
    public void testRemoveNonExistingEntry() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        String name = "Entry";
        int cLat = (minLat/2)+(maxLat/2);
        int cLon = (minLon/2)+(maxLon/2);
        
        QTEntry entry = new QTEntry(cLat, cLon, name);
        QTEntry entryFake = new QTEntry(cLat, cLon, name + "Fake");
        qt.addEntry(entry, null);
        qt.removeEntry(entryFake);
        
        Vector v = new Vector();
        qt.getAllNodesThatContainsLoadedEntrys(v);
        assertEquals(1, v.size());
    }
    
    /**
     * testing if removing of an entry with same name don't remove 
     * the wrong entry
     */
    public void testRemoveEntrySameName() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        
        String name = "Entry";
        int cLat = (minLat/2)+(maxLat/2);
        int cLon = (minLon/2)+(maxLon/2);
        
        QTEntry entry = new QTEntry(cLat, cLon, name);
        QTEntry entry2 = new QTEntry(cLat, cLon-1, name);
        QTEntry entry3 = new QTEntry(cLat, cLon+1, name);
        qt.addEntry(entry, null);
        qt.removeEntry(entry2);
        qt.removeEntry(entry3);
        
        Vector v = new Vector();
        qt.getAllNodesThatContainsLoadedEntrys(v);
        assertEquals(1, v.size());
    }
    
    /**
     * testing if remove of an entry with 
     * {@link QuadTree#removeEntry(QuadTreeEntry)} method
     */
    public void testRemoveEntry() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        
        String name = "Entry";
        
        QTEntry entry = new QTEntry(minLat, maxLon, name);
        QTEntry entry2 = new QTEntry(minLat, minLon, name);
        QTEntry entry3 = new QTEntry(maxLat, minLon, name);
        qt.addEntry(entry, null);
        qt.addEntry(entry2, null);
        qt.addEntry(entry3, null);
        
        assertTrue(qt.removeEntry(entry));
        assertTrue(qt.removeEntry(entry3));

        Vector v = new Vector();
        qt.getAllNodesThatContainsLoadedEntrys(v);
        assertEquals(1, v.size());
    }
    
    /**
     * testing if remove a removed entry donsen't keep references to others 
     * entries 
     */
    public void testRemoveAndAddSameEntry() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        
        QTEntry entry = new QTEntry(minLat, maxLon, name);
        QTEntry entry2 = new QTEntry(minLat, minLon, name);
        QTEntry entry3 = new QTEntry(maxLat, minLon, name);
        qt.addEntry(entry, null);
        qt.addEntry(entry2, null);
        qt.addEntry(entry3, null);
        
        qt.removeEntry(entry2);
        assertEquals(entry2.getNext(), null);
        
        qt.removeEntry(entry);
        assertEquals(entry.getNext(), null);
        
        qt.removeEntry(entry3);
        assertEquals(entry3.getNext(), null);
    }
    
    public void testAddSameObject() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        QTEntry entry = new QTEntry(minLat, maxLon, "Entry");
        
        qt.addEntry(entry, null);
        qt.addEntry(entry, null);
        
        QTEntry returnedEntry = (QTEntry) qt.getEntry(minLat, maxLon, "Entry", null);
        assertEquals(returnedEntry.getNext(), null);
    }
    
    public void testAddSameObject2() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        QTEntry entry1 = new QTEntry(minLat, maxLon, "Entry1");
        QTEntry entry2 = new QTEntry(minLat, maxLon, "Entry2");
        QTEntry entry3 = new QTEntry(minLat, maxLon, "Entry3");
        
        qt.addEntry(entry1, null);
        qt.addEntry(entry2, null);
        qt.addEntry(entry3, null);
        qt.addEntry(entry2, null);
        
        QTEntry returnedEntry = (QTEntry) qt.getEntry(minLat, maxLon, "Entry2", null);
        
        assertEquals(returnedEntry.getNext(), entry1);
    }
    
    public void testAddSameObject3() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        QTEntry entry1 = new QTEntry(minLat, maxLon, "Entry1");
        QTEntry entry2 = new QTEntry(minLat, maxLon, "Entry2");
        QTEntry entry3 = new QTEntry(minLat, maxLon, "Entry3");
        
        qt.addEntry(entry1, null);
        qt.addEntry(entry2, null);
        qt.addEntry(entry3, null);
        qt.addEntry(entry1, null);
        
        QTEntry returnedEntry = (QTEntry) qt.getEntry(minLat, maxLon, "Entry1", null);
        
        assertEquals(returnedEntry.getNext(), null);
    }
    
    public void testAddSameObject4() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        QTEntry entry1 = new QTEntry(minLat, maxLon, "Entry1");
        QTEntry entry2 = new QTEntry(minLat, maxLon, "Entry2");
        QTEntry entry3 = new QTEntry(minLat, maxLon, "Entry3");
        
        qt.addEntry(entry1, null);
        qt.addEntry(entry2, null);
        qt.addEntry(entry3, null);
        qt.removeEntry(entry1);
        qt.addEntry(entry2, null);
        
        QTEntry returnedEntry = (QTEntry) qt.getEntry(minLat, maxLon, "Entry2", null);
        
        assertEquals(returnedEntry.getNext(), null);
    }
    
    public void testAddSameObject5() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        QTEntry entry1 = new QTEntry(minLat, maxLon, "Entry1");
        QTEntry entry2 = new QTEntry(minLat, maxLon, "Entry2");
        QTEntry entry3 = new QTEntry(minLat, maxLon, "Entry3");
        
        qt.addEntry(entry1, null);
        qt.addEntry(entry2, null);
        qt.addEntry(entry3, null);
        qt.removeEntry(minLat, maxLon, "Entry1");
        qt.addEntry(entry2, null);
        
        QTEntry returnedEntry = (QTEntry) qt.getEntry(minLat, maxLon, "Entry2", null);
        
        assertEquals(returnedEntry.getNext(), null);
    }
    
    public void testAddSameObject6() {
        QuadTree qt = new QuadTree(minLat, minLon, maxLat, maxLon, maxNbrItem, name);
        QTEntry entry = new QTEntry(minLat, maxLon, "Entry");
        
        qt.addEntry(entry, null);
        qt.addEntry(entry, null);
        
        QuadTreeNode qtn = qt.getNode(minLat, maxLon);
        assertEquals(1, qtn.getSize());
    }
    
}
