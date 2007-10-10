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
package org.seasar.cms.wiki.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.SimpleNode;
import org.seasar.cms.wiki.parser.WikiArgs;
import org.seasar.cms.wiki.parser.WikiCSVTable;
import org.seasar.cms.wiki.parser.WikiExcerpt;
import org.seasar.cms.wiki.parser.WikiLetters;
import org.seasar.cms.wiki.parser.WikiStrongItalic;
import org.seasar.cms.wiki.parser.WikiTable;
import org.seasar.cms.wiki.parser.WikiTablecolumn;
import org.seasar.cms.wiki.parser.WikiTablemember;

/**
 * VisitorUtils class Provides node object related tasks which is common during
 * various visitor, plugin execution. The difference from WikiHelper is that
 * this class quite depends on node object structure itself.
 * 
 * 
 * @author someda
 */
public class VisitorUtils {

	private static final String STRONGITALIC_MARK = "'";

	private static final int BOLD_NUM = 2;

	private static final int ITALIC_NUM = 3;

	private static final int BOLDITALIC_NUM = 5;

	private static final String[] TAG_ESCAPE = { "<", "&lt;", ">", "&gt;" };

	private static final String[] OTHER_ESCAPE = { "&", "&amp;" };

	// ----- [Start] Excerpt related methods -----

	public static boolean isExcerptStartNeeded(WikiExcerpt node) {
		Node parent = node.jjtGetParent();
		if (parent instanceof WikiExcerpt) {
			int plevel = ((WikiExcerpt) parent).level;
			if (node.level <= plevel) {
				return false;
			}
		}
		return true;
	}

	// ----- [Start] Table related methods -----
	/**
	 * Before accepting node tree, need to set node properties for colspan,
	 * rowspan.
	 * 
	 * The value of colspan or rowspan which might be set after trailing should
	 * be incremented before being used in visitor.
	 */
	public static void prepareWikiTable(SimpleNode node, Object data) {

		if (!(node instanceof WikiTable) && !(node instanceof WikiCSVTable))
			throw new IllegalArgumentException(
					"The class other than WikiTable or WikiCSVTable cannot be accepted.");

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (!(node.jjtGetChild(i) instanceof WikiTablemember)) {
				continue;
			}
			WikiTablemember member = (WikiTablemember) node.jjtGetChild(i);
			int colspannum = 0;
			for (int j = 0; j < member.jjtGetNumChildren(); j++) {
				WikiTablecolumn column = (WikiTablecolumn) member
						.jjtGetChild(j);
				if (column.iscolspan) {
					colspannum++;
				} else if (column.isrowspan) {
					int rowspannum = 1;
					for (int k = i - 1; k >= 0; k--) {
						// Access to previous WikiTablemember object to set
						// rowspan
						if (node.jjtGetChild(k) instanceof WikiTablemember) {
							WikiTablecolumn leftcolumn = (WikiTablecolumn) node
									.jjtGetChild(k).jjtGetChild(j);
							if (leftcolumn.isrowspan) {
								rowspannum++;
								continue;
							} else {
								leftcolumn.rowspannum = rowspannum;
								break;
							}
						} else { // in case WikiError
							break;
						}
					}
				} else {
					if (colspannum > 0) {
						column.colspannum = colspannum;
						colspannum = 0;
					}
				}
			}
		}
	}

	// ----- [End] Table related methods -----

	// ----- [Start] StrongItalic related methods -----

	public static String getAppendString(WikiStrongItalic node, boolean pre) {
		int num = node.prelevel - node.postlevel;
		if (!pre) {
			num = 0 - num;
		}
		if (num <= 0)
			return null;

		StringBuffer buf = new StringBuffer();
		while (num > 0) {
			buf.append(STRONGITALIC_MARK);
			num--;
		}
		return buf.toString();
	}

	public static boolean isBold(WikiStrongItalic node) {
		int level = (node.prelevel > node.postlevel) ? node.postlevel
				: node.prelevel;
		return (level == BOLD_NUM || level == BOLDITALIC_NUM) ? true : false;
	}

	public static boolean isItalic(WikiStrongItalic node) {
		int level = (node.prelevel > node.postlevel) ? node.postlevel
				: node.prelevel;
		return (level == ITALIC_NUM || level == BOLDITALIC_NUM) ? true : false;
	}

	// ----- [End] StrongItalic related methods -----

	// ----- [Start] BlockPlugin/InlinePlugin related methods -----

	public static String[] getArgs(Node node) {
		String args[] = null;
		if (node.jjtGetNumChildren() > 0
				&& node.jjtGetChild(0) instanceof WikiArgs) {
			WikiArgs child = (WikiArgs) node.jjtGetChild(0);
			args = child.args;
		}
		return args;
	}

	// ----- [End] BlockPlugin/InlinePlugin related methods -----

	// ----- HTMLWikiVisitor related
	public static String escape(String letter) {
		return replaceArray(letter, TAG_ESCAPE, true);
	}

	public static String getAnchorId(SimpleNode node) {
		String anchor = null;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof WikiLetters) {
				WikiLetters l = (WikiLetters) node.jjtGetChild(i);
				if (l.isAnchor) {
					anchor = l.letter;
					break;
				}
			}
		}
		return anchor;
	}

	// -- TextWikiVisitor related
	public static String unescape(String letter) {
		letter = replaceArray(letter, TAG_ESCAPE, false);
		letter = replaceArray(letter, OTHER_ESCAPE, false);
		return letter;
	}

	private static String replaceArray(String letter, String[] array,
			boolean forward) {

		if (letter == null)
			return "";
		String ret = letter;
		if (forward) {
			for (int i = 0; i < array.length - 1; i += 2) {
				ret = ret.replaceAll(array[i], array[i + 1]);
			}
		} else {
			for (int i = 1; i < array.length; i += 2) {
				ret = ret.replaceAll(array[i], array[i - 1]);
			}
		}
		return ret;
	}

}
