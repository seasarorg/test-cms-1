package org.seasar.cms.wiki.factory.impl;

import java.io.OutputStream;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.renderer.WikiOutputStreamVisitor;
import org.seasar.cms.wiki.renderer.WikiWriterVisitor;
import org.seasar.framework.container.S2Container;

public class WikiVisitorFactoryImpl implements WikiVisitorFactory {

	private S2Container container;

	public void setContainer(S2Container container) {
		this.container = container;
	}

	public WikiParserVisitor create(WikiContext context, Writer writer) {
		String visitorName = context.getNamespace() + "Visitor";
		WikiWriterVisitor visitor = (WikiWriterVisitor) container
				.getComponent(visitorName);
		if (visitor != null) {
			visitor.init(context, writer);
		}
		return visitor;
	}

	public WikiParserVisitor create(WikiContext context, OutputStream stream) {
		String visitorName = context.getNamespace() + "Visitor";
		WikiOutputStreamVisitor visitor = (WikiOutputStreamVisitor) container
				.getComponent(visitorName);
		if (visitor != null) {
			visitor.init(context, stream);
		}
		return visitor;
	}
}
