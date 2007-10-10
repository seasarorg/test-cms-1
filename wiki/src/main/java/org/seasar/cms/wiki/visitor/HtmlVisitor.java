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

import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.seasar.cms.wiki.util.VisitorUtils;
import org.seasar.cms.wiki.util.WikiHelper;

/**
 * @author someda
 */
public class HtmlVisitor implements WikiParserVisitor {

	private static final String ANNOTATION_TEXT_PREFIX = "notetext_";

	private static final String ANNOTATION_FOOT_PREFIX = "notefoot_";

	private static final String ANNOTATION_NOTE_CLASS = "note_super";

	private static final String FONTCOLOR_ERROR = "red";

	private List keywords_ = null;

	private static final String TAG_SPAN_HIGHLIGHT = "<span class=\"highlight\">";

	private static final String TAG_SPAN_CLOSE = "</span>";

	private HtmlWriter buf_;

	public HtmlVisitor(Writer writer) {
		buf_ = new HtmlWriter(writer);
	}

	public Object visit(SimpleNode node, Object data) {
		return null;
	}

	public Object visit(WikiSyntaxError node, Object data) {
		return appendError(node.letter);
	}

	public Object visit(WikiSkipToNewline node, Object data) {
		buf_.appendBody(VisitorUtils.escape(node.letter));
		return null;
	}

	/**
	 * ルートノードの解析
	 */
	public Object visit(WikiGenerateTree node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		if (node.annotation.size() > 0) {
			buf_.appendStartTag("hr");
			buf_.appendAttribute("class", "note_hr");
			buf_.appendAttribute("align", "left");
			buf_.endTag();

			Iterator itr = node.annotation.iterator();
			int idx = 1;
			buf_.setNewline(false);
			while (itr.hasNext()) {
				buf_.setTab(false);
				appendSuper(buf_, ANNOTATION_FOOT_PREFIX + idx,
						ANNOTATION_NOTE_CLASS, WikiHelper.ANCHOR_MARK
								+ ANNOTATION_TEXT_PREFIX + idx, "*" + idx);
				buf_.setTab(true);

				SimpleNode n = (SimpleNode) itr.next();
				for (int i = 0; i < n.jjtGetNumChildren(); i++) {
					n.jjtGetChild(i).jjtAccept(this, data);
				}
				buf_.appendBr();
				idx++;
			}
			buf_.setNewline(true);
		}
		return buf_.toString();
	}

	public Object visit(WikiParagraph node, Object data) {
		buf_.appendStartTag("p");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf_.endTag();
		return null;
	}

