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
package com.wayfinder.core.map.vectormap.internal.control;

import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;


/**
 * Class for triangulation and storing of polygons
 */
public class ConcavePolygon {
    
    private static final Logger LOG = LogFactory
            .getLoggerForClass(ConcavePolygon.class);
    
    private short[][] ibuff;
    private int size = 0;
    private int[] vbuff;
    private boolean isReversed = false;
    private int startIndex;
    private int length;
    
    /**
     * Creates a polygon object from a specified vertex buffer
     *
     * @param vbuff Vertex buffer (x1,y1,x2,y2,...,xn,yn)
     */
    ConcavePolygon(int[] vbuff, int startIndex, int length) {
        this.vbuff = vbuff;
        this.startIndex = startIndex;
        this.length = length;
        size = (length >> 1);
        ibuff = new short[size-2][3];
    }
    
    /**
     * Determines if a polygon is specified clockwise or counter-clockwise.
     *
     * @return true if clockwise
     */
    public boolean isClockwise(){
        float crossSum = 0;
        int[]startLine = {vbuff[this.startIndex + 2] - vbuff[this.startIndex + 0] , vbuff[this.startIndex + 3] - vbuff[this.startIndex + 1]};
        float[] normStartLine = Utils.normalize(startLine);
        float[] normLine1 = normStartLine;
        float[] normLine2;
        
        for(int i=0;i<this.length-2;i+=2){
            int[]line2 = {vbuff[this.startIndex + ((i+4)%this.length)] - vbuff[this.startIndex + ((i+2)%this.length)] , vbuff[this.startIndex + ((i+5)%this.length)] - vbuff[this.startIndex + ((i+3)%this.length)]};
            normLine2 = Utils.normalize(line2);
            float add = cross2D(normLine1,normLine2);
            crossSum += add;
            normLine1 = normLine2;
        }
        crossSum += cross2D(normLine1,normStartLine);
        return crossSum <= 0;
    }
    
    /**
     * Calculates the cross product for two vectors in 2D space.
     * For float arrays.
     *
     * @param vNorm 1st vector
     * @param uNorm 2nd vector
     * @return the cross product
     */
    public static float cross2D(float[]vNorm, float[]uNorm){
        return vNorm[0] * uNorm[1] - vNorm[1] * uNorm[0];
    }
    
    /**
     * Calculates the cross product for two vectors in 2D space.
     * For int arrays.
     *
     * @param v 1st vector
     * @param u 2nd vector
     * @return the cross product
     */
    public static float cross2D(int[]v,int[]u){
        float[] vNorm = Utils.normalize(v);
        float[] uNorm = Utils.normalize(u);
        return vNorm[0] * uNorm[1] - vNorm[1] * uNorm[0];
    }
    
    
    /**
     * Reverses the order of the vertices for this polygon
     *
     */
    public void reverseVertices(){
        int[]tempbuff = new int[this.length];
        for(int i = this.length-2 ;i>=0;i=i-2){
            tempbuff[i] = vbuff[this.startIndex + this.length-2-i];
            tempbuff[i+1] = vbuff[this.startIndex + this.length-1-i];
        }
        vbuff = tempbuff;
        isReversed = true;
        this.startIndex = 0;
    }
    
