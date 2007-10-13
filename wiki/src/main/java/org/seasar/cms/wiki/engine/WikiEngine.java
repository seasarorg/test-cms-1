package org.seasar.cms.wiki.engine;

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.seasar.cms.wiki.engine.plugin.PluginExecuter;
import org.seasar.cms.wiki.engine.plugin.WikiBodyEvaluator;
import org.seasar.cms.wiki.factory.WikiLinkFactory;

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

	/**
	 * @return プラグイン実行環境
	 */
	public PluginExecuter getPluginExecuter();

	public WikiBodyEvaluator getBodyEvaluator();

	public WikiLinkFactory getLinkFactory();
}
