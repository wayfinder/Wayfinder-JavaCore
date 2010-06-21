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

package com.wayfinder.core.map.vectormap.internal.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.io.WFByteArrayOutputStream;
import com.wayfinder.core.shared.util.qtree.QTFileInterface;
import com.wayfinder.core.shared.util.qtree.QuadTree;
import com.wayfinder.core.shared.util.qtree.QuadTreeNode;
import com.wayfinder.pal.error.PermissionsException;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.WFFileConnection;

public class FileHandler implements QTFileInterface {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(FileHandler.class);
    
    private static final String FNAME_INFO = "FInfo.dvf";      
    private static final String FNAME_INDEX = "FIndex.dvf";
    private static final String FNAME_PAGE_FILE = "FPage.dvf";
    private static final String FNAME_QT_FILE = "FQTFile.dvf";
    
    private WFFileConnection m_QuadTreeFile;
    private WFFileConnection []m_PageConnections;
    private WFFileConnection m_InfoFile;
    private WFFileConnection m_IndexFile;
    
    private int m_NbrOfPages;
    private String m_FileBasePath;
    private PersistenceLayer m_PersistenceLayer; 
    
    private byte m_CurrentPageNumber = 0;
    private int m_VersionNumber = -1;
    
    public FileHandler(int nbrOfPages, PersistenceLayer persistenceLayer) {
        m_NbrOfPages = nbrOfPages;        
        m_PersistenceLayer = persistenceLayer;        
    }
    
