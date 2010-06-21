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
package com.wayfinder.core.userdata.internal.hwkeys;

import com.wayfinder.core.shared.internal.ParameterValidator;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.hardwareinfo.HardwareInfo;


/**
 * Acts as a base class for the collection of hardware keys.
 */
public final class KeyCollector {
    
    private final HardwareInfo m_hardInfo;
    
    /**
     * Standard constructor
     * 
     * @param info The {@link HardwareInfo} from the PAL
     */
    public KeyCollector(HardwareInfo info) {
        m_hardInfo = info;
    }
    
    
    /**
     * Grabs all available hardware keys from the device. There are no
     * guarantees of which type of hardware keys are collected, since this
     * heavily depends on which platform is used and which available APIs 
     * exists.
     * <p>
     * If a dummy key is passed as a parameter, this will be added to the list
     * of hardware keys. <b>The client must take extra steps to ensure that this
     * key is generated on the server side to ensure that several users don't
     * collide. At no point is the client allowed to generate a key on it's
     * own.</b>
     * <p>
     * This method should usually only be used if the current platform doesn't
     * have any real keys. If real keys exists, there is no use or need in
     * creating fake keys.
     * 
     * @return An array containing the collected hardware keys. This will never
     * be null and always have at least 1 (one) key in the array.
     * @throws IllegalArgumentException if aFakeKey is the empty string
     * @throws NoHardwareKeysAvailableException if no keys could be collected.
     * If a fake key is supplied as a parameter, this exception will never be
     * thrown
     * @throws HardwareKeyDisallowedException if the KeyVerifier disallows the 
     * keys
     */
    public final HardwareKeyContainer grabAllHardwareKeys() {
        
        LinkedList list = new LinkedList();
        populateWithKeys(list);
        
        final int size = list.size();
        HardwareKey[] keys = new HardwareKey[size];
        list.toArray(keys);
        return new HardwareKeyContainer(keys);
    }
    

    
    /**
     * When called, the implementing class should perform the following actions:
     * <p>
     * <ol>
     * <li>Collect all available hardware keys</li>
     * <li>For each key create a HardwareKey object with the correct type</li>
     * <li>Add the HardwareKey object to the supplied LinkedList object
     * </ol>
     * Subclasses are responsible for ensuring that:
     * <ul>
     * <li>all keys are collected correctly</li>
     * <li>the HardwareKey objects are created with the correct types</li>
     * <li>no duplicates of hardware keys are created. Each type should only
     * exist once (eg there should only be one object of type "imsi" and so on)</li>
     * <li>all keys are correctly formated. Since different platforms report
     * keys differently, it's not possible for the superclass to handle the
     * formating</li>
     * </ul>
     * If the platform lacks all kinds of hardware keys by design, the 
     * LinkedList object should be left empty.
     * 
     * @param aList A LinkedList to populate
     * @throws NoHardwareKeysAvailableException If only one key is available on
     * the platform and the subclass was unable to read it.
     */
    private void populateWithKeys(LinkedList list) {
        tryAddKey(list, HardwareKey.HARDWARE_KEY_TYPE_IMEI, m_hardInfo.getIMEI());
        tryAddKey(list, HardwareKey.HARDWARE_KEY_TYPE_ESN, m_hardInfo.getESN());
        tryAddKey(list, HardwareKey.HARDWARE_KEY_TYPE_BBPIN, m_hardInfo.getBlackBerryPIN());
        tryAddKey(list, HardwareKey.HARDWARE_KEY_TYPE_BTMAC, m_hardInfo.getBluetoothMACAddress());
        tryAddKey(list, HardwareKey.HARDWARE_KEY_TYPE_IMSI, m_hardInfo.getIMSI());
    }
    
    
    private static void tryAddKey(LinkedList list, int keyType, String keyData) {
        if(!ParameterValidator.isEmptyString(keyData)) {
            list.addLast(new HardwareKey(keyType,keyData));
        }
    }
    
    
    public static HardwareKeyContainer createHardcodedHWKeyContainerWithIMEI(String imei) {
        HardwareKey[] keys = new HardwareKey[1];
        keys[0] = new HardwareKey(HardwareKey.HARDWARE_KEY_TYPE_IMEI, imei);
        return new HardwareKeyContainer(keys);
    }
    
}
