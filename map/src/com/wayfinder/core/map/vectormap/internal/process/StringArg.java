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

public class StringArg extends TileFeatureArg {

    private String[] stringByScaleIdx;
    
    StringArg(int type, int name) {
        super(type, name);
    }
    
    StringArg(int aType, String []aStringByScaleIdx, int name) {
        super(TileFeatureArg.STRINGARG, name);
        stringByScaleIdx = aStringByScaleIdx;
    }

    /**
     * @return the tile argument type
     */
    public int getType(){
        return TileFeatureArg.STRINGARG;
    }
    
    public int getNbrOfValues(){
        return stringByScaleIdx.length;
    }    
    
    public String getValue( int scaleIdx ){
        if(scaleIdx >= stringByScaleIdx.length){
            return stringByScaleIdx[0];
        }else{
            return stringByScaleIdx[ scaleIdx ];
        }
    }
    
    public boolean load( int type, BitBuffer bitBuffer, TileFeature tileFeature, TileFeatureArg tfa, boolean save) {
        // Read if more than one values exists.
        boolean multi = bitBuffer.nextBits( 1 ) != 0;
        if ( multi ) {
            // Multiple values.
            int nbrValues = bitBuffer.nextBits( 5 );
            stringByScaleIdx = new String[nbrValues];
            bitBuffer.alignToByte();
            for ( int i = 0; i < nbrValues; ++i ) {
                stringByScaleIdx[i]= bitBuffer.nextStringUTF();
            }
        } else {
            // Only one value.
            bitBuffer.alignToByte();
            stringByScaleIdx = new String[1];
            stringByScaleIdx[0] = bitBuffer.nextStringUTF() ;
        }
        
        if(save)
            tileFeature.addArgs(new StringArg(type,stringByScaleIdx,getName()));
        
        return true;
    }
}
