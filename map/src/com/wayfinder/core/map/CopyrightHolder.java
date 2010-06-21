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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.map.util.BitBuffer;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.internal.Serializable;

public class CopyrightHolder implements Serializable{
    
       /**
        *   Table describing the parent / child relationship for the 
        *   CopyrightNotices. 
        */
    private BoxByParent[] boxesByParent;
        
        /**
         *   The copyright strings. The copyright id in the CopyrightNotice
         *   refers to the position in this vector for the copyright string.
        */
    private String[] copyrightStrings;
        
       /**
        *   The copyright boxes. The position in the vector refers to
        *   their respective id.
        */
    private CopyrightNotice[] copyrightBoxes;

    //The copyright head, i.e. "(c) Wayfinder"
    private String copyrightHead;
           
    /// The minimum coverage in percent for a copyright ID to be included.
    private int minCovPercent;
    
    public CopyrightHolder(){       
        copyrightHead = "";
        minCovPercent = 0;
    }

    private String iCrc = "";
    public CopyrightHolder(String aCrc){
        this();
        iCrc = aCrc;
    }

    public String getCRC(){
        return iCrc;
    }   

    /* (non-Javadoc)
     * @see wmmg.util.Serializable#read(java.io.DataInputStream)
     */
    public void read(DataInputStream din) throws IOException {
        iCrc = din.readUTF();

        int length = din.readInt();
        copyrightBoxes = new CopyrightNotice[length];
        for(int i = 0; i < length; i++){
            copyrightBoxes[i] = new CopyrightNotice();
            copyrightBoxes[i].read(din);
    }

        boxesByParent = new BoxByParent[length];
        for(int i = 0; i < length; i++){
            boxesByParent[i] = new BoxByParent();
            boxesByParent[i].read(din);
        }

        length = din.readInt();
        copyrightStrings = new String[length];
        for(int i = 0; i < length; i++){
            copyrightStrings[i] = din.readUTF();
        }

        minCovPercent = din.readInt();
        copyrightHead = din.readUTF();
        
        /* Read the map supplier id:s from the persistent storage. */
        if(din.available() > 0) {
            iMapSupplierIds = new int[copyrightStrings.length];
            for(int i=0; i<iMapSupplierIds.length; i++) {
                iMapSupplierIds[i] = din.readInt();
            }
        }
    }

    /* (non-Javadoc)
     * @see wmmg.util.Serializable#write(java.io.DataOutputStream)
     */
    public void write(DataOutputStream dout) throws IOException {
        dout.writeUTF(iCrc);

        dout.writeInt(copyrightBoxes.length);
        for(int i = 0; i < copyrightBoxes.length; i++) {
            copyrightBoxes[i].write(dout);
        }
        for(int i = 0; i < boxesByParent.length; i++){
            boxesByParent[i].write(dout);
        }

        dout.writeInt(copyrightStrings.length);
        for(int i = 0; i < copyrightStrings.length; i++)
            dout.writeUTF(copyrightStrings[i]);

        dout.writeInt(minCovPercent);
        dout.writeUTF(copyrightHead);
        
        /* Write the map supplier id:s to the persistent storage. */
        if(iMapSupplierIds != null) {
            for(int i=0; i<iMapSupplierIds.length; i++) {
                dout.writeInt(iMapSupplierIds[i]);
            }
        }
        
        //XXX LOG
        //System.out.println("Writing copyright strings finished!");
    }

