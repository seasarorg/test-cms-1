package org.seasar.cms.wiki.engine.plugin;

import org.seasar.cms.wiki.engine.WikiContext;

public interface PluginExecuter {

	public void inline(WikiContext ctx, String name, String[] args, String child);

	public void block(WikiContext ctx, String name, String[] args, String child);

}
