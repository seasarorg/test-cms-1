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

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * @author someda
 */
public abstract class AbstractContentsWriter<E> extends WriterWrapper {

	protected boolean appendTab;

	protected boolean appendNewline;

	protected boolean closed = true;

	protected Stack<String> elementStack = new Stack<String>();

	public AbstractContentsWriter(Writer writer) {
		this(writer, false, false);
	}

	public AbstractContentsWriter(Writer writer, boolean appendTab,
			boolean appendNewline) {
		super(writer);
		this.appendTab = appendTab;
		this.appendNewline = appendNewline;
	}

	@SuppressWarnings("unchecked")
	public E attr(String name, String value) {
		if (value != null) {
			doAppendAttribute(name, value);
		}
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public E attrs(Map<String, String> attrs) {
		for (String key : attrs.keySet()) {
			attr(key, attrs.get(key));
		}
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public E body(String body) {
		if (body == null) {
			return (E) this;
		}
		assertBody();
		doAppend(body);
		appendNewLine();
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public E start(String name) {
		assertBody();
		doAppendTag(name, true);
		closed = false;
		doTagPush(name);
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public E end() {
		String name = doTagPop();
		if (closed) {
			doTab();
			doAppendTag(name, false);
			closeTag(true);
		} else {
			closeTag(false);
		}
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public E endAll() {
		Iterator itr = elementStack.iterator();
		while (itr.hasNext()) {
			end();
		}
		return (E) this;
	}

	// ----------- protected method ---------------

	protected void doTab() {
		if (appendTab) {
			for (int i = 0; i < elementStack.size(); i++) {
				doAppend("\t");
			}
		}
	}

	// ---------- Private Method --------

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

}
