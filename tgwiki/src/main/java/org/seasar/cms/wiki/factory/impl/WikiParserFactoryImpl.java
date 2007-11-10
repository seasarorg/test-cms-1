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
package org.seasar.cms.wiki.factory.impl;

import java.io.Reader;

import org.seasar.cms.wiki.engine.WikiParser;
import org.seasar.cms.wiki.factory.WikiParserFactory;
import org.seasar.cms.wiki.parser.WikiCharStream;
import org.seasar.cms.wiki.parser.BasicWikiParser;

/**
 * @author someda
 */
public class WikiParserFactoryImpl implements WikiParserFactory {

	public WikiParser getWikiParser(Reader reader) {		
		WikiCharStream stream = new WikiCharStream(reader);		
		WikiParser parser = new BasicWikiParser(stream);		
		return parser;
	}

}
