/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.cms.wiki.renderer;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

/**
 * @author someda
 */
public class XmlWriterTest extends XMLTestCase {

	private XmlWriter htmlWriter;

	private StringWriter stringWriter;

	protected void setUp() {
		init();
	}

	private void init() {
		stringWriter = new StringWriter();
		htmlWriter = new XmlWriter(stringWriter);
	}

	public void testInline() {
		String actual = new XmlWriter().inline().start("test").attr("style",
				"bstyle").body("body").end().toString();
		String expected = "<test style=\"bstyle\">body</test>";
		assertEquals(expected, actual);
	}

	public void testBlock() {
		String actual = new XmlWriter().block().start("test").attr("style",
				"bstyle").body("body").end().toString();
		String expected = "<test style=\"bstyle\">\nbody\n</test>\n";
		assertEquals(expected, actual);
	}

	public void testStrong() {

		htmlWriter.start("strong");
		htmlWriter.body("emphasis");
		htmlWriter.end();

		String expected = "<strong>emphasis</strong>";
		doTest(expected);
	}

	public void testImage() {

		htmlWriter.start("img");
		htmlWriter.attr("src", "/images/test.gif");
		htmlWriter.end();

		String expected = "<img src=\"/images/test.gif\"/>";
		doTest(expected);
	}

	public void testList() {

		htmlWriter.start("ul");
		htmlWriter.start("li");
		htmlWriter.body("list1");
		htmlWriter.endAll();

		String expected = "<ul><li>list1</li></ul>";
		doTest(expected);
	}

	public void testParagraph() {

		htmlWriter.start("p");
		htmlWriter.body("段落の始まりの途中で");
		htmlWriter.start("span");
		htmlWriter.attr("style", "color:red;");
		htmlWriter.body("赤い文字になり");
		htmlWriter.end();
		htmlWriter.body("また戻る。");
		htmlWriter.end();

		String expected = "<p>段落の始まりの途中で<span style=\"color:red;\">赤い文字になり</span>また戻る。</p>";
		doTest(expected);
	}

	private void doTest(String expected) {
		try {
			htmlWriter.flush();
			String generated = stringWriter.toString();
			// System.out.println(generated);
			assertXMLEqual(expected, generated);
		} catch (IOException e) {
			fail();
		} catch (ParserConfigurationException e) {
			fail();
		} catch (SAXException e) {
			fail();
		}
		init();
	}

}
