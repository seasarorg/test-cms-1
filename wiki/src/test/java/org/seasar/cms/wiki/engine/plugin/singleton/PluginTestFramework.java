package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.extension.unit.S2TestCase;

public class PluginTestFramework extends S2TestCase {

	protected WikiEngine engine;

	@Override
	protected void setUp() throws Exception {
		include("wikiengine.dicon");
	}
	
	public void testTest(){
		System.out.println("fake");
	}
}
