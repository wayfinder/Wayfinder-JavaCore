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


public class SFDFileIndexor extends SFDIndexor implements FileHandlerListener{
    
    /// File handler, i.e. a file.
    private FileHandler m_fileHandler;
    
    /// Object to call when done.
    private SFDFileIndexorListener m_listener;
    
    /// The File Header.
    private SFDLoadableHeader m_header;
    
    /// Nothing is to be done
    public static final int IDLE = 0;
    /// A read of the offset has been requested
    public static final int READING_STR_OFFSET = 1;
    /// A read of the string itself has been requested.
    public static final int READING_STR = 2;
    /// Permanent error has occured
    public static final int PERMANENT_ERROR = 400;
    
    /// The state of this object.
    private int m_state;
    
    /// The maximum string size in the cache.
    private int m_minBufSize;
    
    /// Buffer to read the string offset into.
    private BitBuffer m_strOffsetBuf;
    /// Current string length
    private int m_curStrLength;
    
    
    public SFDFileIndexor(FileHandler fh, SFDFileIndexorListener listener,SFDLoadableHeader header ) {
        super();
        m_strOffsetBuf = new BitBuffer(2 * header.getStrIdxEntrySizeBits() / 8);
        m_fileHandler   = fh;
        allocStr( header.maxStringSize() + 1 );
        m_minBufSize = header.maxStringSize() + 1;
        m_listener = listener;
        m_header = header;
        m_state = IDLE;
    }
    
    private void allocStr( int size ) {
        m_str = new byte[size];
        m_strAllocSize = size;
    }
    /**
     *    Converts a string number to an offset in the file.
     */
    public int strNbrToOffset( int idx ){
        return (m_header.getStrIdxEntrySizeBits() / 8) * idx +
                m_header.getStrIdxStartOffset();
    }
    /**
     *    Starts reading if necessary.
     *    Calls indexorDone on the listener if it is already done.
     */
    public boolean start(){
        if ( m_stringRead ) {
            m_listener.indexorDone( this, 0 );
            return false;
        }
        
        if ( m_state == PERMANENT_ERROR ) {
            m_listener.indexorDone( this, -1 );
        }
        
        // Realloc the string buffer if needed
        if ( m_strAllocSize < m_minBufSize ) {
            allocStr( m_minBufSize + 1 );
        }
        
        m_state = READING_STR_OFFSET;
        
        // Read it into the buffer.
        m_fileHandler.read( m_strOffsetBuf.getByteArray(),m_strOffsetBuf.getBufferSize(),
                this, strNbrToOffset( m_strNbr ) );
        
        return true;
    }
    
    public void writeDone(int r){};
    
    /// Called by FileHandler when read is done
    public void readDone( int nbrRead ){
        
        if ( nbrRead <= 0 ) {
            m_state = PERMANENT_ERROR;
            m_listener.indexorDone( this, -1 );
            return;
        }
        
        if ( m_state == READING_STR_OFFSET ) {
            // Will read string
            m_state = READING_STR;
            // Start at beginning.
            m_strOffsetBuf.reset();
            // Read offset of our string
            m_strOffset =(int) m_strOffsetBuf.nextInt();
            // The length of the string is the difference between
            // this and the next one.
            m_curStrLength = (int)m_strOffsetBuf.nextInt() - m_strOffset;
            // Seek to that pos
            // Read a string
             
             for(int i =0;i<m_str.length;i++){
                 m_str[i]=0;
             }
            m_fileHandler.read(m_str, m_curStrLength,this, m_header.getStrDataStartOffset() + m_strOffset  );
           
            
        } else if ( m_state == READING_STR ) {
            m_stringRead = true;
            // It is done.
            m_state = IDLE;
            // Inform the listener
            m_listener.indexorDone( this, 0 );
        }
        
    }
    
    
    /**
     *    Sets new index to read.
     */
    public void setStrNbr( int idx ) {
        if ( m_strNbr != idx ) {
            m_strNbr = idx;
            m_strOffset = Integer.MAX_VALUE;
            m_stringRead = false;
        }
    }
    
}


