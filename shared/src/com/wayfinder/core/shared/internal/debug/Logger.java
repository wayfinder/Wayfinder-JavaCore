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

import com.wayfinder.pal.PAL;
import com.wayfinder.pal.debug.Level;
import com.wayfinder.pal.debug.LogHandler;

public abstract class Logger {
    
    // same logging levels as in LogHandler, easier to have a copy here
    // copy the logging levels to ensure that they are in phase
    
    /**
     * This level represents messages that are used to trace functionality
     * on an extremely detailed level
     */
    public static final Level TRACE = Level.TRACE;
    
    
    /**
     * This level represents messages that are used to trace functionality
     * on an fine level
     */
    public static final Level DEBUG = Level.DEBUG;
    
    
    /**
     * This level represents messages that are used to inform of global
     * decisions made in the Core.
     */
    public static final Level INFO = Level.INFO;
    
    
    /**
     * This level represents messages that are used to warn about situations
     * where the application encounters a problem or exception that it can 
     * handle and will have no or little impact at all on the user. 
     */
    public static final Level WARN  = Level.WARN;

    
    /**
     * This level represents messages sent when the application encounters a
     * problem or exception that it can handle, but will have a negative impact
     * on the user experience. 
     **/
    public static final Level ERROR = Level.ERROR;
    
    
    /**
     * This level represents messages sent when the Core encounters a problem 
     * that it cannot recover from and that will most likely crash the entire 
     * program.
     */
    public static final Level FATAL = Level.FATAL;
    
    
    /**
     * This level signifies that all logging is turned off
     */
    public static final Level NONE = Level.NONE;
    
    
    /**
     * Standard constructor, package protected
     * 
     * @param handler The {@link LogHandler} from the {@link PAL}
     * @param loglevel The level to set in the Logger. All messages below this
     * level will be discarded
     */
    Logger() { }

    
    
    //-------------------------------------------------------------------------
    // checks
    
    /**
     * Returns the current level set in the logger
     * 
     * @return One of the LEVEL constants in this class
     */
    public abstract Level getLogLevel();

    
    /**
     * A check to see if the specified level will be logged if written to this
     * logger. If this method returns false, messages logged with the level will
     * be discarded.
     * 
     * @param level On the LEVEL constants in this class
     * @return true if and only if messages with this level actually will be
     * written to the log
     */
    public abstract boolean isLoggable(Level level);
    
    
    /**
     * A check to see if messages with {@link #LEVEL_TRACE} will be written
     * to the log.
     * 
     * @return true if and only if the messages will be written
     */
    public abstract boolean isTrace();
    

    /**
     * A check to see if messages with {@link #DEBUG} will be written
     * to the log
     * 
     * @return true if and only if the messages will be written
     */
    public abstract boolean isDebug();
    
    
    /**
     * A check to see if messages with {@link #LEVEL_INFO} will be written
     * to the log
     * 
     * @return true if and only if the messages will be written
     */
    public abstract boolean isInfo();
    
    
    /**
     * A check to see if messages with {@link #LEVEL_WARN} will be written
     * to the log
     * 
     * @return true if and only if the messages will be written
     */
    public abstract boolean isWarn();
    
    
    /**
     * A check to see if messages with {@link #LEVEL_ERROR} will be written
     * to the log
     * 
     * @return true if and only if the messages will be written
     */
    public abstract boolean isError();
    

    /**
     * A check to see if messages with {@link #LEVEL_FATAL} will be written
     * to the log
     * 
     * @return true if and only if the messages will be written
     */
    public abstract boolean isFatal();
    
    
    
    //-------------------------------------------------------------------------
    // logging methods
    
    
    public abstract void logException(Level level, String methodName, Throwable t);
    

    /**
     * Writes a trace message to the internal eventlog of the device.
     * <p>
     * Trace messages should only contain messages that are
     * "developers eyes only". This kind of level can be used for tracing the
     * flow within a method and for output that will be extremely talkative.
     * <p>
     * These messages will NOT be active in release versions.
     * <p>
     * @param methodName A string indicating the origin of the message
     * @param message The message to write
     */

    public abstract void trace(String methodName, String message);
    
    
    /**
     * Writes a debug message to the internal eventlog of the device.
     * <p>
     * Debug messages should only contain messages that are
     * "developers eyes only". Examples of these are entering and exiting of
     * methods. This level can be expected to be default during development.
     * <p>
     * These messages will NOT be active in release versions.
     * <p>
     * @param methodName A string indicating the origin of the message
     * @param message The message to write
     */
    public abstract void debug(String methodName, String message);
    
    
    /**
     * Writes an information message to the log.
     * <p>
     * Information messages should be VERY scarse and may contain general
     * information to allow us to better determine the customer's problems.
     * <p>
     * Example of info messages are decisions by the application that will
     * effect large things. For example what connection method is used.
     * <p>
     * @param methodName A string indicating the origin of the message
     * @param message The message to write
     */
    public abstract void info(String methodName, String message);
    
    
    /**
     * Writes a {@link #LEVEL_WARN} message to the log
     * <p>
     * Warning messages should be logged whenever the application encounters a
     * problem or exception that it can handle and will have no or little
     * impact at all on the user. 
     * <p>
     * For example the application could warn if a server URL
     * cannot be reached due to blockage in the network, but another server URL
     * works fine.
     * <p>
     * @param methodName A string indicating the origin of the message
     * @param message The message to write
     */
    public abstract void warn(String methodName, String message);
    

    /**
     * Writes an {@link #LEVEL_ERROR} message to the log.
     * <p>
     * Error messages should be logged whenever the application encounters a
     * problem or exception that it can handle, but will have a negative impact
     * on the user experience.
     * <p>
     * A typical error message would be if the network cannot be contacted due
     * to lack of permission to do so.
     * 
     * @param methodName A string indicating the origin of the message
     * @param message The message to write
     */
    public abstract void error(String methodName, String message);
    
    public abstract void error(String methodName, Throwable t);
    
    /**
     * Writes a {@link #LEVEL_FATAL} message in the log.
     * 
     * Fatal messages should be logged whenever the application
     * encounters a problem that it cannot recover from and that will most
     * likely crash the entire program.
     * 
     * A typical severe error message would be an application bug that causes
     * a system to shut down.
     * 
     * @param methodName A string indicating the origin of the message
     * @param message The message to write
     */
    public abstract void fatal(String methodName, String message);
    
}
