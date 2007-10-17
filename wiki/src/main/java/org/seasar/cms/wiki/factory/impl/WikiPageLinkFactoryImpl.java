package org.seasar.cms.wiki.factory.impl;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiPageLinkFactory;
import org.seasar.cms.wiki.plugin.WikiPageLink;

public class WikiPageLinkFactoryImpl implements WikiPageLinkFactory {

	public WikiPageLink create(WikiContext context, String pagename,
			String body, String anchor) {
		String url = "" + pagename;
		if (anchor != null && anchor.length() > 0) {
			url += "#" + anchor;
		}
		return new WikiPageLink(body, url);
	}
}
