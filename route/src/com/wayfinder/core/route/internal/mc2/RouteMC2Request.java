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
package com.wayfinder.core.route.internal.mc2;

import java.io.IOException;

import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Request;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.route.RouteRequest;
import com.wayfinder.core.route.internal.nav2route.Nav2Route;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.route.RouteSettings;
import com.wayfinder.core.shared.xml.XmlIterator;
import com.wayfinder.core.shared.xml.XmlWriter;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.RequestID;

/**
 * 
 *
 */
public final class RouteMC2Request implements MC2Request {
    
    private static final Logger LOG = LogFactory.getLoggerForClass(RouteRequest.class);
    
    private static final String ROUTE_DESC_NORMAL = "normal";
    private static final String ROUTE_DESC_COMPACT = "compact";
    
    private final RequestID m_requestID;
    private final RouteRequest m_request;
    private final Nav2Route m_currentRoute;
    private final RouteSettings m_routeSettings;
    private final RouteMC2ReplyListener m_replyListener;
    private final GeneralSettingsInternal m_settings;
    
    
    public RouteMC2Request(GeneralSettingsInternal settings, RequestID requestID, RouteRequest routeReq, RouteMC2ReplyListener listener) {
        m_settings = settings;
        m_requestID = requestID;
        m_request = routeReq;
        m_currentRoute = (Nav2Route) m_request.getRoute();
        m_routeSettings = m_request.getRouteSettings();
        m_replyListener = listener;
    }

    public void error(final CoreError coreError) {
        m_replyListener.error(m_requestID, coreError);
    }
    
    public String getRequestElementName() {
        return MC2Strings.troute_request;
    }
    
    
    public void write(MC2Writer mc2w) throws IOException {
        /*
         * <!ELEMENT route_request ( route_request_header,
         *                              routeable_item_list,
         *                              routeable_item_list )>
         *                              
         * <!ELEMENT route_request_header ( route_preferences )>
         * 
         * <!ENTITY % reroute_reason_t "(unknown|truncated_route|off_track|
         *                              traffic_info_update|user_request)" >
         * 
         * <!ATTLIST route_request_header 
         *              previous_route_id CDATA #IMPLIED
         *              reroute_reason %route_reason_t; #IMPLIED>
         */

        mc2w.startElement(MC2Strings.troute_request_header);

        if (m_request.isReroute()) {
            if (LOG.isInfo()) {
                LOG.info("RouteMC2Request.write(mc2Writer)", "is reroute");
            }
            mc2w.attribute(MC2Strings.aprevious_route_id, 
                    m_currentRoute.getRouteID());
            
            String reRouteReasonStr;
            switch(m_request.getRerouteReason()) {
            case RouteRequest.REASON_OFF_TRACK:
                reRouteReasonStr = "off_track";
                break;
                
            case RouteRequest.REASON_TRAFFIC_INFO_UPDATE:
                reRouteReasonStr = "traffic_info_update";
                break;
                
            case RouteRequest.REASON_TRUNCATED_ROUTE:
                reRouteReasonStr = "truncated_route";
                break;
                
            case RouteRequest.REASON_UNKNOWN:
                reRouteReasonStr = "unknown";
                break;
                
            case RouteRequest.REASON_USER_REQUEST:
                reRouteReasonStr = "user_request";
                break;
            
            default:
                if(LOG.isError()) {
                    LOG.error("RouteMC2Request.write()", 
                            "Unknown reroute reason constant - using REASON_UNKNOWN");
                }
                reRouteReasonStr = "unknown";
                break;
            }
            
            mc2w.attribute(MC2Strings.areroute_reason, reRouteReasonStr);
        }

        /* <!ELEMENT route_preferences ( ( user_id | route_settings | uin |
         *                                  (user_session_id, 
         *                                  user_session_key) ),
         *                                  image_settings? )>
         * 
         * <!ENTITY % route_description_type_t "(normal|compact)">
           
           <!ATTLIST route_preferences
           route_description_type %route_description_type_t; #REQUIRED
           route_image_links %bool; "false"
           route_overview_image_width %number; "256"
           route_overview_image_height %number; "256"
           route_turn_image_width %number; "256"
           route_turn_image_height %number; "256"
           route_image_default_format %route_image_format_t; "png"
           route_image_display_type %image_display_type; "std"
           route_turn_data %bool; "false"
           route_boundingbox_position_sytem %position_system_t; "MC2"
           route_turn_boundingbox %bool; "false"
           route_road_data %bool; "false"
           route_items %bool; "true"
           abbreviate_route_names %bool; "true"
           route_landmarks %bool; "false" >
         */

        if (LOG.isInfo()) {
            LOG.info("RouteMC2Request.write(mc2Writer)", MC2Strings.troute_preferences);
        }
        mc2w.startElement(MC2Strings.troute_preferences);
        
        mc2w.attribute(MC2Strings.aroute_description_type, 
                ROUTE_DESC_NORMAL);
        mc2w.attribute(MC2Strings.aroute_items, false);

        /*
         * <!ELEMENT route_settings ( route_costA?,
         *                              route_costB?,
         *                              route_costC?,
         *                              language )>
         *                              
         * <!ATTLIST route_settings route_vehicle %route_vehicle_t; #REQUIRED
         *              avoid_toll_road %bool; #IMPLIED
         *              avoid_highway %bool; #IMPLIED >
         *              
         * <!ELEMENT routeable_item_list ( (position_item | search_item)+ )>
         * 
         */
        
        if (LOG.isInfo()) {
            LOG.info("RouteMC2Request.write(mc2Writer)", MC2Strings.troute_settings);
        }
        mc2w.startElement(MC2Strings.troute_settings);

        mc2w.attribute(MC2Strings.aroute_vehicle, 
                MC2Strings.route_vehicle(m_routeSettings.getTransportMode()));

        mc2w.attribute(MC2Strings.aavoid_toll_road,
                m_routeSettings.isAvoidTollRoad());

        mc2w.attribute(MC2Strings.aavoid_highway, 
                m_routeSettings.isAvoidHighway());

        String cost;
        switch(m_routeSettings.getOptmization()) {
        case RouteSettings.OPTIMIZE_DISTANCE:
            cost = "route_costA";
            break;
            
        case RouteSettings.OPTIMIZE_TIME:
            cost = "route_costB";
            break;
            
        case RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC:
            cost = "route_costC";
            break;
            
        default:
            if(LOG.isError()) {
                LOG.error("RouteMC2Request.write()", 
                          "Unable to determine optimization mode - defaulting to time/traffic");
            }
            cost = "route_costC";
        }
        
        mc2w.elementWithText(cost, 1);

        mc2w.elementWithText(MC2Strings.tlanguage, 
                m_settings.getInternalLanguage().getXMLCode());
        
        mc2w.endElement(MC2Strings.troute_settings);
        mc2w.endElement(MC2Strings.troute_preferences);
        mc2w.endElement(MC2Strings.troute_request_header);

        if (LOG.isInfo()) {
            LOG.info("RouteMC2Request.write(mc2Writer)", "writing start");
        }
        mc2w.startElement(MC2Strings.trouteable_item_list);
        if (m_request.getRequestedOrigin() instanceof MC2WritableElement) {
            // either a Position or a SearchMatchImpl
            ((MC2WritableElement)m_request.getRequestedOrigin()).write(mc2w);
        }
        else {
            m_request.getRequestedOrigin().getPosition().write(mc2w);
        }
        mc2w.endElement(MC2Strings.trouteable_item_list);

        if (LOG.isInfo()) {
            LOG.info("RouteMC2Request.write(mc2Writer)", "writing destination");
        }
        mc2w.startElement(MC2Strings.trouteable_item_list);
        if (m_request.getRequestedDestination() instanceof MC2WritableElement) {
            // either a Position or a SearchMatchImpl
            ((MC2WritableElement)m_request.getRequestedDestination()).write(mc2w);
        }
        else {
            m_request.getRequestedDestination().getPosition().write(mc2w);
        }
        mc2w.endElement(MC2Strings.trouteable_item_list);

    }
    

