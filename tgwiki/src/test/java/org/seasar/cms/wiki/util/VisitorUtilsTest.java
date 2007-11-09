package org.seasar.cms.wiki.util;

import org.seasar.cms.wiki.parser.WikiExcerpt;
import org.seasar.cms.wiki.parser.WikiStrongItalic;

import junit.framework.TestCase;

/**
 * {@link VisitorUtils}
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

	public void testIsBold() {

		WikiStrongItalic node = new WikiStrongItalic(0);

		
		int[] prepattern = {2,3,5};
		int[] postpattern = {2,3,5};
		
		for(int pre : prepattern){			
			for(int post : postpattern){
				
				node.prelevel = pre;
				node.postlevel = post;
				
				boolean actual = VisitorUtils.isBold(node);
				
				
				
			}			
		}
		
		
		
		node.prelevel = 2;
		node.postlevel = 2;

		// ボールド
		assertTrue(VisitorUtils.isBold(node));

		// ボールド
		node.postlevel = 5;
		assertTrue(VisitorUtils.isBold(node));

		// ボールド
		node.postlevel = 3;
		assertTrue(VisitorUtils.isBold(node));

		// ボールド
		node.postlevel = 2;
		node.prelevel = 3;
		assertTrue(VisitorUtils.isBold(node));
		
		// ボールド		
		node.prelevel = 5;
		assertTrue(VisitorUtils.isBold(node));
		
		node.prelevel = 3;
		node.postlevel = 3;
		
		
	}

	public void testIsItalic() {
		fail("Not yet implemented");
	}

	public void testGetArgs() {
		fail("Not yet implemented");
	}

	public void testGetAnchorId() {
		fail("Not yet implemented");
	}

	public void testSetAnchor() {
		fail("Not yet implemented");
	}

	public void testGetFloatStyle() {
		fail("Not yet implemented");
	}

}
