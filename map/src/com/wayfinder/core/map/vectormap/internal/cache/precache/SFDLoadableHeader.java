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
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.map.vectormap.internal.process.RouteID;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.pal.persistence.PersistenceLayer;

public class SFDLoadableHeader implements FileHandlerListener {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(SFDLoadableHeader.class);
    
    private static final int INITIAL_HEADER_SIZE =  19;
    private int m_nbrBytesToRead;
//    private short m_version;
    private int m_headerSize;
    private int m_fileSize;
    private int m_creationTime;
    private short m_stringsAreNullTerminated;
    private short m_maxStringSize;
    private short[] m_initialCharacters;
    private RouteID[] m_routeIDs;
    
    private int m_strIdxEntrySizeBits;
    private int m_strIdxStartOffset;
    private int m_nbrStrings;
    private int m_strDataStartOffset;
    private int m_bufferIdxStartOffset;
//    private int m_bufferDataStartOffset;
    private short m_readDebugParams;
    private TileCollectionNotice[] m_tileCollection;
    
    
    private String m_name;
    private byte[] m_readBuffer;
    //findbugs: not used at this time
//    private Object m_xorBuffer;
    
    /// The file handler
    private FileHandler m_fileHandler;
    /// The listener that should be informed when the header has been loaded.
    private SFDLoadableHeaderListener m_listener;
    
    
    private boolean m_readOK;
    
    /// Encryption type.
    private static final short NO_ENCRYPTION        = 0;
    //never called
//    private static final short UID_ENCRYPTION     = 1;
//    private static final short WAREZ_ENCRYPTION       = 2;
    
    private short m_encryptionType;
    
    /// The state.
    private static final int NOTHING_LOADED         = 0;
    private static final int LOADED_INITIAL_HEADER  = 1;
    private static final int LOADED_ALL_HEADER      = 2;
    private static final int FAILED_TO_LOAD         = 404;
    
    private int m_state;
    
    /** Creates a new instance of SFDLoadableHeader */
    public SFDLoadableHeader() {
        m_state = NOTHING_LOADED;
        m_nbrBytesToRead = INITIAL_HEADER_SIZE;
    }
    
    public boolean isValid(){
        return m_state != FAILED_TO_LOAD;
    }
    /**
    *    Load more data if necessary or 
    *    notify the listener that the header has been loaded.
    */

    private void innerLoad(){
        if ( m_nbrBytesToRead > 0 && m_fileHandler.fileConnectionOk()) {
            m_readBuffer = new byte[ m_nbrBytesToRead ];
            m_fileHandler.read( m_readBuffer, m_nbrBytesToRead, this );
        } else {
            m_readBuffer = null;
            m_readOK = ( m_state == LOADED_ALL_HEADER );
            if(!m_readOK){
                m_state = FAILED_TO_LOAD;
            }
            m_listener.loadDone( this, m_fileHandler );
        }
    }
    
    /**
     *    Loads the SFDLoadableHeader.
     *    @param   file     The file handler pointing to the
     *                      single file cache to load.
     *    @param   listener Listener that will be notified when the header
     *                      has been loaded.
     */
    public void load( String  fileName, SFDLoadableHeaderListener listener, PersistenceLayer perLayer){
        m_fileHandler = new FileHandler( fileName ,Utils.EMULATOR, perLayer);
        m_listener = listener;
        innerLoad();
    }
    
   /**
    *    Load the initial part of the header.
    */
    private void loadInitialHeader( BitBuffer buf ) {
        if (!m_fileHandler.fileConnectionOk()) {
            m_state = FAILED_TO_LOAD;
            m_nbrBytesToRead = 0;
            innerLoad();
            return;
        }

        // Load initial header.
        String str = buf.nextString();
        if ( !str.equals("storkafinger")) {
            m_state = FAILED_TO_LOAD;
            m_nbrBytesToRead = 0;
            innerLoad();
            return;
        }
        
        // Version
        buf.nextByte();
        
        // Encryption type.
        m_encryptionType = buf.nextByte();
        m_encryptionType = NO_ENCRYPTION;
        // Set the right xorbuffer depending on the encryption type.
        switch ( m_encryptionType ) {
            case ( NO_ENCRYPTION ) :
//                m_xorBuffer = null;
                break;
        /*      case ( UID_ENCRYPTION ) :
                                //MC2_ASSERT( m_uidXorBuffer != NULL );
                                m_xorBuffer = m_uidXorBuffer;
                                break;
                        case ( WAREZ_ENCRYPTION ) :
                                //MC2_ASSERT( m_warezXorBuffer != NULL );
                                m_xorBuffer = m_warezXorBuffer;
                                break;*/
        }
        
        // Header size.
        m_headerSize = (int)buf.nextInt();  //long
        
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadInitialHeader()", " header size: "+m_headerSize);
        }
        
