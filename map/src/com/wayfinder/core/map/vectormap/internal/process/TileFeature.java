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

public class TileFeature{

    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileFeature.class);
    
    /* Feature type */
    private int type;   // street, park, restaurant...
    
    private TileFeatureArg[] internalTileFeatureArgs = null;
    private TileFeatureArg[] tileFeatureArgs = null;
    // Bitmap primitive.
    public static final int BITMAP = -3;
    // Builtin primitive for polygons (filled)
    public static final int POLYGON = -2;
    // Builtin primitive for lines
    public static final int LINE = -1;
    
    private TileMap iTileMap;
    
    public TileFeature(TileMap aTileMap) {
        iTileMap = aTileMap;
    }
    
    public TileFeature(int type){
        this.type = type;
    }
    
    void setArgs(TileFeatureArg[] args){
        tileFeatureArgs = args;
    }
    
    TileFeatureArg[] getArgs(){
        return tileFeatureArgs;
    }
        
    public TileMap getTileMap() {
        return iTileMap;
    }
    
    public TileFeatureArg getArg( int name ){
        for(int i =0; i<internalTileFeatureArgs.length;i++){
            TileFeatureArg tfa = internalTileFeatureArgs[i];
            if ( tfa.getName() == name ) {
                return tfa;
            }
        }
        return null;
    }
   
    /**
     * @return the feature type
     */
    public int getType(){
        return type;
    }
    
    public boolean createFromStream(BitBuffer buf, TileMapFormatDesc desc,
        TileMap tileMap, TileFeature prevFeature ){
    
        // Create TileFeature of correct dynamic type.
        boolean sameAsPrevious = buf.nextBits(1) != 0;
        int type;
        if ( sameAsPrevious ) {
            // Same as previous.
            type = prevFeature.getType();
        } else {
            // Not same as previous.
            // Read the actual type.
            type = buf.nextSignedBits(8);  //nextByte();
        }

        this.type = type;
        TileFeatureArg[] args = desc.getArgsForFeatureType(type);
                
        if (args == null) {
            if(LOG.isError()) {
                LOG.error("TileFeature.createFromStream()", "Could not find args for feature of type "+type);
            }
            return false;
        }
        
        tileFeatureArgs = new TileFeatureArg[args.length];        
        internalTileFeatureArgs = args;
        
        // Load the feature with internalLoad.
        // Supply the previous feature if it is of the same type.
        if(!internalLoad( type,buf, tileMap, sameAsPrevious ? prevFeature : null  )){
            return false;
        }
        
        return true;
    }
    
    private boolean internalLoad( int type,BitBuffer buf, TileMap tileMap, TileFeature prevFeature ){
        
        if ( prevFeature != null ) {
           // Use previous arguments
           TileFeatureArg[] prevTileFeatureArgs = prevFeature.getArgs();
            
            for(int i=0;i<internalTileFeatureArgs.length;i++){
                TileFeatureArg tfa = prevTileFeatureArgs[i];
                TileFeatureArg arg = internalTileFeatureArgs[i];
                arg.load(type,buf,this,tfa,true);
            }
        } else {
            // Don't use previous arguments.
            for(int i=0;i<internalTileFeatureArgs.length;i++){
                TileFeatureArg tfa  = internalTileFeatureArgs[i];
                tfa.load(type,buf,this,null,true);
            }
        }
        return true;
    }
    
    int cnt = 0;
    /**
     * Add argument to the tile feature.
     *  
     * The argument can be CoordArg, CoordsArg, StringArg and/or SimpleArg
     *  
     * @param arg
     */
    void addArgs(TileFeatureArg arg) {
        tileFeatureArgs[cnt] = arg;
        cnt++;
    }
    
}
