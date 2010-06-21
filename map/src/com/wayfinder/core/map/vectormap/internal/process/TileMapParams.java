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
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;

public class TileMapParams {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapParams.class);
    
    private String paramString;
    
    private int importanceNbr=0;
    private int detailLevel;
    private int tileIndexLat;
    private int tileIndexLon;
    private int iTileMapType; // 1==Map, 0==Strings
    private int layerID = 0;
    private int serverPrefix;
    private String iTileID;
    private long iTimeStamp=0;

    /**
     * language type from com.wayfinder.map.vectormap.process.LangTypes
     */
    private int langType;
    private boolean gzip;
    private RouteID routeID;
    private static String sortedCodeChars   = "!()*+-<>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^abcdefghijklmnopqrstuvwxyz";
    
    public static final int MAP = 1;
    public static final int STRINGS = 0;
    private boolean iOverviewMap = false;
    
    
    public TileMapParams(){
        this("","");
    }
    public TileMapParams(String params,String aTileID){
        this.paramString = params;
        iTileID = aTileID;
    }
    
    public int getServerPrefix() {
        return serverPrefix;
    }
    
    public void updateTimeStamp() {
        iTimeStamp = System.currentTimeMillis();
    }
    
    //findbugs: renamed
    public long getTimestamp() {
        return iTimeStamp;
    }
    
    /**
     *  Sets the params.
     *  @param serverPrefix Prefix given by server when requesting
     *                      parameter block. Only six bits are sent.
     *  @param gzip         True if gzip may be used.
     *  @param layer        The layer number.
     *  @param mapOrStrings
     */
    public void setParams( int serverPrefix, boolean gzip, int layer,int mapOrStrings, int importanceNbr,int langType,
            int tileIndexLat,int tileIndexLon, int detailLevel,RouteID routeID, String aTileID){
        
        paramString = "";
        this.serverPrefix = serverPrefix;
        this.gzip = gzip;
        this.layerID = layer;
        this.iTileMapType = mapOrStrings;
        this.importanceNbr = importanceNbr;
        this.langType = langType;
        this.tileIndexLat = tileIndexLat;
        this.tileIndexLon = tileIndexLon;
        this.detailLevel = detailLevel;
        this.routeID = routeID;
        iTileID = aTileID;
        iOverviewMap = false;
        /*

        System.out.println("Params set to:");
        System.out.println("Prefix: "+serverPrefix);
        System.out.println("GZIP: "+gzip);
        System.out.println("Layer: "+layer);
        System.out.println("mapOrStrings: "+mapOrStrings);
        System.out.println("importance: "+importanceNbr);
        System.out.println("langType: "+langType);
        System.out.println("tileIndexLat: "+tileIndexLat);
        System.out.println("tileIndexLon: "+tileIndexLon);
        System.out.println("detailLevel: "+detailLevel);
        System.out.println("routeID: "+routeID.getID());
*/
    }
    
    public void setParams( int serverPrefix, boolean gzip, int layer,int mapOrStrings, int importanceNbr,int langType,
            int tileIndexLat,int tileIndexLon, int detailLevel,RouteID routeID, String aTileID, boolean aOverviewMap) {
        
        setParams(serverPrefix,gzip,layer,mapOrStrings,importanceNbr,langType,
                    tileIndexLat,tileIndexLon,detailLevel,routeID,aTileID);
        iOverviewMap = aOverviewMap;
    }
    
    /**
     * Returns the id for the tile, i.e. importance 0 for the layer. 
     */
    public String getTileID() {
        return iTileID;
    }
    
    public void setTileID(String aTileID) {
        iTileID = aTileID;
    }
    
    public boolean isOverviewMap() {
        return iOverviewMap;
    }
    
    /**
     *   Returns the string representation of the TileMapDesc.
     */
    public final String getAsString(){
        if(paramString.length() == 0) {         
            BitBuffer buf = new BitBuffer(64);          
            updateParamString(buf);
        }
        return paramString;
    }
    
    /**
     * 
     * @param buf
     * @return the string representation of the TileMap.
     */
    public final String getAsString(BitBuffer buf) {
        if ( paramString.equals("") ) {
            updateParamString(buf);
        }
        return paramString;
    }
    
    public final String getAsString(BitBuffer buf, int imp, int type) {
        importanceNbr = imp;
        iTileMapType = type;
        updateParamString(buf);
        return paramString;
    }
    
    private void updateParamString(BitBuffer buf){
        
        // Low bits first.
        buf.writeNextBits(importanceNbr & 0xf, 4);
        buf.writeNextBits(detailLevel, 4);
        
        int nbrBits = 15;
        if ( detailLevel > 0 ) {
            nbrBits = Utils.getNbrBitsSignedGeneric(tileIndexLat);
            int tmpNbrBits = Utils.getNbrBitsSignedGeneric(tileIndexLon);
            nbrBits = Math.max(nbrBits, tmpNbrBits);
            buf.writeNextBits( nbrBits, 4 );
        }
        if ( nbrBits > 8 ) {
            // Write lower bits first since they seem to differ more often.
            buf.writeNextBits(tileIndexLat & 0xff, 8);
            buf.writeNextBits(tileIndexLon & 0xff, 8);
            // Then some higher bits.
            buf.writeNextBits(tileIndexLat >> 8, nbrBits - 8);
            buf.writeNextBits(tileIndexLon >> 8, nbrBits - 8);
        } else {
            // Write all at once.
            buf.writeNextBits(tileIndexLat, nbrBits);
            buf.writeNextBits(tileIndexLon, nbrBits);
        }
        
        buf.writeNextBits(layerID, 4);
        
        // Lowest bits of server prefix.
        buf.writeNextBits(serverPrefix & 0x1f, 5);
        
        if ( iTileMapType == MAP) {
            // The features do not have any language.
        } else {
            // For strings, the language is important.
            // Lowest bits first.
            buf.writeNextBits(langType & 0x7, 3);
            // Then highest bits.
            buf.writeNextBits(langType >> 3, 3 );
        }
        
        // Highest bit of server prefix.
        // Nowdays highest bit of server prefix means special quirk mode.
        // 0x0 means old mode.
        // 0x1 means that an additional 7 bits are used for highest 
        //     bits for language type.
        
        int quirkyMode = 0;
        if ( langType >= 64 ) {
           // Language is too big to fit in old 6 bits.
           quirkyMode = 0x1;
        }
        
        buf.writeNextBits(quirkyMode, 1);
        
        if(quirkyMode == 0x1) {
            // Add additional 7 highest bits for language.
            buf.writeNextBits((langType >> 6), 7 );
        }
        
        // Highest bits of importance nbr.
        buf.writeNextBits(importanceNbr >> 4, 1);
        // Inverted gzip
        if(gzip)
            buf.writeNextBit(false);
        else
            buf.writeNextBit(true);
        
        if ( layerID == 1 ) { //ROUTELAYER
            if ( routeID != null ) {
                routeID.save(buf);
            }
        }
        
        // Char-encode.
        int nbrChars = (buf.getCurrentBitOffset() + 5) / 6;
        
        // The length of the buffer cannot be larger than 64. I know it.
        // Add space for FeatureType 'G'/'T'
        StringBuffer tmpString = new StringBuffer(nbrChars+1);
        
        if ( iTileMapType == MAP ) { //DATA
            tmpString.append('G'); // We use G for features now. G - Geometry :)
        } else {
            tmpString.append('T'); // T as in sTring map. errm.. No no, T - Text
        }
        buf.reset();
        for( int i = 0; i < nbrChars; i++ ) {
            int bits = buf.nextBits(6);
            tmpString.append(sortedCodeChars.charAt( bits & 0xFF  ));
        }
        int end = tmpString.length()-1; 
        while(tmpString.charAt(end)=='+'){
             tmpString.deleteCharAt(end);
             end--;
        }
        
        paramString = tmpString.toString();
    }
    
    public int getTileMapType(){
        return iTileMapType;
    }
    
    public int getImportance() {
        return importanceNbr;
    }
    
    public int getLayerID(){
        return layerID;
    }
    public void setLayerID(int id){
        layerID = id;
    }
    
    public String getRouteID() {
        if(routeID == null)
            return null;
        
        return routeID.getRouteIDAsString();
    }
    
    public int getDetailLevel(){
        return detailLevel;
    }
    public int getTileIndexLat(){
        return tileIndexLat;
    }
    public int getTileIndexLon(){
        return tileIndexLon;
    }
    
    public int getLanguageType(){
        return langType;
    }

    /**
     * @param langType language type from
     * wmmg.map.vectormap.wayfinder.LangTypes
     */
    public void setLanguageType(int langType){
        this.langType = langType;
    }
    
    
    public boolean useGZip(){
        return gzip;
    }
    
    public TileMapParams cloneTileMapParams() {
        TileMapParams p = new TileMapParams();
        p.setParams(serverPrefix, 
                    gzip, 
                    layerID, 
                    iTileMapType, 
                    importanceNbr, 
                    langType, 
                    tileIndexLat, 
                    tileIndexLon, 
                    detailLevel, 
                    routeID, 
                    iTileID);
        return p;
    }  
    
    /**
     * Performs a sanity check of the paramstring contained in the TileMapParams 
     * object passed to the method
     * 
     * @param aParamString A paramstring to check
     * @throws IllegalArgumentException if the string is incorrect
     */
    public static void assertParamStringCorrect(TileMapParams aParams) 
    throws IllegalArgumentException {
        assertParamStringCorrect(aParams.getAsString());
    }
    
    
    /**
     * Performs a sanity check of the paramstring passed to the method
     * 
     * @param aParamString A paramstring to check
     * @throws IllegalArgumentException if the string is incorrect
     */
    public static void assertParamStringCorrect(String aParamString)
    throws IllegalArgumentException {    
        try {
            assertParamStringInternal(aParamString);
        } catch(IllegalArgumentException iae) {
            throw iae;
        } catch(RuntimeException re) {
            // convert to IllegalArgumentException
            throw createAssertEx(aParamString, "String could not be unpacked: " + re.getClass().getName());
        }
    }
    
    
    /**
     * internal method - should only be run when debugging is active since it
     * may incur a significate performance hit
     * <p>
     * Will unpack the supplied paramstring, recreate a TileMapParams object, 
     * extract the paramstring from it and compare the new string to the
     * supplied string.
     * <p>
     * If the strings differ, something has gone wrong when packing the string
     * and the assertion will fail.
     * <p>
     * If the paramstring is valid, this method will return normally
     * 
     * @param aParamString The paramstring to check
     * @throws IllegalArgumentException If the supplied paramstring is invalid
     * @throws IndexOutOfBoundsException If the paramstring was too short to
     * be unpacked
     */
    private static void assertParamStringInternal(String aParamString) 
    throws IllegalArgumentException {
        
        // *** First check that we can unpack this string ***  //
        
        if(!TileMapParamTypes.hasValidParamType(aParamString)) {
            throw createAssertEx(aParamString, "This paramstring has an illegal paramtype");
        } else if(!TileMapParamTypes.isMap(aParamString)) {
            // probably fully valid, but cannot be handled by this method
            // accept it
            return;
        }

        // *** Start by unpacking the paramstring into a bitbuffer ***  //
        
        // text or geometry?
        final char firstChar = aParamString.charAt(0);
        final int paramGeoOrText;
        if(firstChar == 'G') {
            paramGeoOrText = MAP;
        } else if(firstChar == 'T') {
            paramGeoOrText = STRINGS;
        } else {
            throw createAssertEx(aParamString, "This paramstring is neither strings or geometry");
        }
        
        // convert to bits
        final int strLength = aParamString.length();
        BitBuffer buf = new BitBuffer(strLength * 6);
        // reconsitute into bits, skip the first letter when doing so
        for (int i = 1; i < strLength; ++i) {
            char ch = aParamString.charAt(i);
            int index = sortedCodeChars.indexOf(ch);
            if(index < 0) {
                throw createAssertEx(aParamString, "Paramstring contains illegal character");
            }
            buf.writeNextBits(index & 0xff, 6);
        }
        
        buf.reset();
        
        // *** Read the values from the bitbuffer ***  //
        
        // retrieve values
        // importance and detail
        final int tmpLowImportance = buf.nextBits(4);
        final int paramDetailLvl  = buf.nextBits(4);
        
        // tile index
        final int nbrLatLonIndexBits;
        if(paramDetailLvl > 0) {
            nbrLatLonIndexBits = buf.nextBits(4);
        } else {
            nbrLatLonIndexBits = 15;
        }

        final int paramTileIndexLat;
        final int paramTileIndexLon;
        if ( nbrLatLonIndexBits > 8 ) {

            // Read the lower bits first.
            int lowLat = (buf.nextBits(8) & 0xff);
            int lowLon = (buf.nextBits(8) & 0xff);
            
            // then the higher
            paramTileIndexLat = (buf.nextSignedBits(nbrLatLonIndexBits - 8) << 8) | lowLat;
            paramTileIndexLon = (buf.nextSignedBits(nbrLatLonIndexBits - 8) << 8) | lowLon;
        } else {
            paramTileIndexLat = buf.nextSignedBits(nbrLatLonIndexBits);
            paramTileIndexLon = buf.nextSignedBits(nbrLatLonIndexBits);
        }

        // layer
        final int paramLayer = buf.nextBits(4);
        final int paramSrvPrefix = buf.nextBits(5);
        
        // read language
        // obs - can be updated later due to quirkymode :O
        int paramLangType;
        if(paramGeoOrText == MAP) {
            // The geometry does not have a language. Set to swedish.
            paramLangType = LangTypes.SWEDISH;
        } else if(paramGeoOrText == STRINGS){
            paramLangType = buf.nextBits(3) | (buf.nextBits(3) << 3 );
        } else {
            throw createAssertEx(aParamString, "Could not determine langType for this paramstring");
        }

        // check quirky mode
        if(buf.nextBits(1) == 0x1) {
            // quirky the clown! :D, update lang
            paramLangType |= (buf.nextBits(7) << 6);
        }
        
        // High bits of importance nbr (only 1 bit).
        final int paramImportance = buf.nextBit() << 4 | tmpLowImportance;
        
        // Read inverted gzip.
        final boolean paramGzip = (buf.nextBit() != 1); // reversed
        
        final RouteID paramRID;
        if(paramLayer == 1) {
            paramRID = new RouteID(buf.nextBits(32), buf.nextBits(32));
        } else {
            paramRID = null;
        }
        
        // *** Recreate a paramstring from the read values ***  //
        
        TileMapParams repackedParams = new TileMapParams();
        repackedParams.setParams(paramSrvPrefix, paramGzip, paramLayer,
                paramGeoOrText, paramImportance, paramLangType,
                paramTileIndexLat, paramTileIndexLon, paramDetailLvl,
                paramRID, null);
        final String repackedParamStr = repackedParams.getAsString();
        
        // *** If strings match - awsum thx! If not - pewpew exception ***  //
        if(!aParamString.equals(repackedParamStr)) {
            // paramstring is not valid - throw exception
            throw createAssertEx(aParamString, "repacked paramstring became: " + repackedParamStr);
        }
        if(LOG.isTrace()) {
            LOG.trace("TileMapParams.assertParamStringInternal()", "ParamStr " + aParamString + " OK!");
        }
    }
    
    
    private static IllegalArgumentException createAssertEx(String aParamString, String aReason) {
        // text below should NOT be within "//#mdebug" switches - it's intended to always be included!
        // again - do NOT add "//#mdebug" here...
        LOG.error("TileMapParams.createAssertEx()",
                "*********************************************************************");
        LOG.error("TileMapParams.createAssertEx()", " - OH NOES!! :O");
        LOG.error("TileMapParams.createAssertEx()", " - A bad paramstring has been generated!");
        LOG.error("TileMapParams.createAssertEx()", " - Please notify the seniors of this error!");
        LOG.error("TileMapParams.createAssertEx()", " - ParamString was: " + aParamString);
        LOG.error("TileMapParams.createAssertEx()", " - Reason was: " + aReason);
        LOG.error("TileMapParams.createAssertEx()", 
                "*********************************************************************");
        return new IllegalArgumentException(aParamString + " is incorrect: " + aReason);
        // if you are adding a "//#enddebug" here, you are doing it wrong
    }
    
    
    
    
    
