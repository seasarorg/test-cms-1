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
package org.seasar.cms.wiki.extension.pdf.plugin;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.extension.pdf.PdfCmsPageEvents;
import org.seasar.cms.wiki.extension.pdf.PdfUtils;
import org.seasar.cms.wiki.parser.WikiHeading;
import org.seasar.cms.wiki.plugin.singleton.ContentsPlugin;
import org.seasar.cms.wiki.util.NodeUtils;
import org.seasar.cms.wiki.util.VisitorUtils;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

/**
 * @author someda
 */
public class PdfContentsPlugin extends ContentsPlugin {

	public Object doPDFView(WikiContext context, String[] args, Phrase child) {

		// if (request.getRootNode() == null)
		// return null;
		// if (!(request.getRootNode() instanceof WikiGenerateTree))
		// return null;
		//
		// List list = ((WikiGenerateTree) request.getRootNode()).toc;
		// if (list.size() == 0)
		// return null;

		Paragraph p = new Paragraph();
		p.setIndentationLeft(20f);
		p.setIndentationRight(20f);

		int[] num = { 0, 0, 0 };

		Font cfont = new Font(PdfUtils.FONT_JA_MINCHO);
		cfont.setSize(10f);

		for (WikiHeading heading : NodeUtils.find(context.getRoot(),
				WikiHeading.class)) {
			StringBuffer buf = new StringBuffer();
			int level = heading.level;

			for (int j = 0; j < level; j++) {
				buf.append("\u00a0\u00a0");
			}

			for (int j = 0; j < level - 1; j++) {
				buf.append(num[j] + ".");
			}
			buf.append(++num[level - 1] + ".");

			accept(heading, buf);

			Chunk c = new Chunk(buf.toString().trim(), cfont);
			String anchor = VisitorUtils.getAnchorId(heading);
			if (anchor != null) {
				c.setLocalGoto(anchor.substring(1));
			}
			c.setGenericTag(PdfCmsPageEvents.GENERICTAG_TOC);
			p.add(c);
			p.add(Chunk.NEWLINE);
			for (int j = level; j < num.length; j++) {
				num[j] = 0;
			}
		}

		Chunk c = new Chunk("\u00a0");
		c.setGenericTag(PdfCmsPageEvents.GENERICTAG_CLOSETOC);
		p.add(c);
		return p;
	}

	// private void accept0(Node node, StringBuffer buf) {
	//
	// if (node instanceof WikiLetters) {
	// WikiLetters l = (WikiLetters) node;
	// if (!l.isAnchor)
	// buf.append(l.letter);
	// } else if (node instanceof WikiLink) {
	// WikiLink l = (WikiLink) node;
	// String[] s = GenerateNodeHelper.split(l.image,
	// GenerateNodeHelper.LINK_DELIMITER);
	// buf.append(s[0]);
	// } else if (node instanceof WikiAlias) {
	// WikiAlias l = (WikiAlias) node;
	// String[] s = GenerateNodeHelper.split(l.image,
	// GenerateNodeHelper.ALIAS_DELIMITER);
	// buf.append(s[0]);
	// } else if (node instanceof WikiPagename) {
	// WikiPagename l = (WikiPagename) node;
	// buf.append(l.image);
	// } else if (node instanceof WikiInlinePlugin) {
	// WikiInlinePlugin l = (WikiInlinePlugin) node;
	// buf.append("PLUGIN:" + l.name);
	// }
	//
	// for (int i = 0; i < node.jjtGetNumChildren(); i++) {
	// accept(node.jjtGetChild(i), buf);
	//		}
	//	}

}
