package org.seasar.cms.wiki.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.AbstractChildPluginExecuter;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;
import org.seasar.cms.wiki.renderer.WikiWriterVisitor;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;

public class SingletonPluginExecuter extends AbstractChildPluginExecuter {

	private Map<String, SingletonWikiPlugin> plugins = new HashMap<String, SingletonWikiPlugin>();

	public String getNamespace() {
		return "html";
	}

	public void setContainer(S2Container container) {
		ComponentDef[] defs = container
				.findComponentDefs(SingletonWikiPlugin.class);
		for (ComponentDef def : defs) {
			addPlugin(def.getComponentName(), (SingletonWikiPlugin) def
					.getComponent());
		}
	}

	public List<String> getPluginNames() {
		List<String> pluginNames = new ArrayList<String>();
		pluginNames.addAll(plugins.keySet());
		return pluginNames;
	}

	public void addPlugin(String name, SingletonWikiPlugin plugin) {
		plugins.put(name, plugin);
	}

	public void block(WikiContext ctx, String name, String[] args, String child) {
		doService(ctx, name, args, child);
	}

	public void inline(WikiContext ctx, String name, String[] args, String child) {
		doService(ctx, name, args, child);
	}

	// --- private methods --------

	private void doService(WikiContext ctx, String name, String[] args,
			String child) {
		try {
			String result = plugins.get(name).render(ctx, args, child);
			WikiWriterVisitor visitor = (WikiWriterVisitor) ctx.getVisitor();
			visitor.write(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
