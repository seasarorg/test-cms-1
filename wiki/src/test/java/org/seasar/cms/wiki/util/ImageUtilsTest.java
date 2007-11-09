package org.seasar.cms.wiki.util;

import junit.framework.TestCase;

public class ImageUtilsTest extends TestCase {

	public void testIsImage() {
		assertEquals(true, ImageUtils.isImage("hoge.jpg"));
		assertEquals(true, ImageUtils.isImage("hoge.jpeg"));
		assertEquals(false, ImageUtils.isImage("hoge.txt"));
		assertEquals(false, ImageUtils.isImage("hogejpg"));
	}

}
