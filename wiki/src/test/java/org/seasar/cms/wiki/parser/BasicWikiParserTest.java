/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.cms.wiki.parser;

import java.io.Reader;

import org.seasar.cms.wiki.factory.WikiParserFactory;
import org.seasar.cms.wiki.util.WikiTestUtils;
import org.seasar.extension.unit.S2TestCase;

/**
 * WikiParser Test class
 * if you want to debug the actitivies of lexer and parser,
 * set
 *   DEBUG_PARSER = true;
 *   DEBUG_TOKEN_MANAGER = true;
 * in Wiki.jjt and re-run jjtree (mvn compile).
 * These settings shows detail information about them.
 * 
 * @author someda
 */
public class BasicWikiParserTest extends S2TestCase{
	
	private static final String PATH = "BasicWikiParserTest.dicon";
	
	private WikiParserFactory wikiParserFactory;	
	
	public BasicWikiParserTest(String name){
		super(name);
	}
	
	protected void setUp(){
		include(PATH);
	}
	
	protected void tearDown(){		
	}
	
	public void testHeading(){
		doTest("heading.txt");
	}

	public void testList(){
		doTest("list.txt");				
	}
	
	public void testTable(){
		doTest("table.txt");
	}
		
	public void testPlugin(){
		doTest("plugin.txt");
	}
	
	public void testParagraph(){
		doTest("paragraph.txt");
	}
	
	public void testExcerpt(){
		doTest("excerpt.txt");
	}
	
	public void testDefineList(){
		doTest("dlist.txt");
	}

	public void testInline(){
		doTest("inline.txt");
	}
	
	public void testError(){
		doTest("error.txt",2);
	}
	
	public void testSentence(){
		doTest("sentence.txt");
	}
	
	private void doTest(String fileName){
		doTest(fileName,0);
	}	
	
	private void doTest(String fileName, int errorNum){
		Reader reader = WikiTestUtils.getFileReader(fileName);
		org.seasar.cms.wiki.engine.WikiParser parser = wikiParserFactory.getWikiParser(reader);
		parser.parse();
		assertEquals(parser.getNParseErrors(),errorNum);		
	}
		
}
