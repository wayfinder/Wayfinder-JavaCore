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

/**
 *
 * Represent a review list for a POI from a provider
 *    
 * 
 */ 
public class ReviewGroup {
    
    private final Review[] m_reviews;
    private final Provider m_provider;
    
    private final Review[] EMPTY_REVIEW_ARRAY = new Review[0];
    
    /**
     * @param provider the {@link Provider} of the images must not be null
     * @param reviewInternalArray the array with the {@link Review}-s  
     * can be null. 
     */
    public ReviewGroup(Provider provider, Review[] reviewInternalArray) {
        super();
       
        if (provider == null) { 
            throw new IllegalArgumentException("Ctr param provider cannot be null");
        }
        m_provider = provider;
        
        if (reviewInternalArray == null) {
            m_reviews = EMPTY_REVIEW_ARRAY;
        } else {
            m_reviews = reviewInternalArray;
        }
    }

    /**
     * Never return null
     * @return a {@link Provider} of the images 
     */
    public Provider getProvider() {
        return m_provider;
    }
    
    /**
     * @return the total number of reviews (0 if there are none)
     */
    public int getNbrOfReviews() {
        return m_reviews.length;
    }

    /**
     * @param index must be positive and smaller than {@link #getNbrOfReviews()}
     * @return the {@link Review} at the specified index in this group 
     */
    public Review getReview(int index) {
        return m_reviews[index];
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() { 
        return "ReviewGroup " + m_provider
                + " nbrOfReview: " + m_reviews.length; 
    }
}
