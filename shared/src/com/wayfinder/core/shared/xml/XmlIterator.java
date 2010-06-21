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

package com.wayfinder.core.shared.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.wayfinder.core.shared.util.CharArray;


/**
 * An iterator for traversing the elements and text of an XML document with 
 * no mixed content
 * 
 * <p>The parser cannot parse Comments, CDATA sections and Processing Instructions
 * having those in the xml will lead to errors</p>
 *
 * <p>The parser is optimized for non mixed content only</p>
 *
 * 
 */
public interface XmlIterator
{
    /**
     * Moves to the next element at the current level and returns true, or
     * move down one level and returns false if there are no more elements.
     * Moving down one level means closing to the parent of the last element   
     * @throws IllegalStateException if the level is zero 
     * children() has not yet been called or the root was closed 
     * @throws IOException if an error occurs retrieving or processing input data.
     */
    boolean advance()
        throws IllegalStateException, IOException;

    /**
     * Returns the local name of the current element,
     * An empty string will not be returned.
     *   
     * <p>Notice this can be called multiple and any time after first call of children()</p> 
     *  
     * @throws IllegalStateException if {@link #children()} has not yet
     * been called. 
     */
    String name()
        throws IllegalStateException;

    /**
     * <p>Compares by reference a name with local name of the current element.</p>
     *
     * <p>Returns true if and only if <code>(ref == name())</code>. The
     * comparision is made even if ref or name() are null.
     * This is used as an optimization and encapsulation of the normally bad
     * practice of comparing strings by reference. It is useful only with
     * certain implementations, like {@link LightXmlPullIterator#name()}.</p>
     *
     * @param ref the reference to be compated to.
     * @return true if and only if <code>(ref == name())</code>, otherwise
     * false.
     * @throws IllegalStateException if {@link #children()} has not yet
     * been called. 
     */
    boolean nameRefEq(String ref) throws IllegalStateException;

    /**
     * Returns the text contained by the current element if this is a text element 
     * or null if positioned on an element with children content.
     * 
     * <p>WARNING: if called twice second time will always return null</p>
     * 
     * @throws IllegalStateException if advance() has not yet been called. 
     * @throws IOException if an error occurs retrieving or processing input data.
     */
    String value()
        throws IllegalStateException, IOException;
    
    CharArray valueCharArray()
        throws IllegalStateException, IOException;
    
    Reader valueAsReader() throws IOException;

    /**
     * Checks whether the current TEXT returned by <code>value()</code> or
     * <code>valueCharArray</code>  contains only whitespace characters.
     * @return true is the last TEXT value contains only whitespace characters
     */
    boolean isWhitespace(); 
    /**
     * Move to the first child of the current element and return true, 
     * or return false if the current element is a text or empty element 
     * or does not contain any element children
     *  
     * @throws IOException if an error occurs retrieving or processing input data.
     */
    boolean children()
        throws IOException;

    /**
     * Returns the value of the attribute with the given name belonging to the
     * current element, or null if no such attribute exists
     * <p>WARNING: Unlike the DOM methods Node.getAttribute() and
     * getAttributeNS() this method returns null, not an empty string, when the
     * attribute does not exist.</p>
     * @param name is the local name of the attribute.
     * @throws IllegalStateException if advance() has not yet been called. 
     * @throws IOException if an error occurs retrieving input data or processing input data.
     */
    String attribute(String name)
        throws IllegalStateException, IOException;
    
    CharArray attributeCharArray(String name) 
        throws IllegalStateException, IOException ;

    // --------------------------------------------------------------------------
    // miscellaneous reporting methods from PULL API

    /**
     * Returns the current depth of the element.
     * Outside the root element, the depth is 0. The
     * depth is incremented by 1 when a start tag is reached.
     * The depth is decremented AFTER the end tag
     * event was observed.
     *
     * <pre>
     * &lt;!-- outside --&gt;     0
     * &lt;root>                  1
     *   sometext                 1
     *     &lt;foobar&gt;         2
     *     &lt;/foobar&gt;        2
     * &lt;/root&gt;              1
     * &lt;!-- outside --&gt;     0
     * </pre>
     */
    int getDepth();
    
    
    /**
     * Set the input source for parser to the given reader and resets the
     * parser. The event type is set to the initial value START_DOCUMENT.
     * Setting the reader to null will just stop parsing and reset parser state,
     * allowing the parser to free internal resources such as parsing buffers.
     */
    void setInput(Reader in) throws IOException;
    
    /**
     * Sets the input stream the parser is going to process. This call resets
     * the parser state and sets the event type to the initial value
     * START_DOCUMENT.
     * 
     * <p>
     * <strong>NOTE: </strong> If an input encoding string is passed, it MUST be
     * used. Otherwise, if inputEncoding is null, the parser SHOULD try to
     * determine input encoding following XML 1.0 specification (see below). If
     * encoding detection is supported then following feature <a
     * href="http://xmlpull.org/v1/doc/features.html#detect-encoding">http://xmlpull.org/v1/doc/features.html#detect-encoding
     * </a> MUST be true amd otherwise it must be false
     * 
     * @param inputStream
     *            contains a raw byte input stream of possibly unknown encoding
     *            (when inputEncoding is null).
     * 
     * @param inputEncoding
     *            if not null it MUST be used as encoding for inputStream
     */
    void setInput(InputStream inputStream, String inputEncoding) throws IOException; 
}
