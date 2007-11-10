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
