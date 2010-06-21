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
package com.wayfinder.core.shared.xml;

import java.io.*;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.CharArray;


/**
 * 
 *  
 */
public class XmlReader extends Reader {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(XmlReader.class);
    
    /** status constant */
    final static int START_TAG = 1;

    final static int END_TAG = 2;

    final static int PI_XML = 3;

    final static int COMMENT = 4;

    final static int CDATA = 5;

    final static int DOCTYPE = 6;

    final static int TEXT = 7;

    final static int FINISH_TAG = 10;

    final static int FINISH_EMPTY_TAG = 11;
    
    final static int FINISH_PI_XML = 12;

    final static int ATTRIBUTE = 13;

    final static int ATTRIBUTE_VALUE = 14;

    private Reader reader;

    String encoding; // is this actually read by anyone???

    /*
     * Originally this just copied kXML behaviour of selecting
     * buffersize to 8192 if free memory was at least 1048576 bytes
     * and 128 bytes otherwise. But on SonyEricsson devices,
     * especially JP 5 or later, Runtime.freeMemory() is unreliable
     * since total heap memory allocation is dynamic (both upwards and
     * downwards).
     *
     * On Sony Ericsson devices, the buffer has to be large enough to
     * avoid bug #1627 (see XmlReader.read() for more info).
     *
     * On Nokia and Motorola devices memory is scarse, but 128 bytes
     * is likely too small to be efficient. However, that depends on
     * the amount of buffering in the OS and its tcp stack.
     */
    protected static final int CBUFSIZE = 8192; // 16KB of memory

    private char[] srcBuf = new char[CBUFSIZE];

    private int srcPos;

    private int srcCount;

    /**
     * last char readed
     */
    char c;

    /**
     * used to replace \r\n with \n true if last char was \r and was replaced
     * with \n;
     */
    private boolean wasCR;

    private int line;//current line

    private int column;///current column

    CharArray charArray = new CharArray(128);
    
    
    public XmlReader() {
    }
    
    public XmlReader(Reader reader) throws IOException {
        setInput(reader);
    }
    
    public XmlReader(InputStream inStream, String enc) throws IOException {
        setInput(inStream, enc);
    }
    
    

    public String toString() {
        StringBuffer rez = new StringBuffer(100);
        rez.append("XmlReader:");
        rez.append("\n\twrapped reader: ");rez.append(reader);
        rez.append("\n\tcurrent char: ");rez.append(c);
        rez.append("\n\tin postion: ");rez.append(line);rez.append(":");rez.append(column);
        rez.append("\n\tin buffer: ");rez.append(srcBuf,0,srcPos);
        //rez.append("\n\tentire in buffer: ");rez.append(srcBuf);
        rez.append("\n\tout buffer: ");rez.append(charArray.toString());
        //rez.append("\n\tentire out buffer: ");rez.append(charArray.iBuffer);
        
        return rez.toString();
    }

    public void setInput(Reader reader) throws IOException {
        this.reader = reader;
        encoding = null;

        if (reader == null)
            return;
        srcPos = 0;
        srcCount = 0;
        wasCR = false;
        charArray.empty();

        line = 0;
        column = 0;
        
    }

