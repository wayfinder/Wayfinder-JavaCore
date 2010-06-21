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
package com.wayfinder.core.map;

import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.internal.WFCollections;
import com.wayfinder.core.shared.internal.WFComparable;
import com.wayfinder.pal.graphics.WFFont;

public class CopyrightHandler {
    

    // The copyright holder.
    CopyrightHolder m_copyrightHolder;
    
    // Static copyright string. Empty if no static copyright must be used.
    String m_staticCopyrightString;
       
    // The current (last) copyright string used.
    String m_curCopyrightString;
    
    /**
     * Constructs a CopyRightHandler
     */
    public CopyrightHandler() {
        m_copyrightHolder = null;
        m_staticCopyrightString = "";
        m_curCopyrightString = "";
    }

    /**
     * Set the copyright holder
     * 
     * @param copyrightHolder the copyright holder
     */
    public void setCopyrightHolder(CopyrightHolder copyrightHolder) {
        m_copyrightHolder = copyrightHolder;
               
        /*
         * If the tmfd contains a copyright holder but not any copyright boxes something is
         * wrong! Please note which server you are running against and report it to the
         * server team. 
         * 
         * No copyright string will be shown and hopefully someone will notice that when testing. 
         * 
         */
        if (copyrightHolder != null && copyrightHolder.getCopyrightBoxes()== null) {
            setStaticCopyrightString("");
            m_copyrightHolder = null;
        }
    }
    
    /**
     * Get the copyright holder
     * 
     * @return the copyright holder
     */
    public CopyrightHolder getCopyrightHolder(){
        return m_copyrightHolder;
    }   
    
    /**
     * Get the map suppler id for the largest map suppler inside the specified
     * screen bounding box.
     * 
     * @param screenBox  bounding box of the screen
     */
    public int getMapSupplierId(BoundingBox screenBox) {
        CovByCopyrightId []covByCopyrightId = caluclateCovByCopyrightId(screenBox);
        int id = covByCopyrightId[covByCopyrightId.length-1].getCopyrightId();    
        int []supplierIconIds = m_copyrightHolder.getMapSupplierIconIds();        
        int supplierID = -1; 
            
        if (supplierIconIds != null) {
            supplierID = supplierIconIds[id];       
        }
        
        return supplierID;
    }
    
    private CovByCopyrightId[] caluclateCovByCopyrightId(BoundingBox screenBox) {
        int size = m_copyrightHolder.getCopyrightStrings().length;  
        CovByCopyrightId[] covByCopyrightIds = new CovByCopyrightId[size];
        for (int i = 0; i < size ;i++)
            covByCopyrightIds[i] = new CovByCopyrightId(0, i);

        BoxByParent[] boxesByParent = m_copyrightHolder.getBoxesByParent();

        int childId = 0;
        BoxByParent boxByParent = null;
        for (int j = 0; j < boxesByParent.length; j++) {
            // Get the root boxes. (Parent is Integer.MAX_VALUE)
            boxByParent = (BoxByParent) boxesByParent[j];
            if (boxByParent.getParentID()== Integer.MAX_VALUE){
                childId = boxByParent.getChildID();
                recursiveCheckCoverage(screenBox, childId, covByCopyrightIds);
            }
        }

        // Will be sorted in ascending order by coverage area.
        WFCollections.sort(covByCopyrightIds);
        
        return covByCopyrightIds;
    }