    public boolean load( BitBuffer buf ) 
    {
       // Size of data.
//     int startPos = buf.getCurrentOffset();       
        //Dead store to local variable
//         int nbrBytes = (int) buf.nextInt();
           buf.nextInt();

        // Number of boxes.
        int nbrBoxes = (int) buf.nextInt();
        if (nbrBoxes > 0) {
            copyrightBoxes = new CopyrightNotice[nbrBoxes];
            for (int i = 0; i < nbrBoxes; ++i) {
                CopyrightNotice copyrightNotice = new CopyrightNotice();
                boolean res = copyrightNotice.load(buf);

                // System.out.println("Loading box: " + i + " " +
                // copyrightNotice.getBox().toString());
                // If loading went bad, clean up and return false.
                if (!res) {
                    copyrightBoxes = null;
                    copyrightNotice = null;
                    return false;
                }
                copyrightBoxes[i] = copyrightNotice;
            }

            // Load the boxes by parent table.
            boxesByParent = new BoxByParent[nbrBoxes];
            for (int i = 0; i < nbrBoxes; ++i) {
                // Parent.
                int parent = (int) buf.nextInt();
                // Box nbr.
                int boxNbr = (int) buf.nextInt();
                boxesByParent[i] = new BoxByParent(parent, boxNbr);
            }
            // Number of copyright ids and strings.
            int nbrCopyrightStrings = (int) buf.nextInt();
            copyrightStrings = new String[nbrCopyrightStrings];
            for (int i = 0; i < nbrCopyrightStrings; ++i) {
                copyrightStrings[i] = buf.nextStringUTF();
                // System.out.println("copyrightStrings " + i + " = " +
                // copyrightStrings[i]);
            }

            // The minimum coverage in percent for a copyright ID to be
            // included.
            minCovPercent = (int) buf.nextInt();

            // The copyright head, i.e. "(c) Wayfinder"
            copyrightHead = buf.nextStringUTF();
            
            /* Load the map supplier id:s from the buffer. */
            if(buf.getNbrBytesLeft() > 0) {
                iMapSupplierIds = new int [nbrCopyrightStrings];
                for(int i=0; i<nbrCopyrightStrings; i++) {
                    iMapSupplierIds[i] = (int)buf.nextInt();
                }
            }
        }else{
            //XXX LOG
            //System.out.println("No copyright box!");
        }
        return true;
    }
    
    BoxByParent[] getBoxesByParent(){
        return boxesByParent;
    }
    
    String[] getCopyrightStrings(){
        return copyrightStrings;
    }
    
    CopyrightNotice[] getCopyrightBoxes(){
        return copyrightBoxes;
    }
    
    private int []iMapSupplierIds;
    
    /**
     * @return a array with the map supplier id for 
     * the current available map supplier that we use. 
     */
    int []getMapSupplierIconIds() {
        return iMapSupplierIds;
    }

    public String getCopyrightHeader(){
        return copyrightHead;
    }
    
    public int getMinCovPercent(){
        return minCovPercent;
    }
} 

    class CopyrightNotice {
    
    protected BoundingBox boundingBox = null;

    protected int copyrightId = 0;
    
    public boolean load(BitBuffer buf)
    {
       // Size of data.
//     int startPos = buf.getCurrentOffset();       
        
        //Dead store to local variable
//     int nbrBytes = (int) buf.nextInt();
       buf.nextInt();
       
       // Copyright ID.
       copyrightId = (int) buf.nextInt();
       
       // Bounding box.
       int maxLat = (int) buf.nextInt();
       int minLon = (int) buf.nextInt();
       int minLat = (int) buf.nextInt();
       int maxLon = (int) buf.nextInt();
       
       //System.out.println("copyrightID is: " + copyrightID + ", maxLat is: "
        //     + maxLat + ", minLon is: " + minLon + ", minLat is: " + minLat + ", maxLon is: " + maxLon );
       
       boundingBox = new BoundingBox(maxLat, minLat, maxLon, minLon);

       // Skip bytes at the end.
//     int bytesLeft = nbrBytes - ( buf.getCurrentOffset() - startPos );
//     buf.skip( bytesLeft );
       return true;
    }

    public void read(DataInputStream din) throws IOException {
        copyrightId = din.readInt();
        boundingBox = new BoundingBox();
        boundingBox.read(din);
    }

    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(copyrightId);
        boundingBox.write(dout);
    }


    public int getCopyrightId()
    {
       return copyrightId;
    }

    public boolean isOverlappingBox() 
    {
       return getCopyrightId() == Integer.MAX_VALUE;
    }

    public BoundingBox getBox()
    {
       return boundingBox;
    }

}
    /**
    *  The key is the parent id, and it's value refers to the child id. The id are the position in 
    *   the copyrightBoxes vector. 
    *   
    *   Parent id Integer.MAX_VALUE means that there is no parent,
    *   i.e. the box is an overlap box.
    */
    class BoxByParent{
        int iParentBoxID;
        int iChildBoxID;

    public BoxByParent(){}
        
        public BoxByParent(int aParentBoxID, int aChildBoxID){
            iParentBoxID = aParentBoxID;
            iChildBoxID = aChildBoxID;
        }
        
        public int getParentID(){
            return iParentBoxID;
        }
        
        public int getChildID(){
            return iChildBoxID;
    }

    public void read(DataInputStream din) throws IOException {
        iParentBoxID = din.readInt();
        iChildBoxID = din.readInt();
    }

    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(iParentBoxID);
        dout.writeInt(iChildBoxID);
        }
        
    }
