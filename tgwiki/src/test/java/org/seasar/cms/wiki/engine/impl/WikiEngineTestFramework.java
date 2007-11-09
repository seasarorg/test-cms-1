package org.seasar.cms.wiki.engine.impl;

import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.util.WikiStringUtils;
import org.seasar.extension.unit.S2TestCase;

public abstract class WikiEngineTestFramework extends S2TestCase {

	protected WikiEngine engine;

	@Override
	protected void setUp() throws Exception {
		include("wikiengine.dicon");
	}

	/**
	 * 改行は無視をしたマッチングを行う
	 * 
	 * @param expected
	 * @param actual
	 */
	public void assertWikiEquals(String expected, String actual) {
		expected = WikiStringUtils.removeCarriageReturn(expected);
		expected = WikiStringUtils.removeLineFeed(expected);
		expected = expected.trim();
		actual = WikiStringUtils.removeCarriageReturn(actual);
		actual = WikiStringUtils.removeLineFeed(actual);
		actual = actual.trim();
		assertEquals(expected, actual);
	}
}
