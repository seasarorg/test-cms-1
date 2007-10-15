/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.cms.wiki.renderer;

import java.io.IOException;
import java.io.Writer;

/**
 * @author someda
 */
public class HtmlWriter extends AbstractContentsWriter {

	private static final String NEWLINE = "\n";

	private StringBuffer buf;

	public HtmlWriter(Writer writer) {
		super(writer);
		buf = new StringBuffer();
	}

	// ----- [Start] Abstract メソッドの実装 -----

	protected void doAppend(String character) {
		buf.append(character);
	}

	protected void doAppendAttribute(String name, String value) {
		doAppend(" " + name + "=\"" + value + "\"");
	}

	protected void doAppendTag(String name, boolean start) {
		if (start) {
			doAppend("<" + name);
		} else {
			doAppend("</" + name);
		}
	}

	protected void doAppendNewline() {
		doAppend(NEWLINE);
	}

	// ----- [End] Abstract メソッドの実装 -----

	/**
	 * アンカーを追加する
	 * 
	 * @param linkUrl
	 * @param linkLabel
	 * @param mailCheck
	 *            真の場合、メールアドレスかどうかのチェック(@を含んでいるか)を行い、パスすれば"mailto:"がlinkUrlに付与される。
	 */
	public void appendAnchor(String linkUrl, String linkLabel, boolean mailCheck) {

		if (mailCheck) {
			if (linkUrl.indexOf("@") != -1) {
				linkUrl = "mailto:" + linkUrl;
			}
		}
		appendAnchor(linkUrl, linkLabel);
	}

	/**
	 * アンカーを追加する
	 * 
	 * @param linkUrl
	 * @param linkLabel
	 */
	public void appendAnchor(String linkUrl, String linkLabel) {
		appendStartTag("a");
		appendAttribute("href", linkUrl);
		appendBody(linkLabel);
		endTag();
	}

	/**
	 * テーブルのセルを追加する
	 * 
	 * @param value
	 * @param header
	 */
	public void appendTableCell(String value, boolean header) {
		if (header) {
			appendStartTag("th");
		} else {
			appendStartTag("td");
		}
		appendBody(value);
		endTag();
	}

	/**
	 * 見出しを追加する
	 * 
	 * @param level
	 * @param body
	 */
	public void appendHeading(int level, String body) {
		String tag = "h" + level;
		appendStartTag(tag);
		appendBody(body);
		endTag();
	}

	/**
	 * <br/> を追加する
	 */
	public void appendBr() {
		appendBody("<br/>");
	}

	public int nextIndex() {
		int idx = buf.length();
		return (closed) ? idx : idx + 1;
	}

	/**
	 * 指定した範囲の文字列を切り出し返す。 なお、切り出された文字列はバッファから削除される。
	 */
	public String cut(int start, int end) {
		String s = buf.substring(start, end);
		buf.delete(start, end);
		return s;
	}

	/**
	 * バッファの内容を Writer へ書き込み処理を行う。書き込み後バッファはクリアされる。
	 * 
	 * @throws IllegalStateException
	 *             タグが閉じていない状態で書き込みを行った場合
	 */
	public void write() throws IOException {

		if (!closed) {
			throw new IllegalStateException(
					"Cannot write until current element will be closed.");
		}
		writer.write(buf.toString());
		buf = new StringBuffer();
	}

	public void setNewline(boolean flag) {
		this.appendNewline = flag;
	}

	public void setTab(boolean flag) {
		this.appendNewline = flag;
	}
}
