package org.seasar.cms.wiki.factory;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.plugin.WikiPageLink;

/**
 * リンクを生成する Factory
 * 
 * @author nishioka
 */
public interface WikiPageLinkFactory {

	public WikiPageLink create(WikiContext context, String pagename,
			String body, String anchor);

}
