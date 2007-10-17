package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class SizePluginTest extends WikiEngineTestFramework {

	public void testEval() {
		String actual = engine.evaluate("&size(10){child};", new WikiContext());
		String expected = "<p><span style=\"font-size:10px\">child</span></p>";

		
		assertWikiEquals(expected, actual);
	}

}
