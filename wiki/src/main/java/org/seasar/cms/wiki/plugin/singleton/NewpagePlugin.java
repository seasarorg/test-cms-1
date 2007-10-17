package org.seasar.cms.wiki.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;

public class NewpagePlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		return "<div style=\"page-break-after:always;\"></div>";
	}
}
