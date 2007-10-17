package org.seasar.cms.wiki.factory.impl;

import java.io.OutputStream;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.renderer.HtmlVisitor;
import org.seasar.cms.wiki.visitor.PdfWikiVisitor;

public class WikiVisitorFactoryImpl implements WikiVisitorFactory {

	enum Type {
		html, pdf, txt
	}

	public WikiParserVisitor create(WikiContext context, Writer writer) {
		switch (Type.valueOf(context.getOutputType())) {
		case html:
			return new HtmlVisitor(context, writer);
		case txt:
		case pdf:
			break;
		}
		return null;
	}

	public WikiParserVisitor create(WikiContext context, OutputStream stream) {
		switch (Type.valueOf(context.getOutputType())) {
		case pdf:
			return new PdfWikiVisitor(stream);
		case txt:
		case html:
			break;
		}
		return null;
	}
}
