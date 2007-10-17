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
package org.seasar.cms.wiki.renderer;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.plugin.PluginExecuter;
import org.seasar.cms.wiki.engine.plugin.WikiPageLink;
import org.seasar.cms.wiki.factory.WikiBodyEvaluator;
import org.seasar.cms.wiki.factory.WikiPageLinkFactory;
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
import org.seasar.cms.wiki.util.NodeUtils;
import org.seasar.cms.wiki.util.VisitorUtils;
import org.seasar.cms.wiki.util.WikiStringUtils;

/**
 * @author someda
 */
public class HtmlVisitor implements WikiParserVisitor {

	private static final String ANNOTATION_TEXT_PREFIX = "notetext_";

	private static final String ANNOTATION_FOOT_PREFIX = "notefoot_";

	private static final String ANNOTATION_NOTE_CLASS = "note_super";

	private static final String NOTEXIST_CLASS = "notexist";

	private static final String FONTCOLOR_ERROR = "red";

	private HtmlWriter writer;

	private WikiContext context;

	public HtmlVisitor(WikiContext context, Writer writer) {
		this.context = context;
		this.writer = new HtmlWriter(writer);
	}

	public Object visit(SimpleNode node, Object data) {
		return null;
	}

	public Object visit(WikiSyntaxError node, Object data) {
		return appendError(node.letter);
	}

	public Object visit(WikiSkipToNewline node, Object data) {
		writer.body(WikiStringUtils.escape(node.letter));
		return null;
	}

	/**
	 * ルートノードの解析
	 */
	public Object visit(WikiGenerateTree node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		processAnnotations(node, data);
		return null;
	}

	public Object visit(WikiParagraph node, Object data) {
		processBlockElement("p", node, data);
		return null;
	}

	public Object visit(WikiExcerpt node, Object data) {

		boolean isTagNeed = VisitorUtils.isExcerptStartNeeded(node);

		if (isTagNeed) {
			writer.start("blockquote");
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			// if child is paragraph, not generate <p> tag
			if (node.jjtGetChild(i) instanceof WikiParagraph) {
				WikiParagraph p = (WikiParagraph) node.jjtGetChild(i);
				processChildren(p, data);
			} else {
				node.jjtGetChild(i).jjtAccept(this, data);
			}
		}
		changeTabAndNewlineState(true);
		if (isTagNeed)
			writer.end();
		return null;
	}

	public Object visit(WikiList node, Object data) {

		int curtype = 0;
		int curlevel = 0;
		int pretype = 0;
		int prelevel = 0;
		int[] level = { 0, 0, 0 };

		for (WikiListMember child : NodeUtils.find(node, WikiListMember.class)) {

			curtype = child.type;
			curlevel = child.level;

			// change state
			if (curlevel != prelevel || curtype != pretype) {
				if (curlevel > prelevel) { // upward level change
					startListTag(curtype);
				} else if (curlevel < prelevel) { // downward level change
					for (int j = curlevel; j < prelevel; j++) {
						if (level[j] != 0) {
							writer.end();
							level[j] = 0;
						}
					}
					if (level[curlevel - 1] != curtype) {
						writer.end();
						startListTag(curtype);
					}
				} else { // if (curtype != pretype) {
					// not level change but type change
					writer.end();
					startListTag(curtype);
				}
				level[curlevel - 1] = curtype;
			}

			pretype = curtype;
			prelevel = curlevel;

			child.jjtAccept(this, data);
		}

		// close all list related tag
		for (int i = 0; i < level.length; i++) {
			if (level[i] != 0) {
				writer.end();
			}
		}
		return null;
	}

	public Object visit(WikiDefineList node, Object data) {
		processBlockElement("dl", node, data);
		return null;
	}

