package org.seasar.cms.wiki.renderer;

import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.parser.WikiParserVisitor;

public interface WikiWriterVisitor extends WikiParserVisitor {

	public void init(WikiContext context, Writer writer);

	public Writer getWriter();

}
