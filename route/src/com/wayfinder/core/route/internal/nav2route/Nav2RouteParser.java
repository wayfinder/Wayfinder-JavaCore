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

package com.wayfinder.core.route.internal.nav2route;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.wayfinder.core.route.RouteRequest;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.internal.UTF8CStringBufferParser;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.route.NavRoutePoint;
import com.wayfinder.core.shared.route.Turn;
import com.wayfinder.core.shared.util.io.WFByteArrayInputStream;
import com.wayfinder.core.shared.util.io.WFDataInputStream;


/**
 * <p>This class creates our route data model from the raw nav2 route. It also
 * encapsulates the static abstraction functions that operates on the raw data.</p>
 * 
 * <p>The abstraction functions serve as a protocol specification. Those could
 * also be in a separate static class but that would be clunky to use since
 * static imports are not available in Java 1.3.</p>
 * 
 * <p>The route is divided into 12 byte datums where the first 2 bytes encodes
 * an unsigned 16-bit integer describing the datum type and thus how the rest
 * of the datum is laid out. The reference is "Route data V1.08". Formal
 * C-definition:<pre>
 * typedef struct nav_route_datum {
 *   uint16 action;    // Type of structure.
 *   union {
 *     struct nav_route_point              rp;
 *     struct nav_route_meta_point_origo   ro;
 *     struct nav_route_meta_point_scale   rs;
 *     struct nav_route_point_mini_delta_points rd;
 *     struct nav_route_point_micro_delta_points rdm;
 *     struct nav_route_point_meta         rm;
 *   } u;
 * } nav_route_datum_t;</pre>
 * 
 * <p>When retrieved from the server, we use the navigatorroute.bin interface, 
 * which delivers the route is in a blob containing also string table and
 * overall information. The specification can be found in the following
 * MC2-code:<code>
 * <ol><li>Server/Servers/src/HttpNavigatorFunctions.cpp HttpNavigatorFunctions::htmlNavigatorRoute()
 *     <li>Server/Servers/src/isabBoxRouteMessage.cpp isabBoxRouteReply::convertToBytes(...)
 *     <li>Server/Servers/src/isabBoxNavMessage.cpp isabBoxNavMessageUtil::convertHeaderToBytes(...)
 * </ol></code>
 * Protocol version must be 11.</p>
 * 
 * <p>This class is not thread-safe as it is intended to be used only by one
 * thread. But the constructed Nav2Route is.</p>
 * 
 * TODO: separate Exception class to separate malformed routes (means 
 * that we made a programming error or some proxy destroyed the data) from
 * pure I/O errors? 
 */
public final class Nav2RouteParser {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(Nav2RouteParser.class);
    

    // -----------------------------------------------------------------------
    // protocol functions. See also the class Action.
    
    /**
     * The number of bytes per datum. Used to skip forward.
     */
    static final int NBR_BYTES_PER_DATUM = 12;

    /**
     * Check if a nav_route_point.flags indicates right or left hand side
     * driving.
     * 
     * @param flags - read from nav_route_point.flags.
     * @return true if flags indicate right hand side driving; false otherwise.
     */
    static boolean isDriveOnRightSide(int flags) {
        return ((flags & 0x02) == 0); // isLeftTraffic, 1 bit, 0: false 1: true
    }


    // -----------------------------------------------------------------------
    private InputStream m_in;

    /**
     * Set up a Nav2RouteParser to parse the 
     * @param routeRequest
     * @param in
     */
    Nav2RouteParser(InputStream in) {
        m_in = in;
    }

    /**
     * <p>Parse the data sent by navigatorroute.bin and return a new
     * {@link Nav2Route}.</p>
     *
     * <p>TODO: when the internal route reply class is defined we 
     * should sent that in and take route id and boundingbox from it.</p>
     *
     * @param boundingBox - must not be modified after call.
     * @param in - the InputStream to read the reply from.
     * @throws IOException if an I/O error occurs or the route was broken.
     * 
     */
    public static Nav2Route
    parseNavigatorRouteBin(RouteRequest routeRequest,
                           String routeID,
                           BoundingBox boundingBox,
                           InputStream in)
        throws IOException {

        Nav2RouteParser parser = new Nav2RouteParser(in);
        parser.parse();

        return new Nav2Route(routeID,
                             routeRequest.getRequestedOrigin(),
                             routeRequest.getRequestedDestination(),
                             boundingBox,
                             parser.m_waypoints,
                             parser.m_landmarks,
                             parser.m_rawRoute,
                             parser.m_stringTable,
                             parser.m_firstMiniMap
                             );
    }


