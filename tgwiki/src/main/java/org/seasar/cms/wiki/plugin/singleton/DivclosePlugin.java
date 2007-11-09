package org.seasar.cms.wiki.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;

public class DivclosePlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		return "</div>";
	}
}
