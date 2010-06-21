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
package com.wayfinder.core.network.internal.mc2;

import com.wayfinder.core.shared.util.CharArray;

/**
 * Static class contain all the strings specific to IsabMC2 needed in parsing
 * of replies and writing of request; elements name, attributes names and 
 * entities values   
 * 
 * 
 */
public class MC2Strings {

    public static final String MC2 = "MC2";
    public static final String STREET_STRING = "street";
    public static final String EMPTY_STRING = "";
    
    //---------- PARSER AND COMMON USE xml tags & attributes -------------------
    //WARNING: Don't forgot add tags to MC2ParserEntities.elementNameArray array 
    //& attributes to attributeName array!!!
    //others way the element name will be null and attributes will be skipped
    //NOTE: is not necessary to ifdef here ifdef only in the arrays 
    //and in the code the obfuscator will do the job 
    
    //tags
    public static final String tcell_id_reply =         "cell_id_reply";
    public static final String troute_reply_header =    "route_reply_header";
    public static final String troute_origin =          "route_origin";
    public static final String troute_destination =     "route_destination";
    public static final String troute_reply_items =      "route_reply_items";
    public static final String text_service =           "ext_service";
    public static final String tserver_group =          "server_group";
    public static final String tserver =                "server";
    public static final String tuser_id =               "user_id";    
    public static final String tuser_track_add_reply =  "user_track_add_reply";
    public static final String tuser_track_reply =      "user_track_reply";
    public static final String tpin =                   "pin";
    public static final String troute_reply =           "route_reply";
    public static final String tpoi_info_reply =        "poi_info_reply";
    public static final String taverage_speed_nbr =     "average_speed_nbr";
    public static final String trouting_vehicle =       "routing_vehicle";
    public static final String ttotal_distance_nbr =    "total_distance_nbr";
    public static final String ttotal_time =            "total_time";
    public static final String temail_reply =           "email_reply";
    public static final String tactivate_reply =        "activate_reply";
    public static final String tuser_reply =            "user_reply";
    public static final String tuser_favorites_reply =  "user_favorites_reply";
    public static final String tstatus_message =        "status_message";
    public static final String tstatus_code =           "status_code";
    public static final String tdescription =           "description";
    public static final String tfav_info =              "fav_info";
    public static final String tlocation_name =         "location_name";
    public static final String tsearch_reply =          "search_reply";
    public static final String tsearch_item_list =      "search_item_list";
    public static final String tinfo_field =            "info_field";
    public static final String tsearch_area_list =      "search_area_list";
    public static final String ttop_region_reply =      "top_region_reply";
    public static final String ttop_region_crc_ok =     "top_region_crc_ok";
    public static final String ttop_region_id =         "top_region_id";
    public static final String text_services_reply =    "ext_services_reply";
    public static final String text_services_crc_ok =   "ext_services_crc_ok";
    public static final String tserver_list =           "server_list";
    public static final String tserver_auth_bob =       "server_auth_bob";
    public static final String tisab_mc2 =              "isab-mc2";
    public static final String tauth_token =            "auth_token";
    public static final String tboundingbox =           "boundingbox";
    public static final String tname_node =             "name_node";
    public static final String tsearch_item =           "search_item";
    public static final String tname =                  "name";
    public static final String titemid =                "itemid";
    public static final String tadd_favorite_list =     "add_favorite_list";
    public static final String tfavorite =              "favorite";
    public static final String tfavorite_id =           "favorite_id"; 
    public static final String tdelete_favorite_id_list =
                                                        "delete_favorite_id_list";
    public static final String tsearch_area =           "search_area";
    public static final String tareaid =                "areaid";
    public static final String tlat =                   "lat";
    public static final String tlon =                   "lon";
    public static final String tposition_item =         "position_item";
    public static final String tuser =                  "user";
    public static final String ttunnel_reply =          "tunnel_reply";
    public static final String theader =                "header";
    public static final String tbody =                  "body";