    // -----------------------------------------------------------------------
    // parsing functions and containers for parsed data.

    /**
     * Used when parsing the header and string table and for getting
     * the route datums into a buffer.
     */
    private WFDataInputStream m_dis1;

    /**
     * Used when reading route datums. This provides us with buffer position.
     */
    private WFByteArrayInputStream m_rbais;

    /**
     * Used when reading route datums. This provides us with the data type
     * abstraction.
     */
    private WFDataInputStream m_rdis;


    private int m_lenIncludingHeader;
    private int m_distRemainingInTruncatedPart;
    private final Vector m_stringTable = new Vector(30);
    private int m_bytesForStringTable;

    private byte[] m_rawRoute;

    private final Vector m_waypoints = new Vector(30);
    private MiniMap m_firstMiniMap;
    
    private Vector m_landmarks = new Vector(30);

    /**
     * Main parser entry point.
     */
    private void parse() throws IOException {
        m_dis1 = new WFDataInputStream(m_in);
        parsePacketHeader();
        m_bytesForStringTable = parseStringTable();
        readRawRoute();
        parseRouteDatums();
    }


    private static final int SIZE_HEADER = 50;
    
    /**
     * <p>Precondition: m_dis1 in positioned on first byte.</p>
     * 
     * <p>Postcondition: m_dis1 at start of string table.
     *                   m_lenIncludingHeader, m_distRemainingInTruncatedPart set.
     * </p> 
     * 
     * @throws IOException if an I/O error occurs or the packet header is not
     *         correct.
     */
    private void parsePacketHeader() throws IOException {
        /*
         * Header layout
         * -------------
         *  isabBoxNavMessageUtil::convertHeaderToBytes()
         *  - - - - - - - - - - - 
         *
         *  offset      data type   content
         *  (decimal)   
         *   0          byte        STX, always 2
         *   1          uint32      length, including header
         *   5          byte        protocol version
         *   6          uint16      type
         *   8          byte        ReqID
         *
         *                          // not calculated, always 0
         *   9          uint32      CRC
         *  13          byte        status code
         *
         *  isabBoxRouteReply::convertToBytes
         *  - - - - - - - - - - -
         *  14          uint32      m_routeID
         *  18          uint32      m_routeCreateTime
         *
         *                          // typically PI 0 PI 0 - unsuable
         *  22          4 x uint32  bbox (not valid anyway)
         *  38          uint32      dist remaining in truncated part
         *
         *              // always 0 - unusable
         *  42          uint32      dist to next WPT after trunc
         *  46          uint32      phoneHomeDist
         *  50
         */

        if(LOG.isTrace()) {
            LOG.trace("Nav2RouteParser.parsePacketHeader()", "");
        }
        
        int stx = m_dis1.readUnsignedByte();
        // we can't reasonably cope with 2 GB routes anyway.
        m_lenIncludingHeader = m_dis1.readInt();
        int protoVer = m_dis1.readUnsignedByte();
        
        // check for correct protocol
        if (! (stx == 2 && protoVer == 11)) {
            error("malformed packet header stx=" + stx
                    + " protoVer=" + protoVer);
        }

        m_dis1.skipBytesForced(  2 // type
                            + 1 // reqID
                            + 4 // CRC
                            + 1 // status code
                            + 4 // m_routeID
                            + 4 // m_routeCreateTime
                            + 4*4 // useless boundingbox
                            );

        // we can't reasonably cope with 2 GB routes anyway.
        m_distRemainingInTruncatedPart = m_dis1.readInt();
        m_dis1.skipBytesForced(4 + 4);
    }

