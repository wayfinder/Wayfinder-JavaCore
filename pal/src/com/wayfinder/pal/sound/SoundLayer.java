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
package com.wayfinder.pal.sound;

/**
 * Access point for obtaining dependent resources for sound processing 
 * given in the abstract form of SoundPlayers.
 * 
 * This can play a single type of sound files, all files should have same 
 * extension and located in the same root directory. Which is up to each 
 * platform implementation.    
 * 
 *  
 * 
 */
public interface SoundLayer {
    /**
     * Create a sound player that will play a single sound pointed by file path
     * When done with the SoundPlayer, you should call destroy(), to free the 
     * resources. If not released, too many SoundPlayer instances may result 
     * in an exception.
     * 
     * @param filePath
     * @return SoundPlayer object can be used to play the given sound
     */
    SoundPlayer create(String filePath, int duration);
    
    /**
     * create a sound player that will play multiple sounds one after each other
     * 
     * @param filePaths the array containing the sound files identification
     * @param durations the array containing the sound durations, if wrong value 
     * the playing will be incorrect
     * 
     * @return SoundPlayer object can be used to play the given sounds
     */
    SoundPlayer create(String[] fileNames, int[] durations);
    
    /**
     * Determine the duration of each sound from the list and return an array of 
     * the same length with the duration for each sound in milliseconds
     * This method should be run in a separate thread as it's blocking; will 
     * prepare each sound without playing it   
     *  
     * @param filePaths the array containing the sound files identification
     * @return int array representing the duration in milliseconds for each 
     * sound 
     * @throws SoundException if one of the file could not be load and prepared
     */
    int[] getDuration(String[] filePaths) throws SoundException;
}   
