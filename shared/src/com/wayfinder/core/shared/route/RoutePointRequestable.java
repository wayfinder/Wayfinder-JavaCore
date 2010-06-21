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

package com.wayfinder.core.shared.route;

import com.wayfinder.core.shared.Position;

/**
 * <p>This interface is implemented by all classes that can be used as origin or
 * destination for a route.</p>
 * 
 * <p>It is used to simplify the interface to routing so
 * that we don't have to add one method for each combination of
 * position/search hit/favorite...</p>
 *
 * <p>It is placed in shared, since it will need to be implemented by classes from
 * other modules.</p>
 * 
 * <p>This interface does not contain any methods to obtain a string
 * representation of the implementor (like
 * <code>jWMMG:wmmg.data.route.RoutePointRequestable.getRoutePointName()</code> and
 * <code>jWMMG:wmmg.data.route.RoutePointRequestable.getDescription()</code>)
 * The reason for this is that positions don't have names and if the core
 * would provide one, we run into localization and resource problems. The UI
 * must implement its own wrapper classes to pass the names around.</p>
 */
public interface RoutePointRequestable {
    // TODO: add methods to write the item to xml - probably be getting a
    // a reference to another interface specified by the comm-module.
    
    // We will
    // not use the position for routing (except for when the implementor is
    // Position) because that will lead to bad routes for large objects where
    // the center pos is far away from the entrance. Eventum #8390.
    
    /**
     * @return the position of this RoutePointRequestable. 
     */
    public Position getPosition();
}