    public static final String tuser_cap_reply =        "user_cap_reply";
    public static final String tcap =                   "cap";
    public static final String tsearch_desc_reply =     "search_desc_reply";
    public static final String tsearch_hit_type =       "search_hit_type";
    public static final String tcrc_ok =                "crc_ok";
    public static final String tzoom_settings_reply =   "zoom_settings_reply";
    public static final String tzoom_levels =           "zoom_levels";
    public static final String tzoom_level =            "zoom_level";
    public static final String tsimple_poi_desc_reply = "simple_poi_desc_reply";
    public static final String tsimple_poi_desc_data =  "simple_poi_desc_data";
    public static final String tcopyright_str_reply =  "copyright_strings_reply";
    public static final String tcopyright_str_data =   "copyright_strings_data";
    public static final String tuser_favorites_crc_reply = 
                                                        "user_favorites_crc_reply";
    public static final String tcompact_search_reply =  "compact_search_reply";
    public static final String tsearch_hit_list =       "search_hit_list";
    public static final String timage_name =            "image_name";
    public static final String tstatus_uri =            "status_uri";
    public static final String tpopup =                 "popup";
    public static final String tpopup_message =         "popup_message";
    public static final String tpopup_once =            "popup_once";
    public static final String tpopup_url =             "popup_url";
    public static final String tcategory =              "category";
    public static final String tcategory_name =         "category_name";
    public static final String tcategory_list_reply =   "category_list_reply";
    public static final String tcat =                   "cat";
    public static final String tad_results_text =       "ad_results_text";
    public static final String tall_results_text =      "all_results_text";
    public static final String tencryption_reply =      "wgchenre";
    public static final String ttype =                  "type";
    public static final String ttop_region =            "top_region";
    public static final String tinfo_item =             "info_item";
    
    public static final String tlocal_category_tree_reply = 
                                                        "local_category_tree_reply";
    public static final String tcategory_table =        "category_table";
    public static final String tlookup_table =          "lookup_table";
    public static final String tstring_table =          "string_table";
    
    public static final String tcategory_list =         "category_list";
    public static final String tsearch_list =           "search_list";
    public static final String tsearch_match =          "search_match";
    public static final String tone_search_reply =      "one_search_reply";
    
    public static final String tserver_info_reply =     "server_info_reply";
    public static final String tclient_type_info =      "client_type_info";
    
    public static final String tpoi_detail_reply =      "poi_detail_reply";
    public static final String tdetail_item =           "detail_item";
    public static final String tdetail_field =          "detail_field";
    public static final String tresources =             "resources";
    public static final String timage_group =           "image_group";
    public static final String treview_group =          "review_group";
    public static final String timage =                 "image";
    public static final String treview =                "review";
    

