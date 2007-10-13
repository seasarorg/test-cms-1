package org.seasar.cms.wiki.factory.impl;

import java.io.OutputStream;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.visitor.HtmlVisitor;

public class WikiVisitorFactoryImpl implements WikiVisitorFactory {

	enum Type {
		html, pdf, txt
	}

	public WikiParserVisitor create(WikiContext context, Writer writer) {
		switch (Type.valueOf(context.getOutputType())) {
		case html:
			return new HtmlVisitor(context, writer);
		case txt:
			break;
		case pdf:
			break;
		}
		return null;
	}

	public WikiParserVisitor create(WikiContext context, OutputStream stream) {
		// TODO Auto-generated method stub
		return null;
	}

}
