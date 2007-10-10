package org.seasar.cms.wiki.engine.impl;

import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.engine.WikiParser;
import org.seasar.cms.wiki.factory.WikiParserFactory;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.parser.WikiParserVisitor;

public class WikiEngineImpl implements WikiEngine {

	private WikiParserFactory parserFactory;

	private WikiVisitorFactory visitorFactory;

	public void setParserFactory(WikiParserFactory parserFactory) {
		this.parserFactory = parserFactory;
	}

	public void setVisitorFactory(WikiVisitorFactory visitorFactory) {
		this.visitorFactory = visitorFactory;
	}

	public String evaluate(String text, WikiContext context) {
		return evaluate(new StringReader(text), context);
	}

	public String evaluate(Reader reader, WikiContext context) {
		StringWriter writer = new StringWriter();
		merge(reader, context, writer);
		return writer.toString();
	}

	public void merge(Reader reader, WikiContext context, Writer writer) {
		WikiParser parser = parserFactory.getWikiParser(reader);
		WikiParserVisitor visitor = visitorFactory.create(context, writer);
		parser.parse().jjtAccept(visitor, null);
	}

	public void merge(String text, WikiContext context, Writer writer) {
		merge(new StringReader(text), context, writer);
	}

	public void merge(Reader reader, WikiContext context, OutputStream os) {
		// TODO Auto-generated method stub

	}
}
