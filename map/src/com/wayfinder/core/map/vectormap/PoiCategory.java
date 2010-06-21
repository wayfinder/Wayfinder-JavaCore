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

package com.wayfinder.core.map.vectormap;

/**
 * Class used to hold the information need to be able to change the visibility of the
 * poi categories used in the map. 
 * 
 */
public class PoiCategory {
    
    private boolean m_enable;
    private String m_name;
    
    /**
     * Create a new POI Category. <p>
     * 
     * <b>Note that</b> there are not possible to create a new poi category from outside the map. 
     * The poi categories available will be provided via the 
     * {@link MapDetailedConfigInterface#getPoiCategories()} method. 
     * 
     * @param name the name of the poi category. 
     * @param enable true if visible, false if not. 
     */
    public PoiCategory(String name, boolean enable) {
        m_name = name;
        m_enable = enable;
    }
    
    /**
     * Return the name of the poi category. 
     * 
     * @return the name of the poi category.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Return true if the poi category is visible.
     * 
     * @return true if the poi category is visible, false if not. 
     */
    public boolean isEnable() {
        return m_enable;
    }
    
    /**
     * Set the visibility of a poi category. 
     * 
     * @param enable true for visible, false to hide the poi category. 
     */
    public void setEnable(boolean enable) {
        m_enable = enable;
    }

}
