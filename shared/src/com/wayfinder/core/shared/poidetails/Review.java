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
package com.wayfinder.core.shared.poidetails;

public class Review {
    
    //TODO is this global or is provider dependent?
    public static int MAXIM_RATING_NO = 5; 
    
    //TODO is this enough to be a String 
    private final String m_date;
    private final String m_reviewer;
    private final int m_ratingNumber;

    private final String m_text;

    public Review(String date, String reviewer, int ratingNumber, String text) {
        super();
        m_date = date;
        m_reviewer = reviewer;
        m_ratingNumber = ratingNumber;
        m_text = text;
    }

    /**
     * @return the date of the review as text e.g. "2007-11-01T14:27:35+01:00"
     */
    public String getDate() {
        return m_date;
    }

    /**
     * @return the reviewer name of nickname e.g. "Londoner"
     */
    public String getReviewer() {
        return m_reviewer;
    }

    /**
     * @return a number from 0 to {@link #MAXIM_RATING_NO} representing 
     * the rating of the place, bigger is better.
     */
    public int getRatingNumber() {
        return m_ratingNumber;
    }

    /**
     * @return the full text of the review
     */
    public String getText() {
        return m_text;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Review from " + m_reviewer 
        + " at " + m_date 
        + " rating " + m_ratingNumber 
        + ": " + m_text;
    }
}
