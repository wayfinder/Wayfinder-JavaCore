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

import java.util.Vector;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.map.vectormap.internal.control.TileMapRequestListener;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParamTypes;
import com.wayfinder.core.map.vectormap.internal.process.TileMapParams;
import com.wayfinder.pal.persistence.PersistenceLayer;

/**
 *   Read only DBufRequester that uses a single file for
 *   its data.
 */
public class SingleFileDBufRequester extends CacheInfo implements SFDSearcherListener,SFDBufReaderListener,SFDLoadableHeaderListener{
    
    private FileHandler m_fileHandler/*, m_fileHandlerToDelete*/;
    private String m_FileName;
    private SFDLoadableHeader m_header;
    private SFDLoadableHeaderListener m_Listener;
    
    private SFDSearcher m_searcher;
    private SFDBufReader m_bufReader;
    
    private boolean m_ReadDone;
    private int m_state;
    private int m_inStart;
    
    private ReadJob m_curReadJob;
    private ReleaseJob m_curReleaseJob;
    private Vector m_readQueue;
    private Vector m_releaseQueue;
    
    /// Possible states of this requester.
    
    /// Not initialized at all
    public static final int NOT_INITIALIZED = 0;
    /// Reading the header
    public static final int READING_HEADER = 1 ;
    /// Peek fitness condition
    public static final int IDLE = 2;
    /// Using the SFDSearcher
    public static final int SEARCHING_FOR_READING = 3;
    /// Reading the buffer.
    public static final int READING_BUFFER = 4;
    /// Searching for string when releasing
    public static final int SEARCHING_FOR_RELEASING = 5;
    /// Reading a multibuffer
    public static final int READING_MULTI = 6;
    /// Permanent error has occurred.
    public static final int PERMANENT_ERROR = 404;
    
    private int m_preferredLang;
    
    private PersistenceLayer m_PersistenceLayer;
    
    /**
     *   Creates a new SingleFileDBufRequester.
     *   @param parent The parent requester.
     *   @param fh     The file handler to use inside. Will be deleted
     *                 when SingleFileDBufRequester is deleted.
     *   @param listener Object to call when the header has been loaded.
     */
    public SingleFileDBufRequester(String fileName , SFDLoadableHeaderListener listener , int preferredLang, PersistenceLayer perLayer){
        
        m_PersistenceLayer = perLayer;
        m_state = NOT_INITIALIZED;
        m_preferredLang=preferredLang;
        m_FileName = fileName;
        m_Listener = listener;
        m_header = new SFDLoadableHeader();
        m_curReadJob = new ReadJob(null, null,/* cacheOrInternet*/ true);
        m_curReleaseJob = new ReleaseJob(null, null );
        m_searcher = null;
        m_bufReader = null;
        m_inStart = 0;
        m_ReadDone = false;
        m_readQueue = new Vector();
        m_releaseQueue = new Vector();
        
        // Read header
        startReadingHeader();
    }
    
    public int getState(){
        return m_state;
    }
    
    
    public boolean existInPrecache(TileMapParams param) {
        if(m_state == READING_HEADER && m_Listener.readDone()){
            m_state = IDLE;
        }
                
        if ( m_state == PERMANENT_ERROR ) {
            // We cannot recover
            //DBufRequester::request( descr, caller, whereFrom );
//            System.out.println("permanent error");
            return false;
        }
        
        if ( m_state != NOT_INITIALIZED && m_state != READING_HEADER ) {
            if (  m_header.maybeInCache( param ) == true) {
                return true;
            }
        } else {
//           System.out.println("m_state= "+m_state); 
        }
        return false;
    }
    
    
    
