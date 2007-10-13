package org.seasar.cms.wiki.engine.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.plugin.WikiBodyEvaluator;
import org.seasar.cms.wiki.engine.plugin.singleton.PluginTestFramework;

public class WikiBodyEvaluatorImplTest extends PluginTestFramework {

	public void testEval() {
		WikiContext context = new WikiContext();
		List<String> keys = new ArrayList<String>();
		keys.add("test");
		context.put(WikiBodyEvaluator.KEY, keys);
		String actual = engine.evaluate("test", context);
		String expected = "<p><span class=\"highlight\">test</span></p>";
		assertEquals(expected, actual);
		
		actual = engine.evaluate("tset", context);
		expected = "<p>tset</p>";
		assertEquals(expected, actual);
	}
}
