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
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

public abstract class TileFeatureArg {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileFeatureArg.class);

    public static final int SIMPLEARG = 0;
    public static final int COORDARG  = 1;
    public static final int COORDSARG = 2;
    public static final int STRINGARG = 3;
    private int m_name; 
    private int m_type; 
    
    public TileFeatureArg(int type, int name) {
        m_type = type;
        m_name = name;
    }
    
    public static TileFeatureArg loadFullArg(BitBuffer bitBuffer) {
        int type = bitBuffer.nextBits(3);
        int name = bitBuffer.nextBits(8);
        TileFeatureArg arg =null;
        
        switch ( type ) {
            case SIMPLEARG:
                int size = bitBuffer.nextBits( 5 );
                arg = new SimpleArg( type, name, size );
                break;
            case COORDARG:
                arg = new CoordArg(type, name );
                break;
            case COORDSARG:
                // Cannot create default coords arg, since they need a map.
                arg = new CoordsArg(type);
                break;
            case STRINGARG:
                arg = new StringArg(type, name );
                break;
            default:
                if(LOG.isError()) {
                    LOG.error("TileFeatureArg.loadFullArg()", "invalid type: "+type);
                }
                return null;
        }
        TileMap tmpTileMap2 = new TileMap();
        TileFeature tmpTileMap = new TileFeature(tmpTileMap2);
                
        if(!arg.load(type, bitBuffer, tmpTileMap, null, false )){
            if(LOG.isError()) {
                LOG.error("TileFeatureArg.loadFullArg()", "Unable to load type: "+type);
            }
        }
        
        return arg;
    }
    
    public int getType(){
        return m_type;
    }
    
    public int getName(){
        return m_name;
    }
    
    public abstract boolean load(int type, BitBuffer b, TileFeature tm, TileFeatureArg tfa, boolean save);
    
}
