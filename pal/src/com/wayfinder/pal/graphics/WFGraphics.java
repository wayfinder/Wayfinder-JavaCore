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

package com.wayfinder.pal.graphics;

public interface WFGraphics {
    
    /**
     * Draws a line between the coordinates <code>(x1,y1)</code> and
     * <code>(x2,y2)</code> using
     * the current color and stroke style.
     * 
     * @param x1 - the x coordinate of the start of the line
     * @param y1 - the y coordinate of the start of the line
     * @param x2 - the x coordinate of the end of the line
     * @param y2 - the y coordinate of the end of the line
     * @param thickness - how thick the drawn line should be
     */
    public void drawLine(int x1, int y1, int x2, int y2, int thickness); 
    
    
    /**
     * Draws a line between the end of previously drawn line and
     * <code>(x,y)</code> using the current color and stroke style. 
     * First call requires a previous  call of DrawLine.
     * 
     * @param x - the x coordinate of the end of the line
     * @param y - the y coordinate of the end of the line
     * @param thickness - how thick the drawn line should be
     */
    public void drawConnectedLine(int x, int y,  int thickness); 
    
    
    
    /**
     * Checks to see if the underlying implementation supports the drawPath()
     * method.
     * 
     * @return true if drawPath() is supported
     */
    public boolean supportsPath();
    
    
    /**
     * Draws several lines at once, making them appear as one lone line.
     * <p>
     * This method can only be used if supportsPath() returns true
     * 
     * @param xCoords  the x coordinates of the path
     * @param yCoords  the y coordinates of the path
     * @param nbrCoords  number of coordinates in the path
     * @param width  width of the path
     */
    public void drawPath(int[] xCoords, int[] yCoords, int nbrCoords, int width);

    /**
     * Sets the current color to the specified RGB values. All subsequent
     * rendering operations will use this specified color. The RGB value
     * passed in is interpreted with the least significant eight bits
     * giving the blue component, the next eight more significant bits
     * giving the green component, and the next eight more significant
     * bits giving the red component. That is to say, the color component
     * is specified in the form of <code>0x00RRGGBB</code>. The high
     * order byte of
     * this value is ignored.
     * 
     * @param RGB - the color being set
     * @see getColor()
     */
    public void setColor(int color);
    
    
    /**
     * Gets the current color.
     * 
     * @return an integer in form 0x00RRGGBB
     * @see #setColor(int)
     */
    public int getColor();
    
    
    /**
     * Draws the outline of the specified rectangle using the current
     * color and stroke style.
     * The resulting rectangle will cover an area <code>(width + 1)</code>
     * pixels wide by <code>(height + 1)</code> pixels tall.
     * If either width or height is less than
     * zero, nothing is drawn.
     * 
     * @param x - the x coordinate of the rectangle to be drawn
     * @param y - the y coordinate of the rectangle to be drawn
     * @param width - the width of the rectangle to be drawn
     * @param height - the height of the rectangle to be drawn
     * @see #fillRect(int, int, int, int)
     */
    public void drawRect(int x, int y, int width, int height);

    /**
     * Fills the specified rectangle with the current color.
     * If either width or height is zero or less,
     * nothing is drawn.
     * 
     * @param x - the x coordinate of the rectangle to be filled
     * @param y - the y coordinate of the rectangle to be filled
     * @param width - the width of the rectangle to be filled
     * @param height - the height of the rectangle to be filled
     * @see #drawRect(int, int, int, int)
     */
    public void fillRect(int x, int y, int width, int height);
    
    /**
     * Draws the specified <code>String</code> using the current font and color.
     * The <code>x,y</code> position is the position of the anchor point.</a>.
     * 
     * @param str - the String to be drawn
     * @param x - the x coordinate of the anchor point
     * @param y - the y coordinate of the anchor point
     * @param anchor - the anchor point for positioning the text. Should be one
     * of the ANCHOR_ constants in this interface
     * @throws NullPointerException - if str is null
     * @throws IllegalArgumentException - if anchor is not a legal value
     */
    public void drawText(String str, int x, int y, int anchor);
    
