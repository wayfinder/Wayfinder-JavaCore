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

class FakeLogger extends Logger {
    
    /*
     * All methods in this class are either empty or returning false
     * 
     * In the release version, the class "RealLogger" will be removed and only
     * this class remains. This will allow proguard to evaluate all logging
     * statements to "false" and thus remove it as well
     */

    public void debug(String methodName, String message) {}

    public void error(String methodName, String message) {}
    
    public void error(String methodName, Throwable t) {}

    public void fatal(String methodName, String message) {}

    public Level getLogLevel() {
        return Level.NONE;
    }

    public void info(String methodName, String message) {}

    public boolean isDebug() {
        return false;
    }

    public boolean isError() {
        return false;
    }

    public boolean isFatal() {
        return false;
    }

    public boolean isInfo() {
        return false;
    }

    public boolean isLoggable(Level level) {
        return false;
    }

    public boolean isTrace() {
        return false;
    }

    public boolean isWarn() {
        return false;
    }

    public void logException(Level level, String methodName, Throwable t) { }

    public void trace(String methodName, String message) { }

    public void warn(String methodName, String message) { }

}
