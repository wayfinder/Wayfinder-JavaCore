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
package com.wayfinder.core.map.vectormap.internal.process;

import com.wayfinder.core.map.util.BitBuffer;

/* An Integer */
public class SimpleArg extends TileFeatureArg{
 
    private int size;
    private int[]valueByScaleIdx;
    private int m_value;
    
    SimpleArg(int type, int name, int size) {
        super(type, name);
        this.size = size;
    }    
    
    SimpleArg(int type, int name, int size, int[] aValueByScaleIdx, int aValue) {
        this(type,name,size);
        valueByScaleIdx = aValueByScaleIdx;
        m_value = aValue;
    }
    
    public boolean load(int type, BitBuffer bitBuffer, TileFeature map, TileFeatureArg prevArg, boolean save) {
        boolean sameAsPrevious = bitBuffer.nextBits( 1 ) != 0;
        if ( sameAsPrevious ) {
            // Use previous argument value.
            if(prevArg!=null){
                 setValue( ((SimpleArg)prevArg).getValue(0) );
            }
        } else {
            // Not same as previous.
            // Read if multiple values.
            boolean multi = bitBuffer.nextBits( 1 ) != 0;
            if ( multi ) {
                // Multiple values.
                int nbrValues = bitBuffer.nextBits( 5 );
                if(this.valueByScaleIdx == null || this.valueByScaleIdx.length < nbrValues) {
                    valueByScaleIdx = new int[nbrValues];
                }
                for ( int i = 0; i < nbrValues; i++ ) {
                    int value = bitBuffer.nextBits(size);
                    valueByScaleIdx[i] = value;
                }
            } else {
                // Only one value.
                m_value = bitBuffer.nextBits(size);
            }
        }
        
        if(save)
            map.addArgs(new SimpleArg(type,getName(),size,valueByScaleIdx,m_value));
        return true;
    }
    
    public void setValue( int value ){
        m_value = value;
    }
    /**
     * Get value by scale index.
     * @param scaleIdx
     * @return argument specific value at the specified scale.
     */
    public int getValue( int scaleIdx ){
        
        if(this.valueByScaleIdx == null) {
            return m_value;
        }
        return valueByScaleIdx[scaleIdx];
    }
    
    /**
     * @return the tile argument type
     */
    public int getType(){
        return TileFeatureArg.SIMPLEARG;
    }
    public int getNbrValues() {
        if (valueByScaleIdx == null) {
            return 1;
        }
        
        return valueByScaleIdx.length; 
    }
}
