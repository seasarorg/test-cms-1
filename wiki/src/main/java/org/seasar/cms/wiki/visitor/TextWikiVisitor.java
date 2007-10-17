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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.seasar.cms.wiki.renderer.TextWriter;
import org.seasar.cms.wiki.util.VisitorUtils;
import org.seasar.cms.wiki.util.GenerateNodeHelper;

/**
 * @author someda
 */
public class TextWikiVisitor implements WikiParserVisitor {

	private static final int LINESIZE = 80;

	private TextWriter buf;

	private int[] listIndex = { 0, 0, 0 };

	private int[] headerIndex = { 0, 0, 0 };

	private static final String INDENT = " ";

	private Log log = LogFactory.getLog(getClass());

	public TextWikiVisitor(Writer writer) {
		buf = new TextWriter(writer, LINESIZE);
	}

	public Object visit(SimpleNode node, Object data) {
		return null;
	}

	public Object visit(WikiSyntaxError node, Object data) {
		appendError(node.letter);
		return null;
	}

	public Object visit(WikiSkipToNewline node, Object data) {
		buf.append(node.letter);
		return null;
	}

	public Object visit(WikiGenerateTree node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
			node.jjtGetChild(i).jjtAccept(this, data);

		if (node.annotation.size() > 0) {
			buf.appendNewline();
			buf.appendRepeatString("-", LINESIZE / 3);

			Iterator itr = node.annotation.iterator();
			int idx = 1;
			while (itr.hasNext()) {
				buf.appendNewline();
				buf.append("(*" + idx + ")");
				SimpleNode n = (SimpleNode) itr.next();
				for (int i = 0; i < n.jjtGetNumChildren(); i++) {
					n.jjtGetChild(i).jjtAccept(this, data);
				}
				idx++;
			}
		}
		return buf.toString();
	}

	public Object visit(WikiParagraph node, Object data) {

		buf.appendNewline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	public Object visit(WikiExcerpt node, Object data) {

		boolean isTagNeed = VisitorUtils.isExcerptStartNeeded(node);

		if (isTagNeed) {
			String current = buf.getNewlineHeading();
			buf.setNewlineHeading(">" + current);
		}
		buf.appendNewline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof WikiParagraph) {
				WikiParagraph p = (WikiParagraph) node.jjtGetChild(i);
				for (int j = 0; j < p.jjtGetNumChildren(); j++)
					p.jjtGetChild(j).jjtAccept(this, data);
			} else {
				node.jjtGetChild(i).jjtAccept(this, data);
			}
		}

		buf.setNewlineHeading("");
		if (node.level == 1 && isTagNeed) {
			buf.appendNewline();
		}

