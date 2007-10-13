package org.seasar.cms.wiki.factory;

import org.seasar.cms.wiki.engine.plugin.WikiPageLink;

/**
 * リンクを生成する Factory
 * 
 * @author nishioka
 */
public interface WikiLinkFactory {

	public WikiPageLink create(String pagename, String body, String anchor);

}
