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
 * Wiki 解析中に使う文字列処理のユーティリティ
 * 
 * @author nishioka
 */
public class WikiStringUtils {

	private static final String[] TAG_ESCAPE = { "<", "&lt;", ">", "&gt;" };

	private static final String[] OTHER_ESCAPE = { "&", "&amp;" };

	private static final Pattern CR = Pattern.compile("\r");

	private static final Pattern LF = Pattern.compile("\n");

	/**
	 * エスケープ処理を行う
	 * 
	 * @param letter
	 * @return
	 */
	public static String escape(String letter) {
		return replaceArray(letter, TAG_ESCAPE, true);
	}

	/**
	 * XML 実態参照から文字列に戻す
	 * 
	 * @param letter
	 * @return
	 */
	public static String unescape(String letter) {
		letter = replaceArray(letter, TAG_ESCAPE, false);
		letter = replaceArray(letter, OTHER_ESCAPE, false);
		return letter;
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
	 * remove line break
	 * 
	 * @param contents
	 *            html contents
	 */
	public static String removeLineFeed(String contents) {
		return LF.matcher(contents).replaceAll("");
	}

	// ----------- private method ---------------------------

	private static String replaceArray(String letter, String[] array,
			boolean forward) {

		if (letter == null)
			return "";
		String ret = letter;
		if (forward) {
			for (int i = 0; i < array.length - 1; i += 2) {
				ret = ret.replaceAll(array[i], array[i + 1]);
			}
		} else {
			for (int i = 1; i < array.length; i += 2) {
				ret = ret.replaceAll(array[i], array[i - 1]);
			}
		}
		return ret;
	}

}
