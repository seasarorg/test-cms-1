package org.seasar.cms.wiki.engine.impl;

import java.util.Properties;

public class WikiEngineImplTest extends WikiEngineTestFramework {

	public void testGetProperty() {
		String value = engine.getProperty("class.note_super");
		assertEquals("note_super", value);

		Properties props = new Properties();
		props.put("class.note_super", "replace");
		engine.setProperties(props);

		value = engine.getProperty("class.note_super");
		assertEquals("replace", value);
	}

	/*
	 * 
	 * しばらく コメントアウト public void testAll() throws Exception { File[] files =
	 * WikiTestUtils.getDataDirectory().listFiles(); for (File file : files) {
	 * String fileName = file.getName(); if (fileName.endsWith(".txt")) {
	 * doEval(fileName); } } }
	 * 
	 * 
	 * private void doEval(String fileName) throws Exception { Reader reader =
	 * WikiTestUtils.getFileReader(fileName); String actual =
	 * engine.evaluate(reader, new WikiContext()); String expectedFileName =
	 * fileName.substring(0, fileName.length() - 3) + "expected"; String
	 * expected = IOUtils.toString(WikiTestUtils
	 * .getFileReader(expectedFileName)); assertWikiEquals(expected, actual); }
	 */

}
