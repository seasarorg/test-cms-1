package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;

public class SizePluginTest extends PluginTestFramework {

	public void testEval() {
		String actual = engine.evaluate("&size(10){child};", new WikiContext());
		String expected = "<p><span style=\"font-size:10px\">child</span></p>";
		assertEquals(expected, actual);
	}

}
