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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.map.vectormap.internal.cache;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.util.qtree.QuadTree;
import com.wayfinder.pal.persistence.MemoryPersistenceLayer;

import junit.framework.TestCase;

/**
 *
 */
public class FileCacheTest extends TestCase {
    
    private FileCache m_fileCache;
    
    private TileMapParams[] m_paramsNonTile;
    private TileMapParams[] m_paramsTile;
    
    //private String[] m_nonTileData;
    private byte[][] m_nonTileData;
    private byte[][] m_tileData;
    
    private int m_nonTileDataSize;
    private int m_tileDataSize;
    
    private TileMapFormatDesc m_tmfd;

    /**
     * @param name
     */
    public FileCacheTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        DataInputStream din = new DataInputStream(new FileInputStream(
                "./map/test/com/wayfinder/core/map/vectormap/internal/cache/tmfd"));
        byte[] data = new byte[din.available()];
        din.readFully(data);
        m_tmfd = new TileMapFormatDesc();
        m_tmfd.load(new BitBuffer(data), null);
        m_fileCache = new FileCache(MemoryPersistenceLayer.getPersistenceLayer(), false);
        m_fileCache.setTileMapFormatDesc(m_tmfd);
        
        m_paramsNonTile = new TileMapParams[]{
                new TileMapParams("btat_petrolstation.png", "btat_petrolstation.png")
                };
        din = new DataInputStream(new FileInputStream(
                "./map/test/com/wayfinder/core/map/vectormap/internal/cache/data_nontile"));
        data = new byte[din.available()];
        din.readFully(data);
        m_nonTileData = new byte[1][];
        m_nonTileData[0] = data;
        m_nonTileDataSize = data.length;
        
        m_paramsTile = new TileMapParams[2];
        m_paramsTile[0] = new TileMapParams("G+1aA7V0Y", "G+1aA7V0Y");  //map
        m_paramsTile[1] = new TileMapParams("T+1aA7V0Y", "G+1aA7V0Y");  //strings
        m_tileData = new byte[2][];
        m_tileDataSize = 0;
        din = new DataInputStream(new FileInputStream(
                "./map/test/com/wayfinder/core/map/vectormap/internal/cache/data_tile_0"));
        data = new byte[din.available()];
        din.readFully(data);
        m_tileData[0] = data;
        m_tileDataSize += data.length;
        din = new DataInputStream(new FileInputStream(
                "./map/test/com/wayfinder/core/map/vectormap/internal/cache/data_tile_1"));
        data = new byte[din.available()];
        din.readFully(data);
        m_tileData[1] = data;
        m_tileDataSize += data.length;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testOpenCache() {
        boolean success = m_fileCache.openCache();
        assertTrue(success);
    }
    
    public void testWriteNonTileData() {
        m_fileCache.openCache();
        
        int initialOffset = m_fileCache.getCurrentOffset();
        boolean success = m_fileCache.writeDataToCache(
                m_nonTileData, m_paramsNonTile, 
                m_paramsNonTile[0], m_nonTileDataSize, 1, (short)0);
        assertTrue(success);
        int currentOffset = m_fileCache.getCurrentOffset();
        
        FileHandler fh = m_fileCache.getCacheFileHandler();
        assertEquals(0, fh.getPageNumber());
        
        int expectedTotalSize = 0;
        expectedTotalSize += m_nonTileDataSize;
        expectedTotalSize += FileCache.CACHE_HEADER_OFFSET;
        //non-tile
        expectedTotalSize += m_paramsNonTile[0].getTileID().getBytes().length + 3;
        assertEquals(expectedTotalSize, (currentOffset - initialOffset));
        
        try {
            DataInputStream din = fh.getPageDataInputStream(0);
            
            int writtenTotalSize = din.readInt();
            assertEquals(expectedTotalSize, writtenTotalSize);
            
            short writtenEmptyImp = din.readShort();
            assertEquals((short)0, writtenEmptyImp);
            
            byte paramStringSize = din.readByte();
            assertEquals("btat_petrolstation.png".getBytes().length, paramStringSize);
            
            byte[] paramData = new byte[paramStringSize];
            din.read(paramData, 0, paramStringSize);
            String str = new String(paramData);
            assertEquals("btat_petrolstation.png", str);
            
            short dataSize = din.readShort();
            assertEquals(m_nonTileDataSize, dataSize);
            
            byte[] data = new byte[dataSize];
            din.readFully(data);
            for (int i = 0; i < dataSize; i++) {
                assertEquals(m_nonTileData[0][i], data[i]);
            }
        } catch (IOException e) {
            fail("IOException when getting input stream on page 0! "+e);
        }
    }
    
