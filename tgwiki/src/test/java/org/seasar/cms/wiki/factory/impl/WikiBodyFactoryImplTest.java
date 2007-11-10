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
package org.seasar.cms.wiki.factory.impl;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;
import org.seasar.cms.wiki.factory.WikiBodyFactory;

public class WikiBodyFactoryImplTest extends WikiEngineTestFramework {

	public void testEval() {
		WikiContext context = new WikiContext();
		List<String> keys = new ArrayList<String>();
		keys.add("test");
		context.put(WikiBodyFactory.KEY, keys);
		String actual = engine.evaluate("test", context);
		String expected = "<p><span class=\"highlight\">test</span></p>";
		assertWikiEquals(expected, actual);

		actual = engine.evaluate("tset", context);
		expected = "<p>tset</p>";
		assertWikiEquals(expected, actual);
	}
	
}
