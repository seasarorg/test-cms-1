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
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.SimpleNode;
import org.seasar.cms.wiki.parser.WikiAlias;
import org.seasar.cms.wiki.parser.WikiAlign;
import org.seasar.cms.wiki.parser.WikiAnnotation;
import org.seasar.cms.wiki.parser.WikiAnyOther;
import org.seasar.cms.wiki.parser.WikiArgs;
import org.seasar.cms.wiki.parser.WikiBlockPlugin;
import org.seasar.cms.wiki.parser.WikiCSVTable;
import org.seasar.cms.wiki.parser.WikiDefineList;
import org.seasar.cms.wiki.parser.WikiDefinedWord;
import org.seasar.cms.wiki.parser.WikiDeleteline;
import org.seasar.cms.wiki.parser.WikiErrors;
import org.seasar.cms.wiki.parser.WikiExcerpt;
import org.seasar.cms.wiki.parser.WikiExplanationWord;
import org.seasar.cms.wiki.parser.WikiFloatAlign;
import org.seasar.cms.wiki.parser.WikiGenerateTree;
import org.seasar.cms.wiki.parser.WikiHeading;
import org.seasar.cms.wiki.parser.WikiHorizontalline;
import org.seasar.cms.wiki.parser.WikiInlinePlugin;
import org.seasar.cms.wiki.parser.WikiInterwiki;
import org.seasar.cms.wiki.parser.WikiLetters;
import org.seasar.cms.wiki.parser.WikiLink;
import org.seasar.cms.wiki.parser.WikiList;
import org.seasar.cms.wiki.parser.WikiListMember;
import org.seasar.cms.wiki.parser.WikiPagename;
import org.seasar.cms.wiki.parser.WikiParagraph;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.parser.WikiPreshaped;
import org.seasar.cms.wiki.parser.WikiSkipToNewline;
import org.seasar.cms.wiki.parser.WikiStrongItalic;
import org.seasar.cms.wiki.parser.WikiSyntaxError;
import org.seasar.cms.wiki.parser.WikiTable;
import org.seasar.cms.wiki.parser.WikiTablecolumn;
import org.seasar.cms.wiki.parser.WikiTablemember;
import org.seasar.cms.wiki.util.GenerateNodeHelper;
import org.seasar.cms.wiki.util.ImageUtils;
import org.seasar.cms.wiki.util.TableNodeUtils;
import org.seasar.cms.wiki.util.VisitorUtils;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Section;
import com.lowagie.text.SplitCharacter;
import com.lowagie.text.Table;
import com.lowagie.text.markup.MarkupParser;
import com.lowagie.text.markup.MarkupTags;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Using parsed wiki node tree, generates PDF or RTF documents. This class
 * totally depends on iText ver. 1.3.
 * 
 * KNOWN ISSUES : (2005/11) 1. Cannot specify Japanese character in annotation
 * contents. Even if setting Japanese font, the result will not contain Japanese
 * Sentence.
 * 
 * @author someda
 */
public class PdfWikiVisitor implements WikiParserVisitor {

	private static final float LIST_INDENT = 10;

	private static final float PAGEMARGIN_TOP = 50;

	private static final float PAGEMARGIN_RIGHT = 50;

	private static final float PAGEMARGIN_BOTTOM = 50;

	private static final float PAGEMARGIN_LEFT = 50;

	private static final float TABLE_PADDING = 3f;

	private static final float TABLE_BORDERWIDTH = 0.5f;

	private static final float TABLE_WIDTH = 100f;

	// Number of depth is now 3 because, current wiki rule allows
	// 3-level headings.
	private static final int SECTION_NUMBEROFDEPTH = 3;

	private static final float SECTION_INDENT = 10;

	private static final String CREATER_APPLICATION = "WebUDA Tuigwaa";

	protected Document doc_;

	protected OutputStream os;

	private List chapterList = null;

	private Section currentSection_ = null;

	private Section lastSection_ = null;

	private Map pushedMap_ = null;

	private Font defaultFont_;

	private float paragraphSpacing_;

	private Log log_ = LogFactory.getLog(getClass());

