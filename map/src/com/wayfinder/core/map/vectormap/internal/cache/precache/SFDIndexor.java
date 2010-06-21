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
package com.wayfinder.core.map.vectormap.internal.cache.precache;

public class SFDIndexor{
    
    /// The string of the Indexor, if read.
    byte[] m_str;
    /// The index of the string in the file
    int m_strNbr;
    /// The offset of the characters in the file
    int m_strOffset;
    /// Size allocated for strings
    int m_strAllocSize;
    /// True if the string has been read from file
    boolean m_stringRead;
    
    
    public SFDIndexor() /*extends SFDIndexorBase*/{
        m_str =  null;
        m_strNbr = Integer.MAX_VALUE;
        m_strOffset = Integer.MAX_VALUE;
        m_strAllocSize = 0;
        m_stringRead = false;
    }
    
    
    /// Less than operator. Compares only the strings.
    public boolean lessThan(String other ){
        return getStr().compareTo( other ) < 0;
    }
    
    /// Less than operator. Compares only the strings.
    public boolean lessThan(SFDIndexor other ){
        return this.lessThan(other.getStr());
    }
    
    /// Less than operator. Compares only the strings.
    public static boolean lessThan(String str1,SFDIndexor str2 ){
        return str1.compareTo( str2.getStr()) <0;
    }
    
    /// Returns the string number
    public int getStrNbr(){
        return m_strNbr;
    }
    
    /// True if the string has been read
    public boolean stringRead(){
        return m_stringRead;
    }
    
    
    
    public void copyContentsFrom(SFDIndexor other){
         m_str = new byte[other.m_str.length];
         System.arraycopy(other.m_str,0,m_str,0,m_str.length);
         this.m_strAllocSize = other.m_strAllocSize;
         this.m_strNbr = other.m_strNbr;
         this.m_strOffset = other.m_strOffset;
         this.m_stringRead = other.m_stringRead;
    }
    
    
    
    /**
     *   Swaps the contents of this SFDIndexor with the other
     *   one. Note that this is used when searching to swap
     *   with SFDFileIndexor in a special way.
     */
    public void swap( SFDIndexor other ){
    
       if(m_str == null){
           m_str = new byte[other.m_str.length];
           System.arraycopy(other.m_str,0,m_str,0,m_str.length);
       }else{
           byte[] temp_str = new byte[m_str.length];
           System.arraycopy(m_str,0,temp_str,0,m_str.length);
           System.arraycopy(other.m_str,0,m_str,0,m_str.length);
           System.arraycopy(temp_str,0,other.m_str,0,temp_str.length);
       }
        
        int temp = m_strNbr;
        m_strNbr = other.m_strNbr;
        other.m_strNbr = temp;
        
        temp = m_strOffset;
        m_strOffset = other.m_strOffset;
        other.m_strOffset = temp;
        
        temp = m_strAllocSize;
        m_strAllocSize = other.m_strAllocSize;
        other.m_strAllocSize = temp;
        
        boolean temp_bool = m_stringRead;
        m_stringRead = other.m_stringRead;
        other.m_stringRead = temp_bool;
        
    }
    
    //findbugs: not used
/*    private String strcpy(String src) {
        StringBuffer dest = new StringBuffer();
        int i, len = src.length();
        for (i = 0; i < len; i++)
            dest.setCharAt(i, src.charAt(i));
        return new String(dest);
    }*/
    
    
    /// Returns the string. Use only if stringRead.
    public String getStr() {
        if(stringRead()){
            return new String(m_str).trim();
        }else{
            return null;
        }
    }
    
}
