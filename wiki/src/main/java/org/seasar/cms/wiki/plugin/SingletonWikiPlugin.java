package org.seasar.cms.wiki.plugin;

import org.seasar.cms.wiki.engine.WikiContext;

public interface SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child);
	
}
