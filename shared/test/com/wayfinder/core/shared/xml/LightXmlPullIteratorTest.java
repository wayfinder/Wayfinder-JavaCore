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
/**
 * 
 */
package com.wayfinder.core.shared.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class LightXmlPullIteratorTest extends TestCase {

    static final String VALUE1 = "AAwAAAAyAAAARAAAAWwAAAGqAAAB1AAAAeIAAAHwAAACEgAAAiAAAAMqAAADQAAAA1YAAAAFAAACcAAAA9kAAQAAAH4AAAAJAAAB8QAAAuIACwAAAV4AAAFQAAABQgAAATQAAAEmAAABGAAAAQoAAAD8AAAA7gAAAOAAAADSAAAAEgAAAAgAAAKPAAAAAAAUAAACSgAAA74AAAAAABYAAAFFAAAD2QAAAAAAHgAAACMAAAPZAAAAAAAkAAACLgAAA9kAAAAAADYAAAASAAACoAAAAAAAOAAAAl8AAALiAAAAAAA6AAACDgAAAuIAAAAAADsAAAH8AAAC4gAAAAAAPQAAAYwAAALiAAAAAAA+AAABTgAAAuIAAAAAAEAAAAE7AAAC4gAAAAAAQwAAAOYAAALiAAAAAABFAAAAxQAAAuIAAAAAAEYAAAC6AAAC4gAAAAAASwAAAIAAAALiAAAAAABOAAAAQQAAAuIAAAAAAFUAAAHYAAADmQAMAAACyAAAAroAAAKsAAACngAAApAAAAKCAAACdAAAAmYAAAJYAAACSgAAAjwAAAIuAAAAVgAAAFoAAALEAAAAAABYAAACPwAAA60AAAAAAGIAAAB2AAAC0gAAAAAAZwAAAaIAAANcAAAAAABrAAABygAAA4UAAAAAAG8AAAD2AAAD2QAFAAADHAAAAw4AAAMAAAAA/AAAAvIAAAB2AAABFAAAAwoAAAAAAJgAAAACAAACtwAAAAAAngAAAoUAAAOZAAAAAACfAAACOAAAA5kAAAAAAKgAAAHmAAADmQAAAAAArwAAAb8AAAOZAAAAAAC9AAABMAAAA5kAAAAAAL4AAAEmAAADmQAAAAAAwAAAAR0AAAOZAAAAAADDAAAA3gAAA5kAAAAAAMQAAADWAAADmQAAAAAAygAAAK4AAAOZAAAAAADQAAAAbAAAA5kAAAAAANgAAABRAAADmQAAAAAA7AAAAGEAAANFAAAAAADtAAABeAAAAywAAAAAAPUAAAG0AAADcwAAAAAA9gAAAQkAAAL4AAAAAAD4AAAApQAAA9kAAAAAAPkAAACbAAAD2QAAAAABCwAAAZgAAAMsAAIAAALWAAAC5AAAARYAAAFsAAADGQACAAABxgAAAbgAAAKaAAAAMwAAA9kABgAAAMQAAACoAAAAmgAAALYAAAG4AAAAjA==";
    static final String VALUE2 = "AAAABQAAADIAAAAJAAAARAAAABIAAAB+AAAAFAAAAIwAAAAWAAAAmgAAAB4AAACoAAAAJAAAALYAAAA2AAAAxAAAADgAAADSAAAAOgAAAOAAAAA7AAAA7gAAAD0AAAD8AAAAPgAAAQoAAABAAAABGAAAAEMAAAEmAAAARQAAATQAAABGAAABQgAAAEsAAAFQAAAATgAAAV4AAABVAAABbAAAAFYAAAGqAAAAWAAAAbgAAABiAAABxgAAAGcAAAHUAAAAawAAAeIAAABvAAAB8AAAAHYAAAISAAAAmAAAAiAAAACeAAACLgAAAJ8AAAI8AAAAqAAAAkoAAACvAAACWAAAAL0AAAJmAAAAvgAAAnQAAADAAAACggAAAMMAAAKQAAAAxAAAAp4AAADKAAACrAAAANAAAAK6AAAA2AAAAsgAAADsAAAC1gAAAO0AAALkAAAA9QAAAvIAAAD2AAADAAAAAPgAAAMOAAAA+QAAAxwAAAELAAADKgAAARYAAANAAAACmgAAA1Y=";
    static final String VALUE3 = "AANBVE0AAAdBaXJwb3J0AAAOQW11c2VtZW50IHBhcmsAAA1BcnQgZ2FsbGVyaWVzAAALQXR0cmFjdGlvbnMAAA1CZWF1dHkgc2Fsb25zAAAGQmlzdHJvAAAEQ2FmZQAACENhciBwYXJrAAAHQ2hpbmVzZQAAB0NpbmVtYXMAABhDbG90aGluZyBhbmQgYWNjZXNzb3JpZXMAAAdEZW50aXN0AAAGRG9jdG9yAAAJRmFzdCBmb29kAAAIRmxvcmlzdHMAAA5Gb29kIGFuZCBkcmluawAABUdyZWVrAAAFR3JpbGwAAA1Hcm9jZXJ5IHN0b3JlAAAQSGVhbHRoICYgTWVkaWNhbAAACEhvc3BpdGFsAAAGSG90ZWxzAAAGSW5kaWFuAAAHSXRhbGlhbgAACEphcGFuZXNlAAAHSmV3ZWxyeQAABk11c2V1bQAAG05ld3NhZ2VudHMgYW5kIHRvYmFjY29uaXN0cwAACU5pZ2h0bGlmZQAAEU9wZW4gcGFya2luZyBhcmVhAAAJT3B0aWNpYW5zAAAHUGFya2luZwAAD1BldHJvbCBzdGF0aW9ucwAACFBoYXJtYWN5AAAIUGl6emVyaWEAAAtQb3N0IG9mZmljZQAAC1Jlc3RhdXJhbnRzAAAIU2VhIGZvb2QAAAhTaG9wcGluZwAAD1Nob3BwaW5nIGNlbnRyZQAAHVNwb3J0cyBlcXVpcG1lbnQgYW5kIGNsb3RoaW5nAAAHU3RhZGl1bQAABFRoYWkAAAhUaGVhdHJlcwAAElRvdXJpc3QgYXR0cmFjdGlvbgAADlRveXMgYW5kIGdhbWVzAAASVHJhdmVsICYgVHJhbnNwb3J0AAAHVHVya2lzaAAADmNhdF92Zl9haXJwb3J0AAAUY2F0X3ZmX2FtdXNlbWVudHBhcmsAAApjYXRfdmZfYXRtAAALY2F0X3ZmX2NhZmUAAA1jYXRfdmZfY2luZW1hAAATY2F0X3ZmX2dyb2NlcnlzdG9yZQAAD2NhdF92Zl9ob3NwaXRhbAAADGNhdF92Zl9ob3RlbAAAEGNhdF92Zl9uaWdodGxpZmUAABZjYXRfdmZfb3BlbnBhcmtpbmdhcmVhAAAUY2F0X3ZmX3BhcmtpbmdnYXJhZ2UAABRjYXRfdmZfcGV0cm9sc3RhdGlvbgAAD2NhdF92Zl9waGFybWFjeQAAEWNhdF92Zl9wb3N0b2ZmaWNlAAARY2F0X3ZmX3Jlc3RhdXJhbnQAAA5jYXRfdmZfdGhlYXRyZQAAGGNhdF92Zl90b3VyaXN0YXR0cmFjdGlvbgAAE2NhdF92Zl92b2RhZm9uZXNob3AA";
    static final String XML = 
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?>" +
        "<!DOCTYPE isab-mc2><isab-mc2>" + 
        "<local_category_tree_reply crc=\"19643776819364960033658315638\" transaction_id=\"ID0\">" +
            "<category_table length=\"1193\">" + VALUE1 + "</category_table>" +
            "<lookup_table length=\"526\">" + VALUE2 + "</lookup_table>" +
            "<string_table length=\"1344\">" + VALUE3 + "</string_table>"+
        "</local_category_tree_reply>"+
        "</isab-mc2>";
    
  

    static final String tisab_mc2 = "isab-mc2";
    static final String tcategory_table = "category_table";
    static final String[] elementName = {tisab_mc2, tcategory_table};

    
    static final String alength = "length";
    static final String acrc = "crc";
    static final String atransaction_id = "transaction_id";
    static final String[] attributeName = {alength,acrc,atransaction_id};
    
        
    XmlIterator xpi;
    /**
     * @param name
     */
    public LightXmlPullIteratorTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        xpi = new LightXmlPullIterator(elementName,attributeName);
        xpi.setInput(new ByteArrayInputStream(XML.getBytes()), null);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link com.wayfinder.core.shared.xml.LightXmlPullIterator#valueAsReader()}.
     * @throws IOException 
     */
    public void testValueAsReader() throws IOException {
        assertTrue(xpi.children());
        assertTrue(xpi.children());
        assertEquals("19643776819364960033658315638",xpi.attribute(acrc));
        assertTrue(xpi.children());
        assertSame(tcategory_table, xpi.name());
        
        Reader reader1 = xpi.valueAsReader();
        assertNotNull("get value as a reader", reader1);
        
        char[] buf = new char[10];
        int size = reader1.read(buf);
        assertEquals("first 10 char from reader are correct", VALUE1.substring(0, 10),new String(buf));
        reader1.skip(2);
        assertEquals("get attribute in middle of reading","1193", xpi.attribute(alength));
        assertEquals("test a single char read", VALUE1.charAt(12),reader1.read());
        
        int n = 0;
        int pos = 13;
//        while ((n = reader1.read(buf)) >= 0) {
//            assertEquals(VALUE1.substring(pos, pos + n) , new String(buf,0,n));
//            pos +=n;
//        }
//        assertEquals(-1, n);
//        assertEquals(VALUE1.length() , pos);
//        reader1.close();
        
        assertTrue(xpi.advance());
        assertEquals(VALUE2,xpi.value());
        assertTrue(xpi.advance());
        
        Reader reader3 = xpi.valueAsReader();
        assertNotNull("get value as a reader", reader3);
        
        n = 0;
        pos = 0;
        while ((n = reader3.read(buf)) >= 0) {
            assertEquals(VALUE3.substring(pos, pos + n) , new String(buf,0,n));
            pos +=n;
        }
        assertEquals(-1, n);
        assertEquals(VALUE3.length() , pos);
        reader3.close();
        assertEquals("get attribute in after reading","1344", xpi.attribute(alength));
        assertFalse(xpi.advance());
        assertFalse(xpi.advance());
        assertSame(tisab_mc2, xpi.name());
        assertFalse(xpi.advance());
    }
    
    

}
