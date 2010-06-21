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

package com.wayfinder.core.map.vectormap.internal.route;

import com.wayfinder.core.map.vectormap.internal.control.TileMapLayerWrapperInterface;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

/**
 * Class that holds the information for one route tile for one layer.  
 * 
 */
public class RouteTileWrapper implements TileMapLayerWrapperInterface {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(RouteTileWrapper.class);
	
	private int iNumberOfImpotrances;
	private int iNumberReceived;
	private TileMapParams iTileIDParam;
	
	public RouteTileWrapper(int aNumberOfImportances, TileMapParams aTileIDParam) {
		iNumberOfImpotrances = aNumberOfImportances;
		iNumberReceived = 0;
		iTileIDParam = aTileIDParam;
		
		iData = new byte[iNumberOfImpotrances*2][];
    	iParamsToCache = new TileMapParams[iNumberOfImpotrances*2];    	
	}
	
	public int getTileIndexLat() {
	    return iTileIDParam.getTileIndexLat();
	}
	
	public int getTileIndexLon() {
	    return iTileIDParam.getTileIndexLon();
	}
	
	public int getLayerID() {
	    return iTileIDParam.getLayerID();
	}
	
	public int getDetailLevel() {
	    return iTileIDParam.getDetailLevel();
	}
	
	int getNumberOfImportances() {
		return iNumberOfImpotrances;
	}
	
	TileMapParams getTileIDParam() {
	    return iTileIDParam;
	}
	
	void increaseNumberReceived() {
		iNumberReceived++;
	}
	
	boolean isDone(boolean aAlwaysFetchStrings) {
		
		int nbrImp = iNumberOfImpotrances;
		
		if(aAlwaysFetchStrings)
			nbrImp = nbrImp*2;
		
		return (nbrImp==iNumberReceived);
	}
	
	
	private int iTotalCacheSize=0;    
    private byte [][]iData;
    private TileMapParams []iParamsToCache;
    private int iNbrOfImpToCache = 0;
    
    void addData(TileMapParams aParam, byte []aData) {        
        synchronized(iData) {
            
            if(aParam.getTileMapType() == TileMapParams.MAP) {
            
                
                if(LOG.isError()) {
                    if(iData[aParam.getImportance()] != null) {
                        LOG.error("TileMapLayerWrapper.addData()", "Add a imp that isn't empty:" +
                                " paramString= "+aParam.getAsString()+
                                " imp= "+aParam.getImportance());
                    }
                }
                
                iData[aParam.getImportance()] = aData;
                iParamsToCache[aParam.getImportance()] = aParam;
            } else {
                
                if(LOG.isError()) {
                    if(iData[iNumberOfImpotrances+aParam.getImportance()] != null) {
                        LOG.error("TileMapLayerWrapper.addData()", "Add a imp that isn't empty:" +
                                " paramString= "+aParam.getAsString()+
                                " imp= "+aParam.getImportance());                   
                    }
                }
                
                iData[iNumberOfImpotrances+aParam.getImportance()] = aData;
                iParamsToCache[iNumberOfImpotrances+aParam.getImportance()] = aParam;
            }           
            iTotalCacheSize += aData.length;
            iNbrOfImpToCache++;         
        }
    }
    
    byte [][]getData() {
    	return iData;
    }
    
    TileMapParams []getParamsToCache() {
    	return iParamsToCache;
    }
    
    int getTotalSize() {
    	return iTotalCacheSize;
    }
    
    int getNbrOfImpToCache() {
    	return iNbrOfImpToCache;
    }
	
    void clear() {
    	iData = null;
    	iParamsToCache = null;
    }
}
