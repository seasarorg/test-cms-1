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
package org.seasar.cms.wiki.engine.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.renderer.XmlWriter;
import org.seasar.cms.wiki.util.WikiTestUtils;
import org.seasar.framework.util.ResourceUtil;

public class WikiEngineImplTest extends WikiEngineTestFramework {

	private File target;

	private String css;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		target = new File("target/wikiresult");
		FileUtils.forceMkdir(target);

		css = FileUtils.readFileToString(ResourceUtil
				.getResourceAsFile("sample.css"), "UTF-8");
	}

	public void testGetProperty() {
		String value = engine.getProperty("class.note_super");
		assertEquals("note_super", value);

		Properties props = new Properties();
		props.put("class.note_super", "replace");
		engine.setProperties(props);

		value = engine.getProperty("class.note_super");
		assertEquals("replace", value);
	}

	public void testSetProperty() {
		engine.setProperty("class.core.table", "testTable");
		engine.setProperty("class.core.tr.odd", "odd");
		engine.setProperty("class.core.tr.even", "even");
		String actual = engine.evaluate("|a|b|\n|c|d|\n|e|f|");
		assertNotNull(actual);
	}

	public void testOutputStream() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.merge("p", new WikiContext(), bos);
		String actual = new String(bos.toByteArray(), "UTF-8");
		String expected = "<p>p</p>";
		assertWikiEquals(expected, actual);
	}

	public void testAll() throws Exception {
		File[] files = WikiTestUtils.getDataDirectory().listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.endsWith(".txt")) {
				doEval(fileName);
			}
		}
	}

	private void doEval(String fileName) throws Exception {
		Reader reader = WikiTestUtils.getFileReader(fileName);
		String actual = engine.evaluate(reader, new WikiContext());

		String name = fileName.substring(0, fileName.length() - 4);
		String expectedFileName = name + ".expected";
		String expected = IOUtils.toString(WikiTestUtils
				.getFileReader(expectedFileName));
		assertWikiEquals(expected, actual);

		writeHtml(name, actual);
	}

	private void writeHtml(String name, String actual) throws Exception {
		File file = new File(target, name + ".html");

		XmlWriter hw = new XmlWriter();
		hw.block().start("html").start("head");
		hw.start("meta").attr("http-equiv", "Content-Type").attr("content",
				"text/html; charset=UTF-8").end();
		hw.start("title").body("sample").end();
		hw.start("style").attr("type", "text/css").body(css).end();
		hw.end();
		hw.start("body").body(actual).endAll();

		FileUtils.writeStringToFile(file, hw.toString(), "UTF-8");
	}
}
