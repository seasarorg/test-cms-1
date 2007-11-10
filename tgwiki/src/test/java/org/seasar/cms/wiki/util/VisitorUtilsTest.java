/*
 * Copyright 2004-2007 the Seasar Foundation and the Others..
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

import junit.framework.TestCase;

import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.SimpleNode;
import org.seasar.cms.wiki.parser.WikiArgs;
import org.seasar.cms.wiki.parser.WikiExcerpt;
import org.seasar.cms.wiki.parser.WikiFloatAlign;
import org.seasar.cms.wiki.parser.WikiLetters;
import org.seasar.cms.wiki.parser.WikiStrongItalic;

/**
 * {@link VisitorUtils}
 * 
 * @author someda
 */
public class VisitorUtilsTest extends TestCase {

	/**
	 * {@link VisitorUtils#isExcerptStartNeeded(WikiExcerpt)}
	 */
	public void testIsExcerptStartNeeded() {

		WikiExcerpt node = new WikiExcerpt(0);
		// 親がいない
		assertTrue(VisitorUtils.isExcerptStartNeeded(node));

		WikiExcerpt parent = new WikiExcerpt(1);
		node.jjtSetParent(parent);

		// 親が自分よりもレベルが低い
		parent.level = 1;
		node.level = 2;
		assertTrue(VisitorUtils.isExcerptStartNeeded(node));

		// 親が自分よりもレベルが高い
		parent.level = 3;
		assertFalse(VisitorUtils.isExcerptStartNeeded(node));

		// レベルが同じ
		parent.level = 2;
		assertFalse(VisitorUtils.isExcerptStartNeeded(node));
	}

	/**
	 * {@link VisitorUtils#getAppendString(WikiStrongItalic, boolean)}
	 */
	public void testGetAppendString() {

		WikiStrongItalic node = new WikiStrongItalic(0);

		node.prelevel = 3;
		node.postlevel = 3;

		// 同じ場合はどちらも null
		assertNull(VisitorUtils.getAppendString(node, true));
		assertNull(VisitorUtils.getAppendString(node, false));

		// 前が少ない場合は後に「'」が一つつく
		node.prelevel = 2;
		assertNull(VisitorUtils.getAppendString(node, true));
		assertEquals("'", VisitorUtils.getAppendString(node, false));

		// 後が少ない場合は前に「'」が一つつく
		node.prelevel = 3;
		node.postlevel = 2;
		assertEquals("'", VisitorUtils.getAppendString(node, true));
		assertNull(VisitorUtils.getAppendString(node, false));

		// 前がみっつ多い場合には、前に「'」が三つつく
		node.prelevel = 5;
		assertEquals("'''", VisitorUtils.getAppendString(node, true));
		assertNull(VisitorUtils.getAppendString(node, false));
	}

	/**
	 * {@link VisitorUtils#isBold(WikiStrongItalic)}
	 */
	public void testIsBold() {

		WikiStrongItalic node = new WikiStrongItalic(0);

		int[] prepattern = { 2, 3, 5 };
		int[] postpattern = { 2, 3, 5 };

		for (int pre : prepattern) {
			for (int post : postpattern) {
				node.prelevel = pre;
				node.postlevel = post;
				boolean actual = VisitorUtils.isBold(node);
				if (node.prelevel == 2 || node.postlevel == 2) {
					// どちらかが 2 であれば、確実に bold
					assertTrue(actual);
				} else if (node.prelevel == 5 && node.postlevel == 5) {
					// 両方とも 5 ならば bold
					assertTrue(actual);
				} else {
					assertFalse(actual);
				}
			}
		}
	}

	/**
	 * {@link VisitorUtils#isItalic(WikiStrongItalic)}
	 */
	public void testIsItalic() {

		WikiStrongItalic node = new WikiStrongItalic(0);

		int[] prepattern = { 2, 3, 5 };
		int[] postpattern = { 2, 3, 5 };
		for (int post : postpattern) {

			for (int pre : prepattern) {
				node.prelevel = pre;
				node.postlevel = post;
				boolean actual = VisitorUtils.isItalic(node);
				if (node.prelevel != 2 && node.postlevel != 2) {
					// どちらも 2 以外であればイタリック
					assertTrue(actual);
				} else {
					// そうでない場合はボールド
					assertFalse(actual);
				}
			}
		}
	}

	/**
	 * {@link VisitorUtils#getArgs(Node)}
	 */
	public void testGetArgs() {

		SimpleNode node = new SimpleNode(0);

		// 空の場合何も返らない
		String[] actual = VisitorUtils.getArgs(node);
		assertNull(actual);

		// 正常系
		WikiArgs args = new WikiArgs(0);
		args.args = new String[] { "a", "b" };
		node.jjtAddChild(args, 0);
		actual = VisitorUtils.getArgs(node);

		assertEquals("a", actual[0]);
		assertEquals("b", actual[1]);

		// WikiArgs は一つ目の子供でないといけない
		WikiStrongItalic child = new WikiStrongItalic(0);
		node.jjtAddChild(child, 0);
		node.jjtAddChild(args, 1);
		actual = VisitorUtils.getArgs(node);
		assertNull(actual);
	}

	/**
	 * {@link VisitorUtils#getAnchorId(SimpleNode)}
	 */
	public void testGetAnchorId() {
		
		SimpleNode node = new SimpleNode(0);
		
		// 子供がいない場合は null
		String actual = VisitorUtils.getAnchorId(node);
		assertNull(actual);
		
		// anchor があれば、その値を返す
		WikiLetters letters = new WikiLetters(1);
		letters.isAnchor = true;
		letters.letter = "anchor";
		node.jjtAddChild(letters, 0);		
		actual = VisitorUtils.getAnchorId(node);
		assertEquals("anchor",actual);
		
		// 別のノードがいても最初にマッチしたものが返る
		WikiStrongItalic child = new WikiStrongItalic(0);
		node.jjtAddChild(child, 0);
		node.jjtAddChild(letters, 1);
		actual = VisitorUtils.getAnchorId(node);
		assertEquals("anchor",actual);
		
		// 複数の anchor があれば最初のものが返る
		WikiLetters another = new WikiLetters(2);
		another.isAnchor = true;
		another.letter = "another";
		node.jjtAddChild(another, 2);
		actual = VisitorUtils.getAnchorId(node);
		assertEquals("anchor",actual);
	}

	/**
	 * {@link VisitorUtils#setAnchor(SimpleNode, String)}
	 */
	public void testSetAnchor() {
		SimpleNode node = new SimpleNode(0);				
		VisitorUtils.setAnchor(node, "anchor");		
		WikiLetters l = (WikiLetters)node.jjtGetChild(node.jjtGetNumChildren()-1);		
		assertEquals("anchor",l.letter);		
		
	}

	/**
	 * {@link VisitorUtils#getFloatStyle(WikiFloatAlign)}
	 */
	public void testGetFloatStyle() {
		
		WikiFloatAlign node = new WikiFloatAlign(0);
		node.image = "FLEFT:";
		String actual = VisitorUtils.getFloatStyle(node);
		assertEquals("float:left;",actual);
		
		node.image = "FLEFT(100);";		
		actual = VisitorUtils.getFloatStyle(node);
		assertEquals("float:left;width:100px;",actual);
		
		node.image = "FRIGHT:";
		actual = VisitorUtils.getFloatStyle(node);
		assertEquals("float:right;",actual);
		
		node.image = "FRIGHT(200):";
		actual = VisitorUtils.getFloatStyle(node);
		assertEquals("float:right;width:200px;",actual);
	}

}
