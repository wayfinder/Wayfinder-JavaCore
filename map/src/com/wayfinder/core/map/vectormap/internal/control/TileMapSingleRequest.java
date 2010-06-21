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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.core.network.internal.ResponseCallback;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.URITool;

final class TileMapSingleRequest implements ResponseCallback {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapSingleRequest.class);
    
    
    private static final String URI = "/TMap/";
    
    private final String m_paramString;
    private final TileMapRequestListener m_tileMapReqlistener;
    
    private final TileMapNetworkHandler m_networkHandler;


    TileMapSingleRequest(TileMapNetworkHandler handler, String paramString, 
                   TileMapRequestListener listener) {
        m_networkHandler = handler;
        m_paramString = paramString;
        m_tileMapReqlistener = listener;
    }
    
    /**
     * Returns the connection URI that should be used for this request
     * 
     * @return The URI as a String
     */
    String getConnectionURI() {
        StringBuffer sb = new StringBuffer(URI);
        URITool.percentEncodeString(m_paramString, sb);
        return sb.toString();  
        //return URI + m_paramString;
    }
    
    //-------------------------------------------------------------------------
    // incoming
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.network.ResponseCallback#readResponse(java.io.InputStream, long)
     */
    public void readResponse(InputStream in, long length) throws IOException {
        byte []data;
        
        if(length == -1) {        
            final int CHUNKSIZE=2048;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4*CHUNKSIZE);        
            byte[] chunkbuf = new byte[CHUNKSIZE];
            int cnt = 0;
            do {
                int n = in.read(chunkbuf, 0, CHUNKSIZE);
                if (n != -1) {
                    baos.write(chunkbuf, 0, n);
                    cnt += n;
                }
                else {
                    break;
                }
            } while (true);
            
            data = baos.toByteArray();
            
        } else {
            data = new byte[(int)length];
            for (int bytesRead = 0; bytesRead < data.length;) {
                int read = in.read(data, bytesRead, (data.length - bytesRead));
                if(read == -1) {
                    throw new EOFException();
                }
                bytesRead += read;
            }
        }
//        BitBuffer buffer = new BitBuffer(data); 
//        String paramString = buffer.nextString();                
//        int nbrOfBytes = (int) buffer.nextInt();
//        byte []tiledata = buffer.nextByteArray(nbrOfBytes);
//        
//        if (paramString.equals(m_paramString)) {
//            m_tileMapReqlistener.requestReceived(paramString,tiledata, false);
//        } else {
//            if(LOG.isError()) {
//                LOG.warn("TileMapRequest.readResponse()", "bad response " + paramString + " instead of " +m_paramString);
//            }
//            m_tileMapReqlistener.requestFailed(new String[]{m_paramString});
//        }
        m_tileMapReqlistener.requestReceived(m_paramString, data, false);
        m_networkHandler.setHasOutgoingRequest(false);
        m_networkHandler.resetExponentialBackoff();
    }
    
    //-------------------------------------------------------------------------
    // errors
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.ResponseCallback#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(CoreError error) {
        if(LOG.isError()) {
            LOG.error("TileMapNetworkHandler.error()", "msg= "+error.getInternalMsg());
        }
        
        m_networkHandler.increaseExponentialBackoff();
        m_tileMapReqlistener.requestFailed(new String[]{m_paramString});
        m_networkHandler.setHasOutgoingRequest(false);
    }
    
    public String toString() {
        return "TileMapSingleRequest." + m_paramString;
    }

}
