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

public class TileArgContainer {
   
    //Containing TileFeatureArg ordered by index.
    TileFeatureArg[] argByIndex = null; 
    
    public TileArgContainer(){
    }
    
    
    public void load( BitBuffer buf ){
        buf.alignToByte();
        int size = buf.nextShort();
        argByIndex = new TileFeatureArg[size];
        for ( int i = 0; i < size; i++ ) {
            TileFeatureArg arg = TileFeatureArg.loadFullArg( buf );
            argByIndex[i] = arg;
        }
     }
    
    /**
     *    Add the argument to the container and get an index to the
     *    argument. Not supplied argument now is the belongings of
     *    the TileArgContainer.
     *
     *    @param   arg   The TileArg to add to the TileArgContainer.
     *                   NB! The arg is after this owned by the
     *                   TileArgContainer which may delete immediately
     *                   after.
     *    @return  The index to the TileFeatureArg. This should
     *             afterwards be used instead of the argument passed
     *             as parameter.
     */
    public int addArg( TileFeatureArg arg ){
        return 0;
        
    }
    
    /**
     *    Get the argument at the specified index.
     *    @param   index The index.
     *    @return  The argument.
     */
    public TileFeatureArg getArg(int index){
           if(index < argByIndex.length){
                return (TileFeatureArg)argByIndex[index];
           }else { 
              return null;  
           }
    }
    
    /**
     *    Get the index for the specified TileArg.
     *    @param  arg The TileArg.
     *    @return  The index of the TileArg if found in the container,
     *             otherwise -1.
     */
    public int getArgIndex( TileFeatureArg arg ){
            return 0;
    }
}


