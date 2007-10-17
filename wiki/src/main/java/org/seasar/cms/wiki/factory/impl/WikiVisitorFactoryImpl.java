package org.seasar.cms.wiki.factory.impl;

import java.io.OutputStream;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.renderer.HtmlWikiVisitor;
import org.seasar.cms.wiki.renderer.WikiWriterVisitor;

public class WikiVisitorFactoryImpl implements WikiVisitorFactory {

	enum Type {
		html, pdf, txt
	}

	public WikiParserVisitor create(WikiContext context, Writer writer) {
		WikiWriterVisitor visitor = null;
		switch (Type.valueOf(context.getOutputType())) {
		case html:
			visitor = new HtmlWikiVisitor();
		case txt:
		case pdf:
			break;
		}
		if (visitor != null) {
			visitor.init(context, writer);
		}
		return visitor;
	}

	public WikiParserVisitor create(WikiContext context, OutputStream stream) {
		switch (Type.valueOf(context.getOutputType())) {
		case pdf:
			//return new PdfWikiVisitor(stream);
		case txt:
		case html:
			break;
		}
		return null;
	}
}
