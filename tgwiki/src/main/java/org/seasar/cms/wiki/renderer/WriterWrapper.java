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
