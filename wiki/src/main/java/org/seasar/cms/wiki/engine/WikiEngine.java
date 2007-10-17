package org.seasar.cms.wiki.engine;

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.seasar.cms.wiki.engine.plugin.PluginExecuter;
import org.seasar.cms.wiki.factory.WikiBodyEvaluator;
import org.seasar.cms.wiki.factory.WikiPageLinkFactory;
import org.seasar.cms.wiki.factory.WikiParserFactory;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;

public interface WikiEngine {

	/**
	 * @param text
	 * @param context
	 * @return 結果
	 */
	public String evaluate(String text, WikiContext context);

	/**
	 * @param text
	 * @param context
	 * @return 結果
	 */
	public String evaluate(Reader reader, WikiContext context);

	public void merge(Reader reader, WikiContext context, Writer writer);

	public void merge(String text, WikiContext context, Writer writer);

	/**
	 * PDF を出力するときなどに使う
	 * 
	 * @param text
	 * @param context
	 * @param os
	 */
	public void merge(Reader reader, WikiContext context, OutputStream os);

	public void merge(String text, WikiContext context, OutputStream os);

	// ---- Setter Getter -------------------

	public WikiPageLinkFactory getLinkFactory();

	public WikiVisitorFactory getVisitorFactory();

	public WikiParserFactory getParserFactory();

	public PluginExecuter getPluginExecuter();

	public WikiBodyEvaluator getBodyEvaluator();

	public void setLinkFactory(WikiPageLinkFactory linkFactory);

	public void setVisitorFactory(WikiVisitorFactory visitorFactory);

	public void setParserFactory(WikiParserFactory parserFactory);

	public void setPluginExecuter(PluginExecuter pluginExecuter);

	public void setBodyEvaluator(WikiBodyEvaluator bodyEvaluator);

}
