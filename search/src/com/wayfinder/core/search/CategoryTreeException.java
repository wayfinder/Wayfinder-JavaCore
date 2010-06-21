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
/*
 * Copyright, Wayfinder Systems AB, 2010
 */

package com.wayfinder.core.search;

/**
 * <p>Thrown by the category tree-related classes to indicate problems with
 * the category tree that the client can not recover from in any other way
 * than not using the category tree functionality.</p>
 * 
 * <p>Typically, this happens if the data from the server (possibly cached)
 * didn't match the expected format and semantics (such as link to a
 * missing category). This most likely indicates a programming or
 * configuration error in Core or at the server. It is not likely that a
 * re-download of the tree will solve the problem.</p>
 * 
 * <p>CategoryTreeException could have extended {@link RuntimeException} for
 * convenience, since you will likely only want to catch this at a high level
 * in your program. On the other hand, some consider it bad practice to use
 * RuntimeException for errors that can be caused by state of external
 * entities. We also want to make a clear statement in the API that this
 * exception can be thrown instead of format errors being silently ignored.</p>
 * 
 * <p>Note that not all RuntimeExceptions that stems from format errors are
 * converted to CategoryTreeException. This might be improved in a future
 * version.</p>
 */
public class CategoryTreeException extends Exception {

    /**
     * <p>The chained exception or null.</p>
     * 
     * <p>Throwable chaining was introduced in java 1.4 but we are limited
     *  to 1.3 so we have to implement it ourselves.</p>
     */
    protected final Throwable m_cause;


    /**
     * <p>Equivalent to <code>CategoryTreeException(message, null)</code>.
     * 
     * @param message The detailed error message.
     * See the general constructor for details.
     * @see #CategoryTreeException(String, Throwable)
     */
    public CategoryTreeException(String message) {
        this(message, null);
    }

    /**
     * <p>Equivalent to <code>CategoryTreeException(null, cause)</code>.
     * 
     * @param cause The cause of the exception.
     * See the general constructor for details.
     * @see #CategoryTreeException(String, Throwable)
     */
    public CategoryTreeException(Throwable cause) {
        this(null, cause);
    }

    /**
     * <p>Constructs a new exception with the specified detail message and
     * cause.</p>
     * 
     * <p>Note that the detailed message associated with cause is not
     * automatically incorporated in this exception's detail message.</p>
     * 
     * @param message The detailed message that can later be retrieved with
     * {@link Throwable#getMessage()}. Null is allowed.
     * @param cause The cause of the exception.
     * The value can later be retrieved with {@link #getCause()}.
     * Null is allowed.
     */
    public CategoryTreeException(String message, Throwable cause) {
        super(message);
        m_cause = cause;
    }


    /**
     * <p>Returns the original cause for CategoryTreeException.</p>
     * 
     * <p>If you get a CategoryTreeException and want to send error logs to
     * Java Core Team, please also print message and stacktrace for this
     * exception.</p>
     * 
     * @return the chained exception or null.
     */
    public Throwable getCause() {
        return m_cause;
    }
}
