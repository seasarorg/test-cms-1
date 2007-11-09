package org.seasar.cms.wiki.plugin.singleton;

import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class BrPluginTest extends WikiEngineTestFramework {

	public void testEval() {
		assertWikiEquals("<p><br/></p>", engine.evaluate("&br;"));
		assertWikiEquals("<br/>", engine.evaluate("#br"));
	}

}
