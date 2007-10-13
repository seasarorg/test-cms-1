package org.seasar.cms.wiki.factory.impl;

import org.seasar.cms.wiki.engine.plugin.WikiPageLink;
import org.seasar.cms.wiki.factory.WikiLinkFactory;

public class WikiLinkFactoryImpl implements WikiLinkFactory {

	public WikiPageLink create(String pagename, String body, String anchor) {
		String url = "" + pagename;
		if (anchor != null && anchor.length() > 0) {
			url += "#" + anchor;
		}
		return new WikiPageLink(body, url, null);
	}
}