    /**
     * Get the copyright string for the specified screen bounding box.
     * 
     * @param screenBox  the bounding box of the area
     * @param font  font to be used for the copyright string
     * @param screenWidth  width of the screen
     * 
     * @return the copyright string or an empty string if no copyright area was found.
     */
    public String getCopyrightString(BoundingBox screenBox, WFFont font, int screenWidth){
        if (m_copyrightHolder == null) {
            m_curCopyrightString = m_staticCopyrightString;          
            return m_curCopyrightString;
        }

        int size = m_copyrightHolder.getCopyrightStrings().length;
        CovByCopyrightId []covByCopyrightIds = caluclateCovByCopyrightId(screenBox);
        
        // The result should be in covByCopyrightIds.
        float totalArea = 0;
        for (int i = 0; i < size; ++i ) {
            totalArea += covByCopyrightIds[i].getCovArea();
        }

        if (totalArea < 0.000001) {
            // No copyright area found. Return to avoid divide by zero.
            return "";
        }

        m_curCopyrightString = m_copyrightHolder.getCopyrightHeader();      
        String str = "";
        int strWidth = font.getStringWidth(m_curCopyrightString);
        for (int i = size - 1; i >= 0; i--) {
            CovByCopyrightId covByCopyright = covByCopyrightIds[i];
            int percent = (int)(covByCopyright.getCovArea() / totalArea * 100);
            if (percent >= m_copyrightHolder.getMinCovPercent()){ 
                str = (String) m_copyrightHolder.getCopyrightStrings()[covByCopyright.getCopyrightId()];
                strWidth += font.getStringWidth("," + str);

                if (strWidth > screenWidth) {
                    break;
                }
                // Store this copyright string.
                m_curCopyrightString = m_curCopyrightString + "," + str;
            }
        }
        
        return m_curCopyrightString;
    }
      

    /**
     * Set the static copyright string to use in case it's not possible
     * to create a dynamic copyright string.
     * 
     * @param copyrightString  the static copyright string
     */
    public void setStaticCopyrightString(String copyrightString){
        m_staticCopyrightString = copyrightString;
        // Remove reference to any old holder from unused tmfd.
        // I.e. allow old tmfd to be garbage collected.
        m_copyrightHolder = null;
    }

    /**
     * Recursive method for checking map supplier coverage.
     */
    float recursiveCheckCoverage(BoundingBox screenBox, int id, CovByCopyrightId[] covByCopyrightIds) {       
        CopyrightNotice curNotice = (CopyrightNotice) m_copyrightHolder
                .getCopyrightBoxes()[id];
        BoundingBox intersection = new BoundingBox();
        
        if (curNotice.getBox().getIntersection(screenBox, intersection)) {
            // Calculate the coverage as area.
            float coverage = (float) intersection.getLonDiff()
                    * (float) intersection.getHeight();
            float covForChildren = 0;
            int childId = 0;
            BoxByParent[] boxesByParent = m_copyrightHolder.getBoxesByParent();
            
            for (int i = 0; i < boxesByParent.length; i++) {
                BoxByParent boxByParent = (BoxByParent) boxesByParent[i];
                if (boxByParent.getParentID() == id) {
                    childId = boxByParent.getChildID();
                    covForChildren += recursiveCheckCoverage(intersection,
                            childId, covByCopyrightIds);

                    if (covForChildren - coverage == 0) {
                        // System.out.println("Break (copyright)");
                        break;
                    }
                }
            }

            if (curNotice.isOverlappingBox()) {
                // Box is only used as bounding box for children and
                // does not contain any copyright info of it's own.
                // Return the copyright coverage for the children.
                return covForChildren;
            }

            // Calculate the current coverage,
            // by subtracting any coverage already covered by a child.
            float curCov = coverage - covForChildren;

            // Update coverage.
            covByCopyrightIds[curNotice.getCopyrightId()].m_covArea += curCov;

            return coverage;
        }
        // else, no overlapping
        return 0;
    }
    
}

class CovByCopyrightId implements WFComparable {
    
    public float m_covArea;
    public int m_copyrightId;
    
    public CovByCopyrightId(){
        m_covArea = 0;
        m_copyrightId = 0;
    }
    
    public CovByCopyrightId(float covArea, int copyrightId) {
        m_covArea = covArea;
        m_copyrightId = copyrightId;                    
    }
    
    public float getCovArea() {
        return m_covArea;            
    }
    
    public int getCopyrightId() {
        return m_copyrightId;
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.WFComparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (m_covArea - ((CovByCopyrightId) o).m_covArea == 0) {
            return 0;
        } else if (m_covArea < ((CovByCopyrightId) o).m_covArea) {
            return -1;
        } else {
            return 1;
        }
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.WFComparable#compareTo(java.lang.Object, int)
     */
    public int compareTo(Object o, int method) {
        return compareTo(o);
    }
    
}


