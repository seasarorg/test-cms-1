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
package org.seasar.cms.wiki.renderer;

import java.io.IOException;
import java.io.Writer;

public class WriterWrapper extends Writer {

	protected Writer writer;

	public WriterWrapper(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void write(char[] arg0, int arg1, int arg2) throws IOException {
		writer.write(arg0, arg1, arg2);
	}

	@Override
	public void write(char[] arg0) throws IOException {
		writer.write(arg0);
	}

	@Override
	public Writer append(CharSequence arg0) throws IOException {
		return writer.append(arg0);
	}

	@Override
	public Writer append(char arg0) throws IOException {
		return writer.append(arg0);
	}

	@Override
	public Writer append(CharSequence arg0, int arg1, int arg2)
			throws IOException {
		return writer.append(arg0, arg1, arg2);
	}

	@Override
	public boolean equals(Object arg0) {
		return writer.equals(arg0);
	}

	@Override
	public int hashCode() {
		return writer.hashCode();
	}

	@Override
	public String toString() {
		return writer.toString();
	}

	@Override
	public void write(int arg0) throws IOException {
		writer.write(arg0);
	}

	@Override
	public void write(String arg0) throws IOException {
		writer.write(arg0);
	}

	@Override
	public void write(String arg0, int arg1, int arg2) throws IOException {
		writer.write(arg0, arg1, arg2);
	}
}
