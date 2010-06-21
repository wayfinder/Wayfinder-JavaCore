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
package com.wayfinder.core.shared.poidetails;

/**
 * Represent an image gallery from a provider associated with a POI
 *    
 * 
 */
public class ImageGroup {
    
    private final String[] m_imageUrl;
    private final Provider m_provider;
    
    private final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * @param provider the {@link Provider} of the images must not be null
     * @param imageUrlInternalArray the array with the urls to the images 
     * can be null. 
     */
    public ImageGroup(Provider provider, String[] imageUrlInternalArray) {
        super();
        
        if (provider == null) { 
            throw new IllegalArgumentException("Ctr param provider cannot be null");
        }
        m_provider = provider;
        
        if (imageUrlInternalArray == null) {
            m_imageUrl = EMPTY_STRING_ARRAY;
        } else {
            m_imageUrl = imageUrlInternalArray;
        }
    }
    
    /**
     * Never return null
     * @return a {@link Provider} of the images 
     */
    public Provider getProvider() {
        return m_provider;
    }
    
    /**
     * @return the total number of images, 0 if there are none 
     */
    public int getNbrOfImages() {
        return m_imageUrl.length;
    }
    
    /**
     * @param index an positive index smaller than {@link #getNbrOfImages()} 
     * @return the full url of the image at given index 
     * e.g. "http://assets1.qype.com/uploads/photos/0007/7369/Noodle_Time_Greenwich_thumb.jpg"
     */
    public String getImageUrl(int index) {
        return m_imageUrl[index];
    }
    
    public String toString() { 
        return "ImageGroup " + m_provider  
                + " nbrOfImages: " + m_imageUrl.length;
    }
}