    public void testWriteTileData() {
        m_fileCache.openCache();
        int initialOffset = m_fileCache.getCurrentOffset();
        boolean success = m_fileCache.writeDataToCache(
                m_tileData, m_paramsTile, 
                m_paramsTile[0], m_tileDataSize, 2, (short)0);
        assertTrue(success);
        int currentOffset = m_fileCache.getCurrentOffset();
        
        FileHandler fh = m_fileCache.getCacheFileHandler();
        assertEquals(0, fh.getPageNumber());
        
        int expectedTotalSize = 0;
        expectedTotalSize += m_tileDataSize;
        expectedTotalSize += FileCache.CACHE_HEADER_OFFSET;
        //tile
        expectedTotalSize += (FileCache.CACHE_IMP_OFFSET * 2);  //2 imp
        //nothing cached yet, so there can't be any already cached data
        assertEquals(expectedTotalSize, (currentOffset - initialOffset));
        
        try {
            DataInputStream din = fh.getPageDataInputStream(0);
            int writtenTotalSize = din.readInt();
            assertEquals(expectedTotalSize, writtenTotalSize);
        } catch (IOException e) {
            fail("IOException when getting input stream on page 0! "+e);
        }
    }
    
    public void testExistsInCache() {
        m_fileCache.openCache();
        m_fileCache.writeDataToCache(
                m_tileData, m_paramsTile, 
                m_paramsTile[0], m_tileDataSize, 2, (short)0);
        
        boolean success = m_fileCache.existInCache(m_paramsTile[0]);
        assertTrue(success);
        
        success = m_fileCache.existInCache(m_paramsTile[1]);
        assertTrue(success);
    }
    
    public void testRemoveFromCache() {
        m_fileCache.openCache();
        m_fileCache.writeDataToCache(
                m_tileData, m_paramsTile, 
                m_paramsTile[0], m_tileDataSize, 2, (short)0);
        
        boolean success = m_fileCache.existInCache(m_paramsTile[0]);
        assertTrue(success);
        
        m_fileCache.removeFromCache(m_paramsTile[0]);
        
        success = m_fileCache.existInCache(m_paramsTile[0]);
        assertFalse(success);
        
        success = m_fileCache.existInCache(m_paramsTile[1]);
        assertFalse(success);
    }
    
    public void testSaveCache() {
        m_fileCache.openCache();
        m_fileCache.writeDataToCache(
                m_tileData, m_paramsTile, 
                m_paramsTile[0], m_tileDataSize, 2, (short)0);
        int totalTileDataSize = m_fileCache.getCurrentOffset();
        m_fileCache.writeDataToCache(
                m_nonTileData, m_paramsNonTile, 
                m_paramsNonTile[0], m_nonTileDataSize, 1, (short)0);
        
        Hashtable indexTable = m_fileCache.getIndexTableHashtable();
        QuadTree cacheQT = m_fileCache.getCacheQuadTree();
        
        m_fileCache.saveCache();
        FileHandler fh = m_fileCache.getCacheFileHandler();
        try {
            DataInputStream din = fh.getInfoFile().openDataInputStream();
            assertEquals(5, din.available());
            byte page = din.readByte();
            assertEquals(0, page);
            int version = din.readInt();
            assertEquals(FileCache.CACHE_VERSION, version);
            
            din = fh.getIndexFile().openDataInputStream();
            int size = din.readInt();
            assertEquals(1, size);
            page = din.readByte();
            assertEquals(0, page);
            int offset = din.readInt();
            //the tile data was written first
            assertEquals(totalTileDataSize, offset);
            String tileID = din.readUTF();
            assertEquals("btat_petrolstation.png", tileID);

            QuadTree savedQT = new QuadTree(Integer.MIN_VALUE/2,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE/2,
                    Integer.MAX_VALUE,
                    FileCache.MAX_NBR_ITEM_PER_NODE,
                    "root_");
            fh.readQuadTreeFromFile(savedQT, false);
            Vector savedQTNodes = new Vector();
            savedQT.getAllNodes(savedQTNodes);
            Vector cachedQTNodes = new Vector();
            cacheQT.getAllNodes(cachedQTNodes);
            assertEquals(cachedQTNodes.size(), savedQTNodes.size());
            
            Hashtable savedIndexTable = new Hashtable();
            fh.readIndexTableFromFile(savedIndexTable);
            assertEquals(indexTable.size(), savedIndexTable.size());
            Enumeration keys = indexTable.keys();
            //see if contents match
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                IndexTableEntry entry = (IndexTableEntry) indexTable.get(key);
                IndexTableEntry savedEntry = (IndexTableEntry) savedIndexTable.get(key);
                assertEquals(entry.getPage(), savedEntry.getPage());
                assertEquals(entry.getOffset(), savedEntry.getOffset());
                assertEquals(entry.getName(), savedEntry.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
