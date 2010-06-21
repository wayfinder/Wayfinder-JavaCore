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

import com.wayfinder.pal.debug.Level;
import com.wayfinder.pal.debug.LogHandler;
import com.wayfinder.pal.debug.LogMessage;

import junit.framework.TestCase;

public class LogFactoryTest extends TestCase {
    
    private static final Level DEFAULT_LOG_LEVEL = Logger.FATAL;
    private Level[] levelArray = {
            Level.TRACE,
            Level.DEBUG,
            Level.INFO,
            Level.WARN,
            Level.ERROR,
            Level.FATAL
    };
    
    protected void setUp() throws Exception {
        super.setUp();
        LogFactory.initLogFrameWork(new LogHandlerDummy(), DEFAULT_LOG_LEVEL);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        LogFactory.resetLogFramework();
    }
    
    
    public void testGetDefaultLogger() throws Exception {
        // make sure that the same instance is always returned to avoid
        // cluttering the memory
        
        Logger log = LogFactory.getDefaultLogger();
        assertNotNull(log);
        for (int i = 0; i < 5; i++) {
            assertSame(log, LogFactory.getDefaultLogger());
        }
        
        //make sure that the default logger has the same level as the default
        //level passed in at the beginning
        assertEquals(DEFAULT_LOG_LEVEL, log.getLogLevel());
    }
    
    
    public void testGetLoggerWithLevel() {
        for(int i = 0; i < levelArray.length; i++) {
            final Level thisLevel = levelArray[i];
            Logger log = LogFactory.getLoggerWithLevel(thisLevel);
            assertEquals(thisLevel, log.getLogLevel());
        }
    }
    
    
    public void testClassOverride() {
        final Class clazz = getClass();
        
        Logger log = LogFactory.getLoggerForClass(clazz);
        assertEquals(DEFAULT_LOG_LEVEL, log.getLogLevel());
        
        for(int i = 0; i < levelArray.length; i++) {
            final Level thisLevel = levelArray[i];
            LogFactory.overrideLoglevelForClass(clazz, thisLevel);
            log = LogFactory.getLoggerForClass(clazz);
            assertEquals(thisLevel, log.getLogLevel());
        }
        
        LogFactory.removeClassOverride(clazz);
        log = LogFactory.getLoggerForClass(clazz);
        assertEquals(DEFAULT_LOG_LEVEL, log.getLogLevel());
    }
    
    
    private static final class LogHandlerDummy implements LogHandler {

        public void writeMessageToPlatformLog(LogMessage message) { }

        public void startFileLogging() {
            // 
            
        }
        
    }

}
