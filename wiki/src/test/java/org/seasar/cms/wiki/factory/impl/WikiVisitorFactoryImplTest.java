package org.seasar.cms.wiki.factory.impl;

import java.io.StringWriter;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.renderer.HtmlWikiVisitor;

public class WikiVisitorFactoryImplTest extends WikiEngineTestFramework {

	private WikiVisitorFactoryImpl factory;

	public void testCreate() {
		assertNotNull(getComponent(HtmlWikiVisitor.class));
		assertNotNull(getComponent("htmlVisitor"));
		WikiParserVisitor visitor = factory.create(new WikiContext(),
				new StringWriter());
		WikiParserVisitor visitor2 = factory.create(new WikiContext(),
				new StringWriter());

		// visitor and visitor2 are not same instance.
		assertNotSame(visitor2, visitor);
	}

}
