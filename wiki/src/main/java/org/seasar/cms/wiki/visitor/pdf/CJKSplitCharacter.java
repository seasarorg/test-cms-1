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
package org.seasar.cms.wiki.visitor.pdf;

import com.lowagie.text.SplitCharacter;
import com.lowagie.text.pdf.PdfChunk;


public class CJKSplitCharacter implements SplitCharacter {

	public boolean isSplitCharacter(int start, int current, int end, char[] cc,
			PdfChunk[] ck) {

		// copy form PDFChunk;

		char c;
		if (ck == null)
			c = cc[current];
		else
			c = ck[Math.min(current, ck.length - 1)]
					.getUnicodeEquivalent(cc[current]);
		if (c <= ' ' || c == '-') {
			return true;
		}
		if (c < 0x2e80)
			return false;

		
		//handle japanese character added by tuigwaa
		if(cc.length>current+1){
			char c2 = cc[current+1];
			if (c2 == 0x3001 || c2 == 0x3002) {
				return false;
			}
		}
		//handle japanese character   added by tuigwaa

		return ((c >= 0x2e80 && c < 0xd7a0) || (c >= 0xf900 && c < 0xfb00)
				|| (c >= 0xfe30 && c < 0xfe50) || (c >= 0xff61 && c < 0xffa0));

	}
}
