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

import org.seasar.framework.exception.IORuntimeException;

/**
 * @author someda
 */
public class HtmlWriter extends AbstractContentsWriter<HtmlWriter> {

	private static final String NEWLINE = "\n";

	private IOException error = null;

	public HtmlWriter(Writer writer) {
		super(writer);
	}

	// ----- [Start] Abstract メソッドの実装 -----

	protected void doAppend(String character) {
		try {
			super.write(character);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public IOException getError() {
		return error;
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
		start("a");
		attr("href", linkUrl);
		body(linkLabel);
		end();
	}

	/**
	 * テーブルのセルを追加する
	 * 
	 * @param value
	 * @param header
	 */
	public void appendTableCell(String value, boolean header) {
		if (header) {
			start("th");
		} else {
			start("td");
		}
		body(value);
		end();
	}

	/**
	 * 見出しを追加する
	 * 
	 * @param level
	 * @param body
	 */
	public void appendHeading(int level, String body) {
		String tag = "h" + level;
		start(tag);
		body(body);
		end();
	}

	/**
	 * <br/> を追加する
	 */
	public void appendBr() {
		body("<br/>");
	}

	/**
	 * バッファの内容を Writer へ書き込み処理を行う。書き込み後バッファはクリアされる。
	 * 
	 * @throws IllegalStateException
	 *             タグが閉じていない状態で書き込みを行った場合
	 */
	public void close() throws IOException {
		if (!closed) {
			throw new IllegalStateException(
					"Cannot write until current element will be closed.");
		}
		super.close();
	}

	private void setNewline(boolean flag) {
		this.appendNewline = flag;
	}

	private void setTab(boolean flag) {
		this.appendNewline = flag;
	}

	public HtmlWriter enableTab() {
		setTab(true);
		return this;
	}

	public HtmlWriter disableTab() {
		setTab(false);
		return this;
	}

	public HtmlWriter enableLineBreak() {
		setNewline(true);
		return this;
	}

	public HtmlWriter disableLineBreak() {
		setNewline(false);
		return this;
	}

}