    /**
     * Draws the specified <code>String</code> using the current font and color.
     * The <code>x,y</code> position is the position of the anchor point.</a>.
     * 
     * @param str - the String to be drawn
     * @param x - the x coordinate of the anchor point
     * @param y - the y coordinate of the anchor point
     * @param maxWidth - The maximum width of the text in pixels. If the string 
     * is wider that this value, it will be cropped to fit.
     * @param anchor - the anchor point for positioning the text. Should be one
     * of the ANCHOR_ constants in this interface
     * @throws NullPointerException - if str is null
     * @throws IllegalArgumentException - if anchor is not a legal value
     */
    public void drawText(String s, int x, int y, int maxWidth, int anchor);
    
    /**
     * Draws the specified <code>String</code> using the current font and color.
     * The <code>x,y</code> position is the position of the anchor point.</a>.
     * 
     * @param str - the String to be drawn
     * @param x - the x coordinate of the anchor point
     * @param y - the y coordinate of the anchor point
     * @param maxWidth - The maximum width of the text in pixels. If the string 
     * is wider that this value, it will be cropped to fit.
     * @param anchor - the anchor point for positioning the text. Should be one
     * of the ANCHOR_ constants in this interface
     * @param suffix the suffix string that should be displayed at the end if maxWidth less then the with of str.
     * @throws NullPointerException - if str is null
     * @throws IllegalArgumentException - if anchor is not a legal value or if maxWidth is less then the width of suffix. 
     */
    public void drawText(String str, int x, int y, int maxWidth, int anchor, String suffix);
    
    /**
     * Return true if the platform supports to draw rotated texts. 
     * 
     * @return
     */
    public boolean supportRotatedTexts();
    
    /**
     * 
     * @param str
     * @param x
     * @param y
     * @param tanTheta
     */
    public void drawRotatedText(String str, int x, int y, double tanTheta);
    
