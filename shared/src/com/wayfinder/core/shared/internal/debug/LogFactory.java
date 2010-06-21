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
package com.wayfinder.core.shared.internal.debug;

import java.util.Hashtable;

import com.wayfinder.pal.PAL;
import com.wayfinder.pal.debug.Level;
import com.wayfinder.pal.debug.LogHandler;

public final class LogFactory {
    
    private static Logger s_defaultLogger;
    private static LogHandler s_logHandler;
    private static final Hashtable LOGGABLE_CLASSES;
    private static boolean s_logging_activated;
    
    static {
        s_logging_activated = false;
        LOGGABLE_CLASSES = new Hashtable();
        s_defaultLogger = new FakeLogger();
    }
    
    private LogFactory() {}
    
    
    /**
     * Initializes the global logging framework
     * 
     * @param handler The {@link LogHandler} from the {@link PAL}
     */
    public static void initLogFrameWork(LogHandler handler, Level defaultLevel) {
        s_logHandler = handler;
        s_defaultLogger = new RealLogger(handler, defaultLevel);
        s_logging_activated = true;
    }
    
    
    static void resetLogFramework() {
        // for testing purposes only
        s_logHandler = null;
        LOGGABLE_CLASSES.clear();
    }
    
    
    /**
     * Obtains the default logger for the system.
     * 
     * @return A {@link Logger} with the default level set.
     */
    public static Logger getDefaultLogger() {
        return s_defaultLogger;
    }
    
    
    /**
     * Returns a {@link Logger} with a custom level set
     * 
     * @param level One of the LEVEL constants from this class
     * @return A {@link Logger} with the specified level set
     */
    public static Logger getLoggerWithLevel(Level level) {
        return new RealLogger(s_logHandler, level);
    }
    
    
    public static void overrideLoglevelForClass(Class clazz, Level level) {
        LOGGABLE_CLASSES.put(clazz, level);
    }
    
    
    static void removeClassOverride(Class clazz) {
        LOGGABLE_CLASSES.remove(clazz);
    }
    
    
    /**
     * Returns a specialized logger for a class
     * 
     * @param clazz The {@link Class} of the class to debug
     * @return A {@link Logger}
     */
    public static Logger getLoggerForClass(Class clazz) {
        if(s_logging_activated) {
            Level level = (Level) LOGGABLE_CLASSES.get(clazz);
            if(level != null) {
                if(!level.equals(s_defaultLogger.getLogLevel())) {
                    return new RealLogger(s_logHandler, level);
                }

            }
        }
        return s_defaultLogger;
    }

}