    public void setInput(InputStream is, String _enc) throws IOException {
        srcPos = 0;
        srcCount = 0;
        String enc = _enc;
        if (is == null)
            throw new IllegalArgumentException("InputStream cannot be null");
        try {
            if (enc == null) {
                /*
                  see Extensible Markup Language (XML) 1.0 (Third Edition)
                  appendix F.1
                */
                // read four bytes
                int chk = 0;
                while (srcCount < 4) {
                    int i = is.read();
                    if (i == -1)
                        break;
                    chk = (chk << 8) | i;
                    srcBuf[srcCount++] = (char) i;
                }
                if (srcCount == 4) {
                    switch (chk) {
                    case 0x00000FEFF:
                        enc = "UTF-32BE";
                        srcCount = 0;
                        break;
                    case 0x0FFFE0000:
                        enc = "UTF-32LE";
                        srcCount = 0;
                        break;
                    case 0x03c:
                        enc = "UTF-32BE";
                        srcBuf[0] = '<';
                        srcCount = 1;
                        break;
                    case 0x03c000000:
                        enc = "UTF-32LE";
                        srcBuf[0] = '<';
                        srcCount = 1;
                        break;
                    case 0x0003c003f:
                        enc = "UTF-16BE";
                        srcBuf[0] = '<';
                        srcBuf[1] = '?';
                        srcCount = 2;
                        break;
                    case 0x03c003f00:
                        enc = "UTF-16LE";
                        srcBuf[0] = '<';
                        srcBuf[1] = '?';
                        srcCount = 2;
                        break;
                    case 0x03c3f786d: // <?xm
                        while (true) {
                            int i = is.read();
                            if (i == -1)
                                break;
                            srcBuf[srcCount++] = (char) i;
                            if (i == '>') { // read to end of PI
                                String s = new String(srcBuf, 0, srcCount);
                                int i0 = s.indexOf("encoding");
                                if (i0 != -1) {
                                    while (s.charAt(i0) != '"'
                                            && s.charAt(i0) != '\'')
                                        i0++;
                                    char deli = s.charAt(i0++);
                                    int i1 = s.indexOf(deli, i0);
                                    enc = s.substring(i0, i1);
                                }
                                break;
                            }
                        }
                    default:
                        if ((chk & 0x0ffff0000) == 0x0FEFF0000) {
                            enc = "UTF-16BE";
                            srcBuf[0] = (char) ((srcBuf[2] << 8) | srcBuf[3]);
                            srcCount = 1;
                        } else if ((chk & 0x0ffff0000) == 0x0fffe0000) {
                            enc = "UTF-16LE";
                            srcBuf[0] = (char) ((srcBuf[3] << 8) | srcBuf[2]);
                            srcCount = 1;
                        } else if ((chk & 0x0ffffff00) == 0x0EFBBBF00) {
                            enc = "UTF-8";
                            srcBuf[0] = srcBuf[3];
                            srcCount = 1;
                        }
                    } // switch
                    if(LOG.isInfo()) {
                        LOG.info("XmlReader.setInput()","determined encoding from input: " + enc);
                    }
                }
            } // enc == null
            if (enc == null) {
                if(LOG.isInfo()) {
                    LOG.info("XmlReader.setInput()","could not determine encoding, will use utf-8");
                }
                enc = "UTF-8";
            }
            
            int sc = srcCount;

            enc = XmlReader.deviceTranslateEncoding(enc);
            setInput(new InputStreamReader(is, enc));
            encoding = _enc;
            srcCount = sc;
        } catch (IOException e) {
            throw new IOException("Invalid stream or encoding " + e.toString());
        }
    }


    /**
     * read the next char
     * 
     * translating both the two-character sequence \r\n and any \r that is not
     * followed by \n to a single \n character
     * 
     * @throws IOException
     */
    public final void next() throws IOException {
        //TODO check reader.ready() to get data every time is available instead
        // of force reading
        if (srcPos >= srcCount) {
//            //until read() call can block the current thread 
//            //check interruption status
//            int iTimeTowait = 1;
//            while (!reader.ready()) {
//                Thread.sleep(timeToWait++);
//            }
//            timeToWait--;  
            
            if(LOG.isTrace()) {
                LOG.trace("XmlReader.next()","will read from " + reader
                      + " srcPos: " + srcPos
                      + " srcCount: " + srcCount
                      + " srcBuf.length: " + srcBuf.length);
            }

            srcCount = reader.read(srcBuf, 0, srcBuf.length);
            
            if(LOG.isTrace()) {
                LOG.trace("XmlReader.next()","read complete, srcCount: " + srcCount );
            }
            
            if (srcCount > srcBuf.length) {
                // Oh phooey, I burned the darn' muffins!

                /* 
                   Eventum #1627, InputStreamReader.read() buggy on
                   Sony Ericsson JP 7.3 devices. Workaround: caller
                   should use a large enough buffer (chars.length AND
                   length). SEMC recommendation is 8-16 kB (4-8
                   kchars). The problem does not occur on JP 7.2 or
                   earlier or JP 7.4 or later.
                */

                throw new XmlReaderException("Input error: read more than requested bug #1627 " 
                                   + srcCount + " instead of " + srcBuf.length, this.toString());

            }

            if (srcCount <= 0) {
                throw new XmlReaderException("Input error: end not expected", this.toString());
            }
            srcPos = 1;
            c = srcBuf[0];
        } else {
            c = srcBuf[srcPos++];
        }

        //translating

        if (c == '\r') {
            wasCR = true;
            c = '\n';
        } else {
            if (wasCR) {
                wasCR = false;
                if (c == '\n')
                    next();//skip one char \r\n became \n
            }
        }
        
        //TODO: check if position calculation is correct
        if (c == '\n') {
            line++;
            column = 0;
        } else
            column++;
    } //next


