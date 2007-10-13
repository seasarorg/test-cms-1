package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;

public class ClearPluginTest extends PluginTestFramework {

	public void testEval() {
		String expect = "<div style=\"clear:both;padding-bottom:1px;\"></div>";
		assertEquals(expect, engine.evaluate("#clear", new WikiContext()));
	}
}
