package org.seasar.cms.wiki.engine.impl;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.util.WikiHelper;
import org.seasar.cms.wiki.util.WikiTestUtils;
import org.seasar.extension.unit.S2TestCase;

public class WikiEngineImplTest extends S2TestCase {

	protected WikiEngine engine;

	@Override
	protected void setUp() throws Exception {
		include("wikiengine.dicon");
	}

	public void testBasic() {
		System.out.println(engine.evaluate("&br;", new WikiContext()));

		StringWriter writer = new StringWriter();
		engine.merge("*h1", new WikiContext(), writer);
		System.out.println(":::" + writer.getBuffer().toString());
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
		String expectedFileName = fileName.substring(0, fileName.length() - 3)
				+ "expected";
		String expected = IOUtils.toString(WikiTestUtils
				.getFileReader(expectedFileName));
		expected = WikiHelper.removeCarriageReturn(expected);
		assertEquals(expected, actual);
	}
}
