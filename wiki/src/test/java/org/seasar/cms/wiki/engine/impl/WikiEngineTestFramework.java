package org.seasar.cms.wiki.engine.impl;

import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.util.WikiStringUtils;
import org.seasar.extension.unit.S2TestCase;

public class WikiEngineTestFramework extends S2TestCase {

	protected WikiEngine engine;

	@Override
	protected void setUp() throws Exception {
		include("wikiengine.dicon");
	}

	public void testTest() {
		System.out.println("fake");
	}

	public void assertWikiEquals(String expected, String actual) {
		expected = WikiStringUtils.removeCarriageReturn(expected);
		expected = WikiStringUtils.removeLineBreak(expected);
		expected = expected.trim();
		actual = WikiStringUtils.removeCarriageReturn(actual);
		actual = WikiStringUtils.removeLineBreak(actual);
		actual = actual.trim();
		assertEquals(expected, actual);
	}
}