    /**
     * skip until find a character that is not whitespace
     * @throws IOException
     */
    public final void skipWhitespace() throws IOException {
        while (c <= ' ') {
            next();
        }
    }
    /**
     * push in CharArray until find a character that is not whitespace
     * @throws IOException
     */
    public final void pushWhitespace() throws IOException {
        charArray.empty();
        while (c <= ' ') {
            charArray.append(c);
            next();
        }
    }    
    /**
     * skip chars starting from current char until char delimiter is read
     * @param delimiter
     * @throws IOException
     */
    
    public final void skipText(char delimiter) throws IOException {
        while (c != delimiter) {
            next();
        }
    }

    /**
     * skip chars starting from next char until string delimiter is read
     * @param delimiter a string 
     * @throws IOException
     */
    
    public final void skipText(String delimiter) throws IOException {
        int n = delimiter.length();
        int i = 0;
        while (i != n) {
            next();
            if (c == delimiter.charAt(i)) i++;
            else i = 0;
        }
    }
    
    /**
     * read a specific char from the input
     * 
     * @param must
     *            the char to read
     * @throws IOException
     *             if other char was readed or an input error occured while
     *             reading
     */
    public final void read(char must) throws IOException {
        next();
        if (c != must) {
            throw new XmlReaderException("Wrong char " + c + " instead of " + must, 
                    this.toString());
        }
    }

    /**
     * read a specific String from the input starting from next position 
     * which must be equal with the first char from string
     * and ending at the last char from string
     * 
     * @param must
     *            the String to read
     * @throws IOException
     *             if the String doesn't match with the char array read. or an
     *             input error occurred while reading
     */
    public final void read(String must) throws IOException {
        int n = must.length();
        for (int i = 0; i != n; i++) {
            next();
            if (c != must.charAt(i)) {
                throw new XmlReaderException("Wrong char " + c + " instead of " + must.charAt(i), 
                        this.toString());
            }
        }
    }

    public int parseHex() throws IOException {
        int result = 0;
        while (c != ';') {
            int digit = Character.digit(c, 16);
            if (digit < 0)
                throw new XmlReaderException("Invalid char " + c + " instead of hexa digit", 
                        this.toString());                
            if (result == 0) {
                result = digit;
            } else {
                result <<= 4;
                result += digit;
            }
            next();
        }
        return result;
    }

    public int parseDec() throws IOException {
        int result = 0;
        while (c != ';') {
            int digit = Character.digit(c, 10);
            if (digit < 0) {
                throw new XmlReaderException("Invalid char " + c + " instead of decimal digit", 
                        this.toString());
            }
            if (result == 0) {
                result = digit;
            } else {
                digit += result << 1; //r*2 + digit
                result <<= 3; //r*8
                result += digit; //r*8 + r*2 + digit
            }
            next();
        }
        return result;
    }

    /**
     * [&...;]
     * 
     * &lt; &gt; &quot; &amp; &apos; CharRef ::= '&#' [0-9]+ ';' | '&#x'
     * [0-9a-fA-F]+ ';'
     */

