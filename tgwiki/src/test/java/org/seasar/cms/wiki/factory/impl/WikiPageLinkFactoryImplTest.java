package org.seasar.cms.wiki.factory.impl;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;
import org.seasar.cms.wiki.factory.WikiPageLink;
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
		assertEquals("pagename_postmsg", engine
				.getProperty("class.pagename.postmsg"));
		engine.setLinkFactory(new CreationLinkFactory());
		String actual = engine.evaluate("[[a]]");
		String expected = "<p><a href=\"creationLink\">?</a><span class=\"pagename_postmsg\">a</span></p>";
		assertWikiEquals(expected, actual);
	}

	public void testCreateHeading() {
		String actual = engine.evaluate("*h2", new WikiContext());
		String expected = "<h2>h2</h2>";
		assertWikiEquals(expected, actual);

		engine.setLinkFactory(new CreationLinkFactory());
		actual = engine.evaluate("*h2", new WikiContext());
		expected = "<h2>h2<a class=\"anchor_super\" href=\"url://edit\">edit</a></h2>";
		assertWikiEquals(expected, actual);
	}

	public static class CreationLinkFactory implements WikiPageLinkFactory {

		public WikiPageLink create(WikiContext context, String pagename,
				String body, String anchor) {
			WikiPageLink link = new WikiPageLink("?", "creationLink");
			link.setPostMsg(pagename);
			return link;
		}

		public WikiPageLink createEditLink(WikiContext context, String anchor) {
			return new WikiPageLink("edit", "url://edit");
		}
	}
}
