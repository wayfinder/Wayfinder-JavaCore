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
package com.wayfinder.core.sound.internal.voicesyntax;

import com.wayfinder.core.shared.util.CharArray;

/**
 * This is mainly a container that holds the response from the SyntaxTree
 * 
 * 
 * @since 1.15.1
 */
public class SyntaxResponse {
    
    private final String[] iSoundsToPlay;
    private final int iTimeToStartAhead;
    private final int[] iSoundDuration;
    
    /**
     * Creates a new SyntaxResponse
     * 
     * @param sounds The array of sound filenames, with no file ending
     * @param aTimeToStartAhead The time to play ahead depends on sound clips 
     * length and timingmarker
     */
    SyntaxResponse(String[] sounds, int[] durations, int aTimeToStartAhead) {
        iSoundsToPlay = sounds;
        iSoundDuration = durations;
        iTimeToStartAhead = aTimeToStartAhead;
    }
    
    /**
     * Get the names of the sounds
     * @return The array of sound filenames, with no file ending
     */
    public String[] getSoundsInternalArray() {
        return iSoundsToPlay;
    }
    
    /**
     * Get the names of the sounds duration
     * @return The array of sound durations
     */
    public int[] getDurationsInternalArray() {
        return iSoundDuration;
    }
    
    /**
     * Get the time to start ahead in miliseconds
     */
    public int getTimeToStartAhead() {
        return iTimeToStartAhead;
    }
    
    public String toString() {
        CharArray ca = new CharArray(100);
        ca.append("SyntaxResponse ");
        ca.append(" time ahead: " + iTimeToStartAhead );
        ca.append(" sounds: ");
        for (int i = 0; i< iSoundsToPlay.length; i++) {
            if (i != 0) {
                ca.append(", ");
            }
            ca.append(iSoundsToPlay[0]);
        }
 
        return ca.toString();
    }
}
