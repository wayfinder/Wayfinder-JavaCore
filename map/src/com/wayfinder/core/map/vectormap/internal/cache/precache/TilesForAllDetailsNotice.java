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
import java.util.Vector;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;

/**
 *    Class describing all detail levels for tiles covering a rectangle.
 */
public class TilesForAllDetailsNotice {
    
    /**
     *    The starting detail level, corresponding to the first index of
     *    m_tilesNotice.
     */
    private char m_startDetail;
    
    /**
     *    The tiles notices. The index in the vector corresponds to
     *    (detaillevel - m_startDetail).
     */
    private Vector m_tilesNotice = null;
    
    /** Creates a new instance of TilesForAllDetailsNotice */
    public TilesForAllDetailsNotice() {
    }
    
    public void load(BitBuffer buf ){
        // Start detail.
        m_startDetail = buf.nextShort();
        
        // The notices.
        int size = buf.nextShort();       
        m_tilesNotice = new Vector( size );
        
        for ( int i = 0; i < size; i++ ) {
            TilesNotice notice = new TilesNotice();
            notice.load( buf );
            m_tilesNotice.addElement( notice );
        }
    }
    
    public TilesNotice getNotice(TileMapParams param ){
        TilesNotice notice = getTilesForDetail( param.getDetailLevel() );        
        return notice;
    }
    
    public TilesNotice getTilesForDetail( int detail ){
        int offset = detail - m_startDetail;        
        if ( offset >= 0 && offset < (int) m_tilesNotice.size() ) {
            return (TilesNotice)m_tilesNotice.elementAt(offset);
        } else {
            return null;
        }
    }
    
    public int updateOffset( int startOffset ) {
        int nextOffset = startOffset;
        for ( int i = 0; i < m_tilesNotice.size(); i++ ) {
            nextOffset = ((TilesNotice)m_tilesNotice.elementAt(i)).updateOffset( nextOffset );
        }
        return nextOffset;
    }
    
}
