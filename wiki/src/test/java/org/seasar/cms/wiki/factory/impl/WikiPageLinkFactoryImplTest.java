package org.seasar.cms.wiki.factory.impl;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;
import org.seasar.cms.wiki.engine.plugin.WikiPageLink;
import org.seasar.cms.wiki.factory.WikiPageLinkFactory;

public class WikiPageLinkFactoryImplTest extends WikiEngineTestFramework {

	public void testEval() {
		String actual = engine.evaluate("[[a]]", new WikiContext());
		String expected = "<p><a href=\"a\">a</a></p>";
		assertWikiEquals(expected, actual);
	}

	public void testEval2() {
		String actual = engine.evaluate("[[a>b]]", new WikiContext());
		String expected = "<p><a href=\"b\">a</a></p>";
		assertWikiEquals(expected, actual);
	}

	public void testCreate() {
		engine.setLinkFactory(new CreationLinkFactory());
		String actual = engine.evaluate("[[a]]", new WikiContext());
		String expected = "<p><a href=\"creationLink\">?</a><span class=\"notexist\">a</span></p>";
		assertWikiEquals(expected, actual);
	}

	public static class CreationLinkFactory implements WikiPageLinkFactory {
		public WikiPageLink create(WikiContext context, String pagename,
				String body, String anchor) {
			WikiPageLink link = new WikiPageLink("?", "creationLink");
			link.setPostMsg(pagename);
			return link;
		}
	}
}
