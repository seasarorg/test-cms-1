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
package org.seasar.cms.wiki.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.WikiParserVisitor;

public class WikiContext {

	private Date modificationDate;

	private String namespace = "html";

	private WikiEngine engine;

	private Node node;

	private WikiParserVisitor visitor;

	private Map<String, Object> attrs = new HashMap<String, Object>();

	public void setVisitor(WikiParserVisitor visitor) {
		this.visitor = visitor;
	}

	public WikiParserVisitor getVisitor() {
		return visitor;
	}

	public void setRoot(Node node) {
		this.node = node;
	}

	public Node getRoot() {
		return node;
	}

	public void setEngine(WikiEngine engine) {
		this.engine = engine;
	}

	public WikiEngine getEngine() {
		return engine;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	@SuppressWarnings("unchecked")
	public <E> E get(String key) {
		return (E) attrs.get(key);
	}

	public void put(String key, Object obj) {
		attrs.put(key, obj);
	}

}