    //attributes
    public static final String auin =                   "uin";
    public static final String aid =                    "id";
    public static final String aname =                  "name";
    public static final String aptui =                  "ptui";
    public static final String aroute_id =              "route_id";
    public static final String anorth_lat =             "north_lat";
    public static final String asouth_lat =             "south_lat";
    public static final String aeast_lon =              "east_lon";
    public static final String awest_lon =              "west_lon";
    public static final String asearch_area_type =      "search_area_type";
    public static final String asearch_item_type =      "search_item_type";
    public static final String atransaction_id =        "transaction_id";
    public static final String ainfo_type =             "info_type";
    public static final String acrc =                   "crc";
    public static final String atype =                  "type";
    public static final String avalue =                 "value";
    public static final String adescription =           "description";
    public static final String anumberfields =          "numberfields";
    public static final String anumberitems =           "numberitems";
    public static final String atotal_numberitems =     "total_numberitems";
    public static final String anbr_services =          "nbr_services";
    public static final String areq =                   "req";
    public static final String aservice_id =            "service_id";
    public static final String aPIN  =                  "PIN";
    public static final String aswitch_group_threshold =
                                                        "switch_group_threshold"; 
    public static final String abackground_color =      "background_colour";
    public static final String astatus_line =           "status_line";
    public static final String ate =                    "te";
    public static final String aheading =               "heading";
    public static final String around =                 "round";
    public static final String alength =                "length";
    public static final String apixel_size =            "pixel_size";
    public static final String azoom_j2me =             "zoom_j2me";
    public static final String azoom_level_nbr =        "zoom_level_nbr";
    public static final String amax_x =                 "max_x";
    public static final String amax_y =                 "max_y";
    public static final String amin_x =                 "min_x";
    public static final String amin_y =                 "min_y";
    public static final String acrc_match =             "crc_match";
    public static final String aimage =                 "image";
    public static final String ahref =                  "href";
    public static final String aurl_type =              "url_type";
    public static final String acount =                 "count";
    public static final String atop_hits =              "top_hits";
    public static final String acat_id =                "cat_id";
    public static final String aadvert =                "advert";
    public static final String aencrypted_lon =         "sec_o";
    public static final String aencrypted_lat =         "sec_l";
    public static final String aouter_radius =          "outer_radius";
    public static final String amap_icon_name =         "map_icon_name";
    public static final String anumber_matches =        "number_matches";
    public static final String atotal_number_matches =  "total_number_matches";
    public static final String abrand_image =           "brand_image";
    public static final String acategory_image =        "category_image";
    public static final String aprovider_image =        "provider_image";
    public static final String asearch_match_type =     "search_match_type";
    public static final String aadditional_info_exists ="additional_info_exists";
    
    public static final String aupgrade_available =     "upgrade_available";
    public static final String alatest_version =        "latest_version";
    public static final String aforce_upgrade =         "force_upgrade";
    public static final String aupgrade_id =            "upgrade_id";
    
    public static final String adetail_type =           "detail_type";
    public static final String adetail_content_type =   "detail_content_type";
    public static final String aurl =                   "url";
    public static final String anumber_images =         "number_images";
    public static final String aprovider_name =         "provider_name";
    public static final String anumber_reviews =        "number_reviews";
    public static final String adate =                  "date";
    public static final String areviewer =              "reviewer";
    public static final String arating =                "rating";


    //---------- WRITER USE ONLY xml tags & attributes -------------------------
    //NOTICE: Those tags & attribute should not appear in elementName array &
    //attributes to attributeName array because are not used by the xml writer 
    
