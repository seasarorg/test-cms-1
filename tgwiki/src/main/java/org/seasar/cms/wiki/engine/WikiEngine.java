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

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.seasar.cms.wiki.factory.WikiBodyFactory;
import org.seasar.cms.wiki.factory.WikiPageLinkFactory;
import org.seasar.cms.wiki.factory.WikiParserFactory;
import org.seasar.cms.wiki.factory.WikiVisitorFactory;
import org.seasar.cms.wiki.plugin.PluginExecuter;

/**
 * 与えられた Wiki コンテンツを解釈し、指定された処理方法にて出力を行うエンジン。
 * 最も簡単な利用方法は以下のとおり。
 * <pre>
 *   WikiEngine engine;
 *   String html = engine.evaluate("This is wiki text.");
 * </pre>
 * 
 * WikiEngine の実装クラスは、独自に実装する事も可能であるし、標準のエンジンを
 * 利用することも可能。
 * 
 * @see <a href="http://cms.sandbox.seasar.org/tgwiki/">Getting Started</a>
 * 
 * @author nishioka
 * @author someda
 */
public interface WikiEngine {

	/**
	 * Wiki エンジンのプロパティを設定する。 また同じキーのプロパティが設定されていれば上書きする。 デフォルトでは
	 * wikiengine.properties の内容が設定されている。
	 * 
	 * @param props
	 * @return
	 */
	public void setProperties(Properties props);

	/**
	 * Wiki エンジンのプロパティを設定する。 また同じキーのプロパティが設定されていれば上書きする。 デフォルトでは
	 * wikiengine.properties の内容が設定されている。
	 * 
	 * @param props
	 * @return
	 */
	public void setProperty(String name, String value);

	/**
	 * Wiki エンジンに設定されているプロパティを取得する。
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し、 その結果を文字列として返す。
	 * 
	 * @param text
	 * @param context
	 * @return 結果
	 */
	public String evaluate(String text, WikiContext context);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し、 その結果を文字列として返す。
	 * 
	 * @param text
	 * @return 結果
	 */
	public String evaluate(String text);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し、 その結果を文字列として返す。
	 * 
	 * @param reader
	 * @param context
	 * @return 結果
	 */
	public String evaluate(Reader reader, WikiContext context);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し、 その結果を文字列として返す。
	 * 
	 * @param reader
	 * @return 結果
	 */
	public String evaluate(Reader reader);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し Writer に書き出す。 PDF などの通常の文字列出力以外などの場合に利用する。
	 * 
	 * @param reader
	 * @param context
	 * @param writer
	 */
	public void merge(Reader reader, WikiContext context, Writer writer);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し Writer に書き出す。 PDF などの通常の文字列出力以外などの場合に利用する。
	 * 
	 * @param text
	 * @param context
	 * @param writer
	 */
	public void merge(String text, WikiContext context, Writer writer);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し OutputStream に書き出す。 PDF
	 * などの通常の文字列出力以外などの場合に利用する。
	 * 
	 * @param reader
	 * @param context
	 * @param os
	 */
	public void merge(Reader reader, WikiContext context, OutputStream os);

	/**
	 * 引数で与えられている Wiki コンテンツを評価し OutputStream に書き出す。 PDF
	 * などの通常の文字列出力以外などの場合に利用する。
	 * 
	 * @param text
	 * @param context
	 * @param os
	 */
	public void merge(String text, WikiContext context, OutputStream os);

	/**
	 * WikiPageLinkFactory を取得する。
	 * @return
	 */
	public WikiPageLinkFactory getLinkFactory();

	/**
	 * WikiVisitorFactory を取得する。
	 * @return
	 */
	public WikiVisitorFactory getVisitorFactory();

	/**
	 * WikiParserFactory を取得する。
	 * @return
	 */
	public WikiParserFactory getParserFactory();

	/**
	 * PluginExecuter を取得する。
	 * @return
	 */
	public PluginExecuter getPluginExecuter();

	/**
	 * WikiBodyFactory を取得する。
	 * @return
	 */
	public WikiBodyFactory getBodyEvaluator();

	/**
	 * WikiPageLinkFactory を設定する。
	 * @param linkFactory
	 */
	public void setLinkFactory(WikiPageLinkFactory linkFactory);

	/**
	 * WikiVisitorFactory を設定する。
	 * @param visitorFactory
	 */
	public void setVisitorFactory(WikiVisitorFactory visitorFactory);

	/**
	 * WikiParserFactory を設定する。
	 * @param parserFactory
	 */
	public void setParserFactory(WikiParserFactory parserFactory);

	/**
	 * PluginExecuter を設定する。
	 * @param pluginExecuter
	 */
	public void setPluginExecuter(PluginExecuter pluginExecuter);

	/**
	 * WikiBodyFactory を設定する。
	 * @param bodyEvaluator
	 */
	public void setBodyEvaluator(WikiBodyFactory bodyEvaluator);

}