    /**
     *   Requests a buffer from this requester.
     */
    public boolean request(TileMapParams descr, TileMapRequestListener caller){
        
        
        if(m_state == READING_HEADER && m_Listener.readDone()){
            m_state = IDLE;
        }
        
        //System.out.println("_________________________________");
        //System.out.println("requesting "+descr.getAsString() +" from precache");
        // descr.printParams();
        
        if ( m_state == PERMANENT_ERROR ) {
            // We cannot recover
            //DBufRequester::request( descr, caller, whereFrom );
//            System.out.println("permanent error");
            return false;
        }
        
        
        if ( m_state != NOT_INITIALIZED && m_state != READING_HEADER ) {
            //long begin = System.currentTimeMillis();
            if (  m_header.maybeInCache( descr ) == false) {
                //  System.out.println("not in precache");
                // This means that it cannot be in this cache so send it on.
                //if ( m_parentRequester != NULL ) {
                //  m_parentRequester.request(descr, caller, whereFrom );
                //}
                return false;
            }
            ///   System.out.println("maybe in cachetime: "+(System.currentTimeMillis()-begin));
        }
        
        // Make job.
        ReadJob readJob = new ReadJob( descr, caller, true);
        
        // Check if already there
        if ( m_readQueue.indexOf(readJob) == -1){
            m_readQueue.addElement( readJob );
        } else {
//            System.out.println("already in read queue");
        }
        
        if ( m_state == NOT_INITIALIZED ) {
            // Read header
            startReadingHeader();
            // Don't start working
            return false;
        } else if ( m_state != READING_HEADER ) {
            // Will start working if not working already.
            start();
        }
        return true;
        
    }
    
    
    /**
     *   Releases the buffer. Eats it if it belongs here.
     */
    public void release( TileMapParams desc, BitBuffer buffer ){
        
        if ( buffer == null ) {
            // Nothing to do.
            return;
        }
        
        if ( m_state == PERMANENT_ERROR ) {
            // Error - send on
            //DBufRequester::release( desc, buffer );
            return;
        }
        
        if ( m_state == NOT_INITIALIZED ||
            m_state == READING_HEADER ) {
            // It cannot have come from here. Send to next.
            //  DBufRequester::release( desc, buffer );
        } else {
            // Check the offset and don't put it in the queue if it is
            // mine!!
            int multiBufferOffset = -1;
            if ( TileMapParamTypes.isMap( desc.getAsString() ) ) {
                // Only do this for maps!
                multiBufferOffset = getMultiBufferOffset( desc );
            }
            if ( multiBufferOffset >= 0 ) {
                // It is mine!
//                System.out.println("[SFDBR]: IT IS DEFINITELY MINE (1) !!!!");
                //findbugs:stores null into a local variable but the value is not read
//                buffer = null;
            } else {
                if ( m_header.maybeInBinaryCache( desc ) ) {
//                    System.out.println("[SFDBR]: Could be mine "+ desc);
                    m_releaseQueue.addElement( new ReleaseJob( desc, buffer ) );
                } else {
//                    System.out.println("[SFDBR]: Definitely not mine : "+desc.getAsString());
                    //BufRequester::release( desc, buffer );
                }
            }
        }
        
        start();
    }
    
    
    public boolean isValid(){
        return m_state != PERMANENT_ERROR;
    }
    
    public void cancelAll(){
        // L�gg m�rke till!
        m_readQueue.removeAllElements();
        // The parent too!
        //DBufRequester::cancelAll();
    }
    
    
    
    private void startReadingHeader(){
        m_state = READING_HEADER;
        m_header.load( m_FileName, m_Listener, m_PersistenceLayer);
    }
    
    public boolean readDone(){
        return m_ReadDone;
        
    }
    
    public void searcherDone( SFDSearcher searcher, int strIdx ){
        if ( m_state == SEARCHING_FOR_READING ) {
            if ( strIdx == Integer.MAX_VALUE ) {
//                System.out.println("Not found");
                m_state = IDLE;
                
                m_curReadJob.callListener( null, this );
                start();
                return;
            } else {
              //  System.out.println("Searcher done: Found");
                m_state = READING_BUFFER;
              //  System.out.println("state: READING BUFFER");
                m_bufReader.start( strIdx );
                return;
            }
        } else if ( m_state == SEARCHING_FOR_RELEASING ) {
            //System.out.println("state: SEARCHING FOR RELEASING");
            if ( strIdx == Integer.MAX_VALUE ) {
//                System.out.println("[SFDBR]: Released map not found in my cache");
                // Not found -> release to next
                
                release( m_curReleaseJob.desc, m_curReleaseJob.buf );
            } else {
//                System.out.println("[SFDBR]: IT IS MINE");
                // Found here -> delete and forget
                
                m_curReleaseJob.buf = null;
            }
            
            m_curReleaseJob.buf = null;
            m_state = IDLE;
            // Start again if necessary.
            start();
        }
    }
    
    public void searcherDone(){
     //   System.out.println("searcher done");
    }
    
    public void loadDone( SFDLoadableHeader header, FileHandler fileHandler ) {
        // Check if header loaded ok
        if ( !header.isValid() ) {
//            System.out.println("Header invalid");
            m_fileHandler = fileHandler;
            m_state = PERMANENT_ERROR;
        } else {
            // Change file handler
            m_fileHandler = fileHandler;
            m_state = IDLE;
            
            // Initialize the Searcher and buffer readers.
            m_searcher  = new SFDSearcher( m_header, m_fileHandler, this );
            m_bufReader = new SFDBufReader( m_header, m_fileHandler,this );
//            if(Utils.PRECACHE_TRACE)
//                System.out.println("Created Searcher & Reader");
        }
        
        if ( m_state != PERMANENT_ERROR ) {
            // Start if we have something to do.
            start();
        }
    }
    
