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
package com.wayfinder.core.network;

import com.wayfinder.core.shared.error.CoreError;

/**
 * An error occurred at the server when processing the request. Each error has 
 * a status code sent by server. 
 * There are general status codes for the entire document, and specific 
 * status codes for each request. This class contains constant values for
 * general status codes.
 * 
 * 
 */
public class ServerError extends CoreError{
    /*
     * In the XML API, this corresponds to getting <status_code> with another
     * value then 0. 
     */
    
    
    //---------- only general status code -----------------------------------//
    //---------- those that are direct child of isab-mc2 --------------------//
    
    /** Refer to MC2 - The XML API r1.0.10 page 10-11 for
     * interpretation of these codes.
     */
    public static final int ERRSERV_GENERAL_SERVER_ERROR = -1;

    public static final int ERRSERV_REQUEST_MALFORMED = -2;

    public static final int ERRSERV_REQUEST_TIMEOUT = -3;

    public static final int ERRUSER_OUTSIDE_MAP_COVERAGE = -4;

    /**
     * -5 Outside allowed area. 
     * The request was for an area outside the map coverage the user 
     * is allowed to use.
     * NOTE: this is what is sent if your user rights to the area in
     * question have expired. not -201 or -206 
     */
    public static final int ERRUSER_OUTSIDE_ALLOWED_AREA = -5;

    public static final int ERRAUTH_ACCESS_DENIED = -201;

    /**
     * -202 Unknown user. 
     * The user does not exist.
     */
    public static final int ERRAUTH_UNKNOWN_USER = -202;

    public static final int ERRAUTH_INVALID_LOGIN = -203;

    /**
     * -206 Expired user. 
     * The user no longer has access to the service.
     */
    public static final int ERRAUTH_EXPIRED_USER = -206;
    

    /** 
     * -207 Unknown token. The token does not match. 
     * Use activate_request to get new.
     */
    public static final int ERRAUTH_UNKNOWN_TOKEN = -207;
    
    /** 
     * server reply contains new server_list and/or
     * server_auth_bob.
     *
     * You can continue to use the old ones for now. The original
     * request is not processed. We treat this the same way as
     * ERRSERV_NEW_SERVERLIST
     */
    public static final int OKSERV_NEW_SERVERLIST = -210;

    /** 
     * server reply contains new server_list and/or
     * server_auth_bob. The original request (e.g. search) has not
     * been processed. You must re-direct to the other server. This
     * server won't be able to process further requests.
     */
    public static final int ERRSERV_NEW_SERVERLIST = -211;

    
    /**
     * This server is a backup server and the request can not be
     * completed
     */
    public static final int ERRSERV_BACKUP_SERVER  = -212;
    
    /**
     * -214 Version lock. 
     * The user is not allowed to use the current client software.
     */
    public static final int ERRUSER_VERSION_LOCK = -214;

    /** 
     * -401 External auth client not from the external entity it should be. 
     * Check installed client application, SIM and access point.
     */
    public static final int ERREXTAUTH_WRONG_ENTITY = -401;
    
    /** 
     * -402 External auth client is authenticated but external entity says that
     * the user hasn?t access. Buy some extension.
     */
    public static final int ERREXTAUTH_ACESS_DENIED = -402;
    
    
    private final int statusCode;
   
    private final String statusUri;
    
    public ServerError(int statusCode, String statusMessage, String statusUri) {
        super(ERROR_SERVER, statusMessage);
        this.statusCode = statusCode;
        this.statusUri = statusUri;
    }

    /**
     * @return the server status code as a negative number 
     * which can be one of the values above.
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * For some error, the server sends an URI that should be opened in the
     * service window...
     * 
     * <p>TODO: Can actually return null today unless all callers take care.
     * Add param checking in ctor.</p>
     * 
     * @return a string suitable for sending as URI to the service window or the
     * empty string if the server did not send any URI.
     * 
 
     */
    public String getStatusUri() {
        return statusUri;
    }
    
    public String toString() {
        return "ServerError: code="+statusCode+", message="+ getInternalMsg() + ", uri="+ statusUri; 
    }
}
