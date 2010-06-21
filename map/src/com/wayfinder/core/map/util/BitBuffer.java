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
package com.wayfinder.core.map.util;

import java.io.UnsupportedEncodingException;

import com.wayfinder.core.shared.internal.UTF8CStringBufferParser;


public class BitBuffer {
    private byte[] buffer;
    private int index;
    private int bitMask;
    private UTF8CStringBufferParser csparser;
    
    public BitBuffer(byte[] buffer) {
        this.buffer = buffer;
        reset();
    }
    
    public BitBuffer(int size){
        this.buffer = new byte[size];
        reset();
    }
    
    public byte[] getByteArray(){
        return buffer;
    }
    
    public void alignToByte(){
        if ( bitMask != 0x80 ) {
            // Not done writing bits to this byte.
            // Skip to next one.
            index++;
        }
        bitMask = 0x80;
    }
    
    
    public void writeNextString(String s) {
        //    assertByteAligned();
        
        for(int pos=0; pos<s.length();pos++){
            writeNextBits(s.charAt(pos),8);
        }
        writeNextBits(0,8);
        
    }
    
    public void writeNextBits(int value, int nbrOfBits){
        value <<= (32-nbrOfBits);
        while (nbrOfBits-- !=0) {
            writeNextBit( (value & 0x80000000) != 0 );
            value <<= 1;
        }
    }
    
    public void writeNextBit(boolean value) {
        if (value) {
            buffer[index] |= bitMask;
        } else {
            buffer[index] &= (0xff ^ bitMask);
        }
        bitMask >>= 1;
        if ( bitMask == 0x00 ) {
            bitMask = 0x80;
            index++;
        }
    }
    
    
    public int getCurrentBitOffset(){
        int nbrBits = index << 3;
        switch ( bitMask ) {
//          case ( 0x80 ) :
//              return nbrBits;
            case ( 0x40 ) :
                return nbrBits + 1;
            case ( 0x20 ) :
                return nbrBits + 2;
            case ( 0x10 ) :
                return nbrBits + 3;
            case ( 0x08 ) :
                return nbrBits + 4;
            case ( 0x04 ) :
                return nbrBits + 5;
            case ( 0x02 ) :
                return nbrBits + 6;
            case ( 0x01 ) :
                return nbrBits + 7;
            default:                
                return nbrBits;
        }
    }
    
    public int getCurrentByteOffset(){
        return index;
    }
    
    public void reset(){
        index  = 0;
        bitMask = 0x80;
        csparser = new UTF8CStringBufferParser(buffer);
    }
    
    public void softReset() {   
        index  = 0;
        bitMask = 0x80;
        for(int i=0; i<buffer.length; i++) {
            buffer[i] = 0;
        }
    }
    
    public int nextBit(){   // Try to avoid using this
        boolean retVal =  ((buffer[index] & bitMask) != 0);
        bitMask >>= 1;
        if ( bitMask == 0x00 ) {
            bitMask = 0x80;
            index++;
        }
        return retVal ? 1 : 0;
    }
    
    public int nextBits(int nbrOfBits){
        int value = 0;
        for (int i = 0; i < nbrOfBits; i++) {
            value <<= 1;
            value |= nextBit();
        }
        return value;
    }
    
    public byte[]nextByteArray(int nbrOfBytes){
        byte[]toReturn = new byte[nbrOfBytes];
        System.arraycopy(buffer,index,toReturn,0,nbrOfBytes);
        index+=nbrOfBytes;
        return toReturn;
    }
    
    
    
    public long nextInt(){
        int firstByte = (0x000000FF & ((int)buffer[index]));
        int secondByte = (0x000000FF & ((int)buffer[index+1]));
        int thirdByte = (0x000000FF & ((int)buffer[index+2]));
        int fourthByte = (0x000000FF & ((int)buffer[index+3]));
        index = index+4;
        long anUnsignedInt  = ((long) (firstByte << 24
            | secondByte << 16
            | thirdByte << 8
            | fourthByte))
            & 0xFFFFFFFFL;
        return anUnsignedInt;
    }
    
    public char nextShort(){
        int firstByte = (0x000000FF & ((int)buffer[index]));
        int secondByte = (0x000000FF & ((int)buffer[index+1]));
        index = index+2;
        char anUnsignedShort  = (char) (firstByte << 8 | secondByte);
        return anUnsignedShort;
    }
    
    public short nextByte(){
        int firstByte = (0x000000FF & ((int)buffer[index]));
        index++;
        short anUnsignedByte = (short)firstByte;
        return anUnsignedByte;
    }
    

    public int nextSignedBits(int nbrOfBits){
        int value = 0;
        if(nextBit() == 1) {
            // mark all bits as 1
            value = 0xffffffff;
        }
        nbrOfBits--; // already read one
        value <<= nbrOfBits;
        return value | nextBits(nbrOfBits);
    }
    
    
    public int nextSignedByte(){
        int value =0;
        value = buffer[index];
        index++;
        return value;
    }
        
    public int checkNextByte(){
        int value =0;
        value = buffer[index];
        return value;
    }
    
    /**
     * 
     */
    public String nextString(){
//      StringBuffer sb = new StringBuffer();
//      while((char)buffer[index] != '\0'){
//          sb.append((char) buffer[index] );
//          index++;
//      }
//      index++;
//      return sb.toString();
        return nextStringUTF();
    }
    
    public String nextStringUTF(){
            String s = csparser.getNextString(index);
            index = csparser.getNextOffset();
            return s;
    } // nextStringUTF

    
    public void skip(int nbrOfBytes){
        for(int i=0;i<nbrOfBytes;i++){
            index++;
        }
    }
    
    public int getNbrBytesLeft() {
        return buffer.length - index;
    }
    
    public int getCurrentOffset(){
        return index;
    }
    
    public int getBufferSize(){
        return buffer.length;
    }
    
    public int size(){
        return buffer.length;
    }

}
