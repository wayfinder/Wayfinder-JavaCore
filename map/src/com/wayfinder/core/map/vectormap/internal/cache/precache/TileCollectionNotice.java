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
package com.wayfinder.core.map.vectormap.internal.cache.precache;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;

/**
 *    Class describing tiles covering a rectangle.
 */
public class TileCollectionNotice {
    /**
     *    Which index in m_tiles to use for which layer id.
     *    Note that several layer ids can use the same index
     *    in m_tiles.
     */
    private int[][]m_indexByLayerID = null;
    
    /**
     *    Vector of tiles. Index should come from m_indexByLayerID.
     */
    private TilesForAllDetailsNotice[] m_tilesForAllDetails = null;
    
    /** Creates a new instance of TileCollection */
    public TileCollectionNotice() {
        
    }
    
    public int load( BitBuffer buf ) {
        
        // Load the m_indexByLayerID map.
        char size = buf.nextShort();
        //FIXME use layer nbr instead of layer id!
        m_indexByLayerID = new int[size][2];   
        
        for ( int i = 0; i < size; i++ ) {
            // Layer ID.
            char layerID = buf.nextShort();
            // Index.
            char index = buf.nextShort();
            // m_indexByLayerID[ i ] = new int[2];
            m_indexByLayerID[ i ] [0] = index;
            m_indexByLayerID[ i ] [1] = layerID;
        }
        
        // Size of m_tilesForAllDetails.
        size = buf.nextShort();       
        m_tilesForAllDetails = new TilesForAllDetailsNotice[size];
        
        // Load all the TilesForAllDetailsNotices.
        for ( int i = 0; i < size; i++ ) {
            TilesForAllDetailsNotice notice = new TilesForAllDetailsNotice();
            notice.load( buf );
            m_tilesForAllDetails[i] =  notice ;
        }
        
        return buf.getCurrentOffset();
    }
    
    
    public TilesNotice getNotice(TileMapParams param ){
        int layerID = param.getLayerID();
        
        for(int i = 0;i<m_indexByLayerID.length;i++){
            int[] vec = m_indexByLayerID[i];
            int index   = vec[0];
            int layer   = vec[1];
            if(layerID == layer ){
                return m_tilesForAllDetails[index].getNotice(param);
            }
        }
        return null;
    }
    
    public int getOffset(TileMapParams params){
        TilesNotice notice = getNotice( params );
        if ( notice == null ) {
            return -1;
        }
        return notice.getOffset( params );
        
    }
    
    public int[] getImpRange(TileMapParams param){
        TilesNotice notice = getNotice(param);
        return notice.getImpRange(param);
    }
    
    public int updateOffset( int startOffset ) {
        int nextOffset = startOffset;
        for ( int i = 0; i  < m_tilesForAllDetails.length; i++ ) {
            nextOffset = m_tilesForAllDetails[ i ].updateOffset( nextOffset );
        }
        // Note that we have to reserve extra space an extra offset last,
        // so that it is possible to calculate the size of one databuf by
        // subtracting two offsets.
        return nextOffset + TilesNotice.offsetEntrySize;  /*eller inte?*/
    }
    
    
    
}
