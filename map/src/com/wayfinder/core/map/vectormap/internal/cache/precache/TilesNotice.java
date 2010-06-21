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

public class TilesNotice {
    
    protected static final int offsetEntrySize = 4;
    
    protected int m_offset;
    // start lat
    protected int m_startLatIdx;
    // end lat
    protected int m_endLatIdx;
    // start lon
    protected int m_startLonIdx;
    // end lon
    protected int m_endLonIdx;
    
    /// Nbr of layers which also is the size of m_impRange.
    protected int m_nbrLayers;
    /**
     *  Array of importance ranges for each layer.
     *  Note that adding another template to this class will cause
     *  gcc to freak out.
     */
    protected int[][] m_impRange;
    
    
    public TilesNotice() {
        m_offset = -1;
        m_nbrLayers =  0 ;
        m_impRange  = null;
        
        
    }
    
    public TilesNotice(  TilesNotice other ){
        m_impRange = null;
        this.clone(other);
    }
    
    public void load(BitBuffer buf ){
        
        
        // Offset.
        m_offset = (int)buf.nextInt();
        
        // start lat
        m_startLatIdx =(int)buf.nextInt();
        
        // end lat
        m_endLatIdx = (int)buf.nextInt();
        
        // start lon
        m_startLonIdx =(int)buf.nextInt();
        
        // end lon
        m_endLonIdx =(int)buf.nextInt();
        
        // Size of m_impRange.
        m_nbrLayers = buf.nextShort();
        
        m_impRange = new int[m_nbrLayers][3];
        for ( int i = 0; i < m_nbrLayers; i++ ) {
            int m_layerID = (int) buf.nextShort();
            int firstImp = (int)buf.nextInt();
            int lastImp = (int)buf.nextInt();
            
            m_impRange[i][0] = m_layerID;
            m_impRange[i][1] = firstImp;
            m_impRange[i][2] = lastImp;
        }
    }
    
    public int getOffset(TileMapParams param ){
        if ( param.getTileIndexLat() < m_startLatIdx ||
            param.getTileIndexLat() > m_endLatIdx ||
            param.getTileIndexLon() < m_startLonIdx ||
            param.getTileIndexLon() > m_endLonIdx ) {
            return -1;
        }
        
        int[] impRange = getImpRange( param );
        if(impRange == null){
            //System.out.println("IMP range null  ==  BAD");
            return -1;
        }
        
        if ( param.getImportance() < impRange[1] || param.getImportance() > impRange[2]) {
            // Outside the importance range.;
            return -1;
        }
        
        int width = m_endLonIdx - m_startLonIdx + 1;
        
        return m_offset +  offsetEntrySize*( width * ( param.getTileIndexLat() - m_startLatIdx ) + (param.getTileIndexLon() - m_startLonIdx ));
        
        
    }
    
    
    public int getNextOffset() {
        int width = m_endLonIdx - m_startLonIdx + 1;
        int height = m_endLatIdx - m_startLatIdx + 1;
        return m_offset + width * height * offsetEntrySize;
    }
    
    public int[] getImpRange(TileMapParams param){
        int layer = param.getLayerID();
        //System.out.println("imprange length: "+m_impRange.length);
        for(int i = 0 ; i< m_impRange.length;i++){
            //System.out.println("ImpRange for : "+ param.getAsString() + " layer "+m_impRange[i][0]+" first "+m_impRange[i][1]+" last "+m_impRange[i][2]);
            int[] range =  m_impRange[i];
            if(range[0] == layer) {
                return range;
            }
        }
        return null;
    }
    
    public String toString(){
        return "m_offset: "+m_offset+ " m_startLatIdx "+m_startLatIdx+
            " m_endLatIdx "+m_endLatIdx+" m_startLonIdx "+m_startLonIdx+
            " m_endLonIdx "+m_endLonIdx+ " nbr layers "+(int)m_nbrLayers;
    }
    
    public void clone(TilesNotice other){
        
        m_offset = other.m_offset;
        m_startLatIdx = other.m_startLatIdx;
        m_endLatIdx = other.m_endLatIdx;
        m_startLonIdx = other.m_startLonIdx;
        m_endLonIdx = other.m_endLonIdx;
        m_nbrLayers = other.m_nbrLayers;
        
        m_impRange = null;
        if ( m_nbrLayers > 0 ) {
            this.m_impRange = new int[m_nbrLayers][ 3 ];
            for(int i = 0; i< m_nbrLayers;i++){
                System.arraycopy(other.m_impRange[i],0,this.m_impRange[i],0,other.m_impRange[i].length);
            }
            
        } else {
            m_impRange = null;
        }
        
        
    }
    
    public int updateOffset( int startOffset ){
        m_offset = startOffset;
        return getNextOffset();
    }
    
}
