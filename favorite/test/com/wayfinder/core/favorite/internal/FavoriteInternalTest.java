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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldImpl;
import com.wayfinder.core.shared.internal.poiinfo.InfoFieldListImpl;
import com.wayfinder.core.shared.poiinfo.InfoField;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;

import junit.framework.TestCase;


public class FavoriteInternalTest extends TestCase {

    /**
     * Internal used only
     * 
     * @param serverId
     * @param name
     * @param description
     * @param position
     * @param poiInfo
     */
    public void testFavoriteInternalServer() {
        Position position = new Position(340,-125);
        FavoriteInternal fav = new FavoriteInternal(101,"Cofe Shop", "Lund", "star", position, null);
        
        assertEquals(fav.getDescription(), "Lund");
        assertEquals(fav.getName(), "Cofe Shop");
        assertEquals(fav.getPosition(), position);
        
        assertEquals(fav.getStatus(), FavoriteInternal.STATUS_ON_SERVER);
        
        assertTrue(fav.isVisible());
    }

    public void testFavoriteInternalNew() {
        Position position = new Position(340,-125);
        FavoriteInternal fav = new FavoriteInternal("Cofe Shop", "Lund", "star", position, null);
        
        assertEquals("Lund", fav.getDescription());
        assertEquals("Cofe Shop", fav.getName());
        assertEquals(position, fav.getPosition());
        
        assertEquals(fav.getStatus(), FavoriteInternal.STATUS_NEW);
        assertTrue(fav.isVisible());
    }
    
    public void testFavoriteInternalNull() {
        try {
            FavoriteInternal fav = new FavoriteInternal(null, "Lund", "star", null, null);
            fail("ctr. parmater name null checking");
        } catch (IllegalArgumentException e){
        }
    }

    
    public void testReadWrite() throws IOException {
        ByteArrayOutputStream byteInput = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(byteInput);
        
        Position position = new Position(340,-125);
        FavoriteInternal favO1 = new FavoriteInternal(101,"Coffee shop", "Lund", "shop", position, null);
        favO1.write(dout);

        FavoriteInternal favO2 = new FavoriteInternal("Bar", "Malmo", "bar", null, null);
        favO2.write(dout);

        FavoriteInternal favO3 = new FavoriteInternal(102,"Parking lot", "Cluj", "", null, null);
        favO3.markDeleted();
        favO3.write(dout);
        dout.flush();

        DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteInput.toByteArray()));
        
        FavoriteInternal favI1 = new FavoriteInternal();
        favI1.read(din);
        assertEquals("Lund", favI1.getDescription());
        assertEquals("shop", favI1.getIconName());
        
        FavoriteInternal favI2 = new FavoriteInternal();
        favI2.read(din);
        assertEquals("Malmo", favI2.getDescription());
        assertEquals("Bar", favI2.getName());

        FavoriteInternal favI3 = new FavoriteInternal();
        favI3.read(din);
        assertEquals(FavoriteInternal.STATUS_DELETED, favI3.getStatus());
        //deleted favorite don't have save only status and id  
        assertEquals(null,favI3.getName());
        assertEquals(null,favI3.getDescription());
    }
   
    public void testReadWriteWithInfo() throws IOException {
        ByteArrayOutputStream byteInput = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(byteInput);
        
        InfoFieldImpl info1 = new InfoFieldImpl(InfoField.TYPE_MOBILE_PHONE,"mobile","+40745342343");
        InfoFieldImpl info2 = new InfoFieldImpl(InfoField.TYPE_EMAIL,"email","root@wayfinder.com");
        InfoFieldListImpl infoList1 = new InfoFieldListImpl(new InfoFieldImpl[]{info1,info2});
        
        FavoriteInternal favO1 = new FavoriteInternal(101,"Coffee shop", "Lund", "shop", null, infoList1);
        favO1.write(dout);
        
        Position position1 = new Position(140, 254);
        InfoFieldListImpl infoList2 = new InfoFieldListImpl(new InfoFieldImpl[]{info2});
        
        FavoriteInternal favO2 = new FavoriteInternal("Bar", "Malmo", "bar", position1, infoList2);
        favO2.write(dout);
        
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteInput.toByteArray()));
        
        FavoriteInternal favI1 = new FavoriteInternal();
        favI1.read(din);
        assertEquals("Lund", favI1.getDescription());
        assertEquals(2, favI1.getInfoFieldList().getNbrInfoFields());
        assertEquals("+40745342343", favI1.getInfoFieldList().getInfoFieldAt(0).getValue());
        assertEquals(InfoField.TYPE_EMAIL, favI1.getInfoFieldList().getInfoFieldAt(1).getType());
        assertFalse(favI1.getPosition().isValid());
        
        
        FavoriteInternal favI2 = new FavoriteInternal();
        favI2.read(din);
        assertEquals("Malmo", favI2.getDescription());
        assertEquals("bar", favI2.getIconName());
        assertEquals(1, favI2.getInfoFieldList().getNbrInfoFields());
        assertEquals("email", favI2.getInfoFieldList().getInfoFieldAt(0).getKey());
        assertEquals(254,favI2.getPosition().getMc2Longitude());
        
    }
}