    /**
     * <p>Parse a NavigatorRoute.bin string table.</p>
     * 
     * <h3>Algorithm</h3>
     * <p>Precondition: m_dis1 positioned on first byte of string table.
     * 
     * <ol><li>read uint16 with number of bytes, called len.
     *     <li>read len bytes into buffer (this is usually less than 1k).
     *     <li>parse zero-terminated strings until the buffer is exhausted.
     *         Add the strings into m_StringTable.
     *     <li>if len was odd, read one byte to align the stream at an even
     *         byte.
     *     <li>return the number of bytes read in total.
     * </ol>
     * </p>
     * 
     * <p>If len == 2, the string table is empty and no strings will be added.</p>
     * 
     * @return the number of bytes read.
     * @throws IOException if an I/O error occurs.
     */
    private int parseStringTable() throws IOException {

        /*
         * another approach would be to implement
         * readZeroTerminatedString() in WFDataInputStream but we don't
         * know the number of strings, only the space they occupy.
         * 
         * If we knew the number of strings (but not the size) we would need
         * buffer expansion/shrinking code in WFDataInputStream.
         * 
         * This is easier and more efficient for only a small increase in
         * temporary memory footprint.
         */

        int len = m_dis1.readUnsignedShort();

        if (len == 0) {
            return 2;
        }

        byte[] buf = new byte[len];
        m_dis1.readFully(buf);
        UTF8CStringBufferParser parser = new UTF8CStringBufferParser(buf);
        int nextoffset;
        do {
            m_stringTable.addElement(parser.getNextString());            
            nextoffset = parser.getNextOffset();
        } while (nextoffset < len);


        if (len % 2 != 0) {
            m_dis1.readByte();
            ++len;
        }

        return len + 2; // uint16 for length occupies 2 bytes    
    }

    /**
     * <p>Read route datums into m_rawRoute and set up the streams.</p>
     *
     * <p>Postcondition:
     * <ol><li>all data read
     *     <li>m_rawRoute contains the route datums.
     *     <li>m_rbais set up
     *     <li>m_rdis set up.
     *     <li>m_dis1 is null (to avoid accidental use and make buffers
     *         gc()-able) 
     * </ol></p>
     */
    private void readRawRoute() throws IOException {
        int len = m_lenIncludingHeader - (SIZE_HEADER + m_bytesForStringTable);
        m_rawRoute = new byte[len];
        m_dis1.readFully(m_rawRoute);
        m_dis1 = null;
        if(LOG.isDebug()) {
            LOG.debug("Nav2RouteParser.readRawRoute()",
                      "read raw route. #bytes= " + m_rawRoute.length);
        }
        m_rbais = new WFByteArrayInputStream(m_rawRoute);
        m_rdis = new WFDataInputStream(m_rbais);
    }