    //tags
    public static final String tangle =                 "angle";
    public static final String tuser_track_add_request= "user_track_add_request";
    public static final String tuser_track_item =       "user_track_item";
    public static final String tdelete =                "delete";
    public static final String tproximity_query =       "proximity_query";
    public static final String tsearch_request =        "search_request";
    public static final String tsearch_request_header = "search_request_header";
    public static final String tsearch_preferences =    "search_preferences";
    public static final String tsearch_query =          "search_query";
    public static final String tsearch_area_query =     "search_area_query";
    public static final String tsearch_item_query =     "search_item_query";
    public static final String tsearch_settings =       "search_settings";
    public static final String tsearch_for_street =     "search_for_street";
    public static final String tsearch_for_company =    "search_for_company";
    public static final String tsearch_for_person =     "search_for_person";
    public static final String tsearch_for_misc =       "search_for_misc";
    public static final String troute_request =         "route_request";
    public static final String troute_request_header =  "route_request_header";
    public static final String troute_settings =        "route_settings";
    public static final String troute_preferences =     "route_preferences";
    public static final String trouteable_item_list =   "routeable_item_list";
    public static final String tauth =                  "auth";
    public static final String tauth_activate_request = "auth_activate_request";
    public static final String tactivate_request =      "activate_request";
    public static final String tuin =                   "uin";
    public static final String tphone_number =          "phone_number";
    public static final String tuser_favorites_request= "user_favorites_request";
    public static final String tnew_password =          "new_password";
    public static final String tfavorite_id_list =      "favorite_id_list";
    public static final String tlocal_map_data =        "local_map_data";
    public static final String tlocal_map_string =      "local_map_string";
    public static final String tmap_symbol_list =       "map_symbol_list";
    public static final String tmap_symbol_item =       "map_symbol_item";
    public static final String troute_message_data =    "route_message_data";
    public static final String tlanguage =              "language";
    public static final String tsignature =             "signature";
    public static final String toriginString =          "originString";
    public static final String toriginLocationString =  "originLocationString";
    public static final String tdestinationString =     "destinationString";
    public static final String tdestinationLocationString =
                                                    "destinationLocationString";
    public static final String tauth_user =             "auth_user";
    public static final String tauth_passwd =           "auth_passwd";
    public static final String temail_request =         "email_request";
    public static final String temail_request_header =  "email_request_header";
    public static final String temail_address =         "email_address";
    public static final String treturn_email_address =  "return_email_address";
    public static final String tsubject =               "subject";
    public static final String tpoi_info_request =      "poi_info_request";
    public static final String texternal_auth =         "external_auth";
    public static final String tuser_request =          "user_request";
    public static final String ttop_region_request =    "top_region_request";    
    public static final String ttop_region_request_header = 
                                                        "top_region_request_header";    
    public static final String text_services_request =  "ext_services_request";
    public static final String text_search_request =    "ext_search_request";
    public static final String tfield_val =             "field_val";
    public static final String thandle_me =             "handle_me";
    public static final String topt_in =                "opt_in";
    public static final String ttunnel_request =        "tunnel_request";
    public static final String tpost_data =             "post_data";
    public static final String tlicence_key =           "licence_key";
    public static final String tuser_cap_request =      "user_cap_request";
    public static final String tcompact_search_request ="compact_search_request";
    public static final String tsearch_desc_request =   "search_desc_request";
    public static final String tsearch_position_desc_request =
                                                        "search_position_desc_request";
    public static final String tzoom_settings_request = "zoom_settings_request";
    public static final String tsimple_poi_desc_request = 
                                                        "simple_poi_desc_request";
    public static final String tuser_favorites_crc_request = 
                                                        "user_favorites_crc_request";
    public static final String thardware_id =           "hardware_id";
    public static final String thardware_key =          "hardware_key";
    public static final String temail =                 "email";
    public static final String tcategory_list_request = "category_list_request";
    public static final String tdistance =              "distance";
    public static final String tcopyright_str_request = "copyright_strings_request";
    public static final String tcategory_query =        "category_query";
    public static final String tcategory_id =           "category_id";
    public static final String tencryption_request =    "wgchen";
    public static final String tpp =                    "pp";
    
    public static final String tcell_id_request =       "cell_id_request";
    public static final String ttgpp =                  "TGPP";
    public static final String tcdma =                  "CDMA";
    public static final String tiden =                  "IDEN";
    public static final String texpand_request =        "expand_request";
    public static final String texpand_request_header = "expand_request_header";
    public static final String texpand_request_query =  "expand_request_query";
    public static final String tshow_search_item_municipal =
                                                        "show_search_item_municipal";
    public static final String tshow_search_item_city = "show_search_item_city";
    public static final String tshow_search_item_city_part = "show_search_item_city_part";
    
    public static final String tlocal_category_tree_request = "local_category_tree_request";

    public static final String tone_search_request =    "one_search_request";
    public static final String tsearch_match_query =    "search_match_query";
    public static final String tquery_location =        "query_location";
    
    public static final String tserver_info_request =   "server_info_request";
    
    public static final String tpoi_detail_request =    "poi_detail_request";
    
