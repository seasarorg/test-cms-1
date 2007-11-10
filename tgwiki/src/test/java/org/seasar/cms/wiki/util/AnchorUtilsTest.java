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
package org.seasar.cms.wiki.util;

import junit.framework.TestCase;

/**
 * {@link AnchorUtils}
 * @author someda
 *
 */
public class AnchorUtilsTest extends TestCase {

	/**
	 * {@link AnchorUtils#setToHeading(String)}
	 */
	public void testSetToHeading() {

		String contents = "*h\nhoge";
		
		// 無かったらアンカーがつく
		String actual = AnchorUtils.setToHeading(contents);
		String[] result = actual.split("\n");
		int idx = result[0].indexOf("[#tg");
		assertEquals(2, idx);
		
		// 既にアンカーがあれば何もしない
		actual = AnchorUtils.setToHeading(result[0]);
		assertEquals(result[0],actual.replaceAll("\n", ""));
	}
}
