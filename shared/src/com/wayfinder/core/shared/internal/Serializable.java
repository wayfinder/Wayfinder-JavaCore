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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.core.shared.internal;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * <p>This interface is intended to be implemented by classes that need 
 * serialization, for transforming an instance of the class in a byte array.
 * This could be required for saving in the RMS or for sending the entire object
 * over the network, bluetooth, ...</p>
 * 
 * <p>Implementors will be responsible for documenting how to use them
 * in a thread-safe way or that they are intended for single thread usage only.
 * </p> 
 * 
 * @see WriteSerializable
 */
public interface Serializable
    extends WriteSerializable {

    /**
     * <p>Restore the primitive data types needed to reinitialize the object
     * from the input stream.</p>
     * 
     * <p>If the object has complex object(s) within, that need to be restored,
     * those object should also implement the interface and should manage their
     * own restoration.</p>
     * 
     * @param din - the data INPUT stream to read from.
     * @throws IOException - if there is a parsing problem or a read error from
     * the underlying stream.
     */
    public void read(DataInputStream din) throws IOException;    
}
