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
