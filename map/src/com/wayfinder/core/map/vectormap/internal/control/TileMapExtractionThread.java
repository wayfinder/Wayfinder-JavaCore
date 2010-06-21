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

package com.wayfinder.core.map.vectormap.internal.control;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.map.vectormap.internal.process.TileFeature;
import com.wayfinder.core.map.vectormap.internal.process.TileFeatureData;
import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;
import com.wayfinder.pal.util.UtilFactory;


/**
 * The TileMapExtractionThread class handles the extraction of new TileMaps that
 * has been loaded from the cache, pre-installed maps or the server. 
 * 
 * The class contains one thread that is used for extraction. 
 *
 * 
 */
public class TileMapExtractionThread implements Runnable {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapExtractionThread.class);
    
    private static boolean isRunning = true;
    
    private LinkedList iNewUnprocessedTiles;
    
    private TileMapFormatDesc tmfd;
    private TileMapControlThread iTileMapControl;
    private UtilFactory m_UtilFactory;
    
    private boolean m_SupportDrawingPolygons;
    
    public TileMapExtractionThread(TileMapControlThread aTileMapControl, UtilFactory utilFactory) {
        iTileMapControl = aTileMapControl;
        m_UtilFactory = utilFactory;
        
        iNewUnprocessedTiles = new LinkedList();
      
    }
    
    void init(ConcurrencyLayer currLayer, boolean supportPolygons) {
        m_SupportDrawingPolygons = supportPolygons;
        Thread t = currLayer.startNewDaemonThread(this, "TMExtraction");
        t.setPriority(Thread.NORM_PRIORITY);
    }
    
    /**
     * Set the TileMapFormatDesc 
     * 
     * @param atmfd
     */
    public void setTileMapFormatDesc(TileMapFormatDesc atmfd) {
        tmfd = atmfd;
    }
    
    /**
     * Add new Tiles that should be extracted and 
     * notify the extraction thread
     * 
     * @param aParam:  
     * @param data: the byte buffer for the importance 
     */
    public void addTileToExtraction(TileMapParams aParam, byte[] data) {
        synchronized(iNewUnprocessedTiles) {
            iNewUnprocessedTiles.addLast(new UnprocessTile(aParam,data));
            iNewUnprocessedTiles.notifyAll();
        }
    }
    
    /**
     * The run method the handles the extraction of new TileMaps. 
     * 
     * The thread will sleep when no new tilemap are in the queue. 
     * 
     */
    public void run() { 
        while(isRunning) {
            try {
                //move all pending task here in a single synchronized block
                UnprocessTile[] tileMapsToExtract;
                synchronized (iNewUnprocessedTiles) {
                    while (iNewUnprocessedTiles.isEmpty()) {
                        iNewUnprocessedTiles.wait();
                    }
                    //Update new tiles that will be extracted.
                    tileMapsToExtract = new UnprocessTile[iNewUnprocessedTiles.size()]; 
                    iNewUnprocessedTiles.toArray(tileMapsToExtract);
                    iNewUnprocessedTiles.clear();
                }
                for (int i= 0; i< tileMapsToExtract.length; i++) {
                    processExtraction(tileMapsToExtract[i]);
                    tileMapsToExtract[i] = null;//free the memory
                    Thread.yield();//why?
                }
                tileMapsToExtract = null;
            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("TileMapExtractionThread.run()", e);
                }
                iNewUnprocessedTiles.clear();
                iTileMapControl.resetAllLayers();
            }
        }
    }
    
    /**
     * Extract tilemaps that has been send from the cache or internet.  
     * 
     */
    private void processExtraction(UnprocessTile tile) {  
        TileMapParams params = tile.params;
        byte[] data = tile.data;
        String paramString = params.getAsString();  
        
        if(LOG.isDebug()) {
            LOG.debug("TileMapExtractionThread.processExtraction()", paramString);
        }
        
        
        // Extract bitmaps
        if(TileMapParamTypes.isBitmap(paramString)) {           
            iTileMapControl.addBitmap(paramString, data);
            
        // Extract TilemapFormatDesc
        } else if(TileMapParamTypes.isMapFormatDesc(paramString)) {         
            if(TileMapParamTypes.isTmfdDay(paramString) || TileMapParamTypes.isTmfdNight(paramString)) {
                // Extraction tmfd
                if(LOG.isInfo()) {
                    LOG.info("TileMapExtractionThread.processExtraction()", "extract TMDF " + paramString);
                }
                TileMapFormatDesc tempTMFD = new TileMapFormatDesc();
                BitBuffer bitBuffer = new BitBuffer(data);
                if(tempTMFD.load(bitBuffer,null)) {             
                    iTileMapControl.addExtractedTileMapFormatDesc(tempTMFD,paramString);
                }
            } else {            
                // Extracting tmfd crc
                BitBuffer bitBuffer = new BitBuffer(data);

                // skip the first byte in the buffer. For some reason the server always start the 
                // crc replay with a 0. 
                bitBuffer.nextByte();               
                long crc = bitBuffer.nextInt();             
                //System.out.println("INFO: TileMapExtractionThread.processExtraction() CRC= "+(int)crc+" paramString= "+paramString);
                iTileMapControl.addExtractedTileMapFormatDescCrc(crc, paramString);
            }
            
        // Don't extract tilemaps that isn't visible. This happen when the user scroll/zoom a lot in the
        // map. Then the tile can be outside the screen before it has been extracted. 
        } else if(!iTileMapControl.isTileMapVisible(params)) {
            if(LOG.isInfo()) {
                LOG.info("TileMapExtractionThread.processExtraction()", 
                        "Skip to extract a non visible tile: paramString= "+paramString);
            }
            return;
        
        // Extract TileMaps
        } else if(TileMapParamTypes.isMap(paramString)) {
            TileMap tileMap = null;
            try {                   
                tileMap = unpackData(data, params); 
            } catch(Exception e) {      
                if(LOG.isError()) {
                    LOG.error("TileMapExtractionThread.processExtraction()", "Faild to parse "+params.getAsString());
                    LOG.error("TileMapExtractionThread.processExtraction()", e);
                }
                tileMap = null;
            }
            if(tileMap != null) {
                if(params.getTileMapType() == TileMapParams.MAP) { // If geometry     
                    Vector geoData = tileMap.initData(tmfd);
                    tileMap.purgeArgs();
                    triangulatePolygons(geoData, tileMap);
                    scaleCoords(geoData, tileMap);
                } else { // Strings
                    
                    long geoMapCRC = iTileMapControl.getGeoMapCRC(params.getTileID(), tileMap.getImportance());                 
                    if(tileMap.getCRC() != geoMapCRC && geoMapCRC != 0) {                       

                        if(LOG.isError()) {
                            LOG.error("TileMapExtractionThread.processExtraction()", 
                                    "crc mismatch string CRC= "+tileMap.getCRC()+" geometric CRC= "+geoMapCRC+" layerID= "+params.getLayerID());
                        }
                                                
                        String iGeoParam = iTileMapControl.getGeoMapParamString(params.getTileID(), params.getImportance());
                        MapTask event = new MapTask(iTileMapControl);   
                        event.removeAndReloadTileMaps(iGeoParam, paramString, tileMap.getTileMapParams());
                        return; 
                    }                           
                }           
                iTileMapControl.addExtractImportance(params, tileMap);              
            } else {
                
                if(LOG.isError()) {
                    LOG.error("TileMapExtractionThread.processExtraction()", 
                            "Unable to load the TileMap, clear the tile and request new tile and tmfd");
                }
                                                                                        
                MapTask event = new MapTask(iTileMapControl);
                event.removeAndReloadOneTileMap(params);
                
                event = new MapTask(iTileMapControl);
                event.loadTileMapFormatDesc();
            }   
        } 
        
        else {
            if(LOG.isError()) {
                LOG.error("TileMapExtractionThread.processExtraction()", "UNKNOWN TileMapParams to extract");
            }
        }
    }
    
    // ----------------------------------------------------------------------------
    // Extraction methods
    
    /**
     * Unpack and load the tilemap
     */
    private TileMap unpackData(byte[] data, TileMapParams tmp) {
        TileMap map = new TileMap();
        map.setParams(tmp);
        
        //If gzip, then gunzip
        if((((data[0]<<8) & 0xFF00) | (data[1] & 0xFF)) == 0x1f8b){
            try {
                data = unzipData(data);                
                if(LOG.isTrace()) {
                    LOG.trace("TileMapExtractionThread.unpackData()", tmp.getAsString()+" is now unzipped!");
                }
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("TileMapExtractionThread.unpackData()", "failed to unzip "+tmp.getAsString());
                    LOG.error("TileMapExtractionThread.unpackData()", e);
                }
                return null;
            }
        }
        
        BitBuffer bitBuffer = new BitBuffer(data);
        
        if(map.load(bitBuffer,tmfd,false)) {
            return map;
        } else {
            if(LOG.isError()) {
                LOG.error("TileMapExtractionThread.unpackData()", "Failed to load map! "+tmp.getAsString());
            }           
            return null;
        }
    }
    
    /**
     * Unzip the buffer if it's compressed. 
     */
    private byte[] unzipData(byte[] aData) throws IOException {
        
        ByteArrayInputStream bain = new ByteArrayInputStream(aData);
        InputStream gStream = m_UtilFactory.openGZIPInputStream(bain);
        byte[] data;

        byte[]tempBuffer = new byte[1024];
        int check;
        int offset= 0;
        int chunk = 1;

        while((check = gStream.read(tempBuffer,offset,chunk))!=-1){
            offset+=check;
            if(offset+chunk>=tempBuffer.length){
                byte[]newBuffer = new byte[tempBuffer.length<<1];
                System.arraycopy(tempBuffer,0,newBuffer,0,tempBuffer.length);
                tempBuffer=newBuffer;
                newBuffer=null;
            }
        }
        data = new byte[offset];
        System.arraycopy(tempBuffer, 0, data, 0, offset);

        return data;

    }
    
    // ------------------------------------------------------------------------
    // 
    /**
     * For all polygons, correct winding and triangulate.
     *
     * @param geoDatas The vector data that is to be rendered
     * @param map Used for testing purposes
     */
    public void triangulatePolygons(Vector geoDatas, TileMap map){
        long timer = 0;
        TileFeatureData geoData;
        
        for(int i=0; i<geoDatas.size();i++){
            
            geoData = (TileFeatureData)geoDatas.elementAt(i);
            
            if(geoData.getPrimitiveType() == TileFeature.POLYGON && geoData.getCoords().length >= 6) {            
                
                if(Utils.TIME_APP) {
                    timer = System.currentTimeMillis();
                }
                
                int []coords = geoData.getCoords();
                
                int verticesLength;
                int cLength = coords.length;
                if(coords[0] == coords[cLength-2] && coords[1] == coords[cLength-1]) {
                    verticesLength = cLength-2;
                } else {
                    verticesLength = cLength;
                }
             
                //ConcavePolygon cp = new ConcavePolygon(coords, 0, coords.length);
                ConcavePolygon cp = new ConcavePolygon(coords, 0, verticesLength);
             
                // Triangulate polygons if the underlying implementation doesn't support 
                // drawing polygons. Then we need to draw triangles instead. 
                if(!m_SupportDrawingPolygons) {
                    try {
                        if(!cp.triangulate()){
                            if(LOG.isError()) {
                                LOG.error("TileMapExtractionThread.triangulatePolygons()", 
                                        "Unable to triangulate "+map.getParamString());
                            }
                        }
                    } catch(Exception e){
                        if(LOG.isError()) {
                            LOG.error(
                            "TileMapExtractionThread.triangulatePolygons()",e);
                        }                        
                        cp = null;
                    }
                }
                
                if(Utils.TIME_APP) {
                    timer = System.currentTimeMillis()-timer;
                    //System.out.println("Triangulate: " + timer + " ms (" + vertices.length + " vertex)");
                }
                
                geoData.setConcavePolygon(cp);
            }                 
        } // end for
    }
    
    /**
     * Scale the all the coordinates in the TileMap to MC2 coordinates. 
     * 
     * @param gd
     * @param map
     */
    private void scaleCoords(Vector gd, TileMap map) {
        float scale = map.getMC2Scale();
        long[] offset = map.getReferenceCoord();
        TileFeatureData geoData;

        for(int i=0; i<gd.size(); i++) {
            geoData = (TileFeatureData)gd.elementAt(i);
            
            if(geoData.getPrimitiveType() != TileFeature.BITMAP) {
                
                int[] coordExtremes = geoData.getCoordExtremes();
                long minX = ((int)(coordExtremes[0]*scale))+offset[0];
                long maxX = ((int)(coordExtremes[1]*scale))+offset[0];
                long minY = ((int)(coordExtremes[2]*scale))+offset[1];
                long maxY = ((int)(coordExtremes[3]*scale))+offset[1];
                
                if(minX<Integer.MIN_VALUE) minX=Integer.MIN_VALUE+1;
                if(minY<Integer.MIN_VALUE) minY=Integer.MIN_VALUE+1;
                if(maxX>Integer.MAX_VALUE) maxX=Integer.MAX_VALUE-1;
                if(maxY>Integer.MAX_VALUE) maxY=Integer.MAX_VALUE-1;
                
                geoData.setBoundingBox((int)maxX, (int)minX, (int)maxY, (int)minY);
                
                int []coords = geoData.getCoords();
                
                for(int j=0; j<coords.length; j+=2) {
                    
                    long x = ((int)(coords[j]*scale))+offset[0];
                    long y = ((int)(coords[j+1]*scale))+offset[1];
                    
                    if(x<Integer.MIN_VALUE) x=Integer.MIN_VALUE+1;
                    if(y<Integer.MIN_VALUE) y=Integer.MIN_VALUE+1;
                    if(x>Integer.MAX_VALUE) x=Integer.MAX_VALUE-1;
                    if(y>Integer.MAX_VALUE) y=Integer.MAX_VALUE-1;
                    
                    coords[j] = (int)x;
                    coords[j+1] = (int)y;                    
                }                
                gd.setElementAt(geoData,i);
            }      
        }
    }
    
    static public class UnprocessTile {
        public final TileMapParams params;  
        public final byte[] data;
        
        public UnprocessTile(TileMapParams params, byte[] data) {
            super();
            this.params = params;
            this.data = data;
        }
    }
}
