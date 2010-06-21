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
package com.wayfinder.core.favorite.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.network.internal.mc2.MC2Parser;
import com.wayfinder.core.network.internal.mc2.MC2ParserException;
import com.wayfinder.core.network.internal.mc2.MC2Strings;
import com.wayfinder.core.network.internal.mc2.MC2WritableElement;
import com.wayfinder.core.network.internal.mc2.MC2Writer;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.Serializable;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldImpl;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldListImpl;
import com.wayfinder.core.shared.util.ArrayList;
import com.wayfinder.core.shared.xml.XmlIterator;
import com.wayfinder.core.shared.xml.XmlWriter;

public class FavoriteInternal implements Favorite, Serializable, MC2WritableElement {

    /**
     * this favorite has been added on the client
     */
    public static final byte STATUS_NEW = 0;
    
    /**
     * this favorite has been added on the server and downloaded to us
     */
    public static final byte STATUS_ON_SERVER = 1;

    /**
     * this favorite has been deleted on the client, but the server
     * hasn't been informed yet. 
     */
    public static final byte STATUS_DELETED = 2;

    /**
     * either was removed from server or 
     * was a new favorite and has been removed from client
     */
    public static final byte STATUS_PHANTOM = 3;
    
    /**
     * this favorite is new but is in process of synchronization with 
     * the server 
     */
    public static final byte STATUS_NEW_SENDING = 4;

    /**
     * Unique id for each favorite, set by the server
     */
    private int serverId;

    private int status;
    
    private int persistanceIndex; 

    /**
     * Name for the favorite
     * Cannot be null
     */
    private String name;

    /**
     * Description of the favorite
     * Cannot be null
     */
    private String description;

    private String iconName;

    // A has-a relation ship is healthier than an is-a relation.
    private Position position;

    /**
     * Use InfoField instead of another class FavoriteInfo as the xml API
     * is no consistent about it and use different elements name for same 
     * content
     */
    private InfoFieldListImpl infoFieldList;

    /**
     * Internal used only
     * 
     * @param serverId
     * @param name
     * @param description
     * @param position
     * @param poiInfo
     */
    public FavoriteInternal(int serverId, String name, String description, String iconName,
            Position position, InfoFieldListImpl infoFieldList) {
        
        this(name, description, iconName, position, infoFieldList);
        this.status = STATUS_ON_SERVER;
        this.serverId = serverId;
    }
    
    public FavoriteInternal(String name, String description, String iconName,
            Position position, InfoFieldListImpl infoFieldList) {
        
        if (name == null || description == null || iconName == null) {
            throw new IllegalArgumentException(
                    "name, description, iconName cannot be null");
        }

        this.status = STATUS_NEW;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        if (position == null) {
            this.position = Position.NO_POSITION;
        } else {
            this.position = position;
        }
        if (infoFieldList == null) {
            this.infoFieldList = InfoFieldListImpl.EMPTY_INFOFIELDLIST;
        } else {
            this.infoFieldList = infoFieldList;
        }
    }
    
    public FavoriteInternal(){
    }
    
//--- Favorite methods
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconName() {
        return iconName;
    }
    
    public Position getPosition() {
        return position;
    }

    public InfoFieldList getInfoFieldList() {
        return infoFieldList;
    }
//---
    
    public int getServerId() {
        return serverId;
    }
    
    public void setPersistanceIndex(int index) {
        this.persistanceIndex = index;
    }
    
    public int getPersistanceIndex() {
        return persistanceIndex;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public int getStatus() {
        return status;
    }

//--- Serializable
    public void read(DataInputStream din) throws IOException {
        serverId = din.readInt();
        status = din.readInt();
        
        //if fav is new or is on server there is the whole data
        if (status == STATUS_NEW || status == STATUS_ON_SERVER) {
            name = din.readUTF();
            description = din.readUTF();
            iconName = din.readUTF();
            
            position = new Position();
            position.read(din);
            
            //guaranteed  to not be null
            infoFieldList = InfoFieldListImpl.restore(din);
        }// else nothing else to read
    }

    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(serverId);
        dout.writeInt(status);
        
        //if fav is new or is on server save the whole data
        if (status == STATUS_NEW || status == STATUS_ON_SERVER) {
            dout.writeUTF(name);
            dout.writeUTF(description);
            dout.writeUTF(iconName);
            position.write(dout);
            
            infoFieldList.write(dout);
        }// else nothing else to save
    }
//---
    
    public boolean isPhantom() {
        return (status == STATUS_PHANTOM);
    }
    
    public boolean isVisible() {
        return (status == STATUS_ON_SERVER || status == STATUS_NEW || status == STATUS_NEW_SENDING);
    }
    
    public boolean isOnServer() {
        return serverId != 0;
    }

