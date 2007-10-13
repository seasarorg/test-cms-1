package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;

public class BrPluginTest extends PluginTestFramework {

	public void testEval() {
		assertEquals("<p><br/></p>", engine.evaluate("&br;", new WikiContext()));
	}

}
