package org.seasar.cms.wiki.engine.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.engine.WikiParser;
import org.seasar.cms.wiki.factory.WikiBodyFactory;
import org.seasar.cms.wiki.factory.WikiPageLinkFactory;
import org.seasar.cms.wiki.factory.WikiParserFactory;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.WikiParserVisitor;
import org.seasar.cms.wiki.plugin.PluginExecuter;
import org.seasar.cms.wiki.plugin.SingletonWikiPlugin;
import org.seasar.cms.wiki.plugin.impl.PluginExecuterImpl;
import org.seasar.cms.wiki.plugin.impl.SingletonPluginExecuter;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.util.ResourceUtil;

public class WikiEngineImpl implements WikiEngine {

	private WikiParserFactory parserFactory;

	private WikiVisitorFactory visitorFactory;

	private WikiPageLinkFactory linkFactory;

	private WikiBodyFactory bodyEvaluator;

	private PluginExecuter pluginExecuter;

	private Properties props = new Properties();

	public static final WikiEngine getInstance() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
				WikiEngineImpl.class.getClassLoader());
		WikiEngine engine = (WikiEngine) S2ContainerFactory.create(
				"wikiengine.dicon").getComponent(WikiEngine.class);

		Thread.currentThread().setContextClassLoader(cl);

		PluginExecuterImpl executer = new PluginExecuterImpl();
		SingletonPluginExecuter child = new SingletonPluginExecuter();

		for (String name : new String[] { "br", "clear", "color", "contents",
				"date", "div", "divclose", "newpage", "now", "size", "time" }) {
			String className = name.substring(0, 1).toUpperCase()
					+ name.substring(1);
			try {
				Class clazz = Class
						.forName("org.seasar.cms.wiki.plugin.singleton."
								+ className + "Plugin");
				SingletonWikiPlugin plugin = (SingletonWikiPlugin) clazz
						.newInstance();
				child.addPlugin(name, plugin);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		executer.addChildExecuter(child);
		engine.setPluginExecuter(executer);

		return engine;
	}

	public WikiEngineImpl() {
		try {
			props.load(ResourceUtil
					.getResourceAsStream("wikiengine.properties"));
		} catch (IOException e) {
			throw new IllegalStateException("WikiEngine Initialization Fails");
		}
	}

	public void setProperties(Properties props) {
		this.props.putAll(props);
	}

	public void setProperty(String name, String value) {
		this.props.put(name, value);
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public void setParserFactory(WikiParserFactory parserFactory) {
		this.parserFactory = parserFactory;
	}

	public void setVisitorFactory(WikiVisitorFactory visitorFactory) {
		this.visitorFactory = visitorFactory;
	}

	public void setPluginExecuter(PluginExecuter pluginExecuter) {
		this.pluginExecuter = pluginExecuter;
	}

	public void setBodyEvaluator(WikiBodyFactory bodyEvaluator) {
		this.bodyEvaluator = bodyEvaluator;
	}

	public void setLinkFactory(WikiPageLinkFactory linkFactory) {
		this.linkFactory = linkFactory;
	}

	public WikiVisitorFactory getVisitorFactory() {
		return visitorFactory;
	}

	public WikiParserFactory getParserFactory() {
		return parserFactory;
	}

	public PluginExecuter getPluginExecuter() {
		return pluginExecuter;
	}

	public WikiBodyFactory getBodyEvaluator() {
		return bodyEvaluator;
	}

	public WikiPageLinkFactory getLinkFactory() {
		return linkFactory;
	}

	public String evaluate(String text, WikiContext context) {
		return evaluate(new StringReader(text), context);
	}

	public String evaluate(String text) {
		return evaluate(text, new WikiContext());
	}

	public String evaluate(Reader reader) {
		return evaluate(reader, new WikiContext());
	}

	public String evaluate(Reader reader, WikiContext context) {
		StringWriter writer = new StringWriter();
		merge(reader, context, writer);
		return writer.toString();
	}

	public void merge(Reader reader, WikiContext context, Writer writer) {
		WikiParserVisitor visitor = visitorFactory.create(context, writer);
		doExecute(reader, context, visitor);
	}

	public void merge(String text, WikiContext context, Writer writer) {
		merge(new StringReader(text), context, writer);
	}

	public void merge(String text, WikiContext context, OutputStream os) {
		merge(new StringReader(text), context, os);
	}

	public void merge(Reader reader, WikiContext context, OutputStream os) {
		WikiParserVisitor visitor = visitorFactory.create(context, os);
		doExecute(reader, context, visitor);
	}

	private void doExecute(Reader reader, WikiContext context,
			WikiParserVisitor visitor) {
		WikiParser parser = parserFactory.getWikiParser(reader);
		Node root = parser.parse();

		// setup context
		context.setEngine(this);
		context.setRoot(root);
		context.setVisitor(visitor);

		root.jjtAccept(visitor, null);
	}
}
