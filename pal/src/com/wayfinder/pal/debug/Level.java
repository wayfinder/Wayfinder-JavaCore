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
package com.wayfinder.pal.debug;

public final class Level {
    
    public static final int VALUE_NONE  = Integer.MAX_VALUE;
    public static final int VALUE_FATAL = 1000;
    public static final int VALUE_ERROR = 800;
    public static final int VALUE_WARN  = 600;
    public static final int VALUE_INFO  = 400;
    public static final int VALUE_DEBUG = 200;
    public static final int VALUE_TRACE = 0;
    
    public static final Level NONE = new Level(VALUE_NONE, "NONE");
    public static final Level FATAL = new Level(VALUE_FATAL, "FATAL");
    public static final Level ERROR = new Level(VALUE_ERROR, "ERROR");
    public static final Level WARN = new Level(VALUE_WARN, "WARN");
    public static final Level INFO = new Level(VALUE_INFO, "INFO");
    public static final Level DEBUG = new Level(VALUE_DEBUG, "DEBUG");
    public static final Level TRACE = new Level(VALUE_TRACE, "TRACE");

    private final int m_levelInt;
    private final String m_levelString;

    /**
     * Create a <code>Level</code> object.
     * 
     * @param level
     *            the level to create. This should be set using one of the
     *            constants defined in the class.
     * @param levelString
     *            the <code>String</code> that shall represent the level. This
     *            should be set using one of the defined constants defined in
     *            the class.
     */
    Level(int level, String levelString) {
        m_levelInt = level;
        m_levelString = levelString;
    }

    /**
     * Return the integer level for this <code>Level</code>.
     * 
     * @return the integer level.
     */
    public int getIntValue() {
        return m_levelInt;
    }

    /**
     * Return a <code>String</code> representation for this <code>Level</code>.
     * 
     * @return a <code>String</code> representation for the <code>Level</code>.
     */
    public String toString() {
        return m_levelString;
    }   

    
    /**
     * A check to see if this level is higher or equal to the level passed into
     * the method
     * 
     * @param level A {@link Level} to compare with
     * @return true if and only if this level is higher or equal to the level
     * passed into the method
     */
    public boolean isHigherOrEqualTo(Level level) {
        return m_levelInt >= level.m_levelInt;
    }
    
    
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) { 
        if(obj instanceof Level) {
            // just use the hashcode to avoid a typecast
            return m_levelInt == obj.hashCode();
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_levelInt;
    }
}
