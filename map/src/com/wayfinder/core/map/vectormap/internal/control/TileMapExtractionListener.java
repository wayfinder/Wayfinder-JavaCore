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
package com.wayfinder.core.map.vectormap.internal.control;

import com.wayfinder.core.map.vectormap.internal.process.TileMap;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;

/**
 * 
 * This interface will be implemented by the class that want
 * to be notified that an new: 
 * 
 *  - TileMap has been extracted
 *  - TileMap has been removed (outside the bounding box)
 *  - Overview map has been extracted / removed. 
 *  - TileMapFormatDesc has been extracted. 
 *  
 *  Set the TileMapExtractionListener in the TileMapControlThread class. 
 *  @see com.wayfinder.map.vectormap.control.TileMapControlThread
 * 
 *
 */
public interface TileMapExtractionListener {
    
    /**
     * Add one or more extracted TileMaps to the listener
     * 
     * @param aTileMaps, the list of extracted TileMaps
     */
    public void addExtractedTileMaps(TileMap []aTileMaps);
    
    /**
     * Add one extracted TileMap to the listener
     * 
     * @param aTileMaps, the extracted tileMap
     */
    public void addExtractedTileMap(TileMap aTileMaps);
    
    /**
     * Add a extracted TileMapFormatDesc to the listener
     * 
     * @param aTileMapFormatDesc
     */
    public void addExtractedTileMapFormatDesc(TileMapFormatDesc aTileMapFormatDesc);
    
    /**
     * Add a extracted overview map to the listener. 
     * 
     * @param aTileMap
     */
    public void addExtractedOverviewMap(TileMap aTileMap);
    
    /**
     * Remove a unseen TileMap 
     * 
     * @param aTileID, the TileID for the TileMap
     * @param layerID
     */
    public void removeTileMap(TileMapParams aTileIDParam);
    
    /**
     * Remove a unseen Overview map. 
     * 
     * @param aTileID, the TileID for the Overview map. 
     */
    public void removeOverviewMap(String aTileID);
}
