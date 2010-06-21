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
 * Copyright, Wayfinder Systems AB, 2005 - 2009
 */

package com.wayfinder.core.shared.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.util.CharArray;


/**
 * 
 * 
 * <p>The following method iterates recursively over all elements in a
 * document with mixed content</p> 
 *
 * <pre>
 * void parseMixed(XmlIterator xpi) throws IOException {
 *      System.out.println("[" + xpi.name() + "]");
 *      if (xpi.name() == PCDATA) {//the text element constant 
 *          System.out.println(xpi.value());
 *      } else {
 *          if (xpi.children()) {
 *              do {
 *                parseMixed(xpi);
 *              } while (xpi.advance());
 *          }
 *      }
 *      System.out.println("[/" + xpi.name() + "]");
 *  }
 * </pre>
 *
 * <p>The following method iterates linear over all elements in a
 * document with mixed content</p> 
 * <pre>
 * void parseMixedLinear(XmlIterator xpi) throws IOException {
 *       boolean start = true;
 *       while (true) {
 *          if (start) {
 *              printSpaces(xpi.getDepth()-1);
 *              System.out.println("[" + xpi.name() + "]");
 *              if (xpi.name() == PCDATA) {//the text element constant 
 *                  System.out.println(xpi.value());
 *              }
 *              start = xpi.children();
 *          } else {
 *              System.out.println("[/" + xpi.name() + "]");
 *              start = xpi.advance();
 *              if (xpi.getDepth() == 0) break;
 *          } 
 *      } 
 *  }
 *  </pre>
 *
 * This class never operates on a java.io.Reader directly. That is
 * done by com.wayfinder.xml.XmlReader. Thus, this class is not affected by bug
 * #1627
 */
public class LightXmlPullIterator implements XmlIterator {

    private static final Logger LOG =
        LogFactory.getLoggerForClass(LightXmlPullIterator.class);
    
    //used to parse mixed content
    //-------------------------------
    final private boolean mixed;
    final private String textElementName;
    
    /**
     * if mixed is false this will always be false
     */
    private boolean isText;
    //-------------------------------
    
    /**
     * Array that store some of the elements names from DTD 
     * The string are the only strings returned by name()
     * This is used in order to avoid creation of string objects for 
     * each element.
     * Notice: The application can (and should) use == operator instead of 
     * String.equal method to compare elements names.       
     */
    final private String[] elementName;
    
    /**
     * Array that stores the attribute names from DTD
     * This way the String objects will not be created during parsing.
     * Notice: The attributes that are not in this array will be skipped
     * Warning: Use the same string object when calling attribute(String name) 
     * method as the ones in the attributeName array.
     */
    final private String[] attributeName;
    
    private int elementStack[] = new int[16];

    private int depth;

    private int current;

    private final XmlReader reader = new XmlReader();

    private boolean wasParsed;

    private boolean isEmpty;
    
    private boolean isWhitespace;
    
    private int attrCount;
    private int[] attrIndex = new int[18];
    private int[] attrBufferPos = new int[18];

    private CharArray attrValues = new CharArray(128);
    
    /**
     * Create an Iterator for parsing XML document with mixed content
     * The text between elements is considered as a special element with text  
     * advance() and children() will also iterate through text like being simple 
     * elements. 
     * 
     * @param elementName array that store some of the elements names from DTD 
     * @param attributeName array that stores the attribute names from DTD
     * @param textElementName the string returned by name() method when 
     * the parser is positioned on a text; can be set to null
     * 
     * <p>
     * WARNING: the elementName and attributeName arrays parameters will be 
     * accessed directly, do not modified them during parsing.</p>
     */
    public LightXmlPullIterator(String[] elementName, String[] attributeName, 
            String textElementName) {
        this.elementName = elementName;
        this.attributeName = attributeName;
        this.textElementName = textElementName;
        this.mixed = true;
    }

