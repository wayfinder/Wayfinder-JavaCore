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
/*
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.map.vectormap.internal.process;

import com.wayfinder.core.map.CopyrightHolder;
import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;


public class TileMapFormatDesc {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapFormatDesc.class);
    
    private TileImportanceTable[]   importanceTables = null;
    private TileCategory[]          categories = null;
    private int[]                   poiTypeByCategory = null;          
    private TileFeatureArg[][]      argsByTileFeatureTypeArray = null;
    private TileArgContainer        defaultArgs;
    private TileFeature[][]         primMap = null;
    private TileScale[][]           tileScaleByLayer = null;
    private TileMapLayerInfo[]      layerIDsAndDesc = null;
    private int[]                   layerIDByNbr = null;
    private long                    backgroundColor;
    private int                     reserveDetailLevel;
    private int                     extraTilesForReserve;
    private long                    crc;
    private int                     serverPrefix;
    private short                   nbrOfLayers;
    private long                    iTextColor;
    private CopyrightHolder         copyrightHolder;
    private String                  staticCopyrightString;
    
    /**
     * Scale values per level. Use this table to lookup scale levels
     * for each default argument. Each default argument index points into this
     * table, which holds the scale value that the argument is valid for.
     */
    protected int[] m_scaleLevelsTable;
    
    public TileMapFormatDesc() {
        defaultArgs = new TileArgContainer();
        serverPrefix = 0xFFFF;
        nbrOfLayers = -1;
        copyrightHolder = null;
        staticCopyrightString = "";
    }
    
    public static String createParamString( int langType, String clientTypeString,String randChars,boolean preCacheTMFD ){
        StringBuffer tmpDesc = new StringBuffer(1024);
        String langString = LangTypes.getTMFDLanguageStr(langType);
        char lastMinus = ' ';
        if(!randChars.equals("")){
            lastMinus = '-';
        }
        if(!preCacheTMFD){
            tmpDesc.append("DX-"+langString+"-"+clientTypeString+lastMinus+randChars);
        }else{
            tmpDesc.append("DYYY");
        }
        
        
        return tmpDesc.toString().trim();
    }
    
    /**
     * Create the param string for the TMFD-Nightmode
     *
     * @param langType
     * @param clientTypeString
     * @param randChars
     * @return
     */
    public static String createParamStringNight( int langType, String clientTypeString,String randChars, boolean preCacheTMFD ){
        StringBuffer tmpDesc = new StringBuffer(1024);
        String langString = LangTypes.getTMFDLanguageStr(langType);
        char lastMinus = ' ';
        if(!randChars.equals("")){
            lastMinus = '-';
        }
        if(!preCacheTMFD)
            tmpDesc.append("dX-"+langString+"-"+clientTypeString+lastMinus+randChars);
        else
            tmpDesc.append("dYYY");
        
        return tmpDesc.toString().trim();
    }
    
    public boolean load(BitBuffer bitBuffer, TileMapFormatDesc previousDesc){

        bitBuffer.alignToByte();
        serverPrefix = Integer.parseInt(bitBuffer.nextString());
        nbrOfLayers = bitBuffer.nextByte();
        
        if(LOG.isInfo()) {
            LOG.info("TileMapFormatDesc.load()", "number of layers "+ nbrOfLayers);
        }
        
        tileScaleByLayer = new TileScale[nbrOfLayers][];
        layerIDByNbr = new int[nbrOfLayers];
        importanceTables = new TileImportanceTable[nbrOfLayers];
        
        for (int layerNbrCnt = 0; layerNbrCnt < nbrOfLayers; layerNbrCnt++){
            char meters = bitBuffer.nextShort();
            char pixels = bitBuffer.nextShort();
            char dpi =    bitBuffer.nextShort();
            
            float zoomFactor =         bitBuffer.nextInt()/1000.0f;
            float exchangeTileFactor = bitBuffer.nextInt()/1000.0f;
            short detailLevels =       bitBuffer.nextByte();
            
            // Initialize the tiles.
            initTileSizesForLayer( layerNbrCnt,
                meters,
                pixels,
                dpi,
                zoomFactor,
                exchangeTileFactor,
                detailLevels );
            
            short layerID = bitBuffer.nextByte();
            bitBuffer.nextString();

            layerIDByNbr[layerNbrCnt] = layerID;            
            // Create and load the importance table.
            importanceTables[layerNbrCnt] = new TileImportanceTable();
            importanceTables[layerNbrCnt].load( bitBuffer );
            
        }
        char size = bitBuffer.nextShort();
        
        argsByTileFeatureTypeArray = new TileFeatureArg[size][];
        for ( int i = 0; i < size; i++ ) {
            // Key.
            bitBuffer.nextShort();
            
            // Value. vector<TileFeatureArg*>.
            // Size of vector.
            short vecSize = bitBuffer.nextByte();
            
            argsByTileFeatureTypeArray[i] = new TileFeatureArg[vecSize];
            for ( int j = 0; j < vecSize; j++ ) {
                argsByTileFeatureTypeArray[i][j] = TileFeatureArg.loadFullArg( bitBuffer );   //bara koordinater
            }
            
            bitBuffer.alignToByte();
        }
        
        // Load scaleIndexByLevel.
        short nbrScaleIndex = bitBuffer.nextByte();
        m_scaleLevelsTable = new int[nbrScaleIndex];
        for (int scaleIndex = 0; scaleIndex < nbrScaleIndex; ++scaleIndex ) {
            m_scaleLevelsTable[scaleIndex] = (int)bitBuffer.nextInt();
        }

        // Load the default arguments for the primitives.
        defaultArgs.load( bitBuffer );
        bitBuffer.alignToByte();
        
        // Load m_primitiveDefaultMap.
        // m_primitiveDefaultMap contains the default arguments for the
        // primitives, keyed by the (complex) feature type.
        
        // Size of the map.
        size = bitBuffer.nextShort();
        
        // Temporary map needed since all the vectors aren't sent.
        primMap = new TileFeature[size][];
        for (int i = 0; i < size; ++i ) {
            // Key. TileFeature type (complex)
            char type = bitBuffer.nextShort();
            
            //System.out.println("prim map type: "+(int)type);
            // Value.
            // vector<TileFeature*>
            
            // Size of vector. XXX: Note the size should always be 1.
            // There's a 1:1 relationship between the TileFeature and
            // TilePrimitiveFeature.
            short featureSize = bitBuffer.nextByte();
            
            TileFeature[] tileFeatures = new TileFeature[featureSize];
            
            // Read the (one and only) TilePrimitiveFeature
            for ( int j = 0; j < featureSize; j++ ) {
                // Feature type for primitive.
                int primType = bitBuffer.nextSignedBits(16);
                
                TileFeature tileFeature = new TileFeature(primType);  //Writable
                short argSize = bitBuffer.nextByte();
                TileFeatureArg[] args = new TileFeatureArg[argSize];
                
                for ( int k = 0; k < argSize; k++ ) {
                    char c = bitBuffer.nextShort();
                    
                    TileFeatureArg arg = defaultArgs.getArg(c);
                    if(arg == null){
                        return false;
                    }
                    args[k] = arg;
                }
                tileFeature.setArgs(args);
                tileFeatures[j] = tileFeature;
                bitBuffer.alignToByte();
            }
            
            primMap[type] = tileFeatures;
        }

        size = bitBuffer.nextShort();
        for ( int i = 0; i < size; i++ ) {
            // Key. Complex feature type.
            bitBuffer.nextShort();
            
            // Size of the multimap.
            char mapSize = bitBuffer.nextShort();
            
            for ( int j = 0; j < mapSize; j++ ) {
                // Key, argument type.
                bitBuffer.nextByte();
                bitBuffer.nextShort();
                // The name of the value arg.
                bitBuffer.nextByte();
            }
        }
        
        // Background color.
        backgroundColor = bitBuffer.nextInt();
        
        // First addition. The detail level for the reserve maps.
        if ( bitBuffer.getNbrBytesLeft() < 8 ) {
            reserveDetailLevel   = 6;
            extraTilesForReserve = 3;
            return true;
        } else {
            reserveDetailLevel   = (int)bitBuffer.nextInt();
            extraTilesForReserve = (int)bitBuffer.nextInt();
        }
        
        // Second addition. The language and categories.
        // Check for more stuff coming in the buffer.
        if ( bitBuffer.getNbrBytesLeft() < 12 ) {
            return true;
        }
        
        // Language!!!
        bitBuffer.nextInt();
        
        // Categories. Latin-1 to work around bug in S60.
        readCategories( bitBuffer, previousDesc );
        
        // Check if there is room for the CRC.
        // Check if there is a timestamp
        if ( bitBuffer.getNbrBytesLeft() < 8 ) {
            return true;
        }
        crc = bitBuffer.nextInt(); // CRC
        bitBuffer.nextInt(); //timeStamp
        // Check if there is room for new tile settings.
        if ( bitBuffer.getNbrBytesLeft() > 0 ) {
            try{
                for ( int layerNumber = 0; layerNumber < nbrOfLayers; layerNumber++ ) {
                    
                    // Nbr detail levels.
                    short nbrDetails = bitBuffer.nextByte();
                    
                    if(nbrDetails==0) {
                        if(LOG.isError()) {
                            LOG.error("TileMapFormatDesc.load()", "Unknown error in parsing TMFD");
                        }
                        return false;
                    }
                    // System.out.println("layerNbr "+layerNumber);
                    TileScale[] tileScale = new TileScale[nbrDetails];
                    for ( int j = 0; j < nbrDetails; ++j ) {
                        long mc2UnitsForTile = bitBuffer.nextInt();
                        char scale = bitBuffer.nextShort();
                        tileScale[j] = new TileScale(mc2UnitsForTile, scale);
                        //    System.out.println("detail "+j);
                        //  System.out.println("scale "+(int)scale);
                        // System.out.println("mc2unitsperscale "+mc2UnitsForTile);
                    }
                    tileScaleByLayer[layerNumber] = tileScale;
                }
            }catch(Exception e ){
                if(LOG.isError()) {
                    LOG.error("TileMapFormatDesc.load()", e);
                }
            }
        }
        
        // Check if there is room for the copyright string.
        if ( bitBuffer.getNbrBytesLeft() > 0 ) {
            staticCopyrightString = bitBuffer.nextStringUTF() ;
        }
        
        if ( staticCopyrightString.equals("") ) {
            // XXX: Some hardcoded stuff.
            staticCopyrightString = "�2005 Wayfinder, Tele Atlas";
        }
        
        // Check for categories that are sent in utf-8 (old Series 60
        // had problems with the utf-8
        if ( bitBuffer.getNbrBytesLeft() < 8 ) {
            return true;
        }
        if(LOG.isTrace()) {
            LOG.trace("TileMapFormatDesc.load()", "Start to load categories.");
        }
        // Read categories again. This time in utf-8 as God intended.
        readCategories( bitBuffer, previousDesc );
        
        // Check for more layer information.
        if ( bitBuffer.getNbrBytesLeft() < 8 ) {
            return true;
        }
        
        bitBuffer.alignToByte();
        bitBuffer.nextInt(); //version???
        final int length = (int)bitBuffer.nextInt();
        layerIDsAndDesc = new TileMapLayerInfo[length];
        iUpdateTimes = new int[length];
        for(int i=0; i<length; i++) {
            TileMapLayerInfo tmli = new TileMapLayerInfo();
            tmli.load(bitBuffer);
            int layerNbrFromID = getLayerNbrFromID(tmli.getId());
            layerIDsAndDesc[layerNbrFromID] = tmli;
            iUpdateTimes[layerNbrFromID] = tmli.getUpdateTime();
        }
        
        // Load more info about the layers.
        //  layerIDsAndDesc.load( bitBuffer );
        
        // Read the text color and horizon color.
        iTextColor = 0x000000;
        if(bitBuffer.getNbrBytesLeft() < 8)
            return true;
        iTextColor = bitBuffer.nextInt();

        bitBuffer.nextInt(); //iHorizonTopColor
        if(bitBuffer.getNbrBytesLeft() < 4) {
            return true;
        }
        
        bitBuffer.nextInt(); //iHorizonBottomColor
       
        // Check if there is any more to read.
        if ( bitBuffer.getNbrBytesLeft() < 4 ) {
           return true;
        }

        // Load the copyright holder.
        copyrightHolder = new CopyrightHolder();
        copyrightHolder.load( bitBuffer );
        
        return true;        
    }
    
    public void readCategories( BitBuffer bitBuffer, TileMapFormatDesc previousDesc ){
        // Poi types to enable and disable
        long nbrPoiCategories = bitBuffer.nextInt();
        //findbugs: Dead store to local variable
//        long totalNbrPoiTypes = bitBuffer.nextInt();
        bitBuffer.nextInt();
        // Allocate the poi-type-array and the category vector.
        categories = new TileCategory[(int)nbrPoiCategories];

        int poiTypeMaxValue = 0;
        // Read the stuff.
        // int currentPoiTypeIdx = 0;
        for ( int i = 0; i < nbrPoiCategories; i++ ) {
            String categoryName = bitBuffer.nextStringUTF();
          //findbugs: Dead store to local variable
//            int categoryID = bitBuffer.nextShort();
            bitBuffer.nextShort();
            short categoryEnabled    = bitBuffer.nextByte();
            // Read in the poi-types of the category.
            long nbrPoiTypes = bitBuffer.nextInt();
            int[] categoryPoiTypes = new int[(int)nbrPoiTypes];
            
            for ( int j = 0; j < nbrPoiTypes; j++ ) {
                int poiType = (int)bitBuffer.nextInt();
                categoryPoiTypes[j] = poiType;
                if (poiType > poiTypeMaxValue)
                    poiTypeMaxValue = poiType;
            }
            categories[i] = new TileCategory(categoryName, categoryEnabled,(int)nbrPoiTypes,categoryPoiTypes);// firstCat => 0
            //System.out.println("categories[" + i + "]: categoryName = " + categories[i].getName());
        }

        poiTypeByCategory = new int[poiTypeMaxValue + 1];  
        for (int i = 0; i < poiTypeMaxValue + 1; i++)
            poiTypeByCategory[i] = -1;
        for (int i = 0; i < nbrPoiCategories; i++ ){
            TileCategory tg = categories[i];
            for (int j = 0; j < tg.getNbrTypes(); j++){
                poiTypeByCategory[tg.getType(j)] = i;
                if(LOG.isTrace()) {
                    LOG.trace("TileMapFormatDesc.readCategories()", 
                            "PoiType " + tg.getType(j) + " downloaded as category " + i + ", categoryName = " + tg.getName());
                }
            }
        }
 
    }
    
    public int getBackgroundColor(){
        return (int)backgroundColor;
    }
    
    
    private void initTileSizesForLayer(int layerNbr, int meters, int pixels, int dpi,
        float zoomFactor, float exchangeTileFactor, int detailLevels ){
        
        TileScale[] tileScale = new TileScale[detailLevels];
        tileScaleByLayer[layerNbr] = tileScale;
        
        float prevScale = meters / (float)pixels;
        
        int prevMc2UnitsForTile = (int)(meters * Utils.METER_TO_MC2SCALE);
        for ( int i = 0; i < detailLevels; ++i ) {
            // The scale has the unit: map meters / pixel
            float scale = prevScale * zoomFactor;
            float mc2UnitsForTileFloat = (int)(prevMc2UnitsForTile * zoomFactor);
            if ( mc2UnitsForTileFloat < Integer.MIN_VALUE || mc2UnitsForTileFloat > Integer.MAX_VALUE ) {
                // Casting too big / small float to int will result in
                // MIN_INT32 in linux (server). Therefore we must do the same.
                mc2UnitsForTileFloat = Integer.MIN_VALUE;
            }
            
            int mc2UnitsForTile = (int)mc2UnitsForTileFloat ;
            //System.out.println("[TMFD] initTileSizes: detail "+i+", mc2 "+mc2UnitsForTile+" scale "+scale);
            tileScale[i] = new TileScale(mc2UnitsForTile, (int) (prevScale + (scale - prevScale) * exchangeTileFactor));
            prevScale = scale;
            prevMc2UnitsForTile = mc2UnitsForTile;
        }
    }
    
    /**
     *   Returns a new list of arguments for a certain type of feature.
     *   The arguments must be filled from a stream or bytes before
     *   using. These are the arguments that are transferred, i.e. does
     *   not contain the default arguments.
     *   @return   True if the feature type was supported by the tmfd.
     *             False otherwise.
     */
    TileFeatureArg[] getArgsForFeatureType(int type){
        if(type < argsByTileFeatureTypeArray.length ) {
            TileFeatureArg[] currArgs = argsByTileFeatureTypeArray[type];
            return currArgs;
        }
        return null;
    }
    
    public int getCoordAndScaleForTile( int layerID, int detailLevel, int tileLatIdx, int tileLonIdx, long[] coord ){
        int layerNbr = getLayerNbrFromID( layerID );
        TileScale ts = tileScaleByLayer[layerNbr][detailLevel];
        long mc2UnitsPerTile = ts.getMc2UnitsPerTile();
        
        long cx = mc2UnitsPerTile * tileLatIdx;
        long cy = mc2UnitsPerTile * tileLonIdx;
        
        coord[0]= cx;
        coord[1]= cy;
        return getMinScaleForDetailLevel( layerNbr, detailLevel );
    }
    
    public boolean hasLayerID(int id){
        for(int i =0; i<layerIDByNbr.length;i++){
            if(layerIDByNbr[i] == id){
                return true;
            }
        }
        return false;
    }
   
    /**
     * 
     * @see getLayerIDFromLayerNbr
     * @param layerID
     * @return
     */
    public int getLayerNbrFromID( int layerID ){
        final int size = layerIDByNbr.length;
        for(int i = 0;i<size;i++){
            if(layerIDByNbr[i] == layerID)
                return i;
        }
        return -1;
    }
    
    /**
     * Determines the layer ID from layer number.
     * @param layerNbr The layer number to determine the layer ID from.
     * @return Layer ID.
     */
    public int getLayerIDFromLayerNbr( int layerNbr ){
        return layerIDByNbr[layerNbr];
    }
    
    /**
     * 
     * Returns the min scale for at detail level. 
     * 
     * @param layerNbr
     * @param detailLevel
     * @return minimum scale for layer number at detail level.
     */
    public int getMinScaleForDetailLevel(int layerNbr, int detailLevel){
        TileScale ts = tileScaleByLayer[layerNbr][detailLevel];
        return ts.getScale();
    }
    
    /**
     * 
     * Returns the max scale for a detail level
     * 
     * @param layerNbr
     * @param detailLevel
     * @return maximum scale for layer number at detail level.
     */
    public int getMaxScaleForDetailLevel(int layerNbr, int detailLevel){        
        int incDetailLevel = (detailLevel + 1);
        if(tileScaleByLayer[layerNbr].length > incDetailLevel) {
            return (tileScaleByLayer[layerNbr][incDetailLevel].getScale() - 1);
        }       
        return 24000;       
    }
    
    public TileArgContainer getDefaultArgs(){
        return defaultArgs;
    }
    
    TileFeature[][] getPrimitiveDefaultMap(){
        return primMap;
    }
    /**
     * Determines the current detail level from layer number and zoom level.
     * @param layerNbr The layer number.
     * @param zoomLevel zoom level (scale).
     * @return Detail level.
     */
    public int getCurrentDetailLevel(int layerNbr, int zoomLevel) {
        TileScale[] tileScales = tileScaleByLayer[layerNbr];
        for(int i = 0; i < tileScales.length; i++) {
            TileScale ts = tileScales[i];
            int upperLimit = ts.getScale();
            if(zoomLevel<=upperLimit) {
                return Math.max(i - 1,0);
            }
        }
        return tileScales.length - 1;
        
    }
    
    /**
     * 
     * The method return a array with the current tile indices that are visible inside the 
     * current camera bounding box. 
     * 
     * @param cameraBoundingBox
     * @param layerID
     * @param detailLevel
     * @return
     */
    public int[] getTileIndex(int[] cameraBoundingBox, int layerID, int detailLevel) {
        int[] tileIndices = new int[4];     
        getTileIndex(cameraBoundingBox, layerID, detailLevel, tileIndices);
        return tileIndices;
    }
    
    /**
     * 
     * Set the [minLatIdx,maxLatIdx,minLonIdx,maxLonIdx] in the tileIndices array. The tileIndices 
     * are the ones that are visible inside the current camera bounding box. 
     * 
     * @param cameraBoundingBox, the bounding box in MC2
     * @param layerID, the id of the layer (from tmfd)
     * @param detailLevel, the current detail level. 
     * @param tileIndices, must be an array of int[4]
     */
    public void getTileIndex(int[] cameraBoundingBox, int layerID, int detailLevel, int[] tileIndices) {
        
        getTileIndex(getLayerNbrFromID(layerID), detailLevel,
            cameraBoundingBox[2],
            cameraBoundingBox[0], tileIndices);
        
        int a = tileIndices[0];
        int b = tileIndices[1];
        
        getTileIndex(getLayerNbrFromID(layerID), detailLevel,
            cameraBoundingBox[3],
            cameraBoundingBox[1], tileIndices);
        
        tileIndices[3] = tileIndices[1];
        tileIndices[1] = tileIndices[0];        
        tileIndices[0] = a;
        tileIndices[2] = b;       
    }
    
    public long getMc2UnitsPerTile(int aLayerNbr, int aDetailLevel) {
        return tileScaleByLayer[aLayerNbr][aDetailLevel].getMc2UnitsPerTile();
    }
    
    /**
     * 
     * @param layerNbr
     * @param detailLevel
     * @param lat
     * @param lon
     * @param tileIdx, must be an array of int[2]
     */
    public synchronized void getTileIndex( int layerNbr, int detailLevel, int lat, int lon, int[] tileIdx) {
        // Get tile scales for a certain layer
        TileScale ts = tileScaleByLayer[layerNbr][detailLevel];
        
        //Get the mc2Units for the particular detail level (*2 because of pairs in Vector)
        long mc2UnitsPerTile = ts.getMc2UnitsPerTile();
        
        //Create the return values
        tileIdx[1] = (int)(lat / mc2UnitsPerTile);             //Math.floor(lat / mc2UnitsPerTile);
        // Trunc to the nearest lower integer.
        if ( lat < 0) {
            tileIdx[1]--;
        }
        // Trunc to the nearest lower integer.
        tileIdx[0] = (int)(lon / mc2UnitsPerTile);                 //(int)Math.floor(lon / mc2UnitsPerTile);
        if ( lon < 0 ) {
            tileIdx[0]--;
        }        
    }     
    
    public TileImportanceNotice getTileImportanceNotice(int layerID, int detailLevel, int importance) {
        //return importanceTables[getLayerNbrFromID(layerID)].getImportanceNbrSlow(importance,detailLevel);
        return importanceTables[getLayerNbrFromID(layerID)].getImportanceNotice(importance, detailLevel);
    }
    
    public int getNbrImportances(int scale, int detailLevel, int layerID) {
        return importanceTables[getLayerNbrFromID(layerID)].getNbrImportanceNbrs(scale,detailLevel);
    }
    
    public int getServerPrefix(){
        return serverPrefix;
    }
    
    public short getNumberOfLayers(){
        return nbrOfLayers;
    }
    
    public boolean visibleLayer(int layerID){
        return layerIDsAndDesc[getLayerNbrFromID(layerID)].isVisible();
    }
    
    public void setVisibleLayer(int layerID,boolean visible){
        layerIDsAndDesc[getLayerNbrFromID(layerID)].setVisible(visible);
    }
    
    /**
     * Return the POI categories available in the map or null if 
     * no categories are available. 
     * 
     * @return the POI categories available in the map or null if 
     * no categories are available.
     */
    public PoiCategory[] getCategories(){
        PoiCategory []poiCat = null;
        if(categories != null) {
            poiCat = new PoiCategory[categories.length];
            for(int i=0; i<poiCat.length; i++) {
                TileCategory tileCat = categories[i];
                poiCat[i] = new PoiCategory(tileCat.getName(), tileCat.isEnabled());                
            }
        }        
        return poiCat;
    }
    
    public boolean categoryEnabled(int type){
        int category = poiTypeByCategory[type];
        if(category == -1){
            //This will happen for pois that should always be shown and are not allowed to be 
            //turned on/off by the users, e.g. city centres
            /*
            if(LOG.isDebug()) {
                LOG.debug("TileMapFormatDesc.categoryEnabled()", 
                        "poitype " + type + " is not allowed to be turned on/off!");
            }
            */
            return true;
        }
        else {
            return categories[category].isEnabled();
        }
    }
    
    public boolean setCategoryEnabled(int id, boolean visible){
        if(id>=0 && id <categories.length){
            if(visible){
                categories[id].setEnabled(1);
            }else{
                categories[id].setEnabled(0);
            }
            return true;
        }else{
            return false;
        }
    }
    
    
    public short getNumberOfVisibleLayers(){
        short count =0;
        for(int i=0; i<nbrOfLayers;i++){
            if(layerIDsAndDesc[i].isVisible()) {
                count++;
            }
        }
        return count;
    }
    
    private int[] iUpdateTimes = null;
    
    public int getUpdateTimeForLayer(int layerID){
        return iUpdateTimes[getLayerNbrFromID(layerID)];
    }
    
    public boolean alwaysFetchStrings(int layerID) {
        return layerIDsAndDesc[getLayerNbrFromID(layerID)].getAlwaysFetchStrings();
    }
    
    public long getCRC() {
        return crc;
    }
    
    public TileMapLayerInfo getLayer(int layerID){
        return layerIDsAndDesc[getLayerNbrFromID(layerID)];
    }
    
    public int getNbrOverviewMaps() {
        return extraTilesForReserve;
    }
    
    public int getOverviewMapDetailLevel() {
        return reserveDetailLevel;
    }
    
    public int getTextColor() {
        return (int)iTextColor;
    }
    
    public CopyrightHolder getCopyrightHolder(){
        return copyrightHolder;
    }
        
    public String getStaticCopyrightString(){
        return staticCopyrightString;
    }
    
    public void setDownloadACPEnabled( boolean aEnabled ){
        for(int i = 0; i < nbrOfLayers; i++){
            TileMapLayerInfo tmLayerInfo = layerIDsAndDesc[i];
            if(tmLayerInfo.isAffectedByACPMode()){
                tmLayerInfo.setACPDownloadEnabled(aEnabled);
            }
        }       
    }
    
    private static final int LAYER_TRAFFIC = 3; 
    
    /**
     * 
     * Set the traffic info layer setting. It's hard coded as the traffic 
     * layerID is the only way we differentiate it from the others right now. 
     * 
     * @param aIsVisible, true if the traffic info layer should 
     *        be visible, false if not
     */
    public void setTrafficLayerVisible( boolean aIsVisible){
        layerIDsAndDesc[LAYER_TRAFFIC].setVisible(aIsVisible);
    }

    /**
     * 
     * Set the update time for the traffic layer. It's hard coded as the traffic 
     * layerID is the only way we  differentiate it from the others right now. 
     * 
     * @param aMinutes, the update time in minutes. 
     */
    public void setTrafficInfoUpdateTime( int aMinutes){    
        layerIDsAndDesc[LAYER_TRAFFIC].setUpdatePeriodMinutes(aMinutes);
    }

    /**
     * Determines scale index from zoom level.
     * 
     * @param zoomLevel Current zoom level.
     * @return scale index.
     */
    public int getScaleIndexFromZoomLevel(float zoomLevel) {
        int index = m_scaleLevelsTable.length;
        int first = 0;
        int count = index - 1;
        while (count > 0) {
            int step = count/2; 
            index = first + step;
            if (zoomLevel >= m_scaleLevelsTable[index]) {
                first = index + 1;
                count -= step + 1;
            } else {
                count = step;
            }
        }

        return first;
    }

}
