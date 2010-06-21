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
package com.wayfinder.core.shared.poiinfo;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldImpl;

/**
 * Represent extra information associated with a POI
 * The structure is a list of {@link InfoFieldImpl}  
 * 
 * 
 */
public class PoiInfo {

    private final String m_typeName;
    private final String m_itemName;
    private final int m_heading;
    private final Position m_position;
    
    private final InfoFieldList m_infoFieldList;

    public PoiInfo(String typeName, String itemName, int heading, Position pos, InfoFieldList infoFieldList) {
        m_typeName = typeName;
        m_itemName = itemName;
        m_heading = heading;
        if (pos == null) {
            m_position = Position.NO_POSITION;    // keep an invalid position
        }
        else {
            m_position = pos;               // even if it's invalid
        }
        
        m_infoFieldList = infoFieldList;
    }
    
    /**
     * The name of the type of item, such as, “Petrol Station”.
     */
    public String getTypeName() {
        return m_typeName;
    }

    /**
     * The name of the item.
     */
    public String getItemName() {
        return m_itemName;
    }
    
    /**
     * The position of the POI. Since the coordinates are optional, check 
     * {@link Position#isValid()} before using this position.
     */
    public Position getPosition() {
        return m_position;
    }

    /**
     * @return the list of info fields for this poi, use it when create a new 
     * favorite from POI 
     */
    public InfoFieldList getInfoFieldList() {
        return m_infoFieldList;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("name: "); sb.append(m_itemName);
        sb.append("\ntype: "); sb.append(m_typeName);
        sb.append("\nheading: "); sb.append(m_heading);
        sb.append("\n"); sb.append(m_position);
        if (m_infoFieldList != null) {
            sb.append("\n");sb.append(m_infoFieldList);
        }
        
        return sb.toString();
    }
    
    /* XML API 2.1.1
    <!ELEMENT poi_info_reply ( info_item* | ( status_code, status_message,
            status_code_extended? ) ) >
            <!ATTLIST poi_info_reply transaction_id ID #REQUIRED>
            <!ELEMENT info_item ( typeName, itemName, lat?, lon?, category_list?,
            info_field*, search_item? )>
            <!ATTLIST info_item numberfields %number; #REQUIRED
            heading %number; #IMPLIED >
            <!ELEMENT typeName ( #PCDATA )>
            <!ELEMENT itemName ( #PCDATA )>
            <!ELEMENT info_field ( fieldName, fieldValue ) >
            <!ATTLIST info_field info_type %poi_info_t; #IMPLIED >
            <!ELEMENT fieldName ( #PCDATA )>
            <!ELEMENT fieldValue ( #PCDATA )>
    */            
}
