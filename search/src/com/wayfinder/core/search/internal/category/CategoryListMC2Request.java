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
package com.wayfinder.core.search.internal.category;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.xml.XmlWriter;

class CategoryListMC2Request implements MC2Request {

    private final CategoryHolder m_holder;
    private final String m_crc;
    private final Position m_position;
    private final GeneralSettingsInternal m_settings;

    CategoryListMC2Request(
            CategoryHolder categoryHolder, 
            Position pos, 
            String crc,
            GeneralSettingsInternal settings) {
        
        m_holder = categoryHolder;
        m_position = pos;
        m_crc = crc;
        m_settings = settings;
    }


    public String getRequestElementName() {
        return MC2Strings.tcategory_list_request;
    }


    public void write(MC2Writer mc2w) throws IOException {
        mc2w.attribute(MC2Strings.acrc, m_crc);
        mc2w.attribute(MC2Strings.alanguage, m_settings.getInternalLanguage().getXMLCode());
        if(m_position != null && m_position.isValid()) {
            m_position.write(mc2w);
        }
    }


    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        
        //get the attributes first 
        final int nbrOfCats = mc2p.attributeAsInt(MC2Strings.acount);
        final String crc = mc2p.attribute(MC2Strings.acrc);

        mc2p.children();
        
        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        
        if (mc2p.nameRefEq(MC2Strings.tcrc_ok)) {
            m_holder.noUpdateAvailable();
        } else {
            CategoryImpl[] categories = new CategoryImpl[nbrOfCats];
            
            for (int i = 0; i < categories.length; i++) {
                if (mc2p.nameRefEq(MC2Strings.tcat)) {
                    categories[i] = parseCategory(mc2p);
                }
                mc2p.advance();
            }
        
            int langID = m_settings.getInternalLanguage().getId();
            m_holder.updateCategoryList(crc, langID, m_position, categories, true);
        }
    }


    private CategoryImpl parseCategory(MC2Parser mc2p)
    throws MC2ParserException, IOException {
        final int catID = mc2p.attributeAsInt(MC2Strings.acat_id);
        mc2p.children();
        String name = null;
        String imgName = null;
        do {
            if (mc2p.nameRefEq(MC2Strings.tname)) {
                name = mc2p.value();
            } else if (mc2p.nameRefEq(MC2Strings.timage_name)) {
                imgName = mc2p.value();
            }
        } while (mc2p.advance());

        return new CategoryImpl(name, imgName, catID);
    }


    public void error(CoreError coreError) {
        m_holder.error(coreError);
    }

}
