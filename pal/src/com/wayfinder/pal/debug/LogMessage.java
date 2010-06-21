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

public final class LogMessage {
    
    public static final int TYPE_MESSAGE   = 0;
    public static final int TYPE_EXCEPTION = 1;
    
    private final int m_type;
    private final Level m_level;
    private final String m_methodName;
    private final String m_message;
    private final Throwable m_thrown;
    
    
    /**
     * Creates a message based LogMessage
     * 
     * @param level The {@link Level} of the message
     * @param methodName The name of the method the message originates from
     * @param message The actual logging message
     */
    public LogMessage(Level level, String methodName, String message) {
        m_type = TYPE_MESSAGE;
        m_level = level;
        m_methodName = methodName;
        if(message == null) {
            message = "[NULL]";
        }
        m_message = message;
        m_thrown = null;
    }
    
    
    /**
     * Creates a LogMessage containing a caught exception
     * 
     * @param level The {@link Level} of the message
     * @param methodName The name of the method the message originates from
     * @param t The {@link Throwable} to report
     */
    public LogMessage(Level level, String methodName, Throwable t) {
        m_type = TYPE_EXCEPTION;
        m_level = level;
        m_methodName = methodName;
        m_message = null;
        m_thrown = t;
    }
    

    /**
     * Returns the type of the message
     * 
     * @return One of the TYPE constants in this class
     */
    public int getType() {
        return m_type;
    }

    
    /**
     * Returns the level of the message
     * 
     * @return A {@link Level} object
     */
    public Level getLevel() {
        return m_level;
    }
    
    
    /**
     * Returns the name of the method the message originates from
     * 
     * @return The name of the method
     */
    public String getMethodName() {
        return m_methodName;
    }

    
    /**
     * 
     * 
     * @return
     */
    public String getMessage() {
        if(m_type != TYPE_MESSAGE) {
            throw new IllegalStateException("LogMessage is not of type message");
        }
        return m_message;
    }
    

    public Throwable getThrowable() {
        if(m_type != TYPE_EXCEPTION) {
            throw new IllegalStateException("LogMessage is not of type exception");
        }
        return m_thrown;
    }
    
}
