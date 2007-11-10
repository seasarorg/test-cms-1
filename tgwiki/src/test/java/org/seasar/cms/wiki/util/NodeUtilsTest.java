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
