package org.seasar.cms.wiki.factory.impl;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;
import org.seasar.cms.wiki.factory.WikiBodyFactory;

public class WikiBodyEvaluatorImplTest extends WikiEngineTestFramework {

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
