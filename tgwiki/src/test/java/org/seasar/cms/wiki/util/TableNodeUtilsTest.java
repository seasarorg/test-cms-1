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

import java.util.Map;

import junit.framework.TestCase;

import org.seasar.cms.wiki.parser.WikiTablecolumn;

/**
 * {@link TableNodeUtils}
 * @author someda
 */
public class TableNodeUtilsTest extends TestCase {

	/**
	 * {@link TableNodeUtils#getTdAttributes(WikiTablecolumn)}
	 */
	public void testGetTdAttributes() {

		WikiTablecolumn node = new WikiTablecolumn(0);

		// 最初は何もなし
		Map<String, String> actual = TableNodeUtils.getTdAttributes(node);
		assertEquals(0, actual.size());

		// colspannum が 1 の場合
		node.colspannum = 1;
		actual = TableNodeUtils.getTdAttributes(node);
		assertEquals(1,actual.size());
		assertEquals("2",actual.get("colspan"));

		// rowspannum が 2 の場合
		node.rowspannum = 2;
		actual = TableNodeUtils.getTdAttributes(node);
		assertEquals(2,actual.size());
		assertEquals("3",actual.get("rowspan"));
		
		// align がついている場合 (style は順番が決まる)
		node.align = "left";
		actual = TableNodeUtils.getTdAttributes(node);
		assertEquals(3,actual.size());
		assertEquals("text-align:left;",actual.get("style"));

		// size がついている場合
		node.size = "12px";
		actual = TableNodeUtils.getTdAttributes(node);
		assertEquals(3,actual.size());
		assertEquals("text-align:left;font-size:12px;",actual.get("style"));

	}

}
