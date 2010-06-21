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
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;

public class SFDMultiBufferReader {
    
    //findbugs: code that was using m_current was commented-out
    /// The current buffer to return.
//    Object[] m_current;
    
    /// The big buffer
    BitBuffer m_bigBuf;
    
    /// The header.
    SFDLoadableHeader m_header;
    
    /// A parameter of one of the tilemap buffers.
    TileMapParams m_prototypeParam;
    
    /// The importance range.
    int[] m_impRange;
    
    /// The current importance.
    int m_curImp;
    
    /// The current layer ID.
    int m_layerID;
    
    /// Nbr of layers.
    int m_nbrLayers;
    
    /// The current layer index.
    int m_curLayer;
    
    /// Bitfield containing the importances that contains data.
    private int m_existingImps;
    
    /// If the reader contains another buffer.
    boolean m_hasNext;
    
    /// If the current buffer is a string map or not.
    boolean m_strings;
    
    
    
    /**
     *   Creates new SFDMultiBufferReader.
     *   Takes over ownership of the buffer.
     */
    SFDMultiBufferReader( BitBuffer bigbuf, TileMapParams param, SFDLoadableHeader header ){
        m_bigBuf = bigbuf;
        m_nbrLayers = m_bigBuf.nextByte();
        
        m_curLayer = 0;
        m_prototypeParam = param;
        
        m_impRange = null;
        m_header = header;
        
        m_hasNext = readNextMetaIfNeeded();        
    }
    
    /**
     *    Read the meta data for next layer, if there are any more layers.
     *    @return If  the reader contains another buffer.
     */
    public boolean readNextMetaIfNeeded() {
        if ( m_curLayer < m_nbrLayers ) {
            m_layerID = m_bigBuf.nextByte();
            m_existingImps = m_bigBuf.nextShort();
            
            // Get the importance range for this layer.
            TileMapParams tmpParam = m_prototypeParam;
            tmpParam.setLayerID(m_layerID);
            m_impRange = m_header.getImportanceRange(tmpParam);
            if(m_impRange != null) {
                m_curImp = m_impRange[1]; // 0 layer 1 first 2 last
            }            
                        
            m_strings = false;
            return true;
        } // else
        return false;
    }
    
    int getExistingImportances() {
        return m_existingImps;
    }
    
    /**
     *   Returns true if there are more buffers.
     */
    public boolean hasNext(){
        return m_hasNext;
    }
    
    /**
     *    Move to the next buffer.
     *    @return If  the reader contains another buffer.
     */
    public boolean moveToNext() {
        // Toggle between strings or data map.
        m_strings = !m_strings;
        
        if ( m_strings ) {
            return true;
        } // Else move to next importance, or possibly layer.
        
        // Move to next importance.
        m_curImp++;
        if ( m_curImp > m_impRange[m_impRange.length-1]) {
            // The importance was out of range.
            // Move to next layer.
            m_curLayer++;
            return readNextMetaIfNeeded();
        }
        return true;
    }
    
    /**
     *   Returns the current buffer pair.
     *   The buffer must be  deleted or used
     *   in some way before calling readNext
     *   the next time.
     */
/*    Object[] getCurrent() {
        return m_current;
    }*/
    
    // Method checks that the read param string is what was expected.
    static boolean checkParamStr( String readDebugParamStr, TileMapParams expectedParam ) {
        if (!TileMapParamTypes.isMap( readDebugParamStr)) {
            // Should have been a map.
            return false;
        }
        String tmp = readDebugParamStr;
        TileMapParams debugParam = new TileMapParams( tmp, tmp );
        
        // The language can differ for string tilemaps.
        if ( debugParam.getTileMapType() == 1 ) {
            debugParam.setLanguageType( expectedParam.getLanguageType() );
        }
        // Now check the strings.
        return debugParam.getAsString().equals(expectedParam.getAsString());
        
    }
    
    
    
    /**
     *   Returns the next  param/buffer pair.
     *   The buffer must be  deleted or used
     *   in some way before calling readNext
     *   the next time.
     *   @return The new current bufpair.
     */
    /*
    Object[] readNext( int lang ) {
        long beginRead = System.currentTimeMillis();
        int mapOrStrings = TileMapParams.MAP;  //We use 1, Symbian uses 0
        if(m_strings)
            mapOrStrings = TileMapParams.STRINGS;
        m_current = new Object[2];
        
        m_current[0] = new TileMapParams();
        ((TileMapParams)m_current[0]).setParams( 9,      // Server prefix
                m_prototypeParam.useGZip(),
                m_layerID,
                mapOrStrings,
                m_curImp, 
                lang,
                m_prototypeParam.getTileIndexLat(),
                m_prototypeParam.getTileIndexLon(),
                m_prototypeParam.getDetailLevel(),null );
        
        
    System.out.println("TileMapparams : "+(int)(System.currentTimeMillis()-beginRead));
       // System.out.println(Integer.toBinaryString((int)m_existingImps | 0x80000000));
        // Check if existing.
        boolean existing = ((m_existingImps >> m_curImp) & 0x1) == 1;
        
       // System.out.println("existing: "+existing+" curimp: "+m_curImp);
        BitBuffer sean_combs = null;
        
        if ( existing ) {
          
            // Length of buffer
            int bufLen =(int) m_bigBuf.nextInt();
            
            // Read the map buffer.
            if ( bufLen > 0 ) {
                
                sean_combs = new BitBuffer( m_bigBuf.nextByteArray(bufLen) );
                System.out.println("Sean Combs: "+(int)(System.currentTimeMillis() - beginRead));
                
            }else{
                System.out.println("empty buffer for "+ ((TileMapParams)m_current[0]).getAsString() );
            }
            
            if ( m_header.readDebugParams() ) {
                
                String paramStr = m_bigBuf.nextString();
               // if(Utils.PRECACHE_TRACE){
                    System.out.println("[SFDMBR]:readNext: Read: \"" + paramStr+"\"");
                 //   System.out.println("checkparamstr: "+ checkParamStr( paramStr, (TileMapParams)m_current[0] ) );
                //}
            }
            System.out.println("existing: "+(int)(System.currentTimeMillis()-beginRead));
        }else{
           // System.out.println("not existing");
        }
        
        m_hasNext = moveToNext();
         if(Utils.PRECACHE_TRACE)
        System.out.println("[SFDMBR]:readNext: Expected param "
                + ((TileMapParams)m_current[0]).getAsString()+", existing = " + existing);
        
        m_current[1] = sean_combs;
        System.out.println("read next: "+(int)(System.currentTimeMillis() - beginRead));
        
        return m_current;
        
        
    }
     **/
    
}


