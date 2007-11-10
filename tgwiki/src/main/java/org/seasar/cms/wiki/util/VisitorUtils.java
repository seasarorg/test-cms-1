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

import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.SimpleNode;
import org.seasar.cms.wiki.parser.WikiArgs;
import org.seasar.cms.wiki.parser.WikiExcerpt;
import org.seasar.cms.wiki.parser.WikiFloatAlign;
import org.seasar.cms.wiki.parser.WikiLetters;
import org.seasar.cms.wiki.parser.WikiStrongItalic;

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

	private VisitorUtils(){		
	}
	
	/**
	 * 引用のタグを開始するかを判断する為のメソッド
	 * 自分の親ノードにて判断する
	 * @param node
	 * @return
	 */
	public static boolean isExcerptStartNeeded(WikiExcerpt node) {
		WikiExcerpt parent = NodeUtils.parent(node, WikiExcerpt.class);
		if (parent != null) {
			if (node.level <= parent.level) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ボールドやイタリックの場合に、前後にマークアップ用以外の「'」がある場合に、
	 * それを表示するためのメソッド
	 * WikiStrongItalic の prelevel と postlevel は Wiki.jjt 
	 * より、「2」か「3」か「5」が前提となる
	 * 
	 * @param node
	 * @param pre
	 * @return
	 */
	public static String getAppendString(WikiStrongItalic node, boolean pre) {
		int num = node.prelevel - node.postlevel;
		if (!pre) {
			num = 0 - num;
		}
		if (num <= 0) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		while (num > 0) {
			buf.append(STRONGITALIC_MARK);
			num--;
		}
		return buf.toString();
	}

	/**
	 * WikiStrongItalic が強調であるかを返す
	 * ノードの小さい方のマークアップの数にて判断する
	 * @param node
	 * @return
	 */
	public static boolean isBold(WikiStrongItalic node) {
		int level = (node.prelevel > node.postlevel) ? node.postlevel
				: node.prelevel;
		return (level == BOLD_NUM || level == BOLDITALIC_NUM) ? true : false;
	}

	/**
	 * WikiStrongItalic がイタリックであるかを返す
	 * ノードの小さい方のマークアップの数にて判断する
	 * @param node
	 * @return
	 */
	public static boolean isItalic(WikiStrongItalic node) {
		int level = (node.prelevel > node.postlevel) ? node.postlevel
				: node.prelevel;
		return (level == ITALIC_NUM || level == BOLDITALIC_NUM) ? true : false;
	}

	
	/**
	 * 子要素が WikiArgs の場合、その中身の引数を返す
	 * WikiArgs は子要素の中の最初の子供であることを前提としている
	 * @param node
	 * @return
	 */
	public static String[] getArgs(Node node) {
		if (node.jjtGetNumChildren() > 0
				&& node.jjtGetChild(0) instanceof WikiArgs) {
			WikiArgs child = (WikiArgs) node.jjtGetChild(0);
			return child.args;
		}
		return null;
	}

	/**
	 * 子要素に Anchor (WikiLetters) があれば、その値を返す
	 * 複数の Anchor 要素があれば、最初のものを返す
	 * @param node
	 * @return
	 */
	public static String getAnchorId(SimpleNode node) {
		for (WikiLetters l : NodeUtils.find(node, WikiLetters.class)) {
			if (l.isAnchor) {
				return l.letter;
			}
		}
		return null;
	}

	/**
	 * 子要素の一番最後に Anchor 要素を追加する
	 * @param node
	 * @param letter
	 */
	public static void setAnchor(SimpleNode node, String letter) {
		WikiLetters letters = new WikiLetters(-1);
		letters.letter = letter;
		letters.isAnchor = true;
		node.jjtAddChild(letters, node.jjtGetNumChildren());
	}

	/**
	 * FloatAlign 要素の HTML スタイルを返す
	 * 必ず image に値が設定されていることを前提としている
	 * @param node
	 * @return
	 */
	public static String getFloatStyle(WikiFloatAlign node) {
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
			style += "width:" + widthStr + "px;";
		}
		return style;
	}
}
