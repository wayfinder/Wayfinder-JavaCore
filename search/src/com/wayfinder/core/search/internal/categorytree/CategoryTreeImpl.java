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
 * Copyright, Wayfinder Systems AB, 2010
 */

package com.wayfinder.core.search.internal.categorytree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.wayfinder.core.search.CategoryTree;
import com.wayfinder.core.search.CategoryTreeException;
import com.wayfinder.core.search.CategoryTreeIterator;
import com.wayfinder.core.search.HierarchicalCategory;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.WriteSerializable;

/**
 * <p>Implements {@link com.wayfinder.core.search.CategoryTree}.</p>
 * 
 * <p>For information about design choices, see {@link CategoryTree}
 * and {@link CategoryTreeIterator}.</p>
 * 
 * <p>This class is intrinsically thread-safe by virtue of all fields being
 * final. But only if creators don't touch the byte arrays containing the
 * binary representation after construction.</p> 
 * 
 * <p>For information about which version of the binary format is supported,
 * please see the documentation for {@link CategoryTreeIteratorImpl}.</p>
 */
final class CategoryTreeImpl
    extends CategoryTree
    implements WriteSerializable {
    
    /**
     * Supported version of serializing format.
     */
    static final int VERSION = 1;
    
    private final int m_langId;
    private final Position m_position;
    private final String m_crc;
    

    /**
     * See category_table in the specification.
     */
    private final byte[] m_categoryTable;

    /**
     * See lookup_table in the specification.
     */
    private final byte[] m_lookupTable;

    /**
     * See string_table in the specification.
     */
    private final byte[] m_stringTable;

    
    /**
     * @param langId Language id used to request from server.
     * See {@link com.wayfinder.core.shared.settings.Language#getId()}.
     * @param position Position used to request table from server.
     * @param crc CRC sent by the server for this tree.
     * @param categoryTable Do not touch this after constructing the object.
     * @param lookupTable Do not touch this after constructing the object.
     * @param stringTable Do not touch this after constructing the object.
     */
    CategoryTreeImpl(int langId,
                     Position position,
                     String crc,
                     byte[] categoryTable,
                     byte[] lookupTable,
                     byte[] stringTable) {
        m_langId = langId;
        m_position = position;
        m_crc = crc;
        m_categoryTable = categoryTable;
        m_lookupTable = lookupTable;
        m_stringTable = stringTable;
    }

    // for additional constructors, see serializable part below.

    // ---------------------------------------------------------------------
    // CategoryTree interface

    /**
     * @return the position.
     */
    public Position getPosition() {
        return m_position;
    }

    public CategoryTreeIterator getRootLevelCategories()
        throws CategoryTreeException {

        return new CategoryTreeIteratorImpl(m_categoryTable,
                                            m_lookupTable,
                                            m_stringTable);
    }

    public CategoryTreeIterator getSubCategoriesOf(HierarchicalCategory parent)
            throws CategoryTreeException, NoSuchElementException {

        /*
         * The common case is that the category exists. So this is only
         * wasteful in the error cases.
         */
        CategoryTreeIteratorImpl cti = 
            new CategoryTreeIteratorImpl(m_categoryTable,
                                         m_lookupTable,
                                         m_stringTable);
        cti.subCategoriesOf(parent.getCategoryID());

        return cti;
    }


    // ---------------------------------------------------------------------
    // Core internal api stuff
    
    /**
     * @return the crc.
     */
    public String getCrc() {
        return m_crc;
    }
    
    /**
     * What language was the tree constructed for?
     * 
     * @return the language id.
     * @see com.wayfinder.core.shared.settings.Language#getId()
     */
    public int getLanguageId() {
        return m_langId;
    }


    // ---------------------------------------------------------------------
    // Serializable stuff


    /**
     * <p>Restore data written by {@link #write(DataOutputStream)} to a new
     * instance in a thread safe way - as long as the stream or its
     * underlying data is handled in a safe way.</p>
     * 
     * @param din the DataInputStream to read from.
     * @return a new instance.
     * @throws IOException if there is an I/O error or if the storage format
     * written is not supported by this implementation.
     */
    static CategoryTreeImpl read(DataInputStream din)
        throws IOException {
        // this avoids exposing the constructor.
        return new CategoryTreeImpl(din); 
    }

    /**
     * <p>Create an instance from serialized data as written by
     * {@link #write(DataOutputStream)}.</p>
     * 
     * @param din the DataInputStream to read from.
     * @throws IOException if there is an I/O error or if the storage format
     * written is not supported by this implementation.
     */
    private CategoryTreeImpl(DataInputStream din) throws IOException {
        if(VERSION != din.readInt()) {
            throw new IOException("Invalid version");
        }
        
        m_crc = din.readUTF();
        m_langId = din.readInt(); 
        m_position = new Position();
        m_position.read(din);
        
        int ctLength = din.readInt();
        m_categoryTable = new byte[ctLength];
        din.readFully(m_categoryTable);
        
        int ltLength = din.readInt();
        m_lookupTable = new byte[ltLength];
        din.readFully(m_lookupTable);

        int stLength = din.readInt();
        m_stringTable = new byte[stLength];
        din.readFully(m_stringTable);
    }


    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.WriteSerializable#write(java.io.DataOutputStream)
     */
    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(VERSION);
        
        dout.writeUTF(m_crc);
        dout.writeInt(m_langId);
        m_position.write(dout);
        
        dout.writeInt(m_categoryTable.length);
        dout.write(m_categoryTable);

        dout.writeInt(m_lookupTable.length);
        dout.write(m_lookupTable);

        dout.writeInt(m_stringTable.length);
        dout.write(m_stringTable);
    }


    // ---------------------------------------------------------------------
    // debugging

    public String toString() {
        StringBuffer sb = new StringBuffer(200);
        sb.append(super.toString());
        sb.append(" {crc=").append(m_crc);
        sb.append(" m_langId=").append(m_langId);
        sb.append(" m_position=").append(m_position);
        sb.append("}");

        return sb.toString();
    }
}