    private void start(){
        if ( m_inStart == 1) {
            return;
        }
        ++m_inStart;
        while( innerStart() ) {
        }
        --m_inStart;
    }
    
    private boolean innerStart() {
        if ( m_state != IDLE ) {
            // Already doing something.
            return false;
        }
        //Check the sizes of the queues and take the longest?
        if ( !m_readQueue.isEmpty()) {
            // Read wanted - start the searcher.
            // Switch to swap when that is implemented properly
            //std::swap( *m_curReadJob, m_readQueue.front() );
            m_curReadJob = (ReadJob)m_readQueue.firstElement();
            m_readQueue.removeElementAt(0);
            int multiBufferOffset = -1;
            if ( TileMapParamTypes.isMap(m_curReadJob.desc.getAsString()) ) {
                //   System.out.println("Getting map from cache: "+m_curReadJob.desc.getAsString()+" time: "+(System.currentTimeMillis()-1174550000000L));
                // Only do this for maps!
//                long begin = System.currentTimeMillis();
                multiBufferOffset = getMultiBufferOffset( m_curReadJob.desc );
            }
            if ( multiBufferOffset < 0 ) {
                // Not there - search for it using string search.
                // First letter already checked in ::request.
                m_state = SEARCHING_FOR_READING;
                //  System.out.println("search for : "+ m_curReadJob.desc.getAsString()+" time: "+(System.currentTimeMillis()-1174559000000L));
                
//                long begin = System.currentTimeMillis();
                
                m_searcher.searchFor( m_curReadJob.desc.getAsString() );
                
             //   System.out.println("searchfor: "+(System.currentTimeMillis()-begin));
                
            } else {
                m_state = READING_MULTI;
             //   long begin = System.currentTimeMillis();
                
                m_bufReader.startAbsolute( multiBufferOffset );
                
           //     System.out.println("start abs: "+(System.currentTimeMillis()-begin));
            }
        } else if ( ! m_releaseQueue.isEmpty() ) {
            // Switch to swap when that is implemented properly
            //std::swap( *m_curReleaseJob, m_releaseQueue.front() );
            m_curReleaseJob = (ReleaseJob)m_releaseQueue.firstElement();
            m_releaseQueue.removeElementAt(0);
            
            // The ones in the queue should not be in the tile collection.
            m_state = SEARCHING_FOR_RELEASING;
            m_searcher.searchFor( m_curReleaseJob.desc.getAsString() );
        }
        
        // Return if there are anything more to do.
        // Next time innerStart is called it will check the state for idleness.
        return !m_readQueue.isEmpty() || !m_releaseQueue.isEmpty();
        
    }
    
    
    private int getMultiBufferOffset(TileMapParams params ) {
        return m_header.getMultiBufferOffsetOffset( params);
    }
    
    
    public void bufferRead( BitBuffer buf ){
        // These are the allowed states.
        if( m_state == READING_BUFFER ||  m_state == READING_MULTI ){
            
            // Call the listener
            int prevState = m_state;
            m_state = IDLE;
            if ( prevState == READING_BUFFER ) {
                
                m_curReadJob.callListener( buf, this );
                // Check if we want to start.
            } else if ( prevState == READING_MULTI ) {
                
                // We got a buffer with lots of importances.
                handleMultiBuffer( buf );
            }
            
            // Start again if necessary.
            start();
        }else{
//            System.out.println("invalid state");
        }
    }
    