		return null;
	}

	public Object visit(WikiList node, Object data) {

		buf.appendNewline();

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
					startList(curtype, curlevel);

				// downward level change
				if (curlevel < prelevel) {
					for (int j = curlevel; j < prelevel; j++) {
						if (level[j] != 0) {
							endList(pretype, prelevel);
							level[j] = 0;
						}
					}
					if (level[curlevel - 1] != curtype)
						startList(curtype, curlevel);
				}

				// not level change but type change
				if (curlevel == prelevel && curtype != pretype) {
					endList(pretype, prelevel);
					startList(curtype, curlevel);
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

		endAllList();
		return null;
	}

	public Object visit(WikiListMember node, Object data) {

		buf.setNewlineHeading(getRepeatedString(INDENT, node.level * 2));
		buf.appendNewline();
		if (node.type == GenerateNodeHelper.LIST_TYPE_NUMERICAL) {
			for (int i = 0; i < node.level - 1; i++) {
				if (listIndex[i] != 0)
					buf.append(listIndex[i] + ".");
			}
			buf.append((++listIndex[node.level - 1]) + ".");
		} else {
			buf.append("-");
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	public Object visit(WikiDefineList node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	public Object visit(WikiDefinedWord node, Object data) {
		if (node.jjtGetNumChildren() > 0) {
			buf.appendNewline();
			buf.append("[[");
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				node.jjtGetChild(i).jjtAccept(this, data);
			}
			buf.append("]]");
		}
		return null;
	}

	public Object visit(WikiExplanationWord node, Object data) {
		buf.setNewlineHeading("\t");
		buf.appendNewline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf.setNewlineHeading("");
		return null;
	}

	public Object visit(WikiPreshaped node, Object data) {

		buf.appendNewline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			buf.append("\t");
			node.jjtGetChild(i).jjtAccept(this, data);
			buf.appendNewline();
		}
		return null;
	}

	public Object visit(WikiTable node, Object data) {
		processTable(node, data);
		return null;
	}

	public Object visit(WikiTablecolumn node, Object data) {

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf.append("|");
		return null;
	}

	public Object visit(WikiTablemember node, Object data) {
		buf.appendNewline();
		buf.append("|");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	public Object visit(WikiCSVTable node, Object data) {
		processTable(node, data);
		return null;
	}

	public Object visit(WikiHeading node, Object data) {

		buf.appendNewline();

		buf.append("(");
		for (int i = 0; i < node.level - 1; i++) {
			buf.append(headerIndex[i] + "-");
		}
		buf.append((++headerIndex[node.level - 1]) + ")");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		for (int i = node.level; i < headerIndex.length; i++) {
			headerIndex[i] = 0;
		}
		return null;
	}

	public Object visit(WikiAlign node, Object data) {
		buf.appendNewline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	// do-nothing
	public Object visit(WikiFloatAlign node, Object data) {
		buf.appendNewline();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	public Object visit(WikiHorizontalline node, Object data) {
		buf.appendNewline();
		buf.appendRepeatString("-", LINESIZE / 2);
		return null;
	}

	// block plugin not executed for TextVisitor
	public Object visit(WikiBlockPlugin node, Object data) {
		buf.appendNewline();
		// PluginRequest prequest = VisitorUtils.createPluginRequest(node);
		// buf.append("#" + prequest.toString());
		return null;
	}

	public Object visit(WikiLetters node, Object data) {

		if (node.isEmail) {
			buf.append(node.letter);
		} else if (node.isURL) {
			buf.append(node.letter);
		} else if (node.isAnchor) {
			// do-nothing
		} else if (node.isWikiname) {
			processSecurityLink(node.letter, node.letter, null);
		} else if (node.isNewline) {
			buf.appendNewline();
		} else {
			buf.append(node.letter);
		}
		return null;
	}

	public Object visit(WikiAnyOther node, Object data) {
		buf.append(node.letter);
		return null;
	}

	public Object visit(WikiStrongItalic node, Object data) {
		buf.append("''");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		buf.append("''");
		return null;
	}

	public Object visit(WikiDeleteline node, Object data) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return null;
	}

	public Object visit(WikiAnnotation node, Object data) {
		buf.append("(*" + node.num + ")");
		return null;
	}

	public Object visit(WikiInterwiki node, Object data) {
		buf.append("[[" + node.image + "]]");
		return null;
	}

	public Object visit(WikiLink node, Object data) {

		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.LINK_DELIMITER);
		buf.appendLink(s[0], s[1]);
		return null;
	}

	public Object visit(WikiAlias node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ALIAS_DELIMITER);

		if (node.islink) {
			buf.appendLink(s[0], s[1]);
		} else {
			String[] t = GenerateNodeHelper.split(s[1],
					GenerateNodeHelper.ANCHOR_MARK);
			if (t[0] == null || t[0].equals("")) {
				buf.appendLink(s[0], GenerateNodeHelper.ANCHOR_MARK + t[1]);
			} else {
				processSecurityLink(t[0], s[0], t[1]);
			}
		}
		return null;
	}

	public Object visit(WikiPagename node, Object data) {
		String[] s = GenerateNodeHelper.split(node.image,
				GenerateNodeHelper.ANCHOR_MARK);
		processSecurityLink(s[0], s[0], s[1]);
		return null;
	}

	public Object visit(WikiInlinePlugin node, Object data) {

		String name = node.name;

		boolean ispluginused = false;
		int inlinestart = 0;
		// PluginRequest prequest = VisitorUtils.createPluginRequest(node);
		// if (prequest.getArgs() != null)
		// inlinestart = 1;
		//
		// String childstr = getChildString(node, data, inlinestart);
		// prequest.setChild(childstr);
		//
		// if (config.isPluginLoaded(name)) {
		// Plugin plugin = config.getPlugin(name);
		// try {
		// buf
		// .append((String) plugin.service(request, response,
		// prequest));
		// ispluginused = true;
		// } catch (PluginException pe) {
		// log.error(pe.getMessage());
		// } catch (Exception e) {
		// e.printStackTrace();
		// // some exception handling framework needed
		// }
		// }
		//
		// if (!ispluginused) {
		// buf.append("&" + prequest.toString() + ";");
		// }
		return null;
	}

	public Object visit(WikiArgs node, Object data) {
		return null;
	}

	public Object visit(WikiErrors node, Object data) {
		appendError(node.letter);
		return null;
	}

	private void appendError(String str) {
		buf.append(" *(" + str + ")* ");
	}

	private void processSecurityLink(String pagename, String body, String anchor) {
		// try {
		// if (ctx.isPageExist(pagename, request)) {
		// Resource resource = ctx.getResource(request, pagename);
		// if (resource.isFolder()) {
		// if (!pagename.endsWith("/"))
		// pagename = pagename + "/";
		// }
		// URL url = ctx.getURLByName(pagename, request);
		// if (anchor == null || "".equals(anchor)) {
		// buf.appendLink(body, url.toString());
		// } else {
		// buf.appendLink(body, url.toString()
		// + GenerateNodeHelper.ANCHOR_MARK + anchor);
		// }
		// } else {
		// URL url = ctx.getCreatePageURL(pagename, request);
		// buf.appendLink("?" + body, url.toString());
		// }
		// } catch (TgwSecurityException tse) {
		// buf.append(body);
		// }
	}

	private void startList(int type, int level) {
		// buf.appendNewline();
	}

	private void endList(int type, int level) {
		if (type == GenerateNodeHelper.LIST_TYPE_NUMERICAL) {
			listIndex[level - 1] = 0;
		}
	}

	private void endAllList() {
		for (int i = 0; i < listIndex.length; i++) {
			listIndex[i] = 0;
		}
		buf.setNewlineHeading("");
	}

	private String getChildString(SimpleNode node, Object data, int idx) {
		int start = 0; // buf.nextIndex();
		String childstr = null;
		for (int i = idx; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		int end = 0; // buf.nextIndex();
		if (start != end)
			childstr = buf.cut(start, end);
		return childstr;
	}

	private void processTable(SimpleNode node, Object data) {

		VisitorUtils.prepareWikiTable(node, data);
		int prenum = 0;
		int start = 0; // buf.nextIndex();
		int end = 0; // buf.nextIndex();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof WikiTablemember) {
				if (node.jjtGetChild(i).jjtGetNumChildren() != prenum) {
					if (prenum != 0) {
						adjust(start, end);
					}
					// start = buf.nextIndex();

				}
			} else { // in case WikiError
				adjust(start, end);
				prenum = 0;
				continue;
			}
			prenum = node.jjtGetChild(i).jjtGetNumChildren();
			node.jjtGetChild(i).jjtAccept(this, data);
			// end = buf.nextIndex();
		}
		adjust(start, end);
	}

	private void adjust(int start, int end) {

		if (end > start) {
			String str = buf.cut(start, end);
			String[] lines = str.split("\n");
			int maxlength = getMaxlength(lines);
			int[] maxcolsize = getMaxcolsize(lines);

			String decorator = getRepeatedString("-", maxlength);

			buf.appendNewline();
			buf.append(decorator);
			buf.appendNewline();
			for (int i = 0; i < lines.length; i++) {
				if (!lines[i].equals("")) {
					String[] cols = lines[i].split("\\|");
					for (int j = 0; j < cols.length; j++) {
						String col = cols[j];
						buf.append(col);
						buf.appendRepeatString(" ", maxcolsize[j]
								- col.getBytes().length);
						buf.append("|");
					}
					buf.appendNewline();
					buf.append(decorator);
					buf.appendNewline();
				}
			}
		}
	}

	private int getMaxlength(String[] lines) {
		int maxlength = 0;
		for (int i = 0; i < lines.length; i++) {
			int size = lines[i].getBytes().length;
			if (maxlength < size) {
				maxlength = size;
			}
		}
		return maxlength;
	}

	private int[] getMaxcolsize(String[] lines) {

		int[] maxcolsize = null;
		boolean first = true;
		for (int i = 0; i < lines.length; i++) {
			if (!"".equals(lines[i])) {
				String[] cols = lines[i].split("\\|");
				if (first) {
					maxcolsize = new int[cols.length];
					first = false;
				}

				for (int j = 0; j < cols.length; j++) {
					int size = cols[j].getBytes().length;
					if (maxcolsize[j] < size) {
						maxcolsize[j] = size;
					}
				}
			}
		}
		return maxcolsize;
	}

	private String getRepeatedString(String str, int num) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < num; i++) {
			buf.append(str);
		}
		return buf.toString();
	}
}
