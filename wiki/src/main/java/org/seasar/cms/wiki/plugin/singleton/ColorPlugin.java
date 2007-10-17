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
public class ColorPlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {
		String childstr = null;
		if (args == null) {
			return child;
		}
		StringBuffer buf = new StringBuffer("<span style=\"");
		if (args.length >= 1) {
			buf.append("color:" + args[0]);
		}
		if (args.length >= 2) {
			buf.append("; background-color:" + args[1]);
		}
		buf.append("\">");
		buf.append(childstr);
		buf.append("</span>");
		return buf.toString();
	}
}
