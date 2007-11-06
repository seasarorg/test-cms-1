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
import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.WikiAlias;
import org.seasar.cms.wiki.parser.WikiHeading;
import org.seasar.cms.wiki.parser.WikiInlinePlugin;
import org.seasar.cms.wiki.parser.WikiLetters;
import org.seasar.cms.wiki.parser.WikiLink;
import org.seasar.cms.wiki.parser.WikiPagename;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;
import org.seasar.cms.wiki.util.GenerateNodeHelper;
import org.seasar.cms.wiki.util.NodeUtils;
import org.seasar.cms.wiki.util.VisitorUtils;

/**
 * @author someda
 */
public class ContentsPlugin implements SingletonWikiPlugin {

	public String render(WikiContext context, String[] args, String child) {

		int curlevel = 0;
		int prelevel = 0;
		int[] level = { 0, 0, 0 };

		StringBuffer buf = new StringBuffer();
		buf.append("<div class=\"contents\">");

		for (WikiHeading heading : NodeUtils.find(context.getRoot(),
				WikiHeading.class)) {
			curlevel = heading.level;
			updateUl(buf, curlevel, prelevel, level);
			prelevel = curlevel;
			writeLi(buf, heading);
		}

		closeUl(buf, level);
		buf.append("</div>\n");
		return buf.toString();
	}

	private void writeLi(StringBuffer buf, WikiHeading heading) {
		String href = String.format("<a href=\"%s\">", VisitorUtils
				.getAnchorId(heading));

		buf.append("<li>" + href);
		accept(heading, buf);
		buf.append("</li></a>");
	}

	private void closeUl(StringBuffer buf, int[] level) {
		for (int i = 0; i < level.length; i++) {
			if (level[i] != 0) {
				buf.append("</ul>");
			}
		}
	}

	private void updateUl(StringBuffer buf, int curlevel, int prelevel,
			int[] level) {
		if (curlevel == prelevel) {
			return;
		}
		if (curlevel > prelevel) {
			buf.append("<ul>");
		} else if (curlevel < prelevel) {
			for (int j = curlevel; j < prelevel; j++) {
				if (level[j] != 0) {
					buf.append("</ul>");
					level[j] = 0;
				}
			}
		}
		level[curlevel - 1] = 1;
	}

	protected void accept(Node node, StringBuffer buf) {
		if (node instanceof WikiLetters) {
			WikiLetters l = (WikiLetters) node;
			if (!l.isAnchor)
				buf.append(l.letter);
		} else if (node instanceof WikiLink) {
			WikiLink l = (WikiLink) node;
			String[] s = GenerateNodeHelper.split(l.image,
					GenerateNodeHelper.LINK_DELIMITER);
			buf.append(s[0]);
		} else if (node instanceof WikiAlias) {
			WikiAlias l = (WikiAlias) node;
			String[] s = GenerateNodeHelper.split(l.image,
					GenerateNodeHelper.ALIAS_DELIMITER);
			buf.append(s[0]);
		} else if (node instanceof WikiPagename) {
			WikiPagename l = (WikiPagename) node;
			buf.append(l.image);
		} else if (node instanceof WikiInlinePlugin) {
			WikiInlinePlugin l = (WikiInlinePlugin) node;
			buf.append("PLUGIN:" + l.name);
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			accept(node.jjtGetChild(i), buf);
		}
	}
}