    /**
     * Triangulates this polygon
     *
     * @return true if triangulation was successful
     */
    public boolean triangulate() {
        final int len = this.length;
        if (len < 6) {
            return true;
        } else {
            final short[][] ibuff = this.ibuff;
            if (len == 6) {
                ibuff[0][0] = 0;
                ibuff[0][1] = 1;
                ibuff[0][2] = 2;
                return true;
            } else if(len==8) {
                ibuff[0][0] = 0;
                ibuff[0][1] = 1;
                ibuff[0][2] = 2;
                ibuff[1][0] = 2;
                ibuff[1][1] = 3;
                ibuff[1][2] = 0;
                return true;
            }
        
            int indicesSize = size;
            short[] indices = new short[indicesSize];
            for(int i = 0; i < indicesSize; i++) {
                indices[i] = (short)i;
            }
            short cIndex = 0;
            int oldLength=0;
            int count = 0;
            int currIdx = 0;
            while(indicesSize >= 4) {
                if(indicesSize == oldLength)
                    count++;
                else
                    count=0;
                oldLength = indicesSize;
                
                //Should ONLY happen if the triangulation is not possible
                if(count>size) {               
                    if(LOG.isTrace()) {
                        LOG.trace("ConcavePolygon.triangulate()", "\n  Triangulation failed  ");
                        LOG.trace("ConcavePolygon.triangulate()", "It was reversed: " + isReversed);
                        LOG.trace("ConcavePolygon.triangulate()", "For Matlab:");
                        StringBuffer sb = new StringBuffer("x=[");
                        for(int i=0; i<this.length; i+=2) {   
                            sb.append(vbuff[startIndex + i]+ " ");
                        }
                        sb.append("]");
                        LOG.trace("ConcavePolygon.triangulate()", sb.toString());
                        sb = new StringBuffer("y=[");
                        for(int i=0; i<this.length; i+=2) {
                            sb.append(vbuff[this.startIndex + i+1] + " ");
                        }
                        sb.append("]");
                        LOG.trace("ConcavePolygon.triangulate()", sb.toString());
                    }
                    return false;
                }
                
                short last = indices[(cIndex-1+indicesSize)%indicesSize],
                    index = indices[cIndex],
                    next = indices[(cIndex+1+indicesSize)%indicesSize];
                
                try{
                    if(isConvex(last,index,next)) {
                        boolean ear = true;
                        for(int i=0; i<indicesSize-3; i++) {
                            if(isInside(last, index, next, indices[((cIndex+2+i) % indicesSize)])) {
                                ear = false;
                                break;
                            }
                        }
                        if(ear) {
                            for(int i=0; i<indicesSize; i++) {
                                if(i<cIndex) {
                                    indices[i] = indices[i];
                                } else if(i>cIndex){
                                    indices[i-1] = indices[i];
                                }
                            }
                            
                            ibuff[currIdx][0] = last;
                            ibuff[currIdx][1] = index;
                            ibuff[currIdx][2] = next;
                            currIdx++;
                                                  
                            indicesSize --;
                            
                            cIndex = (short)((cIndex-1+indicesSize)%indicesSize);
                        } else {
                            cIndex = (short)((cIndex+1)%indicesSize);
                        }
                    } else {
                        cIndex = (short)((cIndex+1)%indicesSize);
                    }
                } catch(Exception e ) {
                    if(LOG.isError()) {
                        LOG.error("ConcavePolygon.triangulate()", e);
                        LOG.error("ConcavePolygon.triangulate()", "vbuff.start "+this.startIndex);
                        LOG.error("ConcavePolygon.triangulate()", "length "+this.length);
                        LOG.error("ConcavePolygon.triangulate()", "vbuff.length "+vbuff.length);
                        LOG.error("ConcavePolygon.triangulate()", "last "+last);
                        LOG.error("ConcavePolygon.triangulate()", "index "+index);
                        LOG.error("ConcavePolygon.triangulate()", "next "+next);
                    }
                }
            }
            
            if(indicesSize >= 3) {
                ibuff[currIdx][0] = indices[0];
                ibuff[currIdx][1] = indices[1];
                ibuff[currIdx][2] = indices[2];
            }
        }
        
        return true;
    }
    
    /**
     * Returns the indexbuffer for this polygon
     *
     * @return the indexbuffer
     */
    public short[][] getIndexBuffer(){
        return ibuff;
    }
    
    private boolean isInside(short p1, short p2, short p3, short p) {
        final int[] vbuff = this.vbuff;
        final int startIndex = this.startIndex;
        int c1x = vbuff[startIndex + p1 + p1];
        int c1y = vbuff[startIndex + p1 + p1 + 1];
        int c2x = vbuff[startIndex + p2 + p2];
        int c2y = vbuff[startIndex + p2 + p2 + 1];
        int c3x = vbuff[startIndex + p3 + p3];
        int c3y = vbuff[startIndex + p3 + p3 + 1];
        int cx =  vbuff[startIndex + p + p];
        int cy =  vbuff[startIndex + p + p + 1];
        
        if(((c2x-c1x)*(cy-c1y) - (c2y-c1y)*(cx-c1x)) <= 0) { return false; }
        if(((c3x-c2x)*(cy-c2y) - (c3y-c2y)*(cx-c2x)) <= 0) { return false; }
        if(((c1x-c3x)*(cy-c3y) - (c1y-c3y)*(cx-c3x)) <= 0) { return false; }
        return true;
    }
    
    private boolean isConvex(int last, int i, int n) {
        final int[] vbuff = this.vbuff;
        final int startIndex = this.startIndex;
        int lastx = (vbuff[startIndex + (i<<1)]-vbuff[startIndex + (last<<1)]);
        int thisy = (vbuff[startIndex + (n<<1)+1]-vbuff[startIndex + (i<<1)+1]);
        int lasty = (vbuff[startIndex + (i<<1)+1]-vbuff[startIndex + (last<<1)+1]);
        int thisx = (vbuff[startIndex + (n<<1)]-vbuff[startIndex + (i<<1)]);
        int value = (lastx*thisy - lasty*thisx);
        return value >= 0;
    }
    
}
