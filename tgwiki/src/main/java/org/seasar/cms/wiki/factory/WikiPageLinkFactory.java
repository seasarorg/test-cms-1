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
