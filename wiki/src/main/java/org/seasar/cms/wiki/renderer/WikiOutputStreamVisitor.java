package org.seasar.cms.wiki.renderer;

import java.io.OutputStream;
import java.io.Writer;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.parser.WikiParserVisitor;

public interface WikiOutputStreamVisitor extends WikiParserVisitor {

	public void init(WikiContext context, OutputStream os);

	public Writer getWriter();

}