    /**
     * Gets the X offset of the current clipping area, relative
     * to the coordinate system origin of this graphics context.
     * Separating the <code>getClip</code> operation into two methods returning
     * integers is more performance and memory efficient than one
     * <code>getClip()</code> call returning an object.
     * 
     * @return X offset of the current clipping area
     * @see clipRect(int, int, int, int),  setClip(int, int, int, int)
     */
    public int getClipX();
    
    
    /**
     * Gets the Y offset of the current clipping area, relative
     * to the coordinate system origin of this graphics context.
     * Separating the <code>getClip</code> operation into two methods returning
     * integers is more performance and memory efficient than one
     * <code>getClip()</code> call returning an object.
     * 
     * @return Y offset of the current clipping area
     * @see clipRect(int, int, int, int),  setClip(int, int, int, int)
     */
    public int getClipY();
    
    
    /**
     * Gets the width of the current clipping area.
     * 
     * @return width of the current clipping area.
     */
    public int getClipWidth();
    
    
    /**
     * Gets the height of the current clipping area.
     * 
     * @return height of the current clipping area.
     */
    public int getClipHeight();
    
    
    /**
     * Renders a series of device-independent RGB+transparency values in a
     * specified region.  The values are stored in
     * <code>rgbData</code> in a format
     * with <code>24</code> bits of RGB and an eight-bit alpha value
     * (<code>0xAARRGGBB</code>),
     * with the first value stored at the specified offset.  The
     * <code>scanlength</code>
     * specifies the relative offset within the array between the
     * corresponding pixels of consecutive rows.  Any value for
     * <code>scanlength</code> is acceptable (even negative values)
     * provided that all resulting references are within the
     * bounds of the <code>rgbData</code> array. The ARGB data is
     * rasterized horizontally from left to right within each row.
     * The ARGB values are
     * rendered in the region specified by <code>x</code>,
     * <code>y</code>, <code>width</code> and <code>height</code>, and
     * the operation is subject to the current clip region
     * and translation for this <code>Graphics</code> object.
     * 
     * <p>Consider <code>P(a,b)</code> to be the value of the pixel
     * located at column <code>a</code> and row <code>b</code> of the
     * Image, where rows and columns are numbered downward from the
     * top starting at zero, and columns are numbered rightward from
     * the left starting at zero. This operation can then be defined
     * as:</p>
     * 
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     * <pre><code>
     * P(a, b) = rgbData[offset + (a - x) + (b - y) * scanlength]       </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * 
     * <p> for </p>
     * 
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     * <pre><code>
     * x &lt;= a &lt; x + width
     * y &lt;= b &lt; y + height    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p> This capability is provided in the <code>Graphics</code>
     * class so that it can be
     * used to render both to the screen and to offscreen
     * <code>Image</code> objects.  The
     * ability to retrieve ARGB values is provided by the <A HREF="../../../javax/microedition/lcdui/Image.html#getRGB(int[], int, int, int, int, int, int)"><CODE>Image.getRGB(int[], int, int, int, int, int, int)</CODE></A>
     * method. </p>
     * 
     * <p> If <code>processAlpha</code> is <code>true</code>, the
     * high-order byte of the ARGB format
     * specifies opacity; that is, <code>0x00RRGGBB</code> specifies a
     * fully transparent
     * pixel and <code>0xFFRRGGBB</code> specifies a fully opaque
     * pixel.  Intermediate
     * alpha values specify semitransparency.  If the implementation does not
     * support alpha blending for image rendering operations, it must remove
     * any semitransparency from the source data prior to performing any
     * rendering.  (See <a href="Image.html#alpha">Alpha Processing</a> for
     * further discussion.)
     * If <code>processAlpha</code> is <code>false</code>, the alpha
     * values are ignored and all pixels
     * must be treated as completely opaque.</p>
     * 
     * <p> The mapping from ARGB values to the device-dependent
     * pixels is platform-specific and may require significant
     * computation.</p>
     * 
     * @param rgbData - an array of ARGB values in the format 0xAARRGGBB
     * @param offset - the array index of the first ARGB value
     * @param scanlength - the relative array offset between the corresponding pixels in consecutive rows in the rgbData array
     * @param x - the horizontal location of the region to be rendered
     * @param y - the vertical location of the region to be rendered
     * @param width - the width of the region to be rendered
     * @param height - the height of the region to be rendered
     * @param processAlpha - true if rgbData has an alpha channel, false if all pixels are fully opaque
     * @throws ArrayIndexOutOfBoundsException - if the requested operation will attempt to access an element of rgbData whose index is either negative or beyond its length
     * @throws NullPointerException - if rgbData is null
     */
    public void drawRGB(int[] argbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha);
    

    /**
     * Sets the font for all subsequent text rendering operations.  If font is
     * <code>null</code>, the default system font will be used.
     * 
     * @param font - the specified font
     * @see WFFont    
     */
    public void setFont(WFFont aFont);
    
    
    /**
     * Sets the current clip to the rectangle specified by the given coordinates.
     * Rendering operations have no effect outside of the clipping area.
     * 
     * @param x - the x coordinate of the new clip rectangle
     * @param y - the y coordinate of the new clip rectangle
     * @param width - the width of the new clip rectangle
     * @param height - the height of the new clip rectangle
     */
    public void setClip(int x, int y, int width, int height);
    
    
    /**
     * Fills the specified triangle with the current color.  The lines
     * connecting each pair of points are included in the filled
     * triangle.
     * 
     * @param x1 - the x coordinate of the first vertex of the triangle
     * @param y1 - the y coordinate of the first vertex of the triangle
     * @param x2 - the x coordinate of the second vertex of the triangle
     * @param y2 - the y coordinate of the second vertex of the triangle
     * @param x3 - the x coordinate of the third vertex of the triangle
     * @param y3 - the y coordinate of the third vertex of the triangle
     */
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3);
    
    
    /**
     * A check to see if the current drawing platform supports the drawing
     * of polygons. If this method returns true, the methods drawPolygon() and
     * fillPolygon() can be used.
     * 
     * @return true if the platform supports drawing of polygons
     */
    public boolean supportsPolygon();
    
    
    /**
     * Draws the outline of the specified polygon using the current
     * color and stroke style.
     * 
     * @param x - an array specifying the x coordinates for the polygon
     * @param y - an array specifying the y coordinates for the polygon
     */
    public void drawPolygon(int[] x, int[] y);
    
    
    /**
     * Fills the specified polygon with the current color.  The lines
     * connecting each pair of points are included in the filled
     * polygon.
     * 
     * @param x - an array specifying the x coordinates for the polygon
     * @param y - an array specifying the y coordinates for the polygon
     */
    public void fillPolygon(int[] x, int[] y, int length);

    
