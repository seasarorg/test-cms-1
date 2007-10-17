package org.seasar.cms.wiki.util;

import junit.framework.TestCase;

public class WikiStringUtilsTest extends TestCase {

	public void testEscape() {
		assertEquals("&lt;", WikiStringUtils.escape("<"));
	}

	public void testUnescape() {
		assertEquals("&lt;", WikiStringUtils.escape("<"));
	}

	
	
	
}