    //attributes
    public static final String aavoid_highway =         "avoid_highway";
    public static final String aavoid_toll_road =       "avoid_toll_road";
    public static final String aposition_search_items = "position_search_items";
    public static final String aposition_search_areas = "position_search_areas";
    public static final String asearch_area_starting_index = 
                                                        "search_area_starting_index";
    public static final String asearch_area_ending_index = 
                                                        "search_area_ending_index";
    public static final String asearch_item_starting_index = 
                                                        "search_item_starting_index";
    public static final String asearch_item_ending_index = 
                                                        "search_item_ending_index";
    public static final String aroute_description_type= "route_description_type";
    public static final String aroute_turn_boundingbox= "route_turn_boundingbox";
    public static final String aroute_turn_data =       "route_turn_data";
    public static final String aroute_landmarks =       "route_landmarks";
    public static final String aroute_vehicle =         "route_vehicle";
    public static final String async_favorites =        "sync_favorites";
    public static final String ashort_name =            "short_name";
    public static final String acategory =              "category";
    public static final String acategory_search =       "category_search";
    public static final String aactivation_code =       "activation_code";
    public static final String adevelopment =           "development";
    public static final String aimage_format =          "image_format";
    public static final String amessage_type =          "message_type";
    public static final String alanguage =              "language";
    public static final String atop_region_type =       "top_region_type";
    public static final String aposition_system =       "position_system";
    public static final String aindentingandlinebreaks= "indentingandlinebreaks";
    public static final String atop_region_crc =        "top_region_crc";
    public static final String aclient_type =           "client_type";
    public static final String aclient_lang =           "client_lang";
    public static final String atime =                  "time";
    public static final String asource =                "source";  
    public static final String aserver_list_crc =       "server_list_crc";
    public static final String aserver_auth_bob_crc =   "server_auth_bob_crc";
    public static final String ashow =                  "show";
    public static final String aemail =                 "email";
    public static final String afav_info_in_desc =      "fav_info_in_desc";
    public static final String aprevious_route_id =     "previous_route_id";
    public static final String areroute_reason =        "reroute_reason";
    public static final String akey =                   "key";
    public static final String astart_index =           "start_index";
    public static final String aend_index =             "end_index";
    public static final String amax_hits =              "max_hits";
    public static final String aroute_items =           "route_items";
    public static final String amay_use =               "may_use";
    public static final String afield =                 "field";
    public static final String adesc_version =          "desc_version";
    public static final String aversion =               "version";
    public static final String aencryption_lon =        "wg_o";
    public static final String aencryption_lat =        "wg_l";
    public static final String aencryption_alt =        "wg_a";
    public static final String aencryption_gpsweek =    "wg_w";
    public static final String aencryption_timeofweek = "wg_t";
    
    public static final String ac_mcc =                 "c_mcc";
    public static final String ac_mnc =                 "c_mnc";
    public static final String alac =                   "lac";
    public static final String acell_id =               "cell_id";
    public static final String anetwork_type =          "network_type";
    public static final String asignal_strength =       "signal_strength";
    public static final String ainclude_top_region_id = "include_top_region_id";
    
    public static final String amax_number_matches =    "max_number_matches";
    public static final String asorting =               "sorting";
    
    public static final String aclient_version =        "client_version";
    public static final String ainclude_detail_fields = "include_detail_fields";
    public static final String asearch_type =           "search_type";
    

    
    //-------------------------- END String ------------------------------------

    // entities moved to com.wayfinder.core.network.internal.mc2.impl.MC2ParserEntities
    
    /**
     * get the int value of an attribute or a text
     * 
     * @param value the number as CharArray
     * @return the number as int
     * @throws Isab_MC2XMLParserException if valkue dosen't represent a number
     */
    /*<!ENTITY % number  "NMTOKEN">      <!-- a number, format [0-9]+ -->*/

