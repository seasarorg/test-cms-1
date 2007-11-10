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
 * {@link GenerateNodeHelper}
 * 
 * @author someda
 */
public class GenerateNodeHelperTest extends TestCase {

	/**
	 * {@link GenerateNodeHelper#isEmail(String)}
	 */
	public void testIsEmail() {
		String image = "test@isenshi.com";
		boolean actual = GenerateNodeHelper.isEmail(image);
		assertTrue(actual);
		image = "http://www.isenshi.com";
		actual = GenerateNodeHelper.isEmail(image);
		assertFalse(actual);
	}

	/**
	 * {@link GenerateNodeHelper#getListType(String)}
	 */
	public void testGetListType() {
		assertListType("-", GenerateNodeHelper.LIST_TYPE_NORMAL);
		assertListType("--", GenerateNodeHelper.LIST_TYPE_NORMAL);
		assertListType("---", GenerateNodeHelper.LIST_TYPE_NORMAL);
		assertListType("+", GenerateNodeHelper.LIST_TYPE_NUMERICAL);
		assertListType("++", GenerateNodeHelper.LIST_TYPE_NUMERICAL);
		assertListType("+++", GenerateNodeHelper.LIST_TYPE_NUMERICAL);
		assertListType("a", 0);
	}

	/**
	 * {@link GenerateNodeHelper#split(String, String)}
	 */
	public void testSplit() {

		String image = "http://www.isenshi.com/index.html#anchor";

		// 通常に分割
		String[] actual = GenerateNodeHelper.split(image, "#");
		assertEquals("http://www.isenshi.com/index.html", actual[0]);
		assertEquals("anchor", actual[1]);

		// 該当しない場合は配列の二つ目は空
		image = "http://www.isenshi.com/index.html";
		actual = GenerateNodeHelper.split(image, "#");
		assertEquals("http://www.isenshi.com/index.html", actual[0]);
		assertEquals("", actual[1]);

		// 最初の部分で分割される
		actual = GenerateNodeHelper.split(image, "/");
		assertEquals("http:", actual[0]);
		assertEquals("/www.isenshi.com/index.html", actual[1]);
	}

	/**
	 * {@link GenerateNodeHelper#deleteParenthesis(String, String, String)}
	 */
	public void testDeleteParenthesis() {

		String image = "(www.isenshi.com)";

		// 通常の真っ直ぐなパターン
		String actual = GenerateNodeHelper.deleteParenthesis(image, "(", ")");
		assertEquals("www.isenshi.com", actual);
		// 右側は存在しない場合
		actual = GenerateNodeHelper.deleteParenthesis(image, "(", "]");
		assertEquals("www.isenshi.com)", actual);
		// 左側は存在しない場合
		actual = GenerateNodeHelper.deleteParenthesis(image, "[", ")");
		assertEquals("(www.isenshi.com", actual);
		// 右と左が逆
		actual = GenerateNodeHelper.deleteParenthesis(image, ")", "(");
		assertEquals("", actual);
		// 右に指定したものは最後の出現部分できる
		actual = GenerateNodeHelper.deleteParenthesis(image, "(", ".");
		assertEquals("www.isenshi", actual);

		// 異常系 同じ文字列を指定しては駄目
		try {
			GenerateNodeHelper.deleteParenthesis(image, "(", "(");
			fail();
		} catch (Throwable t) {
			// success
		}
	}

	/**
	 * {@link GenerateNodeHelper#splitArgs(String)}
	 */
	public void testSplitArgs() {

		String image = "(a,b,c)";
		String[] actual = GenerateNodeHelper.splitArgs(image);
		assertEquals("a", actual[0]);
		assertEquals("b", actual[1]);
		assertEquals("c", actual[2]);

		image = "(a,&plugin(b,c);,d)";
		actual = GenerateNodeHelper.splitArgs(image);
		assertEquals("a", actual[0]);
		assertEquals("&plugin(b,c);", actual[1]);
		assertEquals("d", actual[2]);

		image = "(a,&plugin(b,c){d,e};,f)";
		actual = GenerateNodeHelper.splitArgs(image);
		assertEquals("a", actual[0]);
		assertEquals("&plugin(b,c){d,e};", actual[1]);
		assertEquals("f", actual[2]);

		image = "(a,&plugin(b,c){d,&plugin(e,f);};,g)";
		actual = GenerateNodeHelper.splitArgs(image);
		assertEquals("a", actual[0]);
		assertEquals("&plugin(b,c){d,&plugin(e,f);};", actual[1]);
		assertEquals("g", actual[2]);
	}

	/**
	 * {@link GenerateNodeHelper#getTableType(String)}
	 */
	public void testGetTableType() {
		
		assertTableType("h", GenerateNodeHelper.TABLE_TYPE_HEADER);
		assertTableType("H", GenerateNodeHelper.TABLE_TYPE_HEADER);
		assertTableType("f", GenerateNodeHelper.TABLE_TYPE_FOOTER);		
		assertTableType("F", GenerateNodeHelper.TABLE_TYPE_FOOTER);				
		assertTableType("a", 0);
	}

	private void assertListType(String image, int expected) {
		int actual = GenerateNodeHelper.getListType(image);
		assertEquals(expected, actual);
	}

	private void assertTableType(String image, int expected){		
		int actual = GenerateNodeHelper.getTableType(image);
		assertEquals(expected,actual);		
	}
	
	
}
