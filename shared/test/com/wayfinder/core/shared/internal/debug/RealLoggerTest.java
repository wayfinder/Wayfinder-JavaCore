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

public class RealLoggerTest extends TestCase {

    
    public void testTrace() {
        testLevel(Logger.TRACE);
    }
    

    public void testDebug() {
        testLevel(Logger.DEBUG);
    }
    

    public void testInfo() {
        testLevel(Logger.INFO);
    }
    

    public void testWarn() {
        testLevel(Logger.WARN);
    }
    

    public void testError() {
        testLevel(Logger.ERROR);
    }
    

    public void testFatal() {
        testLevel(Logger.FATAL);
    }

    
    public void testOff() {
        testLevel(Logger.NONE);
    }
    
    
    
    
    private static void testLevel(final Level level) {
        LogExpector expector = new LogExpector(level);
        Logger log = new RealLogger(expector, level);
        
        // check that the levels return valid values
        
        assertEquals(level, log.getLogLevel());
        
        assertEquals(shouldBeActive(Logger.TRACE, level), log.isTrace());
        assertEquals(shouldBeActive(Logger.TRACE, level), log.isLoggable(Logger.TRACE));
        
        assertEquals(shouldBeActive(Logger.DEBUG, level), log.isDebug());
        assertEquals(shouldBeActive(Logger.DEBUG, level), log.isLoggable(Logger.DEBUG));
        
        assertEquals(shouldBeActive(Logger.INFO, level), log.isInfo());
        assertEquals(shouldBeActive(Logger.INFO, level), log.isLoggable(Logger.INFO));
        
        assertEquals(shouldBeActive(Logger.WARN, level), log.isWarn());
        assertEquals(shouldBeActive(Logger.WARN, level), log.isLoggable(Logger.WARN));
        
        assertEquals(shouldBeActive(Logger.ERROR, level), log.isError());
        assertEquals(shouldBeActive(Logger.ERROR, level), log.isLoggable(Logger.ERROR));
        
        assertEquals(shouldBeActive(Logger.FATAL, level), log.isFatal());
        assertEquals(shouldBeActive(Logger.FATAL, level), log.isLoggable(Logger.FATAL));
        
        assertEquals(shouldBeActive(Logger.NONE, level), log.isLoggable(Logger.NONE));
        
        // check to see that calls are removed properly
        
        final String tag = "tag";
        final String message = "message";
        
        log.trace(tag, message);
        log.debug(tag, message);
        log.info(tag, message);
        log.warn(tag, message);
        log.error(tag, message);
        log.fatal(tag, message);
    }
    
    
    
    
    static boolean shouldBeActive(Level callevel, Level activelevel) {
        // I *COULD* do this with "return (callevel <= activelevel)",
        // but may be better to explicitly state everything...
        // that way there's no question as to what the value should be
        
        return callevel.isHigherOrEqualTo(activelevel);
        
        
        /*
        if(callevel == Logger.LEVEL_NONE || activelevel == Logger.LEVEL_NONE) {
            // everything turned off
            return false;
        }
        
        switch(activelevel) {
        case Logger.FATAL.getIntValue();
            switch(callevel) {
            case Logger.FATAL: return true;
            case Logger.ERROR: return false;
            case Logger.WARN:  return false;
            case Logger.INFO:  return false;
            case Logger.DEBUG: return false;
            case Logger.TRACE: return false;
            }
            
        case Logger.ERROR:
            switch(callevel) {
            case Logger.FATAL: return true;
            case Logger.ERROR: return true;
            case Logger.WARN:  return false;
            case Logger.INFO:  return false;
            case Logger.DEBUG: return false;
            case Logger.TRACE: return false;
            }
            
        case Logger.WARN:
            switch(callevel) {
            case Logger.FATAL: return true;
            case Logger.ERROR: return true;
            case Logger.WARN:  return true;
            case Logger.INFO:  return false;
            case Logger.DEBUG: return false;
            case Logger.TRACE: return false;
            }
            
        case Logger.INFO:
            switch(callevel) {
            case Logger.FATAL: return true;
            case Logger.ERROR: return true;
            case Logger.WARN:  return true;
            case Logger.INFO:  return true;
            case Logger.DEBUG: return false;
            case Logger.TRACE: return false;
            }
        
        case Logger.DEBUG:
            switch(callevel) {
            case Logger.FATAL: return true;
            case Logger.ERROR: return true;
            case Logger.WARN:  return true;
            case Logger.INFO:  return true;
            case Logger.DEBUG: return true;
            case Logger.TRACE: return false;
            }
        
        case Logger.TRACE:
            return true; // everything on
        }
        return false;
        
        //return (calllevel <= activelevel);*/
    }
    
    
    
    
    /**
     * Helper class to check that calls are filtered
     */
    private static class LogExpector implements LogHandler {
        
        private final Level m_levelToExpect;
        
        private LogExpector(Level levelToExpect) {
            m_levelToExpect = levelToExpect;
        }
        

        public void writeMessageToPlatformLog(LogMessage message) {
            assertTrue(shouldBeActive(message.getLevel(), m_levelToExpect));
        }


        public void startFileLogging() {
            // 
        }

    }
    
}