	private SplitCharacter split = new CJKSplitCharacter();

	public PdfWikiVisitor(OutputStream os) {
		try {
			init();
		} catch (DocumentException de) {
			log_
					.error("failed to initialize the document : "
							+ de.getMessage());
			throw new RuntimeException(de);
		}
	}

	private void init() throws DocumentException {
		Rectangle rec = PdfUtils.getRectangle(PdfUtils.DEFAULT_PDF_PAGESTYLE);
		doc_ = new Document(rec, PAGEMARGIN_LEFT, PAGEMARGIN_RIGHT,
				PAGEMARGIN_TOP, PAGEMARGIN_BOTTOM);

		defaultFont_ = PdfUtils.FONT_JA_MINCHO;
		paragraphSpacing_ = defaultFont_.size();

		DocWriter writer_ = getDocWriter();

		// doc_.addAuthor(resource.getModificationUser());
		doc_.addCreationDate();
		// doc_.addSubject(request_.getPage().getResource().getPath());

		// doc_.addSubject(request_.getPage().getResource().getPath());
		doc_.addCreator(CREATER_APPLICATION);

		doc_.open();
	}

	protected DocWriter getDocWriter() throws DocumentException {
		DocWriter writer = PdfWriter.getInstance(doc_, os);
		String style = PdfUtils.DEFAULT_PDF_PAGESTYLE;
		PdfPageEvent event = new PdfCmsPageEvents("" /* pagePath */,
				defaultFont_, style);
		((PdfWriter) writer).setPageEvent(event);
		return writer;
	}

	public Object visit(SimpleNode node, Object data) {
		return null;
	}

	public Object visit(WikiSyntaxError node, Object data) {
		return getError(node.letter);
	}

	public Object visit(WikiSkipToNewline node, Object data) {
		Chunk c = new Chunk(node.letter, defaultFont_);
		c.setSplitCharacter(split);
		return c;
	}

	/**
	 * Main method, all other method in this class should not be called.
	 */
	public Object visit(WikiGenerateTree node, Object data) {

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Object o = node.jjtGetChild(i).jjtAccept(this, data);
			addDocument(o, node.jjtGetChild(i));
		}

		/**
		 * NOTE: iText Document#add method writes the contents on-the-fly to
		 * OutputStream, thus, using chapterList to avoid null contents
		 * appeared.
		 */
		if (chapterList != null) {
			int n = 0;
			for (Iterator i = chapterList.iterator(); i.hasNext();) {
				Element e = (Element) i.next();
				List list = null;
				if (pushedMap_ != null)
					list = (List) pushedMap_.get(new Integer(n));

				try {
					doc_.add(e);
					if (list != null) {
						for (Iterator j = list.iterator(); j.hasNext();) {
							Element elm = (Element) j.next();
							doc_.add(elm);
						}
					}
				} catch (DocumentException de) {
					de.printStackTrace();
				}
				n++;
			}
		}

