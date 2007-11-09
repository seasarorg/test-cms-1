package org.seasar.cms.wiki.plugin;

import java.util.List;

public interface ChildPluginExecuter extends PluginExecuter {

	public PluginExecuter getParent();

	public void setParent(PluginExecuter executer);

	public String getNamespace();

	public List<String> getPluginNames();

}
