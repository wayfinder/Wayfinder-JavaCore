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
 * Created on Feb 7, 2005
 *
 */
package com.wayfinder.core.shared.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Implementation for XmlWriter, dosen't use any internal buffer. 
 *  
 * 
 */
public class XmlWriterImpl implements XmlWriter {
    private Writer m_writer;

    /**
     * if false then characters > 127 will be written using XML
     * decimal character references, e.g. &#C5; for A with a
     * ring. See section 4.1 of "Extensible Markup Language (XML) 1.0
     * (Fourth Edition)"
     *
     * if true then the character is sent to writer.write(char c) and
     * writer is supposed to write the bytes that is the coded
     * representation of the character in the writer's character set,
     * e.g. utf-8.
     *
     * see setOutput(Writer) and setOutput(OutputStream, String)
     */
    private boolean unicode;

    private String encoding;

    private boolean pending;

    private int depth;

    private String elementStack[] = new String[16];
    
    
    public XmlWriterImpl() {
    }
    
    private void check(boolean close) throws IOException {
        if (!pending) return;
        pending = false;
        if (close) m_writer.write('/');
        m_writer.write('>');
        //writer.write(close ? "/>" : ">");
    }

    private void writeEscaped(String s, int quot) throws IOException {

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\n':
            case '\r':
            case '\t':
                if (quot == -1)
                    m_writer.write(c);
                else
                    m_writer.write("&#" + ((int) c) + ';');
                break;
            case '&':
                m_writer.write("&amp;");
                break;
            case '>':
                m_writer.write("&gt;");
                break;
            case '<':
                m_writer.write("&lt;");
                break;
            case '"':
            case '\'':
                if (c == quot) {
                    m_writer.write(c == '"' ? "&quot;" : "&apos;");
                    break;
                }
            default:
                //if(c < ' ') throw new IllegalArgumentException("Illegal
                // control code:"+((int) c));

                if (c >= ' ' && c != '@' && (c < 127 || unicode))
                    m_writer.write(c);
                else {
                    m_writer.write("&#");
                    m_writer.write(String.valueOf((int) c));
                    m_writer.write(';');
                }
            }
        }
    }
    
    private void writeEscaped(byte[] s, int quot) throws IOException {

        for (int i = 0; i < s.length; i+=2) {
            char c = (char) s[i];
            switch (c) {
            case '\n':
            case '\r':
            case '\t':
                if (quot == -1)
                    m_writer.write(c);
                else
                    m_writer.write("&#" + ((int) c) + ';');
                break;
            case '&':
                m_writer.write("&amp;");
                break;
            case '>':
                m_writer.write("&gt;");
                break;
            case '<':
                m_writer.write("&lt;");
                break;
            case '"':
            case '\'':
                if (c == quot) {
                    m_writer.write(c == '"' ? "&quot;" : "&apos;");
                    break;
                }
            default:
                //if(c < ' ') throw new IllegalArgumentException("Illegal
                // control code:"+((int) c));

                if (c >= ' ' && c != '@' && (c < 127 || unicode))
                    m_writer.write(c);
                else {
                    m_writer.write("&#");
                    m_writer.write(String.valueOf((int) c));
                    m_writer.write(';');
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#docdecl(java.lang.String)
     */
    public void docdecl(String dd) throws IOException {
        m_writer.write("<!DOCTYPE ");
        m_writer.write(dd);
        m_writer.write('>');
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#endDocument()
     */
    public void endDocument() throws IOException {
        while (depth > 0) {
            endElement();
            //endElement(elementStack[depth-1]);
        }
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#ignorableWhitespace(java.lang.String)
     */
    public void ignorableWhitespace(String s) throws IOException {
        m_writer.write(s);
    }

    /**
     * Will set unicode = false
     */
    public void setOutput(Writer writer) {
        this.m_writer = writer;
        pending = false;
        depth = 0;
        unicode = false;
    }

    /**
     * Will set unicode = true if encoding starts with "utf"
     */
    public void setOutput(OutputStream os, String encoding) throws IOException {
        if (os == null)
            throw new IllegalArgumentException();
        setOutput(encoding == null ? new OutputStreamWriter(os)
                : new OutputStreamWriter(os, encoding));
        this.encoding = encoding;
        if (encoding != null && encoding.toLowerCase().startsWith("utf"))
            unicode = true;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#startDocument(java.lang.String, java.lang.Boolean)
     */
    public void startDocument(String encoding, Boolean standalone)
            throws IOException {
        m_writer.write("<?xml version='1.0' ");

        if (encoding != null) {
            this.encoding = encoding;
            if (encoding.toLowerCase().startsWith("utf"))
                unicode = true;
        }

        if (this.encoding != null) {
            m_writer.write("encoding='");
            m_writer.write(this.encoding);
            m_writer.write("' ");
        }

        if (standalone != null) {
            m_writer.write("standalone='");
            m_writer.write(standalone.booleanValue() ? "yes" : "no");
            m_writer.write("' ");
        }
        m_writer.write("?>");
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#startElement(java.lang.String)
     */
    public void startElement(String name) throws IOException {
        check(false);
        
        elementStack[depth++] = name;

        m_writer.write('<');

        m_writer.write(name);

        pending = true;
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#attribute(java.lang.String, java.lang.String)
     */
    public void attribute(String name, String value) throws IOException {
        if (!pending)
            throw new IllegalStateException("illegal position for attribute");

        m_writer.write(' ');
        m_writer.write(name);
        m_writer.write('=');
        char q = value.indexOf('"') == -1 ? '"' : '\'';
        m_writer.write(q);
        writeEscaped(value, q);
        m_writer.write(q);
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#attribute(java.lang.String, int)
     */
    public void attribute(String name, int value) throws IOException {
        if (!pending)
            throw new IllegalStateException("illegal position for attribute");

        m_writer.write(' ');
        m_writer.write(name);
        m_writer.write('=');
        char q = '"';
        m_writer.write(q);
        writeInt(value);
        m_writer.write(q);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#attribute(java.lang.String, boolean)
     */
    public void attribute(String name, boolean value) throws IOException {
        if (!pending)
            throw new IllegalStateException("illegal position for attribute");

        m_writer.write(' ');
        m_writer.write(name);
        m_writer.write('=');
        char q = '"';
        m_writer.write(q);
        m_writer.write(String.valueOf(value));
        m_writer.write(q);
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#endElement()
     */
    public void endElement() throws IOException{
        depth--;
        if (pending) { 
            check(true);//empty tag; 
        } else {
            m_writer.write("</");
            m_writer.write(elementStack[depth]);
            m_writer.write('>');
        }
    }
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#endElement(java.lang.String)
     */
    public void endElement(String name) throws IOException {

        if (!elementStack[depth-1].equals(name))
            throw new IllegalArgumentException("</" + name
                    + "> does not match start");
        endElement();
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#getName()
     */
    public String getName() {
        return depth == 0 ? null : elementStack[depth - 1];
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#getDepth()
     */
    public int getDepth() {
        return depth;
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#text(java.lang.String)
     */
    public void text(String text) throws IOException {
        check(false);
        writeEscaped(text, -1);
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#text(int)
     */
    public void text(int value) throws IOException {
        check(false);
        writeInt(value);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#text(byte[])
     */
    public void text(byte[] value) throws IOException {
        check(false);
        writeEscaped(value, -1);
    }
    

    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#cdsect(java.lang.String)
     */
    public void cdsect(String data) throws IOException {
        check(false);
        m_writer.write("<![CDATA[");
        m_writer.write(data);
        m_writer.write("]]>");
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#comment(java.lang.String)
     */
    public void comment(String comment) throws IOException {
        check(false);
        m_writer.write("<!--");
        m_writer.write(comment);
        m_writer.write("-->");
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.core.shared.xml.XmlWriter#close()
     */
    public void close() throws IOException {
        m_writer.close();
    }

    /**
     * string buffer used for conversion from int to string
     * need by writeInt(int value)
     */
    private StringBuffer sb = new StringBuffer(11);
    private char[]       cb = new char[11]; 
    
    /**
     * write a an int value as a string without creation of a temporary 
     * String object
     * @param value
     * @throws IOException
     */
    private void writeInt(int value) throws IOException {
        sb.setLength(0);
        sb.append(value);
        int lenght = sb.length();
        sb.getChars(0, lenght, cb, 0);
        m_writer.write(cb, 0, lenght);
    }
}
