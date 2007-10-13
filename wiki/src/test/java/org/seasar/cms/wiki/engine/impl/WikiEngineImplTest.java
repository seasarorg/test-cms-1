package org.seasar.cms.wiki.engine.impl;

import java.io.StringWriter;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.extension.unit.S2TestCase;

public class WikiEngineImplTest extends S2TestCase {

	protected WikiEngine engine;

	@Override
	protected void setUp() throws Exception {
		include("wikiengine.dicon");
	}

	public void testBasic() {
		System.out.println(engine.evaluate("&br;", new WikiContext()));

		StringWriter writer = new StringWriter();
		engine.merge("*h1", new WikiContext(), writer);
		System.out.println(":::" + writer.getBuffer().toString());
	}

}
