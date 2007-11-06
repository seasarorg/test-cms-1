package org.seasar.cms.wiki.extension.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.extension.unit.S2TestCase;

public class PdfWikiVisitorTest extends S2TestCase {

	private WikiEngine engine;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		include("wikiengine.dicon");
	}

	public void testTest() throws Exception {

		WikiContext context = new WikiContext();
		context.setNamespace("pdf");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		engine.merge("*a", context, bos);

		FileUtils.writeByteArrayToFile(new File("sample.pdf"), bos
				.toByteArray());
	}
}
