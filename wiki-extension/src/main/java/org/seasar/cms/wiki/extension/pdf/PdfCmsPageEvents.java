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
package org.seasar.cms.wiki.extension.pdf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.RomanList;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Event Helper, it will be called on-hook.
 * @see com.lowagie.text.pdf.PdfPageEvent
 * @see PdfPageEventHelper
 * @author someda
 */
public class PdfCmsPageEvents extends PdfPageEventHelper {
		
	private static final float PAGEMARGIN_TOP = 50;
//	private static final float PAGEMARGIN_RIGHT = 50;
	private static final float PAGEMARGIN_BOTTOM = 50;
	private static final float PAGEMARGIN_LEFT = 50;		
	
	private static final String CREATER_APPLICATION = "WebUDA Tuigwaa";
	
	public static final String GENERICTAG_HORIZONTAL = "horizontal";	
	public static final String GENERICTAG_TOC = "toc";
	public static final String GENERICTAG_CLOSETOC = "closetoc";
	
	private static final float size_ = 8;
	private static final String FOOTERPREFIX = "";
	private static final String FOOTERMIDDLE = " / ";
	
	private static final float linewidth_ = 0.5f;
	
	private Font chapterFont_;
	private Font sectionFont_;
	private Font subsectionFont_;
	
	private PdfSpotColor cmykSpc_ = new PdfSpotColor("Tuigwaa", 0.25f, new CMYKColor(0.9f, .2f, .3f, .1f));
	
	private PdfTemplate template_;
	private PdfContentByte cb_;		
	
	private String headerTitle_;
	private float headerX_;
	private float headerY_;
	private float footerX_;
		
	private boolean needHeader = true;
	private boolean needFooter = true;
	private boolean needSidebar = true;
	
	private boolean titleFooter = false;
	
	private List templateList = new ArrayList();
	
	public PdfCmsPageEvents(String headerTitle, Font defaultFont, String style){
		super();
		headerTitle_ = headerTitle;
		chapterFont_ = FontFactory.getFont(PdfUtils.getChapterfontProperties(defaultFont));
		sectionFont_ = FontFactory.getFont(PdfUtils.getSectionfontProperties(defaultFont,2));
		subsectionFont_ = FontFactory.getFont(PdfUtils.getSectionfontProperties(defaultFont,3));
		
		String[] styleColomns = style.split("-");
		if(styleColomns.length > 2){
			String pageStyle = styleColomns[1];
			if("n".equals(pageStyle)){ // normal
				// do nothing
			}else if("s".equals(pageStyle)){ // simple style
				needHeader = false;
				needFooter = false;
				needSidebar = false;
			}else if("r".equals(pageStyle)){ // report style
				needHeader = false;
				needSidebar = false;
				needFooter = false;
				titleFooter = true;
			}			
		}		
	}		
	
	public void onGenericTag(PdfWriter writer, Document document,Rectangle rect,String text){
		
		float y = (rect.top() + rect.bottom())/2;
		if(GENERICTAG_HORIZONTAL.equals(text)){
			cb_.setLineWidth(linewidth_);
			cb_.moveTo(document.left()+20,y);
			cb_.lineTo(document.right()-20,y);
			cb_.stroke();				
		}else if(GENERICTAG_TOC.equals(text)){
			float x = rect.right() + 20;
			cb_.setLineWidth(linewidth_);
			cb_.setLineDash(1.0f,2.0f,0f);
			cb_.moveTo(x,y);
			cb_.lineTo(document.right()-80,y);
			cb_.stroke();			
			PdfTemplate currentPageTemplate_ = cb_.createTemplate(10,10);			
			cb_.addTemplate(currentPageTemplate_,document.right()-70,y-3);
			templateList.add(currentPageTemplate_);
			
			if(needFooter){
				document.resetPageCount();
				needFooter = false;
			}

		}else if(GENERICTAG_CLOSETOC.equals(text)){
			needFooter = true;
//			document.resetPageCount();	
		}
	}
	
	public void onOpenDocument(PdfWriter writer, Document document){
		cb_ = writer.getDirectContent();
		template_ = cb_.createTemplate(50,50);
		
		float width = document.right() - document.left();
		headerX_ = (PAGEMARGIN_LEFT + width - PdfUtils.BASEFONT_GOTHIC.getWidthPoint(headerTitle_,size_))/2;
		headerY_ = document.top() + PAGEMARGIN_TOP/2;
		footerX_ = (PAGEMARGIN_LEFT + width - PdfUtils.BASEFONT_GOTHIC.getWidthPoint(FOOTERPREFIX + " " + FOOTERMIDDLE + " ",size_))/2;
	}
	
	public void onEndPage(PdfWriter writer, Document document) {
		
		cb_.setLineWidth(linewidth_);

		if(needSidebar)
			renderSidebar(writer,document);
		
		if(needHeader)
			renderHeader(writer,document);
		
		if(needFooter){
			renderFooter(writer,document);
		}else if(titleFooter){
			renderTitleFooter(writer,document);
		}
		
	}
	
