/*
 * Copyright 2004-2007 the Seasar Foundation and the Others..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
