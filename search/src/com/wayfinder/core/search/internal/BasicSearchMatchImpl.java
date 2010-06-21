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
 * Copyright, Wayfinder Systems AB, 2010
 */

/**
 * 
 */
package com.wayfinder.core.search.internal;

import com.wayfinder.core.search.SearchReply.BasicSearchMatch;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.Comparator;
import com.wayfinder.pal.util.StringCollator;

/**
 * <p>Implementation of {@link BasicSearchMatchImpl}.</p>
 * 
 * <p>This class is thread safe by virtue of all fields being final
 * or volatile.</p>
 */
class BasicSearchMatchImpl
    implements BasicSearchMatch {

    private final String m_matchID;
    private final String m_matchLocation;
    private final String m_matchName;
    private final Position m_position;


    /**
     * @param matchID see {@link #getMatchID()}. Must not be null.
     * @param matchLocation see {@link #getMatchLocation()}. Must not be null.
     * @param matchName see {@link #getMatchName()}. Must not be null.
     * @param position see {@link #getPosition()}. Must not be null.
     */
    BasicSearchMatchImpl(String matchID,
                         String matchLocation,
                         String matchName, 
                         Position position) {
        if (matchID == null
            || matchLocation == null
            || matchName ==  null
            || position == null) {
            throw new IllegalArgumentException();
        }
        
        m_matchID = matchID;
        m_matchLocation = matchLocation;
        m_matchName = matchName;
        m_position = position;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.SearchReply.BasicSearchMatch#getMatchID()
     */
    public String getMatchID() {
        return m_matchID;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.SearchReply.BasicSearchMatch#getMatchLocation()
     */
    public String getMatchLocation() {
        return m_matchLocation;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.SearchReply.BasicSearchMatch#getMatchName()
     */
    public String getMatchName() {
        return m_matchName;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.search.SearchReply.BasicSearchMatch#getPosition()
     */
    public Position getPosition() {
        return m_position;
    }
    
    
    // -------------------------------------------------------------------
    // sorting

    /* 
     * Hack to avoid calculating the distance over and over again.
     * Must be volatile or synchronization protected because for each
     * reply from the server, we schedule the re-sorting on the WorkScheduler.
     * And we have no guarantees that a new Thread is created for each
     * instances (Thread pooling can be used). And thus we can't use the
     * the guarantee that all actions done before Thread.start() are
     * visible to the new thread.
     * 
     * TODO: is the synchronization overhead greater than calculating
     * the cos-lat and square root?
     */

    private volatile int m_sortingDistance = Integer.MIN_VALUE;
    private volatile Position m_sortingPosition;

    /**
     * 
     * @param position
     * @return the distance to position in meters. Always return >= 0.
     */
    private int getDistance(Position position) {
        if(m_sortingDistance == Integer.MIN_VALUE
           || m_sortingPosition != position) {
            m_sortingDistance = getPosition().distanceTo(position);
            m_sortingPosition = position;
        }
        return m_sortingDistance;
    }
    
    public static class PositionDistanceComparator implements Comparator {
        
        /**
         * the {@link Position} wrto which we compute the distances from two
         * matches to see which is closer
         */
        private final Position m_refPos;
        
        /**
         * Create a {@link PositionDistanceComparator}
         * 
         * @param refPos the reference position
         */
        public PositionDistanceComparator(Position refPos) {
            m_refPos = refPos;
        }

        /* (non-Javadoc)
         * @see com.wayfinder.core.shared.util.Comparator#canBeCompared(java.lang.Object)
         */
        public boolean canBeCompared(Object obj) {
            return (obj != null) && (obj instanceof BasicSearchMatchImpl);
        }
        
        /**
         * @return 
         * <ul>
         *  <li> < 0 if obj1 is closer to the reference position than obj2</li>
         *  <li> 0 if obj1 and obj2 are at the same distance from the reference position</li>
         *  <li> > 0 if obj1 is farther from the reference position than obj2</li>
         * </ul>
         */
        public int compare(Object obj1, Object obj2)
                throws IllegalArgumentException {
            if (obj1 == null || obj2 == null) {
                throw new IllegalArgumentException("Objects compared must not be null!");
            }
            if (! (obj1 instanceof BasicSearchMatchImpl && obj2 instanceof BasicSearchMatchImpl)) {
                throw new IllegalArgumentException("Objects compared must both be of type BasicSearchMatchImpl!");
            }
            
            int dist1 = ((BasicSearchMatchImpl) obj1).getDistance(m_refPos);
            int dist2 = ((BasicSearchMatchImpl) obj2).getDistance(m_refPos);
            
            return dist1 - dist2;
        }
    }
    
    public static class MatchNameComparator implements Comparator {
        
        private final StringCollator m_collator;
        
        public MatchNameComparator(StringCollator collator) {
            m_collator = collator;
        }
        /* (non-Javadoc)
         * @see com.wayfinder.core.shared.util.Comparator#canBeCompared(java.lang.Object)
         */
        public boolean canBeCompared(Object obj) {
            return (obj != null) && (obj instanceof BasicSearchMatchImpl);
        }

        /* (non-Javadoc)
         * @see com.wayfinder.core.shared.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object obj1, Object obj2)
                throws IllegalArgumentException {
            if (obj1 == null || obj2 == null) {
                throw new IllegalArgumentException("Objects compared must not be null!");
            }
            if (! (obj1 instanceof BasicSearchMatchImpl && obj2 instanceof BasicSearchMatchImpl)) {
                throw new IllegalArgumentException("Objects compared must both be of type BasicSearchMatchImpl!");
            }
            return m_collator.compare(
                    ((BasicSearchMatchImpl) obj1).getMatchName(),
                    ((BasicSearchMatchImpl) obj2).getMatchName());
        }
        
    }
}
