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

/**
 * This class represents a tree which is not correctly serialized.
 */
class MinimalTreeWithErrors {

	// Do NOT modify this array or expose it outside of Core
	static final byte[] m_lookupTable = new byte[] {
		// category_id: 305419896, offset: 0x6
		0x12, 0x34, 0x56, 0x78, 0x0, 0x0, 0x0, 0x6, 

        // category_id: 0x00123400, (truncated offset)
        0x00, 0x12, 0x34, 0x00, 0x0, 0x0, 0x0, 
	
	};

	public static final int ID_WITH_TRUNCATED_OFFSET = 0x00123400; 

	// Do NOT modify this array or expose it outside of Core
	static final byte[] m_categoryTable = new byte[] {
		// 1 top level categories as offsets
		0x0, 0x1, 0x0, 0x0, 0x0, 0x6, 

		// offset: 0x6 id: 305419896
		// name: ""@0x2
		// imageName: (@0x500 - this offset is outside of the table)
		// 0 sub categories by ID: 
		0x12, 0x34, 0x56, 0x78, 0x0, 0x0, 0x0, 0x2, 0x0, 0x0, 0x5, 0x0, 0x0, 0x0, 
	};

	static final byte[] m_categoryTableShort = new byte[] {
	    // 1 top level categories as offsets
	    0x0, 0x1, 0x0, 0x0, 0x0, 0x6, 

	    // offset: 0x6 id: 305419896 (truncated) 
	    0x12, 0x34, 0x56, 0x78, 0x0
	};

	
	// Do NOT modify this array or expose it outside of Core
	static final byte[] m_stringTable = new byte[] {
		// ""@0x2 (offset of first char)
		0x0, 0x0, 0x0, 

	};
}