    public void parse(MC2Parser mc2p) throws MC2ParserException,
            IOException {
        if (LOG.isInfo()) {
            LOG.info("RouteMC2Request.parse(mc2Parser)", "at route_reply");
        }
        
        // turns out we only need route ID and bounding box

        /*
         * <!ELEMENT route_reply ( ( route_reply_header, route_origin,
         * route_destination, route_reply_items ) | ( status_code,
         * status_message ) )>
         */
        /*
         * <!ATTLIST route_reply transaction_id ID #REQUIRED route_id CDATA
         * #REQUIRED > ptui %number; #IMPLIED >
         */

        final String route_id = mc2p.attribute(MC2Strings.aroute_id);

        mc2p.children();

        //check and report status code 
        ServerError status = mc2p.getErrorIfExists();
        if (status != null) {
            error(status);
            return;
        }
        
        //TODO most of the stuff the skipped, see with server how to 
        //optimize this
        int north_lat = 0, south_lat = 0, east_lon = 0, west_lon = 0;
        do {
            if (mc2p.nameRefEq(MC2Strings.troute_reply_header)) {

                /*
                 * <!ELEMENT route_reply_header ( total_distance,
                 * total_distance_nbr, total_time, total_time_nbr,
                 * total_standstilltime, total_standstilltime_nbr,
                 * average_speed, average_speed_nbr, routing_vehicle,
                 * routing_vehicle_type, boundingbox, route_overview_link?,
                 * route_overview_width?, route_overview_height? )>
                 */
                mc2p.children();// we are at the first child of
                               // route_reply_header
                do {
                    if (mc2p.nameRefEq(MC2Strings.tboundingbox)) {
                        north_lat = mc2p
                                .attributeAsInt(MC2Strings.anorth_lat);
                        south_lat = mc2p
                                .attributeAsInt(MC2Strings.asouth_lat);
                        east_lon = mc2p
                                .attributeAsInt(MC2Strings.aeast_lon);
                        west_lon = mc2p
                                .attributeAsInt(MC2Strings.awest_lon);
                    }
                    // skip the rest
                } while (mc2p.advance());
            } // skip the rest
        } while (mc2p.advance());

        final BoundingBox boundingbox = new BoundingBox(north_lat, south_lat,
                east_lon, west_lon);

        if (LOG.isInfo()) {
            LOG.info("RouteMC2Request.parse(mc2Parser)",
            "finished parsing route reply");
        }
        m_replyListener.routeMC2ReplyDone(m_requestID, route_id,
                boundingbox);
    }
}