    /**
     * Open the cache files. 
     * 
     * @return 
     */
    public boolean openCache() {
        try {                       
            openCacheFiles(m_NbrOfPages);
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.openCache()", e);
            }
            return false;
        }
        return true;
    }
    

    public void deleteFile(String name) {
        try {
            m_PersistenceLayer.openFile(m_FileBasePath+name).delete();
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.deleteFile()", e);
            }            
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.deleteFile()", e);
            }            
        }
        
    }
    
    /**
     * Write the quad tree to file, only nodes that contain any data will be saved on file. 
     * 
     * @param qt
     * @throws IOException
     */
    public void writeQuadTreeToFile(QuadTree qt) throws IOException {
        DataOutputStream dout = null;
        try {
            dout = m_QuadTreeFile.openDataOutputStream();
        
            Vector v = new Vector();
            qt.getAllNodes(v);                        
            dout.writeInt(v.size());
            
            for(int i=0; i<v.size(); i++) {
                QuadTreeNode node = (QuadTreeNode)v.elementAt(i);                
                dout.writeInt(node.getMinLat());
                dout.writeInt(node.getMinLon());
                dout.writeInt(node.getMaxLat());
                dout.writeInt(node.getMaxLon());
                dout.writeUTF(node.getName());       
            }
        } finally {
            if(dout != null)
                dout.close();            
        }        
    }
    
    /**
     * Read the quad tree from a file into memory. It is only the nodes that contains
     * data that will be loaded. Any empty nodes will not be loaded and no quad tree entrys
     * that are attached to the node will be loaded. 
     * 
     * @param qt
     * @return
     * @throws IOException
     */
    public boolean readQuadTreeFromFile(QuadTree qt, boolean delete) throws IOException {
        
        DataInputStream din = null;        
        try {            
            if(m_QuadTreeFile.fileSize() > 0) {
                din = m_QuadTreeFile.openDataInputStream();
                int size = din.readInt();
                for(int i=0; i<size; i++) {            
                    int minLat = din.readInt();
                    int minLon = din.readInt();
                    int maxLat = din.readInt();
                    int maxLon = din.readInt();
                    String name = din.readUTF();                    
                    QuadTreeNode node = new QuadTreeNode(name, minLat, minLon, maxLat, maxLon);
                    
                    if(!delete)
                        qt.addNode(node);
                    else
                        deleteFile(name);                    
                }                
            }
        } finally {
            
            try {
                if(din != null)
                    din.close();
            } catch (Exception e) {
                if(LOG.isError()) {
                    LOG.error("FileHandler.readQuadTreeFromFile()", e);
                }
            }
        }        
        return true;
    }
    
    
    private WFByteArrayOutputStream wfout = new WFByteArrayOutputStream(32000);
    private DataOutputStream daout = new DataOutputStream(wfout);
    
    /**
     * Write all the QuadTreeEntry:s that are added to the node to a file. The
     * file will be named with the same file name as the node.  
     * 
     * @param node to node to be writed. 
     * @return true if the writing was succeeded, false if not. 
     */
    public boolean writeNodeToFile(QuadTreeNode node) {
        
        WFFileConnection fileConn = null;
        DataOutputStream dout = null;
        try {
            IndexTableEntry qte = (IndexTableEntry)node.getAllEntrys();
            
            if(qte == null) {
                return false;
            }
           
            fileConn = m_PersistenceLayer.openFile(m_FileBasePath+node.getName());            
            dout = fileConn.openDataOutputStream();
            
            wfout.reset();
            
            /* Write the number of entry:s first in the file. */
            daout.writeInt(node.getSize());
            
            // For all the entry:s in the node. 
            do {                
                daout.writeUTF(qte.getName());
                daout.writeInt(qte.getLatitude());
                daout.writeInt(qte.getLongitude());
                daout.writeInt(qte.getOffset());
                daout.writeInt(qte.getPage());
                qte = (IndexTableEntry)qte.getNext();
                
            } while (qte != null);
            
            daout.flush();
            dout.write(wfout.getByteArray(), 0, wfout.size());
            dout.flush();
            
        } catch(Exception e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.writeNodeToFile()", e);
            }
            return false;
            
        } finally {
            try {
                if(dout != null)
                    dout.close();
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("FileHandler.writeNodeToFile()", "e1= "+e);
                }
            }
            
            try {
                if(fileConn != null)
                    fileConn.close();
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("FileHandler.writeNodeToFile()", "unable to close iFileConn, e= "+e);
                }
            }
        }
        return true;
    }
    
    /**
     * Read all the entrys for a node from file. 
     * 
     * This is done when populate a node in the quad tree.
     * 
     *  -------------------------------------------------
     *  Data format
     *  -------------------------------------------------
     *  parameter string
     *  tile index lat (int)
     *  tile index lon (int)
     *  offset (int)
     *  page (int)
     *  -------------------------------------------------
     * 
     * @param node the node to be loaded. 
     * @return the head of the node. 
     */
    public void readNodeFromFile(QuadTreeNode node) {
        
        WFFileConnection file_conn = null;
        DataInputStream din = null;
        
        try {
            file_conn = m_PersistenceLayer.openFile(m_FileBasePath+node.getName());
            
            if(LOG.isError()) {
                if(file_conn.fileSize() == 0) {                
                    LOG.error("FileHandler.readNodeFromFile()", "file_conn.fileSize()= "+file_conn.fileSize());
                }
            }
            
            din = file_conn.openDataInputStream();
            
            final int size = din.readInt();            
            for(int i=0; i<size; i++) {
               
                String tileid = din.readUTF();                
                // tile index lat
                int lat = din.readInt();
                // tile index lon
                int lon = din.readInt();
                // offset into the page file
                int offset = din.readInt();
                // the page file number
                int page = din.readInt();
                
                IndexTableEntry entry = new IndexTableEntry((byte)page, offset, tileid, lat, lon);                
                node.addEntry(entry);
                
            }
            
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.readNodeFromFile()", e);
            }
        } finally {            
            try {
                if(din != null)
                    din.close();
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("FileHandler.readNodeFromFile()", "Unable to close din e2= "+e);
                }
            }
            
            try {
                if(file_conn != null)
                    file_conn.close();
            } catch (IOException e) {
                if(LOG.isError()) {
                    LOG.error("FileHandler.readNodeFromFile()", "Unable to close file_conn, e= "+e);
                }
            }   
        }        
    }
 
    /**
     * Remove all data from a page file.
     * 
     * @param pageNbr the number of the page that should be deleted. 
     */
    public void clearPageFile(byte pageNbr) {        
        try {            
            if(m_PageConnections[pageNbr].fileSize() > 0) {
                m_PageConnections[pageNbr].delete();
                m_PageConnections[pageNbr] = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_PAGE_FILE+pageNbr);
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.clearPageFile()", e);
            }
        }
    }
    
    /*
     * Internal method that open all the cache files needed for the cache to be used. 
     * 
     * @param aNbrOfPages number of page file to open. 
     * @return true if we succed to open the cache files. 
     * @throws IOException
     */
    private boolean openCacheFiles(int aNbrOfPages) throws IOException, PermissionsException {
        
        if(m_PersistenceLayer.getBaseFileDirectory() == null)
            return false;
        
        m_FileBasePath = m_PersistenceLayer.getBaseFileDirectory()+"/mapcache/";        
        m_PageConnections = new WFFileConnection[aNbrOfPages];
        for(int i=0; i<aNbrOfPages; i++) {
            m_PageConnections[i] = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_PAGE_FILE+i);
        }

        m_InfoFile = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_INFO);
        m_IndexFile = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_INDEX);
        m_QuadTreeFile = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_QT_FILE);
        
        return true;
    }
    
    public DataInputStream getPageDataInputStream(int aPage) throws IOException {
        return m_PageConnections[aPage].openDataInputStream();
    }
    
    public DataOutputStream getPageDataOutputStream(int aPage, int aOffset) throws IOException {
        if(LOG.isTrace()) {
            LOG.trace("FileHandler.getPageDataOutputStream()", "page=" + aPage + " offset=" + aOffset);
        }
        
        if(aOffset == 0) {
            return m_PageConnections[aPage].openDataOutputStream();
        } else {
            if(LOG.isWarn()) {
                if (m_PageConnections[aPage].fileSize() != aOffset)
                LOG.warn("FileHandler.getPageDataOutputStream()", "expected size=" + aOffset + " real size " + m_PageConnections[aPage].fileSize());
            }
            
            return m_PageConnections[aPage].openDataOutputStream(true);
        }                
    }
    
    
    // -----------------------------------------------
    // Cache info file
    
    /**
     * Load the cache info file. The file contains the <br>
     * the current active file <br>
     * the version of the cache <br> 
     * <p>
     * Return true if the file contains data, false if not.
     * 
     */
    boolean loadCacheInfoFile() {        
        try {
            if(m_InfoFile.fileSize() > 0) {
                DataInputStream din = m_InfoFile.openDataInputStream();
                m_CurrentPageNumber = din.readByte();
                m_VersionNumber = din.readInt();     
            } else {
                return false;
            }            
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.loadCacheInfoFile()", "e= "+e);
            }
        }
        return true;
    }
    
    /*
     * Return the current active page number
     */
    byte getPageNumber() {
        return m_CurrentPageNumber;
    }
    
    /*
     * Return the offset in to the page file where to start write new data. 
     */
    int getOffset(int pageNbr) {
        return m_PageConnections[pageNbr].fileSize();
    }
    
    int getVersionNumber() {
        return m_VersionNumber;
    }
    
    public void writeCacheInfoToFile(byte aCurrentPage, int aVersion) throws IOException {
        DataOutputStream dout = m_InfoFile.openDataOutputStream();
        dout.writeByte(aCurrentPage);
        dout.writeInt(aVersion);
        dout.close();
    }
    
    public void writeIndexTableToFile(Hashtable aIndexTableHash) throws IOException {
        
        Enumeration indices = aIndexTableHash.elements();
        DataOutputStream dout = m_IndexFile.openDataOutputStream();
        
        final int size1 = aIndexTableHash.size();
        int size2 = 0;
        
        dout.writeInt(aIndexTableHash.size());
        while(indices.hasMoreElements()) {            
            IndexTableEntry entry = (IndexTableEntry)indices.nextElement();            
            dout.writeByte(entry.getPage());
            dout.writeInt(entry.getOffset());
            dout.writeUTF(entry.getName());
            
            size2++;
        }
        
        dout.close();
        
        if(LOG.isError()) {
            if(size1 != size2) {
                LOG.error("FileHandler.writeIndexTableToFile()", "SIZE1 DIFFERS FROM SIZE2!, "+
                        " size1= "+size1+" size2= "+size2);
            }
        }
        
        if(LOG.isInfo()) {
            LOG.info("FileHandler.writeIndexTableToFile()", "writing "+size2+" item to the index table file. ");
        }        
    }
    
    public void readIndexTableFromFile(Hashtable indexTableHashtable) {        
        try {
            if(m_IndexFile.fileSize() > 0) {        
                final DataInputStream din = m_IndexFile.openDataInputStream();            
                if(din != null) {
                    final int size = din.readInt();
                    for(int i=0; i<size; i++) {
                        final byte page = din.readByte();
                        final int offset = din.readInt();
                        final String tileID = din.readUTF();
                        indexTableHashtable.put(tileID, new IndexTableEntry(page, offset, tileID));
                        
                        if(LOG.isDebug()) {
                            LOG.debug("FileHandler.readIndexTableFromFile()", 
                                    "Read IndexFile: "+tileID+" page= "+page+" offset= "+offset);
                        }                        
                    }                
                }
            }
        } catch (Exception e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.readIndexTableFromFile()", e);
            }
        }        
    }
    
    // --------------------------------------------------------------------------------------------
    // Reboot the cache
    public void rebootCache() {
        try {
            /* Delete all the saved node files from disc. */
            readQuadTreeFromFile(null, true);
            
            /* Clear the qt file. */
            if(m_QuadTreeFile.fileSize() > 0) {
                m_QuadTreeFile.delete();
                m_QuadTreeFile = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_QT_FILE);
            }
            
            /* Clear all the cache page files. */
            for(int i=0; i<m_NbrOfPages; i++) {
                clearPageFile((byte)i);                 
            }
            
            /* Clear the cache info file. */
            if(m_IndexFile.fileSize() > 0) {
                m_IndexFile.delete();
                m_IndexFile = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_INDEX);
            }
            
            /* Clear the cache info file. */
            if(m_InfoFile.fileSize() > 0) {
                m_InfoFile.delete();
                m_InfoFile = m_PersistenceLayer.openFile(m_FileBasePath+FNAME_INFO);
            }
            
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.rebootCache()", e);
            }
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("FileHandler.rebootCache()", e);
            }
        }
    }

    WFFileConnection getQuadTreeFile() {
        return m_QuadTreeFile;
    }

    WFFileConnection[] getPageConnections() {
        return m_PageConnections;
    }

    WFFileConnection getInfoFile() {
        return m_InfoFile;
    }

    WFFileConnection getIndexFile() {
        return m_IndexFile;
    }

    int getNbrOfPages() {
        return m_NbrOfPages;
    }
}
