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
import java.util.Iterator;
import java.util.Stack;

/**
 * @author someda
 */
public abstract class AbstractContentsWriter {

	protected Writer writer;

	protected boolean appendTab;

	protected boolean appendNewline;

	protected boolean closed = true;

	protected Stack elementStack = new Stack();

	public AbstractContentsWriter(Writer writer) {
		this(writer, false, false);
	}

	public AbstractContentsWriter(Writer writer, boolean appendTab,
			boolean appendNewline) {
		this.writer = writer;
		this.appendTab = appendTab;
		this.appendNewline = appendNewline;
	}

	public void flush() throws IOException {
		writer.flush();
	}

	public void appendAttribute(String name, String value) {
		if (value != null) {
			doAppendAttribute(name, value);
		}
	}

	public void appendBody(String body) {
		if (body == null) {
			return;
		}
		assertBody();
		doAppend(body);
		appendNewLine();
	}

	public void appendStartTag(String name) {
		assertBody();
		doAppendTag(name, true);
		closed = false;
		doTagPush(name);
	}

	public void endTag() {
		String name = doTagPop();
		if (closed) {
			doTab();
			doAppendTag(name, false);
			closeTag(true);
		} else {
			closeTag(false);
		}
	}

	public void endAllTags() {
		Iterator itr = elementStack.iterator();
		while (itr.hasNext()) {
			endTag();
		}
	}

	private void assertBody() {
		if (!closed) {
			closeTag(true);
		}
		doTab();
	}

	protected void doTab() {
		if (appendTab) {
			for (int i = 0; i < elementStack.size(); i++) {
				doAppend("\t");
			}
		}
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
			doAppend("/");
		}
		closed = true;
		doAppend(">");
		appendNewLine();
	}

	public boolean isTagEmpty() {
		return elementStack.empty();
	}

	public void appendNewLine() {
		if (appendNewline) {
			doAppendNewline();
		}
	}

	protected abstract void doAppend(String character);

	protected abstract void doAppendAttribute(String name, String value);

	protected abstract void doAppendTag(String name, boolean start);

	protected abstract void doAppendNewline();

	public abstract int nextIndex();

	public abstract String cut(int start, int end);

	public abstract void write() throws IOException;

}