    /**
     * Go thru the route datums and extract MiniMaps and WaypointImpls.
     */
    private void parseRouteDatums() throws IOException {
        final String FNAME = "Nav2RouteParser.parseRouteDatums()";
        
        // each nav_route_point encodes the action to take at the NEXT
        // nav_route_point. Except for nav_route_point_delta which are
        // not part of this process.
        int actionInPrevWpt;

        MiniMap prevMiniMap = null;
        int currentFlags = 0;
        int prevFlags = 0; // flags previous segment - used for WPT construction
        int currentSpeed = 0; // FIXME: use some default?
        String currentStreetName = getStringFromIndex(-1);
        WaypointImpl prevWpt = null;


        // read the special SPT-point
        m_rdis.readUnsignedShort(); // always nav_route_point_start cc = eee = 0
        int sptFlags = m_rdis.readUnsignedByte();
        if ((sptFlags & 0x01) == 0) {
            actionInPrevWpt = NavRoutePoint.START;
        } else {
            actionInPrevWpt = NavRoutePoint.START_WITH_U_TURN;
        }
        // speed limit is invalid in this datum and we assume that the street
        // name is the same as in the first real nav_route_point.
        m_rdis.skipBytesForced(NBR_BYTES_PER_DATUM - 3);


        // sanity check
        if (! Action.isOrigo(m_rdis.readUnsignedShort())) {
            error("no origo after start");
        }
        m_rbais.setPosRelative(-2); // back up.


        // main loop. read everything up to the first found nav_route_point_end
        // then we must check for truncation.
        int pos;
        while (true) { // we will break explicitly
            pos = m_rbais.getPos();
            int action = m_rdis.readUnsignedShort();
            
            if(LOG.isTrace()) {
                LOG.trace(FNAME, "route datum #"
                                 + (pos / NBR_BYTES_PER_DATUM));
            }

            if (Action.isOrigo(action)) {
                MiniMap m = new MiniMap(m_rdis,
                                        prevMiniMap,
                                        currentSpeed,
                                        currentFlags,
                                        currentStreetName,
                                        pos + 2 * NBR_BYTES_PER_DATUM);
                if (m_firstMiniMap == null) {
                    m_firstMiniMap = m;
                }
                prevMiniMap = m;

            } else if (Action.isEnd(action)) {
                // flags, speed limit and street name are invalid so we don't
                // need to read them
                if(LOG.isDebug()) {
                    LOG.debug(FNAME, "found EPT.");
                }

                // set prevFlags for potential construction of finally
                prevFlags = currentFlags;
                
                break;

            } else if (Action.isNavRoutePoint(action)) {
                // nav_route_point that is not the EPT. Can be a turn or
                // a track point.
                
                // update flags, speed and street name
                // we do this even at non-turns to be able to cache
                // in the minimaps

                prevFlags = currentFlags; 
                currentFlags = m_rdis.readUnsignedByte();
                currentSpeed = m_rdis.readUnsignedByte();
                short x = m_rdis.readShort();
                short y = m_rdis.readShort();
                m_rdis.skipBytesForced(2); // skip unused meters field
                currentStreetName =
                    getStringFromIndex(m_rdis.readUnsignedShort());

                if (! Action.isWpt(action)) {
                    // we have read the whole datum and can continue with next.
                    continue;
                }

                /*
                 *  we have a Waypoint. Read the TDL-datum.
                 *  
                 *  There is no TDL after EPT but this is not an EPT.
                 */
                if (! Action.isTDL(m_rdis.readUnsignedShort())) {
                    error("No TDL after nav_route_point which is not end");
                }
                m_rdis.skipBytesForced(2); // uint16 reserved
                // time, dist are uint32 but distance
                // Integer.MAX_VALUE is 50 times around the earth
                // so we don't expect to use the whole range
                // anyway and can stick to int instead of long
                int tdl_time = m_rdis.readInt();
                int tdl_dist = m_rdis.readInt();

                /*
                 * we have a waypoint.
                 * 
                 * action (turn, exit count, (crossing) from actionInPrevWpt
                 * 
                 * right-hand-side traffic from previous flag field. This seems
                 * awkward and I wonder what the server actually does if
                 * left/right-traffic changes in a waypoint.
                 */

                // we no longer require a default START at the start of the
                // wpt chain. See Nav2Route() constructor.

                Position position = new Position(prevMiniMap.yToMC2(y),
                        prevMiniMap.xToMC2(x));

                WaypointImpl wpt = createAndStoreWpt(pos,
                                                     prevFlags,
                                                     position,
                                                     actionInPrevWpt,
                                                     tdl_dist,
                                                     tdl_time,
                                                     currentStreetName,
                                                     currentSpeed,
                                                     prevWpt);
                prevWpt = wpt;
                actionInPrevWpt = action;


            } else if (action == Action.LANDMARK) {
                parseAndStoreLandmark();

            } else {
                // unsupported data
                m_rdis.skipBytesForced(NBR_BYTES_PER_DATUM -2 );
            }
        }


        // now we have read the action for the EPT. pos points to the action.
        // skip flags and speed_limit
        m_rdis.skipBytesForced(2);
        // the position is valid but is often (end of) a zero length segment.
        short x = m_rdis.readShort();
        short y = m_rdis.readShort();
        m_rdis.skipBytesForced(2*2); // skip unused meters field and street name
        int actionAfterEPT = m_rdis.readUnsignedShort();

        if (Action.isEnd(actionAfterEPT)) {
            if(LOG.isDebug()) {
                LOG.debug(FNAME, "route was NOT truncated.");
            }

            // construct and store the "finally" Waypoint at first EPT.
            // the flags are always 255 
            Position position = new Position(prevMiniMap.yToMC2(y),
                    prevMiniMap.xToMC2(x));

            createAndStoreWpt(pos,
                              prevFlags,
                              position,
                              actionInPrevWpt, // finally
                              0,               // tdl_dist
                              0,               // tdl_time
                              currentStreetName,
                              currentSpeed,
                              prevWpt);

        } else {
            if(LOG.isDebug()) {
                LOG.debug(FNAME, "route was truncated at route datum #"
                                 + (pos / NBR_BYTES_PER_DATUM));
            }

            // recode to our special value so that the iterator doesn't
            // think we have a wpt here.
            m_rawRoute[pos] = (byte)(0xff & (Action.SPECIAL_TRUNCATION >> 8));
            m_rawRoute[pos + 1] = (byte) (0xff & Action.SPECIAL_TRUNCATION); 

            /*
             *  TODO: go over the rest of the data and parse MiniMaps
             *  (which will not be linked in, just used locally for coordinate
             *   transformation)
             *   and Waypoints (so that the itinerary is complete) 
             */
        }
    }

