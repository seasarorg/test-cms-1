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
package org.seasar.cms.wiki.plugin.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.ChildPluginExecuter;
import org.seasar.cms.wiki.plugin.PluginExecuter;

public class PluginExecuterImpl implements PluginExecuter {

	/**
	 * namespace 毎に pluginExecuter のマップが存在する
	 */
	private Map<String, Map<String, ChildPluginExecuter>> executers = new HashMap<String, Map<String, ChildPluginExecuter>>();

	public Collection<ChildPluginExecuter> getPluginExecuters(String namespace) {
		Map<String, ChildPluginExecuter> plugins = executers.get(namespace);
		if (plugins == null) {
			return null;
		}
		return Collections.unmodifiableCollection(plugins.values());
	}

	public void addChildExecuter(ChildPluginExecuter executer) {
		String namespace = executer.getNamespace();
		Map<String, ChildPluginExecuter> map = executers.get(namespace);
		if (map == null) {
			map = new HashMap<String, ChildPluginExecuter>();
			executers.put(namespace, map);
		}
		for (String pluginName : executer.getPluginNames()) {
			map.put(pluginName, executer);
		}
		executer.setParent(this);
	}

	public void inline(WikiContext ctx, String name, String[] args, String child) {
		String namespace = ctx.getNamespace();
		PluginExecuter executer = getExecuter(namespace, name);
		if (executer == null) {
			return;
		}
		executer.inline(ctx, name, args, child);
	}

	public void block(WikiContext ctx, String name, String[] args, String child) {
		PluginExecuter executer = getExecuter(ctx.getNamespace(), name);
		if (executer == null) {
			return;
		}
		executer.block(ctx, name, args, child);
	}

	private PluginExecuter getExecuter(String outputType, String name) {
		Map<String, ChildPluginExecuter> map = executers.get(outputType);
		if (map == null) {
			return null;
		}
		if (map.get(name) == null) {
			return map.get("*");
		}
		return map.get(name);
	}
}