    /**
     * Create an Iterator for parsing XML document with no mixed content
     * @param elementName array that store some of the elements names from DTD
     * @param attributeName array that stores the attribute names from DTD
     * 
     * <p>
     * WARNING: the elementName and attributeName arrays parameters will be 
     * accessed directly, do not modified them during parsing.</p>
     */
    public LightXmlPullIterator(String[] elementName, String[] attributeName) {
        this.elementName = elementName;
        this.attributeName = attributeName;
        this.textElementName = null;
        this.mixed = false;
    }
    
    private void reset() {
        depth = 0;
        current = -1;
        wasParsed = false;
        isText = false;    
        attrValues.empty();
        attrCount = 0;
    }
    
    public void setInput(Reader in) throws IOException {
        reader.setInput(in);
        reset();
    }

    public void setInput(InputStream inputStream, String inputEncoding)
            throws IOException {
        reader.setInput(inputStream, inputEncoding);
        reset();
    }

    /**
     * read element <[.....>] (or ....../) store on elementStack[depth] depth++
     * 
     * @throws IOException
     */
    private void readStartTag() throws IOException {
        //<[element a]ttr1 = value1 />
        reader.pushName();
        current = reader.charArray.indexIn(elementName);

        elementStack[depth++] = current;

        if (readAttributes() == XmlReader.FINISH_EMPTY_TAG) {
            isEmpty = true;//is empty tag should </element> should not be read
            wasParsed = true;//was fully parsed
        } else {
            isEmpty = false;//is not empty tag </element> should be read
            wasParsed = false;// the parsing didn't start yet
        }
    }

    /**
     * read end tag </|element >] must be elementStack[--depth])
     * 
     * @throws IOException
     */
    private void readEndTag() throws IOException {
        if (!isEmpty) {
            reader.skipText('>'); // <[/element>]
        }

        if (--depth != 0)
            current = elementStack[depth - 1];
        else
            current = -1;

        //set parent
        isEmpty = false;//is not empty because it had a child
        wasParsed = false;//was not fully parsed yet
    }

    /**
     * Fills the tagAttribute Hashtable with <attribute -- value> pairs for each
     * attribute of the tag element that calls the method
     * 
     * @throws IOException
     */
    private int readAttributes() throws IOException {
        int status;
        attrCount = 0;
        attrValues.empty();

        while ((status = reader.getStatus()) == XmlReader.ATTRIBUTE) {
            reader.pushName();
            
            /* Searching for the attribute name */
            int index = reader.charArray.indexIn(attributeName);

            if (index != -1) { 
                //-1 means that the app/user doesn't care about that attribute
                attrIndex[attrCount] = index;
                
                CharArray swap = reader.charArray; //swap the charArray
                reader.charArray = attrValues;
                
                reader.pushAttrValue(false);//store the attribute value in attrValue
                attrBufferPos[attrCount++] = attrValues.length();
                
                reader.charArray = swap;
            } else {
                //not actually interested in the value 
                //improve by skipping the chars
                //reader.charArray.empty();
                reader.pushAttrValue(true);//skip the value
            }
        }
        return status;
    }

