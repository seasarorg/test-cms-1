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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiBodyFactory;
import org.seasar.cms.wiki.factory.WikiPageLink;
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
import org.seasar.cms.wiki.parser.WikiPreshaped;
import org.seasar.cms.wiki.parser.WikiSkipToNewline;
import org.seasar.cms.wiki.parser.WikiStrongItalic;
import org.seasar.cms.wiki.parser.WikiSyntaxError;
import org.seasar.cms.wiki.parser.WikiTable;
import org.seasar.cms.wiki.parser.WikiTablecolumn;
import org.seasar.cms.wiki.parser.WikiTablemember;
import org.seasar.cms.wiki.plugin.PluginExecuter;
import org.seasar.cms.wiki.util.GenerateNodeHelper;
import org.seasar.cms.wiki.util.ImageUtils;
import org.seasar.cms.wiki.util.NodeUtils;
import org.seasar.cms.wiki.util.TableNodeUtils;
import org.seasar.cms.wiki.util.VisitorUtils;
import org.seasar.cms.wiki.util.WikiStringUtils;

/**
 * @author someda
 * @author nishioka
 */
public class HtmlWikiVisitor implements WikiWriterVisitor,
		WikiOutputStreamVisitor {

	private XmlWriter writer;

	private WikiContext context;

	private boolean needWriterClose = false;

	public HtmlWikiVisitor() {
	}

	public void init(WikiContext context, OutputStream os) {
		try {
			needWriterClose = true;
			init(context, new OutputStreamWriter(os, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// UTF-8 always supported
			throw new IllegalStateException(e);
		}
	}

	public void init(WikiContext context, Writer writer) {
		this.context = context;
		this.writer = new XmlWriter(writer);
	}

	public void write(byte[] bytes) {
		try {
			write(new String(bytes, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// UTF-8 always supported
			throw new IllegalStateException(e);
		}
	}

	public void write(String body) {
		writer.body(body);
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
	 * Analyze of the root node.
	 */
	public Object visit(WikiGenerateTree node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		processAnnotations(node, data);

		if (needWriterClose) {
			try {
				writer.close();
			} catch (Exception e) {
				// do nothing
			}
		}

		return null;
	}

	public Object visit(WikiParagraph node, Object data) {
		processBlockElement("p", node, data);
		return null;
	}

	public Object visit(WikiExcerpt node, Object data) {
		boolean isTagNeed = VisitorUtils.isExcerptStartNeeded(node);
		if (isTagNeed) {
			startTag("blockquote");
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			// if child is paragraph, not generate <p> tag
			Node child = node.jjtGetChild(i);
			if (child instanceof WikiParagraph) {
				WikiParagraph p = (WikiParagraph) child;
				processChildren(p, data);
			} else {
				child.jjtAccept(this, data);
			}
		}
		writer.block();
		if (isTagNeed) {
			writer.end();
		}
		return null;
	}

	public Object visit(WikiList node, Object data) {
		int pretype = 0;
		int prelevel = 0;
		int[] level = { 0, 0, 0 };

		for (WikiListMember child : NodeUtils.find(node, WikiListMember.class)) {
			updateListTag(child, level, prelevel, pretype);
			pretype = child.type;
			prelevel = child.level;
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
		startTag("pre").inline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
			writer.body("\n");
		}
		writer.enableNewline().end().enableTab();
		return null;
	}

	public Object visit(WikiTable node, Object data) {
		processTable(node, data);
		return null;
	}

	public Object visit(WikiTablemember node, Object data) {
		String classValue = null;
		if (rowNum % 2 == 0) {
			classValue = getProperty("class.core.tr.even");
		} else {
			classValue = getProperty("class.core.tr.odd");
		}
		processElementWithClass("tr", node, data, classValue);
		return null;
	}

	public Object visit(WikiTablecolumn node, Object data) {
		if (!node.iscolspan && !node.isrowspan) {
			String str = getChildString(node, data, 0);
			Map<String, String> attrs = TableNodeUtils.getTdAttributes(node);
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

		String anchorId = VisitorUtils.getAnchorId(node);
		WikiPageLink pageLink = context.getEngine().getLinkFactory()
				.createEditLink(context, anchorId);
		if (pageLink != null) {
			String body = pageLink.getBody();
			String href = pageLink.getUrl();
			String anchor = new XmlWriter().start("a").attr("class",
					getProperty("class.anchor_super")).attr("href", href).body(
					body).end().toString();
			childstr += anchor;
		}
		startTag("h" + (node.level + 1)).body(childstr).end();
		return null;
	}

	public Object visit(WikiAlign node, Object data) {
		String align = node.image.toLowerCase().substring(0,
				node.image.length() - 1);
		processBlockElement("div", "align", align, node, data);
		return null;
	}

	public Object visit(WikiFloatAlign node, Object data) {
		String style = VisitorUtils.getFloatStyle(node);
		processBlockElement("div", "style", style, node, data);
		return null;
	}

	public Object visit(WikiHorizontalline node, Object data) {
		startTag("hr").end();
		return null;
	}

	public Object visit(WikiLetters node, Object data) {
		writer.inline();
		String letters = (node.isHTMLescape) ? WikiStringUtils
				.escape(node.letter) : node.letter;
		WikiBodyFactory evaluator = context.getEngine().getBodyEvaluator();
		String body = evaluator.eval(context, letters);
		String letter = node.letter;

		if (node.isEmail) {
			writer.start("a").attr("href", "mailto:" + node.letter).body(body)
					.end();
		} else if (node.isURL) {
			if (ImageUtils.isImage(node.letter)) {
				writer.start("a").attr("href", letter);
				writer.start("img").attr("src", letter).attr("alt", body);
				writer.end().end();
			} else {
				writer.start("a").attr("href", node.letter).body(body).end();
			}
		} else if (node.isAnchor) {
			String id = (letter.startsWith("#")) ? letter.substring(1) : letter;
			appendSuper(id, getProperty("class.anchor_super"), body, "&nbsp;");
		} else if (node.isWikiname) {
			processLink(node.letter, body, null);
		} else if (node.isNewline) {
			startTag("br").end();
		} else {
			writer.body(body);
		}
		writer.block();
		return null;
	}

	public Object visit(WikiStrongItalic node, Object data) {

		int tag = 0;

		writer.inline();
		if (VisitorUtils.isBold(node)) {
			startTag("strong");
			tag++;
		}

		if (VisitorUtils.isItalic(node)) {
			startTag("em");
			tag++;
		}

		String pre = VisitorUtils.getAppendString(node, true);
		String post = VisitorUtils.getAppendString(node, false);

		if (pre != null) {
			writer.body(pre);
		}

		processChildren(node, data);

		writer.block();
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
		String id = getProperty("id.note.text.prefix") + node.num;
		String href = "#" + getProperty("id.note.foot.prefix") + node.num;
		String clazz = getProperty("class.note_super");
		String body = "*" + node.num;
		writer.disableNewline();
		writer.start("a").attr("id", id).attr("class", clazz)
				.attr("href", href).body(body).end();
		writer.enableNewline();
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
		appendEmail(s[1], s[0]);
		return null;
	}

	public Object visit(WikiAlias node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ALIAS_DELIMITER);

		writer.inline();
		if (node.islink) {
			appendEmail(s[1], s[0]);
		} else {
			String[] t = GenerateNodeHelper.split(s[1], "#");
			if (t[0] == null || t[0].equals("")) {
				writer.start("a").attr("href", "#" + t[1]).body(s[0]).end();
			} else {
				processLink(t[0], s[0], t[1]);
			}
		}

		writer.block();
		return null;
	}

	public Object visit(WikiPagename node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image, "#");
		processLink(s[0], s[0], s[1]);
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
		writer.body(""); // 閉じていないタグを閉じておく
		executer.block(context, node.name, args, child);
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

		writer.inline();
		writer.body(""); // 閉じていないタグを閉じておく
		executer.inline(context, node.name, args, child);
		return null;
	}

	public Object visit(WikiArgs node, Object data) {
		return null;
	}

	public Object visit(WikiErrors node, Object data) {
		return appendError(node.letter);
	}

	public Object visit(WikiAnyOther node, Object data) {
		writer.inline().body(node.letter);
		return null;
	}

	// ------------- private methods --------------------------

	private void processAnnotations(WikiGenerateTree node, Object data) {
		if (node.annotation.size() <= 0) {
			return;
		}
		writer.start("hr").attr("class", getProperty("class.note_hr"));
		writer.attr("align", "left").end();
		Iterator itr = node.annotation.iterator();

		int idx = 1;
		writer.disableNewline();
		while (itr.hasNext()) {
			writer.disableTab();
			appendSuper(getProperty("id.note.foot.prefix") + idx,
					getProperty("class.note_super"), "#"
							+ getProperty("id.note.text.prefix") + idx, "*"
							+ idx);
			writer.enableTab();
			SimpleNode n = (SimpleNode) itr.next();
			processChildren(n, data);
			startTag("br").end();
			idx++;
		}
		writer.enableNewline();
	}

	private void processBlockElement(String tag, Node node, Object data) {
		processBlockElement(tag, null, null, node, data);
	}

	private void processBlockElement(String tag, String attrKey,
			String attrValue, Node node, Object data) {
		startTag(tag);
		if (attrKey != null) {
			writer.attr(attrKey, attrValue);
		}
		processChildren(node, data);
		writer.end();
	}

	private void processElementWithClass(String tag, Node node, Object data,
			String classValue) {
		if (node.jjtGetNumChildren() == 0) {
			return;
		}

		if (classValue != null && classValue.length() > 0) {
			writer.start(tag).attr("class", classValue);
		} else {
			startTag(tag);
		}
		processChildren(node, data);
		writer.disableTab();
		writer.end();
		writer.enableTab();
	}

	private void processElement(String tag, Node node, Object data) {
		processElementWithClass(tag, node, data, null);
	}

	private void processChildren(Node node, Object data, int fromIndex) {
		for (int i = fromIndex; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
	}

	private void processChildren(Node node, Object data) {
		processChildren(node, data, 0);
	}

	private int rowNum = 0;

	// shared by WikiTable and WikiCSVTable
	private void processTable(SimpleNode node, Object data) {
		TableNodeUtils.prepareWikiTable(node, data);
		int prenum = 0;
		int open = 0;
		boolean isBody = false;

		rowNum = 0;

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
					startTag("table");
					open++;
				}
				if (!isBody) {
					if (child.type == GenerateNodeHelper.TABLE_TYPE_HEADER) {
						startTag("thead");
					} else if (child.type == GenerateNodeHelper.TABLE_TYPE_FOOTER) {
						startTag("tfooter");
					} else {
						startTag("tbody");
						isBody = true;
					}
				}

				prenum = child.jjtGetNumChildren();

				child.jjtAccept(this, data);
				if (!isBody) {
					writer.end();
				} else {
					rowNum++;
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
				writer.enableNewline();
			}
		}
		while (open > 0) {
			if (isBody) {
				writer.end();
			}
			writer.end();
			open--;
		}
	}

	private String getChildString(SimpleNode node, Object data, int fromIndex) {
		XmlWriter currentWriter = writer;
		StringWriter sw = new StringWriter();
		writer = new XmlWriter(sw);
		processChildren(node, data, fromIndex);
		writer = currentWriter;
		String child = sw.toString();
		if (child.length() == 0) {
			return null;
		}
		return child;
	}

	private void processLink(String pagename, String body, String anchor) {
		writer.inline();
		WikiPageLinkFactory linkFactory = context.getEngine().getLinkFactory();
		WikiPageLink link = linkFactory.create(context, pagename, body, anchor);
		if (!link.hasBody()) {
			return;
		}
		String notclass = getProperty("class.notexist");
		if (link.hasPreMsg()) {
			String msg = link.getPreMsg();
			writer.start("span").attr("class", notclass).body(msg).end();
		}
		writer.start("a").attr("href", link.getUrl()).body(link.getBody())
				.end();
		if (link.hasPostMsg()) {
			String msg = link.getPostMsg();
			writer.start("span").attr("class", notclass).body(msg).end();
		}
		writer.block();
	}

	private Object appendError(String str) {
		String style = "color:" + getProperty("color.error") + ";";
		writer.disableNewline();
		writer.start("span").attr("style", style).body(str).end();
		writer.enableNewline();
		return null;
	}

	private void appendSuper(String styleId, String styleClass, String href,
			String body) {
		writer.start("a");
		if (styleId != null) {
			writer.attr("id", styleId);
		}
		if (styleClass != null) {
			writer.attr("class", styleClass);
		}
		writer.attr("href", href).body(body).end();
	}

	private void startListTag(int type) {
		if (type == GenerateNodeHelper.LIST_TYPE_NORMAL) {
			writer.start("ul");
		} else if (type == GenerateNodeHelper.LIST_TYPE_NUMERICAL) {
			writer.start("ol");
		}
	}

	private void updateListTag(WikiListMember child, int[] level, int prelevel,
			int pretype) {
		int curtype = child.type;
		int curlevel = child.level;
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
	}

	private String getProperty(String key) {
		return context.getEngine().getProperty(key);
	}

	/**
	 * メールアドレスかどうかのチェック(@を含んでいるか)を行い、パスすれば"mailto:"がlinkUrlに付与される。
	 * 
	 * @param linkUrl
	 * @param linkLabel
	 *            真の場合、
	 */
	private void appendEmail(String linkUrl, String linkLabel) {
		if (linkUrl.indexOf("@") != -1) {
			linkUrl = "mailto:" + linkUrl;
		}
		writer.start("a").attr("href", linkUrl).body(linkLabel).end();
	}

	private XmlWriter startTag(String tagName) {
		writer.start(tagName);
		String className = getProperty("class.core." + tagName);
		if (className != null && className.length() != 0) {
			writer.attr("class", className);
		}
		return writer;
	}
}
