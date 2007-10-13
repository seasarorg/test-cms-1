package org.seasar.cms.wiki.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.WikiParserVisitor;

public class WikiContext {

	private Date modificationDate;

	private String outputType = "html";

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

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return outputType;
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
