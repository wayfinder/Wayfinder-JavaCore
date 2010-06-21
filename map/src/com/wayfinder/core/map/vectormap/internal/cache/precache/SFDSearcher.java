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

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;

public class SFDSearcher implements SFDFileIndexorListener {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(SFDSearcher.class);
    
    private SFDLoadableHeader m_header;
    private FileHandler m_file;
    private SFDFileIndexor m_fileIndexor;
    private SFDIndexor m_first;
    private SFDIndexor m_last;
    private SFDIndexor m_left;
    private SFDIndexor m_right;
    
    private String m_toSearchFor;
    
    private static final int NOT_INITIALIZED = 0;
    /// Reading the first entry of the file.
    private static final int READING_FIRST   = 1;
    /// Reading the last entry of the file.
    private static final int READING_LAST    = 2;
    /// Ready to search.
    private static final int READY           = 3;
    /// Searching
    private static final int SEARCHING       = 4;
    /// Permanent error has occured
    private static final int PERMANENT_ERROR = 1000;
    
    private int m_state;
    
    private SFDSearcherListener m_listener;
    
    
    
    public SFDSearcher(SFDLoadableHeader header, FileHandler fileHandler, SFDSearcherListener listener ){
        this.m_file = fileHandler;
        this.m_header = header;
        m_state = NOT_INITIALIZED;
        m_fileIndexor = null;
        m_first       = null;
        m_last        = null;
        m_listener    = listener;
        init();
    }
    
    private void init() {
        m_fileIndexor = new SFDFileIndexor( m_file, this, m_header );
        m_first = new SFDIndexor();
        m_last  = new SFDIndexor();
        m_left  = new SFDIndexor();
        m_right = new SFDIndexor();
    }
    
    public void indexorDone( SFDFileIndexor which_one, int status ) {
        if ( status < 0 ) {
            // Permanent error
            if(LOG.isError()) {
                LOG.error("SFDSearcher.indexorDone()", "Permanent error");
            }
            m_state = PERMANENT_ERROR;
            searcherDone();
            return;
        }
        if ( m_state == READING_FIRST ) {
           // System.out.println("state: READING_FIRST");
            m_state = READING_LAST;
            // Swap the data into our resident first entry.
            m_first.swap( m_fileIndexor );
            m_fileIndexor.setStrNbr( m_header.getNbrStrings() - 1 );
            m_fileIndexor.start();
        } else if ( m_state == READING_LAST ) {
            //System.out.println("state: READING_LAST");
            // Swap the data into our resident last entry.
            m_last.swap( m_fileIndexor );
            // Ready to process.
            m_state = SEARCHING;
            initSearchStep();
        } else if ( m_state == SEARCHING ) {
            //System.out.println("state: SEARCHING");
            handleSearchStep();
        }
    }
    
    public void initSearchStep() {
        // Check if inside range at all...
     
        if ( SFDIndexor.lessThan(m_toSearchFor,m_first) || m_last.lessThan( m_toSearchFor )) {
            if(LOG.isInfo()) {
                LOG.info("SFDSearcher.initSearchStep()", "Not found!");
            }
            searcherDone();
            return;
        }
        
        // Set the boundaries. Must copy here.
        m_left.copyContentsFrom(m_first);
        m_right.copyContentsFrom(m_last);
        
    // Set m_fileIndexor to middle
        // Make function
        setMiddleAndStart();
    }
    
    public void searcherDone( int strNbr ) {
      //  System.out.println("[SFDSearcher]: " + m_toSearchFor + "," + strNbr);
        m_state = READY;
     
        m_listener.searcherDone( this, strNbr );
    }
    
    public void searcherDone(SFDSearcher s , int strNbr){
    }
    
    public void searcherDone(){
    }
    
    public void setMiddleAndStart() {
        
        // If right and left are one step apart, we are almost done.
        if ( m_left.getStrNbr() + 1 == m_right.getStrNbr() ) {
            // Check the two strings
            //System.out.println("to search for: "+m_toSearchFor );
             // System.out.println("m_left.getStr(): "+m_left.getStr().trim());
               // System.out.println("m_right.getStr(): "+m_right.getStr().trim());
            if ( m_toSearchFor.equals(m_right.getStr().trim() )) {
                searcherDone( m_right.getStrNbr() );
            } else if ( m_toSearchFor.equals(m_left.getStr().trim() )) {
                searcherDone( m_left.getStrNbr() );
            } else {
                searcherDone();
            }
            return;
        } else {
            // Set new middle.
            m_fileIndexor.setStrNbr(( m_left.getStrNbr() + m_right.getStrNbr() ) / 2 );
            // Read the next one
            m_fileIndexor.start();
        }
    }
    
    public void handleSearchStep() {
        
        if ( SFDIndexor.lessThan(m_toSearchFor,m_fileIndexor) ) {
            // Swap the non-file part of the indexor.
            m_right.swap( m_fileIndexor );
            setMiddleAndStart();
        } else if ( m_fileIndexor.lessThan(m_toSearchFor) ) {
            // Swap the non-file part of the indexor.
            m_left.swap( m_fileIndexor );
            setMiddleAndStart();
        } else {
          //   System.out.println("Found and done");
            searcherDone( m_fileIndexor.getStrNbr() );
        }
    }
    
    public void searchFor( String str ) {  //SLOOOOOOOOW ? 
      //  System.out.println("search for "+ str);
     
        if ( m_state == PERMANENT_ERROR ) {
            if(LOG.isError()) {
                LOG.error("SFDSearcher.searchFor()", "permanent error: "+str);
            }
            searcherDone();
            m_state = PERMANENT_ERROR;
            return;
        }
        // Save the search string.
        m_toSearchFor = str;

        // Check if the first string is read. If not we need to init.
        if ( m_state == NOT_INITIALIZED ) {
            // Start reading the first entry.
            m_state = READING_FIRST;
            m_fileIndexor.setStrNbr( 0 );
            m_fileIndexor.start();
        } else {
            // State == READY
            m_state = SEARCHING;
            initSearchStep();
        }
    }
}

