package org.seasar.cms.wiki.engine.impl;

import junit.framework.TestCase;

public class WikiEngineImplWithoutSeasar2Test extends TestCase {

	public void testGetInstance() {
		String result = WikiEngineImpl.getInstance().evaluate("*a");
		assertEquals("<h2>a</h2>", result);
	}

	public void testPlugin() {
		assertEquals("<p><br/></p>", WikiEngineImpl.getInstance().evaluate(
				"&br;"));
	}

}
