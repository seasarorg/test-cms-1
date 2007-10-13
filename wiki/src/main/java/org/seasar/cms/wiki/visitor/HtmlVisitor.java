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
package org.seasar.cms.wiki.visitor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.plugin.PluginExecuter;
import org.seasar.cms.wiki.engine.plugin.WikiBodyEvaluator;
import org.seasar.cms.wiki.engine.plugin.WikiPageLink;
import org.seasar.cms.wiki.factory.WikiLinkFactory;
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
import org.seasar.cms.wiki.renderer.HtmlWriter;
import org.seasar.cms.wiki.util.WikiStringUtils;
import org.seasar.cms.wiki.util.NodeUtils;
import org.seasar.cms.wiki.util.VisitorUtils;
import org.seasar.cms.wiki.util.WikiHelper;

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
		writer.appendBody(WikiStringUtils.escape(node.letter));
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

		try {
			writer.write();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}

	public Object visit(WikiParagraph node, Object data) {
		writer.appendStartTag("p");
		processChildren(node, data);
		writer.endTag();
		return null;
	}

	public Object visit(WikiExcerpt node, Object data) {

		boolean isTagNeed = VisitorUtils.isExcerptStartNeeded(node);

		if (isTagNeed) {
			writer.appendStartTag("blockquote");
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
			writer.endTag();
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
							writer.endTag();
							level[j] = 0;
						}
					}
					if (level[curlevel - 1] != curtype) {
						writer.endTag();
						startListTag(curtype);
					}
				} else { // if (curtype != pretype) {
					// not level change but type change
					writer.endTag();
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
				writer.endTag();
			}
		}
		return null;
	}

	public Object visit(WikiDefineList node, Object data) {
		writer.appendStartTag("dl");
		processChildren(node, data);
		writer.endTag();
		return null;
	}

	public Object visit(WikiPreshaped node, Object data) {
		writer.appendStartTag("pre");
		changeTabAndNewlineState(false);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
			writer.appendBody("\n");
		}
		writer.setNewline(true);
		writer.endTag();
		writer.setTab(true);
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

			writer.appendStartTag("td");
			for (String key : attrs.keySet()) {
				writer.appendAttribute(key, attrs.get(key));
			}
			writer.appendBody(str);
			writer.endTag();
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
		writer.appendStartTag("div");
		writer.appendAttribute("align", align);
		processChildren(node, data);
		writer.endTag();
		return null;
	}

	public Object visit(WikiFloatAlign node, Object data) {

		String align = node.image.toLowerCase().substring(1,
				node.image.length() - 1);
		int idx = 0;
		String widthStr = null;
		if ((idx = align.indexOf("(")) != -1) {
			widthStr = WikiHelper.deleteParenthesis(align, "(", ")");
			align = align.substring(0, idx);
		}
		String style = "float:" + align + ";";
		if (widthStr != null) {
			style += "width: " + widthStr + "px;";
		}

		writer.appendStartTag("div");
		writer.appendAttribute("style", style);
		processChildren(node, data);
		writer.endTag();
		return null;
	}

	public Object visit(WikiHorizontalline node, Object data) {
		writer.appendStartTag("hr");
		writer.endTag();
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
		String letters = (node.isHTMLescape) ? WikiStringUtils.escape(node.letter)
				: node.letter;

		WikiBodyEvaluator evaluator = context.getEngine().getBodyEvaluator();
		String bodyString = evaluator.eval(context, letters);
		if (node.isEmail) {
			writer.appendAnchor("mailto:" + node.letter, bodyString);
		} else if (node.isURL) {
			if (WikiHelper.isImage(node.letter)) {
				writer.appendStartTag("a");
				writer.appendAttribute("href", node.letter);
				writer.appendStartTag("img");
				writer.appendAttribute("src", node.letter);
				writer.appendAttribute("alt", bodyString);
				writer.endTag();
				writer.endTag();
			} else {
				writer.appendAnchor(node.letter, bodyString);
			}
		} else if (node.isAnchor) {
			String id = (node.letter.startsWith("#")) ? node.letter
					.substring(1) : node.letter;
			appendSuper(writer, id, "anchor_super", bodyString, "&nbsp;");
		} else if (node.isWikiname) {
			processLink(node.letter, bodyString, null);
		} else if (node.isNewline) {
			writer.appendBr();
		} else {
			writer.appendBody(bodyString);
		}
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiStrongItalic node, Object data) {

		int tag = 0;
		changeTabAndNewlineState(false);

		if (VisitorUtils.isBold(node)) {
			writer.appendStartTag("strong");
			tag++;
		}

		if (VisitorUtils.isItalic(node)) {
			writer.appendStartTag("em");
			tag++;
		}

		String pre = VisitorUtils.getAppendString(node, true);
		String post = VisitorUtils.getAppendString(node, false);

		if (pre != null) {
			writer.appendBody(pre);
		}

		processChildren(node, data);

		changeTabAndNewlineState(false);

		if (post != null) {
			writer.appendBody(post);
		}
		for (int i = 0; i < tag; i++) {
			writer.endTag();
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
		writer.setNewline(false);
		writer.appendStartTag("a");
		writer.appendAttribute("id", ANNOTATION_TEXT_PREFIX + node.num);
		writer.appendAttribute("class", ANNOTATION_NOTE_CLASS);
		writer.appendAttribute("href", "#" + ANNOTATION_FOOT_PREFIX + node.num);
		writer.appendBody("*" + node.num);
		writer.endTag();
		writer.setNewline(true);
		return null;
	}

	// interwiki not implemented...
	public Object visit(WikiInterwiki node, Object data) {
		writer.appendBody("[[" + node.image + "]]");
		return null;
	}

	public Object visit(WikiLink node, Object data) {
		String[] s = WikiHelper.split(node.image, WikiHelper.LINK_DELIMITER);
		writer.appendAnchor(s[1], s[0], true);
		return null;
	}

	public Object visit(WikiAlias node, Object data) {
		String[] s = WikiHelper.split(node.image, WikiHelper.ALIAS_DELIMITER);
		changeTabAndNewlineState(false);
		if (node.islink) {
			writer.appendAnchor(s[1], s[0], true);
		} else {
			String[] t = WikiHelper.split(s[1], WikiHelper.ANCHOR_MARK);
			if (t[0] == null || t[0].equals("")) {
				writer.appendAnchor(WikiHelper.ANCHOR_MARK + t[1], s[0]);
			} else {
				processLink(t[0], s[0], t[1]);
			}
		}
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiPagename node, Object data) {
		String[] s = WikiHelper.split(node.image, WikiHelper.ANCHOR_MARK);
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
		writer.appendBody(node.letter);
		return null;
	}

	public void write(String body) {
		writer.appendBody(body);
	}

	// ------------- private methods --------------------------

	private void processAnnotations(WikiGenerateTree node, Object data) {
		if (node.annotation.size() <= 0) {
			return;
		}

		writer.appendStartTag("hr");
		writer.appendAttribute("class", "note_hr");
		writer.appendAttribute("align", "left");
		writer.endTag();
		Iterator itr = node.annotation.iterator();
		int idx = 1;
		writer.setNewline(false);
		while (itr.hasNext()) {
			writer.setTab(false);
			appendSuper(writer, ANNOTATION_FOOT_PREFIX + idx,
					ANNOTATION_NOTE_CLASS, WikiHelper.ANCHOR_MARK
							+ ANNOTATION_TEXT_PREFIX + idx, "*" + idx);
			writer.setTab(true);

			SimpleNode n = (SimpleNode) itr.next();
			for (int i = 0; i < n.jjtGetNumChildren(); i++) {
				n.jjtGetChild(i).jjtAccept(this, data);
			}
			writer.appendBr();
			idx++;
		}
		writer.setNewline(true);
	}

	private void processElement(String tag, Node node, Object data) {
		if (node.jjtGetNumChildren() == 0) {
			return;
		}
		writer.appendStartTag(tag);
		processChildren(node, data);
		writer.setTab(false);
		writer.endTag();
		writer.setTab(true);
	}

	private void processChildren(Node node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
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
							writer.endTag();
						writer.endTag();
						isBody = false;
						open--;
					}
					writer.appendStartTag("table");
					open++;
				}
				if (!isBody) {
					if (child.type == WikiHelper.TABLE_TYPE_HEADER) {
						writer.appendStartTag("thead");
					} else if (child.type == WikiHelper.TABLE_TYPE_FOOTER) {
						writer.appendStartTag("tfooter");
					} else {
						writer.appendStartTag("tbody");
						isBody = true;
					}
				}

				prenum = child.jjtGetNumChildren();
				child.jjtAccept(this, data);

				if (!isBody) {
					writer.endTag();
				}
			} catch (ClassCastException cce) {// in-case wikierror
				while (open > 0) {
					if (isBody)
						writer.endTag();
					writer.endTag();
					open--;
					prenum = 0;
					isBody = false;
				}
				node.jjtGetChild(i).jjtAccept(this, data);
				writer.setNewline(true);
			}
		}
		while (open > 0) {
			if (isBody)
				writer.endTag();
			writer.endTag();
			open--;
		}
	}

	private String getChildString(SimpleNode node, Object data, int idx) {
		int start = writer.nextIndex();
		String childstr = null;
		writer.setTab(false);
		for (int i = idx; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		int end = writer.nextIndex();
		if (start != end)
			childstr = writer.cut(start, end);
		writer.setTab(true);

		return childstr;
	}

	private void processLink(String pagename, String body, String anchor) {
		WikiLinkFactory linkFactory = context.getEngine().getLinkFactory();
		WikiPageLink link = linkFactory.create(pagename, body, anchor);
		if (!link.hasBody()) {
			return;
		} else if (link.hasCreationMark()) {
			writer.appendAnchor(link.getUrl(), link.getCreationMark());
			writer.appendStartTag("span");
			writer.appendAttribute("class", NOTEXIST_CLASS);
			writer.appendBody(link.getBody());
			writer.endTag();
		} else {
			writer.appendAnchor(link.getUrl(), link.getBody());
		}
	}

	private void startListTag(int type) {
		if (type == WikiHelper.LIST_TYPE_NORMAL) {
			writer.appendStartTag("ul");
		} else if (type == WikiHelper.LIST_TYPE_NUMERICAL) {
			writer.appendStartTag("ol");
		}
	}

	private Object appendError(String str) {
		writer.setNewline(false);
		writer.appendStartTag("span");
		writer.appendAttribute("style", "color:" + FONTCOLOR_ERROR + ";");
		writer.appendBody(str);
		writer.endTag();
		writer.setNewline(true);
		return null;
	}

	private void changeTabAndNewlineState(boolean flag) {
		writer.setNewline(flag);
		writer.setTab(flag);
	}

	private void appendSuper(HtmlWriter buf, String styleId, String styleClass,
			String href, String body) {
		buf.appendStartTag("a");
		if (styleId != null)
			buf.appendAttribute("id", styleId);
		if (styleClass != null)
			buf.appendAttribute("class", styleClass);
		buf.appendAttribute("href", href);
		buf.appendBody(body);
		buf.endTag();
	}
}
