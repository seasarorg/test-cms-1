/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.cms.wiki.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.seasar.framework.util.ResourceUtil;

/**
 * @author someda
 */
public class WikiTestUtils {

	private static final String DATAFILE_PATH = "org/seasar/cms/wiki/renderer/data/";

	private static final String FILE_ENCODING = "UTF-8";

	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public static InputStream resourceAsStream(String filePath) {
		ClassLoader cl = getClassLoader();
		return cl.getResourceAsStream(filePath);
	}

	public static File getDataDirectory() {
		return ResourceUtil.getResourceAsFile(DATAFILE_PATH);
	}

	public static Reader getFileReader(String fileName) {
		String filePath = DATAFILE_PATH + fileName;
		InputStream is = resourceAsStream(filePath);
		try {
			InputStreamReader reader = new InputStreamReader(is, FILE_ENCODING);
			return reader;
		} catch (UnsupportedEncodingException e) { // 発生しないはず
			e.printStackTrace();
		}
		return null;
	}

}
