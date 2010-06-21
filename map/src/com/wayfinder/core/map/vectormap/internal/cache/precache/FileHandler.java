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

package com.wayfinder.core.map.vectormap.internal.cache.precache;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.WFFileConnection;


public class FileHandler implements FileHandlerInterface {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(FileHandler.class);
    
    private String m_FileName;
    WFFileConnection m_fconn;
    private int m_totalRead;

    /**
     * Creates a new instance of FileHandler
     */
    public FileHandler(String fileName, boolean emulator, PersistenceLayer perLayer) {
        
        if(LOG.isInfo()) {
            LOG.info("FileHandler.FileHandler()", "try to open "+fileName);
        }

        m_totalRead = 0;

        m_FileName = fileName;
        try {
            startPeriod();
            m_fconn = perLayer.openFile(fileName);
            logPeriod("connect", 0);
        } catch (Exception ex) {
            if(LOG.isError()) {
                LOG.error("FileHandler.FileHandler()", ex);
            }
        }
        if (!m_fconn.exists()) {
            if(LOG.isError()) {
                LOG.error("FileHandler.FileHandler()", "File not found: "+fileName);
            }
        }
    }

    public boolean fileConnectionOk() {
        return m_fconn.exists();
    }

    /**
     * Returns the file name of the handler.
     */
    public String getFileName() {
        return m_FileName;
    }

    // -- loading startup
    private static byte[] loadBuffer = new byte[1024];//the header is 19 + 372
    private static WFFileConnection currentLoadFileConn;
    private static int loadBufferSize;

