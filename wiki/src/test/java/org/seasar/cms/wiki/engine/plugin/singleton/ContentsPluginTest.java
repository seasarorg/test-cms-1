package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class ContentsPluginTest extends WikiEngineTestFramework {

	private String wiki = "#contents\n*A1[#likn]*aa\n**B1\n**B2\n";

	public void testEval() {

		String actual = engine.evaluate(wiki, new WikiContext());	
		
		System.out.println(actual);
	}
}
