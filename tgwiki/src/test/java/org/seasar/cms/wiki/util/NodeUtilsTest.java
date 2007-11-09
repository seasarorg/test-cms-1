package org.seasar.cms.wiki.util;

import java.util.List;

import junit.framework.TestCase;

import org.seasar.cms.wiki.parser.Node;
import org.seasar.cms.wiki.parser.SimpleNode;
import org.seasar.cms.wiki.parser.WikiLetters;
import org.seasar.cms.wiki.parser.WikiStrongItalic;
import org.seasar.cms.wiki.parser.WikiTable;

/**
 * {@link NodeUtils}
 * 
 * @author someda
 */
public class NodeUtilsTest extends TestCase {

	/**
	 * {@link NodeUtils#find(Node, Class)}
	 * 
	 */
	public <E> void testFind() {
		SimpleNode node = new SimpleNode(1);
		node.jjtAddChild(new WikiLetters(2), 0);
		node.jjtAddChild(new WikiLetters(3), 1);
		node.jjtAddChild(new WikiStrongItalic(4), 2);

		// 親クラスである SimpleNode は全てに合致する
		assertFind(node, SimpleNode.class, 3);
		assertFind(node, WikiLetters.class, 2);
		assertFind(node, WikiStrongItalic.class, 1);
		assertFind(node, WikiTable.class, 0);
	}

	private <E> void assertFind(Node node, Class<E> clazz, int expected) {
		List<E> actual = NodeUtils.find(node, clazz);
		assertEquals(expected, actual.size());
	}

	/**
	 * {@link NodeUtils#parent(Node, Class)}
	 */
	public void testParent() {

		SimpleNode parent = new SimpleNode(0);
		SimpleNode node = new SimpleNode(1);
		node.jjtSetParent(parent);

		SimpleNode actual = NodeUtils.parent(node, SimpleNode.class);
		assertEquals(parent, actual);

		actual = NodeUtils.parent(node, WikiLetters.class);
		assertNull(actual);

		node.jjtSetParent(null);
		actual = NodeUtils.parent(node, WikiLetters.class);
		assertNull(actual);
	}

}
