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
package com.wayfinder.core.map.vectormap.internal.process;

import java.util.*;

import com.wayfinder.core.map.util.BitBuffer;

public class TileImportanceTable {
    
    private Vector notices;
    
    public TileImportanceTable() {
        notices=new Vector();
    }
    
    private void clear(){
        notices=new Vector();
    }    
    
    public void load(BitBuffer bitBuffer){
        clear();
        char nbrElems = bitBuffer.nextShort();
        for ( char i = 0; i < nbrElems; i++ ) {
            TileImportanceNotice notice = new TileImportanceNotice();
            notice.load(bitBuffer);
            notices.addElement(notice);
        }
        buildMatrix();
    }
    
    public int getNbrImportanceNbrs( int scale, int detailLevel ){
        int nbrImportance = 0;
        for ( int i=notices.size()-1; i>=0; i-- ) {
            TileImportanceNotice notice = (TileImportanceNotice) notices.elementAt(i);
            if(notice.getMaxScale() >= scale) {
                if(notice.getDetailLevel() == detailLevel || notice.getThreshold() == 4294967295L ) {
                    nbrImportance++;
                } 
            } else {
            
                return nbrImportance;
            }
        }
        return nbrImportance;
    }
   

    public TileImportanceNotice getImportanceNbrSlow( int importanceNbr, int detailLevel ){
       int count = -1;
    
       for(int i=notices.size()-1; i>=0; i-- ) {
           TileImportanceNotice notice = (TileImportanceNotice) notices.elementAt(i);
           if(notice.getDetailLevel() == detailLevel || notice.getThreshold() == 4294967295L) {
               ++count;
               if(count == importanceNbr) {
                   return notice;
               }
          }
       }
       return null;
    }
 
    private TileImportanceNotice[][] importanceMatrix;
    
    private void buildMatrix(){
        
        if(notices.size()==0) {
            return;
        }
        
        int nbrDetailLevels = (int) ((TileImportanceNotice)notices.lastElement()).getDetailLevel()+1;
        importanceMatrix = new TileImportanceNotice[nbrDetailLevels][];

        for ( int i = 0; i < nbrDetailLevels; i++ ) {
            int nbrImportances = getNbrImportanceNbrs( 0, i );
            TileImportanceNotice[] vecPtr = new TileImportanceNotice[nbrImportances];            
            for ( int j = 0; j < nbrImportances; j++ ) {
                // Use the slow method.
                vecPtr[j] = getImportanceNbrSlow( j, i );
            }
            importanceMatrix[i] = vecPtr;
        }
    }
    
    public TileImportanceNotice getImportanceNotice(int importanceNbr, int detailLevel) {       
        //return importanceMatrix[importanceNbr][detailLevel]; 
        return importanceMatrix[detailLevel][importanceNbr];
    }

}