        m_nbrBytesToRead = (int)m_headerSize;
        
        // Load the rest.
        m_state = LOADED_INITIAL_HEADER;
        innerLoad();
    }
    
    /**
    *    Get number of bytes to read next.
    *    Returns zero when there is nothing left to read.
    */
   /*private int getNbrBytesToRead(){
       
   }*/
    
    
   /**
    *    Load the remaining part of the header.
    */
    private void loadRemainingHeader( BitBuffer buf ) {
        // File size.
        m_fileSize = (int)buf.nextInt(); //ba long
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "File Size: "+m_fileSize);
        }
        // The name.
        m_name = buf.nextString();
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "Name: "+m_name);
        }
        
        // Check the file size.
        if ( m_fileHandler.getFileSize() != m_fileSize ) {
            m_state = FAILED_TO_LOAD;
            m_nbrBytesToRead = 0;
            innerLoad();
            return;
        }
        
        // Creation time.
        m_creationTime = (int)buf.nextInt(); //ba long
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "Creation Time: "+m_creationTime);
        }        
        // Null terminated strings?
        m_stringsAreNullTerminated = buf.nextByte(); //ba byte
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "Strings null terminated: "+m_stringsAreNullTerminated);
        }
        // Longest length of string.
        m_maxStringSize = buf.nextByte(); //ba byte
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "max string size "+m_maxStringSize);
        }
        
        // Nbr initial chars.
        short nbrInitialChars = buf.nextByte();  //ba byte
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "nbr initial chars: "+nbrInitialChars);
        }
        m_initialCharacters = new short[nbrInitialChars];
        // Initial chars.
        {for ( short b = 0; b < nbrInitialChars; ++b ) {
             m_initialCharacters[ b ] = buf.nextByte();  //BA byte
         }}
        
        // Nbr route ids.
        short nbrRouteIDs = buf.nextByte();
        if(LOG.isInfo()) {
            LOG.info("SFDLoadableHeader.loadRemainingHeader()", "Number of route IDs: "+nbrRouteIDs);
        }        
        m_routeIDs = new RouteID[nbrRouteIDs];
        for ( byte b = 0; b < nbrRouteIDs; b++ ) {
             int id = (int)buf.nextInt();
             int creationTime = (int)buf.nextInt();
             m_routeIDs[b] = new RouteID( id, creationTime );
         }
        
        // Number of bits for the string index.
        m_strIdxEntrySizeBits = (int)buf.nextInt();
        
        // Position for the start of strings index.
        m_strIdxStartOffset = (int)buf.nextInt();
        
        // Number strings.
        m_nbrStrings =(int) buf.nextInt();
        
        // Position for the start of string data.
        m_strDataStartOffset =(int) buf.nextInt();
        
        // Position for the start of the buffers index.
        m_bufferIdxStartOffset =(int) buf.nextInt();
        
        // Position for the start of the buffer data.
        buf.nextInt();
        
        // If to read debug param strings for the multi buffers.
        m_readDebugParams = buf.nextByte();
        
        // The tile collections.
        char nbrCollections = buf.nextShort();
        
        m_tileCollection = new TileCollectionNotice[ nbrCollections ];
        for ( int i = 0; i < nbrCollections; i++ ) {
            m_tileCollection[ i ] = new TileCollectionNotice();
            m_tileCollection[ i ].load( buf );
        }
        
        // All is now loaded.
        m_state = LOADED_ALL_HEADER;
        m_nbrBytesToRead = 0;
        innerLoad();
    }
    
    public void writeDone(){
        
        
    }
    
    public void writeDone(int i){
        
        
    }
    

   /**
    *    Handle that the filehandler has finished reading.
    */
    public void readDone( int nbrRead ) {
        
        if ( nbrRead == -1 ) {
            m_readOK = false;
            m_listener.loadDone( this, m_fileHandler );
            return;
        }
        BitBuffer buf = new BitBuffer( m_readBuffer );
        switch ( m_state ) {
            case ( NOTHING_LOADED ) :
                loadInitialHeader( buf );
                return;
            case ( LOADED_INITIAL_HEADER ) :
                loadRemainingHeader( buf );
                return;
            default:
                if(LOG.isError()) {
                    LOG.error("SFDLoadableHeader.readDone()", "Invalid state: "+m_state);
                }
        }
    }
    
    //fr�n SFDHEADER
    
    public int getStrIdxStartOffset(){
        return m_strIdxStartOffset;
    }
    public int getStrIdxEntrySizeBits(){
        return m_strIdxEntrySizeBits;
    }
    
    
    
    public int getNbrStrings(){
        return m_nbrStrings;
    }
    public int getStrDataStartOffset(){
        return m_strDataStartOffset;
    }
    public int getBufIdxStartOffset(){
        return m_bufferIdxStartOffset;
    }
    public boolean maybeInBinaryCache(TileMapParams params ){
        
        for ( int i = 0; i < m_initialCharacters.length;i++ ) {
    
            if ( params.getAsString().charAt(0) == m_initialCharacters[i] ) {  //STORT B, STORT D
                // Could be
                return true;
            }
        }
        return false;
    }
    
    public boolean maybeInCache(TileMapParams params ){
        if (TileMapParamTypes.isMap(params.getAsString()) ) {
            int offset = getMultiBufferOffsetOffset( params );
            if (offset >= 0  ) {
                return true;
            }
        }
        return maybeInBinaryCache( params );
    }
    
    public int maxStringSize(){
        return m_maxStringSize;
    }
    
    public boolean stringsAreNullTerminated(){
        if(m_stringsAreNullTerminated == 0){
            return false;
        }else{
            return true;
        }
    }
    
    public int getMultiBufferOffsetOffset(TileMapParams param ){
        
        for ( int i = 0; i < m_tileCollection.length; i++ ) {
            int offset = m_tileCollection[ i ].getOffset( param );
            if ( offset >= 0 ) {
                return offset;
            }
        }
        return -1;
    }
    
    
    public String getName(){
        return m_name;
    }
    
    public int[] getImportanceRange( TileMapParams param ){
        
        for ( int i = 0; i < m_tileCollection.length; i++ ) {
            int[] range = m_tileCollection[ i ].getImpRange( param );
            if ( range != null ) {
                return range;
            }
        }
        return null;
    }
    
    public boolean readDebugParams(){
        if(m_readDebugParams==1)
            return true;
        else
            return false;
    }
    
    public int getCreationTime(){
        return m_creationTime;
    }
    
    
    
    
}
/*
 
#include "config.h"
 
#include "SFDLoadableHeader.h"
#include "SharedBuffer.h"
#include "XorFileHandler.h"
#include "RouteID.h"
#include "SFDLoadableHeaderListener.h"
#include "TileCollectionNotice.h"
 
#define INITIAL_HEADER_SIZE 19
 
 
SFDLoadableHeader::SFDLoadableHeader( const SharedBuffer* uidXorbuffer,
                                                                          const SharedBuffer* warezXorbuffer )
          : SFDHeader(),
                m_state( nothing_loaded ),
                m_nbrBytesToRead( INITIAL_HEADER_SIZE ),
                m_xorBuffer( NULL ),
                m_uidXorBuffer( uidXorbuffer ),
                m_warezXorBuffer( warezXorbuffer ),
                m_listener( NULL ),
                m_readBuffer( NULL ),
                m_fileHandler( NULL )
{
 
}
 
SFDLoadableHeader::~SFDLoadableHeader()
{
   delete m_fileHandler;
   delete[] m_readBuffer;
}
 
void
SFDLoadableHeader::innerLoad()
{
   if ( m_nbrBytesToRead > 0 ) {
 
          delete [] m_readBuffer;
          m_readBuffer = new uint8[ m_nbrBytesToRead ];
 
          m_fileHandler->read( m_readBuffer, m_nbrBytesToRead, this );
   } else {
          delete [] m_readBuffer;
          m_readBuffer = NULL;
          m_readOK = ( m_state == loaded_all_header );
          m_listener->loadDone( this, m_fileHandler );
   }
}
 
void
SFDLoadableHeader::load( FileHandler* file, SFDLoadableHeaderListener* listener )
{
   delete m_fileHandler;
   m_fileHandler = new XorFileHandler( file );
   m_listener = listener;
   innerLoad();
}
 
void
SFDLoadableHeader::loadInitialHeader( SharedBuffer& buf )
{
   // Load initial header.
   const char* str = buf.readNextString();
   if ( strcmp( str, "storkafinger" ) != 0 ) {
          m_state = failed_to_load;
          m_nbrBytesToRead = 0;
          innerLoad();
          return;
   }
 
   // Version
   m_version = buf.readNextBAByte();
 
   // Encryption type.
   m_encryptionType = encryption_t( buf.readNextBAByte() );
 
   // Set the right xorbuffer depending on the encryption type.
   switch ( m_encryptionType ) {
          case ( no_encryption ) :
                 m_xorBuffer = NULL;
                 break;
          case ( uid_encryption ) :
                 MC2_ASSERT( m_uidXorBuffer != NULL );
                 m_xorBuffer = m_uidXorBuffer;
                 break;
          case ( warez_encryption ) :
                 MC2_ASSERT( m_warezXorBuffer != NULL );
                 m_xorBuffer = m_warezXorBuffer;
                 break;
   }
 
   // Header size.
   m_headerSize = buf.readNextBALong();
 
   // Rest of buffer is encrypted.
   if ( m_xorBuffer != NULL ) {
          m_fileHandler->setXorHelper( XorHelper( m_xorBuffer->getBufferAddress(),
                                                                                         m_xorBuffer->getBufferSize(),
                                                                                         buf.getCurrentOffset() ) );
   }
 
   m_nbrBytesToRead = m_headerSize;
 
   // Load the rest.
   m_state = loaded_initial_header;
   innerLoad();
}
 
void
SFDLoadableHeader::loadRemainingHeader( SharedBuffer& buf )
{
   // File size.
   m_fileSize = buf.readNextBALong();
 
   // The name.
   m_name = buf.readNextString();
 
   mc2dbg << "[SFDLH] m_name = " << m_name << endl;
 
   // Check the file size.
   if ( m_fileHandler->getFileSize() != m_fileSize ) {
          m_state = failed_to_load;
          m_nbrBytesToRead = 0;
          innerLoad();
          return;
   }
 
   // Creation time.
   m_creationTime = buf.readNextBALong();
 
   // Null terminated strings?
   m_stringsAreNullTerminated = buf.readNextBAByte();
 
   // Longest length of string.
   m_maxStringSize = buf.readNextBAByte();
 
   // Nbr initial chars.
   byte nbrInitialChars = buf.readNextBAByte();
   m_initialCharacters.resize( nbrInitialChars );
   // Initial chars.
   {for ( byte b = 0; b < nbrInitialChars; ++b ) {
          m_initialCharacters[ b ] = buf.readNextBAByte();
   }}
 
   // Nbr route ids.
   byte nbrRouteIDs = buf.readNextBAByte();
   m_routeIDs.reserve( nbrRouteIDs );
   {for ( byte b = 0; b < nbrRouteIDs; ++b ) {
          uint32 id = buf.readNextBALong();
          uint32 creationTime = buf.readNextBALong();
          m_routeIDs.push_back( RouteID( id, creationTime ) );
   }}
 
   // Number of bits for the string index.
   m_strIdxEntrySizeBits = buf.readNextBALong();
 
   // Position for the start of strings index.
   m_strIdxStartOffset = buf.readNextBALong();
 
   // Number strings.
   m_nbrStrings = buf.readNextBALong();
 
   // Position for the start of string data.
   m_strDataStartOffset = buf.readNextBALong();
 
   // Position for the start of the buffers index.
   m_bufferIdxStartOffset = buf.readNextBALong();
 
   // Position for the start of the buffer data.
   m_bufferDataStartOffset = buf.readNextBALong();
 
   // If to read debug param strings for the multi buffers.
   m_readDebugParams = buf.readNextBAByte();
 
   // The tile collections.
   uint32 nbrCollections = buf.readNextBAShort();
 
   m_tileCollection.resize( nbrCollections );
   {for ( uint32 i = 0; i < nbrCollections; ++i ) {
          m_tileCollection[ i ].load( buf );
   }}
 
   // All is now loaded.
   m_state = loaded_all_header;
   m_nbrBytesToRead = 0;
   innerLoad();
}
 
void
SFDLoadableHeader::readDone( int nbrRead )
{
   if ( nbrRead == -1 ) {
          m_readOK = false;
          m_listener->loadDone( this, m_fileHandler );
          return;
   }
   SharedBuffer buf( m_readBuffer, nbrRead );
   switch ( m_state ) {
          case ( nothing_loaded ) :
                 loadInitialHeader( buf );
                 return;
          case ( loaded_initial_header ) :
                 loadRemainingHeader( buf );
                 return;
          default:
                 // This shouldn't happen.
                 MC2_ASSERT( false );
   }
}
 
 
 */
