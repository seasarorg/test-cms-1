package org.seasar.cms.wiki.util;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.wiki.parser.Node;

/**
 * ノード関連の処理を行う。 JavaCC 共通で使えるユーティリティ.
 * 
 * @author nishioka
 */
public class NodeUtils {

	@SuppressWarnings("unchecked")
	public static <E> List<E> find(Node node, Class<E> clazz) {
		List<E> list = new ArrayList<E>();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (clazz.isInstance(node.jjtGetChild(i))) {
				list.add((E) node.jjtGetChild(i));
			}
		}
		return list;
	}
}
