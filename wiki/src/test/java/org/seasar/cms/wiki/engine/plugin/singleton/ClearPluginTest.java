package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class ClearPluginTest extends WikiEngineTestFramework {

	public void testEval() {
		String expect = "<div style=\"clear:both;padding-bottom:1px;\"></div>";
		assertWikiEquals(expect, engine.evaluate("#clear", new WikiContext()));
	}
}
