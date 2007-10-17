/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.cms.wiki.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;

/**
 * @author someda
 */
public class SizePlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		if (args == null || args.length == 0) {
			return child;
		}
		String size = args[0];

		StringBuffer buf = new StringBuffer("<span style=\"font-size:" + size);
		if (size.indexOf("px") == -1 || size.indexOf("PX") == -1) {
			buf.append("px");
		}
		buf.append("\">");
		buf.append(child);
		buf.append("</span>");
		return buf.toString();
	}
}