		doc_.close();
		return null;
	}

	private void addDocument(Object obj, Node node) {

		try {
			if (currentSection_ != null && !(node instanceof WikiHeading)) {
				if (obj != null) {
					try {
						currentSection_.add(obj);
					} catch (ClassCastException cce) {
						log_.error("couldn't add " + obj
								+ " to current section caused by "
								+ cce.getMessage());
					}
				}
			} else {
				if (obj != null) {
					try {
						doc_.add((Element) obj);
					} catch (ClassCastException cce) {
						log_.error("couldn't add " + obj
								+ " to document caused by " + cce.getMessage());
					}
				}
			}
		} catch (DocumentException de) {
			de.printStackTrace();
		}
	}

	private void pushObject(Element e) {
		if (pushedMap_ == null)
			pushedMap_ = new HashMap();

		if (currentSection_ == null) {
			try {
				doc_.add(e);
			} catch (DocumentException de) {
				log_.error("failed to add element " + de.getMessage());
			}
		} else {
			List list;
			Integer n = new Integer(chapterList.size() - 1);
			if ((list = (List) pushedMap_.get(n)) == null) {
				list = new ArrayList();
				pushedMap_.put(n, list);
			}
			list.add(e);
		}
	}

	public Object visit(WikiParagraph node, Object data) {
		Paragraph p = new Paragraph();
		p.setLeading(20f);
		p.setFirstLineIndent(defaultFont_.size());
		p.setSpacingBefore(paragraphSpacing_);
		p.setSpacingAfter(paragraphSpacing_);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Object o = node.jjtGetChild(i).jjtAccept(this, data);
			if (o != null)
				p.add(o);
		}
		return p;
	}

	public Object visit(WikiExcerpt node, Object data) {

		Properties props = PdfUtils.getDefaultfontProperties(defaultFont_);
		props.setProperty(ElementTags.STYLE, MarkupTags.CSS_ITALIC);

		Paragraph p = new Paragraph(props);
		p.setIndentationLeft(20 * node.level);
		p.setSpacingAfter(paragraphSpacing_);
		p.setSpacingBefore(paragraphSpacing_);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			// if child is paragraph, not generate <p> tag
			if (node.jjtGetChild(i) instanceof WikiParagraph) {
				WikiParagraph c = (WikiParagraph) node.jjtGetChild(i);
				for (int j = 0; j < c.jjtGetNumChildren(); j++) {
					Object o = c.jjtGetChild(j).jjtAccept(this, data);
					if (o != null)
						p.add(o);
				}

			} else {
				Object o = node.jjtGetChild(i).jjtAccept(this, data);
				if (o != null)
					p.add(o);
			}
		}
		return p;
	}

	public Object visit(WikiList node, Object data) {

		int curtype = 0;
		int curlevel = 0;
		int pretype = 0;
		int prelevel = 0;
		com.lowagie.text.List[] state = new com.lowagie.text.List[3];

		Element e = new Paragraph();
		com.lowagie.text.List curlist = null;
		com.lowagie.text.List parentlist = null;

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {

			if (node.jjtGetChild(i) instanceof WikiListMember) {
				WikiListMember child = (WikiListMember) node.jjtGetChild(i);

				curtype = child.type;
				curlevel = child.level;

				// upward level change
				if (curlevel > prelevel) {
					parentlist = curlist;

					curlist = startList(curtype);
					state[curlevel - 1] = curlist;

					if (parentlist != null) {
						parentlist.add(curlist);
					} else {
						parentlist = curlist;
						((Paragraph) e).add(curlist);
					}
				}

				// downward level change
				if (curlevel < prelevel) {

					if (state[curlevel - 1] != null
							&& state[curlevel - 1].size() > 0
							&& isSametype(state[curlevel - 1], curtype)) {
						curlist = state[curlevel - 1];
					} else {
						curlist = startList(curtype);
						for (int j = curlevel - 2; j >= 0; j--) {
							if (state[j] != null && state[j].size() > 0) {
								parentlist = state[j];
								parentlist.add(curlist);
								break;
							}
						}
						if (curlevel == 1) {
							((Paragraph) e).add(curlist);
						}
					}
				}

				// not level change but type change
				if (curlevel == prelevel && curtype != pretype) {
					curlist = startList(curtype);

					if (curlevel == 1) {
						((Paragraph) e).add(curlist);
					} else {
						parentlist.add(curlist);
					}
				}

				// change state
				if (curlevel != prelevel || curtype != pretype) {
					pretype = curtype;
					prelevel = curlevel;
				}
				curlist.add(child.jjtAccept(this, data));
			}
		}
		return e;
	}

	private com.lowagie.text.List startList(int type) {
		com.lowagie.text.List e = null;
		if (type == GenerateNodeHelper.LIST_TYPE_NORMAL) {
			e = new com.lowagie.text.List(false, LIST_INDENT);
		} else if (type == GenerateNodeHelper.LIST_TYPE_NUMERICAL) {
			e = new com.lowagie.text.List(true, LIST_INDENT);
		}
		return e;
	}

	private boolean isSametype(com.lowagie.text.List list, int type) {
		return (!list.isNumbered() && type == GenerateNodeHelper.LIST_TYPE_NORMAL)
				|| (list.isNumbered() && type == GenerateNodeHelper.LIST_TYPE_NUMERICAL);
	}

	public Object visit(WikiListMember node, Object data) {
		ListItem e = new ListItem();
		e.setIndentationLeft(LIST_INDENT);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			e.add(node.jjtGetChild(i).jjtAccept(this, data));
		}
		return e;
	}

	public Object visit(WikiDefineList node, Object data) {
		Paragraph p = new Paragraph();
		p.setSpacingBefore(5f);
		p.setSpacingAfter(5f);
		p.setIndentationLeft(5f);
		p.add(Chunk.NEWLINE);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Object o = node.jjtGetChild(i).jjtAccept(this, data);
			if (o != null)
				p.add(o);
		}
		return p;
	}

	public Object visit(WikiDefinedWord node, Object data) {

		Phrase p = null;
		if (node.jjtGetNumChildren() > 0) {
			Properties props = PdfUtils.getDefaultfontProperties(defaultFont_);
			props.setProperty(ElementTags.STYLE, MarkupTags.CSS_BOLD);
			p = new Phrase(props);
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Object o = node.jjtGetChild(i).jjtAccept(this, data);
				if (o != null)
					p.add(o);
			}
			p.add(Chunk.NEWLINE);
		}
		return p;
	}

	public Object visit(WikiExplanationWord node, Object data) {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(5f);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {

			Node child = node.jjtGetChild(i);
			if (child instanceof WikiParagraph) {
				for (int j = 0; j < child.jjtGetNumChildren(); j++) {
					Object o = child.jjtGetChild(j).jjtAccept(this, data);
					if (o != null)
						p.add(o);
				}
			} else {
				Object o = node.jjtGetChild(i).jjtAccept(this, data);
				if (o != null)
					p.add(o);
			}
		}
		p.add(Chunk.NEWLINE);
		return p;
	}

	public Object visit(WikiPreshaped node, Object data) {

		Table p = null;
		try {
			p = new Table(1);
			p.setPadding(TABLE_PADDING);
			p.setBorderWidth(TABLE_BORDERWIDTH);
			p.setWidth(TABLE_WIDTH);
			p.disableBorderSide(Rectangle.LEFT);
			p.disableBorderSide(Rectangle.RIGHT);
			p.endHeaders();
			Cell c = new Cell();
			c.disableBorderSide(Rectangle.LEFT);
			c.disableBorderSide(Rectangle.RIGHT);

			Paragraph ph = new Paragraph(PdfUtils
					.getDefaultfontProperties(defaultFont_));

			float indent = 0f;
			if (currentSection_ != null) { // important
				indent = (currentSection_.depth() - 1) * SECTION_INDENT;
			}

			if (node.jjtGetParent() instanceof Paragraph) {
				indent += ((Paragraph) node.jjtGetParent()).indentationLeft();
			}

			if (indent > 0f)
				ph.setIndentationLeft(indent);

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				ph.add(node.jjtGetChild(i).jjtAccept(this, data));
				ph.add(Chunk.NEWLINE);
			}
			c.add(ph);
			p.addCell(c);
		} catch (BadElementException bee) {
			bee.printStackTrace();
		}
		return p;
	}

	public Object visit(WikiTable node, Object data) {
		return processTable(node, data);
	}

	private Object processTable(SimpleNode node, Object data) {

		TableNodeUtils.prepareWikiTable(node, data);
		int prenum = 0;
		Table t = null;

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {

			int colnum = node.jjtGetChild(i).jjtGetNumChildren();

			if (node.jjtGetChild(i) instanceof WikiTablemember) {

				if (colnum != prenum) {

					if (t != null) {
						addDocument(t, node);
					}

					try {
						t = new Table(colnum);
						t.setPadding(TABLE_PADDING);
						t.setBorderWidth(TABLE_BORDERWIDTH);
						t.setWidth(TABLE_WIDTH);
						t.setCellsFitPage(true);
						t.setOffset(10f);
						// addDocument(t,node);
						// pushObject(t);
					} catch (BadElementException bee) {
						// in case colnum is not 1
						bee.printStackTrace();
					}
				}
			} else { // in case WikiError
				prenum = 0;
				continue;
			}
			prenum = colnum;
			node.jjtGetChild(i).jjtAccept(this, t);
		}
		return t;
	}

	public Object visit(WikiTablecolumn node, Object data) {

		Cell cell = null;
		if (!node.iscolspan && !node.isrowspan) {
			cell = new Cell();
			Properties props = (data instanceof Properties) ? (Properties) data
					: PdfUtils.getDefaultfontProperties(defaultFont_);
			props.setProperty(ElementTags.SIZE, "10.0");

			if (node.align != null)
				cell.setHorizontalAlignment(getAlign(node.align));

			String bgcolor = null;
			if ((bgcolor = (String) props.remove(ElementTags.BACKGROUNDCOLOR)) != null) {
				cell.setBackgroundColor(MarkupParser.decodeColor(bgcolor));
			}

			if (node.bgcolor != null)
				cell.setBackgroundColor(ColorUtils.getColorByString(
						node.bgcolor, false));

			if (node.color != null) {
				Color c = ColorUtils.getColorByString(node.color, true);
				if (c != null) {
					props.setProperty(ElementTags.RED, String.valueOf(c
							.getRed()));
					props.setProperty(ElementTags.GREEN, String.valueOf(c
							.getGreen()));
					props.setProperty(ElementTags.BLUE, String.valueOf(c
							.getBlue()));
				}
			}

			if (node.size != null)
				props.setProperty(ElementTags.SIZE, node.size);

			if (node.colspannum > 0)
				cell.setColspan(++node.colspannum);

			if (node.rowspannum > 0) {
				cell.setRowspan(++node.rowspannum);
			}

			try {
				Phrase p = getChildPhrase(node, props, 0);
				Paragraph ph = new Paragraph(p);

				float indent = 0f;
				if (currentSection_ != null) { // important
					indent = (currentSection_.depth() - 1) * SECTION_INDENT;
				}

				if (indent > 0)
					ph.setIndentationLeft(indent);

				cell.addElement(ph);
			} catch (BadElementException bee) {
				bee.printStackTrace();
			}
		}
		return cell;
	}

	public Object visit(WikiTablemember node, Object data) {

		if (data instanceof Table) {
			Table t = (Table) data;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Properties p = PdfUtils.getDefaultfontProperties(defaultFont_);
				if (node.type == GenerateNodeHelper.TABLE_TYPE_HEADER) {
					p.setProperty(ElementTags.STYLE, MarkupTags.CSS_BOLD);
					p.setProperty(ElementTags.BACKGROUNDCOLOR, "#C0C0C0");
				}

				Object o = node.jjtGetChild(i).jjtAccept(this, p);
				if (o != null) {
					t.addCell((Cell) o);
				}
			}
		}
		return null;
	}

	public Object visit(WikiCSVTable node, Object data) {
		return processTable(node, data);
	}

	public Object visit(WikiHeading node, Object data) {

		Element e = null;
		Paragraph title;

		if (node.level == 1 || currentSection_ == null) {
			title = new Paragraph(PdfUtils
					.getChapterfontProperties(defaultFont_));
			title.setLeading(20f);
			title.setSpacingAfter(40f);
			title.add(getChildPhrase(node, data, 0));
			if (chapterList == null)
				chapterList = new ArrayList();

			// should add Chapter object to Document object at creating
			// new Chapter object to avoid memory starvation for large document
			// ??
			e = new Chapter(title, chapterList.size() + 1);
			currentSection_ = (Section) e;
			chapterList.add(e);
		} else {
			title = new Paragraph(PdfUtils.getSectionfontProperties(
					defaultFont_, node.level));
			title.setLeading(20f);
			title.add(getChildPhrase(node, data, 0));
			Section sec = null;
			if (node.level == 2) {
				sec = ((Section) chapterList.get(chapterList.size() - 1))
						.addSection(SECTION_INDENT, title,
								SECTION_NUMBEROFDEPTH);
			} else { // in case level-3
				int currentDepth = currentSection_.depth();
				if (currentDepth == SECTION_NUMBEROFDEPTH) {
					sec = lastSection_.addSection(SECTION_INDENT, title,
							SECTION_NUMBEROFDEPTH);
				} else {
					lastSection_ = currentSection_;
					sec = currentSection_.addSection(SECTION_INDENT, title,
							SECTION_NUMBEROFDEPTH);
				}
			}
			if (sec != null)
				currentSection_ = sec;
		}
		return null;
	}

	public Object visit(WikiAlign node, Object data) {
		Paragraph p = new Paragraph();
		String align = node.image.substring(0, node.image.length() - 1);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Object o = node.jjtGetChild(i).jjtAccept(this, data);
			if (o != null)
				p.add(o);
		}
		p.setAlignment(getAlign(align));
		return p;
	}

	public Object visit(WikiFloatAlign node, Object data) {
		Paragraph p = new Paragraph();
		String align = node.image.substring(1, node.image.length() - 1);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Object o = node.jjtGetChild(i).jjtAccept(this, data);
			if (o != null)
				p.add(o);
		}
		p.setAlignment(getAlign(align));
		return p;
	}

	public Object visit(WikiHorizontalline node, Object data) {

		// PDF dependent to use generic tag, though it is rather smart than
		// using Chunk#setHorizontalScaling with "-" character.
		Chunk c = new Chunk("\u00a0");
		c.setGenericTag(PdfCmsPageEvents.GENERICTAG_HORIZONTAL);
		Paragraph p = new Paragraph(c);
		return p;
	}

	public Object visit(WikiBlockPlugin node, Object data) {

		String name = node.name;
		boolean ispluginused = false;
		Object obj = null;

		// PluginRequest prequest = VisitorUtils.createPluginRequest(node);
		// String childStr = getChildAsString(node, data, 0);
		// if (childStr != null && !Constants.LINEBREAK_CODE.equals(childStr)) {
		// prequest.setChild(GenerateNodeHelper.deleteParenthesis(childStr, "{",
		// "}"));
		// }
		//
		// if (config_.isPluginLoaded(name)) {
		// Plugin plugin = config_.getPlugin(name);
		// try {
		// obj = plugin.service(request_, response_, prequest);
		// ispluginused = true;
		// } catch (PluginException pe) {
		// log_.error(pe.getMessage());
		// } catch (Exception e) {
		// e.printStackTrace();
		// // some exception handling framework needed
		// }
		// }

		if (obj instanceof List && !(obj instanceof Phrase)) { // in case
			// VisitorUtils#parseHTMLtoPdf
			// being used
			List list = (List) obj;
			for (Iterator i = list.iterator(); i.hasNext();) {
				Element e = (Element) i.next();

				if (e instanceof PdfPTable) {
					pushObject(e);
				} else {
					addDocument(e, node);
				}
			}
			return null; // important !!
		}

		// if (!ispluginused)
		// obj = new Chunk("#" + prequest.toString(), defaultFont_);
		return obj;
	}

	public Object visit(WikiLetters node, Object data) {

		Element e = null;

		if (node.isURL && !ImageUtils.isImage(node.letter)) {
			e = new Anchor(node.letter);
			((Anchor) e).setReference(node.letter);
		}

		if (node.isAnchor) {
			String id = (node.letter.startsWith("#")) ? node.letter
					.substring(1) : node.letter;
			e = new Chunk("\u00a0");// normal space will be ignored and
			((Chunk) e).setLocalDestination(id);
		}

		if (node.isWikiname) {
			// try {
			// if (ctx.isPageExist(node.letter, request_)) {
			// URL url = ctx.getURLByName(node.letter, request_);
			// e = new Anchor(node.letter);
			// ((Anchor) e).setReference(url.toString());
			// }
			// } catch (TgwSecurityException tse) {
			// e = new Chunk(node.letter, defaultFont_);
			// }

		}

		if (node.isNewline) {
			e = Chunk.NEWLINE;
		}

		if (node.isURL && ImageUtils.isImage(node.letter)) {
			try {
				Image img = Image.getInstance(new URL(node.letter));
				img.setAlignment(Image.MIDDLE | Image.TEXTWRAP);
				return img;
			} catch (BadElementException bee) {
				bee.printStackTrace();
			} catch (MalformedURLException mue) {
				mue.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		if (e == null) {
			if (data instanceof Properties) {
				Properties props = PdfUtils
						.getDefaultfontProperties(defaultFont_);
				props.putAll((Properties) data);
				Font f = FontFactory.getFont(props);
				e = new Chunk(node.letter, f);
			} else {
				e = new Chunk(node.letter, defaultFont_);
				((Chunk) e).setSplitCharacter(split);
			}
		}
		return e;
	}

	public Object visit(WikiStrongItalic node, Object data) {

		Properties props = PdfUtils.getDefaultfontProperties(defaultFont_);
		StringBuffer buf = new StringBuffer();
		if (VisitorUtils.isBold(node))
			buf.append(MarkupTags.CSS_BOLD);
		if (VisitorUtils.isItalic(node))
			buf.append(MarkupTags.CSS_ITALIC);

		String style = buf.toString();
		if (style != null && !style.equals(""))
			props.setProperty(ElementTags.STYLE, buf.toString());

		// font-style propagation
		Phrase p = new Phrase(props);

		String pre = VisitorUtils.getAppendString(node, true);
		String post = VisitorUtils.getAppendString(node, false);

		if (pre != null)
			p.add(new Chunk(pre, defaultFont_));
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			p.add(node.jjtGetChild(i).jjtAccept(this, data));
		}
		if (post != null)
			p.add(new Chunk(post, defaultFont_));
		return p;
	}

	public Object visit(WikiDeleteline node, Object data) {

		Properties props = PdfUtils.getDefaultfontProperties(defaultFont_);
		props.setProperty(ElementTags.STYLE, MarkupTags.CSS_LINETHROUGH);

		// font-style propagation
		Phrase p = new Phrase(props);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			p.add(node.jjtGetChild(i).jjtAccept(this, data));
		}
		return p;
	}

	public Object visit(WikiAnnotation node, Object data) {

		Properties p = PdfUtils.getDefaultfontProperties(defaultFont_);
		p.setProperty(ElementTags.TITLE, "annotation" + node.num);
		p.setProperty(ElementTags.CONTENT, getChildAsString(node, data, 0));
		Annotation a = new Annotation(p);
		// Annotation a = new Annotation("annotation" +
		// node.num,getChildAsString(node,data,0));
		// a.setMarkupAttributes(getDefaultfontProperties());
		return a;
	}

	// not implemented
	public Object visit(WikiInterwiki node, Object data) {
		return new Chunk("[[" + node.image + "]]", defaultFont_);
	}

	public Object visit(WikiLink node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.LINK_DELIMITER);
		Anchor a = new Anchor(s[0], defaultFont_);
		a.setReference(s[1]);
		return a;
	}

	public Object visit(WikiAlias node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ALIAS_DELIMITER);

		Element e = null;
		if (node.islink) { // URL case, same as WikiLink
			e = new Anchor(s[0], defaultFont_);
			((Anchor) e).setReference(s[1]);
		} else {
			String[] t = GenerateNodeHelper.split(s[1],
					GenerateNodeHelper.ANCHOR_MARK);
			// try {
			// if (t[0] == null || t[0].equals("")) {// only anchors, local
			// // reference
			// e = new Anchor(s[0], defaultFont_);
			// ((Anchor) e).setReference(GenerateNodeHelper.ANCHOR_MARK + t[1]);
			// } else if (ctx.isPageExist(t[0], request_)) {
			// URL url = ctx.getURLByName(t[0], request_);
			// String ref = url.toString();
			// if (t[1] != null && !t[1].equals(""))
			// ref += GenerateNodeHelper.ANCHOR_MARK + t[1];
			//
			// e = new Anchor(s[0], defaultFont_);
			// ((Anchor) e).setReference(ref);
			// } else {
			// e = new Chunk(s[0], defaultFont_);
			// }
			// } catch (TgwSecurityException tse) {
			// e = new Chunk(s[0], defaultFont_);
			//
			// }
		}
		return e;
	}

	public Object visit(WikiPagename node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ANCHOR_MARK);
		Element e = null;
		// try {
		// if (ctx.isPageExist(s[0], request_)) {
		// URL url = config_.getWikiContext().getURLByName(s[0], request_);
		// e = new Anchor(s[0], defaultFont_);
		// String ref = url.toString();
		// if (s[1] != null && !s[1].equals(""))
		// ref = ref + GenerateNodeHelper.ANCHOR_MARK + s[1];
		// ((Anchor) e).setReference(ref);
		// }
		// } catch (TgwSecurityException tse) {
		// e = new Chunk(s[0], defaultFont_);
		// }
		return e;
	}

	public Object visit(WikiInlinePlugin node, Object data) {
		String name = node.name;
		boolean ispluginused = false;
		int inlinestart = 0;
		Object obj = null;

		// PluginRequest prequest = VisitorUtils.createPluginRequest(node);
		// if (prequest.getArgs() != null)
		// inlinestart = 1;
		// Phrase childphrase = getChildPhrase(node, data, inlinestart);
		// prequest.setChild(childphrase);
		//
		// if (config_.isPluginLoaded(name)) {
		// Plugin plugin = config_.getPlugin(name);
		// try {
		// obj = plugin.service(request_, response_, prequest);
		// ispluginused = true;
		// } catch (PluginException pe) {
		// log_.error(pe.getMessage());
		// } catch (Exception e) {
		// e.printStackTrace();
		// // some exception handling framework needed
		// }
		// }

		if (obj instanceof List && !(obj instanceof Phrase)) { // in case
			// VisitorUtils#parseHTMLtoPdf
			// being used
			List list = (List) obj;
			for (Iterator i = list.iterator(); i.hasNext();) {
				Element e = (Element) i.next();

				if (e instanceof PdfPTable) {
					pushObject(e);
				} else {
					addDocument(e, node);
				}
			}
			obj = new Chunk("\u00a0");
		}

		if (!ispluginused) {
			if ("amp".equals(name)) {
				obj = new Chunk("\u0026", defaultFont_);

			} else {
				// obj = new Chunk("&" + prequest.toString() + ";",
				// defaultFont_);
			}
		}
		return obj;
	}

	public Object visit(WikiArgs node, Object data) {
		return null;
	}

	public Object visit(WikiErrors node, Object data) {
		return getError(node.letter);
	}

	private Phrase getChildPhrase(SimpleNode node, Object data, int idx) {
		Phrase p = new Phrase();
		for (int i = idx; i < node.jjtGetNumChildren(); i++) {
			p.add(node.jjtGetChild(i).jjtAccept(this, data));
		}
		return p;
	}

	private String getChildAsString(SimpleNode node, Object data, int idx) {
		StringBuffer buf = new StringBuffer();
		for (int i = idx; i < node.jjtGetNumChildren(); i++) {
			Object o = node.jjtGetChild(i).jjtAccept(this, data);
			if (o instanceof Chunk) {
				buf.append(((Chunk) o).content());
			} else if (o instanceof Phrase) {
				for (Iterator itr = ((Phrase) o).getChunks().iterator(); itr
						.hasNext();) {
					buf.append(((Chunk) itr.next()).content());
				}
			} else {
			}
		}
		return buf.toString();
	}

	// ----- [Start] static methods -----

	private static int getAlign(String s) {
		int align = Element.ALIGN_LEFT; // default

		String ls = s.toLowerCase().trim();
		if (ls.startsWith("left")) {
			align = Element.ALIGN_LEFT;
		} else if (ls.equals("center")) {
			align = Element.ALIGN_CENTER;
		} else if (ls.startsWith("right")) {
			align = Element.ALIGN_RIGHT;
		}
		return align;
	}

	private static Chunk getError(String str) {
		return new Chunk(str, PdfUtils.FONT_ERROR);
	}

	public Object visit(WikiAnyOther node, Object data) {
		return new Chunk(node.letter, defaultFont_);
	}

	// ----- [End] static methods -----
}