	public void onCloseDocument(PdfWriter writer, Document document) {
		// fill template with the total page number of this document.
		template_.beginText();
		template_.setFontAndSize(PdfUtils.BASEFONT_MINCHO, size_);
		template_.showText(String.valueOf(writer.getPageNumber() - 1));
		template_.endText();
	}
			
	public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {

		if(titleFooter){
			titleFooter = false;
//			document.resetPageCount();
			document.setPageCount(1);
		}
				
		for (Iterator i = title.getChunks().iterator(); i.hasNext();) {
			Chunk chunk = (Chunk) i.next();
			chunk.setFont(chapterFont_);
		}		
		setPagenumberToTOC(document);		
	}
	
	public void onSection(PdfWriter writer,Document document,float paragraphPosition, int depth, Paragraph title){
		
		StringBuffer buf = new StringBuffer();
		for (Iterator i = title.getChunks().iterator(); i.hasNext();) {
			Chunk chunk = (Chunk) i.next();
			buf.append(chunk.content());
			if(depth > 2){
				chunk.setFont(subsectionFont_);
			}else{
				chunk.setFont(sectionFont_);
			}
		}		
		setPagenumberToTOC(document);
	}
	
	// ----- [Start] private mehots -----
	private void renderHeader(PdfWriter writer,Document document){
		// header lines
		cb_.moveTo(document.left(),document.top()+10);
		cb_.lineTo(document.right(),document.top()+10);
		cb_.stroke();

		// header contents
		cb_.beginText();
		cb_.setFontAndSize(PdfUtils.BASEFONT_GOTHIC,size_);
		cb_.setTextMatrix(headerX_, headerY_);
		cb_.showText(headerTitle_);
		cb_.endText();
	}
	
	private void renderFooter(PdfWriter writer, Document document){		
		// footer lines			
//		int num = writer.getPageNumber();
		int num = document.getPageNumber();
		String text = FOOTERPREFIX + num + FOOTERMIDDLE;
		float len = PdfUtils.BASEFONT_MINCHO.getWidthPoint(text, size_);
		
		cb_.moveTo(document.left(),PAGEMARGIN_BOTTOM - 5);
		cb_.lineTo(document.right(),PAGEMARGIN_BOTTOM - 5);			
		cb_.stroke(); // don't forget !!
		
		// footer contents
		cb_.beginText();
		cb_.setFontAndSize(PdfUtils.BASEFONT_MINCHO,size_);
		cb_.setTextMatrix(footerX_, PAGEMARGIN_BOTTOM/2);
		cb_.showText(text);
		cb_.endText();

		// adding template point into direct content.
		cb_.addTemplate(template_,footerX_+len, PAGEMARGIN_BOTTOM/2);
	}
	
	private void renderTitleFooter(PdfWriter writer, Document document){
		
		String text = RomanList.toRomanLowerCase(writer.getPageNumber());
		
		cb_.moveTo(document.left(),PAGEMARGIN_BOTTOM - 5);
		cb_.lineTo(document.right(),PAGEMARGIN_BOTTOM - 5);	
		cb_.stroke(); // don't forget !!
		
		// footer contents
		cb_.beginText();
		cb_.setFontAndSize(PdfUtils.BASEFONT_MINCHO,size_);
		cb_.setTextMatrix(footerX_, PAGEMARGIN_BOTTOM/2);
		cb_.showText(text);
		cb_.endText();
		
		
	}
	
	
	private void renderSidebar(PdfWriter writer, Document document){
		// sidebar
		cb_.rectangle(0,0,PAGEMARGIN_LEFT/2,document.getPageSize().top());			
		cb_.setColorFill(cmykSpc_,cmykSpc_.getTint());
		cb_.fill();						
		cb_.resetCMYKColorFill();
		
		// sidebar contents
		cb_.beginText();
		cb_.setFontAndSize(PdfUtils.BASEFONT_GOTHIC,size_*1.5f);
		cb_.setColorFill(Color.BLUE);
		cb_.setTextMatrix(0,1,-1,0,PAGEMARGIN_LEFT/3,document.top()/2);
		cb_.showText(CREATER_APPLICATION);			
		cb_.resetRGBColorFill();
		cb_.endText();
	}	

	private void setPagenumberToTOC(Document document){
		if(templateList.size() > 0){
			PdfTemplate currentPageTemplate_ = (PdfTemplate)templateList.remove(0);
			currentPageTemplate_.beginText();
			currentPageTemplate_.setFontAndSize(PdfUtils.BASEFONT_MINCHO,10);
			currentPageTemplate_.showText(document.getPageNumber() + "");
			currentPageTemplate_.endText();			
		}		
	}
		
}
