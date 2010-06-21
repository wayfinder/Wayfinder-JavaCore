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

package com.wayfinder.core.geocoding;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;

/**
 * <p>Interface for geocoding and reverse geocoding.</p>
 * 
 * <p>Currently, only reverse geocoding is supported. Use cases involving
 * (forward) geocoding are handled by
 * {@link com.wayfinder.core.search.SearchInterface}. We have not added the
 * reverse geocoding functions to the SearchInterface since they don't fit
 * the call model.</p>
 * 
 * <p>A separate public request object is not provided since it is not needed
 * for the limited functionality available.</p> 
 */
public interface GeocodeInterface {

    /**
     * <p>Request reverse geocoding for a position.</p>
     * 
     * <p>A request will be sent to the server to try to map the position to
     * the closest street. If the nearest street is too far away, only area
     * information will be provided.</p>
     * 
     * @param position - the position.
     * @param listener the {@link GeocodeListener} which will receive the
     *        result. The result is delivered thru
     *        {@link GeocodeListener#reverseGeocodeDone(RequestID, com.wayfinder.core.shared.geocoding.AddressInfo)}.
     * @return a {@link RequestID} to uniquely identify the request.
     * @throws IllegalArgumentException if position or listener is null or
     *         position is not valid.
     */
    public RequestID reverseGeocode(Position position,
                                    GeocodeListener listener);
}