    private void parseAndStoreLandmark() throws IOException {
        /*
         * migrated from jWMMG so old code does not completely follow the code
         * standard.
         */ 

        int lmFlags = m_rdis.readUnsignedShort();
        String lmStreetname =
            getStringFromIndex(m_rdis.readUnsignedShort());
        int id_and_startstop = m_rdis.readUnsignedShort();
        // distance is int32 but we don't expect negative values
        int distance = m_rdis.readInt();
        // have now read entire Landmark
        
        int id = Landmark.id_and_startstop_to_id(id_and_startstop);
        // note that this Waypoint does not yet exist!
        int approachWPTIdx = m_waypoints.size();

        Landmark lm;

        if (Landmark.isPoint(id_and_startstop)
            || Landmark.isStart(id_and_startstop)) {
            // create
            lm = new Landmark(approachWPTIdx,
                              distance,
                              lmFlags,
                              lmStreetname,
                              id);
            if (m_landmarks.size() != id) {
                if(LOG.isError()) {
                    LOG.error("Nav2RouteParser.parseAndStoreLandmark()",
                              "inconsistent landmark id (1) " + id);
                }
                return; // ignore this LM
            }

            m_landmarks.addElement(lm);

        } else {
            try {
                lm = (Landmark) m_landmarks.elementAt(id);
            } catch (ArrayIndexOutOfBoundsException e) {
                if(LOG.isError()) {
                    LOG.error("Nav2RouteParser.parseAndStoreLandmark()",
                              "inconsistent landmark id (2) " + id);
                }

                return; // ignore this LM
            }
        }

        if (Landmark.isPoint(id_and_startstop)
            || Landmark.isEnd(id_and_startstop)) {
            lm.setApproachWPTIdxEnd(approachWPTIdx);
            lm.setApproachDistanceEnd(distance);
        }
        
    }


    /**
     * internal helper for construction of Waypoints
     */
    private WaypointImpl createAndStoreWpt(int positionOfAction,
                                           int flags,
                                           Position position,
                                           int actionInPrevWpt,
                                           int tdl_dist,
                                           int tdl_time,
                                           String currentStreetName,
                                           int currentSpeed,
                                           WaypointImpl prevWpt) {                                       

        int pos = positionOfAction;
        // calculate index used for Wpt lookup from the iterator
        int index = m_waypoints.size();

        WaypointImpl wpt =
            new WaypointImpl(isDriveOnRightSide(flags),
                             position,
                             Turn.getFromNavRoutePoint(actionInPrevWpt),
                             Action.calculateExitCount(actionInPrevWpt),
                             tdl_dist,
                             tdl_time,
                             currentStreetName,
                             currentSpeed,
                             prevWpt,
                             index);

        if (prevWpt != null) {
            prevWpt.setNextImpl(wpt);
        }
        m_waypoints.addElement(wpt);

        // store the index so that the iterator can read it
        final int METERS_OFFSET =   2   // uint16 action
                                  + 1   // uint8 flags
                                  + 1   // uint8 speed_limit
                                  + 4; // 2 x uint16 x,y
        // see DataOutput.writeShort()
        m_rawRoute[pos + METERS_OFFSET] = (byte)(0xff & (index >> 8));
        m_rawRoute[pos + METERS_OFFSET + 1] = (byte) (0xff & index); 

        return wpt;
    }

    /**
     * <p>Internal helper method to handle route datums which has
     * invalid/unknown street names
     * (<code>nav_route_point.street_name_index == -1</code>).</p>
     * 
     * <p>If streetNameIndex is valid in iStringTable return that string,
     * otherwise return "".</p>
     *
     * <p>Use this to ensure that String references in WPTs and landmarks
     * are never null even if we don't really know the name. For the
     * ui the distinction "name not known" and "has name ''" is
     * unimportant. Especially since real streets are never named ""
     * but we often don't know what name it has.</p>
     * 
     * @param streetNameIndex - read from raw route as
     *        nav_route_point.street_name_index.
     */
    private String getStringFromIndex(int streetNameIndex) {
        final String EMPTY = "";

        if (streetNameIndex >= 0 && streetNameIndex < m_stringTable.size()) {
            return (String) m_stringTable.elementAt(streetNameIndex);
        }

        return EMPTY;
    }
    
    /**
     * Throws an IOException with a message.
     */
    private void error(String msg) throws IOException {
        StringBuffer sb = new StringBuffer(100);
        if(LOG.isError()) {
            LOG.error("Nav2RouteParser.error()", msg);
        }

        sb.append("Nav2RouteParser.error() ");
        sb.append(msg);
        // could add info about position of data etc.
        throw new IOException(sb.toString());
    }
}
