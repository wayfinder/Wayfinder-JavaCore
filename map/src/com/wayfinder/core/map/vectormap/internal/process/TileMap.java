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
import java.util.*;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

public class TileMap {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMap.class);
    
    private int             mc2Scale;
    private long[]          referenceCoord = null;
    private int             features;
    private int[]           strIdxByFeatureIdx = null;
    private int[]           categoryIdxByFeatureIdx = null;
    private ExtendedStrings []iExtenedStringsArray = null;
    private String[]        strings = null;
    private Vector          data = null;
    private Vector          allArgs = null;
    private long            crc;
    private char            emptyImportances;
    private TileMapParams   params = null;
    private long            timestamp;    
    private byte            []iData = null;    
    private Vector          allTileFeatures;

    
    public TileMap() {
        mc2Scale = 1;
        referenceCoord = new long[2];
        allTileFeatures = new Vector();
    }
        
    public void addArg(TileFeatureArg arg) {
        if(allArgs == null) {
            allArgs = new Vector();
        }
        this.allArgs.addElement(arg);
    }
 
    public long getTimestamp(){
        return timestamp;
    }
    
    public int getMC2Scale(){
        return mc2Scale;
    }
    
    public char getEmptyImportances() {
        return emptyImportances;
    }
    
    public long[] getReferenceCoord(){
        return referenceCoord;
    }
    
    public void setParams(TileMapParams tmp) {
        params = tmp;
    }
    
    public TileMapParams getTileMapParams() {
        return params;
    }
    
    public int getImportance() {
        return params.getImportance();
    }
    
    private int iNumberOfImportace = 0;
    
    /**
     * Set the current number of importance for the scale that
     * was used when the tile map was loaded.
     * 
     * @param nbrImp
     */
    public void setNumberOfImportance(int nbrImp) {
        iNumberOfImportace = nbrImp;
    }
    
    /**
     * @return the current number of importance for the tile
     */
    public int getNumberOfImportance() {
        return iNumberOfImportace;
    }
    
    
    private int iMaxNbrOfImportance = 0;    
    
    /**
     * Set the max number of importance for the detail level. 
     *  
     * @param nbrImportance
     */
    public void setMaxNbrOfImportances(int nbrImportance) {    
        iMaxNbrOfImportance = nbrImportance;
    }
    
    /**
     * @return the max number of importance for the detail level. 
     */
    public int getMaxNbrOfImportance() {
        return iMaxNbrOfImportance;
    }
    
    public byte []getByteData() {
        return iData;
    }
    
    public void clearByteData() {
        iData = null;
    }
    
    public boolean load(BitBuffer bitBuffer, TileMapFormatDesc tmfd, boolean gunzippedAlready ){
        
        iData = bitBuffer.getByteArray();
        
        int nbrBitsFeatureIdx = 0;
        int nbrBitsStrIdx = 0;
        int iNbrFeatures = 0;
        timestamp = System.currentTimeMillis();        
        bitBuffer.reset();
        bitBuffer.alignToByte();
        // unknown string
        bitBuffer.nextString();
        
        // Check if the layer exists.
        if(!tmfd.hasLayerID(params.getLayerID())){
            if(LOG.isError()) {
                LOG.error("TileMap.load()", "No such LayerID in TMFD - layer "+params.getLayerID());
            }
            return false;
        }
        
        // Set the reference coordinate and mc2 scale to the map.
        mc2Scale = (int)(tmfd.getCoordAndScaleForTile(
            params.getLayerID(),
            params.getDetailLevel(),
            params.getTileIndexLat(),
            params.getTileIndexLon(),
            referenceCoord ) *
            Utils.METER_TO_MC2SCALE );
        // Adjust the reference coord so it is a multiple of the mc2scale.
        snapCoordToPixel( referenceCoord );
        
        int tileMapType = params.getTileMapType();
        
        if(tileMapType == TileMapParams.MAP ){   //GEOMETRY

            int nbrOfFeatures = bitBuffer.nextBits(31) ;
            if(LOG.isTrace()) {
                if(nbrOfFeatures > 300) {
                    LOG.trace("TileMap.load()", "Warning! Features: " + nbrOfFeatures +
                            ", LayerID: " + params.getLayerID() +
                            ", Detail: "  + params.getDetailLevel());
                }
            }            
            
            TileFeature prevFeature = null;
            TileFeature target = null;
            for ( int i = 0; i < nbrOfFeatures; i++ ) {
                // Read type and create tilemapfeature of correct
                // dynamic type.
                target = new TileFeature(this);
                
                
                if(target.createFromStream(bitBuffer, tmfd, this, prevFeature )) {
                    
                    /* Add the tile feature to a array. All the argument in the tile feature
                     * will be collected in the initData method */
                    allTileFeatures.addElement(target);
                    
                    features ++;                    
                    prevFeature = target;
                } else {
                    if(LOG.isError()) {
                        LOG.error("TileMap.load()", "Unable to load TileMap!");
                    }
                    return false;
                }           
            }
            
        }else{
            // Load the strings.
            // Read the number of bits needed for representing
            // the string index.
            nbrBitsStrIdx = bitBuffer.nextBits( 4 );
            
            // Read the number of bits needed for representing
            // the feature index.
            nbrBitsFeatureIdx = bitBuffer.nextBits( 4 );
            
            // Read the number of features.
            iNbrFeatures = bitBuffer.nextBits( nbrBitsFeatureIdx );
            // Read the number of features with strings.
            int nbrStringFeatures = bitBuffer.nextBits( nbrBitsFeatureIdx );
            // Read the number of strings.
            int nbrStrings = bitBuffer.nextBits( nbrBitsStrIdx );
            // Resize the vectors.
            strings = new String[nbrStrings];
            
            // Default is -1, i.e. that the feature has no strings.
            strIdxByFeatureIdx = new int[iNbrFeatures];          
            for(int f=0;f<strIdxByFeatureIdx.length;f++){
                strIdxByFeatureIdx[f]= -1;
            }
            // Read pairs of ( feature index, string index ) in
            // the order as specified by m_featureIdxInTextOrder.
            
            for ( int i = 0; i < nbrStringFeatures; i++ ) {
                // Feature index.
                int featureIdx = bitBuffer.nextBits( nbrBitsFeatureIdx );
                
                // String index.
                int strIdx = bitBuffer.nextBits( nbrBitsStrIdx );
                
                // Add to m_strIdxByFeatureIdx.
                strIdxByFeatureIdx[featureIdx] = strIdx;
            }
            // Read the strings.
            bitBuffer.alignToByte();
            
            /* Load the string for the tile */
            for ( int i = 0; i < nbrStrings; i++ ) {            
                strings[i]=bitBuffer.nextStringUTF();
            }            
        }
  
        bitBuffer.alignToByte();
        
        // Read CRC if features are present.
        crc = Integer.MAX_VALUE;
        if ( !(features < 1 && (strIdxByFeatureIdx == null || strIdxByFeatureIdx.length<1))) {
            // Make sure there is room for CRC in the buffer.
            if ( bitBuffer.getNbrBytesLeft() < 4 )
                return true;
            
            crc = bitBuffer.nextInt(); // CRC
                //System.out.println("tilemap load "+ crc+ " "+params.getAsString());
        }
        
        // Read the empty importances. 16 bits must be enough!
        if ( bitBuffer.getNbrBytesLeft() < 2 ) 
            return true;
        
        emptyImportances = bitBuffer.nextShort();

        if(tileMapType == TileMapParams.STRINGS) {
            
            // Make sure there are enough bytes left to read the category id data
            if ( bitBuffer.getNbrBytesLeft() < 4) {
                return true;
            }
            
            loadPOICategories(bitBuffer, iNbrFeatures, nbrBitsFeatureIdx);
            
            // Make sure that you can load the extended string table
            if(bitBuffer.getNbrBytesLeft() < 1) 
                return true;
                    
            loadExtendedStringTable(bitBuffer, nbrBitsFeatureIdx, nbrBitsStrIdx);
        }       
        return true;        
    }
    
    /*
     * Format ( first align to byte ):
     *
     *            +------------------------------+---------------------------+
     *            |            Data              |     Size in bits          |
     *            +------------------------------+---------------------------+
     * Header     | Reserved, Do not use         |        32                 |
     *            | Have categories              |        1                  |
     *            +------------------------------+---------------------------+
     *              If "Have categories" == 1:
     *            +------------------------------+---------------------------+
     * Categories | #bits for categories         |        4                  |
     * header     | #bits for Category ID        |        4                  |
     *            | Category size                | #bits for categories      |
     *            +------------------------------+---------------------------+
     *              For all categories:
     *            +------------------------------+---------------------------+
     * Categories | Feature ID                   | #bits for features        |
     *            | Category ID                  | #bits for category ID     |
     *            +------------------------------+---------------------------+
     */ 
    private void loadPOICategories(BitBuffer bitBuffer, int nbrFeatures, int nbrBitsFeatureIdx) {
        
        bitBuffer.alignToByte();
        // Reserved 32 bits, do not use!
        int oldNbrOfCat = (int)bitBuffer.nextInt();
        
        if(oldNbrOfCat != 0) 
            return;
        
        if(bitBuffer.getNbrBytesLeft() == 0)
            return;
        
        int hasCategories = bitBuffer.nextBit();
        
        if(LOG.isTrace()) {
            LOG.trace("TileMap.loadPOICategories()", "hasCategories= "+hasCategories);
        }
        
        if(hasCategories == 1) {
            int nbrBitsCategories = bitBuffer.nextBits(4);
            int nbrBitsCategoryID = bitBuffer.nextBits(4);
            int nbrOfCategories = bitBuffer.nextBits(nbrBitsCategories);
            
            if(LOG.isTrace()) {
                LOG.trace("TileMap.loadPOICategories()", "nbrOfCategories= "+nbrOfCategories);
            }
            
            if(nbrOfCategories > 0) {
                int featureIndex, categoryID;
                categoryIdxByFeatureIdx = new int[nbrFeatures];
                
                for(int f=0;f<categoryIdxByFeatureIdx.length;f++){
                    categoryIdxByFeatureIdx[f] = -1;
                }
                                
                for(int i=0; i<nbrOfCategories; i++) {                      
                    featureIndex = (int)bitBuffer.nextBits(nbrBitsFeatureIdx);
                    categoryID = bitBuffer.nextBits(nbrBitsCategoryID);
                    categoryIdxByFeatureIdx[featureIndex] = categoryID;
                    
                    if(LOG.isTrace()) {
                        LOG.trace("TileMap.loadPOICategories()", "feature index "+featureIndex+" has category id "+categoryID);
                    }
                }
            }
        }
    }
    
    /*
    *
    * Extended tile string format:
    *
    * +------------------------------+-----------------------+
    * |             Data             |    nbr bits           |
    * +------------------------------+-----------------------+
    * | Have extended strings        |     1                 |
    * +------------------------------+-----------------------+
    *   If "Have extended strings" == 1 then:
    * +------------------------------+-----------------------+
    * | #bits per string typ         |     4                 |
    * | #bits for table size         |     4                 |
    * | Table size                   | #bits for table size  |
    * +------------------------------+-----------------------+
    *   Table:
    * +------------------------------+---------------------------------+
    * | Same feature index           |   1  (no bit for first index)   |
    * +------------------------------+---------------------------------+
    *
    *  If same feature index = 0 then read ( else jump to next table):
    * +------------------------------+-----------------------+
    * | Feature index                | #bits per feature     |
    * +------------------------------+-----------------------+
    *
    * +------------------------------+-----------------------+
    * | String type                  | #bits per string type |
    * | String index                 | #bits per index       |
    * +------------------------------+-----------------------+
    *
    */ 
    private void loadExtendedStringTable(BitBuffer bitBuffer, int nbrBitsFeatureIdx, int nbrBitsStrIdx) {       
        
        int haveExtendedStrings = bitBuffer.nextBit();
        
        if(LOG.isTrace()) {
            LOG.trace("TileMap.loadExtendedStringTable()", "haveExtendedStrings= "+haveExtendedStrings);
        }
        
        if(haveExtendedStrings == 1) {
            int nbrBitsPerStrType   = bitBuffer.nextBits(4);
            int nbrBitsForTableSize = bitBuffer.nextBits(4);
            int stringTableSize     = bitBuffer.nextBits(nbrBitsForTableSize);
            
            iExtenedStringsArray = new ExtendedStrings[stringTableSize];
            ExtendedStrings extString = null;
            int extendedStrCnt = 0;
            int featureIdx=0,stringType,stringIndex, sameFeatureIndex=0;
            
            for(int i=0; i<stringTableSize; i++) {                  
                if(i > 0)
                    sameFeatureIndex = bitBuffer.nextBit();
                
                if(sameFeatureIndex == 0)
                    featureIdx  = bitBuffer.nextBits(nbrBitsFeatureIdx);
                
                stringType  = bitBuffer.nextBits(nbrBitsPerStrType);
                stringIndex = bitBuffer.nextBits(nbrBitsStrIdx);
                
                if(sameFeatureIndex == 1 && extString != null) {
                    extString.addString(stringType, strings[stringIndex]);
                } else {
                    extString = new ExtendedStrings(featureIdx, stringType, strings[stringIndex]);
                    iExtenedStringsArray[extendedStrCnt++] = extString;
                }
                
                if(LOG.isTrace()) {
                    LOG.trace("TileMap.loadExtendedStringTable()", 
                               "featureIdx= "+featureIdx+
                               " strType= "+stringType+
                               " strIndex= "+stringIndex+
                               " name= "+strings[stringIndex]+
                               " name2= "+strings[featureIdx]+
                               " sameFeatureIndex= "+sameFeatureIndex);
                }               
            }
        }
    }
    
    public Vector getGeoData() {
        return data;
    }
    
    public Vector initData(TileMapFormatDesc tmfd) {
        TileFeature[][] primitiveDefaultMap = tmfd.getPrimitiveDefaultMap();
        Vector tempData = new Vector();
        int detailLevel = params.getDetailLevel();        
        TileFeature tileFeature;
        
        /* For all features in the TileMap */
        for (int i=0; i<allTileFeatures.size(); i++) {
            tileFeature = (TileFeature)allTileFeatures.elementAt(i);
            
            // Get the feature type
            int type = tileFeature.getType();
            
            TileFeature[] tileFeatures = primitiveDefaultMap[type];
            TileFeature feature = tileFeatures[tileFeatures.length - 1];
            
            /* The default tile feature argument from tmfd */ 
            TileFeatureArg[] defaultTileFeatureArgs = feature.getArgs();
            
            /* The primitive TileFeature type */
            int primitiveType = feature.getType();
            
            TileFeatureData geoData = new TileFeatureData(primitiveType, i, type);
            
         
            for (int x=0; x<defaultTileFeatureArgs.length; x++) {
                if (defaultTileFeatureArgs[x].getType() == TileFeatureArg.SIMPLEARG) {                       
                    SimpleArg simpleArg = (SimpleArg)defaultTileFeatureArgs[x]; 
                    
                    setGeoDataArguments(detailLevel, geoData, simpleArg);                  
                    
                } else if (defaultTileFeatureArgs[x].getType() == TileFeatureArg.STRINGARG) {
                    
                    StringArg stringArg = (StringArg)defaultTileFeatureArgs[x];
                    
                    /* Note that old clients (branch_release_7 and before) will not check if the 
                     * TileArgName is IMAGE_NAME. Those clients will just take any string arg and 
                     * use it as a image name. */
                    if (stringArg.getName() == TileArgNames.IMAGE_NAME) {
                        int poiIdx = TileMapControlThread.
                                        getStringIndex("b"+stringArg.getValue(detailLevel)+".png");
                        geoData.setBitmapIndex(poiIdx);
                    }
                }                   
            }
            
            TileFeatureArg []tileArgs = tileFeature.getArgs();
            
            /* For all argument in the TileFeature. Load the argument send with the feature.
             * The default argument that are loaded from tmfd will be replaced */
            for (int j=0; j<tileArgs.length; j++) {
                
                if (tileArgs[j].getType() == TileFeatureArg.COORDARG) {
                    
                    CoordArg coordArg = (CoordArg)tileArgs[j];
                    
                    // Check if the POI type is visible on the screen where parsing the data.                  
                    if (!tmfd.categoryEnabled(type)) {
                        continue;
                    }
                    
                    geoData.setCenterLat(coordArg.getLatitude());
                    geoData.setCenterLon(coordArg.getLongitude());                  
                    geoData.setBoundingBox(coordArg.getLatitude(), 
                                           coordArg.getLatitude(), 
                                           coordArg.getLongitude(), 
                                           coordArg.getLongitude());    
                    
                } else if(tileArgs[j].getType() == TileFeatureArg.COORDSARG) {
                    
                    CoordsArg coordsArg = (CoordsArg)tileArgs[j];                   
                    geoData.setCoords(coordsArg.getCoords().getArray());
                    geoData.setBoundingBox(coordsArg.getMaxLat(), 
                                           coordsArg.getMinLat(), 
                                           coordsArg.getMaxLon(), 
                                           coordsArg.getMinLon());         
                    
                /* Note that old clients (branch_release_7 and before) will not load
                 * simple arg that are send with the feature. Those client will 
                 * only use the default value from tmfd. */
                } else if (tileArgs[j].getType() == TileFeatureArg.SIMPLEARG) {
                    
                    SimpleArg simpleArg = (SimpleArg)tileArgs[j];     
                    
                    setGeoDataArguments(detailLevel, geoData, simpleArg);  
                    
                } else if (tileArgs[j].getType() == TileFeatureArg.STRINGARG) {
                    
                    StringArg stringArg = (StringArg)tileArgs[j];
                    
                    /* Note that old clients (branch_release_7 and before) will not check if the 
                     * TileArgName is IMAGE_NAME. Those clients will just take any string arg and 
                     * use it as a image name. */
                    if (stringArg.getName() == TileArgNames.IMAGE_NAME) {
                        int poiIdx = TileMapControlThread.
                                        getStringIndex("b"+stringArg.getValue(detailLevel)+".png");
                        geoData.setBitmapIndex(poiIdx);
                    }
                }               
            } 

            tempData.addElement(geoData);
        }
        
        data = tempData;                
        return tempData;
    }

    /**
     * Sets the geometric data from argument.
     * 
     * @param detailLevel The detail level to set the argument.
     * @param geoData The geometric data to set the argument.
     * @param simpleArg The argument to set in geoData.
     */
    private void setGeoDataArguments(int detailLevel, TileFeatureData geoData, SimpleArg simpleArg) {
        if (simpleArg.getName() == TileArgNames.LEVEL) {
            geoData.setLevel(simpleArg.getValue(detailLevel));
        } else if (simpleArg.getName() == TileArgNames.MAX_SCALE) {
            geoData.setMaxScale(simpleArg.getValue(detailLevel));
        } else if (simpleArg.getName() == TileArgNames.COLOR) {
            geoData.setColor(simpleArg);
        } else if (simpleArg.getName() == TileArgNames.WIDTH) {
            geoData.setWidth(simpleArg);
        } else if (simpleArg.getName() == TileArgNames.WIDTH_METERS) {
            geoData.setWidthMeter(simpleArg);
        } else if (simpleArg.getName() == TileArgNames.BORDER_COLOR) {
            geoData.setBorderColor(simpleArg);                       
        } else if (simpleArg.getName() == TileArgNames.TIME) {
            geoData.setTime(simpleArg.getValue(detailLevel));
        } else if (simpleArg.getName() == TileArgNames.ID) {
            geoData.setId(simpleArg.getValue(detailLevel));
        } else if (simpleArg.getName() == TileArgNames.DURATION) {
            geoData.setDuration(simpleArg.getValue(detailLevel));
        }
    }
    
    public String getParamString() {
        return params.getAsString();
    }
    
    
    private void snapCoordToPixel( long[] coord ){
        coord[0]=  mc2Scale * (coord[0] / mc2Scale);
        coord[1]=  mc2Scale * (coord[1] / mc2Scale);
    }
    
    public String getStringForFeature(int featureNbr){
        return "";
    }
    
    public String[]getStringArray(){
        return strings;
    }
    
    public int[] getStrIdxByFeatureIdx(){
        return strIdxByFeatureIdx;
    }
    
    /**
     * 
     * @param aFeatureIndex
     * @return
     */
    public ExtendedStrings getExtendedString(int aFeatureIndex) {
        
        if(iExtenedStringsArray == null)
            return null;
        
        for(int i=0; i<iExtenedStringsArray.length; i++) {
            if(iExtenedStringsArray[i].iFeatureIndex == aFeatureIndex) {
                return iExtenedStringsArray[i];
            }
        }
        
        return null;
    }

    public long getCRC() {
        return crc;
    }
    
    public void purgeArgs(){
        allArgs = null;
        allTileFeatures = null;
        features = 0;
    }
    
    public void purgeGeoData() {
        if(data!=null) {
            data.removeAllElements();
            data = null;
        }
    }
    
    public void purgeStrings() {
        strIdxByFeatureIdx = null;
        strings = null;
        categoryIdxByFeatureIdx = null;
        iExtenedStringsArray = null;
    }
    
    public void clean() {
        purgeArgs();
        purgeGeoData();
        purgeStrings();
    }
    
}
