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
package org.seasar.cms.wiki.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nishioka
 */
public class AnchorUtils {

	private static final String PATTERN = ".*\\[#[a-zA-Z][0-9a-zA-Z]+\\]$";

	private static final Pattern NEWLINE = Pattern.compile("\n");

	private static final Pattern ANCHOR = Pattern.compile(PATTERN);

	public static final String PREFIX = "[#tg";

	public static final String SUFFIX = "]";

	/**
	 * generate anchor data from object hashcode
	 * 
	 * @param contents
	 *            wiki contents
	 * @return contents with anchor data
	 */
	public static String setToHeading(String contents) {
		String mod = WikiStringUtils.removeCarriageReturn(contents);
		String[] lines = NEWLINE.split(mod);

		StringBuilder buf = new StringBuilder();
		int hashcode = contents.hashCode();
		for (String line : lines) {
			buf.append(line);
			Matcher m = ANCHOR.matcher(line);
			if (line.indexOf("*") == 0 && !m.matches()) {
				buf.append(PREFIX + Integer.toHexString(hashcode) + SUFFIX);
				hashcode++;
			}
			buf.append("\n");
		}
		return buf.toString();
	}
}
