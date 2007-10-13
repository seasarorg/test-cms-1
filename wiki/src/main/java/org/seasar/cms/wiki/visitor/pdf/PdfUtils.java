/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.cms.wiki.visitor.pdf;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.Font;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;

/**
 * <p>
 * KNOWN ISSUE 1. Japanese character is unavailable in PdfPTable During parsing,
 * HTMLWorker uses FactoryProperties static methods. It will create the font
 * object by FactoryProperties#getFont, and we can specify fontname as "face"
 * style, though the encoding is fixed to "BaseFont.WINANSI", that is CP1252,
 * Latin1. Thus, at least, we could not get the objects which can display
 * Japanese character soon after parsing phase even if Japanese fonts are
 * registered by FontFactory#register and its name are given to "face" style. To
 * make matters complicated, HTMLWorker uses PdfPTable to convert &lt;table&gt;
 * and wrapper class IncTable for PdfPTable, and IncCell for PdfPCell. In
 * IncCell, it adds Element by PdfPCell#addElement methods. PdfPCell#addElement
 * methods doesn't keep Phrase or Chunk object in its instances but directly
 * calles ColumnText#addElement. ColumnText#addElement adds element into its
 * protected LinkedList and it seems to be no way provided to change that added
 * element property. => This solved by to create extended class CJKHTMLWorker
 * and CJKFactoryProperties for HTMLWorker and FactoryProperties.
 * </p>
 * 
 * @author someda
 */
public class PdfUtils {

	// Mincho Font properties	
	public static Font FONT_JA_GOTHIC;
	private static final String FONTNAME_JA_GOTHIC = "HeiseiKakuGo-W5";
	private static final String FONTNAME_JA_GOTHIC_ENCODING = "UniJIS-UCS2-H";
	
	// Gothic Font properties
	public static Font FONT_JA_MINCHO;		
	private static final String FONTNAME_JA_MINCHO = "HeiseiMin-W3";
	private static final String FONTNAME_JA_MINCHO_ENCODING = "UniJIS-UCS2-HW-H";
	
	public static Font FONT_ERROR;
	
	public static BaseFont BASEFONT_GOTHIC;
	public static BaseFont BASEFONT_MINCHO;
	
	public static final String DEFAULT_PDF_PAGESTYLE = "v-n-A4";
	
	private static final String COLOR_BLACK_HEX = "000000";
	
	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<\\w+.*>");
	
	private static Log log_ = LogFactory.getLog(PdfUtils.class);
	
