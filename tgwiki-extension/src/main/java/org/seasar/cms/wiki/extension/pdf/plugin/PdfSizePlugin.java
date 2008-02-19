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

import java.util.Iterator;
import java.util.List;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.plugin.singleton.SizePlugin;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;

public class PdfSizePlugin extends SizePlugin {

	public Object doPDFView(WikiContext context, String[] args, Phrase child) {

		if (args == null || args.length == 0) {
			return child;
		}

		String sizestr = args[0].replaceAll("px|PX|pX|Px", "");
		float size = Float.parseFloat(sizestr);
		List list = child.getChunks();
		if (list != null) {
			for (Iterator i = list.iterator(); i.hasNext();) {
				Chunk c = (Chunk) i.next();
				Font nf = new Font(c.font());
				nf.setSize(size);
				c.setFont(nf);
			}
		}
		return child;
	}

}