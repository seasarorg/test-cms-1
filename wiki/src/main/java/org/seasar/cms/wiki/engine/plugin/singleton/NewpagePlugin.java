package org.seasar.cms.wiki.engine.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.plugin.SingletonWikiPlugin;

public class NewpagePlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		return "<div style=\"page-break-after:always;\"></div>";
	}
}