	static{
		try{			
			BASEFONT_GOTHIC = BaseFont.createFont(FONTNAME_JA_GOTHIC,FONTNAME_JA_GOTHIC_ENCODING,BaseFont.NOT_EMBEDDED);
			BASEFONT_MINCHO = BaseFont.createFont(FONTNAME_JA_MINCHO,FONTNAME_JA_MINCHO_ENCODING,BaseFont.NOT_EMBEDDED);			
			FONT_JA_GOTHIC = new Font(BASEFONT_GOTHIC, 12,Font.NORMAL);
			FONT_JA_MINCHO = new Font(BASEFONT_MINCHO,12,Font.NORMAL);			
			FONT_ERROR = new Font(BASEFONT_GOTHIC,12,Font.BOLD,Color.RED);			
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch(DocumentException de){
			de.printStackTrace();
		}
	}	
	
	public static Properties getDefaultfontProperties(Font defaultFont){
		
		Properties props = new Properties();
		props.setProperty(ElementTags.FONT,defaultFont.getFamilyname());
		
		// cannot get exact font encoding name set by init(),
		// defaultFont_.getBaseFont().getEncoding() returns "UnicodeBigUnmarked".
		if(defaultFont.equals(FONT_JA_GOTHIC)){
			props.setProperty(ElementTags.ENCODING,FONTNAME_JA_GOTHIC_ENCODING);
		}else if(defaultFont.equals(FONT_JA_MINCHO)){
			props.setProperty(ElementTags.ENCODING,FONTNAME_JA_MINCHO_ENCODING);			
		}
		props.setProperty(ElementTags.EMBEDDED,String.valueOf(defaultFont.getBaseFont().isEmbedded()));
		props.setProperty(ElementTags.STYLE,String.valueOf(defaultFont.style()));
		props.setProperty(ElementTags.SIZE,String.valueOf(defaultFont.size()));
		props.setProperty(ElementTags.COLOR,COLOR_BLACK_HEX);

		return props;
	}
		
	public static Properties getChapterfontProperties(Font defaultFont) {
		Properties props = getDefaultfontProperties(defaultFont);
		props.setProperty(ElementTags.SIZE, String.valueOf(18));
		return props;
	}
		
	public static Properties getSectionfontProperties(Font defaultFont, int depth){
		Properties props = getDefaultfontProperties(defaultFont);
		if(depth > 2){
			props.setProperty(ElementTags.SIZE,String.valueOf(14));
		}else{
			props.setProperty(ElementTags.SIZE,String.valueOf(16));
		}
		return props;		
	}	
	
	// ----- [Start] PDF converter methods -----
	public static Object convertHTMLtoPDF(String s){		
		
		if(s != null){
			Matcher m = HTML_TAG_PATTERN.matcher(s);
			if(m.find()){
				return parseHTMLtoPDF(s);
			}else{
				return new Chunk(s,FONT_JA_GOTHIC);
			}						
		}
		return null;
	}	
	
	public static Object parseHTMLtoPDF(String s) {
		Reader reader = new StringReader(s);
		try{
			return parseHTMLtoPDF(reader);
		}finally{
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					log_.error("failed to close reader object");
				}
			}
		}
	}

	public static Object parseHTMLtoPDF(Reader reader) {

		List list = null;
		float leading = FONT_JA_GOTHIC.size() + 3;
		float listspacing = 2;
		try {
			list = CJKHTMLWorker.parseToList(reader, getCJKStyleSheet());
			for (Iterator i = list.iterator(); i.hasNext();) {
				Element e = (Element) i.next();

				List chunks;
				if ((chunks = e.getChunks()) != null) {
					for (Iterator j = chunks.iterator(); j.hasNext();) {
						Chunk c = (Chunk) j.next();
						c.setFont(FONT_JA_GOTHIC);
					}
				}
				if (e instanceof Paragraph) {
					((Paragraph) e).setLeading(leading);
				} else if (e instanceof PdfPTable) {
					((PdfPTable) e).setSpacingBefore(leading);
				} else if (e instanceof com.lowagie.text.List) {
					com.lowagie.text.List l = (com.lowagie.text.List) e;
					for (Iterator j = l.getItems().iterator(); j.hasNext();) {
						Object listItem = j.next();						
						if(listItem instanceof ListItem){
							ListItem li = (ListItem) listItem;
							li.setSpacingBefore(listspacing);							
						}
					}
				}
			}
		} catch (IOException ioe) {
			log_.error("failed to parse HTML object");
		} 
		return list;
	}

	// ----- [End] PDF converter methods -----

	public static String getStringResult(Object obj){
		
		String ret = null;		
		if(obj instanceof String){
			ret = (String)obj;
		}else if(obj instanceof Chunk){
			Chunk c = (Chunk)obj;
			ret = c.content();
		}else if(obj instanceof Phrase){
			StringBuffer buf = new StringBuffer();
			Phrase p = (Phrase) obj;
			for(int i=0;i<p.size();i++){
				Chunk c = (Chunk)p.get(i);
				buf.append(c.content());
			}
			ret = buf.toString();
		}		
		return ret;
	}
	
	
	/*
	 * Style properties made of 3 parts,
	 *   vertical-or-horizontal (first parts)
	 *   normal-or-simple style (second parts)
	 *   paper size (the last part)
	 * and, it is joined by "-" string.
	 * For example, the default settings
	 *   v-n-A4
	 * indicates, vertical, normal style, A4 PDF document.  
	 */
	public static Rectangle getRectangle(String style){
		
		Rectangle rec = PageSize.A4; // default page size
		if(style.endsWith("B5")){
			rec = PageSize.B5;
		}			
		return (style.startsWith("h-"))? rec.rotate() : rec;		
	}
	
	public static boolean isSimpleStyle(String style){
		return style.indexOf("-s-") != -1;		
	}
	
	
	// ----- [Start] private methods -----
	private static StyleSheet getCJKStyleSheet(){
		StyleSheet cjkstyle = new StyleSheet();
		
		cjkstyle.loadTagStyle("table", "border", "0.5");
		cjkstyle.loadTagStyle("table", "face", FONTNAME_JA_GOTHIC);

		// these two style used in CJKFactoryProperties
		cjkstyle.loadTagStyle("table", "cjk_encoding", FONTNAME_JA_GOTHIC_ENCODING);
		cjkstyle.loadTagStyle("table", "cjk_embeded", "false");

		cjkstyle.loadTagStyle("th", "bgcolor", "silver");
		cjkstyle.loadTagStyle("th", "font", "bold");	
		
		return cjkstyle;
	}
	
}
