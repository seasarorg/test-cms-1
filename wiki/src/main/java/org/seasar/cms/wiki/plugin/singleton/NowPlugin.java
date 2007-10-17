package org.seasar.cms.wiki.plugin.singleton;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;

public class NowPlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss (EEE)")
				.format(new Date());
	}
}
