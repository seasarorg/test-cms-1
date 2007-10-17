package org.seasar.cms.wiki.factory;

import org.seasar.cms.wiki.engine.WikiContext;

/**
 * リンクを生成する Factory
 * 
 * @author nishioka
 */
public interface WikiPageLinkFactory {

	/**
	 * 指定したページに対するリンク
	 * 
	 * @param context
	 * @param pagename
	 * @param body
	 * @param anchor
	 * @return
	 */
	public WikiPageLink create(WikiContext context, String pagename,
			String body, String anchor);

	public WikiPageLink createEditLink(WikiContext context, String anchor);
}
