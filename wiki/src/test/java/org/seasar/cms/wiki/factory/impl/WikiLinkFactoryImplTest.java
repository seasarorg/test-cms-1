package org.seasar.cms.wiki.factory.impl;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.plugin.singleton.PluginTestFramework;

public class WikiLinkFactoryImplTest extends PluginTestFramework {

	public void testEval() {
		String actual = engine.evaluate("[[a]]", new WikiContext());
		String actual2 = engine.evaluate("[[a>b]]", new WikiContext());
		System.out.println(actual);
		System.out.println(actual2);
	}

}