    private void handleMultiBuffer( BitBuffer buf ) {  //SLOOOOOW
        // read will delete the big buffer when it is done
        // Should maybe be called decoder.
        TileMapParams curParam =  m_curReadJob.desc;
        int curLayerID = curParam.getLayerID();
        boolean curLayerIDprocessed = false;
      // System.out.println("cur param: "+curParam.getAsString());
        SFDMultiBufferReader reader = new SFDMultiBufferReader( buf, curParam, m_header );
        
        // Now let's send all the wanted/unwanted stuff to the listener.
        // The small buffer will not be deleted by the reader so it is
        // ok to send it on.
      //  long begin = System.currentTimeMillis();
        
        while ( reader.hasNext() ) {
            Object[] curPair;
            synchronized (reader) {
                
                if(curLayerID == reader.m_layerID) {
                    curLayerIDprocessed = true;
                }
                
                //curPair = reader.readNext( preferredLang ); //preferred lang
                
                //long beginRead = System.currentTimeMillis();
                int mapOrStrings = TileMapParams.MAP;  //We use 1, Symbian uses 0
                if(reader.m_strings)
                    mapOrStrings = TileMapParams.STRINGS;
                curPair = new Object[2];
                
                curPair[0] = new TileMapParams();
                ((TileMapParams)curPair[0]).setParams( 9,      // Server prefix
                    reader.m_prototypeParam.useGZip(),
                    reader.m_layerID,
                    mapOrStrings,
                    reader.m_curImp,
                    m_preferredLang,
                    reader.m_prototypeParam.getTileIndexLat(),
                    reader.m_prototypeParam.getTileIndexLon(),
                    reader.m_prototypeParam.getDetailLevel(),null,"" );
                
                // System.out.println(Integer.toBinaryString((int)m_existingImps | 0x80000000));
                // Check if existing.
                boolean existing = ((reader.getExistingImportances() >> reader.m_curImp) & 0x1) == 1;
                
                // System.out.println("existing: "+existing+" curimp: "+m_curImp);
                BitBuffer sean_combs = null;
                
                if ( existing ) {
                    
                    // Length of buffer
                    int bufLen =(int) reader.m_bigBuf.nextInt();
                    
                    // Read the map buffer.
                    if ( bufLen > 0 ) {
                        
                        sean_combs = new BitBuffer( reader.m_bigBuf.nextByteArray(bufLen) );
                        
                        
                    }else{
//                        System.out.println("empty buffer for "+ ((TileMapParams)curPair[0]).getAsString() );
                    }
                    
                    if ( m_header.readDebugParams() ) {
                        
                        /*String paramStr = */reader.m_bigBuf.nextString();
                    }
                    
                }else{
                    //System.out.println("not existing: paramString= "+((TileMapParams)curPair[0]).getAsString());  
                }
                
                reader.m_hasNext = reader.moveToNext();
//                if(Utils.PRECACHE_TRACE)
//                    System.out.println("[SFDMBR]:readNext: Expected param "
//                        + ((TileMapParams)curPair[0]).getAsString()+", existing = " + existing);
//                
                curPair[1] = sean_combs;
               // int time1 = (int)(System.currentTimeMillis() - beginRead);
              
                //System.out.println("read next: "+time1);
                
                
            }
            // Ok since we have the reader on the stack.
            
            
            
            
            BitBuffer b = (BitBuffer)curPair[1];
            TileMapParams t =  (TileMapParams)curPair[0];
            
            
            
            m_curReadJob.callListener( t, b, this );
            
            
            
            // Also remove the incoming jobs that correspond to this
            // param.
            
            removeFromReadQueue( (TileMapParams)curPair[0] );
            
        }
        
        // XXX: This happen when we request a layer that doesn't contains any information, 
        // for example a empty poi-tile. The pre-installed map-component just ignore the 
        // request and return information about the other layers.  
        // The fix tell the m_curReadJob listener that the layer that we request data from are 
        // empty so that it doesn't have to re-request it again.         
        if(!curLayerIDprocessed) {
//            //#debug info
//            System.out.println("SingleFileDBufRequester: The requested layer are empty, paramString= "+curParam.getAsString());
            m_curReadJob.callListener(curParam, null, this);
        }
        
    }
    
    public void removeFromReadQueue( TileMapParams desc ){
        for(int i =0 ;i< m_readQueue.size();i++){
            ReadJob r = (ReadJob)m_readQueue.elementAt(i);
            if((r.desc.getAsString()).equals(desc.getAsString())){
                m_readQueue.removeElementAt(i);
                return;
            }
        }
    }
    
    public String getNameUTF8(){
        if ( ( m_state == NOT_INITIALIZED ) || ( m_state == READING_HEADER ) ) {
            return "Cache not yet loaded";
        } else {
            return m_header.getName();
        }
    }
    
    public String getPathUTF8(){
        return m_fileHandler.getFileName();
    }
    
    
    
    /**
     * Returns a number that will be used by the application to check if
     * the setup of preinstalled maps are the same as the last startup
     * 
     * @return a crc
     */
    public int getCacheCRC() {
        // Yes, the CRC is far from foolproof since it's
        // theoretically possible to get the same crc for
        // two different setups, but I gather that it's good
        // enough
        return (m_FileName.hashCode() + m_fileHandler.getFileSize());
    }
    
}
