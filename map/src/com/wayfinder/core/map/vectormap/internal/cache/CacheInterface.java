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
package com.wayfinder.core.map.vectormap.internal.cache;

import java.util.Hashtable;

import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapper;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapperInterface;
import com.wayfinder.core.map.vectormap.internal.process.TileMapFormatDesc;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;

/**
 * Interface for the different cache systems. 
 * 
 */
public interface CacheInterface {
    
    public void closeCache();
    
    public void setMemCache(MemCache aMemCache);
    
    public boolean openCache();
    
    public boolean existInCache(TileMapParams aParam);
    
    public void removeFromCache(TileMapParams aParams);
    
    public byte[] getDataFromCache(TileMapLayerWrapper aTileMapWrapper, 
                                   String aParamString, 
                                   String aTileID);
    
    boolean writeDataToCache(byte [][]aCacheData, 
                             TileMapParams []aParams, 
                             TileMapParams aTileIDParam, 
                             int aTotalSize, 
                             int aNbrOfImp, 
                             short aEmptyImp);
    
    
    public boolean updateLoadedNodes(TileMapLayerWrapperInterface wrapper, 
                                     Hashtable aLoadedWrapper);
    
    public void setTileMapFormatDesc(TileMapFormatDesc aTmfd);
    
    public void setVisible(boolean aVisible);
    
    public void purgeData();
    
    public void saveCache();

}
