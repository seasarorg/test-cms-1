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

public interface WikiEngine {

	/**
	 * プロパティの設定。同じキーのプロパティが設定されていれば上書き。
	 * 
	 * @param props
	 * @return
	 */
	public void setProperties(Properties props);

	public void setProperty(String name, String value);

	/**
	 * プロパティの取得
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key);

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
	public String evaluate(String text);

	/**
	 * @param text
	 * @param context
	 * @return 結果
	 */
	public String evaluate(Reader reader, WikiContext context);

	/**
	 * @param text
	 * @param context
	 * @return 結果
	 */
	public String evaluate(Reader reader);

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
	 * PDF を出力するときなどに使う
	 * 
	 * @param text
	 * @param context
	 * @param os
	 */
	public void merge(String text, WikiContext context, OutputStream os);

	// ---- Setter Getter -------------------

	public WikiPageLinkFactory getLinkFactory();

	public WikiVisitorFactory getVisitorFactory();

	public WikiParserFactory getParserFactory();

	public PluginExecuter getPluginExecuter();

	public WikiBodyFactory getBodyEvaluator();

	public void setLinkFactory(WikiPageLinkFactory linkFactory);

	public void setVisitorFactory(WikiVisitorFactory visitorFactory);

	public void setParserFactory(WikiParserFactory parserFactory);

	public void setPluginExecuter(PluginExecuter pluginExecuter);

	public void setBodyEvaluator(WikiBodyFactory bodyEvaluator);

}