    public boolean markDeleted() {
        if (status == STATUS_ON_SERVER) {
            status = STATUS_DELETED;
        } else if (status == STATUS_NEW) {
            status = STATUS_PHANTOM;
        } else {//STATUS_NEW_SENDING //STATUS_DELETED //STATUS_PHANTOM
            return false;
        }
        return true;
    }
    
    public String toString() {
        return this.iconName + ": " + this.name +  "-" + this.description ;
    }
    
//--- MC2 xml

    /*
    <!ELEMENT favorite ( position_item, fav_info* )>
    <!ATTLIST favorite
        id CDATA #REQUIRED
        name CDATA #REQUIRED
        
        // A short name of the favorite for
        // quick selection. Might not be
        // available on all interfaces.
        short_name CDATA #REQUIRED
        description CDATA #REQUIRED
        
        //Used to group favorites together.
        //Currently not used.
        category CDATA #REQUIRED
        
        //The symbol to use for the favorite when drawn on maps. 
        //Currently not used.
        map_icon_name CDATA #REQUIRED > 
    <!ELEMENT fav_info EMPTY >
    <!ATTLIST fav_info type %poi_info_t; #REQUIRED
        key CDATA #REQUIRED
        value CDATA #REQUIRED >
    */
    public void write(MC2Writer mc2w) throws IOException {
        mc2w.startElement(MC2Strings.tfavorite);
        //add a dummy id just because is a required attribute  
        mc2w.attribute(MC2Strings.aid, serverId);
        mc2w.attribute(MC2Strings.aname, this.name);
        
        // A short name of the favorite for quick selection. 
        // Might not be available on all interfaces.
        mc2w.attribute(MC2Strings.ashort_name, EMPTY_STRING);

        mc2w.attribute(MC2Strings.adescription, this.description);
        
        //Used to group favorites together. Currently not used.
        mc2w.attribute(MC2Strings.acategory, EMPTY_STRING);
        
        //The symbol to use for the favorite when drawn on maps. 
        mc2w.attribute(MC2Strings.amap_icon_name, iconName);
        
        this.position.write(mc2w);

        for(int i = 0, n = infoFieldList.getNbrInfoFields(); i < n; i++) {
            /*<!ELEMENT fav_info EMPTY >
            <!ATTLIST fav_info type %poi_info_t; #REQUIRED
              key CDATA #REQUIRED
              value CDATA #REQUIRED >*/
            InfoFieldImpl field =  infoFieldList.getInfoFieldImplAt(i);
            mc2w.startElement(MC2Strings.tfav_info); 
            mc2w.attribute(MC2Strings.atype, field.getTypeString());
            mc2w.attribute(MC2Strings.akey, field.getKey());
            mc2w.attribute(MC2Strings.avalue, field.getValue());
            mc2w.endElement(MC2Strings.tfav_info);
        }

        mc2w.endElement(MC2Strings.tfavorite);
    }
    
    public void parse(MC2Parser mc2p) throws IOException, IllegalStateException, MC2ParserException {
        status = STATUS_ON_SERVER; //as it's come from the server 

        serverId = mc2p.attributeAsInt(MC2Strings.aid);
        name = mc2p.attribute(MC2Strings.aname);
        iconName = mc2p.attribute(MC2Strings.amap_icon_name);
        description = mc2p.attribute(MC2Strings.adescription);
        mc2p.childrenOrError();
        
        ArrayList infoList = new ArrayList();
        do {
            if (mc2p.nameRefEq(MC2Strings.tposition_item)) {
                Position pos = new Position();
                pos.parse(mc2p);
                position = pos;
            } else if (mc2p.nameRefEq(MC2Strings.tfav_info)) {
                /*<!ELEMENT fav_info EMPTY >
                  <!ATTLIST fav_info type %poi_info_t; #REQUIRED
                    key CDATA #REQUIRED
                    value CDATA #REQUIRED >*/
                //TODO: we cannot use the method from MC2Parser to parse
                //the info field as the the element is different
                int type = InfoFieldImpl.getTypeForString(
                        mc2p.attributeCharArray(MC2Strings.ainfo_type));

                String key = mc2p.attribute(MC2Strings.akey);
                String value = mc2p.attribute(MC2Strings.avalue);
                infoList.add(new InfoFieldImpl(type,key,value));
            }
        } while (mc2p.advance());
        if (infoList.isEmpty()) {
            infoFieldList = InfoFieldListImpl.EMPTY_INFOFIELDLIST;
        } else {
            InfoFieldImpl[] array = new InfoFieldImpl[infoList.size()];
            infoList.copyInto(array);
            infoFieldList = new InfoFieldListImpl(array);
        }
        mc2p.nameOrError(MC2Strings.tfavorite);
    }
   

//---



}
