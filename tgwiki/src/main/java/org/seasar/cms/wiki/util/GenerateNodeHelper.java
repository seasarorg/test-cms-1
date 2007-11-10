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

import java.util.ArrayList;
import java.util.List;

/**
 * @author someda
 * 
 * GenerateNodeHelper class provides utility method for wiki parser and other
 * wiki related object during parsing wiki contents. Basically, it does not
 * provide node related tasks for WikiParser but provides only static method
 * which is useful during parsing phase. Also this might be useful for plugin
 * execution phase.
 */
public class GenerateNodeHelper {

	public static final int LIST_TYPE_NORMAL = 1;

	public static final int LIST_TYPE_NUMERICAL = 2;

	public static final int TABLE_TYPE_HEADER = 1;

	public static final int TABLE_TYPE_FOOTER = 2;

	public static final String ANCHOR_MARK = "#";

	public static final String LINK_DELIMITER = ":";

	public static final String ALIAS_DELIMITER = ">";

	public static final String ANCHOR_PREFIX = "[#tg";

	public static final String ANCHOR_SUFFIX = "]";

	private GenerateNodeHelper() {
	}

	/**
	 * 与えられた文字列が email アドレスかどうかを判別する。 このメソッドはパーサから呼ばれる為、引数の image で利用されている
	 * 文字列が限定されている事を前提としているため、そのような前提が無い場合の 文字列に対しては使わないこと。
	 * 
	 * @param image
	 * @return 与えられた文字列が email アドレスの場合「真」、そうでない場合「偽」
	 */
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

	/**
	 * 文字列を delimiter で二分割し、二要素の配列として返す
	 * 
	 * @param image
	 * @param delimiter
	 * @return
	 */
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

	/**
	 * image から、左と右の括弧を取り外す left と right は異なる文字列である事を前提としている right の文字列が left
	 * よりも早くあらわれた場合は空文字を返す
	 * 
	 * @param image
	 * @param left
	 * @param right
	 * @return
	 */
	public static String deleteParenthesis(String image, String left,
			String right) {

		if (left.equals(right)) {
			throw new IllegalArgumentException(
					"The left must be another character to right");
		}

		int idx = 0;
		int lidx = 0;
		int ridx = image.length();

		if ((idx = image.indexOf(left)) != -1) {
			lidx = idx + 1;
		}

		if ((idx = image.lastIndexOf(right)) != -1) {
			ridx = idx;
		}
		return (ridx > lidx) ? image.substring(lidx, ridx) : "";
	}

	/**
	 * 文字列を引数として分割する
	 * 
	 * @param image
	 * @return
	 */
	public static String[] splitArgs(String image) {
		String s = deleteParenthesis(image, "(", ")");

		StringBuilder buf = new StringBuilder();
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
					buf = new StringBuilder();
					cnt = 0;
					continue;
				}
			}
			buf.append(c);
		}

		if (buf.length() > 0) {
			argsList.add(buf.toString().trim());
		}
		return (String[]) argsList.toArray(new String[argsList.size()]);
	}

	public static int getTableType(String image) {
		int type = 0;
		if ("h".equalsIgnoreCase(image)) {
			type = TABLE_TYPE_HEADER;
		} else if ("f".equalsIgnoreCase(image)) {
			type = TABLE_TYPE_FOOTER;
		}
		return type;
	}

}