    /**
     * Close the current element and advance to next siblings if possible If
     * called from the last child doesn't close the parent element
     * 
     * @see XmlIterator#advance()
     */
    public boolean advance() throws IllegalStateException, IOException {
        checkDepth();
        
        if (isText) { 
            //the current element is a text element
            reader.skipText('<');
            isText = false;
        } else {
            if (!wasParsed) reader.skipContent();
        
            //we read the end tag and go to the parent
            readEndTag(); //wasParsed = false;
        
            if (depth == 0) {
                wasParsed = true;//the entire xml was parsed
                return false;// end of root
            }
        }    
        return children();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.wayfinder.io.XmlIterator#children()
     */
    public boolean children() throws IOException {
        if (isEmpty || isText) return false;
        checkNotParsed();
        
        int type;
        if (depth == 0) {
            type = prolog();
        } else {
            type = reader.getType();
        }
        
        switch (type) {
            case XmlReader.START_TAG: readStartTag();return true;
            case XmlReader.TEXT: 
                    if (mixed) {
                        //consider text as a child element
                        isText = true;
                        wasParsed = false;
                        isEmpty = false;
                        attrCount = 0;
                        return true;
                    } else return false;
            case XmlReader.END_TAG: wasParsed = true; // no break call 
            default: return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.wayfinder.io.XmlIterator#value()
     */
    public String value() throws IllegalStateException, IOException {
        CharArray ca = valueCharArray();
        if (ca == null) return null;
        return ca.toString();        
    }

    /* (non-Javadoc)
     * @see com.wayfinder.xml.XmlIterator#valueCharArray()
     */
    public CharArray valueCharArray() throws IllegalStateException, IOException {
        if (isEmpty) {
            if(LOG.isTrace()) {
                LOG.trace("LightXmlPullIterator.valueCharArray()", "isEmpty, returning null");
            }
            return null;
        }
        //checkNotParsed(); better throw exception is try to parse it twice
        if (wasParsed) {
            if(LOG.isTrace()) {
                LOG.trace("LightXmlPullIterator.valueCharArray()", "wasParsed, returning null");
            }
            return null;
        }
        if (reader.isTextType()) {
            if(LOG.isTrace()) {
                LOG.trace("LightXmlPullIterator.valueCharArray()", "isTextType");
            }
            //return the spaces read by isTextType() plus all the chars till 
            //delimiter (can be only '<')
            reader.pushText('<');
            isWhitespace = false; 
            return reader.charArray;
        } else {
            if(LOG.isTrace()) {
                LOG.trace("LightXmlPullIterator.valueCharArray()", "!isTextType is whitespace");
            }
            //return the spaces read by isTextType()
            isWhitespace = true; 
            return reader.charArray;//is whitespaces
            //return null //if whitespaces are not needed anyway
        }
    }
    
    /* (non-Javadoc)
     * @see com.wayfinder.xml.XmlIterator#valueAsReader()
     */
    public Reader valueAsReader() throws IOException {
        if (isEmpty) return null;
        if (wasParsed) return null;
        if (reader.isTextType()) {
            isWhitespace = false;
            return reader.getValueReader();
        } else {
            //return the spaces read by isTextType()
            isWhitespace = true; //is whitespaces
            //return null if whitespaces are not needed anyway
        }
        return null;
    }    
    
    /* (non-Javadoc)
     * @see com.wayfinder.xml.XmlIterator#isWhitespace()
     */
    public boolean isWhitespace() {
        return isWhitespace;
    }    

    /**
     * @return a string from elementName array provided in 
     * constructor or null if if the element is unknown. 
     * 
     * <p>
     * NOTICE: The application can (and should) use == operator instead of 
     * String.equal method to compare elements names.
     * <p> 
     * NOTICE: for mixed content when positioned on a text content this will 
     * return textElementName provided in constructor
     * 
     * @see XmlIterator#name()
     */
    public String name() throws IllegalStateException {
        checkDepth();
        if (isText) return textElementName;//mixed
        if (current != -1) return elementName[current];
        else return null;
    }
    
    /**
     * like <code>name()</code> but return the index in the elementName array 
     * instead of the string object
     * 
     * @return index in the elementName array provided in constructor 
     * or -1 if the element is unknown   
     * 
     * <p>
     * NOTICE: for mixed content when positioned on a text content this will 
     * return  <code>elementName.length</code>
     */
    public int nameIndex() throws IllegalStateException {
        checkDepth();
        if (isText) return elementName.length ;//mixed
        else return current;
    }
    
    /**
     * <p>Compares by reference a name with local name of the current element.</p>
     *
     * <p>Only useful when sending in a reference to a string in elementName as
     * provided to the constructor.</p>
     *
     * @see com.wayfinder.core.shared.xml.XmlIterator#nameRefEq(java.lang.String)
     */
    public boolean nameRefEq(String ref) throws IllegalStateException {
        // FINDBUGS: we will turn off the FindBugs warning for this
        // comparison since it is intentional.
        return (ref == name()); // name() may throw IllegalStateException
    }

    /**
     * Read prolog part of the xml
     * The parser will be positioned just before root element  
     * @return XmlReader.START_TAG if the document is not empty
     * @throws IOException if an error occurs while reading or the xml is empty. 
     */
    private int prolog() throws IOException {
        /* from http://www.w3.org/TR/2004/REC-xml11-20040204/
        document    ::=      prolog element Misc* - Char* RestrictedChar Char*
        prolog      ::=      XMLDecl Misc* (doctypedecl Misc*)?
        Misc        ::=      Comment | PI | S
        element     ::=      EmptyElemTag | STag content ETag 
        */
        reader.next();//read first char from stream
        int type = reader.getType();
        
        while (type != XmlReader.START_TAG) {
            //this part it just skipped
            reader.skipText('>');
            type = reader.getType();
        }
        //the order should be 
        //XmlReader.PI_XML, XmlReader.DOCTYPE, XmlReader.START_TAG
        return type;
    }
    
    /**
     * <p>
     * Warning: Use the same string object when calling this method 
     * as the ones in the attributeName array.
     * 
     * <p>
     * Notice: For the attributes that are not in attributeName array the method 
     * will return null like they don't exist.
     *
     * @see XmlIterator#attribute(String)
     */
    public String attribute(String name) throws IllegalStateException,
            IOException {
        CharArray ca = attributeCharArray(name);
        if (ca == null) return null;
        return ca.toString();
    }
    
    public CharArray attributeCharArray(String name) throws IllegalStateException, IOException {
        checkDepth();
        for (int i = attrCount; i !=0; ) {
           //intentional comparison using == as the name must one of the value 
           //from attributeName
           if (attributeName[attrIndex[--i]] == name) {
               attrValues.setBounds((i==0)?0:attrBufferPos[i-1],attrBufferPos[i]);
               return attrValues; //I don't care what happed with this
               //return text.setBuffer(attrBuffer,(i==0)?0:attrBufferPos[i-1],attrBufferPos[i]);
           }
        }
        if(LOG.isTrace()) {
            LOG.trace("LightXmlPullIterator.attributeCharArray()", 
                    "attribute " + name + " expected but doesn't exist in element" + name() );
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.wayfinder.io.XmlIterator#getDepth()
     */
    public int getDepth() {
        return depth;
    }
    
    private void checkNotParsed() throws IllegalStateException{
        if (wasParsed) {
            if(LOG.isError()) {
                LOG.error("LightXmlPullIterator.checkNotParsed()", "failed parser state: " + this.toString());
            }
            throw new IllegalStateException(name() + " was already parsed");
        }
    }
    
    private void checkDepth() throws IllegalStateException{
        if (depth == 0)
            throw new IllegalStateException("children() has not been called");
    }
    /**
     * @return a String that shows the actual state of the parser
     */
    public String toString() {
        StringBuffer b = new StringBuffer(100);
        b.append(reader.toString());
        b.append("\nLightXmlPullIterator:");
        b.append("\n\tdepth: ");
        b.append(depth);
        if (depth > 0) {
            b.append("\n\telement stack: ");
            for (int i = 0; i < depth; i++) {
                if (elementStack[i]!= -1) {
                    b.append(elementName[elementStack[i]]);
                } else {
                    b.append("unknown");
                }
                b.append(", ");
            }
            b.append("\n\tcurrent element: ");
            b.append(name());

            if (attrCount > 0) {
                b.append("\n\tattributes: ");
                for (int i = 0; i < attrCount; i++) {
                    b.append("\n\t\t");
                    b.append(attributeName[attrIndex[i]]);
                    b.append("=");
                    attrValues.setBounds((i==0)?0:attrBufferPos[i-1],attrBufferPos[i]);
                    b.append(attrValues.toString());
                }
            }
        }
        b.append("\n\tentire attribute buffer: ");
        b.append(attrValues.getInternalBuffer());
        return b.toString();
    }
}