//    /**
//     * A check to see if the current drawing platform supports matrix transformations.
//     * If this method returns true, the methods setTransformMatrix() and
//     * fillPolygonT() can be used.
//     * 
//     * @return true if the platform supports matrix transformations.
//     */
//    public boolean supportsTransforms();
//    
//    /**
//     * Sets the 3x3 transformation matrix to be used in method fillPolygonT().
//     * 
//     * @param matrix - the 3x3 transformation matrix.
//     */
//    public void setTransformMatrix(float[] matrix);
//   
//    /**
//     * Transforms the provided points with the matrix specified in setTransformMatrix().
//     * 
//     * @param points - an array of x,y pairs
//     * @param length - the number of elements in the points array to use
//     */
//    public void transform(int[] points, int length);
//
//    /**
//     * Fills the specified polygon with the current color.  The lines
//     * connecting each pair of points are included in the filled
//     * polygon. The matrix specified in setTransformMatrix() will be applied.
//     * 
//     * @param points - an array of x,y pairs
//     * @param length - the number of elements in the points array to use
//     */
//    public void fillPolygonT(int[] points, int length);

    /**
     * Draws the specified image by using the anchor point.
     * The image can be drawn in different positions relative to
     * the anchor point by passing the appropriate position constants.
     * 
     * <p>If the source image contains transparent pixels, the corresponding
     * pixels in the destination image must be left untouched.  If the source
     * image contains partially transparent pixels, a compositing operation
     * must be performed with the destination pixels, leaving all pixels of
     * the destination image fully opaque.</p>
     * 
     * 
     * @param img - the specified image to be drawn
     * @param x - the x coordinate of the anchor point
     * @param y - the y coordinate of the anchor point
     * @param anchor - the anchor point for positioning the WFImage
     * @throws IllegalArgumentException - if anchor is not a legal value
     * @throws NullPointerException - if img is null
     * @see WFImage
     */
    public void drawImage(WFImage img, int x, int y, int anchor);
    
    
    /**
     * A hint to the implementation that it may draw lines and shapes using
     * antialias at this time. It's up to the implementation to decide if it
     * appropriate to do so based on system load.
     * 
     * This method is only used to enhance the appearance for the user. Not all
     * platforms support this functionallity.
     * 
     * @param allow true if antialias can be used
     */
    public void allowAntialias(boolean allow);
    
    
    //------------------------------------------------------------
    // anchors
    
    /**
     * Constant for centering text and images horizontally
     * around the anchor point
     * 
     * <P>Value <code>1</code> is assigned to <code>HCENTER</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_HCENTER = 1;

    /**
     * Constant for centering images vertically
     * around the anchor point.
     * 
     * <P>Value <code>2</code> is assigned to <code>VCENTER</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_VCENTER = 2;

    /**
     * Constant for positioning the anchor point of text and images
     * to the left of the text or image.
     * 
     * <P>Value <code>4</code> is assigned to <code>LEFT</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_LEFT = 4;

    /**
     * Constant for positioning the anchor point of text and images
     * to the right of the text or image.
     * 
     * <P>Value <code>8</code> is assigned to <code>RIGHT</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_RIGHT = 8;

    /**
     * Constant for positioning the anchor point of text and images
     * above the text or image.
     * 
     * <P>Value <code>16</code> is assigned to <code>TOP</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_TOP = 16;

    /**
     * Constant for positioning the anchor point of text and images
     * below the text or image.
     * 
     * <P>Value <code>32</code> is assigned to <code>BOTTOM</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_BOTTOM = 32;

    /**
     * Constant for positioning the anchor point at the baseline of text.
     * 
     * <P>Value <code>64</code> is assigned to <code>BASELINE</code>.</P></DL>
     * 
     */
    public static final int ANCHOR_BASELINE = 64;

}
