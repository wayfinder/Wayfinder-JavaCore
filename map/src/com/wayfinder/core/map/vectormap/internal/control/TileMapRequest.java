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
import java.io.OutputStream;
import java.util.Vector;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.core.network.internal.PostContent;
import com.wayfinder.core.network.internal.ResponseCallback;
import com.wayfinder.core.network.internal.xscoder.XsDataCodedInputStream;
import com.wayfinder.core.network.internal.xscoder.XsDataCodedOutputStream;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.io.MaybeGZIPInputStream;
import com.wayfinder.pal.util.UtilFactory;

final class TileMapRequest implements ResponseCallback, PostContent {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(TileMapRequest.class);
    
    /*
     * Even when GZIP is activated, only outgoing requests that are larger
     * than this amount will be compressed since compression actually may
     * increase the size of the data due to the addition of headers and a
     * trailer.
     * <p>
     * In reality, this will mean that the only map requests that actually will
     * be compressed are the route predictor requests. 
     */
    private static final int GZIP_CUTOFF_BYTES = 500;
    
    // For now the map follows the encoding used by the XML protocol
    private static final boolean ALLOW_XSGZIP = InternalNetworkInterface.XML_USE_XS_ENCODING;
    
    private static final String URI = "/TMap?";
    private static final String XSURI = "/XSMap?";
    
    private final Vector m_paramStrings;
    private final TileMapRequestListener m_tileMapReqlistener;
    
    // The BitBuffer that holds the parameter strings that should be requested. 
    private final BitBuffer m_bufferToSend;
    
    // True if we should use XSGzip when sending request. 
    private final boolean m_useXSGZIP; 
    private final UtilFactory m_utilFactory;
    private final TileMapNetworkHandler m_networkHandler;


    TileMapRequest(TileMapNetworkHandler handler, Vector paramStrings, 
                   TileMapRequestListener listener, UtilFactory factory) {
        m_networkHandler = handler;
        m_paramStrings = paramStrings;
        m_tileMapReqlistener = listener;
        m_utilFactory = factory;
        m_bufferToSend = prepareOutgoingBitBuffer(paramStrings);
        m_useXSGZIP = ALLOW_XSGZIP && 
                   (m_bufferToSend.getCurrentByteOffset() >= GZIP_CUTOFF_BYTES);
    }
    
    
    /*
     * Prepare the outgoing BitBuffer with parameter strings that
     * should be sent to the server.  
     */
    private static BitBuffer prepareOutgoingBitBuffer(Vector strings) {
        final int size = strings.size();
        
        BitBuffer buffer = new BitBuffer(64+ 64 * size);
        buffer.writeNextBits(0,32);;//problably not used at all keep it 0
        //maximum size of the reply take in consideration that a TMFD is more 
        //than 10k at this moment and it will increase
        buffer.writeNextBits(20480,32);//20k was 10k
        
        for(int i=0; i<size; i++) {
            String paramString = (String) strings.elementAt(i);            
            buffer.writeNextString(paramString);
        }     
        return buffer;
    }
    
    
    /**
     * Returns the connection URI that should be used for this request
     * 
     * @return The URI as a String
     */
    String getConnectionURI() {
        return m_useXSGZIP ? XSURI : URI;
    }
    
    
    /**
     * Check to see if this request will be done using GZIP and the XS protocol
     * 
     * @return true if and only if the request will use GZIP/XS
     */
    boolean usesXSGZIP() {
        return m_useXSGZIP;
    }
    
    
    /**
     * Returns the size of the internal BitBuffer
     * 
     * @return The size of the buffer
     */
    int getBufferSize() {
        return m_bufferToSend.getCurrentByteOffset();
    }
    
    
    //-------------------------------------------------------------------------
    // outbound
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.PostContent#getContentLength()
     */
    public long getContentLength() {
        return m_useXSGZIP ? -1 : m_bufferToSend.getCurrentByteOffset();
    }
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.network.internal.PostContent#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream outstream) throws IOException {
        if(m_useXSGZIP) {
            outstream = m_utilFactory.openGZIPOutputStream(
                    XsDataCodedOutputStream.createStream(outstream, true));
        }        
        outstream.write(m_bufferToSend.getByteArray(), 0, 
                        m_bufferToSend.getCurrentByteOffset());
        if(m_useXSGZIP) {
            // must close to transmit the gzip footer >_<
            outstream.close();
        }
    }
    
    
    
    //-------------------------------------------------------------------------
    // incoming
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.network.ResponseCallback#readResponse(java.io.InputStream, long)
     */
    public void readResponse(InputStream in, long length) throws IOException {
        int len = (int)length;
        
        if(m_useXSGZIP) {
            MaybeGZIPInputStream inStream 
                     = new MaybeGZIPInputStream(new XsDataCodedInputStream(in));
            if(inStream.isGzip()) {
                in = m_utilFactory.openGZIPInputStream(inStream);
            } else {
                in = inStream;
            }
            len = -1;
        }
        
        internalReadResponse(in, len);
        
        if(m_paramStrings.size() > 0) {
            if(LOG.isWarn()) {
                LOG.warn("TileMapRequest.readResponse()", "no reponse for:" + m_paramStrings);
            }
            reportFailedRequests(m_paramStrings);
        }
        m_networkHandler.setHasOutgoingRequest(false);
        m_networkHandler.resetExponentialBackoff();
    }
    
    
    /*
     * Read the response from the InputStream, parsed maps are send 
     * back to the MapLoader class future processing like being saved
     * in the cache and/or unpacked so that it can be drawn on the screen. 
     */
    private void internalReadResponse(InputStream in, int length) throws IOException {
        
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
            data = new byte[length];
            for (int bytesRead = 0; bytesRead < data.length;) {
                int read = in.read(data, bytesRead, (data.length - bytesRead));
                if(read == -1) {
                    throw new EOFException();
                }
                bytesRead += read;
            }
        }
        
               
        /* Split up the response into one or more TileMaps. */
        BitBuffer buffer = new BitBuffer(data);        
        int readBytes = 0;
        while(readBytes < buffer.size()){                    
            String paramString = buffer.nextString();                
            int nbrOfBytes = (int) buffer.nextInt();
            byte []tiledata = buffer.nextByteArray(nbrOfBytes);
            readBytes += (nbrOfBytes + paramString.length() + 5);
            m_tileMapReqlistener.requestReceived(paramString,tiledata, false);
            m_paramStrings.removeElement(paramString);
            
            if(LOG.isTrace()) {
                LOG.trace("TileMapNetworkHandler.internalReadResponse()", 
                        paramString+" loaded from server");
            }
        }        
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
        reportFailedRequests(m_paramStrings);
        m_networkHandler.setHasOutgoingRequest(false);
    }
    
    
    /*
     * There are common that not all maps that has been requested arrives
     * from the server in the replay. Then the maps needs to be reported 
     * back as failed request so that they can be re-send if they are still
     * visible on the screen. 
     * 
     * @param aRequests
     */
    private void reportFailedRequests(Vector requests) {        
        final int nbrFailed = requests.size();
        if (nbrFailed > 0) {
            String[] str = new String[nbrFailed];
            requests.copyInto(str);
            m_tileMapReqlistener.requestFailed(str);
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        final int size = m_paramStrings.size();
        
        for(int i=0; i<size; i++) {
            sb.append('"');
            sb.append((String) m_paramStrings.elementAt(i));
            sb.append('"');
            sb.append(',');
        }     
        return sb.toString();
    }

}
