package org.seasar.cms.wiki.plugin.singleton;

import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class ColorPluginTest extends WikiEngineTestFramework {

	public void testTest() {
		String expected = engine.evaluate("&color(red){child};");
		assertWikiEquals("<p><span style=\"color:red\">child</span></p>",
				expected);
	}

}