    private char getEntity() throws IOException {
        next(); // skip over &
        boolean readSemicolon = true;
        if (c == '#') {
            next();
            if (c == 'x') {
                next();
                return (char) parseHex();
            } else {
                return (char) parseDec();
            }
        } else {
            char result = 0;
            switch (c) {
            case 'l':
                next();
                if (c == 't') //lt
                    result = '<';
                else if (c == 'a') {//&laquo;
                    read("quo");
                    result = '\u0171';
                }
                break;
            case 'g':
                read('t');//gt
                result = '>';
                break;
            case 'q':
                read('u');read('o');read('t');//quot
                result = '"';
                break;
            case 'a':  
                next();
                if (c == 'm') {
                    read('p');//amp
                    result = '&';
                } else if (c == 'p') {
                    read('o');read('s');//apos
                    result = '\'';
                } 
                break;
            case 'r':
                next();
                if (c == 'e') {
                    read('a');read('l');
                    result = '\u8476';
                } else if (c == 'a') {
                    read("quo");
                    result = '\u0187';
                }
                break;
            case 'd':
                read('e');read('g');
                result = '\u0176';
                break;
            case 'n':
                next();
                if(c == 'b'){
                    read('s');read('p');
                    result = ' ';
                }
                else if(c == 'd'){
                    read('a');read('s');read('h');
                    result = '-';
                }
                break;
            case 'c':
                read('o');read('p');read('y');
                result = '\u0169';
                break;
            case 'A':
                next();
                if(c == 't'){
                    read("ilde");
                    result = '\u0195';
                }
                else
                    result = 'A';
                break;
            default:
                readSemicolon = false;
                result = '&';
                if(LOG.isError()) {
                    LOG.error("XmlReader.getEntity()", " unknown xml enitity return '&'");
                }
            }
            
            if(readSemicolon)
                read(';');  
            return result;
        }
    }

    /**
     * read until a delimiter character (? >,/,= or space) is find keep the
     * read char in txtBuffer. used to read start tag names & attribute names
     * 
     * @throws IOException
     */
    public void pushName() throws IOException {
        charArray.empty();
        //txtPos = 0;
        //if (srcPos ==0) read(); // there is no c readed; not possible;
        do {
            charArray.append(c);
            next();
        } while (c > ' ' && c != '>' && c != '/' && c != '=' && c != '?');
        //TODO if String.indexOf is faster ">/=?".indexOf(c) == -1)
    }

    /**
     * read a attribute value  
     * @param skip if set to true the chars representing the value are skipped  
     * 
     * @throws IOException
     */
    public void pushAttrValue(final boolean skip) throws IOException { 
        //<element attribute[ = "value"] ...>
        if (getStatus() != ATTRIBUTE_VALUE) {
            throw new XmlReaderException("Attribute value missing for " + charArray.toString(),
                    this.toString());
        }
        char quote = c;
        if (quote != '\'' && quote != '"')
            throw new XmlReaderException("Attribute value delimiter missing",
                    this.toString());
  
        next();//skip over quote
        if (skip) {
            skipText(quote);
        } else {
            pushText(quote);            
        }
        next();//skip over quote
    }

    /**
     * read until start tag character &lt; is find skip whitespaces keep the
     * readed char in txtBuffer. used to read PCDATA content & attribute value
     * 
     * @throws IOException
     */
    public void pushText(char delimiter) throws IOException {
        //isWhitespace = true;
        while (c != delimiter) { //'<'
            //isWhitespace = isWhitespace && (c <= ' ');
            if (c == '&') {
                charArray.append(getEntity());//read(';');
            } else {
                charArray.append(c);
            }
            next();
        }
    } // pushText


