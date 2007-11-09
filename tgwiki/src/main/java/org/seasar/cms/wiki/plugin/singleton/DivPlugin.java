package org.seasar.cms.wiki.plugin.singleton;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;

public class DivPlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		Map<String, String> attrs = new HashMap<String, String>();
		if (args.length > 0 && args[0] != null && args[0].length() > 0) {
			attrs.put("id", args[0]);
		}
		if (args.length > 1 && args[1] != null && args[1].length() > 0) {
			attrs.put("class", args[1]);
		}
		StringBuffer buf = new StringBuffer();
		buf.append("<div");
		for (String key : attrs.keySet()) {
			buf.append(" " + key + "=\"" + attrs.get(key) + "\"");
		}
		buf.append(">");
		return buf.toString();
	}
}
