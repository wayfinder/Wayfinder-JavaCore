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

package com.wayfinder.core.shared.internal;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * <p>This interface is intended to be implemented by classes that can
 * write an instance of themselves to a DataOutputStream for storing on
 * permanent storage or sending over a network.</p>
 * 
 * <p>The mechanism for restoring an instance from the written data is
 * not defined by this interface. One way is to implement the symmetric
 * interface {@link Serializable}.  But this does not work when you want
 * to accomplish thread-safety of immutable objects without explicit
 * synchronization by using final fields and safe publication (JLS3,
 * sect. 17.5). For those cases, you need to define a suitable constructor
 * or static factory and let the calling code be aware of what type to
 * create. This is usually not a big problem unless you want to pass around
 * weakly-typed instances of <code>Serializable</code> or instances of
 * abstract base classes instead of strongly-typed instances of your
 * your concrete implementation class.</p>
 * 
 * @see Serializable
 */
public interface WriteSerializable {

    /**
     * <p>Write all the needed primitive data types into a stream.</p>
     * 
     * <p>If the object contains other complex object(s) that need to be 
     * saved in the stream, those object should also implement this interface
     * and take care of their own saving.</p>
     *    
     * @param dout the data OUTPUT stream in which to save the primitive data 
     * types.
     * @throws IOException if there is a parsing problem or a read error from
     * the underlying stream.
     */
    public void write(DataOutputStream dout) throws IOException;    
}