    /**
     * read CDATA and Commment <![CDATA#[ any text ]]># <!-#- comment -->#
     * 
     * @param delimiter
     *            can be - or ]
     * @throws IOException
     */
    public void pushCDATA(char delimiter) throws IOException {
        charArray.empty();
        next();//skip [ or -
        int sb = 0;//number of delimiter one after the other
        while (sb != 2 || c != '>') {
            if (c == delimiter) {
                if (sb == 2) //case with ]]] or ---
                    charArray.append(delimiter);//push delimiter back ] or -
                else
                    sb++;
            } else {
                if (sb != 0) { //case with ]c or -c
                    charArray.append(delimiter); //write back delimiter ] or -
                    if (sb == 2) { //case with ]]c or --c
                        charArray.append(delimiter); //write back another delimter ]] or --
                    }
                    sb = 0;
                }
                charArray.append(c);
            }
            next();
        }
    }

    /**
     * skip an xml element
     * <pre> 
     * &lt;element&gt;
     *      &lt;child1/&gt;
     *      &lt;child2/&gt;         
     * &lt;element/&gt; 
     * or
     * &lt;element&gt;content&lt;element/&gt;
     * or 
     * &lt;element/&gt;
     * </pre>
     *   
     * @throws IOException
     */
    public void skipElement() throws IOException {
        while (c != '>') { // [<[element[{/}>].....  
            if (c == '/') {//maybe is empty element
                next(); 
                if (c == '>') return; //empty element   
            } 
            next();//skip element start tag
        }
        skipContent(); // <element[>.........</]element> 
        skipText('>'); // <[/element>   //skip end tag
    }
    
    /**
     * skip the content of an xml element
     * <p> &lt;element[&gt;content&lt;/]element&gt;</p>
     * <p>WARNING: the xml element must not be empty</p> 
     * @throws IOException
     */
    //  <element[>.........</]element>
    public void skipContent() throws IOException {
        if(LOG.isTrace()) {
            LOG.trace("XmlReader.skipContent()", "" );
        }
        do {
            //<element[>......<] text or whitespace before first child 
            //or between children
            skipText('<');
            switch (getType()) {
            //<![CDATA[.....]]>  CDATA Section
            case CDATA: skipText("]]>"); break;
            //<[child>content<child/>] child element
            case START_TAG: skipElement();break;
            //case COMMENT: is skipped by getType()
            //case PI_XML: is skipped by getType()
            default: return;//case FINISH_TAG
            }
        } while (true); 
    }

    /**
     * similar with getType() but is only testing if the type is text 
     * and advance over the whitespaces
     * @return true if the text is following
     * @throws IOException
     */
    public boolean isTextType() throws IOException {
        if (c == '>') {// at the end of one tag only now can be expected TEXT
            next();//skip over >
            pushWhitespace();//if is TEXT the whitespace are memorize in charArray
            //the TEXT comparation is moved down to allow double 
            //TEXT result if the text was not readed
        } 
        return (c != '<');
    }

    
    /**
     * call this only from or exactly after '>'
     * 
     * if text is the next occurrence this cannot be empty and the getType will
     * not advance until the text is read but if other type is returned this
     * must be processed before calling next type
     * 
     * @return one of the type constant {@link #END_TAG}, {@link #TEXT} ...
     * @throws IOException
     */
    public int getType() throws IOException {

        if (c == '>') {// at the end of one tag only now can be expected TEXT
            next();//skip over >
            pushWhitespace();//if is TEXT the whitespace are memorize in charArray
            //the TEXT comparing is moved down to allow double 
            //TEXT result if the text was not read
        }
        
        if (c == '<') {
            next();
            switch (c) {
            case '/':
                return END_TAG;
            case '?':
                //return PI_XML;
                //just skip the processing instructions instead of return PI
                if(LOG.isDebug()) {
                    LOG.debug("XmlReader.getType()", "skip processing instruction");
                }
                skipText("?>");
                //FIXME: any whitespaces before comment will be lost
                return getType();
            case '!':
                next();
                switch (c) {
                case '-':
                    read('-');
                    //return COMMENT;
                    //just skip the comments instead of return COMMENT
                    if(LOG.isDebug()) {
                        LOG.debug("XmlReader.getType()", "skip xml comment");
                    }
                    skipText("-->");
                    //FIXME: any whitespaces before comment will be lost
                    return getType();
                case '[':
                    read("CDATA[");
                    return CDATA;
                case 'D':
                    read("OCTYPE");
                    return DOCTYPE;
                default:
                    throw new XmlReaderException("Unknown type", this.toString());
                }
            default:
                return START_TAG;
            }
        } else {
            return TEXT;
        }
    }

        
    public int getStatus() throws IOException {
        skipWhitespace();
        switch (c) {
        case '/':
            read('>');//don't advance
            return FINISH_EMPTY_TAG;
        case '?':
            read('>');//don't advance
            return FINISH_PI_XML;
        case '>':
            return FINISH_TAG;
        case '=':
            next();
            skipWhitespace();
            return ATTRIBUTE_VALUE;
        default:
            return ATTRIBUTE;
        }
    }
    
