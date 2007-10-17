package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class BrPluginTest extends WikiEngineTestFramework {

	public void testEval() {
		assertWikiEquals("<p><br/></p>", engine.evaluate("&br;",
				new WikiContext()));
		assertWikiEquals("<br/>", engine.evaluate("#br",
				new WikiContext()));
	}

}
