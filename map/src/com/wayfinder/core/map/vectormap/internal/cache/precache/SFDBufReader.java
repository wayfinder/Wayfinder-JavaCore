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
package com.wayfinder.core.map.vectormap.internal.cache.precache;

import com.wayfinder.core.map.util.BitBuffer;

/**
 *   This class reads the buffer offset and size and then
 *   it reads the buffer itself. It has some similarities
 *   with SFDFileIndexor, so they should really share some
 *   code....
 */
public class SFDBufReader implements FileHandlerListener{
    
    FileHandler m_fileHandler;
    
/// Listener to inform when buffer reading is done or has failed.
    private SFDBufReaderListener m_listener;
    
/// Reference to file header
    SFDLoadableHeader m_header;
    
    /// Nothing is to be done
    private static final int IDLE =  0;
    /// A read of the offset has been requested
    private static final int READING_BUF_OFFSET = 1;
    /// A read of the string itself has been requested.
    private static final int READING_BUF        = 2;
    /// Permanent error has occured
    private static final int PERMANENT_ERROR    = 400;
    
    
    /// The state of this object.
    private int m_state;
    
    
/// Buffer to read two buffer sizes into.
    BitBuffer m_bufOffsetBuf;
/// Buffer which is used to hold the map
    BitBuffer m_readBuffer;
    
    
    /**
     *   Creates a new SFDBufReader.
     *   @param fh FileHandler to use. Can be shared, but should
     *             not be used simultaneously with in other places.
     */
    public SFDBufReader(SFDLoadableHeader header, FileHandler fh, SFDBufReaderListener listener ) {
        m_header = header;
        m_bufOffsetBuf = new BitBuffer(8);
        m_listener = listener;
        m_fileHandler = fh;
        m_state = IDLE;
        m_readBuffer = null;
    }
    
    
    /**
     * Starts the SFDBufReader using a strIdx
     */
    public void start( int strNbr ){
        startAbsolute( strNbrToBufIdxOffset( strNbr ) );
    }
    
    /**
     * Starts the SFDBufReader using an absolute offset to the lengths
     */
    public void startAbsolute( int offset ){
        
        if ( m_state == PERMANENT_ERROR ) {
            // Cannot recover from this.
            m_listener.bufferRead( null );
            return;
        }
        m_state = READING_BUF_OFFSET;
        // Read it into the pos and length buffer
        
        m_fileHandler.read( m_bufOffsetBuf.getByteArray(),m_bufOffsetBuf.getBufferSize(), this , offset);
        // Started
    }
    
    /**
     * Called by fileListener when read is done.
     */
    public void readDone( int nbrRead ){
        
        if ( m_state == READING_BUF_OFFSET ) {
            // Will read buf.
            m_state = READING_BUF;
            
            // Read from the start of our buf
            m_bufOffsetBuf.reset();
            // Read offset of the buffer
            int bufOffset =(int) m_bufOffsetBuf.nextInt();
            // Find out the offset of the next buffer and calculate length
            int bufLength = (int)m_bufOffsetBuf.nextInt() - bufOffset;
            // This should really not happen, but you never know
            if ( bufLength == 0 ) {
                m_state = IDLE;
                m_listener.bufferRead( null );
                return;
            }
            
            // Create buffer
            m_readBuffer = new BitBuffer( bufLength );
            // And read
            m_fileHandler.read(m_readBuffer.getByteArray(),m_readBuffer.getBufferSize(),this,bufOffset );
            
        } else if ( m_state == READING_BUF ) {
            // Done and done
            m_state = IDLE;
            // Cannot use the member variable anymore since we might
            // get a callback from m_listener
            byte[]tmp = new byte[m_readBuffer.getByteArray().length];
            System.arraycopy(m_readBuffer.getByteArray() ,0, tmp, 0, m_readBuffer.getByteArray().length );
            BitBuffer tmpBuf = new BitBuffer(tmp);
            // All is set - call listener.
            m_listener.bufferRead( tmpBuf );
        }
        
    }
    
    /// Called by fileListener when write is done.
    public void writeDone( int nbrWritten ) {
        // Should not happen.
    }
    
    /// Converts string number to buffer index offset.
    private int strNbrToBufIdxOffset(int strNbr){
        //System.out.println("strnbr: "+ strNbr);
        //System.out.println("m_header.getBufIdxStartOffset(): "+ m_header.getBufIdxStartOffset());
        return m_header.getBufIdxStartOffset() + strNbr * 4;
    }
    
}

