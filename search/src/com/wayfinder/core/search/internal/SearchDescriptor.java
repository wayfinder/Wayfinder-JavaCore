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
package com.wayfinder.core.search.internal;

import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.core.shared.util.LinkedList;

class SearchDescriptor {

    static final int DESC_CLIENT_VERSION = 2;
    static final int DESC_SERVER_VERSION = 1;
    
    private final String m_crc;
    private final Provider[] m_providers;
    private final int m_highestRound;
    private final int m_language;
    
    
    SearchDescriptor(String crc, Provider[] providers, int language) {
        m_crc = crc;
        m_providers = providers;
        m_language = language;
        
        int highRound = 0;
        for (int i = 0; i < providers.length; i++) {
            int thisRound = providers[i].getRound();
            if(thisRound > highRound) {
                highRound = thisRound;
            }
        }
        m_highestRound = highRound;
    }
    
    
    boolean isValid() {
        return m_providers.length > 0;
    }
    
    
    boolean isValidForLanguage(Language lang) {
        return isValid() && (m_language == lang.getId());
    }
    
    
    int getLanguageID() {
        return m_language;
    }
    
    
    String getCRC() {
        return m_crc;
    }
    
    
    int getHighestRound() {
        return m_highestRound;
    }
    
    
    Provider getProviderWithHeading(int heading) {
        for (int i = 0; i < m_providers.length; i++) {
            if(m_providers[i].m_heading == heading) {
                return m_providers[i];
            }
        }
        return null;
    }
    
    
    Provider[] getProvidersForRegion(int regionID) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < m_providers.length; i++) {
            Provider provider = m_providers[i];
            if(provider.getRound() > 0) {
                final int id = provider.getTopRegionID();
                if(id == regionID || id == Provider.ALL_REGIONS) {
                    list.add(provider);
                }
            }
        }
        Provider[] providerArray = new Provider[list.size()];
        list.toArray(providerArray);
        return providerArray;
    }
    
    
    Provider[] getAllProvidersInternalArray() {
        return m_providers;
    }
    
    
    static class Provider implements SearchProvider {
        
        static final int ALL_REGIONS = Integer.MIN_VALUE;
        
        private final int m_round;
        private final int m_heading;
        private final String m_name;
        private final String m_type;
        private final int m_topRegionID;
        private final String m_imageName;
        
        Provider(int round, int heading, String name, String type, int regionID, String imageName) {
            if(name == null) {
                throw new IllegalArgumentException("Provider<init> - name cannot be null");
            } else if(type == null) {
                throw new IllegalArgumentException("Provider<init> - type cannot be null");
            } else if(imageName == null) {
                throw new IllegalArgumentException("Provider<init> - imageName cannot be null");
            }
            
            m_round = round;
            m_heading = heading;
            m_name = name;
            m_type = type;
            m_topRegionID = regionID;
            m_imageName = imageName;
        }
        
        
        //---------------------------------------------------------------------
        // SearchProvider ifc
        
        
        public String getProviderName() {
            return m_name;
        }
        
        
        public String getProviderType() {
            return m_type;
        }
        
        
        public String getProviderImageName() {
            return m_imageName;
        }


        int getRound() {
            return m_round;
        }
        

        int getTopRegionID() {
            return m_topRegionID;
        }
        
        
        int getHeadingID() {
            return m_heading;
        }
        
        
        String asString() {
            StringBuffer sb = new StringBuffer();
            sb.append("   Provider: ").append(m_name).append('\n');
            sb.append("   Type: ").append(getProviderType()).append('\n');
            sb.append("   Round: ").append(getRound()).append('\n');
            sb.append("   Imagename: ").append(getProviderImageName()).append('\n');
            sb.append("   Heading: ").append(m_heading).append('\n');
            sb.append("   TopRegionID: ").append(getTopRegionID()).append('\n');
            return sb.toString();
        }
    }
}
