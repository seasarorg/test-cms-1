package org.seasar.cms.wiki.util;

import junit.framework.TestCase;

/**
 * {@link AnchorUtils}
 * @author someda
 *
 */
public class AnchorUtilsTest extends TestCase {

	/**
	 * {@link AnchorUtils#setToHeading(String)}
	 */
	public void testSetToHeading() {

		String contents = "*h\nhoge";
		
		// 無かったらアンカーがつく
		String actual = AnchorUtils.setToHeading(contents);
		String[] result = actual.split("\n");
		int idx = result[0].indexOf("[#tg");
		assertEquals(2, idx);
		
		// 既にアンカーがあれば何もしない
		actual = AnchorUtils.setToHeading(result[0]);
		assertEquals(result[0],actual.replaceAll("\n", ""));
	}
}