    /**
     * fill the buffer if the FileConnection has changed
     */
    private static void fillLoadBufferIfNeeded(WFFileConnection conn, int size)
            throws IOException {
        // safe check if the buffer is not big enough
        // if the initial buffer size was well choose this should never happen
        
        if (loadBuffer.length < size) {
            loadBuffer = new byte[size];
            currentLoadFileConn = null; // force reading
        }

        if (currentLoadFileConn != conn) {
            currentLoadFileConn = conn;
            DataInputStream is = null;
            try {
                startPeriod();
                is = conn.openDataInputStream();
                logPeriod("open", 0);
                // we assume that read method will actually read the whole buffer
                // if the size of the file is big enough
                startPeriod();
                loadBufferSize = is.read(loadBuffer);
                logPeriod("read", loadBufferSize);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }
    

    /**
     * Reads maxLength bytes from the file. Calls the listener when done. If
     * listener is NULL the call will by synchronous and the number of bytes
     * written will be returned.
     */
    public int read(byte[] bytes, int maxLength, SFDLoadableHeader header) {
        
        int n = 0;
        try {
            fillLoadBufferIfNeeded(m_fconn, m_totalRead + maxLength);
            n = Math.min(maxLength, loadBufferSize - m_totalRead);
            if (n > 0) {
                System.arraycopy(loadBuffer, m_totalRead, bytes, 0, n);
                m_totalRead += n;
            } else {
                n = 0;
            }
        } catch (IOException ex) {
            if(LOG.isError()) {
                LOG.error("FileHandler.read()", "fileName: "+m_FileName+" e= "+ex);
            }
        }
        
        if (n < maxLength) {
            if(LOG.isWarn()) {
                LOG.warn("FileHandler.read()", m_FileName + " read less " + n + "<" + maxLength);
            }
        }
        header.readDone(n);
        return n;
    }

    //-- during runtime
    private static final boolean HEADER_BUFFER = false;
    //later allocation only if the prestored tile exist and are used   
    private static byte[] headerBuffer; 
    private static int headerBufferSize;
    
    private static WFFileConnection currentFileConn;
    private static InputStream currentFileIs;
    private static long currentPosition;

    private static void closeCurrentFileIs() {
        try {
            if (currentFileIs != null) {
                currentFileIs.close();
                currentFileIs = null;
            }
        } catch (IOException ex) {
            if(LOG.isError()) {
                LOG.error("FileHandler.closeCurrentFileIs()", "ex= "+ex);
            }
        } 
    }
    
    private static void openAndBufferIfNeeded(WFFileConnection fc) throws IOException {
        if (currentFileConn != fc) {
            closeCurrentFileIs();
            currentFileConn = fc;
            startPeriod();
            currentFileIs = fc.openDataInputStream();
            // we assume that read method will actually read the whole buffer
            // if the size of the file is big enough
            logPeriod("open", 0);
            
            if (HEADER_BUFFER) {

                if (headerBuffer == null) {
                    headerBuffer = new byte[256 * 1024]; //maxim ~ 202065
                }
                startPeriod();
                headerBufferSize = currentFileIs.read(headerBuffer);
                currentPosition = headerBufferSize;
                logPeriod("read", headerBufferSize);
            }
        }
    }
    
    private static boolean seekCurrentFile(long position) throws IOException {
        if (currentPosition < position) {
            startPeriod();
            long skip = currentFileIs.skip(position - currentPosition);
            currentPosition += skip;
            logPeriod("skip", skip);
        } else if (currentPosition > position){
            //file dosen't allow 2 input stream open 
            closeCurrentFileIs();
            //reopen the input
            startPeriod();
            
            currentFileIs = currentFileConn.openDataInputStream();
            logPeriod("open", 0);
            startPeriod();
            currentPosition = currentFileIs.skip(position);
            logPeriod("skip", position);
        }
        return (currentPosition == position);
    }

     private static int readFromCurrent(WFFileConnection fc, byte[] b, int size, int position) throws IOException {
         openAndBufferIfNeeded(fc);
         int count = 0;
         if (HEADER_BUFFER) {
             if (position < headerBufferSize) {
                 //we can read something
                 count = Math.min(size, headerBufferSize - position);
                 System.arraycopy(headerBuffer, position, b, 0, count);
             } //else nothing to read from the buffer 
             
             if (headerBufferSize < headerBuffer.length) {
                 if(LOG.isWarn()) {
                    LOG.warn("FileHandler.readFromCurrent()", "The file is smaller than buffer");
                }
                 
                 //we have reached end of file
                 return count;//no point to try to read more  
             } 
         }
         
         if (count < size) {
             //fill directly from the from the file the entire buffer or the rest
             //remaining 
             if (seekCurrentFile(position + count)) {
                startPeriod();
                int n = currentFileIs.read(b, count, size - count);
                logPeriod("read", n);
                count += n;
                currentPosition += n;
            } else {
                if(LOG.isWarn()) {
                    LOG.warn("FileHandler.readFromCurrent()", "Could not seek the file is too small");
                }
            }
         } 
         return count;
     }
     


    /**
     * Reads maxLength bytes from the file. Calls the listener when done. If
     * listener is NULL the call will by synchronous and the number of bytes
     * written will be returned.
     */
    public int read(byte[] bytes, int maxLength,
            FileHandlerListener fileListener, int skip) {

        int n = 0;
        try {
             n = readFromCurrent(m_fconn, bytes, maxLength, skip);
        } catch (IOException ex) {
            if(LOG.isError()) {
                LOG.error("FileHandler.read()", "e= "+ex);
            }
        }
        
         if (n == 0) {
             if(LOG.isWarn()) {
                LOG.warn("FileHandler.read()",  m_FileName+" could not seek to " + skip );
            }
         } else if (n < maxLength) {
             if(LOG.isWarn()) {
                LOG.warn("FileHandler.read()", m_FileName+" read less " + n + "<" + maxLength);
            }
         }      
        fileListener.readDone(n);
        return n;
    }

    
    /**
     * this is actually never called
     */
    public void close() {
        try {
            if (m_fconn == currentFileConn) {
                //not very elegant but we need to state that the currentFileConn
                //cannot be used anymore because has been closed
                //also the currentFileIs should be closed.
                closeCurrentFileIs();
                currentFileConn = null;
            } 
            
            if (m_fconn == currentLoadFileConn) {
                //we need to state that the currentLoadFileConn cannot be used 
                //anymore because has been closed 
                currentLoadFileConn = null;
            }
            
            m_fconn.close();
        } catch (IOException ex) {
            if(LOG.isError()) {
                LOG.error("FileHandler.close()", "fileName= "+m_FileName+" e= "+ex);
            }
        }
    }

    /**
     * Returns the size of the file.
     */
    public int getFileSize() {
        int size = (int) m_fconn.fileSize();
        return size;
    }

    static long time = 0;

    private static final void startPeriod() {
        if(LOG.isDebug()) {
            time = System.currentTimeMillis();
        }        
    }

    private static final void logPeriod(String msg, long position) {
        if(LOG.isDebug()) {
            long duration = System.currentTimeMillis() - time;
            LOG.debug("FileHandler.logPeriod()", "File."  + msg + " " + position + " " + duration + "ms");
        }        
    }

}
