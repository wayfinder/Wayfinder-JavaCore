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
 *    Copyright, Wayfinder Systems AB, 2009
 */
package com.wayfinder.core.shared.error;

import com.wayfinder.core.network.ServerError;

/**
 * Specification: MC2 The XML API Rev. 2.0.7. 2009-07-16
 * 
 * 
 */
public class RouteError extends ServerError {
    
    /**
     * <p>-501 No route found.</p>
     * 
     * <p>No route was found from the origin to the destination.</p>
     * 
     * <p>Example: origin is on an island with no bridges and no information
     * on ferries.</p>
     */
    public static final int ERRROUTE_NOT_FOUND = -501;
    
    /**
     * <p>-502 Too far for vehicle.</p>
     * 
     * <p>Route is too far to go for the vehicle used.</p>
     * 
     * <p>Mostly used for pedestrian routes that are too long.</p>
     */
    public static final int ERRROUTE_TOO_FAR = -502;
    
    /**
     * <p>-503 Problem with origin.</p>
     * 
     * <p>Can not make out origin.</p>
     * 
     * <p>Example: origin position is too far from a drivable/walkable street.
     * Or an invalid search_item was sent in the routeable_item_list.</p>
     */
    public static final int ERRROUTE_ORIGIN_PROBLEM = -503;    
    
    /**
     * <p>-504 Problem with destination.</p>
     * 
     * <p>Can not make out destination.</p>
     *  
     * <p>Examples: see {@link RouteError#ERRROUTE_ORIGIN_PROBLEM} (-503).</p>
     */
    public static final int ERRROUTE_DESTINATION_PROBLEM = -504;
    
    /**
     * <p>-505 Keep your route, it is up to date.</p>
     * 
     * <p>This happens when reroute_reason is traffic_info_update and the route
     * is unchanged.</p>
     */
    public static final int ERRROUTE_UP_TO_DATE = -505;
    
    /**
     * <p>Routing not allowed. You need to buy route service. This error can be 
     * sent to Content Window. Number is -0x17001.</p>
     */
    public static final int ERRROUTE_NOT_ALLOWED = -94209;


    /**
     * Create a new RouteError.
     * 
     * @param statusCode - the status code sent by the server.
     * @param statusMessage - the status message sent by the server.
     * @param statusUri - the status URI sent by the server or the emtpy string
     *        if no URI was sent.
     * 
     * @see ServerError#ServerError(int, String, String)
     */
    public RouteError(int statusCode, String statusMessage, String statusUri) {
        super(statusCode, statusMessage, statusUri);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("RouteError code: ");
        sb.append(getStatusCode());
        sb.append("\nmessage: ");
        sb.append(getInternalMsg());
        sb.append("\nURI: ");
        sb.append(getStatusUri());
        
        return sb.toString();
    }
}
