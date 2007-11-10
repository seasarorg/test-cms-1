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

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.wiki.parser.Node;

/**
 * ノード関連の処理を行う。 JavaCC 共通で使えるユーティリティ.
 * 
 * @author nishioka
 */
public class NodeUtils {

	private NodeUtils(){		
	}
	
	/**
	 * 与えられたノードの子要素で、引数のクラスに該当するもののみを
	 * リストに追加して返す
	 * @param <E>
	 * @param node
	 * @param clazz
	 * @return
	 */
	public static <E> List<E> find(Node node, Class<E> clazz) {
		List<E> list = new ArrayList<E>();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {			
			Node child = node.jjtGetChild(i);			
			if (clazz.isInstance(child)) {
				list.add(clazz.cast(child));
			}
		}
		return list;
	}
	
	/**
	 * 親が指定されたクラスである場合は、その親のクラスにキャストして返す
	 * そうでない場合には null を返す
	 * 
	 * @param <E>
	 * @param node
	 * @param clazz
	 * @return
	 */
	public static <E extends Node> E parent(Node node, Class<E> clazz) {

		Node parent = node.jjtGetParent();
		if (parent == null) {
			return null;
		}
		if (clazz.isInstance(parent)) {
			return clazz.cast(parent);
		}
		return null;
	}

}
