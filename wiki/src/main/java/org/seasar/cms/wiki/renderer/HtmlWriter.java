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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.seasar.framework.exception.IORuntimeException;

/**
 * @author someda
 */
public class HtmlWriter extends WriterWrapper {

	private String newline = "\n";

	protected boolean appendTab = false;

	protected boolean appendNewline = false;

	protected boolean closed = true;

	protected Stack<String> elementStack = new Stack<String>();

	public HtmlWriter() {
		super(new StringWriter());
	}

	public HtmlWriter(Writer writer) {
		super(writer);
	}

	public HtmlWriter(Writer writer, String linebreakcode) {
		super(writer);
		this.newline = linebreakcode;
	}

	/**
	 * バッファの内容を Writer へ書き込み処理を行う。書き込み後バッファはクリアされる。
	 * 
	 * @throws IllegalStateException
	 *             タグが閉じていない状態で書き込みを行った場合
	 */
	@Override
	public void close() throws IOException {
		if (!closed) {
			throw new IllegalStateException(
					"Cannot write until current element will be closed.");
		}
		super.close();
	}

	/**
	 * add xml attribute
	 */
	public HtmlWriter attr(String name, String value) {
		if (value != null) {
			doWrite(" " + name + "=\"" + value + "\"");
		}
		return this;
	}

	/**
	 * add xml attributes
	 */
	public HtmlWriter attrs(Map<String, String> attrs) {
		for (String key : attrs.keySet()) {
			attr(key, attrs.get(key));
		}
		return this;
	}

	/**
	 * add body
	 */
	public HtmlWriter body(String body) {
		if (body == null) {
			return this;
		}
		assertBody();
		doWrite(body);
		doAppendNewLine();
		return this;
	}

	/**
	 * open tag
	 */
	public HtmlWriter start(String name) {
		assertBody();
		doAppendTag(name, true);
		closed = false;
		doTagPush(name);
		return this;
	}

	/**
	 * close tag
	 */
	public HtmlWriter end() {
		String name = doTagPop();
		if (closed) {
			doTab();
			doAppendTag(name, false);
			closeTag(true);
		} else {
			closeTag(false);
		}
		return this;
	}

	/**
	 * close all tags which is opened.
	 */
	public HtmlWriter endAll() {
		Iterator itr = elementStack.iterator();
		while (itr.hasNext()) {
			end();
		}
		return this;
	}

	/**
	 * 新規タグ追加時にタブを挿入
	 * 
	 * @return
	 */
	public HtmlWriter enableTab() {
		this.appendNewline = true;
		return this;
	}

	/**
	 * 新規タグ追加時にタブを挿入せず
	 * 
	 * @return
	 */
	public HtmlWriter disableTab() {
		this.appendNewline = false;
		return this;
	}

	/**
	 * 新規タグ追加時、Body追加時、タグ閉じ追加時、改行コードを挿入
	 * 
	 * @return
	 */
	public HtmlWriter enableNewline() {
		this.appendNewline = true;
		return this;
	}

	/**
	 * 新規タグ追加時、Body追加時、タグ閉じ追加時、改行コードを挿入せず
	 */
	public HtmlWriter disableNewline() {
		this.appendNewline = false;
		return this;
	}

	/**
	 * write contens without linebreak or tab
	 */
	public HtmlWriter inline() {
		disableNewline();
		disableTab();
		return this;
	}

	/**
	 * write content with linebreak or tab
	 */
	public HtmlWriter block() {
		enableNewline();
		enableTab();
		return this;
	}

	// ---------- private methods -----------------

	private void assertBody() {
		if (!closed) {
			closeTag(true);
		}
		doTab();
	}

	private void doTagPush(String tag) {
		elementStack.push(tag);
	}

	private String doTagPop() {
		if (elementStack.size() > 0) {
			return (String) elementStack.pop();
		} else {
			return null;
		}
	}

	public void closeTag() {
		closeTag(true);
	}

	private void closeTag(boolean onlyClose) {
		if (!onlyClose) {
			doWrite("/");
		}
		closed = true;
		doWrite(">");
		doAppendNewLine();
	}

	private void doWrite(String character) {
		try {
			super.write(character);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	private void doAppendTag(String name, boolean start) {
		if (start) {
			doWrite("<" + name);
		} else {
			doWrite("</" + name);
		}
	}

	private void doTab() {
		if (appendTab) {
			for (int i = 0; i < elementStack.size(); i++) {
				doWrite("\t");
			}
		}
	}

	private void doAppendNewLine() {
		if (appendNewline) {
			doWrite(newline);
		}
	}
}
