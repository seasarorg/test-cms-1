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
package org.seasar.cms.wiki.engine.impl;

import junit.framework.TestCase;

public class WikiEngineImplWithoutSeasar2Test extends TestCase {

	public void testGetInstance() {
		String result = WikiEngineImpl.getInstance().evaluate("*a");
		assertEquals("<h2>a</h2>", result);
	}

	public void testPlugin() {
		assertEquals("<p><br/></p>", WikiEngineImpl.getInstance().evaluate(
				"&br;"));
	}

}
