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
 * Copyright, Wayfinder Systems AB, 2009
 */

package com.wayfinder.pal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface UtilFactory {

    /**
     * Creates a new GZIP output stream. 
     * 
     * @param stream the output stream.
     * @return a gzip output stream.
     * @throws IOException if an I/O error has occurred.
     */
    public OutputStream openGZIPOutputStream(OutputStream stream) throws IOException;
    
    /**
     * Creates a new GZIP input stream.
     * 
     * @param stream the input stream. 
     * @return a gzip input stream. 
     * @throws IOException if an I/O error has occurred. 
     */
    public InputStream openGZIPInputStream(InputStream stream) throws IOException;
    
    /**
     * Obtains a collator that can be used to order Strings in a lexicographic
     * order depending on the current locale. The implementation uses the
     * platform specific tools that deal with locale-dependent ordering of
     * strings.
     * 
     * @param collationStrength comparison level, one of
     * {@link StringCollator#STRENGTH_PRIMARY}, {@link StringCollator#STRENGTH_SECONDARY},
     * or {@link StringCollator#STRENGTH_TERTIARY}
     * 
     * @see {@link http://www.unicode.org/reports/tr10/#Multi_Level_Comparison}
     * for details on comparison levels
     * 
     * <br />
     * <b>IMPORTANT:</b> canonical decomposition is always used.
     * 
     * @return a StringCollator
     */
    public StringCollator getStringCollator(int collationStrength);
    
}
