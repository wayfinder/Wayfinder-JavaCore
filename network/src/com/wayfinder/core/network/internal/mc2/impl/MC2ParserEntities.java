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
package com.wayfinder.core.network.internal.mc2.impl;

import com.wayfinder.core.network.internal.mc2.MC2Strings;

final class MC2ParserEntities extends MC2Strings {
    
    /**
     * Represent the list of element names passed to the xml parser. 
     *   
     * The XmlInterator.name() will return one of this constants, or null
     * if the current element is not included in this array
     * 
     * This is an optimization in order to avoid the creation of string object 
     * for each element and also allows the use of reference comparison (==) 
     * instead of string.equal()\
     * 
     * WARNING: All the element names from PARSER & COMMON USE sections 
     * must be added here.
     * 
     * NOTICE 1: The parser will not skip the xml elements that are not 
     * in this array, just the name() method will return always null 
     * for those elements
     * NOTICE 2: Add the element names used for validation (checking) 
     * at the begining of the array 
     * NOTICE 3: Add ifdef elements right after the validation elements  
     */
    static final String[] elementNameArray = { //used for reply only
            tcopyright_str_reply,
            tactivate_reply,
            temail_reply,
            tpoi_info_reply,
            troute_reply,
            troute_reply_header,
            troute_origin,
            troute_destination,
            troute_reply_items,
            text_service,
            tuser,
            tuser_reply,
            tserver_group,
            tserver,
            tuser_cap_reply, 
            tsearch_hit_type,
            tzoom_settings_reply,
            tzoom_level,
            tsimple_poi_desc_reply,
            text_services_reply,
            ttunnel_reply,
            ttop_region_reply,
            tareaid,
            tuser_favorites_reply, 
            tuser_favorites_crc_reply,
            tfavorite_id,
            tfavorite,
            titemid,
            tcopyright_str_data,
            tzoom_levels, 
            tsimple_poi_desc_data,
            tuser_id,
            tuser_track_add_reply,
            tpin,
            tcap,
            tfav_info,
            tserver_list,
            tserver_auth_bob,
            tpopup,
            tpopup_message,
            tpopup_once,
            tpopup_url,
            taverage_speed_nbr,
            trouting_vehicle, 
            ttotal_time, 
            tstatus_code, 
            tdescription, 
            tboundingbox ,
            tlocation_name, 
            tinfo_field, 
            tauth_token, 
            tsearch_item,
            tname,
            tadd_favorite_list, 
            tdelete_favorite_id_list,
            tsearch_area, 
            tlat, 
            tlon,
            tposition_item,
            ttop_region_id,
            tname_node,
            text_services_crc_ok,
            ttop_region_crc_ok,
            theader,
            tbody,
            tcompact_search_reply,
            tsearch_hit_list,
            tcrc_ok,
            timage_name,
            tstatus_uri,
            tcategory,
            tcategory_name,
            tcategory_list_reply,
            tcat,
            tad_results_text,
            tall_results_text,
            ttype,
            ttop_region,
            tsearch_item_list,
            tinfo_item,
            tlocal_category_tree_reply,
            tcategory_table,
            tlookup_table,
            tstring_table,
            tcategory_list,
            tsearch_list,
            tsearch_match,
            tone_search_reply,
            tserver_info_reply,
            tclient_type_info,
            tpoi_detail_reply,
            tdetail_item,
            tdetail_field,
            tresources,
            timage_group,
            treview_group,
            timage,
            treview            
    };
    
    
    /**
     * Represent the list of attribute names passed to the xml parser. 
     *  
     * The LightXmlPullInterator will skip all other attributes 
     * (that are not contained by this array)  
     * 
     * This is an optimization in order to avoid the creation of string object 
     * for each attribute name and also to save memory by discarding the 
     * attributes that we are not interested in.  
     * 
     * WARNING: All the attributes names from PARSER & COMMON USE sections 
     * must be added here.
     * 
     * NOTICE 1: The parser will skip the xml attributes that are not 
     * in this array.
     * NOTICE 2: Add the attribute names used for validation (checking) 
     * at the begining of the array 
     * NOTICE 3: Add ifdef elements right after the validation purpose 
     * attributes name
     */    
    static final String[] attributeNameArray = {//used for reply only
            atop_region_type,
            aPIN, 
            apixel_size,
            azoom_j2me,
            azoom_level_nbr,
            amax_x,
            amax_y,
            amin_x,
            amin_y,
            afield,
            ate,
            akey,
            avalue,
            aurl_type,
            acrc,
            auin, 
            aid, 
            aname,
            aptui,
            aroute_id, 
            anorth_lat, 
            asouth_lat, 
            aeast_lon, 
            awest_lon, 
            asearch_area_type, 
            asearch_item_type, 
            atransaction_id, 
            anumberfields,
            ainfo_type,
            adescription, 
            anumberitems, 
            atotal_numberitems,
            atype,
            atop_region_crc,
            aswitch_group_threshold,
            astatus_line,
            aheading,
            around,
            alength,
            acrc_match,
            aimage,
            ahref,
            acount,
            atop_hits,
            acat_id,
            aadvert,
            aencrypted_lon,
            aencrypted_lat,
            aouter_radius,
            amap_icon_name,
            anumber_matches,
            atotal_number_matches,
            abrand_image, 
            acategory_image,
            aprovider_image,
            aadditional_info_exists,
            
            aupgrade_available,
            alatest_version,
            aforce_upgrade,
            aupgrade_id,
            
            adetail_type,
            adetail_content_type,
            aurl,
            anumber_images,
            aprovider_name,
            anumber_reviews,
            adate,
            areviewer,
            arating
    };

}
