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

public class TileMapLayerInfo {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapLayerInfo.class);
    
    int id;
    String name;   
    int updatePeriod;   
    boolean trans;   
    boolean isOptional;   
    int serverOverride;   
    boolean presentInTileMapFormatDesc;   
    boolean visible;   
    boolean alwaysFetchStrings;
    boolean affectedByACPMode;
    boolean fetchLayerWhenACPEnabled;
    boolean fetchLayerWhenACPDisabled;
    //used for setting view to turn on/off minimize CAP download, if it's true, then don't download CAPs.
    boolean ACPDownloadEnabled;
    
    /** Creates a new instance of TileMapLayerInfo */
    public TileMapLayerInfo() {
        id = Integer.MAX_VALUE;
        updatePeriod = 0;
        isOptional = false;
        trans = false;
        alwaysFetchStrings = true;
        visible = true;
        presentInTileMapFormatDesc = true; 
        serverOverride = 0;
        affectedByACPMode = false;
        fetchLayerWhenACPEnabled = false;
        fetchLayerWhenACPDisabled = false;
        ACPDownloadEnabled = true;
        name = null;
    }
    
    public void setIDAndName( int id, String name) {
        this.name = name;
        this.id = id;
    }
    
    /**
     * @return the name of the layer
     */
    public String getName() {
        return name;
    }
    
    public void setVisible( boolean visible ) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        if(this.affectedByACPMode) {        
            //If user decides to download CAPs, then it decides by the server to send or not
            if(this.ACPDownloadEnabled){
                //System.out.println("ACPDownloadEnabled for layer" + id + ", fetchLayerWhenACPEnabled = " + fetchLayerWhenACPEnabled);
                return this.fetchLayerWhenACPEnabled;
                }
            else{
                //System.out.println("ACPDownloadDisabled for layer" + id + ", fetchLayerWhenACPDisabled = " + fetchLayerWhenACPDisabled);
                return this.fetchLayerWhenACPDisabled; 
            }

        } else {
            return visible;
        }
    }    
    
    /* not really needed as server decides everything, shouldn't be changed
    public void setAffectedByACPMode(boolean affected){
        this.affectedByACPMode = affected;
    }

    public void setFetchLayerWhenACPEnabled(boolean fetchLayerWhenACPEnabled){
        this.ACPDownloadEnabled = fetchLayerWhenACPEnabled;
        //this.fetchLayerWhenACPEnabled = fetchLayerWhenACPEnabled;
    }
    
    public void setFetchLayerWhenACPDisabled(boolean fetchLayerWhenACPDisabled){
        this.fetchLayerWhenACPDisabled = fetchLayerWhenACPDisabled;
    }
    */
    
    public boolean isFetchLayerWhenACPDisabled(){
        return fetchLayerWhenACPDisabled;
    }
    
    public boolean isFetchLayerWhenACPEnabled(){
        return fetchLayerWhenACPEnabled;
    }
    
    public boolean isAffectedByACPMode(){
        return affectedByACPMode;
    }
    
    public void setACPDownloadEnabled(boolean enabled){
        this.ACPDownloadEnabled = enabled;
    }
    
    public boolean isACPDownloadEnabled(){
        return this.ACPDownloadEnabled;
    }
    
    public void setUpdatePeriodMinutes( int period ) {
        this.updatePeriod = period;
    }
    
    /* minutes*/
    public int getUpdateTime(){
        return updatePeriod;
    }

    public void setOptional( boolean optional ) {
        this.isOptional = optional; 
    }
    
    public boolean getOptional() {
        return this.isOptional;
    }

    public void setServerOverrideNumber( int nbr ) {
        this.serverOverride = nbr;
    }
    
    public int getServerOverrideNumber() {
        return this.serverOverride;
    }

    public void setPresent( boolean present ) {
        this.presentInTileMapFormatDesc = present;
    }
    
    public boolean getPresent() {
        return this.presentInTileMapFormatDesc;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean getAlwaysFetchStrings() {
        return alwaysFetchStrings;
    }

    public int getSizeInDataBuffer() {
        return 4 + 4 + name.length() + 1 + 4 + 1 + 1 + 1 + 2;
    }
    
    public boolean getTrans() {
        return this.trans;
    }

    public void load( BitBuffer buf ) {
        buf.alignToByte();
        int nbrBytes = (int)buf.nextInt();
        int totalBytesRead = 0;
        this.id = (int)buf.nextInt();
        totalBytesRead+=4;
        this.name = buf.nextString();
        totalBytesRead+=(name.length()+1);
        this.updatePeriod = (int)buf.nextInt();
        totalBytesRead+=4;
        this.trans = (buf.nextBit()==1);
        this.isOptional = (buf.nextBit()==1);
        this.serverOverride = buf.nextBits(8);
        this.presentInTileMapFormatDesc = (buf.nextBit()==1);
        this.visible = (buf.nextBit()==1);
        this.alwaysFetchStrings = (buf.nextBit()==1);
        buf.alignToByte();
        totalBytesRead+=2;
        
        //Loading Access Control Poi setting related info from buffer
        if ( nbrBytes-totalBytesRead >= 1 ) {
            if(LOG.isDebug()) {
                LOG.debug("TileMapLayerInfo.load()", "Loading acp settings from buffer for layer " + id);
            }
            
            affectedByACPMode = (buf.nextBit() == 1);
            fetchLayerWhenACPEnabled = (buf.nextBit() == 1);
            fetchLayerWhenACPDisabled = (buf.nextBit() == 1);           
            buf.alignToByte();
            totalBytesRead++;
         } 
        else {
            if(LOG.isDebug()) {
                LOG.debug("TileMapLayerInfo.load()", "Using hardcoded acp settings.");
            }
           
            // Default setting is to ignore the ACP mode.
            affectedByACPMode = false;
            fetchLayerWhenACPEnabled = false;
            fetchLayerWhenACPDisabled = false;
         }
                         

        buf.skip( nbrBytes-totalBytesRead );
        
        if(LOG.isDebug()) {
            LOG.debug("TileMapLayerInfo.load()", "affectedByACPMode = " + affectedByACPMode);
            LOG.debug("TileMapLayerInfo.load()", "fetchLayerWhenACPEnabled = " + fetchLayerWhenACPEnabled);
            LOG.debug("TileMapLayerInfo.load()", "fetchLayerWhenACPDisabled = " + fetchLayerWhenACPDisabled);
        }
        
    }
}