    public static final int number_type(CharArray value) 
        throws MC2ParserException {
        if (value == null) {
            throw new MC2ParserException("null number value");
        }
        try {
            return value.intValue();
        } catch (NumberFormatException e) {
            throw new MC2ParserException("invalid number value");
        }
    }
    /**
     * get the tag value for a vehicle mode
     * 
     * @param aVehicleMode the vehicle mode as int (Route.PEDESTIAN or 
     * Route.PASSENGER_CAR)
     * @return the tag as string
     */
    public static String route_vehicle(int aVehicleMode){
        return vehicle_mode[aVehicleMode];
    }

    /**
     * get the int value of an attribute or a text of type size_t
     * 
     * @param value the number as CharArray
     * @return the number as int
     *         -1 if the value is "inf"
     * @throws Isab_MC2XMLParserException if value is null or empty  
     *         or dosen't represent a size_t value
     */
    /*<!ENTITY % size_t  "%number;">     <!-- a number but inf is allowed -->*/
    /* not used at this moment
    public static final int size_type(CharArray value) 
        throws Isab_MC2XMLParserException {
        if (value == null) 
            throw new Isab_MC2XMLParserException("a size value expected");
        if (value.equal("inf")) return -1;
        try {
            return value.intValue();
        } catch (NumberFormatException e) {
            //#debug error
            e.printStackTrace();    
            throw new Isab_MC2XMLParserException("invalid size value");   
        }
    } //size_type(CharArray)
    */
    
   /**
    * search the attribute or text given by aValue param between possible
    * value of that type 
    * 
    * @param aType the enumeration type   
    * @param aValue the attribute or text value 
    * @return the constant code coressponding to the value for the  
    * enumaration type
    *   
    * Notice: return last constant code if the value was not found between 
    * possible values of that type    
    */ 
   public static final int enum_type(int aType, CharArray aValue) {
        if (aValue == null) return -1;
        
        int rez = aValue.indexIn(entity_value[aType]);
        if (rez != -1) { 
            return entityTypes[aType][rez];
        } else {
            return entityTypes[aType][entityTypes[aType].length -1];
                     //return the last one
                     //RouteReplyItem.TURN_UNKNOWN;
                     //SearchArea.OTHER;
                     //CROSSING_UNDEFINED;
        }
    } //enum_type
   
    /**
     * the reverse of method <code>enum_type(int, CharArray)</code>
     *   
     * @param aType the enumeration type
     * @param aCode the code that represent a value in enumeration type
     * @return the String value coressponding to the code for the 
     * enumeration type 
     * @throws IllegalArgumentException if the code is not valid
     *   
     * @see #enum_type(int, CharArray)
     */   
    public static final String enum_type_str(int aType, int aCode) {
        for (int i = entity_value[aType].length; i != 0; ) {
            if (aCode == entityTypes[aType][--i])
                return entity_value[aType][i];
        }
        
        throw new IllegalArgumentException("invalid value type " + aCode + 
                " for enum " + aType);
    } // enum_type_str
    
    public static final int TYPE_SearchArea         = 0;
    public static final int TYPE_SearchItem         = 1;
    public static final int TYPE_POIInfo            = 2;
    // in the DTD there is no specification that the value of the attribute type
    // should be an enumeration, but in the XML specification pdf there is
    public static final int TYPE_PersonSearchField  = 3;
    public static final int TYPE_ServerGroup        = 4;
    
