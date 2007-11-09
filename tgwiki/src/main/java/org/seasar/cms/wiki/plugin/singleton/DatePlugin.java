package org.seasar.cms.wiki.plugin.singleton;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;

public class DatePlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		return new SimpleDateFormat("yyyy/MM/dd").format(new Date());
	}
}
