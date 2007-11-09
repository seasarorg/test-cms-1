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