    private static final String[][] entity_value = {
        /*
         *ATTRIBUTE VALUES
         *<!ENTITY % search_area_type_t "(municipal|city|citypart|zipcode|
         *                                ziparea|other)">
         * 
         */

        {//search_area_t   
            "municipal",
            "city", 
            "citypart",
            "other"
        },
        /*
         *ATTRIBUTE VALUES
         *<!ENTITY % search_item_type_t "(street|pointofinterest|category|misc|
                                other)">
         */
        {//search_item_t
            "street", 
            "pointofinterest", 
            //FIXME category is missing
            "person",
            "misc", 
            "other"
        },
        /*<!ENTITY % poi_info_t "(dont_show|text|url|wap_url|email|phone_number|
                 mobile_phone|fax_number|contact_info|short_info|
                 vis_address|vis_house_nbr|vis_zip_code|
                 vis_complete_zip|vis_zip_area|vis_full_address|
                 brandname|short_description|long_description|
                 citypart|state|neighborhood|open_hours|
                 nearest_train|start_date|end_date|start_time|
                 end_time|accommodation_type|check_in|check_out|
                 nbr_of_rooms|single_room_from|double_room_from|
                 triple_room_from|suite_from|extra_bed_from|
                 weekend_rate|nonhotel_cost|breakfast|
                 hotel_services|credit_card|special_feature|
                 conferences|average_cost|booking_advisable|
                 admission_charge|home_delivery|disabled_access|
                 takeaway_available|allowed_to_bring_alcohol|
                 type_food|decor|image_url|supplier)" >
         * 
         */    
        {
            "string",
            "number",
            "choice"
        },        
        /*<!ENTITY % server_group_t "(backup|config|server|map)" >*/
        {
            "server",
            "backup",
            "config",
            "map"
        }
       
    };
    
    static final String[] cap_name_type = {
        "gps", 
        "locator",  
        "route",  
        "fleet",
        "traffic"
    };
    
    static final String[] url_type = {
        "yes_no",
        "goto_or_exit"
    };
    
    static final String[] vehicle_mode = {      
        "passengercar",
        "pedestrian"        
    };
    
    /*<!ENTITY % te_t "(identity|base64)" >*/
    public static final String TE_BASE64 = "base64";
    
    /*<!ELEMENT opt_in EMPTY>
      <!ATTLIST opt_in name CDATA #REQUIRED> <!-- prod-info -->
    The opt_in is the optional thing the user has accepted, name specifies
    what the user has opt:ed in on.*/
    public static final String OPT_IN_NAME_ATTRIBUTE = "prod-info";
    
    public static final int CAP_GPS = 0;
    public static final int CAP_LOCATOR = 1;
    public static final int CAP_ROUTE = 2;
    public static final int CAP_FLEET = 3;
    
    private static final int SearchArea_MUNICIPAL       = 0;
    private static final int SearchArea_CITY            = 1;
    private static final int SearchArea_CITYPART        = 2;
    private static final int SearchArea_OTHER           = 3;
    
    //TODO: shoud be define is SearchItem class
    public static final int SearchItem_STREET           = 0;
    public static final int SearchItem_POINTOFINTEREST  = 1;
    public static final int SearchItem_PERSON           = 2;  
    public static final int SearchItem_MISC             = 3;
    public static final int SearchItem_OTHER            = 4;
    
    public static final int PersonSearchString          = 0;
    public static final int PersonSearchNumber          = 1;
    public static final int PersonSearchChoice          = 2;
    public static final int PersonSearchOther           = 3;

    private static final int[][] entityTypes = {
        
        { //REPRESENT ATTRIBUTE VALUES
            SearchArea_MUNICIPAL, 
            SearchArea_CITY, 
            SearchArea_CITYPART, 
            SearchArea_OTHER
        },
        {//REPRESENT ATTRIBUTE VALUES
            SearchItem_STREET, 
            SearchItem_POINTOFINTEREST, 
            SearchItem_PERSON,
            SearchItem_MISC, 
            SearchItem_OTHER
        },
        {
            PersonSearchString,
            PersonSearchNumber,
            PersonSearchChoice,
            PersonSearchOther
        },
        /*
        { //TYPE_ServerGroup  
          //REPRESENT ATTRIBUTE VALUES
            ServerGroup_TYPE_SERVER,
            ServerGroup_TYPE_BACKUP,
            ServerGroup_TYPE_CONFIG,
            ServerGroup_TYPE_MAP,
            -2//unknow, future type
        }*/
    };
    //parser

    
    


}