    /**
     * returns the encoding that can be sent to
     * InputStreamReader(InputStream, String)
     * OutputStreamWriter(OutputStream, String) corresponding to
     * encoding. Some phones have bugs and don't accept the normal
     * names or only certain aliases.
     *
     * A more general approach would be to try to create the
     * InputStreamReader, catch the UnsupportedEncodingException and
     * try with other known aliases until success. However, we would
     * then need to have corresponing methods for the methods in class
     * String which also takes the encoding as a parameter.
     *
     * This method should probably be in some utility class instead.
     *
     * see IANA's list of charsets
     * http://www.iana.org/assignments/character-sets
     */
    private static String deviceTranslateEncoding(String encoding) {
        encoding = encoding.toUpperCase(); // CLDC 1.0
        if (encoding.equals("UTF-8") || encoding.equals("UTF8")) {
            return "UTF-8";
            // only upper case ("UTF-8") works on LG phones (at least
            // U8110) and Sony Ericsson JP3 phones (K500, K506, K508,
            // F500, K700, S700, S710a, K300, J300, Z500)
        }
        else if (encoding.equals("ISO-8859-1") // MIME-preferred name
                 || encoding.equals("ISO_8859-1")
                 || encoding.equals("ISO8859_1") // Sun CLDC 1.0 spec
                                                 // and LG-phones
                 ) {
            return "ISO-8859-1";
        }
        else {
            return encoding;
        }
    } // deviceTranslateEncoding
    
    //----------------------- Reader ------------------------------------//
    boolean readerOpen;
    
    Reader getValueReader() {
        readerOpen = true;
        return this;
    }
    
    /* (non-Javadoc)
     * @see java.io.Reader#read()
     */
    public int read() throws IOException {
        checkOpen();
        int rez;
        if (c != '<') {
            rez = c;
            next();
        } else {
            rez = -1;
        }
        return rez;
    }

    /** 
     * As a difference from {@link Reader#read(char[], int, int)} behavior 
     * this method will block until all data is read 
     * (not only until some input is available), an I/O error occurs, 
     * or the end of the stream is reached. 
     *   
     * @see java.io.Reader#read(char[], int, int)
     */
    public int read(char[] cbuf, int off, int len) 
        throws IOException {
        checkOpen();
        //test first end of the stream 
        if (c == '<') return -1;
        
        int i = off;
        int end = off + len;  
        //findbugs: changed to use short-circuit logic(&&) 
        //rather than non-short-circuit logic(&)
        while ((i < end) && c != '<') {
            cbuf[i++] = c;
            next();
        }
        return (i - off);//the number of char actually read
    }
    
    /* (non-Javadoc)
     * @see java.io.Reader#close()
     */
    public void close() throws IOException {
        if (readerOpen) {
            readerOpen = false;
            //skip the rest in order to not remain in a confuse position 
            //other way isTextType() will reaturn true; 
            skipText('<');
        }
    }

    
    private void checkOpen() throws IOException {
        if (!readerOpen) {
            throw new IOException("Value reader closed");
        }
    }
   
}