	public Object visit(WikiExcerpt node, Object data) {

		boolean isTagNeed = VisitorUtils.isExcerptStartNeeded(node);

		if (isTagNeed)
			buf_.appendStartTag("blockquote");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			// if child is paragraph, not generate <p> tag
			if (node.jjtGetChild(i) instanceof WikiParagraph) {
				WikiParagraph p = (WikiParagraph) node.jjtGetChild(i);
				for (int j = 0; j < p.jjtGetNumChildren(); j++)
					p.jjtGetChild(j).jjtAccept(this, data);
			} else {
				node.jjtGetChild(i).jjtAccept(this, data);
			}
		}
		changeTabAndNewlineState(true);
		if (isTagNeed)
			buf_.endTag();
		return null;
	}

	public Object visit(WikiList node, Object data) {

		int curtype = 0;
		int curlevel = 0;
		int pretype = 0;
		int prelevel = 0;
		int[] level = { 0, 0, 0 };

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {

			if (node.jjtGetChild(i) instanceof WikiListMember) {
				WikiListMember child = (WikiListMember) node.jjtGetChild(i);

				curtype = child.type;
				curlevel = child.level;

				// upward level change
				if (curlevel > prelevel)
					startListTag(curtype);

				// downward level change
				if (curlevel < prelevel) {
					for (int j = curlevel; j < prelevel; j++) {
						if (level[j] != 0) {
							buf_.endTag();
							level[j] = 0;
						}
					}
					if (level[curlevel - 1] != curtype) {
						buf_.endTag();
						startListTag(curtype);
					}
				}

				// not level change but type change
				if (curlevel == prelevel && curtype != pretype) {
					buf_.endTag();
					startListTag(curtype);
				}

				// change state
				if (curlevel != prelevel || curtype != pretype) {
					level[curlevel - 1] = curtype;
					pretype = curtype;
					prelevel = curlevel;
				}
				child.jjtAccept(this, data);
			}
		}

		// close all list related tag
		for (int i = 0; i < level.length; i++)
			if (level[i] != 0)
				buf_.endTag();
		return null;
	}

	private void startListTag(int type) {
		if (type == WikiHelper.LIST_TYPE_NORMAL) {
			buf_.appendStartTag("ul");
		} else if (type == WikiHelper.LIST_TYPE_NUMERICAL) {
			buf_.appendStartTag("ol");
		}
	}

	public Object visit(WikiDefineList node, Object data) {
		buf_.appendStartTag("dl");
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
			node.jjtGetChild(i).jjtAccept(this, data);
		buf_.endTag();
		return null;
	}

	public Object visit(WikiPreshaped node, Object data) {
		buf_.appendStartTag("pre");
		changeTabAndNewlineState(false);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
			buf_.appendBody("\n");
		}
		buf_.setNewline(true);
		buf_.endTag();
		buf_.setTab(true);
		return null;
	}

	public Object visit(WikiTable node, Object data) {
		processTable(node, data);
		return null;
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
							buf_.endTag();
						buf_.endTag();
						isBody = false;
						open--;
					}
					buf_.appendStartTag("table");
					open++;
				}
				if (!isBody) {
					if (child.type == WikiHelper.TABLE_TYPE_HEADER) {
						buf_.appendStartTag("thead");
					} else if (child.type == WikiHelper.TABLE_TYPE_FOOTER) {
						buf_.appendStartTag("tfooter");
					} else {
						buf_.appendStartTag("tbody");
						isBody = true;
					}
				}

				prenum = child.jjtGetNumChildren();
				child.jjtAccept(this, data);

				if (!isBody) {
					buf_.endTag();
				}
			} catch (ClassCastException cce) {// in-case wikierror
				while (open > 0) {
					if (isBody)
						buf_.endTag();
					buf_.endTag();
					open--;
					prenum = 0;
					isBody = false;
				}
				node.jjtGetChild(i).jjtAccept(this, data);
				buf_.setNewline(true);
			}
		}
		while (open > 0) {
			if (isBody)
				buf_.endTag();
			buf_.endTag();
			open--;
		}
	}

	public Object visit(WikiTablemember node, Object data) {

		buf_.appendStartTag("tr");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf_.setTab(false);
		buf_.endTag();
		buf_.setTab(true);
		return null;
	}

	public Object visit(WikiTablecolumn node, Object data) {

		if (!node.iscolspan && !node.isrowspan) {
			Map<String, String> attrs = new HashMap<String, String>();
			String str = getChildString(node, data, 0);

			if (node.align != null) {
				attrs.put("style", "text-align:" + node.align + ";");
			}

			if (node.bgcolor != null) {
				String style;
				if ((style = attrs.get("style")) != null) {
					style += "background-color:" + node.bgcolor + ";";
					attrs.put("style", style);
				} else {
					attrs
							.put("style", "background-color:" + node.bgcolor
									+ ";");
				}
			}

			if (node.color != null) {
				String style;
				if ((style = attrs.get("style")) != null) {
					style += "color:" + node.color + ";";
					attrs.put("style", style);
				} else {
					attrs.put("style", "color:" + node.color + ";");
				}
			}

			if (node.size != null) {
				String style;
				if ((style = attrs.get("style")) != null) {
					style += "font-size:" + node.size + ";";
					attrs.put("style", style);
				} else {
					attrs.put("style", "font-size:" + node.size + ";");
				}
			}

			if (node.colspannum > 0) {
				attrs.put("colspan", ++node.colspannum + "");
			}
			if (node.rowspannum > 0) {
				attrs.put("rowspan", ++node.rowspannum + "");
			}

			buf_.appendStartTag("td");
			for (String key : attrs.keySet()) {
				buf_.appendAttribute(key, attrs.get(key));
			}
			buf_.appendBody(str);
			buf_.endTag();
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
		buf_.appendHeading(node.level + 1, childstr);
		return null;
	}

	public Object visit(WikiAlign node, Object data) {

		String align = node.image.toLowerCase().substring(0,
				node.image.length() - 1);
		buf_.appendStartTag("div");
		buf_.appendAttribute("align", align);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf_.endTag();
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

		buf_.appendStartTag("div");
		if (widthStr != null) {
			buf_.appendAttribute("style", "float:" + align + "; width: "
					+ widthStr + "px;");
		} else {
			buf_.appendAttribute("style", "float:" + align + ";");
		}

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf_.endTag();

		return null;
	}

	public Object visit(WikiHorizontalline node, Object data) {
		buf_.appendStartTag("hr");
		buf_.endTag();
		return null;
	}

	public Object visit(WikiBlockPlugin node, Object data) {
		// Block Plugin Executer
		return null;
	}

	public Object visit(WikiLetters node, Object data) {

		changeTabAndNewlineState(false);
		String letters = (node.isHTMLescape) ? VisitorUtils.escape(node.letter)
				: node.letter;
		String bodyString = processKeyword(letters);

		if (node.isEmail) {
			buf_.appendAnchor("mailto:" + node.letter, bodyString);
		} else if (node.isURL) {
			if (WikiHelper.isImage(node.letter)) {
				buf_.appendStartTag("a");
				buf_.appendAttribute("href", node.letter);
				buf_.appendStartTag("img");
				buf_.appendAttribute("src", node.letter);
				buf_.appendAttribute("alt", bodyString);
				buf_.endTag();
				buf_.endTag();
			} else {
				buf_.appendAnchor(node.letter, bodyString);
			}
		} else if (node.isAnchor) {
			String id = (node.letter.startsWith("#")) ? node.letter
					.substring(1) : node.letter;
			appendSuper(buf_, id, "anchor_super", bodyString, "&nbsp;");
		} else if (node.isWikiname) {
			processSecurityLink(node.letter, bodyString, null);
		} else if (node.isNewline) {
			buf_.appendBr();
		} else {
			buf_.appendBody(bodyString);
		}
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiStrongItalic node, Object data) {

		int tag = 0;
		changeTabAndNewlineState(false);

		if (VisitorUtils.isBold(node)) {
			buf_.appendStartTag("strong");
			tag++;
		}

		if (VisitorUtils.isItalic(node)) {
			buf_.appendStartTag("em");
			tag++;
		}

		String pre = VisitorUtils.getAppendString(node, true);
		String post = VisitorUtils.getAppendString(node, false);

		if (pre != null)
			buf_.appendBody(pre);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		changeTabAndNewlineState(false);
		if (post != null)
			buf_.appendBody(post);

		for (int i = 0; i < tag; i++)
			buf_.endTag();

		return null;
	}

	public Object visit(WikiDeleteline node, Object data) {
		changeTabAndNewlineState(false);
		buf_.appendStartTag("del");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		changeTabAndNewlineState(false);
		buf_.endTag();
		return null;
	}

	public Object visit(WikiAnnotation node, Object data) {
		buf_.setNewline(false);
		buf_.appendStartTag("a");
		buf_.appendAttribute("id", ANNOTATION_TEXT_PREFIX + node.num);
		buf_.appendAttribute("class", ANNOTATION_NOTE_CLASS);
		buf_.appendAttribute("href", "#" + ANNOTATION_FOOT_PREFIX + node.num);
		buf_.appendBody("*" + node.num);
		buf_.endTag();
		buf_.setNewline(true);
		return null;
	}

	// interwiki not implemented...
	public Object visit(WikiInterwiki node, Object data) {
		buf_.appendBody("[[" + node.image + "]]");
		return null;
	}

	public Object visit(WikiLink node, Object data) {
		String[] s = WikiHelper.split(node.image, WikiHelper.LINK_DELIMITER);
		buf_.appendAnchor(s[1], s[0], true);
		return null;
	}

	public Object visit(WikiAlias node, Object data) {
		String[] s = WikiHelper.split(node.image, WikiHelper.ALIAS_DELIMITER);

		changeTabAndNewlineState(false);
		if (node.islink) {
			buf_.appendAnchor(s[1], s[0], true);
		} else {
			String[] t = WikiHelper.split(s[1], WikiHelper.ANCHOR_MARK);
			if (t[0] == null || t[0].equals("")) {
				buf_.appendAnchor(WikiHelper.ANCHOR_MARK + t[1], s[0]);
			} else {
				processSecurityLink(t[0], s[0], t[1]);
			}
		}
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiPagename node, Object data) {
		String[] s = WikiHelper.split(node.image, WikiHelper.ANCHOR_MARK);
		changeTabAndNewlineState(false);
		processSecurityLink(s[0], s[0], s[1]);
		changeTabAndNewlineState(true);
		return null;
	}

	public Object visit(WikiInlinePlugin node, Object data) {
		String name = node.name;
		// inline plugin execute
		return null;
	}

	public Object visit(WikiArgs node, Object data) {
		return null;
	}

	public Object visit(WikiErrors node, Object data) {
		return appendError(node.letter);
	}

	public Object visit(WikiListMember node, Object data) {
		buf_.appendStartTag("li");

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf_.setTab(false);
		buf_.endTag();
		buf_.setTab(true);
		return null;
	}

	public Object visit(WikiDefinedWord node, Object data) {
		if (node.jjtGetNumChildren() > 0) {
			buf_.appendStartTag("dt");
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				node.jjtGetChild(i).jjtAccept(this, data);
			}
			buf_.setTab(false);
			buf_.endTag();
			buf_.setTab(true);
		}
		return null;
	}

	public Object visit(WikiExplanationWord node, Object data) {
		buf_.appendStartTag("dd");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf_.setTab(false);
		buf_.endTag();
		buf_.setTab(true);
		return null;
	}

	public Object visit(WikiAnyOther node, Object data) {
		changeTabAndNewlineState(false);
		buf_.appendBody(node.letter);
		return null;
	}

	private String getChildString(SimpleNode node, Object data, int idx) {
		int start = buf_.nextIndex();
		String childstr = null;
		buf_.setTab(false);
		for (int i = idx; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		int end = buf_.nextIndex();
		if (start != end)
			childstr = buf_.cut(start, end);
		buf_.setTab(true);

		return childstr;
	}

	private void processSecurityLink(String pagename, String body, String anchor) {
		// try {
		// if (ctx.isPageExist(pagename, request_)) {
		// Resource resource = ctx.getResource(request_, pagename);
		// if (resource.isFolder()) {
		// if (!pagename.endsWith("/"))
		// pagename = pagename + "/";
		// }
		// URL url = ctx.getURLByName(pagename, request_);
		// if (anchor == null || "".equals(anchor)) {
		// buf_.appendAnchor(url.toString(), body);
		// } else {
		// buf_.appendAnchor(url.toString() + WikiHelper.ANCHOR_MARK
		// + anchor, body);
		// }
		// // } else if (request_.getMode() != CmsConstants.MODE_FULL) {
		// // URL url = ctx.getCreatePageURL(pagename, request_);
		// // buf_.appendAnchor(url.toString(), CREATIONPAGE_MARK);
		// // buf_.appendStartTag("span");
		// // buf_.appendAttribute("class", NOTEXIST_CLASS);
		// // buf_.appendBody(body);
		// // buf_.endTag();
		// } else {
		// buf_.appendBody(body);
		// }
		// } catch (TgwSecurityException tse) {
		// buf_.appendBody(body);
		// }
	}

	private Object appendError(String str) {
		buf_.setNewline(false);
		buf_.appendStartTag("span");
		buf_.appendAttribute("style", "color:" + FONTCOLOR_ERROR + ";");
		buf_.appendBody(str);
		buf_.endTag();
		buf_.setNewline(true);
		return null;
	}

	private void changeTabAndNewlineState(boolean flag) {
		buf_.setNewline(flag);
		buf_.setTab(flag);
	}

	private String processKeyword(String letter) {
		String ret = letter;
		if (keywords_ != null) {
			for (Iterator i = keywords_.iterator(); i.hasNext();) {
				String s = (String) i.next();
				ret = ret
						.replaceAll(s, TAG_SPAN_HIGHLIGHT + s + TAG_SPAN_CLOSE);
			}
		}
		return ret;
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
