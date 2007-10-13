package org.seasar.cms.wiki.factory;

import java.io.OutputStream;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.parser.WikiParserVisitor;

public interface WikiVisitorFactory {

	public WikiParserVisitor create(WikiContext context, Writer writer);

	public WikiParserVisitor create(WikiContext context, OutputStream stream);

}
