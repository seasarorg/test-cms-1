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
package org.seasar.cms.wiki.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * @author someda
 * 
 * WikiHelper class This provides utility method for wiki parser and other wiki
 * related object. Basically, it does not provide node related tasks for
 * WikiParser but provide only static method which is useful during parsing
 * phase. Also this might be useful for plugin execution phase.
 */
public class WikiHelper {

	public static final int LIST_TYPE_NORMAL = 1;

	public static final int LIST_TYPE_NUMERICAL = 2;

	public static final int TABLE_TYPE_HEADER = 1;

	public static final int TABLE_TYPE_FOOTER = 2;

	public static final String ANCHOR_MARK = "#";

	public static final String LINK_DELIMITER = ":";

	public static final String ALIAS_DELIMITER = ">";


	private static final Pattern CR = Pattern.compile("\r");


	// private static final String[] IMAGE_SUFFIX =
	// {".jpg",".jpeg",".gif",".bmp",".png"};
	private static final String[] IMAGE_SUFFIX = ImageIO.getReaderFormatNames();

	public static boolean isEmail(String image) {
		return (image.indexOf("@") != -1);
	}

	public static int getListType(String image) {
		int type = 0;
		if (image.equals("-") || image.equals("--") || image.equals("---")) {
			type = LIST_TYPE_NORMAL;
		} else if (image.equals("+") || image.equals("++")
				|| image.equals("+++")) {
			type = LIST_TYPE_NUMERICAL;
		}
		return type;
	}

	public static String[] split(String image, String delimiter) {
		String[] s = new String[2];
		int idx = image.indexOf(delimiter);
		if (idx != -1) {
			s[0] = image.substring(0, idx);
			s[1] = image.substring(idx + 1, image.length());
		} else {
			s[0] = image;
			s[1] = "";
		}
		return s;
	}

	public static String deleteParenthesis(String image, String left,
			String right) {
		return deleteParenthesis(image, left, right, true);
	}

	public static String deleteParenthesis(String image, String left,
			String right, boolean isWide) {
		int idx = 0;
		int lidx = 0;
		int ridx = image.length();

		if ((idx = image.indexOf(left)) != -1) {
			lidx = idx + 1;
		}

		if (isWide) {
			if ((idx = image.lastIndexOf(right)) != -1) {
				ridx = idx;
			}
		} else {
			if ((idx = image.indexOf(right)) != -1) {
				ridx = idx;
			}
		}
		return (ridx > lidx) ? image.substring(lidx, ridx) : "";
	}

	/**
	 * remove carriage return
	 * 
	 * @param contents
	 *            html contents
	 */
	public static String removeCarriageReturn(String contents) {
		Matcher crm = CR.matcher(contents);
		return crm.replaceAll("");
	}

	/**
	 * Discriminates the specifid string represents image file or not. This see
	 * the string as image if it ends with <strong>.jpg,.jpeg,.gif,.png,and .bmp</strong>
	 * in a case insensitive mannger.
	 */
	public static boolean isImage(String s) {

		boolean flag = false;
		String[] path = s.split("\\.");
		String extension = path[path.length - 1];

		if (extension != null && !extension.equals("")) {
			for (int i = 0; i < IMAGE_SUFFIX.length; i++) {
				if (extension.equalsIgnoreCase(IMAGE_SUFFIX[i])) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * Discriminates the specifid string represents URL or not.
	 */
	public static boolean isURL(String s) {
		try {
			new URL(s);
			return true;
		} catch (MalformedURLException e) {
			// do nothing
		}
		return false;
	}

	public static String[] splitArgs(String image) {
		String s = deleteParenthesis(image, "(", ")");

		StringBuffer buf = new StringBuffer();
		List<String> argsList = new ArrayList<String>();
		int cnt = 0;
		int parenthesis = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '&') { // enter plugin mode
				cnt++;
			}

			if (cnt > 0 && (c == '(' || c == '{')) { // enter parenthesis
				// mode
				parenthesis++;
			}

			if (cnt > 0 && (c == ')' || c == '}')) {
				parenthesis--;
			}

			// if(c == ';'){
			// cnt--;
			// }

			if (c == ',') {
				if ((cnt > 0 && parenthesis == 0) || cnt == 0) {
					argsList.add(buf.toString().trim());
					buf = new StringBuffer();
					cnt = 0;
					continue;
				}
			}
			buf.append(c);
		}

		if (buf.length() > 0) {
			argsList.add(buf.toString().trim());
		}
		return argsList.toArray(new String[argsList.size()]);
	}

	public static int getTableType(String image) {
		int type = 0;
		if ("h".equals(image) || "H".equals(image)) {
			type = TABLE_TYPE_HEADER;
		} else if ("f".equals(image) || "F".equals(image)) {
			type = TABLE_TYPE_FOOTER;
		}
		return type;
	}

}