	public Object visit(WikiPreshaped node, Object data) {
		writer.start("pre");
		changeTabAndNewlineState(false);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
			writer.body("\n");
		}
		writer.enableLineBreak();
		writer.end();
		writer.enableTab();
		return null;
	}

	public Object visit(WikiTable node, Object data) {
		processTable(node, data);
		return null;
	}

	public Object visit(WikiTablemember node, Object data) {
		processElement("tr", node, data);
		return null;
	}

	public Object visit(WikiTablecolumn node, Object data) {
		if (!node.iscolspan && !node.isrowspan) {
			Map<String, String> attrs = new HashMap<String, String>();
			String str = getChildString(node, data, 0);

			StringBuilder style = new StringBuilder();
			if (node.align != null) {
				style.append("text-align:" + node.align + ";");
			}
			if (node.bgcolor != null) {
				style.append("background-color:" + node.bgcolor + ";");
			}
			if (node.color != null) {
				style.append("color:" + node.color + ";");
			}
			if (node.size != null) {
				style.append("font-size:" + node.size + ";");
			}
			String styleString = style.toString();
			if (styleString.length() > 0) {
				attrs.put("style", styleString);
			}

			if (node.colspannum > 0) {
				attrs.put("colspan", ++node.colspannum + "");
			}
			if (node.rowspannum > 0) {
				attrs.put("rowspan", ++node.rowspannum + "");
			}

			writer.start("td").attrs(attrs).body(str).end();
		}
		return null;
	}

	public Object visit(WikiCSVTable node, Object data) {
		processTable(node, data);
		return null;
	}

	public Object visit(WikiHeading node, Object data) {
		String childstr = getChildString(node, data, 0);
		// Resource resource = request_.getPage().getResource();
		// String lockuser =
		// resource.getProperty(CmsConstants.PROPERTY_LOCKUSER);
		// if (lockuser == null) {
		// String anchorid = VisitorUtils.getAnchorId(node);
		// if (anchorid != null && !"".equals(anchorid)) {
		// String pagename = resource.getPath();
		// if (hasCreatePermission) {
		// try {
		// URL url = ctx.getEditPageURL(pagename, request_);
		// String href = url.toString();
		// href += "&amp;" + Constants.PARAM_PAGESECTIONID + "="
		// + anchorid.substring(1);
		// href += request_.getURLEncodedQuery();
		// HtmlBuffer anchorbuf = new HtmlBuffer();
		// anchorbuf.setNewline(false);
		// anchorbuf.setTab(false);
		// appendSuper(anchorbuf, null, "anchor_super", href,
		// " edit");
		// childstr += anchorbuf.toString();
		// } catch (TgwSecurityException tse) {
		// hasCreatePermission = false;
		// }
		// }
		// }
		// }
		writer.appendHeading(node.level + 1, childstr);
		return null;
	}

	public Object visit(WikiAlign node, Object data) {
		String align = node.image.toLowerCase().substring(0,
				node.image.length() - 1);
		processBlockElement("div", "align", align, node, data);
		return null;
	}

	public Object visit(WikiFloatAlign node, Object data) {
		String align = node.image.toLowerCase().substring(1,
				node.image.length() - 1);
		int idx = 0;
		String widthStr = null;
		if ((idx = align.indexOf("(")) != -1) {
			widthStr = GenerateNodeHelper.deleteParenthesis(align, "(", ")");
			align = align.substring(0, idx);
		}
		String style = "float:" + align + ";";
		if (widthStr != null) {
			style += "width: " + widthStr + "px;";
		}

		processBlockElement("div", "style", style, node, data);
		return null;
	}

	public Object visit(WikiHorizontalline node, Object data) {
		writer.start("hr").end();
		return null;
	}

	public Object visit(WikiBlockPlugin node, Object data) {
		String args[] = VisitorUtils.getArgs(node);
		int inlinestart = 0;
		if (args != null) {
			inlinestart = 1;
		}
		String child = getChildString(node, data, inlinestart);
		PluginExecuter executer = context.getEngine().getPluginExecuter();
		executer.block(context, node.name, args, child);
		return null;
	}

	public Object visit(WikiLetters node, Object data) {
		changeTabAndNewlineState(false);
		String letters = (node.isHTMLescape) ? WikiStringUtils
				.escape(node.letter) : node.letter;

		WikiBodyEvaluator evaluator = context.getEngine().getBodyEvaluator();
		String body = evaluator.eval(context, letters);
		String letter = node.letter;

		if (node.isEmail) {
			writer.appendAnchor("mailto:" + node.letter, body);
		} else if (node.isURL) {
			if (ImageUtils.isImage(node.letter)) {
				writer.start("a").attr("href", letter);
				writer.start("img").attr("src", letter).attr("alt", body);
				writer.end().end();
			} else {
				writer.appendAnchor(node.letter, body);
			}
		} else if (node.isAnchor) {
			String id = (letter.startsWith("#")) ? letter.substring(1) : letter;
			appendSuper(writer, id, "anchor_super", body, "&nbsp;");
		} else if (node.isWikiname) {
			processLink(node.letter, body, null);
		} else if (node.isNewline) {
			writer.appendBr();
		} else {
			writer.body(body);
		}
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiStrongItalic node, Object data) {

		int tag = 0;
		changeTabAndNewlineState(false);

		if (VisitorUtils.isBold(node)) {
			writer.start("strong");
			tag++;
		}

		if (VisitorUtils.isItalic(node)) {
			writer.start("em");
			tag++;
		}

		String pre = VisitorUtils.getAppendString(node, true);
		String post = VisitorUtils.getAppendString(node, false);

		if (pre != null) {
			writer.body(pre);
		}

		processChildren(node, data);

		changeTabAndNewlineState(false);

		if (post != null) {
			writer.body(post);
		}
		for (int i = 0; i < tag; i++) {
			writer.end();
		}
		return null;
	}

	public Object visit(WikiListMember node, Object data) {
		processElement("li", node, data);
		return null;
	}

	public Object visit(WikiDefinedWord node, Object data) {
		processElement("dt", node, data);
		return null;
	}

	public Object visit(WikiExplanationWord node, Object data) {
		processElement("dd", node, data);
		return null;
	}

	public Object visit(WikiDeleteline node, Object data) {
		processElement("del", node, data);
		return null;
	}

	public Object visit(WikiAnnotation node, Object data) {
		String id = ANNOTATION_TEXT_PREFIX + node.num;
		String href = "#" + ANNOTATION_FOOT_PREFIX + node.num;
		String clazz = ANNOTATION_NOTE_CLASS;
		String body = "*" + node.num;

		writer.disableLineBreak();
		writer.start("a").attr("id", id).attr("class", clazz)
				.attr("href", href).body(body).end();
		writer.enableLineBreak();
		return null;
	}

	// interwiki not implemented...
	public Object visit(WikiInterwiki node, Object data) {
		writer.body("[[" + node.image + "]]");
		return null;
	}

	public Object visit(WikiLink node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.LINK_DELIMITER);
		writer.appendAnchor(s[1], s[0], true);
		return null;
	}

	public Object visit(WikiAlias node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ALIAS_DELIMITER);
		changeTabAndNewlineState(false);
		if (node.islink) {
			writer.appendAnchor(s[1], s[0], true);
		} else {
			String[] t = GenerateNodeHelper.split(s[1],
					GenerateNodeHelper.ANCHOR_MARK);
			if (t[0] == null || t[0].equals("")) {
				writer
						.appendAnchor(GenerateNodeHelper.ANCHOR_MARK + t[1],
								s[0]);
			} else {
				processLink(t[0], s[0], t[1]);
			}
		}
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiPagename node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ANCHOR_MARK);
		changeTabAndNewlineState(false);
		processLink(s[0], s[0], s[1]);
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiInlinePlugin node, Object data) {
		String args[] = VisitorUtils.getArgs(node);
		int inlinestart = 0;
		if (args != null) {
			inlinestart = 1;
		}
		String child = getChildString(node, data, inlinestart);
		PluginExecuter executer = context.getEngine().getPluginExecuter();
		executer.inline(context, node.name, args, child);

		changeTabAndNewlineState(false);

		return null;
	}

	public Object visit(WikiArgs node, Object data) {
		return null;
	}

	public Object visit(WikiErrors node, Object data) {
		return appendError(node.letter);
	}

	public Object visit(WikiAnyOther node, Object data) {
		changeTabAndNewlineState(false);
		writer.body(node.letter);
		return null;
	}

	public void write(String body) {
		writer.body(body);
	}

	// ------------- private methods --------------------------

	private void processAnnotations(WikiGenerateTree node, Object data) {
		if (node.annotation.size() <= 0) {
			return;
		}

		writer.start("hr").attr("class", "note_hr").attr("align", "left").end();
		Iterator itr = node.annotation.iterator();
		int idx = 1;
		writer.disableLineBreak();
		while (itr.hasNext()) {
			writer.disableTab();
			appendSuper(writer, ANNOTATION_FOOT_PREFIX + idx,
					ANNOTATION_NOTE_CLASS, GenerateNodeHelper.ANCHOR_MARK
							+ ANNOTATION_TEXT_PREFIX + idx, "*" + idx);
			writer.enableTab();

			SimpleNode n = (SimpleNode) itr.next();
			for (int i = 0; i < n.jjtGetNumChildren(); i++) {
				n.jjtGetChild(i).jjtAccept(this, data);
			}
			writer.appendBr();
			idx++;
		}
		writer.enableLineBreak();
	}

	private void processBlockElement(String tag, Node node, Object data) {
		processBlockElement(tag, null, null, node, data);
	}

	private void processBlockElement(String tag, String attrKey,
			String attrValue, Node node, Object data) {
		writer.start(tag);
		if (attrKey != null) {
			writer.attr(attrKey, attrValue);
		}
		processChildren(node, data);
		writer.end();
	}

	private void processElement(String tag, Node node, Object data) {
		if (node.jjtGetNumChildren() == 0) {
			return;
		}
		writer.start(tag);
		processChildren(node, data);
		writer.disableTab();
		writer.end();
		writer.enableTab();
	}

	private void processChildren(Node node, Object data, int fromIndex) {
		for (int i = fromIndex; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
	}

	private void processChildren(Node node, Object data) {
		processChildren(node, data, 0);
	}

	// shared by WikiTable and WikiCSVTable
	private void processTable(SimpleNode node, Object data) {

		VisitorUtils.prepareWikiTable(node, data);
		int prenum = 0;
		int open = 0;
		boolean isBody = false;

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			try {
				WikiTablemember child = (WikiTablemember) node.jjtGetChild(i);
				if (child.jjtGetNumChildren() != prenum) { // start table
					if (prenum != 0) {
						if (isBody)
							writer.end();
						writer.end();
						isBody = false;
						open--;
					}
					writer.start("table");
					open++;
				}
				if (!isBody) {
					if (child.type == GenerateNodeHelper.TABLE_TYPE_HEADER) {
						writer.start("thead");
					} else if (child.type == GenerateNodeHelper.TABLE_TYPE_FOOTER) {
						writer.start("tfooter");
					} else {
						writer.start("tbody");
						isBody = true;
					}
				}

				prenum = child.jjtGetNumChildren();
				child.jjtAccept(this, data);

				if (!isBody) {
					writer.end();
				}
			} catch (ClassCastException cce) {// in-case wikierror
				while (open > 0) {
					if (isBody)
						writer.end();
					writer.end();
					open--;
					prenum = 0;
					isBody = false;
				}
				node.jjtGetChild(i).jjtAccept(this, data);
				writer.enableLineBreak();
			}
		}
		while (open > 0) {
			if (isBody)
				writer.end();
			writer.end();
			open--;
		}
	}

	private String getChildString(SimpleNode node, Object data, int fromIndex) {
		HtmlWriter currentWriter = writer;
		StringWriter sw = new StringWriter();
		writer = new HtmlWriter(sw);
		processChildren(node, data, fromIndex);
		writer = currentWriter;
		String child = sw.toString();
		if (child == null || child.equals("")) {
			return "";
		}
		return child;
	}

	private void processLink(String pagename, String body, String anchor) {
		WikiPageLinkFactory linkFactory = context.getEngine().getLinkFactory();
		WikiPageLink link = linkFactory.create(context, pagename, body, anchor);
		if (!link.hasBody()) {
			return;
		}
		if (link.hasPreMsg()) {
			String msg = link.getPreMsg();
			writer.start("span").attr("class", NOTEXIST_CLASS).body(msg).end();
		}
		writer.appendAnchor(link.getUrl(), link.getBody());
		if (link.hasPostMsg()) {
			String msg = link.getPostMsg();
			writer.start("span").attr("class", NOTEXIST_CLASS).body(msg).end();
		}
	}

	private void startListTag(int type) {
		if (type == GenerateNodeHelper.LIST_TYPE_NORMAL) {
			writer.start("ul");
		} else if (type == GenerateNodeHelper.LIST_TYPE_NUMERICAL) {
			writer.start("ol");
		}
	}

	private Object appendError(String str) {
		String style = "color:" + FONTCOLOR_ERROR + ";";
		writer.disableLineBreak();
		writer.start("span").attr("style", style).body(str).end();
		writer.enableLineBreak();
		return null;
	}

	private void changeTabAndNewlineState(boolean flag) {
		if (flag) {
			writer.enableLineBreak();
			writer.enableTab();
		} else {
			writer.disableTab();
			writer.disableLineBreak();
		}
	}

	private void appendSuper(HtmlWriter buf, String styleId, String styleClass,
			String href, String body) {
		buf.start("a");
		if (styleId != null)
			buf.attr("id", styleId);
		if (styleClass != null)
			buf.attr("class", styleClass);
		buf.attr("href", href);
		buf.body(body);
		buf.end();
	}
}