//    public static int[] paramstringToIntArray(String paramstring) {
//        int len = paramstring.length();
//        int[] i = new int[((len-1)/5)+1];        
//        for(int j=0; j<len; j++) {
//            byte val = (byte)(paramstring.charAt(j)-59);
//            if(val<38) {
//                val+=6;
//                if(val<12) {
//                    val+=7;
//                    if(val==-1) {
//                        val=1;
//                    } else if(val==-3)
//                        val=0;
//                }
//            }
//            i[j/5]|=(val<<((j%5)*6));
//        }
//        return i;
//    }
//    
//    public static String intArrayToParamstring(int[] i) {
//        int len = i.length, j5 = 0, ij = 0;
//        char[] c = new char[len*5];
//        for(int j=0; j<len; j++) {
//             j5 = j*5;
//             ij = i[j];
//             c[j5]     = codeChars[ ij       &0x3F];
//             c[j5+1]   = codeChars[(ij>>6)   &0x3F];
//             c[j5+2]   = codeChars[(ij>>12)  &0x3F];
//             c[j5+3]   = codeChars[(ij>>18)  &0x3F];
//             c[j5+4]   = codeChars[(ij>>24)  &0x3F];
//        }
//        return (new String(c)).replace('+',' ').trim().replace(' ','+');
//    }
}
